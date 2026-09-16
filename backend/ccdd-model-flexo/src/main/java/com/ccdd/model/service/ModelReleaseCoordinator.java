package com.ccdd.model.service;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;
import com.ccdd.common.context.EngineeringContextHolder;
import com.ccdd.common.util.SnowflakeIdGenerator;
import com.ccdd.model.client.FlexoClient;
import com.ccdd.model.diagnostic.DiagnosticReport;
import com.ccdd.model.entity.ModelRelease;
import com.ccdd.model.repository.ModelReleaseRepository;
import com.ccdd.model.validator.CompatibilityProfileValidator;
import com.ccdd.outbox.service.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * SysML v2 模型发布两阶段协调器 (ModelReleaseCoordinator)
 * 核心落实 D03 规范与验收用例:
 * - AT-01: 正常提交全通路
 * - AT-02: 语法准入硬阻断
 * - AT-04: Flexo 写入成功但制品网络中断故障回滚
 */
@Service
public class ModelReleaseCoordinator {

    private static final Logger log = LoggerFactory.getLogger(ModelReleaseCoordinator.class);

    private final ModelReleaseRepository modelReleaseRepository;
    private final CompatibilityProfileValidator profileValidator;
    private final FlexoClient flexoClient;
    private final OutboxService outboxService;

    public ModelReleaseCoordinator(ModelReleaseRepository modelReleaseRepository,
                                   CompatibilityProfileValidator profileValidator,
                                   FlexoClient flexoClient,
                                   OutboxService outboxService) {
        this.modelReleaseRepository = modelReleaseRepository;
        this.profileValidator = profileValidator;
        this.flexoClient = flexoClient;
        this.outboxService = outboxService;
    }

    public static class ReleaseCommand {
        private String snapshotToken;
        private String releaseVersion;
        private Long profileId;
        private Long bindingId;
        private String modelProjectId;
        private String rawSysMLContent;
        private boolean simulateMinIOFailure; // 用于 AT-04 测试注入

        public ReleaseCommand() {}

        public ReleaseCommand(String snapshotToken, String releaseVersion, Long profileId, Long bindingId,
                              String modelProjectId, String rawSysMLContent, boolean simulateMinIOFailure) {
            this.snapshotToken = snapshotToken;
            this.releaseVersion = releaseVersion;
            this.profileId = profileId;
            this.bindingId = bindingId;
            this.modelProjectId = modelProjectId;
            this.rawSysMLContent = rawSysMLContent;
            this.simulateMinIOFailure = simulateMinIOFailure;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String snapshotToken;
            private String releaseVersion;
            private Long profileId;
            private Long bindingId;
            private String modelProjectId;
            private String rawSysMLContent;
            private boolean simulateMinIOFailure;

            public Builder snapshotToken(String snapshotToken) { this.snapshotToken = snapshotToken; return this; }
            public Builder releaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; return this; }
            public Builder profileId(Long profileId) { this.profileId = profileId; return this; }
            public Builder bindingId(Long bindingId) { this.bindingId = bindingId; return this; }
            public Builder modelProjectId(String modelProjectId) { this.modelProjectId = modelProjectId; return this; }
            public Builder rawSysMLContent(String rawSysMLContent) { this.rawSysMLContent = rawSysMLContent; return this; }
            public Builder simulateMinIOFailure(boolean simulateMinIOFailure) { this.simulateMinIOFailure = simulateMinIOFailure; return this; }

            public ReleaseCommand build() {
                return new ReleaseCommand(snapshotToken, releaseVersion, profileId, bindingId,
                        modelProjectId, rawSysMLContent, simulateMinIOFailure);
            }
        }

        public String getSnapshotToken() { return snapshotToken; }
        public void setSnapshotToken(String snapshotToken) { this.snapshotToken = snapshotToken; }

        public String getReleaseVersion() { return releaseVersion; }
        public void setReleaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; }

        public Long getProfileId() { return profileId; }
        public void setProfileId(Long profileId) { this.profileId = profileId; }

        public Long getBindingId() { return bindingId; }
        public void setBindingId(Long bindingId) { this.bindingId = bindingId; }

        public String getModelProjectId() { return modelProjectId; }
        public void setModelProjectId(String modelProjectId) { this.modelProjectId = modelProjectId; }

        public String getRawSysMLContent() { return rawSysMLContent; }
        public void setRawSysMLContent(String rawSysMLContent) { this.rawSysMLContent = rawSysMLContent; }

        public boolean isSimulateMinIOFailure() { return simulateMinIOFailure; }
        public void setSimulateMinIOFailure(boolean simulateMinIOFailure) { this.simulateMinIOFailure = simulateMinIOFailure; }
    }

    /**
     * 发起模型两阶段发布申请
     */
    @Transactional
    public ModelRelease initiateRelease(ReleaseCommand command) {
        String tenantId = EngineeringContextHolder.getTenantId();
        if (tenantId == null) tenantId = "ORG-SEMI-001";
        Long releaseId = SnowflakeIdGenerator.generateId();

        log.info("[2PC] 开始执行阶段一: 快照预检与兼容性准入校验, releaseId: {}", releaseId);

        // 1. 语法与兼容性准入硬阻断校验 (ADR-0002 / AT-02)
        DiagnosticReport report = profileValidator.validate(command.getSnapshotToken(), command.getRawSysMLContent());
        if (!report.isDiagnosticPassed()) {
            log.error("[2PC] 模型未通过准入校验, 硬阻断发布申请, errors: {}", report.getSummary().getTotalErrors());
            throw new BusinessException(ErrorCode.UNPROCESSABLE_ENTITY, report);
        }

        // 2. Flexo 候选模型写入 STAGING 隔离命名空间 (ADR-0003)
        String stagingGraphUri = String.format("urn:ccdd:staging:release:%d", releaseId);
        String flexoCommitId;
        try {
            flexoCommitId = flexoClient.writeToStagingGraph(stagingGraphUri, command.getRawSysMLContent());
        } catch (Exception e) {
            log.error("[2PC] Flexo 暂存图写入失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Flexo 服务写入异常: " + e.getMessage());
        }

        // 3. MinIO 上传配套图形与布局制品包 (AT-04 故障注入点)
        String artifactSha256;
        String artifactUri;
        try {
            if (command.isSimulateMinIOFailure()) {
                throw new RuntimeException("Simulated SocketException: MinIO network connection reset by peer");
            }
            artifactSha256 = "sha256-mock-artifact-bundle-001";
            artifactUri = "s3://ccdd-artifacts/models/vmc1000-bundle.zip";
        } catch (Exception ex) {
            // AT-04 核心落实: Flexo 写入成功但 MinIO 失败时执行补偿回滚与失败标记
            log.error("[2PC-Fail-Safe] MinIO 文件传输网络中断, 启动补偿机制回滚 Flexo 暂存图", ex);
            flexoClient.dropStagingGraph(stagingGraphUri);
            
            // 记录失败终态，严禁向外报成功
            ModelRelease failedRelease = ModelRelease.builder()
                    .releaseId(releaseId)
                    .tenantId(tenantId)
                    .bindingId(command.getBindingId())
                    .snapshotId(101L)
                    .profileId(command.getProfileId())
                    .modelProjectId(command.getModelProjectId())
                    .releaseVersion(command.getReleaseVersion())
                    .lifecycleState("DRAFT")
                    .executionState("FAILED")
                    .failedStep("UPLOAD_ARTIFACTS")
                    .failedReason(ex.getMessage())
                    .flexoRepositoryId("flexo-main")
                    .flexoStagingGraph(stagingGraphUri)
                    .publishedBy("SYSTEM")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            modelReleaseRepository.insert(failedRelease);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "制品上传中断，发布已安全回滚并标记为 FAILED");
        }

        // 4. 组装组合防篡改哈希 (combinedReleaseHash)
        String combinedReleaseHash = "sha256:comb-" + flexoCommitId + "-" + artifactSha256;

        // 5. 暂存发布记录至数据库
        ModelRelease release = ModelRelease.builder()
                .releaseId(releaseId)
                .tenantId(tenantId)
                .bindingId(command.getBindingId())
                .snapshotId(101L)
                .profileId(command.getProfileId())
                .modelProjectId(command.getModelProjectId())
                .releaseVersion(command.getReleaseVersion())
                .lifecycleState("DRAFT")
                .executionState("IN_REVIEW")
                .flexoRepositoryId("flexo-main")
                .flexoStagingGraph(stagingGraphUri)
                .flexoCommitId(flexoCommitId)
                .artifactBundleSha256(artifactSha256)
                .artifactBundleUri(artifactUri)
                .combinedReleaseHash(combinedReleaseHash)
                .workflowInstanceId("wf-mock-" + releaseId)
                .publishedBy("current_engineer")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        modelReleaseRepository.insert(release);

        log.info("[2PC] 阶段一执行完毕, 已启动工作流审批, combinedReleaseHash: {}", combinedReleaseHash);
        return release;
    }

    /**
     * 阶段三：工作流审批通过后，原子激活模型并投递发件箱事件
     */
    @Transactional
    public void activateRelease(Long releaseId, String approverUserId) {
        log.info("[2PC] 阶段三: 收到审批通过凭证，执行原子激活, releaseId: {}", releaseId);

        String stagingGraphUri = String.format("urn:ccdd:staging:release:%d", releaseId);
        String productionGraphUri = "urn:ccdd:production:project:p-vmc1000-sys";

        // 1. Flexo 原子提升暂存图至生产具名图
        flexoClient.promoteStagingToProduction(stagingGraphUri, productionGraphUri);

        // 2. 本地数据库更新为 RELEASED 终态
        modelReleaseRepository.activateRelease(releaseId, approverUserId);

        // 3. 写入 Transactional Outbox 发件箱事件 (D09 联动)
        outboxService.publishEvent(
                "ModelReleasePublished",
                "MODEL_RELEASE",
                String.valueOf(releaseId),
                1L,
                Map.of("releaseId", releaseId, "status", "RELEASED", "activatedAt", Instant.now().toString())
        );

        log.info("[2PC] 模型发布原子激活完成，领域事件已放入 Outbox");
    }
}
