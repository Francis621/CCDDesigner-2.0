package com.ccdd.message;

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
import com.ccdd.message.entity.MsgUserBoxEntity;
import com.ccdd.message.entity.SystemMessageType;
import com.ccdd.message.exception.ManualSystemMessageForbiddenException;
import com.ccdd.message.exception.PbacCrossObjectUnauthorizedException;
import com.ccdd.message.repository.MessageRepository;
import com.ccdd.message.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * M01-MSG 内部邮件与消息中心全流程测试套件
 * 严格覆盖 TC-MSG-01 至 TC-MSG-06 验收标准
 */
class MessageServiceTest {

    private MessageRepository repository;
    private MessageService service;

    @BeforeEach
    void setUp() {
        repository = new MessageRepository();
        service = new MessageService(repository);
    }

    @Test
    @DisplayName("TC-MSG-01: 人工邮件投递与多播投递 (Fan-out) 展开测试")
    void testTC_MSG_01_SendManualMessageSuccess() {
        SendManualMessageRequest request = new SendManualMessageRequest();
        request.setRootCategory(MessageRootCategory.MANUAL);
        request.setSubject("五轴机床主轴轴承游隙装配规范核对");
        request.setContent("<p>请工艺与质检团队核对新批次陶瓷球轴承轴向与径向游隙参数。</p>");
        request.setPriority(MessagePriority.HIGH);
        request.setRelatedProjectId("VMC_ENTERPRISE");
        request.setRelatedObjType("PART");
        request.setRelatedObjId("M-VMC850-BRG-7014");
        request.setTargetActionUrl("/parts/M-VMC850-BRG-7014");

        SendManualMessageRequest.RecipientItem r1 = new SendManualMessageRequest.RecipientItem("process_engineer", "工艺主管工程师", "TO");
        SendManualMessageRequest.RecipientItem r2 = new SendManualMessageRequest.RecipientItem("quality_engineer", "质检主管", "TO");
        request.setRecipients(Arrays.asList(r1, r2));

        SendManualMessageRequest.AttachmentItem att = new SendManualMessageRequest.AttachmentItem("Bearing_Clearance_Spec.pdf", 102450L, "application/pdf", "/files/spec.pdf");
        request.setAttachments(Collections.singletonList(att));

        // 执行发信
        SendMessageResponse resp = service.sendManualMessage(request, "chief_designer", "总设计师");
        assertNotNull(resp);
        assertNotNull(resp.getMessageId());
        assertEquals(MessageRootCategory.MANUAL, resp.getRootCategory());
        assertEquals(2, resp.getRecipientCount());

        // 验证发件箱 (chief_designer 的 OUTBOX)
        List<MsgUserBoxEntity> senderOutbox = repository.findUserBoxes("chief_designer", MailboxBoxType.OUTBOX, null, null);
        boolean foundOutbox = senderOutbox.stream().anyMatch(b -> b.getMessageId().equals(resp.getMessageId()));
        assertTrue(foundOutbox, "发件人信箱中必须记录 OUTBOX 条目");

        // 验证收件人收件箱 (process_engineer 的 INBOX)
        List<MsgUserBoxEntity> recipientInbox = repository.findUserBoxes("process_engineer", MailboxBoxType.INBOX, null, null);
        boolean foundInbox = recipientInbox.stream().anyMatch(b -> b.getMessageId().equals(resp.getMessageId()) && !b.getIsRead());
        assertTrue(foundInbox, "收件人信箱中必须包含未读的 INBOX 条目");
    }

    @Test
    @DisplayName("TC-MSG-02: 发信防伪造拦截测试 (禁止人工通道伪造系统邮件)")
    void testTC_MSG_02_ManualSystemMessageForbiddenRule() {
        // 尝试在人工接口中传入 rootCategory = SYSTEM
        SendManualMessageRequest attackReq1 = new SendManualMessageRequest();
        attackReq1.setRootCategory(MessageRootCategory.SYSTEM);
        attackReq1.setSubject("【伪造通知】虚假阶段门审批通知");
        attackReq1.setContent("伪造内容");
        attackReq1.setRecipients(Collections.singletonList(new SendManualMessageRequest.RecipientItem("admin", "系统管理员", "TO")));

        ManualSystemMessageForbiddenException ex1 = assertThrows(ManualSystemMessageForbiddenException.class, () ->
                service.sendManualMessage(attackReq1, "hacker", "黑客用户")
        );
        assertTrue(ex1.getMessage().contains("人工发信通道严禁伪造系统消息分类"));

        // 尝试传入 isSystemGenerated = true
        SendManualMessageRequest attackReq2 = new SendManualMessageRequest();
        attackReq2.setRootCategory(MessageRootCategory.MANUAL);
        attackReq2.setIsSystemGenerated(true);
        attackReq2.setSubject("【伪造系统标志】虚假变更确认");
        attackReq2.setContent("伪造系统标志");
        attackReq2.setRecipients(Collections.singletonList(new SendManualMessageRequest.RecipientItem("admin", "系统管理员", "TO")));

        ManualSystemMessageForbiddenException ex2 = assertThrows(ManualSystemMessageForbiddenException.class, () ->
                service.sendManualMessage(attackReq2, "hacker", "黑客用户")
        );
        assertTrue(ex2.getMessage().contains("人工发信通道严禁伪造系统消息分类"));
    }

    @Test
    @DisplayName("TC-MSG-03: 系统自动派发通知 (Object-Centric 链接与高可靠落盘)")
    void testTC_MSG_03_DispatchSystemMessageSuccess() {
        DispatchSystemMessageCommand cmd = new DispatchSystemMessageCommand();
        cmd.setSystemType(SystemMessageType.CHANGE);
        cmd.setSubject("【变更推送】主轴驱动单元参数调整");
        cmd.setContent("主轴驱动电机参数已更新，请关注热特性影响。");
        cmd.setPriority(MessagePriority.HIGH);
        cmd.setRelatedProjectId("VMC_ENTERPRISE");
        cmd.setRelatedObjType("ECO");
        cmd.setRelatedObjId("ECO-2026-0042");
        cmd.setTargetActionUrl("/change/eco/ECO-2026-0042");
        cmd.setActionIdentifier("ECO_COLLABORATION_REVIEW");
        cmd.setRecipientUserIds(Arrays.asList("chief_designer", "lead_analyst"));

        SendMessageResponse resp = service.dispatchSystemMessage(cmd);
        assertNotNull(resp);
        assertEquals(MessageRootCategory.SYSTEM, resp.getRootCategory());
        assertEquals(2, resp.getRecipientCount());

        // 验证总设计师收件箱接收到系统消息
        MessageDetailDto detail = service.getMessageDetail(resp.getMessageId(), "chief_designer");
        assertNotNull(detail);
        assertEquals("SYSTEM_NOTIFIER", detail.getSenderUserId());
        assertEquals(SystemMessageType.CHANGE, detail.getSystemType());
        assertEquals("ECO-2026-0042", detail.getRelatedObjId());
    }

    @Test
    @DisplayName("TC-MSG-04: 信箱分页查询、多维过滤与查阅详情自动已读测试")
    void testTC_MSG_04_MailboxQueryAndAutomaticReadStatus() {
        // 种子数据中 chief_designer 有种子消息 9001L (未读)
        MailboxQueryParam query = new MailboxQueryParam();
        query.setUserId("chief_designer");
        query.setBoxType(MailboxBoxType.INBOX);
        query.setIsRead(false);

        MailboxPageDto page1 = service.queryUserMailbox(query);
        assertNotNull(page1);
        assertTrue(page1.getTotalElements() >= 1, "未读收件箱至少应有1条种子消息");

        // 打开 9001L 详情
        MessageDetailDto detail = service.getMessageDetail(9001L, "chief_designer");
        assertNotNull(detail);
        assertTrue(detail.getIsRead(), "查看详情后，该信箱条目必须自动置为已读");

        // 再次查询未读，9001L 不应再出现在未读过滤结果中
        MailboxPageDto page2 = service.queryUserMailbox(query);
        boolean stillUnread = page2.getItems().stream().anyMatch(i -> i.getMessageId().equals(9001L));
        assertFalse(stillUnread, "9001L 已被阅读，不应再为未读");
    }

    @Test
    @DisplayName("TC-MSG-05: PBAC 跨域越权硬拦截测试 (防止通过邮件跳过权限访问敏感工程对象)")
    void testTC_MSG_05_PbacCrossObjectProtectionBlocked() {
        // 1. 合法授权机床工程师 (chief_designer) 直达 ECO-2026-0042
        ResolveActionResponse authorizedResp = service.resolveActionUrl(9001L, "chief_designer");
        assertNotNull(authorizedResp);
        assertTrue(authorizedResp.getAllowed());
        assertEquals("/change/eco/ECO-2026-0042", authorizedResp.getTargetActionUrl());

        // 2. 外部/未授权机密权限的用户 (unauthorized_guest) 试图穿透跳转直达
        PbacCrossObjectUnauthorizedException ex = assertThrows(PbacCrossObjectUnauthorizedException.class, () ->
                service.resolveActionUrl(9001L, "unauthorized_guest")
        );
        assertTrue(ex.getMessage().contains("PBAC跨域越权阻断"));
    }

    @Test
    @DisplayName("TC-MSG-06: 邮件状态变更与底层工程任务强解耦测试")
    void testTC_MSG_06_MessageStatusDecouplingWithBusinessTask() {
        // 用户将阶段门预警邮件 9002L 标星并移动到归档箱
        UpdateMessageStatusRequest req = new UpdateMessageStatusRequest();
        req.setIsStarred(true);
        req.setIsArchived(true);

        boolean updated = service.updateMessageStatus(9002L, "chief_designer", req);
        assertTrue(updated);

        // 验证用户箱体中已变成 ARCHIVE
        MessageDetailDto detail = service.getMessageDetail(9002L, "chief_designer");
        assertEquals(MailboxBoxType.ARCHIVE, detail.getBoxType());
        assertTrue(detail.getIsStarred());

        // 强解耦断言：底层消息主实体中的相关机床工程对象字段 (GATE-3, VMC_ENTERPRISE) 绝不受到任何污染或修改
        assertEquals("GATE-3", detail.getRelatedObjId());
        assertEquals("VMC_ENTERPRISE", detail.getRelatedProjectId());
        assertNotNull(detail.getTargetActionUrl());
    }
}
