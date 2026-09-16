package com.ccdd.document.controller;

import com.ccdd.common.api.Result;
import com.ccdd.document.dto.*;
import com.ccdd.document.entity.DocumentAnnotationEntity;
import com.ccdd.document.entity.FileAccessAuditEntity;
import com.ccdd.document.service.DocumentMgmtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * M19 图文档与文件制品 RESTful 控制器 (OpenAPI §8)
 */
@RestController
@RequestMapping("/api/v1")
public class DocumentMgmtController {

    private final DocumentMgmtService documentService;

    public DocumentMgmtController(DocumentMgmtService documentService) {
        this.documentService = documentService;
    }

    /**
     * 查询图文档台账列表 (支持分类、关键字与密级过滤)
     */
    @GetMapping("/documents")
    public Result<List<DocumentDetailDto>> listDocuments(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "securityLevel", required = false) String securityLevel) {
        List<DocumentDetailDto> list = documentService.listDocuments(category, keyword, securityLevel);
        return Result.success(list);
    }

    /**
     * 获取单个图文档四层解耦聚合详情
     */
    @GetMapping("/documents/{masterId}")
    public Result<DocumentDetailDto> getDocumentDetail(@PathVariable("masterId") Long masterId) {
        DocumentDetailDto dto = documentService.getDocumentDetail(masterId);
        return Result.success(dto);
    }

    /**
     * 新建/注册工程图文档
     */
    @PostMapping("/documents")
    public Result<DocumentDetailDto> createDocument(@RequestBody SaveDocumentRequest request) {
        DocumentDetailDto created = documentService.createDocument(request);
        return Result.success(created);
    }

    /**
     * 悲观签出锁定 (OpenAPI §8.3)
     */
    @PostMapping("/documents/{masterId}/revisions/{revisionId}/checkout")
    public Result<CheckoutResponse> checkoutRevision(
            @PathVariable("masterId") Long masterId,
            @PathVariable("revisionId") Long revisionId,
            @RequestBody(required = false) CheckoutRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-ZHOU") String userId,
            HttpServletRequest servletRequest) {
        CheckoutRequest req = request != null ? request : new CheckoutRequest(8, "在线工作台签出");
        String clientIp = servletRequest.getRemoteAddr();
        CheckoutResponse response = documentService.checkoutRevision(masterId, revisionId, req, userId, clientIp);
        return Result.success(response);
    }

    /**
     * 签入新制品并释放锁 (M19-F01)
     */
    @PostMapping("/documents/{masterId}/revisions/{revisionId}/checkin")
    public Result<DocumentDetailDto> checkinRevision(
            @PathVariable("masterId") Long masterId,
            @PathVariable("revisionId") Long revisionId,
            @RequestBody CheckinRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-ZHOU") String userId) {
        String effectiveUser = request.getUser() != null ? request.getUser() : userId;
        DocumentDetailDto updated = documentService.checkinRevision(masterId, revisionId, request, effectiveUser);
        return Result.success(updated);
    }

    /**
     * 撤销签出锁定 (Cancel Check-Out)
     */
    @PostMapping("/documents/{masterId}/revisions/{revisionId}/cancel-checkout")
    public Result<String> cancelCheckout(
            @PathVariable("masterId") Long masterId,
            @PathVariable("revisionId") Long revisionId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-ZHOU") String userId) {
        documentService.cancelCheckout(masterId, revisionId, userId);
        return Result.success("签出锁定已成功撤销并释放资源");
    }

    /**
     * 初始化大文件分片上传 (OpenAPI §8.1)
     */
    @PostMapping("/artifacts/multipart/init")
    public Result<MultipartInitResponse> initMultipart(
            @RequestBody MultipartInitRequest request,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "VMC_ENTERPRISE") String tenantId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-ZHOU") String userId) {
        MultipartInitResponse response = documentService.initMultipart(request, tenantId, userId);
        return Result.success(response);
    }

    /**
     * 完成分片合并并执行服务端 SHA-256 强哈希校验 (OpenAPI §8.2)
     */
    @PostMapping("/artifacts/multipart/complete")
    public Result<MultipartCompleteResponse> completeMultipart(
            @RequestBody MultipartCompleteRequest request,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "VMC_ENTERPRISE") String tenantId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-ZHOU") String userId) {
        MultipartCompleteResponse response = documentService.completeMultipart(request, tenantId, userId);
        return Result.success(response);
    }

    /**
     * 获取动态受控安全防伪水印 (M19-F03)
     */
    @GetMapping("/documents/{masterId}/revisions/{revisionId}/watermark")
    public Result<Map<String, String>> getSecurityWatermark(
            @PathVariable("masterId") Long masterId,
            @PathVariable("revisionId") Long revisionId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-ZHOU") String userId,
            @RequestHeader(value = "X-Department", defaultValue = "数控机床研发一部") String dept) {
        String watermark = documentService.calculateSecurityWatermark(revisionId, userId, dept);
        return Result.success(Map.of("watermarkText", watermark, "rotationAngle", "45", "opacity", "0.15"));
    }

    /**
     * 在线协同非破坏性批注图层写入 (M19-F03)
     */
    @PostMapping("/documents/{masterId}/revisions/{revisionId}/annotations")
    public Result<DocumentAnnotationEntity> addAnnotation(
            @PathVariable("masterId") Long masterId,
            @PathVariable("revisionId") Long revisionId,
            @RequestBody SaveAnnotationRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-ZHOU") String userId) {
        DocumentAnnotationEntity entity = documentService.addAnnotation(revisionId, request, userId);
        return Result.success(entity);
    }

    /**
     * 获取指定版本的所有批注图层
     */
    @GetMapping("/documents/{masterId}/revisions/{revisionId}/annotations")
    public Result<List<DocumentAnnotationEntity>> getAnnotations(
            @PathVariable("masterId") Long masterId,
            @PathVariable("revisionId") Long revisionId) {
        List<DocumentAnnotationEntity> list = documentService.getAnnotations(revisionId);
        return Result.success(list);
    }

    /**
     * 受控工程交付包组装导出 (M19-F05, AT-14)
     */
    @PostMapping("/documents/export-package")
    public Result<ExportPackageManifestDto> exportPackage(
            @RequestBody ExportPackageRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "ENG-ZHOU") String userId,
            @RequestHeader(value = "X-Security-Clearance", defaultValue = "CONFIDENTIAL") String clearance) {
        ExportPackageManifestDto manifest = documentService.exportPackage(request, userId, clearance);
        return Result.success(manifest);
    }

    /**
     * 查询文件物理访问与下载审计日志
     */
    @GetMapping("/documents/audits")
    public Result<List<FileAccessAuditEntity>> getAudits(
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        List<FileAccessAuditEntity> list = documentService.getRecentAudits(limit);
        return Result.success(list);
    }
}
