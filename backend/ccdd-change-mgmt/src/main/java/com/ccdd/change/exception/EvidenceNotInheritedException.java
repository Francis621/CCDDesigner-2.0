package com.ccdd.change.exception;

/**
 * 验证证据不继承异常 (ADR-08 落地)
 * 当验证用例或仿真项标记为 RE_VERIFY，却尝试直接复用旧版本的 PASS 结论时抛出
 */
public class EvidenceNotInheritedException extends RuntimeException {

    private final String verificationCaseCode;

    public EvidenceNotInheritedException(String message) {
        super(message);
        this.verificationCaseCode = null;
    }

    public EvidenceNotInheritedException(String verificationCaseCode, String message) {
        super(message);
        this.verificationCaseCode = verificationCaseCode;
    }

    public String getVerificationCaseCode() {
        return verificationCaseCode;
    }
}
