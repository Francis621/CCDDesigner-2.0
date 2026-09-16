package com.ccdd.document.entity;

import java.time.Instant;

/**
 * M19 在线协同批注图层实体 (DocumentAnnotation - 矢量非破坏性批注)
 */
public class DocumentAnnotationEntity {

    private Long annotationId;
    private Long revisionId;
    private Long targetArtifactId;
    private Integer pageNumber;
    private String annotationType; // TEXT, HIGHLIGHT, DRAWING, RECTANGLE
    private String geometryData;   // JSON 矢量坐标与尺寸数据
    private String contentText;    // 批注文本与评审意见
    private String authorId;
    private Boolean isResolved;
    private Instant createdAt;
    private Instant updatedAt;

    public DocumentAnnotationEntity() {
    }

    public DocumentAnnotationEntity(Long annotationId, Long revisionId, Long targetArtifactId,
                                    Integer pageNumber, String annotationType, String geometryData,
                                    String contentText, String authorId, Boolean isResolved,
                                    Instant createdAt, Instant updatedAt) {
        this.annotationId = annotationId;
        this.revisionId = revisionId;
        this.targetArtifactId = targetArtifactId;
        this.pageNumber = pageNumber;
        this.annotationType = annotationType;
        this.geometryData = geometryData;
        this.contentText = contentText;
        this.authorId = authorId;
        this.isResolved = isResolved;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(Long annotationId) {
        this.annotationId = annotationId;
    }

    public Long getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(Long revisionId) {
        this.revisionId = revisionId;
    }

    public Long getTargetArtifactId() {
        return targetArtifactId;
    }

    public void setTargetArtifactId(Long targetArtifactId) {
        this.targetArtifactId = targetArtifactId;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public String getGeometryData() {
        return geometryData;
    }

    public void setGeometryData(String geometryData) {
        this.geometryData = geometryData;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Boolean resolved) {
        isResolved = resolved;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
