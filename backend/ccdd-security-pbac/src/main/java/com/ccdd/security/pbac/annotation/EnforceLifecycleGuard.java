package com.ccdd.security.pbac.annotation;

import java.lang.annotation.*;

/**
 * 业务实体受控生命周期防护守卫 (落实 ADR-0002 / D02 规约)
 * 对处于受控发布状态 (如 RELEASED, OBSOLETE) 的实体实施修改阻断，必须走受控工程变更流程 (ECR/ECO)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnforceLifecycleGuard {

    /**
     * 实体类型名称
     */
    String entityType();

    /**
     * 不可修改的受控终态列表，默认为 RELEASED 与 OBSOLETE
     */
    String[] immutableStates() default {"RELEASED", "OBSOLETE"};
}
