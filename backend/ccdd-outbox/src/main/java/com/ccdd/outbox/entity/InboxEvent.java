package com.ccdd.outbox.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 事务性收件箱实体 (纯原生 Java 实现)
 */
public class InboxEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long inboxId;
    private String tenantId;
    private String consumerGroup;
    private String eventId;
    private String eventType;
    private String consumedStatus;
    private Instant consumedAt;

    public InboxEvent() {
    }

    public InboxEvent(Long inboxId, String tenantId, String consumerGroup, String eventId, String eventType, String consumedStatus, Instant consumedAt) {
        this.inboxId = inboxId;
        this.tenantId = tenantId;
        this.consumerGroup = consumerGroup;
        this.eventId = eventId;
        this.eventType = eventType;
        this.consumedStatus = consumedStatus;
        this.consumedAt = consumedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long inboxId;
        private String tenantId;
        private String consumerGroup;
        private String eventId;
        private String eventType;
        private String consumedStatus;
        private Instant consumedAt;

        public Builder inboxId(Long inboxId) { this.inboxId = inboxId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder consumerGroup(String consumerGroup) { this.consumerGroup = consumerGroup; return this; }
        public Builder eventId(String eventId) { this.eventId = eventId; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder consumedStatus(String consumedStatus) { this.consumedStatus = consumedStatus; return this; }
        public Builder consumedAt(Instant consumedAt) { this.consumedAt = consumedAt; return this; }

        public InboxEvent build() {
            return new InboxEvent(inboxId, tenantId, consumerGroup, eventId, eventType, consumedStatus, consumedAt);
        }
    }

    public Long getInboxId() { return inboxId; }
    public void setInboxId(Long inboxId) { this.inboxId = inboxId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getConsumerGroup() { return consumerGroup; }
    public void setConsumerGroup(String consumerGroup) { this.consumerGroup = consumerGroup; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getConsumedStatus() { return consumedStatus; }
    public void setConsumedStatus(String consumedStatus) { this.consumedStatus = consumedStatus; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }
}
