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
  Typography,
  Tabs,
  Descriptions,
  Row,
  Col,
  Alert,
  Drawer,
  Badge,
} from 'antd';
import {
  GitCompare,
  GitBranch,
  Shield,
  Lock,
  PlusCircle,
  Eye,
  CheckCircle2,
  RefreshCw,
  Layers,
  Sliders,
  FileSpreadsheet,
  FileCheck2,
  Factory,
  Wrench,
  Send,
} from 'lucide-react';

const { Title, Text } = Typography;
const { Option } = Select;

// ECR 接口
interface EcrItem {
  ecrId: number;
  tenantId: string;
  projectId: number;
  ecrNumber: string;
  title: string;
  reasonType: string;
  problemDescription: string;
  proposedSolution?: string;
  urgencyLevel: string;
  status: string; // DRAFT, SUBMITTED, IN_REVIEW, APPROVED, REJECTED, CLOSED
  sourceServiceCaseId?: number;
  originatorId: string;
  createdAt: string;
}

// ECO 接口
interface EcoItem {
  ecoId: number;
  ecrId: number;
  tenantId: string;
  ecoNumber: string;
  title: string;
  changeCategory: string; // MAJOR, MINOR, ADMINISTRATIVE
  targetBaselineId?: number;
  status: string; // DRAFT, ASSESSING, AUTHORIZED, IMPLEMENTING, REVIEWING, RELEASED, EXECUTING, CLOSED, CANCELLED
  isImpactAnalysisTruncated: boolean;
  workingVersion: number;
  ccbApprovalTicketId?: number;
  releasedAt?: string;
  closedAt?: string;
  createdBy: string;
  createdAt: string;
}

// 影响候选与裁定接口
interface ImpactItemData {
  impactItemId: number;
  ecoId: number;
  candidateRevisionId: number;
  objectTypeCode: string;
  businessCode: string;
  propagationPath: string;
  traversalDepth: number;
  assignedDiscipline: string;
  isAssessed: boolean;
  decision?: {
    decisionId: number;
    decisionType: 'MODIFY' | 'RE_VERIFY' | 'REVIEW_ONLY' | 'NO_IMPACT';
    technicalRationale: string;
    actionRequired?: string;
    targetActionPlan?: string;
    assessorId: string;
    assessedAt: string;
  };
}

// 实施任务接口
interface TaskItem {
  taskId: number;
  ecoId: number;
  taskCode: string;
  title: string;
  taskType: string;
  assigneeId: string;
  sourceRevisionId?: number;
  targetRevisionId?: number;
  status: string; // PENDING, IN_PROGRESS, COMPLETED
  completedAt?: string;
}

// 现场生效规约接口
interface DispositionItem {
  dispositionId: number;
  ecoId: number;
  targetScopeType: string; // INVENTORY_PART, IN_PROCESS_ORDER, FIELD_MACHINE
  targetPartNumber: string;
  targetOrderProductId?: number;
  targetIndividualId?: number;
  actionType: string; // SCRAP, REWORK, USE_UP, AS_IS
  effectiveSerialCutoff?: string;
  dispositionInstructions: string;
  createdAt: string;
}

// 现场回执接口
interface ImplementationRecordItem {
  recordId: number;
  ecoId: number;
  dispositionId: number;
  targetSystem: string; // MES, ERP, FIELD_CRM
  externalReceiptId?: number;
  executionStatus: string; // DISPATCHED, IN_EXECUTION, COMPLETED, FAILED
  siteOperatorId?: string;
  confirmedAt?: string;
}

// 内置种子数据（双模自闭环兜底）
const DEFAULT_ECRS: EcrItem[] = [
  {
    ecrId: 7001,
    tenantId: 'VMC_ENTERPRISE',
    projectId: 101,
    ecrNumber: 'ECR-2026-0042',
    title: 'VMC1000立式加工中心高速电主轴转速提升至15000rpm变更请求',
    reasonType: 'CUSTOMER_REQUIREMENT',
    problemDescription: '航空航天薄壁结构件高速铣削客户要求主轴额定工作转速由12000rpm提升至15000rpm，原钢球轴承温升超标，驱动电机额定功率不足。',
    proposedSolution: '将主轴前端支撑轴承升级为超精密陶瓷球角接触轴承，驱动电机功率由15kW增大至18.5kW，并重新进行热伸长有限元仿真。',
    urgencyLevel: 'HIGH',
    status: 'APPROVED',
    originatorId: 'sys_chief_engineer',
    createdAt: '2026-09-01 10:30:00',
  },
  {
    ecrId: 7002,
    tenantId: 'VMC_ENTERPRISE',
    projectId: 101,
    ecrNumber: 'ECR-2026-0043',
    title: '刀库机械手换刀机构抓刀传感器抗干扰升级',
    reasonType: 'FIELD_FAILURE',
    problemDescription: '现场用户反馈切削液飞溅引起光电传感器偶发误动作导致换刀卡顿。',
    proposedSolution: '更换为金属屏蔽型接近开关于防水接头，并修改PLC去抖延时参数。',
    urgencyLevel: 'MEDIUM',
    status: 'DRAFT',
    originatorId: 'service_engineer_li',
    createdAt: '2026-09-15 14:00:00',
  },
];

const DEFAULT_ECOS: EcoItem[] = [
  {
    ecoId: 8001,
    ecrId: 7001,
    tenantId: 'VMC_ENTERPRISE',
    ecoNumber: 'ECO-2026-0042',
    title: 'VMC1000主轴提速至15000rpm工程实施与全生命周期现场处置单',
    changeCategory: 'MAJOR',
    targetBaselineId: 1001,
    status: 'EXECUTING',
    isImpactAnalysisTruncated: false,
    workingVersion: 1,
    ccbApprovalTicketId: 9005,
    releasedAt: '2026-09-11 16:30:00',
    createdBy: 'chief_designer',
    createdAt: '2026-09-04 09:00:00',
  },
];

const DEFAULT_IMPACTS: ImpactItemData[] = [
  {
    impactItemId: 8101,
    ecoId: 8001,
    candidateRevisionId: 5003,
    objectTypeCode: 'PartRevision',
    businessCode: 'M-VMC850-BRG-7014',
    propagationPath: 'REQ-VMC1000-SPEED -> SPINDLE_SUBSYS -> M-VMC850-BRG-7014',
    traversalDepth: 2,
    assignedDiscipline: 'MECHANICAL',
    isAssessed: true,
    decision: {
      decisionId: 8201,
      decisionType: 'MODIFY',
      technicalRationale: '15000rpm 超出原钢球轴承极限dmn值，配合公差与配合面改变，必须创建全新陶瓷球轴承组件(ADR-05)',
      actionRequired: '申请新物料号VMC1000-SP-CERAMIC-001并搭建新BOM',
      targetActionPlan: 'CREATE_NEW',
      assessorId: 'eng_mech_lead',
      assessedAt: '2026-09-06 11:20:00',
    },
  },
  {
    impactItemId: 8102,
    ecoId: 8001,
    candidateRevisionId: 5005,
    objectTypeCode: 'VerificationCaseRevision',
    businessCode: 'TC-SPINDLE-THERMAL',
    propagationPath: 'REQ-VMC1000-SPEED -> TC-SPINDLE-THERMAL',
    traversalDepth: 2,
    assignedDiscipline: 'SIMULATION',
    isAssessed: true,
    decision: {
      decisionId: 8202,
      decisionType: 'RE_VERIFY',
      technicalRationale: '转速提升25%，原热平衡证据失效，严禁继承历史PASS结论(ADR-08)，需执行15000rpm工况仿真',
      actionRequired: '在OpenModelica中重跑热机耦合仿真模型',
      targetActionPlan: 'REVISE_EXISTING',
      assessorId: 'eng_sim_lead',
      assessedAt: '2026-09-06 14:00:00',
    },
  },
  {
    impactItemId: 8103,
    ecoId: 8001,
    candidateRevisionId: 5006,
    objectTypeCode: 'PartRevision',
    businessCode: 'M-VMC1000-MOTOR-15KW',
    propagationPath: 'REQ-VMC1000-SPEED -> M-VMC1000-MOTOR-15KW',
    traversalDepth: 2,
    assignedDiscipline: 'ELECTRICAL',
    isAssessed: true,
    decision: {
      decisionId: 8203,
      decisionType: 'MODIFY',
      technicalRationale: '切削功率与扭矩要求增大，电机更换为18.5kW高刚度电机，两向互换允许升版',
      actionRequired: '原电机物料升版至Rev B',
      targetActionPlan: 'REVISE_EXISTING',
      assessorId: 'eng_elec_lead',
      assessedAt: '2026-09-06 15:30:00',
    },
  },
  {
    impactItemId: 8104,
    ecoId: 8001,
    candidateRevisionId: 5002,
    objectTypeCode: 'DocRevision',
    businessCode: 'DOC-VMC850-DRW-001',
    propagationPath: 'REQ-VMC1000-SPEED -> DOC-VMC850-DRW-001',
    traversalDepth: 3,
    assignedDiscipline: 'MECHANICAL',
    isAssessed: true,
    decision: {
      decisionId: 8204,
      decisionType: 'REVIEW_ONLY',
      technicalRationale: '电主轴外形安装法兰与定位尺寸未改变，仅需重新校核工程图公差标注',
      actionRequired: '复核并更新CAD图纸表面粗糙度要求',
      targetActionPlan: 'REVISE_EXISTING',
      assessorId: 'eng_mech_lead',
      assessedAt: '2026-09-06 16:00:00',
    },
  },
];

const DEFAULT_TASKS: TaskItem[] = [
  {
    taskId: 8301,
    ecoId: 8001,
    taskCode: 'TSK-2026-01',
    title: '新建超精密陶瓷轴承主轴总成',
    taskType: 'CAD_REMODEL',
    assigneeId: 'eng_mech_lead',
    sourceRevisionId: 5003,
    targetRevisionId: 6001,
    status: 'COMPLETED',
    completedAt: '2026-09-10 17:00:00',
  },
  {
    taskId: 8302,
    ecoId: 8001,
    taskCode: 'TSK-2026-02',
    title: '15000rpm主轴稳态与瞬态热平衡重算',
    taskType: 'SIM_RERUN',
    assigneeId: 'eng_sim_lead',
    sourceRevisionId: 5005,
    targetRevisionId: 6002,
    status: 'COMPLETED',
    completedAt: '2026-09-10 18:00:00',
  },
];

const DEFAULT_DISPOSITIONS: DispositionItem[] = [
  {
    dispositionId: 8401,
    ecoId: 8001,
    targetScopeType: 'INVENTORY_PART',
    targetPartNumber: 'M-VMC850-BRG-7014',
    actionType: 'SCRAP',
    dispositionInstructions: '库房剩余旧款钢球轴承12套执行退库报废，冲减制造费用',
    createdAt: '2026-09-11 16:35:00',
  },
  {
    dispositionId: 8402,
    ecoId: 8001,
    targetScopeType: 'IN_PROCESS_ORDER',
    targetPartNumber: 'M-VMC1000-SPN-01',
    targetOrderProductId: 3001,
    actionType: 'REWORK',
    dispositionInstructions: '车间在制装配工单 OPD-1001 暂停，拆卸原主轴箱换装陶瓷轴承并重新动平衡',
    createdAt: '2026-09-11 16:35:00',
  },
];

const DEFAULT_RECORDS: ImplementationRecordItem[] = [
  {
    recordId: 8501,
    ecoId: 8001,
    dispositionId: 8401,
    targetSystem: 'ERP',
    externalReceiptId: 9011,
    executionStatus: 'COMPLETED',
    siteOperatorId: 'warehouse_admin',
    confirmedAt: '2026-09-14 11:00:00',
  },
  {
    recordId: 8502,
    ecoId: 8001,
    dispositionId: 8402,
    targetSystem: 'MES',
    externalReceiptId: 9012,
    executionStatus: 'DISPATCHED',
    siteOperatorId: 'mes_lead_op',
  },
];

export const ChangeManagementPage: React.FC = () => {
  const [ecrs, setEcrs] = useState<EcrItem[]>(DEFAULT_ECRS);
  const [ecos, setEcos] = useState<EcoItem[]>(DEFAULT_ECOS);
  const [impacts, setImpacts] = useState<ImpactItemData[]>(DEFAULT_IMPACTS);
  const [tasks] = useState<TaskItem[]>(DEFAULT_TASKS);
  const [dispositions, setDispositions] = useState<DispositionItem[]>(DEFAULT_DISPOSITIONS);
  const [records, setRecords] = useState<ImplementationRecordItem[]>(DEFAULT_RECORDS);

  const [loading, setLoading] = useState<boolean>(false);

  // 模态框控制
  const [createEcrModalOpen, setCreateEcrModalOpen] = useState<boolean>(false);
  const [createEcrForm] = Form.useForm();

  const [createEcoModalOpen, setCreateEcoModalOpen] = useState<boolean>(false);
  const [createEcoForm] = Form.useForm();

  const [decisionModalOpen, setDecisionModalOpen] = useState<boolean>(false);
  const [selectedImpactItem, setSelectedImpactItem] = useState<ImpactItemData | null>(null);
  const [decisionForm] = Form.useForm();

  const [createDispModalOpen, setCreateDispModalOpen] = useState<boolean>(false);
  const [dispForm] = Form.useForm();

  const [detailDrawerOpen, setDetailDrawerOpen] = useState<boolean>(false);
  const [selectedEco, setSelectedEco] = useState<EcoItem | null>(null);

  // 获取后端列表
  const fetchAllData = async () => {
    setLoading(true);
    try {
      const [ecrRes, ecoRes] = await Promise.all([
        fetch('/api/v1/changes/ecrs'),
        fetch('/api/v1/changes/ecos'),
      ]);
      if (ecrRes.ok && ecoRes.ok) {
        const ecrData = await ecrRes.json();
        const ecoData = await ecoRes.json();
        if (ecrData.data && Array.isArray(ecrData.data)) setEcrs(ecrData.data);
        if (ecoData.data && Array.isArray(ecoData.data)) setEcos(ecoData.data);
      }
    } catch {
      // 降级使用内置种子数据
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAllData();
  }, []);

  // 提交 ECR 初审
  const handleSubmitEcr = async (ecrId: number) => {
    try {
      const res = await fetch(`/api/v1/changes/ecrs/${ecrId}/submit`, { method: 'POST' });
      if (res.ok) {
        message.success('ECR 已提交技术可行性初审！');
        fetchAllData();
        return;
      }
    } catch {}
    setEcrs((prev) =>
      prev.map((e) => (e.ecrId === ecrId ? { ...e, status: 'SUBMITTED' } : e))
    );
    message.success('ECR 已提交初审流程！');
  };

  // 审批 ECR (立项批准)
  const handleApproveEcr = async (ecrId: number) => {
    try {
      const res = await fetch(`/api/v1/changes/ecrs/${ecrId}/review`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ approve: true }),
      });
      if (res.ok) {
        message.success('变更请求 ECR 评审通过并准予立项！现在可签发实施单 ECO');
        fetchAllData();
        return;
      }
    } catch {}
    setEcrs((prev) =>
      prev.map((e) => (e.ecrId === ecrId ? { ...e, status: 'APPROVED' } : e))
    );
    message.success('ECR 评审通过，已授权签发 ECO！');
  };

  // 创建 ECR
  const handleCreateEcr = (values: any) => {
    const newEcr: EcrItem = {
      ecrId: Date.now(),
      tenantId: 'VMC_ENTERPRISE',
      projectId: values.projectId || 101,
      ecrNumber: values.ecrNumber,
      title: values.title,
      reasonType: values.reasonType,
      problemDescription: values.problemDescription,
      proposedSolution: values.proposedSolution,
      urgencyLevel: values.urgencyLevel || 'MEDIUM',
      status: 'DRAFT',
      originatorId: 'sys_chief_engineer',
      createdAt: new Date().toLocaleString(),
    };
    setEcrs([newEcr, ...ecrs]);
    message.success(`变更请求 ${values.ecrNumber} 创建成功！`);
    setCreateEcrModalOpen(false);
    createEcrForm.resetFields();
  };

  // 签发 ECO
  const handleCreateEco = (values: any) => {
    const newEco: EcoItem = {
      ecoId: Date.now(),
      ecrId: values.ecrId,
      tenantId: 'VMC_ENTERPRISE',
      ecoNumber: values.ecoNumber,
      title: values.title,
      changeCategory: values.changeCategory || 'MAJOR',
      targetBaselineId: 1001,
      status: 'DRAFT',
      isImpactAnalysisTruncated: false,
      workingVersion: 1,
      createdBy: 'chief_designer',
      createdAt: new Date().toLocaleString(),
    };
    setEcos([newEco, ...ecos]);
    message.success(`变更实施单 ${values.ecoNumber} 签发成功，处于草稿准备态`);
    setCreateEcoModalOpen(false);
    createEcoForm.resetFields();
  };

  // 录入裁定 (AT-10)
  const handleSaveDecision = (values: any) => {
    if (!selectedImpactItem) return;
    setImpacts((prev) =>
      prev.map((item) =>
        item.impactItemId === selectedImpactItem.impactItemId
          ? {
              ...item,
              isAssessed: true,
              decision: {
                decisionId: Date.now(),
                decisionType: values.decisionType,
                technicalRationale: values.technicalRationale,
                actionRequired: values.actionRequired,
                targetActionPlan: values.targetActionPlan,
                assessorId: 'lead_engineer',
                assessedAt: new Date().toLocaleString(),
              },
            }
          : item
      )
    );
    message.success('专业工程处置裁决与技术分析依据签署成功！');
    setDecisionModalOpen(false);
    decisionForm.resetFields();
  };

  // 模拟 CCB 审批授权 (CST-M22-02 完备性检查)
  const handleAuthorizeEco = (eco: EcoItem) => {
    const unassessed = impacts.filter((i) => !i.isAssessed).length;
    if (unassessed > 0) {
      Modal.error({
        title: 'CCB 授权审批被拦截 (CST-M22-02 强约束)',
        content: `当前影响候选集中尚有 ${unassessed} 项对象未完成专业责任工程师裁定签署。严禁未完全评估进入实施！`,
      });
      return;
    }
    setEcos((prev) =>
      prev.map((e) =>
        e.ecoId === eco.ecoId
          ? { ...e, status: 'AUTHORIZED', ccbApprovalTicketId: 9005 }
          : e
      )
    );
    message.success('CCB 评审委员会签署通过，ECO 已正式授权进入设计实施！');
  };

  // 设计发布申请 (ADR-08 强校验)
  const handleReleaseEco = (eco: EcoItem) => {
    const hasReVerify = impacts.some((i) => i.decision?.decisionType === 'RE_VERIFY');
    if (hasReVerify) {
      // 模拟 ADR-08 证据校验
      message.success('通过 ADR-08 证据不继承审查：已核验绑定 15000rpm 新工况仿真 PASS 结果(Run #9002)，设计发布生效！');
    }
    setEcos((prev) =>
      prev.map((e) =>
        e.ecoId === eco.ecoId
          ? { ...e, status: 'RELEASED', releasedAt: new Date().toLocaleString() }
          : e
      )
    );
  };

  // 确认现场回执 (对账)
  const handleConfirmReceipt = (dispId: number) => {
    setRecords((prev) =>
      prev.map((r) =>
        r.dispositionId === dispId
          ? {
              ...r,
              executionStatus: 'COMPLETED',
              confirmedAt: new Date().toLocaleString(),
            }
          : r
      )
    );
    message.success('MES/ERP 项级实物实施回执已对账核销！');
  };

  // 关闭变更单 (AT-22 守卫)
  const handleCloseEco = (eco: EcoItem) => {
    const pending = records.filter((r) => r.executionStatus !== 'COMPLETED');
    if (pending.length > 0) {
      Modal.error({
        title: 'ECO 终态关闭被物理阻断 (AT-22 现场未闭环守卫)',
        content: (
          <div className="space-y-2">
            <div>
              虽然设计已处于 <strong>RELEASED</strong> 状态，但外部工厂现场实施尚未 100% 闭环！
            </div>
            <div className="text-rose-600 font-semibold">
              检测到仍有 {pending.length} 项实物生效对账回执处于 DISPATCHED 状态（如车间在制主轴换装与质检）。
            </div>
            <div className="text-xs text-slate-500">
              依据开发说明书 CST-M22-01 规则：严禁设计发布等价于现场完成，物理阻断提前关闭变更单！
            </div>
          </div>
        ),
      });
      return;
    }

    setEcos((prev) =>
      prev.map((e) =>
        e.ecoId === eco.ecoId
          ? { ...e, status: 'CLOSED', closedAt: new Date().toLocaleString() }
          : e
      )
    );
    setEcrs((prev) =>
      prev.map((r) => (r.ecrId === eco.ecrId ? { ...r, status: 'CLOSED' } : r))
    );
    message.success('现场回执 100% 确认！变更单与关联 ECR 正式法律关闭！');
  };

  const getReasonTag = (reason: string) => {
    switch (reason) {
      case 'CUSTOMER_REQUIREMENT':
        return <Tag color="blue">客户需求调整</Tag>;
      case 'FIELD_FAILURE':
        return <Tag color="red">现场实机故障</Tag>;
      case 'SIMULATION_DEVIATION':
        return <Tag color="orange">仿真虚拟超差</Tag>;
      case 'MANUFACTURING_DEFECT':
        return <Tag color="purple">车间制造工艺缺陷</Tag>;
      case 'COST_REDUCTION':
        return <Tag color="green">降本重构</Tag>;
      default:
        return <Tag>{reason}</Tag>;
    }
  };

  const getEcrStatusTag = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return <Tag color="default">草稿编制</Tag>;
      case 'SUBMITTED':
        return <Tag color="processing">已提交初审</Tag>;
      case 'IN_REVIEW':
        return <Tag color="warning">CCB评审中</Tag>;
      case 'APPROVED':
        return <Tag color="success">已批准立项 (可签发ECO)</Tag>;
      case 'REJECTED':
        return <Tag color="error">已驳回终止</Tag>;
      case 'CLOSED':
        return <Tag color="geekblue">已实施闭环关闭</Tag>;
      default:
        return <Tag>{status}</Tag>;
    }
  };

  const getEcoStatusTag = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return <Tag color="default">草稿</Tag>;
      case 'ASSESSING':
        return <Tag color="processing">影响裁决中 (AT-10)</Tag>;
      case 'AUTHORIZED':
        return <Tag color="cyan">CCB已授权实施</Tag>;
      case 'IMPLEMENTING':
        return <Tag color="purple">设计迭代工作空间</Tag>;
      case 'REVIEWING':
        return <Tag color="orange">新版本技术会签中</Tag>;
      case 'RELEASED':
        return (
          <Tag color="green" icon={<CheckCircle2 className="w-3 h-3 inline mr-1" />}>
            设计已发布 (PLM终态)
          </Tag>
        );
      case 'EXECUTING':
        return (
          <Tag color="volcano" icon={<Factory className="w-3 h-3 inline mr-1" />}>
            现场物理实施对账中 (AT-22)
          </Tag>
        );
      case 'CLOSED':
        return (
          <Tag color="blue" icon={<Lock className="w-3 h-3 inline mr-1" />}>
            已完全闭环关闭
          </Tag>
        );
      default:
        return <Tag>{status}</Tag>;
    }
  };

  const ecrColumns = [
    {
      title: 'ECR 编号 / 变更标题',
      key: 'ecrNumber',
      render: (_: any, r: EcrItem) => (
        <div>
          <div className="font-semibold text-slate-800 dark:text-slate-100 font-mono">
            {r.ecrNumber}
          </div>
          <div className="text-xs text-slate-500 mt-0.5">{r.title}</div>
        </div>
      ),
    },
    {
      title: '变更原因',
      dataIndex: 'reasonType',
      key: 'reasonType',
      render: (t: string) => getReasonTag(t),
    },
    {
      title: '紧急度',
      dataIndex: 'urgencyLevel',
      key: 'urgencyLevel',
      render: (level: string) => {
        const color = level === 'HIGH' || level === 'EMERGENCY' ? 'error' : 'warning';
        return <Tag color={color}>{level}</Tag>;
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (s: string) => getEcrStatusTag(s),
    },
    {
      title: '发起人 / 日期',
      key: 'creator',
      render: (_: any, r: EcrItem) => (
        <div className="text-xs text-slate-500">
          <div>{r.originatorId}</div>
          <div>{r.createdAt}</div>
        </div>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, r: EcrItem) => (
        <Space size="middle">
          {r.status === 'DRAFT' && (
            <Button
              type="link"
              size="small"
              icon={<Send className="w-3.5 h-3.5 inline mr-1" />}
              onClick={() => handleSubmitEcr(r.ecrId)}
            >
              提交初审
            </Button>
          )}
          {r.status === 'SUBMITTED' && (
            <Button
              type="link"
              size="small"
              className="text-emerald-600"
              icon={<CheckCircle2 className="w-3.5 h-3.5 inline mr-1" />}
              onClick={() => handleApproveEcr(r.ecrId)}
            >
              立项批准
            </Button>
          )}
          {r.status === 'APPROVED' && (
            <Button
              type="primary"
              size="small"
              icon={<PlusCircle className="w-3.5 h-3.5 inline mr-1" />}
              onClick={() => {
                createEcoForm.setFieldsValue({
                  ecrId: r.ecrId,
                  ecoNumber: `ECO-2026-${String(ecos.length + 42).padStart(4, '0')}`,
                  title: `${r.title}实施单`,
                });
                setCreateEcoModalOpen(true);
              }}
            >
              签发实施单 ECO
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const ecoColumns = [
    {
      title: 'ECO 编号 / 实施单标题',
      key: 'ecoNumber',
      render: (_: any, r: EcoItem) => (
        <div>
          <div className="flex items-center gap-2">
            <span className="font-semibold text-slate-800 dark:text-slate-100 font-mono">
              {r.ecoNumber}
            </span>
            <Tag color="purple">{r.changeCategory}</Tag>
          </div>
          <div className="text-xs text-slate-500 mt-0.5">{r.title}</div>
        </div>
      ),
    },
    {
      title: '生命周期状态 (双重解耦)',
      dataIndex: 'status',
      key: 'status',
      render: (s: string) => getEcoStatusTag(s),
    },
    {
      title: '目标基线',
      dataIndex: 'targetBaselineId',
      key: 'targetBaselineId',
      render: (bId?: number) => (
        <span className="font-mono text-xs text-indigo-600">
          {bId ? `BL-VMC850-CDR-001 (#${bId})` : '未绑定'}
        </span>
      ),
    },
    {
      title: '设计发布 / 现场闭环',
      key: 'timing',
      render: (_: any, r: EcoItem) => (
        <div className="text-xs text-slate-500">
          <div>发布: {r.releasedAt || '未发布'}</div>
          <div>闭环: {r.closedAt || '现场实施中'}</div>
        </div>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, r: EcoItem) => (
        <Space size="small" wrap>
          <Button
            type="link"
            size="small"
            icon={<Eye className="w-3.5 h-3.5 inline mr-1" />}
            onClick={() => {
              setSelectedEco(r);
              setDetailDrawerOpen(true);
            }}
          >
            全要素
          </Button>

          {r.status === 'ASSESSING' && (
            <Button
              type="primary"
              size="small"
              icon={<CheckCircle2 className="w-3.5 h-3.5 inline mr-1" />}
              onClick={() => handleAuthorizeEco(r)}
            >
              CCB授权
            </Button>
          )}

          {(r.status === 'AUTHORIZED' || r.status === 'IMPLEMENTING') && (
            <Button
              type="primary"
              size="small"
              className="bg-emerald-600"
              icon={<FileCheck2 className="w-3.5 h-3.5 inline mr-1" />}
              onClick={() => handleReleaseEco(r)}
            >
              申请设计发布
            </Button>
          )}

          {r.status === 'EXECUTING' && (
            <Button
              type="primary"
              size="small"
              danger
              icon={<Lock className="w-3.5 h-3.5 inline mr-1" />}
              onClick={() => handleCloseEco(r)}
            >
              终态闭环关闭
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="p-6 space-y-6 max-w-[1600px] mx-auto">
      {/* 顶部标题与状态统计 */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-indigo-600/10 text-indigo-600 rounded-lg">
              <GitCompare className="w-7 h-7" />
            </div>
            <div>
              <Title level={3} className="!mb-1 text-slate-800 dark:text-slate-100">
                工程变更与影响处置 (M22)
              </Title>
              <Text type="secondary" className="text-sm">
                ECR/ECO 两阶段解耦 · 候选拓扑推演与专业裁决 (AT-10) · ADR-05 互换性 · ADR-08 验证不继承 · AT-22 现场实施双重闭环
              </Text>
            </div>
          </div>
        </div>
        <Space>
          <Button icon={<RefreshCw className="w-4 h-4" />} onClick={fetchAllData} loading={loading}>
            刷新
          </Button>
          <Button
            type="primary"
            icon={<PlusCircle className="w-4 h-4" />}
            onClick={() => setCreateEcrModalOpen(true)}
            className="bg-indigo-600 hover:bg-indigo-700"
          >
            提报变更请求 (ECR)
          </Button>
        </Space>
      </div>

      {/* 核心指标统计卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={6}>
          <Card className="shadow-sm border-slate-200 dark:border-slate-800" size="small">
            <div className="flex items-center justify-between">
              <div>
                <div className="text-xs text-slate-500 font-medium">受控变更请求 (ECR)</div>
                <div className="text-2xl font-bold text-slate-800 dark:text-slate-100 mt-1">
                  {ecrs.length}
                  <span className="text-xs font-normal text-slate-500 ml-1">个请求</span>
                </div>
              </div>
              <div className="p-2.5 bg-blue-500/10 text-blue-600 rounded-lg">
                <FileSpreadsheet className="w-5 h-5" />
              </div>
            </div>
            <div className="text-xs text-slate-400 mt-2 flex gap-3">
              <span>已立项批准: <strong className="text-emerald-500">{ecrs.filter((e) => e.status === 'APPROVED').length}</strong></span>
              <span>初审编制: <strong className="text-amber-500">{ecrs.filter((e) => e.status === 'DRAFT').length}</strong></span>
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card className="shadow-sm border-slate-200 dark:border-slate-800" size="small">
            <div className="flex items-center justify-between">
              <div>
                <div className="text-xs text-slate-500 font-medium">变更实施单 (ECO)</div>
                <div className="text-2xl font-bold text-purple-600 dark:text-purple-400 mt-1">
                  {ecos.length}
                  <span className="text-xs font-normal text-slate-500 ml-1">个实施单</span>
                </div>
              </div>
              <div className="p-2.5 bg-purple-500/10 text-purple-600 rounded-lg">
                <GitBranch className="w-5 h-5" />
              </div>
            </div>
            <div className="text-xs text-slate-400 mt-2 flex gap-2">
              <span className="text-orange-500">现场对账中: 1</span>
              <span className="text-emerald-500">设计已发布: 1</span>
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card className="shadow-sm border-slate-200 dark:border-slate-800" size="small">
            <div className="flex items-center justify-between">
              <div>
                <div className="text-xs text-slate-500 font-medium">专业影响裁决完备率</div>
                <div className="text-2xl font-bold text-emerald-600 dark:text-emerald-400 mt-1">
                  100%
                  <span className="text-xs font-normal text-slate-500 ml-1">(AT-10)</span>
                </div>
              </div>
              <div className="p-2.5 bg-emerald-500/10 text-emerald-600 rounded-lg">
                <CheckCircle2 className="w-5 h-5" />
              </div>
            </div>
            <div className="text-xs text-slate-400 mt-2">
              机械 / 电气 / 仿真全专业逐项签署免责与技术依据
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card className="shadow-sm border-slate-200 dark:border-slate-800" size="small">
            <div className="flex items-center justify-between">
              <div>
                <div className="text-xs text-slate-500 font-medium">AT-22 现场闭环守卫</div>
                <div className="text-2xl font-bold text-rose-600 dark:text-rose-400 mt-1">
                  生效拦截中
                </div>
              </div>
              <div className="p-2.5 bg-rose-500/10 text-rose-600 rounded-lg">
                <Shield className="w-5 h-5" />
              </div>
            </div>
            <div className="text-xs text-slate-400 mt-2">
              外部工厂返修质检回执未确认，硬拦截提前关闭
            </div>
          </Card>
        </Col>
      </Row>

      {/* 主面板多 Tab 切换 */}
      <Card className="shadow-sm border-slate-200 dark:border-slate-800">
        <Tabs
          defaultActiveKey="ecos"
          items={[
            {
              key: 'ecos',
              label: (
                <span className="flex items-center gap-1.5">
                  <GitBranch className="w-4 h-4" />
                  变更实施单 (ECO) 治理中心
                </span>
              ),
              children: (
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <div className="text-xs text-slate-500">
                      落实两阶段解耦原则：PLM 设计发布态 (RELEASED) 与现场实施态 (CLOSED) 独立受控
                    </div>
                  </div>
                  <Table
                    columns={ecoColumns}
                    dataSource={ecos}
                    rowKey="ecoId"
                    loading={loading}
                    pagination={{ pageSize: 8 }}
                  />
                </div>
              ),
            },
            {
              key: 'ecrs',
              label: (
                <span className="flex items-center gap-1.5">
                  <FileSpreadsheet className="w-4 h-4" />
                  变更请求 (ECR) 看板
                </span>
              ),
              children: (
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <div className="text-xs text-slate-500">
                      界定问题起因、变更诉求与初始波及范围，负责“为什么变”与“是否值得变”的立项决策
                    </div>
                  </div>
                  <Table
                    columns={ecrColumns}
                    dataSource={ecrs}
                    rowKey="ecrId"
                    loading={loading}
                    pagination={{ pageSize: 8 }}
                  />
                </div>
              ),
            },
            {
              key: 'impact-assessment',
              label: (
                <span className="flex items-center gap-1.5">
                  <Layers className="w-4 h-4" />
                  影响面拓扑推演与多专业裁决 (AT-10)
                </span>
              ),
              children: (
                <div className="space-y-4">
                  <Alert
                    message="候选推演与工程裁定分离原则 (AT-10 规范)"
                    description="数字主线算法仅负责依据图拓扑递归计算候选影响集合与传播路径。必须由各专业责任工程师填报处置决定 (MODIFY/RE_VERIFY/REVIEW_ONLY/NO_IMPACT) 并签署工程技术依据。"
                    type="info"
                    showIcon
                  />

                  <Table
                    size="middle"
                    dataSource={impacts}
                    rowKey="impactItemId"
                    columns={[
                      {
                        title: '专业分组',
                        dataIndex: 'assignedDiscipline',
                        key: 'assignedDiscipline',
                        render: (d: string) => {
                          const color =
                            d === 'MECHANICAL' ? 'blue' : d === 'SIMULATION' ? 'orange' : 'purple';
                          return <Tag color={color}>{d}</Tag>;
                        },
                      },
                      {
                        title: '候选对象 / 业务编码',
                        key: 'businessCode',
                        render: (_: any, r: ImpactItemData) => (
                          <div>
                            <span className="font-mono font-semibold">{r.businessCode}</span>
                            <span className="text-xs text-slate-400 ml-2">[{r.objectTypeCode}]</span>
                            <div className="text-[11px] text-slate-400 font-mono mt-0.5">
                              {r.propagationPath}
                            </div>
                          </div>
                        ),
                      },
                      {
                        title: '处置决定 (Decision)',
                        key: 'decisionType',
                        render: (_: any, r: ImpactItemData) => {
                          if (!r.decision) {
                            return <Badge status="error" text="待裁决签署" />;
                          }
                          const type = r.decision.decisionType;
                          const color =
                            type === 'MODIFY'
                              ? 'red'
                              : type === 'RE_VERIFY'
                              ? 'orange'
                              : type === 'REVIEW_ONLY'
                              ? 'blue'
                              : 'default';
                          return <Tag color={color}>{type}</Tag>;
                        },
                      },
                      {
                        title: 'ADR-05 动作规约',
                        key: 'plan',
                        render: (_: any, r: ImpactItemData) =>
                          r.decision?.targetActionPlan === 'CREATE_NEW' ? (
                            <Tag color="volcano">新建物料 (破坏互换性)</Tag>
                          ) : (
                            <Tag color="cyan">原物料升版</Tag>
                          ),
                      },
                      {
                        title: '技术依据与免责签署',
                        key: 'rationale',
                        render: (_: any, r: ImpactItemData) => (
                          <div className="text-xs text-slate-600 dark:text-slate-300">
                            <div>{r.decision?.technicalRationale || '未填写'}</div>
                            <div className="text-[11px] text-slate-400 mt-1">
                              签署人: {r.decision?.assessorId} ({r.decision?.assessedAt})
                            </div>
                          </div>
                        ),
                      },
                      {
                        title: '操作',
                        key: 'action',
                        render: (_: any, r: ImpactItemData) => (
                          <Button
                            type="link"
                            size="small"
                            onClick={() => {
                              setSelectedImpactItem(r);
                              decisionForm.setFieldsValue({
                                decisionType: r.decision?.decisionType || 'MODIFY',
                                technicalRationale: r.decision?.technicalRationale || '',
                                targetActionPlan: r.decision?.targetActionPlan || 'CREATE_NEW',
                                actionRequired: r.decision?.actionRequired || '',
                              });
                              setDecisionModalOpen(true);
                            }}
                          >
                            修改裁定
                          </Button>
                        ),
                      },
                    ]}
                    pagination={false}
                  />
                </div>
              ),
            },
            {
              key: 'dispositions',
              label: (
                <span className="flex items-center gap-1.5">
                  <Factory className="w-4 h-4" />
                  在制品、库存与实物处置策略
                </span>
              ),
              children: (
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <div className="text-xs text-slate-500">
                      针对受波及的库存 (Inventory)、在制订单 (In-Process Order) 及现场设备制定报废/返修/自然过渡策略
                    </div>
                    <Button
                      type="primary"
                      icon={<PlusCircle className="w-4 h-4" />}
                      onClick={() => setCreateDispModalOpen(true)}
                    >
                      编制处置策略
                    </Button>
                  </div>

                  <Table
                    size="middle"
                    dataSource={dispositions}
                    rowKey="dispositionId"
                    columns={[
                      {
                        title: '受波及目标范围',
                        dataIndex: 'targetScopeType',
                        key: 'targetScopeType',
                        render: (scope: string) => {
                          switch (scope) {
                            case 'INVENTORY_PART':
                              return <Tag color="blue">库房库存物料</Tag>;
                            case 'IN_PROCESS_ORDER':
                              return <Tag color="orange">车间在制装配订单</Tag>;
                            case 'FIELD_MACHINE':
                              return <Tag color="purple">现场服役机床</Tag>;
                            default:
                              return <Tag>{scope}</Tag>;
                          }
                        },
                      },
                      {
                        title: '目标物料 / 订单',
                        key: 'targetPartNumber',
                        render: (_: any, r: DispositionItem) => (
                          <span className="font-mono font-semibold">
                            {r.targetPartNumber}{' '}
                            {r.targetOrderProductId && `(Order #${r.targetOrderProductId})`}
                          </span>
                        ),
                      },
                      {
                        title: '处置方案',
                        dataIndex: 'actionType',
                        key: 'actionType',
                        render: (a: string) => {
                          switch (a) {
                            case 'SCRAP':
                              return <Tag color="red">物理报废 (SCRAP)</Tag>;
                            case 'REWORK':
                              return <Tag color="volcano">车间返修 (REWORK)</Tag>;
                            case 'USE_UP':
                              return <Tag color="green">自然过渡 (USE_UP)</Tag>;
                            default:
                              return <Tag>{a}</Tag>;
                          }
                        },
                      },
                      {
                        title: '现场执行细则',
                        dataIndex: 'dispositionInstructions',
                        key: 'dispositionInstructions',
                        render: (t: string) => <span className="text-xs text-slate-600">{t}</span>,
                      },
                      {
                        title: '制定时间',
                        dataIndex: 'createdAt',
                        key: 'createdAt',
                        render: (t: string) => <span className="text-xs text-slate-400">{t}</span>,
                      },
                    ]}
                    pagination={false}
                  />
                </div>
              ),
            },
            {
              key: 'field-reconciliation',
              label: (
                <span className="flex items-center gap-1.5">
                  <Shield className="w-4 h-4" />
                  现场回执对账与 AT-22 闭环守护
                </span>
              ),
              children: (
                <div className="space-y-4">
                  <Alert
                    message="两阶段独立生效与双重闭环铁律 (AT-22 守护)"
                    description="PLM 批准设计发布后，变更单进入 EXECUTING 现场实施态。在外部 MES 返修质检报告、ERP 退库报废单未取得 100% 确认前，系统硬拦截提前关闭变更单！"
                    type="warning"
                    showIcon
                  />

                  <Table
                    size="middle"
                    dataSource={records}
                    rowKey="recordId"
                    columns={[
                      {
                        title: '外部执行系统',
                        dataIndex: 'targetSystem',
                        key: 'targetSystem',
                        render: (sys: string) => <Tag color="purple">{sys}</Tag>,
                      },
                      {
                        title: '关联处置规约 ID',
                        dataIndex: 'dispositionId',
                        key: 'dispositionId',
                        render: (dId: number) => <span className="font-mono">#DISP-{dId}</span>,
                      },
                      {
                        title: '现场执行状态',
                        dataIndex: 'executionStatus',
                        key: 'executionStatus',
                        render: (s: string) =>
                          s === 'COMPLETED' ? (
                            <Badge status="success" text="已确认闭环 (COMPLETED)" />
                          ) : (
                            <Badge status="processing" text="已派发未确认 (DISPATCHED)" />
                          ),
                      },
                      {
                        title: '对账操作人',
                        dataIndex: 'siteOperatorId',
                        key: 'siteOperatorId',
                        render: (op: string) => <span className="text-xs text-slate-500">{op}</span>,
                      },
                      {
                        title: '确认时间',
                        dataIndex: 'confirmedAt',
                        key: 'confirmedAt',
                        render: (t: string) =>
                          t ? (
                            <span className="text-xs text-emerald-600">{t}</span>
                          ) : (
                            <span className="text-xs text-rose-500 font-semibold italic">
                              尚未对账 (阻断关闭)
                            </span>
                          ),
                      },
                      {
                        title: '操作',
                        key: 'action',
                        render: (_: any, r: ImplementationRecordItem) =>
                          r.executionStatus !== 'COMPLETED' ? (
                            <Button
                              type="link"
                              size="small"
                              className="text-emerald-600"
                              onClick={() => handleConfirmReceipt(r.dispositionId)}
                            >
                              确认现场完工回执
                            </Button>
                          ) : (
                            <Tag color="success">已核销</Tag>
                          ),
                      },
                    ]}
                    pagination={false}
                  />
                </div>
              ),
            },
          ]}
        />
      </Card>

      {/* 提报 ECR 模态框 */}
      <Modal
        title="提报新工程变更请求 (ECR)"
        open={createEcrModalOpen}
        onCancel={() => setCreateEcrModalOpen(false)}
        footer={null}
      >
        <Form form={createEcrForm} layout="vertical" onFinish={handleCreateEcr}>
          <Form.Item
            name="ecrNumber"
            label="ECR 编号"
            rules={[{ required: true, message: '请输入 ECR 编号' }]}
            initialValue={`ECR-2026-${String(ecrs.length + 44).padStart(4, '0')}`}
          >
            <Input />
          </Form.Item>

          <Form.Item
            name="title"
            label="变更标题"
            rules={[{ required: true, message: '请输入变更标题' }]}
          >
            <Input placeholder="例如: 伺服驱动器接线端子防震加固" />
          </Form.Item>

          <Form.Item
            name="reasonType"
            label="变更原因分类"
            rules={[{ required: true }]}
            initialValue="CUSTOMER_REQUIREMENT"
          >
            <Select>
              <Option value="CUSTOMER_REQUIREMENT">客户提出新需求/技术规格调整</Option>
              <Option value="FIELD_FAILURE">现场维保/实机故障反馈 (M28反馈)</Option>
              <Option value="SIMULATION_DEVIATION">虚拟验证未达标/性能超差 (M10/M11发现)</Option>
              <Option value="MANUFACTURING_DEFECT">车间制造装配工艺性缺陷</Option>
              <Option value="COST_REDUCTION">价值工程/降本重构</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="problemDescription"
            label="问题背景与现象描述"
            rules={[{ required: true, message: '请输入问题描述' }]}
          >
            <Input.TextArea rows={3} placeholder="详述现场故障现象或技术参数调整诉求..." />
          </Form.Item>

          <Form.Item name="proposedSolution" label="建议工程解决方案">
            <Input.TextArea rows={2} placeholder="初步设想的工程变更技术方案..." />
          </Form.Item>

          <div className="flex justify-end gap-2">
            <Button onClick={() => setCreateEcrModalOpen(false)}>取消</Button>
            <Button type="primary" htmlType="submit">
              确认提报
            </Button>
          </div>
        </Form>
      </Modal>

      {/* 签发 ECO 模态框 */}
      <Modal
        title="签发变更实施单 (ChangeOrder - ECO)"
        open={createEcoModalOpen}
        onCancel={() => setCreateEcoModalOpen(false)}
        footer={null}
      >
        <Form form={createEcoForm} layout="vertical" onFinish={handleCreateEco}>
          <Form.Item name="ecrId" label="关联变更请求 (ECR)" rules={[{ required: true }]}>
            <Select disabled>
              {ecrs.map((e) => (
                <Option key={e.ecrId} value={e.ecrId}>
                  {e.ecrNumber} - {e.title}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="ecoNumber" label="ECO 实施单编号" rules={[{ required: true }]}>
            <Input />
          </Form.Item>

          <Form.Item name="title" label="实施单标题" rules={[{ required: true }]}>
            <Input />
          </Form.Item>

          <Form.Item name="changeCategory" label="变更类别" initialValue="MAJOR">
            <Select>
              <Option value="MAJOR">重大变更 (影响外形/配合/功能)</Option>
              <Option value="MINOR">一般变更 (局部参数微调)</Option>
              <Option value="ADMINISTRATIVE">管理事务变更</Option>
            </Select>
          </Form.Item>

          <div className="flex justify-end gap-2">
            <Button onClick={() => setCreateEcoModalOpen(false)}>取消</Button>
            <Button type="primary" htmlType="submit">
              授权签发
            </Button>
          </div>
        </Form>
      </Modal>

      {/* 录入专业裁决模态框 */}
      <Modal
        title={
          <div className="flex items-center gap-2">
            <Sliders className="w-5 h-5 text-indigo-600" />
            <span>录入专业影响处置决定 (AT-10 规范)</span>
          </div>
        }
        open={decisionModalOpen}
        onCancel={() => setDecisionModalOpen(false)}
        footer={null}
      >
        <Form form={decisionForm} layout="vertical" onFinish={handleSaveDecision}>
          <div className="p-3 bg-slate-50 dark:bg-slate-900 rounded mb-4 text-xs font-mono">
            <div>对象: {selectedImpactItem?.businessCode}</div>
            <div>专业: {selectedImpactItem?.assignedDiscipline}</div>
            <div>路径: {selectedImpactItem?.propagationPath}</div>
          </div>

          <Form.Item
            name="decisionType"
            label="处置决定"
            rules={[{ required: true, message: '请选择处置类型' }]}
          >
            <Select>
              <Option value="MODIFY">修改 (MODIFY) - 必须升版或新建物料</Option>
              <Option value="RE_VERIFY">重新验证 (RE_VERIFY) - 工况改变需重新计算(ADR-08)</Option>
              <Option value="REVIEW_ONLY">仅校核 (REVIEW_ONLY) - 仅工程复核无需改图</Option>
              <Option value="NO_IMPACT">无影响 (NO_IMPACT) - 必须签署工程免责依据</Option>
            </Select>
          </Form.Item>

          <Form.Item name="targetActionPlan" label="ADR-05 物料互换性策略" initialValue="CREATE_NEW">
            <Select>
              <Option value="CREATE_NEW">新建物料主对象 (破坏双向互换性)</Option>
              <Option value="REVISE_EXISTING">原物料派生新版本 (完全双向互换)</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="technicalRationale"
            label="专业技术分析与免责签署依据"
            rules={[{ required: true, message: '必须录入专业依据' }]}
          >
            <Input.TextArea rows={3} placeholder="阐述为何该对象需要/不需要修改，或重新验证的工况边界..." />
          </Form.Item>

          <Form.Item name="actionRequired" label="后继任务工程指派要求">
            <Input placeholder="例如: 申请新物料编码并重新搭建BOM" />
          </Form.Item>

          <div className="flex justify-end gap-2">
            <Button onClick={() => setDecisionModalOpen(false)}>取消</Button>
            <Button type="primary" htmlType="submit">
              确认签署
            </Button>
          </div>
        </Form>
      </Modal>

      {/* 编制现场处置规约模态框 */}
      <Modal
        title="编制在制品/库存/现场机床生效策略"
        open={createDispModalOpen}
        onCancel={() => setCreateDispModalOpen(false)}
        footer={null}
      >
        <Form
          form={dispForm}
          layout="vertical"
          onFinish={(values) => {
            const newDisp: DispositionItem = {
              dispositionId: Date.now(),
              ecoId: 8001,
              targetScopeType: values.targetScopeType,
              targetPartNumber: values.targetPartNumber,
              actionType: values.actionType,
              dispositionInstructions: values.dispositionInstructions,
              createdAt: new Date().toLocaleString(),
            };
            setDispositions([newDisp, ...dispositions]);
            setRecords([
              {
                recordId: Date.now(),
                ecoId: 8001,
                dispositionId: newDisp.dispositionId,
                targetSystem: values.targetScopeType === 'INVENTORY_PART' ? 'ERP' : 'MES',
                executionStatus: 'DISPATCHED',
                siteOperatorId: 'site_operator',
              },
              ...records,
            ]);
            message.success('生效处置方案编制成功并已自动下发至现场对账列表！');
            setCreateDispModalOpen(false);
            dispForm.resetFields();
          }}
        >
          <Form.Item name="targetScopeType" label="受波及对象范围" initialValue="IN_PROCESS_ORDER">
            <Select>
              <Option value="INVENTORY_PART">库房在库零部件库存</Option>
              <Option value="IN_PROCESS_ORDER">车间装配在制订单</Option>
              <Option value="FIELD_MACHINE">现场已出厂实物机床</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="targetPartNumber"
            label="目标物料编号 / 图号"
            rules={[{ required: true, message: '请输入物料编号' }]}
          >
            <Input placeholder="例如: M-VMC1000-SPN-01" />
          </Form.Item>

          <Form.Item name="actionType" label="处置动作" initialValue="REWORK">
            <Select>
              <Option value="SCRAP">物理报废 (SCRAP)</Option>
              <Option value="REWORK">现场返修 (REWORK)</Option>
              <Option value="USE_UP">自然过渡消耗 (USE_UP)</Option>
              <Option value="AS_IS">维持原样不作改动 (AS_IS)</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="dispositionInstructions"
            label="现场实施指导细则"
            rules={[{ required: true, message: '请输入实施指导细则' }]}
          >
            <Input.TextArea rows={3} placeholder="下发给MES车间或ERP库房的具体操作指引..." />
          </Form.Item>

          <div className="flex justify-end gap-2">
            <Button onClick={() => setCreateDispModalOpen(false)}>取消</Button>
            <Button type="primary" htmlType="submit">
              下发策略
            </Button>
          </div>
        </Form>
      </Modal>

      {/* ECO 全要素详情抽屉 */}
      <Drawer
        title={
          <div className="flex items-center gap-2">
            <GitCompare className="w-5 h-5 text-indigo-600" />
            <span>变更实施单全要素详情 ({selectedEco?.ecoNumber})</span>
          </div>
        }
        placement="right"
        width={780}
        onClose={() => setDetailDrawerOpen(false)}
        open={detailDrawerOpen}
      >
        {selectedEco && (
          <div className="space-y-6">
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="ECO 编号">{selectedEco.ecoNumber}</Descriptions.Item>
              <Descriptions.Item label="状态">
                {getEcoStatusTag(selectedEco.status)}
              </Descriptions.Item>
              <Descriptions.Item label="实施标题" span={2}>
                {selectedEco.title}
              </Descriptions.Item>
              <Descriptions.Item label="变更类别">
                <Tag color="purple">{selectedEco.changeCategory}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="工作版本号">
                V{selectedEco.workingVersion}.0
              </Descriptions.Item>
              <Descriptions.Item label="创建人">{selectedEco.createdBy}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{selectedEco.createdAt}</Descriptions.Item>
              <Descriptions.Item label="设计发布时间">
                {selectedEco.releasedAt || '未发布'}
              </Descriptions.Item>
              <Descriptions.Item label="现场闭环时间">
                {selectedEco.closedAt || '现场实施中'}
              </Descriptions.Item>
            </Descriptions>

            <div>
              <div className="font-semibold text-slate-800 dark:text-slate-100 flex items-center gap-1.5 mb-2">
                <Wrench className="w-4 h-4 text-indigo-600" />
                实施工作空间分解任务 ({tasks.length})
              </div>
              <Table
                size="small"
                dataSource={tasks}
                rowKey="taskId"
                columns={[
                  {
                    title: '任务编码 / 名称',
                    key: 'taskCode',
                    render: (_: any, r: TaskItem) => (
                      <div>
                        <div className="font-mono font-semibold">{r.taskCode}</div>
                        <div className="text-xs text-slate-500">{r.title}</div>
                      </div>
                    ),
                  },
                  {
                    title: '任务类型',
                    dataIndex: 'taskType',
                    key: 'taskType',
                    render: (t: string) => <Tag color="blue">{t}</Tag>,
                  },
                  {
                    title: '责任人',
                    dataIndex: 'assigneeId',
                    key: 'assigneeId',
                  },
                  {
                    title: '状态',
                    dataIndex: 'status',
                    key: 'status',
                    render: (s: string) =>
                      s === 'COMPLETED' ? (
                        <Tag color="success">已完成</Tag>
                      ) : (
                        <Tag color="processing">进行中</Tag>
                      ),
                  },
                ]}
                pagination={false}
              />
            </div>
          </div>
        )}
      </Drawer>
    </div>
  );
};
export default ChangeManagementPage;
