package com.ccdd.outbox.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口防重放幂等切面注解
 * 拦截请求头中的 Idempotency-Key，防止网络超时重发导致重复创建/提交
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotentApi {

    /** 幂等记录在数据库缓存中的过期秒数，默认 86400 (24小时) */
    int expireSeconds() default 86400;
}
