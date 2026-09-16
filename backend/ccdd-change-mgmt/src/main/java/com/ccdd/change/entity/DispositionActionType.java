package com.ccdd.change.entity;

/**
 * M22 物料与实物现场生效处置方案枚举
 */
public enum DispositionActionType {
    SCRAP("SCRAP", "物理报废 (已加工零部件/在制品)"),
    REWORK("REWORK", "现场按新图纸返修/重新调机"),
    USE_UP("USE_UP", "自然消耗过渡 (允许在旧批次中继续装配)"),
    AS_IS("AS_IS", "维持原样 (特批让步使用)");

    private final String code;
    private final String description;

    DispositionActionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
