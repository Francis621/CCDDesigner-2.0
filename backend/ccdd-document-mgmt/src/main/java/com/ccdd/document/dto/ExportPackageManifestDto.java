package com.ccdd.document.dto;

import java.time.Instant;
import java.util.List;

/**
 * 交付包根目录 manifest.json 与 checksums.sha256 结构化清单 (AT-14)
 */
public class ExportPackageManifestDto {

    private String packageId;
    private String packageName;
    private String generatedAt;
    private String generatedBy;
    private Integer totalFileCount;
    private Long totalSizeBytes;
    private String packageSha256Checksum;
    private String downloadPresignedUrl;
    private List<ManifestFileItemDto> files;

    public ExportPackageManifestDto() {
    }

    public ExportPackageManifestDto(String packageId, String packageName, String generatedAt, String generatedBy,
                                   Integer totalFileCount, Long totalSizeBytes, String packageSha256Checksum,
                                   String downloadPresignedUrl, List<ManifestFileItemDto> files) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.generatedAt = generatedAt;
        this.generatedBy = generatedBy;
        this.totalFileCount = totalFileCount;
        this.totalSizeBytes = totalSizeBytes;
        this.packageSha256Checksum = packageSha256Checksum;
        this.downloadPresignedUrl = downloadPresignedUrl;
        this.files = files;
    }

    public static class ManifestFileItemDto {
        private String relativePath;
        private Long artifactId;
        private String documentNumber;
        private String revisionLabel;
        private String fileRole;
        private Long fileSizeBytes;
        private String sha256Hash;

        public ManifestFileItemDto() {
        }

        public ManifestFileItemDto(String relativePath, Long artifactId, String documentNumber,
                                  String revisionLabel, String fileRole, Long fileSizeBytes, String sha256Hash) {
            this.relativePath = relativePath;
            this.artifactId = artifactId;
            this.documentNumber = documentNumber;
            this.revisionLabel = revisionLabel;
            this.fileRole = fileRole;
            this.fileSizeBytes = fileSizeBytes;
            this.sha256Hash = sha256Hash;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public void setRelativePath(String relativePath) {
            this.relativePath = relativePath;
        }

        public Long getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(Long artifactId) {
            this.artifactId = artifactId;
        }

        public String getDocumentNumber() {
            return documentNumber;
        }

        public void setDocumentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
        }

        public String getRevisionLabel() {
            return revisionLabel;
        }

        public void setRevisionLabel(String revisionLabel) {
            this.revisionLabel = revisionLabel;
        }

        public String getFileRole() {
            return fileRole;
        }

        public void setFileRole(String fileRole) {
            this.fileRole = fileRole;
        }

        public Long getFileSizeBytes() {
            return fileSizeBytes;
        }

        public void setFileSizeBytes(Long fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
        }

        public String getSha256Hash() {
            return sha256Hash;
        }

        public void setSha256Hash(String sha256Hash) {
            this.sha256Hash = sha256Hash;
        }
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Integer getTotalFileCount() {
        return totalFileCount;
    }

    public void setTotalFileCount(Integer totalFileCount) {
        this.totalFileCount = totalFileCount;
    }

    public Long getTotalSizeBytes() {
        return totalSizeBytes;
    }

    public void setTotalSizeBytes(Long totalSizeBytes) {
        this.totalSizeBytes = totalSizeBytes;
    }

    public String getPackageSha256Checksum() {
        return packageSha256Checksum;
    }

    public void setPackageSha256Checksum(String packageSha256Checksum) {
        this.packageSha256Checksum = packageSha256Checksum;
    }

    public String getDownloadPresignedUrl() {
        return downloadPresignedUrl;
    }

    public void setDownloadPresignedUrl(String downloadPresignedUrl) {
        this.downloadPresignedUrl = downloadPresignedUrl;
    }

    public List<ManifestFileItemDto> getFiles() {
        return files;
    }

    public void setFiles(List<ManifestFileItemDto> files) {
        this.files = files;
    }
}
