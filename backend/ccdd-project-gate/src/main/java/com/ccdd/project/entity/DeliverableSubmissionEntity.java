package com.ccdd.project.entity;

import java.time.Instant;

/**
 * 任务交付物提交记录实体 (DeliverableSubmission)
 * 映射 plm_project.deliverable_submission
 * 规范：支持多版本演进，旧版本自动标记 isLatest=false (SUPERSEDED)，严禁物理删除
 */
public class DeliverableSubmissionEntity {

    private Long submissionId;
    private Long delivReqId;
    private Long revisionId;
    private Long baselineId;
    private String artifactHash;
    private String submissionNotes;
    private Boolean isLatest;
    private String submittedBy;
    private Instant submittedAt;

    public DeliverableSubmissionEntity() {
    }

    public DeliverableSubmissionEntity(Long submissionId, Long delivReqId, Long revisionId, Long baselineId,
                                       String artifactHash, String submissionNotes, Boolean isLatest,
                                       String submittedBy, Instant submittedAt) {
        this.submissionId = submissionId;
        this.delivReqId = delivReqId;
        this.revisionId = revisionId;
        this.baselineId = baselineId;
        this.artifactHash = artifactHash;
        this.submissionNotes = submissionNotes;
        this.isLatest = isLatest;
        this.submittedBy = submittedBy;
        this.submittedAt = submittedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long submissionId;
        private Long delivReqId;
        private Long revisionId;
        private Long baselineId;
        private String artifactHash;
        private String submissionNotes;
        private Boolean isLatest = true;
        private String submittedBy;
        private Instant submittedAt = Instant.now();

        public Builder submissionId(Long submissionId) { this.submissionId = submissionId; return this; }
        public Builder delivReqId(Long delivReqId) { this.delivReqId = delivReqId; return this; }
        public Builder revisionId(Long revisionId) { this.revisionId = revisionId; return this; }
        public Builder baselineId(Long baselineId) { this.baselineId = baselineId; return this; }
        public Builder artifactHash(String artifactHash) { this.artifactHash = artifactHash; return this; }
        public Builder submissionNotes(String submissionNotes) { this.submissionNotes = submissionNotes; return this; }
        public Builder isLatest(Boolean isLatest) { this.isLatest = isLatest; return this; }
        public Builder submittedBy(String submittedBy) { this.submittedBy = submittedBy; return this; }
        public Builder submittedAt(Instant submittedAt) { this.submittedAt = submittedAt; return this; }

        public DeliverableSubmissionEntity build() {
            return new DeliverableSubmissionEntity(submissionId, delivReqId, revisionId, baselineId, artifactHash, submissionNotes, isLatest, submittedBy, submittedAt);
        }
    }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    public Long getDelivReqId() { return delivReqId; }
    public void setDelivReqId(Long delivReqId) { this.delivReqId = delivReqId; }
    public Long getRevisionId() { return revisionId; }
    public void setRevisionId(Long revisionId) { this.revisionId = revisionId; }
    public Long getBaselineId() { return baselineId; }
    public void setBaselineId(Long baselineId) { this.baselineId = baselineId; }
    public String getArtifactHash() { return artifactHash; }
    public void setArtifactHash(String artifactHash) { this.artifactHash = artifactHash; }
    public String getSubmissionNotes() { return submissionNotes; }
    public void setSubmissionNotes(String submissionNotes) { this.submissionNotes = submissionNotes; }
    public Boolean getIsLatest() { return isLatest; }
    public void setIsLatest(Boolean latest) { isLatest = latest; }
    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
