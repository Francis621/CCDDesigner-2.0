package com.ccdd.document.dto;

import java.time.Instant;

/**
 * 文档签出加锁请求与响应契约 (OpenAPI §8.3)
 */
public class CheckoutRequest {

    private Integer lockDurationHours; // 默认 8 小时
    private String comments;

    public CheckoutRequest() {
    }

    public CheckoutRequest(Integer lockDurationHours, String comments) {
        this.lockDurationHours = lockDurationHours;
        this.comments = comments;
    }

    public Integer getLockDurationHours() {
        return lockDurationHours;
    }

    public void setLockDurationHours(Integer lockDurationHours) {
        this.lockDurationHours = lockDurationHours;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
