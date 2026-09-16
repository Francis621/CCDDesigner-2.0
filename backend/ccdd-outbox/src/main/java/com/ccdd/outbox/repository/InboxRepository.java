package com.ccdd.outbox.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InboxRepository {

    private final JdbcTemplate jdbcTemplate;

    public InboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean recordIfAbsent(Long inboxId, String tenantId, String consumerGroup, String eventId, String eventType) {
        String sql = """
            INSERT INTO sys_inbox_events (inbox_id, tenant_id, consumer_group, event_id, event_type, consumed_status, consumed_at)
            VALUES (?, ?, ?, ?, ?, 'SUCCESS', CURRENT_TIMESTAMP)
        """;
        try {
            jdbcTemplate.update(sql, inboxId, tenantId, consumerGroup, eventId, eventType);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    public void recordDeadLetter(Long deadLetterId, String tenantId, String eventId, String consumerGroup, String payloadJson, String stackTrace) {
        String sql = """
            INSERT INTO sys_dead_letter_events (dead_letter_id, tenant_id, event_id, consumer_group, event_payload_json, stack_trace, failed_at, resolved_state)
            VALUES (?, ?, ?, ?, ?::jsonb, ?, CURRENT_TIMESTAMP, 'UNRESOLVED')
        """;
        jdbcTemplate.update(sql, deadLetterId, tenantId, eventId, consumerGroup, payloadJson, stackTrace);
    }
}
