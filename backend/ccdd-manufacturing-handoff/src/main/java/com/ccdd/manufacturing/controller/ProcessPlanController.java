package com.ccdd.manufacturing.controller;

import com.ccdd.common.api.Result;
import com.ccdd.manufacturing.dto.ProcessPlanDetailDto;
import com.ccdd.manufacturing.entity.ProcessOperationEntity;
import com.ccdd.manufacturing.service.ProcessPlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BOP 工艺路线与工序编排 REST 控制器
 * 遵循《CCD-DEV-SPEC-2.0-D08 专项规格包》BOP 工艺时序与零件工位挂载规约
 */
@RestController
@RequestMapping("/api/v1/manufacturing/bop")
public class ProcessPlanController {

    private final ProcessPlanService processPlanService;

    public ProcessPlanController(ProcessPlanService processPlanService) {
        this.processPlanService = processPlanService;
    }

    /**
     * 查询指定 MBOM 修订版的完整 BOP 工艺路线及工序挂载物料明细
     *
     * @param tenantId       租户标识
     * @param mbomRevisionId MBOM 修订版 ID
     * @return 工艺路线详情
     */
    @GetMapping("/plans/{mbomRevisionId}")
    public Result<ProcessPlanDetailDto> getProcessPlan(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "TENANT_DEFAULT") String tenantId,
            @PathVariable("mbomRevisionId") Long mbomRevisionId) {
        ProcessPlanDetailDto planDetail = processPlanService.getProcessPlanDetail(tenantId, mbomRevisionId);
        return Result.success(planDetail);
    }

    /**
     * 校验工序时序链的递增性与拓扑防环合法性
     *
     * @param operations 待校验工序列表
     * @return 校验通过响应
     */
    @PostMapping("/validate-routing")
    public Result<String> validateRouting(
            @RequestBody List<ProcessOperationEntity> operations) {
        processPlanService.validateRoutingSequences(operations);
        return Result.success("工序时序链校验通过，拓扑结构合法且严格升序");
    }
}
