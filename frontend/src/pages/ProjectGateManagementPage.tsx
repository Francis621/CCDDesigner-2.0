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
  Popconfirm,
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
  Edit3,
  Trash2,
  FolderPlus,
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

interface ProjectGateManagementPageProps {
  activeSubAction?: 'overview' | 'create' | 'edit' | 'delete';
  onNavigateSubAction?: (action: string) => void;
}

export const ProjectGateManagementPage: React.FC<ProjectGateManagementPageProps> = ({
  activeSubAction = 'overview',
  onNavigateSubAction,
}) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [projectList, setProjectList] = useState<ProjectEntity[]>([]);
  const [currentProjectId, setCurrentProjectId] = useState<number>(1001);
  const [project, setProject] = useState<ProjectEntity | null>(null);
  const [stages, setStages] = useState<StageEntity[]>([]);
  const [gates, setGates] = useState<GateEntity[]>([]);
  const [activeGate, setActiveGate] = useState<GateEntity | null>(null);

  // 阶段门预检报告
  const [preCheckReport, setPreCheckReport] = useState<GatePreCheckReport | null>(null);
  const [actionItems, setActionItems] = useState<ActionItem[]>([]);

  // WBS 与 CPM
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
  const [createProjectModalOpen, setCreateProjectModalOpen] = useState<boolean>(false);
  const [editProjectModalOpen, setEditProjectModalOpen] = useState<boolean>(false);
  const [deleteProjectModalOpen, setDeleteProjectModalOpen] = useState<boolean>(false);

  const [currentActionItem, setCurrentActionItem] = useState<ActionItem | null>(null);
  const [targetReqId, setTargetReqId] = useState<number | null>(null);

  const [decisionForm] = Form.useForm();
  const [deliverableForm] = Form.useForm();
  const [closeActionForm] = Form.useForm();
  const [createProjectForm] = Form.useForm();
  const [editProjectForm] = Form.useForm();

  // 根据父组件传入的 activeSubAction 自动打开对应模态操作
  useEffect(() => {
    if (activeSubAction === 'create') {
      setCreateProjectModalOpen(true);
    } else if (activeSubAction === 'edit') {
      setEditProjectModalOpen(true);
    } else if (activeSubAction === 'delete') {
      setDeleteProjectModalOpen(true);
    }
  }, [activeSubAction]);

  // 获取全部项目列表
  const fetchProjectList = useCallback(async () => {
    try {
      const res: any = await apiClient.get('/projects');
      if (res.data) {
        setProjectList(res.data);
      }
    } catch (e: any) {
      console.error('获取项目列表失败', e);
    }
  }, []);

  // 1. 初始化加载项目全景与任务数据
  const loadOverview = useCallback(async (projId: number) => {
    try {
      setLoading(true);
      const res: any = await apiClient.get(`/projects/${projId}/gate-overview`);
      if (res.data) {
        setProject(res.data.project);
        setStages(res.data.stages || []);
        setGates(res.data.gates || []);
        // 默认选中 TR3 或当前活动阶段门
        const tr3 = (res.data.gates || []).find((g: GateEntity) => g.gateCode === 'TR3');
        const defaultGate = tr3 || res.data.currentGate || res.data.gates[0];
        setActiveGate(defaultGate);
        if (defaultGate) {
          fetchPreCheck(projId, defaultGate.gateId);
          fetchActionItems(projId, defaultGate.gateId);
        }
      }

      // 加载 WBS 与 任务
      const wbsRes: any = await apiClient.get(`/projects/${projId}/wbs-tasks`);
      if (wbsRes.data) {
        setTasks(wbsRes.data.tasks || []);
        setDependencies(wbsRes.data.dependencies || []);
      }

      // 执行 CPM 分析
      const cpmRes: any = await apiClient.get(`/projects/${projId}/cpm-analysis`);
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

  const fetchPreCheck = async (projId: number, gateId: number) => {
    try {
      const res: any = await apiClient.get(`/projects/${projId}/gates/${gateId}/pre-check`);
      if (res.data) {
        setPreCheckReport(res.data);
      }
    } catch (e: any) {
      notification.warning({ message: '准入预检查询异常', description: e.message });
    }
  };

  const fetchActionItems = async (projId: number, gateId: number) => {
    try {
      const res: any = await apiClient.get(`/projects/${projId}/gates/${gateId}/action-items`);
      if (res.data) {
        setActionItems(res.data);
      }
    } catch (e: any) {
      notification.warning({ message: '行动项查询异常', description: e.message });
    }
  };

  const fetchTaskDeliverables = useCallback(async (projId: number, taskId: number) => {
    try {
      const res: any = await apiClient.get(`/projects/${projId}/tasks/${taskId}/deliverables`);
      if (res.data) {
        setTaskDeliverables(res.data);
      }
    } catch (e: any) {
      console.error(e);
    }
  }, []);

  useEffect(() => {
    fetchProjectList();
    loadOverview(currentProjectId);
  }, [fetchProjectList, loadOverview, currentProjectId]);

  useEffect(() => {
    if (selectedTaskId) {
      fetchTaskDeliverables(currentProjectId, selectedTaskId);
    }
  }, [selectedTaskId, fetchTaskDeliverables, currentProjectId]);

  // 当 project 加载完成时，同步回填编辑表单
  useEffect(() => {
    if (project) {
      editProjectForm.setFieldsValue({
        projectCode: project.projectCode,
        name: project.name,
        projectType: project.projectType,
        managerId: project.managerId,
        chiefEngineerId: project.chiefEngineerId,
      });
    }
  }, [project, editProjectForm]);

  // 阶段门切换处理
  const handleSelectGate = (gate: GateEntity) => {
    setActiveGate(gate);
    fetchPreCheck(currentProjectId, gate.gateId);
    fetchActionItems(currentProjectId, gate.gateId);
  };

  // 切换选中项目
  const handleSelectProject = (projId: number) => {
    setCurrentProjectId(projId);
    loadOverview(projId);
  };

  // 创建新机床研制项目
  const handleCreateProject = async () => {
    try {
      const values = await createProjectForm.validateFields();
      const res: any = await apiClient.post('/projects', values);
      if (res.code === 200) {
        notification.success({
          message: '机床项目立项创建成功',
          description: `项目代号: ${res.data.projectCode}，名称: ${res.data.name}`,
        });
        confetti({ particleCount: 90, spread: 70, origin: { y: 0.6 } });
        setCreateProjectModalOpen(false);
        createProjectForm.resetFields();
        await fetchProjectList();
        setCurrentProjectId(res.data.projectId);
        if (onNavigateSubAction) onNavigateSubAction('project-mgmt-overview');
      } else {
        notification.error({ message: '创建项目失败', description: res.message });
      }
    } catch (e: any) {
      notification.error({
        message: '创建项目异常',
        description: e.response?.data?.message || e.message || '操作失败',
      });
    }
  };

  // 编辑机床研制项目
  const handleUpdateProject = async () => {
    try {
      const values = await editProjectForm.validateFields();
      const res: any = await apiClient.put(`/projects/${currentProjectId}`, values);
      if (res.code === 200) {
        notification.success({
          message: '项目信息修改已保存',
          description: `项目 #${currentProjectId} 元数据已更新`,
        });
        setEditProjectModalOpen(false);
        await fetchProjectList();
        loadOverview(currentProjectId);
        if (onNavigateSubAction) onNavigateSubAction('project-mgmt-overview');
      } else {
        notification.error({ message: '修改项目失败', description: res.message });
      }
    } catch (e: any) {
      notification.error({
        message: '修改项目异常',
        description: e.response?.data?.message || e.message || '操作失败',
      });
    }
  };

  // 删除机床研制项目
  const handleDeleteProject = async (projId: number) => {
    try {
      const res: any = await apiClient.delete(`/projects/${projId}`);
      if (res.code === 200) {
        notification.success({
          message: '项目已安全删除/归档',
          description: `项目 #${projId} 已从活跃研发列表中移除`,
        });
        await fetchProjectList();
        // 切换到列表中的其它项目
        const remaining = projectList.filter((p) => p.projectId !== projId);
        if (remaining.length > 0) {
          setCurrentProjectId(remaining[0].projectId);
        }
        setDeleteProjectModalOpen(false);
        if (onNavigateSubAction) onNavigateSubAction('project-mgmt-overview');
      } else {
        notification.error({ message: '删除项目失败', description: res.message });
      }
    } catch (e: any) {
      notification.error({
        message: '删除项目被系统拦截',
        description: e.response?.data?.message || e.message || '无法删除受保护的核心项目',
      });
    }
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
        `/projects/${currentProjectId}/gates/${activeGate.gateId}/decisions`,
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
        loadOverview(currentProjectId);
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
        `/projects/${currentProjectId}/gates/${activeGate.gateId}/action-items/${currentActionItem.actionItemId}/close`,
        { notes: values.notes }
      );

      if (res.code === 200) {
        notification.success({
          message: '行动项成功闭环',
          description: `行动项 #${currentActionItem.actionItemId} 已验证合格并关闭`,
        });
        setCloseActionModalOpen(false);
        closeActionForm.resetFields();
        fetchActionItems(currentProjectId, activeGate.gateId);
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
        `/projects/${currentProjectId}/tasks/${selectedTaskId}/deliverables/${targetReqId}/submit`,
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
        fetchTaskDeliverables(currentProjectId, selectedTaskId);
        if (activeGate) fetchPreCheck(currentProjectId, activeGate.gateId);
      }
    } catch (e: any) {
      notification.error({ message: '交付物提审失败', description: e.message });
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* 顶部操作工具栏：项目快速切换与创建、编辑、删除操作按钮 */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
        <div className="flex items-center gap-3">
          <span className="text-sm font-bold text-slate-700 whitespace-nowrap">当前研制项目:</span>
          <Select
            value={currentProjectId}
            onChange={handleSelectProject}
            style={{ width: 340 }}
            options={projectList.map((p) => ({
              value: p.projectId,
              label: `${p.projectCode} - ${p.name}`,
            }))}
          />
          <Tag color="geekblue" className="font-mono text-xs">
            ID: {currentProjectId}
          </Tag>
        </div>

        <div className="flex items-center gap-2.5">
          <Button
            type="primary"
            icon={<PlusCircle className="w-4 h-4" />}
            onClick={() => setCreateProjectModalOpen(true)}
            className="bg-blue-600 hover:bg-blue-700 flex items-center gap-1.5"
          >
            创建项目
          </Button>
          <Button
            icon={<Edit3 className="w-4 h-4 text-indigo-600" />}
            onClick={() => setEditProjectModalOpen(true)}
            className="flex items-center gap-1.5 text-indigo-700 border-indigo-200 hover:border-indigo-400"
          >
            编辑项目
          </Button>
          <Button
            danger
            icon={<Trash2 className="w-4 h-4" />}
            onClick={() => setDeleteProjectModalOpen(true)}
            className="flex items-center gap-1.5"
          >
            删除项目
          </Button>
        </div>
      </div>

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
              <div className="text-sm font-semibold text-white mt-1">
                {project?.chiefEngineerId || '王总师 (Chief Eng)'}
              </div>
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
                            onClick={() => activeGate && fetchPreCheck(currentProjectId, activeGate.gateId)}
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

      {/* 弹窗 A: 创建项目 */}
      <Modal
        title={
          <div className="flex items-center gap-2 text-slate-800">
            <FolderPlus className="w-5 h-5 text-blue-600" />
            <span>创建高端数控机床正向研制项目 (New Machine Project)</span>
          </div>
        }
        open={createProjectModalOpen}
        onCancel={() => {
          setCreateProjectModalOpen(false);
          if (onNavigateSubAction) onNavigateSubAction('project-mgmt-overview');
        }}
        onOk={handleCreateProject}
        width={600}
        okText="立即立项创建"
        cancelText="取消"
      >
        <Form form={createProjectForm} layout="vertical" className="mt-4">
          <Form.Item
            name="projectCode"
            label="项目代号 (Project Code)"
            rules={[{ required: true, message: '请输入项目代号，如 PRJ-HMC630-DUAL' }]}
            initialValue="PRJ-HMC500-FMS"
          >
            <Input placeholder="输入项目唯一代号，如 PRJ-VMC850-5AXIS" className="font-mono" />
          </Form.Item>

          <Form.Item
            name="name"
            label="机床项目全称"
            rules={[{ required: true, message: '请输入机床项目全称' }]}
            initialValue="高精度卧式柔性制造加工中心研制工程"
          >
            <Input placeholder="输入机床产品研发项目名称" />
          </Form.Item>

          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              name="projectType"
              label="机床产品类型"
              rules={[{ required: true, message: '请选择项目类型' }]}
              initialValue="PLATFORM"
            >
              <Select
                options={[
                  { value: 'PLATFORM', label: '平台级核心机型 (PLATFORM)' },
                  { value: 'DERIVATIVE', label: '客户定制衍生机型 (DERIVATIVE)' },
                ]}
              />
            </Form.Item>
            <Form.Item
              name="managerId"
              label="项目管理主管 (PM)"
              rules={[{ required: true, message: '请输入项目主管 ID' }]}
              initialValue="PM-CHEN-005"
            >
              <Input placeholder="项目经理工号" />
            </Form.Item>
          </div>

          <Form.Item
            name="chiefEngineerId"
            label="机床总工程师 (Chief Engineer)"
            rules={[{ required: true, message: '请输入总工程师 ID' }]}
            initialValue="ENG-WANG-CHIEF"
          >
            <Input placeholder="总师工号" />
          </Form.Item>

          <Form.Item
            name="description"
            label="研制背景与指标论证说明"
            initialValue="面向航空航天高刚性叶轮叶盘精密加工需求，开展直联大扭矩电主轴、双工作台快速交换及热误差综合补偿正向设计。"
          >
            <Input.TextArea rows={3} placeholder="输入研制背景、机床主规格与目标" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 弹窗 B: 编辑项目 */}
      <Modal
        title={
          <div className="flex items-center gap-2 text-slate-800">
            <Edit3 className="w-5 h-5 text-indigo-600" />
            <span>编辑机床研制项目信息 (Edit Project #{currentProjectId})</span>
          </div>
        }
        open={editProjectModalOpen}
        onCancel={() => {
          setEditProjectModalOpen(false);
          if (onNavigateSubAction) onNavigateSubAction('project-mgmt-overview');
        }}
        onOk={handleUpdateProject}
        width={600}
        okText="保存修改"
        cancelText="取消"
      >
        <Form form={editProjectForm} layout="vertical" className="mt-4">
          <Form.Item name="projectCode" label="项目代号 (不可变)">
            <Input disabled className="font-mono bg-slate-50" />
          </Form.Item>

          <Form.Item
            name="name"
            label="机床项目全称"
            rules={[{ required: true, message: '项目全称不能为空' }]}
          >
            <Input />
          </Form.Item>

          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="projectType" label="机床产品类型">
              <Select
                options={[
                  { value: 'PLATFORM', label: '平台级核心机型 (PLATFORM)' },
                  { value: 'DERIVATIVE', label: '客户定制衍生机型 (DERIVATIVE)' },
                ]}
              />
            </Form.Item>
            <Form.Item name="managerId" label="项目主管 (PM)">
              <Input />
            </Form.Item>
          </div>

          <Form.Item name="chiefEngineerId" label="机床总工程师 (Chief Engineer)">
            <Input />
          </Form.Item>
        </Form>
      </Modal>

      {/* 弹窗 C: 删除项目与项目台账安全管理 */}
      <Modal
        title={
          <div className="flex items-center gap-2 text-red-600 font-bold">
            <Trash2 className="w-5 h-5" />
            <span>机床研制项目删除与归档管理 (Delete / Archive Projects)</span>
          </div>
        }
        open={deleteProjectModalOpen}
        onCancel={() => {
          setDeleteProjectModalOpen(false);
          if (onNavigateSubAction) onNavigateSubAction('project-mgmt-overview');
        }}
        footer={[
          <Button
            key="close"
            onClick={() => {
              setDeleteProjectModalOpen(false);
              if (onNavigateSubAction) onNavigateSubAction('project-mgmt-overview');
            }}
          >
            关闭返回
          </Button>,
        ]}
        width={720}
      >
        <div className="mt-3 space-y-4">
          <Alert
            type="warning"
            showIcon
            message="项目删除规约警示 (Protection Notice)"
            description="删除研制项目将同步清理其阶段门、WBS 任务网络与交付物规约。已锁定基线并处于关键评审状态的核心平台机型具备高风险守护。"
          />

          <Table
            dataSource={projectList}
            rowKey="projectId"
            pagination={false}
            size="small"
            columns={[
              {
                title: '项目代号',
                dataIndex: 'projectCode',
                key: 'projectCode',
                render: (code: string) => <span className="font-mono font-bold text-xs">{code}</span>,
              },
              {
                title: '机床全称',
                dataIndex: 'name',
                key: 'name',
                render: (name: string) => <span className="text-xs">{name}</span>,
              },
              {
                title: '机型属性',
                dataIndex: 'projectType',
                key: 'projectType',
                render: (type: string) => (
                  <Tag color={type === 'PLATFORM' ? 'blue' : 'cyan'} className="text-[11px]">
                    {type}
                  </Tag>
                ),
              },
              {
                title: '总工程师',
                dataIndex: 'chiefEngineerId',
                key: 'chiefEngineerId',
                render: (eng: string) => <span className="text-xs text-slate-500">{eng}</span>,
              },
              {
                title: '操作',
                key: 'op',
                render: (_: any, row: ProjectEntity) => (
                  <Popconfirm
                    title="确定删除此机床研制项目？"
                    description={`项目代号: ${row.projectCode}。此操作不可逆！`}
                    onConfirm={() => handleDeleteProject(row.projectId)}
                    okText="确定删除"
                    cancelText="取消"
                    okButtonProps={{ danger: true }}
                  >
                    <Button danger type="link" size="small" icon={<Trash2 className="w-3.5 h-3.5 inline mr-0.5" />}>
                      删除项目
                    </Button>
                  </Popconfirm>
                ),
              },
            ]}
          />
        </div>
      </Modal>

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
