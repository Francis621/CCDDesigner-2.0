package com.ccdd.workflow.dto;

import com.ccdd.workflow.entity.ApprovalDecisionEntity;
import com.ccdd.workflow.entity.WorkflowActionRecordEntity;
import com.ccdd.workflow.entity.WorkflowInstanceEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程实例完整详情 DTO (包含实例快照、各节点审批流水与最终防伪凭据)
 */
public class WorkflowInstanceDetailDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private WorkflowInstanceEntity instance;
    private List<WorkflowActionRecordEntity> actionRecords = new ArrayList<>();
    private List<WorkflowTaskItemDto> activeTasks = new ArrayList<>();
    private ApprovalDecisionEntity decision;

    public WorkflowInstanceDetailDto() {
    }

    public WorkflowInstanceDetailDto(WorkflowInstanceEntity instance, List<WorkflowActionRecordEntity> actionRecords,
                                     List<WorkflowTaskItemDto> activeTasks, ApprovalDecisionEntity decision) {
        this.instance = instance;
        this.actionRecords = actionRecords;
        this.activeTasks = activeTasks;
        this.decision = decision;
    }

    public WorkflowInstanceEntity getInstance() {
        return instance;
    }

    public void setInstance(WorkflowInstanceEntity instance) {
        this.instance = instance;
    }

    public List<WorkflowActionRecordEntity> getActionRecords() {
        return actionRecords;
    }

    public void setActionRecords(List<WorkflowActionRecordEntity> actionRecords) {
        this.actionRecords = actionRecords;
    }

    public List<WorkflowTaskItemDto> getActiveTasks() {
        return activeTasks;
    }

    public void setActiveTasks(List<WorkflowTaskItemDto> activeTasks) {
        this.activeTasks = activeTasks;
    }

    public ApprovalDecisionEntity getDecision() {
        return decision;
    }

    public void setDecision(ApprovalDecisionEntity decision) {
        this.decision = decision;
    }
}
