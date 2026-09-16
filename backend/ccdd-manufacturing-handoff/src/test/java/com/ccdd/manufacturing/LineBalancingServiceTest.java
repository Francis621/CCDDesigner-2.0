package com.ccdd.manufacturing;

import com.ccdd.manufacturing.dto.LineBalancingReportDto;
import com.ccdd.manufacturing.repository.ManufacturingRepository;
import com.ccdd.manufacturing.service.LineBalancingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 装配线平衡率 (LBE) 与瓶颈优化分析服务单元测试
 * 纯原生 Java 测试 (无 Mockito agent 依赖，完全兼容 JDK 26)
 */
public class LineBalancingServiceTest {

    private LineBalancingService lineBalancingService;

    @BeforeEach
    public void setUp() {
        ManufacturingRepository repository = new ManufacturingRepository(null);
        lineBalancingService = new LineBalancingService(repository);
    }

    @Test
    @DisplayName("AT-BAL-01: 验证主轴 BOP 4工位线平衡率计算、节拍瓶颈识别与优化仿真")
    public void testLineBalancingAnalysisSuccess() {
        String tenantId = "TENANT_TEST";
        Long planId = 501L;

        LineBalancingReportDto report = lineBalancingService.analyzeLineBalancing(tenantId, planId);

        assertNotNull(report);
        assertEquals(4, report.getTotalStations(), "主轴 BOP 应包含 4 个装配质检工位");
        
        // 净工时总和: OP10(45) + OP20(80) + OP30(55) + OP40(150) = 330 分钟
        assertEquals(new BigDecimal("330.00"), report.getTotalWorkContentMins());
        
        // 节拍 CT: max(45, 80, 55, 150) = 150 分钟
        assertEquals(new BigDecimal("150.00"), report.getCycleTimeMins());
        
        // 瓶颈工序必须准确识别为 OP40
        assertEquals("OP40", report.getBottleneckOperationCode(), "瓶颈工位应当为热态跑车检测 OP40");

        // 生产线平衡效率 LBE = 330 / (4 * 150) = 55.00%
        assertEquals(new BigDecimal("55.00"), report.getLineBalancingEfficiency());
        assertEquals(new BigDecimal("45.00"), report.getBalanceDelayPercentage());

        // 验证平滑指数大于 0
        assertTrue(report.getSmoothnessIndex().compareTo(BigDecimal.ZERO) > 0);

        // 验证优化建议策略
        assertEquals(2, report.getOptimizationProposals().size(), "应输出双通道并行与工步拆分两项优化建议");
        
        LineBalancingReportDto.BalancingOptimizationProposalDto prop1 = report.getOptimizationProposals().get(0);
        assertEquals("PARALLEL_WORKSTATION", prop1.getStrategyType());
        assertTrue(prop1.getProjectedEfficiency().compareTo(new BigDecimal("80.00")) > 0, "双工位并行跑车后平衡率应跃升至 80% 以上");
    }
}
