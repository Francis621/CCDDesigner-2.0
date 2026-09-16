package com.ccdd.project.entity;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * WBS 工作分解结构树节点实体 (WBSNode)
 * 映射 plm_project.wbs_node
 */
public class WbsNodeEntity {

    private Long wbsNodeId;
    private Long projectId;
    private Long parentNodeId;
    private String wbsCode;
    private String name;
    private Integer nodeLevel;
    private BigDecimal weight;
    private Instant createdAt;

    public WbsNodeEntity() {
    }

    public WbsNodeEntity(Long wbsNodeId, Long projectId, Long parentNodeId, String wbsCode,
                         String name, Integer nodeLevel, BigDecimal weight, Instant createdAt) {
        this.wbsNodeId = wbsNodeId;
        this.projectId = projectId;
        this.parentNodeId = parentNodeId;
        this.wbsCode = wbsCode;
        this.name = name;
        this.nodeLevel = nodeLevel;
        this.weight = weight;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long wbsNodeId;
        private Long projectId;
        private Long parentNodeId;
        private String wbsCode;
        private String name;
        private Integer nodeLevel = 1;
        private BigDecimal weight = new BigDecimal("1.00");
        private Instant createdAt = Instant.now();

        public Builder wbsNodeId(Long wbsNodeId) { this.wbsNodeId = wbsNodeId; return this; }
        public Builder projectId(Long projectId) { this.projectId = projectId; return this; }
        public Builder parentNodeId(Long parentNodeId) { this.parentNodeId = parentNodeId; return this; }
        public Builder wbsCode(String wbsCode) { this.wbsCode = wbsCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder nodeLevel(Integer nodeLevel) { this.nodeLevel = nodeLevel; return this; }
        public Builder weight(BigDecimal weight) { this.weight = weight; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public WbsNodeEntity build() {
            return new WbsNodeEntity(wbsNodeId, projectId, parentNodeId, wbsCode, name, nodeLevel, weight, createdAt);
        }
    }

    public Long getWbsNodeId() { return wbsNodeId; }
    public void setWbsNodeId(Long wbsNodeId) { this.wbsNodeId = wbsNodeId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getParentNodeId() { return parentNodeId; }
    public void setParentNodeId(Long parentNodeId) { this.parentNodeId = parentNodeId; }
    public String getWbsCode() { return wbsCode; }
    public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getNodeLevel() { return nodeLevel; }
    public void setNodeLevel(Integer nodeLevel) { this.nodeLevel = nodeLevel; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
