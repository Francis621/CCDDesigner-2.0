package com.ccdd.change.service;

import com.ccdd.change.dto.CloseEcoResponse;
import com.ccdd.change.dto.ConfirmImplementationRequest;
import com.ccdd.change.dto.CreateChangeTaskRequest;
import com.ccdd.change.dto.CreateDispositionRequest;
import com.ccdd.change.dto.CreateEcoRequest;
import com.ccdd.change.dto.CreateEcrRequest;
import com.ccdd.change.dto.EcoDetailDto;
import com.ccdd.change.dto.EcrDetailDto;
import com.ccdd.change.dto.EvaluateImpactRequest;
import com.ccdd.change.dto.ImpactAnalysisResultDto;
import com.ccdd.change.dto.ImpactAssessmentReadinessDto;
import com.ccdd.change.dto.RecordImpactDecisionRequest;
import com.ccdd.change.entity.ChangeOrderEntity;
import com.ccdd.change.entity.ChangeRequestEntity;
import com.ccdd.change.entity.ChangeTaskEntity;
import com.ccdd.change.entity.DispositionActionType;
import com.ccdd.change.entity.EcrStatus;
import com.ccdd.change.entity.EcoStatus;
import com.ccdd.change.entity.EffectivityDispositionEntity;
import com.ccdd.change.entity.ImpactDecisionEntity;
import com.ccdd.change.entity.ImpactDecisionType;
import com.ccdd.change.entity.ImpactItemEntity;
import com.ccdd.change.entity.ImplementationRecordEntity;
import com.ccdd.change.exception.EvidenceNotInheritedException;
import com.ccdd.change.exception.FieldExecutionIncompleteException;
import com.ccdd.change.exception.IncompatibleInterchangeabilityException;
import com.ccdd.change.exception.IncompleteImpactAssessmentException;
import com.ccdd.change.repository.ChangeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * M22 工程变更与影响处置业务核心服务
 * 落实 ECR/ECO两阶段解耦、AT-10专业裁决、ADR-05互换性、ADR-08验证不继承与AT-22现场双重闭环
 */
@Service
public class ChangeService {

    private final ChangeRepository repository;

    public ChangeService(ChangeRepository repository) {
        this.repository = repository;
    }

    // =========================================================================
    // M22-F01: 变更请求 (ECR) 生命周期管理
    // =========================================================================

    public ChangeRequestEntity createEcr(CreateEcrRequest request) {
        if (request.getEcrNumber() == null || request.getEcrNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("ECR 编号不能为空");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("变更标题不能为空");
        }
        Optional<ChangeRequestEntity> existing = repository.findEcrByNumber(request.getEcrNumber());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("ECR 编号已存在: " + request.getEcrNumber());
        }

        ChangeRequestEntity ecr = new ChangeRequestEntity();
        ecr.setTenantId("VMC_ENTERPRISE");
        ecr.setProjectId(request.getProjectId() != null ? request.getProjectId() : 101L);
        ecr.setEcrNumber(request.getEcrNumber());
        ecr.setTitle(request.getTitle());
        ecr.setReasonType(request.getReasonType());
        ecr.setProblemDescription(request.getProblemDescription());
        ecr.setProposedSolution(request.getProposedSolution());
        ecr.setUrgencyLevel(request.getUrgencyLevel() != null ? request.getUrgencyLevel() : "MEDIUM");
        ecr.setStatus(EcrStatus.DRAFT);
        ecr.setSourceServiceCaseId(request.getSourceServiceCaseId());
        ecr.setOriginatorId(request.getOriginatorId() != null ? request.getOriginatorId() : "admin");
        ecr.setCreatedAt(Instant.now());

        return repository.saveEcr(ecr);
    }

    public ChangeRequestEntity submitEcr(Long ecrId) {
        ChangeRequestEntity ecr = repository.findEcrById(ecrId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更请求 ECR: " + ecrId));
        if (ecr.getStatus() != EcrStatus.DRAFT) {
            throw new IllegalStateException("当前状态不支持提交初审: " + ecr.getStatus());
        }
        ecr.setStatus(EcrStatus.SUBMITTED);
        return repository.saveEcr(ecr);
    }

    public ChangeRequestEntity reviewEcr(Long ecrId, boolean approve) {
        ChangeRequestEntity ecr = repository.findEcrById(ecrId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更请求 ECR: " + ecrId));
        if (ecr.getStatus() != EcrStatus.SUBMITTED && ecr.getStatus() != EcrStatus.IN_REVIEW) {
            throw new IllegalStateException("当前状态不在评审流程中: " + ecr.getStatus());
        }
        ecr.setStatus(approve ? EcrStatus.APPROVED : EcrStatus.REJECTED);
        return repository.saveEcr(ecr);
    }

    public List<ChangeRequestEntity> getAllEcrs() {
        return repository.findAllEcrs();
    }

    public EcrDetailDto getEcrDetail(Long ecrId) {
        ChangeRequestEntity ecr = repository.findEcrById(ecrId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更请求 ECR: " + ecrId));
        List<ChangeOrderEntity> ecos = repository.findEcosByEcrId(ecrId);
        return new EcrDetailDto(ecr, ecos);
    }

    // =========================================================================
    // M22-F02: 影响候选计算与专业工程裁定 (AT-10 规范)
    // =========================================================================

    public ImpactAnalysisResultDto evaluateImpact(Long ecoId, EvaluateImpactRequest request) {
        ChangeOrderEntity eco = repository.findEcoById(ecoId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更实施单 ECO: " + ecoId));

        int maxDepth = request.getMaxDepth() != null ? request.getMaxDepth() : 5;
        boolean isTruncated = maxDepth > 5;
        eco.setIsImpactAnalysisTruncated(isTruncated);

        // 模拟调用 M23 数字主线递归展开候选拓扑路径 (针对主轴提速工况)
        List<ImpactItemEntity> detectedItems = new ArrayList<>();

        ImpactItemEntity item1 = new ImpactItemEntity(
                null, ecoId, 5003L, "PartRevision", "M-VMC850-BRG-7014",
                "[\"REQ-VMC1000-SPEED\", \"SPINDLE_SUBSYS\", \"M-VMC850-BRG-7014\"]",
                2, "MECHANICAL", false
        );
        ImpactItemEntity item2 = new ImpactItemEntity(
                null, ecoId, 5005L, "VerificationCaseRevision", "TC-SPINDLE-THERMAL",
                "[\"REQ-VMC1000-SPEED\", \"TC-SPINDLE-THERMAL\"]",
                2, "SIMULATION", false
        );
        ImpactItemEntity item3 = new ImpactItemEntity(
                null, ecoId, 5006L, "PartRevision", "M-VMC1000-MOTOR-15KW",
                "[\"REQ-VMC1000-SPEED\", \"M-VMC1000-MOTOR-15KW\"]",
                2, "ELECTRICAL", false
        );

        detectedItems.add(repository.saveImpactItem(item1));
        detectedItems.add(repository.saveImpactItem(item2));
        detectedItems.add(repository.saveImpactItem(item3));

        eco.setStatus(EcoStatus.ASSESSING);
        repository.saveEco(eco);

        List<ImpactAnalysisResultDto.ImpactItemSummary> summaries = detectedItems.stream()
                .map(i -> new ImpactAnalysisResultDto.ImpactItemSummary(
                        i.getImpactItemId(), i.getCandidateRevisionId(), i.getObjectTypeCode(),
                        i.getBusinessCode(), i.getAssignedDiscipline(), i.getPropagationPath(),
                        i.getTraversalDepth(), i.getIsAssessed()
                ))
                .collect(Collectors.toList());

        return new ImpactAnalysisResultDto(ecoId, summaries.size(), isTruncated, summaries);
    }

    public ImpactDecisionEntity recordImpactDecision(Long ecoId, RecordImpactDecisionRequest request) {
        ImpactItemEntity item = repository.findImpactItemById(request.getImpactItemId())
                .orElseThrow(() -> new IllegalArgumentException("未找到影响候选项: " + request.getImpactItemId()));

        if (request.getDecisionType() == ImpactDecisionType.NO_IMPACT
                && (request.getTechnicalRationale() == null || request.getTechnicalRationale().trim().isEmpty())) {
            throw new IllegalArgumentException("专业判定为 NO_IMPACT 时，必须提供详尽的工程免责与技术分析依据！");
        }

        ImpactDecisionEntity decision = new ImpactDecisionEntity();
        decision.setImpactItemId(item.getImpactItemId());
        decision.setDecisionType(request.getDecisionType());
        decision.setTechnicalRationale(request.getTechnicalRationale());
        decision.setActionRequired(request.getActionRequired());
        decision.setTargetActionPlan(request.getTargetActionPlan() != null ? request.getTargetActionPlan() : "REVISE_EXISTING");
        decision.setAssessorId(request.getAssessorId() != null ? request.getAssessorId() : "lead_engineer");
        decision.setAssessedAt(Instant.now());

        return repository.saveImpactDecision(decision);
    }

    public ImpactAssessmentReadinessDto validateImpactReadiness(Long ecoId) {
        ChangeOrderEntity eco = repository.findEcoById(ecoId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更实施单 ECO: " + ecoId));

        if (Boolean.TRUE.equals(eco.getIsImpactAnalysisTruncated())) {
            return new ImpactAssessmentReadinessDto(false, 0, 0, 0, 0, 0,
                    "影响分析警告：图拓扑遍历因深度或权限被截断，在获得 CCB 授权前必须重新进行完整分析！");
        }

        List<ImpactItemEntity> items = repository.findImpactItemsByEcoId(ecoId);
        int unassessed = (int) items.stream().filter(i -> !Boolean.TRUE.equals(i.getIsAssessed())).count();

        List<ImpactDecisionEntity> decisions = repository.findDecisionsByEcoId(ecoId);
        int reVerify = (int) decisions.stream().filter(d -> d.getDecisionType() == ImpactDecisionType.RE_VERIFY).count();
        int modify = (int) decisions.stream().filter(d -> d.getDecisionType() == ImpactDecisionType.MODIFY).count();
        int reviewOnly = (int) decisions.stream().filter(d -> d.getDecisionType() == ImpactDecisionType.REVIEW_ONLY).count();
        int noImpact = (int) decisions.stream().filter(d -> d.getDecisionType() == ImpactDecisionType.NO_IMPACT).count();

        boolean isReady = unassessed == 0 && !items.isEmpty();
        String msg = isReady ? "影响处置 100% 完成签署，准予提交 CCB 委员会授权实施"
                : "影响处置未完成：尚有 [" + unassessed + "] 项候选对象缺少专业责任工程师裁定签字！";

        return new ImpactAssessmentReadinessDto(isReady, unassessed, reVerify, modify, reviewOnly, noImpact, msg);
    }

    // =========================================================================
    // M22-F03: 签发 ECO、CCB 审批与任务工作空间授权 (ADR-05 落地)
    // =========================================================================

    public ChangeOrderEntity createEco(CreateEcoRequest request) {
        ChangeRequestEntity ecr = repository.findEcrById(request.getEcrId())
                .orElseThrow(() -> new IllegalArgumentException("未找到关联的变更请求 ECR: " + request.getEcrId()));

        if (ecr.getStatus() != EcrStatus.APPROVED) {
            throw new IllegalStateException("仅有处于 APPROVED 立项批准状态的 ECR 允许签发变更实施单 ECO！当前状态: " + ecr.getStatus());
        }

        ChangeOrderEntity eco = new ChangeOrderEntity();
        eco.setEcrId(ecr.getEcrId());
        eco.setTenantId(ecr.getTenantId());
        eco.setEcoNumber(request.getEcoNumber());
        eco.setTitle(request.getTitle());
        eco.setChangeCategory(request.getChangeCategory() != null ? request.getChangeCategory() : "MAJOR");
        eco.setTargetBaselineId(request.getTargetBaselineId());
        eco.setStatus(EcoStatus.DRAFT);
        eco.setIsImpactAnalysisTruncated(false);
        eco.setWorkingVersion(1L);
        eco.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "chief_designer");
        eco.setCreatedAt(Instant.now());

        return repository.saveEco(eco);
    }

    public ChangeOrderEntity authorizeEco(Long ecoId, Long approvalTicketId) {
        ChangeOrderEntity eco = repository.findEcoById(ecoId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更实施单 ECO: " + ecoId));

        // 准入完备性校验 (CST-M22-02)
        ImpactAssessmentReadinessDto readiness = validateImpactReadiness(ecoId);
        if (!Boolean.TRUE.equals(readiness.getIsReady())) {
            throw new IncompleteImpactAssessmentException(ecoId, readiness.getUnassessedCount(),
                    "ECO 授权审批被拦截 (CST-M22-02)：影响分析存在未裁决项或图谱截断！" + readiness.getMessage());
        }

        eco.setStatus(EcoStatus.AUTHORIZED);
        eco.setCcbApprovalTicketId(approvalTicketId != null ? approvalTicketId : 9005L);
        return repository.saveEco(eco);
    }

    public ChangeTaskEntity createChangeTask(Long ecoId, CreateChangeTaskRequest request) {
        ChangeOrderEntity eco = repository.findEcoById(ecoId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更实施单 ECO: " + ecoId));

        // ADR-05 互换性检验：配合尺寸改变必须新建零件物料，禁止原位升版
        if (Boolean.TRUE.equals(request.getEnforceFffCheck())) {
            List<ImpactItemEntity> items = repository.findImpactItemsByEcoId(ecoId);
            for (ImpactItemEntity item : items) {
                if (item.getCandidateRevisionId().equals(request.getSourceRevisionId())) {
                    Optional<ImpactDecisionEntity> decision = repository.findDecisionByImpactItemId(item.getImpactItemId());
                    if (decision.isPresent() && "CREATE_NEW".equalsIgnoreCase(decision.get().getTargetActionPlan())) {
                        if (request.getTaskType().contains("REVISE_ORIGINAL") || request.getTitle().contains("升版原物料")) {
                            throw new IncompatibleInterchangeabilityException(
                                    item.getBusinessCode(),
                                    decision.get().getTechnicalRationale(),
                                    "违反物料互换性准则 (ADR-05)：该零部件因形状/配合面改变已破坏双向互换性，强制要求申请全新的物料主编码，严禁原地升版！"
                            );
                        }
                    }
                }
            }
        }

        ChangeTaskEntity task = new ChangeTaskEntity();
        task.setEcoId(ecoId);
        task.setTaskCode(request.getTaskCode());
        task.setTitle(request.getTitle());
        task.setTaskType(request.getTaskType());
        task.setAssigneeId(request.getAssigneeId());
        task.setSourceRevisionId(request.getSourceRevisionId());
        task.setTargetRevisionId(System.currentTimeMillis());
        task.setStatus("PENDING");

        eco.setStatus(EcoStatus.IMPLEMENTING);
        repository.saveEco(eco);

        return repository.saveTask(task);
    }

    public ChangeTaskEntity completeTask(Long ecoId, Long taskId) {
        ChangeTaskEntity task = repository.findTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("未找到任务: " + taskId));
        task.setStatus("COMPLETED");
        task.setCompletedAt(Instant.now());
        return repository.saveTask(task);
    }

    // =========================================================================
    // M22-F04: 跟踪新版本发布与设计闭环 (ADR-08 落地)
    // =========================================================================

    public ChangeOrderEntity releaseEco(Long ecoId, Long newSimulationRunId) {
        ChangeOrderEntity eco = repository.findEcoById(ecoId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更实施单 ECO: " + ecoId));

        // 1. 检查实施任务是否全量完成
        List<ChangeTaskEntity> tasks = repository.findTasksByEcoId(ecoId);
        boolean allCompleted = tasks.stream().allMatch(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()));
        if (!allCompleted && !tasks.isEmpty()) {
            throw new IllegalStateException("仍有工程实施任务未完成，无法执行设计发布！");
        }

        // 2. ADR-08 再验证强校验：RE_VERIFY 条目不得复用旧证据，必须绑定新工况运行结果
        List<ImpactDecisionEntity> decisions = repository.findDecisionsByEcoId(ecoId);
        boolean hasReVerify = decisions.stream().anyMatch(d -> d.getDecisionType() == ImpactDecisionType.RE_VERIFY);
        if (hasReVerify) {
            if (newSimulationRunId == null || newSimulationRunId <= 0) {
                throw new EvidenceNotInheritedException(
                        "TC-SPINDLE-THERMAL",
                        "违反验证证据不继承规则 (ADR-08)：当前变更涉及工况参数大幅调整，标记为 RE_VERIFY 的验证用例必须提供基于新工况的仿真执行结果(Run ID)，禁止直接复用历史合格结论！"
                );
            }
        }

        eco.setStatus(EcoStatus.RELEASED);
        eco.setReleasedAt(Instant.now());
        return repository.saveEco(eco);
    }

    // =========================================================================
    // M22-F05: 实物处置策略下发与现场回执闭环 (AT-22 守卫，CST-M22-01)
    // =========================================================================

    public EffectivityDispositionEntity createDisposition(Long ecoId, CreateDispositionRequest request) {
        ChangeOrderEntity eco = repository.findEcoById(ecoId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更实施单 ECO: " + ecoId));

        EffectivityDispositionEntity disp = new EffectivityDispositionEntity();
        disp.setEcoId(ecoId);
        disp.setTargetScopeType(request.getTargetScopeType());
        disp.setTargetPartNumber(request.getTargetPartNumber());
        disp.setTargetOrderProductId(request.getTargetOrderProductId());
        disp.setTargetIndividualId(request.getTargetIndividualId());
        disp.setActionType(request.getActionType());
        disp.setEffectiveSerialCutoff(request.getEffectiveSerialCutoff());
        disp.setEffectiveDateCutoff(request.getEffectiveDateCutoff());
        disp.setDispositionInstructions(request.getDispositionInstructions());
        disp.setCreatedAt(Instant.now());

        EffectivityDispositionEntity saved = repository.saveDisposition(disp);

        // 自动向实施回执表生成一条待对账记录 (DISPATCHED)
        ImplementationRecordEntity record = new ImplementationRecordEntity();
        record.setEcoId(ecoId);
        record.setDispositionId(saved.getDispositionId());
        record.setTargetSystem("INVENTORY_PART".equals(request.getTargetScopeType()) ? "ERP" : "MES");
        record.setExecutionStatus("DISPATCHED");
        record.setCreatedAt(Instant.now());
        repository.saveImplementationRecord(record);

        eco.setStatus(EcoStatus.EXECUTING);
        repository.saveEco(eco);

        return saved;
    }

    public ImplementationRecordEntity confirmImplementationReceipt(Long ecoId, ConfirmImplementationRequest request) {
        ImplementationRecordEntity record = repository.findImplementationRecordByDispositionId(request.getDispositionId())
                .orElseGet(() -> {
                    ImplementationRecordEntity r = new ImplementationRecordEntity();
                    r.setEcoId(ecoId);
                    r.setDispositionId(request.getDispositionId());
                    return r;
                });

        record.setTargetSystem(request.getTargetSystem() != null ? request.getTargetSystem() : "MES");
        record.setExternalReceiptId(request.getExternalReceiptId() != null ? request.getExternalReceiptId() : 9099L);
        record.setExecutionStatus(request.getExecutionStatus() != null ? request.getExecutionStatus() : "COMPLETED");
        record.setSiteOperatorId(request.getSiteOperatorId() != null ? request.getSiteOperatorId() : "site_op_01");
        record.setCompletionEvidenceDoc(request.getCompletionEvidenceDoc());
        record.setConfirmedAt(Instant.now());

        return repository.saveImplementationRecord(record);
    }

    /**
     * 终态关闭变更单 (AT-22 守卫，CST-M22-01 硬拦截)
     */
    public CloseEcoResponse closeEco(Long ecoId) {
        ChangeOrderEntity eco = repository.findEcoById(ecoId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更实施单 ECO: " + ecoId));

        // 1. 检查设计任务是否 100% COMPLETED
        List<ChangeTaskEntity> tasks = repository.findTasksByEcoId(ecoId);
        long uncompletedTasks = tasks.stream().filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        if (uncompletedTasks > 0) {
            throw new IllegalStateException("ECO 关闭被拦截：尚有 [" + uncompletedTasks + "] 项工程设计实施任务未完成！");
        }

        // 2. 检查现场生效处置策略是否全部建立跟踪记录
        List<EffectivityDispositionEntity> disps = repository.findDispositionsByEcoId(ecoId);
        List<ImplementationRecordEntity> records = repository.findImplementationRecordsByEcoId(ecoId);
        Map<Long, ImplementationRecordEntity> recordMap = records.stream()
                .collect(Collectors.toMap(ImplementationRecordEntity::getDispositionId, r -> r, (k1, k2) -> k1));

        for (EffectivityDispositionEntity d : disps) {
            if (!recordMap.containsKey(d.getDispositionId())) {
                throw new IllegalStateException("ECO 关闭被拦截：存在生效处置策略尚未下发到 MES/ERP 进行实施对账！");
            }
        }

        // 3. AT-22 守卫：检查现场实施记录是否全部取得 COMPLETED 回执
        List<ImplementationRecordEntity> pendingRecords = records.stream()
                .filter(r -> !"COMPLETED".equalsIgnoreCase(r.getExecutionStatus()))
                .collect(Collectors.toList());

        if (!pendingRecords.isEmpty()) {
            throw new FieldExecutionIncompleteException(
                    ecoId,
                    pendingRecords.size(),
                    "ECO 关闭被物理阻断 (AT-22 守卫)：现场实施未完全闭环！当前仍有 [" + pendingRecords.size() + "] 条现场处置回执处于派发或执行中(DISPATCHED/IN_EXECUTION)，外部工厂/实物未确认前严禁关闭变更单！"
            );
        }

        // 4. 允许关闭并流转至 CLOSED
        eco.setStatus(EcoStatus.CLOSED);
        eco.setClosedAt(Instant.now());
        repository.saveEco(eco);

        // 级联关闭关联的 ECR
        Optional<ChangeRequestEntity> ecrOpt = repository.findEcrById(eco.getEcrId());
        ecrOpt.ifPresent(ecr -> {
            ecr.setStatus(EcrStatus.CLOSED);
            repository.saveEcr(ecr);
        });

        return new CloseEcoResponse(
                eco.getEcoId(),
                eco.getEcoNumber(),
                eco.getStatus(),
                eco.getClosedAt(),
                tasks.size(),
                records.size(),
                "变更单与现场实施已 100% 完成物理对账，整单法律关闭生效！"
        );
    }

    public List<ChangeOrderEntity> getAllEcos() {
        return repository.findAllEcos();
    }

    public EcoDetailDto getEcoDetail(Long ecoId) {
        ChangeOrderEntity eco = repository.findEcoById(ecoId)
                .orElseThrow(() -> new IllegalArgumentException("未找到变更实施单 ECO: " + ecoId));

        List<ImpactItemEntity> items = repository.findImpactItemsByEcoId(ecoId);
        List<EcoDetailDto.ImpactItemWithDecision> itemWithDecisions = new ArrayList<>();
        for (ImpactItemEntity item : items) {
            ImpactDecisionEntity decision = repository.findDecisionByImpactItemId(item.getImpactItemId()).orElse(null);
            itemWithDecisions.add(new EcoDetailDto.ImpactItemWithDecision(item, decision));
        }

        List<ChangeTaskEntity> tasks = repository.findTasksByEcoId(ecoId);
        List<EffectivityDispositionEntity> disps = repository.findDispositionsByEcoId(ecoId);
        List<ImplementationRecordEntity> records = repository.findImplementationRecordsByEcoId(ecoId);

        return new EcoDetailDto(eco, itemWithDecisions, tasks, disps, records);
    }
}
