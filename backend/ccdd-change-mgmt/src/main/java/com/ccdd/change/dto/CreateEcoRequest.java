package com.ccdd.change.dto;

/**
 * 签发变更实施单 (ECO) 请求 DTO
 */
public class CreateEcoRequest {

    private Long ecrId;
    private String ecoNumber;
    private String title;
    private String changeCategory; // MAJOR, MINOR, ADMINISTRATIVE
    private Long targetBaselineId;
    private String createdBy;

    public CreateEcoRequest() {
    }

    public Long getEcrId() {
        return ecrId;
    }

    public void setEcrId(Long ecrId) {
        this.ecrId = ecrId;
    }

    public String getEcoNumber() {
        return ecoNumber;
    }

    public void setEcoNumber(String ecoNumber) {
        this.ecoNumber = ecoNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChangeCategory() {
        return changeCategory;
    }

    public void setChangeCategory(String changeCategory) {
        this.changeCategory = changeCategory;
    }

    public Long getTargetBaselineId() {
        return targetBaselineId;
    }

    public void setTargetBaselineId(Long targetBaselineId) {
        this.targetBaselineId = targetBaselineId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
