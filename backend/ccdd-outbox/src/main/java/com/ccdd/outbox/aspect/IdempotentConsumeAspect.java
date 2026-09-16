package com.ccdd.outbox.aspect;

import com.ccdd.common.util.SnowflakeIdGenerator;
import com.ccdd.outbox.annotation.IdempotentConsume;
import com.ccdd.outbox.entity.EventEnvelope;
import com.ccdd.outbox.repository.InboxRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 消费端强幂等拦截切面 (AT-INBOX-01 落地)
 */
@Aspect
@Component
public class IdempotentConsumeAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotentConsumeAspect.class);

    private final InboxRepository inboxRepository;

    public IdempotentConsumeAspect(InboxRepository inboxRepository) {
        this.inboxRepository = inboxRepository;
    }

    @Around("@annotation(idempotentConsume) && args(eventEnvelope,..)")
    public Object enforceIdempotentConsume(
            ProceedingJoinPoint joinPoint,
            IdempotentConsume idempotentConsume,
            EventEnvelope eventEnvelope) throws Throwable {

        String consumerGroup = idempotentConsume.consumerGroup();
        String eventId = eventEnvelope.getEventId();
        String tenantId = eventEnvelope.getTenantId() != null ? eventEnvelope.getTenantId() : "SYSTEM";
        String eventType = eventEnvelope.getEventType();

        Long inboxId = SnowflakeIdGenerator.generateId();

        boolean isFirstConsume = inboxRepository.recordIfAbsent(inboxId, tenantId, consumerGroup, eventId, eventType);
        if (!isFirstConsume) {
            log.warn("[Inbox] 检测到事件已消费过，自动幂等跳过. consumerGroup: {}, eventId: {}, eventType: {}",
                    consumerGroup, eventId, eventType);
            return null;
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            log.error("[Inbox] 消费事件发生业务异常. consumerGroup: {}, eventId: {}", consumerGroup, eventId, ex);
            Long deadLetterId = SnowflakeIdGenerator.generateId();
            inboxRepository.recordDeadLetter(deadLetterId, tenantId, eventId, consumerGroup, 
                    eventEnvelope.getPayloadJson(), ex.toString());
            throw ex;
        }
    }
}
