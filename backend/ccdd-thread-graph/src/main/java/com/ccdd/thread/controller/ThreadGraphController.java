package com.ccdd.thread.controller;

import com.ccdd.common.api.Result;
import com.ccdd.thread.dto.ImpactAnalysisResult;
import com.ccdd.thread.dto.TraversePathStep;
import com.ccdd.thread.service.ThreadGraphService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数字主线图谱 RESTful 接口控制器 (遵循 D07 / D09 规范)
 */
@RestController
@RequestMapping("/api/v1/thread-graph")
public class ThreadGraphController {

    private final ThreadGraphService threadGraphService;

    public ThreadGraphController(ThreadGraphService threadGraphService) {
        this.threadGraphService = threadGraphService;
    }

    /**
     * 链路遍历追溯 (多跳拓扑爆炸图)
     * @param nodeId 节点唯一ID
     * @param direction 遍历方向: DOWNSTREAM 或 UPSTREAM
     * @param maxDepth 最大深度 (默认 5)
     */
    @GetMapping("/nodes/{nodeId}/traverse")
    public Result<List<TraversePathStep>> traverse(
            @PathVariable("nodeId") String nodeId,
            @RequestParam(value = "direction", defaultValue = "DOWNSTREAM") String direction,
            @RequestParam(value = "maxDepth", defaultValue = "5") int maxDepth) {
        
        List<TraversePathStep> steps = threadGraphService.traverse(nodeId, direction, maxDepth);
        return Result.success(steps);
    }

    /**
     * 变更波及推演分析接口
     * @param nodeId 待变更节点ID
     * @param maxDepth 推演深度
     */
    @GetMapping("/nodes/{nodeId}/impact-analysis")
    public Result<ImpactAnalysisResult> analyzeImpact(
            @PathVariable("nodeId") String nodeId,
            @RequestParam(value = "maxDepth", defaultValue = "5") int maxDepth) {
        
        ImpactAnalysisResult result = threadGraphService.analyzeImpact(nodeId, maxDepth);
        return Result.success(result);
    }
}
