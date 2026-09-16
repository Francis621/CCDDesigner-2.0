package com.ccdd.change;

import com.ccdd.change.dto.CloseEcoResponse;
import com.ccdd.change.dto.ConfirmImplementationRequest;
import com.ccdd.change.dto.CreateChangeTaskRequest;
import com.ccdd.change.dto.CreateEcoRequest;
import com.ccdd.change.dto.CreateEcrRequest;
import com.ccdd.change.dto.EcoDetailDto;
import com.ccdd.change.dto.EvaluateImpactRequest;
import com.ccdd.change.dto.ImpactAnalysisResultDto;
import com.ccdd.change.dto.RecordImpactDecisionRequest;
import com.ccdd.change.entity.ChangeOrderEntity;
import com.ccdd.change.entity.ChangeReasonType;
import com.ccdd.change.entity.ChangeRequestEntity;
import com.ccdd.change.entity.EcrStatus;
import com.ccdd.change.entity.EcoStatus;
import com.ccdd.change.entity.ImpactDecisionType;
import com.ccdd.change.exception.EvidenceNotInheritedException;
import com.ccdd.change.exception.FieldExecutionIncompleteException;
import com.ccdd.change.exception.IncompatibleInterchangeabilityException;
import com.ccdd.change.exception.IncompleteImpactAssessmentException;
import com.ccdd.change.repository.ChangeRepository;
import com.ccdd.change.service.ChangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * M22 工程变更与影响处置全要素验收测试
 * 严格覆盖 TC-M22-01 至 TC-M22-06 验收标准
 */
class ChangeServiceTest {

    private ChangeRepository repository;
    private ChangeService service;

    @BeforeEach
    void setUp() {
        repository = new ChangeRepository();
        service = new ChangeService(repository);
    }

    @Test
    @DisplayName("TC-M22-01: 提高主轴额定转速至 15,000 rpm，调用 evaluateImpact 准确推演波及范围 (AT-10)")
    void testTC_M22_01_EvaluateImpactAnalysis() {
        // 创建已批准的 ECR
        CreateEcrRequest ecrReq = new CreateEcrRequest();
        ecrReq.setEcrNumber("ECR-TEST-SPEED-01");
        ecrReq.setTitle("主轴提速至15000rpm变更");
        ecrReq.setReasonType(ChangeReasonType.CUSTOMER_REQUIREMENT);
        ecrReq.setProblemDescription("客户加工铝合金薄壁件需要15000rpm高转速");
        ChangeRequestEntity ecr = service.createEcr(ecrReq);
        service.submitEcr(ecr.getEcrId());
        service.reviewEcr(ecr.getEcrId(), true);

        // 签发 ECO
        CreateEcoRequest ecoReq = new CreateEcoRequest();
        ecoReq.setEcrId(ecr.getEcrId());
        ecoReq.setEcoNumber("ECO-TEST-SPEED-01");
        ecoReq.setTitle("主轴提速工程实施单");
        ChangeOrderEntity eco = service.createEco(ecoReq);

        // 触发影响拓扑分析
        EvaluateImpactRequest impactReq = new EvaluateImpactRequest(5001L, 5, Collections.singletonList("allocatedTo"), 101L);
        ImpactAnalysisResultDto result = service.evaluateImpact(eco.getEcoId(), impactReq);

        assertNotNull(result);
        assertEquals(eco.getEcoId(), result.getEcoId());
        assertFalse(result.getIsTruncated());
        assertTrue(result.getCandidateCount() >= 3);

        // 验证准确识别出轴承(机械)、热平衡(仿真)、电机(电气)
        boolean hasBearing = result.getImpactItems().stream().anyMatch(i -> "M-VMC850-BRG-7014".equals(i.getBusinessCode()) && "MECHANICAL".equals(i.getDiscipline()));
        boolean hasThermal = result.getImpactItems().stream().anyMatch(i -> "TC-SPINDLE-THERMAL".equals(i.getBusinessCode()) && "SIMULATION".equals(i.getDiscipline()));
        boolean hasMotor = result.getImpactItems().stream().anyMatch(i -> "M-VMC1000-MOTOR-15KW".equals(i.getBusinessCode()) && "ELECTRICAL".equals(i.getDiscipline()));

        assertTrue(hasBearing, "候选集中必须包含主轴轴承");
        assertTrue(hasThermal, "候选集中必须包含热平衡仿真用例");
        assertTrue(hasMotor, "候选集中必须包含主轴驱动电机");
    }

    @Test
    @DisplayName("TC-M22-02: ECO处于RELEASED，MES尚未回传返修质检回执，closeECO 触发硬拦截 (AT-22, CST-M22-01)")
    void testTC_M22_02_EcoCloseBlockedWhenFieldReceiptPending() {
        // 使用种子数据中已发布但现场仍有 DISPATCHED 回执的 ECO (ID: 8001L)
        Long ecoId = 8001L;

        // 尝试直接关闭变更单 -> 必须抛出 FieldExecutionIncompleteException
        FieldExecutionIncompleteException ex = assertThrows(FieldExecutionIncompleteException.class, () -> {
            service.closeEco(ecoId);
        });

        assertEquals(ecoId, ex.getEcoId());
        assertTrue(ex.getPendingReceiptCount() > 0, "未闭环回执数必须大于0");
        assertTrue(ex.getMessage().contains("AT-22 守卫"));
    }

    @Test
    @DisplayName("TC-M22-03: 影响候选存在未裁决项时，尝试提交 CCB 审批授权触发拦截 (CST-M22-02)")
    void testTC_M22_03_AuthorizeEcoBlockedWhenImpactAssessmentIncomplete() {
        // 创建 ECR 并批准
        CreateEcrRequest ecrReq = new CreateEcrRequest();
        ecrReq.setEcrNumber("ECR-TEST-INCOMPLETE-01");
        ecrReq.setTitle("待测试影响裁决完备性的 ECR");
        ecrReq.setReasonType(ChangeReasonType.COST_REDUCTION);
        ecrReq.setProblemDescription("降本结构优化");
        ChangeRequestEntity ecr = service.createEcr(ecrReq);
        service.submitEcr(ecr.getEcrId());
        service.reviewEcr(ecr.getEcrId(), true);

        // 签发 ECO 并展开影响候选
        CreateEcoRequest ecoReq = new CreateEcoRequest();
        ecoReq.setEcrId(ecr.getEcrId());
        ecoReq.setEcoNumber("ECO-TEST-INCOMPLETE-01");
        ecoReq.setTitle("待测试影响裁决完备性的 ECO");
        ChangeOrderEntity eco = service.createEco(ecoReq);
        service.evaluateImpact(eco.getEcoId(), new EvaluateImpactRequest(5001L, 3, null, null));

        // 此时有 3 项候选，但 0 项完成裁定录入 -> 尝试 authorizeEco 必须抛出 IncompleteImpactAssessmentException
        IncompleteImpactAssessmentException ex = assertThrows(IncompleteImpactAssessmentException.class, () -> {
            service.authorizeEco(eco.getEcoId(), 9099L);
        });

        assertEquals(eco.getEcoId(), ex.getEcoId());
        assertTrue(ex.getUnassessedCount() > 0);
        assertTrue(ex.getMessage().contains("CST-M22-02"));
    }

    @Test
    @DisplayName("TC-M22-04: 标记为 RE_VERIFY 的用例未提供新仿真依据时，releaseEco 触发证据不继承拦截 (ADR-08)")
    void testTC_M22_04_EvidenceNotInheritedRuleEnforcement() {
        // 创建 ECR 与 ECO
        CreateEcrRequest ecrReq = new CreateEcrRequest();
        ecrReq.setEcrNumber("ECR-TEST-ADR08-01");
        ecrReq.setTitle("验证不继承测试 ECR");
        ecrReq.setReasonType(ChangeReasonType.SIMULATION_DEVIATION);
        ecrReq.setProblemDescription("仿真偏差纠偏");
        ChangeRequestEntity ecr = service.createEcr(ecrReq);
        service.submitEcr(ecr.getEcrId());
        service.reviewEcr(ecr.getEcrId(), true);

        CreateEcoRequest ecoReq = new CreateEcoRequest();
        ecoReq.setEcrId(ecr.getEcrId());
        ecoReq.setEcoNumber("ECO-TEST-ADR08-01");
        ecoReq.setTitle("验证不继承测试 ECO");
        ChangeOrderEntity eco = service.createEco(ecoReq);

        // 推演并对热平衡仿真裁定为 RE_VERIFY
        ImpactAnalysisResultDto analysis = service.evaluateImpact(eco.getEcoId(), new EvaluateImpactRequest(5001L, 2, null, null));
        Long thermalItemId = analysis.getImpactItems().stream()
                .filter(i -> "TC-SPINDLE-THERMAL".equals(i.getBusinessCode()))
                .findFirst().get().getImpactItemId();

        // 录入全部处置
        for (ImpactAnalysisResultDto.ImpactItemSummary item : analysis.getImpactItems()) {
            RecordImpactDecisionRequest decReq = new RecordImpactDecisionRequest();
            decReq.setImpactItemId(item.getImpactItemId());
            if (item.getImpactItemId().equals(thermalItemId)) {
                decReq.setDecisionType(ImpactDecisionType.RE_VERIFY);
                decReq.setTechnicalRationale("工况参数改变必须在新工况下重算");
            } else {
                decReq.setDecisionType(ImpactDecisionType.REVIEW_ONLY);
                decReq.setTechnicalRationale("工程图仅需校核");
            }
            service.recordImpactDecision(eco.getEcoId(), decReq);
        }

        // CCB 授权实施
        service.authorizeEco(eco.getEcoId(), 9001L);

        // 尝试在 release 时不提供新的仿真 Run ID (传入 null 或 0) -> 必须抛出 EvidenceNotInheritedException
        EvidenceNotInheritedException ex = assertThrows(EvidenceNotInheritedException.class, () -> {
            service.releaseEco(eco.getEcoId(), null);
        });

        assertTrue(ex.getMessage().contains("ADR-08"));

        // 传入有效的新工况仿真 Run ID -> 验证通过并进入 RELEASED 状态
        ChangeOrderEntity released = service.releaseEco(eco.getEcoId(), 9901L);
        assertEquals(EcoStatus.RELEASED, released.getStatus());
        assertNotNull(released.getReleasedAt());
    }

    @Test
    @DisplayName("TC-M22-05: 配合尺寸改变违反 FFF 互换性时，尝试原地升版触发拦截 (ADR-05)")
    void testTC_M22_05_IncompatibleInterchangeabilityEnforcement() {
        // 创建 ECR 与 ECO
        CreateEcrRequest ecrReq = new CreateEcrRequest();
        ecrReq.setEcrNumber("ECR-TEST-ADR05-01");
        ecrReq.setTitle("互换性校验测试 ECR");
        ecrReq.setReasonType(ChangeReasonType.CUSTOMER_REQUIREMENT);
        ecrReq.setProblemDescription("更换陶瓷球轴承");
        ChangeRequestEntity ecr = service.createEcr(ecrReq);
        service.submitEcr(ecr.getEcrId());
        service.reviewEcr(ecr.getEcrId(), true);

        CreateEcoRequest ecoReq = new CreateEcoRequest();
        ecoReq.setEcrId(ecr.getEcrId());
        ecoReq.setEcoNumber("ECO-TEST-ADR05-01");
        ecoReq.setTitle("互换性校验测试 ECO");
        ChangeOrderEntity eco = service.createEco(ecoReq);

        // 推演并对轴承裁定为 CREATE_NEW (破坏互换性)
        ImpactAnalysisResultDto analysis = service.evaluateImpact(eco.getEcoId(), new EvaluateImpactRequest(5001L, 2, null, null));
        ImpactAnalysisResultDto.ImpactItemSummary brgItem = analysis.getImpactItems().stream()
                .filter(i -> "M-VMC850-BRG-7014".equals(i.getBusinessCode()))
                .findFirst().get();

        RecordImpactDecisionRequest decReq = new RecordImpactDecisionRequest();
        decReq.setImpactItemId(brgItem.getImpactItemId());
        decReq.setDecisionType(ImpactDecisionType.MODIFY);
        decReq.setTargetActionPlan("CREATE_NEW");
        decReq.setTechnicalRationale("轴承配合公差与内部滚动体结构完全改变，不可双向互换");
        service.recordImpactDecision(eco.getEcoId(), decReq);

        // 录入其他项完成裁定
        for (ImpactAnalysisResultDto.ImpactItemSummary item : analysis.getImpactItems()) {
            if (!item.getImpactItemId().equals(brgItem.getImpactItemId())) {
                RecordImpactDecisionRequest d = new RecordImpactDecisionRequest();
                d.setImpactItemId(item.getImpactItemId());
                d.setDecisionType(ImpactDecisionType.REVIEW_ONLY);
                d.setTechnicalRationale("仅需设计校核");
                service.recordImpactDecision(eco.getEcoId(), d);
            }
        }
        service.authorizeEco(eco.getEcoId(), 9002L);

        // 尝试创建升版原物料的任务 -> 必须抛出 IncompatibleInterchangeabilityException
        CreateChangeTaskRequest taskReq = new CreateChangeTaskRequest();
        taskReq.setTaskCode("TSK-ILLEGAL-01");
        taskReq.setTitle("升版原物料 M-VMC850-BRG-7014 为 Rev B");
        taskReq.setTaskType("CAD_REVISE_ORIGINAL");
        taskReq.setAssigneeId("eng_mech_01");
        taskReq.setSourceRevisionId(brgItem.getCandidateRevisionId());
        taskReq.setEnforceFffCheck(true);

        IncompatibleInterchangeabilityException ex = assertThrows(IncompatibleInterchangeabilityException.class, () -> {
            service.createChangeTask(eco.getEcoId(), taskReq);
        });

        assertTrue(ex.getMessage().contains("ADR-05"));
        assertEquals("M-VMC850-BRG-7014", ex.getPartNumber());
    }

    @Test
    @DisplayName("TC-M22-06: 现场回执 100% 确认后，closeEco 成功闭环并级联关闭 ECR (M22-F05)")
    void testTC_M22_06_CloseEcoSuccessWhenFieldReceiptsConfirmed() {
        // 使用种子数据中的 ECO (ID: 8001L)
        Long ecoId = 8001L;

        // 补全 MES 现场返修回执 (让 8402 disposition 对应的实施记录转为 COMPLETED)
        ConfirmImplementationRequest confirmReq = new ConfirmImplementationRequest();
        confirmReq.setDispositionId(8402L);
        confirmReq.setTargetSystem("MES");
        confirmReq.setExternalReceiptId(9999L);
        confirmReq.setExecutionStatus("COMPLETED");
        confirmReq.setSiteOperatorId("mes_plant_lead");
        service.confirmImplementationReceipt(ecoId, confirmReq);

        // 再次执行关闭变更单 -> 验证放行成功
        CloseEcoResponse response = service.closeEco(ecoId);
        assertNotNull(response);
        assertEquals(EcoStatus.CLOSED, response.getStatus());
        assertNotNull(response.getClosedAt());

        // 验证数据库中 ECO 与关联 ECR 均已流转至 CLOSED 终态
        EcoDetailDto ecoDetail = service.getEcoDetail(ecoId);
        assertEquals(EcoStatus.CLOSED, ecoDetail.getEco().getStatus());

        ChangeRequestEntity ecr = repository.findEcrById(ecoDetail.getEco().getEcrId()).get();
        assertEquals(EcrStatus.CLOSED, ecr.getStatus(), "ECO 闭环后关联 ECR 必须级联关闭");
    }
}
