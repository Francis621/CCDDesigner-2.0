package com.ccdd.project;

import com.ccdd.common.api.BusinessException;
import com.ccdd.project.dto.GatePreCheckResultDto;
import com.ccdd.project.dto.RecordGateDecisionRequest;
import com.ccdd.project.dto.TaskCpmAnalysisDto;
import com.ccdd.project.entity.*;
import com.ccdd.project.exception.CyclicTaskDependencyException;
import com.ccdd.project.repository.ProjectGateRepository;
import com.ccdd.project.service.ProjectGateService;
import com.ccdd.project.service.WbsTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("M02: 项目、任务与阶段门管理核心领域测试")
public class ProjectGateServiceTest {

    private ProjectGateRepository repository;
    private ProjectGateService projectGateService;
    private WbsTaskService wbsTaskService;

    @BeforeEach
    void setUp() {
        // 使用 null JdbcTemplate 触发高保真机床种子数据降级引擎
        repository = new ProjectGateRepository(null);
        projectGateService = new ProjectGateService(repository);
        wbsTaskService = new WbsTaskService(repository);
    }

    @Test
    @DisplayName("TC-M02-01: 项目群 (Program) 严禁设立与签署阶段门决策")
    void testProgramCannotHaveGateDecision() {
        // 创建一个特定 Mock 仓储，返回 projectType 为 PROGRAM
        ProjectGateRepository programRepo = new ProjectGateRepository(null) {
            @Override
            public Optional<ProjectEntity> findProjectById(String tenantId, Long projectId) {
                return Optional.of(ProjectEntity.builder()
                        .projectId(projectId)
                        .tenantId(tenantId)
                        .name("高档数控机床重大专项工程项目群")
                        .projectType("PROGRAM")
                        .build());
            }
        };

        ProjectGateService service = new ProjectGateService(programRepo);
        RecordGateDecisionRequest request = new RecordGateDecisionRequest();
        request.setDecisionType(GateDecisionType.PASS);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                service.recordGateDecision("TENANT_TEST", 888L, 301L, request));

        assertTrue(ex.getMessage().contains("项目群(Program)严禁设立阶段门评审"),
                "异常信息应明确指出项目群严禁设立阶段门评审");
    }

    @Test
    @DisplayName("TC-M02-02: 阶段门准入预检 AT-15 守护规则校验 (TR3 证据 INCONCLUSIVE 一票否决)")
    void testGatePreCheckAt15Guard() {
        GatePreCheckResultDto report = projectGateService.preCheckGate("TENANT_TEST", 1001L, 303L);

        assertNotNull(report);
        assertEquals(303L, report.getGateId());
        assertEquals("TR3", report.getGateCode());

        // 验证准入未通过
        assertFalse(report.getOverallPassed(), "TR3 阶段门因存在未确定的激光实测证据，准入核验应判定为不通过");
        assertTrue(report.getBlockerCount() > 0, "阻塞项数量应大于 0");
        assertFalse(report.getMissingEvidenceGaps().isEmpty(), "应识别出缺失或 INCONCLUSIVE 的验证证据缺口");

        GatePreCheckResultDto.EvidenceGapDto gap = report.getMissingEvidenceGaps().get(0);
        assertEquals("INCONCLUSIVE", gap.getCurrentStatus());
        assertEquals("TC-VERIFY-LASER-3AXIS", gap.getCaseCode());
    }

    @Test
    @DisplayName("TC-M02-03: 当前阶段存在未闭环 ActionItem 严禁签署 PASS 决策")
    void testUnclosedActionItemBlocksPassDecision() {
        RecordGateDecisionRequest request = new RecordGateDecisionRequest();
        request.setDecisionType(GateDecisionType.PASS);
        request.setDecisionNotes("尝试通过评审");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                projectGateService.recordGateDecision("TENANT_TEST", 1001L, 303L, request));

        assertTrue(ex.getMessage().contains("未闭环的整改行动项"), "应强拦截未闭环 ActionItem 的 PASS 签署");
    }

    @Test
    @DisplayName("TC-M02-04: CONDITIONAL_PASS 强制约束 allowedScope 与 actionItems 非空")
    void testConditionalPassRequiresScopeAndActionItem() {
        // 缺少 allowedScope 拦截
        RecordGateDecisionRequest emptyScopeReq = new RecordGateDecisionRequest();
        emptyScopeReq.setDecisionType(GateDecisionType.CONDITIONAL_PASS);

        BusinessException ex1 = assertThrows(BusinessException.class, () ->
                projectGateService.recordGateDecision("TENANT_TEST", 1001L, 303L, emptyScopeReq));
        assertTrue(ex1.getMessage().contains("明确限定允许放行范围"), "缺少 allowedScope 必须拦截");

        // 补充 allowedScope 但缺少 ActionItem 拦截
        emptyScopeReq.setAllowedScope("仅允许主轴箱体铸件毛坯投产采购");
        BusinessException ex2 = assertThrows(BusinessException.class, () ->
                projectGateService.recordGateDecision("TENANT_TEST", 1001L, 303L, emptyScopeReq));
        assertTrue(ex2.getMessage().contains("整改行动项"), "缺少 ActionItem 必须拦截");

        // 完整参数签署成功
        emptyScopeReq.setActionItems(List.of(new RecordGateDecisionRequest.CreateActionItemDto(
                "补充激光实测反向间隙数据",
                "请验证团队于5日内补全热态反向跳动曲线",
                "ENG-LIU-TEST",
                "CHIEF-ENG",
                LocalDate.now().plusDays(5)
        )));

        GateDecisionEntity decision = projectGateService.recordGateDecision("TENANT_TEST", 1001L, 303L, emptyScopeReq);
        assertNotNull(decision);
        assertEquals(GateDecisionType.CONDITIONAL_PASS, decision.getDecisionType());
    }

    @Test
    @DisplayName("TC-M02-05: WBS 任务网络 DAG 依赖防环探测 (成环抛出 CyclicTaskDependencyException)")
    void testTaskDependencyCycleDetection() {
        List<TaskEntity> tasks = List.of(
                TaskEntity.builder().taskId(1L).taskCode("T1").name("任务1").durationDays(5).build(),
                TaskEntity.builder().taskId(2L).taskCode("T2").name("任务2").durationDays(10).build(),
                TaskEntity.builder().taskId(3L).taskCode("T3").name("任务3").durationDays(15).build()
        );

        // 构造环形依赖: T1 -> T2 -> T3 -> T1
        List<TaskDependencyEntity> cycleDeps = List.of(
                TaskDependencyEntity.builder().predecessorTaskId(1L).successorTaskId(2L).depType(DependencyType.FS).build(),
                TaskDependencyEntity.builder().predecessorTaskId(2L).successorTaskId(3L).depType(DependencyType.FS).build(),
                TaskDependencyEntity.builder().predecessorTaskId(3L).successorTaskId(1L).depType(DependencyType.FS).build()
        );

        CyclicTaskDependencyException ex = assertThrows(CyclicTaskDependencyException.class, () ->
                wbsTaskService.computeCpm(1001L, tasks, cycleDeps));

        assertFalse(ex.getCyclePath().isEmpty());
        assertTrue(ex.getMessage().contains("死循环回路"), "异常信息应指出死循环回路");
    }

    @Test
    @DisplayName("TC-M02-06: CPM 关键路径推导与总时差计算 (TF=0 判定)")
    void testCpmCalculationAndCriticalPath() {
        // 构建简单网络:
        // T1 (5天) -> T2 (10天) -> T4 (5天)   路径工期 = 20天 (关键路径)
        // T1 (5天) -> T3 (3天)  -> T4 (5天)   路径工期 = 13天 (非关键路径，T3 有时差 7天)
        List<TaskEntity> tasks = List.of(
                TaskEntity.builder().taskId(1L).taskCode("T1").name("总体规格").durationDays(5).build(),
                TaskEntity.builder().taskId(2L).taskCode("T2").name("主轴详细设计").durationDays(10).build(),
                TaskEntity.builder().taskId(3L).taskCode("T3").name("钣金外罩绘制").durationDays(3).build(),
                TaskEntity.builder().taskId(4L).taskCode("T4").name("整机装配审查").durationDays(5).build()
        );

        List<TaskDependencyEntity> dependencies = List.of(
                TaskDependencyEntity.builder().predecessorTaskId(1L).successorTaskId(2L).depType(DependencyType.FS).build(),
                TaskDependencyEntity.builder().predecessorTaskId(2L).successorTaskId(4L).depType(DependencyType.FS).build(),
                TaskDependencyEntity.builder().predecessorTaskId(1L).successorTaskId(3L).depType(DependencyType.FS).build(),
                TaskDependencyEntity.builder().predecessorTaskId(3L).successorTaskId(4L).depType(DependencyType.FS).build()
        );

        TaskCpmAnalysisDto cpm = wbsTaskService.computeCpm(1001L, tasks, dependencies);

        assertNotNull(cpm);
        assertEquals(20, cpm.getCriticalPathLengthDays(), "总工期应为最长路径 5 + 10 + 5 = 20 天");
        assertTrue(cpm.getCriticalPathTaskCodes().contains("T1"));
        assertTrue(cpm.getCriticalPathTaskCodes().contains("T2"));
        assertTrue(cpm.getCriticalPathTaskCodes().contains("T4"));
        assertFalse(cpm.getCriticalPathTaskCodes().contains("T3"), "T3 具备时差，不应在关键路径上");

        // 验证 T3 的总时差 TF = 7 天
        TaskCpmAnalysisDto.TaskScheduleMetricDto t3Metric = cpm.getTaskMetrics().stream()
                .filter(m -> "T3".equals(m.getTaskCode()))
                .findFirst()
                .orElseThrow();
        assertEquals(7, t3Metric.getTotalFloatDays(), "T3 总时差 TF 应为 7 天");
        assertFalse(t3Metric.getIsCritical());
    }
}
