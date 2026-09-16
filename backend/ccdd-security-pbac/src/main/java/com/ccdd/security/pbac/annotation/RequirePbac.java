package com.ccdd.security.pbac.annotation;

import com.ccdd.security.pbac.SecurityClearanceLevel;

import java.lang.annotation.*;

/**
 * 基于工程上下文属性的访问控制 (PBAC) 注解 (落实 D02 专项规格)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePbac {

    /**
     * 目标业务资源类型，如 MODEL_RELEASE, CAD_DOCUMENT, SIMULATION_JOB, ECR
     */
    String resourceType();

    /**
     * 业务操作行为，如 READ, CREATE, UPDATE, APPROVE, RELEASE, DELETE
     */
    String action();

    /**
     * 该资源要求的最低安全密级，默认为 INTERNAL
     */
    SecurityClearanceLevel requiredClearance() default SecurityClearanceLevel.INTERNAL;

    /**
     * 是否强制实施职责分离 (SoD: Separation of Duties)，如申请人不能审批自己的单据
     */
    boolean enforceSoD() default false;
}
