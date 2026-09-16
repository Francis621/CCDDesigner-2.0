package com.ccdd.outbox.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 统一领域事件包络契约 (Event Envelope)
 */
public class EventEnvelope implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private String schemaVersion = "1.0";
    private String tenantId;
    private String aggregateType;
    private String aggregateId;
    private Long aggregateVersion = 1L;
    private Instant occurredAt;
    private String correlationId;
    private String causationId;
    private String payloadJson;

    public EventEnvelope() {
    }

    public EventEnvelope(String eventId, String eventType, String schemaVersion, String tenantId, 
                         String aggregateType, String aggregateId, Long aggregateVersion, 
                         Instant occurredAt, String correlationId, String causationId, String payloadJson) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.schemaVersion = schemaVersion != null ? schemaVersion : "1.0";
        this.tenantId = tenantId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion != null ? aggregateVersion : 1L;
        this.occurredAt = occurredAt;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.payloadJson = payloadJson;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventId;
        private String eventType;
        private String schemaVersion = "1.0";
        private String tenantId;
        private String aggregateType;
        private String aggregateId;
        private Long aggregateVersion = 1L;
        private Instant occurredAt;
        private String correlationId;
        private String causationId;
        private String payloadJson;

        public Builder eventId(String eventId) { this.eventId = eventId; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder schemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder aggregateType(String aggregateType) { this.aggregateType = aggregateType; return this; }
        public Builder aggregateId(String aggregateId) { this.aggregateId = aggregateId; return this; }
        public Builder aggregateVersion(Long aggregateVersion) { this.aggregateVersion = aggregateVersion; return this; }
        public Builder occurredAt(Instant occurredAt) { this.occurredAt = occurredAt; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
        public Builder causationId(String causationId) { this.causationId = causationId; return this; }
        public Builder payloadJson(String payloadJson) { this.payloadJson = payloadJson; return this; }

        public EventEnvelope build() {
            return new EventEnvelope(eventId, eventType, schemaVersion, tenantId, aggregateType, aggregateId, 
                    aggregateVersion, occurredAt, correlationId, causationId, payloadJson);
        }
    }

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
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getCausationId() { return causationId; }
    public void setCausationId(String causationId) { this.causationId = causationId; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}
