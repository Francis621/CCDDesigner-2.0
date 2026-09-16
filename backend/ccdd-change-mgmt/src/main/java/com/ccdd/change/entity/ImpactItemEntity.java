package com.ccdd.change.entity;

/**
 * M22 变更影响候选表实体 (ImpactItem - 接收 M23 拓扑推演)
 */
public class ImpactItemEntity {

    private Long impactItemId;
    private Long ecoId;
    private Long candidateRevisionId;
    private String objectTypeCode; // Requirement, ModelRelease, PartRevision, VerificationCase 等
    private String businessCode;
    private String propagationPath; // 追溯路径 JSON 或字符串展示
    private Integer traversalDepth;
    private String assignedDiscipline; // MECHANICAL, ELECTRICAL, CONTROL, SIMULATION
    private Boolean isAssessed;

    public ImpactItemEntity() {
    }

    public ImpactItemEntity(Long impactItemId, Long ecoId, Long candidateRevisionId,
                            String objectTypeCode, String businessCode, String propagationPath,
                            Integer traversalDepth, String assignedDiscipline, Boolean isAssessed) {
        this.impactItemId = impactItemId;
        this.ecoId = ecoId;
        this.candidateRevisionId = candidateRevisionId;
        this.objectTypeCode = objectTypeCode;
        this.businessCode = businessCode;
        this.propagationPath = propagationPath;
        this.traversalDepth = traversalDepth;
        this.assignedDiscipline = assignedDiscipline;
        this.isAssessed = isAssessed;
    }

    public Long getImpactItemId() {
        return impactItemId;
    }

    public void setImpactItemId(Long impactItemId) {
        this.impactItemId = impactItemId;
    }

    public Long getEcoId() {
        return ecoId;
    }

    public void setEcoId(Long ecoId) {
        this.ecoId = ecoId;
    }

    public Long getCandidateRevisionId() {
        return candidateRevisionId;
    }

    public void setCandidateRevisionId(Long candidateRevisionId) {
        this.candidateRevisionId = candidateRevisionId;
    }

    public String getObjectTypeCode() {
        return objectTypeCode;
    }

    public void setObjectTypeCode(String objectTypeCode) {
        this.objectTypeCode = objectTypeCode;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getPropagationPath() {
        return propagationPath;
    }

    public void setPropagationPath(String propagationPath) {
        this.propagationPath = propagationPath;
    }

    public Integer getTraversalDepth() {
        return traversalDepth;
    }

    public void setTraversalDepth(Integer traversalDepth) {
        this.traversalDepth = traversalDepth;
    }

    public String getAssignedDiscipline() {
        return assignedDiscipline;
    }

    public void setAssignedDiscipline(String assignedDiscipline) {
        this.assignedDiscipline = assignedDiscipline;
    }

    public Boolean getIsAssessed() {
        return isAssessed;
    }

    public void setIsAssessed(Boolean assessed) {
        isAssessed = assessed;
    }
}
