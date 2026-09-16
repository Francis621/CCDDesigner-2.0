package com.ccdd.iam.exception;

/**
 * SoD-02 职责分离违背异常: 缺少专职验证资质证书
 */
public class InsufficientQualificationException extends SecurityAccessDeniedException {

    public InsufficientQualificationException(String userId, String requiredQualification) {
        super("ERR_SOD_INSUFFICIENT_QUALIFICATION",
                String.format("SoD 职责分离违规 (SoD-02): 用户 [%s] 缺少有效的 [%s] 资质认证证书，无权签署验证 PASS 结论",
                        userId, requiredQualification));
    }
}
