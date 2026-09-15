# CCDDesigner 2.0 产品说明书

## ——面向复杂数控装备的、基于SysML v2系统模型驱动的Model-Based PLM平台

---

# 1. 产品概述

## 1.1 产品名称

**CCDDesigner 2.0**

产品完整定位：

**面向复杂数控装备的、基于SysML v2系统模型驱动的Model-Based PLM平台**

英文名称：

**CCDDesigner Model-Based PLM Platform for Complex CNC Equipment**

---

# 2. 产品定义

CCDDesigner 2.0 是面向数控机床、工业母机及复杂机电装备研发全过程建设的新一代Model-Based PLM平台。

平台以：

**SysML v2系统模型 + PLM产品结构**

作为双核心。

通过集成：

- SysON；
- OpenSysML；
- Flexo；
- OpenModelica；
- CAD；
- CAE；
- CAPP；
- 制造系统；
- 数字孪生系统；

形成覆盖：

```text
需求
→ 系统设计
→ 系统仿真
→ 详细设计
→ 配置设计
→ 工艺设计
→ 制造
→ 交付
→ 运维
```

的完整数字主线。

CCDDesigner 2.0不再只是传统的：

```text
Part
+ BOM
+ CAD
+ Drawing
+ Document
```

管理平台，而升级为：

```text
System Model
+
Engineering Model
+
Product Structure
+
Lifecycle Management
+
Digital Thread
```

统一的复杂装备研发平台。

---

# 3. 产品核心理念

CCDDesigner 2.0采用：

# “System Model + Product Structure”双核心架构

其中：

```text
System Model
```

解决：

> 为什么设计这个产品？

> 产品需要实现什么功能？

> 系统如何组成？

> 系统之间如何交互？

> 参数如何约束？

> 系统性能是否满足需求？

而：

```text
Product Structure
```

解决：

> 产品最终由什么组成？

> 使用哪些模块和零部件？

> 使用什么CAD模型？

> 如何进行产品配置？

> 如何制造？

> 最终交付的具体设备是什么？

两者通过Digital Thread进行连接。

---

# 4. CCDDesigner总体产品架构

CCDDesigner 2.0总体划分为六大业务域：

```text
CCDDesigner 2.0
│
├─ 01 MBSE系统工程
│
├─ 02 仿真与验证
│
├─ 03 产品工程
│
├─ 04 制造工程
│
├─ 05 PLM生命周期管理
│
└─ 06 产品实例与数字孪生
```

其中MBSE系统工程采用：

```text
SysON
+
OpenSysML
+
Flexo
```

构成统一SysML v2技术体系。

---

# 5. 总体业务主线

CCDDesigner建立以下完整研发主线：

```text
Customer Requirement
        ↓
System Requirement
        ↓
SysML v2 System Model
        ↓
Function
        ↓
Behavior
        ↓
Logical Architecture
        ↓
Interface
        ↓
Parameter / Constraint
        ↓
OpenModelica
        ↓
System Simulation
        ↓
Verification
        ↓
Physical Architecture
        ↓
Product Platform
        ↓
Module
        ↓
150% BOM
        ↓
Configuration
        ↓
100% EBOM
        ↓
CAD / CAE
        ↓
MBOM / BOP
        ↓
Manufacturing
        ↓
Machine Individual
        ↓
Digital Twin
        ↓
Service
```

贯穿整个过程的公共能力包括：

```text
Revision

Lifecycle

Configuration

Change

Baseline

Workflow

Permission

Traceability
```

---

# 6. MBSE总体定位

MBSE模块不独立于PLM运行。

CCDDesigner采用：

```text
MBSE Engineering Environment
             │
             ▼
      PLM Lifecycle Backbone
```

模式。

MBSE负责：

```text
Requirement
Function
Behavior
Logical Architecture
Interface
Parameter
Constraint
Analysis
Verification
Physical Architecture
```

PLM负责：

```text
Object
Revision
Lifecycle
Baseline
Configuration
Change
Workflow
Permission
Project
Deliverable
```

因此：

> SysML v2负责表达工程语义，CCDDesigner负责管理工程语义的企业生命周期。

---

# 7. SysON + OpenSysML + Flexo总体架构

CCDDesigner不将SysON、OpenSysML和Flexo简单作为三个并列工具。

三者分别承担不同职责。

总体关系：

```text
                  CCDDesigner
                       │
                MBSE Workspace
                       │
        ┌──────────────┼──────────────┐
        │              │              │
      SysON        OpenSysML        Flexo
        │              │              │
   图形建模        语义引擎        Model Hub
   协同建模        文本建模        API Services
   Diagram         LSP/Parser       Federation
        │              │              │
        └──────────────┼──────────────┘
                       │
                 SysML v2 Model
                       │
                 CCDDesigner PLM
```

---

# 8. SysON的产品定位

SysON主要承担：

# SysML v2 Web图形建模与协同设计前端

SysON用于为系统工程师提供：

- SysML v2图形建模；
- 系统结构设计；
- 需求建模；
- 部件建模；
- 接口建模；
- 行为建模；
- 状态建模；
- 关系建模；
- 参数展示；
- 模型导航；
- 多用户协同设计。

其主要角色为：

```text
MBSE Graphical Authoring Environment
```

即：

**SysML v2图形化设计工作台。**

SysON目前本身就是Web化建模平台，并使用PostgreSQL作为其支持的企业数据库之一。其2026版本还增加了跨项目搜索能力。

---

# 9. OpenSysML的产品定位

OpenSysML主要承担：

# SysML v2语义引擎

包括：

- SysML v2 Parser；
- KerML Parser；
- Semantic Validation；
- Textual Modeling；
- Language Server；
- LSP；
- Constraint Evaluation；
- Requirement Evaluation；
- Calculation Execution；
- Model Analysis。

其在CCDDesigner中的定位是：

```text
SysML v2 Semantic Engine
```

而不是PLM数据库。

典型业务：

```text
SysON Model
        ↓
SysML v2 Text
        ↓
OpenSysML
        ↓
Parse
        ↓
Semantic Validation
        ↓
Constraint Check
        ↓
Execution
```

OpenSysML当前提供语言服务器、交互运行环境、执行运行时及Python客户端等能力，因此适合作为CCDDesigner内部SysML v2语义服务。

---

# 10. Flexo的产品定位

Flexo承担：

# SysML v2 Model Hub

其核心作用不是替代SysON建模，而是建立：

```text
Model Repository
+
Systems Modeling API
+
Model Federation
+
Model Exchange
```

能力。

Flexo负责：

```text
SysML Repository
        │
        ├─ Model Project
        ├─ Commit
        ├─ Branch
        ├─ Model Element
        ├─ Relationship
        └─ API
```

同时连接：

```text
SysON
OpenSysML
CCDDesigner
OpenModelica
其他SysML工具
外部MBSE系统
```

形成：

```text
       SysON
          │
          ▼
        Flexo
       /  │  \
      /   │   \
OpenSysML │ CCDDesigner
          │
     External Tool
```

Flexo MMS已经提供SysML v2 API Services实现，因此适合承担CCDDesigner的SysML v2标准模型服务层。

---

# 11. 三者之间的职责边界

必须避免：

```text
SysON数据库
=
OpenSysML数据库
=
Flexo数据库
=
PLM数据库
```

这样的重复主数据模式。

推荐职责：

| 系统 | 核心职责 |
|---|---|
| SysON | 图形化建模 |
| OpenSysML | SysML v2语义解释 |
| Flexo | SysML v2模型服务及模型联邦 |
| CCDDesigner | 生命周期与工程数据治理 |

因此定义：

```text
SysON
=
Authoring

OpenSysML
=
Semantic Processing

Flexo
=
Model Hub

CCDDesigner
=
Lifecycle Governance
```

---

# 12. MBSE核心业务链

CCDDesigner MBSE主线：

```text
Requirement
↓
Function
↓
Behavior
↓
Logical Architecture
↓
Interface
↓
Parameter
↓
Constraint
↓
Analysis
↓
Physical Architecture
↓
Verification
```

对应典型RFLP：

```text
R
Requirement

↓

F
Functional Architecture

↓

L
Logical Architecture

↓

P
Physical Architecture
```

---

# 13. 数控机床系统模型

以VMC1000为例：

```text
VMC1000 Machine System
│
├─ Mechanical Structure System
│
├─ Spindle System
│
├─ Feed System
│   ├─ X Axis
│   ├─ Y Axis
│   └─ Z Axis
│
├─ CNC Control System
│
├─ Servo System
│
├─ Electrical System
│
├─ Hydraulic System
│
├─ Pneumatic System
│
├─ Lubrication System
│
├─ Cooling System
│
├─ Tool Magazine System
│
├─ Measurement System
│
└─ Safety System
```

该体系首先存在于SysML v2 System Model中。

---

# 14. 需求管理

CCDDesigner建立结构化需求对象：

```text
Requirement Specification
        │
        └─ Requirement
              │
              ├─ Requirement Revision
              ├─ Parameter
              ├─ Source
              ├─ Priority
              ├─ Verification Method
              └─ Verification Status
```

需求可以：

```text
derive

refine

satisfy

verify

trace
```

其他系统模型对象。

---

# 15. 需求与SysML v2统一

需求不能仅仅存在于PLM数据库中。

采用：

```text
PLM Requirement Object
        │
        │ lifecycle binding
        ▼
SysML Requirement Usage
```

形成：

```text
PLM Object
+
SysML Semantic Object
```

双层模型。

PLM维护：

```text
编号
版本
状态
项目
责任人
审批
Baseline
Change
```

SysML维护：

```text
语义
模型关系
约束
系统上下文
验证关系
```

---

# 16. 系统参数管理

Parameter成为CCDDesigner一级工程对象。

例如：

```text
PAR-SP-001

Spindle Maximum Speed

Value:
15000

Unit:
rpm
```

一个参数可以关联：

```text
Requirement

SysML Attribute

Modelica Parameter

CAD Parameter

CAE Parameter

Test Parameter
```

形成：

```text
                Requirement
                     │
                     ▼
SysML Attribute ← Parameter → Modelica
                     │
                   CAD
                     │
                   CAE
                     │
                   Test
```

---

# 17. OpenModelica系统仿真

CCDDesigner集成OpenModelica建立：

# System Simulation

主要覆盖：

- CNC控制；
- 电机；
- 伺服；
- 进给系统；
- 主轴；
- 机械传动；
- 液压；
- 气动；
- 冷却；
- 润滑；
- 控制系统。

---

# 18. SysML → Modelica集成

建立：

# Model Mapping Service

实现：

```text
SysML Parameter
        ↓
Parameter Mapping
        ↓
Modelica Parameter
        ↓
OpenModelica
        ↓
Simulation
        ↓
Simulation Result
        ↓
Verification
```

例如：

```text
SYSML:

XAxis.motorPower
```

映射：

```text
MODELICA:

XAxisDrive.motorPower
```

映射本身作为PLM对象：

```text
ModelMapping

Source
Target
Direction
Unit
Transform
Version
Status
```

---

# 19. 系统仿真对象模型

OpenModelica仿真不是简单文件运行。

统一管理为：

```text
Simulation Study
        │
        ├─ Simulation Model
        │
        ├─ Parameter Set
        │
        ├─ Simulation Configuration
        │
        ├─ Simulation Case
        │
        ├─ Simulation Job
        │
        ├─ Simulation Run
        │
        ├─ Simulation Result
        │
        └─ Verification Result
```

从而形成SPDM能力。

---

# 20. 系统验证

最终建立：

```text
Requirement
        ↓
Verification Requirement
        ↓
Analysis
        ↓
Simulation Case
        ↓
OpenModelica
        ↓
Simulation Result
        ↓
KPI
        ↓
Verification Result
        ↓
PASS / FAIL
```

例如：

```text
Requirement

X轴最大定位误差 ≤ 8 μm

↓

Simulation

↓

Result

6.7 μm

↓

Verification

PASS
```

---

# 21. System Architecture与BOM分离

SysML System Architecture不能直接等同于EBOM。

例如：

```text
System Architecture

Feed System
├─ X Axis
├─ Y Axis
└─ Z Axis
```

EBOM可能为：

```text
X Axis Assembly
├─ Servo Motor
├─ Coupling
├─ Bearing
├─ Ball Screw
├─ Linear Guide
└─ Encoder
```

通过：

```text
allocate

realize

implement
```

建立关联。

---

# 22. Physical Architecture

SysML Physical Architecture是连接MBSE与PLM产品工程的重要桥梁：

```text
Logical Architecture
        ↓
Physical Architecture
        ↓
Product Platform
        ↓
Module
        ↓
EBOM
```

例如：

```text
Logical:

X Axis Drive System

↓

Physical:

Servo Motor
Ball Screw
Bearing
Guide

↓

PLM:

X Axis Assembly
```

---

# 23. 产品平台

产品工程采用：

```text
Product Family
↓
Platform
↓
Module Slot
↓
Module Variant
```

例如：

```text
PF-VMC
立式加工中心产品族
        │
        └─ VMC Platform
             │
             ├─ Base Module
             ├─ Column Module
             ├─ Spindle Module
             ├─ Feed Module
             ├─ CNC Module
             └─ Tool Magazine Module
```

---

# 24. 150% BOM

平台建立：

```text
Product Platform
        ↓
150% BOM
```

例如：

```text
VMC1000
│
├─ CNC
│   ├─ Siemens
│   ├─ FANUC
│   └─ HNC
│
├─ Spindle
│   ├─ BT40-12000
│   ├─ BT40-15000
│   └─ HSK63-18000
│
└─ Tool Magazine
    ├─ 24T
    ├─ 30T
    └─ 40T
```

---

# 25. 配置设计

配置系统包含：

```text
Configuration Feature

Configuration Option

Configuration Rule

Effectivity

Constraint

Configuration Context
```

支持：

```text
150% BOM
        ↓
Configuration
        ↓
Rule Evaluation
        ↓
100% BOM
```

---

# 26. CTO模式

```text
Platform
↓
Configuration
↓
CTO
↓
Configured Product
↓
100% BOM
↓
Order Machine
```

主要用于标准模块组合。

---

# 27. ETO模式

```text
Customer Requirement
↓
Mother Machine
↓
Clone
↓
SysML Model Change
↓
Simulation
↓
Engineering Change
↓
CAD / CAE
↓
EBOM
↓
Order Machine
```

这意味着ETO不只是：

```text
Clone BOM
```

而应升级为：

```text
Clone Product Definition
```

其中包含：

```text
Requirement
+
SysML
+
Parameter
+
Simulation
+
BOM
+
CAD
```

---

# 28. PLM Backbone

CCDDesigner建立统一PLM核心：

```text
CCObject
│
├─ Revision
├─ Dataset
├─ Relation
├─ Structure
├─ Lifecycle
├─ Configuration
├─ Baseline
├─ Change
└─ Workflow
```

所有工程对象统一进入该体系。

---

# 29. 统一工程对象

CCDDesigner管理的主要对象包括：

```text
Requirement

System Model

SysML Model Publication

Parameter

Modelica Model

Simulation

Verification

Platform

Module

Part

BOM

CAD

CAE

Document

Process Plan

Machine Individual
```

统一获得：

```text
ID
Revision
State
Owner
Project
Change
Baseline
ACL
History
```

能力。

---

# 30. Baseline

Baseline成为一级PLM业务对象。

主要包括：

```text
Requirement Baseline

System Model Baseline

Architecture Baseline

Simulation Baseline

Design Baseline

Configuration Baseline

Manufacturing Baseline

As-Built Baseline
```

例如：

```text
System Baseline SB-001

Requirement       R12
SysML Model       V5
Modelica Model    V3
Simulation        RUN-017
Physical Model    Rev B
EBOM              Rev C
```

---

# 31. 工程变更

变更必须覆盖系统工程和产品工程。

```text
Change Request
        ↓
Impact Analysis
        ↓
Requirement
        ↓
SysML
        ↓
Parameter
        ↓
Modelica
        ↓
Simulation
        ↓
Physical Architecture
        ↓
CAD / CAE
        ↓
BOM
        ↓
Verification
        ↓
Release
```

---

# 32. Digital Thread

CCDDesigner建立四条主要数字主线。

## 32.1 System Digital Thread

```text
Requirement
↓
Function
↓
Behavior
↓
Logical Architecture
↓
Interface
↓
Parameter
↓
Physical Architecture
```

## 32.2 Simulation Digital Thread

```text
Requirement
↓
Analysis
↓
Modelica
↓
Simulation
↓
Result
↓
KPI
↓
Verification
```

## 32.3 Product Digital Thread

```text
Physical Architecture
↓
Platform
↓
Module
↓
150% BOM
↓
Configuration
↓
EBOM
↓
CAD
```

## 32.4 Lifecycle Digital Thread

```text
EBOM
↓
MBOM
↓
BOP
↓
Manufacturing
↓
As-Built
↓
Machine Individual
↓
Service
```

---

# 33. 不采用Neo4j的数字主线方案

CCDDesigner不采用Neo4j。

数字主线关系统一进入：

# PostgreSQL

核心关系模型：

```text
CC_OBJECT

CC_REVISION

CC_RELATION

TRACE_LINK

DEPENDENCY_EDGE

MODEL_MAPPING

STRUCTURE_NODE

STRUCTURE_RELATION
```

例如：

```text
TRACE_LINK

id
source_object_id
source_revision_id
target_object_id
target_revision_id
relation_type
context_id
baseline_id
status
created_at
```

---

# 34. PostgreSQL关系追踪机制

采用关系表：

```text
Source
   │
   │ Relation
   ▼
Target
```

例如：

```text
REQ-001
   │ satisfy
   ▼
FUN-001
   │ allocate
   ▼
LOGICAL-X
   │ realize
   ▼
PHYSICAL-X
   │ implement
   ▼
BOM-X
```

通过：

```text
Recursive CTE

Closure Table

Materialized Path

Relation Index
```

实现多级追踪。

---

# 35. 影响分析

例如：

```text
Spindle Speed

12000 rpm
     ↓
15000 rpm
```

系统通过PostgreSQL Relation Service递归分析：

```text
SpindleSpeed
│
├─ Requirement
├─ Constraint
├─ Spindle Function
├─ Motor
├─ Bearing
├─ Modelica Model
├─ Thermal Analysis
├─ CAD
├─ EBOM
└─ Verification
```

生成：

```text
Impact Analysis Report
```

因此：

> 影响分析能力并不依赖Neo4j。

---

# 36. 搜索与关系导航

为了降低PostgreSQL承担全文检索的压力，可增加：

```text
OpenSearch / Elasticsearch
```

但其作用严格限定为：

```text
Search Index

Model Search

Relation Search

Navigation Index

Full Text Search
```

不是主数据存储。

主数据始终位于：

```text
PostgreSQL
```

SysON自身在2026版本中也已经提供实验性的Elasticsearch跨项目搜索能力，因此这一方向与其生态兼容。

---

# 37. 推荐数据架构

最终建议：

```text
                 CCDDesigner Data Architecture

                           │
          ┌────────────────┼─────────────────┐
          │                │                 │
     PostgreSQL          MinIO        OpenSearch
          │                │                 │
      PLM Data          Dataset          Search
      Relation          CAD/CAE          Index
      BOM               Simulation
      Change            Document
      Baseline
```

MBSE域：

```text
SysON Repository
       │
       ▼
Flexo Model Hub
       │
       ▼
SysML v2 API
```

同时：

```text
OpenSysML
=
Semantic Runtime
```

---

# 38. 数据主权原则

CCDDesigner采用：

# Domain Source of Truth

而不是试图让一个数据库保存全部工程信息。

定义如下：

| 数据 | 权威源 |
|---|---|
| SysML系统模型 | MBSE Model Repository |
| PLM业务对象 | PostgreSQL |
| BOM | PostgreSQL |
| 配置 | PostgreSQL |
| 变更 | PostgreSQL |
| Baseline | PostgreSQL |
| Workflow | PostgreSQL |
| CAD/CAE文件 | MinIO |
| 仿真结果 | MinIO |
| 文档 | MinIO |
| 搜索索引 | OpenSearch |
| CAD几何辅助数据 | MongoDB，可选 |

---

# 39. SysML模型进入PLM的方式

不建议把全部SysML内部Element复制成PLM对象。

采用：

# Model Publication

模式：

```text
SysML Project
        ↓
Model Commit
        ↓
Publish
        ↓
SysML Model Revision
        ↓
CCDDesigner
```

CCDDesigner保存：

```text
Model ID

Version

Commit

Baseline

Status

Owner

Project

Publish Time
```

对于关键元素，再创建：

```text
Model Element Reference
```

---

# 40. Model Element Reference

例如：

```text
PLM Object:

Spindle System
```

关联：

```text
SysML Element:

sysml://project/VMC1000/part/spindleSystem
```

而不是复制完整SysML模型。

形成：

```text
CCObject
      │
      │ semantic reference
      ▼
SysML Element
```

---

# 41. 制造工程

产品设计完成后：

```text
EBOM
↓
MBOM
↓
BOP
↓
Process Plan
↓
Operation
↓
Work Center
↓
Manufacturing Instruction
```

建立设计到制造数字主线。

---

# 42. 产品实例

产品制造完成后建立：

```text
Machine Individual
```

例如：

```text
VMC1000-SN202610001
```

关联：

```text
Product Model

Configuration

As-Built BOM

Software Version

Parameter

Inspection Result

Test Result

Manufacturing Record
```

---

# 43. SysML v2 Individual与产品实例

SysML v2模型中的Individual概念可以与PLM产品实例形成关联：

```text
SysML Individual
        │
        │ realize
        ▼
PLM Machine Individual
```

从而实现：

```text
设计中的设备实例
        ↓
实际制造设备
```

对应。

---

# 44. Digital Twin

产品实例继续连接：

```text
Machine Individual
↓
Digital Twin
```

数字孪生包括：

```text
Design Model
+
System Model
+
Simulation Model
+
As-Built Configuration
+
IoT Data
+
Maintenance Data
```

---

# 45. 项目管理

CCDDesigner继续采用：

```text
Program
│
└─ Project
     │
     ├─ Stage
     │
     ├─ WBS
     │
     ├─ Task
     │
     ├─ Workflow
     │
     ├─ Deliverable
     │
     └─ Gate
```

Deliverable可以对应：

```text
Requirement Baseline

SysML Model

Simulation

CAD

CAE

EBOM

Verification Report
```

---

# 46. 推荐一级产品模块

CCDDesigner 2.0建议最终形成以下一级功能模块：

```text
01 Dashboard

02 Program Management

03 Project Management

04 Requirement Management

05 MBSE Workspace

06 SysML v2 Modeling

07 System Architecture

08 Interface Management

09 Parameter Management

10 Model Mapping

11 Modelica Management

12 Simulation Management

13 Verification Management

14 Product Family

15 Product Platform

16 Module Management

17 Configuration Management

18 150% BOM

19 EBOM

20 CAD Management

21 CAE Management

22 Document Management

23 Change Management

24 Baseline Management

25 Workflow Management

26 MBOM

27 BOP / Process Management

28 Manufacturing Data

29 Machine Individual

30 Digital Twin

31 Digital Thread

32 Impact Analysis

33 Search

34 AI Engineering Assistant

35 System Administration
```

---

# 47. MBSE内部模块

MBSE Workspace进一步包含：

```text
MBSE Workspace
│
├─ Requirement
│
├─ SysON Modeler
│
├─ OpenSysML Editor
│
├─ System Architecture
│
├─ Functional Architecture
│
├─ Logical Architecture
│
├─ Physical Architecture
│
├─ Interface
│
├─ Parameter
│
├─ Constraint
│
├─ Analysis
│
├─ Verification
│
└─ Model Publication
```

底层形成：

```text
SysON
+
OpenSysML
+
Flexo
```

统一服务。

---

# 48. 推荐技术架构

```text
┌──────────────────────────────────────────────────────┐
│               Experience Layer                       │
│                                                      │
│ Portal / Dashboard / Project / Engineering Workspace │
├──────────────────────────────────────────────────────┤
│            Engineering Applications                  │
│                                                      │
│ SysON │ OpenSysML │ Modelica │ CAD │ CAE │ BOM       │
├──────────────────────────────────────────────────────┤
│               MBSE Model Layer                       │
│                                                      │
│ Flexo │ SysML v2 API │ Model Publication             │
├──────────────────────────────────────────────────────┤
│              Digital Thread Layer                    │
│                                                      │
│ Trace │ Mapping │ Dependency │ Impact │ Verification  │
├──────────────────────────────────────────────────────┤
│                PLM Backbone                          │
│                                                      │
│ Object │ Revision │ Structure │ Change │ Baseline     │
│ Configuration │ Lifecycle │ Workflow                 │
├──────────────────────────────────────────────────────┤
│              Integration Layer                       │
│                                                      │
│ SysML API │ FMI │ REST │ MCP │ Connector │ Event Bus  │
├──────────────────────────────────────────────────────┤
│                    Data Layer                        │
│                                                      │
│ PostgreSQL │ MinIO │ OpenSearch │ MongoDB(optional)  │
├──────────────────────────────────────────────────────┤
│               Infrastructure                         │
│                                                      │
│ IAM │ SSO │ Gateway │ Audit │ Container │ Monitoring │
└──────────────────────────────────────────────────────┘
```

---

# 49. 后端架构原则

CCDDesigner PLM核心建议采用：

# Modular Monolith / Coarse-Grained Core

即：

```text
PLM Core
│
├─ Object
├─ Revision
├─ Structure
├─ BOM
├─ Configuration
├─ Lifecycle
├─ Change
├─ Baseline
└─ Relation
```

保持强事务一致性。

外围能力可以服务化：

```text
MBSE Service

SysML Semantic Service

Simulation Service

OpenModelica Service

CAD Connector

CAE Connector

Search Service

Visualization Service

AI Service
```

避免把核心PLM强行拆成大量细粒度微服务。

---

# 50. 推荐技术栈

| 技术域 | 推荐技术 |
|---|---|
| 前端 | React + TypeScript |
| PLM核心 | Spring Boot |
| 核心数据库 | PostgreSQL |
| SysML图形建模 | SysON |
| SysML语义引擎 | OpenSysML |
| SysML模型服务 | Flexo |
| 系统仿真 | OpenModelica |
| 联合仿真 | FMI |
| 工作流 | Flowable |
| 对象文件 | MinIO |
| CAD辅助数据 | MongoDB，可选 |
| 搜索 | OpenSearch / Elasticsearch |
| 消息 | Kafka / Outbox |
| 身份认证 | OIDC / SSO |
| API | REST / GraphQL |
| AI工具集成 | MCP |
| 容器 | Docker / Kubernetes |

明确：

```text
不采用Neo4j
```

---

# 51. CCDDesigner核心数字主线

最终平台需要形成：

```text
Customer Requirement
        ↓
Requirement
        ↓
SysON / SysML v2
        ↓
System Architecture
        ↓
Function
        ↓
Logical Architecture
        ↓
Parameter
        ↓
OpenModelica
        ↓
Simulation
        ↓
Verification
        ↓
Physical Architecture
        ↓
Platform
        ↓
Module
        ↓
150% BOM
        ↓
Configuration
        ↓
100% EBOM
        ↓
CAD / CAE
        ↓
MBOM
        ↓
BOP
        ↓
Manufacturing
        ↓
Machine Individual
        ↓
Digital Twin
```

---

# 52. 四条数字主线

最终产品形成四条相互关联的Digital Thread。

### 第一条：系统设计主线

```text
Requirement
→ Function
→ Behavior
→ Logical
→ Interface
→ Physical
```

### 第二条：仿真验证主线

```text
Requirement
→ Parameter
→ Modelica
→ Simulation
→ KPI
→ Verification
```

### 第三条：产品工程主线

```text
Physical Architecture
→ Platform
→ Module
→ 150%BOM
→ Configuration
→ EBOM
→ CAD
```

### 第四条：产品生命周期主线

```text
EBOM
→ MBOM
→ BOP
→ Manufacturing
→ As-Built
→ Machine Individual
→ Digital Twin
→ Service
```

---

# 53. 产品核心差异化

CCDDesigner不应定位为一个普通PLM。

其差异化能力是：

### 1. SysML v2原生MBSE

采用：

```text
SysON
+
OpenSysML
+
Flexo
```

构建自主可控的SysML v2工程体系。

### 2. MBSE与PLM原生融合

不是：

```text
MBSE
+
PLM
```

简单接口集成。

而是：

```text
System Model
        │
        ▼
PLM Lifecycle
```

统一治理。

### 3. Modelica机电耦合仿真

将系统设计直接连接系统仿真。

### 4. 面向数控机床的产品平台化

支持：

```text
Platform
Module
Variant
150% BOM
Configuration
```

### 5. CTO + ETO统一设计

同时支持：

```text
Configure
```

与：

```text
Clone + Engineering
```

### 6. 完整验证闭环

形成：

```text
Requirement
→ Simulation
→ Verification
```

### 7. 数字主线不依赖图数据库

采用：

```text
PostgreSQL关系模型
+
递归关系查询
+
搜索索引
```

实现工程追踪和影响分析。

---

# 54. 产品核心价值

CCDDesigner需要回答复杂装备研发中的五个问题。

## Why——为什么这样设计？

由：

```text
Requirement
+
SysML v2
```

回答。

## How——系统怎样工作？

由：

```text
Function
+
Behavior
+
Logical Architecture
+
Interface
```

回答。

## Does it work——设计能否满足要求？

由：

```text
OpenModelica
+
CAE
+
Simulation
+
Verification
```

回答。

## What is built——最终制造的是什么？

由：

```text
Configuration
+
EBOM
+
MBOM
+
As-Built
```

回答。

## What happened——设备实际运行情况怎样？

由：

```text
Machine Individual
+
Digital Twin
+
Service
```

回答。

---

# 55. 产品最终定义

CCDDesigner 2.0最终定义为：

> **CCDDesigner 2.0 是面向数控机床及复杂工业装备研发的SysML v2系统模型驱动型Model-Based PLM平台。平台以SysON提供SysML v2图形化系统建模能力，以OpenSysML提供SysML v2/KerML语义解析、校验与执行能力，以Flexo提供标准化SysML v2模型服务及模型联邦能力，以OpenModelica提供机电耦合系统仿真能力，以CCDDesigner PLM Backbone统一管理工程对象、版本、生命周期、产品结构、配置、变更、基线、工作流和权限，并进一步连接CAD、CAE、150% BOM、CTO/ETO、制造工程、产品实例和数字孪生，最终形成贯穿需求、系统设计、仿真验证、详细设计、制造和服役全过程的复杂装备数字主线。**

整个产品体系可以归纳为：

```text
CCDDesigner 2.0

=

SysON
+
OpenSysML
+
Flexo
+
SysML v2 MBSE
+
PLM Backbone
+
OpenModelica
+
SPDM
+
CAD / CAE
+
Product Platform
+
150% BOM
+
Configuration
+
CTO / ETO
+
Change
+
Baseline
+
Digital Thread
+
Manufacturing
+
Machine Individual
+
Digital Twin
```

最终形成：

# “系统模型驱动产品定义，仿真模型驱动设计验证，PLM管理工程状态，数字主线贯穿完整生命周期。”

这应当成为CCDDesigner 2.0最核心的产品设计原则。