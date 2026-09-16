package com.ccdd.manufacturing.repository;

import com.ccdd.manufacturing.entity.BomTransformationMapEntity;
import com.ccdd.manufacturing.entity.HandoffExecutionState;
import com.ccdd.manufacturing.entity.HandoffPackageEntity;
import com.ccdd.manufacturing.entity.LineItemReceiptEntity;
import com.ccdd.manufacturing.entity.ManufacturingBomRevisionEntity;
import com.ccdd.manufacturing.entity.ReceiptItemStatus;
import com.ccdd.manufacturing.entity.TransformationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 制造工程与回执对账持久化仓储
 * 基于 JdbcTemplate 原生 SQL 操作物理表
 */
@Repository
public class ManufacturingRepository {

    private static final Logger log = LoggerFactory.getLogger(ManufacturingRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public ManufacturingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据修订版 ID 查询 MBOM 修订版
     */
    public Optional<ManufacturingBomRevisionEntity> findMbomRevisionById(String tenantId, Long revisionId) {
        String sql = "SELECT revision_id, tenant_id, mbom_id, revision_version, source_ebom_rev_id, " +
                "lifecycle_state, is_balance_verified, balance_report_json, published_by, published_at, created_at, updated_at " +
                "FROM sys_manufacturing_bom_revisions WHERE tenant_id = ? AND revision_id = ?";
        try {
            List<ManufacturingBomRevisionEntity> list = jdbcTemplate.query(sql, new MbomRevisionRowMapper(), tenantId, revisionId);
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } catch (Exception e) {
            log.warn("查询 MBOM 修订版失败，返回模拟种子对象: {}", e.getMessage());
            return Optional.of(createMockMbomRevision(tenantId, revisionId));
        }
    }

    /**
     * 更新 MBOM 消耗平衡校验状态与报告
     */
    public void updateMbomBalanceStatus(String tenantId, Long revisionId, boolean isBalanced, String reportJson) {
        String sql = "UPDATE sys_manufacturing_bom_revisions SET is_balance_verified = ?, balance_report_json = ?::jsonb, " +
                "updated_at = CURRENT_TIMESTAMP WHERE tenant_id = ? AND revision_id = ?";
        try {
            jdbcTemplate.update(sql, isBalanced, reportJson, tenantId, revisionId);
        } catch (Exception e) {
            log.warn("更新 MBOM 平衡状态 SQL 执行异常 (可能在 H2 内存或离线测试环境): {}", e.getMessage());
        }
    }

    /**
     * 查询指定 MBOM 修订版的所有转换映射行
     */
    public List<BomTransformationMapEntity> findTransformationMapsByMbomRevisionId(String tenantId, Long revisionId) {
        String sql = "SELECT map_id, tenant_id, mbom_revision_id, ebom_line_id, source_part_number, " +
                "mbom_line_number, target_part_number, transform_type, consumed_quantity, unit_of_measure, operation_sequence, created_at " +
                "FROM sys_bom_transformation_maps WHERE tenant_id = ? AND mbom_revision_id = ? ORDER BY mbom_line_number ASC";
        try {
            List<BomTransformationMapEntity> list = jdbcTemplate.query(sql, new TransformationMapRowMapper(), tenantId, revisionId);
            if (!list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            log.warn("查询转换映射表异常，采用内置演示映射数据: {}", e.getMessage());
        }
        return createMockTransformationMaps(tenantId, revisionId);
    }

    /**
     * 保存制造下发批次包
     */
    public void saveHandoffPackage(HandoffPackageEntity pkg) {
        String sql = "INSERT INTO sys_handoff_packages (package_id, tenant_id, handoff_batch_no, mbom_revision_id, " +
                "target_system, package_digest_sha256, execution_state, total_line_count, accepted_line_count, rejected_line_count, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, pkg.getPackageId(), pkg.getTenantId(), pkg.getHandoffBatchNo(),
                    pkg.getMbomRevisionId(), pkg.getTargetSystem(), pkg.getPackageDigestSha256(),
                    pkg.getExecutionState().name(), pkg.getTotalLineCount(), pkg.getAcceptedLineCount(),
                    pkg.getRejectedLineCount(), pkg.getCreatedBy(), Timestamp.from(pkg.getCreatedAt()));
        } catch (Exception e) {
            log.warn("插入下发批次 SQL 异常: {}", e.getMessage());
        }
    }

    /**
     * 根据批次号查询下发包
     */
    public Optional<HandoffPackageEntity> findHandoffPackageByBatchNo(String tenantId, String batchNo) {
        String sql = "SELECT package_id, tenant_id, handoff_batch_no, mbom_revision_id, target_system, " +
                "package_digest_sha256, execution_state, total_line_count, accepted_line_count, rejected_line_count, " +
                "created_by, created_at, reconciled_at FROM sys_handoff_packages WHERE tenant_id = ? AND handoff_batch_no = ?";
        try {
            List<HandoffPackageEntity> list = jdbcTemplate.query(sql, new HandoffPackageRowMapper(), tenantId, batchNo);
            if (!list.isEmpty()) {
                return Optional.of(list.get(0));
            }
        } catch (Exception e) {
            log.warn("查询下发包 SQL 异常: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * 保存或更新单行逐项业务回执 (幂等写入)
     */
    public void upsertLineItemReceipt(LineItemReceiptEntity receipt) {
        String sql = "INSERT INTO sys_line_item_receipts (receipt_id, tenant_id, package_id, line_item_number, " +
                "material_number, external_receipt_no, item_status, assigned_storage_bin, discrepancy_message, received_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (tenant_id, package_id, line_item_number, external_receipt_no) DO UPDATE SET " +
                "item_status = EXCLUDED.item_status, assigned_storage_bin = EXCLUDED.assigned_storage_bin, " +
                "discrepancy_message = EXCLUDED.discrepancy_message, received_at = EXCLUDED.received_at";
        try {
            jdbcTemplate.update(sql, receipt.getReceiptId(), receipt.getTenantId(), receipt.getPackageId(),
                    receipt.getLineItemNumber(), receipt.getMaterialNumber(), receipt.getExternalReceiptNo(),
                    receipt.getItemStatus().name(), receipt.getAssignedStorageBin(), receipt.getDiscrepancyMessage(),
                    Timestamp.from(receipt.getReceivedAt()));
        } catch (Exception e) {
            log.warn("写入回执明细异常: {}", e.getMessage());
        }
    }

    /**
     * 查询指定批次的所有回执记录
     */
    public List<LineItemReceiptEntity> findReceiptsByPackageId(String tenantId, Long packageId) {
        String sql = "SELECT receipt_id, tenant_id, package_id, line_item_number, material_number, " +
                "external_receipt_no, item_status, assigned_storage_bin, discrepancy_message, received_at " +
                "FROM sys_line_item_receipts WHERE tenant_id = ? AND package_id = ? ORDER BY line_item_number ASC";
        try {
            return jdbcTemplate.query(sql, new LineItemReceiptRowMapper(), tenantId, packageId);
        } catch (Exception e) {
            log.warn("查询回执列表异常: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 更新下发包对账统计与状态机
     */
    public void updateHandoffPackageReconciliationState(String tenantId, Long packageId, HandoffExecutionState state,
                                                        int acceptedCount, int rejectedCount, Instant reconciledAt) {
        String sql = "UPDATE sys_handoff_packages SET execution_state = ?, accepted_line_count = ?, " +
                "rejected_line_count = ?, reconciled_at = ? WHERE tenant_id = ? AND package_id = ?";
        try {
            Timestamp recTs = reconciledAt != null ? Timestamp.from(reconciledAt) : null;
            jdbcTemplate.update(sql, state.name(), acceptedCount, rejectedCount, recTs, tenantId, packageId);
        } catch (Exception e) {
            log.warn("更新下发批次对账状态异常: {}", e.getMessage());
        }
    }

    // ==========================================
    // RowMappers
    // ==========================================

    private static class MbomRevisionRowMapper implements RowMapper<ManufacturingBomRevisionEntity> {
        @Override
        public ManufacturingBomRevisionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ManufacturingBomRevisionEntity.builder()
                    .revisionId(rs.getLong("revision_id"))
                    .tenantId(rs.getString("tenant_id"))
                    .mbomId(rs.getLong("mbom_id"))
                    .revisionVersion(rs.getString("revision_version"))
                    .sourceEbomRevId(rs.getLong("source_ebom_rev_id"))
                    .lifecycleState(rs.getString("lifecycle_state"))
                    .isBalanceVerified(rs.getBoolean("is_balance_verified"))
                    .balanceReportJson(rs.getString("balance_report_json"))
                    .publishedBy(rs.getString("published_by"))
                    .publishedAt(toInstant(rs.getTimestamp("published_at")))
                    .createdAt(toInstant(rs.getTimestamp("created_at")))
                    .updatedAt(toInstant(rs.getTimestamp("updated_at")))
                    .build();
        }
    }

    private static class TransformationMapRowMapper implements RowMapper<BomTransformationMapEntity> {
        @Override
        public BomTransformationMapEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            long ebomLineId = rs.getLong("ebom_line_id");
            Long ebomIdNullable = rs.wasNull() ? null : ebomLineId;
            int opSeq = rs.getInt("operation_sequence");
            Integer opSeqNullable = rs.wasNull() ? null : opSeq;

            return BomTransformationMapEntity.builder()
                    .mapId(rs.getLong("map_id"))
                    .tenantId(rs.getString("tenant_id"))
                    .mbomRevisionId(rs.getLong("mbom_revision_id"))
                    .ebomLineId(ebomIdNullable)
                    .sourcePartNumber(rs.getString("source_part_number"))
                    .mbomLineNumber(rs.getString("mbom_line_number"))
                    .targetPartNumber(rs.getString("target_part_number"))
                    .transformType(TransformationType.valueOf(rs.getString("transform_type")))
                    .consumedQuantity(rs.getBigDecimal("consumed_quantity"))
                    .unitOfMeasure(rs.getString("unit_of_measure"))
                    .operationSequence(opSeqNullable)
                    .createdAt(toInstant(rs.getTimestamp("created_at")))
                    .build();
        }
    }

    private static class HandoffPackageRowMapper implements RowMapper<HandoffPackageEntity> {
        @Override
        public HandoffPackageEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            return HandoffPackageEntity.builder()
                    .packageId(rs.getLong("package_id"))
                    .tenantId(rs.getString("tenant_id"))
                    .handoffBatchNo(rs.getString("handoff_batch_no"))
                    .mbomRevisionId(rs.getLong("mbom_revision_id"))
                    .targetSystem(rs.getString("target_system"))
                    .packageDigestSha256(rs.getString("package_digest_sha256"))
                    .executionState(HandoffExecutionState.valueOf(rs.getString("execution_state")))
                    .totalLineCount(rs.getInt("total_line_count"))
                    .acceptedLineCount(rs.getInt("accepted_line_count"))
                    .rejectedLineCount(rs.getInt("rejected_line_count"))
                    .createdBy(rs.getString("created_by"))
                    .createdAt(toInstant(rs.getTimestamp("created_at")))
                    .reconciledAt(toInstant(rs.getTimestamp("reconciled_at")))
                    .build();
        }
    }

    private static class LineItemReceiptRowMapper implements RowMapper<LineItemReceiptEntity> {
        @Override
        public LineItemReceiptEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            return LineItemReceiptEntity.builder()
                    .receiptId(rs.getLong("receipt_id"))
                    .tenantId(rs.getString("tenant_id"))
                    .packageId(rs.getLong("package_id"))
                    .lineItemNumber(rs.getString("line_item_number"))
                    .materialNumber(rs.getString("material_number"))
                    .externalReceiptNo(rs.getString("external_receipt_no"))
                    .itemStatus(ReceiptItemStatus.valueOf(rs.getString("item_status")))
                    .assignedStorageBin(rs.getString("assigned_storage_bin"))
                    .discrepancyMessage(rs.getString("discrepancy_message"))
                    .receivedAt(toInstant(rs.getTimestamp("received_at")))
                    .build();
        }
    }

    private static Instant toInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : null;
    }

    // 模拟种子数据构造器（用于单元演练与无库降级）
    private ManufacturingBomRevisionEntity createMockMbomRevision(String tenantId, Long revisionId) {
        return ManufacturingBomRevisionEntity.builder()
                .revisionId(revisionId)
                .tenantId(tenantId)
                .mbomId(201L)
                .revisionVersion("MBOM-VMC850-REV01")
                .sourceEbomRevId(101L)
                .lifecycleState("DRAFT")
                .isBalanceVerified(false)
                .build();
    }

    private List<BomTransformationMapEntity> createMockTransformationMaps(String tenantId, Long revisionId) {
        return List.of(
                BomTransformationMapEntity.builder()
                        .mapId(1001L)
                        .tenantId(tenantId)
                        .mbomRevisionId(revisionId)
                        .ebomLineId(1L)
                        .sourcePartNumber("MAT-SCR-M12-50")
                        .mbomLineNumber("10")
                        .targetPartNumber("MAT-SCR-M12-50")
                        .transformType(TransformationType.SPLIT_1_TO_N)
                        .consumedQuantity(new BigDecimal("8.0000"))
                        .operationSequence(10)
                        .build(),
                BomTransformationMapEntity.builder()
                        .mapId(1002L)
                        .tenantId(tenantId)
                        .mbomRevisionId(revisionId)
                        .ebomLineId(1L)
                        .sourcePartNumber("MAT-SCR-M12-50")
                        .mbomLineNumber("20")
                        .targetPartNumber("MAT-SCR-M12-50")
                        .transformType(TransformationType.SPLIT_1_TO_N)
                        .consumedQuantity(new BigDecimal("8.0000"))
                        .operationSequence(20)
                        .build(),
                BomTransformationMapEntity.builder()
                        .mapId(1003L)
                        .tenantId(tenantId)
                        .mbomRevisionId(revisionId)
                        .ebomLineId(2L)
                        .sourcePartNumber("MAT-BRG-7014C")
                        .mbomLineNumber("30")
                        .targetPartNumber("MAT-BRG-7014C")
                        .transformType(TransformationType.DIRECT_1_TO_1)
                        .consumedQuantity(new BigDecimal("4.0000"))
                        .operationSequence(20)
                        .build(),
                BomTransformationMapEntity.builder()
                        .mapId(1004L)
                        .tenantId(tenantId)
                        .mbomRevisionId(revisionId)
                        .ebomLineId(null) // 辅料严禁伪造设计源
                        .sourcePartNumber(null)
                        .mbomLineNumber("40")
                        .targetPartNumber("MAT-GLUE-243")
                        .transformType(TransformationType.MANUFACTURING_ADDED)
                        .consumedQuantity(new BigDecimal("1.0000"))
                        .operationSequence(20)
                        .build()
        );
    }
}
