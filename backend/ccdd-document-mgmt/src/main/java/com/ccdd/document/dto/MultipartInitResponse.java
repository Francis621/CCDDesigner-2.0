package com.ccdd.document.dto;

import java.time.Instant;
import java.util.List;

/**
 * 分片上传初始化响应契约 (OpenAPI §8.1)
 */
public class MultipartInitResponse {

    private String uploadId;
    private String storageBucket;
    private String objectPath;
    private Long partSize;
    private List<PresignedPartUrlDto> presignedPartUrls;
    private Instant expiresAt;

    public MultipartInitResponse() {
    }

    public MultipartInitResponse(String uploadId, String storageBucket, String objectPath,
                                 Long partSize, List<PresignedPartUrlDto> presignedPartUrls, Instant expiresAt) {
        this.uploadId = uploadId;
        this.storageBucket = storageBucket;
        this.objectPath = objectPath;
        this.partSize = partSize;
        this.presignedPartUrls = presignedPartUrls;
        this.expiresAt = expiresAt;
    }

    public static class PresignedPartUrlDto {
        private Integer partNumber;
        private String uploadUrl;

        public PresignedPartUrlDto() {
        }

        public PresignedPartUrlDto(Integer partNumber, String uploadUrl) {
            this.partNumber = partNumber;
            this.uploadUrl = uploadUrl;
        }

        public Integer getPartNumber() {
            return partNumber;
        }

        public void setPartNumber(Integer partNumber) {
            this.partNumber = partNumber;
        }

        public String getUploadUrl() {
            return uploadUrl;
        }

        public void setUploadUrl(String uploadUrl) {
            this.uploadUrl = uploadUrl;
        }
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public void setStorageBucket(String storageBucket) {
        this.storageBucket = storageBucket;
    }

    public String getObjectPath() {
        return objectPath;
    }

    public void setObjectPath(String objectPath) {
        this.objectPath = objectPath;
    }

    public Long getPartSize() {
        return partSize;
    }

    public void setPartSize(Long partSize) {
        this.partSize = partSize;
    }

    public List<PresignedPartUrlDto> getPresignedPartUrls() {
        return presignedPartUrls;
    }

    public void setPresignedPartUrls(List<PresignedPartUrlDto> presignedPartUrls) {
        this.presignedPartUrls = presignedPartUrls;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
