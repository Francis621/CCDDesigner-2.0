package com.ccdd.iam;

import com.ccdd.iam.aspect.EngineeringSoDGuardAspect;
import com.ccdd.iam.dto.AssignProjectMemberRequest;
import com.ccdd.iam.dto.RevokeMembershipResponse;
import com.ccdd.iam.entity.ProjectRoleType;
import com.ccdd.iam.entity.SysProjectMembershipEntity;
import com.ccdd.iam.exception.AdminSignForbiddenException;
import com.ccdd.iam.exception.FieldWriteProhibitedException;
import com.ccdd.iam.exception.InsufficientQualificationException;
import com.ccdd.iam.exception.SecurityAccessDeniedException;
import com.ccdd.iam.exception.SelfApprovalForbiddenException;
import com.ccdd.iam.repository.IamRepository;
import com.ccdd.iam.service.IamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * M30-IAM 验收测试套件
 * 严格遵照规格说明书第 7 节验收测试矩阵 (TC-IAM-01 ~ TC-IAM-06)
 */
public class IamServiceTest {

    private IamRepository iamRepository;
    private EngineeringSoDGuardAspect soDGuardAspect;
    private IamService iamService;

    private static final Long VMC1000_PROJECT_ID = 100293810293L;
    private static final Long HMC800_PROJECT_ID = 200593810888L; // 卧式加工中心在研项目

    @BeforeEach
    void setUp() {
        iamRepository = new IamRepository();
        soDGuardAspect = new EngineeringSoDGuardAspect();
        iamService = new IamService(iamRepository, soDGuardAspect);
    }

    /**
     * TC-IAM-01: AT-13 权限即时吊销与全链路失效机制
     * 场景：管理员从项目成员组移除外协专家 ENG-EXT-01，系统记录撤销黑名单，立即阻断后续访问
     */
    @Test
    @DisplayName("TC-IAM-01: [AT-13] 权限即时吊销与全链路失效测试")
    void testTcIam01_ImmediatePermissionRevocation() {
        // 先确保 ENG-2048 属于项目
        Optional<SysProjectMembershipEntity> before = iamRepository.findActiveMembership(VMC1000_PROJECT_ID, "ENG-2048");
        assertTrue(before.isPresent(), "张建国初始应属于 VMC1000 研发项目组");

        // 执行移除
        RevokeMembershipResponse resp = iamService.revokeProjectMember(VMC1000_PROJECT_ID, "ENG-2048", "岗位调动，移出五轴项目组");
        assertTrue(resp.getRevoked());
        assertTrue(resp.getSessionBlacklistPushed(), "黑名单广播应已推送");
        assertTrue(resp.getMinioPresignedRevoked(), "MinIO 直链应已吊销");

        // 再次检查成员状态
        Optional<SysProjectMembershipEntity> after = iamRepository.findActiveMembership(VMC1000_PROJECT_ID, "ENG-2048");
        assertTrue(after.isEmpty(), "成员应已被置为非激活状态");

        // 验证后续访问被 AT-13 即时拦截阻断
        SecurityAccessDeniedException ex = assertThrows(SecurityAccessDeniedException.class, () -> {
            iamService.checkProjectAccess(VMC1000_PROJECT_ID, "ENG-2048", ProjectRoleType.DESIGNER);
        });
        assertEquals("ERR_PERMISSION_REVOKED", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("AT-13 鉴权拦截"));
    }

    /**
     * TC-IAM-02: SoD-01 禁止创建人自审自批
     * 场景：系统工程师 A 提交了模型发布，在工作流中以审批人身份签署
     */
    @Test
    @DisplayName("TC-IAM-02: [SoD-01] 禁止创建人自审自批硬拦截测试")
    void testTcIam02_SoD01_SelfApprovalForbidden() {
        String creatorId = "ENG-3001";
        String currentApproverId = "ENG-3001";

        SelfApprovalForbiddenException ex = assertThrows(SelfApprovalForbiddenException.class, () -> {
            iamService.verifySoD01SelfApproval(creatorId, currentApproverId);
        });
        assertEquals("ERR_SOD_SELF_APPROVAL_FORBIDDEN", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("严禁作为自身提交申请的审批签署人"));

        // 他人审批则正常通过
        iamService.verifySoD01SelfApproval("ENG-3001", "ENG-5003");
    }

    /**
     * TC-IAM-03: SoD-02 专职验证资质审查分离
     * 场景：仿真工程师王强 (ENG-4002) 尝试自签 PASS 结论，因未登记 VERIFICATION_REVIEWER 资质被强拦截
     */
    @Test
    @DisplayName("TC-IAM-03: [SoD-02] 签署需求验证 PASS 必须具备专职审查员资质")
    void testTcIam03_SoD02_VerificationQualificationRequired() {
        // 王强 (ENG-4002) 未登记专职审查员资质
        InsufficientQualificationException ex = assertThrows(InsufficientQualificationException.class, () -> {
            iamService.verifySoD02VerificationQualification("ENG-4002", "PASS");
        });
        assertEquals("ERR_SOD_INSUFFICIENT_QUALIFICATION", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("VERIFICATION_REVIEWER"));

        // 赵晓华 (ENG-5003) 具备资质，签署通过
        iamService.verifySoD02VerificationQualification("ENG-5003", "PASS");
    }

    /**
     * TC-IAM-04: SoD-03 现场人员反写已发布设计阻断
     * 场景：车间装配工钱师傅 (ENG-7005) 尝试修改已发布设计 EBOM (PartRevision)
     */
    @Test
    @DisplayName("TC-IAM-04: [SoD-03] 现场制造与服务人员反写设计定义阻断测试")
    void testTcIam04_SoD03_FieldWriteProhibited() {
        FieldWriteProhibitedException ex = assertThrows(FieldWriteProhibitedException.class, () -> {
            iamService.verifySoD03FieldWriteAccess("ENG-7005", "PartRevision");
        });
        assertEquals("ERR_SOD_FIELD_WRITE_PROHIBITED", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("严禁直接反写已发布设计定义"));

        // 机械总工 (ENG-2048) 有权编辑设计定义草稿
        iamService.verifySoD03FieldWriteAccess("ENG-2048", "PartRevision");
    }

    /**
     * TC-IAM-05: SoD-04 系统管理员越权放行工程基线阻断
     * 场景：IT 系统管理员 admin 尝试调用基线冻结放行 (FREEZE_BASELINE)
     */
    @Test
    @DisplayName("TC-IAM-05: [SoD-04] 系统管理员严禁代行工程技术文件签署放行测试")
    void testTcIam05_SoD04_AdminSignForbidden() {
        AdminSignForbiddenException ex = assertThrows(AdminSignForbiddenException.class, () -> {
            iamService.verifySoD04AdminEngineeringSign("ENG-ADMIN-001", "FREEZE_BASELINE");
        });
        assertEquals("ERR_SOD_ADMIN_SIGN_FORBIDDEN", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("系统管理员"));
        assertTrue(ex.getMessage().contains("FREEZE_BASELINE"));

        // 主设总工非管理员，不受 SoD-04 限制
        iamService.verifySoD04AdminEngineeringSign("ENG-2048", "FREEZE_BASELINE");
    }

    /**
     * TC-IAM-06: 项目边界控制与跨项目隔离
     * 场景：工程师张建国 (ENG-2048) 仅属于 VMC1000 项目组，尝试操作未授权的 HMC800 卧式加工中心项目
     */
    @Test
    @DisplayName("TC-IAM-06: [项目边界控制] 未授权项目跨工程串改隔离测试")
    void testTcIam06_ProjectBoundaryAccessControl() {
        // 张建国访问 VMC1000 项目：成功
        iamService.checkProjectAccess(VMC1000_PROJECT_ID, "ENG-2048", ProjectRoleType.DESIGNER);

        // 张建国尝试跨项目操作未加入的 HMC800：硬性阻断
        SecurityAccessDeniedException ex = assertThrows(SecurityAccessDeniedException.class, () -> {
            iamService.checkProjectAccess(HMC800_PROJECT_ID, "ENG-2048", ProjectRoleType.DESIGNER);
        });
        assertEquals("ERR_PROJECT_MEMBERSHIP_REQUIRED", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("未加入机床研制项目"));
    }
}
