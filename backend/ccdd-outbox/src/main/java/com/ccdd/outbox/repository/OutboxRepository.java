package com.ccdd.outbox.repository;

import com.ccdd.outbox.entity.OutboxEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class OutboxRepository {

    private final JdbcTemplate jdbcTemplate;

    public OutboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<OutboxEvent> rowMapper = (rs, rowNum) -> OutboxEvent.builder()
            .outboxId(rs.getLong("outbox_id"))
            .eventId(rs.getString("event_id"))
            .eventType(rs.getString("event_type"))
            .schemaVersion(rs.getString("schema_version"))
            .tenantId(rs.getString("tenant_id"))
            .aggregateType(rs.getString("aggregate_type"))
            .aggregateId(rs.getString("aggregate_id"))
            .aggregateVersion(rs.getLong("aggregate_version"))
            .correlationId(rs.getString("correlation_id"))
            .causationId(rs.getString("causation_id"))
            .payloadJson(rs.getString("payload_json"))
            .publishStatus(rs.getString("publish_status"))
            .retryCount(rs.getInt("retry_count"))
            .maxRetries(rs.getInt("max_retries"))
            .nextRetryAt(rs.getTimestamp("next_retry_at") != null ? rs.getTimestamp("next_retry_at").toInstant() : null)
            .errorMessage(rs.getString("error_message"))
            .occurredAt(rs.getTimestamp("occurred_at").toInstant())
            .createdAt(rs.getTimestamp("created_at").toInstant())
            .publishedAt(rs.getTimestamp("published_at") != null ? rs.getTimestamp("published_at").toInstant() : null)
            .build();

    public void insert(OutboxEvent event) {
        String sql = """
            INSERT INTO sys_outbox_events (
                outbox_id, event_id, event_type, schema_version, tenant_id,
                aggregate_type, aggregate_id, aggregate_version, correlation_id,
                causation_id, payload_json, publish_status, retry_count,
                max_retries, next_retry_at, occurred_at, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                event.getOutboxId(),
                event.getEventId(),
                event.getEventType(),
                event.getSchemaVersion(),
                event.getTenantId(),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getAggregateVersion(),
                event.getCorrelationId(),
                event.getCausationId(),
                event.getPayloadJson(),
                event.getPublishStatus(),
                event.getRetryCount(),
                event.getMaxRetries(),
                event.getNextRetryAt() != null ? Timestamp.from(event.getNextRetryAt()) : null,
                Timestamp.from(event.getOccurredAt()),
                Timestamp.from(event.getCreatedAt())
        );
    }

    public List<OutboxEvent> findPendingEventsForPublish(int limit) {
        // 使用 SKIP LOCKED 防止分布式多实例并发抢占同一批待投递记录
        String sql = """
            SELECT * FROM sys_outbox_events 
            WHERE publish_status IN ('PENDING', 'FAILED') 
              AND (next_retry_at IS NULL OR next_retry_at <= CURRENT_TIMESTAMP)
            ORDER BY created_at ASC 
            LIMIT ? 
            FOR UPDATE SKIP LOCKED
        """;
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public void markPublished(Long outboxId) {
        String sql = "UPDATE sys_outbox_events SET publish_status = 'PUBLISHED', published_at = CURRENT_TIMESTAMP WHERE outboxId = ?";
        jdbcTemplate.update(sql, outboxId);
    }

    public void updateRetryFailure(Long outboxId, int nextRetryCount, Instant nextRetryAt, String errorMsg, boolean isDead) {
        String status = isDead ? "FAILED" : "PENDING";
        String sql = """
            UPDATE sys_outbox_events 
            SET publish_status = ?, retry_count = ?, next_retry_at = ?, error_message = ? 
            WHERE outbox_id = ?
        """;
        jdbcTemplate.update(sql, status, nextRetryCount, Timestamp.from(nextRetryAt), errorMsg, outboxId);
    }
}
