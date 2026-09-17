package com.ccdd.workflow.repository;

import com.ccdd.workflow.entity.ApprovalConclusion;
import com.ccdd.workflow.entity.ApprovalDecisionEntity;
import com.ccdd.workflow.entity.CallbackIdempotencyEntity;
import com.ccdd.workflow.entity.DefinitionBindingEntity;
import com.ccdd.workflow.entity.InstanceStatus;
import com.ccdd.workflow.entity.SignStrategy;
import com.ccdd.workflow.entity.WorkflowActionRecordEntity;
import com.ccdd.workflow.entity.WorkflowInstanceEntity;
import com.ccdd.workflow.exception.CrossMutationForbiddenException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * M24: 工作流与审批仓储层
 * 提供并发安全内存映射，且与 V1.10.0 迁移脚本种子数据完全对齐
 */
@Repository
public class WorkflowRepository {

    private final Map<Long, DefinitionBindingEntity> bindingStore = new ConcurrentHashMap<>();
    private final Map<Long, WorkflowInstanceEntity> instanceStore = new ConcurrentHashMap<>();
    private final Map<Long, ApprovalDecisionEntity> decisionStore = new ConcurrentHashMap<>();
    private final Map<Long, WorkflowActionRecordEntity> actionRecordStore = new ConcurrentHashMap<>();
    private final Map<String, CallbackIdempotencyEntity> idempotencyStore = new ConcurrentHashMap<>();

    // 模拟机床业务主表最新实时哈希映射 (用于 AT-16 审批中防篡改比对)
    private final Map<String, String> liveTargetEntityHashes = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(90000L);

    public WorkflowRepository() {
        initSeedData();
    }

    private void initSeedData() {
        Instant now = Instant.now();

        // 1. 流程绑定模板
        DefinitionBindingEntity b1 = new DefinitionBindingEntity(
                101L, "ModelRelease", "STANDARD_RELEASE", "PROC_MODEL_RELEASE_APPROVAL",
                1, SignStrategy.UNANIMOUS, null, true, "数控机床多专业系统架构与接口定义四级受控会签流", now.minus(10, ChronoUnit.DAYS)
        );
        DefinitionBindingEntity b2 = new DefinitionBindingEntity(
                102L, "ChangeOrder", "MAJOR_CHANGE", "PROC_ECO_CHANGE_APPROVAL",
                1, SignStrategy.UNANIMOUS, null, true, "高端机床重大工程变更单(ECO)跨学科与CCB处置审批流", now.minus(10, ChronoUnit.DAYS)
        );
        DefinitionBindingEntity b3 = new DefinitionBindingEntity(
                103L, "ChangeOrder", "STANDARD_RELEASE", "PROC_ECO_CHANGE_APPROVAL",
                1, SignStrategy.UNANIMOUS, null, true, "高端机床常规工程变更与技术处理审查流", now.minus(10, ChronoUnit.DAYS)
        );
        DefinitionBindingEntity b4 = new DefinitionBindingEntity(
                104L, "Baseline", "STANDARD_RELEASE", "PROC_BASELINE_RELEASE_APPROVAL",
                1, SignStrategy.UNANIMOUS, null, true, "高端数控机床型号基线(功能/分配/产品基线)受控固化审批流", now.minus(10, ChronoUnit.DAYS)
        );
        DefinitionBindingEntity b5 = new DefinitionBindingEntity(
                105L, "DocRevision", "STANDARD_RELEASE", "PROC_DOC_REVISION_APPROVAL",
                1, SignStrategy.UNANIMOUS, null, true, "高端机床受控工程图样与设计规范电子会签审签流", now.minus(10, ChronoUnit.DAYS)
        );
        bindingStore.put(b1.getBindingId(), b1);
        bindingStore.put(b2.getBindingId(), b2);
        bindingStore.put(b3.getBindingId(), b3);
        bindingStore.put(b4.getBindingId(), b4);
        bindingStore.put(b5.getBindingId(), b5);

        // 2. 种子实例 1: 流转中的 ECO-2026-0042 会签审批
        Long wf1Id = 77001L;
        String initialHash1 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        WorkflowInstanceEntity inst1 = new WorkflowInstanceEntity(
                wf1Id, "prc_inst_eco_0042", 102L, "ChangeOrder", 8001L,
                "ECO-2026-0042", initialHash1, "VMC_ENTERPRISE", "chief_designer",
                InstanceStatus.RUNNING, null, null, now.minus(2, ChronoUnit.HOURS), null
        );
        instanceStore.put(wf1Id, inst1);
        liveTargetEntityHashes.put("ChangeOrder:8001", initialHash1);

        WorkflowActionRecordEntity rec1 = new WorkflowActionRecordEntity(
                78001L, wf1Id, "task_eco_mech_01", "机械系统主管工程师审查", "lead_analyst", "APPROVE",
                "已完成陶瓷球轴承与主轴套筒配合公差复核，刚度与预紧力设计满足15000rpm工况要求，同意实施。", null, null, now.minus(1, ChronoUnit.HOURS)
        );
        actionRecordStore.put(rec1.getActionRecordId(), rec1);

        // 3. 种子实例 2: 已完成且已签发电子凭据的 VMC1000 架构基线发布 (REL-VMC1000-SYS-001)
        Long wf2Id = 77002L;
        String initialHash2 = "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01";
        WorkflowInstanceEntity inst2 = new WorkflowInstanceEntity(
                wf2Id, "prc_inst_rel_0001", 101L, "ModelRelease", 5001L,
                "REL-VMC1000-SYS-001", initialHash2, "VMC_ENTERPRISE", "sys_architect",
                InstanceStatus.COMPLETED, ApprovalConclusion.APPROVED, null,
                now.minus(3, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS)
        );
        instanceStore.put(wf2Id, inst2);
        liveTargetEntityHashes.put("ModelRelease:5001", initialHash2);

        ApprovalDecisionEntity dec2 = new ApprovalDecisionEntity(
                88001L, wf2Id, "ModelRelease", 5001L, initialHash2,
                ApprovalConclusion.APPROVED, true, now.minus(2, ChronoUnit.DAYS), "CONSUME-ACT-M06-REL-5001",
                "SM2_SIG_304502204c382901820192a830192830192830192830192830192830192a019283",
                "a7c2b3e891238491820391820391820391820391820391820391820391820391", now.minus(2, ChronoUnit.DAYS)
        );
        decisionStore.put(dec2.getDecisionTicketId(), dec2);

        CallbackIdempotencyEntity idemp2 = new CallbackIdempotencyEntity(
                "ACTION:77002:v1", wf2Id, "NOTIFY_RELEASE", "SUCCESS", null, now.minus(2, ChronoUnit.DAYS)
        );
        idempotencyStore.put(idemp2.getIdempotencyKey(), idemp2);
    }

    public Long nextId() {
        return idGenerator.incrementAndGet();
    }

    // ====== DefinitionBinding ======
    public Optional<DefinitionBindingEntity> findBinding(String targetObjectType, String businessCategory) {
        if (targetObjectType == null) {
            return Optional.empty();
        }
        // 1. 优先精确匹配 (targetObjectType + businessCategory)
        Optional<DefinitionBindingEntity> exactMatch = bindingStore.values().stream()
                .filter(b -> b.getIsActive() && b.getTargetObjectType().equalsIgnoreCase(targetObjectType))
                .filter(b -> businessCategory == null || b.getBusinessCategory().equalsIgnoreCase(businessCategory))
                .findFirst();
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        // 2. 容错回退：同机床业务对象类型的任一有效流程绑定
        Optional<DefinitionBindingEntity> typeFallback = bindingStore.values().stream()
                .filter(b -> b.getIsActive() && b.getTargetObjectType().equalsIgnoreCase(targetObjectType))
                .findFirst();
        if (typeFallback.isPresent()) {
            return typeFallback;
        }

        // 3. 系统级通用受控审查流程兜底（绝不抛出阻断性空指针或未配置异常）
        return bindingStore.values().stream()
                .filter(DefinitionBindingEntity::getIsActive)
                .findFirst();
    }

    public List<DefinitionBindingEntity> findAllBindings() {
        return new ArrayList<>(bindingStore.values());
    }

    public DefinitionBindingEntity saveBinding(DefinitionBindingEntity entity) {
        if (entity.getBindingId() == null) {
            entity.setBindingId(nextId());
        }
        bindingStore.put(entity.getBindingId(), entity);
        return entity;
    }

    // ====== WorkflowInstance ======
    public WorkflowInstanceEntity saveInstance(WorkflowInstanceEntity entity) {
        if (entity.getWorkflowInstId() == null) {
            entity.setWorkflowInstId(nextId());
        }
        if (entity.getStartedAt() == null) {
            entity.setStartedAt(Instant.now());
        }
        instanceStore.put(entity.getWorkflowInstId(), entity);
        // 记录业务实体快照初始哈希
        if (entity.getTargetObjectType() != null && entity.getTargetObjectId() != null && entity.getTargetContentHash() != null) {
            liveTargetEntityHashes.put(entity.getTargetObjectType() + ":" + entity.getTargetObjectId(), entity.getTargetContentHash());
        }
        return entity;
    }

    public Optional<WorkflowInstanceEntity> findInstanceById(Long workflowInstId) {
        return Optional.ofNullable(instanceStore.get(workflowInstId));
    }

    public Optional<WorkflowInstanceEntity> findInstanceByProcInstId(String flowableProcInstId) {
        return instanceStore.values().stream()
                .filter(i -> i.getFlowableProcInstId().equals(flowableProcInstId))
                .findFirst();
    }

    public List<WorkflowInstanceEntity> findAllInstances() {
        return instanceStore.values().stream()
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .collect(Collectors.toList());
    }

    // ====== ApprovalDecision ======
    public ApprovalDecisionEntity saveDecision(ApprovalDecisionEntity decision) {
        if (decision.getDecisionTicketId() == null) {
            decision.setDecisionTicketId(nextId());
        }
        if (decision.getDecidedAt() == null) {
            decision.setDecidedAt(Instant.now());
        }
        decisionStore.put(decision.getDecisionTicketId(), decision);
        return decision;
    }

    public Optional<ApprovalDecisionEntity> findDecisionById(Long ticketId) {
        return Optional.ofNullable(decisionStore.get(ticketId));
    }

    public Optional<ApprovalDecisionEntity> findDecisionByWorkflowInstId(Long workflowInstId) {
        return decisionStore.values().stream()
                .filter(d -> d.getWorkflowInstId().equals(workflowInstId))
                .findFirst();
    }

    // ====== WorkflowActionRecord ======
    public WorkflowActionRecordEntity saveActionRecord(WorkflowActionRecordEntity record) {
        if (record.getActionRecordId() == null) {
            record.setActionRecordId(nextId());
        }
        if (record.getVotedAt() == null) {
            record.setVotedAt(Instant.now());
        }
        actionRecordStore.put(record.getActionRecordId(), record);
        return record;
    }

    public List<WorkflowActionRecordEntity> findActionRecordsByInstId(Long workflowInstId) {
        return actionRecordStore.values().stream()
                .filter(r -> r.getWorkflowInstId().equals(workflowInstId))
                .sorted((a, b) -> a.getVotedAt().compareTo(b.getVotedAt()))
                .collect(Collectors.toList());
    }

    // ====== CallbackIdempotency ======
    public Optional<CallbackIdempotencyEntity> findIdempotency(String idempotencyKey) {
        return Optional.ofNullable(idempotencyStore.get(idempotencyKey));
    }

    public CallbackIdempotencyEntity saveIdempotency(CallbackIdempotencyEntity entity) {
        if (entity.getProcessedAt() == null) {
            entity.setProcessedAt(Instant.now());
        }
        idempotencyStore.put(entity.getIdempotencyKey(), entity);
        return entity;
    }

    // ====== AT-16 防篡改支持接口 ======
    public String getLiveTargetEntityHash(String targetObjectType, Long targetObjectId) {
        return liveTargetEntityHashes.get(targetObjectType + ":" + targetObjectId);
    }

    public void setLiveTargetEntityHash(String targetObjectType, Long targetObjectId, String newHash) {
        liveTargetEntityHashes.put(targetObjectType + ":" + targetObjectId, newHash);
    }

    // ====== CST-M24-01 防越权写防御接口 ======
    public void attemptDirectMutationOfBusinessTable(String tableName) {
        // 模拟数据库触发器 fn_prevent_workflow_cross_mutation 的阻断行为
        throw new CrossMutationForbiddenException(
                "Architecture Security Violation [CST-M24-01]: Workflow engine is strictly PROHIBITED from directly modifying business domain table ["
                        + tableName + "]. Workflow must only issue ApprovalDecision tickets."
        );
    }
}
