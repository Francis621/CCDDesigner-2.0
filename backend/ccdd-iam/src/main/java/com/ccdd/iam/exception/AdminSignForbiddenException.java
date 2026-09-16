package com.ccdd.iam.exception;

/**
 * SoD-04 职责分离违背异常: 系统管理员严禁代行工程技术文件签署放行
 */
public class AdminSignForbiddenException extends SecurityAccessDeniedException {

    public AdminSignForbiddenException(String userId, String actionCode) {
        super("ERR_SOD_ADMIN_SIGN_FORBIDDEN",
                String.format("SoD 职责分离违规 (SoD-04): 系统管理员 [%s] 仅限系统运维管理，严禁越权代行工程签署放行操作 [%s]",
                        userId, actionCode));
    }
}
