package com.ccdd.document.entity;

import java.time.Instant;

/**
 * M19 派生制品谱系来源实体 (ArtifactDerivation - 明确 PDF/轻量化网格与原生源文件的谱系关系)
 */
public class ArtifactDerivationEntity {

    private Long derivationId;
    private Long sourceArtifactId;
    private Long derivedArtifactId;
    private String converterEngine; // 如 LibreOffice, PDFTron, OCC_Converter
    private String converterVersion;
    private String derivationParameters; // JSON 字符串: 水印文字、精度配置
    private Instant convertedAt;

    public ArtifactDerivationEntity() {
    }

    public ArtifactDerivationEntity(Long derivationId, Long sourceArtifactId, Long derivedArtifactId,
                                    String converterEngine, String converterVersion,
                                    String derivationParameters, Instant convertedAt) {
        this.derivationId = derivationId;
        this.sourceArtifactId = sourceArtifactId;
        this.derivedArtifactId = derivedArtifactId;
        this.converterEngine = converterEngine;
        this.converterVersion = converterVersion;
        this.derivationParameters = derivationParameters;
        this.convertedAt = convertedAt;
    }

    public Long getDerivationId() {
        return derivationId;
    }

    public void setDerivationId(Long derivationId) {
        this.derivationId = derivationId;
    }

    public Long getSourceArtifactId() {
        return sourceArtifactId;
    }

    public void setSourceArtifactId(Long sourceArtifactId) {
        this.sourceArtifactId = sourceArtifactId;
    }

    public Long getDerivedArtifactId() {
        return derivedArtifactId;
    }

    public void setDerivedArtifactId(Long derivedArtifactId) {
        this.derivedArtifactId = derivedArtifactId;
    }

    public String getConverterEngine() {
        return converterEngine;
    }

    public void setConverterEngine(String converterEngine) {
        this.converterEngine = converterEngine;
    }

    public String getConverterVersion() {
        return converterVersion;
    }

    public void setConverterVersion(String converterVersion) {
        this.converterVersion = converterVersion;
    }

    public String getDerivationParameters() {
        return derivationParameters;
    }

    public void setDerivationParameters(String derivationParameters) {
        this.derivationParameters = derivationParameters;
    }

    public Instant getConvertedAt() {
        return convertedAt;
    }

    public void setConvertedAt(Instant convertedAt) {
        this.convertedAt = convertedAt;
    }
}
