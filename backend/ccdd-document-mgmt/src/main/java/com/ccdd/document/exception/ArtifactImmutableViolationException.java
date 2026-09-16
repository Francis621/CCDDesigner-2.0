package com.ccdd.document.exception;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;

/**
 * 物理制品不可变规约违反异常 (CST-M19-01: 严禁原位更新或替换物理二进制)
 */
public class ArtifactImmutableViolationException extends BusinessException {

    private final Long artifactId;

    public ArtifactImmutableViolationException(String message, Long artifactId) {
        super(ErrorCode.BAD_REQUEST, message);
        this.artifactId = artifactId;
    }

    public Long getArtifactId() {
        return artifactId;
    }
}
