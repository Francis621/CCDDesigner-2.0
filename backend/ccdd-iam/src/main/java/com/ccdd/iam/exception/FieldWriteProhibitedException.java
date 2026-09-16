package com.ccdd.iam.exception;

/**
 * SoD-03 职责分离违背异常: 现场制造人员无权反写工程设计定义
 */
public class FieldWriteProhibitedException extends SecurityAccessDeniedException {

    public FieldWriteProhibitedException(String userId, String resourceType) {
        super("ERR_SOD_FIELD_WRITE_PROHIBITED",
                String.format("SoD 职责分离违规 (SoD-03): 现场人员 [%s] 严禁直接反写已发布设计定义 [%s]，请通过现场偏离单申请处置",
                        userId, resourceType));
    }
}
