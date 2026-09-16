package com.ccdd.project.entity;

import java.time.Instant;

/**
 * 任务交付物规约要求实体 (DeliverableRequirement)
 * 映射 plm_project.deliverable_requirement
 */
public class DeliverableRequirementEntity {

    private Long delivReqId;
    private Long taskId;
    private String requirementCode;
    private String name;
    private String deliverableType; // SYSML_MODEL, REQ_BASELINE, EBOM_STRUCT, SIM_REPORT, CAD_DRAWING, VERIFICATION_ASSESSMENT
    private Boolean isMandatory;
    private String targetSecurityLevel;
    private Instant createdAt;

    public DeliverableRequirementEntity() {
    }

    public DeliverableRequirementEntity(Long delivReqId, Long taskId, String requirementCode, String name,
                                        String deliverableType, Boolean isMandatory, String targetSecurityLevel,
                                        Instant createdAt) {
        this.delivReqId = delivReqId;
        this.taskId = taskId;
        this.requirementCode = requirementCode;
        this.name = name;
        this.deliverableType = deliverableType;
        this.isMandatory = isMandatory;
        this.targetSecurityLevel = targetSecurityLevel;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long delivReqId;
        private Long taskId;
        private String requirementCode;
        private String name;
        private String deliverableType;
        private Boolean isMandatory = true;
        private String targetSecurityLevel = "INTERNAL";
        private Instant createdAt = Instant.now();

        public Builder delivReqId(Long delivReqId) { this.delivReqId = delivReqId; return this; }
        public Builder taskId(Long taskId) { this.taskId = taskId; return this; }
        public Builder requirementCode(String requirementCode) { this.requirementCode = requirementCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder deliverableType(String deliverableType) { this.deliverableType = deliverableType; return this; }
        public Builder isMandatory(Boolean isMandatory) { this.isMandatory = isMandatory; return this; }
        public Builder targetSecurityLevel(String targetSecurityLevel) { this.targetSecurityLevel = targetSecurityLevel; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public DeliverableRequirementEntity build() {
            return new DeliverableRequirementEntity(delivReqId, taskId, requirementCode, name, deliverableType, isMandatory, targetSecurityLevel, createdAt);
        }
    }

    public Long getDelivReqId() { return delivReqId; }
    public void setDelivReqId(Long delivReqId) { this.delivReqId = delivReqId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getRequirementCode() { return requirementCode; }
    public void setRequirementCode(String requirementCode) { this.requirementCode = requirementCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDeliverableType() { return deliverableType; }
    public void setDeliverableType(String deliverableType) { this.deliverableType = deliverableType; }
    public Boolean getIsMandatory() { return isMandatory; }
    public void setIsMandatory(Boolean mandatory) { isMandatory = mandatory; }
    public String getTargetSecurityLevel() { return targetSecurityLevel; }
    public void setTargetSecurityLevel(String targetSecurityLevel) { this.targetSecurityLevel = targetSecurityLevel; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
