# ADR-0010: 采用 PostgreSQL 承载数字主线图关系与递归查询

## 状态
已接受 (Accepted)

## 上下文
CCDDesigner 2.0 中的 M23（数字主线与关系服务）负责管理需求、模型元素、参数、BOM 零部件、验证证据及实物之间的跨域追溯关系，并支撑变更影响面推演。根据上位产品开发说明书，系统**严禁采用 Neo4j**。同时业务性能 SLA 要求在 10 万节点、100 万关系的图数据中，深度 $\le 5$ 层、返回 $\le 1000$ 节点的影响面路径遍历 P95 $\le 5.0\text{ s}$。

## 决策
选用 **PostgreSQL 标准关系表 + 递归 CTE（`WITH RECURSIVE`）** 方案落地数字主线图数据存储与拓扑遍历引擎：
1. **关系边表物理结构**：设计 `trace_link_revisions` 边表，记录源节点、目标节点、关系类型（如 `satisfies`, `verifies`, `allocatedTo`）、生效版本与配置上下文。
2. **复合双向索引**：建立 `(tenant_id, source_type, source_id, relation_type)` 与 `(tenant_id, target_type, target_id, relation_type)` 的正反向 B-Tree 索引，保障单跳查询毫秒级响应。
3. **受控递归遍历与环路熔断**：通过 `WITH RECURSIVE` 遍历，SQL 内部包含路径数组 `path_ids = array_append(path_ids, current_id)`，出现 `current_id = ANY(path_ids)` 时自动终止环路；限制最大层数 `depth <= :maxDepth`（默认 5）。
4. **截断与告警标头**：当命中最大深度或结果节点数超过上限（1000）时，查询引擎显式设置 `is_truncated = true` 并附带告警信息，严禁静默吞掉截断事实。

## 影响
- **正面**：无需引入和运维专门的图数据库或实验性插件，利用主关系库的 ACID 事务特性，数字主线关系与业务对象状态保持同一事务一致性。
- **负面**：对于超过 10 层的超深层次图遍历并非最优解，但完全覆盖了本平台工程场景（$\le 5$ 层）的业务需求。
