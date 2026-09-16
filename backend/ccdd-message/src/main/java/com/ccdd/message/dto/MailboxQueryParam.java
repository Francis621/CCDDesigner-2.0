package com.ccdd.message.dto;

import com.ccdd.message.entity.MailboxBoxType;
import com.ccdd.message.entity.MessageRootCategory;
import com.ccdd.message.entity.SystemMessageType;

import java.io.Serializable;

/**
 * 邮箱分页检索参数 (对齐 OpenAPI 8.1)
 */
public class MailboxQueryParam implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private MailboxBoxType boxType = MailboxBoxType.INBOX;
    private MessageRootCategory rootCategory;
    private SystemMessageType subType;
    private Boolean isRead;
    private Boolean isStarred;
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 20;

    public MailboxQueryParam() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MailboxBoxType getBoxType() {
        return boxType;
    }

    public void setBoxType(MailboxBoxType boxType) {
        this.boxType = boxType;
    }

    public MessageRootCategory getRootCategory() {
        return rootCategory;
    }

    public void setRootCategory(MessageRootCategory rootCategory) {
        this.rootCategory = rootCategory;
    }

    public SystemMessageType getSubType() {
        return subType;
    }

    public void setSubType(SystemMessageType subType) {
        this.subType = subType;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean read) {
        isRead = read;
    }

    public Boolean getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(Boolean starred) {
        isStarred = starred;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageNum() {
        return page;
    }

    public void setPageNum(Integer pageNum) {
        this.page = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
