package com.ccdd.outbox.aspect;

import com.alibaba.fastjson2.JSON;
import com.ccdd.common.api.Result;
import com.ccdd.common.context.EngineeringContextHolder;
import com.ccdd.outbox.annotation.IdempotentApi;
import com.ccdd.outbox.repository.IdempotencyRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Optional;

/**
 * HTTP 接口幂等性切面 (AT-04-IDEM 落地)
 */
@Aspect
@Component
public class IdempotentApiAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotentApiAspect.class);

    private final IdempotencyRepository idempotencyRepository;

    public IdempotentApiAspect(IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    @Around("@annotation(idempotentApi)")
    public Object enforceApiIdempotency(ProceedingJoinPoint joinPoint, IdempotentApi idempotentApi) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return joinPoint.proceed();
        }

        String tenantId = EngineeringContextHolder.getTenantId();
        if (tenantId == null) {
            tenantId = "SYSTEM";
        }

        Optional<IdempotencyRepository.IdempotencyRecord> existing = 
                idempotencyRepository.findValidRecord(tenantId, idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRepository.IdempotencyRecord record = existing.get();
            log.warn("[Idempotency] 触发接口重放幂等保护, key: {}, uri: {}", idempotencyKey, record.getRequestUri());
            if (record.getResponseBodyJson() != null) {
                return JSON.parseObject(record.getResponseBodyJson(), Result.class);
            }
            return Result.success();
        }

        Object result = joinPoint.proceed();

        Instant expiresAt = Instant.now().plusSeconds(idempotentApi.expireSeconds());
        String responseJson = JSON.toJSONString(result);
        idempotencyRepository.save(
                idempotencyKey,
                tenantId,
                request.getRequestURI(),
                "sha256-req-payload",
                200,
                responseJson,
                expiresAt
        );

        return result;
    }
}
