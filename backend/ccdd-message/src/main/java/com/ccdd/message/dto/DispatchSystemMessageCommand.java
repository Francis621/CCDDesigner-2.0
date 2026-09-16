package com.ccdd.message.dto;

import com.ccdd.message.entity.MessagePriority;
import com.ccdd.message.entity.SystemMessageType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 内部微服务事件派发系统通知指令 DTO
 * 仅供内部事件监听器安全调用
 */
public class DispatchSystemMessageCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private SystemMessageType systemType;
    private String subject;
    private String content;
    private List<String> recipientUserIds = new ArrayList<>();
    private String relatedProjectId;
    private String relatedObjType;
    private String relatedObjId;
    private String targetActionUrl;
    private String actionIdentifier;
    private String actionPayloadJson;
    private MessagePriority priority = MessagePriority.NORMAL;
    private String correlationEventId;

    public DispatchSystemMessageCommand() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DispatchSystemMessageCommand cmd = new DispatchSystemMessageCommand();

        public Builder systemType(SystemMessageType systemType) {
            cmd.setSystemType(systemType);
            return this;
        }

        public Builder subType(SystemMessageType subType) {
            cmd.setSystemType(subType);
            return this;
        }

        public Builder subject(String subject) {
            cmd.setSubject(subject);
            return this;
        }

        public Builder content(String content) {
            cmd.setContent(content);
            return this;
        }

        public Builder recipientUserIds(List<String> recipientUserIds) {
            cmd.setRecipientUserIds(recipientUserIds);
            return this;
        }

        public Builder relatedProjectId(String relatedProjectId) {
            cmd.setRelatedProjectId(relatedProjectId);
            return this;
        }

        public Builder relatedObjType(String relatedObjType) {
            cmd.setRelatedObjType(relatedObjType);
            return this;
        }

        public Builder relatedObjId(String relatedObjId) {
            cmd.setRelatedObjId(relatedObjId);
            return this;
        }

        public Builder targetActionUrl(String targetActionUrl) {
            cmd.setTargetActionUrl(targetActionUrl);
            return this;
        }

        public Builder actionIdentifier(String actionIdentifier) {
            cmd.setActionIdentifier(actionIdentifier);
            return this;
        }

        public Builder actionPayloadJson(String actionPayloadJson) {
            cmd.setActionPayloadJson(actionPayloadJson);
            return this;
        }

        public Builder priority(MessagePriority priority) {
            cmd.setPriority(priority);
            return this;
        }

        public Builder correlationEventId(String correlationEventId) {
            cmd.setCorrelationEventId(correlationEventId);
            return this;
        }

        public DispatchSystemMessageCommand build() {
            return cmd;
        }
    }

    public SystemMessageType getSystemType() {
        return systemType;
    }

    public void setSystemType(SystemMessageType systemType) {
        this.systemType = systemType;
    }

    public SystemMessageType getSubType() {
        return systemType;
    }

    public void setSubType(SystemMessageType subType) {
        this.systemType = subType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBodyContent() {
        return content;
    }

    public void setBodyContent(String bodyContent) {
        this.content = bodyContent;
    }

    public List<String> getRecipientUserIds() {
        return recipientUserIds;
    }

    public void setRecipientUserIds(List<String> recipientUserIds) {
        this.recipientUserIds = recipientUserIds;
    }

    public String getRelatedProjectId() {
        return relatedProjectId;
    }

    public void setRelatedProjectId(String relatedProjectId) {
        this.relatedProjectId = relatedProjectId;
    }

    public String getRelatedObjType() {
        return relatedObjType;
    }

    public void setRelatedObjType(String relatedObjType) {
        this.relatedObjType = relatedObjType;
    }

    public String getRelatedObjectType() {
        return relatedObjType;
    }

    public void setRelatedObjectType(String relatedObjectType) {
        this.relatedObjType = relatedObjectType;
    }

    public String getRelatedObjId() {
        return relatedObjId;
    }

    public void setRelatedObjId(String relatedObjId) {
        this.relatedObjId = relatedObjId;
    }

    public String getTargetActionUrl() {
        return targetActionUrl;
    }

    public void setTargetActionUrl(String targetActionUrl) {
        this.targetActionUrl = targetActionUrl;
    }

    public String getActionIdentifier() {
        return actionIdentifier;
    }

    public void setActionIdentifier(String actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
    }

    public String getActionPayloadJson() {
        return actionPayloadJson;
    }

    public void setActionPayloadJson(String actionPayloadJson) {
        this.actionPayloadJson = actionPayloadJson;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
    }

    public String getCorrelationEventId() {
        return correlationEventId;
    }

    public void setCorrelationEventId(String correlationEventId) {
        this.correlationEventId = correlationEventId;
    }
}
