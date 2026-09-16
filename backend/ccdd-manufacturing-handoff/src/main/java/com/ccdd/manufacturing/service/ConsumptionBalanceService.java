package com.ccdd.manufacturing.service;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;
import com.ccdd.manufacturing.dto.ConsumptionBalanceReport;
import com.ccdd.manufacturing.dto.ConsumptionBalanceReport.ItemBalanceDetail;
import com.ccdd.manufacturing.entity.BomTransformationMapEntity;
import com.ccdd.manufacturing.entity.ManufacturingBomRevisionEntity;
import com.ccdd.manufacturing.entity.TransformationType;
import com.ccdd.manufacturing.repository.ManufacturingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EBOM/MBOM 100% 消耗平衡守恒检验算法服务
 * 遵循《CCD-DEV-SPEC-2.0-D08 专项规格包》第 3 节核心算法规约
 */
@Service
public class ConsumptionBalanceService {

    private static final Logger log = LoggerFactory.getLogger(ConsumptionBalanceService.class);
    private static final BigDecimal EPSILON = new BigDecimal("0.000001");

    private final ManufacturingRepository repository;
    private final ObjectMapper objectMapper;

    public ConsumptionBalanceService(ManufacturingRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * 对指定的 MBOM 修订版执行 100% 消耗平衡与来源合规性校验
     */
    @Transactional
    public ConsumptionBalanceReport verifyBalance(String tenantId, Long mbomRevisionId) {
        ManufacturingBomRevisionEntity mbom = repository.findMbomRevisionById(tenantId, mbomRevisionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到指定的 MBOM 修订版: " + mbomRevisionId));

        List<BomTransformationMapEntity> mappings = repository.findTransformationMapsByMbomRevisionId(tenantId, mbomRevisionId);

        // 获取设计来源 EBOM 物理需求清单 (机床主轴示范 EBOM 基准)
        Map<String, BigDecimal> ebomRequiredMap = loadEbomRequirementMap(mbom.getSourceEbomRevId());
        Map<String, String> ebomNameMap = loadEbomNameMap(mbom.getSourceEbomRevId());

        // 统计 MBOM 累计消耗量 (按照设计物料号分组累计)
        Map<String, BigDecimal> mbomConsumedMap = new HashMap<>();
        List<ItemBalanceDetail> details = new ArrayList<>();

        int illegalSourceCount = 0;

        for (BomTransformationMapEntity map : mappings) {
            // 规则规约 1: 严禁伪造设计来源 (No Fabricated EBOM Source)
            if (map.getTransformType() == TransformationType.MANUFACTURING_ADDED) {
                if (map.getEbomLineId() != null || map.getSourcePartNumber() != null) {
                    illegalSourceCount++;
                    details.add(new ItemBalanceDetail(
                            map.getTargetPartNumber(),
                            "车间工艺辅料 (非法绑定设计源)",
                            BigDecimal.ZERO,
                            map.getConsumedQuantity(),
                            map.getConsumedQuantity().negate(),
                            "FABRICATED_SOURCE",
                            "【工业安全违规】工艺辅料 (MANUFACTURING_ADDED) 严禁伪造 EBOM 设计来源绑定！"
                    ));
                } else {
                    // 合法工艺辅料
                    details.add(new ItemBalanceDetail(
                            map.getTargetPartNumber(),
                            "车间工艺辅料 (合规无来源)",
                            BigDecimal.ZERO,
                            map.getConsumedQuantity(),
                            BigDecimal.ZERO,
                            "OK",
                            "工艺辅料已标记，不参与 EBOM 设计残差计算"
                    ));
                }
            } else {
                // 来源于设计 EBOM 的物料，要求必须有来源
                String sourcePart = map.getSourcePartNumber();
                if (sourcePart == null || !ebomRequiredMap.containsKey(sourcePart)) {
                    illegalSourceCount++;
                    details.add(new ItemBalanceDetail(
                            map.getTargetPartNumber(),
                            "来源孤儿物料",
                            BigDecimal.ZERO,
                            map.getConsumedQuantity(),
                            map.getConsumedQuantity().negate(),
                            "ORPHAN_SOURCE",
                            "【断链违规】引用的设计来源物料号不存在于母版 EBOM 中！"
                    ));
                } else {
                    mbomConsumedMap.put(sourcePart,
                            mbomConsumedMap.getOrDefault(sourcePart, BigDecimal.ZERO).add(map.getConsumedQuantity()));
                }
            }
        }

        // 规则规约 2: 消耗平衡守恒算法计算残差 ΔQ = E_i - Σ M_{i,j}
        int balancedCount = 0;
        int underConsumedCount = 0;
        int overConsumedCount = 0;

        for (Map.Entry<String, BigDecimal> entry : ebomRequiredMap.entrySet()) {
            String partNo = entry.getKey();
            BigDecimal reqQty = entry.getValue();
            BigDecimal conQty = mbomConsumedMap.getOrDefault(partNo, BigDecimal.ZERO);
            BigDecimal delta = reqQty.subtract(conQty);

            String status;
            String diagMsg;

            if (delta.abs().compareTo(EPSILON) < 0) {
                status = "OK";
                diagMsg = "100% 消耗守恒平衡";
                balancedCount++;
            } else if (delta.compareTo(BigDecimal.ZERO) > 0) {
                status = "UNDER_CONSUMED";
                diagMsg = "欠消耗！EBOM 设计量 " + reqQty + "，MBOM 工艺仅分配 " + conQty + "，缺失 " + delta;
                underConsumedCount++;
            } else {
                status = "OVER_CONSUMED";
                diagMsg = "过消耗！EBOM 设计量 " + reqQty + "，MBOM 工艺超额分配 " + conQty + "，超出 " + delta.abs();
                overConsumedCount++;
            }

            details.add(0, new ItemBalanceDetail(
                    partNo,
                    ebomNameMap.getOrDefault(partNo, partNo),
                    reqQty,
                    conQty,
                    delta,
                    status,
                    diagMsg
            ));
        }

        boolean isBalanced = (underConsumedCount == 0 && overConsumedCount == 0 && illegalSourceCount == 0);

        String summary = isBalanced
                ? "【校验通过】EBOM/MBOM 物料消耗严格守恒，残差为 0，符合 MRR 制造就绪发布标准。"
                : "【校验阻断】检测到 " + underConsumedCount + " 项欠消耗、" + overConsumedCount + " 项过消耗、"
                + illegalSourceCount + " 项来源违规，禁止签署 MRR！";

        ConsumptionBalanceReport report = new ConsumptionBalanceReport(
                mbomRevisionId,
                isBalanced,
                ebomRequiredMap.size(),
                balancedCount,
                underConsumedCount,
                overConsumedCount,
                illegalSourceCount,
                details,
                summary
        );

        // 固化报告到持久化仓储
        try {
            String reportJson = objectMapper.writeValueAsString(report);
            repository.updateMbomBalanceStatus(tenantId, mbomRevisionId, isBalanced, reportJson);
        } catch (Exception e) {
            log.error("序列化平衡报告失败: {}", e.getMessage());
        }

        return report;
    }

    // 辅助模拟 EBOM 需求映射 (主轴总成)
    private Map<String, BigDecimal> loadEbomRequirementMap(Long ebomRevId) {
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("MAT-SCR-M12-50", new BigDecimal("16.0000")); // 螺栓 16 颗
        map.put("MAT-BRG-7014C", new BigDecimal("4.0000"));   // 轴承 4 套
        return map;
    }

    private Map<String, String> loadEbomNameMap(Long ebomRevId) {
        Map<String, String> map = new HashMap<>();
        map.put("MAT-SCR-M12-50", "高强度主轴法兰安装螺栓 M12x50 (12.9级)");
        map.put("MAT-BRG-7014C", "精密主轴角接触球轴承 7014C/P4");
        return map;
    }
}
