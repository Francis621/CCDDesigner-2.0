package com.ccdd.change.dto;

import com.ccdd.change.entity.DispositionActionType;
import java.time.LocalDate;

/**
 * 编制现场生效处置方案请求 DTO (M22-F05)
 */
public class CreateDispositionRequest {

    private String targetScopeType; // INVENTORY_PART, IN_PROCESS_ORDER, FIELD_MACHINE
    private String targetPartNumber;
    private Long targetOrderProductId;
    private Long targetIndividualId;
    private DispositionActionType actionType; // SCRAP, REWORK, USE_UP, AS_IS
    private String effectiveSerialCutoff;
    private LocalDate effectiveDateCutoff;
    private String dispositionInstructions;

    public CreateDispositionRequest() {
    }

    public String getTargetScopeType() {
        return targetScopeType;
    }

    public void setTargetScopeType(String targetScopeType) {
        this.targetScopeType = targetScopeType;
    }

    public String getTargetPartNumber() {
        return targetPartNumber;
    }

    public void setTargetPartNumber(String targetPartNumber) {
        this.targetPartNumber = targetPartNumber;
    }

    public Long getTargetOrderProductId() {
        return targetOrderProductId;
    }

    public void setTargetOrderProductId(Long targetOrderProductId) {
        this.targetOrderProductId = targetOrderProductId;
    }

    public Long getTargetIndividualId() {
        return targetIndividualId;
    }

    public void setTargetIndividualId(Long targetIndividualId) {
        this.targetIndividualId = targetIndividualId;
    }

    public DispositionActionType getActionType() {
        return actionType;
    }

    public void setActionType(DispositionActionType actionType) {
        this.actionType = actionType;
    }

    public String getEffectiveSerialCutoff() {
        return effectiveSerialCutoff;
    }

    public void setEffectiveSerialCutoff(String effectiveSerialCutoff) {
        this.effectiveSerialCutoff = effectiveSerialCutoff;
    }

    public LocalDate getEffectiveDateCutoff() {
        return effectiveDateCutoff;
    }

    public void setEffectiveDateCutoff(LocalDate effectiveDateCutoff) {
        this.effectiveDateCutoff = effectiveDateCutoff;
    }

    public String getDispositionInstructions() {
        return dispositionInstructions;
    }

    public void setDispositionInstructions(String dispositionInstructions) {
        this.dispositionInstructions = dispositionInstructions;
    }
}
