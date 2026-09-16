package com.ccdd.iam.aspect;

import com.ccdd.iam.entity.SysQualificationEntity;
import com.ccdd.iam.exception.AdminSignForbiddenException;
import com.ccdd.iam.exception.FieldWriteProhibitedException;
import com.ccdd.iam.exception.InsufficientQualificationException;
import com.ccdd.iam.exception.SelfApprovalForbiddenException;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * M30-IAM 核心工程职责分离 (SoD) 守卫切面
 * 落实四项核心工程职责分离 (SoD-01 ~ SoD-04 物理硬约束)
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class EngineeringSoDGuardAspect {

    /**
     * SoD-01: 禁止创建人自审自批
     *
     * @param creatorId     对象创建人 ID
     * @param currentUserId 当前审批签署人 ID
     */
    public void checkSelfApproval(String creatorId, String currentUserId) {
        if (creatorId != null && creatorId.equalsIgnoreCase(currentUserId)) {
            throw new SelfApprovalForbiddenException(currentUserId);
        }
    }

    /**
     * SoD-02: 签署需求验证 PASS 必须具备专职审查员资质
     *
     * @param userId          用户 ID
     * @param qualifications  用户持有的资质列表
     * @param targetConclusion 目标结论 (如 "PASS")
     */
    public void checkVerificationQualification(String userId, List<SysQualificationEntity> qualifications, String targetConclusion) {
        if ("PASS".equalsIgnoreCase(targetConclusion)) {
            boolean hasReviewerCert = qualifications != null && qualifications.stream()
                    .anyMatch(q -> "VERIFICATION_REVIEWER".equals(q.getQualificationType().name()) && q.isValid());
            if (!hasReviewerCert) {
                throw new InsufficientQualificationException(userId, "VERIFICATION_REVIEWER");
            }
        }
    }

    /**
     * SoD-03: 现场制造与服务人员无权反写已发布设计定义 (EBOM/CAD)
     *
     * @param userId        用户 ID
     * @param globalRoles   用户持有的全局角色列表
     * @param resourceType  操作的目标资源类型 (如 "PartRevision", "BOMViewRevision")
     */
    public void checkFieldWriteAccess(String userId, List<String> globalRoles, String resourceType) {
        if (globalRoles == null) {
            return;
        }
        boolean isFieldStaff = globalRoles.stream()
                .anyMatch(r -> r.matches("ShopFloorOperator|FieldServiceEng"));
        if (isFieldStaff && ("PartRevision".equals(resourceType) || "BOMViewRevision".equals(resourceType))) {
            throw new FieldWriteProhibitedException(userId, resourceType);
        }
    }

    /**
     * SoD-04: 系统管理员默认禁止代行工程技术文件签署与放行
     *
     * @param userId      用户 ID
     * @param globalRoles 用户持有的全局角色列表
     * @param actionCode  操作动作代码 (如 "APPROVE_RELEASE", "FREEZE_BASELINE", "CLOSE_ECO", "SIGN_GATE")
     */
    public void checkAdminEngineeringSign(String userId, List<String> globalRoles, String actionCode) {
        if (globalRoles == null || actionCode == null) {
            return;
        }
        boolean isAdmin = globalRoles.contains("SystemAdmin");
        boolean isEngSignAction = actionCode.matches("APPROVE_RELEASE|FREEZE_BASELINE|CLOSE_ECO|SIGN_GATE");
        if (isAdmin && isEngSignAction) {
            throw new AdminSignForbiddenException(userId, actionCode);
        }
    }
}
