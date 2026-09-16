package com.ccdd.thread.service;

import com.ccdd.thread.dto.ImpactAnalysisResult;
import com.ccdd.thread.dto.TraversePathStep;
import com.ccdd.thread.entity.ThreadNode;
import com.ccdd.thread.entity.ThreadRelation;
import com.ccdd.thread.repository.ThreadGraphRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 数字主线图服务 (落实 D07 专项规格)
 * 支持正反向多跳追溯与工程变更波及推演分析
 */
@Service
public class ThreadGraphService {

    private static final Logger log = LoggerFactory.getLogger(ThreadGraphService.class);

    private final ThreadGraphRepository graphRepository;

    public ThreadGraphService(ThreadGraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    @Transactional
    public void registerNode(ThreadNode node) {
        graphRepository.insertNode(node);
    }

    @Transactional
    public void registerRelation(ThreadRelation relation) {
        graphRepository.insertRelation(relation);
    }

    /**
     * 数字主线图遍历追溯
     * @param startNodeId 起点节点标识
     * @param direction 遍历方向: DOWNSTREAM (正向影响), UPSTREAM (逆向溯源)
     * @param maxDepth 最大探索深度 (默认 5)
     */
    public List<TraversePathStep> traverse(String startNodeId, String direction, int maxDepth) {
        log.info("[ThreadGraph] 执行链路遍历: 起点={}, 方向={}, 深度={}", startNodeId, direction, maxDepth);
        if ("UPSTREAM".equalsIgnoreCase(direction)) {
            return graphRepository.traverseUpstream(startNodeId, maxDepth);
        } else {
            return graphRepository.traverseDownstream(startNodeId, maxDepth, false);
        }
    }

    /**
     * 变更波及推演评估 (Impact Analysis)
     */
    public ImpactAnalysisResult analyzeImpact(String rootNodeId, int maxDepth) {
        log.info("[ThreadGraph] 启动变更波及推演分析: rootNodeId={}", rootNodeId);

        List<TraversePathStep> steps = graphRepository.traverseDownstream(rootNodeId, maxDepth, false);

        int directCount = 0;
        int indirectCount = 0;
        List<String> baselineIds = new ArrayList<>();

        for (TraversePathStep step : steps) {
            if (step.getDepth() == 1) {
                directCount++;
            } else if (step.getDepth() > 1) {
                indirectCount++;
            }
        }

        int totalImpacted = directCount + indirectCount;

        // 计算风险等级
        String riskLevel = "LOW";
        if (totalImpacted > 20 || !baselineIds.isEmpty()) {
            riskLevel = "CRITICAL";
        } else if (totalImpacted > 10) {
            riskLevel = "HIGH";
        } else if (totalImpacted > 3) {
            riskLevel = "MEDIUM";
        }

        return ImpactAnalysisResult.builder()
                .rootNodeId(rootNodeId)
                .totalImpactedNodes(totalImpacted)
                .directImpactedCount(directCount)
                .indirectImpactedCount(indirectCount)
                .impactedBaselineIds(baselineIds)
                .riskLevel(riskLevel)
                .impactPaths(steps)
                .build();
    }
}
