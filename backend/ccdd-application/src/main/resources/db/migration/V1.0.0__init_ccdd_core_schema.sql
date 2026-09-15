-- =============================================================================
-- CCDDesigner 2.0 平台核心数据库初始化迁移脚本
-- 覆盖规格: D01 (数据字典), D02 (生命周期/PBAC), D03 (SysML/Flexo),
--          D04 (参数/仿真/证据), D06 (MinIO/CAD), D07 (数字主线), D09 (发件箱)
-- 适用数据库: PostgreSQL 15+
-- =============================================================================

-- 启用扩展 (若具备权限)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- 1. 全局生命周期、发件箱与幂等底座 (D02, D09)
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_idempotency_records (
    idempotency_key      VARCHAR(128) PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    request_uri          VARCHAR(255) NOT NULL,
    request_hash         CHAR(64) NOT NULL,
    response_status      INT NOT NULL,
    response_body_json   JSONB,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at           TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_idempotency_tenant ON sys_idempotency_records(tenant_id, idempotency_key);

CREATE TABLE IF NOT EXISTS sys_outbox_events (
    outbox_id            BIGINT PRIMARY KEY,
    event_id             VARCHAR(64) NOT NULL UNIQUE,
    event_type           VARCHAR(128) NOT NULL,
    schema_version       VARCHAR(16) NOT NULL DEFAULT '1.0',
    tenant_id            VARCHAR(64) NOT NULL,
    aggregate_type       VARCHAR(64) NOT NULL,
    aggregate_id         VARCHAR(128) NOT NULL,
    aggregate_version    BIGINT NOT NULL DEFAULT 1,
    correlation_id       VARCHAR(128) NOT NULL,
    causation_id         VARCHAR(128),
    payload_json         JSONB NOT NULL,
    publish_status       VARCHAR(32) NOT NULL DEFAULT 'PENDING'
                         CHECK (publish_status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED')),
    retry_count          INT NOT NULL DEFAULT 0,
    max_retries          INT NOT NULL DEFAULT 5,
    next_retry_at        TIMESTAMP WITH TIME ZONE,
    error_message        TEXT,
    occurred_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at         TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_outbox_pending ON sys_outbox_events(publish_status, next_retry_at) 
WHERE publish_status IN ('PENDING', 'FAILED');

CREATE TABLE IF NOT EXISTS sys_inbox_events (
    inbox_id             BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    consumer_group       VARCHAR(128) NOT NULL,
    event_id             VARCHAR(64) NOT NULL,
    event_type           VARCHAR(128) NOT NULL,
    consumed_status      VARCHAR(32) NOT NULL DEFAULT 'SUCCESS'
                         CHECK (consumed_status IN ('SUCCESS', 'FAILED')),
    consumed_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_inbox_consumer_event ON sys_inbox_events(tenant_id, consumer_group, event_id);

CREATE TABLE IF NOT EXISTS sys_dead_letter_events (
    dead_letter_id       BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    event_id             VARCHAR(64) NOT NULL,
    consumer_group       VARCHAR(128) NOT NULL,
    event_payload_json   JSONB NOT NULL,
    stack_trace          TEXT NOT NULL,
    failed_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_state       VARCHAR(32) NOT NULL DEFAULT 'UNRESOLVED'
                         CHECK (resolved_state IN ('UNRESOLVED', 'REPLAYED', 'DISCARDED')),
    resolved_by          VARCHAR(64),
    resolved_at          TIMESTAMP WITH TIME ZONE
);

-- =============================================================================
-- 2. 图文档、文件制品与 CAD 绑定底座 (D06, M19, M17)
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_document_masters (
    document_master_id   BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    document_number      VARCHAR(128) NOT NULL,
    document_title       VARCHAR(255) NOT NULL,
    document_category    VARCHAR(64) NOT NULL,
    security_level       VARCHAR(32) NOT NULL DEFAULT 'INTERNAL' 
                         CHECK (security_level IN ('PUBLIC', 'INTERNAL', 'CONFIDENTIAL', 'SECRET')),
    created_by           VARCHAR(64) NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_doc_number ON sys_document_masters(tenant_id, document_number);

CREATE TABLE IF NOT EXISTS sys_document_revisions (
    document_revision_id BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    document_master_id   BIGINT NOT NULL REFERENCES sys_document_masters(document_master_id),
    revision_label       VARCHAR(32) NOT NULL,
    lifecycle_state      VARCHAR(32) NOT NULL DEFAULT 'DRAFT'
                         CHECK (lifecycle_state IN ('DRAFT', 'IN_REVIEW', 'RELEASED', 'OBSOLETE', 'WITHDRAWN')),
    is_locked            BOOLEAN NOT NULL DEFAULT FALSE,
    locked_by            VARCHAR(64),
    locked_at            TIMESTAMP WITH TIME ZONE,
    working_version      BIGINT NOT NULL DEFAULT 1,
    released_by          VARCHAR(64),
    released_at          TIMESTAMP WITH TIME ZONE,
    created_by           VARCHAR(64) NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_doc_revision ON sys_document_revisions(tenant_id, document_master_id, revision_label);

CREATE TABLE IF NOT EXISTS sys_artifacts (
    artifact_id          BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    file_name            VARCHAR(255) NOT NULL,
    file_extension       VARCHAR(32) NOT NULL,
    file_size_bytes      BIGINT NOT NULL,
    mime_type            VARCHAR(128) NOT NULL,
    sha256_hash          CHAR(64) NOT NULL,
    md5_hash             CHAR(32) NOT NULL,
    storage_bucket       VARCHAR(64) NOT NULL,
    storage_object_path  VARCHAR(512) NOT NULL,
    storage_etag         VARCHAR(128),
    domain_type          VARCHAR(64) NOT NULL,
    binding_revision_id  BIGINT,
    is_primary           BOOLEAN NOT NULL DEFAULT FALSE,
    is_frozen            BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_by          VARCHAR(64) NOT NULL,
    uploaded_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_artifact_hash ON sys_artifacts(tenant_id, sha256_hash);

CREATE TABLE IF NOT EXISTS sys_artifact_derivations (
    derivation_id        BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    source_artifact_id   BIGINT NOT NULL REFERENCES sys_artifacts(artifact_id) ON DELETE CASCADE,
    derived_artifact_id  BIGINT NOT NULL REFERENCES sys_artifacts(artifact_id) ON DELETE CASCADE,
    derivation_type      VARCHAR(64) NOT NULL,
    conversion_status    VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    conversion_error     TEXT,
    applied_watermark    VARCHAR(255),
    converted_at         TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_artifact_derivation ON sys_artifact_derivations(tenant_id, source_artifact_id, derivation_type);

CREATE TABLE IF NOT EXISTS sys_upload_sessions (
    session_id           VARCHAR(128) PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    file_name            VARCHAR(255) NOT NULL,
    file_size_bytes      BIGINT NOT NULL,
    total_chunks         INT NOT NULL,
    chunk_size_bytes     INT NOT NULL,
    expected_sha256      CHAR(64),
    minio_upload_id      VARCHAR(255) NOT NULL,
    target_bucket        VARCHAR(64) NOT NULL,
    target_object_path   VARCHAR(512) NOT NULL,
    uploaded_chunks_mask BIT VARYING(1024),
    session_status       VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by           VARCHAR(64) NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at           TIMESTAMP WITH TIME ZONE NOT NULL
);

-- =============================================================================
-- 3. MBSE 工作区与 SysML 模型两阶段发布底座 (D03, M04, M06)
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_workspace_bindings (
    binding_id           BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    project_id           VARCHAR(64) NOT NULL,
    model_project_id     VARCHAR(128) NOT NULL,
    model_project_name   VARCHAR(255) NOT NULL,
    primary_channel      VARCHAR(32) NOT NULL DEFAULT 'GRAPHICAL',
    channel_lock_token   VARCHAR(128),
    channel_lock_expires_at TIMESTAMP WITH TIME ZONE,
    current_workspace_state VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    bound_user_id        VARCHAR(64) NOT NULL,
    syson_project_uri    VARCHAR(512) NOT NULL,
    base_commit_id       VARCHAR(128),
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_workspace_model_project ON sys_workspace_bindings(tenant_id, model_project_id) 
WHERE current_workspace_state != 'ARCHIVED';

CREATE TABLE IF NOT EXISTS sys_candidate_snapshots (
    snapshot_id          BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    binding_id           BIGINT NOT NULL REFERENCES sys_workspace_bindings(binding_id),
    snapshot_token       VARCHAR(128) NOT NULL UNIQUE,
    source_channel       VARCHAR(32) NOT NULL,
    raw_content_sha256   CHAR(64) NOT NULL,
    raw_content_uri      VARCHAR(512) NOT NULL,
    diagram_bundle_sha256 CHAR(64),
    diagram_bundle_uri   VARCHAR(512),
    diagnostic_passed    BOOLEAN NOT NULL DEFAULT FALSE,
    diagnostic_error_count INT NOT NULL DEFAULT 0,
    diagnostic_report_json JSONB,
    captured_by          VARCHAR(64) NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at           TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_compatibility_profiles (
    profile_id           BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    profile_name         VARCHAR(128) NOT NULL,
    profile_version      VARCHAR(32) NOT NULL,
    target_sysml_spec    VARCHAR(32) NOT NULL DEFAULT 'SysML-v2-2026.2',
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    allowed_constructs   JSONB NOT NULL,
    forbidden_constructs JSONB NOT NULL,
    max_element_count    INT NOT NULL DEFAULT 50000,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_model_releases (
    release_id           BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    binding_id           BIGINT NOT NULL REFERENCES sys_workspace_bindings(binding_id),
    snapshot_id          BIGINT NOT NULL REFERENCES sys_candidate_snapshots(snapshot_id),
    profile_id           BIGINT NOT NULL REFERENCES sys_compatibility_profiles(profile_id),
    model_project_id     VARCHAR(128) NOT NULL,
    release_version      VARCHAR(64) NOT NULL,
    lifecycle_state      VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    execution_state      VARCHAR(32) NOT NULL DEFAULT 'CAPTURING',
    failed_step          VARCHAR(64),
    failed_reason        TEXT,
    flexo_repository_id  VARCHAR(128) NOT NULL,
    flexo_staging_graph  VARCHAR(255) NOT NULL,
    flexo_commit_id      VARCHAR(128),
    artifact_bundle_sha256 CHAR(64),
    artifact_bundle_uri  VARCHAR(512),
    combined_release_hash CHAR(64),
    workflow_instance_id VARCHAR(128),
    approval_decision_id VARCHAR(128),
    approved_by          VARCHAR(64),
    approved_at          TIMESTAMP WITH TIME ZONE,
    published_by         VARCHAR(64) NOT NULL,
    published_at         TIMESTAMP WITH TIME ZONE,
    working_version      BIGINT NOT NULL DEFAULT 1,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_model_release_ver ON sys_model_releases(tenant_id, model_project_id, release_version);

CREATE TABLE IF NOT EXISTS sys_model_element_indices (
    element_index_id     BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    release_id           BIGINT NOT NULL REFERENCES sys_model_releases(release_id) ON DELETE CASCADE,
    repository_id        VARCHAR(128) NOT NULL,
    model_project_id     VARCHAR(128) NOT NULL,
    commit_id            VARCHAR(128) NOT NULL,
    element_id           VARCHAR(128) NOT NULL,
    element_name         VARCHAR(255) NOT NULL,
    element_type         VARCHAR(64) NOT NULL,
    display_path         VARCHAR(1024) NOT NULL,
    element_uri          VARCHAR(512) NOT NULL,
    element_hash         CHAR(64) NOT NULL,
    attributes_json      JSONB,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_element_ref_quadruple ON sys_model_element_indices
    (tenant_id, repository_id, model_project_id, commit_id, element_id);

-- =============================================================================
-- 4. 参数角色、DAG 与 OpenModelica 仿真验证底座 (D04, M07~M11)
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_parameter_definitions (
    parameter_id         BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    parameter_code       VARCHAR(128) NOT NULL,
    parameter_name       VARCHAR(255) NOT NULL,
    standard_unit        VARCHAR(64) NOT NULL,
    dimension_type       VARCHAR(64) NOT NULL,
    value_data_type      VARCHAR(32) NOT NULL DEFAULT 'FLOAT',
    default_lower_limit  NUMERIC(18, 6),
    default_upper_limit  NUMERIC(18, 6),
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_param_code ON sys_parameter_definitions(tenant_id, parameter_code);

CREATE TABLE IF NOT EXISTS sys_parameter_value_records (
    value_record_id      BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    parameter_id         BIGINT NOT NULL REFERENCES sys_parameter_definitions(parameter_id),
    param_role           VARCHAR(32) NOT NULL,
    numeric_value        NUMERIC(18, 6),
    string_value         TEXT,
    unit                 VARCHAR(64) NOT NULL,
    source_entity_type   VARCHAR(64) NOT NULL,
    source_entity_id     BIGINT NOT NULL,
    is_valid             BOOLEAN NOT NULL DEFAULT TRUE,
    recorded_by          VARCHAR(64) NOT NULL,
    recorded_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_param_value_lookup ON sys_parameter_value_records
    (tenant_id, parameter_id, param_role, is_valid);

CREATE TABLE IF NOT EXISTS sys_parameter_set_revisions (
    parameter_set_id     BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    set_code             VARCHAR(128) NOT NULL,
    revision_label       VARCHAR(32) NOT NULL,
    lifecycle_state      VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    parameter_values_map JSONB NOT NULL,
    set_hash             CHAR(64) NOT NULL,
    working_version      BIGINT NOT NULL DEFAULT 1,
    published_at         TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_param_set_rev ON sys_parameter_set_revisions(tenant_id, set_code, revision_label);

CREATE TABLE IF NOT EXISTS sys_simulation_models (
    model_id             BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    model_identifier     VARCHAR(128) NOT NULL,
    revision_label       VARCHAR(32) NOT NULL,
    model_type           VARCHAR(32) NOT NULL DEFAULT 'MODELICA_PACKAGE',
    source_artifact_id   BIGINT NOT NULL,
    modelica_entry_class VARCHAR(255) NOT NULL,
    solver_name          VARCHAR(64) NOT NULL DEFAULT 'dassl',
    default_tolerance    NUMERIC(12, 9) NOT NULL DEFAULT 0.00001,
    default_stop_time    NUMERIC(12, 4) NOT NULL DEFAULT 60.0,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_sim_model_rev ON sys_simulation_models(tenant_id, model_identifier, revision_label);

CREATE TABLE IF NOT EXISTS sys_model_mappings (
    mapping_id           BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    simulation_model_id  BIGINT NOT NULL REFERENCES sys_simulation_models(model_id),
    parameter_id         BIGINT NOT NULL REFERENCES sys_parameter_definitions(parameter_id),
    modelica_variable    VARCHAR(255) NOT NULL,
    conversion_expr      VARCHAR(255),
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_simulation_jobs (
    job_id               BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    project_id           VARCHAR(64) NOT NULL,
    job_name             VARCHAR(255) NOT NULL,
    model_id             BIGINT NOT NULL REFERENCES sys_simulation_models(model_id),
    parameter_set_id     BIGINT REFERENCES sys_parameter_set_revisions(parameter_set_id),
    frozen_input_json    JSONB NOT NULL,
    overall_status       VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    latest_run_id        BIGINT,
    total_retry_count    INT NOT NULL DEFAULT 0,
    created_by           VARCHAR(64) NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_simulation_runs (
    run_id               BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    job_id               BIGINT NOT NULL REFERENCES sys_simulation_jobs(job_id) ON DELETE CASCADE,
    attempt_number       INT NOT NULL DEFAULT 1,
    run_status           VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
    k8s_job_name         VARCHAR(128),
    k8s_pod_name         VARCHAR(128),
    worker_image_tag     VARCHAR(255) NOT NULL,
    worker_host_ip       VARCHAR(64),
    execution_token      VARCHAR(128) NOT NULL UNIQUE,
    started_at           TIMESTAMP WITH TIME ZONE,
    heartbeat_at         TIMESTAMP WITH TIME ZONE,
    ended_at             TIMESTAMP WITH TIME ZONE,
    exit_code            INT,
    error_message        TEXT,
    result_artifact_id   BIGINT,
    reproduce_bundle_id  BIGINT,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_simulation_kpis (
    kpi_id               BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    run_id               BIGINT NOT NULL REFERENCES sys_simulation_runs(run_id) ON DELETE CASCADE,
    kpi_code             VARCHAR(128) NOT NULL,
    kpi_name             VARCHAR(255) NOT NULL,
    measured_value       NUMERIC(18, 6) NOT NULL,
    unit                 VARCHAR(64) NOT NULL,
    kpi_status           VARCHAR(32) NOT NULL DEFAULT 'PASS',
    extracted_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_verification_cases (
    case_id              BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    case_code            VARCHAR(128) NOT NULL,
    case_title           VARCHAR(255) NOT NULL,
    target_requirement_id BIGINT NOT NULL,
    verification_method  VARCHAR(32) NOT NULL DEFAULT 'SIMULATION',
    acceptance_criterion TEXT NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_verification_assessments (
    assessment_id        BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    case_id              BIGINT NOT NULL REFERENCES sys_verification_cases(case_id),
    target_context_ref   VARCHAR(255) NOT NULL,
    verdict              VARCHAR(32) NOT NULL DEFAULT 'NOT_EXECUTED',
    applicability_state  VARCHAR(32) NOT NULL DEFAULT 'PENDING_REVIEW',
    supporting_run_id    BIGINT REFERENCES sys_simulation_runs(run_id),
    justification_notes  TEXT,
    reviewer_user_id     VARCHAR(64) NOT NULL,
    signed_hash          CHAR(64) NOT NULL,
    assessed_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 5. 跨域数字主线与关系网络底座 (D07, M23)
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_relation_type_definitions (
    relation_type_id     BIGINT PRIMARY KEY,
    relation_type_code   VARCHAR(64) NOT NULL UNIQUE,
    relation_name        VARCHAR(128) NOT NULL,
    inverse_name         VARCHAR(128) NOT NULL,
    is_directional       BOOLEAN NOT NULL DEFAULT TRUE,
    allowed_source_types JSONB NOT NULL,
    allowed_target_types JSONB NOT NULL,
    description          TEXT,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_trace_link_revisions (
    link_id              BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    relation_type        VARCHAR(64) NOT NULL REFERENCES sys_relation_type_definitions(relation_type_code),
    source_type          VARCHAR(64) NOT NULL,
    source_id            VARCHAR(128) NOT NULL,
    source_revision_ref  VARCHAR(64),
    source_display_name  VARCHAR(255) NOT NULL,
    target_type          VARCHAR(64) NOT NULL,
    target_id            VARCHAR(128) NOT NULL,
    target_revision_ref  VARCHAR(64),
    target_display_name  VARCHAR(255) NOT NULL,
    baseline_id          BIGINT,
    effective_from       TIMESTAMP WITH TIME ZONE,
    effective_to         TIMESTAMP WITH TIME ZONE,
    context_filters_json JSONB,
    lifecycle_state      VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by           VARCHAR(64) NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trace_forward_lookup ON sys_trace_link_revisions
    (tenant_id, source_type, source_id, relation_type) 
    INCLUDE (target_type, target_id, lifecycle_state);

CREATE INDEX IF NOT EXISTS idx_trace_backward_lookup ON sys_trace_link_revisions
    (tenant_id, target_type, target_id, relation_type) 
    INCLUDE (source_type, source_id, lifecycle_state);

CREATE TABLE IF NOT EXISTS sys_impact_analyses (
    analysis_id          BIGINT PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    change_request_id    BIGINT,
    root_source_type     VARCHAR(64) NOT NULL,
    root_source_id       VARCHAR(128) NOT NULL,
    max_depth_traversed  INT NOT NULL,
    total_nodes_found    INT NOT NULL,
    is_truncated         BOOLEAN NOT NULL DEFAULT FALSE,
    truncation_reason    VARCHAR(255),
    candidate_graph_json JSONB NOT NULL,
    analyzed_by          VARCHAR(64) NOT NULL,
    analyzed_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 6. 初始化种子元数据 (标准关系类型)
-- =============================================================================

INSERT INTO sys_relation_type_definitions 
    (relation_type_id, relation_type_code, relation_name, inverse_name, is_directional, allowed_source_types, allowed_target_types, description)
VALUES 
    (1, 'satisfies', '满足需求', 'satisfiedBy', true, '["SYSML_ELEMENT", "PART_REVISION"]', '["REQUIREMENT_REVISION"]', '模型元素或物料设计满足上位指标'),
    (2, 'verifies', '核实闭环', 'verifiedBy', true, '["VERIFICATION_ASSESSMENT", "TEST_CASE"]', '["REQUIREMENT_REVISION"]', '证据或用例判定满足需求'),
    (3, 'allocatedTo', '逻辑架构分配', 'allocatedFrom', true, '["SYSML_ELEMENT"]', '["PART_REVISION", "BOM_LINE"]', '模型逻辑部件分配至物理BOM'),
    (4, 'derivedFrom', '派生溯源', 'derivesTo', true, '["REQUIREMENT_REVISION", "ORDER_DEFINITION"]', '["REQUIREMENT_REVISION", "PLATFORM_REVISION"]', '需求分解或ETO派生'),
    (5, 'mappedTo', '参数映射', 'mappedFrom', true, '["PARAMETER_DEFINITION"]', '["SIMULATION_MODEL_VARIABLE"]', '系统参数绑定仿真变量'),
    (6, 'evidenceFor', '证据支撑', 'supportedBy', true, '["SIMULATION_RUN", "TEST_DATASET"]', '["EVIDENCE_RECORD"]', '求解产物支撑验证证据')
ON CONFLICT (relation_type_code) DO NOTHING;
