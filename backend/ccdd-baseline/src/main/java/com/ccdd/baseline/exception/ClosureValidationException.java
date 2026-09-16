package com.ccdd.baseline.exception;

/**
 * 闭包完备性校验异常
 * 当基线纳入的对象处于工作草稿态、存在悬挂依赖、或关联主模型缺失时抛出
 */
public class ClosureValidationException extends RuntimeException {

    public ClosureValidationException(String message) {
        super(message);
    }

    public ClosureValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
