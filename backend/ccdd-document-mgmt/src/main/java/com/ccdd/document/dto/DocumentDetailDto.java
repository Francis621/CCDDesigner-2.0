package com.ccdd.document.dto;

import com.ccdd.document.entity.*;

import java.util.List;

/**
 * 图文档四层解耦聚合详情 DTO (Master -> Revision -> Dataset -> Artifact)
 */
public class DocumentDetailDto {

    private DocumentMasterEntity master;
    private DocumentRevisionEntity currentRevision;
    private List<DocumentRevisionEntity> revisions;
    private DocumentLockEntity currentLock;
    private List<DatasetItemDto> datasets;
    private List<ArtifactDerivationEntity> derivations;
    private List<DocumentAnnotationEntity> annotations;
    private List<CrossEngineeringLinkDto> crossLinks; // 跨域引用 (EBOM, 模型, 交付物, 检验证据)

    public DocumentDetailDto() {
    }

    public static class DatasetItemDto {
        private DatasetEntity dataset;
        private List<BoundArtifactDto> boundArtifacts;

        public DatasetItemDto() {
        }

        public DatasetItemDto(DatasetEntity dataset, List<BoundArtifactDto> boundArtifacts) {
            this.dataset = dataset;
            this.boundArtifacts = boundArtifacts;
        }

        public DatasetEntity getDataset() {
            return dataset;
        }

        public void setDataset(DatasetEntity dataset) {
            this.dataset = dataset;
        }

        public List<BoundArtifactDto> getBoundArtifacts() {
            return boundArtifacts;
        }

        public void setBoundArtifacts(List<BoundArtifactDto> boundArtifacts) {
            this.boundArtifacts = boundArtifacts;
        }
    }

    public static class BoundArtifactDto {
        private DatasetArtifactBindingEntity binding;
        private ArtifactEntity artifact;
        private String presignedDownloadUrl;
        private String presignedPreviewUrl;

        public BoundArtifactDto() {
        }

        public BoundArtifactDto(DatasetArtifactBindingEntity binding, ArtifactEntity artifact,
                                String presignedDownloadUrl, String presignedPreviewUrl) {
            this.binding = binding;
            this.artifact = artifact;
            this.presignedDownloadUrl = presignedDownloadUrl;
            this.presignedPreviewUrl = presignedPreviewUrl;
        }

        public DatasetArtifactBindingEntity getBinding() {
            return binding;
        }

        public void setBinding(DatasetArtifactBindingEntity binding) {
            this.binding = binding;
        }

        public ArtifactEntity getArtifact() {
            return artifact;
        }

        public void setArtifact(ArtifactEntity artifact) {
            this.artifact = artifact;
        }

        public String getPresignedDownloadUrl() {
            return presignedDownloadUrl;
        }

        public void setPresignedDownloadUrl(String presignedDownloadUrl) {
            this.presignedDownloadUrl = presignedDownloadUrl;
        }

        public String getPresignedPreviewUrl() {
            return presignedPreviewUrl;
        }

        public void setPresignedPreviewUrl(String presignedPreviewUrl) {
            this.presignedPreviewUrl = presignedPreviewUrl;
        }
    }

    public static class CrossEngineeringLinkDto {
        private String linkType; // PART_EBOM, SYSML_MODEL, DELIVERABLE_GATE, TEST_EVIDENCE
        private String targetEntityId;
        private String targetEntityName;
        private String targetVersion;
        private String status;

        public CrossEngineeringLinkDto() {
        }

        public CrossEngineeringLinkDto(String linkType, String targetEntityId,
                                       String targetEntityName, String targetVersion, String status) {
            this.linkType = linkType;
            this.targetEntityId = targetEntityId;
            this.targetEntityName = targetEntityName;
            this.targetVersion = targetVersion;
            this.status = status;
        }

        public String getLinkType() {
            return linkType;
        }

        public void setLinkType(String linkType) {
            this.linkType = linkType;
        }

        public String getTargetEntityId() {
            return targetEntityId;
        }

        public void setTargetEntityId(String targetEntityId) {
            this.targetEntityId = targetEntityId;
        }

        public String getTargetEntityName() {
            return targetEntityName;
        }

        public void setTargetEntityName(String targetEntityName) {
            this.targetEntityName = targetEntityName;
        }

        public String getTargetVersion() {
            return targetVersion;
        }

        public void setTargetVersion(String targetVersion) {
            this.targetVersion = targetVersion;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public DocumentMasterEntity getMaster() {
        return master;
    }

    public void setMaster(DocumentMasterEntity master) {
        this.master = master;
    }

    public DocumentRevisionEntity getCurrentRevision() {
        return currentRevision;
    }

    public void setCurrentRevision(DocumentRevisionEntity currentRevision) {
        this.currentRevision = currentRevision;
    }

    public List<DocumentRevisionEntity> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<DocumentRevisionEntity> revisions) {
        this.revisions = revisions;
    }

    public DocumentLockEntity getCurrentLock() {
        return currentLock;
    }

    public void setCurrentLock(DocumentLockEntity currentLock) {
        this.currentLock = currentLock;
    }

    public List<DatasetItemDto> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DatasetItemDto> datasets) {
        this.datasets = datasets;
    }

    public List<ArtifactDerivationEntity> getDerivations() {
        return derivations;
    }

    public void setDerivations(List<ArtifactDerivationEntity> derivations) {
        this.derivations = derivations;
    }

    public List<DocumentAnnotationEntity> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<DocumentAnnotationEntity> annotations) {
        this.annotations = annotations;
    }

    public List<CrossEngineeringLinkDto> getCrossLinks() {
        return crossLinks;
    }

    public void setCrossLinks(List<CrossEngineeringLinkDto> crossLinks) {
        this.crossLinks = crossLinks;
    }
}
