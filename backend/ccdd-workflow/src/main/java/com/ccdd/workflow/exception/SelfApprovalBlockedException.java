package com.ccdd.workflow.exception;

/**
 * M24 SoD-01 职责分离异常：流程发起人禁止审批自身提交的发布申请与工程修改单
 */
public class SelfApprovalBlockedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SelfApprovalBlockedException(String message) {
        super(message);
    }
}
