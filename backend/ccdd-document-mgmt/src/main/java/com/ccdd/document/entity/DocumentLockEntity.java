package com.ccdd.document.entity;

import java.time.Instant;

/**
 * M19 签出排他悲观锁实体 (DocumentLock - 悲观并发控制)
 */
public class DocumentLockEntity {

    private Long revisionId;
    private String lockedByUserId;
    private String clientMachineIp;
    private String checkoutComments;
    private Instant lockedAt;
    private Instant lockExpiresAt;

    public DocumentLockEntity() {
    }

    public DocumentLockEntity(Long revisionId, String lockedByUserId, String clientMachineIp,
                              String checkoutComments, Instant lockedAt, Instant lockExpiresAt) {
        this.revisionId = revisionId;
        this.lockedByUserId = lockedByUserId;
        this.clientMachineIp = clientMachineIp;
        this.checkoutComments = checkoutComments;
        this.lockedAt = lockedAt;
        this.lockExpiresAt = lockExpiresAt;
    }

    public boolean isExpired() {
        return lockExpiresAt != null && Instant.now().isAfter(lockExpiresAt);
    }

    public Long getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(Long revisionId) {
        this.revisionId = revisionId;
    }

    public String getLockedByUserId() {
        return lockedByUserId;
    }

    public void setLockedByUserId(String lockedByUserId) {
        this.lockedByUserId = lockedByUserId;
    }

    public String getClientMachineIp() {
        return clientMachineIp;
    }

    public void setClientMachineIp(String clientMachineIp) {
        this.clientMachineIp = clientMachineIp;
    }

    public String getCheckoutComments() {
        return checkoutComments;
    }

    public void setCheckoutComments(String checkoutComments) {
        this.checkoutComments = checkoutComments;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Instant getLockExpiresAt() {
        return lockExpiresAt;
    }

    public void setLockExpiresAt(Instant lockExpiresAt) {
        this.lockExpiresAt = lockExpiresAt;
    }
}
