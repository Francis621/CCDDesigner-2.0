package com.ccdd.model.spi.impl;

import com.ccdd.model.spi.ModelAuthoringAdapter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SysON 图形建模与工程适配器实现 (预置 VMC1000 五轴加工中心模型包)
 */
@Component
public class SysONAdapterImpl implements ModelAuthoringAdapter {

    private final Map<String, String> projectModelTexts = new ConcurrentHashMap<>();
    private final Map<String, SessionTicket> activeSessions = new ConcurrentHashMap<>();

    private static final String DEFAULT_VMC1000_SYSML = """
        package VMC1000_SystemModel {
            doc /* CCDDesigner 2.0 数控机床正向设计系统模型 - VMC1000 */
            
            package '01_Requirements' {
                doc /* 映射 PLM M03 需求规范集 */
                requirement def XAxisStrokeReq {
                    doc /* X轴有效行程不小于 1000mm */
                    attribute minStroke: Real = 1000.0;
                }
            }
            
            package '02_FunctionalBehavior' {
                action def HighSpeedMillingAction {
                    in item workpiece: Workpiece;
                    out item finishedPart: Workpiece;
                }
            }
            
            package '03_LogicalArchitecture' {
                part def CncMotionController {
                    port busPort: IndustrialEthernetPort;
                }
            }
            
            package '04_PhysicalArchitecture' {
                part def VMC1000Structure {
                    part xFeedSystem: XAxisFeedSystem;
                    part spindleModule: HighSpeedSpindle;
                }
                
                part def XAxisFeedSystem {
                    attribute stroke: Real = 1020.0;
                    attribute maxVelocity: Real = 48.0;
                    port ctrlPort: ServoInterfacePort;
                }
                
                part def HighSpeedSpindle {
                    attribute maxSpeed: Real = 18000.0;
                    attribute ratedTorque: Real = 120.0;
                }
            }
            
            package '05_Interfaces' {
                interface def ServoInterfacePort;
                interface def IndustrialEthernetPort;
            }
            
            package '06_Parameters' {
                doc /* M07 受控工程参数映射 */
                attribute def MachineMassLimit = 8500.0;
            }
            
            package '07_VerificationContext' {
                doc /* CDR / PDR 验证分析工况与约束 */
            }
        }
        """;

    @Override
    public ExternalProjectRef createProject(String projectCode, String name, String compatibilityProfileId, String templateId) {
        String externalProjectId = "syson-proj-" + UUID.randomUUID().toString().substring(0, 8);
        projectModelTexts.put(externalProjectId, DEFAULT_VMC1000_SYSML);
        return new ExternalProjectRef(externalProjectId, "SYSON", "main");
    }

    @Override
    public SessionTicket openSession(String externalProjectId, String userId, String channel) {
        String sessionToken = "tkt_sess_" + UUID.randomUUID().toString().replace("-", "");
        long expiresAt = System.currentTimeMillis() + 120 * 60 * 1000L;
        String viewportUrl = "https://syson.ccddesigner.internal/workspaces/" + externalProjectId +
                "?token=" + sessionToken + "&channel=" + channel;
        SessionTicket ticket = new SessionTicket(sessionToken, viewportUrl, expiresAt);
        activeSessions.put(sessionToken, ticket);
        return ticket;
    }

    @Override
    public ModelExportResult exportModelText(String externalProjectId) {
        String raw = projectModelTexts.getOrDefault(externalProjectId, DEFAULT_VMC1000_SYSML);
        String checksum = calculateSha256(raw);
        int elementCount = raw.split("def |part |requirement ").length + 7;
        return new ModelExportResult(externalProjectId, raw, checksum, elementCount);
    }

    @Override
    public ExternalElementRef createElement(String externalProjectId, String plmObjectCode, String plmObjectName, String targetPackage) {
        String elementId = "syson_elem_" + UUID.randomUUID().toString().substring(0, 8);
        String qualifiedName = "VMC1000_SystemModel::'01_Requirements'::" + plmObjectCode + "_" + elementId.substring(11);
        
        // 追加注入到当前工程文本中
        String current = projectModelTexts.getOrDefault(externalProjectId, DEFAULT_VMC1000_SYSML);
        String requirementUsageSnippet = "\n    // [PLM M03 Projecting] " + plmObjectCode + " - " + plmObjectName + "\n" +
                "    requirement " + plmObjectCode + "Usage : '01_Requirements'::XAxisStrokeReq {\n" +
                "        doc /* 权威主数据引用: " + plmObjectCode + " */\n" +
                "    }\n";
        projectModelTexts.put(externalProjectId, current + requirementUsageSnippet);

        return new ExternalElementRef(elementId, qualifiedName, "RequirementUsage", targetPackage);
    }

    @Override
    public boolean releaseSession(String sessionToken) {
        return activeSessions.remove(sessionToken) != null;
    }

    @Override
    public void updateModelText(String externalProjectId, String rawSysml) {
        if (rawSysml != null) {
            projectModelTexts.put(externalProjectId, rawSysml);
        }
    }

    private String calculateSha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "hash_calc_error_" + System.currentTimeMillis();
        }
    }
}
