package com.ccdd.thread.repository;

import com.ccdd.thread.dto.TraversePathStep;
import com.ccdd.thread.entity.ThreadNode;
import com.ccdd.thread.entity.ThreadRelation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 数字主线图谱 PostgreSQL CTE 递归仓储 (落实 D07 专项规格与 ADR-0010)
 * 基于 WITH RECURSIVE 实现毫秒级跨领域依赖追踪与环路防御
 */
@Repository
public class ThreadGraphRepository {

    private final JdbcTemplate jdbcTemplate;

    public ThreadGraphRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertNode(ThreadNode node) {
        String sql = """
            INSERT INTO ccdd_thread_nodes (
                node_id, tenant_id, domain_type, entity_id, display_name,
                version, lifecycle_state, attributes_json, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?)
            ON CONFLICT (node_id) DO UPDATE 
            SET display_name = EXCLUDED.display_name,
                version = EXCLUDED.version,
                lifecycle_state = EXCLUDED.lifecycle_state,
                attributes_json = EXCLUDED.attributes_json
        """;
        jdbcTemplate.update(sql,
                node.getNodeId(),
                node.getTenantId(),
                node.getDomainType(),
                node.getEntityId(),
                node.getDisplayName(),
                node.getVersion(),
                node.getLifecycleState(),
                node.getAttributesJson() == null ? "{}" : node.getAttributesJson(),
                node.getCreatedAt() != null ? Timestamp.from(node.getCreatedAt()) : new Timestamp(System.currentTimeMillis())
        );
    }

    public void insertRelation(ThreadRelation relation) {
        String sql = """
            INSERT INTO ccdd_thread_relations (
                relation_id, tenant_id, source_node_id, target_node_id, relation_type,
                is_bidirectional, is_baseline_locked, baseline_id, attributes_json, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?)
            ON CONFLICT (relation_id) DO NOTHING
        """;
        jdbcTemplate.update(sql,
                relation.getRelationId(),
                relation.getTenantId(),
                relation.getSourceNodeId(),
                relation.getTargetNodeId(),
                relation.getRelationType(),
                relation.getIsBidirectional() != null ? relation.getIsBidirectional() : false,
                relation.getIsBaselineLocked() != null ? relation.getIsBaselineLocked() : false,
                relation.getBaselineId(),
                relation.getAttributesJson() == null ? "{}" : relation.getAttributesJson(),
                relation.getCreatedAt() != null ? Timestamp.from(relation.getCreatedAt()) : new Timestamp(System.currentTimeMillis())
        );
    }

    /**
     * 正向递归遍历下游受影响链路 (Downstream Impact Analysis)
     */
    public List<TraversePathStep> traverseDownstream(String startNodeId, int maxDepth, boolean excludeBaselineLocked) {
        String baselineFilter = excludeBaselineLocked ? "AND r.is_baseline_locked = FALSE" : "";
        String sql = """
            WITH RECURSIVE downstream_cte AS (
                -- 锚点成员 (初始起点)
                SELECT 
                    n.node_id,
                    n.domain_type,
                    n.display_name,
                    n.version,
                    n.lifecycle_state,
                    CAST('ROOT' AS VARCHAR) AS relation_type,
                    0 AS depth,
                    ARRAY[n.node_id] AS path,
                    FALSE AS cycle_detected
                FROM ccdd_thread_nodes n
                WHERE n.node_id = ?

                UNION ALL

                -- 递归成员 (沿有向边向下探索)
                SELECT 
                    next_n.node_id,
                    next_n.domain_type,
                    next_n.display_name,
                    next_n.version,
                    next_n.lifecycle_state,
                    r.relation_type,
                    cte.depth + 1 AS depth,
                    cte.path || next_n.node_id AS path,
                    next_n.node_id = ANY(cte.path) AS cycle_detected
                FROM downstream_cte cte
                JOIN ccdd_thread_relations r ON cte.node_id = r.source_node_id """ + baselineFilter + """
                JOIN ccdd_thread_nodes next_n ON r.target_node_id = next_n.node_id
                WHERE cte.depth < ? 
                  AND NOT cte.cycle_detected
            )
            SELECT 
                node_id, domain_type, display_name, version, lifecycle_state,
                relation_type, depth, array_to_string(path, ' -> ') AS path_str, cycle_detected
            FROM downstream_cte
            ORDER BY depth, node_id;
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> TraversePathStep.builder()
                .nodeId(rs.getString("node_id"))
                .domainType(rs.getString("domain_type"))
                .displayName(rs.getString("display_name"))
                .version(rs.getString("version"))
                .lifecycleState(rs.getString("lifecycle_state"))
                .relationType(rs.getString("relation_type"))
                .depth(rs.getInt("depth"))
                .path(rs.getString("path_str"))
                .cycleDetected(rs.getBoolean("cycle_detected"))
                .build(),
                startNodeId, maxDepth
        );
    }

    /**
     * 反向递归追溯上游根因链路 (Upstream Traceability)
     */
    public List<TraversePathStep> traverseUpstream(String targetNodeId, int maxDepth) {
        String sql = """
            WITH RECURSIVE upstream_cte AS (
                -- 锚点成员 (初始起点)
                SELECT 
                    n.node_id,
                    n.domain_type,
                    n.display_name,
                    n.version,
                    n.lifecycle_state,
                    CAST('ROOT' AS VARCHAR) AS relation_type,
                    0 AS depth,
                    ARRAY[n.node_id] AS path,
                    FALSE AS cycle_detected
                FROM ccdd_thread_nodes n
                WHERE n.node_id = ?

                UNION ALL

                -- 递归成员 (逆向沿着 target_node_id 向上追溯)
                SELECT 
                    prev_n.node_id,
                    prev_n.domain_type,
                    prev_n.display_name,
                    prev_n.version,
                    prev_n.lifecycle_state,
                    r.relation_type,
                    cte.depth + 1 AS depth,
                    cte.path || prev_n.node_id AS path,
                    prev_n.node_id = ANY(cte.path) AS cycle_detected
                FROM upstream_cte cte
                JOIN ccdd_thread_relations r ON cte.node_id = r.target_node_id
                JOIN ccdd_thread_nodes prev_n ON r.source_node_id = prev_n.node_id
                WHERE cte.depth < ? 
                  AND NOT cte.cycle_detected
            )
            SELECT 
                node_id, domain_type, display_name, version, lifecycle_state,
                relation_type, depth, array_to_string(path, ' <- ') AS path_str, cycle_detected
            FROM upstream_cte
            ORDER BY depth, node_id;
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> TraversePathStep.builder()
                .nodeId(rs.getString("node_id"))
                .domainType(rs.getString("domain_type"))
                .displayName(rs.getString("display_name"))
                .version(rs.getString("version"))
                .lifecycleState(rs.getString("lifecycle_state"))
                .relationType(rs.getString("relation_type"))
                .depth(rs.getInt("depth"))
                .path(rs.getString("path_str"))
                .cycleDetected(rs.getBoolean("cycle_detected"))
                .build(),
                targetNodeId, maxDepth
        );
    }
}
