package com.ccdd.iam.controller;

import com.ccdd.common.api.Result;
import com.ccdd.iam.dto.AssignProjectMemberRequest;
import com.ccdd.iam.dto.ChangePasswordRequest;
import com.ccdd.iam.dto.CreateUserRequest;
import com.ccdd.iam.dto.DepartmentDto;
import com.ccdd.iam.dto.LoginRequest;
import com.ccdd.iam.dto.LoginResponse;
import com.ccdd.iam.dto.RegisterQualificationRequest;
import com.ccdd.iam.dto.RevokeMembershipResponse;
import com.ccdd.iam.dto.UpdateUserStatusRequest;
import com.ccdd.iam.dto.UserDetailDto;
import com.ccdd.iam.entity.SysProjectMembershipEntity;
import com.ccdd.iam.entity.SysQualificationEntity;
import com.ccdd.iam.entity.SysRoleEntity;
import com.ccdd.iam.entity.SysSessionRevocationEntity;
import com.ccdd.iam.service.IamService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * M30-IAM 系统用户、组织与权限管理控制器
 * 严格遵从 OpenAPI 3.0 规范协议契约
 */
@RestController
@RequestMapping("/api/v1")
public class IamController {

    private final IamService iamService;

    public IamController(IamService iamService) {
        this.iamService = iamService;
    }

    // ==================== 组织与角色 ====================

    @GetMapping("/departments")
    public Result<List<DepartmentDto>> getDepartments() {
        return Result.success(iamService.getDepartmentHierarchy());
    }

    @GetMapping("/roles")
    public Result<List<SysRoleEntity>> getRoles() {
        return Result.success(iamService.getAllRoles());
    }

    // ==================== 用户台账与生命周期 ====================

    @GetMapping("/users")
    public Result<List<UserDetailDto>> getUsers() {
        return Result.success(iamService.getAllUserDetails());
    }

    @GetMapping("/users/{userId}")
    public Result<UserDetailDto> getUserDetail(@PathVariable String userId) {
        return Result.success(iamService.getUserDetail(userId));
    }

    @PostMapping("/users")
    public Result<UserDetailDto> createUser(@RequestBody CreateUserRequest request) {
        return Result.success(iamService.createUser(request));
    }

    @PutMapping("/users/{userId}/status")
    public Result<UserDetailDto> updateUserStatus(@PathVariable String userId,
                                                        @RequestBody UpdateUserStatusRequest request) {
        return Result.success(iamService.updateUserStatus(userId, request));
    }

    // ==================== 项目工作组成员与细粒度授权 ====================

    @GetMapping("/projects/{projectId}/memberships")
    public Result<List<SysProjectMembershipEntity>> getProjectMemberships(@PathVariable Long projectId) {
        return Result.success(iamService.getProjectMemberships(projectId));
    }

    /**
     * OpenAPI 6.1: 分配用户至项目工作组并授予项目角色
     */
    @PostMapping("/projects/{projectId}/memberships")
    public Result<SysProjectMembershipEntity> assignProjectMember(
            @PathVariable Long projectId,
            @RequestBody AssignProjectMemberRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "ENG-ADMIN-001") String operatorId) {
        return Result.success(iamService.assignProjectMember(projectId, request, operatorId));
    }

    /**
     * OpenAPI 6.2: 撤销用户项目成员权限 (触发 AT-13 毫秒级熔断)
     */
    @DeleteMapping("/projects/{projectId}/memberships/{userId}")
    public Result<RevokeMembershipResponse> revokeProjectMember(
            @PathVariable Long projectId,
            @PathVariable String userId,
            @RequestHeader(value = "X-Reason", required = false) String reason) {
        return Result.success(iamService.revokeProjectMember(projectId, userId, reason));
    }

    // ==================== 专职工程资质认证 ====================

    @GetMapping("/users/{userId}/qualifications")
    public Result<List<SysQualificationEntity>> getUserQualifications(@PathVariable String userId) {
        return Result.success(iamService.getUserQualifications(userId));
    }

    /**
     * OpenAPI 6.3: 登记用户专职工程资质
     */
    @PostMapping("/users/{userId}/qualifications")
    public Result<SysQualificationEntity> registerQualification(
            @PathVariable String userId,
            @RequestBody RegisterQualificationRequest request) {
        return Result.success(iamService.registerQualification(userId, request));
    }

    // ==================== 会话吊销黑名单与审计 ====================

    @GetMapping("/iam/revocations")
    public Result<List<SysSessionRevocationEntity>> getRevocationAudits() {
        return Result.success(iamService.getRevocationAuditList());
    }

    // ==================== 身份认证与密码管理 ====================

    /**
     * OpenAPI: 用户账号密码登录认证
     */
    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(iamService.login(request));
    }

    /**
     * OpenAPI: 用户自主修改登录密码
     */
    @PostMapping("/users/{userId}/change-password")
    public Result<UserDetailDto> changePassword(
            @PathVariable String userId,
            @RequestBody ChangePasswordRequest request) {
        return Result.success(iamService.changePassword(userId, request));
    }

    /**
     * 兼容 REST 风格 PUT /users/{userId}/password
     */
    @PutMapping("/users/{userId}/password")
    public Result<UserDetailDto> updatePassword(
            @PathVariable String userId,
            @RequestBody ChangePasswordRequest request) {
        return Result.success(iamService.changePassword(userId, request));
    }
}
