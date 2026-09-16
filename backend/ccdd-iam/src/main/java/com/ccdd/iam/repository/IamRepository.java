package com.ccdd.iam.repository;

import com.ccdd.iam.entity.DisciplineType;
import com.ccdd.iam.entity.ProjectRoleType;
import com.ccdd.iam.entity.QualificationType;
import com.ccdd.iam.entity.SysDepartmentEntity;
import com.ccdd.iam.entity.SysProjectMembershipEntity;
import com.ccdd.iam.entity.SysQualificationEntity;
import com.ccdd.iam.entity.SysRoleEntity;
import com.ccdd.iam.entity.SysSessionRevocationEntity;
import com.ccdd.iam.entity.SysUserEntity;
import com.ccdd.iam.entity.SysUserRoleEntity;
import com.ccdd.iam.entity.UserAccountStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * M30-IAM 数据访问与内存状态仓储
 * 线程安全，内建企业级组织/用户/项目成员/工程资质代表性种子数据
 */
@Repository
public class IamRepository {

    private final Map<Long, SysDepartmentEntity> departments = new ConcurrentHashMap<>();
    private final Map<String, SysUserEntity> users = new ConcurrentHashMap<>();
    private final Map<String, SysRoleEntity> roles = new ConcurrentHashMap<>();
    private final List<SysUserRoleEntity> userRoles = Collections.synchronizedList(new ArrayList<>());
    private final Map<Long, SysProjectMembershipEntity> memberships = new ConcurrentHashMap<>();
    private final Map<Long, SysQualificationEntity> qualifications = new ConcurrentHashMap<>();
    private final Map<Long, SysSessionRevocationEntity> sessionRevocations = new ConcurrentHashMap<>();

    private final AtomicLong membershipIdGen = new AtomicLong(880192841029200L);
    private final AtomicLong qualificationIdGen = new AtomicLong(991029481930L);
    private final AtomicLong revocationIdGen = new AtomicLong(770192841010L);

    public IamRepository() {
        initSeedData();
    }

    /**
     * 初始化企业种子数据 (严格对齐 V1.8.0 迁移脚本)
     */
    private void initSeedData() {
        // 1. 部门与专业学科
        departments.put(100L, new SysDepartmentEntity(100L, "DEPT-ADMIN", "企业信息技术部 (IT & 运维)", null, DisciplineType.MANAGEMENT, Instant.now()));
        departments.put(200L, new SysDepartmentEntity(200L, "DEPT-MECH", "高端机床机械结构总体室", null, DisciplineType.MECHANICAL, Instant.now()));
        departments.put(300L, new SysDepartmentEntity(300L, "DEPT-ELEC", "数控电气与驱动工程室", null, DisciplineType.ELECTRICAL, Instant.now()));
        departments.put(400L, new SysDepartmentEntity(400L, "DEPT-CTRL", "数控系统与伺服控制研发室", null, DisciplineType.CONTROL, Instant.now()));
        departments.put(500L, new SysDepartmentEntity(500L, "DEPT-SIM", "数字化工程仿真与多体动力学室", null, DisciplineType.SIMULATION, Instant.now()));
        departments.put(600L, new SysDepartmentEntity(600L, "DEPT-HYDR", "液压润滑与排屑系统设计室", null, DisciplineType.HYDRAULIC, Instant.now()));
        departments.put(700L, new SysDepartmentEntity(700L, "DEPT-PROC", "制造工艺与工装工程部", null, DisciplineType.PROCESS, Instant.now()));
        departments.put(800L, new SysDepartmentEntity(800L, "DEPT-QUAL", "整机质量检验与适航认证部", null, DisciplineType.QUALITY, Instant.now()));

        // 2. 全局职能角色 (九大业务角色 + 系统管理员)
        roles.put("SystemAdmin", new SysRoleEntity("SystemAdmin", "系统管理员", "SYSTEM_ADMIN", "负责企业组织、用户授权、集成运维与安全策略管理，严禁代行工程技术文件签署放行 (SoD-04)", true));
        roles.put("ProductManager", new SysRoleEntity("ProductManager", "产品经理与需求工程师", "FUNCTIONAL", "捕获市场客户需求，维护技术规格条目，圈定机型指标范围与验收条件", true));
        roles.put("LeadSystemArchitect", new SysRoleEntity("LeadSystemArchitect", "系统工程师与总体架构师", "FUNCTIONAL", "分解需求，建立系统架构、逻辑物理分配及跨专业接口契约", true));
        roles.put("SimulationEngineer", new SysRoleEntity("SimulationEngineer", "仿真工程师", "FUNCTIONAL", "维护 Modelica 仿真模型，调度计算任务，校准输出结果", true));
        roles.put("ChiefMechanicalEngineer", new SysRoleEntity("ChiefMechanicalEngineer", "机械工程师", "FUNCTIONAL", "开展机械结构正向设计，签入 CAD 装配树，输出 100% EBOM 与工程图纸", true));
        roles.put("ElectricalEngineer", new SysRoleEntity("ElectricalEngineer", "电气控制工程师", "FUNCTIONAL", "开展电气原理图设计、PLC 固件与伺服驱动配置", true));
        roles.put("ConfigEngineer", new SysRoleEntity("ConfigEngineer", "配置工程师", "FUNCTIONAL", "维护机床模块槽位、候选变体集合与 150% BOM 选用规则库", true));
        roles.put("ProcessEngineer", new SysRoleEntity("ProcessEngineer", "工艺工程师", "FUNCTIONAL", "编制制造 MBOM 与 BOP 工艺路线，指派工作中心及新增制造辅料", true));
        roles.put("ProjectManager", new SysRoleEntity("ProjectManager", "项目经理与评审员", "FUNCTIONAL", "编排 WBS 研发计划，跟进交付齐套性，组织阶段门评审", true));
        roles.put("QualityOfficer", new SysRoleEntity("QualityOfficer", "质量与服务工程师", "FUNCTIONAL", "出厂实物检验、记录关键件序列号与服役维保履历", true));
        roles.put("ShopFloorOperator", new SysRoleEntity("ShopFloorOperator", "车间装配工", "FUNCTIONAL", "车间现场工位装配操作，记录实装偏离，无权反写设计定义 (SoD-03)", false));

        // 3. 用户
        SysUserEntity admin = new SysUserEntity("ENG-ADMIN-001", 100L, "admin", "系统管理员 (IT)", "admin@ccddesigner.com", "13800000001", UserAccountStatus.ACTIVE, false, "sso-admin-01", "$2a$10$hash", Instant.now(), Instant.now(), Instant.now());
        admin.setRoleIds(List.of("SystemAdmin"));
        users.put(admin.getUserId(), admin);

        SysUserEntity zhang = new SysUserEntity("ENG-2048", 200L, "zhang_jg", "张建国 (机械总工)", "zhang_jg@ccddesigner.com", "13800000002", UserAccountStatus.ACTIVE, false, "sso-zhang-02", "$2a$10$hash", Instant.now(), Instant.now(), Instant.now());
        zhang.setRoleIds(List.of("ChiefMechanicalEngineer"));
        users.put(zhang.getUserId(), zhang);

        SysUserEntity li = new SysUserEntity("ENG-3001", 400L, "li_sys", "李明 (系统架构师)", "li_ming@ccddesigner.com", "13800000003", UserAccountStatus.ACTIVE, false, "sso-li-03", "$2a$10$hash", Instant.now(), Instant.now(), Instant.now());
        li.setRoleIds(List.of("LeadSystemArchitect"));
        users.put(li.getUserId(), li);

        SysUserEntity wang = new SysUserEntity("ENG-4002", 500L, "wang_sim", "王强 (仿真工程师)", "wang_qiang@ccddesigner.com", "13800000004", UserAccountStatus.ACTIVE, false, "sso-wang-04", "$2a$10$hash", Instant.now(), Instant.now(), Instant.now());
        wang.setRoleIds(List.of("SimulationEngineer"));
        users.put(wang.getUserId(), wang);

        SysUserEntity zhao = new SysUserEntity("ENG-5003", 800L, "zhao_qual", "赵晓华 (专职审查员)", "zhao_xh@ccddesigner.com", "13800000005", UserAccountStatus.ACTIVE, false, "sso-zhao-05", "$2a$10$hash", Instant.now(), Instant.now(), Instant.now());
        zhao.setRoleIds(List.of("QualityOfficer"));
        users.put(zhao.getUserId(), zhao);

        SysUserEntity sun = new SysUserEntity("ENG-6004", 700L, "sun_proc", "孙工 (工艺主管)", "sun_proc@ccddesigner.com", "13800000006", UserAccountStatus.ACTIVE, false, "sso-sun-06", "$2a$10$hash", Instant.now(), Instant.now(), Instant.now());
        sun.setRoleIds(List.of("ProcessEngineer"));
        users.put(sun.getUserId(), sun);

        SysUserEntity qian = new SysUserEntity("ENG-7005", 700L, "qian_field", "钱师傅 (车间装配工)", "qian_field@ccddesigner.com", "13800000007", UserAccountStatus.ACTIVE, false, "sso-qian-07", "$2a$10$hash", Instant.now(), Instant.now(), Instant.now());
        qian.setRoleIds(List.of("ShopFloorOperator"));
        users.put(qian.getUserId(), qian);

        SysUserEntity ext = new SysUserEntity("ENG-EXT-01", 200L, "ext_supplier", "德国主轴外协专家", "spindle_ext@supplier.de", "13900000008", UserAccountStatus.ACTIVE, true, "sso-ext-08", "$2a$10$hash", Instant.now(), Instant.now(), Instant.now());
        ext.setRoleIds(List.of("ChiefMechanicalEngineer"));
        users.put(ext.getUserId(), ext);

        // 4. 用户角色映射
        userRoles.add(new SysUserRoleEntity("ENG-ADMIN-001", "SystemAdmin", Instant.now(), "SYS_INIT"));
        userRoles.add(new SysUserRoleEntity("ENG-2048", "ChiefMechanicalEngineer", Instant.now(), "ENG-ADMIN-001"));
        userRoles.add(new SysUserRoleEntity("ENG-3001", "LeadSystemArchitect", Instant.now(), "ENG-ADMIN-001"));
        userRoles.add(new SysUserRoleEntity("ENG-4002", "SimulationEngineer", Instant.now(), "ENG-ADMIN-001"));
        userRoles.add(new SysUserRoleEntity("ENG-5003", "QualityOfficer", Instant.now(), "ENG-ADMIN-001"));
        userRoles.add(new SysUserRoleEntity("ENG-6004", "ProcessEngineer", Instant.now(), "ENG-ADMIN-001"));
        userRoles.add(new SysUserRoleEntity("ENG-7005", "ShopFloorOperator", Instant.now(), "ENG-ADMIN-001"));
        userRoles.add(new SysUserRoleEntity("ENG-EXT-01", "ChiefMechanicalEngineer", Instant.now(), "ENG-ADMIN-001"));

        // 5. 项目工作组成员 (VMC1000 五轴机床项目 ProjectId=100293810293)
        memberships.put(880192841029181L, new SysProjectMembershipEntity(880192841029181L, 100293810293L, "ENG-3001", ProjectRoleType.PROJECT_LEAD, Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2027-12-31T23:59:59Z"), true, "ENG-ADMIN-001"));
        memberships.put(880192841029182L, new SysProjectMembershipEntity(880192841029182L, 100293810293L, "ENG-2048", ProjectRoleType.DESIGNER, Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2027-12-31T23:59:59Z"), true, "ENG-3001"));
        memberships.put(880192841029183L, new SysProjectMembershipEntity(880192841029183L, 100293810293L, "ENG-4002", ProjectRoleType.CHECKER, Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2027-12-31T23:59:59Z"), true, "ENG-3001"));
        memberships.put(880192841029184L, new SysProjectMembershipEntity(880192841029184L, 100293810293L, "ENG-5003", ProjectRoleType.APPROVER, Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2027-12-31T23:59:59Z"), true, "ENG-3001"));

        // 6. 专职工程资质登记 (赵晓华拥有 VERIFICATION_REVIEWER)
        qualifications.put(991029481920L, new SysQualificationEntity(991029481920L, "ENG-5003", QualificationType.VERIFICATION_REVIEWER, "CERT-2026-VMC-VERIF-099", LocalDate.parse("2026-01-01"), LocalDate.parse("2027-12-31"), "CHIEF-ENG-001", Instant.now()));
        qualifications.put(991029481921L, new SysQualificationEntity(991029481921L, "ENG-3001", QualificationType.LEAD_SYSTEM_ARCHITECT, "CERT-2025-ARCH-L5-002", LocalDate.parse("2025-06-01"), LocalDate.parse("2028-05-31"), "CHIEF-ENG-001", Instant.now()));

        // 7. 会话即时撤销黑名单样本 (AT-13)
        sessionRevocations.put(770192841001L, new SysSessionRevocationEntity(770192841001L, "ENG-EXT-01", 100293810293L, Instant.parse("2026-09-15T08:00:00Z"), "外协合同到期退出机床主轴项目组 (AT-13 立即失效)", Instant.parse("2026-09-15T08:00:00Z")));
    }

    // ==================== 部门查询 ====================
    public List<SysDepartmentEntity> findAllDepartments() {
        return new ArrayList<>(departments.values());
    }

    public Optional<SysDepartmentEntity> findDepartmentById(Long deptId) {
        return Optional.ofNullable(departments.get(deptId));
    }

    // ==================== 用户操作 ====================
    public List<SysUserEntity> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    public Optional<SysUserEntity> findUserById(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public Optional<SysUserEntity> findUserByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public void saveUser(SysUserEntity user) {
        users.put(user.getUserId(), user);
    }

    // ==================== 角色操作 ====================
    public List<SysRoleEntity> findAllRoles() {
        return new ArrayList<>(roles.values());
    }

    public Optional<SysRoleEntity> findRoleById(String roleId) {
        return Optional.ofNullable(roles.get(roleId));
    }

    public List<String> findRoleIdsByUserId(String userId) {
        SysUserEntity user = users.get(userId);
        if (user != null && user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
            return user.getRoleIds();
        }
        return userRoles.stream()
                .filter(ur -> ur.getUserId().equals(userId))
                .map(SysUserRoleEntity::getRoleId)
                .collect(Collectors.toList());
    }

    // ==================== 项目成员管理 ====================
    public List<SysProjectMembershipEntity> findMembershipsByProjectId(Long projectId) {
        return memberships.values().stream()
                .filter(m -> m.getProjectId().equals(projectId) && Boolean.TRUE.equals(m.getIsActive()))
                .collect(Collectors.toList());
    }

    public List<SysProjectMembershipEntity> findMembershipsByUserId(String userId) {
        return memberships.values().stream()
                .filter(m -> m.getUserId().equals(userId) && Boolean.TRUE.equals(m.getIsActive()))
                .collect(Collectors.toList());
    }

    public Optional<SysProjectMembershipEntity> findActiveMembership(Long projectId, String userId) {
        return memberships.values().stream()
                .filter(m -> m.getProjectId().equals(projectId) && m.getUserId().equals(userId) && Boolean.TRUE.equals(m.getIsActive()))
                .findFirst();
    }

    public SysProjectMembershipEntity saveMembership(SysProjectMembershipEntity membership) {
        if (membership.getMembershipId() == null) {
            membership.setMembershipId(membershipIdGen.incrementAndGet());
        }
        memberships.put(membership.getMembershipId(), membership);
        return membership;
    }

    // ==================== 专职资质操作 ====================
    public List<SysQualificationEntity> findQualificationsByUserId(String userId) {
        return qualifications.values().stream()
                .filter(q -> q.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public SysQualificationEntity saveQualification(SysQualificationEntity qualification) {
        if (qualification.getQualificationId() == null) {
            qualification.setQualificationId(qualificationIdGen.incrementAndGet());
        }
        qualifications.put(qualification.getQualificationId(), qualification);
        return qualification;
    }

    // ==================== AT-13 会话吊销黑名单 ====================
    public List<SysSessionRevocationEntity> findAllRevocations() {
        return new ArrayList<>(sessionRevocations.values());
    }

    public SysSessionRevocationEntity saveRevocation(SysSessionRevocationEntity revocation) {
        if (revocation.getRevocationId() == null) {
            revocation.setRevocationId(revocationIdGen.incrementAndGet());
        }
        sessionRevocations.put(revocation.getRevocationId(), revocation);
        return revocation;
    }

    /**
     * 检查当前用户与项目是否在撤销黑名单中 (AT-13 毫秒级阻断)
     */
    public boolean isRevoked(String userId, Long projectId) {
        return sessionRevocations.values().stream()
                .anyMatch(r -> r.getUserId().equals(userId)
                        && (r.getProjectId() == null || (projectId != null && r.getProjectId().equals(projectId))));
    }
}
