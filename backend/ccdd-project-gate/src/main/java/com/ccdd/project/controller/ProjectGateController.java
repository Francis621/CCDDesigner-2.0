package com.ccdd.project.controller;

import com.ccdd.common.api.Result;
import com.ccdd.project.dto.GatePreCheckResultDto;
import com.ccdd.project.dto.RecordGateDecisionRequest;
import com.ccdd.project.dto.TaskCpmAnalysisDto;
import com.ccdd.project.entity.ActionItemEntity;
import com.ccdd.project.entity.DeliverableSubmissionEntity;
import com.ccdd.project.entity.GateDecisionEntity;
import com.ccdd.project.service.ProjectGateService;
import com.ccdd.project.service.WbsTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目、任务与阶段门 (M02) REST API 控制器
 */
@RestController
@RequestMapping("/api/v1/projects")
@CrossOrigin(origins = "*")
public class ProjectGateController {

    private final ProjectGateService projectGateService;
    private final WbsTaskService wbsTaskService;

    public ProjectGateController(ProjectGateService projectGateService, WbsTaskService wbsTaskService) {
        this.projectGateService = projectGateService;
        this.wbsTaskService = wbsTaskService;
    }

    /**
     * 获取项目阶段与 TR 阶段门全景
     */
    @GetMapping("/{projectId}/gate-overview")
    public Result<Map<String, Object>> getGateOverview(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "TENANT_DEFAULT") String tenantId,
            @PathVariable Long projectId) {
        return Result.success(projectGateService.getGateOverview(tenantId, projectId));
    }

    /**
     * 阶段门准入预检 (AT-15 守护三原则核验)
     */
    @GetMapping("/{projectId}/gates/{gateId}/pre-check")
    public Result<GatePreCheckResultDto> preCheckGate(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "TENANT_DEFAULT") String tenantId,
            @PathVariable Long projectId,
            @PathVariable Long gateId) {
        return Result.success(projectGateService.preCheckGate(tenantId, projectId, gateId));
    }

    /**
     * 签署录入阶段门评审决策
     */
    @PostMapping("/{projectId}/gates/{gateId}/decisions")
    public Result<GateDecisionEntity> recordGateDecision(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "TENANT_DEFAULT") String tenantId,
            @PathVariable Long projectId,
            @PathVariable Long gateId,
            @RequestBody RecordGateDecisionRequest request) {
        return Result.success(projectGateService.recordGateDecision(tenantId, projectId, gateId, request));
    }

    /**
     * 获取阶段门关联的整改行动项列表
     */
    @GetMapping("/{projectId}/gates/{gateId}/action-items")
    public Result<List<ActionItemEntity>> getActionItems(
            @PathVariable Long projectId,
            @PathVariable Long gateId) {
        return Result.success(projectGateService.getActionItemsForGate(gateId));
    }

    /**
     * 闭环整改行动项
     */
    @PostMapping("/{projectId}/gates/{gateId}/action-items/{actionItemId}/close")
    public Result<ActionItemEntity> closeActionItem(
            @PathVariable Long projectId,
            @PathVariable Long gateId,
            @PathVariable Long actionItemId,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return Result.success(projectGateService.closeActionItem(gateId, actionItemId, notes));
    }

    /**
     * 获取 WBS 结构树与任务依赖网络
     */
    @GetMapping("/{projectId}/wbs-tasks")
    public Result<Map<String, Object>> getWbsTasks(@PathVariable Long projectId) {
        return Result.success(wbsTaskService.getWbsAndTasks(projectId));
    }

    /**
     * 执行 CPM 关键路径与 DAG 防环探测
     */
    @GetMapping("/{projectId}/cpm-analysis")
    public Result<TaskCpmAnalysisDto> analyzeCpm(@PathVariable Long projectId) {
        return Result.success(wbsTaskService.analyzeCpmAndDetectCycles(projectId));
    }

    /**
     * 获取任务关联的交付物要求与提审历史
     */
    @GetMapping("/{projectId}/tasks/{taskId}/deliverables")
    public Result<List<Map<String, Object>>> getTaskDeliverables(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return Result.success(wbsTaskService.getTaskDeliverables(taskId));
    }

    /**
     * 提交/迭代交付物新版本 (三态独立)
     */
    @PostMapping("/{projectId}/tasks/{taskId}/deliverables/{reqId}/submit")
    public Result<DeliverableSubmissionEntity> submitDeliverable(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long reqId,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : "版本交付成果归档";
        String user = body != null ? body.get("user") : "CURRENT_ENGINEER";
        return Result.success(wbsTaskService.submitDeliverable(taskId, reqId, notes, user));
    }
}
