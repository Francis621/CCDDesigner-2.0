package com.ccdd.baseline.dto;

import com.ccdd.baseline.entity.BaselineState;
import java.time.Instant;

/**
 * 审批冻结基线响应 DTO
 */
public class FreezeBaselineResponse {

    private Long baselineId;
    private String baselineCode;
    private BaselineState state;
    private String closureHash;
    private Integer memberCount;
    private Integer relationSnapshotCount;
    private Instant frozenAt;
    private String frozenBy;
    private String message;

    public FreezeBaselineResponse() {
    }

    public FreezeBaselineResponse(Long baselineId, String baselineCode, BaselineState state,
                                  String closureHash, Integer memberCount, Integer relationSnapshotCount,
                                  Instant frozenAt, String frozenBy, String message) {
        this.baselineId = baselineId;
        this.baselineCode = baselineCode;
        this.state = state;
        this.closureHash = closureHash;
        this.memberCount = memberCount;
        this.relationSnapshotCount = relationSnapshotCount;
        this.frozenAt = frozenAt;
        this.frozenBy = frozenBy;
        this.message = message;
    }

    public Long getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(Long baselineId) {
        this.baselineId = baselineId;
    }

    public String getBaselineCode() {
        return baselineCode;
    }

    public void setBaselineCode(String baselineCode) {
        this.baselineCode = baselineCode;
    }

    public BaselineState getState() {
        return state;
    }

    public void setState(BaselineState state) {
        this.state = state;
    }

    public String getClosureHash() {
        return closureHash;
    }

    public void setClosureHash(String closureHash) {
        this.closureHash = closureHash;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getRelationSnapshotCount() {
        return relationSnapshotCount;
    }

    public void setRelationSnapshotCount(Integer relationSnapshotCount) {
        this.relationSnapshotCount = relationSnapshotCount;
    }

    public Instant getFrozenAt() {
        return frozenAt;
    }

    public void setFrozenAt(Instant frozenAt) {
        this.frozenAt = frozenAt;
    }

    public String getFrozenBy() {
        return frozenBy;
    }

    public void setFrozenBy(String frozenBy) {
        this.frozenBy = frozenBy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
