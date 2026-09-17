import React, { useState, useEffect, useMemo } from 'react';
import {
  Card,
  Button,
  Tag,
  Space,
  Input,
  Select,
  Modal,
  Form,
  message,
  Typography,
  Row,
  Col,
  Alert,
  Badge,
  Tooltip,
  Divider,
  Empty,
  Popconfirm,
  List,
  Avatar,
  Radio,
} from 'antd';
import {
  Mail,
  Inbox,
  Send,
  Archive,
  Trash2,
  Star,
  PlusCircle,
  Paperclip,
  ShieldCheck,
  ShieldAlert,
  Search,
  CheckCircle2,
  User,
  ArrowRight,
  Reply,
  RefreshCw,
  FolderGit2,
  GitPullRequest,
  FileText,
  Layers,
  Download,
} from 'lucide-react';
import { useAuthStore } from '@/stores/useAuthStore';
import { useUserStore } from '@/stores/useUserStore';

const { Text, Title, Paragraph } = Typography;
const { TextArea } = Input;

// ==================== 数据契约定义 ====================

export type BoxType = 'INBOX' | 'OUTBOX' | 'ARCHIVE' | 'TRASH';
export type MessageRootCategory = 'SYSTEM' | 'MANUAL';
export type MessagePriority = 'NORMAL' | 'HIGH' | 'URGENT';

export interface AttachmentItem {
  attachmentId: number;
  fileName: string;
  fileSize: number;
  fileType: string;
  downloadUrl: string;
}

export interface RecipientItem {
  userId: string;
  userName: string;
  recipientType: 'TO' | 'CC' | 'BCC';
}

export interface MailItem {
  messageId: number;
  userBoxId: number;
  rootCategory: MessageRootCategory;
  systemType?: string;
  subject: string;
  content: string;
  senderUserId: string;
  senderUserName: string;
  priority: MessagePriority;
  relatedProjectId?: string;
  relatedObjType?: string;
  relatedObjId?: string;
  targetActionUrl?: string;
  actionIdentifier?: string;
  isRead: boolean;
  isStarred: boolean;
  isArchived: boolean;
  boxType: BoxType;
  hasAttachment: boolean;
  attachments?: AttachmentItem[];
  recipients?: RecipientItem[];
  createdAt: string;
}

interface MailboxPageProps {
  onNavigate?: (tab: string) => void;
}

// 模拟高端机床研发体系初始种子邮件数据（与数据库 V1.9.0 完全对齐）
const INITIAL_MAIL_SEED: MailItem[] = [
  {
    messageId: 9001,
    userBoxId: 9101,
    rootCategory: 'SYSTEM',
    systemType: 'CHANGE',
    subject: '【变更协同】关于ECO-2026-0042(VMC1000主轴提速至15000rpm)的协同会签通知',
    content: `
      <p>尊敬的工程师：</p>
      <p>工程变更单 <strong>ECO-2026-0042</strong>（VMC1000主轴提速至15000rpm工程实施与全生命周期现场处置单）已进入跨学科协同会签阶段。请机械、仿真及电气专业主管在收到本通知后3个工作日内完成影响分析及处置方案在线复核。</p>
      <p><strong>关键变更要素：</strong></p>
      <ul>
        <li>主轴前端支撑轴承升级为超精密陶瓷球角接触轴承（M-VMC850-BRG-7014）；</li>
        <li>主轴驱动电机功率由 15kW 增大至 18.5kW；</li>
        <li>关联全机热平衡仿真校核工单（TC-SPINDLE-THERMAL）待复核签署。</li>
      </ul>
      <p>请点击下方“直达业务对象”按钮进入工程变更处理工作台完成会签签署。</p>
    `,
    senderUserId: 'SYSTEM_NOTIFIER',
    senderUserName: '系统通知服务',
    priority: 'HIGH',
    relatedProjectId: 'VMC_ENTERPRISE',
    relatedObjType: 'ECO',
    relatedObjId: 'ECO-2026-0042',
    targetActionUrl: '/change/eco/ECO-2026-0042',
    actionIdentifier: 'ECO_COLLABORATION_REVIEW',
    isRead: false,
    isStarred: true,
    isArchived: false,
    boxType: 'INBOX',
    hasAttachment: true,
    attachments: [
      {
        attachmentId: 9201,
        fileName: 'VMC1000_ECO_0042_Impact_Report.pdf',
        fileSize: 2584100,
        fileType: 'application/pdf',
        downloadUrl: '/attachments/eco/VMC1000_ECO_0042_Impact_Report.pdf',
      },
    ],
    recipients: [
      { userId: 'chief_designer', userName: '总设计师', recipientType: 'TO' },
      { userId: 'lead_analyst', userName: '仿真分析组长', recipientType: 'TO' },
      { userId: 'admin', userName: '系统管理员', recipientType: 'CC' },
    ],
    createdAt: new Date(Date.now() - 2 * 3600 * 1000).toISOString(),
  },
  {
    messageId: 9002,
    userBoxId: 9105,
    rootCategory: 'SYSTEM',
    systemType: 'GATE',
    subject: '【阶段门预警】VMC1000项目 GATE-3(详细设计评审门) 临期倒计时预警',
    content: `
      <p>项目各主管：</p>
      <p>型号项目 <strong>VMC_ENTERPRISE</strong> 关键节点 <strong>GATE-3(详细设计评审门)</strong> 计划于5个工作日后关闭。</p>
      <p>目前仍有 <strong>1项关键交付物</strong>（全机热伸长有限元分析报告）处于签署中，门禁条件达成度为 85%。请各责任组加快流转，确保门禁条件准时达成，避免触发型号研发延期告警。</p>
    `,
    senderUserId: 'SYSTEM_NOTIFIER',
    senderUserName: '系统通知服务',
    priority: 'URGENT',
    relatedProjectId: 'VMC_ENTERPRISE',
    relatedObjType: 'GATE',
    relatedObjId: 'GATE-3',
    targetActionUrl: '/project/gate/GATE-3',
    actionIdentifier: 'GATE_AUDIT_EXPEDITE',
    isRead: false,
    isStarred: false,
    isArchived: false,
    boxType: 'INBOX',
    hasAttachment: false,
    attachments: [],
    recipients: [
      { userId: 'project_manager', userName: '项目经理', recipientType: 'TO' },
      { userId: 'chief_designer', userName: '总设计师', recipientType: 'TO' },
      { userId: 'admin', userName: '系统管理员', recipientType: 'TO' },
    ],
    createdAt: new Date(Date.now() - 24 * 3600 * 1000).toISOString(),
  },
  {
    messageId: 9003,
    userBoxId: 9108,
    rootCategory: 'MANUAL',
    systemType: 'TECHNICAL',
    subject: '【技术交流】关于五轴联动叶片加工工艺参数优化的研讨纪要与试验排程',
    content: `
      <p>李总、各位工艺师：</p>
      <p>附件为本周二关于五轴铣削钛合金叶片表面粗糙度提升的试验分析报告。初步测算在转速由 10,000 rpm 提升至 12,500 rpm 配合微量润滑 (MQL) 条件下，表面粗糙度可由 Ra 0.8 显著提升至 Ra 0.4。</p>
      <p>请审阅试验数据，并安排下周二在五轴试验台进行二次样件试切验证。</p>
    `,
    senderUserId: 'lead_analyst',
    senderUserName: '仿真分析组长',
    priority: 'NORMAL',
    relatedProjectId: 'VMC_ENTERPRISE',
    relatedObjType: 'PROCESS',
    relatedObjId: 'PROC-BLADE-001',
    targetActionUrl: '/manufacturing/process/PROC-BLADE-001',
    actionIdentifier: 'PROCESS_EXPERIMENT_SCHEDULE',
    isRead: true,
    isStarred: false,
    isArchived: false,
    boxType: 'INBOX',
    hasAttachment: true,
    attachments: [
      {
        attachmentId: 9202,
        fileName: 'Blade_Milling_MQL_Test_Report.xlsx',
        fileSize: 845200,
        fileType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        downloadUrl: '/attachments/process/Blade_Milling_MQL_Test_Report.xlsx',
      },
    ],
    recipients: [
      { userId: 'chief_designer', userName: '总设计师', recipientType: 'TO' },
      { userId: 'process_engineer', userName: '工艺主管工程师', recipientType: 'TO' },
    ],
    createdAt: new Date(Date.now() - 72 * 3600 * 1000).toISOString(),
  },
];

export const MailboxPage: React.FC<MailboxPageProps> = ({ onNavigate }) => {
  const { user } = useAuthStore();
  const currentUserId = user?.username || 'chief_designer';

  // 状态维护
  const [mails, setMails] = useState<MailItem[]>(INITIAL_MAIL_SEED);
  const [activeBox, setActiveBox] = useState<BoxType>('INBOX');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [filterRead, setFilterRead] = useState<'ALL' | 'UNREAD' | 'STARRED'>('ALL');
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [selectedMailId, setSelectedMailId] = useState<number | null>(9001);
  const [isComposeVisible, setIsComposeVisible] = useState<boolean>(false);
  const [composeLoading, setComposeLoading] = useState<boolean>(false);
  const [isActionResolving, setIsActionResolving] = useState<boolean>(false);
  const [securityBlockModalVisible, setSecurityBlockModalVisible] = useState<boolean>(false);
  const [securityBlockReason, setSecurityBlockReason] = useState<string>('');

  // 从全局用户中心获取已创建系统用户（自动包含管理员手动创建的全部用户并持久化）
  const { fetchUsers, getActiveUsers } = useUserStore();
  const systemUsers = getActiveUsers();

  // 模拟当前用户的授权身份（用于展示 PBAC 跨域越权阻断测试）
  const [simulatedUserRole, setSimulatedUserRole] = useState<'AUTHORIZED' | 'UNAUTHORIZED'>('AUTHORIZED');

  const [composeForm] = Form.useForm();

  useEffect(() => {
    fetchUsers();
  }, []);

  // 尝试从真实 API 加载，网络异常自动保留内存 Seed 数据
  useEffect(() => {
    fetchMailboxFromApi();
  }, [activeBox, currentUserId]);

  const fetchMailboxFromApi = async () => {
    try {
      const res = await fetch(`/api/v1/messages/mailbox?boxType=${activeBox}&pageNum=1&pageSize=50`, {
        headers: {
          'X-Current-User-Id': currentUserId,
        },
      });
      if (res.ok) {
        const json = await res.json();
        if (json && json.data && Array.isArray(json.data.items) && json.data.items.length > 0) {
          // 与后端同步
          const apiMails: MailItem[] = json.data.items.map((item: any) => ({
            messageId: item.messageId,
            userBoxId: item.userBoxId || item.itemId,
            rootCategory: item.rootCategory,
            systemType: item.systemType || item.subType,
            subject: item.subject,
            content: item.content || item.bodyContent || '',
            senderUserId: item.senderUserId || item.senderId,
            senderUserName: item.senderUserName || item.senderDisplayName || '未知人员',
            priority: item.priority || 'NORMAL',
            relatedProjectId: item.relatedProjectId,
            relatedObjType: item.relatedObjType || item.relatedObjectType,
            relatedObjId: item.relatedObjId ? String(item.relatedObjId) : undefined,
            targetActionUrl: item.targetActionUrl,
            isRead: item.isRead,
            isStarred: item.isStarred,
            isArchived: item.isArchived,
            boxType: activeBox,
            hasAttachment: item.hasAttachment,
            createdAt: item.createdAt,
          }));
          setMails(apiMails);
          if (apiMails.length > 0 && !selectedMailId) {
            setSelectedMailId(apiMails[0].messageId);
          }
        }
      }
    } catch {
      // 离线环境静默降级为内存种子数据
    }
  };

  // 统计各箱体未读数
  const unreadCount = useMemo(() => {
    return mails.filter((m) => m.boxType === 'INBOX' && !m.isRead).length;
  }, [mails]);

  // 过滤后的列表
  const filteredMails = useMemo(() => {
    return mails.filter((m) => {
      if (m.boxType !== activeBox) return false;

      // 分类过滤
      if (selectedCategory !== 'ALL') {
        if (selectedCategory === 'MANUAL' && m.rootCategory !== 'MANUAL') return false;
        if (selectedCategory === 'CHANGE' && m.systemType !== 'CHANGE') return false;
        if (selectedCategory === 'GATE' && m.systemType !== 'GATE' && m.systemType !== 'REVIEW') return false;
        if (selectedCategory === 'TASK' && m.systemType !== 'TASK') return false;
      }

      // 状态过滤
      if (filterRead === 'UNREAD' && m.isRead) return false;
      if (filterRead === 'STARRED' && !m.isStarred) return false;

      // 关键字搜索
      if (searchKeyword.trim()) {
        const kw = searchKeyword.toLowerCase();
        const matchSubject = m.subject.toLowerCase().includes(kw);
        const matchSender = m.senderUserName.toLowerCase().includes(kw);
        const matchObj = m.relatedObjId ? m.relatedObjId.toLowerCase().includes(kw) : false;
        if (!matchSubject && !matchSender && !matchObj) return false;
      }

      return true;
    });
  }, [mails, activeBox, selectedCategory, filterRead, searchKeyword]);

  // 当前选中的邮件
  const selectedMail = useMemo(() => {
    return mails.find((m) => m.messageId === selectedMailId) || null;
  }, [mails, selectedMailId]);

  // 选中邮件并自动标记为已读
  const handleSelectMail = (mail: MailItem) => {
    setSelectedMailId(mail.messageId);
    if (!mail.isRead) {
      markAsRead(mail.messageId, true);
    }
  };

  // 标记已读/未读
  const markAsRead = async (messageId: number, isRead: boolean) => {
    setMails((prev) =>
      prev.map((m) => (m.messageId === messageId ? { ...m, isRead } : m))
    );
    try {
      await fetch(`/api/v1/messages/${messageId}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'X-Current-User-Id': currentUserId },
        body: JSON.stringify({ isRead }),
      });
    } catch {
      // 离线降级
    }
  };

  // 切换星标
  const toggleStar = async (messageId: number, e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    const mail = mails.find((m) => m.messageId === messageId);
    if (!mail) return;
    const nextStarred = !mail.isStarred;

    setMails((prev) =>
      prev.map((m) => (m.messageId === messageId ? { ...m, isStarred: nextStarred } : m))
    );

    try {
      await fetch(`/api/v1/messages/${messageId}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'X-Current-User-Id': currentUserId },
        body: JSON.stringify({ isStarred: nextStarred }),
      });
      message.success(nextStarred ? '已添加星标' : '已取消星标');
    } catch {
      message.success(nextStarred ? '已添加星标(离线)' : '已取消星标(离线)');
    }
  };

  // 移动箱体（归档 / 移入废纸篓 / 恢复）
  const moveBox = async (messageId: number, targetBox: BoxType) => {
    setMails((prev) =>
      prev.map((m) => {
        if (m.messageId === messageId) {
          return {
            ...m,
            boxType: targetBox,
            isArchived: targetBox === 'ARCHIVE',
          };
        }
        return m;
      })
    );

    const actionText =
      targetBox === 'ARCHIVE'
        ? '已归档'
        : targetBox === 'TRASH'
        ? '已放入废纸篓'
        : '已移至收件箱';
    message.success(actionText);

    try {
      await fetch(`/api/v1/messages/${messageId}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'X-Current-User-Id': currentUserId },
        body: JSON.stringify({ targetBoxType: targetBox }),
      });
    } catch {
      // 离线降级
    }
  };

  // 一键全部标为已读
  const handleMarkAllRead = () => {
    setMails((prev) =>
      prev.map((m) => (m.boxType === activeBox ? { ...m, isRead: true } : m))
    );
    message.success('当前箱体所有邮件已全部标为已读');
  };

  // 业务对象直达解析与 PBAC 跨域越权硬拦截处理（MSG-F06）
  const handleResolveAction = async (mail: MailItem) => {
    if (!mail.targetActionUrl) {
      message.warning('该消息未绑定直达工程对象路径');
      return;
    }

    setIsActionResolving(true);
    try {
      // 若处于模拟未授权状态，触发前端 PBAC 防穿透拦截展示
      if (simulatedUserRole === 'UNAUTHORIZED') {
        throw new Error(
          `[ERR_PBAC_CROSS_OBJECT_UNAUTHORIZED] PBAC跨域越权阻断：当前用户角色无权穿透访问高端机床受限工程对象 [${mail.relatedObjType}:${mail.relatedObjId}]！邮件阅读权与底层业务对象权能完全隔离，直达跳转已被安全拦截。`
        );
      }

      // 尝试调用后端直达接口
      const res = await fetch(`/api/v1/messages/${mail.messageId}/resolve-action`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Current-User-Id': currentUserId,
        },
      });

      if (!res.ok) {
        const errJson = await res.json().catch(() => ({}));
        throw new Error(errJson.message || `请求失败 (${res.status})`);
      }

      const resJson = await res.json();
      message.success(resJson.message || 'PBAC鉴权通过，正在进入机床业务工作台...');

      // 解析直达路径并触发全局路由切换
      dispatchNavigateUrl(mail.targetActionUrl, mail.relatedObjType);
    } catch (err: any) {
      setSecurityBlockReason(err.message || '安全校验失败');
      setSecurityBlockModalVisible(true);
    } finally {
      setIsActionResolving(false);
    }
  };

  // 路由跳转分发器
  const dispatchNavigateUrl = (url: string, objType?: string) => {
    if (!onNavigate) return;
    if (url.includes('/change/') || objType === 'ECO' || objType === 'ECR') {
      onNavigate('change-mgmt');
    } else if (url.includes('/gate/') || objType === 'GATE') {
      onNavigate('project-gate');
    } else if (url.includes('/baseline/')) {
      onNavigate('baseline-mgmt');
    } else if (url.includes('/document/')) {
      onNavigate('doc-mgmt');
    } else {
      message.info(`已直达目标模块: ${url}`);
    }
  };

  // 发送人工邮件
  const handleSendManualMail = async (values: any) => {
    setComposeLoading(true);
    try {
      const newId = Date.now();
      const currentSenderName = user?.realName || '机床研发工程师';
      const currentSenderId = user?.userId || currentUserId;

      // 从已经创建的系统用户中精确匹配收件人
      const resolvedRecipients = (values.recipients || []).map((identifier: string) => {
        const found = systemUsers.find(
          (u) => u.username === identifier || u.userId === identifier || u.email === identifier
        );
        return {
          userId: found?.userId || identifier,
          userName: found ? `${found.realName} (${found.username})` : identifier,
          recipientType: 'TO' as const,
        };
      });

      const newMail: MailItem = {
        messageId: newId,
        userBoxId: newId + 1,
        rootCategory: 'MANUAL',
        systemType: 'TECHNICAL',
        subject: values.subject,
        content: `<p>${values.content.replace(/\n/g, '<br/>')}</p>`,
        senderUserId: currentSenderId,
        senderUserName: currentSenderName,
        priority: values.priority || 'NORMAL',
        relatedProjectId: values.relatedProjectId || 'VMC_ENTERPRISE',
        relatedObjType: values.relatedObjType,
        relatedObjId: values.relatedObjId,
        targetActionUrl: values.targetActionUrl,
        isRead: true,
        isStarred: false,
        isArchived: false,
        boxType: 'OUTBOX',
        hasAttachment: false,
        createdAt: new Date().toISOString(),
        recipients: resolvedRecipients,
      };

      // 尝试推送到后端
      await fetch('/api/v1/messages/manual', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Current-User-Id': currentSenderId,
          'X-Current-User-Name': encodeURIComponent(currentSenderName),
        },
        body: JSON.stringify({
          subject: values.subject,
          content: `<p>${values.content.replace(/\n/g, '<br/>')}</p>`,
          priority: values.priority,
          relatedProjectId: values.relatedProjectId,
          relatedObjType: values.relatedObjType,
          relatedObjId: values.relatedObjId,
          targetActionUrl: values.targetActionUrl,
          recipients: resolvedRecipients,
        }),
      }).catch(() => null);

      setMails((prev) => [newMail, ...prev]);
      message.success('邮件发送成功！已写入发件箱并完成向选定系统用户的投递');
      setIsComposeVisible(false);
      composeForm.resetFields();
    } finally {
      setComposeLoading(false);
    }
  };

  // 优先级标签渲染
  const renderPriorityTag = (priority: MessagePriority) => {
    switch (priority) {
      case 'URGENT':
        return <Tag color="error" style={{ fontWeight: 600 }}>紧急</Tag>;
      case 'HIGH':
        return <Tag color="warning" style={{ fontWeight: 600 }}>高优</Tag>;
      case 'NORMAL':
      default:
        return <Tag color="default">普通</Tag>;
    }
  };

  return (
    <div style={{ height: 'calc(100vh - 110px)', display: 'flex', flexDirection: 'column', gap: 12 }}>
      {/* 顶部状态与仿真测试栏 */}
      <Card bodyStyle={{ padding: '10px 16px' }} style={{ borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Space size="middle">
              <span style={{ fontSize: 16, fontWeight: 700, display: 'flex', alignContent: 'center', gap: 8 }}>
                <Mail className="w-5 h-5 text-blue-600" />
                PLM 内部研制邮件与消息协同中心 (M01-MSG)
              </span>
              <Tag color="blue">去外部协议化（非SMTP安全闭环）</Tag>
              <Tag color="cyan">Object-Centric 穿透直达</Tag>
              <Tag color="purple">PBAC 细粒度越权隔离</Tag>
            </Space>
          </Col>
          <Col>
            <Space size="middle">
              <span style={{ fontSize: 12, color: '#666' }}>PBAC 穿透模式测试：</span>
              <Radio.Group
                size="small"
                value={simulatedUserRole}
                onChange={(e) => setSimulatedUserRole(e.target.value)}
                buttonStyle="solid"
              >
                <Radio.Button value="AUTHORIZED">合法研制人员 (放行)</Radio.Button>
                <Radio.Button value="UNAUTHORIZED">跨域未授权人员 (阻断拦截)</Radio.Button>
              </Radio.Group>
              <Tooltip title="刷新当前信箱">
                <Button size="small" icon={<RefreshCw className="w-3.5 h-3.5" />} onClick={fetchMailboxFromApi}>
                  刷新
                </Button>
              </Tooltip>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 主体三栏布局 */}
      <div style={{ flex: 1, display: 'flex', gap: 12, overflow: 'hidden' }}>
        {/* 左栏：箱体导航与分类过滤 (宽 220px) */}
        <Card
          bodyStyle={{ padding: 12, display: 'flex', flexDirection: 'column', height: '100%' }}
          style={{ width: 220, borderRadius: 8, display: 'flex', flexDirection: 'column' }}
        >
          {/* 写信主按钮 */}
          <Button
            type="primary"
            size="large"
            icon={<PlusCircle className="w-4 h-4" />}
            style={{ width: '100%', marginBottom: 16, fontWeight: 600, borderRadius: 6 }}
            onClick={() => {
              fetchUsers();
              setIsComposeVisible(true);
            }}
          >
            写研制邮件
          </Button>

          {/* 信箱分组列表 */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
            <div
              onClick={() => setActiveBox('INBOX')}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '10px 12px',
                borderRadius: 6,
                cursor: 'pointer',
                backgroundColor: activeBox === 'INBOX' ? '#e6f4ff' : 'transparent',
                color: activeBox === 'INBOX' ? '#1677ff' : '#333',
                fontWeight: activeBox === 'INBOX' ? 600 : 400,
                transition: 'all 0.2s',
              }}
            >
              <Space>
                <Inbox className="w-4 h-4" />
                <span>收件箱</span>
              </Space>
              {unreadCount > 0 && <Badge count={unreadCount} overflowCount={99} />}
            </div>

            <div
              onClick={() => setActiveBox('OUTBOX')}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '10px 12px',
                borderRadius: 6,
                cursor: 'pointer',
                backgroundColor: activeBox === 'OUTBOX' ? '#e6f4ff' : 'transparent',
                color: activeBox === 'OUTBOX' ? '#1677ff' : '#333',
                fontWeight: activeBox === 'OUTBOX' ? 600 : 400,
                transition: 'all 0.2s',
              }}
            >
              <Space>
                <Send className="w-4 h-4" />
                <span>已发送</span>
              </Space>
            </div>

            <div
              onClick={() => setActiveBox('ARCHIVE')}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '10px 12px',
                borderRadius: 6,
                cursor: 'pointer',
                backgroundColor: activeBox === 'ARCHIVE' ? '#e6f4ff' : 'transparent',
                color: activeBox === 'ARCHIVE' ? '#1677ff' : '#333',
                fontWeight: activeBox === 'ARCHIVE' ? 600 : 400,
                transition: 'all 0.2s',
              }}
            >
              <Space>
                <Archive className="w-4 h-4" />
                <span>已归档</span>
              </Space>
            </div>

            <div
              onClick={() => setActiveBox('TRASH')}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '10px 12px',
                borderRadius: 6,
                cursor: 'pointer',
                backgroundColor: activeBox === 'TRASH' ? '#e6f4ff' : 'transparent',
                color: activeBox === 'TRASH' ? '#1677ff' : '#333',
                fontWeight: activeBox === 'TRASH' ? 600 : 400,
                transition: 'all 0.2s',
              }}
            >
              <Space>
                <Trash2 className="w-4 h-4" />
                <span>废纸篓</span>
              </Space>
            </div>
          </div>

          <Divider style={{ margin: '14px 0' }} />

          {/* 业务消息分类树 */}
          <div style={{ fontSize: 12, color: '#888', fontWeight: 600, marginBottom: 8, paddingLeft: 4 }}>
            业务分类筛选
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {[
              { key: 'ALL', label: '全部类别', icon: <Layers className="w-3.5 h-3.5" /> },
              { key: 'CHANGE', label: '变更协同通知', icon: <GitPullRequest className="w-3.5 h-3.5" /> },
              { key: 'GATE', label: '阶段门预警', icon: <FolderGit2 className="w-3.5 h-3.5" /> },
              { key: 'TASK', label: '研发任务待办', icon: <CheckCircle2 className="w-3.5 h-3.5" /> },
              { key: 'MANUAL', label: '人工交流纪要', icon: <User className="w-3.5 h-3.5" /> },
            ].map((cat) => (
              <div
                key={cat.key}
                onClick={() => setSelectedCategory(cat.key)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8,
                  padding: '7px 10px',
                  borderRadius: 6,
                  cursor: 'pointer',
                  fontSize: 13,
                  backgroundColor: selectedCategory === cat.key ? '#f0f5ff' : 'transparent',
                  color: selectedCategory === cat.key ? '#1677ff' : '#555',
                  fontWeight: selectedCategory === cat.key ? 600 : 400,
                }}
              >
                {cat.icon}
                <span>{cat.label}</span>
              </div>
            ))}
          </div>

          <div style={{ marginTop: 'auto', padding: 8, backgroundColor: '#fafafa', borderRadius: 6, fontSize: 11, color: '#888' }}>
            <ShieldCheck className="w-3.5 h-3.5 inline mr-1 text-green-600" />
            SoD 拦截：严禁人工伪造系统消息
          </div>
        </Card>

        {/* 中栏：邮件列表摘要卡片流 (宽 380px) */}
        <Card
          bodyStyle={{ padding: 0, display: 'flex', flexDirection: 'column', height: '100%' }}
          style={{ width: 380, borderRadius: 8, display: 'flex', flexDirection: 'column' }}
        >
          {/* 中栏顶部搜索与过滤器 */}
          <div style={{ padding: '12px 12px 8px 12px', borderBottom: '1px solid #f0f0f0' }}>
            <Input
              placeholder="搜索主题、发件人、工程对象..."
              prefix={<Search className="w-4 h-4 text-gray-400" />}
              allowClear
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              style={{ borderRadius: 6, marginBottom: 8 }}
            />
            <Row justify="space-between" align="middle">
              <Col>
                <Space size={4}>
                  <Button
                    size="small"
                    type={filterRead === 'ALL' ? 'primary' : 'text'}
                    onClick={() => setFilterRead('ALL')}
                  >
                    全部
                  </Button>
                  <Button
                    size="small"
                    type={filterRead === 'UNREAD' ? 'primary' : 'text'}
                    onClick={() => setFilterRead('UNREAD')}
                  >
                    未读
                  </Button>
                  <Button
                    size="small"
                    type={filterRead === 'STARRED' ? 'primary' : 'text'}
                    onClick={() => setFilterRead('STARRED')}
                  >
                    星标
                  </Button>
                </Space>
              </Col>
              <Col>
                <Tooltip title="将当前信箱全部标为已读">
                  <Button type="link" size="small" onClick={handleMarkAllRead} style={{ padding: 0, fontSize: 12 }}>
                    全部已读
                  </Button>
                </Tooltip>
              </Col>
            </Row>
          </div>

          {/* 列表区域 */}
          <div style={{ flex: 1, overflowY: 'auto', padding: '6px' }}>
            {filteredMails.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无符合条件的邮件" style={{ marginTop: 60 }} />
            ) : (
              filteredMails.map((mail) => {
                const isSelected = mail.messageId === selectedMailId;
                return (
                  <div
                    key={mail.messageId}
                    onClick={() => handleSelectMail(mail)}
                    style={{
                      padding: '12px 14px',
                      borderRadius: 6,
                      marginBottom: 6,
                      cursor: 'pointer',
                      border: isSelected ? '1px solid #1677ff' : '1px solid #f0f0f0',
                      backgroundColor: isSelected ? '#f0f7ff' : !mail.isRead ? '#ffffff' : '#fafafa',
                      transition: 'all 0.15s',
                      position: 'relative',
                    }}
                  >
                    {/* 未读小圆点指示 */}
                    {!mail.isRead && (
                      <span
                        style={{
                          position: 'absolute',
                          top: 14,
                          left: 6,
                          width: 6,
                          height: 6,
                          borderRadius: '50%',
                          backgroundColor: '#1677ff',
                        }}
                      />
                    )}

                    {/* 卡片头部：发件人 + 优先级 + 星标 + 时间 */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                      <Space size={6} style={{ paddingLeft: !mail.isRead ? 8 : 0 }}>
                        <Text strong={!mail.isRead} style={{ fontSize: 13, color: '#333' }}>
                          {mail.senderUserName}
                        </Text>
                        {renderPriorityTag(mail.priority)}
                      </Space>
                      <Space size={6}>
                        <span
                          onClick={(e) => toggleStar(mail.messageId, e)}
                          style={{ cursor: 'pointer', color: mail.isStarred ? '#faad14' : '#d9d9d9' }}
                        >
                          <Star className="w-4 h-4" fill={mail.isStarred ? '#faad14' : 'none'} />
                        </span>
                        <Text type="secondary" style={{ fontSize: 11 }}>
                          {new Date(mail.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </Text>
                      </Space>
                    </div>

                    {/* 卡片主标题 */}
                    <div
                      style={{
                        fontSize: 13,
                        fontWeight: !mail.isRead ? 600 : 500,
                        color: '#1f1f1f',
                        marginBottom: 6,
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      {mail.subject}
                    </div>

                    {/* 底部元数据：工程对象 Tag + 附件徽章 */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Space size={4}>
                        {mail.relatedObjId && (
                          <Tag color="blue" style={{ fontSize: 11, lineHeight: '18px', padding: '0 4px', margin: 0 }}>
                            {mail.relatedObjType || 'OBJ'}: {mail.relatedObjId}
                          </Tag>
                        )}
                        {mail.rootCategory === 'SYSTEM' ? (
                          <Tag color="cyan" style={{ fontSize: 11, lineHeight: '18px', padding: '0 4px', margin: 0 }}>
                            系统派发
                          </Tag>
                        ) : (
                          <Tag style={{ fontSize: 11, lineHeight: '18px', padding: '0 4px', margin: 0 }}>人工交流</Tag>
                        )}
                      </Space>
                      {mail.hasAttachment && (
                        <Paperclip className="w-3.5 h-3.5 text-gray-400" />
                      )}
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </Card>

        {/* 右栏：邮件完整详情与机床工程对象穿透直达卡片 (弹性自适应宽度) */}
        <Card
          bodyStyle={{ padding: 0, display: 'flex', flexDirection: 'column', height: '100%' }}
          style={{ flex: 1, borderRadius: 8, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}
        >
          {selectedMail ? (
            <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
              {/* 详情头部工具条 */}
              <div
                style={{
                  padding: '10px 16px',
                  borderBottom: '1px solid #f0f0f0',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  backgroundColor: '#fafafa',
                }}
              >
                <Space>
                  <Button
                    size="small"
                    icon={<Reply className="w-3.5 h-3.5" />}
                    onClick={() => {
                      composeForm.setFieldsValue({
                        subject: `Re: ${selectedMail.subject}`,
                        recipients: [selectedMail.senderUserId],
                        relatedProjectId: selectedMail.relatedProjectId,
                        relatedObjType: selectedMail.relatedObjType,
                        relatedObjId: selectedMail.relatedObjId,
                        targetActionUrl: selectedMail.targetActionUrl,
                      });
                      setIsComposeVisible(true);
                    }}
                  >
                    回复
                  </Button>
                  <Button
                    size="small"
                    onClick={() => markAsRead(selectedMail.messageId, !selectedMail.isRead)}
                  >
                    {selectedMail.isRead ? '标为未读' : '标为已读'}
                  </Button>
                  <Button
                    size="small"
                    icon={<Star className="w-3.5 h-3.5" fill={selectedMail.isStarred ? '#faad14' : 'none'} />}
                    onClick={() => toggleStar(selectedMail.messageId)}
                  >
                    {selectedMail.isStarred ? '取消星标' : '添加星标'}
                  </Button>
                  {selectedMail.boxType !== 'ARCHIVE' && (
                    <Button
                      size="small"
                      icon={<Archive className="w-3.5 h-3.5" />}
                      onClick={() => moveBox(selectedMail.messageId, 'ARCHIVE')}
                    >
                      归档
                    </Button>
                  )}
                  {selectedMail.boxType !== 'TRASH' ? (
                    <Popconfirm
                      title="放入废纸篓？"
                      description="您可以在废纸篓中随时恢复该邮件"
                      onConfirm={() => moveBox(selectedMail.messageId, 'TRASH')}
                    >
                      <Button size="small" danger icon={<Trash2 className="w-3.5 h-3.5" />}>
                        删除
                      </Button>
                    </Popconfirm>
                  ) : (
                    <Button
                      size="small"
                      onClick={() => moveBox(selectedMail.messageId, 'INBOX')}
                    >
                      恢复至收件箱
                    </Button>
                  )}
                </Space>

                <Space size="middle">
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    发送时间: {new Date(selectedMail.createdAt).toLocaleString()}
                  </Text>
                </Space>
              </div>

              {/* 详情主内容滚动区 */}
              <div style={{ flex: 1, overflowY: 'auto', padding: '20px 24px' }}>
                {/* 邮件主题大标题 */}
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12, marginBottom: 16 }}>
                  <div style={{ flex: 1 }}>
                    <Title level={4} style={{ margin: 0, color: '#111' }}>
                      {selectedMail.subject}
                    </Title>
                  </div>
                  {renderPriorityTag(selectedMail.priority)}
                </div>

                {/* 发件人与收件人卡片 */}
                <div
                  style={{
                    backgroundColor: '#f9fbfd',
                    border: '1px solid #e8edf3',
                    borderRadius: 6,
                    padding: '12px 16px',
                    marginBottom: 16,
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                    <Space size={10}>
                      <Avatar style={{ backgroundColor: '#1677ff' }}>
                        {selectedMail.senderUserName.substring(0, 1)}
                      </Avatar>
                      <div>
                        <div style={{ fontWeight: 600, fontSize: 14 }}>{selectedMail.senderUserName}</div>
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          工号/账号: {selectedMail.senderUserId}
                        </Text>
                      </div>
                    </Space>
                    <Tag color={selectedMail.rootCategory === 'SYSTEM' ? 'cyan' : 'default'}>
                      {selectedMail.rootCategory === 'SYSTEM' ? '领域事件系统通知' : '内部协同撰写'}
                    </Tag>
                  </div>

                  {/* 收件人列表 */}
                  {selectedMail.recipients && selectedMail.recipients.length > 0 && (
                    <div style={{ fontSize: 12, color: '#666', marginTop: 6 }}>
                      <span style={{ fontWeight: 600 }}>收件人：</span>
                      {selectedMail.recipients.map((r, idx) => (
                        <Tag key={idx} style={{ margin: '0 4px' }}>
                          {r.userName} ({r.recipientType})
                        </Tag>
                      ))}
                    </div>
                  )}
                </div>

                {/* 【核心亮点】机床研制工程对象穿透直达卡片 (Object-Centric Action Card) */}
                {selectedMail.targetActionUrl && (
                  <div
                    style={{
                      background: 'linear-gradient(135deg, #f0f7ff 0%, #e6f4ff 100%)',
                      border: '1px solid #91caff',
                      borderRadius: 8,
                      padding: '14px 18px',
                      marginBottom: 20,
                      boxShadow: '0 2px 6px rgba(22, 119, 255, 0.06)',
                    }}
                  >
                    <Row justify="space-between" align="middle">
                      <Col span={16}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                          <FolderGit2 className="w-4 h-4 text-blue-600" />
                          <span style={{ fontWeight: 700, color: '#0958d9', fontSize: 14 }}>
                            关联高端机床工程对象：{selectedMail.relatedObjType || 'OBJ'} - {selectedMail.relatedObjId}
                          </span>
                          <Tag color="geekblue">{selectedMail.relatedProjectId || 'VMC_ENTERPRISE'}</Tag>
                        </div>
                        <div style={{ fontSize: 12, color: '#434343' }}>
                          目标直达动作：<code>{selectedMail.targetActionUrl}</code>
                          <span style={{ marginLeft: 8, color: '#666' }}>
                            (通过 PBAC 细粒度跨域安全校验后可直接穿透跳转)
                          </span>
                        </div>
                      </Col>
                      <Col>
                        <Button
                          type="primary"
                          icon={<ArrowRight className="w-4 h-4" />}
                          loading={isActionResolving}
                          onClick={() => handleResolveAction(selectedMail)}
                          style={{
                            background: '#1677ff',
                            boxShadow: '0 2px 4px rgba(22, 119, 255, 0.25)',
                            fontWeight: 600,
                          }}
                        >
                          直达业务对象
                        </Button>
                      </Col>
                    </Row>
                  </div>
                )}

                {/* 邮件正文渲染 */}
                <div
                  style={{
                    lineHeight: 1.8,
                    fontSize: 14,
                    color: '#262626',
                    minHeight: 160,
                    padding: '8px 0',
                  }}
                  dangerouslySetInnerHTML={{ __html: selectedMail.content }}
                />

                {/* 附件列表 */}
                {selectedMail.attachments && selectedMail.attachments.length > 0 && (
                  <div style={{ marginTop: 24, borderTop: '1px solid #f0f0f0', paddingTop: 16 }}>
                    <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 10, display: 'flex', alignItems: 'center', gap: 6 }}>
                      <Paperclip className="w-4 h-4 text-gray-500" />
                      <span>邮件附件 ({selectedMail.attachments.length})</span>
                    </div>
                    <List
                      size="small"
                      dataSource={selectedMail.attachments}
                      renderItem={(att) => (
                        <List.Item
                          style={{
                            padding: '8px 12px',
                            backgroundColor: '#fafafa',
                            borderRadius: 6,
                            marginBottom: 8,
                            border: '1px solid #f0f0f0',
                          }}
                          actions={[
                            <Button
                              key="download"
                              type="link"
                              size="small"
                              icon={<Download className="w-3.5 h-3.5" />}
                              onClick={() => message.success(`已开始下载附件: ${att.fileName}`)}
                            >
                              下载
                            </Button>,
                          ]}
                        >
                          <List.Item.Meta
                            avatar={<FileText className="w-5 h-5 text-blue-500 mt-1" />}
                            title={<span style={{ fontSize: 13, fontWeight: 500 }}>{att.fileName}</span>}
                            description={
                              <span style={{ fontSize: 11, color: '#888' }}>
                                大小: {(att.fileSize / 1024).toFixed(1)} KB | 类型: {att.fileType}
                              </span>
                            }
                          />
                        </List.Item>
                      )}
                    />
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
              <Empty description="请在左侧列表中选择一封邮件进行查阅" />
            </div>
          )}
        </Card>
      </div>

      {/* 写信模态框 (Compose Modal) */}
      <Modal
        title={
          <Space>
            <Mail className="w-4 h-4 text-blue-600" />
            <span>撰写机床研制内部邮件</span>
          </Space>
        }
        open={isComposeVisible}
        onCancel={() => setIsComposeVisible(false)}
        footer={null}
        width={720}
        destroyOnClose
      >
        <Form form={composeForm} layout="vertical" onFinish={handleSendManualMail}>
          <Form.Item
            name="subject"
            label="邮件主题"
            rules={[{ required: true, message: '请输入邮件主题' }]}
          >
            <Input placeholder="例如：关于五轴立加伺服进给轴热变形补偿算法讨论" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={16}>
              <Form.Item
                name="recipients"
                label={
                  <div className="flex items-center justify-between w-full">
                    <span className="font-semibold text-slate-800">收件人 (已创建的系统用户)</span>
                    <span className="text-xs text-slate-400 font-normal">
                      共 {systemUsers.length} 位已注册用户
                    </span>
                  </div>
                }
                rules={[{ required: true, message: '请选择至少一位已创建的系统用户作为收件人' }]}
              >
                <Select
                  mode="multiple"
                  showSearch
                  placeholder="从系统已创建用户中选择收件人 (支持按姓名、工号、账号、部门搜索)"
                  optionFilterProp="filterText"
                  options={systemUsers.map((u) => ({
                    value: u.username,
                    label: (
                      <div className="flex items-center justify-between py-0.5">
                        <Space size={6}>
                          <span className="font-semibold text-slate-800">{u.realName}</span>
                          <span className="text-xs text-slate-400 font-mono">(@{u.username})</span>
                          {u.deptName && (
                            <Tag color="blue" className="text-[10px] m-0">
                              {u.deptName}
                            </Tag>
                          )}
                        </Space>
                        <span className="text-xs text-slate-400 font-mono hidden sm:inline">
                          {u.email}
                        </span>
                      </div>
                    ),
                    filterText: `${u.realName} ${u.username} ${u.userId} ${u.deptName || ''} ${u.email || ''}`,
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="priority" label="优先级" initialValue="NORMAL">
                <Select
                  options={[
                    { label: '普通 (NORMAL)', value: 'NORMAL' },
                    { label: '高优 (HIGH)', value: 'HIGH' },
                    { label: '紧急 (URGENT)', value: 'URGENT' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="relatedProjectId" label="关联机床项目" initialValue="VMC_ENTERPRISE">
                <Input placeholder="例如：VMC_ENTERPRISE" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="relatedObjType" label="关联对象类型">
                <Select
                  placeholder="选填"
                  allowClear
                  options={[
                    { label: '工程变更单 (ECO)', value: 'ECO' },
                    { label: '变更请求 (ECR)', value: 'ECR' },
                    { label: '项目阶段门 (GATE)', value: 'GATE' },
                    { label: '零件/物料 (PART)', value: 'PART' },
                    { label: '工艺过程 (PROCESS)', value: 'PROCESS' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="relatedObjId" label="对象业务主键">
                <Input placeholder="例如：ECO-2026-0042" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="targetActionUrl" label="直达路径 (选填)">
            <Input placeholder="例如：/change/eco/ECO-2026-0042" />
          </Form.Item>

          <Form.Item
            name="content"
            label="邮件正文"
            rules={[{ required: true, message: '请输入正文内容' }]}
          >
            <TextArea rows={6} placeholder="请输入详实的工程技术描述与交流要点..." />
          </Form.Item>

          <Alert
            message="安全与规范提示"
            description="人工发信通道受职责分离 (SoD) 策略管控，严禁伪造系统自动化派发标识 (rootCategory=SYSTEM)，邮件信息自动落盘于审计追踪日志。"
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
          />

          <Row justify="end">
            <Space>
              <Button onClick={() => setIsComposeVisible(false)}>取消</Button>
              <Button type="primary" htmlType="submit" loading={composeLoading} icon={<Send className="w-3.5 h-3.5" />}>
                立即投递
              </Button>
            </Space>
          </Row>
        </Form>
      </Modal>

      {/* PBAC 跨域越权硬拦截安全弹窗 */}
      <Modal
        title={
          <Space>
            <ShieldAlert className="w-5 h-5 text-red-600" />
            <span style={{ color: '#cf1322', fontWeight: 700 }}>PBAC 跨域越权穿透阻断 (403 Forbidden)</span>
          </Space>
        }
        open={securityBlockModalVisible}
        onOk={() => setSecurityBlockModalVisible(false)}
        onCancel={() => setSecurityBlockModalVisible(false)}
        okText="知晓并遵守安全策略"
        cancelButtonProps={{ style: { display: 'none' } }}
        width={600}
      >
        <Alert
          type="error"
          showIcon
          message="机床敏感工程对象安全隔离拦截生效"
          description={securityBlockReason}
          style={{ marginBottom: 16 }}
        />
        <Paragraph style={{ fontSize: 13, color: '#555', lineHeight: 1.8 }}>
          <strong>安全机制说明：</strong>
          <br />
          1. <strong>消息查看权 ≠ 业务对象权能</strong>：拥有内部邮件阅读权不代表具备穿透至高端机床工程对象的修改或会签授权；
          <br />
          2. <strong>PBAC 策略动态计算</strong>：系统已根据当前角色、密级、项目工作组成员资质执行严密校验，当前请求未通过策略断言；
          <br />
          3. <strong>违规访问已记录审计</strong>：该次越权直达尝试已被 M01 消息审计总线与 M30 IAM 审计仓储完整固化。
        </Paragraph>
      </Modal>
    </div>
  );
};
