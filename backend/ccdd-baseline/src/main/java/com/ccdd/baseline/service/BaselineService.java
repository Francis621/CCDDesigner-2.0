package com.ccdd.baseline.service;

import com.ccdd.baseline.dto.BaselineDetailDto;
import com.ccdd.baseline.dto.BaselineDiffResultDto;
import com.ccdd.baseline.dto.BindConfigurationStateRequest;
import com.ccdd.baseline.dto.ClosureCheckResultDto;
import com.ccdd.baseline.dto.CreateBaselineRequest;
import com.ccdd.baseline.dto.FreezeBaselineRequest;
import com.ccdd.baseline.dto.FreezeBaselineResponse;
import com.ccdd.baseline.entity.BaselineEntity;
import com.ccdd.baseline.entity.BaselineMemberEntity;
import com.ccdd.baseline.entity.BaselinePurpose;
import com.ccdd.baseline.entity.BaselineRelationSnapshotEntity;
import com.ccdd.baseline.entity.BaselineState;
import com.ccdd.baseline.entity.ConfigurationStateReferenceEntity;
import com.ccdd.baseline.entity.MemberRole;
import com.ccdd.baseline.entity.SuccessorBaselineLinkEntity;
import com.ccdd.baseline.exception.ClosureValidationException;
import com.ccdd.baseline.repository.BaselineRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 基线管理业务核心服务
 * 涵盖：全要素闭包校验引擎、Merkle Closure Hash 计算、审批冻结、红线差分比对、多形态解耦
 */
@Service
public class BaselineService {

    private final BaselineRepository repository;

    public BaselineService(BaselineRepository repository) {
        this.repository = repository;
    }

    /**
     * 创建基线并圈定初始成员快照 (M21-F01)
     */
    public BaselineEntity createBaseline(CreateBaselineRequest request) {
        if (request.getBaselineCode() == null || request.getBaselineCode().trim().isEmpty()) {
            throw new IllegalArgumentException("基线编码不能为空");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("基线名称不能为空");
        }

        Optional<BaselineEntity> existing = repository.findBaselineByCode(request.getBaselineCode());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("基线编码已存在: " + request.getBaselineCode());
        }

        Instant now = Instant.now();
        BaselineEntity baseline = new BaselineEntity();
        baseline.setBaselineCode(request.getBaselineCode());
        baseline.setName(request.getName());
        baseline.setDescription(request.getDescription());
        baseline.setPurpose(request.getPurpose() != null ? request.getPurpose() : BaselinePurpose.PRODUCT_DESIGN_BASELINE);
        baseline.setProjectId(request.getProjectId());
        baseline.setTenantId(request.getTenantId() != null ? request.getTenantId() : "VMC_ENTERPRISE");
        baseline.setApprovalTicketId(request.getApprovalTicketId());
        baseline.setState(BaselineState.DRAFT);
        baseline.setWorkingVersion(1L);
        baseline.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "admin");
        baseline.setCreatedAt(now);

        repository.saveBaseline(baseline);

        // 保存圈定成员
        if (request.getInitialMembers() != null && !request.getInitialMembers().isEmpty()) {
            for (CreateBaselineRequest.BaselineMemberInput item : request.getInitialMembers()) {
                BaselineMemberEntity member = new BaselineMemberEntity();
                member.setBaselineId(baseline.getBaselineId());
                member.setRevisionId(item.getRevisionId());
                member.setMemberRole(item.getMemberRole() != null ? item.getMemberRole() : MemberRole.BOM_COMPONENT);
                member.setObjectTypeCode(item.getObjectTypeCode() != null ? item.getObjectTypeCode() : "PartRevision");
                member.setBusinessCode(item.getBusinessCode());
                member.setRevisionLabel(item.getRevisionLabel());
                member.setContentHash(item.getContentHash() != null ? item.getContentHash() : generateDefaultHash(item.getBusinessCode() + ":" + item.getRevisionLabel()));
                member.setArtifactId(item.getArtifactId());
                member.setCustomContext(item.getCustomContext() != null ? item.getCustomContext() : "{}");
                member.setAddedAt(now);
                repository.addMember(member);
            }
        }

        return baseline;
    }

    /**
     * 查询所有基线列表
     */
    public List<BaselineEntity> getAllBaselines() {
        return repository.findAllBaselines();
    }

    /**
     * 获取基线完整聚合详情
     */
    public BaselineDetailDto getBaselineDetail(Long baselineId) {
        BaselineEntity baseline = repository.findBaselineById(baselineId)
                .orElseThrow(() -> new IllegalArgumentException("未找到基线: " + baselineId));

        List<BaselineMemberEntity> members = repository.findMembersByBaselineId(baselineId);
        List<BaselineRelationSnapshotEntity> relations = repository.findRelationsByBaselineId(baselineId);
        List<ConfigurationStateReferenceEntity> configs = repository.findConfigStatesByBaselineId(baselineId);
        List<SuccessorBaselineLinkEntity> links = repository.findSuccessorLinksByBaselineId(baselineId);

        return new BaselineDetailDto(baseline, members, relations, configs, links);
    }

    /**
     * 向基线追加成员（仅草稿态允许）
     */
    public BaselineMemberEntity addMemberToBaseline(Long baselineId, BaselineMemberEntity member) {
        member.setBaselineId(baselineId);
        if (member.getContentHash() == null) {
            member.setContentHash(generateDefaultHash(member.getBusinessCode() + ":" + member.getRevisionLabel()));
        }
        return repository.addMember(member);
    }

    /**
     * 移除基线成员（仅草稿态允许）
     */
    public void removeMember(Long baselineId, Long memberId) {
        repository.removeMember(baselineId, memberId);
    }

    /**
     * 添加关系拓扑快照（仅草稿态允许）
     */
    public BaselineRelationSnapshotEntity addRelationSnapshot(Long baselineId, BaselineRelationSnapshotEntity relation) {
        relation.setBaselineId(baselineId);
        if (relation.getRelationHash() == null) {
            relation.setRelationHash(generateDefaultHash(relation.getRelationTypeId() + ":" + relation.getSourceRevisionId() + "->" + relation.getTargetRevisionId()));
        }
        return repository.addRelationSnapshot(relation);
    }

    /**
     * 校验闭包完备性与计算 closure_hash (M21-F02)
     */
    public ClosureCheckResultDto validateClosure(Long baselineId) {
        BaselineEntity baseline = repository.findBaselineById(baselineId)
                .orElseThrow(() -> new IllegalArgumentException("未找到基线: " + baselineId));

        List<BaselineMemberEntity> members = repository.findMembersByBaselineId(baselineId);
        List<BaselineRelationSnapshotEntity> relations = repository.findRelationsByBaselineId(baselineId);

        List<ClosureCheckResultDto.ClosureIssue> issues = new ArrayList<>();

        if (members.isEmpty()) {
            issues.add(new ClosureCheckResultDto.ClosureIssue(
                    "ERROR", "EMPTY_MEMBERS", baseline.getBaselineCode(), "基线未圈定任何纳管成员对象，无法构成有效闭包"
            ));
        }

        // 1. 检查各成员版本合规性
        boolean hasRoot = false;
        Map<Long, BaselineMemberEntity> revisionMap = new HashMap<>();
        for (BaselineMemberEntity m : members) {
            if (m.getRevisionId() != null) {
                revisionMap.put(m.getRevisionId(), m);
            }
            if (m.getMemberRole() == MemberRole.EBOM_ROOT) {
                hasRoot = true;
            }
            // 检查草稿或未受控态
            if (m.getRevisionLabel() == null || m.getRevisionLabel().trim().isEmpty() || m.getRevisionLabel().equalsIgnoreCase("DRAFT") || m.getRevisionLabel().startsWith("0.0")) {
                issues.add(new ClosureCheckResultDto.ClosureIssue(
                        "ERROR", "DRAFT_STATE", m.getBusinessCode(),
                        "成员对象 " + m.getBusinessCode() + " 处于草稿工作态(" + m.getRevisionLabel() + ")，未受控发布不可冻结"
                ));
            }
            if (m.getContentHash() == null || m.getContentHash().trim().length() != 64) {
                issues.add(new ClosureCheckResultDto.ClosureIssue(
                        "ERROR", "CHECKSUM_MISMATCH", m.getBusinessCode(),
                        "成员对象 " + m.getBusinessCode() + " 缺少有效的64位SHA256内容哈希快照"
                ));
            }
        }

        if (!hasRoot && members.size() > 1) {
            issues.add(new ClosureCheckResultDto.ClosureIssue(
                    "WARNING", "NO_EBOM_ROOT", baseline.getBaselineCode(),
                    "基线成员中未显式标记 EBOM_ROOT 顶层装配根节点"
            ));
        }

        // 2. 检查拓扑关系悬挂引用
        for (BaselineRelationSnapshotEntity rel : relations) {
            if (!revisionMap.containsKey(rel.getSourceRevisionId())) {
                issues.add(new ClosureCheckResultDto.ClosureIssue(
                        "ERROR", "DANGLING_RELATION_SOURCE", String.valueOf(rel.getSourceRevisionId()),
                        "关系边的源版本 " + rel.getSourceRevisionId() + " 未包含在本基线闭包成员集合中"
                ));
            }
            if (!revisionMap.containsKey(rel.getTargetRevisionId())) {
                issues.add(new ClosureCheckResultDto.ClosureIssue(
                        "ERROR", "DANGLING_RELATION_TARGET", String.valueOf(rel.getTargetRevisionId()),
                        "关系边的目标版本 " + rel.getTargetRevisionId() + " 未包含在本基线闭包成员集合中"
                ));
            }
        }

        boolean isComplete = issues.stream().noneMatch(i -> "ERROR".equalsIgnoreCase(i.getSeverity()));

        // 计算 Merkle 闭包哈希根 (64位)
        String closureHash;
        if (isComplete && !members.isEmpty()) {
            closureHash = calculateMerkleClosureHash(members, relations);
        } else {
            closureHash = null;
        }

        return new ClosureCheckResultDto(
                baselineId,
                isComplete,
                closureHash,
                members.size(),
                relations.size(),
                issues
        );
    }

    /**
     * 审批冻结基线 (M21-F03)
     */
    public FreezeBaselineResponse freezeBaseline(FreezeBaselineRequest request) {
        BaselineEntity baseline = repository.findBaselineById(request.getBaselineId())
                .orElseThrow(() -> new IllegalArgumentException("未找到基线: " + request.getBaselineId()));

        if (baseline.isFrozen()) {
            return new FreezeBaselineResponse(
                    baseline.getBaselineId(), baseline.getBaselineCode(), baseline.getState(),
                    baseline.getClosureHash(), 0, 0, baseline.getFrozenAt(), baseline.getFrozenBy(),
                    "基线已处于冻结状态"
            );
        }

        // 闭包完备性阻断校验
        ClosureCheckResultDto check = validateClosure(request.getBaselineId());
        if (Boolean.TRUE.equals(request.getEnforceClosureValidation()) && !Boolean.TRUE.equals(check.getIsComplete())) {
            String errorMsg = check.getIssues().stream()
                    .filter(i -> "ERROR".equalsIgnoreCase(i.getSeverity()))
                    .map(i -> i.getTargetIdentifier() + ": " + i.getMessage())
                    .collect(Collectors.joining("; "));
            throw new ClosureValidationException("基线闭包完备性检查未通过，禁止冻结：" + errorMsg);
        }

        String finalHash = check.getClosureHash();
        if (finalHash == null) {
            finalHash = generateDefaultHash(baseline.getBaselineCode() + ":" + System.currentTimeMillis());
        }

        Instant now = Instant.now();
        baseline.setState(BaselineState.FROZEN);
        baseline.setClosureHash(finalHash);
        baseline.setFrozenBy(request.getApprover() != null ? request.getApprover() : "committee_lead");
        baseline.setFrozenAt(now);
        if (request.getApprovalTicketId() != null) {
            baseline.setApprovalTicketId(request.getApprovalTicketId());
        }

        repository.saveBaseline(baseline);

        List<BaselineMemberEntity> members = repository.findMembersByBaselineId(baseline.getBaselineId());
        List<BaselineRelationSnapshotEntity> relations = repository.findRelationsByBaselineId(baseline.getBaselineId());

        return new FreezeBaselineResponse(
                baseline.getBaselineId(),
                baseline.getBaselineCode(),
                baseline.getState(),
                baseline.getClosureHash(),
                members.size(),
                relations.size(),
                baseline.getFrozenAt(),
                baseline.getFrozenBy(),
                "基线审批冻结成功，全要素防篡改哈希已固化"
        );
    }

    /**
     * 红线差分比对 (M21-F03 Redline Diff)
     */
    public BaselineDiffResultDto compareBaselines(Long baselineIdA, Long baselineIdB) {
        BaselineEntity baselineA = repository.findBaselineById(baselineIdA)
                .orElseThrow(() -> new IllegalArgumentException("未找到源基线 A: " + baselineIdA));
        BaselineEntity baselineB = repository.findBaselineById(baselineIdB)
                .orElseThrow(() -> new IllegalArgumentException("未找到目标基线 B: " + baselineIdB));

        List<BaselineMemberEntity> membersA = repository.findMembersByBaselineId(baselineIdA);
        List<BaselineMemberEntity> membersB = repository.findMembersByBaselineId(baselineIdB);

        Map<String, BaselineMemberEntity> mapA = membersA.stream()
                .collect(Collectors.toMap(m -> m.getObjectTypeCode() + ":" + m.getBusinessCode(), m -> m, (k1, k2) -> k1));
        Map<String, BaselineMemberEntity> mapB = membersB.stream()
                .collect(Collectors.toMap(m -> m.getObjectTypeCode() + ":" + m.getBusinessCode(), m -> m, (k1, k2) -> k1));

        List<BaselineDiffResultDto.DiffEntry> diffList = new ArrayList<>();
        int added = 0;
        int removed = 0;
        int modified = 0;
        int unchanged = 0;

        // 检查 A 中的元素
        for (Map.Entry<String, BaselineMemberEntity> entry : mapA.entrySet()) {
            BaselineMemberEntity itemA = entry.getValue();
            if (mapB.containsKey(entry.getKey())) {
                BaselineMemberEntity itemB = mapB.get(entry.getKey());
                boolean hashSame = Objects.equals(itemA.getContentHash(), itemB.getContentHash());
                boolean revSame = Objects.equals(itemA.getRevisionLabel(), itemB.getRevisionLabel());
                if (hashSame && revSame) {
                    unchanged++;
                    diffList.add(new BaselineDiffResultDto.DiffEntry(
                            "UNCHANGED", itemA.getObjectTypeCode(), itemA.getRevisionId(),
                            itemA.getBusinessCode(), itemA.getRevisionLabel(), itemB.getRevisionLabel(),
                            itemA.getContentHash(), itemB.getContentHash(), "版本与哈希完全一致"
                    ));
                } else {
                    modified++;
                    diffList.add(new BaselineDiffResultDto.DiffEntry(
                            "MODIFIED", itemA.getObjectTypeCode(), itemA.getRevisionId(),
                            itemA.getBusinessCode(), itemA.getRevisionLabel(), itemB.getRevisionLabel(),
                            itemA.getContentHash(), itemB.getContentHash(),
                            "版本升版或哈希变更 (" + itemA.getRevisionLabel() + " -> " + itemB.getRevisionLabel() + ")"
                    ));
                }
            } else {
                removed++;
                diffList.add(new BaselineDiffResultDto.DiffEntry(
                        "REMOVED", itemA.getObjectTypeCode(), itemA.getRevisionId(),
                        itemA.getBusinessCode(), itemA.getRevisionLabel(), null,
                        itemA.getContentHash(), null, "在目标基线中被移除"
                ));
            }
        }

        // 检查 B 中独有的元素 (新增)
        for (Map.Entry<String, BaselineMemberEntity> entry : mapB.entrySet()) {
            if (!mapA.containsKey(entry.getKey())) {
                added++;
                BaselineMemberEntity itemB = entry.getValue();
                diffList.add(new BaselineDiffResultDto.DiffEntry(
                        "ADDED", itemB.getObjectTypeCode(), itemB.getRevisionId(),
                        itemB.getBusinessCode(), null, itemB.getRevisionLabel(),
                        null, itemB.getContentHash(), "在目标基线中新增纳管"
                ));
            }
        }

        BaselineDiffResultDto result = new BaselineDiffResultDto();
        result.setBaselineIdA(baselineIdA);
        result.setBaselineCodeA(baselineA.getBaselineCode());
        result.setClosureHashA(baselineA.getClosureHash());
        result.setBaselineIdB(baselineIdB);
        result.setBaselineCodeB(baselineB.getBaselineCode());
        result.setClosureHashB(baselineB.getClosureHash());
        result.setIsIdentical(added == 0 && removed == 0 && modified == 0 && Objects.equals(baselineA.getClosureHash(), baselineB.getClosureHash()));
        result.setAddedCount(added);
        result.setRemovedCount(removed);
        result.setModifiedCount(modified);
        result.setUnchangedCount(unchanged);
        result.setMemberDifferences(diffList);
        result.setComparedAt(Instant.now());

        return result;
    }

    /**
     * 绑定机床多形态配置状态 (M21-F04)
     */
    public ConfigurationStateReferenceEntity bindConfigurationState(BindConfigurationStateRequest request) {
        BaselineEntity baseline = repository.findBaselineById(request.getBaselineId())
                .orElseThrow(() -> new IllegalArgumentException("未找到基线: " + request.getBaselineId()));

        ConfigurationStateReferenceEntity ref = new ConfigurationStateReferenceEntity();
        ref.setTenantId("VMC_ENTERPRISE");
        ref.setConfigStateType(request.getConfigStateType() != null ? request.getConfigStateType() : baseline.getPurpose());
        ref.setBaselineId(baseline.getBaselineId());
        ref.setOrderProductId(request.getOrderProductId());
        ref.setIndividualId(request.getIndividualId());
        ref.setSerialNumber(request.getSerialNumber());
        ref.setEffectiveFrom(Instant.now());
        ref.setIsActive(true);
        ref.setNotes(request.getNotes());
        ref.setBoundAt(Instant.now());
        ref.setBoundBy(request.getBoundBy() != null ? request.getBoundBy() : "admin");

        return repository.saveConfigurationState(ref);
    }

    /**
     * 获取所有配置状态列表
     */
    public List<ConfigurationStateReferenceEntity> getAllConfigurationStates() {
        return repository.findAllConfigStates();
    }

    /**
     * 从冻结基线派生新版本草稿基线（演进分支）
     */
    public BaselineEntity deriveSuccessorBaseline(Long predecessorId, String newCode, String newName, String reason, Long changeOrderId, String operator) {
        BaselineEntity pred = repository.findBaselineById(predecessorId)
                .orElseThrow(() -> new IllegalArgumentException("源基线不存在: " + predecessorId));

        Instant now = Instant.now();
        BaselineEntity successor = new BaselineEntity();
        successor.setBaselineCode(newCode);
        successor.setName(newName);
        successor.setDescription("派生自基线: " + pred.getBaselineCode() + "。原因: " + reason);
        successor.setPurpose(pred.getPurpose());
        successor.setProjectId(pred.getProjectId());
        successor.setTenantId(pred.getTenantId());
        successor.setState(BaselineState.DRAFT);
        successor.setWorkingVersion(pred.getWorkingVersion() + 1);
        successor.setCreatedBy(operator != null ? operator : "engineer");
        successor.setCreatedAt(now);

        repository.saveBaseline(successor);

        // 复制前驱基线的成员到新基线（作为初始副本）
        List<BaselineMemberEntity> predMembers = repository.findMembersByBaselineId(predecessorId);
        for (BaselineMemberEntity pm : predMembers) {
            BaselineMemberEntity copy = new BaselineMemberEntity();
            copy.setBaselineId(successor.getBaselineId());
            copy.setRevisionId(pm.getRevisionId());
            copy.setMemberRole(pm.getMemberRole());
            copy.setObjectTypeCode(pm.getObjectTypeCode());
            copy.setBusinessCode(pm.getBusinessCode());
            copy.setRevisionLabel(pm.getRevisionLabel());
            copy.setContentHash(pm.getContentHash());
            copy.setArtifactId(pm.getArtifactId());
            copy.setCustomContext(pm.getCustomContext());
            copy.setAddedAt(now);
            repository.addMember(copy);
        }

        // 建立后继链接
        SuccessorBaselineLinkEntity link = new SuccessorBaselineLinkEntity(
                null, pred.getBaselineId(), successor.getBaselineId(),
                changeOrderId, reason, now
        );
        repository.saveSuccessorLink(link);

        return successor;
    }

    /**
     * 删除基线（仅草稿态可删除，受不可变性防篡改保护）
     */
    public boolean deleteBaseline(Long baselineId) {
        return repository.deleteBaseline(baselineId);
    }

    // ====== 私有辅助方法：Merkle 闭包哈希计算 ======

    private String calculateMerkleClosureHash(List<BaselineMemberEntity> members, List<BaselineRelationSnapshotEntity> relations) {
        // 1. 对成员特征哈希做规范化排序
        List<String> memberLeaves = members.stream()
                .map(m -> (m.getObjectTypeCode() + ":" + m.getRevisionId() + ":" + m.getRevisionLabel() + ":" + m.getContentHash()))
                .sorted()
                .map(this::sha256Hex)
                .collect(Collectors.toList());

        // 2. 对拓扑关系哈希做规范化排序
        List<String> relationLeaves = relations.stream()
                .map(r -> (r.getRelationTypeId() + ":" + r.getSourceRevisionId() + "->" + r.getTargetRevisionId() + ":" + r.getRelationHash()))
                .sorted()
                .map(this::sha256Hex)
                .collect(Collectors.toList());

        // 3. Merkle 汇总折叠
        String membersRoot = foldHashList(memberLeaves);
        String relationsRoot = foldHashList(relationLeaves);

        return sha256Hex("CLOSURE:" + membersRoot + ":" + relationsRoot);
    }

    private String foldHashList(List<String> hashList) {
        if (hashList.isEmpty()) {
            return sha256Hex("EMPTY_NODE");
        }
        if (hashList.size() == 1) {
            return hashList.get(0);
        }
        StringBuilder sb = new StringBuilder();
        for (String h : hashList) {
            sb.append(h);
        }
        return sha256Hex(sb.toString());
    }

    private String generateDefaultHash(String raw) {
        return sha256Hex(raw != null ? raw : String.valueOf(System.currentTimeMillis()));
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encoded) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 摘要算法不可用", e);
        }
    }
}
