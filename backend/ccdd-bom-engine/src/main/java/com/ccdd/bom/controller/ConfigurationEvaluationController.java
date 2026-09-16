package com.ccdd.bom.controller;

import com.ccdd.bom.dto.ConfigurationEvaluationDto;
import com.ccdd.bom.entity.ConfigurationResultEntity;
import com.ccdd.bom.service.ConfigurationSolverService;
import com.ccdd.bom.service.RuleConflictChecker;
import com.ccdd.common.api.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 150% BOM 配置求解与规则验证 RESTful 控制器 (遵循 D05 / D09 规范)
 */
@RestController
@RequestMapping("/api/v1")
public class ConfigurationEvaluationController {

    private final ConfigurationSolverService solverService;
    private final RuleConflictChecker conflictChecker;

    public ConfigurationEvaluationController(ConfigurationSolverService solverService,
                                            RuleConflictChecker conflictChecker) {
        this.solverService = solverService;
        this.conflictChecker = conflictChecker;
    }

    /**
     * 冻结特征输入，执行确定性 150% BOM 求解并固化 100% 实例结构 (M14 核心接口)
     */
    @PostMapping("/configuration-evaluations")
    public Result<ConfigurationEvaluationDto.EvaluateConfigurationResponse> evaluateConfiguration(
            @RequestBody ConfigurationEvaluationDto.EvaluateConfigurationRequest request) {
        
        ConfigurationEvaluationDto.EvaluateConfigurationResponse response = solverService.evaluateConfiguration(request);
        return Result.success(response);
    }

    /**
     * 读取历史已固化的不可变配置快照 (AT-05-04)
     */
    @GetMapping("/configuration-results/{resultId}")
    public Result<ConfigurationResultEntity> getConfigurationResult(@PathVariable("resultId") Long resultId) {
        ConfigurationResultEntity entity = solverService.getConfigurationResultSnapshot(resultId);
        return Result.success(entity);
    }

    /**
     * 规则集冲突静态校验与求解空间试算 (M14-F03)
     */
    @PostMapping("/rule-sets/validate")
    public Result<ConfigurationEvaluationDto.RuleValidationResponse> validateRuleSet(
            @RequestBody ConfigurationEvaluationDto.RuleValidationRequest request) {
        
        RuleConflictChecker.ConflictReport report = conflictChecker.validateInputFeatures(request.getTestFeatures());
        ConfigurationEvaluationDto.RuleValidationResponse response = new ConfigurationEvaluationDto.RuleValidationResponse(
                !report.hasConflict(),
                report.getErrorMessages().size(),
                report.getErrorMessages()
        );
        return Result.success(response);
    }
}
