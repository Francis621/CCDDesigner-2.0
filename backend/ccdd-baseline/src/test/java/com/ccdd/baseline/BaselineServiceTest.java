package com.ccdd.baseline;

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
import com.ccdd.baseline.entity.BaselineState;
import com.ccdd.baseline.entity.ConfigurationStateReferenceEntity;
import com.ccdd.baseline.entity.MemberRole;
import com.ccdd.baseline.exception.BaselineImmutableViolationException;
import com.ccdd.baseline.exception.ClosureValidationException;
import com.ccdd.baseline.repository.BaselineRepository;
import com.ccdd.baseline.service.BaselineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * M21 基线与配置状态管理全要素验收测试
 * 严格覆盖 TC-M21-01 至 TC-M21-06 验收标准
 */
class BaselineServiceTest {

    private BaselineRepository repository;
    private BaselineService service;

    @BeforeEach
    void setUp() {
        repository = new BaselineRepository();
        service = new BaselineService(repository);
    }

    @Test
    @DisplayName("TC-M21-01: 基线全要素圈定与快照生成测试")
    void testTC_M21_01_ScopeAndSnapshotCreation() {
        Long projectId = 101L;

        CreateBaselineRequest request = new CreateBaselineRequest();
        request.setBaselineCode("BL-TEST-CDR-001");
        request.setName("测试CDR冻结基线");
        request.setPurpose(BaselinePurpose.PRODUCT_DESIGN_BASELINE);
        request.setProjectId(projectId);

        List<CreateBaselineRequest.BaselineMemberInput> members = new ArrayList<>();

        CreateBaselineRequest.BaselineMemberInput m1 = new CreateBaselineRequest.BaselineMemberInput();
        m1.setRevisionId(6001L);
        m1.setMemberRole(MemberRole.EBOM_ROOT);
        m1.setObjectTypeCode("PartRevision");
        m1.setBusinessCode("M-TEST-001");
        m1.setRevisionLabel("A.1");
        m1.setContentHash("1111111122222222333333334444444455555555666666667777777788888888");
        members.add(m1);

        CreateBaselineRequest.BaselineMemberInput m2 = new CreateBaselineRequest.BaselineMemberInput();
        m2.setRevisionId(6002L);
        m2.setMemberRole(MemberRole.CAD_DRAWING);
        m2.setObjectTypeCode("DocRevision");
        m2.setBusinessCode("DOC-TEST-001");
        m2.setRevisionLabel("A.1");
        m2.setContentHash("9999999988888888777777776666666655555555444444443333333322222222");
        members.add(m2);

        request.setInitialMembers(members);

        BaselineEntity created = service.createBaseline(request);
        assertNotNull(created.getBaselineId());
        assertEquals(BaselineState.DRAFT, created.getState());

        BaselineDetailDto detail = service.getBaselineDetail(created.getBaselineId());
        assertEquals(2, detail.getMembers().size());
        assertTrue(detail.getMembers().stream().anyMatch(m -> m.getMemberRole() == MemberRole.EBOM_ROOT));
    }

    @Test
    @DisplayName("TC-M21-02: 闭包完备性阻断与通过测试 (含 64位 Merkle closure_hash 计算)")
    void testTC_M21_02_ClosureCompletenessValidation() {
        // 创建包含草稿对象的基线
        CreateBaselineRequest request = new CreateBaselineRequest();
        request.setBaselineCode("BL-TEST-DRAFT-CLOSURE");
        request.setName("含草稿成员的待测基线");
        request.setPurpose(BaselinePurpose.ALLOCATED_BASELINE);
        request.setProjectId(101L);

        List<CreateBaselineRequest.BaselineMemberInput> members = new ArrayList<>();
        CreateBaselineRequest.BaselineMemberInput m1 = new CreateBaselineRequest.BaselineMemberInput();
        m1.setRevisionId(6003L);
        m1.setBusinessCode("M-DRAFT-001");
        m1.setRevisionLabel("DRAFT"); // 草稿状态
        m1.setMemberRole(MemberRole.EBOM_ROOT);
        m1.setContentHash("1234567812345678123456781234567812345678123456781234567812345678");
        members.add(m1);
        request.setInitialMembers(members);

        BaselineEntity baseline = service.createBaseline(request);

        // 校验闭包：预期失败并阻断
        ClosureCheckResultDto checkResult = service.validateClosure(baseline.getBaselineId());
        assertFalse(checkResult.getIsComplete(), "包含草稿成员时闭包校验应不通过");
        assertTrue(checkResult.getIssues().stream().anyMatch(i -> "DRAFT_STATE".equals(i.getIssueType())));

        // 尝试强行冻结应抛出 ClosureValidationException
        FreezeBaselineRequest freezeReq = new FreezeBaselineRequest(baseline.getBaselineId(), "test_user", "试冻结", null);
        assertThrows(ClosureValidationException.class, () -> service.freezeBaseline(freezeReq));

        // 清理草稿成员，加入正式发布的受控成员
        List<BaselineMemberEntity> curMembers = repository.findMembersByBaselineId(baseline.getBaselineId());
        for (BaselineMemberEntity m : curMembers) {
            service.removeMember(baseline.getBaselineId(), m.getMemberId());
        }

        BaselineMemberEntity validMember = new BaselineMemberEntity();
        validMember.setRevisionId(6004L);
        validMember.setObjectTypeCode("PartRevision");
        validMember.setBusinessCode("M-RELEASED-001");
        validMember.setRevisionLabel("A.1");
        validMember.setMemberRole(MemberRole.EBOM_ROOT);
        validMember.setContentHash("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        service.addMemberToBaseline(baseline.getBaselineId(), validMember);

        // 重新校验：预期通过并产出 64 位 Merkle closure_hash
        ClosureCheckResultDto passedCheck = service.validateClosure(baseline.getBaselineId());
        assertTrue(passedCheck.getIsComplete(), "全部为受控成员后闭包完备性应通过");
        assertNotNull(passedCheck.getClosureHash());
        assertEquals(64, passedCheck.getClosureHash().length(), "闭包哈希必须为标准64位SHA256十六进制");
    }

    @Test
    @DisplayName("TC-M21-03: 审批冻结与 CST-M21-01 不可变性强校验")
    void testTC_M21_03_FreezeAndImmutabilityEnforcement() {
        // 使用种子数据中已冻结的 VMC850 CDR 基线 (ID: 1001L)
        Long frozenId = 1001L;
        BaselineDetailDto detail = service.getBaselineDetail(frozenId);
        assertEquals(BaselineState.FROZEN, detail.getBaseline().getState());
        assertEquals(64, detail.getBaseline().getClosureHash().length());

        // 尝试向冻结基线直接追加成员 -> 必须抛出 BaselineImmutableViolationException
        BaselineMemberEntity illegalMember = new BaselineMemberEntity();
        illegalMember.setRevisionId(9999L);
        illegalMember.setBusinessCode("M-ILLEGAL-01");
        illegalMember.setRevisionLabel("A");
        assertThrows(BaselineImmutableViolationException.class, () -> {
            service.addMemberToBaseline(frozenId, illegalMember);
        }, "冻结基线禁止添加成员");

        // 尝试删除冻结基线 -> 必须抛出 BaselineImmutableViolationException
        assertThrows(BaselineImmutableViolationException.class, () -> {
            service.deleteBaseline(frozenId);
        }, "冻结基线禁止删除");
    }

    @Test
    @DisplayName("TC-M21-04: 红线差分比对引擎测试 (双基线 Diff)")
    void testTC_M21_04_RedlineDiffEngine() {
        Long b1Id = 1001L; // CDR 基线
        Long b2Id = 1002L; // PLAN 基线

        BaselineDiffResultDto diff = service.compareBaselines(b1Id, b2Id);
        assertNotNull(diff);
        assertEquals("BL-VMC850-CDR-001", diff.getBaselineCodeA());
        assertEquals("BL-VMC850-PLAN-001", diff.getBaselineCodeB());
        assertFalse(diff.getIsIdentical(), "不同阶段基线哈希与成员不同，不应相同");
        assertNotNull(diff.getMemberDifferences());
    }

    @Test
    @DisplayName("TC-M21-05: 多形态配置状态解耦与 SN001 机床绑定测试")
    void testTC_M21_05_MultiStateConfigurationDecoupling() {
        Long b1Id = 1001L;

        BindConfigurationStateRequest req = new BindConfigurationStateRequest();
        req.setBaselineId(b1Id);
        req.setConfigStateType(BaselinePurpose.AS_DELIVERED);
        req.setSerialNumber("VMC850-2026-DELIVERED-001");
        req.setNotes("客户验收交付基线配置绑定");
        req.setBoundBy("sys_chief_engineer");

        ConfigurationStateReferenceEntity bound = service.bindConfigurationState(req);
        assertNotNull(bound.getConfigRefId());
        assertEquals(BaselinePurpose.AS_DELIVERED, bound.getConfigStateType());
        assertEquals("VMC850-2026-DELIVERED-001", bound.getSerialNumber());
        assertTrue(bound.getIsActive());

        BaselineDetailDto detail = service.getBaselineDetail(b1Id);
        assertTrue(detail.getConfigurationStates().stream().anyMatch(c -> BaselinePurpose.AS_DELIVERED.equals(c.getConfigStateType())));
    }

    @Test
    @DisplayName("TC-M21-06: 基线演进与后继派生测试 (Successor Branch)")
    void testTC_M21_06_DeriveSuccessorBaseline() {
        Long b1Id = 1001L;

        BaselineEntity successor = service.deriveSuccessorBaseline(
                b1Id,
                "BL-VMC850-CDR-002",
                "VMC850主轴温控优化演进基线",
                "ECR-2026-088主轴轴承冷却油路优化",
                8003L,
                "lead_engineer"
        );

        assertNotNull(successor.getBaselineId());
        assertEquals("BL-VMC850-CDR-002", successor.getBaselineCode());
        assertEquals(BaselineState.DRAFT, successor.getState(), "派生的基线应当处于初始草稿态");

        // 验证成员已从原基线继承拷贝
        BaselineDetailDto succDetail = service.getBaselineDetail(successor.getBaselineId());
        assertFalse(succDetail.getMembers().isEmpty(), "派生基线应继承前驱基线成员副本");

        // 验证演进链包含前驱关联
        assertEquals(1, succDetail.getSuccessorLinks().size());
        assertEquals(b1Id, succDetail.getSuccessorLinks().get(0).getPredecessorBaselineId());
    }
}
