-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M30-IAM 用户、组织与权限 (V1.8.0)
-- 适用环境: PostgreSQL 15+ (企业单体/服务化部署，取消多租户隔离层级)
-- 包含: plm_infra Schema、用户状态/工程资质枚举、7张核心表、基础组织/用户/项目成员/资质种子数据
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_infra;

-- 用户账号状态枚举
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'user_account_status' AND n.nspname = 'plm_infra') THEN
        CREATE TYPE plm_infra.user_account_status AS ENUM (
            'ACTIVE',      -- 正常激活
            'SUSPENDED',   -- 临时冻结
            'DEACTIVATED', -- 离职禁用
            'LOCKED'       -- 安全风控锁定
        );
    END IF;
END $$;

-- 专业工程资质类型枚举 (支撑 SoD-02 与高阶审批资质)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'qualification_type' AND n.nspname = 'plm_infra') THEN
        CREATE TYPE plm_infra.qualification_type AS ENUM (
            'VERIFICATION_REVIEWER', -- 验证结论专职审查员 (拥有签署 PASS 资格)
            'LEAD_SYSTEM_ARCHITECT', -- 首席系统架构师
            'CHIEF_QUALITY_OFFICER', -- 质量总监 (具备作废 WITHDRAWN 特批权)
            'SAFETY_ENGINEER'        -- 安全关键审查员
        );
    END IF;
END $$;

-- 1. 企业部门与专业学科表 (Department)
CREATE TABLE IF NOT EXISTS plm_infra.sys_department (
    dept_id             BIGINT PRIMARY KEY,
    dept_code           VARCHAR(64) NOT NULL UNIQUE,
    dept_name           VARCHAR(128) NOT NULL,
    parent_dept_id      BIGINT NULL REFERENCES plm_infra.sys_department(dept_id),
    discipline_type     VARCHAR(32) NOT NULL CHECK (
        discipline_type IN ('MECHANICAL', 'ELECTRICAL', 'CONTROL', 'SIMULATION', 'HYDRAULIC', 'PROCESS', 'QUALITY', 'MANAGEMENT')
    ),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_infra.sys_department IS 'M30: 企业部门表，融合专业学科属性，支撑跨专业任务分派';

-- 2. 系统用户主表 (User)
CREATE TABLE IF NOT EXISTS plm_infra.sys_user (
    user_id             VARCHAR(64) PRIMARY KEY, -- 工号或企业全局唯一 ID
    dept_id             BIGINT NOT NULL REFERENCES plm_infra.sys_department(dept_id),
    username            VARCHAR(64) NOT NULL UNIQUE,
    real_name           VARCHAR(128) NOT NULL,
    email               VARCHAR(128) NOT NULL UNIQUE,
    mobile              VARCHAR(32),
    status              plm_infra.user_account_status NOT NULL DEFAULT 'ACTIVE',
    is_external         BOOLEAN NOT NULL DEFAULT FALSE, -- 是否为外部供应商/外协人员
    sso_sub             VARCHAR(128) NULL UNIQUE,       -- 企业 OIDC/OAuth2 联邦认证唯一标识
    password_hash       VARCHAR(255) NULL,             -- 本地兜底认证加盐哈希
    last_login_at       TIMESTAMPTZ NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_infra.sys_user IS 'M30: 系统用户主表，企业人员统一身份载体';
CREATE INDEX IF NOT EXISTS idx_user_dept ON plm_infra.sys_user(dept_id);

-- 3. 全局职能角色表 (Role)
CREATE TABLE IF NOT EXISTS plm_infra.sys_role (
    role_id             VARCHAR(64) PRIMARY KEY,
    role_name           VARCHAR(128) NOT NULL,
    role_type           VARCHAR(32) NOT NULL DEFAULT 'FUNCTIONAL', -- FUNCTIONAL, SYSTEM_ADMIN
    description         TEXT,
    is_system_reserved  BOOLEAN NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE plm_infra.sys_role IS 'M30: 全局职能角色表，定义九大工程岗位与系统管理员角色';

-- 4. 用户-全局角色关联表 (UserRole)
CREATE TABLE IF NOT EXISTS plm_infra.sys_user_role (
    user_id             VARCHAR(64) NOT NULL REFERENCES plm_infra.sys_user(user_id) ON DELETE CASCADE,
    role_id             VARCHAR(64) NOT NULL REFERENCES plm_infra.sys_role(role_id) ON DELETE CASCADE,
    assigned_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by         VARCHAR(64) NOT NULL,
    PRIMARY KEY (user_id, role_id)
);
COMMENT ON TABLE plm_infra.sys_user_role IS 'M30: 用户与全局职能角色关联关系表';

-- 5. 项目工作组成员与项目级角色表 (ProjectMembership - 细粒度工程授权核心)
CREATE TABLE IF NOT EXISTS plm_infra.sys_project_membership (
    membership_id       BIGINT PRIMARY KEY,
    project_id          BIGINT NOT NULL, -- 关联工程项目 ID
    user_id             VARCHAR(64) NOT NULL REFERENCES plm_infra.sys_user(user_id) ON DELETE CASCADE,
    project_role        VARCHAR(64) NOT NULL, -- PROJECT_LEAD, DESIGNER, CHECKER, APPROVER, GUEST
    effective_from      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to        TIMESTAMPTZ NULL,     -- 项目授权有效期
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    granted_by          VARCHAR(64) NOT NULL,
    CONSTRAINT uq_project_user_role UNIQUE (project_id, user_id, project_role)
);
COMMENT ON TABLE plm_infra.sys_project_membership IS 'M30: 项目成员表，动态界定研发工程师对特定机型项目的操作范围';
CREATE INDEX IF NOT EXISTS idx_proj_membership_query ON plm_infra.sys_project_membership(user_id, project_id) WHERE is_active IS TRUE;

-- 6. 专职工程资质认证表 (UserQualification - 支撑 SoD-02 与高阶审批资质)
CREATE TABLE IF NOT EXISTS plm_infra.sys_qualification (
    qualification_id    BIGINT PRIMARY KEY,
    user_id             VARCHAR(64) NOT NULL REFERENCES plm_infra.sys_user(user_id) ON DELETE CASCADE,
    qualification_type  plm_infra.qualification_type NOT NULL,
    certificate_no      VARCHAR(128) NOT NULL,
    issued_date         DATE NOT NULL,
    expiry_date         DATE NOT NULL,
    authorized_by       VARCHAR(64) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_qual UNIQUE (user_id, qualification_type),
    CONSTRAINT chk_qual_validity CHECK (expiry_date > issued_date)
);
COMMENT ON TABLE plm_infra.sys_qualification IS 'M30: 专业工程资质认证表，与账号身份解耦，控制关键放行权';

-- 7. 权限即时撤销黑名单表 (SessionRevocation - 支撑 AT-13 即时失效)
CREATE TABLE IF NOT EXISTS plm_infra.sys_session_revocation (
    revocation_id       BIGINT PRIMARY KEY,
    user_id             VARCHAR(64) NOT NULL REFERENCES plm_infra.sys_user(user_id) ON DELETE CASCADE,
    project_id          BIGINT NULL,          -- 若为特定项目移除则填充，全局封禁则为空
    revoked_before      TIMESTAMPTZ NOT NULL, -- 早于该时点的有效凭证全量熔断
    reason              VARCHAR(255) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_infra.sys_session_revocation IS 'M30: 会话与权限撤销表，支撑毫秒级鉴权失效熔断';
CREATE INDEX IF NOT EXISTS idx_revocation_lookup ON plm_infra.sys_session_revocation(user_id, project_id, revoked_before);

-- =============================================================================
-- 种子数据填充 (Seed Data)
-- =============================================================================

-- 1. 部门与专业学科
INSERT INTO plm_infra.sys_department (dept_id, dept_code, dept_name, parent_dept_id, discipline_type)
VALUES 
    (100, 'DEPT-ADMIN', '企业信息技术部 (IT & 运维)', NULL, 'MANAGEMENT'),
    (200, 'DEPT-MECH', '高端机床机械结构总体室', NULL, 'MECHANICAL'),
    (300, 'DEPT-ELEC', '数控电气与驱动工程室', NULL, 'ELECTRICAL'),
    (400, 'DEPT-CTRL', '数控系统与伺服控制研发室', NULL, 'CONTROL'),
    (500, 'DEPT-SIM', '数字化工程仿真与多体动力学室', NULL, 'SIMULATION'),
    (600, 'DEPT-HYDR', '液压润滑与排屑系统设计室', NULL, 'HYDRAULIC'),
    (700, 'DEPT-PROC', '制造工艺与工装工程部', NULL, 'PROCESS'),
    (800, 'DEPT-QUAL', '整机质量检验与适航认证部', NULL, 'QUALITY')
ON CONFLICT (dept_id) DO NOTHING;

-- 2. 全局职能角色 (对齐产品说明书 §2.2 九大核心角色体系 + 系统管理员)
INSERT INTO plm_infra.sys_role (role_id, role_name, role_type, description, is_system_reserved)
VALUES 
    ('SystemAdmin', '系统管理员', 'SYSTEM_ADMIN', '负责企业组织、用户授权、集成运维与安全策略管理，严禁代行工程技术文件签署放行 (SoD-04)', TRUE),
    ('ProductManager', '产品经理与需求工程师', 'FUNCTIONAL', '捕获市场客户需求，维护技术规格条目，圈定机型指标范围与验收条件', TRUE),
    ('LeadSystemArchitect', '系统工程师与总体架构师', 'FUNCTIONAL', '分解需求，建立系统架构、逻辑物理分配及跨专业接口契约', TRUE),
    ('SimulationEngineer', '仿真工程师', 'FUNCTIONAL', '维护 Modelica 仿真模型，调度计算任务，校准输出结果', TRUE),
    ('ChiefMechanicalEngineer', '机械工程师', 'FUNCTIONAL', '开展机械结构正向设计，签入 CAD 装配树，输出 100% EBOM 与工程图纸', TRUE),
    ('ElectricalEngineer', '电气控制工程师', 'FUNCTIONAL', '开展电气原理图设计、PLC 固件与伺服驱动配置', TRUE),
    ('ConfigEngineer', '配置工程师', 'FUNCTIONAL', '维护机床模块槽位、候选变体集合与 150% BOM 选用规则库', TRUE),
    ('ProcessEngineer', '工艺工程师', 'FUNCTIONAL', '编制制造 MBOM 与 BOP 工艺路线，指派工作中心及新增制造辅料', TRUE),
    ('ProjectManager', '项目经理与评审员', 'FUNCTIONAL', '编排 WBS 研发计划，跟进交付齐套性，组织阶段门评审', TRUE),
    ('QualityOfficer', '质量与服务工程师', 'FUNCTIONAL', '出厂实物检验、记录关键件序列号与服役维保履历', TRUE),
    ('ShopFloorOperator', '车间装配工', 'FUNCTIONAL', '车间现场工位装配操作，记录实装偏离，无权反写设计定义 (SoD-03)', FALSE)
ON CONFLICT (role_id) DO NOTHING;

-- 3. 系统用户 (预置核心专业代表人员)
INSERT INTO plm_infra.sys_user (user_id, dept_id, username, real_name, email, mobile, status, is_external, password_hash)
VALUES 
    ('ENG-ADMIN-001', 100, 'admin', '系统管理员 (IT)', 'admin@ccddesigner.com', '13800000001', 'ACTIVE', FALSE, '$2a$10$mockHashAdmin1234567890'),
    ('ENG-2048', 200, 'zhang_jg', '张建国 (机械总工)', 'zhang_jg@ccddesigner.com', '13800000002', 'ACTIVE', FALSE, '$2a$10$mockHashZhang1234567890'),
    ('ENG-3001', 400, 'li_sys', '李明 (系统架构师)', 'li_ming@ccddesigner.com', '13800000003', 'ACTIVE', FALSE, '$2a$10$mockHashLi1234567890'),
    ('ENG-4002', 500, 'wang_sim', '王强 (仿真工程师)', 'wang_qiang@ccddesigner.com', '13800000004', 'ACTIVE', FALSE, '$2a$10$mockHashWang1234567890'),
    ('ENG-5003', 800, 'zhao_qual', '赵晓华 (专职审查员)', 'zhao_xh@ccddesigner.com', '13800000005', 'ACTIVE', FALSE, '$2a$10$mockHashZhao1234567890'),
    ('ENG-6004', 700, 'sun_proc', '孙工 (工艺主管)', 'sun_proc@ccddesigner.com', '13800000006', 'ACTIVE', FALSE, '$2a$10$mockHashSun1234567890'),
    ('ENG-7005', 700, 'qian_field', '钱师傅 (车间装配工)', 'qian_field@ccddesigner.com', '13800000007', 'ACTIVE', FALSE, '$2a$10$mockHashQian1234567890'),
    ('ENG-EXT-01', 200, 'ext_supplier', '德国主轴外协专家', 'spindle_ext@supplier.de', '13900000008', 'ACTIVE', TRUE, '$2a$10$mockHashExt1234567890')
ON CONFLICT (user_id) DO NOTHING;

-- 4. 用户与全局角色映射
INSERT INTO plm_infra.sys_user_role (user_id, role_id, assigned_by)
VALUES 
    ('ENG-ADMIN-001', 'SystemAdmin', 'SYS_INIT'),
    ('ENG-2048', 'ChiefMechanicalEngineer', 'ENG-ADMIN-001'),
    ('ENG-3001', 'LeadSystemArchitect', 'ENG-ADMIN-001'),
    ('ENG-4002', 'SimulationEngineer', 'ENG-ADMIN-001'),
    ('ENG-5003', 'QualityOfficer', 'ENG-ADMIN-001'),
    ('ENG-6004', 'ProcessEngineer', 'ENG-ADMIN-001'),
    ('ENG-7005', 'ShopFloorOperator', 'ENG-ADMIN-001'),
    ('ENG-EXT-01', 'ChiefMechanicalEngineer', 'ENG-ADMIN-001')
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 5. 项目工作组成员与项目级角色 (以 VMC1000 五轴机床项目 ProjectId=100293810293 为例)
INSERT INTO plm_infra.sys_project_membership (membership_id, project_id, user_id, project_role, effective_from, effective_to, is_active, granted_by)
VALUES 
    (880192841029181, 100293810293, 'ENG-3001', 'PROJECT_LEAD', '2026-01-01T00:00:00Z', '2027-12-31T23:59:59Z', TRUE, 'ENG-ADMIN-001'),
    (880192841029182, 100293810293, 'ENG-2048', 'DESIGNER', '2026-01-01T00:00:00Z', '2027-12-31T23:59:59Z', TRUE, 'ENG-3001'),
    (880192841029183, 100293810293, 'ENG-4002', 'CHECKER', '2026-01-01T00:00:00Z', '2027-12-31T23:59:59Z', TRUE, 'ENG-3001'),
    (880192841029184, 100293810293, 'ENG-5003', 'APPROVER', '2026-01-01T00:00:00Z', '2027-12-31T23:59:59Z', TRUE, 'ENG-3001')
ON CONFLICT (membership_id) DO NOTHING;

-- 6. 专职工程资质登记 (赵晓华登记有有效的 VERIFICATION_REVIEWER 资质)
INSERT INTO plm_infra.sys_qualification (qualification_id, user_id, qualification_type, certificate_no, issued_date, expiry_date, authorized_by)
VALUES 
    (991029481920, 'ENG-5003', 'VERIFICATION_REVIEWER', 'CERT-2026-VMC-VERIF-099', '2026-01-01', '2027-12-31', 'CHIEF-ENG-001'),
    (991029481921, 'ENG-3001', 'LEAD_SYSTEM_ARCHITECT', 'CERT-2025-ARCH-L5-002', '2025-06-01', '2028-05-31', 'CHIEF-ENG-001')
ON CONFLICT (qualification_id) DO NOTHING;

-- 7. 会话即时撤销黑名单样本
INSERT INTO plm_infra.sys_session_revocation (revocation_id, user_id, project_id, revoked_before, reason, created_at)
VALUES 
    (770192841001, 'ENG-EXT-01', 100293810293, '2026-09-15T08:00:00Z', '外协合同到期退出机床主轴项目组 (AT-13 立即失效)', '2026-09-15T08:00:00Z')
ON CONFLICT (revocation_id) DO NOTHING;
