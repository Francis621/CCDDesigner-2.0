package com.ccdd.iam.entity;

/**
 * 全局职能角色实体
 */
public class SysRoleEntity {

    private String roleId;
    private String roleName;
    private String roleType; // FUNCTIONAL, SYSTEM_ADMIN
    private String description;
    private Boolean isSystemReserved;

    public SysRoleEntity() {
    }

    public SysRoleEntity(String roleId, String roleName, String roleType, String description, Boolean isSystemReserved) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleType = roleType;
        this.description = description;
        this.isSystemReserved = isSystemReserved;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsSystemReserved() {
        return isSystemReserved;
    }

    public void setIsSystemReserved(Boolean systemReserved) {
        isSystemReserved = systemReserved;
    }

    @Override
    public String toString() {
        return "SysRoleEntity{" +
                "roleId='" + roleId + '\'' +
                ", roleName='" + roleName + '\'' +
                ", roleType='" + roleType + '\'' +
                '}';
    }
}
