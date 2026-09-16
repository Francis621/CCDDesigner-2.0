package com.ccdd.baseline.entity;

import java.time.Instant;

/**
 * M21 基线成员明细实体 (BaselineMember - 固化节点快照)
 */
public class BaselineMemberEntity {

    private Long memberId;
    private Long baselineId;
    private Long revisionId;
    private MemberRole memberRole;
    private String objectTypeCode;
    private String businessCode;
    private String revisionLabel;
    private String contentHash;
    private Long artifactId;
    private String customContext;
    private Instant addedAt;

    public BaselineMemberEntity() {
    }

    public BaselineMemberEntity(Long memberId, Long baselineId, Long revisionId, MemberRole memberRole,
                                String objectTypeCode, String businessCode, String revisionLabel,
                                String contentHash, Long artifactId, String customContext, Instant addedAt) {
        this.memberId = memberId;
        this.baselineId = baselineId;
        this.revisionId = revisionId;
        this.memberRole = memberRole;
        this.objectTypeCode = objectTypeCode;
        this.businessCode = businessCode;
        this.revisionLabel = revisionLabel;
        this.contentHash = contentHash;
        this.artifactId = artifactId;
        this.customContext = customContext;
        this.addedAt = addedAt;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(Long baselineId) {
        this.baselineId = baselineId;
    }

    public Long getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(Long revisionId) {
        this.revisionId = revisionId;
    }

    public MemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(MemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public String getObjectTypeCode() {
        return objectTypeCode;
    }

    public void setObjectTypeCode(String objectTypeCode) {
        this.objectTypeCode = objectTypeCode;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getRevisionLabel() {
        return revisionLabel;
    }

    public void setRevisionLabel(String revisionLabel) {
        this.revisionLabel = revisionLabel;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public Long getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(Long artifactId) {
        this.artifactId = artifactId;
    }

    public String getCustomContext() {
        return customContext;
    }

    public void setCustomContext(String customContext) {
        this.customContext = customContext;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }
}
