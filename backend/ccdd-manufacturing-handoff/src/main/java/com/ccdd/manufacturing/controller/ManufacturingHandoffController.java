package com.ccdd.manufacturing.controller;

import com.ccdd.common.api.Result;
import com.ccdd.manufacturing.dto.BatchReceiptReconciliationRequest;
import com.ccdd.manufacturing.dto.ConsumptionBalanceReport;
import com.ccdd.manufacturing.dto.CreateHandoffPackageRequest;
import com.ccdd.manufacturing.dto.ReconciliationSummaryReport;
import com.ccdd.manufacturing.entity.HandoffPackageEntity;
import com.ccdd.manufacturing.service.ConsumptionBalanceService;
import com.ccdd.manufacturing.service.ManufacturingHandoffService;
import com.ccdd.manufacturing.service.ReceiptReconciliationEngine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 制造工程与逐项对账 REST 控制器
 * 遵循《CCD-DEV-SPEC-2.0-D08 专项规格包》接口标准
 */
@RestController
@RequestMapping("/api/v1/manufacturing")
public class ManufacturingHandoffController {

    private final ConsumptionBalanceService balanceService;
    private final ManufacturingHandoffService handoffService;
    private final ReceiptReconciliationEngine reconciliationEngine;

    public ManufacturingHandoffController(ConsumptionBalanceService balanceService,
                                          ManufacturingHandoffService handoffService,
                                          ReceiptReconciliationEngine reconciliationEngine) {
        this.balanceService = balanceService;
        this.handoffService = handoffService;
        this.reconciliationEngine = reconciliationEngine;
    }

    /**
     * 1. 执行 EBOM/MBOM 100% 消耗平衡守恒残差校验
     */
    @PostMapping("/mbom/{mbomRevisionId}/verify-balance")
    public Result<ConsumptionBalanceReport> verifyBalance(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "TENANT_DEFAULT") String tenantId,
            @PathVariable("mbomRevisionId") Long mbomRevisionId) {
        ConsumptionBalanceReport report = balanceService.verifyBalance(tenantId, mbomRevisionId);
        return Result.success(report);
    }

    /**
     * 2. 创建并发布制造下发批次包 (带 MRR 平衡强校验与 SHA-256 全包签名)
     */
    @PostMapping("/handoff/packages")
    public Result<HandoffPackageEntity> createHandoffPackage(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "TENANT_DEFAULT") String tenantId,
            @RequestBody CreateHandoffPackageRequest request) {
        HandoffPackageEntity pkg = handoffService.createAndDispatchPackage(tenantId, request);
        return Result.success(pkg);
    }

    /**
     * 3. MES/ERP 逐项业务回执批量异步对账收讫
     */
    @PostMapping("/handoff/receipts/batch")
    public Result<ReconciliationSummaryReport> reconcileBatchReceipts(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "TENANT_DEFAULT") String tenantId,
            @RequestBody BatchReceiptReconciliationRequest request) {
        ReconciliationSummaryReport report = reconciliationEngine.processBatchReceipts(tenantId, request);
        return Result.success(report);
    }

    /**
     * 4. 查询制造下发批次对账流水与闭环报告
     */
    @GetMapping("/handoff/packages/{batchNo}/reconciliation")
    public Result<ReconciliationSummaryReport> getReconciliationSummary(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "TENANT_DEFAULT") String tenantId,
            @PathVariable("batchNo") String batchNo) {
        ReconciliationSummaryReport report = reconciliationEngine.getReconciliationSummary(tenantId, batchNo);
        return Result.success(report);
    }
}
