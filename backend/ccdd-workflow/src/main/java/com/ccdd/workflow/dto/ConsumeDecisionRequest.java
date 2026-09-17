package com.ccdd.workflow.dto;

import java.io.Serializable;

/**
 * 业务状态机核销审批凭据请求 DTO (对齐 OpenAPI 8.3)
 */
public class ConsumeDecisionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 全局唯一业务动作序号 (幂等防重键)
     */
    private String actionId;

    /**
     * 业务实体当前最新的实际快照哈希 (AT-16 强校验防线)
     */
    private String expectedContentHash;

    public ConsumeDecisionRequest() {
    }

    public ConsumeDecisionRequest(String actionId, String expectedContentHash) {
        this.actionId = actionId;
        this.expectedContentHash = expectedContentHash;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getExpectedContentHash() {
        return expectedContentHash;
    }

    public void setExpectedContentHash(String expectedContentHash) {
        this.expectedContentHash = expectedContentHash;
    }
}
