-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M19 图文档与文件制品 (Engineering Documents & Artifacts)
-- 适用环境: PostgreSQL 15+
-- 规范依据: CCD-DEV-SPEC-2.0-M19
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_doc;

-- 1. 枚举类型定义 (安全幂等创建)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'dataset_role' AND n.nspname = 'plm_doc') THEN
        CREATE TYPE plm_doc.dataset_role AS ENUM (
            'PRIMARY_NATIVE',     -- 原生设计源文件 (如 SLDPRT, SLDASM, DWG, DOCX)
            'DERIVATIVE_PDF',     -- 转换生成的受控预览 PDF
            'DERIVATIVE_VIEW3D',  -- Web 轻量化预览网格 (如 glTF, 3D Tiles)
            'ATTACHMENT',         -- 辅助参考附件
            'SIGNATURE_STAMP'     -- 电子图章与防伪凭证
        );
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = 'security_classification' AND n.nspname = 'plm_doc') THEN
        CREATE TYPE plm_doc.security_classification AS ENUM (
            'PUBLIC',             -- 公开
            'INTERNAL',           -- 内部受控
            'CONFIDENTIAL',       -- 机密/核心技术
            'RESTRICTED'          -- 绝密/核心代码与参数
        );
    END IF;
END$$;

-- 2. 文档主对象业务扩展表 (DocumentMaster)
CREATE TABLE IF NOT EXISTS plm_doc.document_master (
    master_id               BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'VMC_ENTERPRISE',
    document_number         VARCHAR(128) NOT NULL,
    document_title          VARCHAR(255) NOT NULL,
    doc_category_code       VARCHAR(64) NOT NULL, -- MECH_DRAWING, ELEC_SCHEMATIC, TECH_SPEC, TEST_REPORT, USER_MANUAL
    doc_template_id         BIGINT NULL,
    default_security_level  plm_doc.security_classification NOT NULL DEFAULT 'INTERNAL',
    department_id           VARCHAR(64) NOT NULL,
    created_by              VARCHAR(64) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m19_doc_number UNIQUE (tenant_id, document_number)
);
COMMENT ON TABLE plm_doc.document_master IS 'M19: 文档主业务对象，承载全局唯一业务编号与受控分类属性，禁止物理删除';

-- 3. 文档修订版本业务扩展表 (DocumentRevision)
CREATE TABLE IF NOT EXISTS plm_doc.document_revision (
    revision_id             BIGINT PRIMARY KEY,
    master_id               BIGINT NOT NULL REFERENCES plm_doc.document_master(master_id) ON DELETE RESTRICT,
    revision_label          VARCHAR(32) NOT NULL DEFAULT 'A.1',
    lifecycle_state         VARCHAR(32) NOT NULL DEFAULT 'DRAFT'
                            CHECK (lifecycle_state IN ('DRAFT', 'IN_REVIEW', 'RELEASED', 'OBSOLETE', 'ARCHIVED')),
    security_level          plm_doc.security_classification NOT NULL DEFAULT 'INTERNAL',
    page_count              INT NULL,
    cad_software_type       VARCHAR(64) NULL, -- SolidWorks, NX, CATIA, AutoCAD 等
    cad_software_version    VARCHAR(32) NULL,
    is_template             BOOLEAN NOT NULL DEFAULT FALSE,
    summary                 TEXT NULL,
    created_by              VARCHAR(64) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m19_rev_label UNIQUE (master_id, revision_label)
);
COMMENT ON TABLE plm_doc.document_revision IS 'M19: 文档受控工程修订版，生命周期依托状态机驱动';

-- 4. 签出与协作排他锁表 (DocumentLock - 悲观并发控制)
CREATE TABLE IF NOT EXISTS plm_doc.document_lock (
    revision_id             BIGINT PRIMARY KEY REFERENCES plm_doc.document_revision(revision_id) ON DELETE CASCADE,
    locked_by_user_id       VARCHAR(64) NOT NULL,
    client_machine_ip       VARCHAR(64) NOT NULL,
    checkout_comments       TEXT,
    locked_at               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lock_expires_at         TIMESTAMPTZ NOT NULL, -- 自动熔断过期时间
    CONSTRAINT chk_m19_lock_duration CHECK (lock_expires_at > locked_at)
);
COMMENT ON TABLE plm_doc.document_lock IS 'M19: 签出排他锁，锁定处于 DRAFT 状态的修订版，防止协同冲突';

-- 5. 文件制品物理元数据表 (Artifact - 核心防篡改不可变表)
CREATE TABLE IF NOT EXISTS plm_doc.artifact (
    artifact_id             BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'VMC_ENTERPRISE',
    storage_bucket          VARCHAR(128) NOT NULL,
    storage_object_path     VARCHAR(512) NOT NULL, -- 存储桶内路径
    file_name               VARCHAR(255) NOT NULL,
    file_extension          VARCHAR(32) NOT NULL,
    file_size_bytes         BIGINT NOT NULL,
    mime_type               VARCHAR(128) NOT NULL,
    sha256_hash             CHAR(64) NOT NULL,    -- 核心不可变强哈希摘要
    etag                    VARCHAR(128) NOT NULL,
    is_quarantined          BOOLEAN NOT NULL DEFAULT FALSE, -- 防病毒扫描隔离标记
    uploaded_by             VARCHAR(64) NOT NULL,
    uploaded_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m19_artifact_tenant_hash UNIQUE (tenant_id, sha256_hash),
    CONSTRAINT chk_m19_artifact_size CHECK (file_size_bytes > 0)
);
COMMENT ON TABLE plm_doc.artifact IS 'M19: 物理文件制品表，哈希永久不可变，禁止原位 UPDATE 与物理 DELETE';
CREATE INDEX IF NOT EXISTS idx_m19_artifact_lookup_hash ON plm_doc.artifact(sha256_hash);

-- 6. 数据集逻辑容器表 (Dataset)
CREATE TABLE IF NOT EXISTS plm_doc.dataset (
    dataset_id              BIGINT PRIMARY KEY,
    revision_id             BIGINT NOT NULL REFERENCES plm_doc.document_revision(revision_id) ON DELETE CASCADE,
    dataset_code            VARCHAR(64) NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_m19_dataset_rev_code UNIQUE (revision_id, dataset_code)
);

-- 7. 数据集与物理制品受控绑定关系表 (DatasetArtifactBinding)
CREATE TABLE IF NOT EXISTS plm_doc.dataset_artifact_binding (
    binding_id              BIGINT PRIMARY KEY,
    dataset_id              BIGINT NOT NULL REFERENCES plm_doc.dataset(dataset_id) ON DELETE CASCADE,
    artifact_id             BIGINT NOT NULL REFERENCES plm_doc.artifact(artifact_id) ON DELETE RESTRICT,
    file_role               plm_doc.dataset_role NOT NULL,
    is_current              BOOLEAN NOT NULL DEFAULT TRUE,
    bound_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    bound_by                VARCHAR(64) NOT NULL,
    CONSTRAINT uq_m19_dataset_artifact_role UNIQUE (dataset_id, artifact_id, file_role)
);
CREATE INDEX IF NOT EXISTS idx_m19_binding_artifact ON plm_doc.dataset_artifact_binding(artifact_id);

-- 8. 派生制品谱系来源表 (ArtifactDerivation)
CREATE TABLE IF NOT EXISTS plm_doc.artifact_derivation (
    derivation_id           BIGINT PRIMARY KEY,
    source_artifact_id      BIGINT NOT NULL REFERENCES plm_doc.artifact(artifact_id) ON DELETE RESTRICT,
    derived_artifact_id     BIGINT NOT NULL REFERENCES plm_doc.artifact(artifact_id) ON DELETE RESTRICT,
    converter_engine        VARCHAR(64) NOT NULL, -- 如 LibreOffice, PDFTron, OCC_Converter
    converter_version       VARCHAR(32) NOT NULL,
    derivation_parameters   JSONB NOT NULL DEFAULT '{}'::jsonb, -- 水印文字、精度配置
    converted_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_m19_derivation_not_self CHECK (source_artifact_id != derived_artifact_id)
);
COMMENT ON TABLE plm_doc.artifact_derivation IS 'M19: 派生来源追踪表，明确 PDF/轻量化与原生源文件的谱系关系';
CREATE INDEX IF NOT EXISTS idx_m19_derivation_source ON plm_doc.artifact_derivation(source_artifact_id);

-- 9. 在线协同批注图层表 (DocumentAnnotation - 非破坏性批注)
CREATE TABLE IF NOT EXISTS plm_doc.document_annotation (
    annotation_id           BIGINT PRIMARY KEY,
    revision_id             BIGINT NOT NULL REFERENCES plm_doc.document_revision(revision_id) ON DELETE CASCADE,
    target_artifact_id      BIGINT NOT NULL REFERENCES plm_doc.artifact(artifact_id), -- 批注所依托的受控 PDF
    page_number             INT NOT NULL,
    annotation_type         VARCHAR(32) NOT NULL, -- TEXT, HIGHLIGHT, DRAWING, RECTANGLE
    geometry_data           JSONB NOT NULL,       -- 矢量坐标与尺寸数据
    content_text            TEXT,
    author_id               VARCHAR(64) NOT NULL,
    is_resolved             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_m19_annot_revision_page ON plm_doc.document_annotation(revision_id, page_number);

-- 10. 文件物理访问与下载审计表 (FileAccessAudit)
CREATE TABLE IF NOT EXISTS plm_doc.file_access_audit (
    audit_id                BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL DEFAULT 'VMC_ENTERPRISE',
    user_id                 VARCHAR(64) NOT NULL,
    artifact_id             BIGINT NOT NULL REFERENCES plm_doc.artifact(artifact_id),
    revision_id             BIGINT NULL,
    access_type             VARCHAR(32) NOT NULL, -- PREVIEW, DOWNLOAD, EXPORT_PACKAGE
    client_ip               VARCHAR(64) NOT NULL,
    user_agent              VARCHAR(255),
    authorization_ticket    VARCHAR(128) NOT NULL,
    download_bytes          BIGINT NULL,
    accessed_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_m19_access_audit_user ON plm_doc.file_access_audit(user_id, accessed_at DESC);
CREATE INDEX IF NOT EXISTS idx_m19_access_audit_artifact ON plm_doc.file_access_audit(artifact_id, accessed_at DESC);

-- =============================================================================
-- 11. 核心完整性与只读防篡改触发器
-- =============================================================================

CREATE OR REPLACE FUNCTION plm_doc.fn_enforce_artifact_immutability()
RETURNS TRIGGER AS $$
BEGIN
    IF (OLD.sha256_hash != NEW.sha256_hash) OR 
       (OLD.storage_object_path != NEW.storage_object_path) OR 
       (OLD.storage_bucket != NEW.storage_bucket) OR 
       (OLD.file_size_bytes != NEW.file_size_bytes) THEN
        RAISE EXCEPTION 'Architecture Constraint Violation [CST-M19-01]: Artifact [%] is strictly IMMUTABLE. In-place binary or hash modification is prohibited.',
            OLD.artifact_id USING ERRCODE = '23000';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_enforce_artifact_immutable ON plm_doc.artifact;
CREATE TRIGGER trg_enforce_artifact_immutable
BEFORE UPDATE ON plm_doc.artifact
FOR EACH ROW
EXECUTE FUNCTION plm_doc.fn_enforce_artifact_immutability();

CREATE OR REPLACE FUNCTION plm_doc.fn_check_checkout_lock()
RETURNS TRIGGER AS $$
DECLARE
    v_locked_user VARCHAR(64);
    v_expires_at TIMESTAMPTZ;
BEGIN
    SELECT dl.locked_by_user_id, dl.lock_expires_at 
    INTO v_locked_user, v_expires_at
    FROM plm_doc.dataset d
    JOIN plm_doc.document_lock dl ON d.revision_id = dl.revision_id
    WHERE d.dataset_id = NEW.dataset_id;

    IF FOUND THEN
        IF v_expires_at > CURRENT_TIMESTAMP AND v_locked_user != NEW.bound_by THEN
            RAISE EXCEPTION 'Concurrency Lock Violation: Document Revision is exclusively checked out by [%] until [%]. User [%] cannot modify datasets.',
                v_locked_user, v_expires_at, NEW.bound_by USING ERRCODE = '55P03';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_check_dataset_lock ON plm_doc.dataset_artifact_binding;
CREATE TRIGGER trg_check_dataset_lock
BEFORE INSERT OR UPDATE ON plm_doc.dataset_artifact_binding
FOR EACH ROW
EXECUTE FUNCTION plm_doc.fn_check_checkout_lock();

-- =============================================================================
-- 12. 初始化代表性数控机床工程图文档种子数据
-- =============================================================================

-- Master 1: 五轴机床高速主轴箱 3D 装配图
INSERT INTO plm_doc.document_master (master_id, tenant_id, document_number, document_title, doc_category_code, default_security_level, department_id, created_by)
VALUES (7001, 'VMC_ENTERPRISE', 'DOC-VMC850-MECH-001', 'VMC850 高速电动主轴箱三维总装图', 'MECH_DRAWING', 'CONFIDENTIAL', 'DEPT-MECH', 'ENG-ZHOU')
ON CONFLICT (tenant_id, document_number) DO NOTHING;

INSERT INTO plm_doc.document_revision (revision_id, master_id, revision_label, lifecycle_state, security_level, page_count, cad_software_type, cad_software_version, summary, created_by)
VALUES (7101, 7001, 'A.1', 'RELEASED', 'CONFIDENTIAL', 48, 'SolidWorks', '2024 SP2', '定型投产主轴箱总成，最高转速 24000 rpm，带油气润滑与循环水冷水道', 'ENG-ZHOU')
ON CONFLICT (master_id, revision_label) DO NOTHING;

-- Master 2: 双工位液压驱动回转工作台 2D 工程图
INSERT INTO plm_doc.document_master (master_id, tenant_id, document_number, document_title, doc_category_code, default_security_level, department_id, created_by)
VALUES (7002, 'VMC_ENTERPRISE', 'DOC-HMC630-MECH-002', 'HMC630 双工位回转工作台装配与配合尺寸公差图样', 'MECH_DRAWING', 'INTERNAL', 'DEPT-MECH', 'ENG-LI')
ON CONFLICT (tenant_id, document_number) DO NOTHING;

INSERT INTO plm_doc.document_revision (revision_id, master_id, revision_label, lifecycle_state, security_level, page_count, cad_software_type, cad_software_version, summary, created_by)
VALUES (7102, 7002, 'B.0', 'DRAFT', 'INTERNAL', 12, 'AutoCAD', '2024', '针对重切削工况优化鼠牙盘分度齿定位刚性，重复定位精度达 2.5 角秒', 'ENG-LI')
ON CONFLICT (master_id, revision_label) DO NOTHING;

-- Master 3: 五轴数控系统电气原理图
INSERT INTO plm_doc.document_master (master_id, tenant_id, document_number, document_title, doc_category_code, default_security_level, department_id, created_by)
VALUES (7003, 'VMC_ENTERPRISE', 'DOC-SYS-ELEC-003', 'GMC2030 五轴龙门加工中心数控柜电气拓扑与总线接线图', 'ELEC_SCHEMATIC', 'INTERNAL', 'DEPT-ELEC', 'ENG-WANG')
ON CONFLICT (tenant_id, document_number) DO NOTHING;

INSERT INTO plm_doc.document_revision (revision_id, master_id, revision_label, lifecycle_state, security_level, page_count, cad_software_type, cad_software_version, summary, created_by)
VALUES (7103, 7003, 'A.2', 'RELEASED', 'INTERNAL', 36, 'EPLAN Pro Panel', '2024', '配置西门子 ONE 数控系统与光栅尺闭环总线驱动架构', 'ENG-WANG')
ON CONFLICT (master_id, revision_label) DO NOTHING;

-- Master 4: 激光干涉仪螺距误差补偿与动平衡检验报告
INSERT INTO plm_doc.document_master (master_id, tenant_id, document_number, document_title, doc_category_code, default_security_level, department_id, created_by)
VALUES (7004, 'VMC_ENTERPRISE', 'DOC-QC-TEST-004', 'VMC850 全行程激光干涉仪定位精度及螺距误差补偿检测报告', 'TEST_REPORT', 'CONFIDENTIAL', 'DEPT-QC', 'ENG-CHEN')
ON CONFLICT (tenant_id, document_number) DO NOTHING;

INSERT INTO plm_doc.document_revision (revision_id, master_id, revision_label, lifecycle_state, security_level, page_count, cad_software_type, cad_software_version, summary, created_by)
VALUES (7104, 7004, 'A.0', 'RELEASED', 'CONFIDENTIAL', 8, 'Renishaw LaserXL', 'v10.2', 'ISO 230-2 标准全行程检测，双向重复定位精度 0.003mm 达标', 'ENG-CHEN')
ON CONFLICT (master_id, revision_label) DO NOTHING;

-- 初始物理文件制品 (Artifacts)
INSERT INTO plm_doc.artifact (artifact_id, tenant_id, storage_bucket, storage_object_path, file_name, file_extension, file_size_bytes, mime_type, sha256_hash, etag, uploaded_by)
VALUES 
(9001, 'VMC_ENTERPRISE', 'ccddesigner-raw-vault', 'VMC_ENTERPRISE/2026/8f/c3/8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01.sldasm', 'VMC850_Spindle_Assembly.sldasm', 'sldasm', 1284901824, 'application/x-solidworks-assembly', '8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01', 'etag-spindle-asm', 'ENG-ZHOU'),
(9002, 'VMC_ENTERPRISE', 'ccddesigner-derivative-vault', 'VMC_ENTERPRISE/2026/e3/b0/e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855.pdf', 'VMC850_Spindle_Assembly_Controlled.pdf', 'pdf', 15482910, 'application/pdf', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'etag-spindle-pdf', 'SYSTEM_WORKER'),
(9003, 'VMC_ENTERPRISE', 'ccddesigner-raw-vault', 'VMC_ENTERPRISE/2026/a1/b2/a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef00.dwg', 'HMC630_Turntable_Drawing.dwg', 'dwg', 42890120, 'image/vnd.dwg', 'a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef00', 'etag-turntable-dwg', 'ENG-LI'),
(9004, 'VMC_ENTERPRISE', 'ccddesigner-raw-vault', 'VMC_ENTERPRISE/2026/b2/c3/b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0100.pdf', 'GMC2030_Elec_Schematics.pdf', 'pdf', 28910240, 'application/pdf', 'b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0100', 'etag-elec-pdf', 'ENG-WANG'),
(9005, 'VMC_ENTERPRISE', 'ccddesigner-raw-vault', 'VMC_ENTERPRISE/2026/c3/d4/c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef01234.pdf', 'VMC850_Laser_Interferometer_Report.pdf', 'pdf', 8492010, 'application/pdf', 'c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef01234', 'etag-laser-pdf', 'ENG-CHEN')
ON CONFLICT (tenant_id, sha256_hash) DO NOTHING;

-- 初始数据集与文件绑定
INSERT INTO plm_doc.dataset (dataset_id, revision_id, dataset_code, name)
VALUES 
(8001, 7101, 'DS-SPINDLE-NATIVE', '主轴箱原生 SolidWorks 装配体模型数据集'),
(8002, 7101, 'DS-SPINDLE-DERIVATIVE', '主轴箱受控归档审图 PDF 数据集'),
(8003, 7102, 'DS-TURNTABLE-NATIVE', '回转工作台原生 AutoCAD 图纸数据集'),
(8004, 7103, 'DS-ELEC-SCHEMATIC', '电气原理图数据集'),
(8005, 7104, 'DS-LASER-REPORT', '激光干涉仪检测凭据数据集')
ON CONFLICT (revision_id, dataset_code) DO NOTHING;

INSERT INTO plm_doc.dataset_artifact_binding (binding_id, dataset_id, artifact_id, file_role, is_current, bound_by)
VALUES 
(8101, 8001, 9001, 'PRIMARY_NATIVE', TRUE, 'ENG-ZHOU'),
(8102, 8002, 9002, 'DERIVATIVE_PDF', TRUE, 'SYSTEM_WORKER'),
(8103, 8003, 9003, 'PRIMARY_NATIVE', TRUE, 'ENG-LI'),
(8104, 8004, 9004, 'PRIMARY_NATIVE', TRUE, 'ENG-WANG'),
(8105, 8005, 9005, 'PRIMARY_NATIVE', TRUE, 'ENG-CHEN')
ON CONFLICT (dataset_id, artifact_id, file_role) DO NOTHING;

-- 初始派生谱系 (SLDASM -> 受控 PDF)
INSERT INTO plm_doc.artifact_derivation (derivation_id, source_artifact_id, derived_artifact_id, converter_engine, converter_version, derivation_parameters)
VALUES 
(8201, 9001, 9002, 'PDFTron_CAD_Converter', 'v10.4.1', '{"watermark": "CONFIDENTIAL", "dpi": 300, "colorSpace": "CMYK"}'::jsonb)
ON CONFLICT (derivation_id) DO NOTHING;

-- 初始非破坏性批注 (对主轴箱受控 PDF 的第 1 页圈阅)
INSERT INTO plm_doc.document_annotation (annotation_id, revision_id, target_artifact_id, page_number, annotation_type, geometry_data, content_text, author_id, is_resolved)
VALUES 
(8301, 7101, 9002, 1, 'RECTANGLE', '{"x": 120, "y": 280, "width": 160, "height": 80, "color": "#ff4d4f"}'::jsonb, '校对意见：前端角接触球轴承预紧弹簧座配合公差建议由 H7/k6 调整为 H7/h6 以降低热膨胀卡滞风险', 'CHIEF-ENG-ZHANG', FALSE)
ON CONFLICT (annotation_id) DO NOTHING;

-- 初始物理访问审计记录
INSERT INTO plm_doc.file_access_audit (audit_id, tenant_id, user_id, artifact_id, revision_id, access_type, client_ip, user_agent, authorization_ticket, download_bytes)
VALUES 
(8401, 'VMC_ENTERPRISE', 'ENG-ZHOU', 9002, 7101, 'PREVIEW', '192.168.10.42', 'Chrome/124.0.0.0', 'tkt_preview_7182901', 15482910)
ON CONFLICT (audit_id) DO NOTHING;
