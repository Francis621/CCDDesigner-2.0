package com.ccdd.baseline.entity;

/**
 * 基线固化成员角色枚举 (M21 规格)
 */
public enum MemberRole {
    REQUIREMENT("REQUIREMENT", "需求条目"),
    SYSTEM_MODEL("SYSTEM_MODEL", "SysML v2 模型发布包"),
    PARAMETER_SET("PARAMETER_SET", "冻结参数集"),
    EBOM_ROOT("EBOM_ROOT", "顶层装配件"),
    BOM_COMPONENT("BOM_COMPONENT", "结构子件"),
    CAD_DRAWING("CAD_DRAWING", "工程图纸/3D源文件制品"),
    SOFTWARE_PACKAGE("SOFTWARE_PACKAGE", "数控固件/PLC/驱动程序"),
    SIM_RUN_EVIDENCE("SIM_RUN_EVIDENCE", "仿真执行凭证"),
    TEST_REPORT_EVIDENCE("TEST_REPORT_EVIDENCE", "实机试验报告凭证");

    private final String code;
    private final String description;

    MemberRole(String code, String description) {
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
