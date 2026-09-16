package com.ccdd.bom.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 配置求解协议 DTO 集合 (落实 D05 专项规格)
 */
public class ConfigurationEvaluationDto {

    public static class EvaluateConfigurationRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private String orderId;
        private Long structureRevisionId;
        private Long ruleSetRevisionId;
        private Map<String, Object> selectedFeatures;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public Long getStructureRevisionId() { return structureRevisionId; }
        public void setStructureRevisionId(Long structureRevisionId) { this.structureRevisionId = structureRevisionId; }
        public Long getRuleSetRevisionId() { return ruleSetRevisionId; }
        public void setRuleSetRevisionId(Long ruleSetRevisionId) { this.ruleSetRevisionId = ruleSetRevisionId; }
        public Map<String, Object> getSelectedFeatures() { return selectedFeatures; }
        public void setSelectedFeatures(Map<String, Object> selectedFeatures) { this.selectedFeatures = selectedFeatures; }
    }

    public static class EvaluateConfigurationResponse implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long resultId;
        private String resultDigestSha256;
        private Integer solverDurationMs;
        private Integer totalLinesResolved;
        private List<ResolvedBomLine> resolved100BomTree;

        public EvaluateConfigurationResponse() {}
        public EvaluateConfigurationResponse(Long resultId, String resultDigestSha256, Integer solverDurationMs,
                                             Integer totalLinesResolved, List<ResolvedBomLine> resolved100BomTree) {
            this.resultId = resultId;
            this.resultDigestSha256 = resultDigestSha256;
            this.solverDurationMs = solverDurationMs;
            this.totalLinesResolved = totalLinesResolved;
            this.resolved100BomTree = resolved100BomTree;
        }

        public Long getResultId() { return resultId; }
        public void setResultId(Long resultId) { this.resultId = resultId; }
        public String getResultDigestSha256() { return resultDigestSha256; }
        public void setResultDigestSha256(String resultDigestSha256) { this.resultDigestSha256 = resultDigestSha256; }
        public Integer getSolverDurationMs() { return solverDurationMs; }
        public void setSolverDurationMs(Integer solverDurationMs) { this.solverDurationMs = solverDurationMs; }
        public Integer getTotalLinesResolved() { return totalLinesResolved; }
        public void setTotalLinesResolved(Integer totalLinesResolved) { this.totalLinesResolved = totalLinesResolved; }
        public List<ResolvedBomLine> getResolved100BomTree() { return resolved100BomTree; }
        public void setResolved100BomTree(List<ResolvedBomLine> resolved100BomTree) { this.resolved100BomTree = resolved100BomTree; }
    }

    public static class RuleValidationRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long ruleSetRevisionId;
        private Map<String, Object> testFeatures;

        public Long getRuleSetRevisionId() { return ruleSetRevisionId; }
        public void setRuleSetRevisionId(Long ruleSetRevisionId) { this.ruleSetRevisionId = ruleSetRevisionId; }
        public Map<String, Object> getTestFeatures() { return testFeatures; }
        public void setTestFeatures(Map<String, Object> testFeatures) { this.testFeatures = testFeatures; }
    }

    public static class RuleValidationResponse implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean passed;
        private int conflictCount;
        private List<String> conflictMessages;

        public RuleValidationResponse() {}
        public RuleValidationResponse(boolean passed, int conflictCount, List<String> conflictMessages) {
            this.passed = passed;
            this.conflictCount = conflictCount;
            this.conflictMessages = conflictMessages;
        }

        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public int getConflictCount() { return conflictCount; }
        public void setConflictCount(int conflictCount) { this.conflictCount = conflictCount; }
        public List<String> getConflictMessages() { return conflictMessages; }
        public void setConflictMessages(List<String> conflictMessages) { this.conflictMessages = conflictMessages; }
    }
}
