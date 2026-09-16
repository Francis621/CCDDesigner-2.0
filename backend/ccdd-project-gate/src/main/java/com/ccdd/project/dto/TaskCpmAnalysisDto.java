package com.ccdd.project.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务网络关键路径 (CPM) 与时差分析结果 DTO
 */
public class TaskCpmAnalysisDto {

    private Long projectId;
    private Integer criticalPathLengthDays;
    private List<String> criticalPathTaskCodes = new ArrayList<>();
    private List<TaskScheduleMetricDto> taskMetrics = new ArrayList<>();

    public TaskCpmAnalysisDto() {
    }

    public static class TaskScheduleMetricDto {
        private Long taskId;
        private String taskCode;
        private String taskName;
        private Integer durationDays;
        private Integer earlyStartDay;
        private Integer earlyFinishDay;
        private Integer lateStartDay;
        private Integer lateFinishDay;
        private Integer totalFloatDays; // 总时差 TF = LS - ES
        private Boolean isCritical;     // TF == 0

        public TaskScheduleMetricDto() {
        }

        public TaskScheduleMetricDto(Long taskId, String taskCode, String taskName, Integer durationDays,
                                     Integer earlyStartDay, Integer earlyFinishDay, Integer lateStartDay,
                                     Integer lateFinishDay, Integer totalFloatDays, Boolean isCritical) {
            this.taskId = taskId;
            this.taskCode = taskCode;
            this.taskName = taskName;
            this.durationDays = durationDays;
            this.earlyStartDay = earlyStartDay;
            this.earlyFinishDay = earlyFinishDay;
            this.lateStartDay = lateStartDay;
            this.lateFinishDay = lateFinishDay;
            this.totalFloatDays = totalFloatDays;
            this.isCritical = isCritical;
        }

        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public String getTaskCode() { return taskCode; }
        public void setTaskCode(String taskCode) { this.taskCode = taskCode; }
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        public Integer getDurationDays() { return durationDays; }
        public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
        public Integer getEarlyStartDay() { return earlyStartDay; }
        public void setEarlyStartDay(Integer earlyStartDay) { this.earlyStartDay = earlyStartDay; }
        public Integer getEarlyFinishDay() { return earlyFinishDay; }
        public void setEarlyFinishDay(Integer earlyFinishDay) { this.earlyFinishDay = earlyFinishDay; }
        public Integer getLateStartDay() { return lateStartDay; }
        public void setLateStartDay(Integer lateStartDay) { this.lateStartDay = lateStartDay; }
        public Integer getLateFinishDay() { return lateFinishDay; }
        public void setLateFinishDay(Integer lateFinishDay) { this.lateFinishDay = lateFinishDay; }
        public Integer getTotalFloatDays() { return totalFloatDays; }
        public void setTotalFloatDays(Integer totalFloatDays) { this.totalFloatDays = totalFloatDays; }
        public Boolean getIsCritical() { return isCritical; }
        public void setIsCritical(Boolean critical) { isCritical = critical; }
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Integer getCriticalPathLengthDays() { return criticalPathLengthDays; }
    public void setCriticalPathLengthDays(Integer criticalPathLengthDays) { this.criticalPathLengthDays = criticalPathLengthDays; }
    public List<String> getCriticalPathTaskCodes() { return criticalPathTaskCodes; }
    public void setCriticalPathTaskCodes(List<String> criticalPathTaskCodes) { this.criticalPathTaskCodes = criticalPathTaskCodes; }
    public List<TaskScheduleMetricDto> getTaskMetrics() { return taskMetrics; }
    public void setTaskMetrics(List<TaskScheduleMetricDto> taskMetrics) { this.taskMetrics = taskMetrics; }
}
