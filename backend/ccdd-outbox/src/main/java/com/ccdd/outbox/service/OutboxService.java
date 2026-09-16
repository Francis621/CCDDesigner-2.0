package com.ccdd.outbox.service;

import com.alibaba.fastjson2.JSON;
import com.ccdd.common.context.EngineeringContextHolder;
import com.ccdd.common.util.SnowflakeIdGenerator;
import com.ccdd.outbox.entity.OutboxEvent;
import com.ccdd.outbox.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * 事务性发件箱服务：在当前本地业务事务中，将领域事件安全暂存至数据库
 */
@Service
public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxRepository outboxRepository;

    public OutboxService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishEvent(String eventType, String aggregateType, String aggregateId, Long aggregateVersion, Object payload) {
        String tenantId = EngineeringContextHolder.getTenantId();
        if (tenantId == null) {
            tenantId = "SYSTEM";
        }

        String eventId = "evt-" + UUID.randomUUID().toString();
        String correlationId = "tx-" + UUID.randomUUID().toString();
        Instant now = Instant.now();
        String payloadJson = JSON.toJSONString(payload);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .outboxId(SnowflakeIdGenerator.generateId())
                .eventId(eventId)
                .eventType(eventType)
                .schemaVersion("1.0")
                .tenantId(tenantId)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .aggregateVersion(aggregateVersion != null ? aggregateVersion : 1L)
                .correlationId(correlationId)
                .causationId(null)
                .payloadJson(payloadJson)
                .publishStatus("PENDING")
                .retryCount(0)
                .maxRetries(5)
                .nextRetryAt(null)
                .errorMessage(null)
                .occurredAt(now)
                .createdAt(now)
                .build();

        outboxRepository.insert(outboxEvent);
        log.info("[Outbox] 成功在本地事务中暂存领域事件, eventType: {}, aggregateId: {}, eventId: {}", 
                eventType, aggregateId, eventId);
    }
}
