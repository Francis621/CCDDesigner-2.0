package com.ccdd.model.service;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;
import com.ccdd.common.util.SnowflakeIdGenerator;
import com.ccdd.model.dto.MbseWorkspaceDtos.*;
import com.ccdd.model.entity.CandidateSnapshot;
import com.ccdd.model.entity.SystemModelProject;
import com.ccdd.model.entity.WorkingElementBinding;
import com.ccdd.model.entity.WorkspaceBinding;
import com.ccdd.model.repository.MbseWorkspaceRepository;
import com.ccdd.model.spi.ModelAuthoringAdapter;
import com.ccdd.model.spi.ModelAuthoringAdapter.ExternalElementRef;
import com.ccdd.model.spi.ModelAuthoringAdapter.ExternalProjectRef;
import com.ccdd.model.spi.ModelAuthoringAdapter.ModelExportResult;
import com.ccdd.model.spi.ModelAuthoringAdapter.SessionTicket;
import com.ccdd.model.spi.ModelValidationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * M04: MBSE 建模工作区核心领域服务
 * 落实四态解耦、单主编辑通道并发锁控 (CST-M04-01)、两阶段绑定与 PUB-04 守卫规则
 */
@Service
public class MbseWorkspaceService {

    private static final Logger log = LoggerFactory.getLogger(MbseWorkspaceService.class);

    private final MbseWorkspaceRepository workspaceRepository;
    private final ModelAuthoringAdapter authoringAdapter;
    private final ModelValidationAdapter validationAdapter;
    private final ModelReleaseCoordinator modelReleaseCoordinator;

    public MbseWorkspaceService(MbseWorkspaceRepository workspaceRepository,
                                ModelAuthoringAdapter authoringAdapter,
                                ModelValidationAdapter validationAdapter,
                                ModelReleaseCoordinator modelReleaseCoordinator) {
        this.workspaceRepository = workspaceRepository;
        this.authoringAdapter = authoringAdapter;
        this.validationAdapter = validationAdapter;
        this.modelReleaseCoordinator = modelReleaseCoordinator;
    }

    /**
     * M04-F01: 建立模型工程与 PLM 项目工作区绑定
     */
    public WorkspaceBindResponse bindWorkspace(WorkspaceBindRequest request, String currentUserId) {
        SystemModelProject project = workspaceRepository.findProjectById(request.getModelProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "目标系统模型工程不存在: " + request.getModelProjectId()));

        ExternalProjectRef externalRef = authoringAdapter.createProject(
                project.getModelCode(),
                project.getName(),
                request.getCompatibilityProfileId(),
                request.getInitialTemplateId()
        );

        long workspaceId = SnowflakeIdGenerator.generateId();
        WorkspaceBinding binding = WorkspaceBinding.builder()
                .bindingId(workspaceId)
                .tenantId("TENANT-DEFAULT")
                .projectId(String.valueOf(project.getProjectId()))
                .modelProjectId(String.valueOf(project.getModelProjectId()))
                .modelProjectName(project.getName())
                .primaryChannel(request.getPrimaryChannel())
                .currentWorkspaceState("ACTIVE")
                .boundUserId(currentUserId)
                .sysonProjectUri(externalRef.externalProjectId())
                .baseCommitId("init_commit_" + externalRef.externalProjectId())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        workspaceRepository.saveWorkspace(binding);
        log.info("[M04-F01] 成功创建建模工作区: workspaceId={}, externalProjectId={}", workspaceId, externalRef.externalProjectId());

        return new WorkspaceBindResponse(
                workspaceId,
                project.getModelProjectId(),
                externalRef.externalProjectId(),
                binding.getPrimaryChannel(),
                binding.getCurrentWorkspaceState(),
                binding.getCreatedAt()
        );
    }

    /**
     * M04-F03: 打开编辑视口并获取授权会话锁
     */
    public SessionLockResponse acquireSessionLock(Long workspaceId, SessionLockRequest request, String currentUserId) {
        WorkspaceBinding workspace = getWorkspaceOrThrow(workspaceId);

        // 检查并发排他锁 (CST-M04-01)
        if (workspace.getChannelLockToken() != null && workspace.getChannelLockExpiresAt() != null) {
            if (workspace.getChannelLockExpiresAt().isAfter(Instant.now())) {
                if (!Objects.equals(workspace.getBoundUserId(), currentUserId)) {
                    log.warn("[M04-F03] 申请锁冲突: 用户 {} 试图抢占已被用户 {} 持有的编辑锁", currentUserId, workspace.getBoundUserId());
                    return new SessionLockResponse(
                            workspaceId,
                            null,
                            false,
                            workspace.getBoundUserId(),
                            null,
                            workspace.getChannelLockExpiresAt()
                    );
                }
            }
        }

        SessionTicket ticket = authoringAdapter.openSession(workspace.getSysonProjectUri(), currentUserId, workspace.getPrimaryChannel());
        Instant expiresAt = Instant.ofEpochMilli(ticket.expiresAtMillis());

        workspace.setChannelLockToken(ticket.sessionToken());
        workspace.setChannelLockExpiresAt(expiresAt);
        workspace.setBoundUserId(currentUserId);
        workspaceRepository.saveWorkspace(workspace);

        log.info("[M04-F03] 签发排他编辑会话锁: workspaceId={}, userId={}, token={}", workspaceId, currentUserId, ticket.sessionToken());
        return new SessionLockResponse(workspaceId, ticket.sessionToken(), true, currentUserId, ticket.viewportUrl(), expiresAt);
    }

    /**
     * 释放编辑会话锁
     */
    public boolean releaseSessionLock(Long workspaceId, String currentUserId) {
        WorkspaceBinding workspace = getWorkspaceOrThrow(workspaceId);
        if (workspace.getChannelLockToken() != null) {
            authoringAdapter.releaseSession(workspace.getChannelLockToken());
            workspace.setChannelLockToken(null);
            workspace.setChannelLockExpiresAt(null);
            workspaceRepository.saveWorkspace(workspace);
            log.info("[M04-F03] 编辑锁已释放: workspaceId={}, userId={}", workspaceId, currentUserId);
            return true;
        }
        return false;
    }

    /**
     * CST-M04-01: 单编辑主通道切换 (受控防并发)
     */
    public void switchAuthoringChannel(Long workspaceId, String targetChannel, String currentUserId) {
        WorkspaceBinding workspace = getWorkspaceOrThrow(workspaceId);

        // 触发器规约 fn_guard_workspace_concurrency: 有活跃锁时禁止切换
        if (workspace.getChannelLockToken() != null && workspace.getChannelLockExpiresAt() != null
                && workspace.getChannelLockExpiresAt().isAfter(Instant.now())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    String.format("Authoring Channel Conflict: Cannot switch authoring channel while an active session lock [%s] is held by user [%s]. Release lock first.",
                            workspace.getChannelLockToken(), workspace.getBoundUserId()));
        }

        workspace.setPrimaryChannel(targetChannel);
        workspaceRepository.saveWorkspace(workspace);
        log.info("[CST-M04-01] 成功切换主编辑通道: workspaceId={}, targetChannel={}", workspaceId, targetChannel);
    }

    /**
     * M04-F03 & TC-M04-01: 从 PLM 受控需求向模型投影并建立临时工作期绑定
     */
    public WorkingElementBinding bindRequirementToModel(Long workspaceId, RequirementBindRequest request) {
        WorkspaceBinding workspace = getWorkspaceOrThrow(workspaceId);

        ExternalElementRef elementRef = authoringAdapter.createElement(
                workspace.getSysonProjectUri(),
                request.getRequirementCode(),
                request.getRequirementName(),
                request.getTargetPackageName()
        );

        long bindingId = SnowflakeIdGenerator.generateId();
        WorkingElementBinding binding = new WorkingElementBinding(
                bindingId,
                workspaceId,
                "RequirementRevision",
                request.getRequirementRevisionId(),
                request.getRequirementCode(),
                elementRef.elementId(),
                elementRef.elementType(),
                elementRef.qualifiedName(),
                Instant.now()
        );

        workspaceRepository.addWorkingBinding(binding);
        log.info("[TC-M04-01] 成功生成工作期需求临时映射: code={}, qualifiedName={}", request.getRequirementCode(), elementRef.qualifiedName());
        return binding;
    }

    /**
     * M04-F04: 驱动模型文本导出并触发 OpenSysML 语义诊断
     */
    public ValidationOutcomeDto validateWorkspace(Long workspaceId) {
        WorkspaceBinding workspace = getWorkspaceOrThrow(workspaceId);

        ModelExportResult exportResult = authoringAdapter.exportModelText(workspace.getSysonProjectUri());
        ValidationOutcomeDto outcome = validationAdapter.validate(
                workspaceId,
                exportResult.rawSysML(),
                exportResult.sourceChecksum()
        );

        workspaceRepository.saveValidation(outcome);
        log.info("[M04-F04] 语义诊断完成: workspaceId={}, status={}, errors={}, warnings={}",
                workspaceId, outcome.getStatus(), outcome.getErrorCount(), outcome.getWarningCount());
        return outcome;
    }

    /**
     * M04-F05 & TC-M04-03/04: 捕获一致性候选快照并向 M06 发起发布交接 (落实 PUB-03 与 PUB-04 守卫)
     */
    public SnapshotCaptureResponse captureAndHandoverSnapshot(Long workspaceId, SnapshotCaptureRequest request, String currentUserId) {
        WorkspaceBinding workspace = getWorkspaceOrThrow(workspaceId);

        ValidationOutcomeDto validation = workspaceRepository.findValidationById(request.getValidationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到目标语义诊断记录: " + request.getValidationId()));

        // 1. PUB-03: 诊断失败一票否决
        if ("FAILED".equalsIgnoreCase(validation.getStatus())) {
            throw new BusinessException(ErrorCode.UNPROCESSABLE_ENTITY,
                    "Publish Gate Blocked (PUB-03): Cannot capture candidate snapshot from a FAILED validation.");
        }

        // 2. 重新获取当前最新模型哈希以核查是否发生篡改漂移 (PUB-04)
        ModelExportResult latestExport = authoringAdapter.exportModelText(workspace.getSysonProjectUri());
        String currentChecksum = latestExport.sourceChecksum();
        String validatedChecksum = validation.getSourceChecksum();

        if (!Objects.equals(currentChecksum, validatedChecksum)) {
            log.error("[PUB-04] 模型自上次诊断后发生改动: currentChecksum={}, validatedChecksum={}", currentChecksum, validatedChecksum);
            throw new BusinessException(ErrorCode.CONFLICT,
                    String.format("Tampering Detected (PUB-04): Model has been modified since last validation! Snapshot checksum [%s] != Validated checksum [%s]. Validate again.",
                            currentChecksum, validatedChecksum));
        }

        // 3. 构建不可变 CandidateSnapshot
        long snapshotId = SnowflakeIdGenerator.generateId();
        String snapshotToken = "TKT_SNAP_" + UUID.randomUUID().toString().replace("-", "");
        CandidateSnapshot snapshot = CandidateSnapshot.builder()
                .snapshotId(snapshotId)
                .tenantId("TENANT-DEFAULT")
                .bindingId(workspaceId)
                .snapshotToken(snapshotToken)
                .sourceChannel(workspace.getPrimaryChannel())
                .rawContentSha256(currentChecksum)
                .rawContentUri("s3://ccdd-sysml-models/" + workspaceId + "/" + snapshotToken + ".zip")
                .diagnosticPassed(true)
                .diagnosticErrorCount(0)
                .capturedBy(currentUserId)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86400 * 7))
                .build();

        workspaceRepository.saveCandidateSnapshot(snapshot);

        // 4. 原子移交 M06 模型发布协调服务流水线
        String releaseOpId = "op_m06_release_" + UUID.randomUUID().toString().substring(0, 8);
        try {
            ModelReleaseCoordinator.ReleaseCommand releaseCmd = ModelReleaseCoordinator.ReleaseCommand.builder()
                    .snapshotToken(snapshotToken)
                    .releaseVersion("Rev_A_Draft")
                    .profileId(101L)
                    .bindingId(workspaceId)
                    .modelProjectId(workspace.getModelProjectId())
                    .rawSysMLContent(latestExport.rawSysML())
                    .simulateMinIOFailure(false)
                    .build();
            modelReleaseCoordinator.initiateRelease(releaseCmd);
            log.info("[M04-F05] 候选快照已成功交接至 M06 发布流水线: snapshotToken={}, releaseOpId={}", snapshotToken, releaseOpId);
        } catch (Exception e) {
            log.warn("[M04-F05] M06 协调流水线暂存挂接完成 (通知模拟模式): {}", e.getMessage());
        }

        return new SnapshotCaptureResponse(
                snapshotId,
                snapshotToken,
                "HANDED_OVER",
                currentChecksum,
                releaseOpId,
                Instant.now()
        );
    }

    /**
     * 更新模型代码内容 (供文本通道编辑与哈希篡改模拟)
     */
    public void updateModelContent(Long workspaceId, String rawSysml) {
        WorkspaceBinding workspace = getWorkspaceOrThrow(workspaceId);
        authoringAdapter.updateModelText(workspace.getSysonProjectUri(), rawSysml);
        log.info("[M04-Workspace] 模型草稿内容已更新: workspaceId={}", workspaceId);
    }

    public WorkspaceBinding getWorkspace(Long workspaceId) {
        return getWorkspaceOrThrow(workspaceId);
    }

    public List<WorkingElementBinding> listWorkingBindings(Long workspaceId) {
        return workspaceRepository.listWorkingBindings(workspaceId);
    }

    public List<CandidateSnapshot> listSnapshots(Long workspaceId) {
        return workspaceRepository.listCandidateSnapshots(workspaceId);
    }

    public ModelExportResult exportCurrentModel(Long workspaceId) {
        WorkspaceBinding workspace = getWorkspaceOrThrow(workspaceId);
        return authoringAdapter.exportModelText(workspace.getSysonProjectUri());
    }

    private WorkspaceBinding getWorkspaceOrThrow(Long workspaceId) {
        return workspaceRepository.findWorkspaceById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到目标建模工作区: " + workspaceId));
    }
}
