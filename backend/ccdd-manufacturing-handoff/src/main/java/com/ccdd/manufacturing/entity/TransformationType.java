package com.ccdd.manufacturing.entity;

/**
 * EBOM 到 MBOM 转换类型枚举
 * 遵循《CCD-DEV-SPEC-2.0-D08 专项规格包》第 3.1 节规约
 */
public enum TransformationType {
    /**
     * 直接映射 (1 对 1)：EBOM 物料项完全对应 1 个 MBOM 项，数量 100% 一对一承接
     */
    DIRECT_1_TO_1("直接映射"),

    /**
     * 工艺拆分 (1 对 N)：1 个 EBOM 项在制造中被拆分到不同工位或不同装配步骤消耗
     */
    SPLIT_1_TO_N("工艺拆分"),

    /**
     * 工艺虚拟件重组：车间预分装引入虚拟总成 (is_phantom=true)，子物料严格溯源自 EBOM
     */
    PHANTOM_RESTRUCTURE("工艺虚拟件重组"),

    /**
     * 工艺制造新增物料：车间新增工艺消耗辅料 (螺纹紧固胶、润滑脂、防锈油等)，严禁伪造设计来源 (source_ebom_item_id 必须为 NULL)
     */
    MANUFACTURING_ADDED("工艺制造新增辅料");

    private final String description;

    TransformationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
