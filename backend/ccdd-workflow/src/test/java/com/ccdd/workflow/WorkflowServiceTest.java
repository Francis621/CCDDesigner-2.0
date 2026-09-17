package com.ccdd.workflow;

import com.ccdd.workflow.crypto.DigitalSignatureService;
import com.ccdd.workflow.dto.CompleteTaskRequest;
import com.ccdd.workflow.dto.CompleteTaskResponse;
import com.ccdd.workflow.dto.ConsumeDecisionRequest;
import com.ccdd.workflow.dto.ConsumeDecisionResponse;
import com.ccdd.workflow.dto.StartWorkflowRequest;
import com.ccdd.workflow.dto.StartWorkflowResponse;
import com.ccdd.workflow.dto.WorkflowTaskItemDto;
import com.ccdd.workflow.entity.ApprovalConclusion;
import com.ccdd.workflow.entity.ApprovalDecisionEntity;
import com.ccdd.workflow.entity.InstanceStatus;
import com.ccdd.workflow.entity.WorkflowInstanceEntity;
import com.ccdd.workflow.exception.CrossMutationForbiddenException;
import com.ccdd.workflow.exception.HashTamperingDetectedException;
import com.ccdd.workflow.exception.SelfApprovalBlockedException;
import com.ccdd.workflow.repository.WorkflowRepository;
import com.ccdd.workflow.service.WorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * M24: 工作流与工程审批全要素验收测试套件
 * 严格覆盖 TC-M24-01 至 TC-M24-06 验收标准
 */
class WorkflowServiceTest {

    private WorkflowRepository repository;
    private DigitalSignatureService signatureService;
    private WorkflowService service;

    @BeforeEach
    void setUp() {
        repository = new WorkflowRepository();
        signatureService = new DigitalSignatureService();
        // 单元测试中传入 null 作为 RuntimeService 和 TaskService，使用内存流程调度器
        service = new WorkflowService(repository, signatureService, null, null);
    }

    @Test
    @DisplayName("TC-M24-01: 架构安全防御 CST-M24-01 验证 (工作流服务严禁直接修改业务主表状态)")
    void testTC_M24_01_PreventDirectBusinessMutation() {
        // 模拟外部或流程组件尝试直接对业务主表执行 UPDATE
        CrossMutationForbiddenException ex = assertThrows(CrossMutationForbiddenException.class, () ->
                repository.attemptDirectMutationOfBusinessTable("plm_product.part_revision")
        );
        assertTrue(ex.getMessage().contains("CST-M24-01"));
        assertTrue(ex.getMessage().contains("strictly PROHIBITED"));
    }

    @Test
    @DisplayName("TC-M24-02: 快照防篡改熔断 AT-16 验证 (审批中内容哈希漂移即时熔断终止流程)")
    void testTC_M24_02_ContentHashTamperingDetected() {
        // 1. 提交零件发布申请 (Hash A)
        String hashA = "a1b2c3d4e5f60718293a4b5c6d7e8f901234567890abcdef1234567890abcdef";
        StartWorkflowRequest startReq = new StartWorkflowRequest();
        startReq.setTargetObjectType("ModelRelease");
        startReq.setTargetObjectId(9001L);
        startReq.setTargetBusinessCode("REL-VMC1000-TEST");
        startReq.setTargetContentHash(hashA);
        startReq.setBusinessCategory("STANDARD_RELEASE");

        StartWorkflowResponse startResp = service.startWorkflow(startReq, "chief_designer");
        assertNotNull(startResp);
        assertEquals(InstanceStatus.RUNNING, startResp.getStatus());

        // 获取首个生成的待办任务
        List<WorkflowTaskItemDto> tasks = service.getPendingTasksForUser("lead_analyst");
        WorkflowTaskItemDto targetTask = tasks.stream()
                .filter(t -> t.getWorkflowInstId().equals(startResp.getWorkflowInstId()))
                .findFirst().orElseThrow();

        // 2. 模拟审批流处于会签中，外部工程师强制更新实体内容导致哈希漂移为 Hash B
        String hashB = "f9e8d7c6b5a4039281726354453627182910fedcba9876543210fedcba987654";
        repository.setLiveTargetEntityHash("ModelRelease", 9001L, hashB);

        // 3. 审批人点击“同意”完成流程 -> 探测到篡改，强行熔断
        CompleteTaskRequest completeReq = new CompleteTaskRequest();
        completeReq.setAction("APPROVE");
        completeReq.setComment("技术核查通过");

        HashTamperingDetectedException ex = assertThrows(HashTamperingDetectedException.class, () ->
                service.completeTask(targetTask.getTaskId(), completeReq, "lead_analyst")
        );
        assertTrue(ex.getMessage().contains("AT-16"));
        assertTrue(ex.getMessage().contains("实时哈希"));

        // 验证流程实例已被强行标记为 TERMINATED
        WorkflowInstanceEntity inst = repository.findInstanceById(startResp.getWorkflowInstId()).orElseThrow();
        assertEquals(InstanceStatus.TERMINATED, inst.getStatus());
        assertTrue(inst.getTerminationReason().contains("Security Tampering Detected"));
    }

    @Test
    @DisplayName("TC-M24-03: SoD-01 职责分离运行时守卫 (流程发起人严禁自审自身提交的申请)")
    void testTC_M24_03_SoDSelfApprovalProhibited() {
        // 工程师 chief_designer 提交工程变更单 ECO-2026-0042
        StartWorkflowRequest startReq = new StartWorkflowRequest();
        startReq.setTargetObjectType("ChangeOrder");
        startReq.setTargetObjectId(8002L);
        startReq.setTargetBusinessCode("ECO-2026-0042-TEST");
        startReq.setTargetContentHash("11223344556677889900aabbccddeeff11223344556677889900aabbccddeeff");
        startReq.setBusinessCategory("MAJOR_CHANGE");

        StartWorkflowResponse startResp = service.startWorkflow(startReq, "chief_designer");

        // 查询该任务，并尝试让发起人 chief_designer 自行审批
        List<WorkflowTaskItemDto> tasks = service.getPendingTasksForUser("chief_designer");
        WorkflowTaskItemDto targetTask = tasks.stream()
                .filter(t -> t.getWorkflowInstId().equals(startResp.getWorkflowInstId()))
                .findFirst().orElseThrow();

        // 验证前端标记为自审受限
        assertTrue(targetTask.getIsSelfApprovalRestricted(), "发起人查看自身待办时必须标记为受限");

        // 执行审批，后端硬拦截
        CompleteTaskRequest completeReq = new CompleteTaskRequest();
        completeReq.setAction("APPROVE");
        completeReq.setComment("自己批准自己的变更");

        SelfApprovalBlockedException ex = assertThrows(SelfApprovalBlockedException.class, () ->
                service.completeTask(targetTask.getTaskId(), completeReq, "chief_designer")
        );
        assertTrue(ex.getMessage().contains("SoD-01"));
        assertTrue(ex.getMessage().contains("禁止自发自批"));
    }

    @Test
    @DisplayName("TC-M24-04: M24-F04 / AT-30 业务回调幂等防重测试 (连续快速调用10次相同actionId仅核销1次)")
    void testTC_M24_04_IdempotentCallbackCoordination() {
        // 种子数据中 88001L 是一份已经生成好的决议凭据
        // 我们新创建一个已生成决议凭据但尚未核销的流程
        String hash = "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01";
        Long ticketId = 99001L;
        java.time.Instant now = java.time.Instant.now();
        String payload = String.format("INST:%d|OBJ:%s:%d|HASH:%s|OUTCOME:%s|TIME:%d",
                77099L, "ModelRelease", 5002L, hash, "APPROVED", now.toEpochMilli());
        ApprovalDecisionEntity decision = new ApprovalDecisionEntity(
                ticketId, 77099L, "ModelRelease", 5002L, hash,
                ApprovalConclusion.APPROVED, false, null, null,
                signatureService.signWithPlatformKey(payload),
                signatureService.sha256(payload),
                now
        );
        repository.saveDecision(decision);

        ConsumeDecisionRequest consumeReq = new ConsumeDecisionRequest();
        consumeReq.setActionId("CONSUME-ACT-STORM-001");
        consumeReq.setExpectedContentHash(hash);

        // 模拟网络风暴：连续调用 10 次
        for (int i = 0; i < 10; i++) {
            ConsumeDecisionResponse resp = service.consumeDecision(ticketId, consumeReq);
            assertNotNull(resp);
            assertTrue(resp.getIsConsumed());
            assertTrue(resp.getVerified());
            assertEquals(ApprovalConclusion.APPROVED, resp.getFinalConclusion());
        }

        // 验证最终凭证状态为已消费
        ApprovalDecisionEntity verifiedDec = repository.findDecisionById(ticketId).orElseThrow();
        assertTrue(verifiedDec.getIsConsumed());
        assertEquals("CONSUME-ACT-STORM-001", verifiedDec.getConsumedByAction());
    }

    @Test
    @DisplayName("TC-M24-05: M24-F02 会签表决逻辑验证 (任一专家驳回即刻达成 REJECTED 决议)")
    void testTC_M24_05_UnanimousRejectionOutcome() {
        String hash = "99887766554433221100ffeeddccbbaa99887766554433221100ffeeddccbbaa";
        StartWorkflowRequest startReq = new StartWorkflowRequest();
        startReq.setTargetObjectType("ModelRelease");
        startReq.setTargetObjectId(9005L);
        startReq.setTargetContentHash(hash);
        startReq.setBusinessCategory("STANDARD_RELEASE");

        StartWorkflowResponse startResp = service.startWorkflow(startReq, "chief_designer");

        List<WorkflowTaskItemDto> tasks = service.getPendingTasksForUser("lead_analyst");
        WorkflowTaskItemDto targetTask = tasks.stream()
                .filter(t -> t.getWorkflowInstId().equals(startResp.getWorkflowInstId()))
                .findFirst().orElseThrow();

        // 专家签署 REJECT 驳回
        CompleteTaskRequest rejectReq = new CompleteTaskRequest();
        rejectReq.setAction("REJECT");
        rejectReq.setComment("机床伺服进给轴热变形仿真超差，方案不予通过。");

        CompleteTaskResponse completeResp = service.completeTask(targetTask.getTaskId(), rejectReq, "lead_analyst");
        assertNotNull(completeResp);
        assertTrue(completeResp.getIsProcessCompleted());
        assertEquals(ApprovalConclusion.REJECTED, completeResp.getFinalConclusion());

        // 查验最终签发的决议凭据为 REJECTED
        ApprovalDecisionEntity dec = repository.findDecisionById(completeResp.getDecisionTicketId()).orElseThrow();
        assertEquals(ApprovalConclusion.REJECTED, dec.getFinalConclusion());
        assertFalse(dec.getIsConsumed());
    }

    @Test
    @DisplayName("TC-M24-06: AT-04 跨系统凭据核验与电子签名真伪校验")
    void testTC_M24_06_CryptographicSignatureVerification() {
        // 使用种子决议 88001L 进行验签
        ApprovalDecisionEntity dec = repository.findDecisionById(88001L).orElseThrow();
        assertNotNull(dec.getCryptoSignatureStamp());
        assertNotNull(dec.getSignedPayloadDigest());

        // 正常核销：传入匹配哈希
        ConsumeDecisionRequest req = new ConsumeDecisionRequest("ACT-TEST-002", dec.getTargetContentHash());
        ConsumeDecisionResponse resp = service.consumeDecision(88001L, req);
        assertNotNull(resp);
        assertTrue(resp.getIsConsumed());

        // 异常核销：若传入伪造篡改的哈希，触发 AT-16 拦截
        ConsumeDecisionRequest tamperReq = new ConsumeDecisionRequest("ACT-TEST-TAMPER", "tampered_hash_value");
        assertThrows(HashTamperingDetectedException.class, () ->
                service.consumeDecision(88001L, tamperReq)
        );
    }
}
