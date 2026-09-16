package com.ccdd.thread.dto;

import java.io.Serializable;

/**
 * 数字主线递归遍历单步路径 DTO
 */
public class TraversePathStep implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String domainType;
    private String displayName;
    private String version;
    private String lifecycleState;
    private String relationType;
    private int depth;
    private String path;
    private boolean cycleDetected;

    public TraversePathStep() {
    }

    public TraversePathStep(String nodeId, String domainType, String displayName, String version,
                            String lifecycleState, String relationType, int depth, String path, boolean cycleDetected) {
        this.nodeId = nodeId;
        this.domainType = domainType;
        this.displayName = displayName;
        this.version = version;
        this.lifecycleState = lifecycleState;
        this.relationType = relationType;
        this.depth = depth;
        this.path = path;
        this.cycleDetected = cycleDetected;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String nodeId;
        private String domainType;
        private String displayName;
        private String version;
        private String lifecycleState;
        private String relationType;
        private int depth;
        private String path;
        private boolean cycleDetected;

        public Builder nodeId(String nodeId) { this.nodeId = nodeId; return this; }
        public Builder domainType(String domainType) { this.domainType = domainType; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder version(String version) { this.version = version; return this; }
        public Builder lifecycleState(String lifecycleState) { this.lifecycleState = lifecycleState; return this; }
        public Builder relationType(String relationType) { this.relationType = relationType; return this; }
        public Builder depth(int depth) { this.depth = depth; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder cycleDetected(boolean cycleDetected) { this.cycleDetected = cycleDetected; return this; }

        public TraversePathStep build() {
            return new TraversePathStep(nodeId, domainType, displayName, version, lifecycleState, relationType, depth, path, cycleDetected);
        }
    }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getDomainType() { return domainType; }
    public void setDomainType(String domainType) { this.domainType = domainType; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getLifecycleState() { return lifecycleState; }
    public void setLifecycleState(String lifecycleState) { this.lifecycleState = lifecycleState; }

    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public boolean isCycleDetected() { return cycleDetected; }
    public void setCycleDetected(boolean cycleDetected) { this.cycleDetected = cycleDetected; }
}
