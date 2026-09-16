-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M02 项目、任务与阶段门
-- 适用环境: PostgreSQL 15+
-- 规范依据: CCD-DEV-SPEC-2.0-M02
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_project;

-- 阶段门决策枚举 (Section 10 & 40.4)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'gate_decision_type' AND n.nspname = 'plm_project') THEN
        CREATE TYPE plm_project.gate_decision_type AS ENUM (
            'PASS',              -- 审查完全通过
            'CONDITIONAL_PASS', -- 有条件通过 (强制带行动项)
            'REWORK',            -- 返工重做
            'STOP'               -- 项目终止/熔断
        );
    END IF;
END$$;

-- 任务执行状态枚举
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'task_status' AND n.nspname = 'plm_project') THEN
        CREATE TYPE plm_project.task_status AS ENUM (
            'NOT_STARTED',       -- 未启动
            'IN_PROGRESS',       -- 进行中
            'SUBMITTED',         -- 成果已提交 (待核验)
            'COMPLETED',         -- 任务已完成 (技术口径)
            'BLOCKED',           -- 遇阻挂起
            'CANCELLED'          -- 已取消
        );
    END IF;
END$$;

-- 任务依赖类型枚举
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'dependency_type' AND n.nspname = 'plm_project') THEN
        CREATE TYPE plm_project.dependency_type AS ENUM (
            'FS', -- Finish-to-Start
            'SS', -- Start-to-Start
            'FF', -- Finish-to-Finish
            'SF'  -- Start-to-Finish
        );
    END IF;
END$$;

-- 1. 项目群主表 (Program)
CREATE TABLE IF NOT EXISTS plm_project.program (
    program_id          BIGINT PRIMARY KEY,
    tenant_id           VARCHAR(64) NOT NULL,
    program_code        VARCHAR(64) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    manager_id          VARCHAR(64) NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by          VARCHAR(64) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_program_code UNIQUE (tenant_id, program_code)
);
COMMENT ON TABLE plm_project.program IS 'M02: 项目群主表，跨机型研发组合容器，严禁挂接阶段门 Gate';

-- 2. 项目主表 (Project)
CREATE TABLE IF NOT EXISTS plm_project.project (
    project_id          BIGINT PRIMARY KEY,
    program_id          BIGINT NULL REFERENCES plm_project.program(program_id),
    tenant_id           VARCHAR(64) NOT NULL,
    project_code        VARCHAR(64) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    project_type        VARCHAR(32) NOT NULL CHECK (project_type IN ('PLATFORM', 'CTO', 'ETO', 'PRE_RESEARCH')),
    manager_id          VARCHAR(64) NOT NULL,
    chief_engineer_id   VARCHAR(64) NOT NULL,
    current_stage_id    BIGINT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'PLANNING', -- PLANNING, ACTIVE, SUSPENDED, COMPLETED, TERMINATED
    working_version     BIGINT NOT NULL DEFAULT 1,
    created_by          VARCHAR(64) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_project_code UNIQUE (tenant_id, project_code)
);
CREATE INDEX IF NOT EXISTS idx_project_tenant ON plm_project.project(tenant_id, status);

-- 3. 研发阶段定义表 (Stage)
CREATE TABLE IF NOT EXISTS plm_project.stage (
    stage_id            BIGINT PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES plm_project.project(project_id) ON DELETE CASCADE,
    stage_code          VARCHAR(64) NOT NULL,
    name                VARCHAR(128) NOT NULL,
    sequence_no         INT NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, IN_GATE_REVIEW, CLOSED
    planned_start_date  DATE NOT NULL,
    planned_end_date    DATE NOT NULL,
    actual_start_date   DATE NULL,
    actual_end_date     DATE NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_stage_code UNIQUE (project_id, stage_code),
    CONSTRAINT chk_stage_dates CHECK (planned_end_date >= planned_start_date)
);
CREATE INDEX IF NOT EXISTS idx_stage_project_seq ON plm_project.stage(project_id, sequence_no);

-- 4. 阶段门定义表 (Gate)
CREATE TABLE IF NOT EXISTS plm_project.gate (
    gate_id             BIGINT PRIMARY KEY,
    stage_id            BIGINT NOT NULL REFERENCES plm_project.stage(stage_id) ON DELETE CASCADE,
    gate_code           VARCHAR(64) NOT NULL,
    name                VARCHAR(128) NOT NULL,
    description         TEXT,
    review_workflow_def VARCHAR(128) NOT NULL, -- 绑定的 Flowable BPMN Key
    status              VARCHAR(32) NOT NULL DEFAULT 'INIT', -- INIT, READY, IN_REVIEW, DECIDED
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_gate_code UNIQUE (stage_id, gate_code)
);

-- 5. 阶段门准入核验规则表 (GateEntryCriterion)
CREATE TABLE IF NOT EXISTS plm_project.gate_entry_criterion (
    criterion_id        BIGINT PRIMARY KEY,
    gate_id             BIGINT NOT NULL REFERENCES plm_project.gate(gate_id) ON DELETE CASCADE,
    criterion_code      VARCHAR(64) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    rule_type           VARCHAR(64) NOT NULL, -- DELIVERABLE_CHECK, EVIDENCE_COVERAGE, BASELINE_LOCKED
    threshold_value     NUMERIC(12,4) NULL,   -- 如证据覆盖率 100.00
    is_blocking         BOOLEAN NOT NULL DEFAULT TRUE, -- 是否为一票否决项
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_gate_crit_code UNIQUE (gate_id, criterion_code)
);

-- 6. 阶段门决策记录表 (GateDecision - 不可变凭证)
CREATE TABLE IF NOT EXISTS plm_project.gate_decision (
    decision_id         BIGINT PRIMARY KEY,
    gate_id             BIGINT NOT NULL REFERENCES plm_project.gate(gate_id),
    decision_type       plm_project.gate_decision_type NOT NULL,
    decision_notes      TEXT NOT NULL,
    evaluated_baseline_id BIGINT NULL,
    approval_ticket_id  BIGINT NOT NULL,      -- 审批实例 ID
    allowed_scope       TEXT NULL,            -- CONDITIONAL_PASS 时明确允许放行的范围
    decision_maker_id   VARCHAR(64) NOT NULL, -- 委员会代表工号
    decided_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_scope_for_conditional CHECK (
        (decision_type = 'CONDITIONAL_PASS' AND allowed_scope IS NOT NULL) OR
        (decision_type != 'CONDITIONAL_PASS')
    )
);
COMMENT ON TABLE plm_project.gate_decision IS 'M02: 阶段门决策凭证，已发布不可修改，历史记录永久保留';
CREATE INDEX IF NOT EXISTS idx_gate_decision_gate ON plm_project.gate_decision(gate_id, decided_at DESC);

-- 7. 阶段门遗留行动项表 (ActionItem)
CREATE TABLE IF NOT EXISTS plm_project.action_item (
    action_item_id      BIGINT PRIMARY KEY,
    decision_id         BIGINT NOT NULL REFERENCES plm_project.gate_decision(decision_id),
    title               VARCHAR(255) NOT NULL,
    description         TEXT NOT NULL,
    owner_id            VARCHAR(64) NOT NULL,
    approver_id         VARCHAR(64) NOT NULL,
    due_date            DATE NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'OPEN', -- OPEN, RESOLVED, CLOSED, OVERDUE
    resolution_summary  TEXT NULL,
    closed_at           TIMESTAMPTZ NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_action_item_owner ON plm_project.action_item(owner_id, status);

-- 8. WBS 树形分解节点表 (WBSNode)
CREATE TABLE IF NOT EXISTS plm_project.wbs_node (
    wbs_node_id         BIGINT PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES plm_project.project(project_id) ON DELETE CASCADE,
    parent_node_id      BIGINT NULL REFERENCES plm_project.wbs_node(wbs_node_id),
    wbs_code            VARCHAR(64) NOT NULL,
    name                VARCHAR(128) NOT NULL,
    node_level          INT NOT NULL DEFAULT 1,
    weight              NUMERIC(5,2) NOT NULL DEFAULT 1.00,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_wbs_code UNIQUE (project_id, wbs_code)
);
CREATE INDEX IF NOT EXISTS idx_wbs_hierarchy ON plm_project.wbs_node(project_id, parent_node_id);

-- 9. 任务执行表 (Task)
CREATE TABLE IF NOT EXISTS plm_project.task (
    task_id             BIGINT PRIMARY KEY,
    wbs_node_id         BIGINT NOT NULL REFERENCES plm_project.wbs_node(wbs_node_id) ON DELETE CASCADE,
    stage_id            BIGINT NOT NULL REFERENCES plm_project.stage(stage_id), -- 正交指派 Stage
    task_code           VARCHAR(64) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    assignee_id         VARCHAR(64) NOT NULL,
    planned_start_date  DATE NOT NULL,
    planned_end_date    DATE NOT NULL,
    actual_start_date   DATE NULL,
    actual_end_date     DATE NULL,
    duration_days       INT NOT NULL DEFAULT 1,
    progress_percent    INT NOT NULL DEFAULT 0 CHECK (progress_percent BETWEEN 0 AND 100),
    status              plm_project.task_status NOT NULL DEFAULT 'NOT_STARTED',
    working_version     BIGINT NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_task_code UNIQUE (wbs_node_id, task_code),
    CONSTRAINT chk_task_dates CHECK (planned_end_date >= planned_start_date)
);
CREATE INDEX IF NOT EXISTS idx_task_stage ON plm_project.task(stage_id, status);
CREATE INDEX IF NOT EXISTS idx_task_assignee ON plm_project.task(assignee_id, status);

-- 10. 任务前后置依赖表 (TaskDependency - DAG 物理存储)
CREATE TABLE IF NOT EXISTS plm_project.task_dependency (
    predecessor_task_id BIGINT NOT NULL REFERENCES plm_project.task(task_id) ON DELETE RESTRICT,
    successor_task_id   BIGINT NOT NULL REFERENCES plm_project.task(task_id) ON DELETE RESTRICT,
    dep_type            plm_project.dependency_type NOT NULL DEFAULT 'FS',
    lag_days            INT NOT NULL DEFAULT 0,
    PRIMARY KEY (predecessor_task_id, successor_task_id),
    CONSTRAINT chk_no_self_dependency CHECK (predecessor_task_id != successor_task_id)
);

-- 11. 任务交付物规约要求表 (DeliverableRequirement)
CREATE TABLE IF NOT EXISTS plm_project.deliverable_requirement (
    deliv_req_id        BIGINT PRIMARY KEY,
    task_id             BIGINT NOT NULL REFERENCES plm_project.task(task_id) ON DELETE CASCADE,
    requirement_code    VARCHAR(64) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    deliverable_type    VARCHAR(64) NOT NULL, -- SYSML_MODEL, REQ_BASELINE, EBOM_STRUCT, SIM_REPORT 等
    is_mandatory        BOOLEAN NOT NULL DEFAULT TRUE,
    target_security_level VARCHAR(32) NOT NULL DEFAULT 'INTERNAL',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_deliv_req UNIQUE (task_id, requirement_code)
);

-- 12. 任务交付物提交记录表 (DeliverableSubmission)
CREATE TABLE IF NOT EXISTS plm_project.deliverable_submission (
    submission_id       BIGINT PRIMARY KEY,
    deliv_req_id        BIGINT NOT NULL REFERENCES plm_project.deliverable_requirement(deliv_req_id),
    revision_id         BIGINT NOT NULL,                              -- 挂接核心工程对象修订版
    baseline_id         BIGINT NULL,                                  -- 或直接挂接基线
    artifact_hash       VARCHAR(64) NOT NULL,
    submission_notes    TEXT,
    is_latest           BOOLEAN NOT NULL DEFAULT TRUE,
    submitted_by        VARCHAR(64) NOT NULL,
    submitted_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_deliv_sub_req ON plm_project.deliverable_submission(deliv_req_id, is_latest);

-- 13. 计划进度基线表 (ScheduleBaseline)
CREATE TABLE IF NOT EXISTS plm_project.schedule_baseline (
    sched_baseline_id   BIGINT PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES plm_project.project(project_id),
    baseline_version    VARCHAR(32) NOT NULL, -- 如 "BL-V1.0"
    frozen_snapshot     JSONB NOT NULL,       -- 任务、依赖与关键路径拓扑快照
    snapshot_hash       CHAR(64) NOT NULL,
    frozen_by           VARCHAR(64) NOT NULL,
    frozen_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_sched_baseline UNIQUE (project_id, baseline_version)
);
