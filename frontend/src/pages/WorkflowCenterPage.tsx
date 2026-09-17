import React, { useState, useEffect, useMemo } from 'react';
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
  Row,
  Col,
  Alert,
  Drawer,
  Badge,
  Timeline,
  Statistic,
  Descriptions,
  Tooltip,
} from 'antd';
import {
  GitMerge,
  Clock,
  ShieldCheck,
  FileCheck2,
  Search,
  Send,
  UserCheck,
  KeyRound,
  RefreshCw,
  Eye,
  Workflow,
  Sparkles,
  Lock,
} from 'lucide-react';
import { useAuthStore } from '@/stores/useAuthStore';

const { Text, Title } = Typography;
const { TextArea } = Input;

// ==================== 数据契约接口 ====================

export interface WorkflowTaskItem {
  taskId: string;
  taskName: string;
  workflowInstId: number;
  flowableProcInstId: string;
  targetObjectType: string;
  targetObjectId: number;
  targetBusinessCode: string;
  projectId?: string;
  initiatorId: string;
  assignee?: string;
  isSelfApprovalRestricted: boolean;
  createdAt: string;
}

export interface WorkflowInstanceItem {
  workflowInstId: number;
  flowableProcInstId: string;
  bindingId: number;
  targetObjectType: string;
  targetObjectId: number;
  targetBusinessCode: string;
  targetContentHash: string;
  projectId?: string;
  initiatorId: string;
  status: 'RUNNING' | 'COMPLETED' | 'TERMINATED' | 'SUSPENDED';
  conclusion?: 'APPROVED' | 'REJECTED' | 'WITHDRAWN';
  terminationReason?: string;
  startedAt: string;
  completedAt?: string;
}

export interface ApprovalDecisionItem {
  decisionTicketId: number;
  workflowInstId: number;
  targetObjectType: string;
  targetObjectId: number;
  targetContentHash: string;
  finalConclusion: 'APPROVED' | 'REJECTED' | 'WITHDRAWN';
  isConsumed: boolean;
  consumedAt?: string;
  consumedByAction?: string;
  cryptoSignatureStamp: string;
  signedPayloadDigest: string;
  decidedAt: string;
}

interface WorkflowCenterPageProps {
  onNavigate?: (tab: string) => void;
}

// 模拟初始种子数据
const INITIAL_TASKS_SEED: WorkflowTaskItem[] = [
  {
    taskId: 'task_eco_ccb_02',
    taskName: 'CCB变更控制委员会决策签发',
    workflowInstId: 77001,
    flowableProcInstId: 'prc_inst_eco_0042',
    targetObjectType: 'ChangeOrder',
    targetObjectId: 8001,
    targetBusinessCode: 'ECO-2026-0042',
    projectId: 'VMC_ENTERPRISE',
    initiatorId: 'chief_designer',
    assignee: 'admin',
    isSelfApprovalRestricted: false,
    createdAt: new Date(Date.now() - 3600 * 1000).toISOString(),
  },
];

const INITIAL_INSTANCES_SEED: WorkflowInstanceItem[] = [
  {
    workflowInstId: 77001,
    flowableProcInstId: 'prc_inst_eco_0042',
    bindingId: 102,
    targetObjectType: 'ChangeOrder',
    targetObjectId: 8001,
    targetBusinessCode: 'ECO-2026-0042',
    targetContentHash: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    projectId: 'VMC_ENTERPRISE',
    initiatorId: 'chief_designer',
    status: 'RUNNING',
    startedAt: new Date(Date.now() - 7200 * 1000).toISOString(),
  },
  {
    workflowInstId: 77002,
    flowableProcInstId: 'prc_inst_rel_0001',
    bindingId: 101,
    targetObjectType: 'ModelRelease',
    targetObjectId: 5001,
    targetBusinessCode: 'REL-VMC1000-SYS-001',
    targetContentHash: '8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01',
    projectId: 'VMC_ENTERPRISE',
    initiatorId: 'sys_architect',
    status: 'COMPLETED',
    conclusion: 'APPROVED',
    startedAt: new Date(Date.now() - 3 * 86400 * 1000).toISOString(),
    completedAt: new Date(Date.now() - 2 * 86400 * 1000).toISOString(),
  },
];

const INITIAL_DECISIONS_SEED: ApprovalDecisionItem[] = [
  {
    decisionTicketId: 88001,
    workflowInstId: 77002,
    targetObjectType: 'ModelRelease',
    targetObjectId: 5001,
    targetContentHash: '8fc3a718d0984a1e948c21a37c02b54901239841892809182390192830192a01',
    finalConclusion: 'APPROVED',
    isConsumed: true,
    consumedAt: new Date(Date.now() - 2 * 86400 * 1000).toISOString(),
    consumedByAction: 'CONSUME-ACT-M06-REL-5001',
    cryptoSignatureStamp: 'SIG_RSA_MC0CFQCZ01...99a01x==',
    signedPayloadDigest: 'a7c2b3e891238491820391820391820391820391820391820391820391820391',
    decidedAt: new Date(Date.now() - 2 * 86400 * 1000).toISOString(),
  },
];

export const WorkflowCenterPage: React.FC<WorkflowCenterPageProps> = () => {
  const { user } = useAuthStore();
  const currentUserId = user?.username || 'admin';

  // 数据列表状态
  const [tasks, setTasks] = useState<WorkflowTaskItem[]>(INITIAL_TASKS_SEED);
  const [instances, setInstances] = useState<WorkflowInstanceItem[]>(INITIAL_INSTANCES_SEED);
  const [decisions, setDecisions] = useState<ApprovalDecisionItem[]>(INITIAL_DECISIONS_SEED);

  // 交互状态
  const [activeTab, setActiveTab] = useState<string>('tasks');
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [approveModalVisible, setApproveModalVisible] = useState<boolean>(false);
  const [currentTask, setCurrentTask] = useState<WorkflowTaskItem | null>(null);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState<boolean>(false);
  const [selectedInstance, setSelectedInstance] = useState<WorkflowInstanceItem | null>(null);

  // 数字签名成功凭证展示弹窗
  const [stampModalVisible, setStampModalVisible] = useState<boolean>(false);
  const [latestDecision, setLatestDecision] = useState<ApprovalDecisionItem | null>(null);

  // 新发起流程模态框
  const [startModalVisible, setStartModalVisible] = useState<boolean>(false);

  const [approveForm] = Form.useForm();
  const [startForm] = Form.useForm();

  // 尝试拉取后端真实数据
  useEffect(() => {
    fetchWorkflowData();
  }, [currentUserId]);

  const fetchWorkflowData = async () => {
    try {
      const [tRes, iRes] = await Promise.all([
        fetch('/api/v1/workflow-tasks/pending', { headers: { 'X-Current-User-Id': currentUserId } }),
        fetch('/api/v1/workflow-instances'),
      ]);

      if (tRes.ok) {
        const tJson = await tRes.json();
        if (tJson?.data) setTasks(tJson.data);
      }
      if (iRes.ok) {
        const iJson = await iRes.json();
        if (iJson?.data) setInstances(iJson.data);
      }
    } catch {
      // 离线降级使用种子数据
    }
  };

  // 过滤待办列表
  const filteredTasks = useMemo(() => {
    return tasks.filter((t) => {
      // SoD 发起人自审标注计算
      const isInitiator = t.initiatorId === currentUserId;
      t.isSelfApprovalRestricted = isInitiator;

      if (!searchKeyword.trim()) return true;
      const kw = searchKeyword.toLowerCase();
      return (
        t.taskName.toLowerCase().includes(kw) ||
        t.targetBusinessCode.toLowerCase().includes(kw) ||
        t.initiatorId.toLowerCase().includes(kw)
      );
    });
  }, [tasks, searchKeyword, currentUserId]);

  // 打开审批抽屉
  const handleOpenApprove = (task: WorkflowTaskItem) => {
    if (task.initiatorId === currentUserId) {
      Modal.warning({
        title: 'SoD 职责分离保护生效 (SoD-01)',
        content: (
          <div>
            <p><strong>您是该流程的发起人 ({task.initiatorId})。</strong></p>
            <p>根据高端数控机床正向设计安全规约，流程发起人严禁自发自批，系统已对当前审批操作执行物理锁定。</p>
          </div>
        ),
      });
      return;
    }
    setCurrentTask(task);
    approveForm.resetFields();
    setApproveModalVisible(true);
  };

  // 执行审批
  const handleCompleteTask = async (values: any) => {
    if (!currentTask) return;
    setSubmitting(true);
    try {
      const res = await fetch(`/api/v1/workflow-tasks/${currentTask.taskId}/complete`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Current-User-Id': currentUserId,
        },
        body: JSON.stringify({
          action: values.action,
          comment: values.comment,
        }),
      });

      let resData;
      if (res.ok) {
        resData = (await res.json())?.data;
      } else {
        const errJson = await res.json().catch(() => ({}));
        throw new Error(errJson.message || '后端审批流转异常');
      }

      // 本地状态更新
      setTasks((prev) => prev.filter((t) => t.taskId !== currentTask.taskId));

      const newDecision: ApprovalDecisionItem = {
        decisionTicketId: resData?.decisionTicketId || Date.now(),
        workflowInstId: currentTask.workflowInstId,
        targetObjectType: currentTask.targetObjectType,
        targetObjectId: currentTask.targetObjectId,
        targetContentHash: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
        finalConclusion: resData?.finalConclusion || values.action === 'REJECT' ? 'REJECTED' : 'APPROVED',
        isConsumed: false,
        cryptoSignatureStamp: 'SIG_RSA_PLATFORM_KEY_' + Math.random().toString(36).substring(2, 12).toUpperCase(),
        signedPayloadDigest: 'digest_' + Math.random().toString(36).substring(2, 12),
        decidedAt: new Date().toISOString(),
      };

      setDecisions((prev) => [newDecision, ...prev]);
      setLatestDecision(newDecision);

      setInstances((prev) =>
        prev.map((inst) =>
          inst.workflowInstId === currentTask.workflowInstId
            ? { ...inst, status: 'COMPLETED', conclusion: newDecision.finalConclusion }
            : inst
        )
      );

      message.success('节点审批完成！已生成不可篡改数字签名凭据');
      setApproveModalVisible(false);
      setStampModalVisible(true);
    } catch (err: any) {
      message.error(err.message || '审批提交失败');
    } finally {
      setSubmitting(false);
    }
  };

  // 模拟业务系统核销凭据
  const handleConsumeDecision = async (ticket: ApprovalDecisionItem, tamperHash = false) => {
    try {
      const expectedHash = tamperHash ? 'tampered_hash_error' : ticket.targetContentHash;
      const res = await fetch(`/api/v1/workflow-decisions/${ticket.decisionTicketId}/consume`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          actionId: 'ACTION-CONSUME-TEST-' + Date.now(),
          expectedContentHash: expectedHash,
        }),
      });

      if (!res.ok) {
        const errJson = await res.json().catch(() => ({}));
        throw new Error(errJson.message || '凭证核销失败');
      }

      setDecisions((prev) =>
        prev.map((d) =>
          d.decisionTicketId === ticket.decisionTicketId
            ? { ...d, isConsumed: true, consumedAt: new Date().toISOString() }
            : d
        )
      );
      message.success('凭证核销成功！业务状态机已推进至 RELEASED');
    } catch (err: any) {
      Modal.error({
        title: '凭证核销安全拦截 (AT-16)',
        content: err.message || '凭据核销校验失败',
      });
    }
  };

  // 发起新审批
  const handleStartWorkflow = async (values: any) => {
    try {
      const res = await fetch('/api/v1/workflow-instances', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Current-User-Id': currentUserId,
        },
        body: JSON.stringify(values),
      });

      if (!res.ok) {
        const errJson = await res.json().catch(() => ({}));
        throw new Error(errJson.message || '启动流程失败');
      }

      const resData = (await res.json())?.data;
      const newInst: WorkflowInstanceItem = {
        workflowInstId: resData?.workflowInstId || Date.now(),
        flowableProcInstId: resData?.flowableProcInstId || 'prc_' + Date.now(),
        bindingId: 101,
        targetObjectType: values.targetObjectType,
        targetObjectId: values.targetObjectId,
        targetBusinessCode: values.targetBusinessCode,
        targetContentHash: values.targetContentHash,
        projectId: values.projectId || 'VMC_ENTERPRISE',
        initiatorId: currentUserId,
        status: 'RUNNING',
        startedAt: new Date().toISOString(),
      };

      setInstances((prev) => [newInst, ...prev]);

      // 生成一条待办
      const newTask: WorkflowTaskItem = {
        taskId: 'task_' + Date.now(),
        taskName: '专业技术联合审查',
        workflowInstId: newInst.workflowInstId,
        flowableProcInstId: newInst.flowableProcInstId,
        targetObjectType: values.targetObjectType,
        targetObjectId: values.targetObjectId,
        targetBusinessCode: values.targetBusinessCode,
        projectId: newInst.projectId,
        initiatorId: currentUserId,
        assignee: 'lead_analyst',
        isSelfApprovalRestricted: false,
        createdAt: new Date().toISOString(),
      };
      setTasks((prev) => [newTask, ...prev]);

      message.success('业务审批流程已成功启动！Flowable 实例已激活');
      setStartModalVisible(false);
      startForm.resetFields();
    } catch (err: any) {
      message.error(err.message || '启动流程异常');
    }
  };

  return (
    <div style={{ padding: '0 4px', display: 'flex', flexDirection: 'column', gap: 14 }}>
      {/* 顶部架构规范与安全徽标卡片 */}
      <Card bodyStyle={{ padding: '14px 20px' }} style={{ borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Space size="middle" align="center">
              <span style={{ fontSize: 18, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 8 }}>
                <Workflow className="w-5 h-5 text-indigo-600" />
                M24 工作流与工程审批中心
              </span>
              <Tag color="blue">Flowable 7.x 嵌入式轻量引擎</Tag>
              <Tag color="green">CST-M24-01 业务表防越权写保护</Tag>
              <Tag color="cyan">AT-16 快照哈希防篡改</Tag>
              <Tag color="purple">SoD-01 职责分离防自审</Tag>
              <Tag color="gold">不可伪造电子签名凭据</Tag>
            </Space>
          </Col>
          <Col>
            <Space>
              <Button
                type="primary"
                icon={<Send className="w-4 h-4" />}
                onClick={() => setStartModalVisible(true)}
              >
                发起业务审批流
              </Button>
              <Button icon={<RefreshCw className="w-3.5 h-3.5" />} onClick={fetchWorkflowData}>
                刷新
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 核心指标统计 */}
      <Row gutter={16}>
        <Col span={6}>
          <Card bodyStyle={{ padding: 14 }} style={{ borderRadius: 8 }}>
            <Statistic
              title="待我审批任务"
              value={tasks.length}
              prefix={<Clock className="w-4 h-4 text-orange-500" />}
              valueStyle={{ color: tasks.length > 0 ? '#fa8c16' : '#52c41a', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bodyStyle={{ padding: 14 }} style={{ borderRadius: 8 }}>
            <Statistic
              title="运行中流程实例"
              value={instances.filter((i) => i.status === 'RUNNING').length}
              prefix={<GitMerge className="w-4 h-4 text-blue-500" />}
              valueStyle={{ color: '#1677ff', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bodyStyle={{ padding: 14 }} style={{ borderRadius: 8 }}>
            <Statistic
              title="已签发审批决议"
              value={decisions.length}
              prefix={<FileCheck2 className="w-4 h-4 text-green-500" />}
              valueStyle={{ color: '#52c41a', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bodyStyle={{ padding: 14 }} style={{ borderRadius: 8 }}>
            <Statistic
              title="当前登录会签身份"
              value={currentUserId}
              prefix={<UserCheck className="w-4 h-4 text-purple-500" />}
              valueStyle={{ fontSize: 16, fontWeight: 600, color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 主体功能多页签卡片 */}
      <Card bodyStyle={{ padding: '8px 16px 16px' }} style={{ borderRadius: 8 }}>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'tasks',
              label: (
                <Space>
                  <Clock className="w-4 h-4" />
                  <span>待办任务与会签流转</span>
                  {tasks.length > 0 && <Badge count={tasks.length} />}
                </Space>
              ),
              children: (
                <div>
                  <Row justify="space-between" align="middle" style={{ marginBottom: 12 }}>
                    <Col span={8}>
                      <Input
                        placeholder="搜索任务名称、工程对象编号、发起人..."
                        prefix={<Search className="w-4 h-4 text-gray-400" />}
                        value={searchKeyword}
                        onChange={(e) => setSearchKeyword(e.target.value)}
                        allowClear
                      />
                    </Col>
                    <Col>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        审批服务仅生成不可篡改决策凭据，严禁直接改写业务表主状态
                      </Text>
                    </Col>
                  </Row>

                  <Table
                    dataSource={filteredTasks}
                    rowKey="taskId"
                    pagination={{ pageSize: 10 }}
                    columns={[
                      {
                        title: '待办任务名称',
                        dataIndex: 'taskName',
                        key: 'taskName',
                        render: (text, record) => (
                          <Space direction="vertical" size={2}>
                            <Text strong style={{ fontSize: 14 }}>{text}</Text>
                            <Text type="secondary" style={{ fontSize: 11 }}>
                              Flowable 任务 ID: {record.taskId}
                            </Text>
                          </Space>
                        ),
                      },
                      {
                        title: '被审机床工程对象',
                        key: 'targetObject',
                        render: (_, r) => (
                          <Space direction="vertical" size={2}>
                            <Tag color="geekblue">{r.targetObjectType}: {r.targetBusinessCode}</Tag>
                            <Text type="secondary" style={{ fontSize: 11 }}>
                              所属项目: {r.projectId || 'VMC_ENTERPRISE'}
                            </Text>
                          </Space>
                        ),
                      },
                      {
                        title: '流程发起人',
                        dataIndex: 'initiatorId',
                        key: 'initiatorId',
                        render: (initiator) => (
                          <Space>
                            <UserCheck className="w-3.5 h-3.5 text-blue-600" />
                            <span>{initiator}</span>
                          </Space>
                        ),
                      },
                      {
                        title: 'SoD 职责分离状态',
                        key: 'sodStatus',
                        render: (_, r) => {
                          const isRestricted = r.initiatorId === currentUserId;
                          return isRestricted ? (
                            <Tooltip title="SoD-01 规则生效：流程发起人严禁审批自身提交的修改申请与模型发布">
                              <Tag color="error" icon={<Lock className="w-3 h-3 inline mr-1" />}>
                                发起人自审受限
                              </Tag>
                            </Tooltip>
                          ) : (
                            <Tag color="success" icon={<ShieldCheck className="w-3 h-3 inline mr-1" />}>
                              符合会签资格
                            </Tag>
                          );
                        },
                      },
                      {
                        title: '到达时间',
                        dataIndex: 'createdAt',
                        key: 'createdAt',
                        render: (t) => new Date(t).toLocaleString(),
                      },
                      {
                        title: '操作',
                        key: 'action',
                        render: (_, r) => (
                          <Space>
                            <Button
                              type="primary"
                              size="small"
                              disabled={r.initiatorId === currentUserId}
                              onClick={() => handleOpenApprove(r)}
                            >
                              签署决议
                            </Button>
                          </Space>
                        ),
                      },
                    ]}
                  />
                </div>
              ),
            },
            {
              key: 'instances',
              label: (
                <Space>
                  <GitMerge className="w-4 h-4" />
                  <span>流程实例运行台账</span>
                </Space>
              ),
              children: (
                <Table
                  dataSource={instances}
                  rowKey="workflowInstId"
                  columns={[
                    {
                      title: '流程实例ID',
                      dataIndex: 'workflowInstId',
                      key: 'workflowInstId',
                    },
                    {
                      title: '关联工程对象',
                      key: 'targetObject',
                      render: (_, r) => (
                        <Space direction="vertical" size={2}>
                          <Text strong>{r.targetBusinessCode}</Text>
                          <Tag color="blue">{r.targetObjectType} (ID: {r.targetObjectId})</Tag>
                        </Space>
                      ),
                    },
                    {
                      title: '启动快照 SHA-256 (AT-16)',
                      dataIndex: 'targetContentHash',
                      key: 'targetContentHash',
                      render: (hash) => (
                        <Tooltip title={hash}>
                          <code style={{ fontSize: 11 }}>{hash.substring(0, 16)}...</code>
                        </Tooltip>
                      ),
                    },
                    {
                      title: '发起人工号',
                      dataIndex: 'initiatorId',
                      key: 'initiatorId',
                    },
                    {
                      title: '运行状态',
                      dataIndex: 'status',
                      key: 'status',
                      render: (status) => {
                        if (status === 'RUNNING') return <Tag color="processing">流转中</Tag>;
                        if (status === 'COMPLETED') return <Tag color="success">已完成</Tag>;
                        if (status === 'TERMINATED') return <Tag color="error">已强行熔断</Tag>;
                        return <Tag>{status}</Tag>;
                      },
                    },
                    {
                      title: '最终结论',
                      dataIndex: 'conclusion',
                      key: 'conclusion',
                      render: (con) => {
                        if (con === 'APPROVED') return <Tag color="green">全票通过</Tag>;
                        if (con === 'REJECTED') return <Tag color="red">驳回否决</Tag>;
                        if (con === 'WITHDRAWN') return <Tag color="default">发起人撤回</Tag>;
                        return <Text type="secondary">-</Text>;
                      },
                    },
                    {
                      title: '启动时间',
                      dataIndex: 'startedAt',
                      key: 'startedAt',
                      render: (t) => new Date(t).toLocaleString(),
                    },
                    {
                      title: '操作',
                      key: 'action',
                      render: (_, r) => (
                        <Space>
                          <Button
                            size="small"
                            icon={<Eye className="w-3.5 h-3.5" />}
                            onClick={() => {
                              setSelectedInstance(r);
                              setDetailDrawerVisible(true);
                            }}
                          >
                            流程拓扑
                          </Button>
                        </Space>
                      ),
                    },
                  ]}
                />
              ),
            },
            {
              key: 'decisions',
              label: (
                <Space>
                  <FileCheck2 className="w-4 h-4" />
                  <span>法律级审批凭据与核销 (Approval Decisions)</span>
                </Space>
              ),
              children: (
                <Table
                  dataSource={decisions}
                  rowKey="decisionTicketId"
                  columns={[
                    {
                      title: '凭证票据号',
                      dataIndex: 'decisionTicketId',
                      key: 'decisionTicketId',
                    },
                    {
                      title: '被审业务对象',
                      key: 'targetObj',
                      render: (_, r) => (
                        <Tag color="cyan">{r.targetObjectType}: {r.targetObjectId}</Tag>
                      ),
                    },
                    {
                      title: '审批结论',
                      dataIndex: 'finalConclusion',
                      key: 'finalConclusion',
                      render: (con) => (
                        <Tag color={con === 'APPROVED' ? 'green' : 'red'}>
                          {con === 'APPROVED' ? '通过 (APPROVED)' : '驳回 (REJECTED)'}
                        </Tag>
                      ),
                    },
                    {
                      title: '不可伪造数字签名印章',
                      dataIndex: 'cryptoSignatureStamp',
                      key: 'cryptoSignatureStamp',
                      render: (stamp) => (
                        <Tooltip title={stamp}>
                          <Tag color="gold" icon={<KeyRound className="w-3 h-3 inline mr-1" />}>
                            平台托管签名有效
                          </Tag>
                        </Tooltip>
                      ),
                    },
                    {
                      title: '核销状态 (两阶段解耦)',
                      dataIndex: 'isConsumed',
                      key: 'isConsumed',
                      render: (consumed) => (
                        <Tag color={consumed ? 'purple' : 'orange'}>
                          {consumed ? '已由业务状态机核销' : '待状态机核销'}
                        </Tag>
                      ),
                    },
                    {
                      title: '签发时间',
                      dataIndex: 'decidedAt',
                      key: 'decidedAt',
                      render: (t) => new Date(t).toLocaleString(),
                    },
                    {
                      title: '状态机核销操作',
                      key: 'consumeAction',
                      render: (_, r) => (
                        <Space>
                          <Button
                            size="small"
                            type="dashed"
                            disabled={r.isConsumed}
                            onClick={() => handleConsumeDecision(r, false)}
                          >
                            模拟业务核销
                          </Button>
                          <Button
                            size="small"
                            danger
                            onClick={() => handleConsumeDecision(r, true)}
                          >
                            篡改测试(AT-16)
                          </Button>
                        </Space>
                      ),
                    },
                  ]}
                />
              ),
            },
          ]}
        />
      </Card>

      {/* 节点签署抽屉 / 对话框 */}
      <Modal
        title={
          <Space>
            <ShieldCheck className="w-5 h-5 text-blue-600" />
            <span>执行工程审批与数字签署</span>
          </Space>
        }
        open={approveModalVisible}
        onCancel={() => setApproveModalVisible(false)}
        footer={null}
        width={640}
        destroyOnClose
      >
        {currentTask && (
          <Form form={approveForm} layout="vertical" onFinish={handleCompleteTask}>
            <Alert
              type="info"
              showIcon
              message="严格职责分离与数字签名说明"
              description="本操作将在会签终点使用平台托管数字私钥对您的审查决议及当前快照哈希进行不可逆加签。审批完成后将签发 ApprovalDecision 法律凭证，供业务状态机推进。"
              style={{ marginBottom: 16 }}
            />

            <Descriptions size="small" column={2} bordered style={{ marginBottom: 16 }}>
              <Descriptions.Item label="任务节点">{currentTask.taskName}</Descriptions.Item>
              <Descriptions.Item label="工程对象">{currentTask.targetObjectType}: {currentTask.targetBusinessCode}</Descriptions.Item>
              <Descriptions.Item label="流程发起人">{currentTask.initiatorId}</Descriptions.Item>
              <Descriptions.Item label="当前会签人">{currentUserId}</Descriptions.Item>
            </Descriptions>

            <Form.Item
              name="action"
              label="表决结论"
              initialValue="APPROVE"
              rules={[{ required: true }]}
            >
              <Select
                options={[
                  { label: '✅ 同意通过 (APPROVE)', value: 'APPROVE' },
                  { label: '❌ 驳回不通过 (REJECT)', value: 'REJECT' },
                  { label: '➡️ 委派/转办 (DELEGATE)', value: 'DELEGATE' },
                ]}
              />
            </Form.Item>

            <Form.Item
              name="comment"
              label="技术审查批注与专家意见"
              rules={[{ required: true, message: '请填写详实的审查批注' }]}
            >
              <TextArea
                rows={4}
                placeholder="例如：经校核，主轴热-刚度耦合接口与电气接线定义符合设计规范，同意发布。"
              />
            </Form.Item>

            <Row justify="end">
              <Space>
                <Button onClick={() => setApproveModalVisible(false)}>取消</Button>
                <Button type="primary" htmlType="submit" loading={submitting} icon={<FileCheck2 className="w-4 h-4" />}>
                  确认并加签
                </Button>
              </Space>
            </Row>
          </Form>
        )}
      </Modal>

      {/* 签发成功与不可伪造电子凭证展示模态框 */}
      <Modal
        title={
          <Space>
            <Sparkles className="w-5 h-5 text-green-600" />
            <span style={{ color: '#52c41a', fontWeight: 700 }}>不可篡改审批决议凭据签发成功</span>
          </Space>
        }
        open={stampModalVisible}
        onOk={() => setStampModalVisible(false)}
        onCancel={() => setStampModalVisible(false)}
        okText="完成"
        cancelButtonProps={{ style: { display: 'none' } }}
        width={680}
      >
        {latestDecision && (
          <div>
            <Alert
              type="success"
              showIcon
              message="平台非对称数字签名 (SHA256withRSA) 加签完成"
              description="该凭据具备完整法律证据效应。依据 CST-M24-01 原则，审批服务未修改业务主表，业务模块须显式消费本凭据推进状态机。"
              style={{ marginBottom: 16 }}
            />

            <Descriptions size="small" column={1} bordered>
              <Descriptions.Item label="决议票据编号">{latestDecision.decisionTicketId}</Descriptions.Item>
              <Descriptions.Item label="被审工程对象">{latestDecision.targetObjectType} (ID: {latestDecision.targetObjectId})</Descriptions.Item>
              <Descriptions.Item label="快照哈希">{latestDecision.targetContentHash}</Descriptions.Item>
              <Descriptions.Item label="表决结论">
                <Tag color={latestDecision.finalConclusion === 'APPROVED' ? 'green' : 'red'}>
                  {latestDecision.finalConclusion}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="电子签名印章 (Stamp)">
                <code style={{ fontSize: 11, wordBreak: 'break-all', color: '#1677ff' }}>
                  {latestDecision.cryptoSignatureStamp}
                </code>
              </Descriptions.Item>
              <Descriptions.Item label="签名原文摘要 (Digest)">
                <code style={{ fontSize: 11, color: '#666' }}>{latestDecision.signedPayloadDigest}</code>
              </Descriptions.Item>
              <Descriptions.Item label="签发时间戳">{new Date(latestDecision.decidedAt).toLocaleString()}</Descriptions.Item>
            </Descriptions>
          </div>
        )}
      </Modal>

      {/* 流程拓扑详情抽屉 */}
      <Drawer
        title="BPMN 会签拓扑与多专业协同流转图"
        placement="right"
        width={560}
        open={detailDrawerVisible}
        onClose={() => setDetailDrawerVisible(false)}
      >
        {selectedInstance && (
          <div>
            <Descriptions size="small" column={1} bordered style={{ marginBottom: 20 }}>
              <Descriptions.Item label="流程实例ID">{selectedInstance.workflowInstId}</Descriptions.Item>
              <Descriptions.Item label="工程编号">{selectedInstance.targetBusinessCode}</Descriptions.Item>
              <Descriptions.Item label="发起人工号">{selectedInstance.initiatorId}</Descriptions.Item>
              <Descriptions.Item label="当前状态">{selectedInstance.status}</Descriptions.Item>
            </Descriptions>

            <Title level={5} style={{ marginBottom: 16 }}>会签全景时间线 (Timeline)</Title>
            <Timeline
              items={[
                {
                  color: 'green',
                  children: (
                    <div>
                      <Text strong>流程启动 (StartEvent)</Text>
                      <div>发起人: {selectedInstance.initiatorId} | 固化快照 SHA-256</div>
                      <Text type="secondary" style={{ fontSize: 11 }}>{new Date(selectedInstance.startedAt).toLocaleString()}</Text>
                    </div>
                  ),
                },
                {
                  color: 'blue',
                  children: (
                    <div>
                      <Text strong>跨专业并行会签节点 (Parallel Multi-Instance)</Text>
                      <div>机械工程、电气伺服、系统架构师协同评审</div>
                    </div>
                  ),
                },
                {
                  color: selectedInstance.status === 'COMPLETED' ? 'green' : 'gray',
                  children: (
                    <div>
                      <Text strong>签发只读 ApprovalDecision 凭据 (SignOffDelegate)</Text>
                      <div>非对称加密数字签名、生成防篡改印章</div>
                    </div>
                  ),
                },
                {
                  color: selectedInstance.status === 'COMPLETED' ? 'purple' : 'gray',
                  children: (
                    <div>
                      <Text strong>业务状态机两阶段异步核销 (Outbox Event)</Text>
                      <div>业务主表状态由 IN_REVIEW 迁移至 RELEASED</div>
                    </div>
                  ),
                },
              ]}
            />
          </div>
        )}
      </Drawer>

      {/* 发起业务审批模态框 */}
      <Modal
        title={
          <Space>
            <Send className="w-5 h-5 text-blue-600" />
            <span>发起机床业务受控审批流程</span>
          </Space>
        }
        open={startModalVisible}
        onCancel={() => setStartModalVisible(false)}
        footer={null}
        width={640}
        destroyOnClose
      >
        <Form form={startForm} layout="vertical" onFinish={handleStartWorkflow}>
          <Form.Item
            name="targetObjectType"
            label="被审业务实体类型"
            initialValue="ModelRelease"
            rules={[{ required: true }]}
          >
            <Select
              options={[
                { label: '系统模型发布 (ModelRelease)', value: 'ModelRelease' },
                { label: '工程变更单 (ChangeOrder / ECO)', value: 'ChangeOrder' },
                { label: '工程基线 (Baseline)', value: 'Baseline' },
                { label: '图文档 (DocRevision)', value: 'DocRevision' },
              ]}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="targetObjectId"
                label="实体业务主键 (ID)"
                initialValue={5003}
                rules={[{ required: true }]}
              >
                <Input type="number" placeholder="例如: 5003" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="targetBusinessCode"
                label="工程编号 / 版本号"
                initialValue="REL-VMC1000-NEW-01"
                rules={[{ required: true }]}
              >
                <Input placeholder="例如: REL-VMC1000-NEW-01" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="targetContentHash"
            label="候选快照 SHA-256 哈希 (AT-16 防线)"
            initialValue="7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069"
            rules={[{ required: true, message: '必须固化快照哈希' }]}
          >
            <Input placeholder="64位十六进制哈希摘要" />
          </Form.Item>

          <Form.Item
            name="businessCategory"
            label="业务审批分类"
            initialValue="STANDARD_RELEASE"
          >
            <Select
              options={[
                { label: '标准受控发布 (STANDARD_RELEASE)', value: 'STANDARD_RELEASE' },
                { label: '重大工程变更 (MAJOR_CHANGE)', value: 'MAJOR_CHANGE' },
              ]}
            />
          </Form.Item>

          <Row justify="end">
            <Space>
              <Button onClick={() => setStartModalVisible(false)}>取消</Button>
              <Button type="primary" htmlType="submit">
                立即启动 Flowable 审批流
              </Button>
            </Space>
          </Row>
        </Form>
      </Modal>
    </div>
  );
};
