package com.ccdd.model.entity;

import java.io.Serializable;
import java.time.Instant;

public class CandidateSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long snapshotId;
    private String tenantId;
    private Long bindingId;
    private String snapshotToken;
    private String sourceChannel;
    private String rawContentSha256;
    private String rawContentUri;
    private String diagramBundleSha256;
    private String diagramBundleUri;
    private Boolean diagnosticPassed;
    private Integer diagnosticErrorCount;
    private String diagnosticReportJson;
    private String capturedBy;
    private Instant createdAt;
    private Instant expiresAt;

    public CandidateSnapshot() {
    }

    public CandidateSnapshot(Long snapshotId, String tenantId, Long bindingId, String snapshotToken,
                             String sourceChannel, String rawContentSha256, String rawContentUri,
                             String diagramBundleSha256, String diagramBundleUri, Boolean diagnosticPassed,
                             Integer diagnosticErrorCount, String diagnosticReportJson, String capturedBy,
                             Instant createdAt, Instant expiresAt) {
        this.snapshotId = snapshotId;
        this.tenantId = tenantId;
        this.bindingId = bindingId;
        this.snapshotToken = snapshotToken;
        this.sourceChannel = sourceChannel;
        this.rawContentSha256 = rawContentSha256;
        this.rawContentUri = rawContentUri;
        this.diagramBundleSha256 = diagramBundleSha256;
        this.diagramBundleUri = diagramBundleUri;
        this.diagnosticPassed = diagnosticPassed;
        this.diagnosticErrorCount = diagnosticErrorCount;
        this.diagnosticReportJson = diagnosticReportJson;
        this.capturedBy = capturedBy;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long snapshotId;
        private String tenantId;
        private Long bindingId;
        private String snapshotToken;
        private String sourceChannel;
        private String rawContentSha256;
        private String rawContentUri;
        private String diagramBundleSha256;
        private String diagramBundleUri;
        private Boolean diagnosticPassed;
        private Integer diagnosticErrorCount;
        private String diagnosticReportJson;
        private String capturedBy;
        private Instant createdAt;
        private Instant expiresAt;

        public Builder snapshotId(Long snapshotId) { this.snapshotId = snapshotId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder bindingId(Long bindingId) { this.bindingId = bindingId; return this; }
        public Builder snapshotToken(String snapshotToken) { this.snapshotToken = snapshotToken; return this; }
        public Builder sourceChannel(String sourceChannel) { this.sourceChannel = sourceChannel; return this; }
        public Builder rawContentSha256(String rawContentSha256) { this.rawContentSha256 = rawContentSha256; return this; }
        public Builder rawContentUri(String rawContentUri) { this.rawContentUri = rawContentUri; return this; }
        public Builder diagramBundleSha256(String diagramBundleSha256) { this.diagramBundleSha256 = diagramBundleSha256; return this; }
        public Builder diagramBundleUri(String diagramBundleUri) { this.diagramBundleUri = diagramBundleUri; return this; }
        public Builder diagnosticPassed(Boolean diagnosticPassed) { this.diagnosticPassed = diagnosticPassed; return this; }
        public Builder diagnosticErrorCount(Integer diagnosticErrorCount) { this.diagnosticErrorCount = diagnosticErrorCount; return this; }
        public Builder diagnosticReportJson(String diagnosticReportJson) { this.diagnosticReportJson = diagnosticReportJson; return this; }
        public Builder capturedBy(String capturedBy) { this.capturedBy = capturedBy; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }

        public CandidateSnapshot build() {
            return new CandidateSnapshot(snapshotId, tenantId, bindingId, snapshotToken, sourceChannel,
                    rawContentSha256, rawContentUri, diagramBundleSha256, diagramBundleUri,
                    diagnosticPassed, diagnosticErrorCount, diagnosticReportJson, capturedBy, createdAt, expiresAt);
        }
    }

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }

    public String getSnapshotToken() { return snapshotToken; }
    public void setSnapshotToken(String snapshotToken) { this.snapshotToken = snapshotToken; }

    public String getSourceChannel() { return sourceChannel; }
    public void setSourceChannel(String sourceChannel) { this.sourceChannel = sourceChannel; }

    public String getRawContentSha256() { return rawContentSha256; }
    public void setRawContentSha256(String rawContentSha256) { this.rawContentSha256 = rawContentSha256; }

    public String getRawContentUri() { return rawContentUri; }
    public void setRawContentUri(String rawContentUri) { this.rawContentUri = rawContentUri; }

    public String getDiagramBundleSha256() { return diagramBundleSha256; }
    public void setDiagramBundleSha256(String diagramBundleSha256) { this.diagramBundleSha256 = diagramBundleSha256; }

    public String getDiagramBundleUri() { return diagramBundleUri; }
    public void setDiagramBundleUri(String diagramBundleUri) { this.diagramBundleUri = diagramBundleUri; }

    public Boolean getDiagnosticPassed() { return diagnosticPassed; }
    public void setDiagnosticPassed(Boolean diagnosticPassed) { this.diagnosticPassed = diagnosticPassed; }

    public Integer getDiagnosticErrorCount() { return diagnosticErrorCount; }
    public void setDiagnosticErrorCount(Integer diagnosticErrorCount) { this.diagnosticErrorCount = diagnosticErrorCount; }

    public String getDiagnosticReportJson() { return diagnosticReportJson; }
    public void setDiagnosticReportJson(String diagnosticReportJson) { this.diagnosticReportJson = diagnosticReportJson; }

    public String getCapturedBy() { return capturedBy; }
    public void setCapturedBy(String capturedBy) { this.capturedBy = capturedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
