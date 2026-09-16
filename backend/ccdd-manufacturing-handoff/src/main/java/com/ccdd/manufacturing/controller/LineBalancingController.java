package com.ccdd.manufacturing.controller;

import com.ccdd.common.api.Result;
import com.ccdd.manufacturing.dto.LineBalancingReportDto;
import com.ccdd.manufacturing.service.LineBalancingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 装配线平衡率与瓶颈分析 RESTful 控制器
 */
@RestController
@RequestMapping("/api/v1/manufacturing/bop")
public class LineBalancingController {

    private final LineBalancingService lineBalancingService;

    public LineBalancingController(LineBalancingService lineBalancingService) {
        this.lineBalancingService = lineBalancingService;
    }

    /**
     * 获取指定工艺路线的工业工程线平衡度分析与优化报告
     *
     * @param tenantId 租户标识
     * @param planId   工艺路线 ID
     * @return 平衡度与优化建议报告
     */
    @GetMapping("/plans/{planId}/line-balancing")
    public Result<LineBalancingReportDto> getLineBalancingReport(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "TENANT_DEFAULT") String tenantId,
            @PathVariable("planId") Long planId) {
        LineBalancingReportDto report = lineBalancingService.analyzeLineBalancing(tenantId, planId);
        return Result.success(report);
    }
}
