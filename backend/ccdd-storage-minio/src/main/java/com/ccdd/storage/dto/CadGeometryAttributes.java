package com.ccdd.storage.dto;

import java.io.Serializable;

/**
 * CAD 模型轻量化物理属性与包络盒 DTO (落实 D06 专项规格)
 */
public class CadGeometryAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double boundingBoxLengthMm;
    private Double boundingBoxWidthMm;
    private Double boundingBoxHeightMm;
    private Double volumeMm3;
    private Double surfaceAreaMm2;
    private Double massKg;
    private String materialName;
    private Double densityGPerCm3;

    public CadGeometryAttributes() {
    }

    public CadGeometryAttributes(Double boundingBoxLengthMm, Double boundingBoxWidthMm, Double boundingBoxHeightMm,
                                 Double volumeMm3, Double surfaceAreaMm2, Double massKg,
                                 String materialName, Double densityGPerCm3) {
        this.boundingBoxLengthMm = boundingBoxLengthMm;
        this.boundingBoxWidthMm = boundingBoxWidthMm;
        this.boundingBoxHeightMm = boundingBoxHeightMm;
        this.volumeMm3 = volumeMm3;
        this.surfaceAreaMm2 = surfaceAreaMm2;
        this.massKg = massKg;
        this.materialName = materialName;
        this.densityGPerCm3 = densityGPerCm3;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Double boundingBoxLengthMm;
        private Double boundingBoxWidthMm;
        private Double boundingBoxHeightMm;
        private Double volumeMm3;
        private Double surfaceAreaMm2;
        private Double massKg;
        private String materialName;
        private Double densityGPerCm3;

        public Builder boundingBoxLengthMm(Double val) { this.boundingBoxLengthMm = val; return this; }
        public Builder boundingBoxWidthMm(Double val) { this.boundingBoxWidthMm = val; return this; }
        public Builder boundingBoxHeightMm(Double val) { this.boundingBoxHeightMm = val; return this; }
        public Builder volumeMm3(Double val) { this.volumeMm3 = val; return this; }
        public Builder surfaceAreaMm2(Double val) { this.surfaceAreaMm2 = val; return this; }
        public Builder massKg(Double val) { this.massKg = val; return this; }
        public Builder materialName(String val) { this.materialName = val; return this; }
        public Builder densityGPerCm3(Double val) { this.densityGPerCm3 = val; return this; }

        public CadGeometryAttributes build() {
            return new CadGeometryAttributes(boundingBoxLengthMm, boundingBoxWidthMm, boundingBoxHeightMm,
                    volumeMm3, surfaceAreaMm2, massKg, materialName, densityGPerCm3);
        }
    }

    public Double getBoundingBoxLengthMm() { return boundingBoxLengthMm; }
    public void setBoundingBoxLengthMm(Double boundingBoxLengthMm) { this.boundingBoxLengthMm = boundingBoxLengthMm; }

    public Double getBoundingBoxWidthMm() { return boundingBoxWidthMm; }
    public void setBoundingBoxWidthMm(Double boundingBoxWidthMm) { this.boundingBoxWidthMm = boundingBoxWidthMm; }

    public Double getBoundingBoxHeightMm() { return boundingBoxHeightMm; }
    public void setBoundingBoxHeightMm(Double boundingBoxHeightMm) { this.boundingBoxHeightMm = boundingBoxHeightMm; }

    public Double getVolumeMm3() { return volumeMm3; }
    public void setVolumeMm3(Double volumeMm3) { this.volumeMm3 = volumeMm3; }

    public Double getSurfaceAreaMm2() { return surfaceAreaMm2; }
    public void setSurfaceAreaMm2(Double surfaceAreaMm2) { this.surfaceAreaMm2 = surfaceAreaMm2; }

    public Double getMassKg() { return massKg; }
    public void setMassKg(Double massKg) { this.massKg = massKg; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public Double getDensityGPerCm3() { return densityGPerCm3; }
    public void setDensityGPerCm3(Double densityGPerCm3) { this.densityGPerCm3 = densityGPerCm3; }
}
