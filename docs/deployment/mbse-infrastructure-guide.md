# CCDDesigner 2.0 - MBSE 核心基础设施部署与运维手册 (SysON, OpenSysML, Flexo MMS)

## 1. 概述与各组件部署规范

根据《高端数控机床正向设计云平台 (CCDDesigner 2.0)》产品与模块开发详细规格说明书，系统依托三大开源与官方标准化基础设施构成完整的 MBSE 领域闭环：

| 核心组件 | 核心职责 | 部署形态 | 存储依赖 | 扩缩容策略 | 规划端口 |
|---|---|---|---|---|---|
| **SysON** | 图形化建模创作 (Authoring) | 容器化 Spring Boot 后端 + Web 静态视口前端 | 独立 Postgres 实例 (`syson_db`) | 按项目/租户垂直隔离，必要时分片 | 8085 (Web/API), 5434 (DB) |
| **OpenSysML** | 形式化语义解释与诊断 (Semantic Processing) | `sysml-grpc` 容器，纯无状态计算服务 | 无存储依赖（纯校验计算型） | 按请求量水平扩展 (`--scale opensysml=N`) | 50051 (gRPC), 8086 (HTTP/REST) |
| **Flexo MMS** | SysML v2 模型服务仓库与联邦 (Model Hub) | Docker Compose 官方部署栈 (Layer 1) | RDF 四元组存储 (Apache Jena Fuseki) | 按 Org/Repo 分片，支持只读/读写分离 | 8088 (MMS Layer 1), 3030 (SPARQL) |
| **MBSE Gateway** | 统一反向代理网关与动态负载分发 | 高性能 Nginx 容器 | 无 | 支持多微服务路由与无状态多副本轮询分发 | 8084 (统一网关入口) |

> [!IMPORTANT]
> **设计铁律与部署原则**：
> 1. **四态彻底解耦**：工作模型（Working Model）存在于 SysON/OpenSysML 临时草稿中；已发布模型（Published Model）固化写入 Flexo MMS RDF 四元组；PLM 修订版本与基线（PLM Revision & Baseline）由 CCDDesigner Core 统一纳管。
> 2. **存储严格隔离**：SysON 必须拥有独立的 Postgres 数据库（端口 5434），严禁与 PLM 业务核心主库混合建表，实现不同研制项目和租户的数据垂直物理隔离。
> 3. **OpenSysML 无状态与弹性伸缩**：`sysml-grpc` 不挂接任何数据库或磁盘持久卷，支持按需启动任意数量容器副本，由前置 Nginx 统一调度。

---

## 2. 纯容器化部署拓扑与网络架构

```
                             [ 客户端浏览器 / 工程师工作台 ]
                                           │
                                           ▼
                      ┌────────────────────────────────────────┐
                      │    MBSE 统一网关 (Nginx 端口: 8084)     │
                      └────┬───────────────┬───────────────┬───┘
                           │               │               │
                 /syson/*  │   /opensysml/*│       /flexo/*│
                 :8085     │   :8086/:50051│       :8088   │
                           ▼               ▼               ▼
                  ┌────────────────┐┌─────────────┐┌───────────────┐
                  │  SysON Server  ││  OpenSysML  ││   Flexo MMS   │
                  │ (Spring Boot + ││ (sysml-grpc ││ (Layer 1 栈  │
                  │  Web 前端视口) ││ 无状态服务) ││  模型仓库服务)│
                  └───────┬────────┘└──────┬──────┘└───────┬───────┘
                          │ 垂直隔离       │ 无状态        │ Org/Repo 分片
                          ▼                ▼ 容器水平扩容  ▼ 读写分离
                  ┌────────────────┐┌─────────────┐┌───────────────┐
                  │ 独立 Postgres  ││ OpenSysML#2 ││  Jena Fuseki  │
                  │   (syson_db)   ││ OpenSysML#3 ││(SPARQL 1.1   │
                  │   端口: 5434   ││ ...弹性伸缩 ││ 端口: 3030)  │
                  └────────────────┘└─────────────┘└───────────────┘
```

---

## 3. 部署目录与文件组织

部署工程统一纳管于平台根目录下：

```
deploy/
├── docker-compose/
│   ├── docker-compose.yml             # 纯容器化一键部署核心编排文件
│   ├── .env.example                   # 环境变量模板
│   ├── .env                           # 运行实例环境配置
│   ├── syson/
│   │   ├── Dockerfile                 # SysON 运行时镜像构建定义
│   │   ├── mock_syson_server.py       # SysON 容器服务运行时
│   │   ├── application-syson.yml      # Spring Boot 独立数据源配置
│   │   └── init-syson-db.sql          # 独立 Postgres 实例初始化脚本
│   ├── opensysml/
│   │   ├── Dockerfile                 # sysml-grpc 无状态计算镜像定义
│   │   ├── mock_sysml_grpc_server.py  # 无状态语言服务与健康探针实现
│   │   └── server-config.json         # 语言服务运行时参数
│   ├── flexo-mms/
│   │   ├── Dockerfile                 # Flexo MMS Layer 1 部署镜像定义
│   │   ├── mock_flexo_mms_server.py   # Flexo MMS 模型服务实现
│   │   ├── application.conf           # Flexo MMS 官方服务端配置
│   │   └── fuseki-config.ttl          # Apache Jena Fuseki 四元组存储与具名图配置
│   └── gateway/
│       └── nginx.conf                 # 统一反向代理网关与负载均衡配置
└── scripts/
    ├── deploy-local.sh                # 一键部署与弹性伸缩控制脚本
    ├── verify-deployment.sh           # 全链路端到端健康探测与核验脚本
    └── clean-data.sh                  # 数据卷安全清理与重置脚本
```

---

## 4. 快速上手操作指南

### 4.1 一键启动全部 MBSE 组件

在终端中执行部署脚本：

```bash
# 赋予执行权限
chmod +x deploy/scripts/*.sh

# 启动全量基础服务 (默认单实例)
./deploy/scripts/deploy-local.sh up
```

### 4.2 无状态 OpenSysML 水平弹性扩展

当正向设计系统模型复杂度激增、并发语法与语义校验任务频繁时，可直接通过水平伸缩命令将 OpenSysML 扩展为多个无状态计算副本：

```bash
# 水平扩展为 3 个副本 (Nginx 自动轮询分发)
./deploy/scripts/deploy-local.sh up --scale 3
```

此时系统将自动拉起 3 个无状态 `opensysml` 容器实例，网关 `mbse-gateway` 依据内嵌 DNS 自动实现轮询负载均衡，无需停机或修改任何配置。

### 4.3 全链路健康检查与探针核验

执行自动化探针核验脚本，校验各组件连通性与服务状态：

```bash
./deploy/scripts/deploy-local.sh verify
# 或直接运行
./deploy/scripts/verify-deployment.sh
```

输出示例：
```text
======================================================================
  CCDDesigner 2.0 - MBSE 基础组件端到端健康检查与规范核验
======================================================================

--- 1. SysON 建模创作环境规范核验 ---
  [形态]: 容器化 Spring Boot + Web 视口静态资源 | [存储]: 独立 Postgres
• 正在探测 [SysON Server 健康探针] (http://localhost:8085/health)... [正常通过]
• 正在探测 [SysON Web 视口加载] (http://localhost:8085/workspaces/...)... [正常通过]
• 正在检测端口连通性 [SysON 独立 PostgreSQL 存储实例] (localhost:5434)... [正常通过]

--- 2. OpenSysML 语义诊断计算服务规范核验 ---
  [形态]: sysml-grpc 容器，无状态 | [扩缩容]: 支持水平扩展
• 正在探测 [OpenSysML 无状态计算探针] (http://localhost:8086/health)... [正常通过]
• 正在检测端口连通性 [OpenSysML gRPC 语言服务接口] (localhost:50051)... [正常通过]

--- 3. Flexo MMS 模型仓库与 RDF 存储规范核验 ---
  [形态]: Docker Compose 官方部署栈 | [存储]: Apache Jena Fuseki 四元组
• 正在探测 [Flexo MMS Layer 1 模型服务] (http://localhost:8088/health)... [正常通过]
• 正在探测 [Apache Jena Fuseki SPARQL 引擎] (http://localhost:3030/$/ping)... [正常通过]

--- 4. MBSE Gateway 统一网关与多副本负载分发 ---
• 正在探测 [MBSE 统一网关综合探针] (http://localhost:8084/health)... [正常通过]
• 正在探测 [网关路由: SysON 代理] (http://localhost:8084/syson/health)... [正常通过]
• 正在探测 [网关路由: OpenSysML 负载分发] (http://localhost:8084/opensysml/health)... [正常通过]
• 正在探测 [网关路由: Flexo MMS 代理] (http://localhost:8084/flexo/health)... [正常通过]

======================================================================
🎉 所有 MBSE 基础组件容器化部署与规范核验 100% 达标！
======================================================================
```

### 4.4 服务停止与重置

```bash
# 停止容器运行 (保留数据卷)
./deploy/scripts/deploy-local.sh down

# 安全清理并重置全部持久化数据卷 (SysON DB 与 Fuseki RDF)
./deploy/scripts/clean-data.sh
```

---

## 5. 存储依赖与分片运维策略

### 5.1 SysON 独立 Postgres (垂直物理隔离)
- **数据库名**：`syson_workspace_db`
- **监听端口**：`5434` (容器映射)
- **多租户策略**：支持不同研制团队分配独立的 PostgreSQL Schema 或分片数据库实例，保障工程草稿的物理安全性。
- **备份命令**：
  ```bash
  docker exec -t ccdd-syson-postgres pg_dump -U syson_admin -d syson_workspace_db > backup_syson_$(date +%F).sql
  ```

### 5.2 Flexo MMS Jena Fuseki RDF 四元组存储 (读写分离与分片)
- **存储后端**：Apache Jena Fuseki TDB2
- **只读端点**：`http://localhost:3030/ccdd-models/query` (支持部署只读副本，向分析工具与仿真引擎提供高并发查询)
- **写端点 (SPARQL Update)**：`http://localhost:3030/ccdd-models/update`
- **具名图分片机制**：
  - 候选暂存图：`urn:ccdd:staging:release:{releaseId}` (独立隔离暂存，失败一键物理 DROP)
  - 生产发布图：`urn:ccdd:production:models` (两阶段提交审批通过后原子合入)

---

## 6. 故障排查与运维 Checklist

| 故障现象 | 可能原因 | 推荐排查与修复手段 |
|---|---|---|
| SysON 启动失败提示 DB 连接超时 | Postgres 容器未就绪或端口冲突 | 运行 `docker compose ps` 查看 `ccdd-syson-postgres` 状态；检查宿主机 5434 端口是否被占用 |
| OpenSysML 校验请求超时 | 计算密集型大模型导致工作队列占满 | 执行 `./deploy/scripts/deploy-local.sh up --scale 4` 增加无状态计算副本 |
| Flexo MMS 报 404 数据集不存在 | Fuseki 配置未挂载成功 | 检查 `deploy/docker-compose/flexo-mms/fuseki-config.ttl` 是否正确映射至 `/fuseki/configuration/ccdd-models.ttl` |
| 前端视口报网关 502 Bad Gateway | 后端微服务启动中或异常退出 | 运行 `./deploy/scripts/deploy-local.sh logs mbse-gateway` 观察 upstream 转发日志 |
