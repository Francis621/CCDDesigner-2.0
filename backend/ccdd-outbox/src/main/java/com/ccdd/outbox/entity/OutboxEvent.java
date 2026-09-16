package com.ccdd.outbox.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 事务性发件箱实体 (纯原生 Java 实现)
 */
public class OutboxEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long outboxId;
    private String eventId;
    private String eventType;
    private String schemaVersion;
    private String tenantId;
    private String aggregateType;
    private String aggregateId;
    private Long aggregateVersion;
    private String correlationId;
    private String causationId;
    private String payloadJson;
    private String publishStatus;
    private Integer retryCount;
    private Integer maxRetries;
    private Instant nextRetryAt;
    private String errorMessage;
    private Instant occurredAt;
    private Instant createdAt;
    private Instant publishedAt;

    public OutboxEvent() {
    }

    public OutboxEvent(Long outboxId, String eventId, String eventType, String schemaVersion, String tenantId, 
                       String aggregateType, String aggregateId, Long aggregateVersion, String correlationId, 
                       String causationId, String payloadJson, String publishStatus, Integer retryCount, 
                       Integer maxRetries, Instant nextRetryAt, String errorMessage, Instant occurredAt, 
                       Instant createdAt, Instant publishedAt) {
        this.outboxId = outboxId;
        this.eventId = eventId;
        this.eventType = eventType;
        this.schemaVersion = schemaVersion;
        this.tenantId = tenantId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.payloadJson = payloadJson;
        this.publishStatus = publishStatus;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.nextRetryAt = nextRetryAt;
        this.errorMessage = errorMessage;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long outboxId;
        private String eventId;
        private String eventType;
        private String schemaVersion;
        private String tenantId;
        private String aggregateType;
        private String aggregateId;
        private Long aggregateVersion;
        private String correlationId;
        private String causationId;
        private String payloadJson;
        private String publishStatus;
        private Integer retryCount;
        private Integer maxRetries;
        private Instant nextRetryAt;
        private String errorMessage;
        private Instant occurredAt;
        private Instant createdAt;
        private Instant publishedAt;

        public Builder outboxId(Long outboxId) { this.outboxId = outboxId; return this; }
        public Builder eventId(String eventId) { this.eventId = eventId; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder schemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder aggregateType(String aggregateType) { this.aggregateType = aggregateType; return this; }
        public Builder aggregateId(String aggregateId) { this.aggregateId = aggregateId; return this; }
        public Builder aggregateVersion(Long aggregateVersion) { this.aggregateVersion = aggregateVersion; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
        public Builder causationId(String causationId) { this.causationId = causationId; return this; }
        public Builder payloadJson(String payloadJson) { this.payloadJson = payloadJson; return this; }
        public Builder publishStatus(String publishStatus) { this.publishStatus = publishStatus; return this; }
        public Builder retryCount(Integer retryCount) { this.retryCount = retryCount; return this; }
        public Builder maxRetries(Integer maxRetries) { this.maxRetries = maxRetries; return this; }
        public Builder nextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder occurredAt(Instant occurredAt) { this.occurredAt = occurredAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder publishedAt(Instant publishedAt) { this.publishedAt = publishedAt; return this; }

        public OutboxEvent build() {
            return new OutboxEvent(outboxId, eventId, eventType, schemaVersion, tenantId, aggregateType, aggregateId, 
                    aggregateVersion, correlationId, causationId, payloadJson, publishStatus, retryCount, maxRetries, 
                    nextRetryAt, errorMessage, occurredAt, createdAt, publishedAt);
        }
    }

    public Long getOutboxId() { return outboxId; }
    public void setOutboxId(Long outboxId) { this.outboxId = outboxId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    public Long getAggregateVersion() { return aggregateVersion; }
    public void setAggregateVersion(Long aggregateVersion) { this.aggregateVersion = aggregateVersion; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getCausationId() { return causationId; }
    public void setCausationId(String causationId) { this.causationId = causationId; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public String getPublishStatus() { return publishStatus; }
    public void setPublishStatus(String publishStatus) { this.publishStatus = publishStatus; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
    public Instant getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
}
