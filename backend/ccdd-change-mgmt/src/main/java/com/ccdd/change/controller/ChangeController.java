package com.ccdd.change.controller;

import com.ccdd.change.dto.CloseEcoResponse;
import com.ccdd.change.dto.ConfirmImplementationRequest;
import com.ccdd.change.dto.CreateChangeTaskRequest;
import com.ccdd.change.dto.CreateDispositionRequest;
import com.ccdd.change.dto.CreateEcoRequest;
import com.ccdd.change.dto.CreateEcrRequest;
import com.ccdd.change.dto.EcoDetailDto;
import com.ccdd.change.dto.EcrDetailDto;
import com.ccdd.change.dto.EvaluateImpactRequest;
import com.ccdd.change.dto.ImpactAnalysisResultDto;
import com.ccdd.change.dto.ImpactAssessmentReadinessDto;
import com.ccdd.change.dto.RecordImpactDecisionRequest;
import com.ccdd.change.entity.ChangeOrderEntity;
import com.ccdd.change.entity.ChangeRequestEntity;
import com.ccdd.change.entity.ChangeTaskEntity;
import com.ccdd.change.entity.EffectivityDispositionEntity;
import com.ccdd.change.entity.ImpactDecisionEntity;
import com.ccdd.change.entity.ImplementationRecordEntity;
import com.ccdd.change.service.ChangeService;
import com.ccdd.common.api.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * M22 工程变更与影响处置 RESTful 控制器 (OpenAPI §8)
 */
@RestController
@RequestMapping("/api/v1/changes")
public class ChangeController {

    private final ChangeService changeService;

    public ChangeController(ChangeService changeService) {
        this.changeService = changeService;
    }

    // ====== ECR 变更请求端点 ======

    @PostMapping("/ecrs")
    public Result<ChangeRequestEntity> createEcr(@RequestBody CreateEcrRequest request) {
        return Result.success(changeService.createEcr(request));
    }

    @GetMapping("/ecrs")
    public Result<List<ChangeRequestEntity>> listEcrs() {
        return Result.success(changeService.getAllEcrs());
    }

    @GetMapping("/ecrs/{id}")
    public Result<EcrDetailDto> getEcrDetail(@PathVariable("id") Long id) {
        return Result.success(changeService.getEcrDetail(id));
    }

    @PostMapping("/ecrs/{id}/submit")
    public Result<ChangeRequestEntity> submitEcr(@PathVariable("id") Long id) {
        return Result.success(changeService.submitEcr(id));
    }

    @PostMapping("/ecrs/{id}/review")
    public Result<ChangeRequestEntity> reviewEcr(@PathVariable("id") Long id, @RequestBody Map<String, Boolean> body) {
        boolean approve = body.getOrDefault("approve", true);
        return Result.success(changeService.reviewEcr(id, approve));
    }

    // ====== ECO 变更实施单端点 ======

    @PostMapping("/ecos")
    public Result<ChangeOrderEntity> createEco(@RequestBody CreateEcoRequest request) {
        return Result.success(changeService.createEco(request));
    }

    @GetMapping("/ecos")
    public Result<List<ChangeOrderEntity>> listEcos() {
        return Result.success(changeService.getAllEcos());
    }

    @GetMapping("/ecos/{id}")
    public Result<EcoDetailDto> getEcoDetail(@PathVariable("id") Long id) {
        return Result.success(changeService.getEcoDetail(id));
    }

    @PostMapping("/ecos/{id}/impact-analysis")
    public Result<ImpactAnalysisResultDto> evaluateImpact(@PathVariable("id") Long id, @RequestBody(required = false) EvaluateImpactRequest request) {
        if (request == null) {
            request = new EvaluateImpactRequest();
        }
        return Result.success(changeService.evaluateImpact(id, request));
    }

    @PostMapping("/ecos/{id}/impact-decisions")
    public Result<ImpactDecisionEntity> recordImpactDecision(@PathVariable("id") Long id, @RequestBody RecordImpactDecisionRequest request) {
        return Result.success(changeService.recordImpactDecision(id, request));
    }

    @GetMapping("/ecos/{id}/readiness")
    public Result<ImpactAssessmentReadinessDto> validateImpactReadiness(@PathVariable("id") Long id) {
        return Result.success(changeService.validateImpactReadiness(id));
    }

    @PostMapping("/ecos/{id}/authorize")
    public Result<ChangeOrderEntity> authorizeEco(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body) {
        Long ticketId = null;
        if (body != null && body.containsKey("approvalTicketId")) {
            ticketId = Long.valueOf(body.get("approvalTicketId").toString());
        }
        return Result.success(changeService.authorizeEco(id, ticketId));
    }

    @PostMapping("/ecos/{id}/tasks")
    public Result<ChangeTaskEntity> createChangeTask(@PathVariable("id") Long id, @RequestBody CreateChangeTaskRequest request) {
        return Result.success(changeService.createChangeTask(id, request));
    }

    @PostMapping("/ecos/{id}/tasks/{taskId}/complete")
    public Result<ChangeTaskEntity> completeTask(@PathVariable("id") Long id, @PathVariable("taskId") Long taskId) {
        return Result.success(changeService.completeTask(id, taskId));
    }

    @PostMapping("/ecos/{id}/release")
    public Result<ChangeOrderEntity> releaseEco(@PathVariable("id") Long id, @RequestParam(value = "simulationRunId", required = false) Long simulationRunId) {
        return Result.success(changeService.releaseEco(id, simulationRunId));
    }

    @PostMapping("/ecos/{id}/dispositions")
    public Result<EffectivityDispositionEntity> createDisposition(@PathVariable("id") Long id, @RequestBody CreateDispositionRequest request) {
        return Result.success(changeService.createDisposition(id, request));
    }

    @PostMapping("/ecos/{id}/confirm-receipt")
    public Result<ImplementationRecordEntity> confirmReceipt(@PathVariable("id") Long id, @RequestBody ConfirmImplementationRequest request) {
        return Result.success(changeService.confirmImplementationReceipt(id, request));
    }

    @PostMapping("/ecos/{id}/close")
    public Result<CloseEcoResponse> closeEco(@PathVariable("id") Long id) {
        return Result.success(changeService.closeEco(id));
    }
}
