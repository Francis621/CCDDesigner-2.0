package com.ccdd.document;

import com.ccdd.common.api.BusinessException;
import com.ccdd.document.dto.*;
import com.ccdd.document.entity.*;
import com.ccdd.document.exception.ArtifactImmutableViolationException;
import com.ccdd.document.exception.ChecksumVerificationException;
import com.ccdd.document.exception.ConcurrencyLockViolationException;
import com.ccdd.document.repository.DocumentMgmtRepository;
import com.ccdd.document.service.DocumentMgmtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * M19 图文档与文件制品全生命周期验收测试用例集
 * 覆盖 TC-M19-01 至 TC-M19-06 全部规约与验收指标
 */
public class DocumentMgmtServiceTest {

    private DocumentMgmtRepository repository;
    private DocumentMgmtService documentService;

    @BeforeEach
    void setUp() {
        repository = new DocumentMgmtRepository();
        documentService = new DocumentMgmtService(repository);
    }

    @Test
    @DisplayName("TC-M19-01: 制品哈希永久不可变约束 (CST-M19-01) - 阻断原位修改物理字节")
    void testArtifactImmutabilityViolation() {
        ArtifactEntity art = repository.findArtifactById(9001L).orElseThrow();
        assertEquals("8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01", art.getSha256Hash());

        // 尝试原位篡改哈希并保存
        ArtifactEntity tampered = new ArtifactEntity(
                art.getArtifactId(), art.getTenantId(), art.getStorageBucket(),
                art.getStorageObjectPath(), art.getFileName(), art.getFileExtension(),
                art.getFileSizeBytes(), art.getMimeType(),
                "tampered_sha256_hash_value_which_violates_cst_m19_01_constraint_001",
                art.getEtag(), art.getIsQuarantined(), "ATTACKER", Instant.now()
        );

        assertThrows(ArtifactImmutableViolationException.class, () -> {
            repository.saveArtifact(tampered);
        }, "预期数据库/仓储触发 CST-M19-01 拦截，严禁原位更新不可变制品！");
    }

    @Test
    @DisplayName("TC-M19-02: 分片合并与 SHA-256 强校验 (AT-04) - 申报哈希不匹配硬阻断")
    void testMultipartChecksumVerificationFailure() {
        // 1. 初始化分片上传
        MultipartInitRequest initReq = new MultipartInitRequest(
                "VMC1000_Spindle_Shaft.sldprt", 1024000L,
                "expected_clean_hash_9876543210abcdef9876543210abcdef9876543210abcdef",
                5, "application/octet-stream", 7101L
        );
        MultipartInitResponse initResp = documentService.initMultipart(initReq, "VMC_ENTERPRISE", "ENG-ZHOU");
        assertNotNull(initResp.getUploadId());
        assertEquals(5, initResp.getPresignedPartUrls().size());

        // 2. 合并时传入被篡改的伪造哈希
        MultipartCompleteRequest completeReq = new MultipartCompleteRequest(
                initResp.getUploadId(),
                "tampered_sha256_declared_by_client_not_matching_real_stream_bytes",
                List.of(new MultipartCompleteRequest.UploadedPartDto(1, "etag1"))
        );

        assertThrows(ChecksumVerificationException.class, () -> {
            documentService.completeMultipart(completeReq, "VMC_ENTERPRISE", "ENG-ZHOU");
        }, "预期服务端独立核算后比对失败，抛出 ChecksumVerificationException (HTTP 422)");
    }

    @Test
    @DisplayName("TC-M19-03: 悲观签出排他锁保护 (M19-F01) - 阻断非持有者协同冲突")
    void testPessimisticCheckOutLockViolation() {
        Long masterId = 7002L;
        Long revId = 7102L; // HMC630 DRAFT 版本

        // 用户 A (ENG-LI) 签出锁定
        CheckoutRequest req = new CheckoutRequest(8, "机械结构室进行配合公差优化");
        CheckoutResponse checkoutResp = documentService.checkoutRevision(masterId, revId, req, "ENG-LI", "192.168.1.100");
        assertTrue(checkoutResp.getIsLocked());
        assertEquals("ENG-LI", checkoutResp.getLockedBy());

        // 用户 B (ENG-WANG) 尝试对同一图档签出或签入
        CheckoutRequest conflictReq = new CheckoutRequest(4, "电气工程师尝试覆盖该草稿");
        assertThrows(ConcurrencyLockViolationException.class, () -> {
            documentService.checkoutRevision(masterId, revId, conflictReq, "ENG-WANG", "192.168.1.105");
        }, "预期触发悲观锁并发冲突拦截，阻断非持有者 ENG-WANG 操作");

        // 用户 B 尝试签入提交也必须被硬阻断
        CheckinRequest checkinReq = new CheckinRequest(9003L, "非法提交", false, "ENG-WANG");
        assertThrows(ConcurrencyLockViolationException.class, () -> {
            documentService.checkinRevision(masterId, revId, checkinReq, "ENG-WANG");
        }, "预期签入操作被排他锁拦截");

        // 用户 A 正常签入完成解锁
        CheckinRequest validCheckin = new CheckinRequest(null, "完成公差优化校核", false, "ENG-LI");
        DocumentDetailDto checkedIn = documentService.checkinRevision(masterId, revId, validCheckin, "ENG-LI");
        assertNull(checkedIn.getCurrentLock(), "签入后排他锁应成功释放");
    }

    @Test
    @DisplayName("TC-M19-04: PBAC 密级核验与即时撤销阻断 (AT-13)")
    void testPbacSecurityClearanceBlock() {
        ExportPackageRequest exportReq = new ExportPackageRequest(
                "EXPORT_VMC850_RESTRICTED_CORE",
                List.of(7101L),
                true,
                "RESTRICTED", // 请求绝密技术包
                "ENG-INTERN"
        );

        // 仅具有 INTERNAL 权限的普通工程师尝试导出绝密包
        assertThrows(BusinessException.class, () -> {
            documentService.exportPackage(exportReq, "ENG-INTERN", "INTERNAL");
        }, "预期 PBAC 拦截并抛出 FORBIDDEN (AT-13)");
    }

    @Test
    @DisplayName("TC-M19-05: 格式派生流水线与受控防伪水印 (M19-F03)")
    void testDerivativePdfAndDynamicWatermark() {
        ArtifactEntity nativeArt = repository.findArtifactById(9001L).orElseThrow();
        DocumentRevisionEntity rev = repository.findRevisionById(7101L).orElseThrow();

        // 触发派生生成
        ArtifactDerivationEntity derivation = documentService.generateDerivativePdf(nativeArt, rev);
        assertNotNull(derivation);
        assertEquals(nativeArt.getArtifactId(), derivation.getSourceArtifactId());
        assertNotEquals(nativeArt.getArtifactId(), derivation.getDerivedArtifactId());

        // 计算动态安全水印文本
        String watermark = documentService.calculateSecurityWatermark(7101L, "ENG-ZHOU", "结构总体室");
        assertNotNull(watermark);
        assertTrue(watermark.contains("ENG-ZHOU"));
        assertTrue(watermark.contains("结构总体室"));
        assertTrue(watermark.contains("机密"));
        assertTrue(watermark.contains("RELEASED"));
    }

    @Test
    @DisplayName("TC-M19-06: 受控工程交付包组装与清单校验 (AT-14)")
    void testExportPackageAssemblyAndManifest() {
        ExportPackageRequest exportReq = new ExportPackageRequest(
                "EXPORT_VMC850_MANUFACTURING_BUNDLE",
                List.of(7101L, 7104L), // 包含主轴装配与激光干涉仪检测报告
                true,
                "CONFIDENTIAL",
                "CHIEF-ENG-ZHANG"
        );

        ExportPackageManifestDto manifest = documentService.exportPackage(exportReq, "CHIEF-ENG-ZHANG", "CONFIDENTIAL");
        assertNotNull(manifest);
        assertEquals("EXPORT_VMC850_MANUFACTURING_BUNDLE", manifest.getPackageName());
        assertTrue(manifest.getTotalFileCount() >= 2);
        assertTrue(manifest.getTotalSizeBytes() > 0);
        assertNotNull(manifest.getPackageSha256Checksum());

        // 清单中每个文件必须具备不可变哈希
        for (ExportPackageManifestDto.ManifestFileItemDto item : manifest.getFiles()) {
            assertNotNull(item.getSha256Hash());
            assertEquals(64, item.getSha256Hash().length());
            assertNotNull(item.getRelativePath());
        }

        // 审计流水中必须记入该导出行为
        List<FileAccessAuditEntity> audits = documentService.getRecentAudits(10);
        assertTrue(audits.stream().anyMatch(a -> "EXPORT_PACKAGE".equals(a.getAccessType())));
    }
}
