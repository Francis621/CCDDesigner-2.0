package com.ccdd.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * CCDDesigner 2.0 统一工程上下文契约
 * 跨模块调用、异步作业与事件通知必须显式携带，严禁隐式动态指代"最新版本"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngineeringContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 租户全局唯一标识 */
    private String tenantId;

    /** 所属研制项目编号 */
    private String projectId;

    /** 当前操作的目标版本或主对象引用 */
    private String targetRevisionRef;

    /** 关联的操作用户ID */
    private String operatorUserId;

    /** 配置与时态上下文 */
    private ConfigurationContext configurationContext;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigurationContext implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 锁定的基线ID (若在基线受控范围内) */
        private Long baselineId;

        /** 全局产品配置特征表达式 URI */
        private String globalConfigUri;

        /** 工程生效基准时点 */
        private Instant effectiveAt;
    }
}
