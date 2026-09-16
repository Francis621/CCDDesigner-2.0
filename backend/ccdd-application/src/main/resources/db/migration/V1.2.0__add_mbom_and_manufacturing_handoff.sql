-- =============================================================================
-- CCDDesigner 2.0 数据库增量迁移脚本 (P3 阶段: 制造工程、工艺与下发回执)
-- 脚本版本: V1.2.0
-- 对应规约: CCD-DEV-SPEC-2.0-D08 (M25, M26, M27)
-- =============================================================================

-- 1. 制造 MBOM 主表与修订版
CREATE TABLE IF NOT EXISTS sys_manufacturing_boms (
    mbom_id          BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    mbom_code        VARCHAR(64) NOT NULL,
    plant_code       VARCHAR(32) NOT NULL, -- 制造工厂编号 (如 PLANT_01)
    product_number   VARCHAR(64) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_mbom_code UNIQUE (tenant_id, mbom_code, plant_code)
);

CREATE TABLE IF NOT EXISTS sys_manufacturing_bom_revisions (
    revision_id      BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    mbom_id          BIGINT NOT NULL REFERENCES sys_manufacturing_boms(mbom_id) ON DELETE CASCADE,
    revision_version VARCHAR(32) NOT NULL,
    source_ebom_rev_id BIGINT NOT NULL,    -- 追溯绑定的设计来源 EBOM 版本
    lifecycle_state  VARCHAR(32) NOT NULL DEFAULT 'DRAFT', -- DRAFT, IN_REVIEW, RELEASED, OBSOLETE
    is_balance_verified BOOLEAN NOT NULL DEFAULT FALSE,    -- 100% 消耗平衡校验标志
    balance_report_json JSONB NULL,       -- 消耗平衡明细残差报告
    published_by     VARCHAR(64) NULL,
    published_at     TIMESTAMP WITH TIME ZONE NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_mbom_rev UNIQUE (tenant_id, mbom_id, revision_version)
);

-- 2. EBOM/MBOM 转换映射与消耗平衡矩阵表
CREATE TABLE IF NOT EXISTS sys_bom_transformation_maps (
    map_id           BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    mbom_revision_id BIGINT NOT NULL REFERENCES sys_manufacturing_bom_revisions(revision_id) ON DELETE CASCADE,
    ebom_line_id     BIGINT NULL,          -- 来源 EBOM 行 ID (制造新增辅料时可为空)
    source_part_number VARCHAR(64) NULL,
    mbom_line_number VARCHAR(32) NOT NULL,
    target_part_number VARCHAR(64) NOT NULL,
    transform_type   VARCHAR(32) NOT NULL, -- DIRECT_1_TO_1, SPLIT_1_TO_N, PHANTOM_RESTRUCTURE, MANUFACTURING_ADDED
    consumed_quantity NUMERIC(18, 4) NOT NULL,
    unit_of_measure  VARCHAR(16) NOT NULL DEFAULT 'EA',
    operation_sequence INT NULL,           -- 指派消耗的工艺路线工序号 (如 0010)
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trans_map_mbom ON sys_bom_transformation_maps(tenant_id, mbom_revision_id);

-- 3. 制造下发批次包主表 (Handoff Packages)
CREATE TABLE IF NOT EXISTS sys_handoff_packages (
    package_id       BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    handoff_batch_no VARCHAR(64) NOT NULL,
    mbom_revision_id BIGINT NOT NULL REFERENCES sys_manufacturing_bom_revisions(revision_id),
    target_system    VARCHAR(32) NOT NULL, -- ERP, MES_PLANT_01, WMS
    package_digest_sha256 VARCHAR(64) NOT NULL,
    execution_state  VARCHAR(32) NOT NULL DEFAULT 'DRAFT', -- DRAFT, READY_TO_SEND, TRANSMITTING, ACKNOWLEDGED, RECONCILED_CONFIRMED, REJECTED
    total_line_count INT NOT NULL,
    accepted_line_count INT NOT NULL DEFAULT 0,
    rejected_line_count INT NOT NULL DEFAULT 0,
    created_by       VARCHAR(64) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reconciled_at    TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT uk_tenant_handoff_batch UNIQUE (tenant_id, handoff_batch_no)
);

-- 4. 逐项业务回执与对账明细表 (Line-item Receipts)
CREATE TABLE IF NOT EXISTS sys_line_item_receipts (
    receipt_id       BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    package_id       BIGINT NOT NULL REFERENCES sys_handoff_packages(package_id) ON DELETE CASCADE,
    line_item_number VARCHAR(32) NOT NULL,
    material_number  VARCHAR(64) NOT NULL,
    external_receipt_no VARCHAR(64) NOT NULL, -- 外部 MES 系统返回的唯一收讫流水凭证号
    item_status      VARCHAR(32) NOT NULL,    -- ACCEPTED, REJECTED, PENDING
    assigned_storage_bin VARCHAR(64) NULL,
    discrepancy_message TEXT NULL,
    received_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_receipt_item_unique UNIQUE (tenant_id, package_id, line_item_number, external_receipt_no)
);

CREATE INDEX IF NOT EXISTS idx_receipt_pkg ON sys_line_item_receipts(tenant_id, package_id);
