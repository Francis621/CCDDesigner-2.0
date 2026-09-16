package com.ccdd.project.dto;

import com.ccdd.project.entity.GateDecisionType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 阶段门评审决策签署请求 DTO
 */
public class RecordGateDecisionRequest {

    private GateDecisionType decisionType;
    private String decisionNotes;
    private Long evaluatedBaselineId;
    private Long approvalTicketId;
    private String allowedScope; // CONDITIONAL_PASS 时强制要求
    private List<CreateActionItemDto> actionItems = new ArrayList<>();

    public RecordGateDecisionRequest() {
    }

    public static class CreateActionItemDto {
        private String title;
        private String description;
        private String ownerId;
        private String approverId;
        private LocalDate dueDate;

        public CreateActionItemDto() {
        }

        public CreateActionItemDto(String title, String description, String ownerId, String approverId, LocalDate dueDate) {
            this.title = title;
            this.description = description;
            this.ownerId = ownerId;
            this.approverId = approverId;
            this.dueDate = dueDate;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public String getApproverId() { return approverId; }
        public void setApproverId(String approverId) { this.approverId = approverId; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    }

    public GateDecisionType getDecisionType() { return decisionType; }
    public void setDecisionType(GateDecisionType decisionType) { this.decisionType = decisionType; }
    public String getDecisionNotes() { return decisionNotes; }
    public void setDecisionNotes(String decisionNotes) { this.decisionNotes = decisionNotes; }
    public Long getEvaluatedBaselineId() { return evaluatedBaselineId; }
    public void setEvaluatedBaselineId(Long evaluatedBaselineId) { this.evaluatedBaselineId = evaluatedBaselineId; }
    public Long getApprovalTicketId() { return approvalTicketId; }
    public void setApprovalTicketId(Long approvalTicketId) { this.approvalTicketId = approvalTicketId; }
    public String getAllowedScope() { return allowedScope; }
    public void setAllowedScope(String allowedScope) { this.allowedScope = allowedScope; }
    public List<CreateActionItemDto> getActionItems() { return actionItems; }
    public void setActionItems(List<CreateActionItemDto> actionItems) { this.actionItems = actionItems; }
}
