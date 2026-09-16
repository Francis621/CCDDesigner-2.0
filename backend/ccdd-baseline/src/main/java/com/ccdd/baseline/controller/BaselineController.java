package com.ccdd.baseline.controller;

import com.ccdd.baseline.dto.BaselineDetailDto;
import com.ccdd.baseline.dto.BaselineDiffResultDto;
import com.ccdd.baseline.dto.BindConfigurationStateRequest;
import com.ccdd.baseline.dto.ClosureCheckResultDto;
import com.ccdd.baseline.dto.CreateBaselineRequest;
import com.ccdd.baseline.dto.FreezeBaselineRequest;
import com.ccdd.baseline.dto.FreezeBaselineResponse;
import com.ccdd.baseline.entity.BaselineEntity;
import com.ccdd.baseline.entity.BaselineMemberEntity;
import com.ccdd.baseline.entity.BaselineRelationSnapshotEntity;
import com.ccdd.baseline.entity.ConfigurationStateReferenceEntity;
import com.ccdd.baseline.service.BaselineService;
import com.ccdd.common.api.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * M21 基线与配置状态管理 RESTful 控制器 (OpenAPI §7)
 */
@RestController
@RequestMapping("/api/v1/baselines")
public class BaselineController {

    private final BaselineService baselineService;

    public BaselineController(BaselineService baselineService) {
        this.baselineService = baselineService;
    }

    /**
     * 查询所有基线台账列表
     */
    @GetMapping
    public Result<List<BaselineEntity>> listBaselines() {
        return Result.success(baselineService.getAllBaselines());
    }

    /**
     * 获取基线聚合详情 (包括成员、拓扑快照、配置状态、演进链)
     */
    @GetMapping("/{id}")
    public Result<BaselineDetailDto> getBaselineDetail(@PathVariable("id") Long id) {
        return Result.success(baselineService.getBaselineDetail(id));
    }

    /**
     * 创建新基线并圈定初始成员快照
     */
    @PostMapping
    public Result<BaselineEntity> createBaseline(@RequestBody CreateBaselineRequest request) {
        return Result.success(baselineService.createBaseline(request));
    }

    /**
     * 删除草稿基线（不可变性防篡改拦截冻结/归档基线）
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteBaseline(@PathVariable("id") Long id) {
        return Result.success(baselineService.deleteBaseline(id));
    }

    /**
     * 向草稿基线追加纳管成员
     */
    @PostMapping("/{id}/members")
    public Result<BaselineMemberEntity> addMember(@PathVariable("id") Long id, @RequestBody BaselineMemberEntity member) {
        return Result.success(baselineService.addMemberToBaseline(id, member));
    }

    /**
     * 从草稿基线中移除成员
     */
    @DeleteMapping("/{id}/members/{memberId}")
    public Result<Boolean> removeMember(@PathVariable("id") Long id, @PathVariable("memberId") Long memberId) {
        baselineService.removeMember(id, memberId);
        return Result.success(true);
    }

    /**
     * 向草稿基线添加关系拓扑快照
     */
    @PostMapping("/{id}/relations")
    public Result<BaselineRelationSnapshotEntity> addRelation(@PathVariable("id") Long id, @RequestBody BaselineRelationSnapshotEntity relation) {
        return Result.success(baselineService.addRelationSnapshot(id, relation));
    }

    /**
     * 全要素闭包完备性检查与 Merkle ClosureHash 试算
     */
    @PostMapping("/{id}/closure-check")
    public Result<ClosureCheckResultDto> checkClosure(@PathVariable("id") Long id) {
        return Result.success(baselineService.validateClosure(id));
    }

    /**
     * 审批冻结基线 (严格闭包前置阻断)
     */
    @PostMapping("/{id}/freeze")
    public Result<FreezeBaselineResponse> freezeBaseline(@PathVariable("id") Long id, @RequestBody(required = false) FreezeBaselineRequest request) {
        if (request == null) {
            request = new FreezeBaselineRequest();
        }
        request.setBaselineId(id);
        return Result.success(baselineService.freezeBaseline(request));
    }

    /**
     * 双基线红线差分比对 (Redline Diff Engine)
     */
    @GetMapping("/compare")
    public Result<BaselineDiffResultDto> compareBaselines(@RequestParam("idA") Long idA, @RequestParam("idB") Long idB) {
        return Result.success(baselineService.compareBaselines(idA, idB));
    }

    /**
     * 绑定机床全生命周期多形态配置状态 (As-Designed / As-Planned / As-Built / As-Delivered / As-Maintained)
     */
    @PostMapping("/configuration-states")
    public Result<ConfigurationStateReferenceEntity> bindConfigurationState(@RequestBody BindConfigurationStateRequest request) {
        return Result.success(baselineService.bindConfigurationState(request));
    }

    /**
     * 查询所有机床配置状态映射列表
     */
    @GetMapping("/configuration-states")
    public Result<List<ConfigurationStateReferenceEntity>> listConfigurationStates() {
        return Result.success(baselineService.getAllConfigurationStates());
    }

    /**
     * 从已冻结基线派生新演进分支 (Derive Successor)
     */
    @PostMapping("/{id}/derive")
    public Result<BaselineEntity> deriveSuccessor(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        String newCode = (String) body.getOrDefault("newCode", "BL-DERIVED-" + System.currentTimeMillis());
        String newName = (String) body.getOrDefault("newName", "派生演进基线");
        String reason = (String) body.getOrDefault("reason", "设计工程变更演进");
        Object coIdObj = body.get("changeOrderId");
        Long changeOrderId = coIdObj != null ? Long.valueOf(coIdObj.toString()) : null;
        String operator = (String) body.getOrDefault("operator", "engineer");

        return Result.success(baselineService.deriveSuccessorBaseline(id, newCode, newName, reason, changeOrderId, operator));
    }
}
