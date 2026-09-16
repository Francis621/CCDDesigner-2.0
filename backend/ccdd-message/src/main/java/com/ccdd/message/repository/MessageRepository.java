package com.ccdd.message.repository;

import com.ccdd.message.entity.MailboxBoxType;
import com.ccdd.message.entity.MessagePriority;
import com.ccdd.message.entity.MessageRootCategory;
import com.ccdd.message.entity.MsgAttachmentEntity;
import com.ccdd.message.entity.MsgAuditLogEntity;
import com.ccdd.message.entity.MsgMasterEntity;
import com.ccdd.message.entity.MsgRecipientEntity;
import com.ccdd.message.entity.MsgUserBoxEntity;
import com.ccdd.message.entity.SystemMessageType;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * M01-MSG PLM内部邮件与消息中心仓储层
 * 提供并发安全内存存储，且与 V1.9.0 数据库迁移脚本种子数据完全对齐
 */
@Repository
public class MessageRepository {

    private final Map<Long, MsgMasterEntity> msgMasterStore = new ConcurrentHashMap<>();
    private final Map<Long, MsgUserBoxEntity> userBoxStore = new ConcurrentHashMap<>();
    private final Map<Long, MsgRecipientEntity> recipientStore = new ConcurrentHashMap<>();
    private final Map<Long, MsgAttachmentEntity> attachmentStore = new ConcurrentHashMap<>();
    private final Map<Long, MsgAuditLogEntity> auditLogStore = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(10000L);

    public MessageRepository() {
        initSeedData();
    }

    private void initSeedData() {
        Instant now = Instant.now();

        // -------------------------------------------------------------
        // 种子邮件 1: ECO 会签通知 (系统自动派发)
        // -------------------------------------------------------------
        Long msg1Id = 9001L;
        MsgMasterEntity msg1 = new MsgMasterEntity(
                msg1Id,
                MessageRootCategory.SYSTEM,
                SystemMessageType.CHANGE,
                "【变更协同】关于ECO-2026-0042(VMC1000主轴提速至15000rpm)的协同会签通知",
                "<p>尊敬的工程师：</p><p>工程变更单 <strong>ECO-2026-0042</strong> (VMC1000主轴提速至15000rpm工程实施与全生命周期现场处置单) 已进入跨学科协同会签阶段。请机械、仿真及电气专业主管在收到本通知后3个工作日内完成影响分析及处置方案在线复核。</p><p>点击下方按钮可直达变更详情面板进行方案批注与电子签章。</p>",
                MessagePriority.HIGH,
                "SYSTEM_NOTIFIER",
                "系统通知服务",
                "VMC_ENTERPRISE",
                "ECO",
                "ECO-2026-0042",
                "/change/eco/ECO-2026-0042",
                "ECO_COLLABORATION_REVIEW",
                "{\"ecrNo\":\"ECR-2026-0042\",\"targetSpeed\":15000,\"impactItemCount\":4}",
                true,
                now.minus(2, ChronoUnit.HOURS)
        );
        msgMasterStore.put(msg1Id, msg1);

        // 接收人列表
        saveRecipient(new MsgRecipientEntity(9301L, msg1Id, "chief_designer", "总设计师", "TO"));
        saveRecipient(new MsgRecipientEntity(9302L, msg1Id, "lead_analyst", "仿真分析组长", "TO"));
        saveRecipient(new MsgRecipientEntity(9303L, msg1Id, "admin", "系统管理员", "CC"));

        // 用户信箱索引
        saveUserBox(new MsgUserBoxEntity(9101L, "chief_designer", msg1Id, MailboxBoxType.INBOX, false, false, false, false, now.minus(2, ChronoUnit.HOURS)));
        saveUserBox(new MsgUserBoxEntity(9102L, "lead_analyst", msg1Id, MailboxBoxType.INBOX, false, false, false, false, now.minus(2, ChronoUnit.HOURS)));
        saveUserBox(new MsgUserBoxEntity(9103L, "admin", msg1Id, MailboxBoxType.INBOX, true, false, false, false, now.minus(2, ChronoUnit.HOURS)));

        // 附件
        saveAttachment(new MsgAttachmentEntity(9201L, msg1Id, "VMC1000_ECO_0042_Impact_Report.pdf", 2584100L, "application/pdf", "/attachments/eco/VMC1000_ECO_0042_Impact_Report.pdf", now.minus(2, ChronoUnit.HOURS)));

        // -------------------------------------------------------------
        // 种子邮件 2: 关键阶段门倒计时预警 (系统自动派发)
        // -------------------------------------------------------------
        Long msg2Id = 9002L;
        MsgMasterEntity msg2 = new MsgMasterEntity(
                msg2Id,
                MessageRootCategory.SYSTEM,
                SystemMessageType.GATE,
                "【阶段门预警】VMC1000项目 GATE-3(详细设计评审门) 临期倒计时预警",
                "<p>项目各主管：</p><p>型号项目 <strong>VMC_ENTERPRISE</strong> 关键节点 <strong>GATE-3(详细设计评审门)</strong> 计划于5个工作日后关闭。目前仍有1项关键交付物(全机热伸长有限元分析报告)处于审批中。请加快流转，确保门禁条件按时闭环。</p>",
                MessagePriority.URGENT,
                "SYSTEM_NOTIFIER",
                "系统通知服务",
                "VMC_ENTERPRISE",
                "GATE",
                "GATE-3",
                "/project/gate/GATE-3",
                "GATE_AUDIT_EXPEDITE",
                "{\"gateId\":\"GATE-3\",\"pendingDocCount\":1,\"deadline\":\"2026-09-21\"}",
                true,
                now.minus(1, ChronoUnit.DAYS)
        );
        msgMasterStore.put(msg2Id, msg2);

        saveRecipient(new MsgRecipientEntity(9304L, msg2Id, "project_manager", "项目经理", "TO"));
        saveRecipient(new MsgRecipientEntity(9305L, msg2Id, "chief_designer", "总设计师", "TO"));
        saveRecipient(new MsgRecipientEntity(9306L, msg2Id, "admin", "系统管理员", "TO"));

        saveUserBox(new MsgUserBoxEntity(9104L, "project_manager", msg2Id, MailboxBoxType.INBOX, false, true, false, false, now.minus(1, ChronoUnit.DAYS)));
        saveUserBox(new MsgUserBoxEntity(9105L, "chief_designer", msg2Id, MailboxBoxType.INBOX, false, false, false, false, now.minus(1, ChronoUnit.DAYS)));
        saveUserBox(new MsgUserBoxEntity(9106L, "admin", msg2Id, MailboxBoxType.INBOX, false, true, false, false, now.minus(1, ChronoUnit.DAYS)));

        // -------------------------------------------------------------
        // 种子邮件 3: 工艺参数研讨纪要 (人工撰写邮件)
        // -------------------------------------------------------------
        Long msg3Id = 9003L;
        MsgMasterEntity msg3 = new MsgMasterEntity(
                msg3Id,
                MessageRootCategory.MANUAL,
                null,
                "【技术交流】关于五轴联动叶片加工工艺参数优化的研讨纪要与试验排程",
                "<p>李总、各位工艺师：</p><p>附件为本周二关于五轴铣削钛合金叶片表面粗糙度提升的试验分析报告。初步测算在转速由10000rpm提升至12500rpm配合微量润滑(MQL)条件下，表面粗糙度可由Ra 0.8提升至Ra 0.4。请审阅试验数据，并安排下周二的二次试切验证。</p>",
                MessagePriority.NORMAL,
                "lead_analyst",
                "仿真分析组长",
                "VMC_ENTERPRISE",
                "PROCESS",
                "PROC-BLADE-001",
                "/manufacturing/process/PROC-BLADE-001",
                "PROCESS_EXPERIMENT_SCHEDULE",
                "{\"experimentBatch\":\"EXP-202609-01\",\"mqlEnabled\":true}",
                false,
                now.minus(3, ChronoUnit.DAYS)
        );
        msgMasterStore.put(msg3Id, msg3);

        saveRecipient(new MsgRecipientEntity(9307L, msg3Id, "chief_designer", "总设计师", "TO"));
        saveRecipient(new MsgRecipientEntity(9308L, msg3Id, "process_engineer", "工艺主管工程师", "TO"));

        // 发送方发件箱
        saveUserBox(new MsgUserBoxEntity(9107L, "lead_analyst", msg3Id, MailboxBoxType.OUTBOX, true, false, false, false, now.minus(3, ChronoUnit.DAYS)));
        // 接收方收件箱
        saveUserBox(new MsgUserBoxEntity(9108L, "chief_designer", msg3Id, MailboxBoxType.INBOX, true, false, false, false, now.minus(3, ChronoUnit.DAYS)));
        saveUserBox(new MsgUserBoxEntity(9109L, "process_engineer", msg3Id, MailboxBoxType.INBOX, false, false, false, false, now.minus(3, ChronoUnit.DAYS)));

        saveAttachment(new MsgAttachmentEntity(9202L, msg3Id, "Blade_Milling_MQL_Test_Report.xlsx", 845200L, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "/attachments/process/Blade_Milling_MQL_Test_Report.xlsx", now.minus(3, ChronoUnit.DAYS)));
    }

    public Long nextId() {
        return idGenerator.incrementAndGet();
    }

    public MsgMasterEntity saveMaster(MsgMasterEntity entity) {
        if (entity.getMessageId() == null) {
            entity.setMessageId(nextId());
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        msgMasterStore.put(entity.getMessageId(), entity);
        return entity;
    }

    public Optional<MsgMasterEntity> findMasterById(Long messageId) {
        return Optional.ofNullable(msgMasterStore.get(messageId));
    }

    public MsgUserBoxEntity saveUserBox(MsgUserBoxEntity entity) {
        if (entity.getUserBoxId() == null) {
            entity.setUserBoxId(nextId());
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        userBoxStore.put(entity.getUserBoxId(), entity);
        return entity;
    }

    public Optional<MsgUserBoxEntity> findUserBoxById(Long userBoxId) {
        return Optional.ofNullable(userBoxStore.get(userBoxId));
    }

    public Optional<MsgUserBoxEntity> findUserBoxByUserAndMessage(String userId, Long messageId) {
        return userBoxStore.values().stream()
                .filter(b -> b.getUserId().equals(userId) && b.getMessageId().equals(messageId))
                .findFirst();
    }

    public List<MsgUserBoxEntity> findUserBoxes(String userId, MailboxBoxType boxType, Boolean isRead, Boolean isStarred) {
        return userBoxStore.values().stream()
                .filter(b -> b.getUserId().equals(userId))
                .filter(b -> boxType == null || b.getBoxType() == boxType)
                .filter(b -> isRead == null || b.getIsRead().equals(isRead))
                .filter(b -> isStarred == null || b.getIsStarred().equals(isStarred))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public MsgRecipientEntity saveRecipient(MsgRecipientEntity recipient) {
        if (recipient.getRecipientId() == null) {
            recipient.setRecipientId(nextId());
        }
        recipientStore.put(recipient.getRecipientId(), recipient);
        return recipient;
    }

    public List<MsgRecipientEntity> findRecipientsByMessageId(Long messageId) {
        return recipientStore.values().stream()
                .filter(r -> r.getMessageId().equals(messageId))
                .collect(Collectors.toList());
    }

    public MsgAttachmentEntity saveAttachment(MsgAttachmentEntity attachment) {
        if (attachment.getAttachmentId() == null) {
            attachment.setAttachmentId(nextId());
        }
        if (attachment.getUploadedAt() == null) {
            attachment.setUploadedAt(Instant.now());
        }
        attachmentStore.put(attachment.getAttachmentId(), attachment);
        return attachment;
    }

    public List<MsgAttachmentEntity> findAttachmentsByMessageId(Long messageId) {
        return attachmentStore.values().stream()
                .filter(a -> a.getMessageId().equals(messageId))
                .collect(Collectors.toList());
    }

    public MsgAuditLogEntity saveAuditLog(MsgAuditLogEntity log) {
        if (log.getLogId() == null) {
            log.setLogId(nextId());
        }
        if (log.getTimestamp() == null) {
            log.setTimestamp(Instant.now());
        }
        auditLogStore.put(log.getLogId(), log);
        return log;
    }

    public List<MsgAuditLogEntity> findAuditLogsByMessageId(Long messageId) {
        return auditLogStore.values().stream()
                .filter(l -> l.getMessageId().equals(messageId))
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .collect(Collectors.toList());
    }

    public long countUnread(String userId) {
        return userBoxStore.values().stream()
                .filter(b -> b.getUserId().equals(userId))
                .filter(b -> b.getBoxType() == MailboxBoxType.INBOX)
                .filter(b -> !b.getIsRead())
                .count();
    }
}
