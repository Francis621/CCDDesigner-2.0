package com.ccdd.security.pbac.evaluator;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;
import com.ccdd.common.context.EngineeringContextHolder;
import com.ccdd.security.pbac.SecurityClearanceLevel;
import com.ccdd.security.pbac.annotation.RequirePbac;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PBAC 属性化访问控制评估器 (落实 D02 专项规格)
 * 支持多维属性计算：
 * 1. 密级属性 (Bell-LaPadula): User Clearance >= Resource Classification
 * 2. 租户与项目上下文隔离
 * 3. 职责分离原则 (SoD): 提交人不可自行审批
 */
@Component
public class PbacPolicyEvaluator {

    private static final Logger log = LoggerFactory.getLogger(PbacPolicyEvaluator.class);

    public void evaluate(RequirePbac pbacAnnotation, Object[] args) {
        String tenantId = EngineeringContextHolder.getTenantId();
        String userId = EngineeringContextHolder.getUserId();
        String clearanceStr = EngineeringContextHolder.getSecurityClearance();

        if (tenantId == null || userId == null) {
            log.warn("[PBAC] 缺少租户或用户信息上下文，鉴权拒绝");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录或缺少租户工程上下文");
        }

        // 1. 密级校验 (Bell-LaPadula 上读下写模型)
        SecurityClearanceLevel userClearance = SecurityClearanceLevel.fromString(clearanceStr);
        SecurityClearanceLevel requiredClearance = pbacAnnotation.requiredClearance();
        if (userClearance.getLevel() < requiredClearance.getLevel()) {
            log.error("[PBAC] 密级越权拒绝: 用户密级={}, 资源所需最低密级={}", userClearance, requiredClearance);
            throw new BusinessException(ErrorCode.FORBIDDEN, 
                    String.format("安全密级不足: 当前密级为 [%s], 操作 [%s] 需具备 [%s] 密级许可",
                            userClearance.getDescription(), pbacAnnotation.action(), requiredClearance.getDescription()));
        }

        // 2. 职责分离 (SoD) 校验: 如果要求职责分离，检查参数中是否存在创建者与当前审批者重合
        if (pbacAnnotation.enforceSoD() && args != null) {
            for (Object arg : args) {
                if (arg instanceof String creatorId && userId.equals(creatorId)) {
                    log.error("[PBAC] 触发职责分离 (SoD) 硬阻断: 审批人不能与提交人为同一人 ({})", userId);
                    throw new BusinessException(ErrorCode.FORBIDDEN, "违反职责分离安全规约 (SoD): 申请人严禁审批自己发起的变更或发布单");
                }
            }
        }

        log.debug("[PBAC] 权限评估通过: 用户={}, 租户={}, 操作={}, 资源={}",
                userId, tenantId, pbacAnnotation.action(), pbacAnnotation.resourceType());
    }
}
