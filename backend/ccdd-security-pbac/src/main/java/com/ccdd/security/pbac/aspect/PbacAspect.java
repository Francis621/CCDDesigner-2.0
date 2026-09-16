package com.ccdd.security.pbac.aspect;

import com.ccdd.security.pbac.annotation.RequirePbac;
import com.ccdd.security.pbac.evaluator.PbacPolicyEvaluator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * PBAC 动态鉴权切面 (落实 D02 专项规格)
 */
@Aspect
@Component
public class PbacAspect {

    private final PbacPolicyEvaluator policyEvaluator;

    public PbacAspect(PbacPolicyEvaluator policyEvaluator) {
        this.policyEvaluator = policyEvaluator;
    }

    @Before("@annotation(requirePbac)")
    public void checkPermission(JoinPoint joinPoint, RequirePbac requirePbac) {
        policyEvaluator.evaluate(requirePbac, joinPoint.getArgs());
    }
}
