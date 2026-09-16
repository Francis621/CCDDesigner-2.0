package com.ccdd.manufacturing.service;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;
import com.ccdd.manufacturing.dto.BatchReceiptReconciliationRequest;
import com.ccdd.manufacturing.dto.BatchReceiptReconciliationRequest.LineReceiptItemDto;
import com.ccdd.manufacturing.dto.ReconciliationSummaryReport;
import com.ccdd.manufacturing.dto.ReconciliationSummaryReport.ReconciliationLineItemDetail;
import com.ccdd.manufacturing.entity.HandoffExecutionState;
import com.ccdd.manufacturing.entity.HandoffPackageEntity;
import com.ccdd.manufacturing.entity.LineItemReceiptEntity;
import com.ccdd.manufacturing.entity.ReceiptItemStatus;
import com.ccdd.manufacturing.repository.ManufacturingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 逐项业务流水回执异步对账引擎
 * 遵循《CCD-DEV-SPEC-2.0-D08 专项规格包》第 5 节核心规约:
 * 1. HTTP 200 不等于业务消费成功，必须逐行流水确认
 * 2. 幂等入库与状态机自动闭环跃迁
 */
@Service
public class ReceiptReconciliationEngine {

    private static final Logger log = LoggerFactory.getLogger(ReceiptReconciliationEngine.class);

    private final ManufacturingRepository repository;

    public ReceiptReconciliationEngine(ManufacturingRepository repository) {
        this.repository = repository;
    }

    /**
     * 批量处理外部 MES/ERP 逐项回执流水并执行对账闭环核算
     */
    @Transactional
    public ReconciliationSummaryReport processBatchReceipts(String tenantId, BatchReceiptReconciliationRequest request) {
        String batchNo = request.getHandoffBatchNo();

        // 1. 定位下发批次包
        HandoffPackageEntity pkg = repository.findHandoffPackageByBatchNo(tenantId, batchNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到指定的制造下发批次: " + batchNo));

        // 2. 逐行幂等入库回执
        for (LineReceiptItemDto itemDto : request.getReceipts()) {
            ReceiptItemStatus status;
            try {
                status = ReceiptItemStatus.valueOf(itemDto.getItemStatus().toUpperCase());
            } catch (Exception e) {
                status = ReceiptItemStatus.PENDING;
            }

            long receiptId = Math.abs(UUID.randomUUID().getMostSignificantBits());
            LineItemReceiptEntity receipt = LineItemReceiptEntity.builder()
                    .receiptId(receiptId)
                    .tenantId(tenantId)
                    .packageId(pkg.getPackageId())
                    .lineItemNumber(itemDto.getLineItemNumber())
                    .materialNumber(itemDto.getMaterialNumber())
                    .externalReceiptNo(itemDto.getExternalReceiptNo())
                    .itemStatus(status)
                    .assignedStorageBin(itemDto.getAssignedStorageBin())
                    .discrepancyMessage(itemDto.getDiscrepancyMessage())
                    .receivedAt(Instant.now())
                    .build();

            repository.upsertLineItemReceipt(receipt);
        }

        // 3. 统计该批次当前的全局对账指标
        return generateReconciliationReportAndPromoteState(tenantId, pkg);
    }

    /**
     * 查询批次对账流水与汇总报告
     */
    @Transactional(readOnly = true)
    public ReconciliationSummaryReport getReconciliationSummary(String tenantId, String batchNo) {
        HandoffPackageEntity pkg = repository.findHandoffPackageByBatchNo(tenantId, batchNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到指定的制造下发批次: " + batchNo));

        List<LineItemReceiptEntity> receipts = repository.findReceiptsByPackageId(tenantId, pkg.getPackageId());

        List<ReconciliationLineItemDetail> details = new ArrayList<>();
        int acceptedCount = 0;
        int rejectedCount = 0;

        for (LineItemReceiptEntity r : receipts) {
            if (r.getItemStatus() == ReceiptItemStatus.ACCEPTED) {
                acceptedCount++;
            } else if (r.getItemStatus() == ReceiptItemStatus.REJECTED) {
                rejectedCount++;
            }

            details.add(new ReconciliationLineItemDetail(
                    r.getLineItemNumber(),
                    r.getMaterialNumber(),
                    r.getExternalReceiptNo(),
                    r.getItemStatus().name(),
                    r.getAssignedStorageBin(),
                    r.getDiscrepancyMessage(),
                    r.getReceivedAt()
            ));
        }

        boolean isFullyReconciled = pkg.getExecutionState() == HandoffExecutionState.RECONCILED_CONFIRMED;

        return new ReconciliationSummaryReport(
                pkg.getHandoffBatchNo(),
                pkg.getPackageId(),
                pkg.getExecutionState().name(),
                pkg.getTotalLineCount(),
                acceptedCount,
                rejectedCount,
                isFullyReconciled,
                pkg.getReconciledAt(),
                details
        );
    }

    /**
     * 自动统计并驱动下发批次状态机流转
     */
    private ReconciliationSummaryReport generateReconciliationReportAndPromoteState(String tenantId, HandoffPackageEntity pkg) {
        List<LineItemReceiptEntity> receipts = repository.findReceiptsByPackageId(tenantId, pkg.getPackageId());

        int acceptedCount = 0;
        int rejectedCount = 0;
        List<ReconciliationLineItemDetail> details = new ArrayList<>();

        for (LineItemReceiptEntity r : receipts) {
            if (r.getItemStatus() == ReceiptItemStatus.ACCEPTED) {
                acceptedCount++;
            } else if (r.getItemStatus() == ReceiptItemStatus.REJECTED) {
                rejectedCount++;
            }

            details.add(new ReconciliationLineItemDetail(
                    r.getLineItemNumber(),
                    r.getMaterialNumber(),
                    r.getExternalReceiptNo(),
                    r.getItemStatus().name(),
                    r.getAssignedStorageBin(),
                    r.getDiscrepancyMessage(),
                    r.getReceivedAt()
            ));
        }

        // 状态跃迁判定
        HandoffExecutionState nextState = pkg.getExecutionState();
        Instant reconciledAt = pkg.getReconciledAt();

        if (acceptedCount >= pkg.getTotalLineCount()) {
            // 全部明细收讫且成功
            nextState = HandoffExecutionState.RECONCILED_CONFIRMED;
            reconciledAt = Instant.now();
            log.info("【制造对账闭环】批次 {} 所有行 ({} 项) 均收到 ACCEPTED 回执，下发彻底完成确认！",
                    pkg.getHandoffBatchNo(), acceptedCount);
        } else if (rejectedCount > 0) {
            nextState = HandoffExecutionState.PARTIALLY_ACCEPTED;
            log.warn("【制造对账预警】批次 {} 存在 {} 项被驳回物料行，标记为 PARTIALLY_ACCEPTED 待纠偏！",
                    pkg.getHandoffBatchNo(), rejectedCount);
        }

        // 更新数据库
        repository.updateHandoffPackageReconciliationState(
                tenantId,
                pkg.getPackageId(),
                nextState,
                acceptedCount,
                rejectedCount,
                reconciledAt
        );

        boolean isFully = (nextState == HandoffExecutionState.RECONCILED_CONFIRMED);

        return new ReconciliationSummaryReport(
                pkg.getHandoffBatchNo(),
                pkg.getPackageId(),
                nextState.name(),
                pkg.getTotalLineCount(),
                acceptedCount,
                rejectedCount,
                isFully,
                reconciledAt,
                details
        );
    }
}
