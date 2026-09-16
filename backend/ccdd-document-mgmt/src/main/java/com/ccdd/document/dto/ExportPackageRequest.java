package com.ccdd.document.dto;

import java.time.Instant;
import java.util.List;

/**
 * 受控交付包组装导出契约 (M19-F05, AT-14)
 */
public class ExportPackageRequest {

    private String packageName;
    private List<Long> documentRevisionIds;
    private Boolean includeDerivatives;
    private String securityLevel;
    private String requesterUserId;

    public ExportPackageRequest() {
    }

    public ExportPackageRequest(String packageName, List<Long> documentRevisionIds,
                                Boolean includeDerivatives, String securityLevel, String requesterUserId) {
        this.packageName = packageName;
        this.documentRevisionIds = documentRevisionIds;
        this.includeDerivatives = includeDerivatives;
        this.securityLevel = securityLevel;
        this.requesterUserId = requesterUserId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<Long> getDocumentRevisionIds() {
        return documentRevisionIds;
    }

    public void setDocumentRevisionIds(List<Long> documentRevisionIds) {
        this.documentRevisionIds = documentRevisionIds;
    }

    public Boolean getIncludeDerivatives() {
        return includeDerivatives;
    }

    public void setIncludeDerivatives(Boolean includeDerivatives) {
        this.includeDerivatives = includeDerivatives;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(String requesterUserId) {
        this.requesterUserId = requesterUserId;
    }
}
