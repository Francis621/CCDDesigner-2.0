package com.ccdd.baseline.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 基线红线差异比对结果 DTO
 */
public class BaselineDiffResultDto {

    private Long baselineIdA;
    private String baselineCodeA;
    private String closureHashA;
    private Long baselineIdB;
    private String baselineCodeB;
    private String closureHashB;
    private Boolean isIdentical;
    private Integer addedCount;
    private Integer removedCount;
    private Integer modifiedCount;
    private Integer unchangedCount;
    private List<DiffEntry> memberDifferences;
    private List<RelationDiffEntry> relationDifferences;
    private Instant comparedAt;

    public BaselineDiffResultDto() {
        this.memberDifferences = new ArrayList<>();
        this.relationDifferences = new ArrayList<>();
    }

    public Long getBaselineIdA() {
        return baselineIdA;
    }

    public void setBaselineIdA(Long baselineIdA) {
        this.baselineIdA = baselineIdA;
    }

    public String getBaselineCodeA() {
        return baselineCodeA;
    }

    public void setBaselineCodeA(String baselineCodeA) {
        this.baselineCodeA = baselineCodeA;
    }

    public String getClosureHashA() {
        return closureHashA;
    }

    public void setClosureHashA(String closureHashA) {
        this.closureHashA = closureHashA;
    }

    public Long getBaselineIdB() {
        return baselineIdB;
    }

    public void setBaselineIdB(Long baselineIdB) {
        this.baselineIdB = baselineIdB;
    }

    public String getBaselineCodeB() {
        return baselineCodeB;
    }

    public void setBaselineCodeB(String baselineCodeB) {
        this.baselineCodeB = baselineCodeB;
    }

    public String getClosureHashB() {
        return closureHashB;
    }

    public void setClosureHashB(String closureHashB) {
        this.closureHashB = closureHashB;
    }

    public Boolean getIsIdentical() {
        return isIdentical;
    }

    public void setIsIdentical(Boolean isIdentical) {
        this.isIdentical = isIdentical;
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

    public List<DiffEntry> getMemberDifferences() {
        return memberDifferences;
    }

    public void setMemberDifferences(List<DiffEntry> memberDifferences) {
        this.memberDifferences = memberDifferences;
    }

    public List<RelationDiffEntry> getRelationDifferences() {
        return relationDifferences;
    }

    public void setRelationDifferences(List<RelationDiffEntry> relationDifferences) {
        this.relationDifferences = relationDifferences;
    }

    public Instant getComparedAt() {
        return comparedAt;
    }

    public void setComparedAt(Instant comparedAt) {
        this.comparedAt = comparedAt;
    }

    public static class DiffEntry {
        private String diffType; // ADDED, REMOVED, MODIFIED, UNCHANGED
        private String objectTypeCode;
        private Long revisionId;
        private String businessCode;
        private String revisionLabelA;
        private String revisionLabelB;
        private String hashA;
        private String hashB;
        private String changeSummary;

        public DiffEntry() {
        }

        public DiffEntry(String diffType, String objectTypeCode, Long revisionId, String businessCode,
                         String revisionLabelA, String revisionLabelB,
                         String hashA, String hashB, String changeSummary) {
            this.diffType = diffType;
            this.objectTypeCode = objectTypeCode;
            this.revisionId = revisionId;
            this.businessCode = businessCode;
            this.revisionLabelA = revisionLabelA;
            this.revisionLabelB = revisionLabelB;
            this.hashA = hashA;
            this.hashB = hashB;
            this.changeSummary = changeSummary;
        }

        public String getDiffType() {
            return diffType;
        }

        public void setDiffType(String diffType) {
            this.diffType = diffType;
        }

        public String getObjectTypeCode() {
            return objectTypeCode;
        }

        public void setObjectTypeCode(String objectTypeCode) {
            this.objectTypeCode = objectTypeCode;
        }

        public Long getRevisionId() {
            return revisionId;
        }

        public void setRevisionId(Long revisionId) {
            this.revisionId = revisionId;
        }

        public String getBusinessCode() {
            return businessCode;
        }

        public void setBusinessCode(String businessCode) {
            this.businessCode = businessCode;
        }

        public String getRevisionLabelA() {
            return revisionLabelA;
        }

        public void setRevisionLabelA(String revisionLabelA) {
            this.revisionLabelA = revisionLabelA;
        }

        public String getRevisionLabelB() {
            return revisionLabelB;
        }

        public void setRevisionLabelB(String revisionLabelB) {
            this.revisionLabelB = revisionLabelB;
        }

        public String getHashA() {
            return hashA;
        }

        public void setHashA(String hashA) {
            this.hashA = hashA;
        }

        public String getHashB() {
            return hashB;
        }

        public void setHashB(String hashB) {
            this.hashB = hashB;
        }

        public String getChangeSummary() {
            return changeSummary;
        }

        public void setChangeSummary(String changeSummary) {
            this.changeSummary = changeSummary;
        }
    }

    public static class RelationDiffEntry {
        private String diffType; // ADDED, REMOVED, UNCHANGED
        private String relationType;
        private Long sourceRevisionId;
        private Long targetRevisionId;
        private String description;

        public RelationDiffEntry() {
        }

        public RelationDiffEntry(String diffType, String relationType, Long sourceRevisionId,
                                 Long targetRevisionId, String description) {
            this.diffType = diffType;
            this.relationType = relationType;
            this.sourceRevisionId = sourceRevisionId;
            this.targetRevisionId = targetRevisionId;
            this.description = description;
        }

        public String getDiffType() {
            return diffType;
        }

        public void setDiffType(String diffType) {
            this.diffType = diffType;
        }

        public String getRelationType() {
            return relationType;
        }

        public void setRelationType(String relationType) {
            this.relationType = relationType;
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
