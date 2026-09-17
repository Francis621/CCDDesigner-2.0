package com.ccdd.model;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;
import com.ccdd.model.client.FlexoClient;
import com.ccdd.model.dto.MbseWorkspaceDtos.*;
import com.ccdd.model.entity.ModelRelease;
import com.ccdd.model.entity.WorkingElementBinding;
import com.ccdd.model.repository.MbseWorkspaceRepository;
import com.ccdd.model.repository.ModelReleaseRepository;
import com.ccdd.model.service.MbseWorkspaceService;
import com.ccdd.model.service.ModelReleaseCoordinator;
import com.ccdd.model.spi.impl.OpenSysMLAdapterImpl;
import com.ccdd.model.spi.impl.SysONAdapterImpl;
import com.ccdd.model.validator.CompatibilityProfileValidator;
import com.ccdd.outbox.service.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * M04 MBSE 建模工作区核心验收测试集 (覆盖 TC-M04-01 ~ TC-M04-06)
 * 纯原生 Java 17 实现，无外部 Mock 代理，跨版本 JVM 稳定可靠
 */
class MbseWorkspaceServiceTest {

    private MbseWorkspaceRepository workspaceRepository;
    private SysONAdapterImpl sysONAdapter;
    private OpenSysMLAdapterImpl openSysMLAdapter;
    private ModelReleaseCoordinator releaseCoordinator;
    private MbseWorkspaceService workspaceService;

    private static final Long DEFAULT_WORKSPACE_ID = 801928410290182L;

    @BeforeEach
    void setUp() {
        workspaceRepository = new MbseWorkspaceRepository();
        sysONAdapter = new SysONAdapterImpl();
        openSysMLAdapter = new OpenSysMLAdapterImpl();

        ModelReleaseRepository releaseRepo = new ModelReleaseRepository(null) {
            @Override
            public void insert(ModelRelease release) {}
            @Override
            public void updateExecutionState(Long releaseId, String executionState, String failedStep, String failedReason) {}
            @Override
            public void activateRelease(Long releaseId, String approverUserId) {}
        };
        CompatibilityProfileValidator validator = new CompatibilityProfileValidator();
        FlexoClient flexoClient = new FlexoClient();
        OutboxService outboxService = new OutboxService(null) {
            @Override
            public void publishEvent(String eventType, String aggregateType, String aggregateId, Long aggregateVersion, Object payload) {}
        };

        releaseCoordinator = new ModelReleaseCoordinator(releaseRepo, validator, flexoClient, outboxService);
        workspaceService = new MbseWorkspaceService(workspaceRepository, sysONAdapter, openSysMLAdapter, releaseCoordinator);
    }

    @Test
    @DisplayName("TC-M04-01: 受控需求投影生成 RequirementUsage 并记录临时工作绑定")
    void testRequirementProjectionBinding() {
        RequirementBindRequest req = new RequirementBindRequest();
        req.setRequirementRevisionId(3002L);
        req.setRequirementCode("REQ-SPINDLE-002");
        req.setRequirementName("主轴最高转速不低于 18000rpm");
        req.setTargetPackageName("01_Requirements");

        WorkingElementBinding binding = workspaceService.bindRequirementToModel(DEFAULT_WORKSPACE_ID, req);

        assertNotNull(binding);
        assertEquals("REQ-SPINDLE-002", binding.getPlmObjectCode());
        assertEquals("RequirementUsage", binding.getElementType());
        assertTrue(binding.getQualifiedName().contains("REQ-SPINDLE-002"));

        var bindings = workspaceService.listWorkingBindings(DEFAULT_WORKSPACE_ID);
        assertTrue(bindings.stream().anyMatch(b -> b.getPlmObjectCode().equals("REQ-SPINDLE-002")));
    }

    @Test
    @DisplayName("TC-M04-02: 语法校验 FAILED 时阻断候选快照生成 (PUB-03 守卫)")
    void testFailedValidationBlocksSnapshot() {
        // 构造语法未解析错误
        workspaceService.updateModelContent(DEFAULT_WORKSPACE_ID, "package InvalidModel { UNRESOLVED syntax_error_mock }");

        ValidationOutcomeDto outcome = workspaceService.validateWorkspace(DEFAULT_WORKSPACE_ID);
        assertEquals("FAILED", outcome.getStatus());
        assertTrue(outcome.getErrorCount() > 0);

        SnapshotCaptureRequest snapReq = new SnapshotCaptureRequest();
        snapReq.setValidationId(outcome.getValidationId());
        snapReq.setExpectedChecksum(outcome.getSourceChecksum());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                workspaceService.captureAndHandoverSnapshot(DEFAULT_WORKSPACE_ID, snapReq, "ENG-MECH-1042"));

        assertTrue(ex.getMessage().contains("PUB-03"));
    }

    @Test
    @DisplayName("TC-M04-03: 正常校验通过后生成 CandidateSnapshot 并原子交接 M06")
    void testSuccessfulValidationAndSnapshotHandover() {
        ValidationOutcomeDto outcome = workspaceService.validateWorkspace(DEFAULT_WORKSPACE_ID);
        assertNotEquals("FAILED", outcome.getStatus());

        SnapshotCaptureRequest snapReq = new SnapshotCaptureRequest();
        snapReq.setValidationId(outcome.getValidationId());
        snapReq.setExpectedChecksum(outcome.getSourceChecksum());
        snapReq.setSnapshotDescription("VMC1000 进给系统架构与需求匹配快照");

        SnapshotCaptureResponse response = workspaceService.captureAndHandoverSnapshot(DEFAULT_WORKSPACE_ID, snapReq, "ENG-MECH-1042");

        assertNotNull(response);
        assertEquals("HANDED_OVER", response.getStatus());
        assertEquals(outcome.getSourceChecksum(), response.getSourceChecksum());
        assertTrue(response.getSnapshotToken().startsWith("TKT_SNAP_"));
    }

    @Test
    @DisplayName("TC-M04-04: 校验后微调模型发生哈希漂移，PUB-04 防篡改守卫物理拦截")
    void testPub04TamperingGuardInterception() {
        // 1. 先执行一次正常校验
        ValidationOutcomeDto outcome = workspaceService.validateWorkspace(DEFAULT_WORKSPACE_ID);
        assertNotEquals("FAILED", outcome.getStatus());

        // 2. 模拟工程师在工作区中微调参数导致哈希漂移
        var export = workspaceService.exportCurrentModel(DEFAULT_WORKSPACE_ID);
        workspaceService.updateModelContent(DEFAULT_WORKSPACE_ID, export.rawSysML() + "\n// modified parameter");

        // 3. 尝试用旧验证记录提交快照
        SnapshotCaptureRequest snapReq = new SnapshotCaptureRequest();
        snapReq.setValidationId(outcome.getValidationId());
        snapReq.setExpectedChecksum(outcome.getSourceChecksum());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                workspaceService.captureAndHandoverSnapshot(DEFAULT_WORKSPACE_ID, snapReq, "ENG-MECH-1042"));

        assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("PUB-04"));
    }

    @Test
    @DisplayName("TC-M04-05: 单主编辑通道排他锁与并发冲突控制 (CST-M04-01)")
    void testChannelConcurrencyAndLockGuard() {
        // 用户 A 申请排他锁
        SessionLockRequest lockReq = new SessionLockRequest();
        SessionLockResponse lockRes = workspaceService.acquireSessionLock(DEFAULT_WORKSPACE_ID, lockReq, "ENG-USER-A");
        assertTrue(lockRes.isLockGranted());
        assertEquals("ENG-USER-A", lockRes.getLockedByUser());

        // 用户 B 尝试获取锁，判定冲突未授予
        SessionLockResponse conflictRes = workspaceService.acquireSessionLock(DEFAULT_WORKSPACE_ID, lockReq, "ENG-USER-B");
        assertFalse(conflictRes.isLockGranted());
        assertEquals("ENG-USER-A", conflictRes.getLockedByUser());

        // 用户 B 试图在持锁状态下强行切换通道，触发冲突拦截
        BusinessException ex = assertThrows(BusinessException.class, () ->
                workspaceService.switchAuthoringChannel(DEFAULT_WORKSPACE_ID, "TEXTUAL", "ENG-USER-B"));
        assertTrue(ex.getMessage().contains("Authoring Channel Conflict"));

        // 用户 A 释放锁
        boolean released = workspaceService.releaseSessionLock(DEFAULT_WORKSPACE_ID, "ENG-USER-A");
        assertTrue(released);

        // 释放后允许切换通道
        assertDoesNotThrow(() -> workspaceService.switchAuthoringChannel(DEFAULT_WORKSPACE_ID, "TEXTUAL", "ENG-USER-A"));
    }

    @Test
    @DisplayName("TC-M04-06: 四态解耦隔离性验证 (Working Model 修改零污染已发布模型)")
    void testWorkingModelStateIsolation() {
        // 在工作区中自由修改草稿
        workspaceService.updateModelContent(DEFAULT_WORKSPACE_ID, "package WorkingDraft { part X; }");
        var export = workspaceService.exportCurrentModel(DEFAULT_WORKSPACE_ID);
        assertTrue(export.rawSysML().contains("WorkingDraft"));

        // 已发布的历史快照列表依然保持独立不可篡改
        var snapshots = workspaceService.listSnapshots(DEFAULT_WORKSPACE_ID);
        for (var s : snapshots) {
            assertNotEquals(export.sourceChecksum(), s.getRawContentSha256());
        }
    }
}
