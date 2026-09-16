package com.ccdd.document.dto;

import java.util.List;

/**
 * 分片合并与哈希强校验请求契约 (OpenAPI §8.2)
 */
public class MultipartCompleteRequest {

    private String uploadId;
    private String expectedSha256;
    private List<UploadedPartDto> parts;

    public MultipartCompleteRequest() {
    }

    public MultipartCompleteRequest(String uploadId, String expectedSha256, List<UploadedPartDto> parts) {
        this.uploadId = uploadId;
        this.expectedSha256 = expectedSha256;
        this.parts = parts;
    }

    public static class UploadedPartDto {
        private Integer partNumber;
        private String eTag;

        public UploadedPartDto() {
        }

        public UploadedPartDto(Integer partNumber, String eTag) {
            this.partNumber = partNumber;
            this.eTag = eTag;
        }

        public Integer getPartNumber() {
            return partNumber;
        }

        public void setPartNumber(Integer partNumber) {
            this.partNumber = partNumber;
        }

        public String getETag() {
            return eTag;
        }

        public void setETag(String eTag) {
            this.eTag = eTag;
        }
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getExpectedSha256() {
        return expectedSha256;
    }

    public void setExpectedSha256(String expectedSha256) {
        this.expectedSha256 = expectedSha256;
    }

    public List<UploadedPartDto> getParts() {
        return parts;
    }

    public void setParts(List<UploadedPartDto> parts) {
        this.parts = parts;
    }
}
