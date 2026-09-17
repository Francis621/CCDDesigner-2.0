package com.ccdd.workflow.controller;

import com.ccdd.common.api.Result;
import com.ccdd.workflow.dto.CompleteTaskRequest;
import com.ccdd.workflow.dto.CompleteTaskResponse;
import com.ccdd.workflow.dto.ConsumeDecisionRequest;
import com.ccdd.workflow.dto.ConsumeDecisionResponse;
import com.ccdd.workflow.dto.StartWorkflowRequest;
import com.ccdd.workflow.dto.StartWorkflowResponse;
import com.ccdd.workflow.dto.WorkflowInstanceDetailDto;
import com.ccdd.workflow.dto.WorkflowTaskItemDto;
import com.ccdd.workflow.entity.DefinitionBindingEntity;
import com.ccdd.workflow.entity.WorkflowInstanceEntity;
import com.ccdd.workflow.exception.CrossMutationForbiddenException;
import com.ccdd.workflow.exception.HashTamperingDetectedException;
import com.ccdd.workflow.exception.SelfApprovalBlockedException;
import com.ccdd.workflow.service.WorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * M24: 工作流与工程审批 RESTful 控制器 (OpenAPI §8)
 * 严格遵从 OpenAPI 契约、SoD 职责分离与快照防篡改防御规范
 */
@RestController
@RequestMapping("/api/v1")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * OpenAPI 8.1: 发起业务审批流程
     */
    @PostMapping("/workflow-instances")
    public ResponseEntity<Result<StartWorkflowResponse>> startWorkflow(
            @RequestBody StartWorkflowRequest request,
            @RequestHeader(value = "X-Current-User-Id", required = false, defaultValue = "chief_designer") String currentUserId) {
        StartWorkflowResponse response = workflowService.startWorkflow(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(response));
    }

    /**
     * OpenAPI 8.2: 执行节点任务审批
     */
    @PostMapping("/workflow-tasks/{taskId}/complete")
    public Result<CompleteTaskResponse> completeTask(
            @PathVariable("taskId") String taskId,
            @RequestBody CompleteTaskRequest request,
            @RequestHeader(value = "X-Current-User-Id", required = false, defaultValue = "lead_analyst") String currentUserId) {
        CompleteTaskResponse response = workflowService.completeTask(taskId, request, currentUserId);
        return Result.success(response);
    }

    /**
     * OpenAPI 8.3: 业务状态机查询并核销审批决策凭据
     */
    @PostMapping("/workflow-decisions/{decisionTicketId}/consume")
    public Result<ConsumeDecisionResponse> consumeDecision(
            @PathVariable("decisionTicketId") Long decisionTicketId,
            @RequestBody ConsumeDecisionRequest request) {
        ConsumeDecisionResponse response = workflowService.consumeDecision(decisionTicketId, request);
        return Result.success(response);
    }

    /**
     * 获取当前用户的待办任务列表 (自动进行 SoD 自审限制标识)
     */
    @GetMapping("/workflow-tasks/pending")
    public Result<List<WorkflowTaskItemDto>> getPendingTasks(
            @RequestHeader(value = "X-Current-User-Id", required = false, defaultValue = "chief_designer") String currentUserId) {
        List<WorkflowTaskItemDto> list = workflowService.getPendingTasksForUser(currentUserId);
        return Result.success(list);
    }

    /**
     * 获取所有流程实例列表
     */
    @GetMapping("/workflow-instances")
    public Result<List<WorkflowInstanceEntity>> listWorkflowInstances() {
        return Result.success(workflowService.getAllInstances());
    }

    /**
     * 获取指定流程实例详情与拓扑图
     */
    @GetMapping("/workflow-instances/{id}")
    public Result<WorkflowInstanceDetailDto> getWorkflowInstanceDetail(@PathVariable("id") Long id) {
        return Result.success(workflowService.getWorkflowInstanceDetail(id));
    }

    /**
     * 发起人撤回流程
     */
    @PostMapping("/workflow-instances/{id}/revoke")
    public Result<Boolean> revokeWorkflow(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestHeader(value = "X-Current-User-Id", required = false, defaultValue = "chief_designer") String currentUserId) {
        boolean ok = workflowService.revokeWorkflow(id, currentUserId, reason);
        return Result.success(ok);
    }

    /**
     * 获取系统流程定义绑定配置
     */
    @GetMapping("/workflow-bindings")
    public Result<List<DefinitionBindingEntity>> listBindings() {
        return Result.success(workflowService.getAllBindings());
    }

    // ================== 异常精准映射 ==================

    @ExceptionHandler(SelfApprovalBlockedException.class)
    public ResponseEntity<Result<Void>> handleSelfApprovalBlocked(SelfApprovalBlockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(403, ex.getMessage()));
    }

    @ExceptionHandler(HashTamperingDetectedException.class)
    public ResponseEntity<Result<Void>> handleHashTampering(HashTamperingDetectedException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Result.error(422, ex.getMessage()));
    }

    @ExceptionHandler(CrossMutationForbiddenException.class)
    public ResponseEntity<Result<Void>> handleCrossMutationForbidden(CrossMutationForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(403, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, ex.getMessage()));
    }
}
