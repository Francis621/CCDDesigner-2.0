# CCDDesigner 2.0 专项开发规格包 (Detailed Specifications)

## D02: 生命周期状态机与 PBAC 切面拦截详细规格

| **文档属性** | **内容**                                                     |
| ------------ | ------------------------------------------------------------ |
| **规格编号** | `CCD-DEV-SPEC-2.0-001-D02`                                   |
| **文档版本** | V1.0                                                         |
| **生效日期** | 2026年9月15日                                                |
| **上位依据** | 《CCDDesigner 2.0 产品开发说明书》(`CCD-DEV-SPEC-2.0-001`)    《CCDDesigner 2.0 产品功能架构与模块设计说明书》(`CCD-ARCH-FUNC-2.0-001`) |
| **主责模块** | M20 (工程对象与生命周期)、M30 (平台管理与集成运维)、M24 (工作流与审批) |
| **协同模块** | M02、M04、M06、M07、M10、M11、M14、M15、M16、M21、M22、M26、M27、M28 |
| **适用范围** | 核心后端架构师、安全开发工程师、业务逻辑开发工程师、质量保障团队 |

### 1. 架构原则与拦截模型定义

依据上位开发说明书第 5 章与第 7 章安全架构要求，本规格包为 CCDDesigner 2.0 核心业务微服务定义统一的生命周期推进引擎与属性化访问控制（PBAC）切面拦截机制：  

1. **三层状态正交管理原则**：严禁将业务生命周期、异步协调作业与物理可用性混为单一字段存储。系统统一定义并维护三层正交状态：  

   - **业务生命周期（Business Lifecycle State）**：反映工程对象在研制流程中的法律与工程有效性（如 `DRAFT`、`RELEASED`）。  
   - **异步作业状态（Job Execution State）**：反映模型发布、仿真求解、下发传输等跨系统协调过程的技术状态（如 `STAGING`、`RUNNING`）。  
   - **内容可用性（Content Availability）**：反映物理模型、制品文件当前是否可读可达（`AVAILABLE`、`DEGRADED`、`UNAVAILABLE`）。  

2. **多维授权判定模型（PBAC）**：请求访问控制不再采用静态 RBAC，统一落地动态多维判定函数：  

   $$\text{AccessGranted} = f(\text{SubjectRole}, \text{ProjectMembership}, \text{ObjectSecurityLevel}, \text{ObjectState}, \text{EnvironmentContext})$$

   任何针对受控对象的主动操作，必须在 Spring AOP 前置切面中完成上下文解析与策略树求值。  

3. **职责分离（Separation of Duties, SoD）物理阻断**：切面层强制阻断四类越权：提交人自批、仿真解算自动替代验证判定、制造现场人员回写设计 EBOM、系统管理员代行工程签署。  

4. **两阶段防篡改守卫**：

   - 业务层：工作流返回的 `ApprovalDecision` 仅作为输入凭证，状态机迁移切面必须再次复核候选包内容哈希（`releaseHash`）一致性与当前状态前置条件。  
   - 数据层：结合 D01 规范的触发器，双重阻止终态（`RELEASED` / `FROZEN`）记录的原位篡改与物理删除。  

### 2. 双向追踪矩阵 (Bidirectional Traceability Matrix)

| **规格组件 / 拦截规则**               | **主责模块  MD+ 1** | **对应架构决策 / 规范章节  MD+ 1**                      | **覆盖验收用例  MD+ 1**    | **核心控制逻辑与阻断行为**                                   |
| ------------------------------------- | ------------------- | ------------------------------------------------------- | -------------------------- | ------------------------------------------------------------ |
| **TR-LFC-01** (通用修订状态机)        | M20                 | CCD-DEV-SPEC §5.3.1    CCD-ARCH-FUNC §40.1              | AT-03, AT-14, AT-21        | `RELEASED` 终态只读阻断；版本派生来源校验；作废生效范围校验  |
| **TR-LFC-02** (模型发布协调状态机)    | M06, M24            | CCD-DEV-SPEC §5.1, §5.3.2    CCD-ARCH-FUNC §39.1, §40.2 | AT-01, AT-02, AT-04, AT-16 | 两阶段发布协调；内容哈希变动审批失效阻断；部分写入故障恢复   |
| **TR-LFC-03** (仿真作业与运行状态机)  | M10                 | CCD-DEV-SPEC §4.3, §5.3.3    CCD-ARCH-FUNC §40.3        | AT-05, AT-06, AT-19, AT-20 | `Job` 与 `Run` 解耦；终态 `Run` 只读锁定；旧 Worker 迟到租约失效丢弃 |
| **TR-LFC-04** (验证判定与适用性状态)  | M11                 | CCD-DEV-SPEC §4.3, §5.3.3    ADR-08, CCD-ARCH-FUNC §19  | AT-07, AT-09, AT-17, AT-21 | 严禁求解 `SUCCEEDED` 自动转 `PASS`；需求升版后适用性置为 `PENDING_REVIEW` |
| **TR-LFC-05** (基线与配置状态机)      | M21                 | CCD-DEV-SPEC §3.1, §4.5    CCD-ARCH-FUNC §40.4          | AT-14, AT-15, AT-25        | `FROZEN` 基线防写切面；闭包依赖发布完整性检查；阶段门证据准入拦截 |
| **TR-PBAC-01** (多租户隔离拦截器)     | M30                 | CCD-DEV-SPEC §7.1                                       | AT-13                      | 上下文 `tenantId` 强校验，非法租户边界访问一票否决           |
| **TR-PBAC-02** (未受控候选隔离切面)   | M06, M30            | CCD-DEV-SPEC §7.1, ADR-03                               | AT-13, AT-29               | 拦截未批准模型暂存区、草稿快照对普通工程角色的查询与直连     |
| **TR-PBAC-03** (自审批物理阻断切面)   | M20, M24            | CCD-DEV-SPEC §7.2 Rule 1                                | AT-16                      | 强制 `creatorId != approverId`，防止发起人自批提权           |
| **TR-PBAC-04** (验证资质签署拦截切面) | M11, M30            | CCD-DEV-SPEC §7.2 Rule 2                                | AT-07                      | 签署 `PASS` 必须具备 `VerificationReviewer` 专职资质         |
| **TR-PBAC-05** (现场反写设计阻断切面) | M27, M28            | CCD-DEV-SPEC §7.2 Rule 3                                | AT-11, AT-12, AT-23        | 车间/维保角色调用 EBOM 写接口直接返回 HTTP 403 权限违规      |
| **TR-PBAC-06** (管理员越权签署阻断)   | M30                 | CCD-DEV-SPEC §7.2 Rule 4                                | AT-13                      | `SystemAdmin` 角色强制剔除工程发布与基线冻结操作权限         |

### 3. 核心领域状态机规格 (State Machine Specification)

#### 3.1 通用工程修订生命周期状态机 (`ObjectRevision`)

适用于 `RequirementRevision`、`PartRevision`、`DocumentRevision`、`PlatformRevision`、`VariantRevision` 等核心工程修订对象。  

代码段

```
stateDiagram-v2
    [*] --> DRAFT : 创建新修订/自RELEASED派生
    DRAFT --> IN_REVIEW : 提交评审 (Submit)
    IN_REVIEW --> DRAFT : 驳回/撤回 (Reject/Withdraw)
    IN_REVIEW --> RELEASED : 批准生效 (Approve & Release)
    RELEASED --> OBSOLETE : 停用/限制新使用 (Deprecate)
    RELEASED --> WITHDRAWN : 紧急作废 (Revoke)
    OBSOLETE --> WITHDRAWN : 紧急作废 (Revoke)
    RELEASED --> [*] : 派生新修订 (不改变原对象)
    OBSOLETE --> [*]
    WITHDRAWN --> [*]
```

##### 状态跃迁控制规格矩阵：

| **初始状态**                | **触发事件 / API 动作**               | **目标状态** | **前置守卫条件 (Guard Conditions)**                          | **业务副作用与拦截动作 (Side Effects)**                      | **允许执行角色  MD**             |
| --------------------------- | ------------------------------------- | ------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | -------------------------------- |
| **[\*]**                    | `createRevision()`                    | `DRAFT`      | 1. 目标 `masterId` 必须存在且未软删除；    2. 修订版本标签遵循主从规则（如 `A -> B`）。 | 初始化 `working_version = 1`；登记创建人与所属项目。         | `Designer`, `SysEngineer`        |
| **`DRAFT`**                 | `submitForReview()`                   | `IN_REVIEW`  | 1. 实体必填属性完整度校验 100%；    2. 内部引用对象无悬空引用；    3. `working_version` 乐观锁匹配。 | 锁定草稿编辑权；生成待审摘要；向 M24 发起流程实例绑定。      | `Owner`, `Author`                |
| **`IN_REVIEW`**             | `rejectReview()` / `withdrawReview()` | `DRAFT`      | 1. 工作流决策为 `REJECTED` 或发起人自主 `WITHDRAW`；    2. 流程实例未完成归档。 | 解锁编辑权；保留审批意见与驳回审计日志；原候选摘要置为失效。 | `Reviewer`, `Submitter`          |
| **`IN_REVIEW`**             | `approveAndRelease()`                 | `RELEASED`   | 1. M24 签署凭证签名合法且决策为 `APPROVED`；    2. 凭证中记录的快照哈希与当前实体哈希一致（AT-16）；    3. 满足 SoD-01（`creatorId != approverId`）；    4. 依赖引用的子项必须全部处于 `RELEASED` 状态。 | 1. 置 `is_latest = TRUE`，降级前序版本的最新标记；    2. 激活只读保护，触发器与 AOP 联合锁定；  3. 发送本地事务发件箱事件 `ObjectRevisionReleased`。 | `ApprovalService` (系统回调)     |
| **`RELEASED`**              | `deprecateUsage()`                    | `OBSOLETE`   | 1. 明确声明受影响机型与生效范围；    2. 无进行中的 ECR/ECO 处于实施锁定状态。 | 禁止新配置或新装配引用该版本；历史已冻结基线不受影响。       | `ConfigManager`, `LeadEngineer`  |
| **`RELEASED` / `OBSOLETE`** | `revokeEmergency()`                   | `WITHDRAWN`  | 1. 关联重大工程问题单或安全隐患单；    2. 具备首席质量官（CQO）特别授权凭证。 | 阻断在制工单领料；强行中断包含该对象的生效发布流程；触发安全预警。 | `QualityDirector`, `SafetyBoard` |

#### 3.2 模型发布两阶段协调作业状态机 (`ModelRelease Execution`)

管理 SysML v2 模型从工作区快照捕获到正式发布激活的技术作业流。  

代码段

```
stateDiagram-v2
    [*] --> CAPTURING : 请求发布 (submitRelease)
    CAPTURING --> VALIDATING : 快照捕获完成
    VALIDATING --> STAGING : 语法/依赖/准入校验通过
    VALIDATING --> FAILED : 发现未准入构造/语法错误 (AT-02)
    STAGING --> IN_REVIEW : Flexo Commit & MinIO 写入完成
    STAGING --> FAILED : 文件/语义写入中断 (AT-04)
    IN_REVIEW --> FINALIZING : M24 流程审批通过
    IN_REVIEW --> REJECTED : 审批驳回
    FINALIZING --> RELEASED : 最终复核通过，原子激活 (AT-01)
    FINALIZING --> FAILED : 可达性失效/内容校验失败
    FAILED --> [*] : 修复后重新发起
    REJECTED --> [*]
    RELEASED --> [*]
```

##### 状态跃迁异常熔断规格：

1. **原子激活前置校验**：进入 `FINALIZING` 后，切面必须向 Flexo SPARQL 发送 Ask 查询确认 Commit 可读，并校验 MinIO 的 SHA-256 摘要（AT-01）。若任一依赖项不可达，作业强制置为 `FAILED`，绝不进入 `RELEASED` 终态（AT-04）。  
2. **幂等重试约束**：当状态为 `FAILED` 时，支持依据相同的 `idempotency_key` 重试缺失步骤（如重新上传中断的 MinIO 制品），成功后推进至 `IN_REVIEW`，严禁产生重复的业务发布记录（AT-04, AT-30）。  

#### 3.3 仿真运行与验证闭环状态机 (`SimulationRun` & `VerificationAssessment`)

代码段

```
flowchart TD
    subgraph M10 仿真计算技术流水线
        J[Job: QUEUED] --> R_PREP[Run: PREPARING]
        R_PREP --> R_RUN[Run: RUNNING]
        R_RUN --> R_COL[Run: COLLECTING]
        R_COL -->|解算成功| R_SUCC[Run: SUCCEEDED]
        R_COL -->|解算失败/崩溃| R_FAIL[Run: FAILED]
        R_RUN -->|超时/取消| R_CANC[Run: CANCELLED]
    end

    subgraph M11 需求验证工程判定闭环
        V_INIT[Assessment: NOT_EXECUTED]
        R_SUCC -.->|提供分析证据 (evidenceFor)| EVID[EvidenceRecord: PENDING_REVIEW]
        EVID --> V_EVAL{资质人员审查与判据核验}
        V_EVAL -->|完全符合指标| V_PASS[Assessment: PASS]
        V_EVAL -->|超差/指标不合格| V_FAIL[Assessment: FAIL]
        V_EVAL -->|数据存疑/缺少实机测试| V_INCON[Assessment: INCONCLUSIVE]
    end

    style R_SUCC fill:#d4edda,stroke:#28a745
    style V_PASS fill:#cce5ff,stroke:#004085
    style V_INCON fill:#fff3cd,stroke:#856404
```

##### 强制规则控制（AT-05, AT-07, AT-19）：

1. **Job 与 Run 彻底解耦**：当 Worker 发生超时重试时，原 `Run` 状态永久标记为 `FAILED` 并固化现场日志；系统必须分配全新的 `run_id` 重新排队。  
2. **迟到回写阻断切面**：Worker 向 PLM 核心写回仿真结果时，切面检查其携带的 `worker_lease_token`。若对应 Run 已由于超时被置为 `FAILED` 或已有后继 Run 启动，切面拦截并丢弃该写入，返回 HTTP 409 Conflict（AT-19）。  
3. **技术成功与工程合格隔离**：拦截切面严格限制：任何系统自动化接口均无权将 `VerificationAssessment` 的状态从 `NOT_EXECUTED` 直接置为 `PASS`。即便 Run 状态为 `SUCCEEDED`，必须人工由具备 `VerificationReviewer` 资质的专职工程师签署（AT-07）。  

### 4. PBAC 属性化访问控制模型规范

#### 4.1 PBAC 判定上下文向量

策略判定引擎（Policy Decision Point, PDP）求值所需的环境与对象属性上下文定义：

$$\text{Context} = \left\langle  \begin{array}{ll} \text{Subject:} & \{\text{userId}, \text{tenantId}, \text{globalRoles}, \text{qualifications}\} \\ \text{Resource:} & \{\text{objType}, \text{masterId}, \text{revisionId}, \text{projectId}, \text{securityLevel}, \text{state}, \text{creatorId}\} \\ \text{Action:} & \{\text{actionCode}, \text{targetState}, \text{delegationToken}\} \\ \text{Environment:} & \{\text{clientIp}, \text{timestamp}, \text{channel}, \text{emergencyMode}\} \end{array} \right\rangle$$

#### 4.2 核心工程职责分离 (SoD) 策略矩阵

后端切面强制逐项执行以下防越权矩阵检查，任一规则违背直接抛出 `SecurityAccessDeniedException`：

| **规则编码** | **策略名称**       | **违规拦截判定表达式 (Violation Guard)**                     | **物理阻断行为与 HTTP 响应  MD+ 1**                          |
| ------------ | ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **SoD-01**   | **禁止创建者自审** | `Resource.creatorId == Subject.userId && Action.code IN ('APPROVE_RELEASE', 'FREEZE_BASELINE')` | 抛出 `403 Forbidden`，错误码 `ERR_SOD_SELF_APPROVAL_PROHIBITED`，阻断审批提权。 |
| **SoD-02**   | **仿真资质分离**   | `Action.code == 'SIGN_VERIFICATION_PASS' && !Subject.qualifications.contains('VerificationReviewer')` | 阻断仿真人员签署自身产出，错误码 `ERR_INSUFFICIENT_QUALIFICATION_LEVEL`。 |
| **SoD-03**   | **现场反写阻断**   | `Subject.currentRole IN ('ShopFloorOperator', 'FieldServiceEng') && Resource.objType == 'PartRevision' && Action.isWrite()` | 阻断现场人员篡改已发布设计 EBOM，引导其填报现场偏离单或维修事件。 |
| **SoD-04**   | **管理员越权阻断** | `Subject.globalRoles.contains('SystemAdmin') && Action.code IN ('APPROVE_RELEASE', 'CLOSE_ECO')` | 阻断 IT/系统管理员代行工程技术文件放行，错误码 `ERR_ADMIN_ENGINEERING_SIGN_FORBIDDEN`。 |
| **SoD-05**   | **候选模型隔离**   | `Resource.objType == 'ModelRelease' && Resource.state != 'RELEASED' && !Subject.projectMemberships[Resource.projectId].roles.contains('ModelOwner')` | 过滤未受控候选包，阻断普通查询，直接在读切面返回 404 或排除在列表外（AT-29）。 |
| **SoD-06**   | **基线只读锁定**   | `Resource.objType == 'Baseline' && Resource.state == 'FROZEN' && Action.isWrite()` | 一票否决对已冻结基线成员集合的原位增删改查写操作（AT-14, AT-25）。 |

### 5. Spring Boot AOP 切面拦截体系实现规格

平台基于 Spring Boot 3 与 AspectJ 构建不可绕过的安全拦截管道。所有对外发布的 Controller 与内部 Application Service 必须嵌入切面流水线。

#### 5.1 切面拦截执行流水线 (Execution Pipeline)

代码段

```
flowchart TD
    REQ([进入 REST / RPC 请求]) --> AOP_T[1. TenantContextAspect<br/>多租户与项目边界校验]
    AOP_T -->|通过| AOP_P[2. PBACAuthorizationAspect<br/>多维属性策略与 SoD 评估]
    AOP_P -->|通过| AOP_L[3. ConcurrencyLockAspect<br/>草稿 workingVersion 乐观并发锁检查]
    AOP_L -->|通过| AOP_S[4. LifecycleTransitionAspect<br/>状态机前置守卫与哈希防篡改复核]
    AOP_S -->|通过| BIZ_SVC[[执行业务逻辑 Core Service<br/>更新聚合根实体]]
    BIZ_SVC --> AOP_O[5. OutboxEventPublisherAspect<br/>原子写入本地事务发件箱]
    AOP_O --> AOP_A[6. SecurityAuditAspect<br/>记录不可篡改操作日志]
    AOP_A --> RESP([正常返回 200 / 202])

    AOP_T -- 租户不匹配 --> ERR_403([403 Forbidden])
    AOP_P -- 权限/SoD违背 --> ERR_403
    AOP_L -- 并发冲突 --> ERR_409([409 Conflict])
    AOP_S -- 守卫校验失败 --> ERR_422([422 Unprocessable Entity])
```

#### 5.2 核心注解定义规范

##### 1. `@EnforcePBAC`

用于标注受控业务方法，触发属性化鉴权。

Java

```
package com.ccddesigner.common.security.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnforcePBAC {
    String action();                     // 业务动作编码，如 "APPROVE_RELEASE"
    String resourceType();               // 资源类型，如 "ModelRelease"
    String idSpEL() default "#id";       // 获取目标资源 ID 的 SpEL 表达式
    boolean checkCandidateIsolation() default false; // 是否开启候选隔离过滤
}
```

##### 2. `@LifecycleTransition`

用于标注状态迁移方法，控制状态机流转守卫。

Java

```
package com.ccddesigner.common.lifecycle.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LifecycleTransition {
    String resourceType();
    String fromState();                  // 允许的源状态，支持逗号分隔或 "*"
    String toState();                    // 目标状态
    String releaseHashSpEL() default ""; // 审批发布时强制复核的内容摘要 SpEL
    boolean requireWorkflowTicket() default false; // 是否强制校验 M24 审批凭证
}
```

##### 3. `@ConcurrencyProtected`

用于草稿更新，强制执行基于 `working_version` 的乐观锁检测。

Java

```
package com.ccddesigner.common.concurrency.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConcurrencyProtected {
    String revisionIdSpEL() default "#cmd.revisionId";
    String clientVersionSpEL() default "#cmd.workingVersion";
}
```

#### 5.3 核心切面实现代码规范 (Java 17 / Spring Boot 3)

##### 5.3.1 PBAC 权限与 SoD 阻断切面 (`PBACAuthorizationAspect.java`)

Java

```
package com.ccddesigner.common.security.aspect;

import com.ccddesigner.common.exception.SecurityAccessDeniedException;
import com.ccddesigner.common.security.SecurityUtils;
import com.ccddesigner.common.security.annotation.EnforcePBAC;
import com.ccddesigner.common.security.pdp.PolicyDecisionPoint;
import com.ccddesigner.common.security.context.EvaluationContext;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20) // 位于 TenantAspect 之后
public class PBACAuthorizationAspect {

    @Resource
    private PolicyDecisionPoint pdp;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(enforcePBAC)")
    public Object authorize(ProceedingJoinPoint pjp, EnforcePBAC enforcePBAC) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = pjp.getArgs();

        StandardEvaluationContext spelContext = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            spelContext.setVariable(paramNames[i], args[i]);
        }

        Long resourceId = parser.parseExpression(enforcePBAC.idSpEL()).getValue(spelContext, Long.class);

        // 组装 EvaluationContext
        EvaluationContext context = EvaluationContext.builder()
                .subject(SecurityUtils.getCurrentSubject())
                .actionCode(enforcePBAC.action())
                .resourceType(enforcePBAC.resourceType())
                .resourceId(resourceId)
                .checkCandidateIsolation(enforcePBAC.checkCandidateIsolation())
                .timestamp(System.currentTimeMillis())
                .build();

        // 调用 PDP 执行多维策略求值 (包含 SoD-01 ~ SoD-06 检测)
        pdp.decideOrThrow(context);

        return pjp.proceed();
    }
}
```

##### 5.3.2 状态机守卫与内容哈希防篡改切面 (`LifecycleTransitionAspect.java`)

Java

```
package com.ccddesigner.common.lifecycle.aspect;

import com.ccddesigner.common.exception.InvalidStateTransitionException;
import com.ccddesigner.common.exception.SecurityTamperingException;
import com.ccddesigner.common.lifecycle.ObjectLifecycleRepository;
import com.ccddesigner.common.lifecycle.annotation.LifecycleTransition;
import com.ccddesigner.common.lifecycle.entity.LifecycleEntity;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
public class LifecycleTransitionAspect {

    @Resource
    private ObjectLifecycleRepository lifecycleRepository;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(transition)")
    public Object interceptTransition(ProceedingJoinPoint pjp, LifecycleTransition transition) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Object[] args = pjp.getArgs();
        String[] paramNames = signature.getParameterNames();

        StandardEvaluationContext spelCtx = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            spelCtx.setVariable(paramNames[i], args[i]);
        }

        // 提取对象 ID (约定首参数或按注解提取)
        Long entityId = (Long) args[0];
        LifecycleEntity entity = lifecycleRepository.findByIdOrThrow(entityId);

        // 1. 状态跃迁合法性守卫
        String currentState = entity.getLifecycleState();
        if (!transition.fromState().equals("*") && !transition.fromState().contains(currentState)) {
            throw new InvalidStateTransitionException(
                String.format("State Transition Rejected: Entity [%s] is in state [%s], required fromState [%s].",
                    entityId, currentState, transition.fromState())
            );
        }

        // 2. 审批内容哈希一致性复核 (针对发布阶段防篡改 - AT-16)
        if (!transition.releaseHashSpEL().isEmpty()) {
            String incomingHash = parser.parseExpression(transition.releaseHashSpEL()).getValue(spelCtx, String.class);
            String frozenTargetHash = entity.calculateContentHash();
            if (!frozenTargetHash.equalsIgnoreCase(incomingHash)) {
                throw new SecurityTamperingException(
                    String.format("Release Aborted (AT-16): Content hash mismatch! Approved ticket hash [%s] != Staged target hash [%s].",
                        incomingHash, frozenTargetHash)
                );
            }
        }

        // 3. 执行核心状态变更
        Object result = pjp.proceed();

        // 4. 迁移后后置处理 (由本地事务写入发件箱)
        lifecycleRepository.recordAuditLog(entityId, currentState, transition.toState());

        return result;
    }
}
```

##### 5.3.3 乐观并发锁切面 (`ConcurrencyLockAspect.java`)

Java

```
package com.ccddesigner.common.concurrency.aspect;

import com.ccddesigner.common.concurrency.annotation.ConcurrencyProtected;
import com.ccddesigner.common.exception.OptimisticLockConflictException;
import com.ccddesigner.common.lifecycle.ObjectLifecycleRepository;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
public class ConcurrencyLockAspect {

    @Resource
    private ObjectLifecycleRepository lifecycleRepository;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(lock)")
    public Object checkConcurrency(ProceedingJoinPoint pjp, ConcurrencyProtected lock) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        String[] paramNames = signature.getParameterNames();
        Object[] args = pjp.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            ctx.setVariable(paramNames[i], args[i]);
        }

        Long revisionId = parser.parseExpression(lock.revisionIdSpEL()).getValue(ctx, Long.class);
        Long clientVersion = parser.parseExpression(lock.clientVersionSpEL()).getValue(ctx, Long.class);

        Long currentDbVersion = lifecycleRepository.getWorkingVersion(revisionId);

        if (!currentDbVersion.equals(clientVersion)) {
            throw new OptimisticLockConflictException(
                String.format("Optimistic Lock Error: Entity [%s] has been updated by another user. Client working_version [%s], Current DB version [%s].",
                    revisionId, clientVersion, currentDbVersion)
            );
        }

        return pjp.proceed();
    }
}
```

### 6. 异常分类体系、错误响应契约与审计日志

#### 6.1 RFC 7807 统一安全与状态异常响应格式

当切面拦截到未授权访问、状态非法迁移或并发冲突时，统一返回 RFC 7807 标准 Problem Details 结构，严禁直接对外抛出原始数据库异常堆栈：

JSON

```
{
  "type": "https://plm.company.com/errors/sod-violation",
  "title": "Separation of Duties Violation",
  "status": 403,
  "detail": "Action 'APPROVE_RELEASE' rejected: Creator [ENG-1002] cannot approve their own release proposal according to SoD-01.",
  "instance": "/api/v1/model-releases/REL-2026-009/submit-review",
  "errorCode": "ERR_SOD_SELF_APPROVAL_PROHIBITED",
  "timestamp": "2026-09-15T10:14:22.108Z",
  "invalidAttributes": {
    "creatorId": "ENG-1002",
    "approverId": "ENG-1002",
    "policyRule": "SoD-01"
  }
}
```

#### 6.2 异常分类与 HTTP 状态码映射表

| **异常类名称**                    | **触发源与业务原因**                                | **HTTP 状态码**   | **对应错误码 (errorCode)**      |
| --------------------------------- | --------------------------------------------------- | ----------------- | ------------------------------- |
| `TenantMismatchException`         | 跨租户数据访问违规                                  | 403 Forbidden     | `ERR_TENANT_ACCESS_DENIED`      |
| `SecurityAccessDeniedException`   | PBAC 角色、项目成员或候选隔离未通过（AT-13, AT-29） | 403 Forbidden     | `ERR_PBAC_UNAUTHORIZED`         |
| `SoDViolationException`           | 触碰 SoD-01~06 职责分离规则（自批/越权签署）        | 403 Forbidden     | `ERR_SOD_VIOLATION`             |
| `OptimisticLockConflictException` | 草稿编辑并发 `working_version` 不匹配               | 409 Conflict      | `ERR_CONCURRENCY_CONFLICT`      |
| `StaleWorkerLeaseException`       | 旧 Worker 迟到回写，租约已失效（AT-19）             | 409 Conflict      | `ERR_WORKER_LEASE_EXPIRED`      |
| `InvalidStateTransitionException` | 试图逆向流转状态或前置守卫未满足                    | 422 Unprocessable | `ERR_INVALID_STATE_TRANSITION`  |
| `SecurityTamperingException`      | 候选内容哈希与审批凭证不一致（AT-16）               | 422 Unprocessable | `ERR_HASH_TAMPERING_DETECTED`   |
| `ImmutableEntityException`        | 对已发布/已冻结对象执行写操作（AT-03, AT-25）       | 422 Unprocessable | `ERR_IMMUTABLE_RELEASED_OBJECT` |

#### 6.3 防篡改审计事件规范 (`AuditRecord`)

拦截切面无论成功或失败，均异步通过内部无锁队列记录不可篡改审计追踪，字段落入 `plm_govern.audit_trail` 表：  

- `audit_id` (Snowflake ID)  
- `tenant_id` (租户隔离键)  
- `user_id` (操作主体工号)  
- `action_code` (操作动作)  
- `target_resource_type` (实体类型)  
- `target_resource_id` (实体主键)  
- `pre_state` (跃迁前状态)  
- `post_state` (跃迁后状态)  
- `decision_status` (`SUCCESS` / `BLOCKED`)
- `block_reason` (若被切面拦截，记录详细阻断规则与异常信息)  
- `client_ip` (客户端网络来源)
- `content_digest` (当前受控实体内容 SHA-256 哈希)  
- `recorded_at` (入库物理时间，由数据库时钟生成)  

### 7. 本包验收与对账结论

本开发规格包已实现与上位说明书的逐条映射与安全落地：  

1. **状态机全生命周期封闭**：完全规范了通用工程修订版、模型发布协调作业、仿真计算与验证判定的状态跃迁路径，消除了模糊状态。  
2. **PBAC 与 SoD 严格切面化**：通过 Spring Boot 统一切面流水线，在 Java 核心应用层强制筑牢租户隔离、自审批阻断、无资质签署阻断及候选隔离防线，杜绝依靠前端“隐藏按钮”的伪安全模式。  
3. **闭环防篡改保障**：将并发乐观锁与两阶段发布审批内容哈希检测植入 AOP 切面，与 D01 物理级数据库触发器形成纵深防御，完全满足 AT-04、AT-07、AT-13、AT-16、AT-19、AT-29 等核心用例的验收标准。  