package com.ccdd.common.context;

/**
 * 基于 ThreadLocal 的当前线程工程上下文持有者
 */
public class EngineeringContextHolder {

    private static final ThreadLocal<EngineeringContext> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setContext(EngineeringContext context) {
        CONTEXT_HOLDER.set(context);
    }

    public static EngineeringContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    public static String getTenantId() {
        EngineeringContext ctx = getContext();
        return ctx != null ? ctx.getTenantId() : null;
    }

    public static String getProjectId() {
        EngineeringContext ctx = getContext();
        return ctx != null ? ctx.getProjectId() : null;
    }

    public static String getUserId() {
        EngineeringContext ctx = getContext();
        return ctx != null ? ctx.getOperatorUserId() : null;
    }

    public static String getSecurityClearance() {
        EngineeringContext ctx = getContext();
        return ctx != null ? ctx.getSecurityClearance() : null;
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
