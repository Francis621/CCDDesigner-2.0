package com.ccdd.project.exception;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;

/**
 * 任务依赖成环异常 (CyclicTaskDependencyException)
 * 对应规约: M02-F02 DAG 有向无环图循环检测与 TC-M02-02
 */
public class CyclicTaskDependencyException extends BusinessException {

    private final String cyclePath;

    public CyclicTaskDependencyException(String cyclePath) {
        super(ErrorCode.UNPROCESSABLE_ENTITY, "检测到任务依赖网络存在死循环回路违规 (DAG Cycle Detected): " + cyclePath);
        this.cyclePath = cyclePath;
    }

    public String getCyclePath() {
        return cyclePath;
    }
}
