package com.ccdd.document.dto;

import com.ccdd.document.entity.SecurityClassification;

/**
 * 创建/注册新图文档主对象及初始修订版请求契约
 */
public class SaveDocumentRequest {

    private String documentNumber;
    private String documentTitle;
    private String docCategoryCode; // MECH_DRAWING, ELEC_SCHEMATIC, TECH_SPEC, TEST_REPORT, USER_MANUAL
    private SecurityClassification securityLevel;
    private String departmentId;
    private String cadSoftwareType;
    private String cadSoftwareVersion;
    private String summary;
    private String createdBy;

    public SaveDocumentRequest() {
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getDocCategoryCode() {
        return docCategoryCode;
    }

    public void setDocCategoryCode(String docCategoryCode) {
        this.docCategoryCode = docCategoryCode;
    }

    public SecurityClassification getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(SecurityClassification securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getCadSoftwareType() {
        return cadSoftwareType;
    }

    public void setCadSoftwareType(String cadSoftwareType) {
        this.cadSoftwareType = cadSoftwareType;
    }

    public String getCadSoftwareVersion() {
        return cadSoftwareVersion;
    }

    public void setCadSoftwareVersion(String cadSoftwareVersion) {
        this.cadSoftwareVersion = cadSoftwareVersion;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
