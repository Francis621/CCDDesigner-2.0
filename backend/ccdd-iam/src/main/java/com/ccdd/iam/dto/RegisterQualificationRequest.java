package com.ccdd.iam.dto;

import com.ccdd.iam.entity.QualificationType;

import java.time.LocalDate;

/**
 * 登记专职工程资质请求 DTO (对齐 OpenAPI 6.3)
 */
public class RegisterQualificationRequest {

    private QualificationType qualificationType;
    private String certificateNo;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
    private String authorizedBy;

    public RegisterQualificationRequest() {
    }

    public RegisterQualificationRequest(QualificationType qualificationType, String certificateNo,
                                        LocalDate issuedDate, LocalDate expiryDate, String authorizedBy) {
        this.qualificationType = qualificationType;
        this.certificateNo = certificateNo;
        this.issuedDate = issuedDate;
        this.expiryDate = expiryDate;
        this.authorizedBy = authorizedBy;
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
}
