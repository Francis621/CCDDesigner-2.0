package com.ccdd.common.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * SysML v2 模型元素全局稳定唯一四元组引用契约 (纯原生 Java 实现)
 * 形式化定义: ModelElementRef = <repositoryId, modelProjectId, commitId, elementId>
 */
public class ModelElementRef implements Serializable {

    private static final long serialVersionUID = 1L;

    private String repositoryId;
    private String modelProjectId;
    private String commitId;
    private String elementId;
    private String displayPath;

    public ModelElementRef() {
    }

    public ModelElementRef(String repositoryId, String modelProjectId, String commitId, String elementId, String displayPath) {
        this.repositoryId = repositoryId;
        this.modelProjectId = modelProjectId;
        this.commitId = commitId;
        this.elementId = elementId;
        this.displayPath = displayPath;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String repositoryId;
        private String modelProjectId;
        private String commitId;
        private String elementId;
        private String displayPath;

        public Builder repositoryId(String repositoryId) {
            this.repositoryId = repositoryId;
            return this;
        }

        public Builder modelProjectId(String modelProjectId) {
            this.modelProjectId = modelProjectId;
            return this;
        }

        public Builder commitId(String commitId) {
            this.commitId = commitId;
            return this;
        }

        public Builder elementId(String elementId) {
            this.elementId = elementId;
            return this;
        }

        public Builder displayPath(String displayPath) {
            this.displayPath = displayPath;
            return this;
        }

        public ModelElementRef build() {
            return new ModelElementRef(repositoryId, modelProjectId, commitId, elementId, displayPath);
        }
    }

    public String toGlobalUri() {
        return String.format("ccdd://flexo/%s/%s/%s/%s",
                repositoryId, modelProjectId, commitId, elementId);
    }

    public static ModelElementRef parseUri(String uri) {
        if (uri == null || !uri.startsWith("ccdd://flexo/")) {
            throw new IllegalArgumentException("非法的 SysML v2 四元组 URI 格式: " + uri);
        }
        String[] parts = uri.substring("ccdd://flexo/".length()).split("/");
        if (parts.length < 4) {
            throw new IllegalArgumentException("四元组 URI 段数不全: " + uri);
        }
        return ModelElementRef.builder()
                .repositoryId(parts[0])
                .modelProjectId(parts[1])
                .commitId(parts[2])
                .elementId(parts[3])
                .build();
    }

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public String getModelProjectId() { return modelProjectId; }
    public void setModelProjectId(String modelProjectId) { this.modelProjectId = modelProjectId; }
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
    public String getElementId() { return elementId; }
    public void setElementId(String elementId) { this.elementId = elementId; }
    public String getDisplayPath() { return displayPath; }
    public void setDisplayPath(String displayPath) { this.displayPath = displayPath; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelElementRef that = (ModelElementRef) o;
        return Objects.equals(repositoryId, that.repositoryId) &&
               Objects.equals(modelProjectId, that.modelProjectId) &&
               Objects.equals(commitId, that.commitId) &&
               Objects.equals(elementId, that.elementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryId, modelProjectId, commitId, elementId);
    }
}
