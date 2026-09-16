package com.ccdd.baseline.exception;

/**
 * 基线不可变违例异常
 * 当已冻结或已归档的基线尝试被直接修改、插入成员或删除时抛出 (CST-M21-01)
 */
public class BaselineImmutableViolationException extends RuntimeException {

    public BaselineImmutableViolationException(String message) {
        super(message);
    }

    public BaselineImmutableViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
