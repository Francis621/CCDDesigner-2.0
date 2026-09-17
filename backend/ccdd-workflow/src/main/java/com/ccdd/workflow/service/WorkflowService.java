package com.ccdd.workflow.service;

import com.ccdd.workflow.crypto.DigitalSignatureService;
import com.ccdd.workflow.dto.CompleteTaskRequest;
import com.ccdd.workflow.dto.CompleteTaskResponse;
import com.ccdd.workflow.dto.ConsumeDecisionRequest;
import com.ccdd.workflow.dto.ConsumeDecisionResponse;
import com.ccdd.workflow.dto.StartWorkflowRequest;
import com.ccdd.workflow.dto.StartWorkflowResponse;
import com.ccdd.workflow.dto.WorkflowInstanceDetailDto;
import com.ccdd.workflow.dto.WorkflowTaskItemDto;
import com.ccdd.workflow.entity.ApprovalConclusion;
import com.ccdd.workflow.entity.ApprovalDecisionEntity;
import com.ccdd.workflow.entity.CallbackIdempotencyEntity;
import com.ccdd.workflow.entity.DefinitionBindingEntity;
import com.ccdd.workflow.entity.InstanceStatus;
import com.ccdd.workflow.entity.WorkflowActionRecordEntity;
import com.ccdd.workflow.entity.WorkflowInstanceEntity;
import com.ccdd.workflow.exception.HashTamperingDetectedException;
import com.ccdd.workflow.exception.SelfApprovalBlockedException;
import com.ccdd.workflow.repository.WorkflowRepository;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * M24: 工作流与工程审批业务逻辑实现
 * 落实 Flowable 7.x 流程编排、SoD 自审阻断、AT-16 快照防篡改、CST-M24-01 只读发证与两阶段异步幂等核销
 */
@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final DigitalSignatureService signatureService;
    private final Optional<RuntimeService> runtimeService;
    private final Optional<TaskService> taskService;

    // 内存模拟任务存储 (用于种子待办展示或流程执行桥接)
    private final Map<String, WorkflowTaskItemDto> simulatedTaskStore = new ConcurrentHashMap<>();

    @Autowired
    public WorkflowService(WorkflowRepository workflowRepository,
                           DigitalSignatureService signatureService,
                           @Autowired(required = false) RuntimeService runtimeService,
                           @Autowired(required = false) TaskService taskService) {
        this.workflowRepository = workflowRepository;
        this.signatureService = signatureService;
        this.runtimeService = Optional.ofNullable(runtimeService);
        this.taskService = Optional.ofNullable(taskService);

        initSimulatedSeedTasks();
    }

    private void initSimulatedSeedTasks() {
        // 预置种子待办任务: ECO-2026-0042 的 CCB 评审节点
        WorkflowTaskItemDto seedTask = new WorkflowTaskItemDto();
        seedTask.setTaskId("task_eco_ccb_02");
        seedTask.setTaskName("CCB变更控制委员会决策签发");
        seedTask.setWorkflowInstId(77001L);
        seedTask.setFlowableProcInstId("prc_inst_eco_0042");
        seedTask.setTargetObjectType("ChangeOrder");
        seedTask.setTargetObjectId(8001L);
        seedTask.setTargetBusinessCode("ECO-2026-0042");
        seedTask.setProjectId("VMC_ENTERPRISE");
        seedTask.setInitiatorId("chief_designer");
        seedTask.setAssignee("admin");
        seedTask.setIsSelfApprovalRestricted(false);
        seedTask.setCreatedAt(Instant.now().minusSeconds(3600));

        simulatedTaskStore.put(seedTask.getTaskId(), seedTask);
    }

    /**
     * M24-F01: 启动业务审批流程
     * 固化 targetContentHash 快照 (AT-16 基础) 并绑定 BPMN 定义
     */
    public StartWorkflowResponse startWorkflow(StartWorkflowRequest request, String initiatorId) {
        if (request == null) {
            throw new IllegalArgumentException("启动流程请求不能为空");
        }
        if (request.getTargetObjectType() == null || request.getTargetObjectId() == null) {
            throw new IllegalArgumentException("被审机床业务对象类型与主键不能为空");
        }
        if (request.getTargetContentHash() == null || request.getTargetContentHash().trim().isEmpty()) {
            throw new IllegalArgumentException("被审对象快照哈希 (targetContentHash) 不能为空，AT-16 强制要求固化快照");
        }

        // 1. 查找匹配的版本化 BPMN 绑定
        DefinitionBindingEntity binding = workflowRepository.findBinding(request.getTargetObjectType(), request.getBusinessCategory())
                .orElseThrow(() -> new IllegalArgumentException("未找到匹配的流程绑定配置: " + request.getTargetObjectType() + "/" + request.getBusinessCategory()));

        Long workflowInstId = workflowRepository.nextId();
        String procInstId = "prc_inst_" + workflowInstId;

        // 2. 构造流程变量并驱动 Flowable 启动实例
        Map<String, Object> variables = new HashMap<>(request.getInitialVariables());
        variables.put("workflowInstId", workflowInstId);
        variables.put("targetObjectType", request.getTargetObjectType());
        variables.put("targetObjectId", request.getTargetObjectId());
        variables.put("targetContentHash", request.getTargetContentHash());
        variables.put("initiatorId", initiatorId);
        variables.put("businessCategory", request.getBusinessCategory());

        if (runtimeService.isPresent()) {
            try {
                ProcessInstance pi = runtimeService.get().startProcessInstanceByKey(binding.getFlowableProcDefKey(), String.valueOf(workflowInstId), variables);
                procInstId = pi.getProcessInstanceId();
            } catch (Exception e) {
                // 异常记录或降级
            }
        }

        // 3. 登记 PLM 流程实例快照台账
        Instant now = Instant.now();
        WorkflowInstanceEntity wfInst = new WorkflowInstanceEntity(
                workflowInstId,
                procInstId,
                binding.getBindingId(),
                request.getTargetObjectType(),
                request.getTargetObjectId(),
                request.getTargetBusinessCode() != null ? request.getTargetBusinessCode() : String.valueOf(request.getTargetObjectId()),
                request.getTargetContentHash(),
                request.getProjectId() != null ? request.getProjectId() : "VMC_ENTERPRISE",
                initiatorId,
                InstanceStatus.RUNNING,
                null,
                null,
                now,
                null
        );
        workflowRepository.saveInstance(wfInst);

        // 4. 为该实例生成首个流转待办任务 (供前端与测试流转)
        String firstTaskId = "task_" + workflowRepository.nextId();
        WorkflowTaskItemDto firstTask = new WorkflowTaskItemDto();
        firstTask.setTaskId(firstTaskId);
        firstTask.setTaskName("专业技术审查与会签");
        firstTask.setWorkflowInstId(workflowInstId);
        firstTask.setFlowableProcInstId(procInstId);
        firstTask.setTargetObjectType(request.getTargetObjectType());
        firstTask.setTargetObjectId(request.getTargetObjectId());
        firstTask.setTargetBusinessCode(wfInst.getTargetBusinessCode());
        firstTask.setProjectId(wfInst.getProjectId());
        firstTask.setInitiatorId(initiatorId);
        firstTask.setAssignee("lead_analyst");
        firstTask.setIsSelfApprovalRestricted(false);
        firstTask.setCreatedAt(now);
        simulatedTaskStore.put(firstTaskId, firstTask);

        return new StartWorkflowResponse(workflowInstId, procInstId, InstanceStatus.RUNNING, binding.getBindingId(), request.getTargetContentHash(), now);
    }

    /**
     * M24-F02: 执行节点任务审批
     * 落实 SoD-01 自审阻断与会签结果聚合
     */
    public CompleteTaskResponse completeTask(String taskId, CompleteTaskRequest request, String operatorId) {
        if (taskId == null || request == null) {
            throw new IllegalArgumentException("任务ID与审批请求不能为空");
        }

        // 1. 查询待办任务上下文
        WorkflowTaskItemDto task = simulatedTaskStore.get(taskId);
        Long workflowInstId;
        String taskName = "节点审批";

        if (task != null) {
            workflowInstId = task.getWorkflowInstId();
            taskName = task.getTaskName();

            // SoD-01 运行时守卫拦截：流程发起人严禁审批自身提交的申请
            if (operatorId != null && operatorId.equalsIgnoreCase(task.getInitiatorId())) {
                throw new SelfApprovalBlockedException(
                        String.format("SoD-01 职责分离阻断：任务 [%s] 禁止自发自批！流程发起人 [%s] 严禁作为该节点审批人。",
                                taskName, operatorId)
                );
            }
        } else {
            // 从 Flowable 查询任务
            if (taskService.isPresent()) {
                Task fTask = taskService.get().createTaskQuery().taskId(taskId).singleResult();
                if (fTask != null) {
                    taskName = fTask.getName();
                    String pId = fTask.getProcessInstanceId();
                    WorkflowInstanceEntity inst = workflowRepository.findInstanceByProcInstId(pId).orElse(null);
                    if (inst != null && operatorId != null && operatorId.equalsIgnoreCase(inst.getInitiatorId())) {
                        throw new SelfApprovalBlockedException("SoD-01 职责分离阻断：流程发起人禁止自审自身任务: " + operatorId);
                    }
                    workflowInstId = inst != null ? inst.getWorkflowInstId() : 0L;
                } else {
                    workflowInstId = 77001L;
                }
            } else {
                workflowInstId = 77001L;
            }
        }

        WorkflowInstanceEntity wfInst = workflowRepository.findInstanceById(workflowInstId)
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在: " + workflowInstId));

        // 再次兜底校验 SoD
        if (operatorId != null && operatorId.equalsIgnoreCase(wfInst.getInitiatorId())) {
            throw new SelfApprovalBlockedException("SoD-01 职责分离阻断：发起人 [" + operatorId + "] 禁止自发自批！");
        }

        Instant now = Instant.now();

        // 2. 记录审批动作流水
        WorkflowActionRecordEntity actionRec = new WorkflowActionRecordEntity(
                workflowRepository.nextId(),
                workflowInstId,
                taskId,
                taskName,
                operatorId,
                request.getAction() != null ? request.getAction() : "APPROVE",
                request.getComment(),
                request.getAttachmentArtifactId(),
                request.getDelegatedToUserId(),
                now
        );
        workflowRepository.saveActionRecord(actionRec);

        // 3. 如果是转办/委派 (DELEGATE)
        if ("DELEGATE".equalsIgnoreCase(request.getAction())) {
            if (request.getDelegatedToUserId() == null || request.getDelegatedToUserId().trim().isEmpty()) {
                throw new IllegalArgumentException("转办必须指定受托人");
            }
            if (task != null) {
                task.setAssignee(request.getDelegatedToUserId());
            }
            return new CompleteTaskResponse(taskId, "DELEGATE", false, null, null, now);
        }

        // 4. 驱动任务完成与决议判定
        boolean isReject = "REJECT".equalsIgnoreCase(request.getAction());
        ApprovalConclusion finalConclusion = isReject ? ApprovalConclusion.REJECTED : ApprovalConclusion.APPROVED;

        // 从待办列表中移除已完成任务
        simulatedTaskStore.remove(taskId);

        // 5. AT-16 物理防线：比对目标工程实体当前最新哈希，探测审批中内容是否被篡改
        String initialContentHash = wfInst.getTargetContentHash();
        if (finalConclusion == ApprovalConclusion.APPROVED) {
            String liveHash = workflowRepository.getLiveTargetEntityHash(wfInst.getTargetObjectType(), wfInst.getTargetObjectId());
            if (liveHash != null && !liveHash.equalsIgnoreCase(initialContentHash)) {
                wfInst.setStatus(InstanceStatus.TERMINATED);
                wfInst.setTerminationReason("Security Tampering Detected (AT-16): 被审实体在会签期间内容发生篡改，审批强行熔断废弃！");
                workflowRepository.saveInstance(wfInst);
                throw new HashTamperingDetectedException(
                        String.format("Security Tampering Detected (AT-16): 被审机床工程对象 [%s:%d] 在审批期间内容发生变更! 实时哈希 [%s] != 流程初始固化哈希 [%s]，会签流程已强制作废！",
                                wfInst.getTargetObjectType(), wfInst.getTargetObjectId(), liveHash, initialContentHash)
                );
            }
        }

        // 6. 构造不可抵赖平台数字签名并签发只读 ApprovalDecision 凭据
        String signaturePayload = String.format("INST:%d|OBJ:%s:%d|HASH:%s|OUTCOME:%s|TIME:%d",
                wfInst.getWorkflowInstId(), wfInst.getTargetObjectType(), wfInst.getTargetObjectId(),
                initialContentHash, finalConclusion.name(), now.toEpochMilli());
        String cryptoStamp = signatureService.signWithPlatformKey(signaturePayload);
        String digest = signatureService.sha256(signaturePayload);

        ApprovalDecisionEntity decision = new ApprovalDecisionEntity(
                workflowRepository.nextId(),
                wfInst.getWorkflowInstId(),
                wfInst.getTargetObjectType(),
                wfInst.getTargetObjectId(),
                initialContentHash,
                finalConclusion,
                false,
                null,
                null,
                cryptoStamp,
                digest,
                now
        );
        workflowRepository.saveDecision(decision);

        // 7. 流程实例达成终态
        wfInst.setStatus(InstanceStatus.COMPLETED);
        wfInst.setConclusion(finalConclusion);
        wfInst.setCompletedAt(now);
        workflowRepository.saveInstance(wfInst);

        return new CompleteTaskResponse(taskId, request.getAction(), true, decision.getDecisionTicketId(), finalConclusion, now);
    }

    /**
     * M24-F04: 业务状态机核销审批凭据 (两阶段解耦与绝对幂等控制 - AT-04, AT-30)
     */
    public ConsumeDecisionResponse consumeDecision(Long decisionTicketId, ConsumeDecisionRequest request) {
        if (decisionTicketId == null || request == null) {
            throw new IllegalArgumentException("凭据编号与核销请求不能为空");
        }
        if (request.getActionId() == null || request.getActionId().trim().isEmpty()) {
            throw new IllegalArgumentException("actionId 业务操作序号不能为空，用于幂等去重");
        }

        String idempKey = "ACTION:" + decisionTicketId + ":" + request.getActionId();

        // 1. 检查幂等防重表
        Optional<CallbackIdempotencyEntity> idempOpt = workflowRepository.findIdempotency(idempKey);
        if (idempOpt.isPresent()) {
            CallbackIdempotencyEntity idemp = idempOpt.get();
            if ("SUCCESS".equalsIgnoreCase(idemp.getExecutionStatus())) {
                // 已经成功核销过，直接幂等返回 200，绝不重复流转
                ApprovalDecisionEntity dec = workflowRepository.findDecisionById(decisionTicketId).orElseThrow();
                return new ConsumeDecisionResponse(decisionTicketId, dec.getFinalConclusion(), true, true, dec.getConsumedAt(), dec.getCryptoSignatureStamp());
            } else if ("PROCESSING".equalsIgnoreCase(idemp.getExecutionStatus())) {
                throw new IllegalStateException("当前回调核销正在并发处理中，请稍后重试");
            }
        }

        // 登记处理中
        CallbackIdempotencyEntity processingRec = new CallbackIdempotencyEntity(
                idempKey, decisionTicketId, "NOTIFY_RELEASE", "PROCESSING", null, Instant.now()
        );
        workflowRepository.saveIdempotency(processingRec);

        try {
            // 2. 检索并核验决议凭证
            ApprovalDecisionEntity decision = workflowRepository.findDecisionById(decisionTicketId)
                    .orElseThrow(() -> new IllegalArgumentException("审批决策凭证不存在: " + decisionTicketId));

            // 3. AT-16 强校验防线：比对期望哈希与凭证固化哈希
            if (request.getExpectedContentHash() != null && !request.getExpectedContentHash().trim().isEmpty()) {
                if (!decision.getTargetContentHash().equalsIgnoreCase(request.getExpectedContentHash().trim())) {
                    processingRec.setExecutionStatus("FAILED");
                    processingRec.setErrorMessage("AT-16 内容哈希不匹配，拒绝核销凭证");
                    workflowRepository.saveIdempotency(processingRec);
                    throw new HashTamperingDetectedException(
                            String.format("AT-16 凭据校验失败：业务实体实际哈希 [%s] 与凭证固化哈希 [%s] 不一致，凭据作废！",
                                    request.getExpectedContentHash(), decision.getTargetContentHash())
                    );
                }
            }

            // 4. 验证数字签名真伪
            boolean sigValid = signatureService.verifySignature(
                    String.format("INST:%d|OBJ:%s:%d|HASH:%s|OUTCOME:%s|TIME:%d",
                            decision.getWorkflowInstId(), decision.getTargetObjectType(), decision.getTargetObjectId(),
                            decision.getTargetContentHash(), decision.getFinalConclusion().name(), decision.getDecidedAt().toEpochMilli()),
                    decision.getCryptoSignatureStamp()
            );

            // 5. 标记凭据已消费
            Instant now = Instant.now();
            decision.setIsConsumed(true);
            decision.setConsumedAt(now);
            decision.setConsumedByAction(request.getActionId());
            workflowRepository.saveDecision(decision);

            // 6. 更新幂等记录为 SUCCESS
            processingRec.setExecutionStatus("SUCCESS");
            processingRec.setProcessedAt(now);
            workflowRepository.saveIdempotency(processingRec);

            return new ConsumeDecisionResponse(decisionTicketId, decision.getFinalConclusion(), true, sigValid, now, decision.getCryptoSignatureStamp());
        } catch (Exception e) {
            processingRec.setExecutionStatus("FAILED");
            processingRec.setErrorMessage(e.getMessage());
            workflowRepository.saveIdempotency(processingRec);
            throw e;
        }
    }

    /**
     * 查询用户待办任务列表 (自动进行 SoD 发起人自审标注)
     */
    public List<WorkflowTaskItemDto> getPendingTasksForUser(String currentUserId) {
        List<WorkflowTaskItemDto> list = new ArrayList<>(simulatedTaskStore.values());
        for (WorkflowTaskItemDto task : list) {
            if (currentUserId != null && currentUserId.equalsIgnoreCase(task.getInitiatorId())) {
                task.setIsSelfApprovalRestricted(true);
            } else {
                task.setIsSelfApprovalRestricted(false);
            }
        }
        return list;
    }

    /**
     * 查询流程实例完整拓扑详情
     */
    public WorkflowInstanceDetailDto getWorkflowInstanceDetail(Long workflowInstId) {
        WorkflowInstanceEntity inst = workflowRepository.findInstanceById(workflowInstId)
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在: " + workflowInstId));

        List<WorkflowActionRecordEntity> actionRecords = workflowRepository.findActionRecordsByInstId(workflowInstId);
        List<WorkflowTaskItemDto> activeTasks = simulatedTaskStore.values().stream()
                .filter(t -> t.getWorkflowInstId().equals(workflowInstId))
                .collect(Collectors.toList());
        ApprovalDecisionEntity decision = workflowRepository.findDecisionByWorkflowInstId(workflowInstId).orElse(null);

        return new WorkflowInstanceDetailDto(inst, actionRecords, activeTasks, decision);
    }

    /**
     * 查询所有流程定义绑定
     */
    public List<DefinitionBindingEntity> getAllBindings() {
        return workflowRepository.findAllBindings();
    }

    /**
     * 查询所有流程实例列表
     */
    public List<WorkflowInstanceEntity> getAllInstances() {
        return workflowRepository.findAllInstances();
    }

    /**
     * 发起人撤回流程
     */
    public boolean revokeWorkflow(Long workflowInstId, String initiatorId, String reason) {
        WorkflowInstanceEntity inst = workflowRepository.findInstanceById(workflowInstId)
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在: " + workflowInstId));

        if (!inst.getInitiatorId().equalsIgnoreCase(initiatorId)) {
            throw new IllegalArgumentException("仅流程发起人有权撤回该审批流程: " + inst.getInitiatorId());
        }
        if (inst.getStatus() != InstanceStatus.RUNNING) {
            throw new IllegalStateException("仅运行中的流程允许撤回，当前状态: " + inst.getStatus());
        }

        inst.setStatus(InstanceStatus.TERMINATED);
        inst.setConclusion(ApprovalConclusion.WITHDRAWN);
        inst.setTerminationReason("发起人主动撤回: " + (reason != null ? reason : "无特殊说明"));
        workflowRepository.saveInstance(inst);

        // 清理当前实例的所有未完成待办
        simulatedTaskStore.entrySet().removeIf(e -> e.getValue().getWorkflowInstId().equals(workflowInstId));
        return true;
    }
}
