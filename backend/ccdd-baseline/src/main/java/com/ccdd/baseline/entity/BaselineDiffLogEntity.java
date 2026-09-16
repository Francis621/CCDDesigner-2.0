package com.ccdd.baseline.entity;

import java.time.Instant;

/**
 * M21 基线红线差分比对日志实体 (BaselineDiffLog)
 * 记录两基线比对的增删改差异以及闭包哈希变化
 */
public class BaselineDiffLogEntity {

    private Long diffId;
    private Long baselineIdA;
    private Long baselineIdB;
    private Integer addedCount;
    private Integer removedCount;
    private Integer modifiedCount;
    private Integer unchangedCount;
    private String diffDetailsJson;
    private Instant computedAt;
    private String computedBy;

    public BaselineDiffLogEntity() {
    }

    public BaselineDiffLogEntity(Long diffId, Long baselineIdA, Long baselineIdB,
                                 Integer addedCount, Integer removedCount, Integer modifiedCount, Integer unchangedCount,
                                 String diffDetailsJson, Instant computedAt, String computedBy) {
        this.diffId = diffId;
        this.baselineIdA = baselineIdA;
        this.baselineIdB = baselineIdB;
        this.addedCount = addedCount;
        this.removedCount = removedCount;
        this.modifiedCount = modifiedCount;
        this.unchangedCount = unchangedCount;
        this.diffDetailsJson = diffDetailsJson;
        this.computedAt = computedAt;
        this.computedBy = computedBy;
    }

    public Long getDiffId() {
        return diffId;
    }

    public void setDiffId(Long diffId) {
        this.diffId = diffId;
    }

    public Long getBaselineIdA() {
        return baselineIdA;
    }

    public void setBaselineIdA(Long baselineIdA) {
        this.baselineIdA = baselineIdA;
    }

    public Long getBaselineIdB() {
        return baselineIdB;
    }

    public void setBaselineIdB(Long baselineIdB) {
        this.baselineIdB = baselineIdB;
    }

    public Integer getAddedCount() {
        return addedCount;
    }

    public void setAddedCount(Integer addedCount) {
        this.addedCount = addedCount;
    }

    public Integer getRemovedCount() {
        return removedCount;
    }

    public void setRemovedCount(Integer removedCount) {
        this.removedCount = removedCount;
    }

    public Integer getModifiedCount() {
        return modifiedCount;
    }

    public void setModifiedCount(Integer modifiedCount) {
        this.modifiedCount = modifiedCount;
    }

    public Integer getUnchangedCount() {
        return unchangedCount;
    }

    public void setUnchangedCount(Integer unchangedCount) {
        this.unchangedCount = unchangedCount;
    }

    public String getDiffDetailsJson() {
        return diffDetailsJson;
    }

    public void setDiffDetailsJson(String diffDetailsJson) {
        this.diffDetailsJson = diffDetailsJson;
    }

    public Instant getComputedAt() {
        return computedAt;
    }

    public void setComputedAt(Instant computedAt) {
        this.computedAt = computedAt;
    }

    public String getComputedBy() {
        return computedBy;
    }

    public void setComputedBy(String computedBy) {
        this.computedBy = computedBy;
    }
}
