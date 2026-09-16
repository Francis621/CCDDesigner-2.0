package com.ccdd.iam.entity;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 专职工程资质认证实体 (支撑 SoD-02 与高阶审批资质)
 */
public class SysQualificationEntity {

    private Long qualificationId;
    private String userId;
    private QualificationType qualificationType;
    private String certificateNo;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
    private String authorizedBy;
    private Instant createdAt;

    public SysQualificationEntity() {
    }

    public SysQualificationEntity(Long qualificationId, String userId, QualificationType qualificationType,
                                  String certificateNo, LocalDate issuedDate, LocalDate expiryDate,
                                  String authorizedBy, Instant createdAt) {
        this.qualificationId = qualificationId;
        this.userId = userId;
        this.qualificationType = qualificationType;
        this.certificateNo = certificateNo;
        this.issuedDate = issuedDate;
        this.expiryDate = expiryDate;
        this.authorizedBy = authorizedBy;
        this.createdAt = createdAt;
    }

    /**
     * 校验资质证书是否处于有效期内
     */
    public boolean isValid() {
        if (expiryDate == null) {
            return false;
        }
        return !expiryDate.isBefore(LocalDate.now());
    }

    public Long getQualificationId() {
        return qualificationId;
    }

    public void setQualificationId(Long qualificationId) {
        this.qualificationId = qualificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public QualificationType getQualificationType() {
        return qualificationType;
    }

    public void setQualificationType(QualificationType qualificationType) {
        this.qualificationType = qualificationType;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDate issuedDate) {
        this.issuedDate = issuedDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getAuthorizedBy() {
        return authorizedBy;
    }

    public void setAuthorizedBy(String authorizedBy) {
        this.authorizedBy = authorizedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SysQualificationEntity{" +
                "qualificationId=" + qualificationId +
                ", userId='" + userId + '\'' +
                ", qualificationType=" + qualificationType +
                ", certificateNo='" + certificateNo + '\'' +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
