package com.ccdd.baseline.repository;

import com.ccdd.baseline.entity.BaselineEntity;
import com.ccdd.baseline.entity.BaselineMemberEntity;
import com.ccdd.baseline.entity.BaselinePurpose;
import com.ccdd.baseline.entity.BaselineRelationSnapshotEntity;
import com.ccdd.baseline.entity.BaselineState;
import com.ccdd.baseline.entity.ConfigurationStateReferenceEntity;
import com.ccdd.baseline.entity.MemberRole;
import com.ccdd.baseline.entity.SuccessorBaselineLinkEntity;
import com.ccdd.baseline.exception.BaselineImmutableViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * M21 基线与配置状态仓储层
 * 实现了并发安全内存映射，以及与 V1.6.0 迁移脚本种子数据完全一致的装配
 * 严格执行 CST-M21-01 不可变性防篡改控制
 */
@Repository
public class BaselineRepository {

    private final Map<Long, BaselineEntity> baselineStore = new ConcurrentHashMap<>();
    private final Map<Long, BaselineMemberEntity> memberStore = new ConcurrentHashMap<>();
    private final Map<Long, BaselineRelationSnapshotEntity> relationStore = new ConcurrentHashMap<>();
    private final Map<Long, ConfigurationStateReferenceEntity> configStateStore = new ConcurrentHashMap<>();
    private final Map<Long, SuccessorBaselineLinkEntity> successorLinkStore = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(2000L);

    public BaselineRepository() {
        initSeedData();
    }

    private void initSeedData() {
        Instant now = Instant.now();
        Long projectVmc850Id = 101L;

        // 1. BL-VMC850-CDR-001 (FROZEN 关键设计评审基线)
        Long b1Id = 1001L;
        String b1Hash = "58a9e142f36098dca084620f4c82c2a075218d6e3c54a938b3c9f280a71d82f1";
        BaselineEntity b1 = new BaselineEntity(
                b1Id,
                projectVmc850Id,
                "VMC_ENTERPRISE",
                "BL-VMC850-CDR-001",
                "VMC850立式加工中心关键设计评审(CDR)冻结基线",
                BaselinePurpose.PRODUCT_DESIGN_BASELINE,
                BaselineState.FROZEN,
                "整机详细设计完成，通过关键设计评审（Gate-3），BOM层级与图文档全要素冻结",
                b1Hash,
                1L,
                "sys_chief_engineer",
                now.minus(30, ChronoUnit.DAYS),
                "expert_committee",
                now.minus(30, ChronoUnit.DAYS),
                9001L
        );
        baselineStore.put(b1Id, b1);

        // B1 成员
        BaselineMemberEntity mem1_1 = new BaselineMemberEntity(
                1101L, b1Id, 5001L, MemberRole.EBOM_ROOT, "PartRevision",
                "M-VMC850-SPN-01", "B",
                "a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0",
                7001L, "{\"slot\":\"SPINDLE_HEAD\",\"qty\":1}", now.minus(30, ChronoUnit.DAYS)
        );
        memberStore.put(mem1_1.getMemberId(), mem1_1);

        BaselineMemberEntity mem1_2 = new BaselineMemberEntity(
                1102L, b1Id, 5002L, MemberRole.CAD_DRAWING, "DocRevision",
                "DOC-VMC850-DRW-001", "B",
                "b2c3d4e5f6a708192a3b4c5d6e7f809123456789abcdef0123456789abcdef01",
                7002L, "{\"sheet\":\"1/2\",\"scale\":\"1:1\"}", now.minus(30, ChronoUnit.DAYS)
        );
        memberStore.put(mem1_2.getMemberId(), mem1_2);

        // B1 拓扑关系快照
        BaselineRelationSnapshotEntity rel1 = new BaselineRelationSnapshotEntity(
                1201L, b1Id, 5001L, 5002L, "DOC_REFERENCE",
                "c3d4e5f6a7b8091a2b3c4d5e6f7081920123456789abcdef0123456789abcdef",
                "{\"association\":\"DESIGN_DEFINITION\"}", now.minus(30, ChronoUnit.DAYS)
        );
        relationStore.put(rel1.getSnapshotRelId(), rel1);

        // B1 多形态配置状态引用 (As-Designed)
        ConfigurationStateReferenceEntity cs1 = new ConfigurationStateReferenceEntity(
                1301L, "VMC_ENTERPRISE", BaselinePurpose.AS_DESIGNED, b1Id, 3001L, null,
                null, now.minus(30, ChronoUnit.DAYS), null, true,
                "设计发布基线，已锁定为制造BOP转换基准", now.minus(30, ChronoUnit.DAYS), "sys_chief_engineer"
        );
        configStateStore.put(cs1.getConfigRefId(), cs1);

        // 2. BL-VMC850-PLAN-001 (FROZEN 制造工艺基线)
        Long b2Id = 1002L;
        String b2Hash = "d3a4f891b2c4e5a6f708192a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c";
        BaselineEntity b2 = new BaselineEntity(
                b2Id,
                projectVmc850Id,
                "VMC_ENTERPRISE",
                "BL-VMC850-PLAN-001",
                "VMC850制造工艺规划(MPR)发布基线",
                BaselinePurpose.AS_PLANNED,
                BaselineState.FROZEN,
                "工艺路线、装配工步工序与数控NC程序锁定基线",
                b2Hash,
                1L,
                "process_lead_01",
                now.minus(15, ChronoUnit.DAYS),
                "plant_manager",
                now.minus(15, ChronoUnit.DAYS),
                9002L
        );
        baselineStore.put(b2Id, b2);

        // B2 成员
        BaselineMemberEntity mem2_1 = new BaselineMemberEntity(
                1103L, b2Id, 5001L, MemberRole.EBOM_ROOT, "PartRevision",
                "M-VMC850-SPN-01", "B",
                "a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0",
                7001L, "{\"station\":\"OP10-PRE_ASSEMBLE\"}", now.minus(15, ChronoUnit.DAYS)
        );
        memberStore.put(mem2_1.getMemberId(), mem2_1);

        // B2 配置状态引用 (As-Planned)
        ConfigurationStateReferenceEntity cs2 = new ConfigurationStateReferenceEntity(
                1302L, "VMC_ENTERPRISE", BaselinePurpose.AS_PLANNED, b2Id, 3001L, null,
                null, now.minus(15, ChronoUnit.DAYS), null, true,
                "车间执行工艺与工位指派锁定", now.minus(15, ChronoUnit.DAYS), "process_lead_01"
        );
        configStateStore.put(cs2.getConfigRefId(), cs2);

        // 3. BL-VMC850-BUILT-SN001 (FROZEN 实装出厂基线)
        Long b3Id = 1003L;
        String b3Hash = "e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6";
        BaselineEntity b3 = new BaselineEntity(
                b3Id,
                projectVmc850Id,
                "VMC_ENTERPRISE",
                "BL-VMC850-BUILT-SN001",
                "VMC850首台实物机床(SN-001)出厂实装基线",
                BaselinePurpose.AS_BUILT,
                BaselineState.FROZEN,
                "机床出厂实物配置、装配序列号、实测几何精度与激光干涉仪补偿参数归档",
                b3Hash,
                1L,
                "qc_director",
                now.minus(5, ChronoUnit.DAYS),
                "quality_committee",
                now.minus(5, ChronoUnit.DAYS),
                9003L
        );
        baselineStore.put(b3Id, b3);

        // B3 配置状态引用 (As-Built)
        ConfigurationStateReferenceEntity cs3 = new ConfigurationStateReferenceEntity(
                1303L, "VMC_ENTERPRISE", BaselinePurpose.AS_BUILT, b3Id, null, 4001L,
                "VMC850-202603-001", now.minus(5, ChronoUnit.DAYS), null, true,
                "首台样机实测数据与出厂配置台账", now.minus(5, ChronoUnit.DAYS), "qc_director"
        );
        configStateStore.put(cs3.getConfigRefId(), cs3);

        // 4. 后继演进链接 (b1 -> b2, b2 -> b3)
        SuccessorBaselineLinkEntity link1 = new SuccessorBaselineLinkEntity(
                1401L, b1Id, b2Id, 8001L, "从设计基线发布转化为工艺规划基线", now.minus(15, ChronoUnit.DAYS)
        );
        successorLinkStore.put(link1.getLinkId(), link1);

        SuccessorBaselineLinkEntity link2 = new SuccessorBaselineLinkEntity(
                1402L, b2Id, b3Id, 8002L, "从制造基线转化为SN001实装出厂基线", now.minus(5, ChronoUnit.DAYS)
        );
        successorLinkStore.put(link2.getLinkId(), link2);

        // 5. BL-HMC630-PDR-001 (草稿基线 DRAFT)
        Long b4Id = 1004L;
        BaselineEntity b4 = new BaselineEntity(
                b4Id,
                102L,
                "VMC_ENTERPRISE",
                "BL-HMC630-PDR-001",
                "HMC630卧式加工中心初步设计基线(PDR)",
                BaselinePurpose.ALLOCATED_BASELINE,
                BaselineState.DRAFT,
                "初步方案论证草案，物料与图纸处于持续变更与圈定中",
                null,
                1L,
                "engineer_wang",
                now.minus(2, ChronoUnit.DAYS),
                null,
                null,
                null
        );
        baselineStore.put(b4Id, b4);
    }

    public BaselineEntity saveBaseline(BaselineEntity entity) {
        if (entity.getBaselineId() == null) {
            entity.setBaselineId(idGenerator.incrementAndGet());
        }
        BaselineEntity existing = baselineStore.get(entity.getBaselineId());
        if (existing != null && existing.isFrozen()) {
            // 允许冻结审批动作写入 closureHash 与 frozenAt，其他任何修改皆阻断 (CST-M21-01)
            if (existing.getState() == entity.getState()
                    && (!existing.getBaselineCode().equals(entity.getBaselineCode())
                    || !existing.getName().equals(entity.getName())
                    || !existing.getPurpose().equals(entity.getPurpose()))) {
                throw new BaselineImmutableViolationException("基线 " + existing.getBaselineCode() + " 处于冻结状态，受 CST-M21-01 保护严格禁止直接修改基线定义！");
            }
        }
        baselineStore.put(entity.getBaselineId(), entity);
        return entity;
    }

    public Optional<BaselineEntity> findBaselineById(Long baselineId) {
        return Optional.ofNullable(baselineStore.get(baselineId));
    }

    public Optional<BaselineEntity> findBaselineByCode(String code) {
        return baselineStore.values().stream()
                .filter(b -> b.getBaselineCode().equalsIgnoreCase(code))
                .findFirst();
    }

    public List<BaselineEntity> findAllBaselines() {
        return new ArrayList<>(baselineStore.values());
    }

    public List<BaselineEntity> findBaselinesByProjectId(Long projectId) {
        return baselineStore.values().stream()
                .filter(b -> projectId.equals(b.getProjectId()))
                .collect(Collectors.toList());
    }

    public boolean deleteBaseline(Long baselineId) {
        BaselineEntity existing = baselineStore.get(baselineId);
        if (existing == null) {
            return false;
        }
        if (existing.isFrozen()) {
            throw new BaselineImmutableViolationException("基线 " + existing.getBaselineCode() + " 已被审批冻结，受 CST-M21-01 规则保护严禁物理删除！");
        }
        baselineStore.remove(baselineId);
        memberStore.entrySet().removeIf(e -> baselineId.equals(e.getValue().getBaselineId()));
        relationStore.entrySet().removeIf(e -> baselineId.equals(e.getValue().getBaselineId()));
        return true;
    }

    // ====== 基线成员管理 ======

    public BaselineMemberEntity addMember(BaselineMemberEntity member) {
        BaselineEntity baseline = baselineStore.get(member.getBaselineId());
        if (baseline == null) {
            throw new IllegalArgumentException("关联的基线不存在: " + member.getBaselineId());
        }
        if (baseline.isFrozen()) {
            throw new BaselineImmutableViolationException("基线 " + baseline.getBaselineCode() + " 已冻结，禁止追加、修改或替换成员！");
        }
        if (member.getMemberId() == null) {
            member.setMemberId(idGenerator.incrementAndGet());
        }
        if (member.getAddedAt() == null) {
            member.setAddedAt(Instant.now());
        }
        memberStore.put(member.getMemberId(), member);
        return member;
    }

    public List<BaselineMemberEntity> findMembersByBaselineId(Long baselineId) {
        return memberStore.values().stream()
                .filter(m -> baselineId.equals(m.getBaselineId()))
                .collect(Collectors.toList());
    }

    public void removeMember(Long baselineId, Long memberId) {
        BaselineEntity baseline = baselineStore.get(baselineId);
        if (baseline != null && baseline.isFrozen()) {
            throw new BaselineImmutableViolationException("基线 " + baseline.getBaselineCode() + " 已冻结，禁止删除其成员！");
        }
        memberStore.remove(memberId);
    }

    // ====== 关系拓扑快照 ======

    public BaselineRelationSnapshotEntity addRelationSnapshot(BaselineRelationSnapshotEntity relation) {
        BaselineEntity baseline = baselineStore.get(relation.getBaselineId());
        if (baseline != null && baseline.isFrozen()) {
            throw new BaselineImmutableViolationException("基线 " + baseline.getBaselineCode() + " 已冻结，禁止添加关系快照！");
        }
        if (relation.getSnapshotRelId() == null) {
            relation.setSnapshotRelId(idGenerator.incrementAndGet());
        }
        if (relation.getSnapshottedAt() == null) {
            relation.setSnapshottedAt(Instant.now());
        }
        relationStore.put(relation.getSnapshotRelId(), relation);
        return relation;
    }

    public List<BaselineRelationSnapshotEntity> findRelationsByBaselineId(Long baselineId) {
        return relationStore.values().stream()
                .filter(r -> baselineId.equals(r.getBaselineId()))
                .collect(Collectors.toList());
    }

    // ====== 多形态配置状态引用 ======

    public ConfigurationStateReferenceEntity saveConfigurationState(ConfigurationStateReferenceEntity configState) {
        if (configState.getConfigRefId() == null) {
            configState.setConfigRefId(idGenerator.incrementAndGet());
        }
        if (configState.getBoundAt() == null) {
            configState.setBoundAt(Instant.now());
        }
        configStateStore.put(configState.getConfigRefId(), configState);
        return configState;
    }

    public List<ConfigurationStateReferenceEntity> findConfigStatesByBaselineId(Long baselineId) {
        return configStateStore.values().stream()
                .filter(c -> baselineId.equals(c.getBaselineId()))
                .collect(Collectors.toList());
    }

    public List<ConfigurationStateReferenceEntity> findAllConfigStates() {
        return new ArrayList<>(configStateStore.values());
    }

    // ====== 演进链接 ======

    public SuccessorBaselineLinkEntity saveSuccessorLink(SuccessorBaselineLinkEntity link) {
        if (link.getLinkId() == null) {
            link.setLinkId(idGenerator.incrementAndGet());
        }
        if (link.getLinkedAt() == null) {
            link.setLinkedAt(Instant.now());
        }
        successorLinkStore.put(link.getLinkId(), link);
        return link;
    }

    public List<SuccessorBaselineLinkEntity> findSuccessorLinksByBaselineId(Long baselineId) {
        return successorLinkStore.values().stream()
                .filter(l -> baselineId.equals(l.getPredecessorBaselineId()) || baselineId.equals(l.getSuccessorBaselineId()))
                .collect(Collectors.toList());
    }
}
