package com.ccdd.security.pbac.aspect;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;
import com.ccdd.security.pbac.annotation.EnforceLifecycleGuard;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 业务实体不可变受控生命周期防护切面 (落实 ADR-0002 / D02 规约)
 * 硬阻断对已发布 (RELEASED) 或废弃 (OBSOLETE) 对象的直接变更，防范静默篡改
 */
@Aspect
@Component
public class LifecycleGuardAspect {

    private static final Logger log = LoggerFactory.getLogger(LifecycleGuardAspect.class);

    @Before("@annotation(guard)")
    public void enforceGuard(JoinPoint joinPoint, EnforceLifecycleGuard guard) {
        List<String> immutableStates = Arrays.asList(guard.immutableStates());
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        for (Object arg : args) {
            if (arg == null) continue;

            // 1. 如果入参本身是状态字符串
            if (arg instanceof String stateStr && immutableStates.contains(stateStr.toUpperCase())) {
                blockMutation(guard.entityType(), stateStr);
            }

            // 2. 反射探测入参对象是否包含 getLifecycleState() 方法
            try {
                Method method = arg.getClass().getMethod("getLifecycleState");
                Object stateVal = method.invoke(arg);
                if (stateVal != null && immutableStates.contains(stateVal.toString().toUpperCase())) {
                    blockMutation(guard.entityType(), stateVal.toString());
                }
            } catch (NoSuchMethodException ignored) {
                // 不包含该方法则略过
            } catch (Exception e) {
                log.warn("[LifecycleGuard] 反射提取实体生命周期状态异常", e);
            }
        }
    }

    private void blockMutation(String entityType, String currentState) {
        log.error("[LifecycleGuard] 触发状态机受控硬阻断: 实体类型={}, 当前状态={}", entityType, currentState);
        throw new BusinessException(ErrorCode.CONFLICT, 
                String.format("实体 [%s] 当前处于不可变受控状态 [%s]，严禁直接修改！请发起工程变更申请 (ECR/ECO) 流程。",
                        entityType, currentState));
    }
}
