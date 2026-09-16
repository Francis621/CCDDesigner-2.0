package com.ccdd.baseline.dto;

import com.ccdd.baseline.entity.BaselinePurpose;
import com.ccdd.baseline.entity.MemberRole;
import java.util.List;

/**
 * 创建基线请求 DTO
 */
public class CreateBaselineRequest {

    private String baselineCode;
    private String name;
    private String description;
    private BaselinePurpose purpose;
    private Long projectId;
    private String tenantId;
    private Long approvalTicketId;
    private List<BaselineMemberInput> initialMembers;
    private String createdBy;

    public CreateBaselineRequest() {
    }

    public String getBaselineCode() {
        return baselineCode;
    }

    public void setBaselineCode(String baselineCode) {
        this.baselineCode = baselineCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BaselinePurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(BaselinePurpose purpose) {
        this.purpose = purpose;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Long getApprovalTicketId() {
        return approvalTicketId;
    }

    public void setApprovalTicketId(Long approvalTicketId) {
        this.approvalTicketId = approvalTicketId;
    }

    public List<BaselineMemberInput> getInitialMembers() {
        return initialMembers;
    }

    public void setInitialMembers(List<BaselineMemberInput> initialMembers) {
        this.initialMembers = initialMembers;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public static class BaselineMemberInput {
        private Long revisionId;
        private MemberRole memberRole;
        private String objectTypeCode;
        private String businessCode;
        private String revisionLabel;
        private String contentHash;
        private Long artifactId;
        private String customContext;

        public BaselineMemberInput() {
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
    }
}
