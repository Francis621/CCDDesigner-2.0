package com.ccdd.change.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 影响面拓扑分析响应结果 DTO (OpenAPI §8.1)
 */
public class ImpactAnalysisResultDto {

    private Long ecoId;
    private Integer candidateCount;
    private Boolean isTruncated;
    private List<ImpactItemSummary> impactItems;

    public ImpactAnalysisResultDto() {
        this.impactItems = new ArrayList<>();
        this.isTruncated = false;
        this.candidateCount = 0;
    }

    public ImpactAnalysisResultDto(Long ecoId, Integer candidateCount, Boolean isTruncated, List<ImpactItemSummary> impactItems) {
        this.ecoId = ecoId;
        this.candidateCount = candidateCount;
        this.isTruncated = isTruncated;
        this.impactItems = impactItems != null ? impactItems : new ArrayList<>();
    }

    public Long getEcoId() {
        return ecoId;
    }

    public void setEcoId(Long ecoId) {
        this.ecoId = ecoId;
    }

    public Integer getCandidateCount() {
        return candidateCount;
    }

    public void setCandidateCount(Integer candidateCount) {
        this.candidateCount = candidateCount;
    }

    public Boolean getIsTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(Boolean truncated) {
        isTruncated = truncated;
    }

    public List<ImpactItemSummary> getImpactItems() {
        return impactItems;
    }

    public void setImpactItems(List<ImpactItemSummary> impactItems) {
        this.impactItems = impactItems;
    }

    public static class ImpactItemSummary {
        private Long impactItemId;
        private Long candidateRevisionId;
        private String objectType;
        private String businessCode;
        private String discipline;
        private String propagationPath;
        private Integer traversalDepth;
        private Boolean isAssessed;

        public ImpactItemSummary() {
        }

        public ImpactItemSummary(Long impactItemId, Long candidateRevisionId, String objectType,
                                 String businessCode, String discipline, String propagationPath,
                                 Integer traversalDepth, Boolean isAssessed) {
            this.impactItemId = impactItemId;
            this.candidateRevisionId = candidateRevisionId;
            this.objectType = objectType;
            this.businessCode = businessCode;
            this.discipline = discipline;
            this.propagationPath = propagationPath;
            this.traversalDepth = traversalDepth;
            this.isAssessed = isAssessed;
        }

        public Long getImpactItemId() {
            return impactItemId;
        }

        public void setImpactItemId(Long impactItemId) {
            this.impactItemId = impactItemId;
        }

        public Long getCandidateRevisionId() {
            return candidateRevisionId;
        }

        public void setCandidateRevisionId(Long candidateRevisionId) {
            this.candidateRevisionId = candidateRevisionId;
        }

        public String getObjectType() {
            return objectType;
        }

        public void setObjectType(String objectType) {
            this.objectType = objectType;
        }

        public String getBusinessCode() {
            return businessCode;
        }

        public void setBusinessCode(String businessCode) {
            this.businessCode = businessCode;
        }

        public String getDiscipline() {
            return discipline;
        }

        public void setDiscipline(String discipline) {
            this.discipline = discipline;
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

        public Boolean getIsAssessed() {
            return isAssessed;
        }

        public void setIsAssessed(Boolean assessed) {
            isAssessed = assessed;
        }
    }
}
