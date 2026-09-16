package com.ccdd.baseline.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 闭包完备性校验结果 DTO
 */
public class ClosureCheckResultDto {

    private Long baselineId;
    private Boolean isComplete;
    private String closureHash; // 64位十六进制 Merkle 闭包哈希根
    private Integer totalMemberCount;
    private Integer totalRelationCount;
    private List<ClosureIssue> issues;

    public ClosureCheckResultDto() {
        this.issues = new ArrayList<>();
    }

    public ClosureCheckResultDto(Long baselineId, Boolean isComplete, String closureHash,
                                 Integer totalMemberCount, Integer totalRelationCount, List<ClosureIssue> issues) {
        this.baselineId = baselineId;
        this.isComplete = isComplete;
        this.closureHash = closureHash;
        this.totalMemberCount = totalMemberCount;
        this.totalRelationCount = totalRelationCount;
        this.issues = issues != null ? issues : new ArrayList<>();
    }

    public Long getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(Long baselineId) {
        this.baselineId = baselineId;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
    }

    public String getClosureHash() {
        return closureHash;
    }

    public void setClosureHash(String closureHash) {
        this.closureHash = closureHash;
    }

    public Integer getTotalMemberCount() {
        return totalMemberCount;
    }

    public void setTotalMemberCount(Integer totalMemberCount) {
        this.totalMemberCount = totalMemberCount;
    }

    public Integer getTotalRelationCount() {
        return totalRelationCount;
    }

    public void setTotalRelationCount(Integer totalRelationCount) {
        this.totalRelationCount = totalRelationCount;
    }

    public List<ClosureIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<ClosureIssue> issues) {
        this.issues = issues;
    }

    public static class ClosureIssue {
        private String severity; // ERROR, WARNING
        private String issueType; // DRAFT_STATE, MISSING_DEPENDENCY, DANGLING_REFERENCE, CHECKSUM_MISMATCH
        private String targetIdentifier;
        private String message;

        public ClosureIssue() {
        }

        public ClosureIssue(String severity, String issueType, String targetIdentifier, String message) {
            this.severity = severity;
            this.issueType = issueType;
            this.targetIdentifier = targetIdentifier;
            this.message = message;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getIssueType() {
            return issueType;
        }

        public void setIssueType(String issueType) {
            this.issueType = issueType;
        }

        public String getTargetIdentifier() {
            return targetIdentifier;
        }

        public void setTargetIdentifier(String targetIdentifier) {
            this.targetIdentifier = targetIdentifier;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
