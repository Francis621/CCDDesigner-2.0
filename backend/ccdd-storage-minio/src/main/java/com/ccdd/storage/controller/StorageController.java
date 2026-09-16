package com.ccdd.storage.controller;

import com.ccdd.common.api.Result;
import com.ccdd.storage.dto.CadGeometryAttributes;
import com.ccdd.storage.dto.MultipartUploadDto;
import com.ccdd.storage.service.CadMetadataExtractor;
import com.ccdd.storage.service.MinioStorageService;
import org.springframework.web.bind.annotation.*;

/**
 * 对象存储与分片防篡改 RESTful 控制器 (遵循 D06 规范)
 */
@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {

    private final MinioStorageService storageService;
    private final CadMetadataExtractor cadMetadataExtractor;

    public StorageController(MinioStorageService storageService, CadMetadataExtractor cadMetadataExtractor) {
        this.storageService = storageService;
        this.cadMetadataExtractor = cadMetadataExtractor;
    }

    /**
     * 初始化分片上传任务
     */
    @PostMapping("/multipart/initiate")
    public Result<MultipartUploadDto.InitiateMultipartResponse> initiateMultipart(
            @RequestBody MultipartUploadDto.InitiateMultipartRequest request) {
        
        MultipartUploadDto.InitiateMultipartResponse response = storageService.initiateMultipartUpload(request);
        return Result.success(response);
    }

    /**
     * 完成分片上传并执行 SHA-256 防篡改校验
     */
    @PostMapping("/multipart/complete")
    public Result<MultipartUploadDto.CompleteMultipartResponse> completeMultipart(
            @RequestBody MultipartUploadDto.CompleteMultipartRequest request) {
        
        MultipartUploadDto.CompleteMultipartResponse response = storageService.completeMultipartUpload(request);
        return Result.success(response);
    }

    /**
     * 提取 CAD 模型物理属性元数据
     */
    @GetMapping("/cad/extract-attributes")
    public Result<CadGeometryAttributes> extractCadAttributes(@RequestParam("fileName") String fileName) {
        CadGeometryAttributes attributes = cadMetadataExtractor.extractAttributes(fileName, new byte[0]);
        return Result.success(attributes);
    }

    /**
     * 获取制造工艺卡 SOP 或检测报告的安全预签名上传/下载凭证
     */
    @GetMapping("/documents/presigned-url")
    public Result<MultipartUploadDto.DocumentPresignedDto> getDocumentPresignedUrl(
            @RequestParam(value = "category", defaultValue = "sop") String category,
            @RequestParam(value = "businessKey", defaultValue = "OP10") String businessKey,
            @RequestParam(value = "fileName", defaultValue = "sop.pdf") String fileName) {
        MultipartUploadDto.DocumentPresignedDto dto = storageService.generateDocumentPresignedUrl(category, businessKey, fileName);
        return Result.success(dto);
    }
}
