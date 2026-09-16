package com.ccdd.baseline.entity;

import java.time.Instant;

/**
 * M21 基线关系拓扑快照实体 (BaselineRelationSnapshot - 固化图关系边)
 */
public class BaselineRelationSnapshotEntity {

    private Long snapshotRelId;
    private Long baselineId;
    private Long sourceRevisionId;
    private Long targetRevisionId;
    private String relationTypeId;
    private String relationHash;
    private String structuralContext;
    private Instant snapshottedAt;

    public BaselineRelationSnapshotEntity() {
    }

    public BaselineRelationSnapshotEntity(Long snapshotRelId, Long baselineId, Long sourceRevisionId,
                                          Long targetRevisionId, String relationTypeId, String relationHash,
                                          String structuralContext, Instant snapshottedAt) {
        this.snapshotRelId = snapshotRelId;
        this.baselineId = baselineId;
        this.sourceRevisionId = sourceRevisionId;
        this.targetRevisionId = targetRevisionId;
        this.relationTypeId = relationTypeId;
        this.relationHash = relationHash;
        this.structuralContext = structuralContext;
        this.snapshottedAt = snapshottedAt;
    }

    public Long getSnapshotRelId() {
        return snapshotRelId;
    }

    public void setSnapshotRelId(Long snapshotRelId) {
        this.snapshotRelId = snapshotRelId;
    }

    public Long getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(Long baselineId) {
        this.baselineId = baselineId;
    }

    public Long getSourceRevisionId() {
        return sourceRevisionId;
    }

    public void setSourceRevisionId(Long sourceRevisionId) {
        this.sourceRevisionId = sourceRevisionId;
    }

    public Long getTargetRevisionId() {
        return targetRevisionId;
    }

    public void setTargetRevisionId(Long targetRevisionId) {
        this.targetRevisionId = targetRevisionId;
    }

    public String getRelationTypeId() {
        return relationTypeId;
    }

    public void setRelationTypeId(String relationTypeId) {
        this.relationTypeId = relationTypeId;
    }

    public String getRelationHash() {
        return relationHash;
    }

    public void setRelationHash(String relationHash) {
        this.relationHash = relationHash;
    }

    public String getStructuralContext() {
        return structuralContext;
    }

    public void setStructuralContext(String structuralContext) {
        this.structuralContext = structuralContext;
    }

    public Instant getSnapshottedAt() {
        return snapshottedAt;
    }

    public void setSnapshottedAt(Instant snapshottedAt) {
        this.snapshottedAt = snapshottedAt;
    }
}
