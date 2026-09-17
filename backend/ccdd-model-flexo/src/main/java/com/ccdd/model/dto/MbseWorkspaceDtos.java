package com.ccdd.model.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * M04 MBSE 建模工作区传输对象集合 (遵循 OpenAPI 3.0 契约)
 */
public class MbseWorkspaceDtos {

    // ==========================================
    // 1. 工作区绑定相关 DTO
    // ==========================================
    public static class WorkspaceBindRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long modelProjectId;
        private String externalToolName = "SYSON";
        private String primaryChannel = "GRAPHICAL";
        private String compatibilityProfileId = "SYSML_V2_CNC_V2026_09";
        private String initialTemplateId = "VMC1000_MACHINE_TEMPLATE_V1";

        public Long getModelProjectId() { return modelProjectId; }
        public void setModelProjectId(Long modelProjectId) { this.modelProjectId = modelProjectId; }

        public String getExternalToolName() { return externalToolName; }
        public void setExternalToolName(String externalToolName) { this.externalToolName = externalToolName; }

        public String getPrimaryChannel() { return primaryChannel; }
        public void setPrimaryChannel(String primaryChannel) { this.primaryChannel = primaryChannel; }

        public String getCompatibilityProfileId() { return compatibilityProfileId; }
        public void setCompatibilityProfileId(String compatibilityProfileId) { this.compatibilityProfileId = compatibilityProfileId; }

        public String getInitialTemplateId() { return initialTemplateId; }
        public void setInitialTemplateId(String initialTemplateId) { this.initialTemplateId = initialTemplateId; }
    }

    public static class WorkspaceBindResponse implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long workspaceId;
        private Long modelProjectId;
        private String externalProjectId;
        private String primaryChannel;
        private String status;
        private Instant boundAt;

        public WorkspaceBindResponse() {}
        public WorkspaceBindResponse(Long workspaceId, Long modelProjectId, String externalProjectId,
                                     String primaryChannel, String status, Instant boundAt) {
            this.workspaceId = workspaceId;
            this.modelProjectId = modelProjectId;
            this.externalProjectId = externalProjectId;
            this.primaryChannel = primaryChannel;
            this.status = status;
            this.boundAt = boundAt;
        }

        public Long getWorkspaceId() { return workspaceId; }
        public Long getModelProjectId() { return modelProjectId; }
        public String getExternalProjectId() { return externalProjectId; }
        public String getPrimaryChannel() { return primaryChannel; }
        public String getStatus() { return status; }
        public Instant getBoundAt() { return boundAt; }
    }

    // ==========================================
    // 2. 编辑会话排他锁 DTO
    // ==========================================
    public static class SessionLockRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean requestLock = true;
        private int lockTimeoutMinutes = 120;

        public boolean isRequestLock() { return requestLock; }
        public void setRequestLock(boolean requestLock) { this.requestLock = requestLock; }

        public int getLockTimeoutMinutes() { return lockTimeoutMinutes; }
        public void setLockTimeoutMinutes(int lockTimeoutMinutes) { this.lockTimeoutMinutes = lockTimeoutMinutes; }
    }

    public static class SessionLockResponse implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long workspaceId;
        private String sessionToken;
        private boolean lockGranted;
        private String lockedByUser;
        private String viewportUrl;
        private Instant expiresAt;

        public SessionLockResponse() {}
        public SessionLockResponse(Long workspaceId, String sessionToken, boolean lockGranted,
                                   String lockedByUser, String viewportUrl, Instant expiresAt) {
            this.workspaceId = workspaceId;
            this.sessionToken = sessionToken;
            this.lockGranted = lockGranted;
            this.lockedByUser = lockedByUser;
            this.viewportUrl = viewportUrl;
            this.expiresAt = expiresAt;
        }

        public Long getWorkspaceId() { return workspaceId; }
        public String getSessionToken() { return sessionToken; }
        public boolean isLockGranted() { return lockGranted; }
        public String getLockedByUser() { return lockedByUser; }
        public String getViewportUrl() { return viewportUrl; }
        public Instant getExpiresAt() { return expiresAt; }
    }

    // ==========================================
    // 3. 语义诊断 DTO
    // ==========================================
    public static class DiagnosticItemDto implements Serializable {
        private static final long serialVersionUID = 1L;
        private String severity; // ERROR, WARNING, INFO
        private String errorCode;
        private String message;
        private String elementId;
        private String qualifiedName;
        private String sourceLocation;
        private String recommendation;

        public DiagnosticItemDto() {}
        public DiagnosticItemDto(String severity, String errorCode, String message, String elementId,
                                 String qualifiedName, String sourceLocation, String recommendation) {
            this.severity = severity;
            this.errorCode = errorCode;
            this.message = message;
            this.elementId = elementId;
            this.qualifiedName = qualifiedName;
            this.sourceLocation = sourceLocation;
            this.recommendation = recommendation;
        }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getElementId() { return elementId; }
        public void setElementId(String elementId) { this.elementId = elementId; }

        public String getQualifiedName() { return qualifiedName; }
        public void setQualifiedName(String qualifiedName) { this.qualifiedName = qualifiedName; }

        public String getSourceLocation() { return sourceLocation; }
        public void setSourceLocation(String sourceLocation) { this.sourceLocation = sourceLocation; }

        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }

    public static class ValidationOutcomeDto implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long validationId;
        private Long workspaceId;
        private String status; // PASSED, PASSED_WITH_WARNING, FAILED
        private String sourceChecksum;
        private int errorCount;
        private int warningCount;
        private List<DiagnosticItemDto> diagnostics = new ArrayList<>();
        private Instant startedAt;
        private Instant completedAt;

        public ValidationOutcomeDto() {}
        public ValidationOutcomeDto(Long validationId, Long workspaceId, String status, String sourceChecksum,
                                    int errorCount, int warningCount, List<DiagnosticItemDto> diagnostics,
                                    Instant startedAt, Instant completedAt) {
            this.validationId = validationId;
            this.workspaceId = workspaceId;
            this.status = status;
            this.sourceChecksum = sourceChecksum;
            this.errorCount = errorCount;
            this.warningCount = warningCount;
            this.diagnostics = diagnostics != null ? diagnostics : new ArrayList<>();
            this.startedAt = startedAt;
            this.completedAt = completedAt;
        }

        public Long getValidationId() { return validationId; }
        public Long getWorkspaceId() { return workspaceId; }
        public String getStatus() { return status; }
        public String getSourceChecksum() { return sourceChecksum; }
        public int getErrorCount() { return errorCount; }
        public int getWarningCount() { return warningCount; }
        public List<DiagnosticItemDto> getDiagnostics() { return diagnostics; }
        public Instant getStartedAt() { return startedAt; }
        public Instant getCompletedAt() { return completedAt; }
    }

    // ==========================================
    // 4. 候选快照与发布交接 DTO
    // ==========================================
    public static class SnapshotCaptureRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long validationId;
        private String expectedChecksum;
        private String snapshotDescription;

        public Long getValidationId() { return validationId; }
        public void setValidationId(Long validationId) { this.validationId = validationId; }

        public String getExpectedChecksum() { return expectedChecksum; }
        public void setExpectedChecksum(String expectedChecksum) { this.expectedChecksum = expectedChecksum; }

        public String getSnapshotDescription() { return snapshotDescription; }
        public void setSnapshotDescription(String snapshotDescription) { this.snapshotDescription = snapshotDescription; }
    }

    public static class SnapshotCaptureResponse implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long snapshotId;
        private String snapshotToken;
        private String status; // STAGED, HANDED_OVER
        private String sourceChecksum;
        private String releaseOperationId;
        private Instant capturedAt;

        public SnapshotCaptureResponse() {}
        public SnapshotCaptureResponse(Long snapshotId, String snapshotToken, String status,
                                       String sourceChecksum, String releaseOperationId, Instant capturedAt) {
            this.snapshotId = snapshotId;
            this.snapshotToken = snapshotToken;
            this.status = status;
            this.sourceChecksum = sourceChecksum;
            this.releaseOperationId = releaseOperationId;
            this.capturedAt = capturedAt;
        }

        public Long getSnapshotId() { return snapshotId; }
        public String getSnapshotToken() { return snapshotToken; }
        public String getStatus() { return status; }
        public String getSourceChecksum() { return sourceChecksum; }
        public String getReleaseOperationId() { return releaseOperationId; }
        public Instant getCapturedAt() { return capturedAt; }
    }

    // ==========================================
    // 5. 需求投影与工作期动态绑定 DTO
    // ==========================================
    public static class RequirementBindRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long requirementRevisionId;
        private String requirementCode;
        private String requirementName;
        private String targetPackageName = "01_Requirements";

        public Long getRequirementRevisionId() { return requirementRevisionId; }
        public void setRequirementRevisionId(Long requirementRevisionId) { this.requirementRevisionId = requirementRevisionId; }

        public String getRequirementCode() { return requirementCode; }
        public void setRequirementCode(String requirementCode) { this.requirementCode = requirementCode; }

        public String getRequirementName() { return requirementName; }
        public void setRequirementName(String requirementName) { this.requirementName = requirementName; }

        public String getTargetPackageName() { return targetPackageName; }
        public void setTargetPackageName(String targetPackageName) { this.targetPackageName = targetPackageName; }
    }

    public static class ChannelSwitchRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private String targetChannel; // GRAPHICAL, TEXTUAL

        public String getTargetChannel() { return targetChannel; }
        public void setTargetChannel(String targetChannel) { this.targetChannel = targetChannel; }
    }

    public static class ModelContentUpdateRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private String rawSysmlContent;

        public String getRawSysmlContent() { return rawSysmlContent; }
        public void setRawSysmlContent(String rawSysmlContent) { this.rawSysmlContent = rawSysmlContent; }
    }
}
