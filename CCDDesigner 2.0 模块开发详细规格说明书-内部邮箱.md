# CCDDesigner 2.0 模块开发详细规格说明书

## M01-MSG: PLM 内部邮件与消息中心 (Internal Mail & Notification Center)

| **文档属性**    | **内容**                                                     |
| --------------- | ------------------------------------------------------------ |
| **模块编号**    | `M01-MSG` (归属 M01 统一工作台与协同域，协同 M20/M24/M30, Phase: P1, Type: N) |
| **文档编号**    | `CCD-DEV-SPEC-2.0-M01-MSG`                                   |
| **版本 / 状态** | V1.0 / 评审发布稿                                            |
| **主责模块**    | M01 统一工作台与检索（消息中心子系统）                       |
| **协同模块**    | M02 (项目与任务)、M03 (需求)、M06 (模型发布)、M11 (验证评估)、M16 (EBOM)、M19 (文件制品)、M21 (基线)、M22 (变更)、M24 (工作流)、M30 (身份与权限) |
| **上位依据**    | 《CCDDesigner 2.0 产品说明书》§2.2, §30    《CCDDesigner 2.0 产品功能架构与模块设计说明书》§9, §30, §38, §41.3    《CCDDesigner 2.0 产品开发说明书》§1.2, §4.1, §6.3, §7.1 |
| **适用受众**    | 全栈开发工程师、前端架构师、流程引擎实施工程师、安全合规审计员 |

### 1. 模块定位与核心设计原则

依据 CCDDesigner 2.0 平台总体架构，内部邮件系统定位为 **以 PLM 受控工程对象为中心的内部统一通知、沟通、协作与审计中心**，服务于多专业团队协作、流程驱动与状态追踪：  

1. **去外部协议化与数据自闭环原则**：
   - 本系统是企业 PLM 内部消息系统，**完全不接入且不依赖任何外部邮件协议（如 SMTP、POP3、IMAP）**，规避外网端口暴露与数据外泄安全风险。  
   - 全量数据基于 PostgreSQL 关系表、Kafka 事件队列与本地发件箱（Transactional Outbox）实现可靠传输与持久化归档。  
2. **以 PLM 业务对象为中心（Object-Centric Linkage）**：
   - 消息不再是孤立的纯文本，其核心元数据中强制固化目标业务对象的统一工程上下文（`objectType`、`masterId`、`revisionId`、`projectId` 及业务页面直接跳转 URI）。  
   - 业务消息支持一键直达处理视图（如流程审批、变更处置、任务看板、图纸比对）。  
3. **严格继承 PLM 权限与防穿透防护（PBAC Security Inheritance）**：
   - **通知权限与数据权限隔离**：用户在收件箱中看到通知摘要，仅代表其拥有“接收消息”的权利；点击业务链接时，后端 API 必须再次严格穿透校验当前用户针对目标工程对象的 PBAC 权限与生命周期状态。无权人员即使接收到消息，也严禁越权读取图纸、BOM 或变更单敏感数据。  
   - **发信防伪造**：系统消息（System Message）由受信任的内部微服务/事件处理器通过安全上下文生成，严格禁止普通客户端通过 REST API 伪造系统发送人身份。  
   - **管理员权限最小化审计**：平台管理员可审计消息投递流水、送达率与安全日志，但默认无权检索或偷窥员工间点对点通信的私密正文。  
4. **内部邮件与待办事项（TodoItem）彻底解耦**：
   - **内部邮件（Message）**：反映事实通知、技术交流、流转历史与知会记录，状态维度为“未读/已读/星标/归档”。  
   - **待办事项（TodoItem）**：反映由 M02 任务或 M24 审批流派发的、需要用户执行法定动作的工作指令，状态维度为“待处理/处理中/已完成/已驳回”。  
   - **联动约束**：用户阅读或删除了“任务分配”或“审批通知”邮件，**绝不自动等价于任务完成或流程获批**，待办动作必须在对应业务页面经合法签署后闭环。  

### 2. 双向追踪矩阵 (Bidirectional Traceability Matrix)

| **功能编号 / 触发场景**            | **主责与协同模块  MD+ 1** | **业务事件契约 (Event Schema)  MD+ 1**                       | **覆盖验收用例  MD+ 1** | **核心控制逻辑与阻断行为**                                   |
| ---------------------------------- | ------------------------- | ------------------------------------------------------------ | ----------------------- | ------------------------------------------------------------ |
| **MSG-F01** (邮箱基础容器与状态)   | M01-MSG                   | N/A (内部事务操作)                                           | AT-13                   | 收件箱、发件箱、草稿箱、归档箱隔离；物理软删除；多维标签与全文检索。 |
| **MSG-F02** (内部人员通信)         | M01-MSG, M30              | `DirectMessageSentEvent`                                     | AT-13                   | 点对点、抄送；支持按部门、角色、项目组动态解析收件名单；支持引用原信回复。 |
| **MSG-F03** (工作流与审批通知)     | M01-MSG, M24              | `WorkflowTaskAssignedEvent`  `WorkflowInstanceCompletedEvent` | AT-04, AT-16            | 审批提报触发投递；携带不可篡改摘要；附带审批直达路由及候选包版本号。 |
| **MSG-F04** (研发任务与阶段门通知) | M01-MSG, M02              | `TaskAssignedEvent`  `GateDecisionRecordedEvent`             | AT-15                   | 任务指派、逾期黄橙警报通知；阶段门通过/返工决议抄送全项目组成员。 |
| **MSG-F05** (工程变更与影响通知)   | M01-MSG, M22              | `ImpactDispositionRequiredEvent`  `ChangeOrderReleasedEvent` | AT-10, AT-22            | 候选影响指派通知专业主管处置；ECO 发布后通知在制订单、工厂及维保责任人。 |
| **MSG-F06** (工程发布与基线通知)   | M01-MSG, M06, M21         | `ModelReleasePublishedEvent`  `BaselineFrozenEvent`          | AT-01, AT-14, AT-25     | 系统模型正式发布、CDR/交付基线冻结完成全员广播，锁定版本快照防漂移。 |
| **MSG-SEC-01** (防穿透与资质审计)  | M01-MSG, M30              | N/A (PBAC 切面拦截)                                          | AT-07, AT-13            | 点击消息卡片时强验当前 PBAC 策略；无权限时抛出 403 并记录越权探测日志。 |

### 3. 消息分类体系与业务触发规则

系统将内部邮件严格划分为两大根类别（`MANUAL` 与 `SYSTEM`），并对系统消息按业务域实施细粒度类型派生：  

代码段

```
classDiagram
    class BaseMessage {
        +Long messageId
        +String subject
        +MessageRootCategory rootCategory
        +String senderId
        +Boolean isSystemGenerated
        +EngineeringContext contextRef
    }
    class ManualMessage {
        +List~String~ toUserIds
        +List~String~ ccUserIds
        +String threadId
        +Long quotedMessageId
    }
    class SystemMessage {
        +SystemMessageType subType
        +String businessEventId
        +String targetActionUrl
        +JsonMap payloadDigest
    }
    BaseMessage <|-- ManualMessage
    BaseMessage <|-- SystemMessage
```

#### 3.1 消息类型明细字典

| **根类型** | **子类型编码 (subType)** | **业务触发源模块  MD+ 1** | **触发业务场景与时机  MD+ 1**           | **接收人解析规则 (Recipient Resolver)**                  | **默认优先级** | **快捷操作指令** |
| ---------- | ------------------------ | ------------------------- | --------------------------------------- | -------------------------------------------------------- | -------------- | ---------------- |
| **MANUAL** | `USER_DIRECT`            | M01-MSG                   | 工程师间主动沟通、设计答疑              | 用户显式指定的收件人、抄送人（支持按部门/角色/项目展开） | NORMAL         | 回复、转发、引用 |
| **SYSTEM** | `WORKFLOW`               | M24 (工作流)              | 流程发起、待办加签、节点流转            | 当前流程节点的法定审批人 / 会签人名单                    | HIGH           | 进入流程审批页   |
| **SYSTEM** | `WORKFLOW_CHANGE`        | M24, M30                  | 流程模板调整、版本升级、规则替换        | 受影响流程所属项目经理、当前存量流转参与者               | URGENT         | 查看流程变更单   |
| **SYSTEM** | `TASK`                   | M02 (项目管理)            | 任务派发、计划延期、任务提交            | 任务责任人（Assignee）、执行助理及任务关注人             | NORMAL / HIGH  | 查看任务详情     |
| **SYSTEM** | `REVIEW`                 | M02 (阶段门)              | 阶段门评审开启、达成决议（PASS/REWORK） | 评审委员会成员、项目总师、项目组成员                     | HIGH           | 查看阶段门决议   |
| **SYSTEM** | `CHANGE`                 | M22 (变更管理)            | ECR 签发、候选处置指派、ECO 批准实施    | 被波及专业的责任主管、CCB 委员、制造/现场联络员          | HIGH           | 进入变更处置台   |
| **SYSTEM** | `DOCUMENT`               | M19 (图文档)              | 图纸升版发布、设计撤销作废、轻量化就绪  | 借阅人、关联零部件主设计人、项目图文档负责员             | NORMAL         | 查看图纸与批注   |
| **SYSTEM** | `BOM`                    | M14, M16, M25             | 150% BOM 规则更新、EBOM/MBOM 变更       | 下游工艺工程师、模块槽位变体维护工程师                   | HIGH           | 打开结构红线对比 |
| **SYSTEM** | `BASELINE`               | M21 (基线)                | 阶段基线（CDR/As-Designed）冻结         | 全项目组成员、配置管理员、质量部代表                     | NORMAL         | 查阅冻结基线闭包 |
| **SYSTEM** | `COMMENT`                | 全域通用                  | 协同设计协作批注、评论 @相关人员        | 被 @提及人员、关联业务对象当前修订的 Owner               | NORMAL         | 定位协同批注图层 |

### 4. 物理数据字典与 PostgreSQL DDL 规范

以下物理 DDL 属于 `plm_msg` 独立数据 Schema，建立在 CCDDesigner 2.0 基础存储底座之上，杜绝数据堆积与单点瓶颈：  

SQL

```
-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M01-MSG 内部邮件与消息中心
-- 适用环境: PostgreSQL 15+
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_msg;

-- 消息大类枚举
CREATE TYPE plm_msg.message_root_category AS ENUM (
    'MANUAL',  -- 人工普通邮件
    'SYSTEM'   -- PLM 业务事件系统生成通知
);

-- 业务子分类枚举
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

-- 邮箱箱体枚举
CREATE TYPE plm_msg.mailbox_box_type AS ENUM (
    'INBOX',    -- 收件箱
    'OUTBOX',   -- 发件箱 (已发送)
    'DRAFT',    -- 草稿箱
    'ARCHIVE'   -- 已归档
);

-- 1. 消息主内容表 (MessageMaster - 物理正文单一存储，支持多播引用)
CREATE TABLE plm_msg.msg_master (
    message_id              BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_ENTERPRISE',
    root_category           plm_msg.message_root_category NOT NULL DEFAULT 'MANUAL',
    sub_type                plm_msg.system_message_type NULL, -- 系统消息必填，人工消息可为空
    subject                 VARCHAR(512) NOT NULL,
    body_content            TEXT NOT NULL,                     -- HTML / Markdown 结构化正文
    sender_id               VARCHAR(64) NOT NULL,              -- 人工发件人 userId，或 "SYSTEM_SERVICE"
    is_system_generated     BOOLEAN NOT NULL DEFAULT FALSE,
    priority                VARCHAR(16) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    
    -- 强工程上下文绑定 (M01-M30 统一追踪要素)
    related_project_id      BIGINT NULL,                       -- 归属研发项目
    related_obj_type        VARCHAR(64) NULL,                  -- PartRevision, ChangeOrder, Baseline 等
    related_obj_id          BIGINT NULL,                       -- 目标业务实体 Snowflake 主键
    target_action_url       VARCHAR(1024) NULL,                -- 前端单点跳转路由
    correlation_event_id    VARCHAR(128) NULL,                 -- 关联的 Kafka Outbox 事件 ID
    
    thread_id               BIGINT NULL,                       -- 邮件会话聚合 ID
    quoted_message_id       BIGINT NULL REFERENCES plm_msg.msg_master(message_id), -- 引用回复的原信
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_msg.msg_master IS 'M01-MSG: 邮件正文元数据主表，采用不可变多播设计';
CREATE INDEX idx_msg_master_event ON plm_msg.msg_master(correlation_event_id) WHERE correlation_event_id IS NOT NULL;
CREATE INDEX idx_msg_master_obj ON plm_msg.msg_master(related_obj_type, related_obj_id);

-- 2. 用户个性化投递箱表 (UserMailboxItem - 承载单用户的箱体状态、标记与已读状态)
CREATE TABLE plm_msg.msg_user_box (
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
    
    custom_tags             JSONB NOT NULL DEFAULT '[]'::jsonb, -- 自定义标签数组，如 ["待处理", "质量问题", "项目A"]
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_message_box UNIQUE (user_id, message_id, box_type)
);
COMMENT ON TABLE plm_msg.msg_user_box IS 'M01-MSG: 用户个人邮箱箱体明细表，承载已读、星标与标签';
CREATE INDEX idx_user_box_status ON plm_msg.msg_user_box(user_id, box_type, is_read, is_deleted, is_starred);
CREATE INDEX idx_user_box_tags_gin ON plm_msg.msg_user_box USING GIN (custom_tags);

-- 3. 消息收发人明细快照表 (MessageRecipient - 记录全量投递关系)
CREATE TABLE plm_msg.msg_recipient (
    recipient_record_id     BIGINT PRIMARY KEY,
    message_id              BIGINT NOT NULL REFERENCES plm_msg.msg_master(message_id) ON DELETE CASCADE,
    recipient_type          VARCHAR(16) NOT NULL CHECK (recipient_type IN ('TO', 'CC', 'BCC')),
    recipient_user_id       VARCHAR(64) NOT NULL,
    display_name            VARCHAR(128) NOT NULL,
    delivery_status         VARCHAR(32) NOT NULL DEFAULT 'DELIVERED', -- DELIVERED, FAILED
    delivered_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_recipient_lookup ON plm_msg.msg_recipient(message_id, recipient_type);

-- 4. 邮件物理附件绑定表 (MessageAttachment - 依托 M19 制品服务)
CREATE TABLE plm_msg.msg_attachment (
    attachment_id           BIGINT PRIMARY KEY,
    message_id              BIGINT NOT NULL REFERENCES plm_msg.msg_master(message_id) ON DELETE CASCADE,
    artifact_id             BIGINT NOT NULL REFERENCES plm_doc.artifact(artifact_id), -- 强制关联 M19 不可变制品
    file_display_name       VARCHAR(255) NOT NULL,
    file_size_bytes         BIGINT NOT NULL,
    uploaded_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_msg.msg_attachment IS 'M01-MSG: 邮件附件关联表，物理存储严格复用 M19 MinIO 防篡改制品';

-- 5. 消息防篡改与安全审计日志表 (MessageAuditLog)
CREATE TABLE plm_msg.msg_audit_log (
    audit_id                BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,
    action_type             VARCHAR(64) NOT NULL, -- SEND_MANUAL, DISPATCH_SYSTEM, RETRACT, HARD_DELETE
    operator_id             VARCHAR(64) NOT NULL,
    target_message_id       BIGINT NOT NULL,
    recipient_summary       TEXT NULL,
    client_ip               VARCHAR(64) NOT NULL,
    is_violation_blocked    BOOLEAN NOT NULL DEFAULT FALSE,
    blocked_reason          TEXT NULL,
    recorded_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_msg_audit_query ON plm_msg.msg_audit_log(target_message_id, action_type);
```

### 5. 核心业务流程与业务触发拓扑 (Event-Driven Architecture)

全系统事件通知采用 **异步解耦、可靠入库与实时管道下发** 的现代反应式架构：  

代码段

```
sequenceDiagram
    autonumber
    participant Biz as PLM 业务核心服务 (M02/M22/M24)
    participant Outbox as plm_infra.sys_outbox_event
    participant Kafka as Kafka 消息总线 (Event Stream)
    participant MsgSvc as M01-MSG 内部邮件服务
    participant DB as PostgreSQL (plm_msg)
    participant Push as WebSocket / SSE 推送服务
    actor Client as 工程师 Web 前端工作台

    Biz->>Outbox: 1. 本地事务提交业务聚合根 + 写入 Outbox 事件
    Note over Outbox: 保证业务状态变更与事件发射的绝对原子性
    Outbox->>Kafka: 2. CDC / 定时扫描派发事件至 Kafka Topic[cite: 3]
    Kafka->>MsgSvc: 3. 监听事件 (e.g. WorkflowSubmittedEvent)
    
    critical 消息组装与防伪造入库
        MsgSvc->>MsgSvc: 4. 根据事件解析接收人 (部门/角色/项目展开)
        MsgSvc->>MsgSvc: 5. 组装强工程上下文 (项目ID/对象URI/操作动作)
        MsgSvc->>DB: 6. 批量持久化 (msg_master + N 个 msg_user_box)
    end
    
    MsgSvc->>Push: 7. 发送用户即时气泡信号 (userId, unreadCount, summary)
    Push-->>Client: 8. WebSocket 实时推送至顶部铃铛与浮窗通知
    Client->>MsgSvc: 9. 点击通知卡片，请求业务跳转
    Note over MsgSvc,Client: 触发 PBAC 穿透校验：无权访问时抛出 403 阻断穿透！
```

#### 5.1 业务事件监听与投递规则映射表

Java

```
package com.ccddesigner.message.consumer;

import com.ccddesigner.common.event.*;
import com.ccddesigner.message.service.InternalMessageDispatchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

@Component
public class PLMSystemEventNotificationConsumer {

    @Resource
    private InternalMessageDispatchService messageDispatchService;

    /**
     * 场景一：工作流提交审批，投递给指定审批人
     */
    @KafkaListener(topics = "plm.workflow.events", groupId = "msg-center-workflow-group")
    public void onWorkflowSubmitted(WorkflowTaskAssignedEvent event) {
        messageDispatchService.dispatchSystemMessage(
            SystemMessageDispatchCommand.builder()
                .subType(SystemMessageType.WORKFLOW)
                .subject("【待办审批】" + event.getWorkflowTitle() + " 提请您会签评审")
                .bodyContent(String.format("业务对象: %s (%s)<br/>发起人: %s<br/>请在规定期限内完成合规签署。",
                    event.getBusinessCode(), event.getRevisionLabel(), event.getOriginatorName()))
                .recipientUserIds(event.getAssigneeUserIds())
                .relatedProjectId(event.getProjectId())
                .relatedObjectType(event.getTargetObjectType())
                .relatedObjectId(event.getTargetObjectId())
                .targetActionUrl("/workspace/approvals/" + event.getTaskId())
                .priority(Priority.HIGH)
                .correlationEventId(event.getEventId())
                .build()
        );
    }

    /**
     * 场景二：WBS 任务分配，投递给任务责任人
     */
    @KafkaListener(topics = "plm.project.events", groupId = "msg-center-task-group")
    public void onTaskAssigned(TaskAssignedEvent event) {
        messageDispatchService.dispatchSystemMessage(
            SystemMessageDispatchCommand.builder()
                .subType(SystemMessageType.TASK)
                .subject("【任务分配】您已被指派为任务负责人: " + event.getTaskName())
                .bodyContent(String.format("所属项目: %s<br/>计划工期: %s 至 %s<br/>交付物要求: %s",
                    event.getProjectName(), event.getPlannedStartDate(), event.getPlannedEndDate(), event.getDeliverableDescription()))
                .recipientUserIds(List.of(event.getAssigneeId()))
                .relatedProjectId(event.getProjectId())
                .relatedObjectType("Task")
                .relatedObjectId(event.getTaskId())
                .targetActionUrl("/workspace/projects/" + event.getProjectId() + "/tasks/" + event.getTaskId())
                .priority(Priority.NORMAL)
                .correlationEventId(event.getEventId())
                .build()
        );
    }

    /**
     * 场景三：项目阶段门评审决议达成，全员广播
     */
    @KafkaListener(topics = "plm.gate.events", groupId = "msg-center-gate-group")
    public void onGateDecisionRecorded(GateDecisionRecordedEvent event) {
        messageDispatchService.dispatchSystemMessage(
            SystemMessageDispatchCommand.builder()
                .subType(SystemMessageType.REVIEW)
                .subject(String.format("【评审决议】%s 阶段门决策结果: %s", event.getGateName(), event.getDecisionType()))
                .bodyContent(String.format("决议签署人: %s<br/>审查结论: %s<br/>行动项数量: %d 项<br/>附言: %s",
                    event.getDecidedBy(), event.getDecisionType(), event.getOpenActionCount(), event.getDecisionNotes()))
                .recipientUserIds(event.getProjectMemberUserIds()) // 项目工作组全量投递
                .relatedProjectId(event.getProjectId())
                .relatedObjectType("GateDecision")
                .relatedObjectId(event.getDecisionId())
                .targetActionUrl("/workspace/projects/" + event.getProjectId() + "/gates/" + event.getGateId())
                .priority(event.getDecisionType().equals("PASS") ? Priority.NORMAL : Priority.HIGH)
                .correlationEventId(event.getEventId())
                .build()
        );
    }
}
```

### 6. 安全、权限防穿透与职责分离设计 (PBAC Guard)

为落实产品开发说明书 §7 安全架构要求，消息模块设立不可逾越的安全防火墙[cite: 3]：

#### 6.1 业务穿透双重鉴权守卫协议

当用户在内部邮件中查阅与点击业务对象时，系统遵循以下鉴权时序：

```
[前端点击消息直达链接]
         │
         ▼
[GET /api/v1/internal-messages/{id}/resolve-action]
         │
         ├── 1. 基础消息权校验：检查该消息是否属于当前上下文操作人 (msg_user_box.user_id == currentUserId)
         │      [否] ──▶ 抛出 403 (ERR_MESSAGE_ACCESS_DENIED)
         │
         ├── 2. 状态获取：从 msg_master 提取 target_action_url 与 related_obj
         │
         ▼
[进入目标业务 API: GET /api/v1/cad-documents/{id}]
         │
         ├── 3. PBAC 属性策略强制评估 (AOP Aspect 拦截)：
         │      - 校验当前用户是否为目标项目的有效成员 (ProjectMembership.isActive)
         │      - 校验目标对象的密级与用户的安全许可等级 (SecurityClassification)
         │      - 校验对象的受控研制状态 (DRAFT/RELEASED 等)
         │
         ├── [鉴权通过] ──▶ 正常返回 CAD 装配数据与轻量化视图
         │
         └── [鉴权失败] ──▶ 物理阻断！返回 HTTP 403 Forbidden:
                {
                  "errorCode": "ERR_PBAC_CROSS_OBJECT_UNAUTHORIZED",
                  "detail": "您已接收该变更通知，但当前账户不具备机密级 CAD 图纸的工程查阅权限，系统已阻断非法访问。"
                }
```

#### 6.2 职责分离 (SoD) 与防伪造限制

1. **系统发信防篡改**：
   - 对外暴露的发送邮件接口 `POST /api/v1/internal-messages` 仅允许创建 `MANUAL` 类型的普通信件，强制将 `senderId` 设为当前会话操作人工号，且 `isSystemGenerated = FALSE`。  
   - 任何试图在 HTTP 载荷中伪造 `sender_id = 'SYSTEM'` 或填充 `SYSTEM` 类型的行为，网关前置校验直接拒绝，记录安全风控告警。  
2. **管理员保密审计原则**：
   - 平台管理员在“系统管理 - 审计中心”仅能查询消息发送量、投递延迟、失败率及系统消息的业务派发轨迹；  
   - 严禁提供“全局收件箱窥视”接口；普通员工的点对点技术沟通正文受到行级保密隔离，杜绝管理特权滥用。  

### 7. 前端工作台三栏式交互规格 (React UI Specification)

消息中心无缝嵌入 M01 统一工作台，采用桌面级三栏响应式工作布局：  

```
┌─────────────────┬──────────────────────────────────┬───────────────────────────────────────────┐
│  左侧：导航与分类 │       中间：消息摘要列表          │             右侧：消息详情与业务入口       │
├─────────────────┼──────────────────────────────────┼───────────────────────────────────────────┤
│ [ ✍️ 撰写新邮件 ]  │ 🔍 搜索邮件/按标签过滤...         │ 🏷️ [任务通知] [待处理] [高优先级]          │
│                 │                                  │ 标 题: 【待办审批】主轴部件 Rev B 提请审批 │
│ 📥 收件箱 (12)  │ 🔵 [流程] 主轴部件 Rev B 审批     │ 发件人: 系统服务 (触发人: 李工/机械室)      │
│ ⭐ 星标消息 (3)  │    李工 · 2026-09-16 10:14       │ 时 间: 2026-09-16 10:14:22                │
│ ⏳ 待处理 (5)    │    "主轴动刚度仿真已达成，申请..." │ 收件人: 张总师 (您), 王主管               │
│                 │                                  │ ----------------------------------------- │
│ ── 业务分类 ──  │ ⚪ [任务] 完成动密封配合公差计算 │ 📌 关联业务对象:                           │
│ 🔄 流程审批 (4)  │    项目组 · 2026-09-16 08:30     │    项目: VMC1000加工中心研制 (PRJ-2026)    │
│ 📋 研发任务 (3)  │    "计划截止: 2026-09-20..."     │    对象: PartRevision (VMC-SP-001-B)      │
│ 🎯 项目评审 (1)  │                                  │                                           │
│ ⚠️ 变更通知 (2)  │ ⚪ [变更] ECO-2026-0042 正式发布 │ [ 🚀 进入流程审批 ]  [ 🔍 查看图纸比对 ]    │
│ 📑 文档图纸 (2)  │    变更委员会 · 昨天 17:20       │                                           │
│                 │                                  │ 正 文:                                    │
│ ── 个人箱体 ──  │ ⚪ [人工] 关于主轴锥孔跳动的问题 │ 主轴动刚度优化设计已完成，现将 EBOM 结构与 │
│ 📤 已发送       │    赵工 · 昨天 14:10             │ CAD 拓扑装配提请审批，请复核关键配合公差。 │
│ 📝 草稿箱 (1)   │                                  │                                           │
│ 🗄️ 已归档       │                                  │ 📎 附件清单 (1 件):                       │
│ 🗑️ 回收站       │                                  │    📄 Spindle_FEA_Report.pdf (4.2 MB)     │
└─────────────────┴──────────────────────────────────┴───────────────────────────────────────────┘
```

#### 7.1 交互动作规范

1. **多重筛选器联动**：
   - 点击左侧“待处理”，中间列表过滤展示打上“待处理”标签或子类型为 `WORKFLOW`/`TASK` 且未完成处理的消息。
   - 点击“星标消息”，过滤 `is_starred = TRUE` 的高优收藏记录。
2. **快捷业务处理条（Business Action Bar）**：
   - 凡 `msg_master.target_action_url` 存在的系统邮件，详情顶部固定渲染高亮引导按钮，例如 **[进入流程审批]**、**[查看变更单]**、**[进入评审看板]**。
   - 点击按钮在工作台标签页（Tabs）中直接打开目标业务，并在完成签署回调后，系统通过 WebSocket 自动给该邮件追加“已处理”置灰标签。
3. **标签与状态操作**：
   - 支持单信标星、一键标记为已读、移动至归档、自定义着色标签（最多支持 5 个标签/信件）。

### 8. OpenAPI 3.0 接口契约定义

#### 8.1 分页查询当前用户邮箱列表

- **HTTP 请求**：`GET /api/v1/internal-messages`
- **请求参数**：
  - `boxType`: `INBOX` | `OUTBOX` | `DRAFT` | `ARCHIVE` (默认 `INBOX`)
  - `subType`: `WORKFLOW` | `TASK` | `REVIEW` 等 (可选)
  - `isRead`: `true` | `false` (可选)
  - `isStarred`: `true` | `false` (可选)
  - `keyword`: 检索关键字
  - `page`: 1, `size`: 20
- **响应报文 (Response 200 OK)**：

JSON

```
{
  "total": 48,
  "page": 1,
  "pageSize": 20,
  "unreadCount": 12,
  "items": [
    {
      "itemId": 9018290182901,
      "messageId": 8019284102901,
      "rootCategory": "SYSTEM",
      "subType": "WORKFLOW",
      "subject": "【待办审批】主轴部件 Rev B 提请审批",
      "senderDisplayName": "系统服务",
      "priority": "HIGH",
      "isRead": false,
      "isStarred": true,
      "customTags": ["待处理", "关键核心"],
      "relatedObjectType": "PartRevision",
      "relatedObjectId": 70192841001,
      "createdAt": "2026-09-16T10:14:22Z"
    }
  ]
}
```

#### 8.2 发送人工内部邮件

- **HTTP 请求**：`POST /api/v1/internal-messages`
- **请求头**：`Idempotency-Key: msg-send-7b8192-20260916`
- **请求载荷 (Request Body)**：

JSON

```
{
  "subject": "关于 VMC1000 主轴法兰配合公差的技术确认",
  "bodyContent": "<p>李工，请确认陶瓷轴承外圈与箱体配合是否采用 k6 级过渡配合？详见附件草图。</p>",
  "toUserIds": ["ENG-2041"],
  "ccUserIds": ["LEAD-1002"],
  "priority": "NORMAL",
  "relatedProjectId": 100293810293,
  "relatedObjectType": "PartRevision",
  "relatedObjectId": 70192841001,
  "quotedMessageId": null,
  "attachmentArtifactIds": [901829018209182]
}
```

- **响应报文 (Response 201 Created)**：

JSON

```
{
  "messageId": 8019284102999,
  "status": "SENT",
  "deliveredCount": 2,
  "sentAt": "2026-09-16T10:30:00Z"
}
```

#### 8.3 解析消息业务动作并校验权限 (Action Resolution)

- **HTTP 请求**：`GET /api/v1/internal-messages/{messageId}/resolve-action`
- **响应报文 (Response 200 OK - 权限正常放行)**：

JSON

```
{
  "messageId": 8019284102901,
  "authorized": true,
  "targetActionUrl": "/workspace/approvals/task-wf-881920",
  "securityContext": {
    "projectRole": "APPROVER",
    "requiredSecurityLevel": "INTERNAL",
    "userClearance": "CONFIDENTIAL"
  }
}
```

- **响应报文 (Response 403 Forbidden - 跨域无权防穿透拦截)**：

JSON

```
{
  "type": "https://plm.company.com/errors/cross-object-unauthorized",
  "title": "Business Navigation Blocked",
  "status": 403,
  "detail": "Action Rejected: You do not possess sufficient project clearance to view PartRevision [70192841001]. Message linkage access denied.",
  "errorCode": "ERR_PBAC_CROSS_OBJECT_UNAUTHORIZED",
  "instance": "/api/v1/internal-messages/8019284102901/resolve-action"
}
```

### 9. 验收测试矩阵 (Acceptance Testing Matrix)

| **用例编号**  | **上位验收对照  MD+ 1**    | **测试步骤与注入场景**                                       | **预期判定结果 (Pass Criteria)**                             | **验证日志与断言依据  MD+ 1**                                |
| ------------- | -------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **TC-MSG-01** | **MSG-F03** / AT-16        | 业务工程师在 M06 提交模型发布申请，M24 生成工作流审批实例    | 审批人收件箱在 1 秒内收到 `WORKFLOW` 类型通知；未读数自增 1，WebSocket 弹窗提示[cite: 1] | 验证 `msg_master` 携带 `target_action_url` 且 `sender_id = 'SYSTEM_SERVICE'`。 |
| **TC-MSG-02** | **MSG-SEC-01** / AT-13     | 用户收到“主轴技术图样变更”通知，但此时管理员将其移出该机型项目组；用户随后在邮件中点击“查看图纸”[cite: 1, 3] | 调用 `resolve-action` 时，PBAC 切面拦截阻断，直接响应 `403 Forbidden`，**严禁越权呈现图纸内容** | 接口抛出 `ERR_PBAC_CROSS_OBJECT_UNAUTHORIZED`，记录安全防穿透审计日志[cite: 1, 3]。 |
| **TC-MSG-03** | 业务与待办解耦[cite: 1, 3] | 用户收到任务派发通知邮件，在邮件详情中将其标记为“已读”并移入“已归档”箱 | 任务本身的执行状态仍为 `IN_PROGRESS`，WBS 进度绝不发生变更，M01 待办列表任务依然存在[cite: 1, 3] | 校验 `plm_project.task.status` 保持不变，验证消息阅读与任务状态正交[cite: 1, 3]。 |
| **TC-MSG-04** | 协议独立性[cite: 3]        | 安全渗透测试尝试在服务器 25 (SMTP)、110 (POP3)、143 (IMAP) 端口发起连接嗅探 | 端口物理关闭，连接被强行 Reset；全系统无任何邮件客户端协议栈监听 | 确认邮件系统完全基于内部数据库与 Kafka 闭环运行，无网络外泄面[cite: 2, 3]。 |
| **TC-MSG-05** | **MSG-F02** [cite: 1]      | 编写包含 5MB 附图的人工信件，指派发给“立式加工中心研发室”整个部门及“项目总师”角色 | 系统自动递归展开部门与角色，精准生成多条 `msg_user_box` 记录；附件引用 M19 的 `Artifact` | 数据库无重复文件冗余存储，所有收件人收件箱均准确呈现实时未读标记。 |
| **TC-MSG-06** | 防伪造与防篡改[cite: 3]    | 攻击者伪造 API 调用，传入 `rootCategory = 'SYSTEM'` 试图向全体员工广播伪造的系统停机维护通知 | API 前置参数校验器拦截，抛出 `422 Unprocessable Entity` 并强行中断请求[cite: 1, 3] | 错误码 `ERR_MANUAL_SYSTEM_MESSAGE_FORBIDDEN`，审计表记录该非授权发信行为[cite: 1, 3]。 |