-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M01-MSG 内部邮件与消息中心 (V1.9.0)
-- 适用环境: PostgreSQL 15+ (去外部协议化，以 PLM 业务对象为中心)
-- 包含: plm_msg Schema、消息根分类/子类型/箱体枚举、5张核心表、丰富典型机床研制邮件种子数据
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_msg;

-- 消息根分类枚举
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'message_root_category' AND n.nspname = 'plm_msg') THEN
        CREATE TYPE plm_msg.message_root_category AS ENUM (
            'MANUAL',  -- 人工普通邮件
            'SYSTEM'   -- PLM 业务事件系统生成通知
        );
    END IF;
END $$;

-- 业务子分类枚举
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'system_message_type' AND n.nspname = 'plm_msg') THEN
        CREATE TYPE plm_msg.system_message_type AS ENUM (
            'WORKFLOW',         -- 工作流审批通知
            'WORKFLOW_CHANGE',  -- 流程模板/规则变更通知
            'TASK',             -- 任务分配/延期/完成通知
            'REVIEW',           -- 项目阶段门/技术评审状态通知
            'CHANGE',           -- 工程问题/ECR/ECO 状态与处置通知
            'DOCUMENT',         -- 文档/图纸修订与发布通知
            'BOM',              -- EBOM/MBOM/规则结构变更通知
            'BASELINE',         -- 基线固化与冻结通知
            'COMMENT'           -- 协同批注与@提醒通知
        );
    END IF;
END $$;

-- 邮箱箱体枚举
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'mailbox_box_type' AND n.nspname = 'plm_msg') THEN
        CREATE TYPE plm_msg.mailbox_box_type AS ENUM (
            'INBOX',    -- 收件箱
            'OUTBOX',   -- 发件箱 (已发送)
            'DRAFT',    -- 草稿箱
            'ARCHIVE'   -- 已归档
        );
    END IF;
END $$;

-- 1. 消息主内容表 (MessageMaster - 物理正文单一存储，支持多播引用)
CREATE TABLE IF NOT EXISTS plm_msg.msg_master (
    message_id              BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_ENTERPRISE',
    root_category           plm_msg.message_root_category NOT NULL DEFAULT 'MANUAL',
    sub_type                plm_msg.system_message_type NULL, -- 系统消息必填，人工消息可为空
    subject                 VARCHAR(512) NOT NULL,
    body_content            TEXT NOT NULL,                     -- 结构化正文
    sender_id               VARCHAR(64) NOT NULL,              -- 人工发件人 userId，或 "SYSTEM_SERVICE"
    is_system_generated     BOOLEAN NOT NULL DEFAULT FALSE,
    priority                VARCHAR(16) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    
    -- 强工程上下文绑定 (M01-M30 统一追踪要素)
    related_project_id      BIGINT NULL,                       -- 归属研发项目
    related_obj_type        VARCHAR(64) NULL,                  -- PartRevision, ChangeOrder, Task 等
    related_obj_id          BIGINT NULL,                       -- 目标业务实体主键
    target_action_url       VARCHAR(1024) NULL,                -- 前端单点跳转路由
    correlation_event_id    VARCHAR(128) NULL,                 -- 关联的 Kafka Outbox 事件 ID
    
    thread_id               BIGINT NULL,                       -- 邮件会话聚合 ID
    quoted_message_id       BIGINT NULL REFERENCES plm_msg.msg_master(message_id), -- 引用回复的原信
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_msg.msg_master IS 'M01-MSG: 邮件正文元数据主表，采用不可变多播设计';
CREATE INDEX IF NOT EXISTS idx_msg_master_event ON plm_msg.msg_master(correlation_event_id) WHERE correlation_event_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_msg_master_obj ON plm_msg.msg_master(related_obj_type, related_obj_id);

-- 2. 用户个性化投递箱表 (UserMailboxItem - 承载单用户的箱体状态、标记与已读状态)
CREATE TABLE IF NOT EXISTS plm_msg.msg_user_box (
    item_id                 BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_ENTERPRISE',
    user_id                 VARCHAR(64) NOT NULL,              -- 所属用户工号
    message_id              BIGINT NOT NULL REFERENCES plm_msg.msg_master(message_id) ON DELETE CASCADE,
    box_type                plm_msg.mailbox_box_type NOT NULL DEFAULT 'INBOX',
    
    is_read                 BOOLEAN NOT NULL DEFAULT FALSE,
    read_at                 TIMESTAMPTZ NULL,
    is_starred              BOOLEAN NOT NULL DEFAULT FALSE,    -- 星标置顶
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,    -- 放入回收站标记 (软删除)
    deleted_at              TIMESTAMPTZ NULL,
    
    custom_tags             JSONB NOT NULL DEFAULT '[]'::jsonb, -- 自定义标签数组，如 ["待处理", "关键核心"]
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_message_box UNIQUE (user_id, message_id, box_type)
);
COMMENT ON TABLE plm_msg.msg_user_box IS 'M01-MSG: 用户个人邮箱箱体明细表，承载已读、星标与标签';
CREATE INDEX IF NOT EXISTS idx_user_box_status ON plm_msg.msg_user_box(user_id, box_type, is_read, is_deleted, is_starred);

-- 3. 消息收发人明细快照表 (MessageRecipient - 记录全量投递关系)
CREATE TABLE IF NOT EXISTS plm_msg.msg_recipient (
    recipient_record_id     BIGINT PRIMARY KEY,
    message_id              BIGINT NOT NULL REFERENCES plm_msg.msg_master(message_id) ON DELETE CASCADE,
    recipient_type          VARCHAR(16) NOT NULL CHECK (recipient_type IN ('TO', 'CC', 'BCC')),
    recipient_user_id       VARCHAR(64) NOT NULL,
    display_name            VARCHAR(128) NOT NULL,
    delivery_status         VARCHAR(32) NOT NULL DEFAULT 'DELIVERED', -- DELIVERED, FAILED
    delivered_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_recipient_lookup ON plm_msg.msg_recipient(message_id, recipient_type);

-- 4. 邮件物理附件绑定表 (MessageAttachment - 依托 M19 制品服务)
CREATE TABLE IF NOT EXISTS plm_msg.msg_attachment (
    attachment_id           BIGINT PRIMARY KEY,
    message_id              BIGINT NOT NULL REFERENCES plm_msg.msg_master(message_id) ON DELETE CASCADE,
    artifact_id             BIGINT NULL,                       -- 关联 M19 不可变制品
    file_display_name       VARCHAR(255) NOT NULL,
    file_size_bytes         BIGINT NOT NULL,
    uploaded_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_msg.msg_attachment IS 'M01-MSG: 邮件附件关联表，物理存储复用 MinIO 防篡改制品';

-- 5. 消息防篡改与安全审计日志表 (MessageAuditLog)
CREATE TABLE IF NOT EXISTS plm_msg.msg_audit_log (
    audit_id                BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_ENTERPRISE',
    action_type             VARCHAR(64) NOT NULL, -- SEND_MANUAL, DISPATCH_SYSTEM, RETRACT, HARD_DELETE
    operator_id             VARCHAR(64) NOT NULL,
    target_message_id       BIGINT NOT NULL,
    recipient_summary       TEXT NULL,
    client_ip               VARCHAR(64) NOT NULL,
    is_violation_blocked    BOOLEAN NOT NULL DEFAULT FALSE,
    blocked_reason          TEXT NULL,
    recorded_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_msg_audit_query ON plm_msg.msg_audit_log(target_message_id, action_type);

-- =============================================================================
-- 种子数据填充 (Seed Data) - 代表性机床研制通知与协作邮件
-- =============================================================================

-- 1. 消息主内容 (MessageMaster)
INSERT INTO plm_msg.msg_master (
    message_id, root_category, sub_type, subject, body_content, sender_id, is_system_generated, priority,
    related_project_id, related_obj_type, related_obj_id, target_action_url, correlation_event_id, created_at
) VALUES 
    (
        8019284102901, 'SYSTEM', 'WORKFLOW', '【待办审批】主轴部件 Rev B 结构提请会签审批',
        '<p><strong>业务说明：</strong>高速机床主轴刚度优化设计已完成，现将最新装配 EBOM 结构与 CAD 拓扑工程图提请会签审核，请复核动密封及轴承配合公差。</p><p><strong>发起人：</strong>李明 (系统架构师)</p><p><strong>受控对象：</strong>PartRevision (编码: M-VMC1000-SPN-01B)</p>',
        'SYSTEM_SERVICE', TRUE, 'HIGH',
        100293810293, 'PartRevision', 70192841001, 'change-mgmt', 'evt-wf-001', '2026-09-16 10:14:22+08'
    ),
    (
        8019284102902, 'SYSTEM', 'TASK', '【任务指派】您已被指定为关键任务责任人: 主轴动密封公差配合计算',
        '<p><strong>所属项目：</strong>VMC1000 高速立式加工中心研发项目</p><p><strong>计划工期：</strong>2026-09-16 至 2026-09-25</p><p><strong>交付物要求：</strong>输出密封圈压缩比分析报告与三维装配配隙工程图。</p>',
        'SYSTEM_SERVICE', TRUE, 'NORMAL',
        100293810293, 'Task', 600192841002, 'my-tasks', 'evt-tsk-002', '2026-09-16 09:30:00+08'
    ),
    (
        8019284102903, 'SYSTEM', 'CHANGE', '【工程变更】ECO-2026-0042 正式发布通知 (主轴提速至15000rpm)',
        '<p><strong>变更指令：</strong>经 CCB 委员会审定，VMC1000 主轴提速工程变更指令正式签署下发。涉及主轴芯轴重构、动平衡等级提升至 G1.0 及相关工装报废。</p><p><strong>生效策略：</strong>在制批次 LOT-2026-09A 执行用尽切换，库存毛坯报废。</p>',
        'SYSTEM_SERVICE', TRUE, 'HIGH',
        100293810293, 'ChangeOrder', 500192841003, 'change-mgmt', 'evt-eco-003', '2026-09-15 17:20:15+08'
    ),
    (
        8019284102904, 'SYSTEM', 'REVIEW', '【阶段门决议】PDR 初步设计评审达成决议: PASS 准予进入详细设计',
        '<p><strong>决议审查结论：</strong>PDR 阶段门审查全员会签通过，遗留 2 项非关键行动项已纳管跟踪。全机床 EBOM 解锁进入详细设计阶段。</p><p><strong>签署人：</strong>张建国 (机械总工/项目总师)</p>',
        'SYSTEM_SERVICE', TRUE, 'NORMAL',
        100293810293, 'GateDecision', 400192841004, 'project-mgmt-overview', 'evt-gate-004', '2026-09-15 14:00:00+08'
    ),
    (
        8019284102905, 'MANUAL', NULL, '关于 VMC1000 五轴联动转台法兰配合尺寸的技术讨论',
        '<p>张总工、李工：针对德国供应商提供的直驱转台接口协议，法兰止口配合尺寸建议由 H7/h6 调整为 H7/k6，以避免五轴重切削工况下的微动磨损。附图纸测绘草案供评审。</p>',
        'ENG-2048', FALSE, 'NORMAL',
        100293810293, 'PartRevision', 70192841005, 'document-mgmt', NULL, '2026-09-14 16:45:00+08'
    )
ON CONFLICT (message_id) DO NOTHING;

-- 2. 个人投递箱关联 (UserMailboxItem) - 面向管理员 admin 与总工张建国
INSERT INTO plm_msg.msg_user_box (
    item_id, user_id, message_id, box_type, is_read, read_at, is_starred, is_deleted, custom_tags, created_at
) VALUES 
    (9018290182901, 'admin', 8019284102901, 'INBOX', FALSE, NULL, TRUE, FALSE, '["待处理", "关键核心"]'::jsonb, '2026-09-16 10:14:22+08'),
    (9018290182902, 'admin', 8019284102902, 'INBOX', FALSE, NULL, FALSE, FALSE, '["研发任务"]'::jsonb, '2026-09-16 09:30:00+08'),
    (9018290182903, 'admin', 8019284102903, 'INBOX', TRUE, '2026-09-16 08:00:00+08', TRUE, FALSE, '["变更下发", "高优关注"]'::jsonb, '2026-09-15 17:20:15+08'),
    (9018290182904, 'admin', 8019284102904, 'INBOX', TRUE, '2026-09-15 16:00:00+08', FALSE, FALSE, '["阶段门决议"]'::jsonb, '2026-09-15 14:00:00+08'),
    (9018290182905, 'admin', 8019284102905, 'INBOX', FALSE, NULL, FALSE, FALSE, '["技术答疑"]'::jsonb, '2026-09-14 16:45:00+08'),
    -- 张建国收件箱
    (9018290182911, 'ENG-2048', 8019284102901, 'INBOX', FALSE, NULL, TRUE, FALSE, '["待审批"]'::jsonb, '2026-09-16 10:14:22+08'),
    (9018290182912, 'ENG-2048', 8019284102905, 'OUTBOX', TRUE, '2026-09-14 16:45:00+08', FALSE, FALSE, '["已发送"]'::jsonb, '2026-09-14 16:45:00+08')
ON CONFLICT (item_id) DO NOTHING;

-- 3. 投递人明细快照 (MessageRecipient)
INSERT INTO plm_msg.msg_recipient (
    recipient_record_id, message_id, recipient_type, recipient_user_id, display_name, delivery_status, delivered_at
) VALUES 
    (7018290182901, 8019284102901, 'TO', 'admin', '系统管理员', 'DELIVERED', '2026-09-16 10:14:22+08'),
    (7018290182902, 8019284102901, 'TO', 'ENG-2048', '张建国 (机械总工)', 'DELIVERED', '2026-09-16 10:14:22+08'),
    (7018290182903, 8019284102902, 'TO', 'admin', '系统管理员', 'DELIVERED', '2026-09-16 09:30:00+08'),
    (7018290182904, 8019284102903, 'TO', 'admin', '系统管理员', 'DELIVERED', '2026-09-15 17:20:15+08'),
    (7018290182905, 8019284102904, 'TO', 'admin', '系统管理员', 'DELIVERED', '2026-09-15 14:00:00+08'),
    (7018290182906, 8019284102905, 'TO', 'admin', '系统管理员', 'DELIVERED', '2026-09-14 16:45:00+08')
ON CONFLICT (recipient_record_id) DO NOTHING;

-- 4. 邮件物理附件样本 (MessageAttachment)
INSERT INTO plm_msg.msg_attachment (
    attachment_id, message_id, artifact_id, file_display_name, file_size_bytes, uploaded_at
) VALUES 
    (6018290182901, 8019284102901, 9001, 'VMC1000_Spindle_FEM_Report.pdf', 4404019, '2026-09-16 10:14:00+08'),
    (6018290182902, 8019284102905, 9002, 'Spindle_Flange_Tolerance_Drawing.dxf', 1258291, '2026-09-14 16:44:00+08')
ON CONFLICT (attachment_id) DO NOTHING;

-- 5. 消息安全审计日志样本 (MessageAuditLog)
INSERT INTO plm_msg.msg_audit_log (
    audit_id, action_type, operator_id, target_message_id, recipient_summary, client_ip, is_violation_blocked, blocked_reason, recorded_at
) VALUES 
    (5018290182901, 'DISPATCH_SYSTEM', 'SYSTEM_SERVICE', 8019284102901, 'admin, ENG-2048', '127.0.0.1', FALSE, NULL, '2026-09-16 10:14:22+08'),
    (5018290182902, 'SEND_MANUAL', 'ENG-2048', 8019284102905, 'admin', '192.168.1.102', FALSE, NULL, '2026-09-14 16:45:00+08')
ON CONFLICT (audit_id) DO NOTHING;
