package com.ccdd.common.api;

/**
 * 平台通用业务错误码枚举 (零依赖原生 Java)
 */
public enum ErrorCode {

    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数不合法"),
    UNAUTHORIZED(401, "尚未登录或令牌已失效"),
    FORBIDDEN(403, "PBAC 权限策略拒绝访问"),
    NOT_FOUND(404, "目标业务对象不存在"),
    CONFLICT(409, "业务状态或并发版本冲突"),
    GONE(410, "资源已过期或失效 (迟到数据丢弃)"),
    UNPROCESSABLE_ENTITY(422, "语义准入校验未通过"),
    INTERNAL_SERVER_ERROR(500, "系统内部执行异常"),

    // 业务专属错误码 (600 起)
    IMMUTABILITY_VIOLATION(601, "违反发布不可变性约束，禁止覆写已发布版本"),
    CIRCULAR_DEPENDENCY_DETECTED(602, "检测到参数或结构循环依赖"),
    SOD_VIOLATION(603, "违反职责分离策略 (如创建者禁止自审)"),
    CHECKSUM_MISMATCH(604, "文件 SHA-256 完整性校验不匹配"),
    IDEMPOTENCY_CONFLICT(605, "幂等凭证正在处理中或已完成");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
