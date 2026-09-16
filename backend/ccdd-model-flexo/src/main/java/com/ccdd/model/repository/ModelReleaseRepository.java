package com.ccdd.model.repository;

import com.ccdd.model.entity.ModelRelease;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class ModelReleaseRepository {

    private final JdbcTemplate jdbcTemplate;

    public ModelReleaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(ModelRelease release) {
        String sql = """
            INSERT INTO sys_model_releases (
                release_id, tenant_id, binding_id, snapshot_id, profile_id,
                model_project_id, release_version, lifecycle_state, execution_state,
                flexo_repository_id, flexo_staging_graph, flexo_commit_id,
                artifact_bundle_sha256, artifact_bundle_uri, combined_release_hash,
                published_by, working_version, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                release.getReleaseId(),
                release.getTenantId(),
                release.getBindingId(),
                release.getSnapshotId(),
                release.getProfileId(),
                release.getModelProjectId(),
                release.getReleaseVersion(),
                release.getLifecycleState(),
                release.getExecutionState(),
                release.getFlexoRepositoryId(),
                release.getFlexoStagingGraph(),
                release.getFlexoCommitId(),
                release.getArtifactBundleSha256(),
                release.getArtifactBundleUri(),
                release.getCombinedReleaseHash(),
                release.getPublishedBy(),
                release.getWorkingVersion(),
                Timestamp.from(release.getCreatedAt()),
                Timestamp.from(release.getUpdatedAt())
        );
    }

    public void updateExecutionState(Long releaseId, String executionState, String failedStep, String failedReason) {
        String sql = """
            UPDATE sys_model_releases 
            SET execution_state = ?, failed_step = ?, failed_reason = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE release_id = ?
        """;
        jdbcTemplate.update(sql, executionState, failedStep, failedReason, releaseId);
    }

    public void activateRelease(Long releaseId, String approverUserId) {
        String sql = """
            UPDATE sys_model_releases 
            SET lifecycle_state = 'RELEASED', execution_state = 'RELEASED', 
                approved_by = ?, approved_at = CURRENT_TIMESTAMP, 
                published_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP 
            WHERE release_id = ?
        """;
        jdbcTemplate.update(sql, approverUserId, releaseId);
    }
}
