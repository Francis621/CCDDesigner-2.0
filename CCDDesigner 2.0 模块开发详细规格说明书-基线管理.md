# CCDDesigner 2.0 模块开发详细规格说明书

## M21: 基线与配置状态 (Baselines and Configuration States)

| **文档属性**    | **内容**                                                     |
| --------------- | ------------------------------------------------------------ |
| **模块编号**    | `M21` (Phase: P1, Type: N)                                   |
| **文档编号**    | `CCD-DEV-SPEC-2.0-M21`                                       |
| **版本 / 状态** | V1.0 / 评审发布稿                                            |
| **主责模块**    | M21 基线与配置状态                                           |
| **协同模块**    | M02 (项目与阶段门)、M03 (需求)、M06 (模型发布)、M07 (参数)、M10 (仿真)、M11 (验证证据)、M14/M15 (配置与订单)、M16 (EBOM)、M19 (图文档)、M20 (生命周期底座)、M22 (工程变更)、M23 (数字主线)、M25/M26 (MBOM与制造下发)、M27/M28 (实物与维保) |
| **上位依据**    | 《CCDDesigner 2.0 产品开发说明书》(`CCD-DEV-SPEC-2.0-001`) §3.1, §4.5 (M21), §7.1, §8.4    《CCDDesigner 2.0 产品功能架构与模块设计说明书》(`CCD-ARCH-FUNC-2.0-001`) §6, §23, §29, §40.4, §41.1 |
| **适用受众**    | 配置管理员、系统架构师、后端核心开发工程师、质量与适航审查专家 |

### 1. 模块定位与核心设计原则

依据上位规范要求，M21 承担复杂数控机床正向设计与交付过程中工程数据集合固化、历史状态复现及全生命周期配置状态演进的法定管理职责：  

1. **快照不可变与抗漂移原则（Immutability & Zero Drift）**：
   - 基线一旦完成审批并跃迁至 `FROZEN` 终态，该基线实体及其关联的成员集合、图拓扑关系快照在物理数据库中永久只读。  
   - 严禁动态跟随“最新版本（Latest）”；后续任何成员对象的升版、修订、失效或废弃，均绝对不允许导致历史基线的内容与拓扑发生静默漂移（AT-03, AT-25）。  
2. **“节点+拓扑关系”全要素协同冻结原则**：
   - 基线不仅冻结工程对象节点本身（如需求 Rev A、模型 Commit C1、零件 Rev B、仿真结果 Run R1），**必须同时冻结节点之间的关键有向关系网络（Trace Links & Structural Links）**。  
   - 严禁仅固化节点 ID 而在基线回溯时动态查询当前可变关系网络，杜绝“因当前关系被解除而导致历史设计上下文解体”的伪基线缺陷。  
3. **依赖闭包严格校验准则（Closure Completeness Rule）**：
   - 基线冻结前必须执行递归深度拓扑展开，强校验候选成员集的闭包完整性。  
   - 闭包准入铁律：**严禁包含处于 `DRAFT` 或 `IN_REVIEW` 的草稿对象；严禁存在下级依赖断链或悬空引用（Dangling References）**。  
   - 针对不可变原始记录（仿真 Run、物理实测 EvidenceRecord），校验其终态有效性与 SHA-256 物理摘要，不强套“工程修订发布（RELEASED）”状态。  
4. **多形态配置状态解耦管理（Multi-As Configuration Paradigm）**：
   - 系统严格支持 `As-Designed`（设计配置）、`As-Planned`（制造计划配置）、`As-Built`（实际制造配置）、`As-Delivered`（实际交付配置）、`As-Maintained`（现场服役配置）五大多形态配置状态引用。  
   - **严禁将五大形态建成为单一直线型状态机**。一个订单的设计基线可以派生出一个或多个工厂的工艺基线，并实例化为多台独立运行的序列号设备实物基线，每类形态独立保留版本分支与差分比对凭证。  
5. **成员引用非独占共享（Non-exclusive Member Reference）**：
   - 基线通过关联中间表组织成员，一个工程修订版可被多个历史基线（如 CDR 基线、订单基线、交付基线）共同引用；禁止将单一 `baseline_id` 写入对象主表作为排他归属。  

### 2. 双向追踪矩阵 (Bidirectional Traceability Matrix)

| **功能编号 / 约束规范**                 | **主责与协同模块  MD+ 1** | **上位架构依据与章节  MD+ 1**                           | **覆盖验收用例  MD+ 1**    | **核心控制逻辑与阻断行为**                                   |
| --------------------------------------- | ------------------------- | ------------------------------------------------------- | -------------------------- | ------------------------------------------------------------ |
| **M21-F01** (基线草稿与候选成员圈定)    | M21, M02, M20             | CCD-DEV-SPEC §4.5    CCD-ARCH-FUNC §29                  | AT-14, AT-15               | 按业务阶段（CDR/PDR/发布）圈定种子节点，支持手动与规则匹配圈定候选集 |
| **M21-F02** (依赖闭包完整性强校验)      | M21, M11, M16, M23        | CCD-DEV-SPEC §4.5, §7.1    CCD-ARCH-FUNC §29, §41.1     | AT-03, AT-15, AT-17        | 递归遍历全要素闭包，发现未发布草稿、断链引用或缺失证据时一票否决冻结申请 |
| **M21-F03** (审批冻结、红线比对与后继)  | M21, M20, M22, M24        | CCD-DEV-SPEC §3.1, §5.3    CCD-ARCH-FUNC §29, §40.4     | AT-14, AT-16, AT-25        | 数据库触发器锁定不可变；固化 SHA-256 闭包摘要；支持两基线全属性全拓扑 Diff |
| **M21-F04** (组织多形态配置状态引用)    | M21, M15, M25, M27, M28   | CCD-DEV-SPEC §2.2, §4.5    CCD-ARCH-FUNC §23, §29       | AT-09, AT-11, AT-12, AT-24 | 分离管理 As-Designed/Planned/Built/Delivered/Maintained；保留时空演化拓扑分支 |
| **CST-M21-01** (基线历史只读防篡改约束) | M21, M30                  | CCD-DEV-SPEC §7.1, ADR-08    CCD-ARCH-FUNC §1(5), §44.2 | AT-03, AT-14, AT-25        | 触发器与 AOP 联合拦截：禁止对 `FROZEN` 状态的基线及其关联行执行 UPDATE/DELETE |
| **CST-M21-02** (关系快照防漂移约束)     | M21, M23                  | CCD-DEV-SPEC §4.5, §7.1    CCD-ARCH-FUNC §29            | AT-25                      | 冻结时深拷贝 M23 端点关系与 BOM 结构行至快照表，隔离生产环境可变关系的扰动 |

### 3. 领域对象模型与 ER 逻辑关系 (Mermaid ERD)

代码段

```
erDiagram
    PROJECT ||--o{ BASELINE : "owns"
    BASELINE ||--|{ BASELINE_MEMBER : "freezes_nodes"
    BASELINE ||--o{ BASELINE_RELATION_SNAPSHOT : "freezes_edges"
    BASELINE ||--o{ BASELINE_DIFF_LOG : "compared_in"
    BASELINE ||--o{ SUCCESSOR_BASELINE_LINK : "predecessor_of"
    BASELINE ||--o{ SUCCESSOR_BASELINE_LINK : "successor_to"
    
    BASELINE_MEMBER }o--|| OBJ_REVISION : "points_to_revision"
    BASELINE_MEMBER }o--o| ARTIFACT : "verifies_checksum"
    
    CONFIGURATION_STATE_REFERENCE ||--|| BASELINE : "anchors_to"
    CONFIGURATION_STATE_REFERENCE }o--o| ORDER_PRODUCT_DEFINITION : "context_for_design"
    CONFIGURATION_STATE_REFERENCE }o--o| MACHINE_INDIVIDUAL : "context_for_physical"
    
    GATE_DECISION }o--|| BASELINE : "evaluates_snapshot"
    DELIVERABLE_SUBMISSION }o--o| BASELINE : "submits_as_deliverable"
```

### 4. 物理数据字典与 PostgreSQL DDL 规范

以下物理 DDL 属于 `plm_baseline` Schema，与 D01 `plm_govern` 治理底座无缝衔接。

SQL

```
-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M21 基线与配置状态
-- 适用环境: PostgreSQL 15+
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_baseline;

-- 基线生命周期状态枚举 (Section 40.4)
CREATE TYPE plm_baseline.baseline_state AS ENUM (
    'DRAFT',           -- 编制中/候选圈定中
    'IN_REVIEW',       -- 闭包校验通过，处于 M24 审批流程中
    'FROZEN',          -- 已冻结生效 (物理级不可变，终态)
    'SUPERSEDED'       -- 已被后继基线替代 (仍保留只读历史与溯源)
);

-- 基线工程目的枚举
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

-- 成员角色枚举
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

-- 1. 基线主表 (Baseline)
CREATE TABLE plm_baseline.baseline (
    baseline_id             BIGINT PRIMARY KEY,
    project_id              BIGINT NOT NULL,
    tenant_id               VARCHAR(64) NOT NULL,
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
    approval_ticket_id      BIGINT NULL,   -- 关联 M24 Flowable 流程凭证
    CONSTRAINT uq_baseline_tenant_code UNIQUE (tenant_id, baseline_code)
);
COMMENT ON TABLE plm_baseline.baseline IS 'M21: 工程配置基线主表，FROZEN 后只读不可篡改';
CREATE INDEX idx_baseline_project_purpose ON plm_baseline.baseline(project_id, purpose, state);

-- 2. 基线成员明细表 (BaselineMember - 固化节点快照)
CREATE TABLE plm_baseline.baseline_member (
    member_id               BIGINT PRIMARY KEY,
    baseline_id             BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id) ON DELETE CASCADE,
    revision_id             BIGINT NOT NULL REFERENCES plm_govern.obj_revision(revision_id) ON DELETE RESTRICT,
    member_role             plm_baseline.member_role NOT NULL,
    object_type_code        VARCHAR(64) NOT NULL, -- PartRevision, ReqRevision, ModelRelease 等
    business_code           VARCHAR(128) NOT NULL,
    revision_label          VARCHAR(32) NOT NULL,
    content_hash            CHAR(64) NOT NULL,    -- 固化加入时该成员版本自身的内容哈希
    artifact_id             BIGINT NULL REFERENCES plm_govern.artifact(artifact_id), -- 附件/源文件摘要引用
    custom_context          JSONB NOT NULL DEFAULT '{}'::jsonb, -- 扩展上下文 (如装配位号、变体槽位)
    added_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_baseline_member UNIQUE (baseline_id, revision_id)
);
COMMENT ON TABLE plm_baseline.baseline_member IS 'M21: 基线成员明细表，物理锁定特定版本的工程对象与内容哈希';
CREATE INDEX idx_member_lookup ON plm_baseline.baseline_member(revision_id, baseline_id);

-- 3. 基线关系拓扑快照表 (BaselineRelationSnapshot - 固化图关系边)
CREATE TABLE plm_baseline.baseline_relation_snapshot (
    snapshot_rel_id         BIGINT PRIMARY KEY,
    baseline_id             BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id) ON DELETE CASCADE,
    source_revision_id      BIGINT NOT NULL REFERENCES plm_govern.obj_revision(revision_id),
    target_revision_id      BIGINT NOT NULL REFERENCES plm_govern.obj_revision(revision_id),
    relation_type_id        VARCHAR(64) NOT NULL, -- satisfies, verifies, allocatedTo, BOM_USAGE 等
    relation_hash           CHAR(64) NOT NULL,    -- 关系属性与端点组合摘要
    structural_context      JSONB NOT NULL DEFAULT '{}'::jsonb, -- 固化使用位置 lineId、数量、位号等
    snapshotted_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_baseline_rel UNIQUE (baseline_id, source_revision_id, target_revision_id, relation_type_id)
);
COMMENT ON TABLE plm_baseline.baseline_relation_snapshot IS 'M21: 基线关系拓扑快照表，冻结跨域追溯与装配关系边，防止历史断链漂移';
CREATE INDEX idx_rel_snapshot_source ON plm_baseline.baseline_relation_snapshot(baseline_id, source_revision_id);
CREATE INDEX idx_rel_snapshot_target ON plm_baseline.baseline_relation_snapshot(baseline_id, target_revision_id);

-- 4. 多形态配置状态引用表 (ConfigurationStateReference)
CREATE TABLE plm_baseline.configuration_state_reference (
    config_ref_id           BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,
    config_state_type       plm_baseline.baseline_purpose NOT NULL,
    baseline_id             BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id) ON DELETE RESTRICT,
    order_product_id        BIGINT NULL, -- 关联 M15 订单产品定义 (针对设计与制造形态)
    individual_id           BIGINT NULL, -- 关联 M27 序列号机床实物 (针对实装/交付/服役形态)
    effective_from          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to            TIMESTAMPTZ NULL,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    notes                   TEXT,
    bound_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    bound_by                VARCHAR(64) NOT NULL,
    CONSTRAINT chk_config_context_target CHECK (
        (order_product_id IS NOT NULL AND individual_id IS NULL) OR
        (order_product_id IS NULL AND individual_id IS NOT NULL) OR
        (order_product_id IS NULL AND individual_id IS NULL)
    )
);
COMMENT ON TABLE plm_baseline.configuration_state_reference IS 'M21: 承接 As-Designed 到 As-Maintained 多形态配置生命周期引用';
CREATE INDEX idx_cfg_state_order ON plm_baseline.configuration_state_reference(order_product_id, config_state_type);
CREATE INDEX idx_cfg_state_indiv ON plm_baseline.configuration_state_reference(individual_id, config_state_type);

-- 5. 基线演进系谱关联表 (SuccessorBaselineLink)
CREATE TABLE plm_baseline.successor_baseline_link (
    link_id                 BIGINT PRIMARY KEY,
    predecessor_baseline_id BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id),
    successor_baseline_id   BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id),
    change_order_id         BIGINT NULL, -- 驱动升版的 M22 ECO 单据
    derivation_reason       TEXT NOT NULL,
    linked_at               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_successor_link UNIQUE (predecessor_baseline_id, successor_baseline_id),
    CONSTRAINT chk_no_self_predecessor CHECK (predecessor_baseline_id != successor_baseline_id)
);
COMMENT ON TABLE plm_baseline.successor_baseline_link IS 'M21: 记录基线间受控替代与版本演化关系';

-- 6. 基线红线差分比对日志表 (BaselineDiffLog - 缓存重算快照)
CREATE TABLE plm_baseline.baseline_diff_log (
    diff_id                 BIGINT PRIMARY KEY,
    base_baseline_id        BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id),
    target_baseline_id      BIGINT NOT NULL REFERENCES plm_baseline.baseline(baseline_id),
    comparison_hash         CHAR(64) NOT NULL,
    diff_summary            JSONB NOT NULL, -- 差异增删改统计与明细树
    performed_by            VARCHAR(64) NOT NULL,
    performed_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_baseline_diff UNIQUE (base_baseline_id, target_baseline_id, comparison_hash)
);
```

### 5. 核心完整性触发器与高级约束机制 (Functions & Triggers)

#### 5.1 冻结基线防篡改物理阻断触发器 (`fn_enforce_baseline_immutability`)

落实 CST-M21-01 规范：基线一旦处于 `FROZEN` 或 `SUPERSEDED` 终态，物理级阻止任何原位修改与删除。  

SQL

```
CREATE OR REPLACE FUNCTION plm_baseline.fn_enforce_baseline_immutability()
RETURNS TRIGGER AS $$
BEGIN
    -- 1. 针对基线主表的保护
    IF TG_TABLE_NAME = 'baseline' THEN
        IF OLD.state IN ('FROZEN', 'SUPERSEDED') THEN
            -- 仅允许状态由 FROZEN 迁移至 SUPERSEDED (建立后继基线时)
            IF NOT (OLD.state = 'FROZEN' AND NEW.state = 'SUPERSEDED') THEN
                RAISE EXCEPTION 'Architecture Security Violation [CST-M21-01]: Baseline [%] is FROZEN and strictly IMMUTABLE. In-place modification is rejected.',
                    OLD.baseline_id USING ERRCODE = '23000';
            END IF;
        END IF;
    END IF;

    -- 2. 针对基线成员表与关系快照表的保护
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

-- 绑定主表拦截
CREATE TRIGGER trg_baseline_prevent_update
BEFORE UPDATE ON plm_baseline.baseline
FOR EACH ROW EXECUTE FUNCTION plm_baseline.fn_enforce_baseline_immutability();

CREATE TRIGGER trg_baseline_prevent_delete
BEFORE DELETE ON plm_baseline.baseline
FOR EACH ROW EXECUTE FUNCTION plm_govern.fn_prevent_physical_delete();

-- 绑定成员表拦截
CREATE TRIGGER trg_member_prevent_tamper
BEFORE UPDATE OR DELETE ON plm_baseline.baseline_member
FOR EACH ROW EXECUTE FUNCTION plm_baseline.fn_enforce_baseline_immutability();

-- 绑定关系快照表拦截
CREATE TRIGGER trg_rel_snapshot_prevent_tamper
BEFORE UPDATE OR DELETE ON plm_baseline.baseline_relation_snapshot
FOR EACH ROW EXECUTE FUNCTION plm_baseline.fn_enforce_baseline_immutability();
```

#### 5.2 闭包状态前置校验函数 (`fn_validate_closure_readiness`)

在状态跃迁为 `IN_REVIEW` 或 `FROZEN` 之前执行物理检测，确保无草稿对象并核验内容哈希。  

SQL

```
CREATE OR REPLACE FUNCTION plm_baseline.fn_validate_closure_readiness(p_baseline_id BIGINT)
RETURNS TABLE (
    is_valid BOOLEAN,
    invalid_reason TEXT,
    unreleased_count INT
) AS $$
DECLARE
    v_unreleased INT := 0;
    v_dangling INT := 0;
BEGIN
    -- 1. 检查是否存在非终态工程对象 (草稿 DRAFT 或未完成评审 IN_REVIEW)
    SELECT COUNT(1) INTO v_unreleased
    FROM plm_baseline.baseline_member bm
    JOIN plm_govern.obj_revision obr ON bm.revision_id = obr.revision_id
    WHERE bm.baseline_id = p_baseline_id
      AND bm.member_role NOT IN ('SIM_RUN_EVIDENCE', 'TEST_REPORT_EVIDENCE')
      AND obr.lifecycle_state != 'RELEASED';

    IF v_unreleased > 0 THEN
        RETURN QUERY SELECT FALSE, 
            format('Closure Validation Failed: %s members are not in RELEASED state.', v_unreleased),
            v_unreleased;
        RETURN;
    END IF;

    -- 2. 检查证据类成员是否存在未终态记录
    IF EXISTS (
        SELECT 1 FROM plm_baseline.baseline_member bm
        JOIN plm_sim.sim_run sr ON bm.revision_id = sr.run_id
        WHERE bm.baseline_id = p_baseline_id
          AND bm.member_role = 'SIM_RUN_EVIDENCE'
          AND sr.run_state NOT IN ('SUCCEEDED', 'FAILED', 'CANCELLED')
    ) THEN
        RETURN QUERY SELECT FALSE, 'Closure Validation Failed: Simulation Run evidence is still executing/pending.', 1;
        RETURN;
    END IF;

    -- 闭包合法
    RETURN QUERY SELECT TRUE, 'Closure integrity fully satisfied.'::TEXT, 0;
END;
$$ LANGUAGE plpgsql STABLE;
```

### 6. 功能特性详细技术规格 (M21-F01 ~ M21-F04)

#### M21-F01：按业务阶段创建基线草稿与候选成员圈定

1. **业务阶段基线策略驱动**：
   - 支持针对系统开发主里程碑按模板创建基线草稿：如概念评审门（PDR / Functional Baseline）、详细工程阶段门（CDR / Product Design Baseline）、工艺下发（As-Planned）及客户交付（As-Delivered）。  
   - 创建基线时，系统基于 Snowflake 算法生成唯一 `baseline_id`，设置初始状态为 `DRAFT`，初始化并发锁 `working_version = 1`。  
2. **多通道候选成员圈定（Candidate Enrollment Channels）**：
   - **通道 A：顶层装配递归展开（EBOM Traversal）**：指定顶层装配件 `PartRevision`，配置算法沿 `plm_product.bom_line` 向下深度优先遍历，抽取当前生效层级的全部子零部组件修订版。  
   - **通道 B：系统模型与接口绑定（MBSE Allocation）**：指定已发布的 `ModelRelease`，自动抽取发布包中固化的 `PublicationManifest` 元数据元素、接口契约（`InterfaceContractRevision`）及架构分配零件。  
   - **通道 C：需求与验证闭环包（V&V Trace Traversal）**：抽取当前项目范围内的需求集合（`RequirementRevision`）、关联的验证用例（`VerificationCaseRevision`）以及 M11 终态验证评估结果（`VerificationAssessment`）。  
   - **通道 D：手动精细选配与补充**：支持配置工程师手动追加出厂图样（`DocumentRevision`）、调试参数集（`ParameterSetRevision`）及专用固件二进制包（`SoftwareReleasePackage`）。  

#### M21-F02：基线依赖闭包完整性拓扑校验（Closure Completeness Engine）

在基线提交审批或冻结前，闭包检查引擎必须顺序执行五道严格的准入检验逻辑（图拓扑求值）：  

代码段

```
flowchart TD
    START([触发闭包完整性检查]) --> STEP1[1. 成员生命周期终态校验]
    STEP1 -->|发现 DRAFT/IN_REVIEW 成员| ERR1[拦截: ERR_UNRELEASED_MEMBER_DETECTED]
    STEP1 -->|全部为 RELEASED / 归档终态| STEP2[2. 物理哈希防篡改复核]
    
    STEP2 -->|当前数据库内容摘要与登记不符| ERR2[拦截: ERR_MEMBER_CONTENT_TAMPERED]
    STEP2 -->|摘要一致| STEP3[3. 多级装配与图文档断链校验]
    
    STEP3 -->|发现子零件或 CAD 关联丢失| ERR3[拦截: ERR_DANGLING_DEPENDENCY_FOUND]
    STEP3 -->|结构依赖完整| STEP4[4. 验证证据与需求覆盖度校验]
    
    STEP4 -->|核心需求缺失 PASS 评估 (AT-15)| ERR4[拦截: ERR_GATE_EVIDENCE_INCOMPLETE]
    STEP4 -->|证据链条完备| STEP5[5. 拓扑关系边闭包归集计算]
    
    STEP5 --> SUCCESS([校验通过: 锁定候选集并生成 ClosureHash])

    style ERR1 fill:#f8d7da,stroke:#721c24
    style ERR2 fill:#f8d7da,stroke:#721c24
    style ERR3 fill:#f8d7da,stroke:#721c24
    style ERR4 fill:#f8d7da,stroke:#721c24
    style SUCCESS fill:#d4edda,stroke:#155724
```

1. **生命周期校验**：非证据类成员必须处于 `RELEASED` 状态；证据类记录必须达到终态（`SUCCEEDED` / `CONFIRMED`）。  

2. **哈希复核**：比对当前成员记录的实际内容摘要与物理 MinIO / Git Commit 哈希，若存在静默修改，一票否决。  

3. **断链探测**：若基线圈定了装配件 A，但装配行中引用的子件 B 未被纳入基线成员集，系统自动将子件 B 提升为必选候选；若子件 B 仍处于草稿状态，判定为断链阻塞。  

4. **关系快照圈定**：在内存中构建包含全部候选成员的诱导子图（Induced Subgraph）：

   $$\text{BaselineEdges} = \{ e = (u, v) \in \text{TraceLinks} \mid u \in \text{Members} \land v \in \text{Members} \}$$

   将这些关联边逐一持久化至 `plm_baseline.baseline_relation_snapshot` 表中，完成关系的物理固化。  

5. **计算闭包全局哈希（ClosureHash）**：

   $$\text{ClosureHash} = \text{SHA-256}\left( \sum_{i} \text{MemberContentHash}_i \parallel \sum_{j} \text{RelationHash}_j \right)$$

   将该摘要固化入 `baseline.closure_hash`，作为全系统的工程完整性根凭据（Merkle Root 概念）。  

#### M21-F03：审批后基线锁定冻结、红线比对与后继变更追溯

1. **原子锁定冻结与发件箱投递（Atomic Freeze & Outbox Event）**：
   - 接收来自 M24 的 `ApprovalDecision` 凭证，切面再次比对候选内容摘要一致性（AT-16）。  
   - 在本地数据库事务中执行状态迁移：`state = 'FROZEN'`，记录 `frozen_by` 与 `frozen_at`。  
   - 触发底层触发器激活，阻断后续任何增删改操作；同时在同一本地事务中向 `plm_infra.sys_outbox_event` 写入 `BaselineFrozenEvent` 事件。  
2. **多基线红线比对引擎（Baseline Redline Diff Engine）**：
   - 支持任意两个同类型或跨阶段基线（如 `Base: BL-CDR-V1.0` 与 `Target: BL-CDR-V2.0`）执行全维度差分比对：  
     - **对象层差分**：新增成员（Added）、移除成员（Removed）、版本更替（Replaced，如 Part 001 Rev A $\rightarrow$ Rev B）。  
     - **参数层差分**：提取绑定的参数集，比对相同参数编码下的设计值、工况值变化及量纲差异。  
     - **拓扑关系层差分**：比对满足关系（satisfies）、分配关系（allocatedTo）的新建与解除，高亮设计意图偏离。  
   - 比对结果以 JSON 树持久化于 `plm_baseline.baseline_diff_log`，供工作台以红绿高亮双侧树形展示（M01 / M16-F03）。  
3. **后继基线衍生与变更关联（Successor Lineage Tracking）**：
   - 当 M22 工程变更单（ECO）发布需要演进基线时，系统以源冻结基线为模版派生新的 `DRAFT` 基线，并在 `successor_baseline_link` 中登记前任与后继关系。  
   - 历史基线在后继基线冻结后标记为 `SUPERSEDED`，但其内部数据完全只读保留，确保溯源链条不断。  

#### M21-F04：多形态配置引用组织 (As-Designed 至 As-Maintained)

为确保装备研发、工艺、制造、交付与服役的配置物理一致性，通过 `ConfigurationStateReference` 组织五大形态演进：  

| **配置形态类别**                    | **业务定义与权威依据**                                | **承载的主要成员与关系类型  MD+ 1**                          | **关联业务实体与上下文**                        |
| ----------------------------------- | ----------------------------------------------------- | ------------------------------------------------------------ | ----------------------------------------------- |
| **`As-Designed`**  (设计配置基线)   | 经详细设计与验证闭环确认的产品工程定义（100% 结构）。 | 需求、SysML 模型 Commit、100% EBOM 行、受控 CAD 制品、验证报告。 | 绑定 `OrderProductDefinition`（订单产品定义）。 |
| **`As-Planned`**  (制造计划基线)    | 面向特定装配车间工艺路线分解的制造结构与资源。        | 制造 MBOM 行、工艺 BOP 规程、工装夹具分配、制造新增辅料。    | 绑定 `OrderProductDefinition` + `PlantCode`。   |
| **`As-Built`**  (出厂实装基线)      | 机床出厂时实际装配的物理部件序列号、初始参数与偏差。  | 关键件序列号履历、调试补偿参数集、制造偏离许可证（Deviation）。 | 绑定具体 `MachineIndividual`（出厂设备）。      |
| **`As-Delivered`**  (交付验收基线)  | 客户现场初验/终验合格时的状态，交付资料的法律依据。   | 激光干涉仪实测曲线、终检验收单、随机随车资料包制品摘要。     | 绑定具体 `MachineIndividual` + `ContractId`。   |
| **`As-Maintained`**  (现场服役基线) | 历经售后维保、零部件更换与固件升级后的现场实际配置。  | 现役备件序列号、维修记录、再检验证据、升级后数控 PLC 固件。  | 绑定具体 `MachineIndividual` + 动态时序区间。   |

*核心解耦铁律*：制造车间因物料短缺发生的代用料记录仅进入该批次的 `As-Built` 快照，绝对禁止反向改写上游已冻结的 `As-Designed` 基线；售后维保的主轴更换仅在 `As-Maintained` 产生新快照，原出厂 `As-Delivered` 记录永久不可修改（AT-12, AT-23）。  

### 7. OpenAPI 3.0 接口契约定义

#### 7.1 创建基线草稿并触发成员自动圈定

- **HTTP 请求**：`POST /api/v1/baselines`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "projectId": 100293810293,
  "baselineCode": "BL-VMC1000-CDR-001",
  "name": "VMC1000加工中心关键设计评审(CDR)基线",
  "purpose": "PRODUCT_DESIGN_BASELINE",
  "description": "锁定系统模型Commit 8fc3a、100% EBOM及进给轴仿真验证闭环证据",
  "enrollmentConfig": {
    "seedEbomRootRevId": 7019284102901,
    "includeSysmlReleaseId": 60192841002,
    "includeRequirementScope": "ALL_VERIFIED",
    "additionalMemberIds": [55019284102, 55019284108]
  }
}
```

- **响应报文 (Response 201 Created)**：

JSON

```
{
  "baselineId": 8102948192048,
  "baselineCode": "BL-VMC1000-CDR-001",
  "state": "DRAFT",
  "totalMembersEnrolled": 482,
  "workingVersion": 1,
  "createdAt": "2026-09-15T16:00:00Z"
}
```

#### 7.2 执行闭包完整性预检 (Pre-Freeze Closure Check)

- **HTTP 请求**：`POST /api/v1/baselines/{baselineId}/closure-check`
- **响应报文 (Response 200 OK - 检出未发布阻断)**：

JSON

```
{
  "baselineId": 8102948192048,
  "isClosureComplete": false,
  "blockerCount": 2,
  "closureHash": null,
  "validationErrors": [
    {
      "errorCode": "ERR_UNRELEASED_MEMBER_DETECTED",
      "objectId": 7019284102988,
      "objectType": "PartRevision",
      "businessCode": "VMC1000-SP-BRG-04",
      "currentState": "IN_REVIEW",
      "detail": "轴承配置子件仍处于评审流程中，基线禁止包含草稿或未发布修订版。"
    },
    {
      "errorCode": "ERR_DANGLING_DEPENDENCY_FOUND",
      "objectId": 60192841002,
      "objectType": "ModelRelease",
      "detail": "引用的 SysML 模型端口契约 EXT-BUS-CAN 未在成员集合中登记对应接口版本。"
    }
  ]
}
```

#### 7.3 提交基线锁定冻结请求 (Freeze Execution)

- **HTTP 请求**：`POST /api/v1/baselines/{baselineId}/freeze`

    

- **请求头**：`Idempotency-Key: freeze-bl-vmc1000-cdr-001-req`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "approvalTicketId": 99201488102,
  "verifyClosureHash": true,
  "freezeNotes": "CDR 评审委员会全员签署通过，正式冻结全系统设计定义"
}
```

- **响应报文 (Response 200 OK - 冻结成功)**：

JSON

```
{
  "baselineId": 8102948192048,
  "baselineCode": "BL-VMC1000-CDR-001",
  "state": "FROZEN",
  "closureHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "frozenBy": "CFG-MGR-002",
  "frozenAt": "2026-09-15T16:30:12.891Z",
  "totalMembersLocked": 482,
  "totalRelationsSnapshotted": 1290
}
```

#### 7.4 两基线多维红线比对 (Redline Diff)

- **HTTP 请求**：`GET /api/v1/baselines/{baselineId}/diff?targetBaselineId=8102948192099`
- **响应报文 (Response 200 OK)**：

JSON

```
{
  "baseBaselineId": 8102948192048,
  "targetBaselineId": 8102948192099,
  "diffSummary": {
    "addedMembersCount": 3,
    "removedMembersCount": 0,
    "replacedRevisionsCount": 2,
    "relationChangesCount": 4
  },
  "differences": [
    {
      "category": "REPLACED_REVISION",
      "masterBusinessCode": "PART-SPINDLE-MOTOR",
      "baseRevision": "REV-A",
      "targetRevision": "REV-B",
      "reason": "ECO-2026-089: 提升主轴额定扭矩，更换为高功率驱动电机"
    },
    {
      "category": "RELATION_MODIFIED",
      "relationType": "verifies",
      "sourceCode": "TC-SPINDLE-DYNAMIC",
      "targetCode": "REQ-VMC1000-SPEED-001",
      "changeType": "RE-LINKED",
      "detail": "验证用例版本由 Rev A.1 替换为 Rev A.2，支撑高转速工况"
    }
  ]
}
```

### 8. 领域事件与发件箱架构契约 (Outbox Schema)

M21 业务事务提交时，严格在本地事务中向 `plm_infra.sys_outbox_event` 写入事件，确保消息可靠派发至 Kafka：  

#### 事件一：`BaselineFrozenEvent`

- **触发时机**：基线完成闭包强校验与 M24 审批后物理冻结。  
- **Payload 契约**：

JSON

```
{
  "eventId": 9812739109901,
  "eventType": "BaselineFrozen",
  "aggregateType": "Baseline",
  "aggregateId": "8102948192048",
  "tenantId": "VMC_ENTERPRISE",
  "payload": {
    "baselineId": 8102948192048,
    "baselineCode": "BL-VMC1000-CDR-001",
    "projectId": 100293810293,
    "purpose": "PRODUCT_DESIGN_BASELINE",
    "closureHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "totalMembers": 482,
    "frozenBy": "CFG-MGR-002",
    "frozenAt": "2026-09-15T16:30:12.891Z"
  }
}
```

#### 事件二：`ConfigurationStateBoundEvent`

- **触发时机**：将特定基线与订单产品定义或序列号机床建立多形态状态绑定。  
- **Payload 契约**：

JSON

```
{
  "eventId": 9812739109902,
  "eventType": "ConfigurationStateBound",
  "aggregateType": "ConfigurationStateReference",
  "aggregateId": "9102830192831",
  "tenantId": "VMC_ENTERPRISE",
  "payload": {
    "configRefId": 9102830192831,
    "baselineId": 8102948192048,
    "configStateType": "AS_DESIGNED",
    "orderProductId": 50192841029,
    "individualId": null,
    "boundBy": "ORDER-ENG-102"
  }
}
```

### 9. 验收测试矩阵 (Acceptance Testing Matrix)

| **用例编号**  | **上位验收对照  MD+ 1** | **场景与测试步骤**                                           | **预期判定结果 (Pass Criteria)**                             | **验证日志与断言依据  MD+ 1**                                |
| ------------- | ----------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **TC-M21-01** | **CST-M21-01**          | 对已处于 `FROZEN` 状态的基线执行 SQL `UPDATE baseline SET name = 'X'` 或尝试删除成员 | 数据库触发器 `fn_enforce_baseline_immutability` 拦截，事务回滚 | 抛出 SQL 异常代码 `23000`，确认基线物理只读                  |
| **TC-M21-02** | **AT-03**               | 圈定 SysML 元素并冻结基线，随后在工作区分支重命名/移动该元素并提交新 Commit | 历史基线仍稳定解析出原 Commit 及原元素，名称及属性零漂移     | 基线明细查询验证 `content_hash` 及 `commitId` 维持原值（AT-03） |
| **TC-M21-03** | **AT-14**               | 在生产系统加载三年前归档的历史设计基线，执行全闭包结构与关系恢复 | 准确恢复当时的系统模型、BOM 结构行、CAD 图纸、关系网络及仿真证据 | 全闭包哈希重算结果与数据库 `closure_hash` 逐位一致，依赖无漂移（AT-14） |
| **TC-M21-04** | **AT-15**               | 构造包含未通过验证证据（`INCONCLUSIVE`）或存在 `DRAFT` 零件的基线申请冻结 | 闭包检查器 `fn_validate_closure_readiness` 报错阻断，拒绝进入审批流 | 接口返回 HTTP 422 及 `ERR_UNRELEASED_MEMBER_DETECTED`（AT-15） |
| **TC-M21-05** | **AT-25**               | 基线冻结后，管理员在 M23 数字主线服务中直接修改或解除了上游的 `satisfies` 关系 | 查询该历史基线时，系统基于 `baseline_relation_snapshot` 还原历史拓扑，关系依然存在 | 验证历史关系边未随当前最新图表被污染，关系零漂移（AT-25）    |
| **TC-M21-06** | **M21-F04**             | 车间回传实装数据记录代用轴承（生成 As-Built 配置），随后查询原订单设计基线 | `As-Designed` 仍展示原始设计轴承物料；`As-Built` 准确展示实际安装序列号件 | 验证五大形态彼此解耦，生产实装代用绝不污染设计基线           |