package com.ccdd.model.spi;

import com.ccdd.model.dto.MbseWorkspaceDtos;
import java.io.InputStream;
import java.util.List;

/**
 * MBSE Gateway: 模型创作与图形环境适配器 SPI (遵循 CCD-DEV-SPEC-2.0-M04 §6.1)
 */
public interface ModelAuthoringAdapter {

    /**
     * 外部项目引用对象
     */
    record ExternalProjectRef(String externalProjectId, String externalToolName, String defaultBranch) {}

    /**
     * 外部元素引用对象
     */
    record ExternalElementRef(String elementId, String qualifiedName, String elementType, String targetPackage) {}

    /**
     * 会话票据
     */
    record SessionTicket(String sessionToken, String viewportUrl, long expiresAtMillis) {}

    /**
     * 模型文本流导出结果
     */
    record ModelExportResult(String externalProjectId, String rawSysML, String sourceChecksum, int elementCount) {}

    /**
     * 在建模工具中初始化模型工程并装载机床行业模板 (如 VMC1000 骨架)
     */
    ExternalProjectRef createProject(String projectCode, String name, String compatibilityProfileId, String templateId);

    /**
     * 建立编辑会话并获取视口加载票据
     */
    SessionTicket openSession(String externalProjectId, String userId, String channel);

    /**
     * 导出全量一致性 SysML 文本流用于语法验证与制品归档
     */
    ModelExportResult exportModelText(String externalProjectId);

    /**
     * 创建模型元素映射 (例如从 PLM 需求投影生成 SysML RequirementUsage)
     */
    ExternalElementRef createElement(String externalProjectId, String plmObjectCode, String plmObjectName, String targetPackage);

    /**
     * 释放排他会话锁
     */
    boolean releaseSession(String sessionToken);

    /**
     * 更新模型草稿文本内容
     */
    void updateModelText(String externalProjectId, String rawSysml);
}
