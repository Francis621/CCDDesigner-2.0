# CCDDesigner 2.0 模块开发详细规格说明书

## M30-IAM: 系统用户、组织与权限管理 (User Identity, Organization and Access Management)

| **文档属性**    | **内容**                                                     |
| --------------- | ------------------------------------------------------------ |
| **模块编号**    | `M30-IAM` (归属 M30 平台管理与集成运维域, Phase: P0/P1, Type: N＋I) |
| **文档编号**    | `CCD-DEV-SPEC-2.0-M30-IAM`                                   |
| **版本 / 状态** | V1.0 / 评审发布稿                                            |
| **主责模块**    | M30 平台管理与集成运维                                       |
| **协同模块**    | M01 (统一工作台)、M02 (项目与阶段门)、M11 (验证资质审查)、M20 (工程对象底座)、M24 (工作流与审批) |
| **上位依据**    | 《CCDDesigner 2.0 产品说明书》§2.1, §2.2, §32    《CCDDesigner 2.0 产品功能架构与模块设计说明书》§7, §38, §42    《CCDDesigner 2.0 产品开发说明书》§7.1, §7.2, §8.4 |
| **适用受众**    | 安全架构师、IAM 后端开发工程师、系统管理员、适航与质量合规审计员 |

### 1. 模块定位与核心设计原则

依据上位规范，本模块面向具备机械、电气、控制、液压等多专业协同需求，存在平台化产品、模块配置及订单定制（CTO/ETO）业务，并需要贯通研发、制造和服务数据的装备制造企业，构建统一的企业级身份认证、学科组织架构与细粒度访问控制底座：  

1. **企业统一身份与多专业学科矩阵原则**：

   - 采用单企业集中部署架构，全面取消多租户隔离层级。  
   - 建立“行政部门—专业学科（Discipline）”二维矩阵组织结构，统一纳管机械、电气、控制、仿真、液压、工艺、质量及售后服务团队，为跨专业任务分派与工程影响处置提供权威组织依据。  

2. **“全局职能角色”与“项目工作组角色”彻底解耦**：

   - 用户在企业中拥有基础的专业行政职能属性（如机械工程师、仿真工程师）；  
   - 在具体工程研发项目（Project）中，必须通过 `ProjectMembership` 显式授予项目级业务角色（如 Project Lead、Designer、Checker、Approver）。严禁授予无边界的全系统工程读写特权。  

3. **基于项目与生命周期的属性化访问控制（PBAC）**：

   - 统一下发多维授权判定模型：

     $$\text{AccessGranted} = f(\text{SubjectRole}, \text{ProjectMembership}, \text{ObjectSecurityLevel}, \text{ObjectState}, \text{EnvironmentContext}) \text{[cite: 2]}$$

   - 受控工程对象的研制状态机（`DRAFT`、`IN_REVIEW`、`RELEASED`、`OBSOLETE`）直接决定写权限边界。前端按钮隐藏仅用于交互降噪，后端切面与 API 网关执行物理级一票否决。  

4. **四项核心工程职责分离（Separation of Duties, SoD 物理硬约束）**：

   - **SoD-01（禁止自批）**：创建人（`creatorId`）严禁作为自身提交的发布申请、基线冻结的审批签署人。  
   - **SoD-02（验证资质分离）**：仅具备 `VerificationReviewer` 资质证书的工程师有权签署需求验证 `PASS` 结论，仿真计算人员无权自签。  
   - **SoD-03（现场反写阻断）**：车间制造与现场维保角色仅能填报实装、偏离与维修记录，无权反写处于 `RELEASED` 状态的设计 EBOM。  
   - **SoD-04（管理员越权阻断）**：系统管理员（`SystemAdmin`）仅负责运维与账号配置，严禁代行任何工程技术文件的签署与放行。  

5. **权限即时吊销与全链路失效机制（AT-13 强合规）**：

   - 当用户从项目工作组移除或权限被收回时，平台在 1 秒内通过分布式会话吊销黑名单同步失效该用户的 API Token、全局检索可见性及 MinIO 临时预签名下载链接，杜绝权限残留。  

### 2. 九大业务角色体系与职责权限矩阵

依据产品说明书 §2.2 定义的九大核心用户角色，结合多专业协同特点，建立角色、主要工作、平台交付结果及安全边界映射表：  

| **用户角色  MD**           | **主责业务域  MD** | **主要工程活动与工作职责  MD**                               | **核心平台交付结果  MD**                                     | **默认受控边界与 SoD 阻断约束  MD+ 1**                       |
| -------------------------- | ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **产品经理、需求工程师**   | M03, M12           | 捕获市场与客户需求，维护技术规格条目，圈定机型指标范围与验收条件。 | 受控需求条目、产品规格书、需求验证指标阈值。                 | 仅限需求与规格草稿编辑；禁止改写仿真原始结果与实测记录。     |
| **系统工程师**             | M04, M05, M06      | 分解需求，在 SysON/OpenSysML 建立系统架构、逻辑/物理分配及跨专业接口。 | 受控系统模型发布快照（Commit）、跨专业接口契约、分配矩阵。   | 仅限通过受控工作区发布模型；严禁直接写入外部 Flexo 已发布库。 |
| **仿真工程师**             | M07, M08, M09, M10 | 维护 Modelica 仿真模型，映射变量工况，调度计算任务，校准输出结果。 | 仿真复现包、Run 执行记录、原始曲线与分析证据。               | 计算结果仅能写入 `CALCULATED_RESULT` 角色；无权签署验证 PASS。 |
| **机械、电气、控制工程师** | M16, M17, M18      | 开展详细工程设计，签入 CAD 装配树，输出工程图样、PLC 固件与电气接线图。 | 一致的 100% EBOM 结构、图纸制品、受控固件发布包。            | 已发布设计对象只读；设计变更必须通过 ECO 授权并遵循 FFF 互换性原则。 |
| **配置工程师**             | M13, M14           | 维护机床模块槽位、候选变体集合、150% BOM 选用逻辑与配置规则集。 | 合法且可复现的订单配置解析结果（ConfigurationResult）。      | 仅限维护平台与规则库；无权直接修改下游已冻结的订单基线结构。 |
| **工艺工程师**             | M25, M26           | 编制制造 MBOM、工艺路线（BOP）、指派装配工作中心与新增制造辅料。 | 制造计划基线（As-Planned）、工艺规程、下发制造包。           | 新增辅料必须标记为制造新增；禁止反向伪造或篡改设计 EBOM 来源。 |
| **项目经理、评审人员**     | M02, M21, M24      | 编排 WBS 研发计划，跟进交付物齐套性，组织阶段门（Gate）成熟度评审。 | 项目计划基线、阶段门审查决策（GateDecision）、审批签署凭证。 | 仅限计划与成熟度裁定；严禁在核心证据不足时越权强制阶段门 PASS。 |
| **质量与服务人员**         | M27, M28           | 出厂实物检验、记录关键件实装序列号、登记现场维保换件与服役配置。 | 序列号实物台账（As-Built）、维保履历、As-Maintained 配置。   | 仅限记录现场事实；禁止直接回写或修改设计定义及出厂交付基线。 |
| **管理员、配置管理员**     | M20, M21, M30      | 管理企业组织、用户授权、字典配置、连接器巡检、归档与基线冻结治理。 | 可审计的受控工程环境、冻结基线闭包、集成调度监控。           | 负责系统运维；强制剥夺工程技术文件放行与专业设计签字权（SoD-04）。 |

### 3. 领域对象模型与 ER 物理字典 (PostgreSQL DDL)

以下物理 DDL 属于 `plm_infra` 独立 Schema，面向企业级部署进行了主键唯一性与鉴权缓存加速优化：  

SQL

```
-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M30-IAM 用户、组织与权限
-- 适用环境: PostgreSQL 15+ (企业单体/服务化部署，无多租户字段)
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_infra;

-- 用户状态枚举
CREATE TYPE plm_infra.user_account_status AS ENUM (
    'ACTIVE',      -- 正常激活
    'SUSPENDED',   -- 临时冻结
    'DEACTIVATED', -- 离职禁用
    'LOCKED'       -- 安全风控锁定
);

-- 专业工程资质类型枚举 (支撑 SoD-02)
CREATE TYPE plm_infra.qualification_type AS ENUM (
    'VERIFICATION_REVIEWER', -- 验证结论专职审查员 (拥有签署 PASS 资格)
    'LEAD_SYSTEM_ARCHITECT', -- 首席系统架构师
    'CHIEF_QUALITY_OFFICER', -- 质量总监 (具备作废 WITHDRAWN 特批权)
    'SAFETY_ENGINEER'        -- 安全关键审查员
);

-- 1. 企业部门与专业学科表 (Department)
CREATE TABLE plm_infra.sys_department (
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
CREATE TABLE plm_infra.sys_user (
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
CREATE INDEX idx_user_dept ON plm_infra.sys_user(dept_id);

-- 3. 全局职能角色表 (Role)
CREATE TABLE plm_infra.sys_role (
    role_id             VARCHAR(64) PRIMARY KEY,
    role_name           VARCHAR(128) NOT NULL,
    role_type           VARCHAR(32) NOT NULL DEFAULT 'FUNCTIONAL', -- FUNCTIONAL, SYSTEM_ADMIN
    description         TEXT,
    is_system_reserved  BOOLEAN NOT NULL DEFAULT FALSE
);

-- 4. 用户-全局角色关联表 (UserRole)
CREATE TABLE plm_infra.sys_user_role (
    user_id             VARCHAR(64) NOT NULL REFERENCES plm_infra.sys_user(user_id) ON DELETE CASCADE,
    role_id             VARCHAR(64) NOT NULL REFERENCES plm_infra.sys_role(role_id) ON DELETE CASCADE,
    assigned_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by         VARCHAR(64) NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 5. 项目工作组成员与项目级角色表 (ProjectMembership - 细粒度工程授权核心)
CREATE TABLE plm_infra.sys_project_membership (
    membership_id       BIGINT PRIMARY KEY,
    project_id          BIGINT NOT NULL, -- 关联 plm_project.project
    user_id             VARCHAR(64) NOT NULL REFERENCES plm_infra.sys_user(user_id) ON DELETE CASCADE,
    project_role        VARCHAR(64) NOT NULL, -- PROJECT_LEAD, DESIGNER, CHECKER, APPROVER, GUEST
    effective_from      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to        TIMESTAMPTZ NULL,     -- 项目授权有效期
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    granted_by          VARCHAR(64) NOT NULL,
    CONSTRAINT uq_project_user_role UNIQUE (project_id, user_id, project_role)
);
COMMENT ON TABLE plm_infra.sys_project_membership IS 'M30: 项目成员表，动态界定研发工程师对特定机型项目的操作范围';
CREATE INDEX idx_proj_membership_query ON plm_infra.sys_project_membership(user_id, project_id) WHERE is_active IS TRUE;

-- 6. 专职工程资质认证表 (UserQualification - 支撑 SoD-02 与高阶审批资质)
CREATE TABLE plm_infra.sys_qualification (
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
CREATE TABLE plm_infra.sys_session_revocation (
    revocation_id       BIGINT PRIMARY KEY,
    user_id             VARCHAR(64) NOT NULL REFERENCES plm_infra.sys_user(user_id) ON DELETE CASCADE,
    project_id          BIGINT NULL,          -- 若为特定项目移除则填充，全局封禁则为空
    revoked_before      TIMESTAMPTZ NOT NULL, -- 早于该时点的有效凭证全量熔断
    reason              VARCHAR(255) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_infra.sys_session_revocation IS 'M30: 会话与权限撤销表，支撑毫秒级鉴权失效熔断';
CREATE INDEX idx_revocation_lookup ON plm_infra.sys_session_revocation(user_id, project_id, revoked_before);
```

### 4. 核心功能特性详细规格 (IAM-F01 ~ IAM-F05)

#### IAM-F01：企业组织架构与多专业学科矩阵管理

1. **组织树与学科属性建模**：
   - 采用统一企业组织架构树，取消租户隔离配置。  
   - 部门实体必须显式声明其工程学科归属（`discipline_type`：机械、电气、控制、仿真、液压、工艺、质量、管理）；  
   - 当 M22 执行工程变更影响推演时，系统依赖该学科字典，将 `ImpactItem` 候选对象精准指派至对应专业主管工程师的工作台（M01-F01）。  
2. **外部协作者与供应商受控管理**：
   - 支持标记用户为外部人员（`is_external = TRUE`）；  
   - 外部人员进入系统必须严格绑定特定项目工作组，默认禁止在全局检索中枚举任何未经授权的物料主数据与机型目录。  

#### IAM-F02：账号生命周期与企业级 SSO/OIDC 认证集成

1. **统一联邦认证集成（SSO Integration）**：
   - 模块原生支持 OpenID Connect (OIDC) 与 OAuth 2.0 协议，无缝接入企业 Active Directory、Keycloak 或统一身份认证中台；  
   - 用户初次登录成功后，系统通过 `sso_sub` 自动映射企业档案；若匹配成功，自动关联对应的行政部门与默认职能角色。  
2. **账号全周期状态管理**：
   - 支持 `ACTIVE`（激活） $\rightarrow$ `SUSPENDED`（冻结） $\rightarrow$ `DEACTIVATED`（注销）流转；  
   - 当账号被置为 `DEACTIVATED` 时，系统自动解除其在所有在制项目中的未完成 Task 指派与 Gate 会签席位，并向项目经理触发工作交接报警卡片（M01-F04）。  

#### IAM-F03：基于项目工作组（Project Membership）的细粒度指派与继承

1. **动态项目团队组织**：
   - 任何研发项目（Project）由项目经理在 M02 中立项后，按 WBS 计划在工作组中圈定成员并指派项目角色（`PROJECT_LEAD`、`DESIGNER`、`CHECKER`、`APPROVER`、`GUEST`）；  
   - 支持配置项目准入时间窗口（`effective_from` 至 `effective_to`），超期后权限自动失效。  
2. **工程权限动态继承与隔离**：
   - 用户具备“机械工程师”全局角色，仅代表其具备机械图样设计技能；  
   - 只有当其被加入具体机床研发项目（如 VMC1000 研制项目）且分配为 `DESIGNER` 时，才具备在该项目下新建 `PartRevision` 草稿、修改 150% BOM 及签出 CAD 图样的写权限；  
   - 未加入该项目的工程师，系统默认仅允许检索并只读查阅已发布的通用标准件或已冻结的公共基线。  

#### IAM-F04：专职工程资质认证管理（SoD-02 闭环落地）

1. **工程资质与账号身份解耦**：

   - 具备 `SimulationEngineer` 角色仅代表拥有配置工况及调度 OpenModelica 容器计算的系统操作权；  
   - 签署需求验证通过结论（`PASS`）属于法定的工程判定行为，要求签署人账号必须在 `sys_qualification` 登记有效的 `VERIFICATION_REVIEWER` 证书。  

2. **资质合规拦截校验器**：

   - M11 验证管理模块在调用签署接口前，切面必须向 IAM 发起实时资质核验：

     $$\text{IsQualified} = \exists Q \in \text{Qualifications}(\text{userId}) \text{ s.t. } Q.\text{type} = \text{VERIFICATION\_REVIEWER} \land Q.\text{expiryDate} \ge \text{today()} \text{[cite: 1, 2]}$$

   - 资质证书过期或未年审时，系统自动熔断其签字权，防止未合规签署（AT-07）。  

#### IAM-F05：PBAC 动态判定与权限即时吊销（AT-13 拦截实现）

1. **PBAC 多维属性策略评估管道**：
   - 请求到达 Spring Boot 核心拦截层时，评估引擎综合解析环境向量：`Subject`（用户、部门、资质、角色）、`Resource`（对象类型、密级、当前生命周期状态、所属项目、创建人）、`Action`（读、写、审批、冻结）与 `Environment`（客户端 IP、时间戳）。  
2. **权限撤销实时断流（AT-13 强防线）**：
   - 当配置管理员在界面将某工程师从项目组成员中移除或调整其角色时，系统在同一事务内完成：
     1. 物理更新 `sys_project_membership.is_active = FALSE`；  
     2. 向 `sys_session_revocation` 插入撤销指令；  
     3. 向 Redis 发布全局广播事件 `USER_PERM_REVOKED`；  
     4. API 网关校验每个请求的 JWT 签发时间（`iat`），凡早于 `revoked_before` 的 Token，一律直接拒绝访问，返回 `HTTP 403 Forbidden`；  
     5. 异步调用 MinIO STS 服务，即时吊销该用户所有现存临时 Presigned URL 签名有效性，杜绝离线偷跑下载（AT-13）。  

### 5. 核心工程职责分离 (SoD) 切面拦截规则实现

在 Spring Boot 核心底座中，通过不可绕过的 AOP 切面落实四大职责分离硬约束：  

Java

```
package com.ccddesigner.infra.security.aspect;

import com.ccddesigner.common.exception.SecurityAccessDeniedException;
import com.ccddesigner.infra.security.context.SecurityUtils;
import com.ccddesigner.infra.security.model.SubjectPrincipal;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // 核心业务安全前置防线
public class EngineeringSoDGuardAspect {

    /**
     * SoD-01: 禁止创建人自审自批
     */
    public void checkSelfApproval(String creatorId, String currentUserId) {
        if (creatorId != null && creatorId.equalsIgnoreCase(currentUserId)) {
            throw new SecurityAccessDeniedException(
                "ERR_SOD_SELF_APPROVAL_FORBIDDEN",
                String.format("SoD Violation (SoD-01): Creator [%s] is strictly forbidden from approving their own release proposal.", currentUserId)
            );
        }
    }

    /**
     * SoD-02: 签署需求验证 PASS 必须具备专业评审员资质
     */
    public void checkVerificationQualification(SubjectPrincipal principal, String targetConclusion) {
        if ("PASS".equalsIgnoreCase(targetConclusion)) {
            boolean hasReviewerCert = principal.getQualifications().stream()
                .anyMatch(q -> "VERIFICATION_REVIEWER".equals(q.getType()) && q.isValid());
            if (!hasReviewerCert) {
                throw new SecurityAccessDeniedException(
                    "ERR_SOD_INSUFFICIENT_QUALIFICATION",
                    "SoD Violation (SoD-02): Signing a verification PASS decision mandates an active VERIFICATION_REVIEWER qualification certificate."
                );
            }
        }
    }

    /**
     * SoD-03: 现场制造与服务人员无权反写已发布设计定义 (EBOM/CAD)
     */
    public void checkFieldWriteAccess(SubjectPrincipal principal, String resourceType) {
        boolean isFieldStaff = principal.getGlobalRoles().stream()
            .anyMatch(r -> r.matches("ShopFloorOperator|FieldServiceEng"));
        if (isFieldStaff && ("PartRevision".equals(resourceType) || "BOMViewRevision".equals(resourceType))) {
            throw new SecurityAccessDeniedException(
                "ERR_SOD_FIELD_WRITE_PROHIBITED",
                "SoD Violation (SoD-03): Field and manufacturing service personnel are prohibited from writing directly to released engineering design definitions."
            );
        }
    }

    /**
     * SoD-04: 系统管理员默认禁止代行工程技术文件签署与放行
     */
    public void checkAdminEngineeringSign(SubjectPrincipal principal, String actionCode) {
        boolean isAdmin = principal.getGlobalRoles().contains("SystemAdmin");
        boolean isEngSignAction = actionCode.matches("APPROVE_RELEASE|FREEZE_BASELINE|CLOSE_ECO|SIGN_GATE");
        if (isAdmin && isEngSignAction) {
            throw new SecurityAccessDeniedException(
                "ERR_SOD_ADMIN_SIGN_FORBIDDEN",
                "SoD Violation (SoD-04): System Administrators are restricted from performing engineering sign-off, gate decisions, or baseline freezing."
            );
        }
    }
}
```

### 6. OpenAPI 3.0 接口契约定义

#### 6.1 分配用户至项目工作组并授予项目角色

- **HTTP 请求**：`POST /api/v1/projects/{projectId}/memberships`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "userId": "ENG-2048",
  "projectRole": "DESIGNER",
  "effectiveFrom": "2026-09-16T00:00:00Z",
  "effectiveTo": "2027-12-31T23:59:59Z"
}
```

- **响应报文 (Response 201 Created)**：

JSON

```
{
  "membershipId": 880192841029182,
  "projectId": 100293810293,
  "userId": "ENG-2048",
  "projectRole": "DESIGNER",
  "status": "ACTIVE",
  "grantedAt": "2026-09-16T09:30:00Z"
}
```

#### 6.2 撤销用户项目成员权限（触发 AT-13 熔断）

- **HTTP 请求**：`DELETE /api/v1/projects/{projectId}/memberships/{userId}`

    

- **请求头**：`X-Reason: 工程师岗位调整，退出机床主轴研发项目组`

    

- **响应报文 (Response 200 OK)**：

JSON

```
{
  "userId": "ENG-2048",
  "projectId": 100293810293,
  "revoked": true,
  "revokedAt": "2026-09-16T09:31:12.891Z",
  "sessionBlacklistPushed": true,
  "minioPresignedRevoked": true
}
```

#### 6.3 登记用户专职工程资质

- **HTTP 请求**：`POST /api/v1/users/{userId}/qualifications`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "qualificationType": "VERIFICATION_REVIEWER",
  "certificateNo": "CERT-2026-VMC-VERIF-099",
  "issuedDate": "2026-01-01",
  "expiryDate": "2027-12-31",
  "authorizedBy": "CHIEF-ENG-001"
}
```

- **响应报文 (Response 201 Created)**：

JSON

```
{
  "qualificationId": 991029481920,
  "userId": "ENG-2048",
  "qualificationType": "VERIFICATION_REVIEWER",
  "isValid": true
}
```

### 7. 验收测试矩阵 (Acceptance Testing Matrix)

| **用例编号**  | **上位验收与约束对照  MD+ 1** | **测试场景与操作步骤**                                       | **预期判定结果 (Pass Criteria)**                             | **验证日志与断言依据  MD+ 1**                                |
| ------------- | ----------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **TC-IAM-01** | **AT-13**                     | 1. 工程师正在浏览器端查阅核心装配图纸与模型；  2. 管理员在后台将其从该项目成员组移除（调用 `DELETE /memberships`）；  3. 工程师立即刷新页面、调用搜索 API 或使用 5 秒前生成的 MinIO 下载链接。 | 1. 检索与对象详情接口即时返回 `HTTP 403 Forbidden`；  2. MinIO 预签名直连下载返回 `AccessDenied`，彻底无法读取未授权内容（AT-13）。 | 断言网关及 AOP 日志打印 `ERR_PERMISSION_REVOKED`，会话撤销时延 $\le 1000\text{ ms}$。 |
| **TC-IAM-02** | **SoD-01**                    | 系统工程师 A 编制了 `ModelRelease` 候选包并提交会签，随后在工作流中以审批人身份尝试签署同意放行。 | 切面拦截审批调用，系统阻断自批动作，抛出 `403 Forbidden`。   | 响应错误码 `ERR_SOD_SELF_APPROVAL_FORBIDDEN`，事务回滚，模型保持待审。 |
| **TC-IAM-03** | **SoD-02**                    | 仿真工程师 B 运行 OpenModelica 完成轴系仿真（Run 获得 `SUCCEEDED`），其尝试调用 M11 接口将需求验证评估置为 `PASS`，但其账号未登记资质。 | 系统强校验阻断，拒绝生成 `PASS` 判定记录，提示缺少专职审查员资质。 | 抛出异常 `ERR_SOD_INSUFFICIENT_QUALIFICATION`，阻止非资质人员自签闭环（AT-07）。 |
| **TC-IAM-04** | **SoD-03**                    | 车间装配工人 C 登录系统，调用零部件更新接口尝试修改已发布的图纸或设计 BOM 行数量。 | 切面校验其角色属于现场制造，一票否决写权限，引导其开具现场偏离单。 | 接口返回 `HTTP 403` 及 `ERR_SOD_FIELD_WRITE_PROHIBITED`，设计基线只读不可篡改。 |
| **TC-IAM-05** | **SoD-04**                    | IT 系统管理员（持有 `SystemAdmin` 角色）尝试调用基线冻结接口（`POST /baselines/{id}/freeze`）代行放行工程基线。 | 系统判定管理员无工程放行审批权，硬性阻断操作。               | 抛出 `ERR_SOD_ADMIN_SIGN_FORBIDDEN`，管理员账号被限制在运维边界内。 |
| **TC-IAM-06** | 项目边界控制                  | 工程师甲属于立式加工中心项目组，尝试调用 API 接口修改卧式加工中心在研项目的草稿零部件。 | PBAC 校验其不具备目标项目的有效 `ProjectMembership`，直接返回 `403 Forbidden`。 | 确认未授权项目的写操作被严格隔离，杜绝跨研发项目串改。       |