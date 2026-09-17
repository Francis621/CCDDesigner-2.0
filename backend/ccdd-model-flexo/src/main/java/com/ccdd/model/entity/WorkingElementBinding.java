package com.ccdd.model.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * M04: 工作期元素临时动态绑定实体 (WorkingElementBinding)
 * 落实两阶段绑定原则：草稿期间维系临时工作映射，发布后由 M06 转为受控数字主线
 */
public class WorkingElementBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long bindingId;
    private Long workspaceId;
    private String plmObjectType = "RequirementRevision";
    private Long plmObjectId;
    private String plmObjectCode;
    private String sysonElementId;
    private String elementType = "RequirementUsage";
    private String qualifiedName;
    private Instant lastSyncedAt;

    public WorkingElementBinding() {}

    public WorkingElementBinding(Long bindingId, Long workspaceId, String plmObjectType, Long plmObjectId,
                                 String plmObjectCode, String sysonElementId, String elementType,
                                 String qualifiedName, Instant lastSyncedAt) {
        this.bindingId = bindingId;
        this.workspaceId = workspaceId;
        this.plmObjectType = plmObjectType;
        this.plmObjectId = plmObjectId;
        this.plmObjectCode = plmObjectCode;
        this.sysonElementId = sysonElementId;
        this.elementType = elementType;
        this.qualifiedName = qualifiedName;
        this.lastSyncedAt = lastSyncedAt;
    }

    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }

    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }

    public String getPlmObjectType() { return plmObjectType; }
    public void setPlmObjectType(String plmObjectType) { this.plmObjectType = plmObjectType; }

    public Long getPlmObjectId() { return plmObjectId; }
    public void setPlmObjectId(Long plmObjectId) { this.plmObjectId = plmObjectId; }

    public String getPlmObjectCode() { return plmObjectCode; }
    public void setPlmObjectCode(String plmObjectCode) { this.plmObjectCode = plmObjectCode; }

    public String getSysonElementId() { return sysonElementId; }
    public void setSysonElementId(String sysonElementId) { this.sysonElementId = sysonElementId; }

    public String getElementType() { return elementType; }
    public void setElementType(String elementType) { this.elementType = elementType; }

    public String getQualifiedName() { return qualifiedName; }
    public void setQualifiedName(String qualifiedName) { this.qualifiedName = qualifiedName; }

    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(Instant lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}
