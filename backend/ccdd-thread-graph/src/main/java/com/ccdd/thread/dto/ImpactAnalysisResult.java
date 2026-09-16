package com.ccdd.thread.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 变更波及推演结果 DTO
 */
public class ImpactAnalysisResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String rootNodeId;
    private int totalImpactedNodes;
    private int directImpactedCount;
    private int indirectImpactedCount;
    private List<String> impactedBaselineIds;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private List<TraversePathStep> impactPaths;

    public ImpactAnalysisResult() {
    }

    public ImpactAnalysisResult(String rootNodeId, int totalImpactedNodes, int directImpactedCount,
                                int indirectImpactedCount, List<String> impactedBaselineIds,
                                String riskLevel, List<TraversePathStep> impactPaths) {
        this.rootNodeId = rootNodeId;
        this.totalImpactedNodes = totalImpactedNodes;
        this.directImpactedCount = directImpactedCount;
        this.indirectImpactedCount = indirectImpactedCount;
        this.impactedBaselineIds = impactedBaselineIds;
        this.riskLevel = riskLevel;
        this.impactPaths = impactPaths;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String rootNodeId;
        private int totalImpactedNodes;
        private int directImpactedCount;
        private int indirectImpactedCount;
        private List<String> impactedBaselineIds;
        private String riskLevel;
        private List<TraversePathStep> impactPaths;

        public Builder rootNodeId(String rootNodeId) { this.rootNodeId = rootNodeId; return this; }
        public Builder totalImpactedNodes(int totalImpactedNodes) { this.totalImpactedNodes = totalImpactedNodes; return this; }
        public Builder directImpactedCount(int directImpactedCount) { this.directImpactedCount = directImpactedCount; return this; }
        public Builder indirectImpactedCount(int indirectImpactedCount) { this.indirectImpactedCount = indirectImpactedCount; return this; }
        public Builder impactedBaselineIds(List<String> impactedBaselineIds) { this.impactedBaselineIds = impactedBaselineIds; return this; }
        public Builder riskLevel(String riskLevel) { this.riskLevel = riskLevel; return this; }
        public Builder impactPaths(List<TraversePathStep> impactPaths) { this.impactPaths = impactPaths; return this; }

        public ImpactAnalysisResult build() {
            return new ImpactAnalysisResult(rootNodeId, totalImpactedNodes, directImpactedCount,
                    indirectImpactedCount, impactedBaselineIds, riskLevel, impactPaths);
        }
    }

    public String getRootNodeId() { return rootNodeId; }
    public void setRootNodeId(String rootNodeId) { this.rootNodeId = rootNodeId; }

    public int getTotalImpactedNodes() { return totalImpactedNodes; }
    public void setTotalImpactedNodes(int totalImpactedNodes) { this.totalImpactedNodes = totalImpactedNodes; }

    public int getDirectImpactedCount() { return directImpactedCount; }
    public void setDirectImpactedCount(int directImpactedCount) { this.directImpactedCount = directImpactedCount; }

    public int getIndirectImpactedCount() { return indirectImpactedCount; }
    public void setIndirectImpactedCount(int indirectImpactedCount) { this.indirectImpactedCount = indirectImpactedCount; }

    public List<String> getImpactedBaselineIds() { return impactedBaselineIds; }
    public void setImpactedBaselineIds(List<String> impactedBaselineIds) { this.impactedBaselineIds = impactedBaselineIds; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public List<TraversePathStep> getImpactPaths() { return impactPaths; }
    public void setImpactPaths(List<TraversePathStep> impactPaths) { this.impactPaths = impactPaths; }
}
