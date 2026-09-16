package com.ccdd.change.dto;

/**
 * 创建实施任务请求 DTO
 */
public class CreateChangeTaskRequest {

    private String taskCode;
    private String title;
    private String taskType; // CAD_REMODEL, SIM_RERUN, REQ_UPDATE, DRAWING_REDRAW
    private String assigneeId;
    private Long sourceRevisionId;
    private Boolean enforceFffCheck; // 是否执行 ADR-05 形状配合功能互换性校验

    public CreateChangeTaskRequest() {
        this.enforceFffCheck = true;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getSourceRevisionId() {
        return sourceRevisionId;
    }

    public void setSourceRevisionId(Long sourceRevisionId) {
        this.sourceRevisionId = sourceRevisionId;
    }

    public Boolean getEnforceFffCheck() {
        return enforceFffCheck;
    }

    public void setEnforceFffCheck(Boolean enforceFffCheck) {
        this.enforceFffCheck = enforceFffCheck;
    }
}
