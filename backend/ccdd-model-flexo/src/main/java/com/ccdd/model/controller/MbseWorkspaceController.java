package com.ccdd.model.controller;

import com.ccdd.common.api.Result;
import com.ccdd.model.dto.MbseWorkspaceDtos.*;
import com.ccdd.model.entity.CandidateSnapshot;
import com.ccdd.model.entity.WorkingElementBinding;
import com.ccdd.model.entity.WorkspaceBinding;
import com.ccdd.model.service.MbseWorkspaceService;
import com.ccdd.outbox.annotation.IdempotentApi;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * M04: MBSE 建模工作区 RESTful 控制器 (遵循 OpenAPI 3.0 / D09 规范)
 */
@RestController
@RequestMapping("/api/v1/mbse/workspaces")
public class MbseWorkspaceController {

    private final MbseWorkspaceService workspaceService;

    public MbseWorkspaceController(MbseWorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * 9.1 创建/绑定系统模型工作区
     */
    @PostMapping("/bind")
    @IdempotentApi
    public Result<WorkspaceBindResponse> bindWorkspace(
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-MECH-1042") String currentUserId,
            @RequestBody WorkspaceBindRequest request) {
        WorkspaceBindResponse response = workspaceService.bindWorkspace(request, currentUserId);
        return Result.success(response);
    }

    /**
     * 查询工作区信息
     */
    @GetMapping("/{workspaceId}")
    public Result<WorkspaceBinding> getWorkspace(@PathVariable("workspaceId") Long workspaceId) {
        return Result.success(workspaceService.getWorkspace(workspaceId));
    }

    /**
     * 9.2 打开编辑视口并获取授权会话锁
     */
    @PostMapping("/{workspaceId}/sessions")
    public Result<SessionLockResponse> acquireSessionLock(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-MECH-1042") String currentUserId,
            @RequestBody(required = false) SessionLockRequest request) {
        if (request == null) {
            request = new SessionLockRequest();
        }
        SessionLockResponse response = workspaceService.acquireSessionLock(workspaceId, request, currentUserId);
        return Result.success(response);
    }

    /**
     * 释放编辑会话锁
     */
    @DeleteMapping("/{workspaceId}/sessions")
    public Result<Boolean> releaseSessionLock(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-MECH-1042") String currentUserId) {
        boolean released = workspaceService.releaseSessionLock(workspaceId, currentUserId);
        return Result.success(released);
    }

    /**
     * 切换主编辑通道 (CST-M04-01 锁控)
     */
    @PutMapping("/{workspaceId}/channel")
    public Result<Void> switchChannel(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-MECH-1042") String currentUserId,
            @RequestBody ChannelSwitchRequest request) {
        workspaceService.switchAuthoringChannel(workspaceId, request.getTargetChannel(), currentUserId);
        return Result.success();
    }

    /**
     * 9.3 触发 OpenSysML 语义诊断
     */
    @PostMapping("/{workspaceId}/validate")
    public Result<ValidationOutcomeDto> validate(
            @PathVariable("workspaceId") Long workspaceId) {
        ValidationOutcomeDto outcome = workspaceService.validateWorkspace(workspaceId);
        return Result.success(outcome);
    }

    /**
     * 9.4 捕获候选快照并向 M06 发起发布交接 (PUB-04 守卫)
     */
    @PostMapping("/{workspaceId}/candidate-snapshots")
    @IdempotentApi
    public Result<SnapshotCaptureResponse> captureCandidateSnapshot(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-MECH-1042") String currentUserId,
            @RequestBody SnapshotCaptureRequest request) {
        SnapshotCaptureResponse response = workspaceService.captureAndHandoverSnapshot(workspaceId, request, currentUserId);
        return Result.success(response);
    }

    /**
     * 查询快照记录
     */
    @GetMapping("/{workspaceId}/candidate-snapshots")
    public Result<List<CandidateSnapshot>> listSnapshots(@PathVariable("workspaceId") Long workspaceId) {
        return Result.success(workspaceService.listSnapshots(workspaceId));
    }

    /**
     * 需求投影与临时工作绑定生成 (TC-M04-01)
     */
    @PostMapping("/{workspaceId}/elements/bind-requirement")
    public Result<WorkingElementBinding> bindRequirement(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestBody RequirementBindRequest request) {
        WorkingElementBinding binding = workspaceService.bindRequirementToModel(workspaceId, request);
        return Result.success(binding);
    }

    /**
     * 列出工作期临时绑定映射
     */
    @GetMapping("/{workspaceId}/elements/bindings")
    public Result<List<WorkingElementBinding>> listWorkingBindings(@PathVariable("workspaceId") Long workspaceId) {
        return Result.success(workspaceService.listWorkingBindings(workspaceId));
    }

    /**
     * 导出当前模型文本与哈希摘要
     */
    @GetMapping("/{workspaceId}/export")
    public Result<Object> exportModel(@PathVariable("workspaceId") Long workspaceId) {
        return Result.success(workspaceService.exportCurrentModel(workspaceId));
    }

    /**
     * 更新模型草稿代码 (用于在线测试与哈希篡改模拟)
     */
    @PutMapping("/{workspaceId}/model-content")
    public Result<Void> updateModelContent(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestBody ModelContentUpdateRequest request) {
        workspaceService.updateModelContent(workspaceId, request.getRawSysmlContent());
        return Result.success();
    }
}
