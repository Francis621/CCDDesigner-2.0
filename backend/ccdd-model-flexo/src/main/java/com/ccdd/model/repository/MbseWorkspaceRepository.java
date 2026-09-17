package com.ccdd.model.repository;

import com.ccdd.model.dto.MbseWorkspaceDtos.ValidationOutcomeDto;
import com.ccdd.model.entity.CandidateSnapshot;
import com.ccdd.model.entity.SystemModelProject;
import com.ccdd.model.entity.WorkingElementBinding;
import com.ccdd.model.entity.WorkspaceBinding;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * M04 MBSE 建模工作区仓储层
 */
@Repository
public class MbseWorkspaceRepository {

    private final Map<Long, SystemModelProject> projectMap = new ConcurrentHashMap<>();
    private final Map<Long, WorkspaceBinding> workspaceMap = new ConcurrentHashMap<>();
    private final Map<Long, ValidationOutcomeDto> validationMap = new ConcurrentHashMap<>();
    private final Map<Long, List<WorkingElementBinding>> workingBindingsMap = new ConcurrentHashMap<>();
    private final Map<Long, CandidateSnapshot> candidateSnapshotMap = new ConcurrentHashMap<>();

    public MbseWorkspaceRepository() {
        initDefaultData();
    }

    private void initDefaultData() {
        // 初始化默认 VMC1000 系统模型工程
        Long defaultProjId = 701928410293812L;
        SystemModelProject project = new SystemModelProject(
                defaultProjId,
                100293810293L, // 所属五轴数控机床研制项目 ID
                "SMP-VMC1000-01",
                "VMC1000 五轴立式加工中心系统工程模型",
                "FLEXO",
                "SYSML_V2",
                "main",
                1001L,
                "ENG-MECH-1042",
                Instant.now(),
                Instant.now()
        );
        projectMap.put(defaultProjId, project);

        // 初始化默认工作区绑定
        Long defaultWorkspaceId = 801928410290182L;
        WorkspaceBinding binding = WorkspaceBinding.builder()
                .bindingId(defaultWorkspaceId)
                .tenantId("TENANT-DEFAULT")
                .projectId("100293810293")
                .modelProjectId(defaultProjId.toString())
                .modelProjectName(project.getName())
                .primaryChannel("GRAPHICAL")
                .channelLockToken(null)
                .channelLockExpiresAt(null)
                .currentWorkspaceState("ACTIVE")
                .boundUserId("ENG-MECH-1042")
                .sysonProjectUri("syson-proj-uuid-88192a01-c918")
                .baseCommitId("cmt_flexo_init_vmc1000")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        workspaceMap.put(defaultWorkspaceId, binding);

        // 初始化默认临时工作期需求映射
        List<WorkingElementBinding> defaultBindings = new ArrayList<>();
        defaultBindings.add(new WorkingElementBinding(
                1L,
                defaultWorkspaceId,
                "RequirementRevision",
                3001L,
                "REQ-X-001",
                "elem_req_stroke_1000",
                "RequirementUsage",
                "VMC1000_SystemModel::'01_Requirements'::XAxisStrokeReqUsage",
                Instant.now()
        ));
        workingBindingsMap.put(defaultWorkspaceId, defaultBindings);
    }

    public Optional<SystemModelProject> findProjectById(Long modelProjectId) {
        return Optional.ofNullable(projectMap.get(modelProjectId));
    }

    public List<SystemModelProject> listAllProjects() {
        return new ArrayList<>(projectMap.values());
    }

    public Optional<WorkspaceBinding> findWorkspaceById(Long workspaceId) {
        return Optional.ofNullable(workspaceMap.get(workspaceId));
    }

    public List<WorkspaceBinding> listWorkspaces() {
        return new ArrayList<>(workspaceMap.values());
    }

    public void saveWorkspace(WorkspaceBinding binding) {
        binding.setUpdatedAt(Instant.now());
        workspaceMap.put(binding.getBindingId(), binding);
    }

    public void saveValidation(ValidationOutcomeDto validation) {
        validationMap.put(validation.getValidationId(), validation);
    }

    public Optional<ValidationOutcomeDto> findValidationById(Long validationId) {
        return Optional.ofNullable(validationMap.get(validationId));
    }

    public Optional<ValidationOutcomeDto> findLatestValidationByWorkspace(Long workspaceId) {
        return validationMap.values().stream()
                .filter(v -> Objects.equals(v.getWorkspaceId(), workspaceId))
                .max(Comparator.comparing(ValidationOutcomeDto::getCompletedAt));
    }

    public List<WorkingElementBinding> listWorkingBindings(Long workspaceId) {
        return workingBindingsMap.computeIfAbsent(workspaceId, k -> new ArrayList<>());
    }

    public void addWorkingBinding(WorkingElementBinding binding) {
        List<WorkingElementBinding> list = workingBindingsMap.computeIfAbsent(binding.getWorkspaceId(), k -> new ArrayList<>());
        list.add(binding);
    }

    public void saveCandidateSnapshot(CandidateSnapshot snapshot) {
        candidateSnapshotMap.put(snapshot.getSnapshotId(), snapshot);
    }

    public Optional<CandidateSnapshot> findCandidateSnapshotById(Long snapshotId) {
        return Optional.ofNullable(candidateSnapshotMap.get(snapshotId));
    }

    public Optional<CandidateSnapshot> findCandidateSnapshotByToken(String token) {
        return candidateSnapshotMap.values().stream()
                .filter(s -> Objects.equals(s.getSnapshotToken(), token))
                .findFirst();
    }

    public List<CandidateSnapshot> listCandidateSnapshots(Long workspaceId) {
        return candidateSnapshotMap.values().stream()
                .filter(s -> Objects.equals(s.getBindingId(), workspaceId))
                .sorted(Comparator.comparing(CandidateSnapshot::getCreatedAt).reversed())
                .toList();
    }
}
