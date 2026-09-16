package com.ccdd.manufacturing;

import com.ccdd.common.api.BusinessException;
import com.ccdd.manufacturing.dto.ProcessPlanDetailDto;
import com.ccdd.manufacturing.entity.ProcessOperationEntity;
import com.ccdd.manufacturing.repository.ManufacturingRepository;
import com.ccdd.manufacturing.service.ProcessPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BOP 工艺路线编排与工序时序校验单元测试
 * 对应规约: CCD-DEV-SPEC-2.0-D08
 */
public class ProcessPlanServiceTest {

    private ManufacturingRepository repository;
    private ProcessPlanService processPlanService;

    @BeforeEach
    public void setUp() {
        // 使用注入 null JdbcTemplate 的静态仓储实例（触发默认 Mock 种子数据降级逻辑）
        repository = new ManufacturingRepository(null);
        processPlanService = new ProcessPlanService(repository);
    }

    @Test
    @DisplayName("AT-BOP-01: 验证五轴机床主轴 BOP 工艺路线与工序物料装配挂载的完整性")
    public void testGetProcessPlanDetailSuccess() {
        String tenantId = "TENANT_TEST";
        Long mbomRevId = 201L;

        ProcessPlanDetailDto planDetail = processPlanService.getProcessPlanDetail(tenantId, mbomRevId);

        assertNotNull(planDetail);
        assertEquals("ROUT-VMC850-SPINDLE-01", planDetail.getRoutingCode());
        assertEquals("VMC-850五轴加工中心主轴单元精密装配与跑车工艺路线", planDetail.getRoutingName());
        assertEquals(4, planDetail.getOperations().size(), "主轴 BOP 应包含 OP10 到 OP40 共 4 道核心装配与质检工序");

        // 验证 OP10 刮研
        ProcessPlanDetailDto.OperationDetailDto op10 = planDetail.getOperations().get(0);
        assertEquals("OP10", op10.getOperationCode());
        assertEquals(10, op10.getSequenceNumber());
        assertEquals("WC-SPINDLE-CLEAN", op10.getWorkCenterCode());
        assertEquals(1, op10.getAllocatedParts().size(), "OP10 应分配 1 项清洁刮研物料 (8 颗固定螺钉)");

        // 验证 OP20 轴承装配
        ProcessPlanDetailDto.OperationDetailDto op20 = planDetail.getOperations().get(1);
        assertEquals("OP20", op20.getOperationCode());
        assertEquals(20, op20.getSequenceNumber());
        assertEquals(3, op20.getAllocatedParts().size(), "OP20 轴承精密装配应挂载 3 项物料 (螺钉、角接触轴承、锁固胶)");

        // 验证 OP40 热态跑车质检
        ProcessPlanDetailDto.OperationDetailDto op40 = planDetail.getOperations().get(3);
        assertEquals("OP40", op40.getOperationCode());
        assertEquals(new BigDecimal("120.00"), op40.getRunTimeMins());
        assertTrue(op40.getInspectionRequirement().contains("15000 rpm"));
    }

    @Test
    @DisplayName("AT-BOP-02: 验证工序时序链倒挂与工序号重复时的工业防呆阻断")
    public void testRoutingSequenceValidationFailure() {
        // 1. 测试时序倒挂 (OP20 序号小于前道工序)
        List<ProcessOperationEntity> invertedOps = List.of(
                ProcessOperationEntity.builder()
                        .operationId(1L)
                        .sequenceNumber(20)
                        .operationCode("OP20")
                        .build(),
                ProcessOperationEntity.builder()
                        .operationId(2L)
                        .sequenceNumber(10)
                        .operationCode("OP10")
                        .build()
        );
        assertThrows(BusinessException.class, () -> processPlanService.validateRoutingSequences(invertedOps),
                "工序倒挂必须抛出 BusinessException 阻断保存");

        // 2. 测试序号重复
        List<ProcessOperationEntity> duplicateOps = List.of(
                ProcessOperationEntity.builder()
                        .operationId(1L)
                        .sequenceNumber(10)
                        .operationCode("OP10")
                        .build(),
                ProcessOperationEntity.builder()
                        .operationId(2L)
                        .sequenceNumber(10)
                        .operationCode("OP10_DUP")
                        .build()
        );
        assertThrows(BusinessException.class, () -> processPlanService.validateRoutingSequences(duplicateOps),
                "工序号重复必须抛出 BusinessException 阻断保存");
    }
}
