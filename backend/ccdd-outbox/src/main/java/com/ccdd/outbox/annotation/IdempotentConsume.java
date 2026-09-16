package com.ccdd.outbox.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消费端强幂等消费切面注解
 * 依靠 sys_inbox_events 表 (consumerGroup, eventId) 唯一索引进行排他拦截
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotentConsume {

    /** 消费者组标识，如 "m23-digital-thread-group" */
    String consumerGroup();
}
