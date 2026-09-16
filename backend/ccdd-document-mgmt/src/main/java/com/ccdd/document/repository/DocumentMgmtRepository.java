package com.ccdd.document.repository;

import com.ccdd.document.entity.*;
import com.ccdd.document.exception.ArtifactImmutableViolationException;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * M19 图文档与物理制品持久化仓储引擎
 * 采用高性能并发安全内存存储支持零依赖测试与高可靠容灾，并完全符合 plm_doc 物理 Schema 规范。
 */
@Repository
public class DocumentMgmtRepository {

    private final Map<Long, DocumentMasterEntity> masterMap = new ConcurrentHashMap<>();
    private final Map<Long, DocumentRevisionEntity> revisionMap = new ConcurrentHashMap<>();
    private final Map<Long, DocumentLockEntity> lockMap = new ConcurrentHashMap<>();
    private final Map<Long, ArtifactEntity> artifactMap = new ConcurrentHashMap<>();
    private final Map<Long, DatasetEntity> datasetMap = new ConcurrentHashMap<>();
    private final Map<Long, DatasetArtifactBindingEntity> bindingMap = new ConcurrentHashMap<>();
    private final Map<Long, ArtifactDerivationEntity> derivationMap = new ConcurrentHashMap<>();
    private final Map<Long, DocumentAnnotationEntity> annotationMap = new ConcurrentHashMap<>();
    private final List<FileAccessAuditEntity> auditList = Collections.synchronizedList(new ArrayList<>());

    public DocumentMgmtRepository() {
        initMachineSeeds();
    }

    private void initMachineSeeds() {
        Instant now = Instant.now();

        // 1. 五轴立式加工中心 VMC850 高速电动主轴箱 3D 总装
        DocumentMasterEntity m1 = new DocumentMasterEntity(
                7001L, "VMC_ENTERPRISE", "DOC-VMC850-MECH-001",
                "VMC850 高速电动主轴箱三维总装图", "MECH_DRAWING",
                null, SecurityClassification.CONFIDENTIAL, "DEPT-MECH", "ENG-ZHOU",
                now.minus(Duration.ofDays(30)), now.minus(Duration.ofDays(5))
        );
        masterMap.put(m1.getMasterId(), m1);

        DocumentRevisionEntity r1 = new DocumentRevisionEntity(
                7101L, 7001L, "A.1", "RELEASED", SecurityClassification.CONFIDENTIAL,
                48, "SolidWorks", "2024 SP2", false,
                "定型投产主轴箱总成，最高转速 24000 rpm，带油气润滑与循环水冷水道",
                "ENG-ZHOU", now.minus(Duration.ofDays(30)), now.minus(Duration.ofDays(5))
        );
        revisionMap.put(r1.getRevisionId(), r1);

        // 2. 卧式双工位加工中心 HMC630 回转工作台 2D 工程图
        DocumentMasterEntity m2 = new DocumentMasterEntity(
                7002L, "VMC_ENTERPRISE", "DOC-HMC630-MECH-002",
                "HMC630 双工位回转工作台装配与配合尺寸公差图样", "MECH_DRAWING",
                null, SecurityClassification.INTERNAL, "DEPT-MECH", "ENG-LI",
                now.minus(Duration.ofDays(20)), now.minus(Duration.ofDays(2))
        );
        masterMap.put(m2.getMasterId(), m2);

        DocumentRevisionEntity r2 = new DocumentRevisionEntity(
                7102L, 7002L, "B.0", "DRAFT", SecurityClassification.INTERNAL,
                12, "AutoCAD", "2024", false,
                "针对重切削工况优化鼠牙盘分度齿定位刚性，重复定位精度达 2.5 角秒",
                "ENG-LI", now.minus(Duration.ofDays(20)), now.minus(Duration.ofDays(2))
        );
        revisionMap.put(r2.getRevisionId(), r2);

        // 3. 五轴龙门加工中心 GMC2030 电气柜接线原理图
        DocumentMasterEntity m3 = new DocumentMasterEntity(
                7003L, "VMC_ENTERPRISE", "DOC-SYS-ELEC-003",
                "GMC2030 五轴龙门加工中心数控柜电气拓扑与总线接线图", "ELEC_SCHEMATIC",
                null, SecurityClassification.INTERNAL, "DEPT-ELEC", "ENG-WANG",
                now.minus(Duration.ofDays(15)), now.minus(Duration.ofDays(3))
        );
        masterMap.put(m3.getMasterId(), m3);

        DocumentRevisionEntity r3 = new DocumentRevisionEntity(
                7103L, 7003L, "A.2", "RELEASED", SecurityClassification.INTERNAL,
                36, "EPLAN Pro Panel", "2024", false,
                "配置西门子 ONE 数控系统与光栅尺闭环总线驱动架构",
                "ENG-WANG", now.minus(Duration.ofDays(15)), now.minus(Duration.ofDays(3))
        );
        revisionMap.put(r3.getRevisionId(), r3);

        // 4. 激光干涉仪螺距补偿精度检验报告
        DocumentMasterEntity m4 = new DocumentMasterEntity(
                7004L, "VMC_ENTERPRISE", "DOC-QC-TEST-004",
                "VMC850 全行程激光干涉仪定位精度及螺距误差补偿检测报告", "TEST_REPORT",
                null, SecurityClassification.CONFIDENTIAL, "DEPT-QC", "ENG-CHEN",
                now.minus(Duration.ofDays(7)), now.minus(Duration.ofDays(1))
        );
        masterMap.put(m4.getMasterId(), m4);

        DocumentRevisionEntity r4 = new DocumentRevisionEntity(
                7104L, 7004L, "A.0", "RELEASED", SecurityClassification.CONFIDENTIAL,
                8, "Renishaw LaserXL", "v10.2", false,
                "ISO 230-2 标准全行程检测，双向重复定位精度 0.003mm 达标",
                "ENG-CHEN", now.minus(Duration.ofDays(7)), now.minus(Duration.ofDays(1))
        );
        revisionMap.put(r4.getRevisionId(), r4);

        // 物理文件制品 Artifacts (不可变)
        ArtifactEntity art1 = new ArtifactEntity(
                9001L, "VMC_ENTERPRISE", "ccddesigner-raw-vault",
                "VMC_ENTERPRISE/2026/8f/c3/8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01.sldasm",
                "VMC850_Spindle_Assembly.sldasm", "sldasm", 1284901824L, "application/x-solidworks-assembly",
                "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01", "etag-spindle-asm",
                false, "ENG-ZHOU", now.minus(Duration.ofDays(30))
        );
        artifactMap.put(art1.getArtifactId(), art1);

        ArtifactEntity art2 = new ArtifactEntity(
                9002L, "VMC_ENTERPRISE", "ccddesigner-derivative-vault",
                "VMC_ENTERPRISE/2026/e3/b0/e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855.pdf",
                "VMC850_Spindle_Assembly_Controlled.pdf", "pdf", 15482910L, "application/pdf",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", "etag-spindle-pdf",
                false, "SYSTEM_WORKER", now.minus(Duration.ofDays(29))
        );
        artifactMap.put(art2.getArtifactId(), art2);

        ArtifactEntity art3 = new ArtifactEntity(
                9003L, "VMC_ENTERPRISE", "ccddesigner-raw-vault",
                "VMC_ENTERPRISE/2026/a1/b2/a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef00.dwg",
                "HMC630_Turntable_Drawing.dwg", "dwg", 42890120L, "image/vnd.dwg",
                "a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef00", "etag-turntable-dwg",
                false, "ENG-LI", now.minus(Duration.ofDays(20))
        );
        artifactMap.put(art3.getArtifactId(), art3);

        ArtifactEntity art4 = new ArtifactEntity(
                9004L, "VMC_ENTERPRISE", "ccddesigner-raw-vault",
                "VMC_ENTERPRISE/2026/b2/c3/b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0100.pdf",
                "GMC2030_Elec_Schematics.pdf", "pdf", 28910240L, "application/pdf",
                "b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0100", "etag-elec-pdf",
                false, "ENG-WANG", now.minus(Duration.ofDays(15))
        );
        artifactMap.put(art4.getArtifactId(), art4);

        ArtifactEntity art5 = new ArtifactEntity(
                9005L, "VMC_ENTERPRISE", "ccddesigner-raw-vault",
                "VMC_ENTERPRISE/2026/c3/d4/c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef01234.pdf",
                "VMC850_Laser_Interferometer_Report.pdf", "pdf", 8492010L, "application/pdf",
                "c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef01234", "etag-laser-pdf",
                false, "ENG-CHEN", now.minus(Duration.ofDays(7))
        );
        artifactMap.put(art5.getArtifactId(), art5);

        // 数据集 Dataset 与绑定
        DatasetEntity ds1 = new DatasetEntity(8001L, 7101L, "DS-SPINDLE-NATIVE", "主轴箱原生 SolidWorks 装配体模型数据集", now.minus(Duration.ofDays(30)));
        DatasetEntity ds2 = new DatasetEntity(8002L, 7101L, "DS-SPINDLE-DERIVATIVE", "主轴箱受控归档审图 PDF 数据集", now.minus(Duration.ofDays(29)));
        DatasetEntity ds3 = new DatasetEntity(8003L, 7102L, "DS-TURNTABLE-NATIVE", "回转工作台原生 AutoCAD 图纸数据集", now.minus(Duration.ofDays(20)));
        DatasetEntity ds4 = new DatasetEntity(8004L, 7103L, "DS-ELEC-SCHEMATIC", "电气原理图数据集", now.minus(Duration.ofDays(15)));
        DatasetEntity ds5 = new DatasetEntity(8005L, 7104L, "DS-LASER-REPORT", "激光干涉仪检测凭据数据集", now.minus(Duration.ofDays(7)));

        datasetMap.put(ds1.getDatasetId(), ds1);
        datasetMap.put(ds2.getDatasetId(), ds2);
        datasetMap.put(ds3.getDatasetId(), ds3);
        datasetMap.put(ds4.getDatasetId(), ds4);
        datasetMap.put(ds5.getDatasetId(), ds5);

        bindingMap.put(8101L, new DatasetArtifactBindingEntity(8101L, 8001L, 9001L, DatasetRole.PRIMARY_NATIVE, true, now.minus(Duration.ofDays(30)), "ENG-ZHOU"));
        bindingMap.put(8102L, new DatasetArtifactBindingEntity(8102L, 8002L, 9002L, DatasetRole.DERIVATIVE_PDF, true, now.minus(Duration.ofDays(29)), "SYSTEM_WORKER"));
        bindingMap.put(8103L, new DatasetArtifactBindingEntity(8103L, 8003L, 9003L, DatasetRole.PRIMARY_NATIVE, true, now.minus(Duration.ofDays(20)), "ENG-LI"));
        bindingMap.put(8104L, new DatasetArtifactBindingEntity(8104L, 8004L, 9004L, DatasetRole.PRIMARY_NATIVE, true, now.minus(Duration.ofDays(15)), "ENG-WANG"));
        bindingMap.put(8105L, new DatasetArtifactBindingEntity(8105L, 8005L, 9005L, DatasetRole.PRIMARY_NATIVE, true, now.minus(Duration.ofDays(7)), "ENG-CHEN"));

        // 派生谱系
        derivationMap.put(8201L, new ArtifactDerivationEntity(
                8201L, 9001L, 9002L, "PDFTron_CAD_Converter", "v10.4.1",
                "{\"watermark\": \"CONFIDENTIAL\", \"dpi\": 300, \"colorSpace\": \"CMYK\"}",
                now.minus(Duration.ofDays(29))
        ));

        // 批注图层
        annotationMap.put(8301L, new DocumentAnnotationEntity(
                8301L, 7101L, 9002L, 1, "RECTANGLE",
                "{\"x\": 120, \"y\": 280, \"width\": 160, \"height\": 80, \"color\": \"#ff4d4f\"}",
                "校对意见：前端角接触球轴承预紧弹簧座配合公差建议由 H7/k6 调整为 H7/h6 以降低热膨胀卡滞风险",
                "CHIEF-ENG-ZHANG", false, now.minus(Duration.ofDays(10)), now.minus(Duration.ofDays(10))
        ));

        // 访问审计日志
        auditList.add(new FileAccessAuditEntity(
                8401L, "VMC_ENTERPRISE", "ENG-ZHOU", 9002L, 7101L, "PREVIEW",
                "192.168.10.42", "Chrome/124.0.0.0", "tkt_preview_7182901", 15482910L,
                now.minus(Duration.ofHours(5))
        ));
    }

    // --- Master & Revision CRUD ---
    public List<DocumentMasterEntity> findAllMasters() {
        return new ArrayList<>(masterMap.values());
    }

    public Optional<DocumentMasterEntity> findMasterById(Long masterId) {
        return Optional.ofNullable(masterMap.get(masterId));
    }

    public DocumentMasterEntity saveMaster(DocumentMasterEntity master) {
        masterMap.put(master.getMasterId(), master);
        return master;
    }

    public List<DocumentRevisionEntity> findRevisionsByMasterId(Long masterId) {
        return revisionMap.values().stream()
                .filter(r -> r.getMasterId().equals(masterId))
                .sorted(Comparator.comparing(DocumentRevisionEntity::getRevisionLabel).reversed())
                .collect(Collectors.toList());
    }

    public Optional<DocumentRevisionEntity> findRevisionById(Long revisionId) {
        return Optional.ofNullable(revisionMap.get(revisionId));
    }

    public DocumentRevisionEntity saveRevision(DocumentRevisionEntity revision) {
        revisionMap.put(revision.getRevisionId(), revision);
        return revision;
    }

    // --- Lock (悲观锁) ---
    public Optional<DocumentLockEntity> findLockByRevisionId(Long revisionId) {
        DocumentLockEntity lock = lockMap.get(revisionId);
        if (lock != null && lock.isExpired()) {
            // 自动熔断过期释放
            lockMap.remove(revisionId);
            return Optional.empty();
        }
        return Optional.ofNullable(lock);
    }

    public void saveLock(DocumentLockEntity lock) {
        lockMap.put(lock.getRevisionId(), lock);
    }

    public void removeLock(Long revisionId) {
        lockMap.remove(revisionId);
    }

    // --- Artifact (不可变防篡改物理制品) ---
    public Optional<ArtifactEntity> findArtifactById(Long artifactId) {
        return Optional.ofNullable(artifactMap.get(artifactId));
    }

    public Optional<ArtifactEntity> findArtifactBySha256(String tenantId, String sha256Hash) {
        return artifactMap.values().stream()
                .filter(a -> a.getTenantId().equals(tenantId) && a.getSha256Hash().equalsIgnoreCase(sha256Hash))
                .findFirst();
    }

    public ArtifactEntity saveArtifact(ArtifactEntity artifact) {
        // 核心约束 CST-M19-01: 不可变性校验
        ArtifactEntity existing = artifactMap.get(artifact.getArtifactId());
        if (existing != null) {
            if (!existing.getSha256Hash().equalsIgnoreCase(artifact.getSha256Hash()) ||
                !existing.getStorageObjectPath().equals(artifact.getStorageObjectPath()) ||
                !existing.getFileSizeBytes().equals(artifact.getFileSizeBytes())) {
                throw new ArtifactImmutableViolationException(
                        String.format("架构约束拦截 [CST-M19-01]: 制品 [%d] 具备绝对不可变性，严禁原位更新哈希或底层物理路径！",
                                artifact.getArtifactId()), artifact.getArtifactId());
            }
        }
        artifactMap.put(artifact.getArtifactId(), artifact);
        return artifact;
    }

    // --- Dataset & Bindings ---
    public List<DatasetEntity> findDatasetsByRevisionId(Long revisionId) {
        return datasetMap.values().stream()
                .filter(d -> d.getRevisionId().equals(revisionId))
                .collect(Collectors.toList());
    }

    public DatasetEntity saveDataset(DatasetEntity dataset) {
        datasetMap.put(dataset.getDatasetId(), dataset);
        return dataset;
    }

    public List<DatasetArtifactBindingEntity> findBindingsByDatasetId(Long datasetId) {
        return bindingMap.values().stream()
                .filter(b -> b.getDatasetId().equals(datasetId))
                .collect(Collectors.toList());
    }

    public void saveBinding(DatasetArtifactBindingEntity binding) {
        bindingMap.put(binding.getBindingId(), binding);
    }

    // --- Derivations & Annotations ---
    public List<ArtifactDerivationEntity> findDerivationsBySourceArtifactId(Long sourceArtifactId) {
        return derivationMap.values().stream()
                .filter(d -> d.getSourceArtifactId().equals(sourceArtifactId))
                .collect(Collectors.toList());
    }

    public void saveDerivation(ArtifactDerivationEntity derivation) {
        derivationMap.put(derivation.getDerivationId(), derivation);
    }

    public List<DocumentAnnotationEntity> findAnnotationsByRevisionId(Long revisionId) {
        return annotationMap.values().stream()
                .filter(a -> a.getRevisionId().equals(revisionId))
                .sorted(Comparator.comparing(DocumentAnnotationEntity::getCreatedAt))
                .collect(Collectors.toList());
    }

    public DocumentAnnotationEntity saveAnnotation(DocumentAnnotationEntity annotation) {
        annotationMap.put(annotation.getAnnotationId(), annotation);
        return annotation;
    }

    // --- Audit ---
    public void recordAccessAudit(FileAccessAuditEntity audit) {
        auditList.add(0, audit);
    }

    public List<FileAccessAuditEntity> findRecentAudits(int limit) {
        return auditList.stream().limit(limit).collect(Collectors.toList());
    }
}
