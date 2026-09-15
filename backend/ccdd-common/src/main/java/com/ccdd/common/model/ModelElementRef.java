package com.ccdd.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * SysML v2 模型元素全局稳定唯一四元组引用契约
 * 形式化定义: ModelElementRef = <repositoryId, modelProjectId, commitId, elementId>
 * 严禁使用易变的树状显示路径 (displayPath) 作为历史引用键
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelElementRef implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Flexo 存储库标识 (如 flexo-repo-main) */
    private String repositoryId;

    /** 模型工程标识 (如 p-vmc1000-sys) */
    private String modelProjectId;

    /** 绑定的不可变 Commit ID */
    private String commitId;

    /** 模型内部稳定的元素唯一UUID */
    private String elementId;

    /** 易变的树状显示路径 (仅供 UI 导航和日志阅读，严禁作为业务外键) */
    private String displayPath;

    /**
     * 序列化为规范统一 URI 格式:
     * ccdd://flexo/{repositoryId}/{modelProjectId}/{commitId}/{elementId}
     */
    public String toGlobalUri() {
        return String.format("ccdd://flexo/%s/%s/%s/%s",
                repositoryId, modelProjectId, commitId, elementId);
    }

    /**
     * 从 URI 反解析四元组
     */
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
