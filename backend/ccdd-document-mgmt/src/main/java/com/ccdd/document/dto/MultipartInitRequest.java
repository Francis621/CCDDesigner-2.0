package com.ccdd.document.dto;

import java.util.List;

/**
 * 分片上传初始化请求与响应契约 (OpenAPI §8.1)
 */
public class MultipartInitRequest {

    private String fileName;
    private Long fileSizeBytes;
    private String clientSha256;
    private Integer chunkCount;
    private String mimeType;
    private Long targetRevisionId;

    public MultipartInitRequest() {
    }

    public MultipartInitRequest(String fileName, Long fileSizeBytes, String clientSha256,
                                Integer chunkCount, String mimeType, Long targetRevisionId) {
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.clientSha256 = clientSha256;
        this.chunkCount = chunkCount;
        this.mimeType = mimeType;
        this.targetRevisionId = targetRevisionId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getClientSha256() {
        return clientSha256;
    }

    public void setClientSha256(String clientSha256) {
        this.clientSha256 = clientSha256;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getTargetRevisionId() {
        return targetRevisionId;
    }

    public void setTargetRevisionId(Long targetRevisionId) {
        this.targetRevisionId = targetRevisionId;
    }
}
