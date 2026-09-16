package com.ccdd.document.dto;

/**
 * 协同批注保存请求契约 (M19-F03)
 */
public class SaveAnnotationRequest {

    private Long targetArtifactId;
    private Integer pageNumber;
    private String annotationType; // TEXT, HIGHLIGHT, DRAWING, RECTANGLE
    private String geometryData;
    private String contentText;
    private String authorId;

    public SaveAnnotationRequest() {
    }

    public SaveAnnotationRequest(Long targetArtifactId, Integer pageNumber, String annotationType,
                                 String geometryData, String contentText, String authorId) {
        this.targetArtifactId = targetArtifactId;
        this.pageNumber = pageNumber;
        this.annotationType = annotationType;
        this.geometryData = geometryData;
        this.contentText = contentText;
        this.authorId = authorId;
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
}
