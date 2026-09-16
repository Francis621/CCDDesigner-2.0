package com.ccdd.change.dto;

import com.ccdd.change.entity.EcoStatus;
import java.time.Instant;

/**
 * 关闭变更单响应 DTO
 */
public class CloseEcoResponse {

    private Long ecoId;
    private String ecoNumber;
    private EcoStatus status;
    private Instant closedAt;
    private Integer completedTasksCount;
    private Integer completedReceiptsCount;
    private String message;

    public CloseEcoResponse() {
    }

    public CloseEcoResponse(Long ecoId, String ecoNumber, EcoStatus status, Instant closedAt,
                            Integer completedTasksCount, Integer completedReceiptsCount, String message) {
        this.ecoId = ecoId;
        this.ecoNumber = ecoNumber;
        this.status = status;
        this.closedAt = closedAt;
        this.completedTasksCount = completedTasksCount;
        this.completedReceiptsCount = completedReceiptsCount;
        this.message = message;
    }

    public Long getEcoId() {
        return ecoId;
    }

    public void setEcoId(Long ecoId) {
        this.ecoId = ecoId;
    }

    public String getEcoNumber() {
        return ecoNumber;
    }

    public void setEcoNumber(String ecoNumber) {
        this.ecoNumber = ecoNumber;
    }

    public EcoStatus getStatus() {
        return status;
    }

    public void setStatus(EcoStatus status) {
        this.status = status;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public Integer getCompletedTasksCount() {
        return completedTasksCount;
    }

    public void setCompletedTasksCount(Integer completedTasksCount) {
        this.completedTasksCount = completedTasksCount;
    }

    public Integer getCompletedReceiptsCount() {
        return completedReceiptsCount;
    }

    public void setCompletedReceiptsCount(Integer completedReceiptsCount) {
        this.completedReceiptsCount = completedReceiptsCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
