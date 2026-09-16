package com.ccdd.manufacturing.service;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;
import com.ccdd.manufacturing.dto.CreateHandoffPackageRequest;
import com.ccdd.manufacturing.entity.BomTransformationMapEntity;
import com.ccdd.manufacturing.entity.HandoffExecutionState;
import com.ccdd.manufacturing.entity.HandoffPackageEntity;
import com.ccdd.manufacturing.entity.ManufacturingBomRevisionEntity;
import com.ccdd.manufacturing.repository.ManufacturingRepository;
import com.ccdd.outbox.service.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 制造下发包组装与可靠发布服务
 * 遵循《CCD-DEV-SPEC-2.0-D08 专项规格包》第 4 节规约
 */
@Service
public class ManufacturingHandoffService {

    private static final Logger log = LoggerFactory.getLogger(ManufacturingHandoffService.class);

    private final ManufacturingRepository repository;
    private final ConsumptionBalanceService balanceService;
    private final OutboxService outboxService;

    public ManufacturingHandoffService(ManufacturingRepository repository,
                                      ConsumptionBalanceService balanceService,
                                      OutboxService outboxService) {
        this.repository = repository;
        this.balanceService = balanceService;
        this.outboxService = outboxService;
    }

    /**
     * 组装并发布制造下发批次包 (带 100% 平衡强校验与 SHA-256 全包签名)
     */
    @Transactional
    public HandoffPackageEntity createAndDispatchPackage(String tenantId, CreateHandoffPackageRequest request) {
        Long mbomRevisionId = request.getMbomRevisionId();

        // 1. 验证 MBOM 修订版存在性
        ManufacturingBomRevisionEntity mbom = repository.findMbomRevisionById(tenantId, mbomRevisionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到指定的 MBOM: " + mbomRevisionId));

        // 2. 严格守恒门禁校验 (若未通过平衡校验，自动触发一次并严格断言)
        if (!Boolean.TRUE.equals(mbom.getIsBalanceVerified())) {
            var report = balanceService.verifyBalance(tenantId, mbomRevisionId);
            if (!Boolean.TRUE.equals(report.getIsBalanced())) {
                throw new BusinessException(ErrorCode.UNPROCESSABLE_ENTITY,
                        "【下发阻断】MBOM 未达成 100% 消耗平衡，存在残差或来源违规，禁止创建制造下发包！");
            }
        }

        // 3. 获取所有待下发的物料工位明细行
        List<BomTransformationMapEntity> lines = repository.findTransformationMapsByMbomRevisionId(tenantId, mbomRevisionId);
        if (lines.isEmpty()) {
            throw new BusinessException(ErrorCode.UNPROCESSABLE_ENTITY, "下发 MBOM 不包含任何工艺分配行！");
        }

        // 4. 计算全包确定性 SHA-256 签名 (防工业网络篡改)
        String packageDigest = calculatePackageDigest(lines);

        // 5. 生成标准工业下发批次号
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String batchNo = "DISPATCH-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        long packageId = Math.abs(UUID.randomUUID().getMostSignificantBits());

        String targetSys = request.getTargetSystem() != null ? request.getTargetSystem() : "MES_PLANT_01";
        String operator = request.getOperatorName() != null ? request.getOperatorName() : "SYSTEM_ENGINEER";

        HandoffPackageEntity pkg = HandoffPackageEntity.builder()
                .packageId(packageId)
                .tenantId(tenantId)
                .handoffBatchNo(batchNo)
                .mbomRevisionId(mbomRevisionId)
                .targetSystem(targetSys)
                .packageDigestSha256(packageDigest)
                .executionState(HandoffExecutionState.ACKNOWLEDGED) // 模拟网络已送达，进入 ACK 态等待逐行对账
                .totalLineCount(lines.size())
                .acceptedLineCount(0)
                .rejectedLineCount(0)
                .createdBy(operator)
                .createdAt(Instant.now())
                .build();

        // 6. 持久化下发批次包
        repository.saveHandoffPackage(pkg);

        // 7. 写入事务发件箱 (Outbox)，保证高可靠异步解耦下发
        outboxService.publishEvent(
                "ManufacturingHandoffDispatchedEvent",
                "MANUFACTURING_HANDOFF",
                pkg.getPackageId().toString(),
                1L,
                "{\"batchNo\":\"" + batchNo + "\",\"digest\":\"" + packageDigest + "\",\"lines\":" + lines.size() + "}"
        );

        log.info("成功创建并下发制造批次包: batchNo={}, lines={}, digest={}", batchNo, lines.size(), packageDigest);
        return pkg;
    }

    /**
     * 计算下发全包内容 SHA-256 签名
     */
    private String calculatePackageDigest(List<BomTransformationMapEntity> lines) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (BomTransformationMapEntity line : lines) {
                String lineStr = line.getMbomLineNumber() + "|" + line.getTargetPartNumber() + "|"
                        + line.getConsumedQuantity() + "|" + line.getTransformType().name() + "|"
                        + (line.getOperationSequence() != null ? line.getOperationSequence() : 0);
                md.update(lineStr.getBytes(StandardCharsets.UTF_8));
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }
}
