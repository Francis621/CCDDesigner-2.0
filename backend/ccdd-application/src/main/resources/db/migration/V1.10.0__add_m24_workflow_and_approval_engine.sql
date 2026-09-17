-- =============================================================================
-- CCDDesigner 2.0 数据库迁移脚本: V1.10.0
-- 模块编号: M24 工作流与审批 (Workflow and Engineering Approvals)
-- 适用环境: PostgreSQL 15+ (Flowable 7.x 嵌入式集成与业务解耦架构)
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_workflow;

-- 1. 枚举类型定义
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'instance_status' AND typnamespace = 'plm_workflow'::regnamespace) THEN
        CREATE TYPE plm_workflow.instance_status AS ENUM (
            'RUNNING',      -- 流程流转中
            'COMPLETED',    -- 流程正常结束 (已达成最终决议)
            'TERMINATED',   -- 因内容篡改、手动撤回或异常被强行终止
            'SUSPENDED'     -- 管理员挂起
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'approval_conclusion' AND typnamespace = 'plm_workflow'::regnamespace) THEN
        CREATE TYPE plm_workflow.approval_conclusion AS ENUM (
            'APPROVED',     -- 审查全票/达标通过
            'REJECTED',     -- 驳回不通过 (打回草稿)
            'WITHDRAWN'     -- 发起人主动撤回
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sign_strategy' AND typnamespace = 'plm_workflow'::regnamespace) THEN
        CREATE TYPE plm_workflow.sign_strategy AS ENUM (
            'UNANIMOUS',    -- 一票否决制 (必须全员通过)
            'PERCENTAGE',   -- 比例通过制 (如赞成票 >= 80%)
            'FIRST_WINS'    -- 首人决定制 (任意一人签署即流转)
        );
    END IF;
END $$;

-- 2. 业务对象与流程定义版本化绑定表 (WorkflowDefinitionBinding)
CREATE TABLE IF NOT EXISTS plm_workflow.definition_binding (
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

-- 3. PLM 流程实例快照表 (WorkflowInstance - 桥接 Flowable 运行实例)
CREATE TABLE IF NOT EXISTS plm_workflow.workflow_instance (
    workflow_inst_id        BIGINT PRIMARY KEY,
    flowable_proc_inst_id   VARCHAR(64) NOT NULL UNIQUE, -- 对应的 Flowable ProcessInstanceId
    binding_id              BIGINT NOT NULL REFERENCES plm_workflow.definition_binding(binding_id),
    target_object_type      VARCHAR(64) NOT NULL,
    target_object_id        BIGINT NOT NULL,             -- 关联被审实体主键 (如 revision_id / order_id)
    target_business_code    VARCHAR(128) NOT NULL,
    target_content_hash     CHAR(64) NOT NULL,           -- 启动审批时捕获的快照 SHA-256 (AT-16 防线)
    project_id              VARCHAR(64) NULL,            -- 所属工程研发项目标识
    initiator_id            VARCHAR(64) NOT NULL,        -- 流程发起人工号
    status                  plm_workflow.instance_status NOT NULL DEFAULT 'RUNNING',
    conclusion              plm_workflow.approval_conclusion NULL,
    termination_reason      TEXT NULL,
    started_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at            TIMESTAMPTZ NULL
);
COMMENT ON TABLE plm_workflow.workflow_instance IS 'M24: 流程实例台账，固化被审对象哈希与 Flowable 实例映射';
CREATE INDEX IF NOT EXISTS idx_wf_inst_target ON plm_workflow.workflow_instance(target_object_type, target_object_id, status);

-- 4. 不可伪造审批决策凭据表 (ApprovalDecision - 核心法律凭证)
CREATE TABLE IF NOT EXISTS plm_workflow.approval_decision (
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
CREATE INDEX IF NOT EXISTS idx_decision_hash_query ON plm_workflow.approval_decision(target_object_id, target_content_hash, final_conclusion);

-- 5. 节点审批动作与意见明细记录表 (WorkflowActionRecord)
CREATE TABLE IF NOT EXISTS plm_workflow.workflow_action_record (
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
CREATE INDEX IF NOT EXISTS idx_action_record_inst ON plm_workflow.workflow_action_record(workflow_inst_id, flowable_task_id);

-- 6. 业务回调幂等防重表 (WorkflowCallbackIdempotency - 防止网络风暴与重复回调)
CREATE TABLE IF NOT EXISTS plm_workflow.callback_idempotency (
    idempotency_key         VARCHAR(128) PRIMARY KEY,    -- 格式: "ACTION:{workflowInstId}:{eventVersion}"
    workflow_inst_id        BIGINT NOT NULL REFERENCES plm_workflow.workflow_instance(workflow_inst_id),
    callback_type           VARCHAR(64) NOT NULL,        -- NOTIFY_RELEASE, NOTIFY_REJECT, SYNC_TASK
    execution_status        VARCHAR(32) NOT NULL DEFAULT 'PROCESSING', -- PROCESSING, SUCCESS, FAILED
    error_message           TEXT NULL,
    processed_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_workflow.callback_idempotency IS 'M24: 状态机异步回调幂等控制表';

-- 7. 注入流程绑定配置种子数据
INSERT INTO plm_workflow.definition_binding (
    binding_id, target_object_type, business_category, flowable_proc_def_key, proc_def_version, sign_strategy, pass_threshold_percent, is_active, binding_description
) VALUES
(101, 'ModelRelease', 'STANDARD_RELEASE', 'PROC_MODEL_RELEASE_APPROVAL', 1, 'UNANIMOUS', NULL, TRUE, '数控机床多专业系统架构与接口定义四级受控会签流'),
(102, 'ChangeOrder', 'MAJOR_CHANGE', 'PROC_ECO_CHANGE_APPROVAL', 1, 'UNANIMOUS', NULL, TRUE, '高端机床重大工程变更单(ECO)跨学科与CCB处置审批流')
ON CONFLICT (binding_id) DO NOTHING;

-- 8. 注入典型机床研制种子流程数据
-- 种子 1: 流转中的 ECO-2026-0042 会签审批流 (当前正在进行跨专业主管会签)
INSERT INTO plm_workflow.workflow_instance (
    workflow_inst_id, flowable_proc_inst_id, binding_id, target_object_type, target_object_id,
    target_business_code, target_content_hash, project_id, initiator_id, status, started_at
) VALUES (
    77001, 'prc_inst_eco_0042', 102, 'ChangeOrder', 8001,
    'ECO-2026-0042', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    'VMC_ENTERPRISE', 'chief_designer', 'RUNNING', CURRENT_TIMESTAMP - INTERVAL '2 hours'
) ON CONFLICT (workflow_inst_id) DO NOTHING;

-- 种子 1 的已处理动作流水
INSERT INTO plm_workflow.workflow_action_record (
    action_record_id, workflow_inst_id, flowable_task_id, task_node_name, operator_id, action_type, comment_text, voted_at
) VALUES (
    78001, 77001, 'task_eco_mech_01', '机械系统主管工程师审查', 'lead_analyst', 'APPROVE',
    '已完成陶瓷球轴承与主轴套筒配合公差复核，刚度与预紧力设计满足15000rpm工况要求，同意实施。', CURRENT_TIMESTAMP - INTERVAL '1 hour'
) ON CONFLICT (action_record_id) DO NOTHING;

-- 种子 2: 已完成且已生成签名凭据的历史决议: VMC1000 架构基线发布 (REL-VMC1000-SYS-001)
INSERT INTO plm_workflow.workflow_instance (
    workflow_inst_id, flowable_proc_inst_id, binding_id, target_object_type, target_object_id,
    target_business_code, target_content_hash, project_id, initiator_id, status, conclusion, started_at, completed_at
) VALUES (
    77002, 'prc_inst_rel_0001', 101, 'ModelRelease', 5001,
    'REL-VMC1000-SYS-001', '8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01',
    'VMC_ENTERPRISE', 'sys_architect', 'COMPLETED', 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
) ON CONFLICT (workflow_inst_id) DO NOTHING;

INSERT INTO plm_workflow.approval_decision (
    decision_ticket_id, workflow_inst_id, target_object_type, target_object_id, target_content_hash,
    final_conclusion, is_consumed, consumed_at, consumed_by_action, crypto_signature_stamp, signed_payload_digest, decided_at
) VALUES (
    88001, 77002, 'ModelRelease', 5001, '8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01',
    'APPROVED', TRUE, CURRENT_TIMESTAMP - INTERVAL '2 days', 'CONSUME-ACT-M06-REL-5001',
    'SM2_SIG_304502204c382901820192a830192830192830192830192830192830192830192a019283',
    'a7c2b3e891238491820391820391820391820391820391820391820391820391', CURRENT_TIMESTAMP - INTERVAL '2 days'
) ON CONFLICT (decision_ticket_id) DO NOTHING;

INSERT INTO plm_workflow.callback_idempotency (
    idempotency_key, workflow_inst_id, callback_type, execution_status, processed_at
) VALUES (
    'ACTION:77002:v1', 77002, 'NOTIFY_RELEASE', 'SUCCESS', CURRENT_TIMESTAMP - INTERVAL '2 days'
) ON CONFLICT (idempotency_key) DO NOTHING;
