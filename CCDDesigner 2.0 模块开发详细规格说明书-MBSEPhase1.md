# CCDDesigner 2.0 模块开发详细规格说明书

## M04: MBSE 建模工作区 (MBSE Modeling Workspace)

| **文档属性**    | **内容**                                                     |
| --------------- | ------------------------------------------------------------ |
| **模块编号**    | `M04` (Phase: P0/P1, Type: N＋I)                             |
| **文档编号**    | `CCD-DEV-SPEC-2.0-M04`                                       |
| **版本 / 状态** | V1.0 / 评审发布稿                                            |
| **主责模块**    | M04 MBSE 建模工作区                                          |
| **协同模块**    | M01 (统一工作台)、M02 (项目与阶段门)、M03 (需求与规格)、M05 (接口与架构映射)、M06 (模型发布与兼容性)、M11 (验证与证据)、M19 (制品服务)、M20 (生命周期底座)、M21 (基线服务)、M23 (数字主线)、M30 (身份与集成运维) |
| **上位依据**    | 《CCDDesigner 2.0 产品说明书》§10, §12, §13, §14    《CCDDesigner 2.0 产品功能架构与模块设计说明书》§12, §39.1, §41.1    《CCDDesigner 2.0 产品开发说明书》§4.2 (M04), §5.1, §8.4    《Phase 1：SysML v2基础集成开发说明书》§1~§93 |
| **适用受众**    | 系统工程师、MBSE 集成架构师、后端核心开发人员、Web 前端开发工程师、验证与测试工程师 |

### 1. 模块定位与核心设计原则

依据 CCDDesigner 2.0 总体架构与 Phase 1 基础集成规划，M04 承担连接工程师建模工具环境（SysON/OpenSysML）与 PLM 工程对象体系的枢纽职责。其核心目标在于解决：工程师在建模环境中的工作模型（Working Model），如何受控、可靠且无损地转化为 PLM 中具备独立身份、可追踪、可校验并可向发布协调服务（M06）移交的数字资产。  

本模块必须落实以下核心设计原则：

1. **四态彻底解耦原则（Fundamental State Separation）**： 系统严格区分并固化以下四个概念，禁止混淆：  

   $$\text{Working Model} \ne \text{Published Model} \ne \text{PLM Revision} \ne \text{Baseline} \text{[cite: 1]}$$

   - **Working Model**：SysON 中工程师正在频繁增删改查、允许中间状态不一致的受控工作副本。  
   - **Published Model**：经验证通过并经 M06 写入 Flexo 模型服务的只读受控快照（固定 Commit）。  
   - **PLM Revision**：CCDDesigner 赋予的正式工程修订版本（如 Rev A），挂接企业研制生命周期状态机。  
   - **Baseline**：在特定评审节点（如 CDR）冻结的模型与跨学科工程对象的不可变闭包。  
   - *铁律*：SysON 负责“图形编辑”，OpenSysML 负责“语义验证”，Flexo 负责“模型仓库”，CCDDesigner 负责“工程治理”；严禁使用 SysON 分支（Branch）或提交记录直接等同于 PLM Revision。  

2. **单主编辑通道与并发锁控原则（Single Primary Authoring Channel）**：

   - 每个建模工作区必须显式声明其主要编辑通道：**图形通道（`GRAPHICAL`，基于 SysON）** 或 **文本通道（`TEXTUAL`，基于 OpenSysML 语言服务）**。  
   - 在双向图文无损往返未取得正式准入认证前，系统严格禁止多通道在同一工作区无锁并发覆盖编辑；跨通道修改必须作为受控导入或差异提案处理。  

3. **两阶段绑定与数字主线防污染原则（Two-Stage Decoupled Binding）**：

   - 区分 **工作期动态绑定（`WorkingElementBinding`）** 与 **发布期受控引用（`ModelElementReference` + `SemanticBinding`）**。  
   - 工程师在 SysON 中频繁创建、删除、重命名元素期间，PLM 仅在工作区上下文内维系临时工作映射；只有在正式发布时，才将经过挑选的关键系统工程元素抽取并固化为不可变数字主线端点，防止草稿期的无效震荡污染正式数字主线。  

4. **快照一致性与哈希防篡改防护（Snapshot Consistency & Immutability）**：

   - 快照捕获必须满足事务一致性边界，基于原子导出机制生成包含全局哈希（`source_checksum`）与单次不可变快照令牌（`snapshot_token`）的物理包。  
   - **前置校验防篡改锁（PUB-04）**：若模型自上次验证（Validation）之后发生任何文件或元数据变动（`ValidationChecksum != CurrentChecksum`），系统直接拦截发布提交并强制要求重新校验。  

5. **MBSE Gateway SPI 架构抽象隔离**：

   - PLM Core 严禁直接调用 SysON REST API、OpenSysML Language Server 或 Flexo Client 的底层私有 SDK。  
   - 统一通过 MBSE Gateway 提供的标准化 SPI 契约（`ModelAuthoringAdapter`、`ModelValidationAdapter`）进行多工具解耦。  

### 2. 双向追踪矩阵 (Bidirectional Traceability Matrix)

| **功能编号 / 开发约束**               | **主责与协同模块  MD+ 1** | **上位架构依据与章节  MD+ 2**                    | **覆盖验收用例  MD+ 2** | **核心控制逻辑与阻断行为**                                   |
| ------------------------------------- | ------------------------- | ------------------------------------------------ | ----------------------- | ------------------------------------------------------------ |
| **M04-F01** (模型项目与 PLM 绑定)     | M04, M02, M30             | CCD-DEV-SPEC §4.2    SysML-SPEC §17, §47         | DoD-02, TC-01           | 建立 `SystemModelProject` 与 PLM 项目映射；分配独立 Snowflake ID 与工作区令牌。 |
| **M04-F02** (行业建模模板库加载)      | M04, M03, M05             | CCD-DEV-SPEC §4.2, ADR-01    CCD-ARCH-FUNC §12   | AT-01, DoD-03           | 预置数控机床结构、行为、接口参考库；锁定外部依赖库精确版本，严禁拉取 latest 镜像。 |
| **M04-F03** (编辑上下文与视口集成)    | M04, M01, M30             | CCD-DEV-SPEC §4.2    SysML-SPEC §26, §63         | AT-13, DoD-03           | 嵌入 SysON Web 视口，传递受控 SSO 票据；执行编辑锁控，非责任人强制只读模式。 |
| **M04-F04** (集成 OpenSysML 语义诊断) | M04, M06                  | CCD-DEV-SPEC §4.2, ADR-02    SysML-SPEC §30, §59 | AT-02, DoD-05, TC-04    | 驱动模型文本导出并分派至 OpenSysML 容器；返回语法/语义错误报告及精确代码定位。 |
| **M04-F05** (一致性快照捕获与交接)    | M04, M06, M19             | CCD-DEV-SPEC §4.2, §5.1    SysML-SPEC §34~§37    | AT-01, AT-04, DoD-06    | 冻结模型导出制品，核算 SHA-256 摘要；生成 `CandidateSnapshot` 并向 M06 发起发布作业。 |
| **CST-M04-01** (单编辑主通道强制)     | M04                       | CCD-DEV-SPEC §2.2, §4.2    CCD-ARCH-FUNC §12     | AT-02                   | 工作区声明主通道；文本与图形通道切换时强制版本检入与一致性审查。 |
| **CST-M04-02** (变更防篡改验证锁)     | M04, M06                  | SysML-SPEC §34, §68    CCD-DEV-SPEC §5.1         | AT-15, TC-15            | 校验通过后若模型哈希改变，一票否决发布申请并报错：“Model changed since validation”。 |

### 3. 领域对象模型与 ER 物理字典 (PostgreSQL DDL 规范)

以下 DDL 属于 `plm_syseng` 独立业务 Schema，承载系统工程工作区上下文与诊断元数据：  

SQL

```
-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M04 MBSE 建模工作区
-- 适用环境: PostgreSQL 15+ (无多租户边界，单企业集中部署)
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_syseng;

-- 编辑通道模式枚举
CREATE TYPE plm_syseng.authoring_channel AS ENUM (
    'GRAPHICAL',    -- 图形化通道 (以 SysON 为主写源)
    'TEXTUAL'       -- 形式化文本通道 (以 OpenSysML/Text 为主写源)
);

-- 验证状态枚举
CREATE TYPE plm_syseng.validation_status_type AS ENUM (
    'PASSED',               -- 语法与语义完全通过
    'PASSED_WITH_WARNING',  -- 包含非阻塞警告
    'FAILED'                -- 存在语法错误或未解析依赖
);

-- 快照状态枚举
CREATE TYPE plm_syseng.snapshot_status AS ENUM (
    'CAPTURING',    -- 快照生成与哈希计算中
    'STAGED',       -- 已就绪并冻结
    'HANDED_OVER',  -- 已移交 M06 发布流水线
    'EXPIRED',      -- 已被后继快照覆盖或超时失效
    'INVALIDATED'   -- 因校验后改动而作废
);

-- 1. 系统模型主对象表 (SystemModelProject)
CREATE TABLE plm_syseng.system_model_project (
    model_project_id        BIGINT PRIMARY KEY,
    project_id              BIGINT NOT NULL REFERENCES plm_project.project(project_id) ON DELETE RESTRICT,
    model_code              VARCHAR(128) NOT NULL UNIQUE,
    name                    VARCHAR(255) NOT NULL,
    repository_type         VARCHAR(32) NOT NULL DEFAULT 'FLEXO',
    model_language          VARCHAR(32) NOT NULL DEFAULT 'SYSML_V2',
    default_branch          VARCHAR(128) NOT NULL DEFAULT 'main',
    current_revision_id     BIGINT NULL, -- 当前活动的 PLM Revision
    created_by              VARCHAR(64) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_syseng.system_model_project IS 'M04/M06: 系统模型工程项目主表，维护模型业务身份';
CREATE INDEX idx_smp_project ON plm_syseng.system_model_project(project_id);

-- 2. 建模工作区与上下文绑定表 (WorkspaceBinding)
CREATE TABLE plm_syseng.sysml_workspace_binding (
    workspace_id            BIGINT PRIMARY KEY,
    model_project_id        BIGINT NOT NULL REFERENCES plm_syseng.system_model_project(model_project_id) ON DELETE CASCADE,
    external_tool_name      VARCHAR(64) NOT NULL, -- SYSON, OPENSYSML
    external_tool_version   VARCHAR(64) NOT NULL,
    external_project_id     VARCHAR(128) NOT NULL, -- SysON 内部分配的项目 UUID
    primary_channel         plm_syseng.authoring_channel NOT NULL DEFAULT 'GRAPHICAL',
    active_session_token    VARCHAR(128) NULL,    -- 当前活跃编辑锁令牌
    locked_by_user_id       VARCHAR(64) NULL,     -- 排他编辑持有者工号
    locked_at               TIMESTAMPTZ NULL,
    compatibility_profile_id VARCHAR(64) NOT NULL, -- 准入的语法特性配置规范
    status                  VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, CLOSED
    working_version         BIGINT NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_workspace_tool_proj UNIQUE (external_tool_name, external_project_id)
);
COMMENT ON TABLE plm_syseng.sysml_workspace_binding IS 'M04: 外部建模环境与 PLM 系统模型工作区的绑定会话表';
CREATE INDEX idx_workspace_model_proj ON plm_syseng.sysml_workspace_binding(model_project_id);

-- 3. 语义与语法诊断结果表 (DiagnosticReport / ModelValidationResult)
CREATE TABLE plm_syseng.model_validation_result (
    validation_id           BIGINT PRIMARY KEY,
    workspace_id            BIGINT NOT NULL REFERENCES plm_syseng.sysml_workspace_binding(workspace_id) ON DELETE CASCADE,
    source_checksum         CHAR(64) NOT NULL,    -- 验证时的模型文本快照 SHA-256
    validation_engine       VARCHAR(64) NOT NULL, -- OpenSysML Validation Service
    engine_version          VARCHAR(32) NOT NULL,
    status                  plm_syseng.validation_status_type NOT NULL,
    error_count             INT NOT NULL DEFAULT 0,
    warning_count           INT NOT NULL DEFAULT 0,
    report_artifact_id      BIGINT NULL,          -- 完整的日志或诊断 JSON 制品引用 (M19)
    started_at              TIMESTAMPTZ NOT NULL,
    completed_at            TIMESTAMPTZ NOT NULL
);
COMMENT ON TABLE plm_syseng.model_validation_result IS 'M04: OpenSysML 语义验证作业结果主表';
CREATE INDEX idx_val_workspace_hash ON plm_syseng.model_validation_result(workspace_id, source_checksum);

-- 4. 诊断条目明细表 (DiagnosticItem)
CREATE TABLE plm_syseng.validation_diagnostic_item (
    diagnostic_item_id      BIGINT PRIMARY KEY,
    validation_id           BIGINT NOT NULL REFERENCES plm_syseng.model_validation_result(validation_id) ON DELETE CASCADE,
    severity                VARCHAR(16) NOT NULL CHECK (severity IN ('ERROR', 'WARNING', 'INFO')),
    error_code              VARCHAR(64) NOT NULL, -- 例如: SYSML-UNRESOLVED-REF, SYSML-TYPE-MISMATCH
    message                 TEXT NOT NULL,
    element_id              VARCHAR(256) NULL,    -- 模型内部元素 ID
    qualified_name          TEXT NULL,            -- 结构限定名 (如: VMC1000::FeedSystem::XAxis)
    source_location         VARCHAR(128) NULL,    -- 文件行号位置 (例如: line 42, col 15)
    recommendation          TEXT NULL
);
COMMENT ON TABLE plm_syseng.validation_diagnostic_item IS 'M04: 模型校验违规条目表，支持在界面精确定位高亮';
CREATE INDEX idx_diag_item_val ON plm_syseng.validation_diagnostic_item(validation_id, severity);

-- 5. 一致性工作模型候选快照表 (CandidateSnapshot)
CREATE TABLE plm_syseng.candidate_snapshot (
    snapshot_id             BIGINT PRIMARY KEY,
    workspace_id            BIGINT NOT NULL REFERENCES plm_syseng.sysml_workspace_binding(workspace_id),
    snapshot_token          VARCHAR(128) NOT NULL UNIQUE, -- 防重放与防篡改单次有效令牌
    source_checksum         CHAR(64) NOT NULL,            -- 全模型 SysML 文本 SHA-256 摘要
    validation_id           BIGINT NOT NULL REFERENCES plm_syseng.model_validation_result(validation_id),
    raw_archive_artifact_id BIGINT NOT NULL,              -- 固化存储于 MinIO 的原始工程归档 ZIP (M19)
    element_count           INT NOT NULL,
    status                  plm_syseng.snapshot_status NOT NULL DEFAULT 'STAGED',
    captured_by             VARCHAR(64) NOT NULL,
    captured_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handed_over_at          TIMESTAMPTZ NULL
);
COMMENT ON TABLE plm_syseng.candidate_snapshot IS 'M04: 捕获的不可变模型候选快照，用于向 M06 移交发布';
CREATE INDEX idx_snap_workspace ON plm_syseng.candidate_snapshot(workspace_id, status);

-- 6. 工作期元素临时动态绑定表 (WorkingElementBinding)
CREATE TABLE plm_syseng.working_element_binding (
    binding_id              BIGINT PRIMARY KEY,
    workspace_id            BIGINT NOT NULL REFERENCES plm_syseng.sysml_workspace_binding(workspace_id) ON DELETE CASCADE,
    plm_object_type         VARCHAR(64) NOT NULL, -- RequirementRevision, PartRevision 等
    plm_object_id           BIGINT NOT NULL,
    syson_element_id        VARCHAR(256) NOT NULL,
    element_type            VARCHAR(64) NOT NULL, -- RequirementUsage, PartUsage 等
    qualified_name          TEXT NOT NULL,
    last_synced_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_working_binding UNIQUE (workspace_id, plm_object_id, syson_element_id)
);
COMMENT ON TABLE plm_syseng.working_element_binding IS 'M04: 编辑草稿期间的临时语义映射，发布后转为 M06/M23 受控链接';
```

### 4. 核心完整性触发器与业务控制规则 (Functions & Triggers)

#### 4.1 发布前校验哈希零漂移触发器 (`fn_enforce_snapshot_checksum_match`)

落实开发说明书 PUB-04 准入铁律：向 M06 移交的快照内容摘要，必须与 OpenSysML 实际执行诊断通过的摘要完全逐位一致，杜绝“验证通过后又改动模型再提交发布”的安全漏洞。  

SQL

```
CREATE OR REPLACE FUNCTION plm_syseng.fn_enforce_snapshot_checksum_match()
RETURNS TRIGGER AS $$
DECLARE
    v_val_status plm_syseng.validation_status_type;
    v_val_checksum CHAR(64);
BEGIN
    -- 提取关联的验证结果信息
    SELECT status, source_checksum 
    INTO v_val_status, v_val_checksum
    FROM plm_syseng.model_validation_result
    WHERE validation_id = NEW.validation_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Validation Integrity Error: Target validation record [%] does not exist.', 
            NEW.validation_id USING ERRCODE = '23000';
    END IF;

    -- 1. 验证状态检查 (必须为 PASSED 或 PASSED_WITH_WARNING)
    IF v_val_status = 'FAILED' THEN
        RAISE EXCEPTION 'Publish Gate Blocked (PUB-03): Cannot capture candidate snapshot from a FAILED validation.' 
            USING ERRCODE = '23000';
    END IF;

    -- 2. 核心摘要强校验 (PUB-04 防篡改)
    IF v_val_checksum != NEW.source_checksum THEN
        RAISE EXCEPTION 'Tampering Detected (PUB-04): Model has been modified since last validation! Snapshot checksum [%] != Validated checksum [%]. Validate again.',
            NEW.source_checksum, v_val_checksum USING ERRCODE = '23000';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_snapshot_checksum_guard
BEFORE INSERT ON plm_syseng.candidate_snapshot
FOR EACH ROW
EXECUTE FUNCTION plm_syseng.fn_enforce_snapshot_checksum_match();
```

#### 4.2 单主编辑通道并发排他锁控制触发器 (`fn_guard_workspace_concurrency`)

落实单一主要编辑通道控制原则：当工程师在图形通道签出并获取会话锁时，阻断外部其他客户端在未经授权状态下并发提交文本变更。  

SQL

```
CREATE OR REPLACE FUNCTION plm_syseng.fn_guard_workspace_concurrency()
RETURNS TRIGGER AS $$
BEGIN
    -- 检查主通道模式是否被非法篡改
    IF OLD.primary_channel != NEW.primary_channel THEN
        -- 通道切换必须确保当前无活跃签出锁
        IF OLD.active_session_token IS NOT NULL THEN
            RAISE EXCEPTION 'Authoring Channel Conflict: Cannot switch authoring channel while an active session lock [%] is held by user [%]. Release lock first.',
                OLD.active_session_token, OLD.locked_by_user_id USING ERRCODE = '55P03';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_workspace_channel_guard
BEFORE UPDATE ON plm_syseng.sysml_workspace_binding
FOR EACH ROW
EXECUTE FUNCTION plm_syseng.fn_guard_workspace_concurrency();
```

### 5. 功能特性详细技术规格 (M04-F01 ~ M04-F05)

#### M04-F01：模型工程与 PLM 项目绑定配置

1. **项目关系建立**：
   - 在 PLM 中立项（M02）后，系统工程师在工作台选择“新建系统模型”。系统在 `plm_syseng.system_model_project` 生成唯一身份，并通过 MBSE Gateway 调用 SysON REST 端点 `/api/rest/projects` 创建对应的底层图形工程。  
   - 物理绑定持久化在 `sysml_workspace_binding`，登记 `external_project_id`（SysON UUID）与默认分支（`main`）。  
2. **SSO 鉴权与安全凭据委托**：
   - 前端不直接向 SysON 传递数据库直连账密。  
   - 用户打开工作区时，系统通过 M30 颁发包含该用户的上下文安全令牌（Signed JWT），SysON 通过 OIDC 回调验证该用户身份，并依据 PLM 项目成员权限（`ProjectMembership`）赋予对应只读/编辑角色。  

#### M04-F02：数控机床行业专用建模模板与标准库加载

1. **行业模板结构体系（VMC1000 参考架构）**： 系统在初始化工作区时，自动注入符合数控机床正向设计规范的包（Package）骨架结构：  

   Plaintext

   ```
   VMC1000_SystemModel/
   ├── 01_Requirements/        // 映射 M03 需求的 RequirementDefinition & Usage
   ├── 02_FunctionalBehavior/  // 功能动作、分解与状态机 (Actions, States)
   ├── 03_LogicalArchitecture/ // 逻辑架构分配与跨专业接口 (Logical Parts, Interfaces)
   ├── 04_PhysicalArchitecture/ // 对应机床实体的装配结构 (FeedSystem, Spindle, Frame)
   ├── 05_Interfaces/          // 机械法兰、总线、电气端子契约
   ├── 06_Parameters/          // 映射 M07 受控工程参数的定义与取值
   └── 07_VerificationContext/ // 验证工况、约束与分析上下文
   ```

2. **外部标准库准入与锁定（ADR-01 约束）**：

   - 严格锁定 SysML v2 Standard Library 及数控机床专用领域库的精确发行版本号；  
   - 配置文件在 `compatibility_profile_id` 中锁定（如 `SYSML_V2_STD_LIB_V2026_09`），严禁使用动态拉取 latest 依赖的策略，杜绝因标准库更新导致的历史解析漂移。  

#### M04-F03：编辑上下文维护与 SysON 视口集成

1. **前端视口多栏协同交互**：
   - 采用微前端/受控 iframe 容器在 Web 工作台内嵌 SysON 图形编辑器，消除多窗口割裂感。  
   - 支持从 PLM 左侧“受控需求树”拖拽需求条目（如 `REQ-X-001`）至 SysON 画布，触发 MBSE Gateway 调用 `createElement()`，自动在 SysML 模型中实例化为 `RequirementUsage`，并在 `working_element_binding` 登记临时映射。  
2. **排他编辑锁（Checkout / Session Lock）**：
   - 用户在图形通道执行实质性建模前，必须向后端申请编辑会话锁，系统签发 `active_session_token`，并在工作区标记当前锁持有者；  
   - 其他同项目人员进入该工作区时，系统默认激活只读浏览视图，顶栏高亮提示“工程师 [李工] 正在编辑中，当前处于锁定保护模式”，彻底阻断无锁并发覆盖。  

#### M04-F04：集成 OpenSysML 语法与语义诊断

1. **模型文本导出与交换管道**：
   - 用户在工作台点击 **[模型诊断 (Validate)]**，系统触发异步诊断任务；  
   - MBSE Gateway 调用 SysON 导出端点，提取模型全量 SysML v2 规范文本包（`.sysml`）并计算其 SHA-256 哈希（`source_checksum`）；  
   - Gateway 将文本流提交给 OpenSysML 语言服务容器，执行 Parser 解析、符号表推演、类型兼容性及约束求解检查。  
2. **结构化诊断报告呈现**：
   - 诊断引擎返回包含 `severity`（`ERROR` / `WARNING`）、`error_code`、`location`（行号列号）、`qualified_name` 的结构化数据集；  
   - 后端持久化入 `model_validation_result` 与 `validation_diagnostic_item` 表；  
   - 工作台底部“诊断面板”结构化呈现错误清单，工程师点击任意报错行（如 `Unresolved reference: FeedSystem::ServoDrive`），中心视口自动在 SysON 画布或文本编辑器中高亮聚焦到问题元素。  

#### M04-F05：一致性工作快照捕获与交接 (M04 $\rightarrow$ M06)

1. **快照捕获准入守卫**：
   - 触发快照捕获的前提：
     1. 存在最新的 `model_validation_result`；  
     2. 验证状态为 `PASSED` 或策略允许的 `PASSED_WITH_WARNING`；  
     3. 当前模型实时哈希与验证时完全一致（PUB-04 守卫触发器保障）。  
2. **生成不可变候选快照（CandidateSnapshot）**：
   - 系统将全套模型文本、布局元数据打包为 ZIP 归档，上传至 MinIO 的受控暂存存储桶，生成不可变物理制品记录（M19 `Artifact`）；  
   - 生成唯一且单次有效的 `snapshot_token`，在 `candidate_snapshot` 固化 `source_checksum` 与元素总数；  
   - 状态标记为 `STAGED`，向 M06 模型发布协调服务发出发布申请 RPC，完成工作区向发布流水线的原子交接。  

### 6. MBSE Gateway 适配架构与统一 SPI 契约

为实现 PLM 业务核心与专业建模工具的物理隔离，M04 严格依托 MBSE Gateway 抽象标准 Java SPI 接口：  

代码段

```
classDiagram
    class ModelAuthoringAdapter {
        <<interface>>
        +createProject(String projectName, String templateId) ProjectRef
        +openSession(String projectId, String userId) SessionTicket
        +exportModelText(String projectId) ModelTextStream
        +createElement(String projectId, ElementCreateCmd cmd) ElementRef
        +listElements(String projectId, String filter) List~ElementRef~
        +releaseSession(String sessionToken) Boolean
    }

    class ModelValidationAdapter {
        <<interface>>
        +validateModelText(InputStream sysmlStream) ValidationOutcome
        +getDiagnostics(String validationId) List~DiagnosticItem~
        +getEngineVersion() String
    }

    class SysONAdapterImpl {
        -SysONRestClient restClient
        -SysONGraphQLClient gqlClient
        +exportModelText()
        +createElement()
    }

    class OpenSysMLAdapterImpl {
        -OpenSysMLLanguageServerClient lspClient
        +validateModelText()
    }

    ModelAuthoringAdapter <|.. SysONAdapterImpl
    ModelValidationAdapter <|.. OpenSysMLAdapterImpl
```

#### 6.1 SPI 接口定义规范 (Java 17)

##### 1. `ModelAuthoringAdapter.java` (图形与工作区适配)

Java

```
package com.ccddesigner.mbse.gateway.spi;

import com.ccddesigner.mbse.gateway.dto.*;
import java.io.InputStream;
import java.util.List;

public interface ModelAuthoringAdapter {
    /**
     * 在 SysON 中初始化模型工程并装载行业模板
     */
    ExternalProjectRef createProject(String projectCode, String name, String compatibilityProfileId);

    /**
     * 建立编辑会话并获取视口加载票据
     */
    SessionTicket openSession(String externalProjectId, String userId, AuthoringChannel channel);

    /**
     * 导出全量一致性 SysML 文本流用于验证与归档 (路径 B)
     */
    ModelExportResult exportModelText(String externalProjectId);

    /**
     * 创建模型元素映射 (例如从 PLM 需求投影生成 SysML RequirementUsage)
     */
    ExternalElementRef createElement(String externalProjectId, ElementCreateCommand command);

    /**
     * 释放排他会话锁
     */
    boolean releaseSession(String sessionToken);
}
```

##### 2. `ModelValidationAdapter.java` (语义诊断适配)

Java

```
package com.ccddesigner.mbse.gateway.spi;

import com.ccddesigner.mbse.gateway.dto.ValidationOutcome;
import java.io.InputStream;

public interface ModelValidationAdapter {
    /**
     * 调用 OpenSysML 核心引擎执行语法与语义诊断
     */
    ValidationOutcome validate(InputStream sysmlModelStream, String sourceChecksum);

    /**
     * 获取校验服务引擎版本信息 (用于环境审计)
     */
    String getEngineVersion();
}
```

### 7. 核心业务流程与跨模块协同序列 (Sequence Diagrams)

#### 7.1 需求绑定与 SysON 图形建模协同流程

展示从 CCDDesigner 统一工作台拖拽需求至 SysON 视口，生成临时工作绑定的完整闭环：  

代码段

```
sequenceDiagram
    autonumber
    actor SysEng as 系统工程师
    participant UI as CCDDesigner 前端工作区
    participant M04 as M04 MBSE 工作区服务
    participant GW as MBSE Gateway
    participant SysON as SysON 图形建模环境
    participant DB as PostgreSQL (plm_syseng)

    SysEng->>UI: 1. 从左侧树选择受控需求拖拽入工作区
    UI->>M04: 2. 请求建立映射: createRequirementUsage(workspaceId, reqRevisionId)
    M04->>M04: 3. 校验编辑会话锁合法性 (checkSessionToken)
    M04->>GW: 4. 调用 SPI: createElement(cmd)
    GW->>SysON: 5. REST/GraphQL 注入 RequirementUsage 元素
    SysON-->>GW: 6. 返回生成的 SysON ElementId
    GW-->>M04: 7. 返回 ExternalElementRef
    M04->>DB: 8. 持久化入 working_element_binding (临时工作绑定)
    M04-->>UI: 9. 刷新前端视口，SysON 画布渲染新需求图形构件
```

#### 7.2 语义诊断与快照交接时序 (AT-01, AT-02, TC-04)

展示工程师完成阶段设计后，执行 OpenSysML 诊断并生成候选快照，向 M06 发起发布的完整技术流程：  

代码段

```
sequenceDiagram
    autonumber
    actor SysEng as 系统工程师
    participant UI as CCDDesigner 工作区 UI
    participant M04 as M04 工作区服务
    participant GW as MBSE Gateway
    participant SysON as SysON 服务
    participant OpenSysML as OpenSysML 校验引擎
    participant M19 as M19 文件制品服务
    participant M06 as M06 发布协调服务
    participant DB as PostgreSQL (plm_syseng)

    SysEng->>UI: 1. 点击 [执行模型诊断 (Validate)]
    UI->>M04: 2. triggerValidation(workspaceId)
    M04->>GW: 3. exportModelText(externalProjectId)
    GW->>SysON: 4. 抽取最新 SysML 模型文本包 (.sysml)
    SysON-->>GW: 5. 返回文本二进制流
    GW->>GW: 6. 计算 SHA-256 摘要: sourceChecksum
    
    M04->>OpenSysML: 7. validate(stream, sourceChecksum)
    OpenSysML-->>M04: 8. 返回诊断结果 (status: PASSED, errors: 0, warnings: 2)
    M04->>DB: 9. 写入 model_validation_result 与 diagnostic_items
    M04-->>UI: 10. 底部状态栏展示诊断结果为 PASSED

    SysEng->>UI: 11. 点击 [提交发布申请 (Submit Release)]
    UI->>M04: 12. captureCandidateSnapshot(workspaceId, validationId)
    M04->>M04: 13. 重新核算当前模型哈希并触发触发器 (PUB-04 守卫复核)
    M04->>M19: 14. 暂存原始模型归档 ZIP 制品，固化 artifactId
    M04->>DB: 15. INSERT candidate_snapshot (status=STAGED, token=TKT-...)
    
    M04->>M06: 16. RPC 发起发布交接: initiateModelRelease(snapshotToken, sourceChecksum)
    M06-->>M04: 17. 返回发布作业编号: releaseOperationId
    M04->>DB: 18. 更新 snapshot.status = HANDED_OVER
    M04-->>UI: 19. 提示快照已锁定，进入 M06 审批与 Flexo 暂存流水线
```

### 8. 前端工作区交互与视口集成规范 (React UI/UX)

工作区采用桌面级专业 CAD/MBSE 视口交互模式，嵌入 M01 统一工作台：  

Plaintext

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ 📌 VMC1000加工中心 / 系统模型 / Rev A (草稿)    [主通道: GRAPHICAL]  [🔒 编辑者: 李工]  [⚙️ 设置] │
├─────────────────┬───────────────────────────────────────────────┬───────────────────────────────┤
│ 左侧：工程导航树 │                中间：SysON 视口集成画布         │        右侧：语义属性与绑定    │
├─────────────────┼───────────────────────────────────────────────┼───────────────────────────────┤
│ 🔍 搜索元素...  │                                               │ 🏷️ [SysML RequirementUsage]   │
│ 📁 01_需求规格  │      ┌─────────────────────────────┐          │ 元素名称: XAxisStrokeReq      │
│   ├── REQ-X-001 │      │ «requirement»               │          │ 限定名: VMC1000::Req::Stroke  │
│   └── REQ-X-002 │      │ XAxisStrokeReq              │          │ ----------------------------- │
│ 📁 02_逻辑架构  │      │ Text: "X轴行程 >= 1000 mm"  │          │ 📌 关联 PLM 权威主数据:       │
│ 📁 03_物理结构  │      └──────────────┬──────────────┘          │ 业务编号: REQ-VMC-001 (Rev A) │
│   ├── FeedSystem│                     │ satisfies               │ 状态: RELEASED (受控只读)     │
│   └── Spindle   │      ┌──────────────▼──────────────┐          │ 指标阈值: >= 1000 mm          │
│ 📁 04_接口契约  │      │ «part»                      │          │ ----------------------------- │
│                 │      │ XAxisFeedSystem             │          │ 属性参数:                     │
│                 │      └─────────────────────────────┘          │   - stroke = 1020 mm          │
│                 │                                               │   - max_velocity = 48 m/min   │
├─────────────────┴───────────────────────────────────────────────┴───────────────────────────────┤
│ 底部：工程状态面板 (Tabs: [ 语法诊断 (2 Warnings) ]  [ 数字主线追踪 ]  [ 发布历史 ]  [ 操作审计 ])   │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│ ⚠️ WARNING | SYSML-ATTR-WARN | 属性 max_velocity 未在 M07 参数集中显式绑定标准量纲单位           │
│ ℹ️ INFO    | SYSML-PORT-INFO | 端口 CtrlBus 符合 InterfaceContract: EXT-CAN-V2.0 规范契约        │
│                                            [ 🔍 定位到构件 ]  [ ⚡ 执行诊断 ]  [ 🚀 提交发布快照 ]│
└─────────────────────────────────────────────────────────────────────────────────────────────────┘
```

#### 8.1 关键交互动作控制

1. **编辑锁定状态感知**：
   - 若当前用户未持有锁，画布呈现淡灰色半透明蒙层，右下角展示“只读浏览模式”；
   - 点击顶栏 **[申请编辑锁]**，后端校验无他人占用后签发令牌，视口自动转为可编辑状态。  
2. **诊断错误即时反查定位**：
   - 双击底部诊断面板中的错误行，前端通过 postMessage 向 SysON iframe 派发定位消息 `{ action: "HIGHLIGHT_ELEMENT", elementId: "..." }`，画布平滑移动并红色线框高亮目标元素。  

### 9. OpenAPI 3.0 接口契约定义

#### 9.1 创建/绑定系统模型工作区

- **HTTP 请求**：`POST /api/v1/mbse/workspaces/bind`

    

- **请求头**：`Idempotency-Key: idemp-ws-bind-20260917-001`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "modelProjectId": 701928410293812,
  "externalToolName": "SYSON",
  "primaryChannel": "GRAPHICAL",
  "compatibilityProfileId": "SYSML_V2_CNC_V2026_09",
  "initialTemplateId": "VMC1000_MACHINE_TEMPLATE_V1"
}
```

- **响应报文 (Response 201 Created)**：

JSON

```
{
  "workspaceId": 801928410290182,
  "modelProjectId": 701928410293812,
  "externalProjectId": "syson-proj-uuid-88192a01-c918",
  "primaryChannel": "GRAPHICAL",
  "status": "ACTIVE",
  "boundAt": "2026-09-17T10:00:00Z"
}
```

#### 9.2 打开编辑视口并获取授权会话锁

- **HTTP 请求**：`POST /api/v1/mbse/workspaces/{workspaceId}/sessions`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "requestLock": true,
  "lockTimeoutMinutes": 120
}
```

- **响应报文 (Response 200 OK - 成功获取锁)**：

JSON

```
{
  "workspaceId": 801928410290182,
  "sessionToken": "tkt_sess_8819204918239019",
  "lockGranted": true,
  "lockedByUser": "ENG-MECH-1042",
  "viewportUrl": "https://syson.ccddesigner.internal/workspaces/syson-proj-uuid-88192a01-c918?token=tkt_sess_8819204918239019",
  "expiresAt": "2026-09-17T12:00:00Z"
}
```

#### 9.3 触发 OpenSysML 语义诊断

- **HTTP 请求**：`POST /api/v1/mbse/workspaces/{workspaceId}/validate`

    

- **响应报文 (Response 200 OK - 存在未解析错误)**：

JSON

```
{
  "validationId": 901829018290182,
  "workspaceId": 801928410290182,
  "status": "FAILED",
  "sourceChecksum": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01",
  "errorCount": 1,
  "warningCount": 1,
  "diagnostics": [
    {
      "severity": "ERROR",
      "errorCode": "SYSML-UNRESOLVED-REF",
      "message": "Unresolved reference: Cannot find definition for 'SpindleCoolingPort'",
      "elementId": "elem_port_99182",
      "qualifiedName": "VMC1000::Spindle::CoolingLoop::PortIn",
      "sourceLocation": "SpindleModule.sysml:142:18",
      "recommendation": "Check if package 'FluidInterfaces' is properly imported in header."
    },
    {
      "severity": "WARNING",
      "errorCode": "SYSML-ATTR-WARN",
      "message": "Attribute 'ratedTorque' has no explicit unit binding",
      "elementId": "elem_attr_44129",
      "qualifiedName": "VMC1000::Spindle::Motor::ratedTorque",
      "sourceLocation": "Motor.sysml:45:12",
      "recommendation": "Bind unit 'N.m' from ISO standard library."
    }
  ]
}
```

#### 9.4 捕获候选快照并向 M06 发起发布交接

- **HTTP 请求**：`POST /api/v1/mbse/workspaces/{workspaceId}/candidate-snapshots`

    

- **请求头**：`Idempotency-Key: snap-submit-20260917-099`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "validationId": 901829018290182,
  "expectedChecksum": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01",
  "snapshotDescription": "VMC1000 X轴进给系统架构与需求完全匹配，准备提交阶段门发布"
}
```

- **响应报文 (Response 201 Created)**：

JSON

```
{
  "snapshotId": 991827491029182,
  "snapshotToken": "TKT_SNAP_8f3b2a19c7d44e82b99210e74f6e12aa",
  "status": "HANDED_OVER",
  "sourceChecksum": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01",
  "releaseOperationId": "op_m06_release_771920192",
  "capturedAt": "2026-09-17T10:45:12.891Z"
}
```

### 10. 验收测试矩阵与参考场景 (Acceptance Testing Matrix)

测试团队必须以 **VMC1000 立式加工中心 X 轴进给系统** 为基准场景展开全通路验收：  

| **用例编号**  | **对应上位验收  MD+ 2**     | **测试步骤与操作细节**                                       | **预期判定结果 (Pass Criteria)**                             | **验证日志与断言依据  MD+ 2**                                |
| ------------- | --------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **TC-M04-01** | **DoD-01/04**  TC-01, TC-02 | 1. 在 CCDDesigner 创建需求 `REQ-X-001` (有效行程 $\ge 1000\text{ mm}$)；    2. 拖入工作区生成 SysON `RequirementUsage`；    3. 检查映射库。 | 1. SysON 画布正确生成需求构件；    2. `working_element_binding` 准确记录临时映射，不生成正式受控 `ModelElementReference`。 | 数据库包含该临时绑定记录，且 `status = 'ACTIVE'`。           |
| **TC-M04-02** | **AT-02**  PUB-03, TC-05    | 1. 在模型中故意构造语法错误（如未导入端口接口契约）；  2. 点击执行诊断；  3. 尝试强制点击提交快照捕获。 | 1. OpenSysML 返回 `FAILED` 及错误代码位置；    2. 快照提交被数据库触发器 `fn_enforce_snapshot_checksum_match` 阻断，抛出 HTTP 422。 | 拦截抛出错误码 `PUB-03: Cannot capture candidate snapshot from a FAILED validation`。 |
| **TC-M04-03** | **AT-01**  TC-04, TC-06     | 1. 修复所有语法与引用错误，重新执行诊断；  2. 诊断通过（`PASSED`）；  3. 提交快照捕获并向 M06 移交。 | 1. 成功生成 `CandidateSnapshot` 及 `snapshotToken`；    2. 导出 ZIP 制品存入 MinIO；    3. M06 接收到移交请求并启动两阶段发布。 | 数据库 `candidate_snapshot.status` 正确流转为 `HANDED_OVER`。 |
| **TC-M04-04** | **TC-15**  PUB-04 守卫      | 1. 校验通过获得 Checksum A；  2. 工程师在 SysON 微调进给轴阻尼参数（模型哈希变为 Checksum B）；  3. 不重新点击校验，直接提交旧快照申请。 | 触发器比对发现当前哈希 Checksum B != 验证记录 Checksum A，物理阻止快照生成。 | 抛出异常 `PUB-04: Model has been modified since last validation! Validate again.` |
| **TC-M04-05** | **CST-M04-01**  并发锁控    | 用户 A 已在图形通道获取编辑锁，用户 B 尝试调用 API 将主通道切换为文本通道 `TEXTUAL`。 | 数据库触发器 `fn_guard_workspace_concurrency` 拦截该更新操作，提示锁被占用。 | 返回 HTTP 409 Conflict，通道保持 `GRAPHICAL`，工作区状态不发生混乱。 |
| **TC-M04-06** | **AT-03**  隔离性验证       | 工程师在 SysON 工作区中任意删除或重命名零件构件，随后查询 PLM 已发布的基线与数字主线。 | 1. 工作区内草稿模型顺利更新；    2. 历史基线与已发布的数字主线解析完全不受影响，旧 URI 仍可读取历史快照。 | 满足 Working Model $\ne$ Published Model $\ne$ Baseline 原则，草稿变动对受控数据零污染。 |