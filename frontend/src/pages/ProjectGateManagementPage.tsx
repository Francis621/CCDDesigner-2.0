import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Tag,
  Button,
  Table,
  notification,
  Badge,
  Modal,
  Descriptions,
  Tabs,
  Alert,
  Form,
  Input,
  Select,
  Timeline,
  Divider,
} from 'antd';
import {
  CheckCircle2,
  AlertTriangle,
  XCircle,
  ShieldCheck,
  ShieldAlert,
  Clock,
  Activity,
  Layers,
  FileCheck2,
  GitBranch,
  FileText,
  Cpu,
  RefreshCw,
  PlusCircle,
  CheckSquare,
  Network,
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { apiClient } from '../infra/api/httpClient';

// ==========================================
// 领域实体与 DTO 定义
// ==========================================

interface ProjectEntity {
  projectId: number;
  projectCode: string;
  name: string;
  projectType: string;
  managerId: string;
  chiefEngineerId: string;
  currentStageId: number;
  status: string;
}

interface StageEntity {
  stageId: number;
  projectId: number;
  stageCode: string;
  name: string;
  sequenceNo: number;
  status: string;
  plannedStartDate: string;
  plannedEndDate: string;
}

interface GateEntity {
  gateId: number;
  stageId: number;
  gateCode: string;
  name: string;
  description: string;
  reviewWorkflowDef?: string;
  status: string;
}

interface CriterionResult {
  criterionCode: string;
  name: string;
  passed: boolean;
  actualValue: string;
  message: string;
  isBlocking: boolean;
}

interface EvidenceGap {
  reqRevisionId: number;
  reqCode: string;
  caseCode: string;
  currentStatus: string;
  reason: string;
}

interface GatePreCheckReport {
  gateId: number;
  gateCode: string;
  gateName: string;
  overallPassed: boolean;
  blockerCount: number;
  evaluatedAt: string;
  criterionResults: CriterionResult[];
  missingEvidenceGaps: EvidenceGap[];
}

interface ActionItem {
  actionItemId: number;
  decisionId: number;
  title: string;
  description: string;
  ownerId: string;
  approverId: string;
  dueDate: string;
  status: string;
  resolutionSummary?: string;
}

interface WbsNode {
  wbsNodeId: number;
  projectId: number;
  parentNodeId?: number;
  wbsCode: string;
  name: string;
  nodeLevel: number;
}

interface Task {
  taskId: number;
  wbsNodeId: number;
  stageId: number;
  taskCode: string;
  name: string;
  assigneeId: string;
  plannedStartDate: string;
  plannedEndDate: string;
  durationDays: number;
  progressPercent: number;
  status: string;
}

interface TaskDependency {
  predecessorTaskId: number;
  successorTaskId: number;
  depType: string;
  lagDays?: number;
}

interface TaskScheduleMetric {
  taskId: number;
  taskCode: string;
  taskName: string;
  durationDays: number;
  earlyStartDay: number;
  earlyFinishDay: number;
  lateStartDay: number;
  lateFinishDay: number;
  totalFloatDays: number;
  isCritical: boolean;
}

interface CpmAnalysisReport {
  projectId: number;
  criticalPathLengthDays: number;
  criticalPathTaskCodes: string[];
  taskMetrics: TaskScheduleMetric[];
}

interface DeliverableSubmission {
  submissionId: number;
  delivReqId: number;
  revisionId: number;
  artifactHash: string;
  submissionNotes: string;
  isLatest: boolean;
  submittedBy: string;
  submittedAt?: string;
}

interface DeliverableRequirement {
  delivReqId: number;
  taskId: number;
  requirementCode: string;
  name: string;
  deliverableType: string;
  isMandatory: boolean;
  targetSecurityLevel: string;
}

interface TaskDeliverableGroup {
  requirement: DeliverableRequirement;
  submissions: DeliverableSubmission[];
}

export const ProjectGateManagementPage: React.FC = () => {
  const [loading, setLoading] = useState<boolean>(false);
  const [project, setProject] = useState<ProjectEntity | null>(null);
  const [stages, setStages] = useState<StageEntity[]>([]);
  const [gates, setGates] = useState<GateEntity[]>([]);
  const [activeGate, setActiveGate] = useState<GateEntity | null>(null);

  // 阶段门预检报告
  const [preCheckReport, setPreCheckReport] = useState<GatePreCheckReport | null>(null);
  const [actionItems, setActionItems] = useState<ActionItem[]>([]);

  // WBS 与 CPM
  const [, setWbsNodes] = useState<WbsNode[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [dependencies, setDependencies] = useState<TaskDependency[]>([]);
  const [cpmReport, setCpmReport] = useState<CpmAnalysisReport | null>(null);

  // 交付物
  const [selectedTaskId, setSelectedTaskId] = useState<number>(1003);
  const [taskDeliverables, setTaskDeliverables] = useState<TaskDeliverableGroup[]>([]);

  // 弹窗与表单
  const [decisionModalOpen, setDecisionModalOpen] = useState<boolean>(false);
  const [submitDeliverableModalOpen, setSubmitDeliverableModalOpen] = useState<boolean>(false);
  const [closeActionModalOpen, setCloseActionModalOpen] = useState<boolean>(false);
  const [currentActionItem, setCurrentActionItem] = useState<ActionItem | null>(null);
  const [targetReqId, setTargetReqId] = useState<number | null>(null);

  const [decisionForm] = Form.useForm();
  const [deliverableForm] = Form.useForm();
  const [closeActionForm] = Form.useForm();

  // 1. 初始化加载项目全景与任务数据
  const loadOverview = useCallback(async () => {
    try {
      setLoading(true);
      const res: any = await apiClient.get('/projects/1001/gate-overview');
      if (res.data) {
        setProject(res.data.project);
        setStages(res.data.stages || []);
        setGates(res.data.gates || []);
        // 默认选中 TR3 或当前活动阶段门
        const tr3 = (res.data.gates || []).find((g: GateEntity) => g.gateCode === 'TR3');
        const defaultGate = tr3 || res.data.currentGate || res.data.gates[0];
        setActiveGate(defaultGate);
        if (defaultGate) {
          fetchPreCheck(defaultGate.gateId);
          fetchActionItems(defaultGate.gateId);
        }
      }

      // 加载 WBS 与 任务
      const wbsRes: any = await apiClient.get('/projects/1001/wbs-tasks');
      if (wbsRes.data) {
        setWbsNodes(wbsRes.data.wbsNodes || []);
        setTasks(wbsRes.data.tasks || []);
        setDependencies(wbsRes.data.dependencies || []);
      }

      // 执行 CPM 分析
      const cpmRes: any = await apiClient.get('/projects/1001/cpm-analysis');
      if (cpmRes.data) {
        setCpmReport(cpmRes.data);
      }
    } catch (e: any) {
      notification.error({
        message: '数据加载失败',
        description: e.message || '网络或后端服务异常',
      });
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchPreCheck = async (gateId: number) => {
    try {
      const res: any = await apiClient.get(`/projects/1001/gates/${gateId}/pre-check`);
      if (res.data) {
        setPreCheckReport(res.data);
      }
    } catch (e: any) {
      notification.warning({ message: '准入预检查询异常', description: e.message });
    }
  };

  const fetchActionItems = async (gateId: number) => {
    try {
      const res: any = await apiClient.get(`/projects/1001/gates/${gateId}/action-items`);
      if (res.data) {
        setActionItems(res.data);
      }
    } catch (e: any) {
      notification.warning({ message: '行动项查询异常', description: e.message });
    }
  };

  const fetchTaskDeliverables = useCallback(async (taskId: number) => {
    try {
      const res: any = await apiClient.get(`/projects/1001/tasks/${taskId}/deliverables`);
      if (res.data) {
        setTaskDeliverables(res.data);
      }
    } catch (e: any) {
      console.error(e);
    }
  }, []);

  useEffect(() => {
    loadOverview();
  }, [loadOverview]);

  useEffect(() => {
    if (selectedTaskId) {
      fetchTaskDeliverables(selectedTaskId);
    }
  }, [selectedTaskId, fetchTaskDeliverables]);

  // 阶段门切换处理
  const handleSelectGate = (gate: GateEntity) => {
    setActiveGate(gate);
    fetchPreCheck(gate.gateId);
    fetchActionItems(gate.gateId);
  };

  // 签署阶段门决策
  const handleRecordDecision = async () => {
    try {
      const values = await decisionForm.validateFields();
      if (!activeGate) return;

      const payload = {
        decisionType: values.decisionType,
        decisionNotes: values.decisionNotes,
        allowedScope: values.allowedScope,
        evaluatedBaselineId: 8802,
        actionItems: values.actionTitle
          ? [
              {
                title: values.actionTitle,
                description: values.actionDesc || '阶段门决策签署附带整改项',
                ownerId: values.actionOwner || 'ENG-LIU-TEST',
                approverId: 'CHIEF-ENG-WANG',
                dueDate: values.actionDueDate || '2026-10-25',
              },
            ]
          : [],
      };

      const res: any = await apiClient.post(
        `/projects/1001/gates/${activeGate.gateId}/decisions`,
        payload
      );

      if (res.code === 200) {
        notification.success({
          message: '阶段门评审决策签署完成',
          description: `决策编号: ${res.data.decisionId}，结论: ${res.data.decisionType}`,
        });
        confetti({ particleCount: 80, spread: 60, origin: { y: 0.6 } });
        setDecisionModalOpen(false);
        decisionForm.resetFields();
        loadOverview();
      } else {
        notification.error({
          message: '签署被系统拦截',
          description: res.message || '阶段门硬性准入或防假达标规约拦截',
        });
      }
    } catch (e: any) {
      notification.error({
        message: '签署被系统拦截 (规约约束)',
        description: e.response?.data?.message || e.message || '操作失败',
      });
    }
  };

  // 闭环行动项
  const handleCloseActionItem = async () => {
    try {
      const values = await closeActionForm.validateFields();
      if (!activeGate || !currentActionItem) return;

      const res: any = await apiClient.post(
        `/projects/1001/gates/${activeGate.gateId}/action-items/${currentActionItem.actionItemId}/close`,
        { notes: values.notes }
      );

      if (res.code === 200) {
        notification.success({
          message: '行动项成功闭环',
          description: `行动项 #${currentActionItem.actionItemId} 已验证合格并关闭`,
        });
        setCloseActionModalOpen(false);
        closeActionForm.resetFields();
        fetchActionItems(activeGate.gateId);
      }
    } catch (e: any) {
      notification.error({ message: '行动项闭环失败', description: e.message });
    }
  };

  // 提审交付物新版本
  const handleSubmitDeliverable = async () => {
    try {
      const values = await deliverableForm.validateFields();
      if (!targetReqId) return;

      const res: any = await apiClient.post(
        `/projects/1001/tasks/${selectedTaskId}/deliverables/${targetReqId}/submit`,
        {
          notes: values.notes,
          user: 'ENG-ZHOU (主管结构工程师)',
        }
      );

      if (res.code === 200) {
        notification.success({
          message: '交付物版本提审成功',
          description: `新版本已生成，历史版本已安全归档 (SUPERSEDED)，哈希: ${res.data.artifactHash.slice(0, 12)}...`,
        });
        setSubmitDeliverableModalOpen(false);
        deliverableForm.resetFields();
        fetchTaskDeliverables(selectedTaskId);
        if (activeGate) fetchPreCheck(activeGate.gateId);
      }
    } catch (e: any) {
      notification.error({ message: '交付物提审失败', description: e.message });
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* 顶部机床项目全景横幅 */}
      <Card
        loading={loading}
        className="shadow-sm border border-slate-200 bg-gradient-to-r from-slate-900 via-slate-800 to-indigo-950 text-white rounded-xl overflow-hidden"
        bodyStyle={{ padding: '24px 32px' }}
      >
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <span className="px-2.5 py-1 bg-blue-500/30 border border-blue-400/40 text-blue-300 text-xs font-mono rounded">
                {project?.projectCode || 'PRJ-VMC850-5AXIS'}
              </span>
              <Tag color="cyan" className="font-semibold px-2 py-0.5">
                {project?.projectType === 'PLATFORM' ? '平台级核心机型' : '定制衍生机型'}
              </Tag>
              <Tag color="green">正向研制阶段活跃中</Tag>
            </div>
            <h1 className="text-2xl font-bold tracking-tight text-white m-0">
              {project?.name || '高刚度立式五轴加工中心正向研制项目 (平台级机型)'}
            </h1>
            <p className="text-slate-300 text-sm m-0 max-w-3xl leading-relaxed">
              严格执行系统架构 SysML v2 规范、多物理场仿真指标分解、150% Super BOM 规则约束以及 AT-15 准入阶段门一票否决机制。
            </p>
          </div>

          <div className="flex items-center gap-4 bg-white/10 p-4 rounded-xl backdrop-blur-md border border-white/10">
            <div className="text-center px-3 border-r border-white/20">
              <div className="text-xs text-slate-400">项目总师</div>
              <div className="text-sm font-semibold text-white mt-1">王总师 (Chief Eng)</div>
            </div>
            <div className="text-center px-3 border-r border-white/20">
              <div className="text-xs text-slate-400">关键路径工期</div>
              <div className="text-sm font-semibold text-amber-300 mt-1">
                {cpmReport?.criticalPathLengthDays || 210} 天 (CPM)
              </div>
            </div>
            <div className="text-center px-3">
              <div className="text-xs text-slate-400">当前阶段</div>
              <div className="text-sm font-semibold text-emerald-400 mt-1">
                {stages.find((s) => s.status === 'IN_PROGRESS')?.name || 'STAGE-3 详细工程设计'}
              </div>
            </div>
          </div>
        </div>

        {/* TR 阶段门甘特时序线 */}
        <Divider className="my-5 border-white/10" />
        <div>
          <div className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-3 flex items-center gap-2">
            <Cpu className="w-4 h-4 text-blue-400" />
            机床研发全生命周期 TR 阶段门网络 (点击切换当前审查门)
          </div>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {gates.map((g) => {
              const isSelected = activeGate?.gateId === g.gateId;
              const isDecided = g.status.includes('DECIDED');
              const isReady = g.status === 'READY';

              return (
                <div
                  key={g.gateId}
                  onClick={() => handleSelectGate(g)}
                  className={`cursor-pointer p-3.5 rounded-lg border transition-all ${
                    isSelected
                      ? 'bg-blue-600/30 border-blue-400 shadow-lg shadow-blue-500/20'
                      : 'bg-white/5 border-white/10 hover:bg-white/10'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono font-bold text-sm text-white">{g.gateCode}</span>
                    <Badge
                      status={isDecided ? 'success' : isReady ? 'processing' : 'default'}
                      text={
                        <span className="text-xs text-slate-300">
                          {g.status === 'READY'
                            ? '待准入审查'
                            : g.status === 'DECIDED_PASS'
                            ? '已决策 PASS'
                            : g.status === 'DECIDED_CONDITIONAL'
                            ? '条件性放行'
                            : g.status === 'REWORK'
                            ? '返工整改'
                            : '已完成'}
                        </span>
                      }
                    />
                  </div>
                  <div className="text-xs font-medium text-slate-200 mt-1 line-clamp-1">{g.name}</div>
                  <div className="text-[11px] text-slate-400 mt-1 line-clamp-1">{g.description}</div>
                </div>
              );
            })}
          </div>
        </div>
      </Card>

      {/* 核心工作台功能区 */}
      <Tabs
        defaultActiveKey="gate-review"
        type="card"
        className="bg-white rounded-xl p-4 shadow-sm border border-slate-200"
        items={[
          {
            key: 'gate-review',
            label: (
              <span className="flex items-center gap-2 font-medium">
                <ShieldCheck className="w-4 h-4 text-blue-600" />
                阶段门准入核验与决策中心 (AT-15 守护)
              </span>
            ),
            children: (
              <div className="space-y-6 pt-2">
                {/* 阶段门审查总述与准入拦截报警 */}
                <div className="flex flex-col md:flex-row gap-6 items-start">
                  <div className="w-full md:w-2/3 space-y-4">
                    <Card
                      title={
                        <div className="flex items-center justify-between">
                          <span className="text-base font-bold text-slate-800 flex items-center gap-2">
                            <Activity className="w-4 h-4 text-blue-600" />
                            {activeGate?.name || 'TR3 关键设计评审门 (CDR)'} - 审查基准
                          </span>
                          <Tag color="blue" className="font-mono">
                            {activeGate?.gateCode || 'TR3'}
                          </Tag>
                        </div>
                      }
                      bordered
                      className="rounded-lg shadow-none"
                    >
                      <Descriptions column={2} size="small" bordered>
                        <Descriptions.Item label="归属研发阶段">
                          STAGE-3 详细工程设计与工艺BOM编制
                        </Descriptions.Item>
                        <Descriptions.Item label="当前门禁状态">
                          <Tag
                            color={
                              activeGate?.status === 'READY'
                                ? 'processing'
                                : activeGate?.status === 'DECIDED_CONDITIONAL'
                                ? 'warning'
                                : 'success'
                            }
                          >
                            {activeGate?.status}
                          </Tag>
                        </Descriptions.Item>
                        <Descriptions.Item label="评估工程基线" span={2}>
                          <span className="font-mono text-xs font-bold text-indigo-700">
                            BL-VMC850-STAGE3-REV2 (MD5: 9a7b... 快照已锁定)
                          </span>
                        </Descriptions.Item>
                        <Descriptions.Item label="评审业务范围" span={2}>
                          {activeGate?.description ||
                            '审查直联电主轴详细模型、铸造床身有限元热平衡、EBOM/MBOM 消耗守恒及激光动态定位精度'}
                        </Descriptions.Item>
                      </Descriptions>
                    </Card>

                    {/* AT-15 守护规则核验列表 */}
                    <Card
                      title={
                        <div className="flex items-center justify-between">
                          <span className="text-base font-bold text-slate-800 flex items-center gap-2">
                            <ShieldAlert className="w-4 h-4 text-amber-600" />
                            AT-15 准入核验三原则 (Automated Gate Entry Criteria)
                          </span>
                          <Button
                            size="small"
                            icon={<RefreshCw className="w-3.5 h-3.5" />}
                            onClick={() => activeGate && fetchPreCheck(activeGate.gateId)}
                          >
                            重新评估
                          </Button>
                        </div>
                      }
                      bordered
                      className="rounded-lg shadow-none"
                    >
                      <Table
                        dataSource={preCheckReport?.criterionResults || []}
                        rowKey="criterionCode"
                        pagination={false}
                        size="middle"
                        columns={[
                          {
                            title: '准入守则',
                            dataIndex: 'name',
                            key: 'name',
                            render: (text: string, row: CriterionResult) => (
                              <div>
                                <div className="font-semibold text-slate-800 text-sm">{text}</div>
                                <div className="text-xs font-mono text-slate-400">{row.criterionCode}</div>
                              </div>
                            ),
                          },
                          {
                            title: '实测状态 / 达成值',
                            dataIndex: 'actualValue',
                            key: 'actualValue',
                            render: (val: string) => (
                              <span className="font-mono text-xs font-medium bg-slate-100 px-2 py-1 rounded">
                                {val}
                              </span>
                            ),
                          },
                          {
                            title: '规约裁决',
                            dataIndex: 'passed',
                            key: 'passed',
                            width: 110,
                            render: (passed: boolean) =>
                              passed ? (
                                <Tag color="success" icon={<CheckCircle2 className="w-3 h-3 inline mr-1" />}>
                                  合规达标
                                </Tag>
                              ) : (
                                <Tag color="error" icon={<XCircle className="w-3 h-3 inline mr-1" />}>
                                  一票否决
                                </Tag>
                              ),
                          },
                          {
                            title: '核验诊断结论',
                            dataIndex: 'message',
                            key: 'message',
                            render: (msg: string, row: CriterionResult) => (
                              <span className={row.passed ? 'text-slate-600 text-xs' : 'text-red-600 text-xs font-semibold'}>
                                {msg}
                              </span>
                            ),
                          },
                        ]}
                      />

                      {/* 证据缺口告警卡 */}
                      {preCheckReport?.missingEvidenceGaps && preCheckReport.missingEvidenceGaps.length > 0 && (
                        <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-xs space-y-1">
                          <div className="font-bold text-red-800 flex items-center gap-1.5">
                            <AlertTriangle className="w-4 h-4 text-red-600" />
                            规约严厉拦截：检测到关键验证指标证据缺口 (Missing/Inconclusive Evidence)
                          </div>
                          {preCheckReport.missingEvidenceGaps.map((gap, idx) => (
                            <div key={idx} className="text-red-700 pl-5">
                              • 用例 <span className="font-mono font-bold">[{gap.caseCode}]</span> 状态为{' '}
                              <Tag color="error">{gap.currentStatus}</Tag>：{gap.reason}
                            </div>
                          ))}
                        </div>
                      )}
                    </Card>
                  </div>

                  {/* 右侧：决策控制台与行动项闭环 */}
                  <div className="w-full md:w-1/3 space-y-4">
                    <Card
                      title={
                        <span className="text-base font-bold text-slate-800 flex items-center gap-2">
                          <CheckSquare className="w-4 h-4 text-emerald-600" />
                          阶段门终审决策控制台
                        </span>
                      }
                      bordered
                      className="rounded-lg shadow-none"
                    >
                      <div className="space-y-4">
                        <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg">
                          <div className="text-xs text-slate-500">准入状态结论</div>
                          <div className="mt-1 flex items-center gap-2">
                            {preCheckReport?.overallPassed ? (
                              <Tag color="success" className="text-sm py-1 px-3">
                                准入全部达标 (ALLOWED PASS)
                              </Tag>
                            ) : (
                              <Tag color="error" className="text-sm py-1 px-3">
                                准入被阻断 ({preCheckReport?.blockerCount || 1} 项违规)
                              </Tag>
                            )}
                          </div>
                          <div className="text-[11px] text-slate-400 mt-2">
                            依据规约：有未决证据或阻断项时，一票否决直接签署 PASS。
                          </div>
                        </div>

                        <Button
                          type="primary"
                          danger={!preCheckReport?.overallPassed}
                          block
                          size="large"
                          icon={<ShieldCheck className="w-4 h-4" />}
                          onClick={() => setDecisionModalOpen(true)}
                        >
                          签署阶段门评审决策
                        </Button>
                      </div>
                    </Card>

                    {/* 整改行动项跟踪 */}
                    <Card
                      title={
                        <div className="flex items-center justify-between">
                          <span className="text-base font-bold text-slate-800 flex items-center gap-2">
                            <Clock className="w-4 h-4 text-amber-600" />
                            整改行动项闭环跟踪 (Action Items)
                          </span>
                          <Badge count={actionItems.filter((i) => i.status !== 'CLOSED').length} />
                        </div>
                      }
                      bordered
                      className="rounded-lg shadow-none"
                    >
                      {actionItems.length === 0 ? (
                        <div className="text-xs text-slate-400 text-center py-4">当前阶段暂无待整改行动项</div>
                      ) : (
                        <div className="space-y-3">
                          {actionItems.map((item) => (
                            <div
                              key={item.actionItemId}
                              className="p-3 bg-slate-50 border border-slate-200 rounded-lg text-xs space-y-1.5"
                            >
                              <div className="flex items-center justify-between">
                                <span className="font-semibold text-slate-800">{item.title}</span>
                                <Tag color={item.status === 'CLOSED' ? 'green' : 'orange'}>{item.status}</Tag>
                              </div>
                              <div className="text-slate-500">{item.description}</div>
                              <div className="flex items-center justify-between text-slate-400 pt-1">
                                <span>责任人: {item.ownerId}</span>
                                <span>截止: {item.dueDate}</span>
                              </div>
                              {item.status !== 'CLOSED' && (
                                <Button
                                  type="link"
                                  size="small"
                                  className="p-0 text-blue-600"
                                  onClick={() => {
                                    setCurrentActionItem(item);
                                    setCloseActionModalOpen(true);
                                  }}
                                >
                                  提交复测验证并闭环 →
                                </Button>
                              )}
                              {item.resolutionSummary && (
                                <div className="text-[11px] text-emerald-700 bg-emerald-50 p-1.5 rounded">
                                  {item.resolutionSummary}
                                </div>
                              )}
                            </div>
                          ))}
                        </div>
                      )}
                    </Card>
                  </div>
                </div>
              </div>
            ),
          },
          {
            key: 'wbs-cpm',
            label: (
              <span className="flex items-center gap-2 font-medium">
                <Network className="w-4 h-4 text-emerald-600" />
                WBS 任务网络与 CPM 关键路径 (TF=0)
              </span>
            ),
            children: (
              <div className="space-y-6 pt-2">
                {/* CPM 关键路径概览卡 */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <Card bordered className="rounded-lg shadow-none bg-slate-50">
                    <div className="text-xs text-slate-500">项目总工期 (Critical Path Duration)</div>
                    <div className="text-2xl font-bold font-mono text-slate-900 mt-1">
                      {cpmReport?.criticalPathLengthDays || 210}{' '}
                      <span className="text-sm font-normal text-slate-500">天</span>
                    </div>
                    <div className="text-[11px] text-slate-400 mt-1">
                      基于 DFS 拓扑推导与正向/逆向时差分析
                    </div>
                  </Card>
                  <Card bordered className="rounded-lg shadow-none bg-slate-50">
                    <div className="text-xs text-slate-500">关键路径链路 (TF = 0)</div>
                    <div className="text-sm font-bold font-mono text-red-600 mt-1 truncate">
                      {cpmReport?.criticalPathTaskCodes?.join(' → ') || 'TASK-SYS-01 → TASK-SPN-01 → TASK-SPN-02'}
                    </div>
                    <div className="text-[11px] text-slate-400 mt-1">任何延误将直接推迟整机下线时间</div>
                  </Card>
                  <Card bordered className="rounded-lg shadow-none bg-slate-50">
                    <div className="text-xs text-slate-500">DAG 有向无环防环状态</div>
                    <div className="text-sm font-bold text-emerald-600 mt-1 flex items-center gap-1.5">
                      <ShieldCheck className="w-4 h-4" />
                      已通过 DFS 三色探测 (无回路违规)
                    </div>
                    <div className="text-[11px] text-slate-400 mt-1">
                      强防环守护: 阻断闭环死锁
                    </div>
                  </Card>
                </div>

                {/* 任务明细与 CPM 时差表 */}
                <Card
                  title={
                    <span className="text-base font-bold text-slate-800 flex items-center gap-2">
                      <Layers className="w-4 h-4 text-blue-600" />
                      WBS 工程任务分解与关键路径时差指标
                    </span>
                  }
                  bordered
                  className="rounded-lg shadow-none"
                >
                  <Table
                    dataSource={cpmReport?.taskMetrics || []}
                    rowKey="taskId"
                    pagination={false}
                    size="middle"
                    columns={[
                      {
                        title: '任务代码',
                        dataIndex: 'taskCode',
                        key: 'taskCode',
                        render: (code: string, row: TaskScheduleMetric) => (
                          <div className="flex items-center gap-2">
                            <span className="font-mono font-bold text-slate-800 text-xs">{code}</span>
                            {row.isCritical && (
                              <Tag color="error" className="font-bold text-[10px] px-1 py-0">
                                关键路径
                              </Tag>
                            )}
                          </div>
                        ),
                      },
                      {
                        title: '任务名称',
                        dataIndex: 'taskName',
                        key: 'taskName',
                        render: (text: string) => <span className="text-xs text-slate-800">{text}</span>,
                      },
                      {
                        title: '工期(天)',
                        dataIndex: 'durationDays',
                        key: 'durationDays',
                        width: 80,
                        render: (days: number) => <span className="font-mono text-xs">{days}</span>,
                      },
                      {
                        title: '最早开工 ES',
                        dataIndex: 'earlyStartDay',
                        key: 'earlyStartDay',
                        width: 90,
                        render: (v: number) => <span className="font-mono text-xs text-slate-500">Day {v}</span>,
                      },
                      {
                        title: '最早完工 EF',
                        dataIndex: 'earlyFinishDay',
                        key: 'earlyFinishDay',
                        width: 90,
                        render: (v: number) => <span className="font-mono text-xs text-slate-500">Day {v}</span>,
                      },
                      {
                        title: '最迟开工 LS',
                        dataIndex: 'lateStartDay',
                        key: 'lateStartDay',
                        width: 90,
                        render: (v: number) => <span className="font-mono text-xs text-slate-500">Day {v}</span>,
                      },
                      {
                        title: '最迟完工 LF',
                        dataIndex: 'lateFinishDay',
                        key: 'lateFinishDay',
                        width: 90,
                        render: (v: number) => <span className="font-mono text-xs text-slate-500">Day {v}</span>,
                      },
                      {
                        title: '总时差 TF (LS-ES)',
                        dataIndex: 'totalFloatDays',
                        key: 'totalFloatDays',
                        width: 120,
                        render: (tf: number, row: TaskScheduleMetric) => (
                          <Tag color={row.isCritical ? 'red' : 'blue'} className="font-mono font-bold">
                            TF = {tf} 天
                          </Tag>
                        ),
                      },
                      {
                        title: '操作',
                        key: 'action',
                        width: 100,
                        render: (_: any, row: TaskScheduleMetric) => (
                          <Button
                            type="link"
                            size="small"
                            onClick={() => {
                              setSelectedTaskId(row.taskId);
                              // 切换到交付物 Tab
                              const tabsEl = document.querySelector('.ant-tabs-nav-list');
                              if (tabsEl) {
                                const delivTab = tabsEl.querySelectorAll('.ant-tabs-tab')[2] as HTMLElement;
                                if (delivTab) delivTab.click();
                              }
                            }}
                          >
                            查看交付物 →
                          </Button>
                        ),
                      },
                    ]}
                  />
                </Card>

                {/* 依赖关系网络 */}
                <Card
                  title={
                    <span className="text-base font-bold text-slate-800 flex items-center gap-2">
                      <GitBranch className="w-4 h-4 text-purple-600" />
                      工程任务网络四类逻辑依赖 (FS / SS / FF / SF)
                    </span>
                  }
                  bordered
                  className="rounded-lg shadow-none"
                >
                  <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
                    {dependencies.map((dep, idx) => (
                      <div key={idx} className="p-3 bg-slate-50 border border-slate-200 rounded-lg text-xs space-y-1">
                        <div className="flex items-center justify-between">
                          <Tag color="purple" className="font-mono">
                            {dep.depType}
                          </Tag>
                          <span className="text-slate-400 font-mono">Lag: {dep.lagDays || 0}d</span>
                        </div>
                        <div className="font-mono text-slate-700 font-semibold pt-1">
                          Task #{dep.predecessorTaskId} → Task #{dep.successorTaskId}
                        </div>
                      </div>
                    ))}
                  </div>
                </Card>
              </div>
            ),
          },
          {
            key: 'deliverables',
            label: (
              <span className="flex items-center gap-2 font-medium">
                <FileCheck2 className="w-4 h-4 text-indigo-600" />
                交付物规约与多版本提审中心 (三态解耦)
              </span>
            ),
            children: (
              <div className="space-y-6 pt-2">
                <Alert
                  type="info"
                  showIcon
                  message="三态彻底独立原则 (Independent Tri-State)"
                  description="在高端机床研制中，任务进度百分比 (0~100%)、交付物审批状态 (RELEASED) 与阶段门决策 (PASS) 彻底解耦。即使结构工程师将任务标为 100%，若交付物未提审或未达标，严禁视作达标通过！"
                />

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <span className="text-sm font-semibold text-slate-700">当前聚焦任务:</span>
                    <Select
                      value={selectedTaskId}
                      onChange={(v) => setSelectedTaskId(v)}
                      style={{ width: 360 }}
                      options={tasks.map((t) => ({
                        value: t.taskId,
                        label: `${t.taskCode} - ${t.name}`,
                      }))}
                    />
                  </div>
                </div>

                {/* 交付物规约与多版本提审卡片 */}
                <div className="space-y-4">
                  {taskDeliverables.map((group) => (
                    <Card
                      key={group.requirement.delivReqId}
                      title={
                        <div className="flex items-center justify-between">
                          <div className="space-y-0.5">
                            <div className="text-sm font-bold text-slate-800 flex items-center gap-2">
                              <FileText className="w-4 h-4 text-blue-600" />
                              {group.requirement.name}
                              {group.requirement.isMandatory && <Tag color="red">阶段门必选齐套项</Tag>}
                              <Tag color="cyan">{group.requirement.deliverableType}</Tag>
                            </div>
                            <div className="text-xs font-mono text-slate-400">
                              规约编码: {group.requirement.requirementCode} | 密级: {group.requirement.targetSecurityLevel}
                            </div>
                          </div>
                          <Button
                            type="primary"
                            size="small"
                            icon={<PlusCircle className="w-3.5 h-3.5" />}
                            onClick={() => {
                              setTargetReqId(group.requirement.delivReqId);
                              setSubmitDeliverableModalOpen(true);
                            }}
                          >
                            提审迭代版本
                          </Button>
                        </div>
                      }
                      bordered
                      className="rounded-lg shadow-none"
                    >
                      <Timeline
                        mode="left"
                        className="mt-2"
                        items={group.submissions.map((sub) => ({
                          color: sub.isLatest ? 'green' : 'gray',
                          children: (
                            <div className="space-y-1">
                              <div className="flex items-center gap-2">
                                <span className="font-mono font-bold text-xs">Rev #{sub.revisionId}</span>
                                {sub.isLatest ? (
                                  <Tag color="success">当前生效版本 (LATEST)</Tag>
                                ) : (
                                  <Tag color="default">历史过时版本 (SUPERSEDED 留档)</Tag>
                                )}
                                <span className="text-xs text-slate-400">提交人: {sub.submittedBy}</span>
                              </div>
                              <div className="text-xs text-slate-600">{sub.submissionNotes}</div>
                              <div className="font-mono text-[11px] text-slate-400">
                                SHA-256 哈希防伪凭证: {sub.artifactHash}
                              </div>
                            </div>
                          ),
                        }))}
                      />
                    </Card>
                  ))}
                </div>
              </div>
            ),
          },
        ]}
      />

      {/* 弹窗 1: 签署阶段门评审决策 */}
      <Modal
        title="签署阶段门评审决策 (Gate Review Decision Sign-off)"
        open={decisionModalOpen}
        onCancel={() => setDecisionModalOpen(false)}
        onOk={handleRecordDecision}
        width={640}
        okText="确认签署并生效"
        cancelText="取消"
      >
        <Form form={decisionForm} layout="vertical" className="mt-4">
          <Form.Item
            name="decisionType"
            label="决策结论类型"
            rules={[{ required: true, message: '请选择评审决策类型' }]}
            initialValue={preCheckReport?.overallPassed ? 'PASS' : 'CONDITIONAL_PASS'}
          >
            <Select
              options={[
                {
                  value: 'PASS',
                  label: '完全放行通过 (PASS) - 必须所有准入项达标且证据齐套',
                  disabled: !preCheckReport?.overallPassed,
                },
                {
                  value: 'CONDITIONAL_PASS',
                  label: '条件性放行 (CONDITIONAL_PASS) - 强制限定放行范围并随单派发行动项',
                },
                {
                  value: 'REWORK',
                  label: '返工重新评估 (REWORK) - 冻结推进，限期完成技术整改',
                },
                {
                  value: 'STOP',
                  label: '终止项目/暂停推进 (STOP) - 项目移交控制部处理',
                },
              ]}
            />
          </Form.Item>

          <Form.Item
            noStyle
            shouldUpdate={(prev, curr) => prev.decisionType !== curr.decisionType}
          >
            {({ getFieldValue }) =>
              getFieldValue('decisionType') === 'CONDITIONAL_PASS' ? (
                <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg space-y-3 mb-4">
                  <div className="text-xs font-bold text-amber-800 flex items-center gap-1.5">
                    <AlertTriangle className="w-4 h-4 text-amber-600" />
                    条件性放行强约束规则 (CONDITIONAL_PASS 必填要素)
                  </div>
                  <Form.Item
                    name="allowedScope"
                    label="允许放行业务范围 (Allowed Scope)"
                    rules={[{ required: true, message: '条件性放行必须明确指定允许放行范围！' }]}
                    initialValue="仅允许主轴箱体铸件毛坯长周期采购投产，禁止直接装配跑车"
                  >
                    <Input.TextArea rows={2} placeholder="例如：仅允许结构铸件毛坯采购，装配试制冻结" />
                  </Form.Item>

                  <div className="text-xs font-semibold text-slate-700">派发整改行动项 (随单闭环):</div>
                  <Form.Item
                    name="actionTitle"
                    label="行动项主题"
                    rules={[{ required: true, message: '必须派发至少一项整改行动项！' }]}
                    initialValue="补充五轴联动激光干涉仪双向重复定位精度实测报告"
                  >
                    <Input placeholder="输入整改任务标题" />
                  </Form.Item>
                  <div className="grid grid-cols-2 gap-3">
                    <Form.Item name="actionOwner" label="指定责任人" initialValue="ENG-LIU-TEST">
                      <Input placeholder="责任工程师 ID" />
                    </Form.Item>
                    <Form.Item name="actionDueDate" label="整改截止日期" initialValue="2026-10-25">
                      <Input type="date" />
                    </Form.Item>
                  </div>
                </div>
              ) : null
            }
          </Form.Item>

          <Form.Item
            name="decisionNotes"
            label="专家评审意见与结论记录"
            rules={[{ required: true, message: '请输入评审意见' }]}
            initialValue="经联合专家组审议，五轴机床主轴刚度与温升热平衡设计方案可行；由于现场激光实测精度尚缺反向间隙数据，特批条件性放行长周期备料。"
          >
            <Input.TextArea rows={3} placeholder="详细记录评审专家意见及签署意见" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 弹窗 2: 提审交付物迭代版本 */}
      <Modal
        title="提审交付物迭代版本 (Submit Deliverable Revision)"
        open={submitDeliverableModalOpen}
        onCancel={() => setSubmitDeliverableModalOpen(false)}
        onOk={handleSubmitDeliverable}
        width={500}
        okText="立即提审归档"
        cancelText="取消"
      >
        <Form form={deliverableForm} layout="vertical" className="mt-4">
          <Form.Item
            name="notes"
            label="版本变更说与演进说明"
            rules={[{ required: true, message: '请输入版本演进说明' }]}
            initialValue="Rev A.2: 修正了前轴承锁紧螺母防松垫圈倒角尺寸，并同步更新3D CAD模型"
          >
            <Input.TextArea rows={3} placeholder="说明此版本的技术修改点与演进原因" />
          </Form.Item>
          <div className="p-2.5 bg-slate-50 border border-slate-200 rounded text-xs text-slate-500">
            提示：系统将自动生成 SHA-256 唯一制品防伪凭证，并将前一版本自动转为 SUPERSEDED 归档。
          </div>
        </Form>
      </Modal>

      {/* 弹窗 3: 闭环行动项 */}
      <Modal
        title="整改行动项闭环核验 (Close Action Item)"
        open={closeActionModalOpen}
        onCancel={() => setCloseActionModalOpen(false)}
        onOk={handleCloseActionItem}
        width={500}
        okText="验证合格并闭环"
        cancelText="取消"
      >
        <Form form={closeActionForm} layout="vertical" className="mt-4">
          <div className="mb-3 text-xs text-slate-600">
            正在闭环：<span className="font-bold text-slate-800">{currentActionItem?.title}</span>
          </div>
          <Form.Item
            name="notes"
            label="整改闭环结论与实测报告编号"
            rules={[{ required: true, message: '请输入闭环结论' }]}
            initialValue="已取得国家机床质量监督检验中心激光干涉仪全向实测合格报告 (No. MT-2026-0916)，反向间隙实测 0.002mm，符合要求。"
          >
            <Input.TextArea rows={3} placeholder="详细记录验证证据与合格结论" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
