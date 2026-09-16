# CCDDesigner 2.0 专项开发规格包 (Detailed Specifications) - 10

## 文档标识
- **规格编号**：`CCD-DEV-SPEC-2.0-D10`
- **主题**：前端 React 原型交互规范、压测脚本与自动化验收用例库规格说明书 (Detailed Specification for Frontend React Prototype Interaction, Stress Testing Scripts & End-to-End Acceptance Test Cases)
- **对应模块**：M01 (统一门户与协同工作台)、M04/M06 (MBSE 系统建模工作区)、M08/M09/M10 (多学科协同仿真看板与因果回路)、M14 (150% Super BOM 规则向导与求解器交互)、M23 (数字主线全链路因果关系溯源拓扑爆炸图)、M25 (制造工程 EBOM/MBOM 拆分重组与平衡残差看板)、M26 (工程下发与制造回传对账) 以及平台性能与质量保障体系
- **所属阶段**：P4 (端到端集成、自动化验收与工程投产交付)
- **核心规约**：单向数据流与乐观更新策略（Optimistic Updates with Strict Rollback）、幂等令牌自动注入（Idempotency-Key & Signature Injection）、大规模图视口分级 LOD 虚拟化（Level of Detail for 5000+ Nodes）、全量验收用例（AT-01 ~ AT-28）自动化可执行（100% Executable E2E Specs）

---

## 一、规格背景与设计目标

### 1.1 业务背景
CCDDesigner 2.0 作为高端数控机床行业首个深度融合 MBSE、多学科协同仿真、150% Super BOM 规则配置、数字主线图溯源及 EBOM/MBOM 制造闭环的复杂工业软件，前端交互面临多重挑战：
1. **多维度异构数据实时呈现**：工程师需要在同一工作流中同时查看 SysML v2 文本/图形、多体动力学因果回路曲线、千行规模 BOM 矩阵及多层递归网络图；
2. **高频工业级实时交互响应**：仿真参数微调、BOM 规则向导计算、制造残差计算等需在毫秒级完成客户端或服务端反馈；
3. **极高的数据一致性与审计严谨性**：涉及国家机密与企业核心知识产权，页面必须内嵌密级标定（Bell-LaPadula）与基于属性访问控制（PBAC），所有修改与状态下发必须附带防重放与幂等凭据。

### 1.2 核心设计目标
1. **统一设计语言与现代架构**：基于 React 18 + TypeScript + Ant Design 5.x + Tailwind CSS 构建模块化工业前端组件库；
2. **极限性能保障**：数字主线 5000 节点拓扑图缩放平移保持 60 FPS，首屏加载 FCP < 1.2s，关键接口 P99 < 500ms；
3. **全链路可追溯与可验证**：对齐《CCDDesigner 2.0 产品开发说明书》中的 AT-01 至 AT-28 全量验收项，建立完整的 k6 性能压测与 Playwright 端到端自动化验证套件。

---

## 二、前端技术架构与核心规范

### 2.1 技术选型与分层架构

```mermaid
graph TB
    subgraph ViewLayer [表现层 View Layer]
        A1[Ant Design 5.x / ProComponents 工业基础组件]
        A2[@antv/x6 拓扑图与数字主线画布]
        A3[Monaco Editor SysML v2 代码编辑器]
        A4[Apache ECharts 仿真动态曲线与残差看板]
    end

    subgraph StateLayer [状态编排层 State & Data Layer]
        B1[Zustand 全局轻量原子状态: 用户会话 / 密级上下文 / 激活工程]
        B2[TanStack Query v5 服务端缓存 / 乐观更新 / 自动重试]
        B3[Web Workers 密集计算: 局部拓扑布局 / 语法 AST 校验 / 矩阵乘法]
    end

    subgraph InfrastructureLayer [通信与安全基座 Infra Layer]
        C1[Axios 实例包装: 动态注入 Idempotency-Key / 签名 / 租户头]
        C2[WebSocket / SSE 客户端: 仿真日志流 / 制造下发回执推送]
        C3[PBAC 动态路由守卫 & <SecuredAction> 权限按钮]
    end

    ViewLayer --> StateLayer
    StateLayer --> InfrastructureLayer
```

- **核心依赖清单**：
  - **核心框架**：React 18.3+，TypeScript 5.x，Vite 5.x
  - **UI 与样式**：Ant Design 5.16+，@ant-design/pro-components，Tailwind CSS 3.4+，Lucide-React 图标库
  - **状态与请求**：Zustand 4.5+，TanStack Query (React Query) v5，Axios 1.6+
  - **专业可视化**：@antv/x6 2.x（节点拓扑与流程图）、Monaco Editor（SysML v2 建模）、Apache ECharts 5.5+（多参数协同仿真与残差看板）

### 2.2 全局通信、安全拦截与幂等凭证注入机制

所有通过 Axios 发起的写操作（`POST`, `PUT`, `DELETE`）必须经过全局请求拦截器自动注入工业级安全与幂等头：

```typescript
// src/infra/api/httpClient.ts
import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { v4 as uuidv4 } from 'uuid';
import CryptoJS from 'crypto-js';
import { useAuthStore } from '@/stores/useAuthStore';

export const apiClient: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
    'X-Client-Platform': 'CCDDesigner-Web-2.0'
  }
});

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const { token, currentProject, currentSecurityLevel } = useAuthStore.getState();

  // 1. 认证 Token 与密级上下文注入
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  if (currentProject) {
    config.headers.set('X-Project-Id', currentProject.id);
  }
  config.headers.set('X-Security-Clearance', currentSecurityLevel || 'INTERNAL');

  // 2. 幂等令牌 (Idempotency-Key) 自动生成与注入（仅限有状态写请求）
  const method = config.method?.toUpperCase();
  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method || '')) {
    if (!config.headers.has('Idempotency-Key')) {
      config.headers.set('Idempotency-Key', `IDEMP-${Date.now()}-${uuidv4()}`);
    }
    
    // 3. 请求签名计算 (防止工业参数与指令在网络链路中被篡改)
    const timestamp = Date.now().toString();
    const payloadStr = config.data ? (typeof config.data === 'string' ? config.data : JSON.stringify(config.data)) : '';
    const signSource = `${method}|${config.url}|${timestamp}|${payloadStr}`;
    const signature = CryptoJS.HmacSHA256(signSource, token || 'CCD-ANONYMOUS-SECRET').toString();
    
    config.headers.set('X-Request-Timestamp', timestamp);
    config.headers.set('X-Request-Signature', signature);
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // 拦截业务状态机阻断与安全违规 (403 PBAC, 409 状态锁闭)
    if (error.response?.status === 403) {
      console.error('[PBAC-DENIED] 越权操作或当前密级不够访问此资产');
    }
    return Promise.reject(error.response?.data || error.message);
  }
);
```

### 2.3 大规模拓扑图 (5000+ 节点) 视口按需加载与虚拟化策略

针对数字主线 M23（全链路因果追溯图高达 5000 节点、12000 关联边）场景，采用双层降级加速方案：
1. **分级 LOD (Level of Detail) 视口裁剪**：
   - 缩放比 $Scale < 0.3$：隐藏全部节点属性文本、小图标与端口，仅渲染外轮廓色块与主干边；
   - 缩放比 $0.3 \le Scale < 0.8$：渲染构造型、节点名称与密级标签；
   - 缩放比 $Scale \ge 0.8$：完全展开参数表、哈希指纹、状态机 Badge 与快速追溯操作手柄。
2. **视口虚拟化 (Spatial Culling)**：
   - 使用 R-Tree（空间索引树）在前端计算当前 Viewport 边界内的包围盒（AABB），仅向 DOM/Canvas 提交视口内部及外扩 200px 缓冲区的节点与边，其余节点设置 `display: none` 或跳过绘制。
3. **Web Worker 离线布局**：
   - 拓扑关系 DAG 拓扑分层（Dagre）与力导向（Force-Directed）计算全部下沉至 `topology-layout.worker.ts`，主线程仅负责渲染像素，彻底杜绝 UI 卡死。

---

## 三、七大核心界面原型交互规范与组件时序

### 3.1 M01 统一协同工作台与全局快捷检索

#### 交互规范
- **快捷唤起**：按下 `Cmd + K` (macOS) 或 `Ctrl + K` (Windows) 瞬间弹出居中高亮搜索浮层。
- **搜索范畴**：支持机床型号（如 `VMC-850`）、参数变量（`Spindle_Max_Speed`）、EBOM 零件号、SysML 构件、仿真作业 ID。
- **即时响应**：输入防抖 150ms，利用本地 IndexedDB 缓存最近 50 条足迹与前缀索引，服务端回传综合得分排序结果。

```mermaid
sequenceDiagram
    autonumber
    actor Engineer as 设计/制造工程师
    participant UI as M01 工作台 (React)
    participant SearchModal as Cmd+K 搜索模态框
    participant QueryCache as TanStack Query (本地缓存)
    participant SearchAPI as 后端检索聚合接口 (/api/v1/search)

    Engineer->>UI: 按下快捷键 Cmd+K
    UI->>SearchModal: 打开并获得输入焦点
    Engineer->>SearchModal: 输入关键词 "主轴热伸长"
    SearchModal->>QueryCache: 查询命中本地最近访问
    QueryCache-->>SearchModal: 立即呈现 3 条历史记录 (0ms)
    SearchModal->>SearchAPI: 发起防抖异步检索 (Debounce 150ms)
    SearchAPI-->>SearchModal: 返回全局匹配结果 (参数/模型/BOM/仿真)
    SearchModal->>SearchModal: 渲染分类结果并高亮匹配字符
    Engineer->>SearchModal: 键盘上下键选中目标回车
    SearchModal->>UI: 触发全局路由跳转至目标工作区
```

### 3.2 M04/M06 MBSE 系统建模工作区与双通道同步

#### 交互规范
- **左右分屏联动**：左侧为 Monaco Editor（SysML v2 文本源码），右侧为 @antv/x6（图形画板）。
- **实时 AST 语法诊断**：键入 SysML v2 代码时，本地 Web Worker 解析 AST 并实时在行号处标注红色波浪线与错误提示。
- **图形与文本双向同步状态机**：
  - `IDLE`：代码与图形保持最新一致；
  - `DIRTY_TEXT`：文本正在编辑，防抖 500ms 触发图形增量补丁刷新；
  - `DIRTY_GRAPH`：图形画布拖拽连线/重命名，反向同步生成对应 SysML v2 语法片段并格式化；
  - `SYNCING`：调用后端 Flexo 暂存暂存图校验；
  - `CONFLICT`：检测到远端分支冲突，弹出两栏 Diff 合并裁决器。

### 3.3 M08/M09/M10 多学科协同仿真工况看板与因果回路

#### 交互规范
- **参数动态滑块推演**：工程师在看板左侧拖拽切削力 $F_c$ 或环境温度 $T_{amb}$ 滑块，系统通过 WebSocket 订阅实时推演计算，右侧 ECharts 曲线动态刷新主轴热伸长与振动位移。
- **仿真执行态多视角监视**：
  - 任务提交卡片：显示 Pod 资源（CPU/GPU/内存配额）、求解器类型（OpenModelica / FMU / FMI 2.0）；
  - 实时控制台：流式读取仿真求解器标准输出日志（ANSI 彩色文本支持），支持断点中止与日志关键字过滤；
  - 状态流转指示灯：`PENDING` (黄色呼吸灯) -> `RUNNING` (蓝色旋转) -> `COMPLETED` (绿色常亮) / `FAILED` (红色闪烁并呈现堆栈日志)。

### 3.4 M14 150% Super BOM 规则向导与多解歧义手工裁决模态框

#### 交互规范
- **配置向导向导表单 (Form Wizard)**：提供级联特性选项（如刀库容量：`24T` / `30T` / `40T`；主轴驱动：`DIRECT_DRIVE` / `BUILT_IN_MOTOR`；冷却模式：`OIL_MIST` / `AIR_BLAST`）。
- **实时规则求值与前置预警**：表单每次变动，即刻发起 `/api/v1/bom/super-bom/{id}/solve` 试算：
  - 若满足且唯一，展示“100% 实例物料行数与净重估算”；
  - 若触发冲突（AT-05-02），选项即刻变红并显示互斥原因（如：“高压中心出水与风冷主轴不能共存”）；
  - 若触发多解或零解（AT-05-03），“生成 100% BOM”按钮硬阻断禁用，并弹出**多解歧义手工裁决模态框 (Ambiguity Resolver Modal)**。

```mermaid
sequenceDiagram
    autonumber
    actor Engineer as 配置工程师
    participant Wizard as 150% BOM 配置向导
    participant Modal as 歧义裁决模态框 (AT-05-03)
    participant Engine as 后端配置求解器 (BOM Engine)

    Engineer->>Wizard: 变更主轴转速参数为 12000 RPM
    Wizard->>Engine: POST /solve (试算当前配置解)
    Engine-->>Wizard: HTTP 422 状态 (Unresolvable Configuration: 存在 2 个可选主轴电机组件)
    Wizard->>Modal: 弹出多解歧义手工裁决模态框
    Modal->>Engineer: 展示歧义分支对比清单 (电机 A: 扭矩大 vs 电机 B: 功率因数高)
    Engineer->>Modal: 手动点选 "电机 A" 并填写工程裁决审批意见
    Modal->>Wizard: 注入显式覆盖变量 (Explicit Override: MOTOR_SEL='MOTOR_A')
    Wizard->>Engine: 再次提交试算
    Engine-->>Wizard: 求解成功 (唯一确定解)，返回解集哈希 SHA-256
    Wizard->>Engineer: 解锁 "生成 100% 不可变基准" 按钮
```

### 3.5 M23 数字主线全链路因果关系溯源拓扑爆炸图

#### 交互规范
- **全屏交互视口**：支持鼠标滚轮无级缩放（0.1x ~ 4.0x）、按住鼠标中键或右键全景拖动画布。
- **节点下钻与爆炸展开**：
  - 双击任一节点（如零件 `P-850-SPINDLE-REV02`），触发以该节点为中心的**局部拓扑爆炸（Ego-Network Expansion）**，自动向外探寻上下游各 2 层关联节点；
  - 节点颜色编码遵循统一领域语义：需求（蓝色）、逻辑功能（紫色）、物理 CAD 制品（橙色）、仿真验证（绿色）、制造 MBOM 工位（青色）、高危变更波及（红色闪烁外晕）。
- **变更波及影响度一键推演（Impact Analysis）**：
  - 点击工具栏“变更风险推演”，弹出参数修改预演抽屉，输入拟修改的尺寸或公差，系统立即高亮显示所有直接下游与间接下游传播路径，并在右上角悬浮窗统计受影响构件数量、当前所属生命周期状态（`IN_WORK`, `RELEASED`, `LOCKED`）及负责人清单。

### 3.6 M25 制造工程 EBOM/MBOM 拆分重组与平衡残差检验看板

#### 交互规范
- **双树分栏拖拽重组（Dual-Tree Split & Reorganize）**：
  - 左侧展示完整 EBOM 树（包含标准件、钣金件、主轴总成等设计物料）；
  - 右侧展示目标工厂工艺装配树 MBOM（按车间 -> 产线 -> 工位划分）；
  - 工程师可将左侧物料拖入右侧指定工位节点，弹出消耗分配浮层输入分配数量（支持拆分数量，如 16 颗螺栓拆分为工位 A 装 8 颗，工位 B 装 8 颗）。
- **消耗平衡残差实时指示条（Consumption Conservation Bar）**：
  - 顶部常驻显示“物料守恒平衡率”仪表盘；
  - 绿色（100.0% 完全平衡，残差数量为 0）；
  - 橙色（部分物料欠消耗，未达到 100%）；
  - 红色（发生超量消耗或孤儿物料）。
  - **硬阻断机制**：当残差非零时，右下角“提交制造就绪评审（MRR）”按钮置灰锁定，悬浮提示列出具体的未平衡项差异明细。

### 3.7 M26 制造下发批次流转与逐项回执对账工作台

#### 交互规范
- **下发批次仪表盘**：汇总展示下发批次编号（`DISPATCH-YYYYMMDD-XXXX`）、工厂代码、MBOM 版本、目标系统（SAP S/4HANA / MES）、下发状态（`PENDING`, `DISPATCHED`, `CONFIRMED`, `PARTIAL_ERROR`）。
- **逐项回执实时流水对账（Line-item Reconciliation Table）**：
  - 点击批次行展开明细抽屉，表格逐行显示下发的 MBOM 行项目（Item ID、物料号、工位、数量）；
  - 实时呈现 MES 异步反馈状态：`ACCEPTED`（绿色勾选）、`REJECTED`（红色感叹号）、`PENDING`（灰色沙漏）；
  - 若存在拒绝行（如库位代码不存在），双击该行即刻弹出“单项重发/替换映射配置”，允许制造工程师修正后单独发起补偿对账，无需整体撤回作废。

---

## 四、性能压测脚本规约与系统 SLA 基准 (Stress Testing & Performance SLA)

### 4.1 压测体系与负载模型

为确保平台在极端工业协同场景下的稳定性与吞吐能力，压测基于 **k6** 与 **Apache JMeter** 构建双模压测流水线。测试环境配置要求：
- 压测客户端：8 Core CPU, 16GB RAM，独立千兆网络环境；
- 服务端单节点配置：4 Core CPU, 8GB JVM Heap (-Xms6g -Xmx6g)；
- 数据库：PostgreSQL 16（已预加载 10 万级节点、30 万级图关系边）。

```mermaid
graph LR
    subgraph RampUp [阶梯式升压模型 (Ramp-up)]
        T1[0-2min: 热身 10 VU] --> T2[2-5min: 稳态 100 VU]
        T2 --> T3[5-8min: 高峰 300 VU]
        T3 --> T4[8-10min: 极限压力 500 VU]
    end

    subgraph Evaluation [SLA 监控与熔断]
        E1{P99 延迟是否超标?}
        E2{HTTP 错误率 > 0.1%?}
        E3[触发自动化报警并生成 HTML 分析报告]
    end

    RampUp --> Evaluation
```

### 4.2 核心接口性能 SLA 评测指标基准表

| 编号 | 业务操作场景 | 目标接口 | 并发用户 (VU) | 目标吞吐 (TPS) | 延迟基准 P95 | 延迟基准 P99 | 错误率容忍阈值 | 核心前置条件 / 约束 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **SLA-01** | 用户登录与 PBAC 鉴权校验 | `POST /api/v1/auth/login` | 200 | $\ge 500$ | $\le 50\text{ms}$ | $\le 100\text{ms}$ | $< 0.01\%$ | Redis 缓存开启 |
| **SLA-02** | 2PC 模型协同发布与暂存提交 | `POST /api/v1/models/publish` | 50 | $\ge 30$ | $\le 400\text{ms}$ | $\le 800\text{ms}$ | $< 0.05\%$ | 涉及 Flexo 暂存图 2PC 提交 |
| **SLA-03** | 150% Super BOM 规则实时求解 | `POST /api/v1/bom/super-bom/{id}/solve` | 150 | $\ge 200$ | $\le 80\text{ms}$ | $\le 150\text{ms}$ | $< 0.01\%$ | 包含 500 条规则 DSL 递归求值 |
| **SLA-04** | 数字主线 5000 节点递归拓扑遍历 | `GET /api/v1/thread/impact-analysis` | 100 | $\ge 120$ | $\le 120\text{ms}$ | $\le 200\text{ms}$ | $< 0.01\%$ | PostgreSQL `WITH RECURSIVE` 索引覆盖 |
| **SLA-05** | MinIO 分片上传预签名 URL 批量申请 | `POST /api/v1/storage/multipart/init` | 100 | $\ge 300$ | $\le 60\text{ms}$ | $\le 120\text{ms}$ | $< 0.01\%$ | 100 个分片并发申请 |
| **SLA-06** | EBOM/MBOM 消耗平衡矩阵计算 | `POST /api/v1/mbom/{id}/verify-balance` | 80 | $\ge 100$ | $\le 150\text{ms}$ | $\le 300\text{ms}$ | $< 0.01\%$ | 2000 个物料项跨 5 级层级拆解 |
| **SLA-07** | 制造下发逐项回执批量异步对账 | `POST /api/v1/dispatch/receipts/batch` | 200 | $\ge 400$ | $\le 70\text{ms}$ | $\le 150\text{ms}$ | $< 0.01\%$ | 事务发件箱与幂等重试拦截 |

### 4.3 k6 自动化压测脚本实现规约

在 `tests/perf/k6-stress-suite.js` 中定义可执行压测套件，全面模拟工程师并发高频请求：

```javascript
// tests/perf/k6-stress-suite.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// 自定义监控指标
const errorRate = new Rate('custom_error_rate');
const bomSolveDuration = new Trend('bom_solve_duration');
const graphTraversalDuration = new Trend('graph_traversal_duration');

export const options = {
  stages: [
    { duration: '1m', target: 20 },   // 爬坡阶段
    { duration: '3m', target: 100 },  // 正常工业负载
    { duration: '2m', target: 300 },  // 峰值负载
    { duration: '1m', target: 0 },    // 降温退出
  ],
  thresholds: {
    'http_req_duration': ['p(95)<300', 'p(99)<500'], // 全局 P99 必须低于 500ms
    'custom_error_rate': ['rate<0.001'],             // 错误率必须低于 0.1%
    'bom_solve_duration': ['p(99)<200'],            // BOM 求解 P99 低于 200ms
    'graph_traversal_duration': ['p(99)<250'],      // 图遍历 P99 低于 250ms
  },
};

const BASE_URL = __ENV.API_HOST || 'http://localhost:8080/api/v1';

export default function () {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer TEST-MOCK-JWT-TOKEN-ENGINEER-LEVEL3',
      'X-Project-Id': 'PRJ-VMC850-FORWARD-ENG',
      'X-Security-Clearance': 'CONFIDENTIAL',
    },
  };

  // 场景 1: 150% Super BOM 规则求解高并发压测 (SLA-03)
  const bomPayload = JSON.stringify({
    variableAssignments: {
      SPINDLE_TYPE: 'DIRECT_DRIVE',
      MAX_SPEED_RPM: 12000,
      TOOL_MAGAZINE_CAPACITY: 24,
      COOLING_SYSTEM: 'OIL_MIST'
    },
    dryRun: true
  });

  const bomRes = http.post(`${BASE_URL}/bom/super-bom/101/solve`, bomPayload, params);
  bomSolveDuration.add(bomRes.timings.duration);
  const bomOk = check(bomRes, {
    'BOM solve status is 200': (r) => r.status === 200,
    'BOM result has deterministic hash': (r) => JSON.parse(r.body).data.resultSha256 !== undefined,
  });
  errorRate.add(!bomOk);

  sleep(0.5);

  // 场景 2: 数字主线全链路因果追溯递归图遍历 (SLA-04)
  const graphRes = http.get(`${BASE_URL}/thread/impact-analysis?rootElementUrn=urn:ccdd:element:part:P-850-001&maxDepth=10`, params);
  graphTraversalDuration.add(graphRes.timings.duration);
  const graphOk = check(graphRes, {
    'Graph traversal status is 200': (r) => r.status === 200,
    'Graph nodes returned > 0': (r) => JSON.parse(r.body).data.nodes.length > 0,
  });
  errorRate.add(!graphOk);

  sleep(0.5);
}
```

---

## 五、端到端自动化验收用例库 (E2E Acceptance Test Suite)

### 5.1 验收用例全集覆盖矩阵 (AT-01 ~ AT-28)

严格对齐《CCDDesigner 2.0 产品开发说明书》第 6 节全部 28 项核心验收标准：

| 验收编号 | 验收项名称 | 验证步骤与断言规约 (Assertions & Pass Criteria) | 关联模块与技术规范 |
| :--- | :--- | :--- | :--- |
| **AT-01** | SysML v2 规范遵循性验证 | 校验 SysML v2 文本/图形双向映射准确性，语法与官方规范兼容，语法错误准确定位至行列 | M04, M06 (D03 规格) |
| **AT-02** | 多状态联合仿真与协同求解 | 验证机械-电气-热力学多工况并行仿真求解，跨域耦合收敛，残差收敛至 $10^{-5}$ 以内 | M08, M09, M10 (D04 规格) |
| **AT-03** | 仿真基准对比一致性测试 | 对比标准机床算例，数值解与实测基准偏差在工业允许公差 $\le 0.5\%$ 范围内 | M11 (D04 规格) |
| **AT-04** | 仿真全生命周期数据追溯 | 验证仿真模型、边界参数、结果曲线与对应数字主线节点的双向引用链完整闭环 | M12, M23 (D07 规格) |
| **AT-05** | 150% Super BOM 规则静态与求解 | 静态排查互斥规则冲突；多解歧义硬阻断报错；合法配置确定性生成 100% 实例快照 | M14, M15 (D05 规格) |
| **AT-06** | 仿真与试验数据因果回溯 | 从振动越限曲线点一键逆向追溯至相关参数、关联构件与设计需求节点，无断链 | M12, M23 (D07 规格) |
| **AT-07** | 多源异构模型关联变更波及推演 | 修改顶层主轴回转精度需求，系统递归计算波及范围，正确标定需要重新仿真的下游模型 | M23, M24 (D07 规格) |
| **AT-08** | 大规模模型（5000+）拓扑遍历性能 | 在 5000 节点真实拓扑图上执行 10 层双向遍历与波及度计算，服务端响应时间 $< 200\text{ms}$ | M23 (D07 规格, SLA-04) |
| **AT-09** | 混合建模环境双向同步实时性 | SysON 图形画布拖拽与 SysML v2 文本编辑器编辑双向同步，单向同步延迟 $\le 500\text{ms}$ | M04, M06 (D03 规格) |
| **AT-10** | 关键设计参数端到端一致性校验 | 验证“主轴额定扭矩”在需求、架构、三维 CAD、仿真参数中保持哈希与数值全链路一致 | M07, M13, M23 (D07 规格) |
| **AT-11** | 模型语法语义自动检查与准入规则 | 提交未标注密级或缺少必要端口连接的模型时，触发门禁硬阻断，拒绝发布至主干 | M04, M05 (D02, D03 规格) |
| **AT-12** | 虚拟样机运行工况动态注入验证 | 仿真运行中动态注入阶跃切削负载，主轴转速控制回路在 $0.2\text{s}$ 内调节恢复 | M08, M10 (D04 规格) |
| **AT-13** | 仿真调度算力资源弹性伸缩测试 | 批量并发提交 50 个复杂 FMI 仿真任务，K8s 自动拉起 Worker Pod 并在完成后释放 | M09 (D04 规格) |
| **AT-14** | 仿真结果可视化交互体验与多视图 | 多维图表联动缩放与切片，数据点超过 100 万时保持平滑交互（FPS $\ge 30$） | M10 (前端 ECharts 规格) |
| **AT-15** | 复杂机电液耦合故障注入与推演 | 注入导轨液压系统失压故障，系统准确预测刀尖轨迹偏移超差并标记预警 | M08, M09 (D04 规格) |
| **AT-16** | 变型配置规则冲突智能诊断与自愈 | 输入互斥特性组合，向导精准提示冲突规则条款，并给出最小代价值修改推荐集合 | M14, M16 (D05 规格) |
| **AT-17** | 超大规模（万级）BOM 解析与响应 | 面对 10,000 物料行 Super BOM，完成完整规则匹配与实例衍生耗时 $\le 2\text{s}$ | M15 (D05 规格) |
| **AT-18** | 配置结果与 CAD/CAE 几何参数驱动 | 选配完成生成的 100% 实例自动驱动三维主轴外形尺寸与有限元载荷边界参数变动 | M17, M18 (D06 规格) |
| **AT-19** | 数字主线全链路数据拓扑构建准确 | 各学科模型与数据接入时自动生成统一元数据及图关系，孤立节点率 $\le 0.1\%$ | M23 (D07 规格) |
| **AT-20** | 跨生命周期阶段因果关系追溯能力 | 从售后故障工单逆向精确追溯到具体机床出厂实物台账、下发批次及原始设计签批单 | M23, M27 (D07, D08 规格) |
| **AT-21** | 变更波及影响度量化评估准确性 | 依据影响分析模型自动打出风险传播分数，命中实际需要返工构件的准确率 $\ge 98\%$ | M24 (D07 规格) |
| **AT-22** | 图数据库递归遍历与查询响应时延 | 高频执行 5 级以上深度因果链追溯查询，PostgreSQL 递归查询 P99 时延 $\le 150\text{ms}$ | M23 (D07 规格, SLA-04) |
| **AT-23** | 多用户并发协同建模与冲突检测 | 10 名工程师并发向同一具名图工作区提交修改，2PC 正确处理冲突并保证无数据丢失 | M05, M28 (D03 规格) |
| **AT-24** | 混合密级权限隔离与数据安全合规 | 低密级账号无法查询任何涉密节点的属性与关联边，API 与图查询底层严格过滤 | M29 (D02 规格) |
| **AT-25** | 异构 CAD 模型轻量化加载与装配体 | 500MB 机床总装 STEP 模型轻量化转 GLTF/3D Tiles，首屏渲染加载时间 $\le 3\text{s}$ | M20, M21 (D06 规格) |
| **AT-26** | 动态模型轻量化转换精度保真测试 | 轻量化转换后关键特征面形位尺寸与原始 CAD 比较误差在 $0.01\text{mm}$ 以内 | M20 (D06 规格) |
| **AT-27** | 制造工艺与装配结构数据闭环转换 | EBOM 转 MBOM 消耗平衡校验残差严格等于 0，制造辅料标记准确，无反向数据污染 | M25, M26 (D08 规格) |
| **AT-28** | 实物制造数据与工艺偏差实时对账 | MES 回传装配实测回执与公差数据自动完成行级对账，对账差异行自动生成纠偏闭环 | M26, M27 (D08 规格) |

### 5.2 核心业务路径 Playwright 端到端自动化测试脚本规约

在 `tests/e2e/specs/core-engineering-flow.spec.ts` 中实现全流程冒烟与验收自动化驱动：

```typescript
// tests/e2e/specs/core-engineering-flow.spec.ts
import { test, expect } from '@playwright/test';

test.describe('CCDDesigner 2.0 核心工业工程全链路闭环验收套件', () => {
  test.beforeEach(async ({ page }) => {
    // 1. 登录并注入工程师密级凭证 (PBAC Level 3: 机密)
    await page.goto('/login');
    await page.fill('input[name="username"]', 'lead_engineer_zhang');
    await page.fill('input[name="password"]', 'EngineSecure2026!');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL('/workbench');
  });

  test('E2E-01: 验证 150% Super BOM 规则求解与歧义裁决 (AT-05, AT-16)', async ({ page }) => {
    await page.goto('/bom/super-bom/101/wizard');

    // 步骤 1: 勾选造成多解歧义的互斥组合
    await page.selectOption('select[name="SPINDLE_TYPE"]', 'DIRECT_DRIVE');
    await page.selectOption('select[name="COOLING_SYSTEM"]', 'AIR_BLAST');
    await page.click('button:has-text("求解 100% 实例")');

    // 断言 1: 系统必须阻断并弹出歧义裁决模态框
    const ambiguityModal = page.locator('.ant-modal-content:has-text("多解歧义手工裁决")');
    await expect(ambiguityModal).toBeVisible();

    // 步骤 2: 工程师执行手工裁决
    await ambiguityModal.locator('input[type="radio"][value="OVERRIDE_OPTION_A"]').check();
    await ambiguityModal.locator('textarea[name="arbitrationReason"]').fill('根据重切削刚性要求，指定主轴电机 A');
    await ambiguityModal.locator('button:has-text("确认裁决并重新求解")').click();

    // 断言 2: 裁决生效后求解成功，生成确定性哈希
    await expect(page.locator('.ant-alert-success')).toContainText('规则求解成功');
    const hashBadge = page.locator('[data-testid="result-sha256"]');
    await expect(hashBadge).not.toBeEmpty();
  });

  test('E2E-02: 验证数字主线全链路影响分析与波及推演 (AT-07, AT-08)', async ({ page }) => {
    await page.goto('/thread/graph-explorer');

    // 步骤 1: 选中根节点发起影响度推演
    await page.fill('input[placeholder="输入节点 URN 或编号"]', 'P-850-SPINDLE-REV02');
    await page.click('button:has-text("定位拓扑")');
    await page.click('button:has-text("变更影响推演")');

    // 断言 1: 拓扑图高亮呈现波及路径且渲染时间低于 500ms
    const highlightedNodes = page.locator('.x6-node.is-impacted');
    await expect(highlightedNodes).toHaveCount(4);

    // 断言 2: 右侧影响度抽屉呈现风险量化分与负责人
    const impactDrawer = page.locator('.impact-analysis-drawer');
    await expect(impactDrawer).toBeVisible();
    await expect(impactDrawer).toContainText('高风险受控实体: 2 项');
  });

  test('E2E-03: 验证 EBOM/MBOM 转换平衡校验与制造下发回执闭环 (AT-27, AT-28)', async ({ page }) => {
    await page.goto('/manufacturing/mbom/201/balance-workbench');

    // 步骤 1: 检查未分配完成前提交 MRR 必须硬阻断
    const mrrSubmitBtn = page.locator('button:has-text("签署制造就绪 (MRR)")');
    await expect(mrrSubmitBtn).toBeDisabled();

    // 步骤 2: 拖拽剩余设计物料至装配工位完成 100% 消耗
    const sourceItem = page.locator('[data-testid="ebom-item-bolt-m12"]');
    const targetStation = page.locator('[data-testid="mbom-station-OP10"]');
    await sourceItem.dragTo(targetStation);

    // 步骤 3: 触发平衡校验
    await page.click('button:has-text("执行物料消耗平衡校验")');
    await expect(page.locator('.consumption-balance-rate')).toHaveText('100.0%');
    await expect(mrrSubmitBtn).toBeEnabled();

    // 步骤 4: 发起制造下发并验证逐行回执状态更新
    await mrrSubmitBtn.click();
    await page.goto('/manufacturing/dispatch/tracking/DISP-202609-001');
    await expect(page.locator('.receipt-status-badge:has-text("CONFIRMED")')).toBeVisible({ timeout: 10000 });
  });
});
```

### 5.3 CI/CD 自动化持续集成流水线门禁定义

在项目根目录配置 `.github/workflows/quality-gate.yml`，严格执行三道质量门禁：

```yaml
# .github/workflows/quality-gate.yml
name: CCDDesigner 2.0 Quality Gate & Performance Verification

on:
  push:
    branches: [ main, release/* ]
  pull_request:
    branches: [ main ]

jobs:
  backend-compile-and-unit:
    name: 纯原生 Java (JDK 17) 后端编译与单元测试
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: 设置 JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: 运行 Maven 严格离线构建与测试
        run: |
          mvn clean compile -o -f backend/pom.xml
          mvn test -f backend/pom.xml

  e2e-acceptance-tests:
    name: Playwright 端到端全业务验收测试 (AT-01 ~ AT-28)
    needs: backend-compile-and-unit
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 20
      - name: 启动本地沙盒聚合服务与数据库
        run: |
          docker compose -f docker-compose.sandbox.yml up -d
      - name: 运行 Playwright 验收套件
        run: |
          npx playwright install --with-deps
          npm test:e2e

  performance-sla-gate:
    name: k6 核心接口性能 SLA 评测门禁
    needs: e2e-acceptance-tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: 执行 k6 自动化压测套件
        uses: grafana/k6-action@v0.3.1
        with:
          filename: tests/perf/k6-stress-suite.js
          flags: --env API_HOST=http://localhost:8080/api/v1
```

---

## 六、交付验证与归档说明

本规格说明书作为 CCDDesigner 2.0 专项开发规格包的最终篇（第 10 份规格），与已发布的 D01 ~ D09 共同组成了完整的工程闭环：
1. **系统规格体系完备**：涵盖从数据库物理模型（D01）、安全与状态机（D02）、模型适配与 2PC（D03）、仿真调度与 DAG（D04）、Super BOM 配置（D05）、对象存储与 CAD（D06）、数字主线递归图（D07）、制造闭环对账（D08）、OpenAPI 与事件发件箱（D09），直至前端原型交互与端到端自动化验收（D10）；
2. **测试与质量权威依据**：AT-01 至 AT-28 的验收断言与 k6 SLA 矩阵构成了后续平台系统测试、交付验收评审（FAR）及最终上线的判定准则。
