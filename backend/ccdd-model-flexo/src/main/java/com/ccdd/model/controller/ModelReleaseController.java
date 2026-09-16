package com.ccdd.model.controller;

import com.ccdd.common.api.Result;
import com.ccdd.model.entity.ModelRelease;
import com.ccdd.model.service.ModelReleaseCoordinator;
import com.ccdd.outbox.annotation.IdempotentApi;
import org.springframework.web.bind.annotation.*;

/**
 * 模型受控发布 RESTful 控制器 (遵循 D09 / D03 契约)
 */
@RestController
@RequestMapping("/api/v1/model-releases")
public class ModelReleaseController {

    private final ModelReleaseCoordinator releaseCoordinator;

    public ModelReleaseController(ModelReleaseCoordinator releaseCoordinator) {
        this.releaseCoordinator = releaseCoordinator;
    }

    public static class InitiateReleaseRequest {
        private String snapshotToken;
        private String releaseVersion;
        private Long profileId;
        private Long bindingId;
        private String modelProjectId;
        private String rawSysMLContent;
        private boolean simulateMinIOFailure;

        public String getSnapshotToken() { return snapshotToken; }
        public void setSnapshotToken(String snapshotToken) { this.snapshotToken = snapshotToken; }

        public String getReleaseVersion() { return releaseVersion; }
        public void setReleaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; }

        public Long getProfileId() { return profileId; }
        public void setProfileId(Long profileId) { this.profileId = profileId; }

        public Long getBindingId() { return bindingId; }
        public void setBindingId(Long bindingId) { this.bindingId = bindingId; }

        public String getModelProjectId() { return modelProjectId; }
        public void setModelProjectId(String modelProjectId) { this.modelProjectId = modelProjectId; }

        public String getRawSysMLContent() { return rawSysMLContent; }
        public void setRawSysMLContent(String rawSysMLContent) { this.rawSysMLContent = rawSysMLContent; }

        public boolean isSimulateMinIOFailure() { return simulateMinIOFailure; }
        public void setSimulateMinIOFailure(boolean simulateMinIOFailure) { this.simulateMinIOFailure = simulateMinIOFailure; }
    }

    /**
     * 提交候选模型发布申请 (带 Idempotency-Key 强幂等防重放)
     */
    @PostMapping
    @IdempotentApi
    public Result<ModelRelease> initiateRelease(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody InitiateReleaseRequest request) {

        ModelReleaseCoordinator.ReleaseCommand command = ModelReleaseCoordinator.ReleaseCommand.builder()
                .snapshotToken(request.getSnapshotToken())
                .releaseVersion(request.getReleaseVersion())
                .profileId(request.getProfileId())
                .bindingId(request.getBindingId())
                .modelProjectId(request.getModelProjectId())
                .rawSysMLContent(request.getRawSysMLContent())
                .simulateMinIOFailure(request.isSimulateMinIOFailure())
                .build();

        ModelRelease release = releaseCoordinator.initiateRelease(command);
        return Result.success(release);
    }

    /**
     * 模拟工作流审批通过后调用原子激活
     */
    @PostMapping("/{id}/activate")
    public Result<Void> activateRelease(
            @PathVariable("id") Long id,
            @RequestParam("approverUserId") String approverUserId) {
        
        releaseCoordinator.activateRelease(id, approverUserId);
        return Result.success();
    }
}
