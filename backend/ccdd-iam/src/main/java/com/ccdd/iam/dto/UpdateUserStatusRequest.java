package com.ccdd.iam.dto;

import com.ccdd.iam.entity.UserAccountStatus;

/**
 * 更新用户状态请求 DTO
 */
public class UpdateUserStatusRequest {

    private UserAccountStatus targetStatus;
    private String reason;

    public UpdateUserStatusRequest() {
    }

    public UpdateUserStatusRequest(UserAccountStatus targetStatus, String reason) {
        this.targetStatus = targetStatus;
        this.reason = reason;
    }

    public UserAccountStatus getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(UserAccountStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
