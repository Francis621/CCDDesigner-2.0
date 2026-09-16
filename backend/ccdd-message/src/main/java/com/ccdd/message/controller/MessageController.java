package com.ccdd.message.controller;

import com.ccdd.common.api.Result;
import com.ccdd.message.dto.DispatchSystemMessageCommand;
import com.ccdd.message.dto.MailboxPageDto;
import com.ccdd.message.dto.MailboxQueryParam;
import com.ccdd.message.dto.MessageDetailDto;
import com.ccdd.message.dto.ResolveActionResponse;
import com.ccdd.message.dto.SendManualMessageRequest;
import com.ccdd.message.dto.SendMessageResponse;
import com.ccdd.message.dto.UpdateMessageStatusRequest;
import com.ccdd.message.entity.MailboxBoxType;
import com.ccdd.message.entity.MessageRootCategory;
import com.ccdd.message.exception.ManualSystemMessageForbiddenException;
import com.ccdd.message.exception.MessageAccessDeniedException;
import com.ccdd.message.exception.PbacCrossObjectUnauthorizedException;
import com.ccdd.message.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * M01-MSG PLM 内部邮件与消息中心 RESTful 控制器
 * 严格遵从 OpenAPI 契约规范与 PBAC 安全拦截规范
 */
@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * OpenAPI 8.1: 人工写信与跨部门邮件投递
     */
    @PostMapping("/manual")
    public Result<SendMessageResponse> sendManualMessage(
            @RequestBody SendManualMessageRequest request,
            @RequestHeader(value = "X-Current-User-Id", required = false, defaultValue = "chief_designer") String currentUserId,
            @RequestHeader(value = "X-Current-User-Name", required = false, defaultValue = "总设计师") String currentUserName) {
        SendMessageResponse response = messageService.sendManualMessage(request, currentUserId, currentUserName);
        return Result.success(response);
    }

    /**
     * 系统内部自动派发通知接口（供机床研制事件监听器或工作流引擎调用）
     */
    @PostMapping("/dispatch-system")
    public Result<SendMessageResponse> dispatchSystemMessage(@RequestBody DispatchSystemMessageCommand command) {
        SendMessageResponse response = messageService.dispatchSystemMessage(command);
        return Result.success(response);
    }

    /**
     * OpenAPI 8.2: 分页与多条件查询用户信箱（收件箱、发件箱、归档箱、废纸篓）
     */
    @GetMapping("/mailbox")
    public Result<MailboxPageDto> queryMailbox(
            @RequestParam(value = "boxType", required = false, defaultValue = "INBOX") String boxType,
            @RequestParam(value = "rootCategory", required = false) String rootCategory,
            @RequestParam(value = "isRead", required = false) Boolean isRead,
            @RequestParam(value = "isStarred", required = false) Boolean isStarred,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
            @RequestHeader(value = "X-Current-User-Id", required = false, defaultValue = "chief_designer") String currentUserId) {

        MailboxQueryParam param = new MailboxQueryParam();
        param.setUserId(currentUserId);
        if (boxType != null && !boxType.trim().isEmpty()) {
            param.setBoxType(MailboxBoxType.valueOf(boxType.toUpperCase()));
        }
        if (rootCategory != null && !rootCategory.trim().isEmpty()) {
            param.setRootCategory(MessageRootCategory.valueOf(rootCategory.toUpperCase()));
        }
        param.setIsRead(isRead);
        param.setIsStarred(isStarred);
        param.setKeyword(keyword);
        param.setPageNum(pageNum);
        param.setPageSize(pageSize);

        MailboxPageDto pageDto = messageService.queryUserMailbox(param);
        return Result.success(pageDto);
    }

    /**
     * OpenAPI 8.3: 查看邮件详情，自动标为已读
     */
    @GetMapping("/{id}")
    public Result<MessageDetailDto> getMessageDetail(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-Current-User-Id", required = false, defaultValue = "chief_designer") String currentUserId) {
        MessageDetailDto detail = messageService.getMessageDetail(id, currentUserId);
        return Result.success(detail);
    }

    /**
     * 更新邮件状态（已读、星标、归档、移入废纸篓）
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateMessageStatus(
            @PathVariable("id") Long id,
            @RequestBody UpdateMessageStatusRequest request,
            @RequestHeader(value = "X-Current-User-Id", required = false, defaultValue = "chief_designer") String currentUserId) {
        boolean success = messageService.updateMessageStatus(id, currentUserId, request);
        return Result.success(success);
    }

    /**
     * 业务直达解析与 PBAC 跨域越权硬拦截
     */
    @PostMapping("/{id}/resolve-action")
    public Result<ResolveActionResponse> resolveActionUrl(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-Current-User-Id", required = false, defaultValue = "chief_designer") String currentUserId) {
        ResolveActionResponse response = messageService.resolveActionUrl(id, currentUserId);
        return Result.success(response);
    }

    /**
     * 获取未读邮件数量统计
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(
            @RequestHeader(value = "X-Current-User-Id", required = false, defaultValue = "chief_designer") String currentUserId) {
        return Result.success(messageService.getUnreadCount(currentUserId));
    }

    // ================== 异常处理与精准 HTTP 状态码映射 ==================

    @ExceptionHandler(ManualSystemMessageForbiddenException.class)
    public ResponseEntity<Result<Void>> handleManualSystemForbidden(ManualSystemMessageForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Result.error(422, ex.getMessage()));
    }

    @ExceptionHandler(PbacCrossObjectUnauthorizedException.class)
    public ResponseEntity<Result<Void>> handlePbacUnauthorized(PbacCrossObjectUnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(403, ex.getMessage()));
    }

    @ExceptionHandler(MessageAccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(MessageAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(403, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, ex.getMessage()));
    }
}
