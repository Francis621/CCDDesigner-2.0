package com.ccdd.common.context;

import java.io.Serializable;
import java.time.Instant;

/**
 * CCDDesigner 2.0 统一工程上下文契约 (纯原生 Java 实现)
 */
public class EngineeringContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tenantId;
    private String projectId;
    private String targetRevisionRef;
    private String operatorUserId;
    private String securityClearance; // PUBLIC, INTERNAL, CONFIDENTIAL, STRICTLY_CONFIDENTIAL
    private ConfigurationContext configurationContext;

    public EngineeringContext() {
    }

    public EngineeringContext(String tenantId, String projectId, String targetRevisionRef, String operatorUserId, String securityClearance, ConfigurationContext configurationContext) {
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.targetRevisionRef = targetRevisionRef;
        this.operatorUserId = operatorUserId;
        this.securityClearance = securityClearance;
        this.configurationContext = configurationContext;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String tenantId;
        private String projectId;
        private String targetRevisionRef;
        private String operatorUserId;
        private String securityClearance;
        private ConfigurationContext configurationContext;

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder targetRevisionRef(String targetRevisionRef) {
            this.targetRevisionRef = targetRevisionRef;
            return this;
        }

        public Builder operatorUserId(String operatorUserId) {
            this.operatorUserId = operatorUserId;
            return this;
        }

        public Builder securityClearance(String securityClearance) {
            this.securityClearance = securityClearance;
            return this;
        }

        public Builder configurationContext(ConfigurationContext configurationContext) {
            this.configurationContext = configurationContext;
            return this;
        }

        public EngineeringContext build() {
            return new EngineeringContext(tenantId, projectId, targetRevisionRef, operatorUserId, securityClearance, configurationContext);
        }
    }

    public static class ConfigurationContext implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long baselineId;
        private String globalConfigUri;
        private Instant effectiveAt;

        public ConfigurationContext() {
        }

        public ConfigurationContext(Long baselineId, String globalConfigUri, Instant effectiveAt) {
            this.baselineId = baselineId;
            this.globalConfigUri = globalConfigUri;
            this.effectiveAt = effectiveAt;
        }

        public static ConfigurationContextBuilder builder() {
            return new ConfigurationContextBuilder();
        }

        public static class ConfigurationContextBuilder {
            private Long baselineId;
            private String globalConfigUri;
            private Instant effectiveAt;

            public ConfigurationContextBuilder baselineId(Long baselineId) {
                this.baselineId = baselineId;
                return this;
            }

            public ConfigurationContextBuilder globalConfigUri(String globalConfigUri) {
                this.globalConfigUri = globalConfigUri;
                return this;
            }

            public ConfigurationContextBuilder effectiveAt(Instant effectiveAt) {
                this.effectiveAt = effectiveAt;
                return this;
            }

            public ConfigurationContext build() {
                return new ConfigurationContext(baselineId, globalConfigUri, effectiveAt);
            }
        }

        public Long getBaselineId() { return baselineId; }
        public void setBaselineId(Long baselineId) { this.baselineId = baselineId; }
        public String getGlobalConfigUri() { return globalConfigUri; }
        public void setGlobalConfigUri(String globalConfigUri) { this.globalConfigUri = globalConfigUri; }
        public Instant getEffectiveAt() { return effectiveAt; }
        public void setEffectiveAt(Instant effectiveAt) { this.effectiveAt = effectiveAt; }
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getTargetRevisionRef() { return targetRevisionRef; }
    public void setTargetRevisionRef(String targetRevisionRef) { this.targetRevisionRef = targetRevisionRef; }
    public String getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(String operatorUserId) { this.operatorUserId = operatorUserId; }
    public String getSecurityClearance() { return securityClearance; }
    public void setSecurityClearance(String securityClearance) { this.securityClearance = securityClearance; }
    public ConfigurationContext getConfigurationContext() { return configurationContext; }
    public void setConfigurationContext(ConfigurationContext configurationContext) { this.configurationContext = configurationContext; }
}
