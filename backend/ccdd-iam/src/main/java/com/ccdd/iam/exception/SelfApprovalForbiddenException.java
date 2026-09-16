package com.ccdd.iam.exception;

/**
 * SoD-01 职责分离违背异常: 创建人严禁自审自批
 */
public class SelfApprovalForbiddenException extends SecurityAccessDeniedException {

    public SelfApprovalForbiddenException(String creatorId) {
        super("ERR_SOD_SELF_APPROVAL_FORBIDDEN",
                String.format("SoD 职责分离违规 (SoD-01): 创建人 [%s] 严禁作为自身提交申请的审批签署人", creatorId));
    }
}
