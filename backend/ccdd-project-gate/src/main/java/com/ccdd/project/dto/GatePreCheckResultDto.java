package com.ccdd.project.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 阶段门准入核验报告 DTO (AT-15 守护核心契约)
 */
public class GatePreCheckResultDto {

    private Long gateId;
    private String gateCode;
    private String gateName;
    private Boolean overallPassed;
    private Integer blockerCount;
    private Instant evaluatedAt;
    private List<CriterionResultDto> criterionResults = new ArrayList<>();
    private List<EvidenceGapDto> missingEvidenceGaps = new ArrayList<>();

    public GatePreCheckResultDto() {
    }

    public static class CriterionResultDto {
        private String criterionCode;
        private String name;
        private Boolean passed;
        private String actualValue;
        private String message;
        private Boolean isBlocking;

        public CriterionResultDto() {
        }

        public CriterionResultDto(String criterionCode, String name, Boolean passed,
                                  String actualValue, String message, Boolean isBlocking) {
            this.criterionCode = criterionCode;
            this.name = name;
            this.passed = passed;
            this.actualValue = actualValue;
            this.message = message;
            this.isBlocking = isBlocking;
        }

        public String getCriterionCode() { return criterionCode; }
        public void setCriterionCode(String criterionCode) { this.criterionCode = criterionCode; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Boolean getPassed() { return passed; }
        public void setPassed(Boolean passed) { this.passed = passed; }
        public String getActualValue() { return actualValue; }
        public void setActualValue(String actualValue) { this.actualValue = actualValue; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Boolean getIsBlocking() { return isBlocking; }
        public void setIsBlocking(Boolean blocking) { isBlocking = blocking; }
    }

    public static class EvidenceGapDto {
        private Long reqRevisionId;
        private String reqCode;
        private String caseCode;
        private String currentStatus;
        private String reason;

        public EvidenceGapDto() {
        }

        public EvidenceGapDto(Long reqRevisionId, String reqCode, String caseCode, String currentStatus, String reason) {
            this.reqRevisionId = reqRevisionId;
            this.reqCode = reqCode;
            this.caseCode = caseCode;
            this.currentStatus = currentStatus;
            this.reason = reason;
        }

        public Long getReqRevisionId() { return reqRevisionId; }
        public void setReqRevisionId(Long reqRevisionId) { this.reqRevisionId = reqRevisionId; }
        public String getReqCode() { return reqCode; }
        public void setReqCode(String reqCode) { this.reqCode = reqCode; }
        public String getCaseCode() { return caseCode; }
        public void setCaseCode(String caseCode) { this.caseCode = caseCode; }
        public String getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public Long getGateId() { return gateId; }
    public void setGateId(Long gateId) { this.gateId = gateId; }
    public String getGateCode() { return gateCode; }
    public void setGateCode(String gateCode) { this.gateCode = gateCode; }
    public String getGateName() { return gateName; }
    public void setGateName(String gateName) { this.gateName = gateName; }
    public Boolean getOverallPassed() { return overallPassed; }
    public void setOverallPassed(Boolean overallPassed) { this.overallPassed = overallPassed; }
    public Integer getBlockerCount() { return blockerCount; }
    public void setBlockerCount(Integer blockerCount) { this.blockerCount = blockerCount; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    public List<CriterionResultDto> getCriterionResults() { return criterionResults; }
    public void setCriterionResults(List<CriterionResultDto> criterionResults) { this.criterionResults = criterionResults; }
    public List<EvidenceGapDto> getMissingEvidenceGaps() { return missingEvidenceGaps; }
    public void setMissingEvidenceGaps(List<EvidenceGapDto> missingEvidenceGaps) { this.missingEvidenceGaps = missingEvidenceGaps; }
}
