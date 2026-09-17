package com.ccdd.workflow.exception;

/**
 * M24 CST-M24-01 架构防御异常：工作流审批引擎严禁越权直接修改业务主表生命周期状态
 */
public class CrossMutationForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CrossMutationForbiddenException(String message) {
        super(message);
    }
}
