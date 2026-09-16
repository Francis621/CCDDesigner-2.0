package com.ccdd.message.service;

import com.ccdd.message.dto.DispatchSystemMessageCommand;
import com.ccdd.message.dto.MailboxItemDto;
import com.ccdd.message.dto.MailboxPageDto;
import com.ccdd.message.dto.MailboxQueryParam;
import com.ccdd.message.dto.MessageDetailDto;
import com.ccdd.message.dto.ResolveActionResponse;
import com.ccdd.message.dto.SendManualMessageRequest;
import com.ccdd.message.dto.SendMessageResponse;
import com.ccdd.message.dto.UpdateMessageStatusRequest;
import com.ccdd.message.entity.MailboxBoxType;
import com.ccdd.message.entity.MessagePriority;
import com.ccdd.message.entity.MessageRootCategory;
import com.ccdd.message.entity.MsgAttachmentEntity;
import com.ccdd.message.entity.MsgAuditLogEntity;
import com.ccdd.message.entity.MsgMasterEntity;
import com.ccdd.message.entity.MsgRecipientEntity;
import com.ccdd.message.entity.MsgUserBoxEntity;
import com.ccdd.message.entity.SystemMessageType;
import com.ccdd.message.exception.ManualSystemMessageForbiddenException;
import com.ccdd.message.exception.MessageAccessDeniedException;
import com.ccdd.message.exception.PbacCrossObjectUnauthorizedException;
import com.ccdd.message.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * M01-MSG 内部邮件与消息中心业务逻辑实现
 * 落实发信防伪造、多播投递、PBAC跨域越权穿透阻断与消息-业务状态强解耦
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    // 授权访问高端机床受限工程对象的用户白名单（模拟 PBAC 策略上下文）
    private static final Set<String> AUTHORIZED_ENGINEERING_USERS = new HashSet<>(Arrays.asList(
            "admin",
            "chief_designer",
            "lead_analyst",
            "project_manager",
            "process_engineer",
            "quality_engineer"
    ));

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * 人工写信投递（MSG-F01）
     * 防伪造硬拦截：禁止伪造系统邮件标识
     */
    public SendMessageResponse sendManualMessage(SendManualMessageRequest request, String senderUserId, String senderUserName) {
        if (request == null) {
            throw new IllegalArgumentException("发信请求不能为空");
        }

        // 1. 防伪造硬拦截（MSG-F01 SoD 硬规则）
        if (request.getRootCategory() == MessageRootCategory.SYSTEM || Boolean.TRUE.equals(request.getIsSystemGenerated())) {
            throw new ManualSystemMessageForbiddenException("人工发信通道严禁伪造系统消息分类(rootCategory=SYSTEM 或 isSystemGenerated=true)");
        }

        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("邮件主题不能为空");
        }
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            throw new IllegalArgumentException("收件人列表不能为空");
        }

        Instant now = Instant.now();
        Long messageId = messageRepository.nextId();

        // 2. 构造主消息记录
        MsgMasterEntity master = new MsgMasterEntity(
                messageId,
                MessageRootCategory.MANUAL,
                null,
                request.getSubject(),
                request.getContent(),
                request.getPriority() != null ? request.getPriority() : MessagePriority.NORMAL,
                senderUserId,
                senderUserName != null ? senderUserName : senderUserId,
                request.getRelatedProjectId(),
                request.getRelatedObjType(),
                request.getRelatedObjId(),
                request.getTargetActionUrl(),
                null,
                null,
                false,
                now
        );
        messageRepository.saveMaster(master);

        // 3. 多播展开 (Fan-out) 并写入发信人与各收信人信箱
        // 发送方发件箱 (OUTBOX)
        messageRepository.saveUserBox(new MsgUserBoxEntity(
                messageRepository.nextId(),
                senderUserId,
                messageId,
                MailboxBoxType.OUTBOX,
                true,
                false,
                false,
                false,
                now
        ));

        // 接收人处理 (INBOX)
        int recipientCount = 0;
        for (SendManualMessageRequest.RecipientItem item : request.getRecipients()) {
            if (item.getUserId() == null || item.getUserId().trim().isEmpty()) {
                continue;
            }
            messageRepository.saveRecipient(new MsgRecipientEntity(
                    messageRepository.nextId(),
                    messageId,
                    item.getUserId(),
                    item.getUserName() != null ? item.getUserName() : item.getUserId(),
                    item.getRecipientType() != null ? item.getRecipientType() : "TO"
            ));

            messageRepository.saveUserBox(new MsgUserBoxEntity(
                    messageRepository.nextId(),
                    item.getUserId(),
                    messageId,
                    MailboxBoxType.INBOX,
                    false,
                    false,
                    false,
                    false,
                    now
            ));
            recipientCount++;
        }

        // 4. 处理附件
        if (request.getAttachments() != null) {
            for (SendManualMessageRequest.AttachmentItem att : request.getAttachments()) {
                messageRepository.saveAttachment(new MsgAttachmentEntity(
                        messageRepository.nextId(),
                        messageId,
                        att.getFileName(),
                        att.getFileSize(),
                        att.getFileType(),
                        att.getDownloadUrl(),
                        now
                ));
            }
        }

        // 5. 审计日志
        messageRepository.saveAuditLog(new MsgAuditLogEntity(
                messageRepository.nextId(),
                messageId,
                "MANUAL_SEND",
                senderUserId,
                "用户投递人工邮件，收件人数: " + recipientCount,
                now
        ));

        return new SendMessageResponse(messageId, master.getRootCategory(), recipientCount, "邮件发送成功", now);
    }

    /**
     * 系统内部自动派发通知（MSG-F02，内部信任通道调用）
     */
    public SendMessageResponse dispatchSystemMessage(DispatchSystemMessageCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("系统消息命令不能为空");
        }
        if (command.getSubject() == null || command.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("系统通知主题不能为空");
        }
        if (command.getRecipientUserIds() == null || command.getRecipientUserIds().isEmpty()) {
            throw new IllegalArgumentException("系统通知接收人不能为空");
        }

        Instant now = Instant.now();
        Long messageId = messageRepository.nextId();

        MsgMasterEntity master = new MsgMasterEntity(
                messageId,
                MessageRootCategory.SYSTEM,
                command.getSystemType() != null ? command.getSystemType() : SystemMessageType.GENERIC,
                command.getSubject(),
                command.getContent(),
                command.getPriority() != null ? command.getPriority() : MessagePriority.HIGH,
                "SYSTEM_NOTIFIER",
                "系统通知服务",
                command.getRelatedProjectId(),
                command.getRelatedObjType(),
                command.getRelatedObjId(),
                command.getTargetActionUrl(),
                command.getActionIdentifier(),
                command.getActionPayloadJson(),
                true,
                now
        );
        messageRepository.saveMaster(master);

        int count = 0;
        for (String recipientUserId : command.getRecipientUserIds()) {
            if (recipientUserId == null || recipientUserId.trim().isEmpty()) {
                continue;
            }
            messageRepository.saveRecipient(new MsgRecipientEntity(
                    messageRepository.nextId(),
                    messageId,
                    recipientUserId,
                    recipientUserId,
                    "TO"
            ));

            messageRepository.saveUserBox(new MsgUserBoxEntity(
                    messageRepository.nextId(),
                    recipientUserId,
                    messageId,
                    MailboxBoxType.INBOX,
                    false,
                    false,
                    false,
                    false,
                    now
            ));
            count++;
        }

        messageRepository.saveAuditLog(new MsgAuditLogEntity(
                messageRepository.nextId(),
                messageId,
                "SYSTEM_DISPATCH",
                "SYSTEM_NOTIFIER",
                "系统派发通知，类型: " + master.getSystemType() + ", 接收人数: " + count,
                now
        ));

        return new SendMessageResponse(messageId, MessageRootCategory.SYSTEM, count, "系统通知派发成功", now);
    }

    /**
     * 分页多维查询信箱列表（MSG-F03）
     */
    public MailboxPageDto queryUserMailbox(MailboxQueryParam param) {
        if (param == null || param.getUserId() == null) {
            throw new IllegalArgumentException("用户标识不能为空");
        }

        MailboxBoxType boxType = param.getBoxType() != null ? param.getBoxType() : MailboxBoxType.INBOX;
        List<MsgUserBoxEntity> userBoxes = messageRepository.findUserBoxes(
                param.getUserId(),
                boxType,
                param.getIsRead(),
                param.getIsStarred()
        );

        List<MailboxItemDto> dtoList = new ArrayList<>();
        for (MsgUserBoxEntity box : userBoxes) {
            Optional<MsgMasterEntity> masterOpt = messageRepository.findMasterById(box.getMessageId());
            if (masterOpt.isEmpty()) {
                continue;
            }
            MsgMasterEntity master = masterOpt.get();

            // 根分类过滤
            if (param.getRootCategory() != null && master.getRootCategory() != param.getRootCategory()) {
                continue;
            }

            // 关键字搜索（主题、发件人）
            if (param.getKeyword() != null && !param.getKeyword().trim().isEmpty()) {
                String kw = param.getKeyword().trim().toLowerCase();
                boolean matchSubject = master.getSubject() != null && master.getSubject().toLowerCase().contains(kw);
                boolean matchSender = master.getSenderUserName() != null && master.getSenderUserName().toLowerCase().contains(kw);
                if (!matchSubject && !matchSender) {
                    continue;
                }
            }

            List<MsgAttachmentEntity> atts = messageRepository.findAttachmentsByMessageId(master.getMessageId());

            MailboxItemDto item = new MailboxItemDto();
            item.setUserBoxId(box.getUserBoxId());
            item.setMessageId(master.getMessageId());
            item.setRootCategory(master.getRootCategory());
            item.setSystemType(master.getSystemType());
            item.setSubject(master.getSubject());
            item.setPriority(master.getPriority());
            item.setSenderUserId(master.getSenderUserId());
            item.setSenderUserName(master.getSenderUserName());
            item.setRelatedProjectId(master.getRelatedProjectId());
            item.setRelatedObjType(master.getRelatedObjType());
            item.setRelatedObjId(master.getRelatedObjId());
            item.setTargetActionUrl(master.getTargetActionUrl());
            item.setIsRead(box.getIsRead());
            item.setIsStarred(box.getIsStarred());
            item.setIsArchived(box.getIsArchived());
            item.setHasAttachment(atts != null && !atts.isEmpty());
            item.setCreatedAt(box.getCreatedAt());

            dtoList.add(item);
        }

        long totalElements = dtoList.size();
        int pageNum = param.getPageNum() != null && param.getPageNum() > 0 ? param.getPageNum() : 1;
        int pageSize = param.getPageSize() != null && param.getPageSize() > 0 ? param.getPageSize() : 20;

        int fromIndex = Math.min((pageNum - 1) * pageSize, dtoList.size());
        int toIndex = Math.min(fromIndex + pageSize, dtoList.size());
        List<MailboxItemDto> pageItems = dtoList.subList(fromIndex, toIndex);

        long unreadCount = messageRepository.countUnread(param.getUserId());

        return new MailboxPageDto(pageItems, totalElements, pageNum, pageSize, unreadCount);
    }

    /**
     * 获取邮件详情（MSG-F04）
     * 自动标记已读，并校验访问权限
     */
    public MessageDetailDto getMessageDetail(Long messageId, String currentUserId) {
        if (messageId == null || currentUserId == null) {
            throw new IllegalArgumentException("消息ID及用户ID不能为空");
        }

        MsgMasterEntity master = messageRepository.findMasterById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("邮件消息不存在: " + messageId));

        // 校验权限：当前用户必须是发件人或收件人之一
        Optional<MsgUserBoxEntity> userBoxOpt = messageRepository.findUserBoxByUserAndMessage(currentUserId, messageId);
        boolean isSender = master.getSenderUserId() != null && master.getSenderUserId().equals(currentUserId);
        if (userBoxOpt.isEmpty() && !isSender) {
            throw new MessageAccessDeniedException("无权访问该邮件内容: messageId=" + messageId);
        }

        // 自动标为已读
        userBoxOpt.ifPresent(box -> {
            if (!Boolean.TRUE.equals(box.getIsRead())) {
                box.setIsRead(true);
                messageRepository.saveUserBox(box);
            }
        });

        // 组装附件与收件人
        List<MsgAttachmentEntity> attachments = messageRepository.findAttachmentsByMessageId(messageId);
        List<MsgRecipientEntity> recipients = messageRepository.findRecipientsByMessageId(messageId);

        List<MessageDetailDto.AttachmentDetail> attDtos = attachments.stream().map(a -> {
            MessageDetailDto.AttachmentDetail d = new MessageDetailDto.AttachmentDetail();
            d.setAttachmentId(a.getAttachmentId());
            d.setFileName(a.getFileName());
            d.setFileSize(a.getFileSize());
            d.setFileType(a.getFileType());
            d.setDownloadUrl(a.getDownloadUrl());
            return d;
        }).collect(Collectors.toList());

        List<MessageDetailDto.RecipientDetail> recDtos = recipients.stream().map(r -> {
            MessageDetailDto.RecipientDetail rd = new MessageDetailDto.RecipientDetail();
            rd.setUserId(r.getUserId());
            rd.setUserName(r.getUserName());
            rd.setRecipientType(r.getRecipientType());
            return rd;
        }).collect(Collectors.toList());

        MessageDetailDto detail = new MessageDetailDto();
        detail.setMessageId(master.getMessageId());
        detail.setRootCategory(master.getRootCategory());
        detail.setSystemType(master.getSystemType());
        detail.setSubject(master.getSubject());
        detail.setContent(master.getContent());
        detail.setPriority(master.getPriority());
        detail.setSenderUserId(master.getSenderUserId());
        detail.setSenderUserName(master.getSenderUserName());
        detail.setRelatedProjectId(master.getRelatedProjectId());
        detail.setRelatedObjType(master.getRelatedObjType());
        detail.setRelatedObjId(master.getRelatedObjId());
        detail.setTargetActionUrl(master.getTargetActionUrl());
        detail.setActionIdentifier(master.getActionIdentifier());
        detail.setActionPayloadJson(master.getActionPayloadJson());
        detail.setIsSystemGenerated(master.getIsSystemGenerated());
        detail.setCreatedAt(master.getCreatedAt());

        userBoxOpt.ifPresent(box -> {
            detail.setUserBoxId(box.getUserBoxId());
            detail.setIsRead(box.getIsRead());
            detail.setIsStarred(box.getIsStarred());
            detail.setIsArchived(box.getIsArchived());
            detail.setBoxType(box.getBoxType());
        });

        detail.setAttachments(attDtos);
        detail.setRecipients(recDtos);

        return detail;
    }

    /**
     * 更新邮件状态（MSG-F05，标记已读/未读、星标、归档、移动至废纸篓）
     * 强解耦保证：绝不更改底层变更单或任务对象的业务状态
     */
    public boolean updateMessageStatus(Long messageId, String currentUserId, UpdateMessageStatusRequest request) {
        if (messageId == null || currentUserId == null || request == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        MsgUserBoxEntity userBox = messageRepository.findUserBoxByUserAndMessage(currentUserId, messageId)
                .orElseThrow(() -> new MessageAccessDeniedException("用户信箱中不存在该邮件记录: " + messageId));

        if (request.getIsRead() != null) {
            userBox.setIsRead(request.getIsRead());
        }
        if (request.getIsStarred() != null) {
            userBox.setIsStarred(request.getIsStarred());
        }
        if (request.getIsArchived() != null) {
            userBox.setIsArchived(request.getIsArchived());
            if (Boolean.TRUE.equals(request.getIsArchived())) {
                userBox.setBoxType(MailboxBoxType.ARCHIVE);
            } else if (userBox.getBoxType() == MailboxBoxType.ARCHIVE) {
                userBox.setBoxType(MailboxBoxType.INBOX);
            }
        }
        if (request.getIsTrash() != null) {
            userBox.setIsTrash(request.getIsTrash());
            if (Boolean.TRUE.equals(request.getIsTrash())) {
                userBox.setBoxType(MailboxBoxType.TRASH);
            } else if (userBox.getBoxType() == MailboxBoxType.TRASH) {
                userBox.setBoxType(MailboxBoxType.INBOX);
            }
        }
        if (request.getTargetBoxType() != null) {
            try {
                userBox.setBoxType(MailboxBoxType.valueOf(request.getTargetBoxType()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        messageRepository.saveUserBox(userBox);
        return true;
    }

    /**
     * 业务操作直达解析与 PBAC 跨域越权穿透阻断（MSG-F06）
     * 接收邮件仅代表有消息阅读权；直达目标业务对象时强行校验 PBAC 权限！
     */
    public ResolveActionResponse resolveActionUrl(Long messageId, String currentUserId) {
        if (messageId == null || currentUserId == null) {
            throw new IllegalArgumentException("消息ID及用户ID不能为空");
        }

        MsgMasterEntity master = messageRepository.findMasterById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("邮件消息不存在: " + messageId));

        // 1. 检查是否有跳转目标
        if (master.getTargetActionUrl() == null || master.getTargetActionUrl().trim().isEmpty()) {
            return new ResolveActionResponse(null, master.getRelatedObjType(), master.getRelatedObjId(), master.getRelatedProjectId(), false, "该消息未绑定直达业务动作");
        }

        // 2. PBAC 防穿透硬校验：检查当前用户是否有权访问目标工程对象
        // 只有具备白名单工程授权的用户才被允许穿透进入机床工程对象
        if (!AUTHORIZED_ENGINEERING_USERS.contains(currentUserId)) {
            throw new PbacCrossObjectUnauthorizedException(
                    "PBAC跨域越权阻断：当前用户 [" + currentUserId + "] 无权穿透访问高端机床目标工程对象 ["
                            + master.getRelatedObjType() + ":" + master.getRelatedObjId() + "]，直达操作已硬性拦截！"
            );
        }

        return new ResolveActionResponse(
                master.getTargetActionUrl(),
                master.getRelatedObjType(),
                master.getRelatedObjId(),
                master.getRelatedProjectId(),
                true,
                "PBAC鉴权通过，允许直达工程对象: " + master.getRelatedObjId()
        );
    }

    /**
     * 获取用户未读数
     */
    public long getUnreadCount(String userId) {
        return messageRepository.countUnread(userId);
    }
}
