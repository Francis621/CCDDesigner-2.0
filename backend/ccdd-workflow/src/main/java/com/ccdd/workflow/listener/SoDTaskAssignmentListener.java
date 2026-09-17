package com.ccdd.workflow.listener;

import com.ccdd.workflow.entity.WorkflowInstanceEntity;
import com.ccdd.workflow.exception.SelfApprovalBlockedException;
import com.ccdd.workflow.repository.WorkflowRepository;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * M24: 职责分离 (SoD) 动态校验任务监听器
 * 落实规则 SoD-01: 流程发起人不得审批自身提交的发布申请与工程修改单 (AT-16 守卫)
 */
@Component("soDTaskAssignmentListener")
public class SoDTaskAssignmentListener implements TaskListener {

    private final WorkflowRepository workflowRepository;

    @Autowired
    public SoDTaskAssignmentListener(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        String procInstId = delegateTask.getProcessInstanceId();
        if (procInstId == null) {
            return;
        }

        WorkflowInstanceEntity wfInst = workflowRepository.findInstanceByProcInstId(procInstId).orElse(null);
        if (wfInst == null) {
            return;
        }

        String initiatorId = wfInst.getInitiatorId();
        String assignee = delegateTask.getAssignee();

        // 规则 SoD-01 判定 1: 若当前任务直接指派给发起人自身，坚决阻断自审
        if (assignee != null && assignee.equalsIgnoreCase(initiatorId)) {
            throw new SelfApprovalBlockedException(
                    String.format("SoD-01 职责分离阻断：任务 [%s] 禁止自发自批！流程发起人 [%s] 严禁作为该节点唯一审批人。",
                            delegateTask.getName(), initiatorId)
            );
        }

        // 规则 SoD-01 判定 2: 若候选人集合中包含发起人，自动剥夺其候选审批人资格
        if (delegateTask.getCandidates() != null) {
            delegateTask.getCandidates().removeIf(candidate ->
                    candidate.getUserId() != null && candidate.getUserId().equalsIgnoreCase(initiatorId)
            );
        }
    }
}
