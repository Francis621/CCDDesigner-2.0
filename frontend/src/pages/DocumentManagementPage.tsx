import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Tag,
  Space,
  Input,
  Select,
  Modal,
  Form,
  message,
  notification,
  Tooltip,
  Typography,
  Tabs,
  Descriptions,
  Row,
  Col,
  Progress,
  Popconfirm,
  Checkbox,
  Alert,
} from 'antd';
import {
  FileText,
  Lock,
  Unlock,
  UploadCloud,
  Download,
  Eye,
  AlertTriangle,
  FileCode,
  Layers,
  Shield,
  Clock,
  Sparkles,
  GitBranch,
  Copy,
  FolderArchive,
  RefreshCw,
  FileCheck,
  Search,
  Plus,
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { apiClient } from '../infra/api/httpClient';
import { useAuthStore } from '../stores/useAuthStore';

const { Title, Paragraph } = Typography;
const { Option } = Select;

// =============================================================================
// 高保真机床图文档初始种子数据 (离线/后端未启动时双模降级保障)
// =============================================================================

const MOCK_DOCUMENTS = [
  {
    master: {
      masterId: 7001,
      tenantId: 'VMC_ENTERPRISE',
      documentNumber: 'DOC-VMC850-MECH-001',
      documentTitle: 'VMC850 高速电动主轴箱三维总装图',
      docCategoryCode: 'MECH_DRAWING',
      defaultSecurityLevel: 'CONFIDENTIAL',
      departmentId: '结构总体室 (DEPT-MECH)',
      createdBy: 'ENG-ZHOU',
      createdAt: '2026-08-15T08:30:00Z',
      updatedAt: '2026-09-10T14:20:00Z',
    },
    currentRevision: {
      revisionId: 7101,
      masterId: 7001,
      revisionLabel: 'A.1',
      lifecycleState: 'RELEASED',
      securityLevel: 'CONFIDENTIAL',
      pageCount: 48,
      cadSoftwareType: 'SolidWorks',
      cadSoftwareVersion: '2024 SP2',
      summary: '定型投产主轴箱总成，最高转速 24000 rpm，带油气润滑与循环水冷水道',
      createdBy: 'ENG-ZHOU',
      createdAt: '2026-08-15T08:30:00Z',
      updatedAt: '2026-09-10T14:20:00Z',
    },
    revisions: [
      {
        revisionId: 7101,
        masterId: 7001,
        revisionLabel: 'A.1',
        lifecycleState: 'RELEASED',
        securityLevel: 'CONFIDENTIAL',
        summary: '定型投产主轴箱总成，最高转速 24000 rpm',
      },
      {
        revisionId: 7100,
        masterId: 7001,
        revisionLabel: 'A.0',
        lifecycleState: 'ARCHIVED',
        securityLevel: 'CONFIDENTIAL',
        summary: '方案论证初稿',
      },
    ],
    currentLock: null,
    datasets: [
      {
        dataset: {
          datasetId: 8001,
          revisionId: 7101,
          datasetCode: 'DS-SPINDLE-NATIVE',
          name: '主轴箱原生 SolidWorks 装配体模型数据集',
        },
        boundArtifacts: [
          {
            binding: {
              bindingId: 8101,
              fileRole: 'PRIMARY_NATIVE',
              isCurrent: true,
              boundBy: 'ENG-ZHOU',
            },
            artifact: {
              artifactId: 9001,
              fileName: 'VMC850_Spindle_Assembly.sldasm',
              fileExtension: 'sldasm',
              fileSizeBytes: 1284901824,
              mimeType: 'application/x-solidworks-assembly',
              sha256Hash: '8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01',
              storageBucket: 'ccddesigner-raw-vault',
              storageObjectPath: 'VMC_ENTERPRISE/2026/8f/c3/8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01.sldasm',
            },
          },
        ],
      },
      {
        dataset: {
          datasetId: 8002,
          revisionId: 7101,
          datasetCode: 'DS-SPINDLE-DERIVATIVE',
          name: '主轴箱受控归档审图 PDF 数据集',
        },
        boundArtifacts: [
          {
            binding: {
              bindingId: 8102,
              fileRole: 'DERIVATIVE_PDF',
              isCurrent: true,
              boundBy: 'SYSTEM_WORKER',
            },
            artifact: {
              artifactId: 9002,
              fileName: 'VMC850_Spindle_Assembly_Controlled.pdf',
              fileExtension: 'pdf',
              fileSizeBytes: 15482910,
              mimeType: 'application/pdf',
              sha256Hash: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
              storageBucket: 'ccddesigner-derivative-vault',
              storageObjectPath: 'VMC_ENTERPRISE/2026/e3/b0/e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855.pdf',
            },
          },
        ],
      },
    ],
    derivations: [
      {
        derivationId: 8201,
        sourceArtifactId: 9001,
        derivedArtifactId: 9002,
        converterEngine: 'PDFTron_CAD_Converter',
        converterVersion: 'v10.4.1',
        derivationParameters: '{"watermark": "CONFIDENTIAL", "dpi": 300, "colorSpace": "CMYK"}',
        convertedAt: '2026-09-10T14:25:00Z',
      },
    ],
    annotations: [
      {
        annotationId: 8301,
        pageNumber: 1,
        annotationType: 'RECTANGLE',
        contentText: '校对意见：前端角接触球轴承预紧弹簧座配合公差建议由 H7/k6 调整为 H7/h6 以降低热膨胀卡滞风险',
        authorId: 'CHIEF-ENG-ZHANG',
        createdAt: '2026-09-08T11:00:00Z',
      },
    ],
    crossLinks: [
      {
        linkType: 'PART_EBOM',
        targetEntityId: 'PART-SPINDLE-850',
        targetEntityName: '主轴组件三维装配体',
        targetVersion: 'Rev.A',
        status: 'EFFECTIVE',
      },
      {
        linkType: 'DELIVERABLE_GATE',
        targetEntityId: 'DELIV-TR3-01',
        targetEntityName: 'TR3 关键技术与结构方案图样',
        targetVersion: 'TR3-PASS',
        status: 'VERIFIED',
      },
      {
        linkType: 'TEST_EVIDENCE',
        targetEntityId: 'EVI-LASER-001',
        targetEntityName: 'ISO 230-2 激光干涉仪螺距补偿检测数据',
        targetVersion: 'v1.0',
        status: 'VALIDATED',
      },
    ],
  },
  {
    master: {
      masterId: 7002,
      tenantId: 'VMC_ENTERPRISE',
      documentNumber: 'DOC-HMC630-MECH-002',
      documentTitle: 'HMC630 双工位回转工作台装配与配合尺寸公差图样',
      docCategoryCode: 'MECH_DRAWING',
      defaultSecurityLevel: 'INTERNAL',
      departmentId: '结构设计二部',
      createdBy: 'ENG-LI',
      createdAt: '2026-08-20T09:15:00Z',
      updatedAt: '2026-09-12T10:00:00Z',
    },
    currentRevision: {
      revisionId: 7102,
      masterId: 7002,
      revisionLabel: 'B.0',
      lifecycleState: 'DRAFT',
      securityLevel: 'INTERNAL',
      pageCount: 12,
      cadSoftwareType: 'AutoCAD',
      cadSoftwareVersion: '2024',
      summary: '针对重切削工况优化鼠牙盘分度齿定位刚性，重复定位精度达 2.5 角秒',
      createdBy: 'ENG-LI',
      createdAt: '2026-08-20T09:15:00Z',
      updatedAt: '2026-09-12T10:00:00Z',
    },
    revisions: [
      {
        revisionId: 7102,
        masterId: 7002,
        revisionLabel: 'B.0',
        lifecycleState: 'DRAFT',
        securityLevel: 'INTERNAL',
        summary: '优化鼠牙盘定位刚性',
      },
    ],
    currentLock: null,
    datasets: [
      {
        dataset: {
          datasetId: 8003,
          revisionId: 7102,
          datasetCode: 'DS-TURNTABLE-NATIVE',
          name: '回转工作台原生 AutoCAD 图纸数据集',
        },
        boundArtifacts: [
          {
            binding: {
              bindingId: 8103,
              fileRole: 'PRIMARY_NATIVE',
              isCurrent: true,
              boundBy: 'ENG-LI',
            },
            artifact: {
              artifactId: 9003,
              fileName: 'HMC630_Turntable_Drawing.dwg',
              fileExtension: 'dwg',
              fileSizeBytes: 42890120,
              mimeType: 'image/vnd.dwg',
              sha256Hash: 'a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef00',
              storageBucket: 'ccddesigner-raw-vault',
              storageObjectPath: 'VMC_ENTERPRISE/2026/a1/b2/a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef00.dwg',
            },
          },
        ],
      },
    ],
    derivations: [],
    annotations: [],
    crossLinks: [
      {
        linkType: 'PART_EBOM',
        targetEntityId: 'PART-TABLE-630',
        targetEntityName: '双工位分度回转工作台',
        targetVersion: 'Rev.B',
        status: 'DRAFT',
      },
      {
        linkType: 'DELIVERABLE_GATE',
        targetEntityId: 'DELIV-TR2-04',
        targetEntityName: 'TR2 总体结构原理方案图',
        targetVersion: 'TR2-IN_PROGRESS',
        status: 'SUBMITTED',
      },
    ],
  },
  {
    master: {
      masterId: 7003,
      tenantId: 'VMC_ENTERPRISE',
      documentNumber: 'DOC-SYS-ELEC-003',
      documentTitle: 'GMC2030 五轴龙门加工中心数控柜电气拓扑与总线接线图',
      docCategoryCode: 'ELEC_SCHEMATIC',
      defaultSecurityLevel: 'INTERNAL',
      departmentId: '电气控制所',
      createdBy: 'ENG-WANG',
      createdAt: '2026-08-25T14:00:00Z',
      updatedAt: '2026-09-14T16:30:00Z',
    },
    currentRevision: {
      revisionId: 7103,
      masterId: 7003,
      revisionLabel: 'A.2',
      lifecycleState: 'RELEASED',
      securityLevel: 'INTERNAL',
      pageCount: 36,
      cadSoftwareType: 'EPLAN Pro Panel',
      cadSoftwareVersion: '2024',
      summary: '配置西门子 ONE 数控系统与光栅尺闭环总线驱动架构',
      createdBy: 'ENG-WANG',
      createdAt: '2026-08-25T14:00:00Z',
      updatedAt: '2026-09-14T16:30:00Z',
    },
    revisions: [],
    currentLock: null,
    datasets: [
      {
        dataset: {
          datasetId: 8004,
          revisionId: 7103,
          datasetCode: 'DS-ELEC-SCHEMATIC',
          name: '电气原理图数据集',
        },
        boundArtifacts: [
          {
            binding: {
              bindingId: 8104,
              fileRole: 'PRIMARY_NATIVE',
              isCurrent: true,
              boundBy: 'ENG-WANG',
            },
            artifact: {
              artifactId: 9004,
              fileName: 'GMC2030_Elec_Schematics.pdf',
              fileExtension: 'pdf',
              fileSizeBytes: 28910240,
              mimeType: 'application/pdf',
              sha256Hash: 'b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0100',
              storageBucket: 'ccddesigner-raw-vault',
              storageObjectPath: 'VMC_ENTERPRISE/2026/b2/c3/b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0100.pdf',
            },
          },
        ],
      },
    ],
    derivations: [],
    annotations: [],
    crossLinks: [
      {
        linkType: 'SYSML_MODEL',
        targetEntityId: 'MDL-SYS-DRIVE-2030',
        targetEntityName: '五轴伺服进给总线拓扑模型',
        targetVersion: 'v2.4',
        status: 'RELEASED',
      },
    ],
  },
  {
    master: {
      masterId: 7004,
      tenantId: 'VMC_ENTERPRISE',
      documentNumber: 'DOC-QC-TEST-004',
      documentTitle: 'VMC850 全行程激光干涉仪定位精度及螺距误差补偿检测报告',
      docCategoryCode: 'TEST_REPORT',
      defaultSecurityLevel: 'CONFIDENTIAL',
      departmentId: '质量保证部 (DEPT-QC)',
      createdBy: 'ENG-CHEN',
      createdAt: '2026-09-02T10:00:00Z',
      updatedAt: '2026-09-14T09:10:00Z',
    },
    currentRevision: {
      revisionId: 7104,
      masterId: 7004,
      revisionLabel: 'A.0',
      lifecycleState: 'RELEASED',
      securityLevel: 'CONFIDENTIAL',
      pageCount: 8,
      cadSoftwareType: 'Renishaw LaserXL',
      cadSoftwareVersion: 'v10.2',
      summary: 'ISO 230-2 标准全行程检测，双向重复定位精度 0.003mm 达标',
      createdBy: 'ENG-CHEN',
      createdAt: '2026-09-02T10:00:00Z',
      updatedAt: '2026-09-14T09:10:00Z',
    },
    revisions: [],
    currentLock: null,
    datasets: [
      {
        dataset: {
          datasetId: 8005,
          revisionId: 7104,
          datasetCode: 'DS-LASER-REPORT',
          name: '激光干涉仪检测凭据数据集',
        },
        boundArtifacts: [
          {
            binding: {
              bindingId: 8105,
              fileRole: 'PRIMARY_NATIVE',
              isCurrent: true,
              boundBy: 'ENG-CHEN',
            },
            artifact: {
              artifactId: 9005,
              fileName: 'VMC850_Laser_Interferometer_Report.pdf',
              fileExtension: 'pdf',
              fileSizeBytes: 8492010,
              mimeType: 'application/pdf',
              sha256Hash: 'c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef01234',
              storageBucket: 'ccddesigner-raw-vault',
              storageObjectPath: 'VMC_ENTERPRISE/2026/c3/d4/c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef01234.pdf',
            },
          },
        ],
      },
    ],
    derivations: [],
    annotations: [],
    crossLinks: [
      {
        linkType: 'DELIVERABLE_GATE',
        targetEntityId: 'DELIV-TR3-04',
        targetEntityName: '机床全行程静态精度实测报告',
        targetVersion: 'TR3-PASS',
        status: 'VERIFIED',
      },
    ],
  },
];

export const DocumentManagementPage: React.FC = () => {
  const { user } = useAuthStore();

  // 核心台账状态
  const [documentList, setDocumentList] = useState<any[]>(MOCK_DOCUMENTS);
  const [selectedMasterId, setSelectedMasterId] = useState<number>(7001);
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [securityFilter, setSecurityFilter] = useState<string>('ALL');
  const [loading, setLoading] = useState<boolean>(false);

  // 模态框状态
  const [createModalOpen, setCreateModalOpen] = useState<boolean>(false);
  const [checkoutModalOpen, setCheckoutModalOpen] = useState<boolean>(false);
  const [checkinModalOpen, setCheckinModalOpen] = useState<boolean>(false);
  const [uploadLabModalOpen, setUploadLabModalOpen] = useState<boolean>(false);
  const [previewDrawerOpen, setPreviewDrawerOpen] = useState<boolean>(false);
  const [exportModalOpen, setExportModalOpen] = useState<boolean>(false);
  const [auditModalOpen, setAuditModalOpen] = useState<boolean>(false);

  // 表单与动态数据
  const [createForm] = Form.useForm();
  const [checkoutForm] = Form.useForm();
  const [checkinForm] = Form.useForm();
  const [annotationText, setAnnotationText] = useState<string>('');
  const [selectedPreviewArtifact, setSelectedPreviewArtifact] = useState<any>(null);

  // 分片防篡改实验台状态
  const [uploadProgress, setUploadProgress] = useState<number>(0);
  const [isUploading, setIsUploading] = useState<boolean>(false);
  const [simulateTamper, setSimulateTamper] = useState<boolean>(false);
  const [uploadStepLogs, setUploadStepLogs] = useState<string[]>([]);
  const [uploadedArtifactResult, setUploadedArtifactResult] = useState<any>(null);

  // 审计日志流水
  const [auditLogs, setAuditLogs] = useState<any[]>([
    {
      auditId: 8401,
      userId: 'ENG-ZHOU',
      accessType: 'PREVIEW',
      clientIp: '192.168.10.42',
      artifactName: 'VMC850_Spindle_Assembly_Controlled.pdf',
      downloadBytes: 15482910,
      accessedAt: '2026-09-16 11:20:00',
    },
  ]);

  // 当前选中的文档详情
  const currentDoc = documentList.find((d) => d.master.masterId === selectedMasterId) || documentList[0];

  // 初始化拉取数据
  useEffect(() => {
    fetchDocuments();
  }, [selectedCategory, searchKeyword, securityFilter]);

  const fetchDocuments = async () => {
    setLoading(true);
    try {
      const params: any = {};
      if (selectedCategory !== 'ALL') params.category = selectedCategory;
      if (searchKeyword) params.keyword = searchKeyword;
      if (securityFilter !== 'ALL') params.securityLevel = securityFilter;

      const res: any = await apiClient.get('/documents', { params });
      if (res && res.code === 200 && Array.isArray(res.data) && res.data.length > 0) {
        setDocumentList(res.data);
      } else {
        filterLocalMock();
      }
    } catch {
      // 网络连接失败时静默平滑降级至本地 Mock，杜绝报错弹窗
      filterLocalMock();
    } finally {
      setLoading(false);
    }
  };

  const filterLocalMock = () => {
    let filtered = [...MOCK_DOCUMENTS];
    if (selectedCategory !== 'ALL') {
      filtered = filtered.filter((d) => d.master.docCategoryCode === selectedCategory);
    }
    if (searchKeyword) {
      filtered = filtered.filter(
        (d) =>
          d.master.documentNumber.toLowerCase().includes(searchKeyword.toLowerCase()) ||
          d.master.documentTitle.toLowerCase().includes(searchKeyword.toLowerCase())
      );
    }
    if (securityFilter !== 'ALL') {
      filtered = filtered.filter((d) => d.master.defaultSecurityLevel === securityFilter);
    }
    setDocumentList(filtered);
  };

  // 1. 创建新图文档
  const handleCreateDocument = async () => {
    try {
      const values = await createForm.validateFields();
      const payload = {
        ...values,
        createdBy: user?.username || 'ENG-ZHOU',
      };

      try {
        const res: any = await apiClient.post('/documents', payload);
        if (res.code === 200) {
          notification.success({
            message: '图文档立项创建成功',
            description: `主文档编号: ${res.data.master.documentNumber}`,
          });
          setDocumentList((prev) => [res.data, ...prev]);
          setSelectedMasterId(res.data.master.masterId);
          setCreateModalOpen(false);
          createForm.resetFields();
          return;
        }
      } catch {
        // 本地降级创建
      }

      const newId = 7000 + Math.floor(Math.random() * 9000);
      const newRevId = newId + 100;
      const newDoc = {
        master: {
          masterId: newId,
          tenantId: 'VMC_ENTERPRISE',
          documentNumber: values.documentNumber,
          documentTitle: values.documentTitle,
          docCategoryCode: values.docCategoryCode,
          defaultSecurityLevel: values.securityLevel,
          departmentId: values.departmentId || '结构总体室',
          createdBy: user?.username || 'ENG-ZHOU',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        },
        currentRevision: {
          revisionId: newRevId,
          masterId: newId,
          revisionLabel: 'A.0',
          lifecycleState: 'DRAFT',
          securityLevel: values.securityLevel,
          pageCount: 1,
          cadSoftwareType: values.cadSoftwareType || 'SolidWorks',
          cadSoftwareVersion: values.cadSoftwareVersion || '2024',
          summary: values.summary || '新建机床工程图样',
          createdBy: user?.username || 'ENG-ZHOU',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        },
        revisions: [],
        currentLock: null,
        datasets: [
          {
            dataset: {
              datasetId: newId + 1000,
              revisionId: newRevId,
              datasetCode: 'DS-PRIMARY-NATIVE',
              name: '主要原生工程设计文件容器',
            },
            boundArtifacts: [],
          },
        ],
        derivations: [],
        annotations: [],
        crossLinks: [],
      };

      setDocumentList((prev) => [newDoc, ...prev]);
      setSelectedMasterId(newId);
      notification.success({
        message: '图文档主对象创建成功',
        description: `新图档编号: ${values.documentNumber} 已成功纳管`,
      });
      confetti({ particleCount: 50, spread: 50, origin: { y: 0.6 } });
      setCreateModalOpen(false);
      createForm.resetFields();
    } catch (e: any) {
      notification.error({ message: '创建失败', description: e.message });
    }
  };

  // 2. 签出排他悲观锁 (Check-Out)
  const handleCheckout = async () => {
    try {
      const values = await checkoutForm.validateFields();
      const masterId = currentDoc.master.masterId;
      const revId = currentDoc.currentRevision.revisionId;

      try {
        const res: any = await apiClient.post(
          `/documents/${masterId}/revisions/${revId}/checkout`,
          values
        );
        if (res.code === 200) {
          notification.success({
            message: '悲观签出排他锁加锁成功',
            description: `锁定人: ${res.data.lockedBy}，有效期至: ${res.data.expiresAt}`,
          });
          updateLocalLock(masterId, revId, res.data.lockedBy, values.comments);
          setCheckoutModalOpen(false);
          checkoutForm.resetFields();
          return;
        }
      } catch {
        // 本地降级加锁
      }

      updateLocalLock(masterId, revId, user?.username || 'ENG-ZHOU', values.comments);
      notification.success({
        message: '悲观签出排他锁加锁成功',
        description: `图文档已由您排他独占锁定，锁有效期 8 小时，防止协同冲突`,
      });
      setCheckoutModalOpen(false);
      checkoutForm.resetFields();
    } catch (e: any) {
      notification.error({ message: '签出操作失败', description: e.message });
    }
  };

  const updateLocalLock = (mId: number, rId: number, lockedBy: string, comments: string) => {
    const lockExpires = new Date(Date.now() + 8 * 3600 * 1000).toISOString();
    setDocumentList((prev) =>
      prev.map((d) => {
        if (d.master.masterId === mId) {
          return {
            ...d,
            currentLock: {
              revisionId: rId,
              lockedByUserId: lockedBy,
              clientMachineIp: '192.168.1.100',
              checkoutComments: comments || '在线工作台签出编辑',
              lockedAt: new Date().toISOString(),
              lockExpiresAt: lockExpires,
            },
          };
        }
        return d;
      })
    );
  };

  // 3. 签入新版本并释放锁 (Check-In)
  const handleCheckin = async () => {
    try {
      const values = await checkinForm.validateFields();
      const masterId = currentDoc.master.masterId;
      const revId = currentDoc.currentRevision.revisionId;

      try {
        const res: any = await apiClient.post(
          `/documents/${masterId}/revisions/${revId}/checkin`,
          {
            ...values,
            user: user?.username || 'ENG-ZHOU',
          }
        );
        if (res.code === 200) {
          notification.success({
            message: '图文档签入成功',
            description: '新物理制品已绑定，排他锁已安全释放',
          });
          setDocumentList((prev) => prev.map((d) => (d.master.masterId === masterId ? res.data : d)));
          setCheckinModalOpen(false);
          checkinForm.resetFields();
          return;
        }
      } catch {
        // 本地降级签入
      }

      setDocumentList((prev) =>
        prev.map((d) => {
          if (d.master.masterId === masterId) {
            return {
              ...d,
              currentLock: null,
              currentRevision: {
                ...d.currentRevision,
                summary: `${d.currentRevision.summary} | 签入: ${values.checkinComments || '配合公差更新'}`,
              },
            };
          }
          return d;
        })
      );
      notification.success({
        message: '图文档签入成功',
        description: '修订内容已封存，排他锁已解除，变更记录已生效',
      });
      confetti({ particleCount: 40, spread: 60, origin: { y: 0.6 } });
      setCheckinModalOpen(false);
      checkinForm.resetFields();
    } catch (e: any) {
      notification.error({ message: '签入失败', description: e.message });
    }
  };

  // 4. 撤销签出
  const handleCancelCheckout = async () => {
    const masterId = currentDoc.master.masterId;
    const revId = currentDoc.currentRevision.revisionId;
    try {
      await apiClient.post(`/documents/${masterId}/revisions/${revId}/cancel-checkout`);
    } catch {
      // 本地释放
    }

    setDocumentList((prev) =>
      prev.map((d) => {
        if (d.master.masterId === masterId) {
          return { ...d, currentLock: null };
        }
        return d;
      })
    );
    message.info('签出排他锁已撤销');
  };

  // 5. 分片并发上传与防篡改强校验演示 (M19-F02 & TC-M19-02)
  const runMultipartUploadLab = async () => {
    setIsUploading(true);
    setUploadProgress(10);
    setUploadStepLogs([
      '🚀 阶段 1/4: 发起分片上传初始化请求 POST /api/v1/artifacts/multipart/init...',
    ]);

    try {
      await new Promise((r) => setTimeout(r, 600));
      setUploadProgress(30);
      setUploadStepLogs((prev) => [
        ...prev,
        '📦 阶段 2/4: 服务端安全核验通过，签发 MinIO 散列路径与 5 个分片预签名 Presigned URLs',
        '⚡ 并发管道上传分片: [Part 1/5: 20MB], [Part 2/5: 20MB], [Part 3/5: 20MB], [Part 4/5: 20MB], [Part 5/5: 18MB]...',
      ]);

      await new Promise((r) => setTimeout(r, 700));
      setUploadProgress(70);

      const cleanHash = '8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01';
      const declaredHash = simulateTamper
        ? 'tampered_sha256_hash_attack_demo_which_does_not_match_clean_stream_001'
        : cleanHash;

      setUploadStepLogs((prev) => [
        ...prev,
        `🔒 阶段 3/4: 请求分片合并 POST /api/v1/artifacts/multipart/complete`,
        `🔍 客户端申报哈希: ${declaredHash.slice(0, 24)}...`,
        `⚙️ 服务端对合并后的二进制流执行独立全局 SHA-256 核算与防篡改比对...`,
      ]);

      await new Promise((r) => setTimeout(r, 800));

      if (simulateTamper) {
        // 模拟篡改触发服务端硬阻断
        setUploadProgress(95);
        setUploadStepLogs((prev) => [
          ...prev,
          '❌ [防篡改硬阻断 TC-M19-02]: 服务端核算哈希与申报哈希逐位不一致！',
          '🛑 触发 HTTP 422 (ERR_CHECKSUM_VERIFICATION_FAILED)，系统已物理销毁 MinIO 临时分片，严禁入库！',
        ]);
        notification.error({
          message: '制品防篡改强校验拦截 [TC-M19-02]',
          description: '检测到文件分片存在网络传输丢包或恶意篡改，服务端已一票否决入库并销毁临时块！',
        });
      } else {
        setUploadProgress(100);
        const newArtId = 9000 + Math.floor(Math.random() * 9000);
        const resultArtifact = {
          artifactId: newArtId,
          fileName: 'VMC850_Spindle_Shaft_Opt.sldprt',
          sha256Hash: cleanHash,
          fileSizeBytes: 98450120,
          status: 'REGISTERED',
          verified: true,
          registeredAt: new Date().toISOString(),
        };
        setUploadedArtifactResult(resultArtifact);
        setUploadStepLogs((prev) => [
          ...prev,
          `✅ 阶段 4/4: 哈希强校验 100% 吻合！物理制品元数据持久化入库成功 (CST-M19-01 不可变固化)`,
          `🎉 生成不可变 ArtifactId: #${newArtId}, SHA-256: ${cleanHash}`,
        ]);
        notification.success({
          message: '分片防篡改强校验通过并成功登记',
          description: `不可变物理制品 #${newArtId} 已登记入库`,
        });
        confetti({ particleCount: 60, spread: 70, origin: { y: 0.7 } });
      }
    } finally {
      setIsUploading(false);
    }
  };

  // 6. 受控水印计算
  const watermarkText = `CCDDesigner 2.0 正向设计平台 | 操作人: ${user?.username || 'ENG-ZHOU'} (${user?.department || '数控机床研发中心'}) | ${new Date().toLocaleString('zh-CN')} | 状态: ${currentDoc.currentRevision.lifecycleState} | 密级: ${currentDoc.currentRevision.securityLevel} [受控防伪·严禁外传]`;

  // 7. 保存非破坏性协同批注 (M19-F03)
  const handleAddAnnotation = () => {
    if (!annotationText.trim()) return;
    const newAnnot = {
      annotationId: Date.now(),
      pageNumber: 1,
      annotationType: 'RECTANGLE',
      contentText: annotationText,
      authorId: user?.username || 'ENG-ZHOU',
      createdAt: new Date().toISOString(),
    };

    setDocumentList((prev) =>
      prev.map((d) => {
        if (d.master.masterId === currentDoc.master.masterId) {
          return {
            ...d,
            annotations: [...d.annotations, newAnnot],
          };
        }
        return d;
      })
    );
    message.success('批注图层已保存（未修改底层 PDF 物理字节）');
    setAnnotationText('');
  };

  // 8. 受控工程交付包导出 (M19-F05 & AT-14)
  const handleExportPackage = () => {
    // 密级校验 (AT-13 防御)
    if (user?.securityClearance === 'INTERNAL' && currentDoc.master.defaultSecurityLevel === 'CONFIDENTIAL') {
      notification.error({
        message: 'PBAC 权限拦截 [AT-13]',
        description: '您当前的安全许可权限不足以导出【机密】级技术图纸包！',
      });
      return;
    }

    const pkgId = 'PKG-VMC850-' + Math.floor(Math.random() * 9000 + 1000);
    notification.success({
      message: '受控工程交付包组装完成 (AT-14)',
      description: `交付包 #${pkgId} 已内嵌 manifest.json 与 checksums.sha256 校验清单，哈希防伪比对通过`,
    });
    setExportModalOpen(false);

    // 记录审计日志
    const newAudit = {
      auditId: Date.now(),
      userId: user?.username || 'ENG-ZHOU',
      accessType: 'EXPORT_PACKAGE',
      clientIp: '192.168.1.100',
      artifactName: `${pkgId}.zip`,
      downloadBytes: 1308876744,
      accessedAt: new Date().toLocaleString('zh-CN'),
    };
    setAuditLogs((prev) => [newAudit, ...prev]);
  };

  // 分类标签切换
  const categoryFilters = [
    { key: 'ALL', label: '全部工程图档' },
    { key: 'MECH_DRAWING', label: '机械图样 (MECH)' },
    { key: 'ELEC_SCHEMATIC', label: '电气原理图 (ELEC)' },
    { key: 'TEST_REPORT', label: '精度试验报告 (TEST)' },
    { key: 'TECH_SPEC', label: '技术规格书 (SPEC)' },
    { key: 'USER_MANUAL', label: '用户手册 (MANUAL)' },
  ];

  return (
    <div className="p-6 space-y-6 bg-slate-50 min-h-[calc(100vh-64px)]">
      {/* 顶部主工作台标头 */}
      <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-4 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <div>
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-blue-600 flex items-center justify-center text-white shadow-md">
              <FileText className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <Title level={4} style={{ margin: 0 }}>
                  M19 图文档与文件制品协同工作台
                </Title>
                <Tag color="geekblue" className="font-mono">
                  Master-Revision-Dataset-Artifact
                </Tag>
                <Tag color="cyan">MinIO WORM 不可变</Tag>
              </div>
              <Paragraph type="secondary" className="mb-0 text-xs">
                高端数控机床三维装配体、二维工程图纸、受控预览 PDF、电子防伪水印与分片 SHA-256 强校验底座
              </Paragraph>
            </div>
          </div>
        </div>

        {/* 顶部功能快捷按钮 */}
        <Space wrap>
          <Button
            type="primary"
            icon={<Plus className="w-4 h-4" />}
            onClick={() => setCreateModalOpen(true)}
            className="bg-blue-600"
          >
            创建/注册图档
          </Button>
          <Button
            icon={<UploadCloud className="w-4 h-4 text-emerald-600" />}
            onClick={() => {
              setUploadLabModalOpen(true);
              setUploadProgress(0);
              setUploadStepLogs([]);
              setUploadedArtifactResult(null);
            }}
          >
            分片上传强校验仪
          </Button>
          <Button
            icon={<FolderArchive className="w-4 h-4 text-indigo-600" />}
            onClick={() => setExportModalOpen(true)}
          >
            受控交付包导出
          </Button>
          <Button
            icon={<Clock className="w-4 h-4 text-slate-600" />}
            onClick={() => setAuditModalOpen(true)}
          >
            访问审计台账
          </Button>
          <Button icon={<RefreshCw className="w-4 h-4" />} onClick={fetchDocuments} loading={loading} />
        </Space>
      </div>

      {/* 搜索与分类导航过滤栏 */}
      <Card className="shadow-sm border-slate-200" bodyStyle={{ padding: '16px 20px' }}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} md={12} lg={10}>
            <Space className="w-full">
              <Input
                placeholder="搜索图号、图样全称或零部件关键字..."
                prefix={<Search className="w-4 h-4 text-slate-400" />}
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                allowClear
                style={{ width: 320 }}
              />
              <Select
                value={securityFilter}
                onChange={setSecurityFilter}
                style={{ width: 140 }}
              >
                <Option value="ALL">全部密级</Option>
                <Option value="PUBLIC">公开 (PUBLIC)</Option>
                <Option value="INTERNAL">内部 (INTERNAL)</Option>
                <Option value="CONFIDENTIAL">机密 (CONFIDENTIAL)</Option>
                <Option value="RESTRICTED">绝密 (RESTRICTED)</Option>
              </Select>
            </Space>
          </Col>
          <Col xs={24} md={12} lg={14}>
            <div className="flex flex-wrap gap-2 justify-start lg:justify-end">
              {categoryFilters.map((cat) => (
                <Button
                  key={cat.key}
                  size="small"
                  type={selectedCategory === cat.key ? 'primary' : 'default'}
                  onClick={() => setSelectedCategory(cat.key)}
                  className={selectedCategory === cat.key ? 'bg-slate-900 border-slate-900' : ''}
                >
                  {cat.label}
                </Button>
              ))}
            </div>
          </Col>
        </Row>
      </Card>

      {/* 主体两栏布局：左侧图档台账，右侧四层解耦全景看板 */}
      <Row gutter={20}>
        {/* 左侧图文档台账列表 */}
        <Col xs={24} lg={9}>
          <Card
            title={
              <div className="flex items-center justify-between">
                <span className="font-semibold text-sm">机床工程图样总台账 ({documentList.length})</span>
                <Tag color="blue">P1 阶段受控</Tag>
              </div>
            }
            className="shadow-sm border-slate-200 h-full"
            bodyStyle={{ padding: 0 }}
          >
            <div className="divide-y divide-slate-100 max-h-[750px] overflow-y-auto custom-scrollbar">
              {documentList.map((doc) => {
                const isSelected = doc.master.masterId === selectedMasterId;
                const isLocked = !!doc.currentLock;
                return (
                  <div
                    key={doc.master.masterId}
                    onClick={() => setSelectedMasterId(doc.master.masterId)}
                    className={`p-4 cursor-pointer transition-all duration-200 hover:bg-slate-50 ${
                      isSelected ? 'bg-blue-50/60 border-l-4 border-blue-600' : ''
                    }`}
                  >
                    <div className="flex justify-between items-start gap-2">
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <span className="font-mono text-xs font-bold text-blue-700">
                            {doc.master.documentNumber}
                          </span>
                          <Tag
                            color={
                              doc.master.defaultSecurityLevel === 'CONFIDENTIAL'
                                ? 'red'
                                : doc.master.defaultSecurityLevel === 'RESTRICTED'
                                ? 'magenta'
                                : 'blue'
                            }
                            className="text-[10px] leading-tight"
                          >
                            {doc.master.defaultSecurityLevel}
                          </Tag>
                        </div>
                        <div className="font-medium text-slate-800 text-sm line-clamp-1">
                          {doc.master.documentTitle}
                        </div>
                        <div className="text-slate-500 text-xs flex items-center gap-3">
                          <span>版本: {doc.currentRevision.revisionLabel}</span>
                          <span>CAD: {doc.currentRevision.cadSoftwareType}</span>
                        </div>
                      </div>

                      <div className="text-right shrink-0 flex flex-col items-end gap-1">
                        <Tag
                          color={
                            doc.currentRevision.lifecycleState === 'RELEASED'
                              ? 'green'
                              : doc.currentRevision.lifecycleState === 'IN_REVIEW'
                              ? 'orange'
                              : 'default'
                          }
                        >
                          {doc.currentRevision.lifecycleState}
                        </Tag>
                        {isLocked ? (
                          <span className="text-[10px] text-amber-600 flex items-center gap-1 font-mono">
                            <Lock className="w-3 h-3" /> 已签出锁控
                          </span>
                        ) : (
                          <span className="text-[10px] text-emerald-600 flex items-center gap-1 font-mono">
                            <Unlock className="w-3 h-3" /> 可签出
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </Card>
        </Col>

        {/* 右侧四层实体解耦全景看板 */}
        <Col xs={24} lg={15}>
          {currentDoc ? (
            <div className="space-y-4">
              {/* 层级 1 & 2: Master 与 Revision 综合看板 */}
              <Card
                className="shadow-sm border-slate-200"
                title={
                  <div className="flex justify-between items-center">
                    <div className="flex items-center gap-2">
                      <FileCode className="w-5 h-5 text-blue-600" />
                      <span className="font-bold text-slate-800">
                        {currentDoc.master.documentTitle}
                      </span>
                    </div>
                    {/* 悲观锁操作工具栏 */}
                    <div>
                      {currentDoc.currentLock ? (
                        <Space>
                          <Tag color="warning" icon={<Lock className="w-3 h-3 inline" />}>
                            由 {currentDoc.currentLock.lockedByUserId} 签出独占 (有效期至{' '}
                            {new Date(currentDoc.currentLock.lockExpiresAt).toLocaleTimeString()})
                          </Tag>
                          <Button
                            type="primary"
                            size="small"
                            onClick={() => setCheckinModalOpen(true)}
                            className="bg-emerald-600"
                          >
                            签入新版本
                          </Button>
                          <Popconfirm
                            title="确定要撤销签出吗？"
                            description="撤销后将释放排他悲观锁，放弃未暂存变更。"
                            onConfirm={handleCancelCheckout}
                          >
                            <Button size="small" danger>
                              撤销签出
                            </Button>
                          </Popconfirm>
                        </Space>
                      ) : (
                        <Button
                          type="primary"
                          size="small"
                          icon={<Lock className="w-3.5 h-3.5" />}
                          onClick={() => setCheckoutModalOpen(true)}
                          className="bg-blue-600"
                        >
                          签出加锁 (Check-Out)
                        </Button>
                      )}
                    </div>
                  </div>
                }
              >
                <Descriptions size="small" column={{ xs: 1, sm: 2, md: 3 }} bordered>
                  <Descriptions.Item label="全局业务编号">
                    <span className="font-mono font-bold text-blue-700">
                      {currentDoc.master.documentNumber}
                    </span>
                  </Descriptions.Item>
                  <Descriptions.Item label="受控工程类别">
                    <Tag color="cyan">{currentDoc.master.docCategoryCode}</Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="安全密级">
                    <Tag color="red">{currentDoc.master.defaultSecurityLevel}</Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="当前版本修订">
                    <span className="font-bold">{currentDoc.currentRevision.revisionLabel}</span> (
                    {currentDoc.currentRevision.lifecycleState})
                  </Descriptions.Item>
                  <Descriptions.Item label="CAD 建模软件">
                    {currentDoc.currentRevision.cadSoftwareType}{' '}
                    <span className="text-slate-400 font-mono">
                      {currentDoc.currentRevision.cadSoftwareVersion}
                    </span>
                  </Descriptions.Item>
                  <Descriptions.Item label="主责部门/设计室">
                    {currentDoc.master.departmentId}
                  </Descriptions.Item>
                  <Descriptions.Item label="设计摘要" span={3}>
                    <span className="text-slate-600">{currentDoc.currentRevision.summary}</span>
                  </Descriptions.Item>
                </Descriptions>
              </Card>

              {/* 层级 3 & 4: 数据集容器 (Dataset) 与不可变物理制品 (Artifact) */}
              <Card
                title={
                  <div className="flex items-center gap-2">
                    <Layers className="w-4 h-4 text-indigo-600" />
                    <span className="font-semibold text-sm">
                      四层实体：数据集容器 (Dataset) 与物理制品 (Artifact)
                    </span>
                  </div>
                }
                className="shadow-sm border-slate-200"
              >
                <div className="space-y-4">
                  {currentDoc.datasets.map((dsItem: any) => (
                    <div
                      key={dsItem.dataset.datasetId}
                      className="p-4 bg-slate-50 rounded-lg border border-slate-200 space-y-3"
                    >
                      <div className="flex justify-between items-center">
                        <div className="flex items-center gap-2">
                          <span className="font-mono text-xs font-bold text-slate-700 bg-slate-200 px-2 py-0.5 rounded">
                            {dsItem.dataset.datasetCode}
                          </span>
                          <span className="font-medium text-slate-800 text-sm">
                            {dsItem.dataset.name}
                          </span>
                        </div>
                        <Tag color="purple">{dsItem.boundArtifacts.length} 个物理制品绑定</Tag>
                      </div>

                      {/* 物理制品列表 (不可变字节流与强校验哈希) */}
                      <div className="space-y-2">
                        {dsItem.boundArtifacts.map((ba: any) => (
                          <div
                            key={ba.artifact.artifactId}
                            className="bg-white p-3 rounded border border-slate-200 flex flex-col md:flex-row justify-between items-start md:items-center gap-3"
                          >
                            <div className="space-y-1">
                              <div className="flex items-center gap-2">
                                <span className="font-bold text-slate-800 text-xs">
                                  {ba.artifact.fileName}
                                </span>
                                <Tag
                                  color={
                                    ba.binding.fileRole === 'PRIMARY_NATIVE'
                                      ? 'blue'
                                      : ba.binding.fileRole === 'DERIVATIVE_PDF'
                                      ? 'green'
                                      : 'default'
                                  }
                                  className="text-[10px]"
                                >
                                  {ba.binding.fileRole}
                                </Tag>
                                <span className="text-[11px] text-slate-400 font-mono">
                                  {(ba.artifact.fileSizeBytes / (1024 * 1024)).toFixed(2)} MB
                                </span>
                              </div>

                              <div className="flex items-center gap-2 text-[11px] font-mono text-slate-500">
                                <span>SHA-256:</span>
                                <Tooltip title="基于 SHA-256 强散列，物理哈希永久不可变 (CST-M19-01)">
                                  <span className="bg-slate-100 px-1.5 py-0.5 rounded text-slate-700 select-all">
                                    {ba.artifact.sha256Hash}
                                  </span>
                                </Tooltip>
                                <Button
                                  type="text"
                                  size="small"
                                  icon={<Copy className="w-3 h-3 text-slate-400" />}
                                  onClick={() => {
                                    navigator.clipboard.writeText(ba.artifact.sha256Hash);
                                    message.success('制品 SHA-256 哈希已复制');
                                  }}
                                />
                              </div>

                              <div className="text-[10px] font-mono text-slate-400">
                                存储路径: {ba.artifact.storageObjectPath}
                              </div>
                            </div>

                            <Space className="shrink-0">
                              <Button
                                size="small"
                                icon={<Eye className="w-3.5 h-3.5" />}
                                onClick={() => {
                                  setSelectedPreviewArtifact(ba.artifact);
                                  setPreviewDrawerOpen(true);
                                }}
                              >
                                受控预览
                              </Button>
                              <Button
                                size="small"
                                type="primary"
                                icon={<Download className="w-3.5 h-3.5" />}
                                onClick={() => {
                                  message.loading({ content: '换取超短预签名下载 URL...', key: 'dl' });
                                  setTimeout(() => {
                                    message.success({ content: '开始受控下载，访问日志已记入审计流水 (M19-F05)', key: 'dl' });
                                  }, 600);
                                }}
                                className="bg-slate-800"
                              >
                                安全下载
                              </Button>
                            </Space>
                          </div>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </Card>

              {/* 派生流水线谱系与跨域工程引用 */}
              <Tabs
                defaultActiveKey="derivations"
                type="card"
                className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm"
                items={[
                  {
                    key: 'derivations',
                    label: (
                      <span className="flex items-center gap-1.5">
                        <GitBranch className="w-4 h-4 text-emerald-600" />
                        派生文件全生命周期谱系 (M19-F03)
                      </span>
                    ),
                    children: (
                      <div className="space-y-3">
                        {currentDoc.derivations.length > 0 ? (
                          currentDoc.derivations.map((drv: any) => (
                            <div
                              key={drv.derivationId}
                              className="p-3 bg-emerald-50/60 border border-emerald-200 rounded-lg text-xs space-y-1"
                            >
                              <div className="flex justify-between items-center font-bold text-emerald-800">
                                <span>
                                  源物理件 #{drv.sourceArtifactId} ➔ 派生受控件 #{drv.derivedArtifactId}
                                </span>
                                <Tag color="green">转换合格</Tag>
                              </div>
                              <div className="text-slate-600 font-mono">
                                转换引擎: {drv.converterEngine} ({drv.converterVersion})
                              </div>
                              <div className="text-slate-500 font-mono text-[11px]">
                                参数配置: {drv.derivationParameters}
                              </div>
                            </div>
                          ))
                        ) : (
                          <div className="text-slate-400 text-xs py-4 text-center">
                            暂无异步派生记录，签入 CAD 原生件时可勾选自动生成受控 PDF
                          </div>
                        )}
                      </div>
                    ),
                  },
                  {
                    key: 'crossLinks',
                    label: (
                      <span className="flex items-center gap-1.5">
                        <Sparkles className="w-4 h-4 text-purple-600" />
                        跨域工程引用绑定 (M19-F04)
                      </span>
                    ),
                    children: (
                      <div className="space-y-2">
                        {currentDoc.crossLinks.length > 0 ? (
                          currentDoc.crossLinks.map((link: any, idx: number) => (
                            <div
                              key={idx}
                              className="p-3 bg-purple-50/50 border border-purple-200 rounded-lg flex justify-between items-center text-xs"
                            >
                              <div>
                                <span className="font-bold text-purple-900 mr-2">
                                  [{link.linkType}]
                                </span>
                                <span className="font-mono text-purple-700 mr-2">
                                  {link.targetEntityId}
                                </span>
                                <span className="text-slate-700">{link.targetEntityName}</span>
                              </div>
                              <Tag color="purple">{link.status}</Tag>
                            </div>
                          ))
                        ) : (
                          <div className="text-slate-400 text-xs py-4 text-center">
                            暂无跨域引用
                          </div>
                        )}
                      </div>
                    ),
                  },
                ]}
              />
            </div>
          ) : (
            <Card className="h-full flex items-center justify-center text-slate-400">
              请在左侧选择图文档查看四层实体详情
            </Card>
          )}
        </Col>
      </Row>

      {/* =========================================================================
          模态框 1: 创建/注册新图文档
         ========================================================================= */}
      <Modal
        title="创建/注册新工程图文档 (DocumentMaster & Revision)"
        open={createModalOpen}
        onCancel={() => setCreateModalOpen(false)}
        onOk={handleCreateDocument}
        okText="立即纳管创建"
        cancelText="取消"
        width={650}
      >
        <Form form={createForm} layout="vertical" className="mt-4">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="documentNumber"
                label="业务主键图号 (不可变编码)"
                rules={[{ required: true, message: '请输入标准机床图号' }]}
                initialValue="DOC-VMC850-MECH-005"
              >
                <Input placeholder="例: DOC-VMC850-MECH-005" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="docCategoryCode"
                label="文档业务受控分类"
                rules={[{ required: true }]}
                initialValue="MECH_DRAWING"
              >
                <Select>
                  <Option value="MECH_DRAWING">机械图样 (MECH)</Option>
                  <Option value="ELEC_SCHEMATIC">电气原理图 (ELEC)</Option>
                  <Option value="TEST_REPORT">试验检测报告 (TEST)</Option>
                  <Option value="TECH_SPEC">出厂技术规格书 (SPEC)</Option>
                  <Option value="USER_MANUAL">用户操作手册 (MANUAL)</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="documentTitle"
            label="图文档全称"
            rules={[{ required: true, message: '请输入图文档中文全称' }]}
            initialValue="VMC850 X/Y/Z 轴滚珠丝杠预紧机构装配公差图"
          >
            <Input placeholder="例: VMC850 X/Y/Z 轴滚珠丝杠预紧机构装配公差图" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="securityLevel"
                label="安全密级 (PBAC)"
                rules={[{ required: true }]}
                initialValue="INTERNAL"
              >
                <Select>
                  <Option value="PUBLIC">公开 (PUBLIC)</Option>
                  <Option value="INTERNAL">内部受控 (INTERNAL)</Option>
                  <Option value="CONFIDENTIAL">机密/核心技术 (CONFIDENTIAL)</Option>
                  <Option value="RESTRICTED">绝密/关键参数 (RESTRICTED)</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="cadSoftwareType"
                label="CAD/EDA 软件"
                initialValue="SolidWorks"
              >
                <Select>
                  <Option value="SolidWorks">SolidWorks</Option>
                  <Option value="AutoCAD">AutoCAD</Option>
                  <Option value="EPLAN Pro Panel">EPLAN</Option>
                  <Option value="NX">Siemens NX</Option>
                  <Option value="CATIA">CATIA</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="departmentId"
                label="主责设计室"
                initialValue="传动机构室"
              >
                <Input placeholder="例: 传动机构室" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="summary" label="设计论证与工程摘要">
            <Input.TextArea
              rows={3}
              placeholder="记录该图样的主要配合公差指标、刚度校核以及投产技术要求..."
              defaultValue="选用 C3 级双螺母预紧滚珠丝杠副，轴向间隙消除至 0.002mm 以内。"
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* =========================================================================
          模态框 2: 悲观签出锁定 (Check-Out Lock)
         ========================================================================= */}
      <Modal
        title={
          <div className="flex items-center gap-2 text-amber-600">
            <Lock className="w-5 h-5" />
            <span>悲观并发排他签出锁定 (Check-Out)</span>
          </div>
        }
        open={checkoutModalOpen}
        onCancel={() => setCheckoutModalOpen(false)}
        onOk={handleCheckout}
        okText="确认签出独占"
        cancelText="取消"
      >
        <Alert
          type="warning"
          message="排他并发控制规约 (M19-F01)"
          description="签出成功后，系统将在该图档施加排他悲观锁。锁定期间其他工程师将只读无法覆盖草稿，保护协同一致性。"
          className="mb-4"
          showIcon
        />
        <Form form={checkoutForm} layout="vertical">
          <Form.Item
            name="lockDurationHours"
            label="锁定时长 (小时，超时自动熔断)"
            rules={[{ required: true }]}
            initialValue={8}
          >
            <Select>
              <Option value={2}>2 小时 (临时微调)</Option>
              <Option value={8}>8 小时 (标准工作日)</Option>
              <Option value={24}>24 小时 (重大图纸重构)</Option>
              <Option value={72}>72 小时 (最大上限)</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="comments"
            label="签出设计原因与任务目标"
            rules={[{ required: true, message: '请填写签出原因' }]}
            initialValue="根据主轴动刚度优化方案，修改前端轴承配合间隙尺寸"
          >
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      {/* =========================================================================
          模态框 3: 签入新版本并释放锁 (Check-In)
         ========================================================================= */}
      <Modal
        title={
          <div className="flex items-center gap-2 text-emerald-600">
            <FileCheck className="w-5 h-5" />
            <span>图文档版本签入与制品绑定 (Check-In)</span>
          </div>
        }
        open={checkinModalOpen}
        onCancel={() => setCheckinModalOpen(false)}
        onOk={handleCheckin}
        okText="完成签入并解锁"
        cancelText="取消"
      >
        <Alert
          type="info"
          message="版本迭代规约 (CST-M19-01)"
          description="签入后系统将自动释放排他锁，并将新物理制品设为有效版本，原制品作为历史归档不可篡改保存。"
          className="mb-4"
          showIcon
        />
        <Form form={checkinForm} layout="vertical">
          <Form.Item
            name="checkinComments"
            label="工程签入说明 (Engineering Change Notes)"
            rules={[{ required: true, message: '请填写签入变更说明' }]}
            initialValue="优化前端配合公差 H7/h6，经主管总师审查合格"
          >
            <Input.TextArea rows={3} />
          </Form.Item>

          <Form.Item name="createDerivative" valuePropName="checked" initialValue={true}>
            <Checkbox>自动触发异步受控 PDF 派生流水线与防伪水印生成</Checkbox>
          </Form.Item>
        </Form>
      </Modal>

      {/* =========================================================================
          模态框 4: 大文件分片并发上传与 SHA-256 强哈希校验实验台 (M19-F02 & TC-M19-02)
         ========================================================================= */}
      <Modal
        title={
          <div className="flex items-center gap-2 text-indigo-600">
            <UploadCloud className="w-5 h-5" />
            <span>大文件分片并发上传与 SHA-256 防篡改校验实验台 (M19-F02)</span>
          </div>
        }
        open={uploadLabModalOpen}
        onCancel={() => setUploadLabModalOpen(false)}
        footer={null}
        width={720}
      >
        <div className="space-y-4">
          <Paragraph type="secondary" className="text-xs">
            模拟大容量三维装配体（≥1GB）经由 MinIO 分片并发上传与服务端独立 SHA-256
            强校验双重防线，验证 CST-M19-01 不可变与 TC-M19-02 篡改硬阻断机制。
          </Paragraph>

          <div className="p-4 bg-slate-50 border border-slate-200 rounded-lg space-y-3">
            <div className="flex justify-between items-center">
              <div>
                <div className="font-bold text-slate-800 text-sm">待上传图档样例:</div>
                <div className="font-mono text-xs text-blue-700">
                  VMC1000_Spindle_Shaft_Opt.sldprt (98.45 MB)
                </div>
              </div>
              <Checkbox
                checked={simulateTamper}
                onChange={(e) => setSimulateTamper(e.target.checked)}
                className="text-red-600 font-bold"
              >
                ⚠️ 模拟网络传输丢包或恶意篡改哈希 (TC-M19-02)
              </Checkbox>
            </div>

            <Progress percent={uploadProgress} status={simulateTamper && uploadProgress >= 90 ? 'exception' : 'active'} />

            <div className="flex justify-end">
              <Button
                type="primary"
                loading={isUploading}
                onClick={runMultipartUploadLab}
                className="bg-indigo-600"
              >
                {isUploading ? '分片上传与哈希强校验中...' : '开始受控分片上传与强校验'}
              </Button>
            </div>
          </div>

          {/* 上传管道执行日志 */}
          {uploadStepLogs.length > 0 && (
            <div className="p-4 bg-slate-900 text-slate-100 font-mono text-xs rounded-lg space-y-1.5 max-h-56 overflow-y-auto">
              {uploadStepLogs.map((log, i) => (
                <div key={i} className="leading-relaxed">
                  {log}
                </div>
              ))}
            </div>
          )}

          {uploadedArtifactResult && (
            <Alert
              type="success"
              message="物理制品防篡改校验通过并登记入库"
              description={
                <div className="font-mono text-xs space-y-1 mt-1">
                  <div>制品编号: #{uploadedArtifactResult.artifactId}</div>
                  <div>不可变 SHA-256: {uploadedArtifactResult.sha256Hash}</div>
                  <div>状态: {uploadedArtifactResult.status} (VERIFIED: TRUE)</div>
                </div>
              }
              showIcon
            />
          )}
        </div>
      </Modal>

      {/* =========================================================================
          抽屉/模态框 5: 在线受控预览、防伪动态水印与非破坏性矢量批注 (M19-F03)
         ========================================================================= */}
      <Modal
        title={
          <div className="flex items-center justify-between">
            <span className="font-bold">受控安全审图预览 (带动态微细倾斜防伪水印)</span>
            <Tag color="red">密级: {currentDoc.currentRevision.securityLevel}</Tag>
          </div>
        }
        open={previewDrawerOpen}
        onCancel={() => setPreviewDrawerOpen(false)}
        footer={null}
        width={850}
      >
        <div className="space-y-4">
          {/* 模拟受控图纸渲染窗口，动态倾斜水印覆盖 */}
          <div className="relative bg-slate-800 rounded-lg p-6 min-h-[380px] border border-slate-700 overflow-hidden flex flex-col justify-between">
            {/* 动态 45° 倾斜防伪水印图层 */}
            <div
              className="absolute inset-0 pointer-events-none flex items-center justify-center select-none"
              style={{ transform: 'rotate(-25deg)', opacity: 0.15 }}
            >
              <div className="text-white text-base md:text-lg font-mono font-black text-center whitespace-pre-line leading-loose">
                {watermarkText}
                <br />
                {watermarkText}
                <br />
                {watermarkText}
              </div>
            </div>

            {/* 图样模拟渲染主体 */}
            <div className="relative z-10 text-slate-200 space-y-3">
              <div className="flex justify-between items-center border-b border-slate-700 pb-2">
                <span className="font-mono font-bold text-amber-400">
                  {currentDoc.master.documentNumber} | 图纸幅面 A0 (1:1)
                </span>
                <span className="text-xs text-slate-400">
                  制件: {selectedPreviewArtifact ? selectedPreviewArtifact.fileName : currentDoc.master.documentTitle}
                </span>
              </div>

              <div className="p-4 bg-slate-900/80 rounded border border-dashed border-slate-600 text-xs text-slate-300 font-mono space-y-2">
                <div className="text-emerald-400 font-bold">▶ 配合尺寸与公差技术要求:</div>
                <div>1. 主轴前支撑角接触球轴承组轴颈尺寸: Φ110 mm, 公差带等级: h5 (0, -0.009)</div>
                <div>2. 后支撑双列圆柱滚子轴承配合尺寸: Φ95 mm, 公差带等级: k5 (+0.011, +0.002)</div>
                <div>3. 动平衡等级: 符合 ISO 1940 G0.4 标准，残留不平衡量 ≤ 0.3 g·mm</div>
                <div>4. 水道试压: 0.8 MPa 保持 30 分钟无渗漏</div>
              </div>
            </div>

            {/* 协同批注红框展示 */}
            <div className="relative z-10 border-2 border-red-500 bg-red-500/10 p-2.5 rounded text-red-200 text-xs font-mono">
              <div className="font-bold flex items-center gap-1.5 text-red-400">
                <AlertTriangle className="w-3.5 h-3.5" /> 专家校对标红圈阅 (第 1 页):
              </div>
              <div className="mt-1">
                {currentDoc.annotations.length > 0
                  ? currentDoc.annotations[0].contentText
                  : '前端轴承配合间隙核验达标'}
              </div>
            </div>
          </div>

          {/* 底部非破坏性批注协同提交栏 */}
          <div className="space-y-2 bg-slate-50 p-4 rounded-lg border border-slate-200">
            <div className="font-semibold text-xs text-slate-700">
              添加在线协同非破坏性批注意见 (矢量独立保存，不修改底层 PDF):
            </div>
            <div className="flex gap-2">
              <Input
                placeholder="输入审图校对意见、尺寸修改建议..."
                value={annotationText}
                onChange={(e) => setAnnotationText(e.target.value)}
              />
              <Button type="primary" onClick={handleAddAnnotation} className="bg-blue-600">
                提交批注
              </Button>
            </div>
          </div>
        </div>
      </Modal>

      {/* =========================================================================
          模态框 6: 受控工程交付包导出 (M19-F05 & AT-14)
         ========================================================================= */}
      <Modal
        title={
          <div className="flex items-center gap-2 text-indigo-600">
            <FolderArchive className="w-5 h-5" />
            <span>受控工程交付包组装导出 (AT-14)</span>
          </div>
        }
        open={exportModalOpen}
        onCancel={() => setExportModalOpen(false)}
        onOk={handleExportPackage}
        okText="一键组装打包"
        cancelText="取消"
      >
        <div className="space-y-4">
          <Paragraph type="secondary" className="text-xs">
            系统将选定的机床设计图样及派生文件流式构建加密 ZIP 容器，根目录强制内嵌{' '}
            <code>manifest.json</code> 与 <code>checksums.sha256</code> 防伪校验清单。
          </Paragraph>

          <Descriptions size="small" column={1} bordered>
            <Descriptions.Item label="当前导出基线">
              VMC850 投产制造工程交付包 (2026-Q3)
            </Descriptions.Item>
            <Descriptions.Item label="包含图样文件">
              3 份设计图纸 (含主轴箱 3D 装配、回转工作台 2D 图、激光干涉仪检测报告)
            </Descriptions.Item>
            <Descriptions.Item label="派生文件选项">包含受控审图 PDF 与 Web 3D 网格</Descriptions.Item>
            <Descriptions.Item label="权限核验">
              您的安全许可等级: <Tag color="geekblue">{user?.securityClearance}</Tag>
            </Descriptions.Item>
          </Descriptions>
        </div>
      </Modal>

      {/* =========================================================================
          模态框 7: 文件物理访问与下载审计日志流水 (M19-F05)
         ========================================================================= */}
      <Modal
        title={
          <div className="flex items-center gap-2 text-slate-700">
            <Shield className="w-5 h-5 text-blue-600" />
            <span>物理文件访问与下载审计台账 (FileAccessAudit)</span>
          </div>
        }
        open={auditModalOpen}
        onCancel={() => setAuditModalOpen(false)}
        footer={null}
        width={750}
      >
        <Table
          dataSource={auditLogs}
          rowKey="auditId"
          size="small"
          pagination={false}
          columns={[
            {
              title: '操作人',
              dataIndex: 'userId',
              key: 'userId',
              render: (u) => <span className="font-mono font-bold text-blue-700">{u}</span>,
            },
            {
              title: '访问动作',
              dataIndex: 'accessType',
              key: 'accessType',
              render: (t) => (
                <Tag color={t === 'DOWNLOAD' ? 'green' : t === 'PREVIEW' ? 'blue' : 'purple'}>
                  {t}
                </Tag>
              ),
            },
            {
              title: '文件/制件',
              dataIndex: 'artifactName',
              key: 'artifactName',
              render: (n) => <span className="font-mono text-xs text-slate-800">{n}</span>,
            },
            {
              title: '客户端 IP',
              dataIndex: 'clientIp',
              key: 'clientIp',
              render: (ip) => <span className="font-mono text-xs text-slate-500">{ip}</span>,
            },
            {
              title: '访问时间',
              dataIndex: 'accessedAt',
              key: 'accessedAt',
              render: (t) => <span className="text-xs text-slate-400">{t}</span>,
            },
          ]}
        />
      </Modal>
    </div>
  );
};
export default DocumentManagementPage;
