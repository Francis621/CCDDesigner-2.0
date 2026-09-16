package com.ccdd.change.entity;

import java.time.Instant;
import java.time.LocalDate;

/**
 * M22 物料、订单与设备现场生效处置规约实体 (EffectivityDisposition)
 * 明确在制品/库存/现场机床的报废/返工/自然过渡规则
 */
public class EffectivityDispositionEntity {

    private Long dispositionId;
    private Long ecoId;
    private String targetScopeType; // INVENTORY_PART, IN_PROCESS_ORDER, FIELD_MACHINE
    private String targetPartNumber;
    private Long targetOrderProductId;
    private Long targetIndividualId;
    private DispositionActionType actionType;
    private String effectiveSerialCutoff;
    private LocalDate effectiveDateCutoff;
    private String dispositionInstructions;
    private Instant createdAt;

    public EffectivityDispositionEntity() {
    }

    public EffectivityDispositionEntity(Long dispositionId, Long ecoId, String targetScopeType,
                                        String targetPartNumber, Long targetOrderProductId,
                                        Long targetIndividualId, DispositionActionType actionType,
                                        String effectiveSerialCutoff, LocalDate effectiveDateCutoff,
                                        String dispositionInstructions, Instant createdAt) {
        this.dispositionId = dispositionId;
        this.ecoId = ecoId;
        this.targetScopeType = targetScopeType;
        this.targetPartNumber = targetPartNumber;
        this.targetOrderProductId = targetOrderProductId;
        this.targetIndividualId = targetIndividualId;
        this.actionType = actionType;
        this.effectiveSerialCutoff = effectiveSerialCutoff;
        this.effectiveDateCutoff = effectiveDateCutoff;
        this.dispositionInstructions = dispositionInstructions;
        this.createdAt = createdAt;
    }

    public Long getDispositionId() {
        return dispositionId;
    }

    public void setDispositionId(Long dispositionId) {
        this.dispositionId = dispositionId;
    }

    public Long getEcoId() {
        return ecoId;
    }

    public void setEcoId(Long ecoId) {
        this.ecoId = ecoId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
