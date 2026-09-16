package com.ccdd.message.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 邮件附件实体 (对应 plm_msg.msg_attachment)
 */
public class MsgAttachmentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long attachmentId;
    private Long messageId;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String downloadUrl;
    private Instant uploadedAt;

    public MsgAttachmentEntity() {
    }

    public MsgAttachmentEntity(Long attachmentId, Long messageId, String fileName, Long fileSize,
                               String fileType, String downloadUrl, Instant uploadedAt) {
        this.attachmentId = attachmentId;
        this.messageId = messageId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.downloadUrl = downloadUrl;
        this.uploadedAt = uploadedAt;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
