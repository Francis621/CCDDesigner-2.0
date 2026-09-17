package com.ccdd.model.spi;

import com.ccdd.model.dto.MbseWorkspaceDtos.ValidationOutcomeDto;

/**
 * MBSE Gateway: 形式化语言服务与语义诊断适配器 SPI (遵循 CCD-DEV-SPEC-2.0-M04 §6.1)
 */
public interface ModelValidationAdapter {

    /**
     * 调用 OpenSysML 核心引擎执行语法与语义诊断
     *
     * @param workspaceId 建模工作区 ID
     * @param sysmlModelText 模型全量 SysML 规范文本
     * @param sourceChecksum 待校验的模型 SHA-256 摘要
     * @return 结构化诊断报告
     */
    ValidationOutcomeDto validate(Long workspaceId, String sysmlModelText, String sourceChecksum);

    /**
     * 获取校验服务引擎版本信息 (用于环境审计)
     */
    String getEngineVersion();
}
