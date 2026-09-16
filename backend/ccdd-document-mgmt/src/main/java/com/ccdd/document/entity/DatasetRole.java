package com.ccdd.document.entity;

/**
 * 文档用途与数据集文件角色枚举 (M19 规格)
 */
public enum DatasetRole {
    PRIMARY_NATIVE("PRIMARY_NATIVE", "原生设计源文件 (如 SLDPRT, SLDASM, DWG, DOCX)"),
    DERIVATIVE_PDF("DERIVATIVE_PDF", "转换生成的受控审图预览 PDF"),
    DERIVATIVE_VIEW3D("DERIVATIVE_VIEW3D", "Web 轻量化预览网格 (如 glTF, 3D Tiles)"),
    ATTACHMENT("ATTACHMENT", "辅助参考附件与设计计算书"),
    SIGNATURE_STAMP("SIGNATURE_STAMP", "电子图章与防伪防篡改凭证");

    private final String code;
    private final String description;

    DatasetRole(String code, String description) {
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
