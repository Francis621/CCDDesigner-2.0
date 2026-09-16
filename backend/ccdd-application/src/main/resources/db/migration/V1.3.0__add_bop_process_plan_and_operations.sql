-- =============================================================================
-- CCDDesigner 2.0 数据库增量迁移脚本 (P3 阶段: BOP 工艺路线编排与工序建模)
-- 脚本版本: V1.3.0
-- 对应规约: CCD-DEV-SPEC-2.0-D08 (M25 制造工艺与装配结构数据闭环转换)
-- =============================================================================

-- 1. 工艺路线主表 (Process Plans)
CREATE TABLE IF NOT EXISTS sys_process_plans (
    plan_id          BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    routing_code     VARCHAR(64) NOT NULL, -- 工艺路线编号 (如 ROUT-VMC850-SPINDLE)
    routing_name     VARCHAR(128) NOT NULL, -- 工艺路线名称 (如 五轴高速主轴装配工艺规程)
    mbom_revision_id BIGINT NOT NULL REFERENCES sys_manufacturing_bom_revisions(revision_id) ON DELETE CASCADE,
    plant_code       VARCHAR(32) NOT NULL, -- 实施制造工厂
    lifecycle_state  VARCHAR(32) NOT NULL DEFAULT 'DRAFT', -- DRAFT, IN_REVIEW, RELEASED, OBSOLETE
    created_by       VARCHAR(64) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_routing_code UNIQUE (tenant_id, routing_code, plant_code)
);

CREATE INDEX IF NOT EXISTS idx_process_plan_mbom ON sys_process_plans(tenant_id, mbom_revision_id);

-- 2. 工艺工序明细表 (Process Operations)
CREATE TABLE IF NOT EXISTS sys_process_operations (
    operation_id     BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    plan_id          BIGINT NOT NULL REFERENCES sys_process_plans(plan_id) ON DELETE CASCADE,
    sequence_number  INT NOT NULL,          -- 工序序号 (如 10, 20, 30, 40)
    operation_code   VARCHAR(64) NOT NULL,  -- 工序代码 (如 OP10_CLEAN_SCRAPE)
    operation_name   VARCHAR(128) NOT NULL, -- 工序名称
    work_center_code VARCHAR(64) NOT NULL,  -- 工作中心/工位代码 (如 WC_SPINDLE_PREP)
    setup_time_mins  NUMERIC(10, 2) NOT NULL DEFAULT 0.0, -- 准备工时 (分钟)
    run_time_mins    NUMERIC(10, 2) NOT NULL DEFAULT 0.0, -- 单件作业工时 (分钟)
    tooling_fixtures VARCHAR(256) NULL,     -- 专用工艺装备/夹具/检具 (如 00级大理石平板、千分表、动平衡仪)
    inspection_requirement TEXT NULL,       -- 质量控制要点与检验要求
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_plan_op_seq UNIQUE (tenant_id, plan_id, sequence_number)
);

CREATE INDEX IF NOT EXISTS idx_op_plan_id ON sys_process_operations(tenant_id, plan_id);
