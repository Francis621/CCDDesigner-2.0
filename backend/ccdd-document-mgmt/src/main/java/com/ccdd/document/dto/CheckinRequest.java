package com.ccdd.document.dto;

/**
 * 文档签入请求契约 (M19-F01)
 */
public class CheckinRequest {

    private Long newArtifactId;      // 新上传完成的不可变制品 ID
    private String checkinComments;  // 签入工程变更说明
    private Boolean createDerivative; // 是否自动触发派生 PDF/网格渲染
    private String user;             // 签入操作人（必须与持锁人一致）

    public CheckinRequest() {
    }

    public CheckinRequest(Long newArtifactId, String checkinComments, Boolean createDerivative, String user) {
        this.newArtifactId = newArtifactId;
        this.checkinComments = checkinComments;
        this.createDerivative = createDerivative;
        this.user = user;
    }

    public Long getNewArtifactId() {
        return newArtifactId;
    }

    public void setNewArtifactId(Long newArtifactId) {
        this.newArtifactId = newArtifactId;
    }

    public String getCheckinComments() {
        return checkinComments;
    }

    public void setCheckinComments(String checkinComments) {
        this.checkinComments = checkinComments;
    }

    public Boolean getCreateDerivative() {
        return createDerivative;
    }

    public void setCreateDerivative(Boolean createDerivative) {
        this.createDerivative = createDerivative;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
