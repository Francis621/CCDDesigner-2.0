-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M22 工程变更与影响处置 (Change Management)
-- 适用环境: PostgreSQL 15+
-- 规范依据: CCD-DEV-SPEC-2.0-M22
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_change;

-- 1. 枚举类型定义 (安全幂等创建)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'change_reason_type' AND n.nspname = 'plm_change') THEN
        CREATE TYPE plm_change.change_reason_type AS ENUM (
            'CUSTOMER_REQUIREMENT',  -- 客户提出新需求/技术规格调整
            'FIELD_FAILURE',         -- 现场维保/实机故障反馈 (M28反馈)
            'SIMULATION_DEVIATION',  -- 虚拟验证未达标/性能超差 (M10/M11发现)
            'MANUFACTURING_DEFECT',  -- 车间制造装配工艺性缺陷 (M25/M26反馈)
            'COST_REDUCTION',        -- 价值工程/降本重构
            'SUPPLIER_OBSOLESCENCE', -- 供应商停产/元器件升级
            'STANDARDS_COMPLIANCE'   -- 行业标准/适航法规变更
        );
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'ecr_status' AND n.nspname = 'plm_change') THEN
        CREATE TYPE plm_change.ecr_status AS ENUM (
            'DRAFT',                 -- 编制中
            'SUBMITTED',             -- 已提交/技术可行性初审中
            'IN_REVIEW',             -- 变更委员会 (CCB) 评审中
            'APPROVED',              -- 批准立项 (授权签发 ECO)
            'REJECTED',              -- 驳回终止
            'CLOSED'                 -- 关联的 ECO 已全闭环后关闭
        );
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'eco_status' AND n.nspname = 'plm_change') THEN
        CREATE TYPE plm_change.eco_status AS ENUM (
            'DRAFT',                 -- 编制中
            'ASSESSING',             -- 影响分析与专业处置裁决中
            'AUTHORIZED',            -- CCB 正式授权实施新版本
            'IMPLEMENTING',          -- 新版本设计工作区迭代中
            'REVIEWING',             -- 新设计与再验证报告会签中
            'RELEASED',              -- 设计新版本发布 (PLM设计态终结)
            'EXECUTING',             -- 现场与工厂物理实施对账中
            'CLOSED',                -- 现场回执 100% 确认，整单法律关闭
            'CANCELLED'              -- 中途取消
        );
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'impact_decision_type' AND n.nspname = 'plm_change') THEN
        CREATE TYPE plm_change.impact_decision_type AS ENUM (
            'MODIFY',                -- 确认受波及且必须升版/新建设计
            'RE_VERIFY',             -- 结构不变，但历史验证失效，需重新计算/试验
            'REVIEW_ONLY',           -- 确认受波及，仅需设计校核，无需修改实体
            'NO_IMPACT'              -- 经专业分析判定无实际影响 (必须填写工程免责依据)
        );
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'disposition_action_type' AND n.nspname = 'plm_change') THEN
        CREATE TYPE plm_change.disposition_action_type AS ENUM (
            'SCRAP',                 -- 物理报废 (已加工零部件/在制品)
            'REWORK',                -- 现场按新图纸返修/重新调机
            'USE_UP',                -- 自然消耗过渡 (允许在旧批次中继续装配)
            'AS_IS'                  -- 维持原样 (特批让步使用)
        );
    END IF;
END$$;

-- 2. 变更请求表 (ChangeRequest - ECR)
CREATE TABLE IF NOT EXISTS plm_change.change_request (
    ecr_id                  BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'VMC_ENTERPRISE',
    project_id              BIGINT NOT NULL,
    ecr_number              VARCHAR(128) NOT NULL,
    title                   VARCHAR(255) NOT NULL,
    reason_type             plm_change.change_reason_type NOT NULL,
    problem_description     TEXT NOT NULL,
    proposed_solution       TEXT,
    urgency_level           VARCHAR(32) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, EMERGENCY
    status                  plm_change.ecr_status NOT NULL DEFAULT 'DRAFT',
    source_service_case_id  BIGINT NULL,
    originator_id           VARCHAR(64) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m22_ecr_number UNIQUE (tenant_id, ecr_number)
);
COMMENT ON TABLE plm_change.change_request IS 'M22: 变更请求表，界定问题起因与初始诉求';
CREATE INDEX IF NOT EXISTS idx_m22_ecr_project ON plm_change.change_request(project_id, status);

-- 3. 变更实施单表 (ChangeOrder - ECO)
CREATE TABLE IF NOT EXISTS plm_change.change_order (
    eco_id                  BIGINT PRIMARY KEY,
    ecr_id                  BIGINT NOT NULL REFERENCES plm_change.change_request(ecr_id),
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'VMC_ENTERPRISE',
    eco_number              VARCHAR(128) NOT NULL,
    title                   VARCHAR(255) NOT NULL,
    change_category         VARCHAR(64) NOT NULL DEFAULT 'MAJOR', -- MAJOR, MINOR, ADMINISTRATIVE
    target_baseline_id      BIGINT NULL, -- 目标受波及基线
    status                  plm_change.eco_status NOT NULL DEFAULT 'DRAFT',
    is_impact_analysis_truncated BOOLEAN NOT NULL DEFAULT FALSE,
    working_version         BIGINT NOT NULL DEFAULT 1,
    ccb_approval_ticket_id  BIGINT NULL,
    released_at             TIMESTAMPTZ NULL,
    closed_at               TIMESTAMPTZ NULL,
    created_by              VARCHAR(64) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m22_eco_number UNIQUE (tenant_id, eco_number)
);
COMMENT ON TABLE plm_change.change_order IS 'M22: 变更实施单，管理跨学科工程设计更新与现场生效';
CREATE INDEX IF NOT EXISTS idx_m22_eco_status ON plm_change.change_order(tenant_id, status);

-- 4. 影响面候选节点表 (ImpactItem - 接收 M23 拓扑推演)
CREATE TABLE IF NOT EXISTS plm_change.impact_item (
    impact_item_id          BIGINT PRIMARY KEY,
    eco_id                  BIGINT NOT NULL REFERENCES plm_change.change_order(eco_id) ON DELETE CASCADE,
    candidate_revision_id   BIGINT NOT NULL,
    object_type_code        VARCHAR(64) NOT NULL,
    propagation_path        JSONB NOT NULL DEFAULT '[]'::jsonb,
    traversal_depth         INT NOT NULL DEFAULT 1,
    assigned_discipline     VARCHAR(32) NOT NULL, -- MECHANICAL, ELECTRICAL, CONTROL, SIMULATION
    is_assessed             BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_m22_eco_impact_revision UNIQUE (eco_id, candidate_revision_id)
);
COMMENT ON TABLE plm_change.impact_item IS 'M22: 变更影响候选表，承接 M23 递归计算的有向网络节点';
CREATE INDEX IF NOT EXISTS idx_m22_impact_eco_assigned ON plm_change.impact_item(eco_id, assigned_discipline, is_assessed);

-- 5. 专业影响处置决定表 (ImpactDecision - 工程师签字裁定)
CREATE TABLE IF NOT EXISTS plm_change.impact_decision (
    decision_id             BIGINT PRIMARY KEY,
    impact_item_id          BIGINT NOT NULL REFERENCES plm_change.impact_item(impact_item_id) ON DELETE CASCADE,
    decision_type           plm_change.impact_decision_type NOT NULL,
    technical_rationale     TEXT NOT NULL,
    action_required         TEXT,
    target_action_plan      VARCHAR(64) NULL, -- REVISE_EXISTING (升版), CREATE_NEW (新建件 - ADR-05)
    assessor_id             VARCHAR(64) NOT NULL,
    assessed_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m22_decision_item UNIQUE (impact_item_id)
);
COMMENT ON TABLE plm_change.impact_decision IS 'M22: 影响处置裁决表，固化专家决策与免责依据';

-- 6. 变更实施分解任务表 (ChangeTask - 驱动设计与验证工作)
CREATE TABLE IF NOT EXISTS plm_change.change_task (
    task_id                 BIGINT PRIMARY KEY,
    eco_id                  BIGINT NOT NULL REFERENCES plm_change.change_order(eco_id) ON DELETE CASCADE,
    task_code               VARCHAR(64) NOT NULL,
    title                   VARCHAR(255) NOT NULL,
    task_type               VARCHAR(64) NOT NULL, -- CAD_REMODEL, SIM_RERUN, REQ_UPDATE, DRAWING_REDRAW
    assignee_id             VARCHAR(64) NOT NULL,
    source_revision_id      BIGINT NULL,
    target_revision_id      BIGINT NULL,
    status                  VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED
    completed_at            TIMESTAMPTZ NULL,
    CONSTRAINT uq_m22_eco_task_code UNIQUE (eco_id, task_code)
);
COMMENT ON TABLE plm_change.change_task IS 'M22: 变更实施分解任务表，驱动跨专业设计工作空间实施';

-- 7. 物料、订单与设备现场生效处置规约表 (EffectivityDisposition)
CREATE TABLE IF NOT EXISTS plm_change.effectivity_disposition (
    disposition_id          BIGINT PRIMARY KEY,
    eco_id                  BIGINT NOT NULL REFERENCES plm_change.change_order(eco_id) ON DELETE CASCADE,
    target_scope_type       VARCHAR(32) NOT NULL CHECK (target_scope_type IN ('INVENTORY_PART', 'IN_PROCESS_ORDER', 'FIELD_MACHINE')),
    target_part_number      VARCHAR(128) NOT NULL,
    target_order_product_id BIGINT NULL,
    target_individual_id    BIGINT NULL,
    action_type             plm_change.disposition_action_type NOT NULL,
    effective_serial_cutoff VARCHAR(128) NULL,
    effective_date_cutoff   DATE NULL,
    disposition_instructions TEXT NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_change.effectivity_disposition IS 'M22: 现场处置策略表，明确在制品/库存/现场机床的报废/返工规则';

-- 8. 变更现场实施回执跟踪表 (ImplementationRecord - 跨系统对账)
CREATE TABLE IF NOT EXISTS plm_change.implementation_record (
    record_id               BIGINT PRIMARY KEY,
    eco_id                  BIGINT NOT NULL REFERENCES plm_change.change_order(eco_id),
    disposition_id          BIGINT NOT NULL REFERENCES plm_change.effectivity_disposition(disposition_id),
    target_system           VARCHAR(64) NOT NULL, -- MES, ERP, FIELD_CRM
    external_receipt_id     BIGINT NULL,
    execution_status        VARCHAR(32) NOT NULL DEFAULT 'DISPATCHED', -- DISPATCHED, IN_EXECUTION, COMPLETED, FAILED
    site_operator_id        VARCHAR(64) NULL,
    completion_evidence_doc BIGINT NULL,
    confirmed_at            TIMESTAMPTZ NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_change.implementation_record IS 'M22: 现场执行对账表，严格依赖 MES 项级回执核验真实闭环';
CREATE INDEX IF NOT EXISTS idx_m22_impl_record_eco ON plm_change.implementation_record(eco_id, execution_status);

-- =============================================================================
-- 核心约束与触发器: AT-22 现场实施未闭环禁止关闭拦截
-- =============================================================================
CREATE OR REPLACE FUNCTION plm_change.fn_enforce_eco_close_guard()
RETURNS TRIGGER AS $$
DECLARE
    v_uncompleted_task_count INT;
    v_pending_impl_count INT;
    v_open_disposition_count INT;
BEGIN
    -- 仅拦截向 CLOSED 终态迁移的动作
    IF NEW.status = 'CLOSED' AND OLD.status != 'CLOSED' THEN
        -- 1. 检查设计实施任务是否 100% 完成
        SELECT COUNT(1) INTO v_uncompleted_task_count
        FROM plm_change.change_task
        WHERE eco_id = NEW.eco_id AND status != 'COMPLETED';

        IF v_uncompleted_task_count > 0 THEN
            RAISE EXCEPTION 'ECO Close Rejected: Cannot close ECO [%]. There are [%] engineering tasks not COMPLETED.',
                NEW.eco_number, v_uncompleted_task_count
                USING ERRCODE = '23000';
        END IF;

        -- 2. 检查现场生效处置方案是否存在未下发对账的孤儿策略
        SELECT COUNT(1) INTO v_open_disposition_count
        FROM plm_change.effectivity_disposition ed
        LEFT JOIN plm_change.implementation_record ir ON ed.disposition_id = ir.disposition_id
        WHERE ed.eco_id = NEW.eco_id AND ir.record_id IS NULL;

        IF v_open_disposition_count > 0 THEN
            RAISE EXCEPTION 'ECO Close Rejected: Cannot close ECO [%]. There are [%] Effectivity Dispositions without dispatched implementation tracking.',
                NEW.eco_number, v_open_disposition_count
                USING ERRCODE = '23000';
        END IF;

        -- 3. 检查现场实施记录是否全部取得 COMPLETED 终态回执 (AT-22)
        SELECT COUNT(1) INTO v_pending_impl_count
        FROM plm_change.implementation_record
        WHERE eco_id = NEW.eco_id AND execution_status != 'COMPLETED';

        IF v_pending_impl_count > 0 THEN
            RAISE EXCEPTION 'ECO Close Blocked (AT-22): Field execution is incomplete. [%] external implementation receipts are still pending or in-progress. ECO cannot be closed before physical execution confirmation.',
                NEW.eco_number, v_pending_impl_count
                USING ERRCODE = '23000';
        END IF;

        NEW.closed_at = CURRENT_TIMESTAMP;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_enforce_eco_close ON plm_change.change_order;
CREATE TRIGGER trg_enforce_eco_close
BEFORE UPDATE ON plm_change.change_order
FOR EACH ROW
EXECUTE FUNCTION plm_change.fn_enforce_eco_close_guard();

-- =============================================================================
-- 代表性数控机床工程变更种子数据: VMC1000 主轴提速到 15000 rpm 变更案例
-- =============================================================================
INSERT INTO plm_change.change_request (
    ecr_id, tenant_id, project_id, ecr_number, title, reason_type, problem_description, proposed_solution, urgency_level, status, originator_id, created_at
) VALUES (
    7001, 'VMC_ENTERPRISE', 101, 'ECR-2026-0042',
    'VMC1000立式加工中心高速电主轴转速提升至15000rpm变更请求',
    'CUSTOMER_REQUIREMENT',
    '航空航天薄壁结构件高速铣削客户要求主轴额定工作转速由12000rpm提升至15000rpm，原钢球轴承温升超标，驱动电机额定功率不足。',
    '将主轴前端支撑轴承升级为超精密陶瓷球角接触轴承，驱动电机功率由15kW增大至18.5kW，并重新进行热伸长有限元仿真。',
    'HIGH', 'APPROVED', 'sys_chief_engineer', CURRENT_TIMESTAMP - INTERVAL '15 days'
) ON CONFLICT (tenant_id, ecr_number) DO NOTHING;

INSERT INTO plm_change.change_order (
    eco_id, ecr_id, tenant_id, eco_number, title, change_category, target_baseline_id, status, is_impact_analysis_truncated, working_version, ccb_approval_ticket_id, released_at, created_by, created_at
) VALUES (
    8001, 7001, 'VMC_ENTERPRISE', 'ECO-2026-0042',
    'VMC1000主轴提速至15000rpm工程实施与全生命周期现场处置单',
    'MAJOR', 1001, 'EXECUTING', FALSE, 1, 9005, CURRENT_TIMESTAMP - INTERVAL '5 days', 'chief_designer', CURRENT_TIMESTAMP - INTERVAL '12 days'
) ON CONFLICT (tenant_id, eco_number) DO NOTHING;

-- 插入候选影响项
INSERT INTO plm_change.impact_item (
    impact_item_id, eco_id, candidate_revision_id, object_type_code, propagation_path, traversal_depth, assigned_discipline, is_assessed
) VALUES 
(8101, 8001, 5003, 'PartRevision', '["REQ-VMC1000-SPEED", "SPINDLE_SUBSYS", "M-VMC850-BRG-7014"]'::jsonb, 2, 'MECHANICAL', TRUE),
(8102, 8001, 5005, 'VerificationCaseRevision', '["REQ-VMC1000-SPEED", "TC-SPINDLE-THERMAL"]'::jsonb, 2, 'SIMULATION', TRUE),
(8103, 8001, 5006, 'PartRevision', '["REQ-VMC1000-SPEED", "M-VMC1000-MOTOR-15KW"]'::jsonb, 2, 'ELECTRICAL', TRUE),
(8104, 8001, 5002, 'DocRevision', '["REQ-VMC1000-SPEED", "DOC-VMC850-DRW-001"]'::jsonb, 3, 'MECHANICAL', TRUE)
ON CONFLICT (eco_id, candidate_revision_id) DO NOTHING;

-- 插入工程师裁定
INSERT INTO plm_change.impact_decision (
    decision_id, impact_item_id, decision_type, technical_rationale, action_required, target_action_plan, assessor_id
) VALUES
(8201, 8101, 'MODIFY', '15000rpm 超出原钢球轴承极限dmn值，配合公差与配合面改变，必须创建全新陶瓷球轴承组件(ADR-05)', '申请新物料号VMC1000-SP-CERAMIC-001并搭建新BOM', 'CREATE_NEW', 'eng_mech_lead'),
(8202, 8102, 'RE_VERIFY', '转速提升25%，原热平衡证据失效，严禁继承历史PASS结论(ADR-08)，需执行15000rpm工况仿真', '在OpenModelica中重跑热机耦合仿真模型', 'REVISE_EXISTING', 'eng_sim_lead'),
(8203, 8103, 'MODIFY', '切削功率与扭矩要求增大，电机更换为18.5kW高刚度电机，两向互换允许升版', '原电机物料升版至Rev B', 'REVISE_EXISTING', 'eng_elec_lead'),
(8204, 8104, 'REVIEW_ONLY', '电主轴外形安装法兰与定位尺寸未改变，仅需重新校核工程图公差标注', '复核并更新CAD图纸表面粗糙度要求', 'REVISE_EXISTING', 'eng_mech_lead')
ON CONFLICT (impact_item_id) DO NOTHING;

-- 插入实施任务
INSERT INTO plm_change.change_task (
    task_id, eco_id, task_code, title, task_type, assignee_id, source_revision_id, target_revision_id, status, completed_at
) VALUES
(8301, 8001, 'TSK-2026-01', '新建超精密陶瓷轴承主轴总成', 'CAD_REMODEL', 'eng_mech_lead', 5003, 6001, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '6 days'),
(8302, 8001, 'TSK-2026-02', '15000rpm主轴稳态与瞬态热平衡重算', 'SIM_RERUN', 'eng_sim_lead', 5005, 6002, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '6 days')
ON CONFLICT (eco_id, task_code) DO NOTHING;

-- 插入现场生效策略规约
INSERT INTO plm_change.effectivity_disposition (
    disposition_id, eco_id, target_scope_type, target_part_number, target_order_product_id, target_individual_id, action_type, effective_serial_cutoff, disposition_instructions
) VALUES
(8401, 8001, 'INVENTORY_PART', 'M-VMC850-BRG-7014', NULL, NULL, 'SCRAP', NULL, '库房剩余旧款钢球轴承12套执行退库报废，冲减制造费用'),
(8402, 8001, 'IN_PROCESS_ORDER', 'M-VMC1000-SPN-01', 3001, NULL, 'REWORK', NULL, '车间在制装配工单 OPD-1001 暂停，拆卸原主轴箱换装陶瓷轴承并重新动平衡')
ON CONFLICT (disposition_id) DO NOTHING;

-- 插入现场回执对账记录 (故意保留一条未完成回执，用于验证 AT-22 守卫)
INSERT INTO plm_change.implementation_record (
    record_id, eco_id, disposition_id, target_system, external_receipt_id, execution_status, site_operator_id, confirmed_at
) VALUES
(8501, 8001, 8401, 'ERP', 9011, 'COMPLETED', 'warehouse_admin', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(8502, 8001, 8402, 'MES', 9012, 'DISPATCHED', 'mes_lead_op', NULL)
ON CONFLICT (record_id) DO NOTHING;
