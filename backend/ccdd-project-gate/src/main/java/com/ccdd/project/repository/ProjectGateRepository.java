package com.ccdd.project.repository;

import com.ccdd.project.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 项目、任务与阶段门数据持久化仓储
 * 支持原生 PostgreSQL SQL 操作与静态内存种子降级
 */
@Repository
public class ProjectGateRepository {

    private static final Logger log = LoggerFactory.getLogger(ProjectGateRepository.class);

    private final JdbcTemplate jdbcTemplate;

    private final Map<Long, ProjectEntity> memoryProjectMap = new LinkedHashMap<>();

    public ProjectGateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initMockProjects();
    }

    private void initMockProjects() {
        ProjectEntity p1 = ProjectEntity.builder()
                .projectId(1001L)
                .tenantId("TENANT_DEFAULT")
                .projectCode("PRJ-VMC850-5AXIS")
                .name("高刚度立式五轴加工中心正向研制项目 (平台级机型)")
                .projectType("PLATFORM")
                .managerId("PM-ZHANG-001")
                .chiefEngineerId("ENG-WANG-CHIEF")
                .currentStageId(203L)
                .status("ACTIVE")
                .build();
        ProjectEntity p2 = ProjectEntity.builder()
                .projectId(1002L)
                .tenantId("TENANT_DEFAULT")
                .projectCode("PRJ-HMC630-DUAL")
                .name("卧式双工作台柔性加工中心正向研制项目 (衍生机型)")
                .projectType("DERIVATIVE")
                .managerId("PM-LI-002")
                .chiefEngineerId("ENG-CHEN-CHIEF")
                .currentStageId(202L)
                .status("ACTIVE")
                .build();
        ProjectEntity p3 = ProjectEntity.builder()
                .projectId(1003L)
                .tenantId("TENANT_DEFAULT")
                .projectCode("PRJ-GMC2030-ULTRA")
                .name("超精密五轴龙门铣削加工中心重大专项工程")
                .projectType("PLATFORM")
                .managerId("PM-SUN-003")
                .chiefEngineerId("ENG-ZHAO-CHIEF")
                .currentStageId(201L)
                .status("ACTIVE")
                .build();
        memoryProjectMap.put(p1.getProjectId(), p1);
        memoryProjectMap.put(p2.getProjectId(), p2);
        memoryProjectMap.put(p3.getProjectId(), p3);
    }

    // ==========================================
    // 项目 CRUD 与阶段查询
    // ==========================================

    public List<ProjectEntity> findAllProjects(String tenantId) {
        String sql = "SELECT project_id, program_id, tenant_id, project_code, name, project_type, " +
                "manager_id, chief_engineer_id, current_stage_id, status, working_version, created_by, created_at, updated_at " +
                "FROM plm_project.project WHERE tenant_id = ? ORDER BY project_id ASC";
        try {
            List<ProjectEntity> list = jdbcTemplate.query(sql, (rs, rowNum) -> ProjectEntity.builder()
                    .projectId(rs.getLong("project_id"))
                    .programId(rs.getLong("program_id"))
                    .tenantId(rs.getString("tenant_id"))
                    .projectCode(rs.getString("project_code"))
                    .name(rs.getString("name"))
                    .projectType(rs.getString("project_type"))
                    .managerId(rs.getString("manager_id"))
                    .chiefEngineerId(rs.getString("chief_engineer_id"))
                    .currentStageId(rs.getLong("current_stage_id"))
                    .status(rs.getString("status"))
                    .workingVersion(rs.getLong("working_version"))
                    .createdBy(rs.getString("created_by"))
                    .build(), tenantId);
            if (!list.isEmpty()) return list;
        } catch (Exception e) {
            log.warn("查询项目列表异常，使用内存多机床项目种子: {}", e.getMessage());
        }
        return new ArrayList<>(memoryProjectMap.values());
    }

    public Optional<ProjectEntity> findProjectById(String tenantId, Long projectId) {
        if (memoryProjectMap.containsKey(projectId)) {
            return Optional.of(memoryProjectMap.get(projectId));
        }
        String sql = "SELECT project_id, program_id, tenant_id, project_code, name, project_type, " +
                "manager_id, chief_engineer_id, current_stage_id, status, working_version, created_by, created_at, updated_at " +
                "FROM plm_project.project WHERE tenant_id = ? AND project_id = ?";
        try {
            List<ProjectEntity> list = jdbcTemplate.query(sql, (rs, rowNum) -> ProjectEntity.builder()
                    .projectId(rs.getLong("project_id"))
                    .programId(rs.getLong("program_id"))
                    .tenantId(rs.getString("tenant_id"))
                    .projectCode(rs.getString("project_code"))
                    .name(rs.getString("name"))
                    .projectType(rs.getString("project_type"))
                    .managerId(rs.getString("manager_id"))
                    .chiefEngineerId(rs.getString("chief_engineer_id"))
                    .currentStageId(rs.getLong("current_stage_id"))
                    .status(rs.getString("status"))
                    .workingVersion(rs.getLong("working_version"))
                    .createdBy(rs.getString("created_by"))
                    .build(), tenantId, projectId);
            if (!list.isEmpty()) return Optional.of(list.get(0));
        } catch (Exception e) {
            log.warn("查询项目异常，使用默认五轴加工中心项目种子: {}", e.getMessage());
        }
        return Optional.of(createMockProject(tenantId, projectId));
    }

    public ProjectEntity saveProject(ProjectEntity entity) {
        memoryProjectMap.put(entity.getProjectId(), entity);
        return entity;
    }

    public ProjectEntity updateProject(ProjectEntity entity) {
        memoryProjectMap.put(entity.getProjectId(), entity);
        return entity;
    }

    public boolean deleteProject(Long projectId) {
        return memoryProjectMap.remove(projectId) != null;
    }

    public List<StageEntity> findStagesByProjectId(Long projectId) {
        String sql = "SELECT stage_id, project_id, stage_code, name, sequence_no, status, " +
                "planned_start_date, planned_end_date, actual_start_date, actual_end_date, created_at " +
                "FROM plm_project.stage WHERE project_id = ? ORDER BY sequence_no ASC";
        try {
            List<StageEntity> list = jdbcTemplate.query(sql, (rs, rowNum) -> StageEntity.builder()
                    .stageId(rs.getLong("stage_id"))
                    .projectId(rs.getLong("project_id"))
                    .stageCode(rs.getString("stage_code"))
                    .name(rs.getString("name"))
                    .sequenceNo(rs.getInt("sequence_no"))
                    .status(rs.getString("status"))
                    .plannedStartDate(rs.getDate("planned_start_date").toLocalDate())
                    .plannedEndDate(rs.getDate("planned_end_date").toLocalDate())
                    .build(), projectId);
            if (!list.isEmpty()) return list;
        } catch (Exception e) {
            log.warn("查询研发阶段异常，使用标准四阶段种子数据: {}", e.getMessage());
        }
        return createMockStages(projectId);
    }

    public List<GateEntity> findGatesByProjectId(Long projectId) {
        String sql = "SELECT g.gate_id, g.stage_id, g.gate_code, g.name, g.description, " +
                "g.review_workflow_def, g.status, g.created_at " +
                "FROM plm_project.gate g JOIN plm_project.stage s ON g.stage_id = s.stage_id " +
                "WHERE s.project_id = ? ORDER BY s.sequence_no ASC";
        try {
            List<GateEntity> list = jdbcTemplate.query(sql, (rs, rowNum) -> GateEntity.builder()
                    .gateId(rs.getLong("gate_id"))
                    .stageId(rs.getLong("stage_id"))
                    .gateCode(rs.getString("gate_code"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .reviewWorkflowDef(rs.getString("review_workflow_def"))
                    .status(rs.getString("status"))
                    .build(), projectId);
            if (!list.isEmpty()) return list;
        } catch (Exception e) {
            log.warn("查询阶段门异常，使用标准四级 TR 阶段门种子: {}", e.getMessage());
        }
        return createMockGates();
    }

    public List<GateEntryCriterionEntity> findCriteriaByGateId(Long gateId) {
        return createMockCriteria(gateId);
    }

    public List<ActionItemEntity> findActionItemsByGateId(Long gateId) {
        return createMockActionItems(gateId);
    }

    // ==========================================
    // 任务与 WBS 查询
    // ==========================================

    public List<WbsNodeEntity> findWbsNodesByProjectId(Long projectId) {
        return createMockWbsNodes(projectId);
    }

    public List<TaskEntity> findTasksByProjectId(Long projectId) {
        return createMockTasks(projectId);
    }

    public List<TaskDependencyEntity> findDependenciesByProjectId(Long projectId) {
        return createMockDependencies();
    }

    public List<DeliverableRequirementEntity> findDeliverableRequirementsByTaskId(Long taskId) {
        return createMockDeliverableRequirements(taskId);
    }

    public List<DeliverableSubmissionEntity> findSubmissionsByReqId(Long reqId) {
        return createMockSubmissions(reqId);
    }

    // ==========================================
    // 种子数据生成器 (支持离线测试与高保真运行)
    // ==========================================

    private ProjectEntity createMockProject(String tenantId, Long projectId) {
        return ProjectEntity.builder()
                .projectId(projectId != null ? projectId : 1001L)
                .tenantId(tenantId != null ? tenantId : "TENANT_DEFAULT")
                .projectCode("PRJ-VMC850-5AXIS")
                .name("高刚度立式五轴加工中心正向研制项目 (平台级机型)")
                .projectType("PLATFORM")
                .managerId("PM-ZHANG-001")
                .chiefEngineerId("ENG-WANG-CHIEF")
                .currentStageId(203L) // 处于 Stage 3 详细设计与BOM发布
                .status("ACTIVE")
                .build();
    }

    private List<StageEntity> createMockStages(Long projectId) {
        return List.of(
                StageEntity.builder()
                        .stageId(201L)
                        .projectId(projectId)
                        .stageCode("STAGE-1")
                        .name("概念与指标论证阶段")
                        .sequenceNo(1)
                        .status("CLOSED")
                        .plannedStartDate(LocalDate.of(2026, 1, 1))
                        .plannedEndDate(LocalDate.of(2026, 3, 31))
                        .build(),
                StageEntity.builder()
                        .stageId(202L)
                        .projectId(projectId)
                        .stageCode("STAGE-2")
                        .name("架构与系统多物理场仿真阶段")
                        .sequenceNo(2)
                        .status("CLOSED")
                        .plannedStartDate(LocalDate.of(2026, 4, 1))
                        .plannedEndDate(LocalDate.of(2026, 6, 30))
                        .build(),
                StageEntity.builder()
                        .stageId(203L)
                        .projectId(projectId)
                        .stageCode("STAGE-3")
                        .name("详细工程设计与工艺BOM编制阶段")
                        .sequenceNo(3)
                        .status("IN_PROGRESS")
                        .plannedStartDate(LocalDate.of(2026, 7, 1))
                        .plannedEndDate(LocalDate.of(2026, 9, 30))
                        .build(),
                StageEntity.builder()
                        .stageId(204L)
                        .projectId(projectId)
                        .stageCode("STAGE-4")
                        .name("整机装配试制与跑车验证阶段")
                        .sequenceNo(4)
                        .status("PENDING")
                        .plannedStartDate(LocalDate.of(2026, 10, 1))
                        .plannedEndDate(LocalDate.of(2026, 12, 31))
                        .build()
        );
    }

    private List<GateEntity> createMockGates() {
        return List.of(
                GateEntity.builder()
                        .gateId(301L)
                        .stageId(201L)
                        .gateCode("TR1")
                        .name("TR1 概念与顶层需求冻结门")
                        .description("审查机床主规格、转速功率指标与初步外廓方案")
                        .status("DECIDED")
                        .build(),
                GateEntity.builder()
                        .gateId(302L)
                        .stageId(202L)
                        .gateCode("TR2")
                        .name("TR2 系统架构与仿真就绪门")
                        .description("审查 SysML v2 功能分解、热力学刚度有限元及电主轴选型匹配")
                        .status("DECIDED")
                        .build(),
                GateEntity.builder()
                        .gateId(303L)
                        .stageId(203L)
                        .gateCode("TR3")
                        .name("TR3 关键设计评审门 (CDR)")
                        .description("审查主轴/床身详细图样、EBOM/MBOM 100% 消耗守恒及动态切削刚度实测证据")
                        .status("READY")
                        .build(),
                GateEntity.builder()
                        .gateId(304L)
                        .stageId(204L)
                        .gateCode("TR4")
                        .name("TR4 出厂验收与放行门 (FAT)")
                        .description("审查整机 15000rpm 跑车温升试验、球杆仪空间跳动及客户定制交付物")
                        .status("INIT")
                        .build()
        );
    }

    private List<GateEntryCriterionEntity> createMockCriteria(Long gateId) {
        return List.of(
                GateEntryCriterionEntity.builder()
                        .criterionId(401L)
                        .gateId(gateId)
                        .criterionCode("CRIT-MANDATORY-DELIVERABLES")
                        .name("必选技术交付物 100% 齐套审查")
                        .ruleType("DELIVERABLE_CHECK")
                        .thresholdValue(new BigDecimal("100.00"))
                        .isBlocking(true)
                        .build(),
                GateEntryCriterionEntity.builder()
                        .criterionId(402L)
                        .gateId(gateId)
                        .criterionCode("CRIT-EVIDENCE-COVERAGE")
                        .name("关键验证指标证据覆盖率核验 (AT-15 守护)")
                        .ruleType("EVIDENCE_COVERAGE")
                        .thresholdValue(new BigDecimal("100.00"))
                        .isBlocking(true)
                        .build(),
                GateEntryCriterionEntity.builder()
                        .criterionId(403L)
                        .gateId(gateId)
                        .criterionCode("CRIT-BASELINE-LOCKED")
                        .name("M21 阶段计划基线锁定与快照生成")
                        .ruleType("BASELINE_LOCKED")
                        .thresholdValue(new BigDecimal("100.00"))
                        .isBlocking(true)
                        .build()
        );
    }

    private List<ActionItemEntity> createMockActionItems(Long gateId) {
        return List.of(
                ActionItemEntity.builder()
                        .actionItemId(501L)
                        .decisionId(901L)
                        .title("激光干涉仪定位精度复测报告补充归档")
                        .description("取得激光干涉仪三向实测数据并经验证总师评估合格，方可闭环")
                        .ownerId("ENG-LIU-TEST")
                        .approverId("LEAD-WANG")
                        .dueDate(LocalDate.of(2026, 10, 15))
                        .status("OPEN")
                        .build()
        );
    }

    private List<WbsNodeEntity> createMockWbsNodes(Long projectId) {
        return List.of(
                WbsNodeEntity.builder().wbsNodeId(101L).projectId(projectId).parentNodeId(null).wbsCode("WBS.01").name("机床整机总体设计与指标分解").nodeLevel(1).build(),
                WbsNodeEntity.builder().wbsNodeId(102L).projectId(projectId).parentNodeId(null).wbsCode("WBS.02").name("直联主轴单元详细结构设计").nodeLevel(1).build(),
                WbsNodeEntity.builder().wbsNodeId(103L).projectId(projectId).parentNodeId(null).wbsCode("WBS.03").name("AC轴双摆台传动伺服系统").nodeLevel(1).build(),
                WbsNodeEntity.builder().wbsNodeId(104L).projectId(projectId).parentNodeId(null).wbsCode("WBS.04").name("数控电气及工艺BOM编排").nodeLevel(1).build()
        );
    }

    private List<TaskEntity> createMockTasks(Long projectId) {
        return List.of(
                TaskEntity.builder()
                        .taskId(1001L)
                        .wbsNodeId(101L)
                        .stageId(202L)
                        .taskCode("TASK-SYS-01")
                        .name("SysML v2 整机功能逻辑分解与接口定义")
                        .assigneeId("ARCH-CHENG")
                        .plannedStartDate(LocalDate.of(2026, 4, 1))
                        .plannedEndDate(LocalDate.of(2026, 5, 15))
                        .durationDays(45)
                        .progressPercent(100)
                        .status(TaskStatus.COMPLETED)
                        .build(),
                TaskEntity.builder()
                        .taskId(1002L)
                        .wbsNodeId(102L)
                        .stageId(203L)
                        .taskCode("TASK-SPN-01")
                        .name("主轴箱体结构3D建模与有限元热固耦合仿真")
                        .assigneeId("ENG-QIAN")
                        .plannedStartDate(LocalDate.of(2026, 5, 16))
                        .plannedEndDate(LocalDate.of(2026, 7, 31))
                        .durationDays(75)
                        .progressPercent(100)
                        .status(TaskStatus.COMPLETED)
                        .build(),
                TaskEntity.builder()
                        .taskId(1003L)
                        .wbsNodeId(102L)
                        .stageId(203L)
                        .taskCode("TASK-SPN-02")
                        .name("主轴单元设计EBOM编制与P4级轴承组装图样绘制")
                        .assigneeId("ENG-ZHOU")
                        .plannedStartDate(LocalDate.of(2026, 8, 1))
                        .plannedEndDate(LocalDate.of(2026, 9, 15))
                        .durationDays(45)
                        .progressPercent(100) // 技术进度 100%
                        .status(TaskStatus.COMPLETED)
                        .build(),
                TaskEntity.builder()
                        .taskId(1004L)
                        .wbsNodeId(104L)
                        .stageId(203L)
                        .taskCode("TASK-BOP-01")
                        .name("主轴装配BOP工艺路线规划与100%消耗守恒对账")
                        .assigneeId("ENG-PROCESS")
                        .plannedStartDate(LocalDate.of(2026, 8, 15))
                        .plannedEndDate(LocalDate.of(2026, 9, 30))
                        .durationDays(45)
                        .progressPercent(90)
                        .status(TaskStatus.IN_PROGRESS)
                        .build(),
                TaskEntity.builder()
                        .taskId(1005L)
                        .wbsNodeId(102L)
                        .stageId(204L)
                        .taskCode("TASK-TEST-01")
                        .name("主轴整机15000rpm热态跑车与空间跳动精度实测")
                        .assigneeId("ENG-LIU-TEST")
                        .plannedStartDate(LocalDate.of(2026, 10, 1))
                        .plannedEndDate(LocalDate.of(2026, 11, 15))
                        .durationDays(45)
                        .progressPercent(20)
                        .status(TaskStatus.IN_PROGRESS)
                        .build()
        );
    }

    private List<TaskDependencyEntity> createMockDependencies() {
        return List.of(
                TaskDependencyEntity.builder().predecessorTaskId(1001L).successorTaskId(1002L).depType(DependencyType.FS).build(),
                TaskDependencyEntity.builder().predecessorTaskId(1002L).successorTaskId(1003L).depType(DependencyType.FS).build(),
                TaskDependencyEntity.builder().predecessorTaskId(1003L).successorTaskId(1004L).depType(DependencyType.SS).lagDays(10).build(),
                TaskDependencyEntity.builder().predecessorTaskId(1003L).successorTaskId(1005L).depType(DependencyType.FS).build()
        );
    }

    private List<DeliverableRequirementEntity> createMockDeliverableRequirements(Long taskId) {
        return List.of(
                DeliverableRequirementEntity.builder()
                        .delivReqId(601L)
                        .taskId(taskId)
                        .requirementCode("DELIV-EBOM-VMC850")
                        .name("主轴单元设计EBOM发布版本 (含配对角接触球轴承与锁紧螺母)")
                        .deliverableType("EBOM_STRUCT")
                        .isMandatory(true)
                        .targetSecurityLevel("INTERNAL")
                        .build(),
                DeliverableRequirementEntity.builder()
                        .delivReqId(602L)
                        .taskId(taskId)
                        .requirementCode("DELIV-SIM-THERMAL")
                        .name("主轴瞬态温升与热膨胀有限元分析报告")
                        .deliverableType("SIM_REPORT")
                        .isMandatory(true)
                        .targetSecurityLevel("INTERNAL")
                        .build()
        );
    }

    private List<DeliverableSubmissionEntity> createMockSubmissions(Long reqId) {
        return List.of(
                DeliverableSubmissionEntity.builder()
                        .submissionId(701L)
                        .delivReqId(reqId)
                        .revisionId(8801L)
                        .artifactHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                        .submissionNotes("Rev A.0 初版提交 (已过时)")
                        .isLatest(false)
                        .submittedBy("ENG-ZHOU")
                        .build(),
                DeliverableSubmissionEntity.builder()
                        .submissionId(702L)
                        .delivReqId(reqId)
                        .revisionId(8802L)
                        .artifactHash("790ac1784c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08a1")
                        .submissionNotes("Rev A.1 正式发布版本 (当前生效)")
                        .isLatest(true)
                        .submittedBy("ENG-ZHOU")
                        .build()
        );
    }
}
