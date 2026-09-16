-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M21 基线与配置状态 (Baselines and Configuration States)
-- 适用环境: PostgreSQL 15+
-- 规范依据: CCD-DEV-SPEC-2.0-M21
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_baseline;

-- 1. 枚举类型定义 (安全幂等创建)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'baseline_state' AND n.nspname = 'plm_baseline') THEN
        CREATE TYPE plm_baseline.baseline_state AS ENUM (
            'DRAFT',           -- 编制中/候选圈定中
            'IN_REVIEW',       -- 闭包校验通过，处于审批流程中
            'FROZEN',          -- 已冻结生效 (物理级不可变，终态)
            'SUPERSEDED'       -- 已被后继基线替代 (仍保留只读历史与溯源)
        );
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'baseline_purpose' AND n.nspname = 'plm_baseline') THEN
        CREATE TYPE plm_baseline.baseline_purpose AS ENUM (
            'REQUIREMENT_BASELINE',   -- 需求规格基线
            'FUNCTIONAL_BASELINE',    -- 功能基线 (FBL / 系统方案阶段)
            'ALLOCATED_BASELINE',     -- 分配基线 (ABL / 初步设计/PDR)
            'PRODUCT_DESIGN_BASELINE',-- 产品设计基线 (DBL / 关键设计评审/CDR)
            'AS_DESIGNED',            -- 订单工程设计基线 (100% EBOM)
            'AS_PLANNED',             -- 工艺制造计划基线 (MBOM + BOP)
            'AS_BUILT',               -- 实物装配基线 (出厂实装)
            'AS_DELIVERED',           -- 客户交付验收基线
            'AS_MAINTAINED'           -- 现场维保服役配置基线
        );
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'member_role' AND n.nspname = 'plm_baseline') THEN
        CREATE TYPE plm_baseline.member_role AS ENUM (
            'REQUIREMENT',            -- 需求条目
            'SYSTEM_MODEL',           -- SysML v2 模型发布包
            'PARAMETER_SET',          -- 冻结参数集
            'EBOM_ROOT',              -- 顶层装配件
            'BOM_COMPONENT',          -- 结构子件
            'CAD_DRAWING',            -- 工程图纸/3D源文件
            'SOFTWARE_PACKAGE',       -- 数控固件/PLC/驱动程序
            'SIM_RUN_EVIDENCE',       -- 仿真执行凭证
            'TEST_REPORT_EVIDENCE'    -- 实机试验报告凭证
        );
    END IF;
END$$;

-- 2. 基线主表 (Baseline)
CREATE TABLE IF NOT EXISTS plm_baseline.baseline (
    baseline_id             BIGINT PRIMARY KEY,
    project_id              BIGINT NOT NULL,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'VMC_ENTERPRISE',
    baseline_code           VARCHAR(128) NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    purpose                 plm_baseline.baseline_purpose NOT NULL,
    state                   plm_baseline.baseline_state NOT NULL DEFAULT 'DRAFT',
    description             TEXT,
    closure_hash            CHAR(64) NULL, -- 全闭包节点与拓扑关系 SHA-256 强校验摘要
    working_version         BIGINT NOT NULL DEFAULT 1,
    created_by              VARCHAR(64) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    frozen_by               VARCHAR(64) NULL,
    frozen_at               TIMESTAMPTZ NULL,
    approval_ticket_id      BIGINT NULL,
    CONSTRAINT uq_m21_baseline_tenant_code UNIQUE (tenant_id, baseline_code)
);
COMMENT ON TABLE plm_baseline.baseline IS 'M21: 工程配置基线主表，FROZEN 后只读不可篡改';
CREATE INDEX IF NOT EXISTS idx_m21_baseline_project_purpose ON plm_baseline.baseline(project_id, purpose, state);

-- 3. 基线成员明细表 (BaselineMember - 固化节点快照)
CREATE TABLE IF NOT EXISTS plm_baseline.baseline_member (
    member_id               BIGINT PRIMARY KEY,
    baseline_id             BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id) ON DELETE CASCADE,
    revision_id             BIGINT NOT NULL,
    member_role             plm_baseline.member_role NOT NULL,
    object_type_code        VARCHAR(64) NOT NULL, -- PartRevision, ReqRevision, ModelRelease 等
    business_code           VARCHAR(128) NOT NULL,
    revision_label          VARCHAR(32) NOT NULL,
    content_hash            CHAR(64) NOT NULL,    -- 固化加入时该成员版本自身的内容哈希
    artifact_id             BIGINT NULL,
    custom_context          JSONB NOT NULL DEFAULT '{}'::jsonb, -- 扩展上下文 (如装配位号、变体槽位)
    added_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m21_baseline_member UNIQUE (baseline_id, revision_id)
);
COMMENT ON TABLE plm_baseline.baseline_member IS 'M21: 基线成员明细表，物理锁定特定版本的工程对象与内容哈希';
CREATE INDEX IF NOT EXISTS idx_m21_member_lookup ON plm_baseline.baseline_member(revision_id, baseline_id);

-- 4. 基线关系拓扑快照表 (BaselineRelationSnapshot - 固化图关系边)
CREATE TABLE IF NOT EXISTS plm_baseline.baseline_relation_snapshot (
    snapshot_rel_id         BIGINT PRIMARY KEY,
    baseline_id             BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id) ON DELETE CASCADE,
    source_revision_id      BIGINT NOT NULL,
    target_revision_id      BIGINT NOT NULL,
    relation_type_id        VARCHAR(64) NOT NULL, -- satisfies, verifies, allocatedTo, BOM_USAGE 等
    relation_hash           CHAR(64) NOT NULL,    -- 关系属性与端点组合摘要
    structural_context      JSONB NOT NULL DEFAULT '{}'::jsonb, -- 固化使用位置 lineId、数量、位号等
    snapshotted_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m21_baseline_rel UNIQUE (baseline_id, source_revision_id, target_revision_id, relation_type_id)
);
COMMENT ON TABLE plm_baseline.baseline_relation_snapshot IS 'M21: 基线关系拓扑快照表，冻结跨域追溯与装配关系边，防止历史断链漂移';
CREATE INDEX IF NOT EXISTS idx_m21_rel_snapshot_source ON plm_baseline.baseline_relation_snapshot(baseline_id, source_revision_id);
CREATE INDEX IF NOT EXISTS idx_m21_rel_snapshot_target ON plm_baseline.baseline_relation_snapshot(baseline_id, target_revision_id);

-- 5. 多形态配置状态引用表 (ConfigurationStateReference)
CREATE TABLE IF NOT EXISTS plm_baseline.configuration_state_reference (
    config_ref_id           BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'VMC_ENTERPRISE',
    config_state_type       plm_baseline.baseline_purpose NOT NULL,
    baseline_id             BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id) ON DELETE RESTRICT,
    order_product_id        BIGINT NULL, -- 关联订单产品定义 (针对设计与制造形态)
    individual_id           BIGINT NULL, -- 关联序列号机床实物 (针对实装/交付/服役形态)
    effective_from          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to            TIMESTAMPTZ NULL,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    notes                   TEXT,
    bound_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    bound_by                VARCHAR(64) NOT NULL,
    CONSTRAINT chk_m21_config_context_target CHECK (
        (order_product_id IS NOT NULL AND individual_id IS NULL) OR
        (order_product_id IS NULL AND individual_id IS NOT NULL) OR
        (order_product_id IS NULL AND individual_id IS NULL)
    )
);
COMMENT ON TABLE plm_baseline.configuration_state_reference IS 'M21: 承接 As-Designed 到 As-Maintained 多形态配置生命周期引用';
CREATE INDEX IF NOT EXISTS idx_m21_cfg_state_order ON plm_baseline.configuration_state_reference(order_product_id, config_state_type);
CREATE INDEX IF NOT EXISTS idx_m21_cfg_state_indiv ON plm_baseline.configuration_state_reference(individual_id, config_state_type);

-- 6. 基线演进系谱关联表 (SuccessorBaselineLink)
CREATE TABLE IF NOT EXISTS plm_baseline.successor_baseline_link (
    link_id                 BIGINT PRIMARY KEY,
    predecessor_baseline_id BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id),
    successor_baseline_id   BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id),
    change_order_id         BIGINT NULL, -- 驱动升版的 ECO 单据
    derivation_reason       TEXT NOT NULL,
    linked_at               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m21_successor_link UNIQUE (predecessor_baseline_id, successor_baseline_id),
    CONSTRAINT chk_m21_no_self_predecessor CHECK (predecessor_baseline_id != successor_baseline_id)
);
COMMENT ON TABLE plm_baseline.successor_baseline_link IS 'M21: 记录基线间受控替代与版本演化关系';

-- 7. 基线红线差分比对日志表 (BaselineDiffLog)
CREATE TABLE IF NOT EXISTS plm_baseline.baseline_diff_log (
    diff_id                 BIGINT PRIMARY KEY,
    base_baseline_id        BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id),
    target_baseline_id      BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id),
    comparison_hash         CHAR(64) NOT NULL,
    diff_summary            JSONB NOT NULL, -- 差异增删改统计与明细树
    performed_by            VARCHAR(64) NOT NULL,
    performed_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m21_baseline_diff UNIQUE (base_baseline_id, target_baseline_id, comparison_hash)
);

-- =============================================================================
-- 8. 核心完整性与只读防篡改触发器
-- =============================================================================

CREATE OR REPLACE FUNCTION plm_baseline.fn_enforce_baseline_immutability()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_TABLE_NAME = 'baseline' THEN
        IF OLD.state IN ('FROZEN', 'SUPERSEDED') THEN
            IF NOT (OLD.state = 'FROZEN' AND NEW.state = 'SUPERSEDED') THEN
                RAISE EXCEPTION 'Architecture Security Violation [CST-M21-01]: Baseline [%] is FROZEN and strictly IMMUTABLE. In-place modification is rejected.',
                    OLD.baseline_id USING ERRCODE = '23000';
            END IF;
        END IF;
    END IF;

    IF TG_TABLE_NAME IN ('baseline_member', 'baseline_relation_snapshot') THEN
        DECLARE
            v_parent_state plm_baseline.baseline_state;
        BEGIN
            SELECT state INTO v_parent_state FROM plm_baseline.baseline WHERE baseline_id = OLD.baseline_id;
            IF v_parent_state IN ('FROZEN', 'SUPERSEDED') THEN
                RAISE EXCEPTION 'Architecture Security Violation [CST-M21-01]: Cannot modify or delete elements in FROZEN Baseline [%]. History records cannot drift.',
                    OLD.baseline_id USING ERRCODE = '23000';
            END IF;
        END;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_baseline_prevent_update ON plm_baseline.baseline;
CREATE TRIGGER trg_baseline_prevent_update
BEFORE UPDATE ON plm_baseline.baseline
FOR EACH ROW EXECUTE FUNCTION plm_baseline.fn_enforce_baseline_immutability();

DROP TRIGGER IF EXISTS trg_member_prevent_tamper ON plm_baseline.baseline_member;
CREATE TRIGGER trg_member_prevent_tamper
BEFORE UPDATE OR DELETE ON plm_baseline.baseline_member
FOR EACH ROW EXECUTE FUNCTION plm_baseline.fn_enforce_baseline_immutability();

DROP TRIGGER IF EXISTS trg_rel_snapshot_prevent_tamper ON plm_baseline.baseline_relation_snapshot;
CREATE TRIGGER trg_rel_snapshot_prevent_tamper
BEFORE UPDATE OR DELETE ON plm_baseline.baseline_relation_snapshot
FOR EACH ROW EXECUTE FUNCTION plm_baseline.fn_enforce_baseline_immutability();

-- =============================================================================
-- 9. 初始化数控机床代表性基线种子数据
-- =============================================================================

-- 基线 1: VMC850 关键设计评审(CDR)产品设计基线 (已冻结 FROZEN)
INSERT INTO plm_baseline.baseline (
    baseline_id, project_id, tenant_id, baseline_code, name, purpose, state,
    description, closure_hash, working_version, created_by, frozen_by, frozen_at, approval_ticket_id
) VALUES (
    8101, 1001, 'VMC_ENTERPRISE', 'BL-VMC850-CDR-001',
    'VMC850加工中心关键设计评审(CDR)产品设计基线', 'PRODUCT_DESIGN_BASELINE', 'FROZEN',
    '锁定系统模型Commit 8fc3a、100% EBOM装配结构及主轴温升补偿闭环证据',
    'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    1, 'CFG-MGR-ZHOU', 'CHIEF-ENG-ZHANG', CURRENT_TIMESTAMP - INTERVAL '10 days', 99201488101
) ON CONFLICT (tenant_id, baseline_code) DO NOTHING;

-- 基线 2: VMC850 制造计划基线 As-Planned (已冻结 FROZEN)
INSERT INTO plm_baseline.baseline (
    baseline_id, project_id, tenant_id, baseline_code, name, purpose, state,
    description, closure_hash, working_version, created_by, frozen_by, frozen_at, approval_ticket_id
) VALUES (
    8102, 1001, 'VMC_ENTERPRISE', 'BL-VMC850-PLANNED-001',
    'VMC850工艺制造计划基线 (As-Planned)', 'AS_PLANNED', 'FROZEN',
    '绑定总装车间 MBOM 结构行与 BOP 关键装配工艺规程',
    'a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef00',
    1, 'ENG-MFG-LI', 'CHIEF-ENG-ZHANG', CURRENT_TIMESTAMP - INTERVAL '5 days', 99201488102
) ON CONFLICT (tenant_id, baseline_code) DO NOTHING;

-- 基线 3: VMC850 #SN-2026-001 出厂实装基线 As-Built (已冻结 FROZEN)
INSERT INTO plm_baseline.baseline (
    baseline_id, project_id, tenant_id, baseline_code, name, purpose, state,
    description, closure_hash, working_version, created_by, frozen_by, frozen_at, approval_ticket_id
) VALUES (
    8103, 1001, 'VMC_ENTERPRISE', 'BL-VMC850-BUILT-SN001',
    'VMC850实物机床 #SN-2026-001 出厂实装配置基线 (As-Built)', 'AS_BUILT', 'FROZEN',
    '记录实际装配 NSK 代用轴承序列号件与激光干涉仪全行程螺距补偿表',
    'b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0100',
    1, 'ENG-QC-CHEN', 'CHIEF-ENG-ZHANG', CURRENT_TIMESTAMP - INTERVAL '2 days', 99201488103
) ON CONFLICT (tenant_id, baseline_code) DO NOTHING;

-- 基线 4: HMC630 初步设计评审基线 (编制中 DRAFT)
INSERT INTO plm_baseline.baseline (
    baseline_id, project_id, tenant_id, baseline_code, name, purpose, state,
    description, closure_hash, working_version, created_by
) VALUES (
    8104, 1002, 'VMC_ENTERPRISE', 'BL-HMC630-PDR-DRAFT',
    'HMC630卧式双工位加工中心初步设计基线 (PDR)', 'ALLOCATED_BASELINE', 'DRAFT',
    '包含总体布局模型与双工位托盘交换机构初步图样，待闭包核查',
    NULL, 1, 'CFG-MGR-ZHOU'
) ON CONFLICT (tenant_id, baseline_code) DO NOTHING;

-- 初始化 CDR 基线成员节点 (固化快照)
INSERT INTO plm_baseline.baseline_member (
    member_id, baseline_id, revision_id, member_role, object_type_code, business_code, revision_label, content_hash, artifact_id
) VALUES 
(8501, 8101, 7001, 'EBOM_ROOT', 'PartRevision', 'PART-VMC850-ROOT', 'Rev.A', '8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01', 9001),
(8502, 8101, 7101, 'CAD_DRAWING', 'DocumentRevision', 'DOC-VMC850-MECH-001', 'A.1', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 9002),
(8503, 8101, 6001, 'SYSTEM_MODEL', 'ModelRelease', 'MDL-SYS-VMC850', 'v2.0', 'c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef01234', NULL),
(8504, 8101, 5001, 'REQUIREMENT', 'RequirementRevision', 'REQ-SPINDLE-SPEED', 'Rev.1', 'a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef00', NULL),
(8505, 8101, 9101, 'TEST_REPORT_EVIDENCE', 'EvidenceRecord', 'EVI-LASER-001', 'v1.0', 'c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef01234', 9005)
ON CONFLICT (baseline_id, revision_id) DO NOTHING;

-- 初始化 CDR 拓扑关系边快照 (固化图边，抗漂移)
INSERT INTO plm_baseline.baseline_relation_snapshot (
    snapshot_rel_id, baseline_id, source_revision_id, target_revision_id, relation_type_id, relation_hash, structural_context
) VALUES 
(8601, 8101, 7001, 7101, 'specifiesDrawing', 'rel_hash_drawing_01_8fc3a718d0984a1e948c21a37c02b549012398418928091', '{"primary": true}'::jsonb),
(8602, 8101, 7001, 5001, 'satisfies', 'rel_hash_satisfies_02_8fc3a718d0984a1e948c21a37c02b54901239841892809', '{"margin": "5%"}'::jsonb),
(8603, 8101, 9101, 5001, 'verifies', 'rel_hash_verifies_03_8fc3a718d0984a1e948c21a37c02b549012398418928092', '{"pass": true, "instrument": "Renishaw"}'::jsonb)
ON CONFLICT (baseline_id, source_revision_id, target_revision_id, relation_type_id) DO NOTHING;

-- 初始化多形态配置状态引用 (As-Designed, As-Planned, As-Built)
INSERT INTO plm_baseline.configuration_state_reference (
    config_ref_id, tenant_id, config_state_type, baseline_id, order_product_id, individual_id, notes, bound_by
) VALUES 
(8701, 'VMC_ENTERPRISE', 'PRODUCT_DESIGN_BASELINE', 8101, 5001, NULL, '订单 ORD-2026-VMC850 设计配置权威依据', 'ORDER-ENG-102'),
(8702, 'VMC_ENTERPRISE', 'AS_PLANNED', 8102, 5001, NULL, '第一总装车间工艺规划制造基准', 'MFG-PLANNER-01'),
(8703, 'VMC_ENTERPRISE', 'AS_BUILT', 8103, NULL, 9001, '出厂机床 #SN-2026-001 实装技术履历凭据', 'QC-INSPECTOR-03')
ON CONFLICT (config_ref_id) DO NOTHING;
