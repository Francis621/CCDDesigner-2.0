package com.ccdd.document.entity;

import java.time.Instant;

/**
 * M19 文档主对象实体 (Master - 承载业务全局唯一代号与分类)
 */
public class DocumentMasterEntity {

    private Long masterId;
    private String tenantId;
    private String documentNumber;
    private String documentTitle;
    private String docCategoryCode; // MECH_DRAWING, ELEC_SCHEMATIC, TECH_SPEC, TEST_REPORT, USER_MANUAL
    private Long docTemplateId;
    private SecurityClassification defaultSecurityLevel;
    private String departmentId;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public DocumentMasterEntity() {
    }

    public DocumentMasterEntity(Long masterId, String tenantId, String documentNumber, String documentTitle,
                                String docCategoryCode, Long docTemplateId, SecurityClassification defaultSecurityLevel,
                                String departmentId, String createdBy, Instant createdAt, Instant updatedAt) {
        this.masterId = masterId;
        this.tenantId = tenantId;
        this.documentNumber = documentNumber;
        this.documentTitle = documentTitle;
        this.docCategoryCode = docCategoryCode;
        this.docTemplateId = docTemplateId;
        this.defaultSecurityLevel = defaultSecurityLevel;
        this.departmentId = departmentId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getMasterId() {
        return masterId;
    }

    public void setMasterId(Long masterId) {
        this.masterId = masterId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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

    public Long getDocTemplateId() {
        return docTemplateId;
    }

    public void setDocTemplateId(Long docTemplateId) {
        this.docTemplateId = docTemplateId;
    }

    public SecurityClassification getDefaultSecurityLevel() {
        return defaultSecurityLevel;
    }

    public void setDefaultSecurityLevel(SecurityClassification defaultSecurityLevel) {
        this.defaultSecurityLevel = defaultSecurityLevel;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
