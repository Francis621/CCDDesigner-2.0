package com.ccdd.baseline.exception;

/**
 * 关系结构漂移检测异常
 * 当实物装配或工艺状态偏离原设计基线拓扑或发生未授权替换时抛出
 */
public class RelationDriftDetectedException extends RuntimeException {

    public RelationDriftDetectedException(String message) {
        super(message);
    }

    public RelationDriftDetectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
