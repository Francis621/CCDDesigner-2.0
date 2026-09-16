package com.ccdd.project.entity;

/**
 * 任务依赖拓扑实体 (TaskDependency - DAG 边)
 * 映射 plm_project.task_dependency
 */
public class TaskDependencyEntity {

    private Long predecessorTaskId;
    private Long successorTaskId;
    private DependencyType depType;
    private Integer lagDays;

    public TaskDependencyEntity() {
    }

    public TaskDependencyEntity(Long predecessorTaskId, Long successorTaskId, DependencyType depType, Integer lagDays) {
        this.predecessorTaskId = predecessorTaskId;
        this.successorTaskId = successorTaskId;
        this.depType = depType;
        this.lagDays = lagDays;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long predecessorTaskId;
        private Long successorTaskId;
        private DependencyType depType = DependencyType.FS;
        private Integer lagDays = 0;

        public Builder predecessorTaskId(Long predecessorTaskId) { this.predecessorTaskId = predecessorTaskId; return this; }
        public Builder successorTaskId(Long successorTaskId) { this.successorTaskId = successorTaskId; return this; }
        public Builder depType(DependencyType depType) { this.depType = depType; return this; }
        public Builder lagDays(Integer lagDays) { this.lagDays = lagDays; return this; }

        public TaskDependencyEntity build() {
            return new TaskDependencyEntity(predecessorTaskId, successorTaskId, depType, lagDays);
        }
    }

    public Long getPredecessorTaskId() { return predecessorTaskId; }
    public void setPredecessorTaskId(Long predecessorTaskId) { this.predecessorTaskId = predecessorTaskId; }
    public Long getSuccessorTaskId() { return successorTaskId; }
    public void setSuccessorTaskId(Long successorTaskId) { this.successorTaskId = successorTaskId; }
    public DependencyType getDepType() { return depType; }
    public void setDepType(DependencyType depType) { this.depType = depType; }
    public Integer getLagDays() { return lagDays; }
    public void setLagDays(Integer lagDays) { this.lagDays = lagDays; }
}
