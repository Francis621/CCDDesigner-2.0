package com.ccdd.manufacturing;

import com.ccdd.manufacturing.dto.BatchReceiptReconciliationRequest;
import com.ccdd.manufacturing.dto.BatchReceiptReconciliationRequest.LineReceiptItemDto;
import com.ccdd.manufacturing.dto.ConsumptionBalanceReport;
import com.ccdd.manufacturing.dto.ReconciliationSummaryReport;
import com.ccdd.manufacturing.entity.HandoffExecutionState;
import com.ccdd.manufacturing.entity.HandoffPackageEntity;
import com.ccdd.manufacturing.entity.LineItemReceiptEntity;
import com.ccdd.manufacturing.entity.ReceiptItemStatus;
import com.ccdd.manufacturing.repository.ManufacturingRepository;
import com.ccdd.manufacturing.service.ConsumptionBalanceService;
import com.ccdd.manufacturing.service.ReceiptReconciliationEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 制造工程消耗平衡校验与逐项回执对账引擎单元测试
 * 纯原生 Java 测试 (无 Mockito 动态字节码依赖，与 JDK 26 完全兼容)
 * 对应规约: CCD-DEV-SPEC-2.0-D08 (AT-27, AT-28)
 */
public class ConsumptionBalanceAndReconciliationTest {

    private InMemoryManufacturingRepository inMemoryRepository;
    private ConsumptionBalanceService balanceService;
    private ReceiptReconciliationEngine reconciliationEngine;

    @BeforeEach
    public void setUp() {
        inMemoryRepository = new InMemoryManufacturingRepository();
        ObjectMapper objectMapper = new ObjectMapper();
        balanceService = new ConsumptionBalanceService(inMemoryRepository, objectMapper);
        reconciliationEngine = new ReceiptReconciliationEngine(inMemoryRepository);
    }

    @Test
    @DisplayName("AT-27: 验证 EBOM/MBOM 转换物料消耗 100% 守恒平衡算法与工艺辅料合规性")
    public void testEbomMbomBalanceVerificationSuccess() {
        String tenantId = "TENANT_TEST";
        Long mbomRevId = 201L;

        ConsumptionBalanceReport report = balanceService.verifyBalance(tenantId, mbomRevId);

        assertNotNull(report);
        assertTrue(report.getIsBalanced(), "物料消耗守恒应当 100% 通过校验");
        assertEquals(0, report.getUnderConsumedCount(), "欠消耗数量应为 0");
        assertEquals(0, report.getOverConsumedCount(), "过消耗数量应为 0");
        assertEquals(0, report.getIllegalSourceCount(), "非法来源数量应为 0");
        assertTrue(report.getItemDetails().size() >= 3, "应包含螺栓、轴承与工艺辅料明细");
    }

    @Test
    @DisplayName("AT-28: 验证逐项业务流水回执全部 ACCEPTED 时下发批次自动晋升为 RECONCILED_CONFIRMED")
    public void testReceiptReconciliationFullConfirmed() {
        String tenantId = "TENANT_TEST";
        String batchNo = "DISPATCH-20260916-TEST01";
        Long packageId = 5001L;

        HandoffPackageEntity pkg = HandoffPackageEntity.builder()
                .packageId(packageId)
                .tenantId(tenantId)
                .handoffBatchNo(batchNo)
                .mbomRevisionId(201L)
                .targetSystem("MES_PLANT_01")
                .executionState(HandoffExecutionState.ACKNOWLEDGED)
                .totalLineCount(3)
                .acceptedLineCount(0)
                .rejectedLineCount(0)
                .createdAt(Instant.now())
                .build();

        inMemoryRepository.saveHandoffPackage(pkg);

        BatchReceiptReconciliationRequest request = new BatchReceiptReconciliationRequest(
                batchNo,
                "MES-SESSION-8899",
                List.of(
                        new LineReceiptItemDto("10", "MAT-SCR-M12-50", "REC-001", "ACCEPTED", "BIN-A-01", null),
                        new LineReceiptItemDto("20", "MAT-BRG-7014C", "REC-002", "ACCEPTED", "BIN-A-02", null),
                        new LineReceiptItemDto("30", "MAT-GLUE-243", "REC-003", "ACCEPTED", "BIN-B-01", null)
                )
        );

        ReconciliationSummaryReport summary = reconciliationEngine.processBatchReceipts(tenantId, request);

        assertNotNull(summary);
        assertEquals(3, summary.getAcceptedLines());
        assertEquals(0, summary.getRejectedLines());
        assertTrue(summary.getIsFullyReconciled(), "所有行收讫通过应达成完全对账闭环");
        assertEquals("RECONCILED_CONFIRMED", summary.getExecutionState());
    }

    @Test
    @DisplayName("AT-28-EX: 验证存在 REJECTED 驳回行时下发批次标记为 PARTIALLY_ACCEPTED 待纠偏")
    public void testReceiptReconciliationPartiallyAccepted() {
        String tenantId = "TENANT_TEST";
        String batchNo = "DISPATCH-20260916-TEST02";
        Long packageId = 5002L;

        HandoffPackageEntity pkg = HandoffPackageEntity.builder()
                .packageId(packageId)
                .tenantId(tenantId)
                .handoffBatchNo(batchNo)
                .mbomRevisionId(201L)
                .targetSystem("MES_PLANT_01")
                .executionState(HandoffExecutionState.ACKNOWLEDGED)
                .totalLineCount(2)
                .acceptedLineCount(0)
                .rejectedLineCount(0)
                .createdAt(Instant.now())
                .build();

        inMemoryRepository.saveHandoffPackage(pkg);

        BatchReceiptReconciliationRequest request = new BatchReceiptReconciliationRequest(
                batchNo,
                "MES-SESSION-9900",
                List.of(
                        new LineReceiptItemDto("10", "MAT-SCR-M12-50", "REC-101", "ACCEPTED", "BIN-A-01", null),
                        new LineReceiptItemDto("20", "MAT-BRG-7014C", "REC-102", "REJECTED", null, "车间该主轴轴承库位已锁死")
                )
        );

        ReconciliationSummaryReport summary = reconciliationEngine.processBatchReceipts(tenantId, request);

        assertNotNull(summary);
        assertEquals(1, summary.getAcceptedLines());
        assertEquals(1, summary.getRejectedLines());
        assertFalse(summary.getIsFullyReconciled(), "存在驳回行时不应判定为完全对账确认");
        assertEquals("PARTIALLY_ACCEPTED", summary.getExecutionState());
    }

    /**
     * 纯原生 Java 内存测试桩 (零依赖任何 Mock 框架)
     */
    private static class InMemoryManufacturingRepository extends ManufacturingRepository {

        private final Map<String, HandoffPackageEntity> packages = new HashMap<>();
        private final Map<Long, List<LineItemReceiptEntity>> receipts = new HashMap<>();

        public InMemoryManufacturingRepository() {
            super(null);
        }

        @Override
        public void saveHandoffPackage(HandoffPackageEntity pkg) {
            packages.put(pkg.getHandoffBatchNo(), pkg);
        }

        @Override
        public Optional<HandoffPackageEntity> findHandoffPackageByBatchNo(String tenantId, String batchNo) {
            return Optional.ofNullable(packages.get(batchNo));
        }

        @Override
        public void upsertLineItemReceipt(LineItemReceiptEntity receipt) {
            receipts.computeIfAbsent(receipt.getPackageId(), k -> new ArrayList<>());
            List<LineItemReceiptEntity> list = receipts.get(receipt.getPackageId());
            list.removeIf(r -> r.getLineItemNumber().equals(receipt.getLineItemNumber()));
            list.add(receipt);
        }

        @Override
        public List<LineItemReceiptEntity> findReceiptsByPackageId(String tenantId, Long packageId) {
            return receipts.getOrDefault(packageId, new ArrayList<>());
        }

        @Override
        public void updateHandoffPackageReconciliationState(String tenantId, Long packageId,
                                                            HandoffExecutionState state,
                                                            int acceptedCount, int rejectedCount,
                                                            Instant reconciledAt) {
            for (HandoffPackageEntity p : packages.values()) {
                if (p.getPackageId().equals(packageId)) {
                    p.setExecutionState(state);
                    p.setAcceptedLineCount(acceptedCount);
                    p.setRejectedLineCount(rejectedCount);
                    p.setReconciledAt(reconciledAt);
                    break;
                }
            }
        }
    }
}
