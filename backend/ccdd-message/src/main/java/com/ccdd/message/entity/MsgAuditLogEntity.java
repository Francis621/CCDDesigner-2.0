package com.ccdd.message.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 消息审计与防篡改日志实体 (对应 plm_msg.msg_audit_log)
 */
public class MsgAuditLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long messageId;
    private String action;
    private String operatorUserId;
    private String detail;
    private Instant timestamp;

    public MsgAuditLogEntity() {
    }

    public MsgAuditLogEntity(Long logId, Long messageId, String action, String operatorUserId, String detail, Instant timestamp) {
        this.logId = logId;
        this.messageId = messageId;
        this.action = action;
        this.operatorUserId = operatorUserId;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(String operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
