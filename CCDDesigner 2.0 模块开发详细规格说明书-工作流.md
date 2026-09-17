# CCDDesigner 2.0 模块开发详细规格说明书

## M24: 工作流与审批 (Workflow and Engineering Approvals)

| **文档属性**    | **内容**                                                     |
| --------------- | ------------------------------------------------------------ |
| **模块编号**    | `M24` (Phase: P1, Type: N＋I)                                |
| **文档编号**    | `CCD-DEV-SPEC-2.0-M24`                                       |
| **版本 / 状态** | V1.0 / 评审发布稿                                            |
| **主责模块**    | M24 工作流与审批                                             |
| **协同模块**    | M01 (统一工作台/消息中心)、M02 (项目与阶段门)、M06 (模型发布)、M11 (验证证据评审)、M16 (EBOM发布)、M19 (图文档签入)、M20 (生命周期底座)、M21 (基线冻结)、M22 (工程变更CCB)、M30 (身份与资质) |
| **上位依据**    | 《CCDDesigner 2.0 产品说明书》§22, §30    《CCDDesigner 2.0 产品功能架构与模块设计说明书》§32, §40.1, §41.1, §42    《CCDDesigner 2.0 产品开发说明书》§4.5 (M24), §5.1, §7.2 |
| **适用受众**    | 流程引擎开发工程师、系统架构师、后端核心开发工程师、安全与合规审计员 |

### 1. 模块定位与核心设计原则

依据上位开发说明书与功能架构要求，M24 承担全系统多专业工程会签、技术评审流编排及签发法律级审批凭证的权威职责。本模块采用嵌入式 **Flowable BPMN 流程引擎**，并建立与业务领域的解耦边界：  

1. **严格职责分离与无越权写原则（Separation of Concerns & No In-Place Mutation）**：
   - **审批服务只负责产生签署凭证（`ApprovalDecision`）**，在流程执行中**严禁直接修改任何业务对象的业务属性或强制改写主表生命周期状态**。  
   - 业务状态机推进（如 `IN_REVIEW` $\rightarrow$ `RELEASED`）必须由所属业务领域的应用服务显式消费凭证，并在本域本地事务内二次复核前置约束后完成状态迁移（M20-F03）。  
2. **快照哈希锚定与动态防篡改（Content Hash Anchor - AT-16 守护）**：
   - 流程启动时，强制提取并固化被审工程对象（或模型候选包、基线闭包、变更单）的全局内容摘要（`targetContentHash`）。  
   - 审批期间目标对象若发生任何内容篡改或重新提交，原审批流及关联凭据**即时失效**，严禁使用旧凭证放行新内容（AT-16）。  
3. **两阶段异步回调与绝对幂等控制（Idempotent Callback Coordination）**：
   - 流程节点完成或流程实例终结时，通过异步安全通道触发业务回调通知。  
   - 所有回调机制必须基于全局唯一操作序号（`action_id`）与本地发件箱（Outbox）事件，建立严格的防重去重表，**保证同一事件回调多次执行结果物理绝对幂等**。  
4. **企业单体集中授权（无多租户边界设计）**：
   - 依据企业单组织部署规约，全面移除租户隔离字段，通过 M30-IAM 的“组织部门—专业学科—项目工作组（`ProjectMembership`）”实现细粒度审批人动态解析与转派。  
5. **不可伪造电子签名凭据（Non-repudiable Cryptographic Stamp）**：
   - 审批人提交决议时，系统使用该用户的工程证书私钥或平台托管安全密钥对“用户身份＋审批结论＋处理时间戳＋候选内容哈希”进行数字加签，生成不可抵赖的加密签名串。  

### 2. 双向追踪矩阵 (Bidirectional Traceability Matrix)

| **功能编号 / 约束规范**              | **主责与协同模块  MD+ 1** | **上位架构依据与章节  MD+ 1**                       | **覆盖验收用例  MD+ 1** | **核心控制逻辑与阻断行为**                                   |
| ------------------------------------ | ------------------------- | --------------------------------------------------- | ----------------------- | ------------------------------------------------------------ |
| **M24-F01** (流程绑定与版本化配置)   | M24, M20                  | CCD-DEV-SPEC §4.5    CCD-ARCH-FUNC §32              | AT-01, AT-15            | 按业务类型、密级及变更等级动态绑定特定版本 BPMN 流程定义，支持 SpEL 路由。 |
| **M24-F02** (流程流转、会签与撤回)   | M24, M01                  | CCD-DEV-SPEC §4.5, §7.2    CCD-ARCH-FUNC §32        | AT-13, AT-16            | 支持顺序/并行多实例会签；支持任务转办、委派、加签；发起人可在前置节点安全撤回。 |
| **M24-F03** (审批意见与签名凭证生成) | M24, M30                  | CCD-DEV-SPEC §4.5, §5.1    CCD-ARCH-FUNC §32, §41.1 | AT-01, AT-16            | 固化数字签名、候选哈希与审批意见，产出不可变 `ApprovalDecision` 凭证。 |
| **M24-F04** (异步状态机回调与幂等)   | M24, M06, M20, M21, M22   | CCD-DEV-SPEC §4.5, §6.2    CCD-ARCH-FUNC §32, §41.3 | AT-04, AT-30            | 流程终结异步投递 Outbox 事件；业务回调层采用 `actionId` 严格防重幂等处理。 |
| **CST-M24-01** (职责分离与只读凭证)  | M24, M20                  | CCD-DEV-SPEC §4.5    CCD-ARCH-FUNC §5, §32          | AT-04, AT-07            | 数据库触发器硬阻断：M24 服务严禁向 `plm_product` 或 `plm_syseng` 业务主表执行 UPDATE。 |
| **CST-M24-02** (审批中内容篡改熔断)  | M24, M06, M21             | CCD-DEV-SPEC §5.1, §7.2    CCD-ARCH-FUNC §32, §45   | AT-16                   | 流程执行中若探测到关联实体内容哈希变化，强制自动终止流程并作废已签署记录。 |

### 3. 领域对象模型与 ER 物理字典 (PostgreSQL DDL)

以下物理 DDL 属于 `plm_workflow` 独立业务 Schema，不包含多租户隔离字段，与通用治理底座 `plm_govern` 深度联动：  

SQL

```
-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M24 工作流与审批
-- 适用环境: PostgreSQL 15+ (Flowable 7.x 外部集成与业务解耦架构)
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_workflow;

-- 流程实例运行状态枚举
CREATE TYPE plm_workflow.instance_status AS ENUM (
    'RUNNING',      -- 流程流转中
    'COMPLETED',    -- 流程正常结束 (已达成最终决议)
    'TERMINATED',   -- 因内容篡改、手动撤回或异常被强行终止
    'SUSPENDED'     -- 管理员挂起
);

-- 审批最终结论枚举
CREATE TYPE plm_workflow.approval_conclusion AS ENUM (
    'APPROVED',     -- 审查全票/达标通过
    'REJECTED',     -- 驳回不通过 (打回草稿)
    'WITHDRAWN'     -- 发起人主动撤回
);

-- 会签投票策略枚举
CREATE TYPE plm_workflow.sign_strategy AS ENUM (
    'UNANIMOUS',    -- 一票否决制 (必须全员通过)
    'PERCENTAGE',   -- 比例通过制 (如赞成票 >= 80%)
    'FIRST_WINS'    -- 首人决定制 (任意一人签署即流转)
);

-- 1. 业务对象与流程定义版本化绑定表 (WorkflowDefinitionBinding)
CREATE TABLE plm_workflow.definition_binding (
    binding_id              BIGINT PRIMARY KEY,
    target_object_type      VARCHAR(64) NOT NULL, -- PartRevision, ModelRelease, Baseline, ChangeOrder 等
    business_category       VARCHAR(64) NOT NULL, -- 如 MAJOR_CHANGE, STANDARD_RELEASE, CRITICAL_CDR
    flowable_proc_def_key   VARCHAR(128) NOT NULL, -- Flowable BPMN ProcessDefinitionKey
    proc_def_version        INT NOT NULL,          -- 绑定的固定 BPMN 版本号
    sign_strategy           plm_workflow.sign_strategy NOT NULL DEFAULT 'UNANIMOUS',
    pass_threshold_percent  NUMERIC(5,2) NULL,     -- 比例通过制时的阈值 (如 80.00)
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    binding_description     TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_workflow_binding UNIQUE (target_object_type, business_category, proc_def_version)
);
COMMENT ON TABLE plm_workflow.definition_binding IS 'M24: 业务类型与 BPMN 流程定义的映射配置表';

-- 2. PLM 流程实例快照表 (WorkflowInstance - 桥接 Flowable 运行实例)
CREATE TABLE plm_workflow.workflow_instance (
    workflow_inst_id        BIGINT PRIMARY KEY,
    flowable_proc_inst_id   VARCHAR(64) NOT NULL UNIQUE, -- 对应的 Flowable ProcessInstanceId
    binding_id              BIGINT NOT NULL REFERENCES plm_workflow.definition_binding(binding_id),
    target_object_type      VARCHAR(64) NOT NULL,
    target_object_id        BIGINT NOT NULL,             -- 关联被审实体主键 (如 revision_id)
    target_business_code    VARCHAR(128) NOT NULL,
    target_content_hash     CHAR(64) NOT NULL,           -- 启动审批时捕获的快照 SHA-256 (AT-16 防线)
    project_id              BIGINT NULL,                 -- 所属工程研发项目
    initiator_id            VARCHAR(64) NOT NULL,        -- 流程发起人工号
    status                  plm_workflow.instance_status NOT NULL DEFAULT 'RUNNING',
    conclusion              plm_workflow.approval_conclusion NULL,
    termination_reason      TEXT NULL,
    started_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at            TIMESTAMPTZ NULL
);
COMMENT ON TABLE plm_workflow.workflow_instance IS 'M24: 流程实例台账，固化被审对象哈希与 Flowable 实例映射';
CREATE INDEX idx_wf_inst_target ON plm_workflow.workflow_instance(target_object_type, target_object_id, status);

-- 3. 不可伪造审批决策凭据表 (ApprovalDecision - 核心法律凭证)
CREATE TABLE plm_workflow.approval_decision (
    decision_ticket_id      BIGINT PRIMARY KEY,
    workflow_inst_id        BIGINT NOT NULL REFERENCES plm_workflow.workflow_instance(workflow_inst_id),
    target_object_type      VARCHAR(64) NOT NULL,
    target_object_id        BIGINT NOT NULL,
    target_content_hash     CHAR(64) NOT NULL,           -- 凭证生效必须逐位匹配的哈希
    final_conclusion        plm_workflow.approval_conclusion NOT NULL,
    is_consumed             BOOLEAN NOT NULL DEFAULT FALSE, -- 业务状态机是否已核销消费该凭据
    consumed_at             TIMESTAMPTZ NULL,
    consumed_by_action      VARCHAR(128) NULL,           -- 消费该凭据的业务动作标识
    crypto_signature_stamp  TEXT NOT NULL,               -- 平台/会签委员会安全证书签名摘要
    signed_payload_digest   CHAR(64) NOT NULL,           -- 签名原文 SHA-256
    decided_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_decision_inst UNIQUE (workflow_inst_id)
);
COMMENT ON TABLE plm_workflow.approval_decision IS 'M24: 不可篡改审批决议凭证表，状态机凭此推进，无权改写业务数据';
CREATE INDEX idx_decision_hash_query ON plm_workflow.approval_decision(target_object_id, target_content_hash, final_conclusion);

-- 4. 节点审批动作与意见明细记录表 (WorkflowActionRecord)
CREATE TABLE plm_workflow.workflow_action_record (
    action_record_id        BIGINT PRIMARY KEY,
    workflow_inst_id        BIGINT NOT NULL REFERENCES plm_workflow.workflow_instance(workflow_inst_id),
    flowable_task_id        VARCHAR(64) NOT NULL,        -- Flowable TaskId
    task_node_name          VARCHAR(128) NOT NULL,       -- 如 "机械主管工程师会签"
    operator_id             VARCHAR(64) NOT NULL,        -- 实际操作人工号
    action_type             VARCHAR(32) NOT NULL,        -- APPROVE, REJECT, DELEGATE, ADD_SIGN, REVOKE
    comment_text            TEXT,
    attachment_artifact_id  BIGINT NULL,                 -- 关联的批注文件或签字附件 (M19)
    delegated_to_user_id    VARCHAR(64) NULL,            -- 若为转办/委派，记录受托人
    voted_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_workflow.workflow_action_record IS 'M24: 人工审批流水与审查意见追踪表';
CREATE INDEX idx_action_record_inst ON plm_workflow.workflow_action_record(workflow_inst_id, flowable_task_id);

-- 5. 业务回调幂等防重表 (WorkflowCallbackIdempotency - 防止网络风暴与重复回调)
CREATE TABLE plm_workflow.callback_idempotency (
    idempotency_key         VARCHAR(128) PRIMARY KEY,    -- 格式: "ACTION:{workflowInstId}:{eventVersion}"
    workflow_inst_id        BIGINT NOT NULL REFERENCES plm_workflow.workflow_instance(workflow_inst_id),
    callback_type           VARCHAR(64) NOT NULL,        -- NOTIFY_RELEASE, NOTIFY_REJECT, SYNC_TASK
    execution_status        VARCHAR(32) NOT NULL DEFAULT 'PROCESSING', -- PROCESSING, SUCCESS, FAILED
    error_message           TEXT NULL,
    processed_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_workflow.callback_idempotency IS 'M24: 状态机异步回调幂等控制表';
```

### 4. 核心完整性触发器与职责分离防御机制 (Triggers & Guards)

#### 4.1 审批服务严禁直接修改业务主表触发器 (`fn_prevent_workflow_direct_business_mutation`)

落实开发说明书核心约束：M24 仅生成只读签署凭据，无权直接跨库或在同库事务中私自修改业务数据表的生命周期字段：  

SQL

```
CREATE OR REPLACE FUNCTION plm_workflow.fn_prevent_workflow_cross_mutation()
RETURNS TRIGGER AS $$
DECLARE
    v_app_name VARCHAR(64);
BEGIN
    -- 提取当前连接会话的 ApplicationName 标识
    SELECT current_setting('application_name', true) INTO v_app_name;

    -- 若当前执行上下文明确来自 Flowable 引擎或 M24 流程微服务模块
    IF v_app_name ILIKE '%FlowableWorkflowService%' OR v_app_name ILIKE '%M24_WorkflowCore%' THEN
        -- 尝试直接修改业务表的受控状态字段 (如 PartRevision, ModelRelease, ChangeOrder)
        RAISE EXCEPTION 'Architecture Security Violation [CST-M24-01]: Workflow engine is strictly PROHIBITED from directly modifying business domain tables [%]. Workflow must only issue ApprovalDecision tickets.',
            TG_TABLE_NAME USING ERRCODE = '23000';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 挂接至工程对象核心表
CREATE TRIGGER trg_guard_obj_revision_mutation
BEFORE UPDATE OF lifecycle_state ON plm_govern.obj_revision
FOR EACH ROW EXECUTE FUNCTION plm_workflow.fn_prevent_workflow_cross_mutation();
```

#### 4.2 候选快照防篡改自动熔断触发器 (`fn_check_content_hash_integrity` - AT-16 物理防线)

在签署节点完成前，自动比对目标实体的当前哈希是否与流程启动时一致，若不一致直接强行熔断流程：  

SQL

```
CREATE OR REPLACE FUNCTION plm_workflow.fn_check_content_hash_integrity()
RETURNS TRIGGER AS $$
DECLARE
    v_staged_content_hash CHAR(64);
BEGIN
    -- 仅对审批通过并试图生成 ApprovalDecision 的动作执行检验
    IF NEW.final_conclusion = 'APPROVED' THEN
        -- 穿透查询目标业务实体最新的当前哈希 (以通用 obj_revision 为例)
        SELECT (custom_attributes->>'content_hash') INTO v_staged_content_hash
        FROM plm_govern.obj_revision
        WHERE revision_id = NEW.target_object_id;

        IF v_staged_content_hash IS NOT NULL AND v_staged_content_hash != NEW.target_content_hash THEN
            RAISE EXCEPTION 'Security Tampering Detected (AT-16): The content of target object [%] was modified during review! Staged hash [%] != Initial workflow hash [%]. Process aborted.',
                NEW.target_object_id, v_staged_content_hash, NEW.target_content_hash
                USING ERRCODE = '23000';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_enforce_hash_integrity_before_decision
BEFORE INSERT ON plm_workflow.approval_decision
FOR EACH ROW EXECUTE FUNCTION plm_workflow.fn_check_content_hash_integrity();
```

### 5. 功能特性详细技术规格 (M24-F01 ~ M24-F04)

#### M24-F01：业务对象与流程定义（BPMN）的版本化绑定配置

1. **多维条件路由匹配引擎**：

   - 业务发起审批调用 `POST /api/v1/workflow-instances` 时，系统根据传入的上下文向量执行规则求值：

     $$\text{MatchedBinding} = \arg\max (\text{TargetObjectType}, \text{Discipline}, \text{SecurityLevel}, \text{ChangeCategory}) \text{[cite: 1, 2]}$$

   - 例如：`PartRevision`（机密级、机械专业、全新件）自动匹配至 `PROC-MECH-HEAVY-RELEASE-V2`（要求室主任、校对、标准化、总师四级会签）；普通标准件自动路由至轻量级两级审批流。  

2. **流程定义版本锁定**：

   - 绑定记录显式固定 `proc_def_version`（如 BPMN 版本号 3）；  
   - 后续流程管理员在 Flowable 模型设计器中部署了版本 4 时，**存量运行中的流程实例继续在原版本 3 引擎空间流转**，绝不发生运行期定义漂移。  

#### M24-F02：启动流程、任务委派、加签、会签流转及撤回控制

1. **会签计算模型（BPMN Multi-Instance Execution）**：
   - 系统通过 Flowable 嵌入式引擎提供三种标准化会签节点模板：  
     - **全票一票否决（UNANIMOUS）**：配置 `completionCondition = ${nrOfCompletedInstances == nrOfInstances && nrOfRejectedInstances == 0}`；任意 1 票拒绝，立即触发流程驳回逻辑。  
     - **加权比例通过（PERCENTAGE）**：配置 `completionCondition = ${(nrOfApprovedInstances / nrOfInstances) >= passThreshold}`。  
     - **首人决定制（FIRST_WINS）**：适用于并行传阅告知或抢签池。  
2. **职责分离动态校验（SoD-01 运行时守卫）**：
   - 在 Flowable `TaskListener`（`EVENTNAME_CREATE`）中植入拦截切面：
     - 若计算所得当前节点的候选审批人集合包含该流程的 `initiatorId`（流程发起人），系统**自动剔除其审批资格**；  
     - 若节点仅有一位审批人且恰为发起人自身，Flowable 抛出 `SelfApprovalBlockedException` 并向流程管理员发出流转阻塞报警，坚决阻断自审（AT-16）。  
3. **加签、转办与安全撤回协议**：
   - **加签（Add Sign-off）**：支持在前置（Pre-sign）或后置（Post-sign）插入临时技术专家，Flowable 动态向活动执行树（ActivityExecution）注入子执行线。  
   - **委派/转办（Delegate / Transfer）**：记录委派人工号至 `WorkflowActionRecord`，权限临时委托，原责任人保留连带审计追踪。  
   - **安全撤回（Revoke）**：仅允许在“下一个节点尚未有人开始处理”时由发起人主动撤回；撤回后流程状态标记为 `TERMINATED`，原因写入 `WITHDRAWN_BY_INITIATOR`，并向各参与方收件箱发送撤销通知（M01-MSG）。  

#### M24-F03：审批意见归档、哈希签名并生成 ApprovalDecision

1. **非对称数字签名结构**：

   - 当 Flowable 流程到达 EndEvent 终点事件时，系统激活 `SignOffDelegate` 服务组件，对会签成果进行摘要打包：  

     $$\text{SignPayload} = \langle \text{workflowInstId}, \text{targetObjectId}, \text{targetContentHash}, \text{conclusion}, \text{completedAt}, \text{approverList} \rangle \text{[cite: 1, 2]}$$

   - 签名服务调用平台专用加密库（采用国密 SM2 或 RSA-2048 算法），生成不可篡改的 `cryptoSignatureStamp` 电子签名戳。  

2. **签发 ApprovalDecision 凭证**：

   - 在 `plm_workflow.approval_decision` 插入记录，置 `is_consumed = FALSE`；  
   - 该凭证具备法律证据效应，直接作为 M06 模型发布、M21 基线冻结、M22 变更生效的前置放行要件。  

#### M24-F04：执行异步业务状态机回调协调，保证幂等性

1. **两阶段解耦状态流转架构**：

   - Flowable 内部事务提交后，在同一本地数据库事务内向 `plm_infra.sys_outbox_event` 写入 `WorkflowInstanceCompletedEvent`。  
   - 消息监听器消费该事件并调用业务模块提供的回调接口（如 `POST /api/v1/model-releases/{id}/confirm-release`）。  

2. **幂等控制与状态机防重协议（Idempotent Protocol - AT-04, AT-30）**：

   代码段

   ```
   flowchart TD
       START([收到工作流完成回调请求]) --> CHECK_IDEMP{检查 callback_idempotency<br/>是否存在该 Key?}
       CHECK_IDEMP -->|已存在且为 SUCCESS| RETURN_CACHED[直接返回 HTTP 200: 幂等响应, 不重复流转业务]
       CHECK_IDEMP -->|已存在且为 PROCESSING| RETURN_CONFLICT[返回 HTTP 409: 正在处理中, 避免并发重入]
       CHECK_IDEMP -->|不存在| INSERT_RUN[插入 callback_idempotency (PROCESSING)]
   
       INSERT_RUN --> BIZ_VERIFY[1. 校验凭据有效性: checkApprovalTicketValid]
       BIZ_VERIFY --> HASH_VERIFY[2. 复核内容哈希防篡改: verifyCurrentHash == ticketHash]
       HASH_VERIFY --> BIZ_TRANS[3. 业务状态机推进: DRAFT -> RELEASED]
       BIZ_TRANS --> MARK_CONSUMED[4. 标记凭据: is_consumed = TRUE]
       MARK_CONSUMED --> UPDATE_SUCCESS[5. 更新 callback_idempotency = SUCCESS]
       UPDATE_SUCCESS --> RESP_200([响应 200 OK: 业务正式激活发布])
   
       HASH_VERIFY -- 哈希不匹配 (AT-16) --> ERR_TAMPER[抛出 422: 内容已被篡改, 拒绝消费凭证]
       BIZ_TRANS -- 领域状态冲突 --> ERR_TRANS[更新 callback = FAILED, 报警人工介入]
   ```

### 6. Flowable 引擎集成架构与核心代码实现

CCDDesigner 2.0 采用无状态 Spring Boot 模块集成模式引入 Flowable 7.x 核心引擎，所有流程实例与历史数据持久化至独立的数据表分区（`ACT_*`）。  

#### 6.1 引擎自定义委派组件：审批决议安全签发 (`SignOffDecisionDelegate.java`)

Java

```
package com.ccddesigner.workflow.delegate;

import com.ccddesigner.common.crypto.DigitalSignatureService;
import com.ccddesigner.workflow.repository.WorkflowDecisionRepository;
import com.ccddesigner.workflow.entity.ApprovalDecisionEntity;
import com.ccddesigner.workflow.entity.WorkflowInstanceEntity;
import com.ccddesigner.workflow.repository.WorkflowInstanceRepository;
import jakarta.annotation.Resource;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Component("signOffDecisionDelegate")
public class SignOffDecisionDelegate implements JavaDelegate {

    @Resource
    private WorkflowInstanceRepository instanceRepository;
    
    @Resource
    private WorkflowDecisionRepository decisionRepository;
    
    @Resource
    private DigitalSignatureService signatureService;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void execute(DelegateExecution execution) {
        String procInstId = execution.getProcessInstanceId();
        WorkflowInstanceEntity wfInst = instanceRepository.findByFlowableProcInstIdOrThrow(procInstId);

        // 1. 提取会签综合表决结果 (由 BPMN 聚合变量注入)
        String decisionConclusion = (String) execution.getVariable("finalConclusion"); // APPROVED / REJECTED
        String initialContentHash = wfInst.getTargetContentHash();

        // 2. 组装待加签原文并生成不可逆数字签名
        String signaturePayload = String.format("INST:%d|OBJ:%d|HASH:%s|OUTCOME:%s|TIME:%d",
                wfInst.getWorkflowInstId(), wfInst.getTargetObjectId(), initialContentHash, decisionConclusion, Instant.now().toEpochMilli());
        
        String cryptoStamp = signatureService.signWithPlatformKey(signaturePayload);
        String digest = signatureService.sha256(signaturePayload);

        // 3. 持久化只读 ApprovalDecision 凭据 (严禁直接更新业务对象表！)
        ApprovalDecisionEntity decision = ApprovalDecisionEntity.builder()
                .decisionTicketId(SnowflakeIdGenerator.nextId())
                .workflowInstId(wfInst.getWorkflowInstId())
                .targetObjectType(wfInst.getTargetObjectType())
                .targetObjectId(wfInst.getTargetObjectId())
                .targetContentHash(initialContentHash)
                .finalConclusion(decisionConclusion)
                .isConsumed(false)
                .cryptoSignatureStamp(cryptoStamp)
                .signedPayloadDigest(digest)
                .decidedAt(Instant.now())
                .build();

        decisionRepository.save(decision);

        // 4. 将决议凭证编号回传至流程上下文，供后续监听器发射事件
        execution.setVariable("generatedTicketId", decision.getDecisionTicketId());
    }
}
```

#### 6.2 职责分离 (SoD) 动态校验任务监听器 (`SoDTaskAssignmentListener.java`)

Java

```
package com.ccddesigner.workflow.listener;

import com.ccddesigner.common.exception.SecurityAccessDeniedException;
import com.ccddesigner.workflow.entity.WorkflowInstanceEntity;
import com.ccddesigner.workflow.repository.WorkflowInstanceRepository;
import jakarta.annotation.Resource;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("soDTaskAssignmentListener")
public class SoDTaskAssignmentListener implements TaskListener {

    @Resource
    private WorkflowInstanceRepository instanceRepository;

    @Override
    public void notify(DelegateTask delegateTask) {
        String procInstId = delegateTask.getProcessInstanceId();
        WorkflowInstanceEntity wfInst = instanceRepository.findByFlowableProcInstIdOrThrow(procInstId);

        String initiatorId = wfInst.getInitiatorId();
        String assignee = delegateTask.getAssignee();

        // 规则 SoD-01: 流程发起人不得审批自身提交的发布申请与工程修改单
        if (assignee != null && assignee.equalsIgnoreCase(initiatorId)) {
            throw new SecurityAccessDeniedException(
                "ERR_SOD_SELF_APPROVAL_PROHIBITED",
                String.format("SoD-01 Violation: Task [%s] assignment rejected. Initiator [%s] cannot be designated as reviewer.",
                    delegateTask.getName(), initiatorId)
            );
        }

        // 针对候选人列表 (Candidate Users) 进行排查过滤
        delegateTask.getCandidates().removeIf(candidate -> candidate.getUserId().equalsIgnoreCase(initiatorId));
    }
}
```

### 7. 典型 BPMN 会签模型与核心流转时序

#### 7.1 系统模型发布受控会签 BPMN 流水线

代码段

```
flowchart LR
    START((开始)) --> T1[机械专业负责人会签]
    START --> T2[电气与控制负责人会签]
    START --> T3[系统架构师审查]
    
    T1 & T2 & T3 --> GATE{会签结果判定}
    GATE -->|全员赞成| SIGN[服务任务: signOffDecisionDelegate<br/>生成不可篡改凭证]
    GATE -->|任一驳回| REJ[服务任务: recordRejectionDelegate<br/>标记决议为 REJECTED]
    
    SIGN --> EVT_PUB[发射完成事件至 Outbox]
    REJ --> EVT_PUB
    EVT_PUB --> END((流程终结))
```

#### 7.2 业务协同与状态机推进完整时序图 (AT-04, AT-16 闭环)

代码段

```
sequenceDiagram
    autonumber
    actor Submitter as 工程师 (发起人)
    participant M06 as M06 模型发布服务[cite: 1]
    participant M24 as M24 工作流服务[cite: 1]
    participant Flowable as Flowable BPMN Engine[cite: 1]
    actor Approver as 审批人 (总师/主管)
    participant M20 as M20 生命周期底座[cite: 1]

    Submitter->>M06: 1. 提交发布候选申请: submitRelease(candidateId)[cite: 1]
    M06->>M06: 2. 固化快照, 计算 SHA-256 releaseHash[cite: 1]
    M06->>M24: 3. 发起审批流程: startWorkflow(ModelRelease, candidateId, releaseHash)[cite: 1]
    
    M24->>Flowable: 4. runtimeService.startProcessInstanceByKey(...)
    M24->>M24: 5. 登记 plm_workflow.workflow_instance (status=RUNNING)
    Flowable-->>Approver: 6. 派发待办任务, 发送 M01 站内通知[cite: 1]

    opt 审批期间模型发生篡改或重交 (AT-16 异常场景)
        Submitter->>M06: A1. 尝试修改草稿/重新捕获快照[cite: 1]
        M06->>M24: A2. 发送内容变动事件: notifyTargetTampered(candidateId)[cite: 1]
        M24->>Flowable: A3. runtimeService.deleteProcessInstance(TERMINATED)
        M24-->>Submitter: A4. 流程强制作废, 原有审批流全部失效 (AT-16 通过)
    end

    Approver->>M24: 7. 提交签署意见: completeTask(taskId, APPROVE, comments)
    Flowable->>Flowable: 8. 会签完成, 触发 signOffDecisionDelegate
    Flowable->>M24: 9. 写入 plm_workflow.approval_decision 凭据表
    Note over M24: 仅生成凭据, 绝对不修改 M06/M20 实体表！

    Flowable->>M24: 10. 流程实例达成 COMPLETED 终态
    M24->>M24: 11. 异步向 Outbox 写入 WorkflowCompletedEvent[cite: 1]
    
    par 异步消费与状态机推进 (两阶段解耦)
        M24-->>M06: 12. 回调通知: handleApprovalCallback(ticketId, actionId)[cite: 1]
        M06->>M24: 13. 读取并复核凭证: getDecision(ticketId)[cite: 1]
        M06->>M06: 14. 强校验: ticket.contentHash == candidate.actualHash[cite: 1]
        M06->>M20: 15. 本地事务原子推进: stateTransition(IN_REVIEW -> RELEASED)[cite: 1]
        M06->>M24: 16. 标记凭据已消费: markConsumed(ticketId)
    end
```

### 8. OpenAPI 3.0 接口契约定义

#### 8.1 发起业务审批流程

- **HTTP 请求**：`POST /api/v1/workflow-instances`

    

- **请求头**：`Idempotency-Key: idemp-wf-start-20260916-001`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "targetObjectType": "ModelRelease",
  "targetObjectId": 70918209182019,
  "targetBusinessCode": "REL-VMC1000-SYS-001",
  "targetContentHash": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01",
  "businessCategory": "STANDARD_RELEASE",
  "projectId": 100293810293,
  "workflowTitle": "【模型发布】VMC1000 系统架构与接口定义正式发布审查",
  "initialVariables": {
    "leadDiscipline": "SYSTEM_ENGINEERING",
    "criticalityLevel": "HIGH"
  }
}
```

- **响应报文 (Response 201 Created)**：

JSON

```
{
  "workflowInstId": 99201488102,
  "flowableProcInstId": "prc_inst_881920192",
  "status": "RUNNING",
  "bindingId": 101,
  "targetContentHash": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01",
  "startedAt": "2026-09-16T11:00:00Z"
}
```

#### 8.2 执行节点任务审批

- **HTTP 请求**：`POST /api/v1/workflow-tasks/{taskId}/complete`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "action": "APPROVE",
  "comment": "经校核，主轴热-刚度耦合接口与电气接线定义符合设计规范，同意发布。",
  "attachmentArtifactId": 901829018209182
}
```

- **响应报文 (Response 200 OK)**：

JSON

```
{
  "taskId": "task_wf_771920",
  "action": "APPROVE",
  "isProcessCompleted": true,
  "decisionTicketId": 881029481920,
  "finalConclusion": "APPROVED",
  "completedAt": "2026-09-16T11:45:12.891Z"
}
```

#### 8.3 查询并核销审批决策凭据 (供业务模块状态机调用)

- **HTTP 请求**：`POST /api/v1/workflow-decisions/{decisionTicketId}/consume`

  [cite: 2]

- **请求载荷 (Request Body)**：

JSON

```
{
  "actionId": "CONSUME-ACT-M06-REL-70918209182019",
  "expectedContentHash": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01"
}
```

- **响应报文 (Response 200 OK - 核销成功)**：

JSON

```
{
  "decisionTicketId": 881029481920,
  "finalConclusion": "APPROVED",
  "isConsumed": true,
  "verified": true,
  "consumedAt": "2026-09-16T11:45:15.102Z",
  "cryptoSignatureStamp": "SM2_SIG_304502204c3...90182a"
}
```

### 9. 领域事件与发件箱契约 (Outbox Schema)

M24 流程生命周期跃迁时，严格在同一事务中向 `plm_infra.sys_outbox_event` 写入领域事件，广播至 Kafka 消息总线：  

#### 事件一：`WorkflowTaskAssignedEvent`

- **触发时机**：Flowable 生成新审批任务并成功分派责任人。  
- **Payload 契约**：

JSON

```
{
  "eventId": 9812739102831,
  "eventType": "WorkflowTaskAssigned",
  "aggregateType": "WorkflowTask",
  "aggregateId": "task_wf_771920",
  "payload": {
    "taskId": "task_wf_771920",
    "workflowInstId": 99201488102,
    "taskName": "系统架构与接口定义审查",
    "assigneeUserIds": ["ENG-1042"],
    "targetObjectType": "ModelRelease",
    "targetObjectId": 70918209182019,
    "targetBusinessCode": "REL-VMC1000-SYS-001",
    "projectId": 100293810293,
    "assignedAt": "2026-09-16T11:00:05Z"
  }
}
```

#### 事件二：`WorkflowDecisionGeneratedEvent`

- **触发时机**：流程正常到达终点，签发不可篡改的 `ApprovalDecision` 凭证。  
- **Payload 契约**：

JSON

```
{
  "eventId": 9812739102832,
  "eventType": "WorkflowDecisionGenerated",
  "aggregateType": "ApprovalDecision",
  "aggregateId": "881029481920",
  "payload": {
    "decisionTicketId": 881029481920,
    "workflowInstId": 99201488102,
    "targetObjectType": "ModelRelease",
    "targetObjectId": 70918209182019,
    "targetContentHash": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01",
    "finalConclusion": "APPROVED",
    "decidedAt": "2026-09-16T11:45:12.891Z"
  }
}
```

### 10. 验收测试矩阵 (Acceptance Testing Matrix)

| **用例编号**  | **上位验收对照  MD+ 1** | **测试场景与操作步骤**                                       | **预期判定结果 (Pass Criteria)**                             | **验证日志与断言依据  MD+ 1**                                |
| ------------- | ----------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **TC-M24-01** | **CST-M24-01**          | 试图在 Flowable JavaDelegate 中注入业务 Repository，直接调用 SQL `UPDATE plm_product.part_revision SET lifecycle_state = 'RELEASED'` | 数据库触发器 `fn_prevent_workflow_cross_mutation` 硬性阻断，事务回滚 | 抛出 SQL 异常代码 `23000`，确认工作流服务无权直接修改业务实体。 |
| **TC-M24-02** | **AT-16**               | 1. 提交零件设计草稿（Hash A）发起会签；  2. 审批流处于会签中，工程师在外部通过接口强制更新该草稿内容（变更为 Hash B）；  3. 审批人点击“同意”完成流程。 | 1. 触发器 `fn_check_content_hash_integrity` 探测到当前实体 Hash B != 流程初值 Hash A；  2. 拒绝生成 `APPROVED` 凭据，流程强行置为 `TERMINATED`（AT-16）。 | 接口抛出 `ERR_HASH_TAMPERING_DETECTED`，旧审批无法放行新内容，阻断误发布。 |
| **TC-M24-03** | **SoD-01**              | 工程师 A 提报 `ModelRelease` 申请，在流程配置中将下一节点审批人指定为工程师 A 自身 | `SoDTaskAssignmentListener` 拦截任务创建，剥夺其候选审批人资格 | 抛出 `SelfApprovalBlockedException`，记录自审违规风控审计日志。 |
| **TC-M24-04** | **M24-F04** / AT-30     | 模拟网络风暴，向业务回调接口连续快速发送 10 次包含相同 `actionId` 的流程完成确认请求 | 系统仅在第 1 次执行状态机迁移与凭证核销，后序 9 次均被幂等表命中并直接返回 200 OK | 业务主表版本号仅自增一次，绝对不产生重复升版或重复发件箱事件。 |
| **TC-M24-05** | **M24-F02** [cite: 2]   | 配置包含 3 位专业负责人的全票会签节点（UNANIMOUS），其中 1 人签署 `REJECT` [cite: 2] | 流程引擎立即中断后续流转，判定分支走向 `recordRejectionDelegate`，生成 `REJECTED` 决议[cite: 2] | 流程实例状态置为 `COMPLETED`，`finalConclusion = REJECTED`，业务维持 DRAFT 状态。 |
| **TC-M24-06** | **AT-04**               | 审批流已生成 `APPROVED` 凭据，但在业务回调推进阶段 MinIO 文件服务发生网络断连[cite: 1, 2] | 业务状态机抛错拦截，凭据保持 `is_consumed = FALSE`，发布状态保持未发布[cite: 1, 2] | 待 MinIO 恢复后重新触发回调，消费既有凭据成功激活发布，保证跨系统一致性[cite: 1, 2]。 |