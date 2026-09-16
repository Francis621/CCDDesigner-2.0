package com.ccdd.storage.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 分片上传协议 DTO 集合 (落实 D06 专项规格)
 */
public class MultipartUploadDto {

    public static class InitiateMultipartRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private String bucketName;
        private String objectKey;
        private String contentType;
        private long totalSizeBytes;
        private int partCount;

        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
        public String getObjectKey() { return objectKey; }
        public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public long getTotalSizeBytes() { return totalSizeBytes; }
        public void setTotalSizeBytes(long totalSizeBytes) { this.totalSizeBytes = totalSizeBytes; }
        public int getPartCount() { return partCount; }
        public void setPartCount(int partCount) { this.partCount = partCount; }
    }

    public static class InitiateMultipartResponse implements Serializable {
        private static final long serialVersionUID = 1L;
        private String uploadId;
        private String bucketName;
        private String objectKey;
        private List<PresignedPartUrl> partUrls;

        public InitiateMultipartResponse() {}
        public InitiateMultipartResponse(String uploadId, String bucketName, String objectKey, List<PresignedPartUrl> partUrls) {
            this.uploadId = uploadId;
            this.bucketName = bucketName;
            this.objectKey = objectKey;
            this.partUrls = partUrls;
        }

        public String getUploadId() { return uploadId; }
        public void setUploadId(String uploadId) { this.uploadId = uploadId; }
        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
        public String getObjectKey() { return objectKey; }
        public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
        public List<PresignedPartUrl> getPartUrls() { return partUrls; }
        public void setPartUrls(List<PresignedPartUrl> partUrls) { this.partUrls = partUrls; }
    }

    public static class PresignedPartUrl implements Serializable {
        private static final long serialVersionUID = 1L;
        private int partNumber;
        private String uploadUrl;

        public PresignedPartUrl() {}
        public PresignedPartUrl(int partNumber, String uploadUrl) {
            this.partNumber = partNumber;
            this.uploadUrl = uploadUrl;
        }

        public int getPartNumber() { return partNumber; }
        public void setPartNumber(int partNumber) { this.partNumber = partNumber; }
        public String getUploadUrl() { return uploadUrl; }
        public void setUploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; }
    }

    public static class PartInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private int partNumber;
        private String eTag;

        public PartInfo() {}
        public PartInfo(int partNumber, String eTag) {
            this.partNumber = partNumber;
            this.eTag = eTag;
        }

        public int getPartNumber() { return partNumber; }
        public void setPartNumber(int partNumber) { this.partNumber = partNumber; }
        public String getETag() { return eTag; }
        public void setETag(String eTag) { this.eTag = eTag; }
    }

    public static class CompleteMultipartRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private String uploadId;
        private String bucketName;
        private String objectKey;
        private List<PartInfo> parts;
        private String expectedSha256; // 客户端上报的制品预期 SHA256 (用于防篡改校验)

        public String getUploadId() { return uploadId; }
        public void setUploadId(String uploadId) { this.uploadId = uploadId; }
        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
        public String getObjectKey() { return objectKey; }
        public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
        public List<PartInfo> getParts() { return parts; }
        public void setParts(List<PartInfo> parts) { this.parts = parts; }
        public String getExpectedSha256() { return expectedSha256; }
        public void setExpectedSha256(String expectedSha256) { this.expectedSha256 = expectedSha256; }
    }

    public static class CompleteMultipartResponse implements Serializable {
        private static final long serialVersionUID = 1L;
        private String objectUri;
        private String sha256;
        private long totalSizeBytes;
        private boolean integrityVerified;

        public CompleteMultipartResponse() {}
        public CompleteMultipartResponse(String objectUri, String sha256, long totalSizeBytes, boolean integrityVerified) {
            this.objectUri = objectUri;
            this.sha256 = sha256;
            this.totalSizeBytes = totalSizeBytes;
            this.integrityVerified = integrityVerified;
        }

        public String getObjectUri() { return objectUri; }
        public void setObjectUri(String objectUri) { this.objectUri = objectUri; }
        public String getSha256() { return sha256; }
        public void setSha256(String sha256) { this.sha256 = sha256; }
        public long getTotalSizeBytes() { return totalSizeBytes; }
        public void setTotalSizeBytes(long totalSizeBytes) { this.totalSizeBytes = totalSizeBytes; }
        public boolean isIntegrityVerified() { return integrityVerified; }
        public void setIntegrityVerified(boolean integrityVerified) { this.integrityVerified = integrityVerified; }
    }

    public static class DocumentPresignedDto implements Serializable {
        private static final long serialVersionUID = 1L;
        private String bucketName;
        private String objectKey;
        private String uploadUrl;
        private String downloadUrl;
        private int expiresInSeconds;

        public DocumentPresignedDto() {}

        public DocumentPresignedDto(String bucketName, String objectKey, String uploadUrl, String downloadUrl, int expiresInSeconds) {
            this.bucketName = bucketName;
            this.objectKey = objectKey;
            this.uploadUrl = uploadUrl;
            this.downloadUrl = downloadUrl;
            this.expiresInSeconds = expiresInSeconds;
        }

        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
        public String getObjectKey() { return objectKey; }
        public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
        public String getUploadUrl() { return uploadUrl; }
        public void setUploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; }
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
        public int getExpiresInSeconds() { return expiresInSeconds; }
        public void setExpiresInSeconds(int expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
    }
}
