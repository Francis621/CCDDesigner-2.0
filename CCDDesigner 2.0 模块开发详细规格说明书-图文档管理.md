# CCDDesigner 2.0 模块开发详细规格说明书

## M19: 图文档与文件制品 (Engineering Documents and File Artifacts)

| **文档属性**    | **内容**                                                     |
| --------------- | ------------------------------------------------------------ |
| **模块编号**    | `M19` (Phase: P1, Type: N)                                   |
| **文档编号**    | `CCD-DEV-SPEC-2.0-M19`                                       |
| **版本 / 状态** | V1.0 / 评审发布稿                                            |
| **主责模块**    | M19 图文档与文件制品                                         |
| **协同模块**    | M01 (统一工作台)、M02 (项目与交付物)、M04/M06 (模型发布与制品)、M10 (仿真结果归档)、M11 (证据管理)、M16/M17 (EBOM与CAD协同)、M20 (工程对象与生命周期)、M21 (基线服务)、M24 (工作流与审批)、M30 (安全审计与MinIO适配) |
| **上位依据**    | 《CCDDesigner 2.0 产品开发说明书》(`CCD-DEV-SPEC-2.0-001`) §1.2, §2.2, §4.5 (M19), §8.4    《CCDDesigner 2.0 产品功能架构与模块设计说明书》(`CCD-ARCH-FUNC-2.0-001`) §7, §27, §40.1, §41.1, §44.2 |
| **适用受众**    | 数据架构师、文件存储与微服务开发工程师、CAD/CAE 集成开发、安全合规审计员 |

### 1. 模块定位与核心设计原则

依据上位开发说明书与架构设计要求，M19 承担全系统非结构化工程文件制品的安全物理存储、哈希防篡改校验、业务生命周期绑定及格式派生转换的权威职责：  

1. **四层实体解耦模型（Master-Revision-Dataset-Artifact）**：
   - **`DocumentMaster`（文档主对象）**：承载全局唯一业务编号与受控分类属性，代表文档的跨版本业务身份。  
   - **`DocumentRevision`（文档修订版）**：承载研制状态机（`DRAFT`、`RELEASED` 等）、密级及工程审批属性。  
   - **`Dataset`（文件数据集）**：作为一组物理文件的业务逻辑容器，区分主要文件（Primary/Native）、派生文件（Derivative）与预览附件（Attachment）。  
   - **`Artifact`（文件物理制品）**：严格对应 MinIO 存储桶内的单一不可变字节流对象，固化文件大小、MIME 类型与 SHA-256 强校验摘要。  
2. **制品哈希绝对不可变原则（Immutability Enforcement）**：
   - 物理文件上传成功并完成哈希强校验后，其 `artifactId` 与对应的底层存储字节流绑定并永久只读。  
   - **严禁原位替换底层字节**：任何图纸改动、重新上传或内容更正，均必须生成全新的 `Artifact` 实体并分配独立 Snowflake ID。  
3. **派生文件全生命周期溯源（Derivative Provenance）**：
   - 异步轻量化转换引擎生成的受控 PDF、Web 3D 视图或缩略图，必须作为新的 `Artifact` 进行持久化登记，并通过 `ArtifactDerivation` 显式记录派生算法版本、转换时间戳及源文件引用。  
4. **四步受控上传管道（Staging Pipeline）**：
   - 文件必须严格遵循“临时分片暂存（Staging） $\rightarrow$ 分片合并与 SHA-256 强校验 $\rightarrow$ 制品元数据持久化登记 $\rightarrow$ 业务对象版本关联绑定”四阶段生命周期。未完成哈希校验的文件绝对禁止被业务版本或工程基线引用。  
5. **安全去重与租户边界隔离**：
   - 系统支持底层基于 SHA-256 内容摘要的文件块级存储去重，但**安全授权与业务可见性严格受控于 PBAC 和租户边界**；严禁因摘要相同而跨租户或跨项目穿透授予未授权用户访问权限。  

### 2. 双向追踪矩阵 (Bidirectional Traceability Matrix)

| **功能编号 / 约束规范**                | **主责与协同模块  MD+ 1** | **上位架构依据与章节  MD+ 1**                          | **覆盖验收用例  MD+ 1**    | **核心控制逻辑与阻断行为**                                   |
| -------------------------------------- | ------------------------- | ------------------------------------------------------ | -------------------------- | ------------------------------------------------------------ |
| **M19-F01** (分类、修订与签入/签出锁)  | M19, M20, M30             | CCD-DEV-SPEC §4.5    CCD-ARCH-FUNC §27, §40.1          | AT-03, AT-14, AT-16        | 签出施加排他排他悲观锁；非锁持有者禁止上传覆盖草稿；终态已发布修订只读 |
| **M19-F02** (分片上传、哈希校验与登记) | M19, M30                  | CCD-DEV-SPEC §1.2, §4.5    CCD-ARCH-FUNC §27, §41.1    | AT-01, AT-04, AT-30        | MinIO 分片合并校验 SHA-256；摘要不匹配直接清理临时分片并拒绝登记 |
| **M19-F03** (PDF派生、受控水印与批注)  | M19, M24                  | CCD-DEV-SPEC §4.5, ADR-03    CCD-ARCH-FUNC §27         | AT-01, AT-27               | 转换失败生成报警并标记降级可用；动态注入受控安全水印；批注为矢量独立图层 |
| **M19-F04** (跨域工程引用绑定)         | M19, M02, M06, M11, M16   | CCD-DEV-SPEC §2.2, §3.1    CCD-ARCH-FUNC §5, §27       | AT-01, AT-07, AT-14, AT-18 | 建立零件、模型发布、阶段门交付物及验证证据到文件制品的统一引用与版本锁定 |
| **M19-F05** (受控打包导出与访问审计)   | M19, M01, M30             | CCD-DEV-SPEC §7.1, §7.2    CCD-ARCH-FUNC §27, §42      | AT-13, AT-14               | 权限撤销后即时阻断预签名 URL；导出包内嵌校验清单；逐次读取写穿防篡改审计表 |
| **CST-M19-01** (制品哈希不可变约束)    | M19, M20                  | CCD-DEV-SPEC §1.1(5), §4.5    CCD-ARCH-FUNC §1(5), §27 | AT-03, AT-04, AT-16        | 数据库触发器物理拦截对 `artifact` 表记录的原位 `UPDATE` 和 `DELETE` 操作 |

### 3. 领域对象模型与 ER 关系 (Mermaid ERD)

代码段

```
erDiagram
    OBJ_MASTER ||--o| DOCUMENT_MASTER : "polymorphic_extension"
    OBJ_REVISION ||--o| DOCUMENT_REVISION : "polymorphic_extension"
    
    DOCUMENT_MASTER ||--o{ DOCUMENT_REVISION : "has_revisions"
    DOCUMENT_REVISION ||--o| DOCUMENT_LOCK : "secured_by"
    DOCUMENT_REVISION ||--|{ DATASET : "contains_datasets"
    
    DATASET ||--|{ DATASET_ARTIFACT_BINDING : "binds_files"
    ARTIFACT ||--o{ DATASET_ARTIFACT_BINDING : "referenced_by"
    ARTIFACT ||--o{ ARTIFACT_DERIVATION : "derived_as_source"
    ARTIFACT ||--o{ ARTIFACT_DERIVATION : "derived_as_target"
    
    DOCUMENT_REVISION ||--o{ DOCUMENT_ANNOTATION : "annotated_by"
    ARTIFACT ||--o{ FILE_ACCESS_AUDIT : "monitored_access"
    
    %% 跨模块多态关联
    DATASET_ARTIFACT_BINDING }o--o| OBJ_REVISION : "attached_to_part_or_req"
    DATASET_ARTIFACT_BINDING }o--o| BASELINE : "locked_in_baseline"
```

### 4. 物理数据字典与 PostgreSQL DDL 规范

以下物理 DDL 属于 `plm_doc` Schema，并与 D01 规范定义的 `plm_govern` 治理底座深度联动。

SQL

```
-- =============================================================================
-- CCDDesigner 2.0 模块数据定义: M19 图文档与文件制品
-- 适用环境: PostgreSQL 15+
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS plm_doc;

-- 文档用途角色枚举
CREATE TYPE plm_doc.dataset_role AS ENUM (
    'PRIMARY_NATIVE',     -- 原生设计源文件 (如 SLDPRT, DWG, DOCX)
    'DERIVATIVE_PDF',     -- 转换生成的受控预览 PDF
    'DERIVATIVE_VIEW3D',  -- Web 轻量化预览网格 (如 glTF, 3D Tiles)
    'ATTACHMENT',         -- 辅助参考附件
    'SIGNATURE_STAMP'     -- 电子图章与防伪凭证
);

-- 文档安全密级枚举
CREATE TYPE plm_doc.security_classification AS ENUM (
    'PUBLIC',             -- 公开
    'INTERNAL',           -- 内部受控
    'CONFIDENTIAL',       -- 机密/核心技术
    'RESTRICTED'          -- 绝密/核心代码与参数
);

-- 1. 文档主对象业务扩展表 (DocumentMaster)
CREATE TABLE plm_doc.document_master (
    master_id               BIGINT PRIMARY KEY REFERENCES plm_govern.obj_master(master_id) ON DELETE RESTRICT,
    doc_category_code       VARCHAR(64) NOT NULL, -- 如 MECH_DRAWING, TECH_SPEC, TEST_REPORT
    doc_template_id         BIGINT NULL,
    default_security_level  plm_doc.security_classification NOT NULL DEFAULT 'INTERNAL',
    department_id           VARCHAR(64) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE plm_doc.document_master IS 'M19: 文档主业务对象，继承 obj_master 唯一身份，禁止物理删除';

-- 2. 文档修订版本业务扩展表 (DocumentRevision)
CREATE TABLE plm_doc.document_revision (
    revision_id             BIGINT PRIMARY KEY REFERENCES plm_govern.obj_revision(revision_id) ON DELETE RESTRICT,
    security_level          plm_doc.security_classification NOT NULL DEFAULT 'INTERNAL',
    page_count              INT NULL,
    cad_software_type       VARCHAR(64) NULL, -- SolidWorks, NX, CATIA, AutoCAD 等
    cad_software_version    VARCHAR(32) NULL,
    is_template             BOOLEAN NOT NULL DEFAULT FALSE,
    summary                 TEXT NULL
);
COMMENT ON TABLE plm_doc.document_revision IS 'M19: 文档受控工程修订版，生命周期依托 obj_revision 驱动';

-- 3. 签出与协作排他锁表 (DocumentLock - 悲观并发控制)
CREATE TABLE plm_doc.document_lock (
    revision_id             BIGINT PRIMARY KEY REFERENCES plm_doc.document_revision(revision_id) ON DELETE CASCADE,
    locked_by_user_id       VARCHAR(64) NOT NULL,
    client_machine_ip       VARCHAR(64) NOT NULL,
    checkout_comments       TEXT,
    locked_at               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lock_expires_at         TIMESTAMPTZ NOT NULL, -- 自动熔断过期时间
    CONSTRAINT chk_lock_duration CHECK (lock_expires_at > locked_at)
);
COMMENT ON TABLE plm_doc.document_lock IS 'M19: 签出排他锁，锁定处于 DRAFT 状态的修订版，防止协同冲突';

-- 4. 文件制品物理元数据表 (Artifact - 核心防篡改不可变表)
CREATE TABLE plm_doc.artifact (
    artifact_id             BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,
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
    CONSTRAINT uq_artifact_tenant_hash UNIQUE (tenant_id, sha256_hash),
    CONSTRAINT chk_artifact_size CHECK (file_size_bytes > 0)
);
COMMENT ON TABLE plm_doc.artifact IS 'M19: 物理文件制品表，哈希永久不可变，禁止原位 UPDATE 与物理 DELETE';
CREATE INDEX idx_artifact_lookup_hash ON plm_doc.artifact(sha256_hash);

-- 5. 数据集逻辑容器表 (Dataset)
CREATE TABLE plm_doc.dataset (
    dataset_id              BIGINT PRIMARY KEY,
    revision_id             BIGINT NOT NULL REFERENCES plm_doc.document_revision(revision_id) ON DELETE CASCADE,
    dataset_code            VARCHAR(64) NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dataset_rev_code UNIQUE (revision_id, dataset_code)
);

-- 6. 数据集与物理制品受控绑定关系表 (DatasetArtifactBinding)
CREATE TABLE plm_doc.dataset_artifact_binding (
    binding_id              BIGINT PRIMARY KEY,
    dataset_id              BIGINT NOT NULL REFERENCES plm_doc.dataset(dataset_id) ON DELETE CASCADE,
    artifact_id             BIGINT NOT NULL REFERENCES plm_doc.artifact(artifact_id) ON DELETE RESTRICT,
    file_role               plm_doc.dataset_role NOT NULL,
    is_current              BOOLEAN NOT NULL DEFAULT TRUE,
    bound_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    bound_by                VARCHAR(64) NOT NULL,
    CONSTRAINT uq_dataset_artifact_role UNIQUE (dataset_id, artifact_id, file_role)
);
CREATE INDEX idx_binding_artifact ON plm_doc.dataset_artifact_binding(artifact_id);

-- 7. 派生制品谱系来源表 (ArtifactDerivation)
CREATE TABLE plm_doc.artifact_derivation (
    derivation_id           BIGINT PRIMARY KEY,
    source_artifact_id      BIGINT NOT NULL REFERENCES plm_doc.artifact(artifact_id) ON DELETE RESTRICT,
    derived_artifact_id     BIGINT NOT NULL REFERENCES plm_doc.artifact(artifact_id) ON DELETE RESTRICT,
    converter_engine        VARCHAR(64) NOT NULL, -- 如 LibreOffice, PDFTron, OCC_Converter
    converter_version       VARCHAR(32) NOT NULL,
    derivation_parameters   JSONB NOT NULL DEFAULT '{}'::jsonb, -- 水印文字、精度配置
    converted_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_derivation_not_self CHECK (source_artifact_id != derived_artifact_id)
);
COMMENT ON TABLE plm_doc.artifact_derivation IS 'M19: 派生来源追踪表，明确 PDF/轻量化与原生源文件的谱系关系';
CREATE INDEX idx_derivation_source ON plm_doc.artifact_derivation(source_artifact_id);

-- 8. 在线协同批注图层表 (DocumentAnnotation - 非破坏性批注)
CREATE TABLE plm_doc.document_annotation (
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
CREATE INDEX idx_annot_revision_page ON plm_doc.document_annotation(revision_id, page_number);

-- 9. 文件物理访问与下载审计表 (FileAccessAudit)
CREATE TABLE plm_doc.file_access_audit (
    audit_id                BIGINT PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,
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
CREATE INDEX idx_access_audit_user ON plm_doc.file_access_audit(user_id, accessed_at DESC);
CREATE INDEX idx_access_audit_artifact ON plm_doc.file_access_audit(artifact_id, accessed_at DESC);
```

### 5. 核心完整性触发器规范 (Functions & Triggers)

#### 5.1 制品哈希只读防篡改触发器 (`fn_enforce_artifact_immutability`)

落实开发说明书核心约束：物理文件一旦持久化写入，严禁在相同 `artifact_id` 下原位替换底层字节。  

SQL

```
CREATE OR REPLACE FUNCTION plm_doc.fn_enforce_artifact_immutability()
RETURNS TRIGGER AS $$
BEGIN
    -- 严格阻断任何对核心存储标识与哈希内容的 UPDATE
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

CREATE TRIGGER trg_enforce_artifact_immutable
BEFORE UPDATE ON plm_doc.artifact
FOR EACH ROW
EXECUTE FUNCTION plm_doc.fn_enforce_artifact_immutability();
```

#### 5.2 签出锁（Check-Out Lock）并发保护触发器

已签出的图文档在未签入前，严禁除锁持有者之外的任何其他用户上传新文件或绑定新物理制品。

SQL

```
CREATE OR REPLACE FUNCTION plm_doc.fn_check_checkout_lock()
RETURNS TRIGGER AS $$
DECLARE
    v_locked_user VARCHAR(64);
    v_expires_at TIMESTAMPTZ;
BEGIN
    -- 查询当前文档修订版是否被排他锁定
    SELECT dl.locked_by_user_id, dl.lock_expires_at 
    INTO v_locked_user, v_expires_at
    FROM plm_doc.dataset d
    JOIN plm_doc.document_lock dl ON d.revision_id = dl.revision_id
    WHERE d.dataset_id = NEW.dataset_id;

    IF FOUND THEN
        -- 若锁未过期且操作用户不是锁持有者
        IF v_expires_at > CURRENT_TIMESTAMP AND v_locked_user != NEW.bound_by THEN
            RAISE EXCEPTION 'Concurrency Lock Violation: Document Revision is exclusively checked out by [%] until [%]. User [%] cannot modify datasets.',
                v_locked_user, v_expires_at, NEW.bound_by USING ERRCODE = '55P03';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_dataset_lock
BEFORE INSERT OR UPDATE ON plm_doc.dataset_artifact_binding
FOR EACH ROW
EXECUTE FUNCTION plm_doc.fn_check_checkout_lock();
```

### 6. 功能特性详细技术规格 (M19-F01 ~ M19-F05)

#### M19-F01：工程文档分类、模板套用、修订与签入/签出锁控

1. **分类与编码规则（Classification & Numbering）**：
   - 支持按机械图样（MECH）、电气接线图（ELEC）、出厂规格书（SPEC）、验证试验报告（TEST）、用户手册（MANUAL）进行层级化业务分类。  
   - 必须强制关联通用 M20 编码生成服务，生成不可变业务编号（如 `DOC-VMC1000-00892`）。  
2. **模板套用机制（Template Application）**：
   - 允许将特定 `DocumentRevision` 标记为模板（`is_template = TRUE`）；  
   - 新建文档时，系统自动克隆模板工程文件的元数据属性及占位主要文件制品（如预置标准图框的 DWG 或包含受控目录的 DOCX）。  
3. **签入/签出悲观锁控协议（Check-in / Check-out Protocol）**：
   - **签出（Check-Out）**：
     - 前置条件：文档修订版必须处于 `DRAFT` 状态，且未被他人锁定。  
     - 业务行为：在 `plm_doc.document_lock` 插入排他锁记录，默认超时熔断窗口为 8 小时（最大支持 72 小时）；系统授予签出用户专属临时上传授权凭单（Upload Ticket）。  
   - **签入（Check-In）**：
     - 校验签入主体与锁持有者一致性；
     - 将新上传的文件制品（`Artifact`）绑定至数据集并标记 `is_current = TRUE`，原制品自动降级为历史历史记录（`is_current = FALSE`）；  
     - 释放并物理删除 `plm_doc.document_lock` 中的排他锁。  
   - **撤销签出（Cancel Check-Out）**：
     - 仅锁持有者或具备 `DocAdmin` 权限的管理人员可执行撤销，撤销后丢弃未签入的暂存制品，恢复原装配锁定状态。  

#### M19-F02：文件分片上传、SHA-256 摘要强校验与对象元数据登记

针对大型 CAD 装配图纸与高精度仿真结果（$\ge 1\text{ GB}$），系统全面采用 MinIO 分片并发上传与双重校验管道。  

代码段

```
sequenceDiagram
    autonumber
    actor User as Web前端 / CAD插件
    participant API as M19 文档服务 API
    participant S3 as MinIO 对象存储
    participant DB as PostgreSQL (plm_doc)

    User->>API: 1. 请求初始化分片: initMultipart(fileName, size, clientSHA256)
    API->>API: 2. 校验文件扩展名白名单与项目存储配额
    API->>S3: 3. CreateMultipartUpload(Bucket, ObjectPath)
    S3-->>API: 返回 uploadId
    API->>API: 4. 签发分片预签名 Presigned URLs (Part 1..N)
    API-->>User: 返回 uploadId 与预签名分片 URL 列表

    loop 分片并发上传
        User->>S3: 5. PUT 分片二进制流至指定 Presigned URL
        S3-->>User: 返回分片 ETag
    end

    User->>API: 6. 请求合并与完成: completeMultipart(uploadId, partsMap, clientSHA256)
    API->>S3: 7. CompleteMultipartUpload(uploadId, partsMap)
    S3-->>API: 返回合成后的 S3 ETag

    critical SHA-256 强校验与哈希防篡改
        API->>S3: 8. 读取合并后字节流，服务端独立计算全局 SHA-256
        Note over API: 计算所得 ServerSHA256 必须与客户端声明 clientSHA256 逐位一致
        alt 哈希不匹配 (网络丢包或恶意篡改)
            API->>S3: 9a. Abort / DeleteObject(ObjectPath)
            API-->>User: 拦截抛出 HTTP 422 (ERR_CHECKSUM_VERIFICATION_FAILED)
        else 强校验 100% 一致
            API->>DB: 9b. 本地事务持久化: INSERT INTO plm_doc.artifact(...)
            API-->>User: 返回 201 Created (生成不可变 artifactId)
        end
    end
```

#### M19-F03：异步生成 PDF 派生物、添加受控电子水印及在线协同批注

1. **异步格式派生流水线（Async PDF Derivative Worker）**：

   - 当原生设计文档签入成功或进入 `IN_REVIEW` 流程时，系统向 Kafka 派发异步转换任务 `DocDerivativeConvertTask`；  
   - 后端无状态 Worker 容器拉取原生制品，调用转换组件（如 LibreOffice / PDFTron）生成标准 PDF/A 归档格式；  
   - 转换成功后，将派生 PDF 注册为**全新独立 `Artifact`**，并向 `plm_doc.artifact_derivation` 写入来源谱系记录（AT-01）。  

2. **受控电子水印安全注入（Dynamic Security Watermarking）**：

   - 用户在线预览或下载 PDF 时，系统拦截切面根据当前上下文**动态叠加不可擦除的受控图层**：

     $$\text{WatermarkText} = \langle \text{TenantName}, \text{UserName}, \text{Department}, \text{CurrentTimestamp}, \text{DocLifecycleState}, \text{SecurityLevel} \rangle$$

   - 针对 `CONFIDENTIAL` 及以上密级，在底图以 $45^\circ$ 倾角注入微细半透明平铺水印及隐式数字盲水印（用于泄密溯源追踪）。  

3. **在线协同非破坏性批注（Non-destructive Annotations）**：

   - 评审人员在 Web 工作台直接对受控 PDF 执行标红圈阅、测量尺寸或添加文字评审意见；  
   - 批注信息作为矢量拓扑坐标保存在 `plm_doc.document_annotation` 独立数据库表中，**严禁回写或破坏底层的 PDF 物理制品字节**。  

#### M19-F04：提供向零件、模型、项目及证据的安全引用绑定

M19 作为 CCDDesigner 2.0 的通用非结构化数据底座，通过统一的引用接口为全域业务实体提供制品支撑：  

- **向产品零件（M16 EBOM）绑定**：零件修订版（`PartRevision`）通过 `cad_document_id` 关联对应的 3D 装配图或 2D 工程图纸；在装配层级变更时，通过 `BOMLine` 携带精确图号版本。  
- **向系统模型（M06 发布）绑定**：SysML v2 发布模型通过 `PublicationManifest` 关联 MinIO 中的原始 SysML 文本、图形布局及专有元数据制品包，保证发布快照完整性（AT-01, AT-04）。  
- **向研发项目（M02 交付物）绑定**：WBS 任务交付物规约（`DeliverableRequirement`）由责任人提交具体 `DocumentRevision`，冻结交付历史，支撑阶段门成熟度核查（AT-15）。  
- **向验证闭环（M11 证据）绑定**：测试报告、现场激光干涉仪检验曲线（`EvidenceRecord`）直接挂接不可变 `Artifact`，其 SHA-256 摘要作为工程判定法律凭证，杜绝事后篡改。  

#### M19-F05：受控打包导出与文件访问日志审计

1. **工程交付包组装（Export Package Assembly）**：
   - 支持按特定产品基线（M21）或制造下发包（M26）打包全套图纸与技术文件；  
   - 打包服务在后台流式构建加密 ZIP 容器，容器根目录强制生成 `manifest.json` 与 `checksums.sha256` 校验清单，列明包内每一个文件的相对路径、对应 `artifactId`、文档业务版本及出库防伪哈希。  
2. **权限联动与即时撤销阻断（AT-13 防御）**：
   - MinIO 原生 API 端口对外物理隐藏，所有下载均通过 M19 后端换取**超短生命周期（$\le 300\text{ s}$）的临时预签名 URL**；  
   - 当工程师的项目权限被管理员收回或降权时，其正在调用的下载接口及已持有的旧预签名链接全量失效，即时返回 `HTTP 403 Forbidden`（AT-13）。  
3. **不可篡改物理访问审计（Audit Trail）**：
   - 针对任何一次预览、下载、导出或分片上传动作，系统异步记录操作人 ID、IP、凭据单号及传输字节数至 `plm_doc.file_access_audit`，日志数据独立入库，严禁系统管理员删除。  

### 7. MinIO 存储拓扑编排与对象路径规划

为防范跨租户数据渗透与单一目录对象堆积造成的 I/O 性能下降，MinIO 存储桶与对象路径严格遵循以下拓扑规约：  

#### 7.1 存储桶划分策略 (Bucket Strategy)

- `ccddesigner-raw-vault`：存放未发布的原生图纸、仿真输入及设计工程工作草稿文件。  
- `ccddesigner-released-vault`：仅存放已完成工程发布（`RELEASED`）、已冻结基线（`FROZEN`）的不可变制品，对底座施加 Object Lock 合规保留策略（WORM 模式）。  
- `ccddesigner-derivative-vault`：存放系统派生生成的 PDF、Web 3D 网格以及切片缩略图。  

#### 7.2 对象全局存储路径规划 (Storage Path Convention)

对象在 MinIO 中的存储 Key 必须严格采用哈希二级散列目录，杜绝明文文件名冲突：  

Plaintext

```
/{tenantId}/{year}/{sha256[0:2]}/{sha256[2:4]}/{sha256}.{fileExtension}
```

- **路径实例**：

  `/VMC_ENTERPRISE/2026/8f/c3/8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01.dwg`

- **优势**：

  1. 保证单目录内文件节点分布均匀，消除对象文件系统元数据瓶颈；  
  2. 即使不同用户上传相同名称的文件，依据 SHA-256 散列在底层绝不发生文件覆盖；  
  3. 彻底杜绝通过遍历存储路径猜测技术文件内容的越权漏洞。  

### 8. OpenAPI 3.0 接口契约定义

#### 8.1 初始化大文件分片上传

- **HTTP 请求**：`POST /api/v1/artifacts/multipart/init`

    

- **请求头**：`Idempotency-Key: 9a8c1-4b10-82d1-upload-init`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "fileName": "VMC1000_Spindle_Assembly.sldasm",
  "fileSizeBytes": 1284901824,
  "clientSha256": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01",
  "chunkCount": 65,
  "mimeType": "application/x-solidworks-assembly",
  "targetRevisionId": 70918209182019
}
```

- **响应报文 (Response 200 OK)**：

JSON

```
{
  "uploadId": "upload_session_8892182741029",
  "storageBucket": "ccddesigner-raw-vault",
  "objectPath": "VMC_ENTERPRISE/2026/8f/c3/8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01.sldasm",
  "partSize": 20971520,
  "presignedPartUrls": [
    {
      "partNumber": 1,
      "uploadUrl": "https://storage.ccddesigner.internal/ccddesigner-raw-vault/...&partNumber=1&uploadId=..."
    },
    {
      "partNumber": 2,
      "uploadUrl": "https://storage.ccddesigner.internal/ccddesigner-raw-vault/...&partNumber=2&uploadId=..."
    }
  ],
  "expiresAt": "2026-09-15T18:00:00Z"
}
```

#### 8.2 完成分片合并并强校验登记

- **HTTP 请求**：`POST /api/v1/artifacts/multipart/complete`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "uploadId": "upload_session_8892182741029",
  "expectedSha256": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01",
  "parts": [
    { "partNumber": 1, "eTag": "\"7182901a18290182b\"" },
    { "partNumber": 2, "eTag": "\"8829102c91820192d\"" }
  ]
}
```

- **响应报文 (Response 201 Created)**：

JSON

```
{
  "artifactId": 901829018209182,
  "fileName": "VMC1000_Spindle_Assembly.sldasm",
  "sha256Hash": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01",
  "fileSizeBytes": 1284901824,
  "status": "REGISTERED",
  "verified": true,
  "registeredAt": "2026-09-15T10:45:12.891Z"
}
```

#### 8.3 文档签出加锁请求

- **HTTP 请求**：`POST /api/v1/documents/{masterId}/revisions/{revisionId}/checkout`

    

- **请求载荷 (Request Body)**：

JSON

```
{
  "lockDurationHours": 8,
  "comments": "根据机床主轴散热改造需求，更新前端轴承配合公差图样。"
}
```

- **响应报文 (Response 200 OK)**：

JSON

```
{
  "revisionId": 70918209182019,
  "isLocked": true,
  "lockedBy": "ENG-1042",
  "lockedAt": "2026-09-15T11:00:00Z",
  "expiresAt": "2026-09-15T19:00:00Z",
  "ticket": "lock_tkt_7192841029"
}
```

### 9. 领域事件与发件箱架构契约 (Outbox Schema)

M19 事务提交时，严格在本地事务中向 `plm_infra.sys_outbox_event` 写入领域事件，驱动异步渲染与跨模块状态同步。  

#### 事件一：`ArtifactRegisteredEvent`

- **触发时机**：文件物理分片合并完成且 SHA-256 强校验通过后登记入库。  
- **Payload 契约**：

JSON

```
{
  "eventId": 9812739102831,
  "eventType": "ArtifactRegistered",
  "aggregateType": "Artifact",
  "aggregateId": "901829018209182",
  "tenantId": "VMC_ENTERPRISE",
  "payload": {
    "artifactId": 901829018209182,
    "fileName": "VMC1000_Spindle_Assembly.sldasm",
    "fileExtension": "sldasm",
    "sha256Hash": "8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01",
    "fileSizeBytes": 1284901824,
    "storageBucket": "ccddesigner-raw-vault",
    "storagePath": "VMC_ENTERPRISE/2026/8f/c3/8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01.sldasm",
    "uploaderId": "ENG-1042"
  }
}
```

#### 事件二：`DerivativeGeneratedEvent`

- **触发时机**：派生渲染容器生成受控 PDF 或 3D 轻量化文件完成。  
- **Payload 契约**：

JSON

```
{
  "eventId": 9812739102832,
  "eventType": "DerivativeGenerated",
  "aggregateType": "Artifact",
  "aggregateId": "901829018209199",
  "tenantId": "VMC_ENTERPRISE",
  "payload": {
    "derivedArtifactId": 901829018209199,
    "sourceArtifactId": 901829018209182,
    "derivativeRole": "DERIVATIVE_PDF",
    "sha256Hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "converterEngine": "PDFTron_CAD_Converter",
    "converterVersion": "v10.4.1"
  }
}
```

### 10. 验收测试矩阵 (Acceptance Testing Matrix)

| **用例编号**  | **上位验收对照  MD+ 1** | **场景与测试步骤**                                           | **预期判定结果 (Pass Criteria)**                             | **验证日志与断言依据  MD+ 1**                                |
| ------------- | ----------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **TC-M19-01** | **CST-M19-01**          | 尝试直接在数据库对 `plm_doc.artifact` 的 `sha256_hash` 或路径执行 `UPDATE` | 数据库内核拦截阻断，事务回滚抛错                             | 抛出 SQL 异常 `23000` (Artifact is strictly IMMUTABLE)       |
| **TC-M19-02** | **AT-04**               | 客户端上传分片，合并时伪造一个与二进制实际字节不符的 SHA-256 摘要 | 服务端合并后重新核算哈希，比对失败，终止入库并自动清理 MinIO 临时对象 | 接口返回 HTTP 422，数据库绝不产生残留的 `Artifact` 记录（AT-04） |
| **TC-M19-03** | M19-F01                 | 用户 A 签出图文档，用户 B 尝试调用签入接口提交新制品或解除锁定 | 系统触发悲观锁保护拦截，阻断用户 B 操作                      | 抛出 `ConcurrencyLockViolation`，HTTP 409 Conflict           |
| **TC-M19-04** | **AT-13**               | 某工程师被项目管理员移除项目权限后，尝试使用 10 秒前生成的下载 URL 获取文件 | API 网关与切面拦截预签名解析，一票否决下载请求               | 接口与下载通道全量返回 HTTP 403 Forbidden（AT-13）           |
| **TC-M19-05** | M19-F03                 | 原生 DWG 格式签入后，系统异步触发派生 PDF 渲染任务           | 派生出新 `Artifact`，其 `source_artifact_id` 精准指向原生件，原件字节完全不变 | 检查 `artifact_derivation` 记录，验证 PDF 带有当前用户和机密级别的受控水印 |
| **TC-M19-06** | **AT-14**               | 在生产系统加载三年前已归档的设计基线并执行交付导出           | 打包器精准拉取当时固化的全部原生件与派生 PDF 制品，生成的 ZIP 附带校验清单 | 清单内所有文件的 SHA-256 与历史 `Artifact` 完全一致，内容零漂移（AT-14） |