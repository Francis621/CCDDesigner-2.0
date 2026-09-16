package com.ccdd.message.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮箱分页数据响应包装 DTO (对齐 OpenAPI 8.1)
 */
public class MailboxPageDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long total;
    private Integer page;
    private Integer pageSize;
    private Long unreadCount;
    private List<MailboxItemDto> items = new ArrayList<>();

    public MailboxPageDto() {
    }

    public MailboxPageDto(Long total, Integer page, Integer pageSize, Long unreadCount, List<MailboxItemDto> items) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.unreadCount = unreadCount;
        this.items = items;
    }

    public MailboxPageDto(List<MailboxItemDto> items, Long total, Integer page, Integer pageSize, Long unreadCount) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.unreadCount = unreadCount;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getTotalElements() {
        return total;
    }

    public void setTotalElements(Long totalElements) {
        this.total = totalElements;
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

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<MailboxItemDto> getItems() {
        return items;
    }

    public void setItems(List<MailboxItemDto> items) {
        this.items = items;
    }
}
