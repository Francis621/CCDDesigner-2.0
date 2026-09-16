package com.ccdd.outbox.scheduler;

import com.ccdd.outbox.entity.EventEnvelope;
import com.ccdd.outbox.entity.OutboxEvent;
import com.ccdd.outbox.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 发件箱定时投递中继器 (纯原生 Java 实现)
 */
@Component
public class OutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);

    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OutboxPublisherScheduler(OutboxRepository outboxRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.outboxRepository = outboxRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Scheduled(fixedDelayString = "${ccdd.outbox.scan-interval-ms:1000}")
    @Transactional
    public void scanAndPublish() {
        List<OutboxEvent> pendingList = outboxRepository.findPendingEventsForPublish(50);
        if (pendingList.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingList) {
            try {
                EventEnvelope envelope = EventEnvelope.builder()
                        .eventId(event.getEventId())
                        .eventType(event.getEventType())
                        .schemaVersion(event.getSchemaVersion())
                        .tenantId(event.getTenantId())
                        .aggregateType(event.getAggregateType())
                        .aggregateId(event.getAggregateId())
                        .aggregateVersion(event.getAggregateVersion())
                        .occurredAt(event.getOccurredAt())
                        .correlationId(event.getCorrelationId())
                        .causationId(event.getCausationId())
                        .payloadJson(event.getPayloadJson())
                        .build();

                applicationEventPublisher.publishEvent(envelope);

                outboxRepository.markPublished(event.getOutboxId());
                log.debug("[Outbox] 事件投递成功, eventId: {}, eventType: {}", event.getEventId(), event.getEventType());
            } catch (Exception ex) {
                int nextRetry = event.getRetryCount() + 1;
                boolean isDead = nextRetry >= event.getMaxRetries();
                Instant nextRetryAt = Instant.now().plusSeconds((long) Math.pow(2, nextRetry));

                log.error("[Outbox] 事件投递失败, eventId: {}, retry: {}, isDead: {}", 
                        event.getEventId(), nextRetry, isDead, ex);
                outboxRepository.updateRetryFailure(event.getOutboxId(), nextRetry, nextRetryAt, ex.getMessage(), isDead);
            }
        }
    }
}
