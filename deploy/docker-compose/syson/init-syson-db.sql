-- =============================================================================
-- SysON 独立 PostgreSQL 实例初始化脚本 (按项目/租户垂直隔离)
-- 数据库: syson_workspace_db | 适用环境: PostgreSQL 15+
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS syson_core;

-- 1. SysON 建模项目元数据表 (按租户与项目隔离)
CREATE TABLE IF NOT EXISTS syson_core.syson_project (
    project_uuid            VARCHAR(64) PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'TENANT-DEFAULT',
    project_code            VARCHAR(128) NOT NULL,
    project_name            VARCHAR(255) NOT NULL,
    model_language          VARCHAR(32) NOT NULL DEFAULT 'SYSML_V2',
    default_branch          VARCHAR(64) NOT NULL DEFAULT 'main',
    active_session_token    VARCHAR(128) NULL,
    locked_by_user_id       VARCHAR(64) NULL,
    locked_at               TIMESTAMPTZ NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_syson_proj_tenant_code UNIQUE (tenant_id, project_code)
);

-- 2. SysON 图元与模型元素临时草稿表 (Working Model)
CREATE TABLE IF NOT EXISTS syson_core.syson_model_element (
    element_uuid            VARCHAR(64) PRIMARY KEY,
    project_uuid            VARCHAR(64) NOT NULL REFERENCES syson_core.syson_project(project_uuid) ON DELETE CASCADE,
    parent_element_uuid     VARCHAR(64) NULL,
    element_type            VARCHAR(64) NOT NULL, -- PartUsage, RequirementUsage, Port, Action
    name                    VARCHAR(255) NOT NULL,
    qualified_name          TEXT NOT NULL,
    package_name            VARCHAR(128) NOT NULL,
    raw_payload_json        JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. SysON 视口在线编辑会话表
CREATE TABLE IF NOT EXISTS syson_core.syson_editing_session (
    session_token           VARCHAR(128) PRIMARY KEY,
    project_uuid            VARCHAR(64) NOT NULL REFERENCES syson_core.syson_project(project_uuid) ON DELETE CASCADE,
    user_id                 VARCHAR(64) NOT NULL,
    channel                 VARCHAR(32) NOT NULL DEFAULT 'GRAPHICAL',
    ip_address              VARCHAR(64) NULL,
    expires_at              TIMESTAMPTZ NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 预置 VMC1000 五轴加工中心专用图形工程
INSERT INTO syson_core.syson_project (
    project_uuid, tenant_id, project_code, project_name, model_language, default_branch
) VALUES (
    'syson-proj-uuid-88192a01-c918',
    'TENANT-DEFAULT',
    'SMP-VMC1000-01',
    'VMC1000 五轴立式加工中心系统工程模型',
    'SYSML_V2',
    'main'
) ON CONFLICT (project_uuid) DO NOTHING;

-- 预置 VMC1000 核心图元
INSERT INTO syson_core.syson_model_element (
    element_uuid, project_uuid, element_type, name, qualified_name, package_name
) VALUES 
('elem_part_vmc', 'syson-proj-uuid-88192a01-c918', 'PartUsage', 'VMC1000Structure', 'VMC1000_SystemModel::04_PhysicalArchitecture::VMC1000Structure', '04_PhysicalArchitecture'),
('elem_part_xfeed', 'syson-proj-uuid-88192a01-c918', 'PartUsage', 'XAxisFeedSystem', 'VMC1000_SystemModel::04_PhysicalArchitecture::XAxisFeedSystem', '04_PhysicalArchitecture'),
('elem_part_spindle', 'syson-proj-uuid-88192a01-c918', 'PartUsage', 'HighSpeedSpindle', 'VMC1000_SystemModel::04_PhysicalArchitecture::HighSpeedSpindle', '04_PhysicalArchitecture'),
('elem_part_cnc', 'syson-proj-uuid-88192a01-c918', 'PartUsage', 'CncMotionController', 'VMC1000_SystemModel::03_LogicalArchitecture::CncMotionController', '03_LogicalArchitecture')
ON CONFLICT (element_uuid) DO NOTHING;
