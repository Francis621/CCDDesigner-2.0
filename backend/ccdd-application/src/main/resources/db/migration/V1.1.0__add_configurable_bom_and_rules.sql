-- =============================================================================
-- CCDDesigner 2.0 数据库增量迁移脚本 (P2 阶段: 150% BOM 与配置规则引擎)
-- 脚本版本: V1.1.0
-- 对应规约: CCD-DEV-SPEC-2.0-D05 (M12, M14, M15)
-- =============================================================================

-- 1. 特征与选项字典表 (Features & Options)
CREATE TABLE IF NOT EXISTS sys_feature_definitions (
    feature_id       BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    feature_code     VARCHAR(64) NOT NULL,
    feature_name     VARCHAR(128) NOT NULL,
    value_type       VARCHAR(32) NOT NULL, -- SINGLE_SELECT, MULTI_SELECT, BOOLEAN, INTEGER, DECIMAL
    is_mandatory     BOOLEAN NOT NULL DEFAULT TRUE,
    default_value    VARCHAR(128) NULL,
    domain_category  VARCHAR(32) NOT NULL DEFAULT 'MECHANICAL',
    description      TEXT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_feature_code UNIQUE (tenant_id, feature_code)
);

CREATE TABLE IF NOT EXISTS sys_feature_options (
    option_id        BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    feature_id       BIGINT NOT NULL REFERENCES sys_feature_definitions(feature_id) ON DELETE CASCADE,
    option_code      VARCHAR(64) NOT NULL,
    option_name      VARCHAR(128) NOT NULL,
    numeric_value    NUMERIC(18, 4) NULL,
    attributes_json  JSONB NOT NULL DEFAULT '{}'::jsonb,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_feature_option UNIQUE (tenant_id, feature_id, option_code)
);

-- 2. 150% 可配置超级结构主表与修订版 (150% Super BOM)
CREATE TABLE IF NOT EXISTS sys_configurable_structures (
    structure_id     BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    structure_code   VARCHAR(64) NOT NULL,
    structure_name   VARCHAR(128) NOT NULL,
    platform_id      VARCHAR(64) NOT NULL, -- 归属机床产品平台 (如 PLATFORM-VMC1000)
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_structure_code UNIQUE (tenant_id, structure_code)
);

CREATE TABLE IF NOT EXISTS sys_configurable_structure_revisions (
    revision_id      BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    structure_id     BIGINT NOT NULL REFERENCES sys_configurable_structures(structure_id) ON DELETE CASCADE,
    revision_version VARCHAR(32) NOT NULL, -- 'A.1', 'B.0'
    lifecycle_state  VARCHAR(32) NOT NULL DEFAULT 'DRAFT', -- DRAFT, RELEASED, OBSOLETE
    rule_set_rev_id  BIGINT NULL,          -- 绑定的全局规则集版本
    published_by     VARCHAR(64) NULL,
    published_at     TIMESTAMP WITH TIME ZONE NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_structure_rev UNIQUE (tenant_id, structure_id, revision_version)
);

-- 150% BOM 行明细 (包含槽位挂载与 DSL 表达式)
CREATE TABLE IF NOT EXISTS sys_configurable_bom_lines (
    line_id          BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    revision_id      BIGINT NOT NULL REFERENCES sys_configurable_structure_revisions(revision_id) ON DELETE CASCADE,
    parent_line_id   BIGINT NULL REFERENCES sys_configurable_bom_lines(line_id) ON DELETE CASCADE,
    line_number      INT NOT NULL,
    slot_id          VARCHAR(64) NOT NULL,        -- 槽位编号 (如 SLOT_MAIN_SPINDLE)
    slot_name        VARCHAR(128) NOT NULL,
    cardinality      VARCHAR(16) NOT NULL DEFAULT '1..1', -- '1..1', '0..1', '1..N'
    child_part_rev_id VARCHAR(128) NOT NULL,     -- 挂载的零部件/变体修订版标识
    child_part_number VARCHAR(64) NOT NULL,
    child_part_name  VARCHAR(128) NOT NULL,
    selection_rule   TEXT NOT NULL,               -- 选用条件 DSL (如 $SPINDLE_TYPE == "BT40")
    quantity_formula TEXT NOT NULL DEFAULT '1',   -- 数量公式表达式
    is_phantom       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cfg_bom_lines_rev ON sys_configurable_bom_lines(tenant_id, revision_id, parent_line_id);

-- 3. 全局配置规则集版本表 (RuleSet Revisions)
CREATE TABLE IF NOT EXISTS sys_rule_set_revisions (
    rule_set_rev_id  BIGINT PRIMARY KEY,
    tenant_id        VARCHAR(64) NOT NULL,
    rule_set_code    VARCHAR(64) NOT NULL,
    rule_set_version VARCHAR(32) NOT NULL,
    lifecycle_state  VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    rules_dsl_json   JSONB NOT NULL,              -- 包含 REQUIRES, MUTEX, NUMERIC 数组
    validation_passed BOOLEAN NOT NULL DEFAULT FALSE,
    validation_error_count INT NOT NULL DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_ruleset_ver UNIQUE (tenant_id, rule_set_code, rule_set_version)
);

-- 4. 确定性配置求解结果固化快照表 (Configuration Results)
CREATE TABLE IF NOT EXISTS sys_configuration_results (
    result_id            BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    order_id             VARCHAR(64) NOT NULL,       -- 对应订单产品定义编号 (如 ORD-2026-VMC1000-001)
    structure_revision_id BIGINT NOT NULL REFERENCES sys_configurable_structure_revisions(revision_id),
    rule_set_rev_id      BIGINT NOT NULL REFERENCES sys_rule_set_revisions(rule_set_rev_id),
    input_selections_json JSONB NOT NULL,            -- 输入冻结的特征字典 {"CNC_SYSTEM":"SIEMENS_840D", ...}
    resolved_100_bom_json JSONB NOT NULL,            -- 解算生成的不可变 100% BOM 树快照
    provenance_trace_json JSONB NOT NULL,            -- 每行选用的规则溯源跟踪字典
    result_digest_sha256 VARCHAR(64) NOT NULL,       -- 结果确定性哈希
    solver_duration_ms   INT NOT NULL,
    evaluated_by         VARCHAR(64) NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cfg_result_order ON sys_configuration_results(tenant_id, order_id);
