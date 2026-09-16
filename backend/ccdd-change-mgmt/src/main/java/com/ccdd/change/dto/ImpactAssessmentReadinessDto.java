package com.ccdd.change.dto;

/**
 * 影响分析完备性校验结果 DTO (CST-M22-02)
 */
public class ImpactAssessmentReadinessDto {

    private Boolean isReady;
    private Integer unassessedCount;
    private Integer reVerifyCount;
    private Integer modifyCount;
    private Integer reviewOnlyCount;
    private Integer noImpactCount;
    private String message;

    public ImpactAssessmentReadinessDto() {
    }

    public ImpactAssessmentReadinessDto(Boolean isReady, Integer unassessedCount, Integer reVerifyCount,
                                        Integer modifyCount, Integer reviewOnlyCount, Integer noImpactCount, String message) {
        this.isReady = isReady;
        this.unassessedCount = unassessedCount;
        this.reVerifyCount = reVerifyCount;
        this.modifyCount = modifyCount;
        this.reviewOnlyCount = reviewOnlyCount;
        this.noImpactCount = noImpactCount;
        this.message = message;
    }

    public Boolean getIsReady() {
        return isReady;
    }

    public void setIsReady(Boolean ready) {
        isReady = ready;
    }

    public Integer getUnassessedCount() {
        return unassessedCount;
    }

    public void setUnassessedCount(Integer unassessedCount) {
        this.unassessedCount = unassessedCount;
    }

    public Integer getReVerifyCount() {
        return reVerifyCount;
    }

    public void setReVerifyCount(Integer reVerifyCount) {
        this.reVerifyCount = reVerifyCount;
    }

    public Integer getModifyCount() {
        return modifyCount;
    }

    public void setModifyCount(Integer modifyCount) {
        this.modifyCount = modifyCount;
    }

    public Integer getReviewOnlyCount() {
        return reviewOnlyCount;
    }

    public void setReviewOnlyCount(Integer reviewOnlyCount) {
        this.reviewOnlyCount = reviewOnlyCount;
    }

    public Integer getNoImpactCount() {
        return noImpactCount;
    }

    public void setNoImpactCount(Integer noImpactCount) {
        this.noImpactCount = noImpactCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
