# CCDDesigner 2.0 模块开发详细规格说明书

## M22: 工程变更与影响处置 (Engineering Change and Impact Disposition)

| **文档属性**    | **内容**                                                     |
| --------------- | ------------------------------------------------------------ |
| **模块编号**    | `M22` (Phase: P1/P2, Type: N)                                |
| **文档编号**    | `CCD-DEV-SPEC-2.0-M22`                                       |
| **版本 / 状态** | V1.0 / 评审发布稿                                            |
| **主责模块**    | M22 工程变更与影响处置                                       |
| **协同模块**    | M01 (统一工作台)、M02 (项目与阶段门)、M03 (需求与规格)、M06 (系统模型发布)、M07 (参数集)、M10 (仿真重算)、M11 (证据重评)、M15 (订单产品定义)、M16 (EBOM)、M17 (CAD/CAE协同)、M18 (电气软件)、M20 (生命周期底座)、M21 (基线服务)、M23 (数字主线与拓扑遍历)、M24 (工作流与审批)、M25/M26 (MBOM与制造回传)、M27/M28 (实物与维保) |
| **上位依据**    | 《CCDDesigner 2.0 产品开发说明书》(`CCD-DEV-SPEC-2.0-001`) §4.5 (M22), §5.2, §7.1, §8.4    《CCDDesigner 2.0 产品功能架构与模块设计说明书》(`CCD-ARCH-FUNC-2.0-001`) §30, §40.4, §41.1, §45 |
| **适用受众**    | 变更协调人、配置管理员、各专业总师、后端开发工程师、MES/ERP 集成实施工程师 |

### 1. 模块定位与核心设计原则

依据上位规范要求，M22 承担全系统工程问题捕获、变更请求分析、变更实施授权及跨系统生效闭环的法定治理职责：  

1. **管理两阶段解耦原则（ECR 与 ECO 分离）**：
   - **变更请求（ChangeRequest, ECR）**：界定问题起因、变更诉求与初始波及范围，负责“为什么变”与“是否值得变”的业务决策。  
   - **变更实施单（ChangeOrder, ECO）**：定义工程实施计划、授权新修订工作空间、拆解跨专业设计任务并跟踪发布闭环，负责“如何受控实施”的技术落地。  
2. **“候选推演”与“工程裁定”分离原则（AT-10 规范）**：
   - M23 数字主线仅负责依据图拓扑关系自动计算影响候选集（ImpactCandidateSet）及传播路径，不能直接将关联节点标记为必须修改。  
   - 必须由各专业责任工程师在 M22 填报**处置决定（ImpactDecision）**，明确标识为“修改（MODIFY）”、“复核（REVIEW_ONLY）”、“重新验证（RE_VERIFY）”或“不受影响（NO_IMPACT）”，并签署工程依据。  
   - 当图遍历因深度限制（$\le 5$ 层）或权限截断时，系统必须强制标记“评估不完整”，严禁出具伪完全的分析报告。  
3. **两阶段独立生效与双重闭环铁律（AT-22 守护）**：
   - 设计发布状态（PLM Released）**与**现场实施状态（Field Executed）严格解耦。  
   - PLM 内部审批通过仅代表允许发布新工程定义，严禁将其等价于现场实施完成。  
   - **终态关闭拦截**：在外部 ERP/MES 未确认逐项回执、在制设备未完成返工或现场维保未闭环前，系统物理阻断变更单流转至 `CLOSED` 终态。  
4. **物料互换性准则（ADR-05 落地）**：
   - 严格遵循“形状、配合、功能（Form-Fit-Function, FFF）改变必须新建零件主对象”原则；  
   - 仅在完全双向互换时允许对既有物料派生新修订版本（`PartRevision`）；单向互换或不互换时强制创建全新物料编码。  
5. **在制与现场实物差异化处置（EffectivityDisposition）**：
   - 必须针对受波及的在制订单（OrderProduct）、库存批次（Inventory）及现场出厂设备（MachineIndividual）显式制定生效处置策略（报废 SCRAP、返工 REWORK、自然过渡 USE_UP、维持原样 AS_IS）。  
   - 禁止“一刀切”式全盘覆写历史或实物状态。  

### 2. 双向追踪矩阵 (Bidirectional Traceability Matrix)

| **功能编号 / 约束规范**               | **主责与协同模块  MD+ 1** | **上位架构依据与章节  MD+ 1**                          | **覆盖验收用例  MD+ 1** | **核心控制逻辑与阻断行为**                                   |
| ------------------------------------- | ------------------------- | ------------------------------------------------------ | ----------------------- | ------------------------------------------------------------ |
| **M22-F01** (变更原因与 ECR 签发)     | M22, M01, M20             | CCD-DEV-SPEC §4.5    CCD-ARCH-FUNC §30                 | AT-10                   | 登记客户、现场失效或降本原因；圈定初始种子对象；签发受控 ECR 编号 |
| **M22-F02** (拓扑候选计算与专业处置)  | M22, M23, M30             | CCD-DEV-SPEC §4.5, §5.2    CCD-ARCH-FUNC §30, §31      | AT-10, AT-13            | 调用 M23 沿关系展开；对无权限节点脱敏；专业工程师逐项录入处置依据与签名 |
| **M22-F03** (签发 ECO 与工作空间授权) | M22, M02, M04, M16        | CCD-DEV-SPEC §4.5, ADR-05    CCD-ARCH-FUNC §30         | AT-09, AT-18            | 审批通过后自动为授权的待修改对象生成 DRAFT 新修订；指派 WBS 变更任务 |
| **M22-F04** (新版本发布与验证闭环)    | M22, M06, M11, M21        | CCD-DEV-SPEC §4.3, ADR-08    CCD-ARCH-FUNC §19, §30    | AT-05, AT-07, AT-21     | 检查关联新对象是否全量 RELEASED；要求强制重评的用例必须取得新 PASS 证据 |
| **M22-F05** (实物处置与生效闭环)      | M22, M26, M27, M28        | CCD-DEV-SPEC §4.5, §4.6    CCD-ARCH-FUNC §30, §34, §35 | AT-11, AT-12, AT-22     | 向在制订单、工厂及实物下发生效方案；收集 MES 逐项回执，回执未全前阻断关闭 |
| **CST-M22-01** (发布与现场解耦约束)   | M22, M26                  | CCD-DEV-SPEC §1.1(6), §4.5    CCD-ARCH-FUNC §30        | AT-22                   | 数据库触发器硬拦截：ECO 在 `RELEASED` 状态仅代表设计完成，禁止直接置 `CLOSED` |
| **CST-M22-02** (影响处置评估完整性)   | M22, M23                  | CCD-DEV-SPEC §4.5    CCD-ARCH-FUNC §30, §31            | AT-10                   | 候选集中若存在未完成处置签名的 `ImpactItem`，一票否决 ECO 进入审批流 |

### 3. 领域对象模型与 ER 逻辑关系 (Mermaid ERD)

代码段

```
erDiagram
    CHANGE_REQUEST ||--o| CHANGE_ORDER : "authorizes"
    CHANGE_REQUEST ||--|{ CHANGE_REQUEST_ITEM : "specifies_initial_scope"
    
    CHANGE_ORDER ||--|{ IMPACT_ITEM : "evaluates_candidates"
    IMPACT_ITEM ||--|| IMPACT_DECISION : "disposed_by"
    
    CHANGE_ORDER ||--o{ CHANGE_TASK : "decomposes_into"
    CHANGE_TASK }o--|| OBJ_REVISION : "modifies_or_creates"
    
    CHANGE_ORDER ||--o{ EFFECTIVITY_DISPOSITION : "defines_field_rules"
    EFFECTIVITY_DISPOSITION }o--o| ORDER_PRODUCT_DEFINITION : "applies_to_order"
    EFFECTIVITY_DISPOSITION }o--o| MACHINE_INDIVIDUAL : "applies_to_machine"
    
    CHANGE_ORDER ||--o{ IMPLEMENTATION_RECORD : "tracks_execution"
    IMPLEMENTATION_RECORD }o--o| EXTERNAL_RECEIPT : "verified_by_receipt"
    
    CHANGE_ORDER }o--|| BASELINE : "targets_baseline"
```

### 4. 物理数据字典与 PostgreSQL DDL 规范

以下物理 DDL 落入 `plm_change` 独立业务 Schema，与通用底座 `plm_govern` 严格联动。

SQL

```
-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M22 工程变更与影响处置
-- 适用环境: PostgreSQL 15+
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_change;

-- 变更原因分类枚举
CREATE TYPE plm_change.change_reason_type AS ENUM (
    'CUSTOMER_REQUIREMENT',  -- 客户提出新需求/技术规格调整
    'FIELD_FAILURE',         -- 现场维保/实机故障反馈 (M28反馈)
    'SIMULATION_DEVIATION',  -- 虚拟验证未达标/性能超差 (M10/M11发现)
    'MANUFACTURING_DEFECT',  -- 车间制造装配工艺性缺陷 (M25/M26反馈)
    'COST_REDUCTION',        -- 价值工程/降本重构
    'SUPPLIER_OBSOLESCENCE', -- 供应商停产/元器件升级
    'STANDARDS_COMPLIANCE'   -- 行业标准/适航法规变更
);

-- ECR 业务状态枚举
CREATE TYPE plm_change.ecr_status AS ENUM (
    'DRAFT',                 -- 编制中
    'SUBMITTED',             -- 已提交/技术可行性初审中
    'IN_REVIEW',             -- 变更委员会 (CCB) 评审中
    'APPROVED',              -- 批准立项 (授权签发 ECO)
    'REJECTED',              -- 驳回终止
    'CLOSED'                 -- 关联的 ECO 已全闭环后关闭
);

-- ECO 业务状态枚举 (Section 40.4: 必须区分发布与现场完成)
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

-- 候选影响处置决定枚举
CREATE TYPE plm_change.impact_decision_type AS ENUM (
    'MODIFY',                -- 确认受波及且必须升版/新建设计
    'RE_VERIFY',             -- 结构不变，但历史验证失效，需重新计算/试验
    'REVIEW_ONLY',           -- 确认受波及，仅需设计校核，无需修改实体
    'NO_IMPACT'              -- 经专业分析判定无实际影响 (必须填写工程免责依据)
);

-- 物料与实物生效处置方案枚举
CREATE TYPE plm_change.disposition_action_type AS ENUM (
    'SCRAP',                 -- 物理报废 (已加工零部件/在制品)
    'REWORK',                -- 现场按新图纸返修/重新调机
    'USE_UP',                -- 自然消耗过渡 (允许在旧批次中继续装配)
    'AS_IS'                  -- 维持原样 (特批让步使用)
);

-- 1. 工程变更请求表 (ChangeRequest - ECR)
CREATE TABLE plm_change.change_request (
    ecr_id                  BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,
    project_id              BIGINT NOT NULL,
    ecr_number              VARCHAR(128) NOT NULL,
    title                   VARCHAR(255) NOT NULL,
    reason_type             plm_change.change_reason_type NOT NULL,
    problem_description     TEXT NOT NULL,
    proposed_solution       TEXT,
    urgency_level           VARCHAR(32) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, EMERGENCY
    status                  plm_change.ecr_status NOT NULL DEFAULT 'DRAFT',
    source_service_case_id  BIGINT NULL, -- 若由售后维保故障引发，关联 M28 工单
    originator_id           VARCHAR(64) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_ecr_number UNIQUE (tenant_id, ecr_number)
);
COMMENT ON TABLE plm_change.change_request IS 'M22: 变更请求表，界定问题起因与初始诉求';
CREATE INDEX idx_ecr_project ON plm_change.change_request(project_id, status);

-- 2. 变更实施单表 (ChangeOrder - ECO)
CREATE TABLE plm_change.change_order (
    eco_id                  BIGINT PRIMARY KEY,
    ecr_id                  BIGINT NOT NULL REFERENCES plm_change.change_request(ecr_id),
    tenant_id               VARCHAR(64) NOT NULL,
    eco_number              VARCHAR(128) NOT NULL,
    title                   VARCHAR(255) NOT NULL,
    change_category         VARCHAR(64) NOT NULL, -- MAJOR, MINOR, ADMINISTRATIVE
    target_baseline_id      BIGINT NULL REFERENCES plm_govern.baseline(baseline_id), -- 目标受波及基线
    status                  plm_change.eco_status NOT NULL DEFAULT 'DRAFT',
    is_impact_analysis_truncated BOOLEAN NOT NULL DEFAULT FALSE, -- M23 遍历是否因深度/权限被截断
    working_version         BIGINT NOT NULL DEFAULT 1,
    ccb_approval_ticket_id  BIGINT NULL,   -- M24 变更委员会审批凭证
    released_at             TIMESTAMPTZ NULL, -- PLM 批准发布时间
    closed_at               TIMESTAMPTZ NULL, -- 现场实施全闭环时间
    created_by              VARCHAR(64) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_eco_number UNIQUE (tenant_id, eco_number)
);
COMMENT ON TABLE plm_change.change_order IS 'M22: 变更实施单，管理跨学科工程设计更新与现场生效';
CREATE INDEX idx_eco_status ON plm_change.change_order(tenant_id, status);

-- 3. 影响面候选节点表 (ImpactItem - 接收 M23 拓扑推演)
CREATE TABLE plm_change.impact_item (
    impact_item_id          BIGINT PRIMARY KEY,
    eco_id                  BIGINT NOT NULL REFERENCES plm_change.change_order(eco_id) ON DELETE CASCADE,
    candidate_revision_id   BIGINT NOT NULL REFERENCES plm_govern.obj_revision(revision_id),
    object_type_code        VARCHAR(64) NOT NULL, -- Requirement, ModelRelease, PartRevision, VerificationCase 等
    propagation_path        JSONB NOT NULL,       -- M23 追溯路径: [{rel: 'allocatedTo', target: '...'}, ...]
    traversal_depth         INT NOT NULL,
    assigned_discipline     VARCHAR(32) NOT NULL, -- MECHANICAL, ELECTRICAL, CONTROL, SIMULATION
    is_assessed             BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_eco_impact_revision UNIQUE (eco_id, candidate_revision_id)
);
COMMENT ON TABLE plm_change.impact_item IS 'M22: 变更影响候选表，承接 M23 递归计算的有向网络节点';
CREATE INDEX idx_impact_eco_assigned ON plm_change.impact_item(eco_id, assigned_discipline, is_assessed);

-- 4. 专业影响处置决定表 (ImpactDecision - 工程师签字裁定)
CREATE TABLE plm_change.impact_decision (
    decision_id             BIGINT PRIMARY KEY,
    impact_item_id          BIGINT NOT NULL REFERENCES plm_change.impact_item(impact_item_id) ON DELETE CASCADE UNIQUE,
    decision_type           plm_change.impact_decision_type NOT NULL,
    technical_rationale     TEXT NOT NULL,        -- 专业分析依据 (判 NO_IMPACT 时必填)
    action_required         TEXT,                 -- 指派给后继任务的具体工程要求
    target_action_plan      VARCHAR(64) NULL,     -- REVISE_EXISTING (升版), CREATE_NEW (新建件 - ADR-05)
    assessor_id             VARCHAR(64) NOT NULL, -- 专业责任工程师工号
    assessed_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_change.impact_decision IS 'M22: 影响处置裁决表，固化专家决策与免责依据，防止系统误伤或漏判';

-- 5. 变更实施分解任务表 (ChangeTask - 驱动设计与验证工作)
CREATE TABLE plm_change.change_task (
    task_id                 BIGINT PRIMARY KEY,
    eco_id                  BIGINT NOT NULL REFERENCES plm_change.change_order(eco_id) ON DELETE CASCADE,
    task_code               VARCHAR(64) NOT NULL,
    title                   VARCHAR(255) NOT NULL,
    task_type               VARCHAR(64) NOT NULL, -- CAD_REMODEL, SIM_RERUN, REQ_UPDATE, DRAWING_REDRAW
    assignee_id             VARCHAR(64) NOT NULL,
    source_revision_id      BIGINT NULL REFERENCES plm_govern.obj_revision(revision_id),
    target_revision_id      BIGINT NULL REFERENCES plm_govern.obj_revision(revision_id), -- 产生的新修订 DRAFT
    status                  VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED
    completed_at            TIMESTAMPTZ NULL,
    CONSTRAINT uq_eco_task_code UNIQUE (eco_id, task_code)
);

-- 6. 物料、订单与设备现场生效处置规约表 (EffectivityDisposition)
CREATE TABLE plm_change.effectivity_disposition (
    disposition_id          BIGINT PRIMARY KEY,
    eco_id                  BIGINT NOT NULL REFERENCES plm_change.change_order(eco_id) ON DELETE CASCADE,
    target_scope_type       VARCHAR(32) NOT NULL CHECK (target_scope_type IN ('INVENTORY_PART', 'IN_PROCESS_ORDER', 'FIELD_MACHINE')),
    target_part_number      VARCHAR(128) NOT NULL,
    target_order_product_id BIGINT NULL,          -- 针对特定在制订单
    target_individual_id    BIGINT NULL,          -- 针对特定序列号实物设备
    action_type             plm_change.disposition_action_type NOT NULL,
    effective_serial_cutoff VARCHAR(128) NULL,    -- 截止断点序列号 (自该序列号起执行新方案)
    effective_date_cutoff   DATE NULL,
    disposition_instructions TEXT NOT NULL,       -- 现场执行细则
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_change.effectivity_disposition IS 'M22: 现场处置策略表，明确在制品/库存/现场机床的报废/返工规则';

-- 7. 变更现场实施回执跟踪表 (ImplementationRecord - 跨系统对账)
CREATE TABLE plm_change.implementation_record (
    record_id               BIGINT PRIMARY KEY,
    eco_id                  BIGINT NOT NULL REFERENCES plm_change.change_order(eco_id),
    disposition_id          BIGINT NOT NULL REFERENCES plm_change.effectivity_disposition(disposition_id),
    target_system           VARCHAR(64) NOT NULL, -- MES, ERP, FIELD_CRM
    external_receipt_id     BIGINT NULL REFERENCES plm_mfg.external_receipt(receipt_id),
    execution_status        VARCHAR(32) NOT NULL DEFAULT 'DISPATCHED', -- DISPATCHED, IN_EXECUTION, COMPLETED, FAILED
    site_operator_id        VARCHAR(64) NULL,
    completion_evidence_doc BIGINT NULL REFERENCES plm_govern.artifact(artifact_id), -- 返工质检报告/报废单
    confirmed_at            TIMESTAMPTZ NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_change.implementation_record IS 'M22: 现场执行对账表，严格依赖 MES 项级回执核验真实闭环';
CREATE INDEX idx_impl_record_eco ON plm_change.implementation_record(eco_id, execution_status);
```

### 5. 核心完整性触发器与高级业务约束 (Functions & Triggers)

#### 5.1 变更单关闭阻断触发器 (`fn_enforce_eco_close_guard` - AT-22 物理防线)

落实开发说明书核心约束：变更已发布（PLM 批准）与现场实施完成是两个独立状态，严禁在外部回执未全量确认前关闭变更单。  

SQL

```
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

        -- 2. 检查现场生效处置方案是否存在未下发或未对账记录
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

CREATE TRIGGER trg_enforce_eco_close
BEFORE UPDATE ON plm_change.change_order
FOR EACH ROW
EXECUTE FUNCTION plm_change.fn_enforce_eco_close_guard();
```

#### 5.2 影响分析裁决完备性校验函数 (`fn_validate_impact_assessment_readiness`)

在变更委员会（CCB）批准 ECO 并授权派生工作空间前，强校验候选集中无遗漏项：  

SQL

```
CREATE OR REPLACE FUNCTION plm_change.fn_validate_impact_assessment_readiness(p_eco_id BIGINT)
RETURNS TABLE (
    is_ready BOOLEAN,
    unassessed_count INT,
    re_verify_count INT,
    modify_count INT,
    error_message TEXT
) AS $$
DECLARE
    v_unassessed INT := 0;
    v_re_verify INT := 0;
    v_modify INT := 0;
    v_truncated BOOLEAN := FALSE;
BEGIN
    -- 1. 检查 M23 遍历是否曾被截断
    SELECT is_impact_analysis_truncated INTO v_truncated
    FROM plm_change.change_order WHERE eco_id = p_eco_id;

    IF v_truncated IS TRUE THEN
        RETURN QUERY SELECT FALSE, 0, 0, 0, 
            'Impact Assessment Warning: Graph traversal was truncated due to depth limit or access control. Complete re-analysis required before authorization.'::TEXT;
        RETURN;
    END IF;

    -- 2. 统计未裁决候选数
    SELECT COUNT(1) INTO v_unassessed
    FROM plm_change.impact_item
    WHERE eco_id = p_eco_id AND is_assessed IS FALSE;

    IF v_unassessed > 0 THEN
        RETURN QUERY SELECT FALSE, v_unassessed, 0, 0, 
            format('Impact Assessment Incomplete: [%s] candidate items lack engineering disposition decisions.', v_unassessed);
        RETURN;
    END IF;

    -- 3. 统计需要重新验证和修改的对象
    SELECT 
        COUNT(1) FILTER (WHERE id.decision_type = 'RE_VERIFY'),
        COUNT(1) FILTER (WHERE id.decision_type = 'MODIFY')
    INTO v_re_verify, v_modify
    FROM plm_change.impact_item ii
    JOIN plm_change.impact_decision id ON ii.impact_item_id = id.impact_item_id
    WHERE ii.eco_id = p_eco_id;

    RETURN QUERY SELECT TRUE, 0, v_re_verify, v_modify, 'Impact assessment 100% complete and signed.'::TEXT;
END;
$$ LANGUAGE plpgsql STABLE;
```

### 6. 功能特性详细技术规格 (M22-F01 ~ M22-F05)

#### M22-F01：登记变更原因，划定变更初始范围并签发 ChangeRequest

1. **输入来源与问题归集**：
   - 支持通过 M01 工作台直接录入，或由 M28 售后维保工单（`ServiceCase` 现场严重故障）、M11 验证失效报警（`VerificationAssessment` FAIL 状态）一键下钻提报。  
   - 必须强制声明 `reason_type` 与 `urgency_level`，并上传故障描述图谱或缺陷说明。  
2. **初始范围圈定（Initial Problem Scope）**：
   - 变更发起人（Originator）选择发生问题的受控种子节点（如特定需求 `REQ-VMC1000-SPEED-001`、机床主轴部件 `PartRevision`、或数控系统固件包）。  
   - 系统通过 Snowflake 算法签发唯一 `ecr_number`（如 `ECR-2026-0042`），状态置为 `DRAFT`。  
3. **技术初审与 ECR 审批流**：
   - 发起技术可行性预审，经由 M24 路由至相关工程总师；
   - 审批通过后，ECR 跃迁为 `APPROVED` 状态，并自动授予签发关联 ECO 的凭单上下文。  

#### M22-F02：调用 M23 获取候选拓扑路径，各专业负责人录入处置决定 (AT-10 实现)

以 VMC1000 立式加工中心主轴额定转速从 12,000 rpm 提升至 15,000 rpm 为例，系统影响推演与处置时序如下：  

代码段

```
sequenceDiagram
    autonumber
    actor CE as 变更协调人 (Change Coordinator)
    participant M22 as M22 变更服务
    participant M23 as M23 数字主线
    participant M30 as M30 权限服务
    actor EngMech as 机械工程师 (Eng-Mech)
    actor EngSim as 仿真工程师 (Eng-Sim)
    actor EngElec as 电气工程师 (Eng-Elec)

    CE->>M22: 1. 触发候选影响分析: evaluateImpact(ecoId, seedRevisionId)
    M22->>M23: 2. RPC 图拓扑深度遍历: traverseImpactGraph(seedRevisionId, depth=5, configContext)
    Note over M23: M23 沿 allocatedTo, satisfies, verifies,<br/>usesRevision 展开有向图谱，检测环路
    M23->>M30: 3. 跨域节点访问权限脱敏与安全裁剪
    M30-->>M23: 返回脱敏后的拓扑子图
    M23-->>M22: 4. 返回候选集合 (Candidate Set) 及传播路径
    Note over M22: 识别出候选: 主轴轴承(机械), 动态热平衡(仿真),<br/>主轴驱动电机(电气), 定位精度用例(验证)

    M22->>M22: 5. 固化写入 plm_change.impact_item 表
    
    par 跨专业协同处置填报 (M22-F02)
        EngMech->>M22: 6a. 机械裁决: recordDecision(轴承, MODIFY, "现有轴承极限转速不满足，换装陶瓷球轴承")
        EngSim->>M22: 6b. 仿真裁决: recordDecision(热平衡, RE_VERIFY, "需在15000rpm新工况下重算热伸长")
        EngElec->>M22: 6c. 电气裁决: recordDecision(驱动电机, MODIFY, "额定功率需增大至18.5kW")
    end

    CE->>M22: 7. 提交准入预检: preCheckAssessment(ecoId)
    M22->>M22: 8. 执行 fn_validate_impact_assessment_readiness
    M22-->>CE: 校验 100% 通过，进入 CCB 审批授权
```

#### M22-F03：签发 ChangeOrder，授权新版本工程工作空间与实施任务

1. **CCB 委员会评审与授权（Authorization Gate）**：
   - 变更委员会通过 M24 工作流会签，全面审查处置矩阵与成本估算；
   - 签署通过后，ECO 跃迁为 `AUTHORIZED` 状态。  
2. **工作空间自动隔离派生机制（ADR-05 执行）**：
   - 系统扫描所有处置类型为 `MODIFY` 的 `ImpactDecision`：
     - **符合 FFF 互换性**：自动调用 M20，基于被修改对象的当前 `RELEASED` 修订版派生下一代草稿（如 Rev A $\rightarrow$ Rev B 处于 `DRAFT` 状态），并在 `ChangeTask` 中建立绑定关系。  
     - **违反 FFF 互换性**：系统强制阻断原件升版，自动指派新建零件物料编码任务（如新物料 `VMC1000-SP-CERAMIC-001` Rev A）。  
3. **工作任务下发**：
   - 为每个责任人自动创建 `ChangeTask` 并投影到个人工作台（M01-F01）；  
   - 在变更实施期间，工作空间中的修改被锁定在当前 ECO 上下文中，外部普通用户访问该工程对象时仍只能读取历史 `RELEASED` 视图，完全避免半成品技术状态外泄。  

#### M22-F04：跟踪新版本发布与设计闭环验证报告 (ADR-08 落地)

1. **多专业协同迭代跟踪**：
   - 机械工程师在 M16 完成新 EBOM 搭建，CAD 插件完成 3D 拓扑装配签入（M17）；  
   - 系统工程团队在 M04/M06 完成 SysML v2 模型发布包审批（Commit `8fc3a...`）；  
   - 电气与软件团队在 M18 完成 PLC 控制程序固件重新构建与 SHA-256 摘要固化。  
2. **再验证闭环强校验（ADR-08 证据不继承）**：
   - 针对处置决定中标记为 `RE_VERIFY` 的条目，系统**绝不复制历史 PASS 结论**；  
   - 必须通过 M08 触发 OpenModelica 执行新工况（15,000 rpm）的仿真 Job，产生全新的 `SimulationRun` 结果（M10）；  
   - 具备 `VerificationReviewer` 资质的专职工程师在 M11 签署全新的 `VerificationAssessment`（PASS）。  
3. **设计态终结与发布激活（Transition to RELEASED）**：
   - 变更协调人申请关闭工程实施阶段，系统核验：
     1. 所有 `ChangeTask` 状态为 `COMPLETED`；  
     2. 关联的新修订版全部跃迁为 `RELEASED`；  
     3. 强制验证项 100% 具备新的合格证据；  
   - 校验通过后，ECO 跃迁至 `RELEASED`，标志着**设计定义在 PLM 内部完成闭环**。  

#### M22-F05：实物处置方案下发与制造实施闭环 (AT-22 约束落地)

1. **划定在制品、库存与现场实物生效边界（Effectivity Scope）**：
   - 变更工程师编制 `EffectivityDisposition`，精准定义适用范围：
     - **库存毛坯件**：针对旧款钢球轴承库存，执行 `SCRAP`（报废），下发 ERP 财务报废指令。  
     - **车间在制装配**：针对未下线加工中心订单（如 `OPD-1001`），执行 `REWORK`（返工更换主轴箱）。  
     - **现场服役机床**：针对序列号 `MACHINE-1001`，客户购买了高精升级包，执行现场加装与更换；针对 `MACHINE-1002`，维持 `AS_IS`（不升级）。  
2. **向 MES/ERP 组装下发与回执接收**：
   - 通过 M26 组装包含 As-Planned MBOM 差异及实装替换指南的下发包（`HandoffPackage`）；  
   - ECO 状态跃迁为 `EXECUTING`；  
   - 外部系统执行完成后，通过 `POST /api/v1/manufacturing-feedback` 异步回传逐项确认凭证（包含车间返修质检合格单、退库报废流水号）。  
3. **闭环核销终结（Closure Execution）**：
   - 仅当所有 `ImplementationRecord` 均核销为 `COMPLETED` 且现场实装数据通过 M27 校验生效后，变更协调人才被允许执行 `CloseECO` 操作，ECO 正式转为 `CLOSED` 终态。  

### 7. 变更管理状态机规格 (State Machines)

#### 7.1 变更请求状态机 (ECR State Machine)

代码段

```
stateDiagram-v2
    [*] --> DRAFT : 创建变更请求
    DRAFT --> SUBMITTED : 提交初审 (Submit)
    SUBMITTED --> DRAFT : 补充资料退回
    SUBMITTED --> IN_REVIEW : 初审通过，进入CCB会签
    IN_REVIEW --> REJECTED : CCB否决驳回
    IN_REVIEW --> APPROVED : CCB批准立项
    APPROVED --> CLOSED : 关联ECO全部实施闭环
    REJECTED --> [*]
    CLOSED --> [*]
```

#### 7.2 变更实施单两阶段闭环状态机 (ECO State Machine - AT-22 守护)

代码段

```
stateDiagram-v2
    [*] --> DRAFT : 签发ECO
    DRAFT --> ASSESSING : 触发 M23 候选计算
    ASSESSING --> AUTHORIZED : 专业处置完成，CCB授权实施
    AUTHORIZED --> IMPLEMENTING : 派生工作空间，下发设计任务
    IMPLEMENTING --> REVIEWING : 新版本设计完成，提交技术会签
    REVIEWING --> IMPLEMENTING : 会签发现缺陷，打回修改
    REVIEWING --> RELEASED : 会签与再验证通过，设计新版本发布激活
    
    note right of RELEASED
      设计发布终态达成！
      但现场实施尚未完成，
      系统物理阻断直接关闭！
    end note
    
    RELEASED --> EXECUTING : 下发生效方案至 MES/现场
    EXECUTING --> CLOSED : MES/ERP/现场回执 100% 确认核销 (AT-22)
    
    DRAFT --> CANCELLED : 撤销变更
    AUTHORIZED --> CANCELLED : 变更终止
    CLOSED --> [*]
    CANCELLED --> [*]
```

### 8. OpenAPI 3.0 接口契约定义

#### 8.1 触发影响面拓扑分析

- **HTTP 请求**：`POST /api/v1/change-orders/{ecoId}/impact-analysis`

    

- **操作描述**：调用 M23 递归遍历图网络，自动在 `impact_item` 表固化候选清单。  

- **请求载荷 (Request Body)**：

JSON

```
{
  "seedRevisionId": 6019284102901,
  "maxDepth": 5,
  "relationTypes": ["allocatedTo", "satisfies", "verifies", "usesRevision"],
  "contextConfigId": 50192841029
}
```

- **响应报文 (Response 200 OK - 检出候选)**：

JSON

```
{
  "ecoId": 901829018290182,
  "candidateCount": 4,
  "isTruncated": false,
  "impactItems": [
    {
      "impactItemId": 101,
      "candidateRevisionId": 70192841001,
      "objectType": "PartRevision",
      "businessCode": "VMC1000-BEARING-01",
      "discipline": "MECHANICAL",
      "propagationPath": "REQ-VMC1000-SPEED -> SPINDLE_SUBSYS -> VMC1000-BEARING-01"
    },
    {
      "impactItemId": 102,
      "candidateRevisionId": 80192841005,
      "objectType": "VerificationCaseRevision",
      "businessCode": "TC-SPINDLE-THERMAL",
      "discipline": "SIMULATION",
      "propagationPath": "REQ-VMC1000-SPEED -> TC-SPINDLE-THERMAL"
    }
  ]
}
```

#### 8.2 录入专业影响处置决定

- **HTTP 请求**：`POST /api/v1/change-orders/{ecoId}/impact-decisions`

    

- **请求头**：`Idempotency-Key: idemp-decide-eco-2026-0042`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "impactItemId": 101,
  "decisionType": "MODIFY",
  "technicalRationale": "15000 rpm 超出原钢球轴承极限 dmn 值，必须换装角接触陶瓷球轴承组合。",
  "targetActionPlan": "CREATE_NEW",
  "actionRequired": "建立新物料号并完成主轴前端装配位配合公差计算"
}
```

- **响应报文 (Response 201 Created)**：

JSON

```
{
  "decisionId": 7710291823901,
  "impactItemId": 101,
  "status": "RECORDED",
  "assessorId": "ENG-MECH-042",
  "assessedAt": "2026-09-15T11:20:00Z"
}
```

#### 8.3 申请关闭变更单 (AT-22 守卫检验)

- **HTTP 请求**：`POST /api/v1/change-orders/{ecoId}/close`

    

- **响应报文 (Response 422 Unprocessable Entity - 现场未实施拦截报错)**：

JSON

```
{
  "type": "https://plm.company.com/errors/change-execution-pending",
  "title": "ECO Closure Blocked by Constraint CST-M22-01 (AT-22)",
  "status": 422,
  "detail": "ECO Close Rejected: Design is RELEASED, but MES physical execution is not completed. 1 implementation receipt is still DISPATCHED without factory confirmation.",
  "instance": "/api/v1/change-orders/901829018290182/close",
  "errorCode": "ERR_FIELD_EXECUTION_INCOMPLETE",
  "pendingReceipts": [
    {
      "dispositionId": 40192831,
      "targetSystem": "MES",
      "targetOrder": "OPD-1001",
      "status": "DISPATCHED",
      "message": "Shopfloor rework work-order WO-2026-991 has not reported completion receipt."
    }
  ]
}
```

### 9. 领域事件与发件箱架构契约 (Outbox Schema)

M22 在本地数据库事务提交时，严格向 `plm_infra.sys_outbox_event` 写入领域事件，保证消息原子广播至 Kafka：  

#### 事件一：`ChangeOrderReleasedEvent`

- **触发时机**：ECO 内部技术任务与验证全部完成，设计新版本发布激活（`status = 'RELEASED'`）。  
- **Payload 契约**：

JSON

```
{
  "eventId": 992019284102931,
  "eventType": "ChangeOrderReleased",
  "aggregateType": "ChangeOrder",
  "aggregateId": "901829018290182",
  "tenantId": "VMC_ENTERPRISE",
  "payload": {
    "ecoId": 901829018290182,
    "ecoNumber": "ECO-2026-0042",
    "releasedRevisions": [
      { "partNumber": "VMC1000-SP-CERAMIC-001", "revision": "A", "type": "NEW_PART" },
      { "partNumber": "VMC1000-MOTOR-DRIVE", "revision": "B", "type": "REVISED_PART" }
    ],
    "verificationReportRef": 8102948192048,
    "releasedBy": "LEAD-ENG-001",
    "releasedAt": "2026-09-15T15:00:00Z"
  }
}
```

#### 事件二：`ChangeOrderClosedEvent`

- **触发时机**：MES/ERP/现场回执 100% 对账核销，变更单全闭环关闭（`status = 'CLOSED'`）。  
- **Payload 契约**：

JSON

```
{
  "eventId": 992019284102932,
  "eventType": "ChangeOrderClosed",
  "aggregateType": "ChangeOrder",
  "aggregateId": "901829018290182",
  "tenantId": "VMC_ENTERPRISE",
  "payload": {
    "ecoId": 901829018290182,
    "ecoNumber": "ECO-2026-0042",
    "closedAt": "2026-09-20T17:30:00Z",
    "dispositionSummary": {
      "scrappedCount": 12,
      "reworkedOrders": ["OPD-1001"],
      "fieldUpdatedMachines": ["MACHINE-1001"]
    },
    "closedBy": "CHANGE-ADMIN-02"
  }
}
```

### 10. 验收测试矩阵 (Acceptance Testing Matrix)

| **用例编号**  | **上位验收对照  MD+ 1** | **场景与测试步骤**                                           | **预期判定结果 (Pass Criteria)**                             | **验证日志与断言依据  MD+ 1**                                |
| ------------- | ----------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **TC-M22-01** | **AT-10**               | 提高主轴额定转速至 15,000 rpm，调用 `evaluateImpact` 推演波及范围 | 准确识别出轴承、热平衡仿真用例、驱动电机等候选节点，并展示完整关系链 | 候选集中无多余无关图文档，无遗漏核心受波及节点（AT-10）      |
| **TC-M22-02** | **AT-22**               | ECO 设计发布为 `RELEASED`，MES 尚未回传返修质检回执，管理员尝试调用 `closeECO` | 系统触发 `fn_enforce_eco_close_guard` 物理阻断，严禁关闭变更单 | 接口返回 HTTP 422 及 `ERR_FIELD_EXECUTION_INCOMPLETE`（AT-22） |
| **TC-M22-03** | M22-F02                 | 变更影响候选包含 4 项，其中 1 项未完成处置录入，尝试提交 CCB 审批授权 | 准入校验函数 `fn_validate_impact_assessment_readiness` 阻断审批流发起 | 提示必须 100% 完成工程裁决填报方可授权实施                   |
| **TC-M22-04** | **ADR-08**              | 对标记为 `RE_VERIFY` 的验证用例，工程师尝试不运行新仿真，直接复用旧版本的 PASS 证据 | M22/M11 联合拦截，拒绝通过审查，提示必须附带基于当前参数上下文的新证据 | 检查 `VerificationAssessment`，必须绑定新 Run ID，旧证据自动标记待评估 |
| **TC-M22-05** | **ADR-05**              | 变更导致轴承配合尺寸改变（违反 FFF 互换性），工程师尝试在原零件号上派生 Rev B | 系统互换性规则引擎拦截，强制要求申请全新的物料主编码并保留来源关系 | 抛出 `IncompatibleInterchangeabilityException`，阻断盲目升版 |
| **TC-M22-06** | M22-F05                 | MES 回传返修完成回执，ERP 回传报废账务确认，调用 `closeECO`  | 校验全闭环通过，ECO 状态跃迁至 `CLOSED`，同时关联 ECR 自动置为 `CLOSED` | 数据库触发器顺利放行，持久化 `closed_at` 物理审计时间戳      |