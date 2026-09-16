package com.ccdd.document.dto;

import java.time.Instant;

/**
 * 文档签出响应报文 (OpenAPI §8.3)
 */
public class CheckoutResponse {

    private Long revisionId;
    private Boolean isLocked;
    private String lockedBy;
    private Instant lockedAt;
    private Instant expiresAt;
    private String ticket;

    public CheckoutResponse() {
    }

    public CheckoutResponse(Long revisionId, Boolean isLocked, String lockedBy,
                            Instant lockedAt, Instant expiresAt, String ticket) {
        this.revisionId = revisionId;
        this.isLocked = isLocked;
        this.lockedBy = lockedBy;
        this.lockedAt = lockedAt;
        this.expiresAt = expiresAt;
        this.ticket = ticket;
    }

    public Long getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(Long revisionId) {
        this.revisionId = revisionId;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean locked) {
        isLocked = locked;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
