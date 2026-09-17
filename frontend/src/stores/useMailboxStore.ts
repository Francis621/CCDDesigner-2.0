import { create } from 'zustand';

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

export interface MailboxItem {
  messageId: number;
  userBoxId: number;
  /**
   * 信箱所有者（用户名，如 admin, zhang_jg 等，用于实现多用户信箱精准隔离）
   */
  ownerUserId: string;
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

const STORAGE_KEY = 'ccdd_mailbox_messages_v3';

// 基础系统默认种子邮件
function buildDefaultSeeds(): MailboxItem[] {
  const now = Date.now();
  const isoTime = (diffHours: number) => new Date(now - diffHours * 3600 * 1000).toISOString();

  const baseSeeds: MailboxItem[] = [];

  // 典型核心研制角色用户名清单（预装通用系统协同邮件）
  const coreUsernames = [
    'admin',
    'zhang_jg',
    'li_sys',
    'wang_sim',
    'zhao_qual',
    'sun_proc',
    'qian_field',
    'chief_designer',
  ];

  // 1. ECO-2026-0042 变更会签通知（派发给所有核心工程师的收件箱）
  coreUsernames.forEach((uName, idx) => {
    baseSeeds.push({
      messageId: 9001,
      userBoxId: 10000 + idx * 10 + 1,
      ownerUserId: uName,
      rootCategory: 'SYSTEM',
      systemType: 'CHANGE',
      subject: '【变更协同】关于ECO-2026-0042(VMC1000主轴提速至15000rpm)的协同会签通知',
      content: `<p>尊敬的工程师：</p>
<p>工程变更单 <strong>ECO-2026-0042</strong> (VMC1000主轴提速至15000rpm工程实施与全生命周期现场处置单) 已进入跨学科协同会签阶段。请机械、仿真及电气专业主管在收到本通知后3个工作日内完成影响分析及处置方案在线复核。</p>
<p>点击下方按钮可直达变更详情面板进行方案批注与电子签章。</p>`,
      senderUserId: 'SYSTEM_NOTIFIER',
      senderUserName: '系统通知服务 (PLM-Core)',
      priority: 'HIGH',
      relatedProjectId: 'VMC_ENTERPRISE',
      relatedObjType: 'ECO',
      relatedObjId: 'ECO-2026-0042',
      targetActionUrl: '/change/eco/ECO-2026-0042',
      actionIdentifier: 'ECO_COLLABORATION_REVIEW',
      isRead: uName === 'admin',
      isStarred: false,
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
        { userId: 'zhang_jg', userName: '张建国 (机械总工)', recipientType: 'TO' },
        { userId: 'li_sys', userName: '李明 (系统架构师)', recipientType: 'TO' },
        { userId: 'admin', userName: '系统管理员', recipientType: 'CC' },
      ],
      createdAt: isoTime(2),
    });
  });

  // 2. GATE-3 阶段门预警通知（派发给相关负责人的收件箱）
  ['admin', 'zhang_jg', 'li_sys', 'chief_designer'].forEach((uName, idx) => {
    baseSeeds.push({
      messageId: 9002,
      userBoxId: 20000 + idx * 10 + 2,
      ownerUserId: uName,
      rootCategory: 'SYSTEM',
      systemType: 'GATE',
      subject: '【阶段门预警】VMC1000项目 GATE-3(详细设计评审门) 临期倒计时预警',
      content: `<p>项目各主管：</p>
<p>型号项目 <strong>VMC_ENTERPRISE</strong> 关键节点 <strong>GATE-3(详细设计评审门)</strong> 计划于5个工作日后关闭。目前仍有1项关键交付物(全机热伸长有限元分析报告)处于审批中。请加快流转，确保门禁条件按时闭环。</p>`,
      senderUserId: 'SYSTEM_NOTIFIER',
      senderUserName: '系统通知服务 (PLM-Core)',
      priority: 'URGENT',
      relatedProjectId: 'VMC_ENTERPRISE',
      relatedObjType: 'GATE',
      relatedObjId: 'GATE-3',
      targetActionUrl: '/project/gate/GATE-3',
      actionIdentifier: 'GATE_AUDIT_EXPEDITE',
      isRead: false,
      isStarred: true,
      isArchived: false,
      boxType: 'INBOX',
      hasAttachment: false,
      recipients: [
        { userId: 'admin', userName: '系统管理员', recipientType: 'TO' },
        { userId: 'zhang_jg', userName: '张建国 (机械总工)', recipientType: 'TO' },
      ],
      createdAt: isoTime(24),
    });
  });

  // 3. 工艺参数研讨纪要（发件人 wang_sim，收件人 zhang_jg, sun_proc, chief_designer）
  // 发件人的 OUTBOX
  baseSeeds.push({
    messageId: 9003,
    userBoxId: 30001,
    ownerUserId: 'wang_sim',
    rootCategory: 'MANUAL',
    systemType: 'TECHNICAL',
    subject: '【技术交流】关于五轴联动叶片加工工艺参数优化的研讨纪要与试验排程',
    content: `<p>张总、各位工艺师：</p>
<p>附件为本周二关于五轴铣削钛合金叶片表面粗糙度提升的试验分析报告。初步测算在转速由10000rpm提升至12500rpm配合微量润滑(MQL)条件下，表面粗糙度可由Ra 0.8提升至Ra 0.4。请审阅试验数据，并安排下周二的二次试切验证。</p>`,
    senderUserId: 'wang_sim',
    senderUserName: '王强 (仿真工程师)',
    priority: 'NORMAL',
    relatedProjectId: 'VMC_ENTERPRISE',
    relatedObjType: 'PROCESS',
    relatedObjId: 'PROC-BLADE-001',
    targetActionUrl: '/manufacturing/process/PROC-BLADE-001',
    actionIdentifier: 'PROCESS_EXPERIMENT_SCHEDULE',
    isRead: true,
    isStarred: false,
    isArchived: false,
    boxType: 'OUTBOX',
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
      { userId: 'zhang_jg', userName: '张建国 (机械总工)', recipientType: 'TO' },
      { userId: 'sun_proc', userName: '孙工 (工艺主管)', recipientType: 'TO' },
      { userId: 'chief_designer', userName: '总设计师', recipientType: 'TO' },
    ],
    createdAt: isoTime(72),
  });

  // 收件人的 INBOX
  ['zhang_jg', 'sun_proc', 'chief_designer'].forEach((recip, idx) => {
    baseSeeds.push({
      messageId: 9003,
      userBoxId: 30010 + idx,
      ownerUserId: recip,
      rootCategory: 'MANUAL',
      systemType: 'TECHNICAL',
      subject: '【技术交流】关于五轴联动叶片加工工艺参数优化的研讨纪要与试验排程',
      content: `<p>张总、各位工艺师：</p>
<p>附件为本周二关于五轴铣削钛合金叶片表面粗糙度提升的试验分析报告。初步测算在转速由10000rpm提升至12500rpm配合微量润滑(MQL)条件下，表面粗糙度可由Ra 0.8提升至Ra 0.4。请审阅试验数据，并安排下周二的二次试切验证。</p>`,
      senderUserId: 'wang_sim',
      senderUserName: '王强 (仿真工程师)',
      priority: 'NORMAL',
      relatedProjectId: 'VMC_ENTERPRISE',
      relatedObjType: 'PROCESS',
      relatedObjId: 'PROC-BLADE-001',
      targetActionUrl: '/manufacturing/process/PROC-BLADE-001',
      actionIdentifier: 'PROCESS_EXPERIMENT_SCHEDULE',
      isRead: recip === 'chief_designer',
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
        { userId: 'zhang_jg', userName: '张建国 (机械总工)', recipientType: 'TO' },
        { userId: 'sun_proc', userName: '孙工 (工艺主管)', recipientType: 'TO' },
        { userId: 'chief_designer', userName: '总设计师', recipientType: 'TO' },
      ],
      createdAt: isoTime(72),
    });
  });

  return baseSeeds;
}

// 从 LocalStorage 读取
function loadStoredMails(): MailboxItem[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed;
      }
    }
  } catch (e) {
    console.warn('[useMailboxStore] 加载本地邮件存储失败，重置为默认种子:', e);
  }
  const defaultSeeds = buildDefaultSeeds();
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(defaultSeeds));
  } catch {
    // ignore
  }
  return defaultSeeds;
}

export interface SendMailPayload {
  subject: string;
  content: string;
  priority?: MessagePriority;
  relatedProjectId?: string;
  relatedObjType?: string;
  relatedObjId?: string;
  targetActionUrl?: string;
  recipients: Array<{
    userId: string; // 接收人唯一用户名 username
    userName: string;
    recipientType?: 'TO' | 'CC';
  }>;
  attachments?: AttachmentItem[];
}

interface MailboxStoreState {
  allMails: MailboxItem[];
  loading: boolean;
  fetchMailbox: (username: string, userJobNumber?: string, boxType?: BoxType) => Promise<void>;
  sendManualMail: (
    payload: SendMailPayload,
    sender: { username: string; realName: string; userId?: string }
  ) => Promise<{ messageId: number; recipientCount: number }>;
  updateMailStatus: (
    messageId: number,
    ownerIdentifier: string,
    updates: Partial<Pick<MailboxItem, 'isRead' | 'isStarred' | 'boxType' | 'isArchived'>>
  ) => Promise<void>;
  deleteMailPermanently: (messageId: number, ownerIdentifier: string) => Promise<void>;
  getUserMails: (username: string, userJobNumber?: string, boxType?: BoxType) => MailboxItem[];
  getUnreadCount: (username: string, userJobNumber?: string) => number;
}

export const useMailboxStore = create<MailboxStoreState>((set, get) => ({
  allMails: loadStoredMails(),
  loading: false,

  fetchMailbox: async (username: string, userJobNumber?: string, boxType: BoxType = 'INBOX') => {
    set({ loading: true });
    try {
      const res = await fetch(`/api/v1/messages/mailbox?boxType=${boxType}&pageNum=1&pageSize=50`, {
        headers: {
          'X-Current-User-Id': username,
        },
      });
      if (res.ok) {
        const json = await res.json();
        if (json && json.data && Array.isArray(json.data.items)) {
          const apiItems = json.data.items;
          const currentList = get().allMails;

          // 将后端项映射为 MailboxItem 并更新/追加到本地
          const updatedList = [...currentList];
          apiItems.forEach((item: any) => {
            const mId = Number(item.messageId);
            const foundIdx = updatedList.findIndex(
              (m) =>
                m.messageId === mId &&
                (m.ownerUserId === username || (userJobNumber && m.ownerUserId === userJobNumber)) &&
                m.boxType === boxType
            );

            const newItem: MailboxItem = {
              messageId: mId,
              userBoxId: Number(item.userBoxId || item.itemId || Date.now()),
              ownerUserId: username,
              rootCategory: item.rootCategory || 'MANUAL',
              systemType: item.systemType || item.subType,
              subject: item.subject,
              content: item.content || item.bodyContent || '',
              senderUserId: item.senderUserId || item.senderId,
              senderUserName: item.senderUserName || item.senderDisplayName || '未知用户',
              priority: item.priority || 'NORMAL',
              relatedProjectId: item.relatedProjectId,
              relatedObjType: item.relatedObjType || item.relatedObjectType,
              relatedObjId: item.relatedObjId ? String(item.relatedObjId) : undefined,
              targetActionUrl: item.targetActionUrl,
              actionIdentifier: item.actionIdentifier,
              isRead: Boolean(item.isRead),
              isStarred: Boolean(item.isStarred),
              isArchived: Boolean(item.isArchived),
              boxType: boxType,
              hasAttachment: Boolean(item.hasAttachment),
              attachments: item.attachments,
              recipients: item.recipients,
              createdAt: item.createdAt || new Date().toISOString(),
            };

            if (foundIdx >= 0) {
              updatedList[foundIdx] = { ...updatedList[foundIdx], ...newItem };
            } else {
              updatedList.unshift(newItem);
            }
          });

          localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedList));
          set({ allMails: updatedList });
        }
      }
    } catch {
      // 离线或后端未连接，自动保留本地已持久化数据
    } finally {
      set({ loading: false });
    }
  },

  sendManualMail: async (payload, sender) => {
    const messageId = Date.now();
    const createdAt = new Date().toISOString();
    const createdItems: MailboxItem[] = [];

    // 1. 发件人发件箱 (OUTBOX) 条目
    const senderItem: MailboxItem = {
      messageId,
      userBoxId: messageId + 1,
      ownerUserId: sender.username,
      rootCategory: 'MANUAL',
      systemType: 'TECHNICAL',
      subject: payload.subject,
      content: payload.content,
      senderUserId: sender.username,
      senderUserName: sender.realName,
      priority: payload.priority || 'NORMAL',
      relatedProjectId: payload.relatedProjectId || 'VMC_ENTERPRISE',
      relatedObjType: payload.relatedObjType,
      relatedObjId: payload.relatedObjId,
      targetActionUrl: payload.targetActionUrl,
      isRead: true,
      isStarred: false,
      isArchived: false,
      boxType: 'OUTBOX',
      hasAttachment: Boolean(payload.attachments && payload.attachments.length > 0),
      attachments: payload.attachments,
      recipients: payload.recipients.map((r) => ({
        userId: r.userId,
        userName: r.userName,
        recipientType: r.recipientType || 'TO',
      })),
      createdAt,
    };
    createdItems.push(senderItem);

    // 2. 多播展开 (Fan-out)：向每一位收件人信箱投递一条 INBOX 记录
    let recipientCounter = 2;
    payload.recipients.forEach((recip) => {
      const recipientUsername = recip.userId; // 规范化的用户名标识
      const inboxItem: MailboxItem = {
        messageId,
        userBoxId: messageId + recipientCounter++,
        ownerUserId: recipientUsername,
        rootCategory: 'MANUAL',
        systemType: 'TECHNICAL',
        subject: payload.subject,
        content: payload.content,
        senderUserId: sender.username,
        senderUserName: sender.realName,
        priority: payload.priority || 'NORMAL',
        relatedProjectId: payload.relatedProjectId || 'VMC_ENTERPRISE',
        relatedObjType: payload.relatedObjType,
        relatedObjId: payload.relatedObjId,
        targetActionUrl: payload.targetActionUrl,
        isRead: false, // 接收方初始状态为未读
        isStarred: false,
        isArchived: false,
        boxType: 'INBOX',
        hasAttachment: Boolean(payload.attachments && payload.attachments.length > 0),
        attachments: payload.attachments,
        recipients: payload.recipients.map((r) => ({
          userId: r.userId,
          userName: r.userName,
          recipientType: r.recipientType || 'TO',
        })),
        createdAt,
      };
      createdItems.push(inboxItem);
    });

    // 3. 本地全量持久化
    const updatedMails = [...createdItems, ...get().allMails];
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedMails));
    } catch (e) {
      console.error('[useMailboxStore] 保存邮件到本地失败:', e);
    }
    set({ allMails: updatedMails });

    // 4. 异步同步后端
    try {
      await fetch('/api/v1/messages/manual', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Current-User-Id': sender.username,
          'X-Current-User-Name': encodeURIComponent(sender.realName),
        },
        body: JSON.stringify({
          rootCategory: 'MANUAL',
          subject: payload.subject,
          content: payload.content,
          priority: payload.priority || 'NORMAL',
          relatedProjectId: payload.relatedProjectId,
          relatedObjType: payload.relatedObjType,
          relatedObjId: payload.relatedObjId,
          targetActionUrl: payload.targetActionUrl,
          recipients: payload.recipients.map((r) => ({
            userId: r.userId,
            userName: r.userName,
            recipientType: r.recipientType || 'TO',
          })),
          attachments: payload.attachments,
        }),
      });
    } catch {
      // 离线环境静默放行
    }

    return { messageId, recipientCount: payload.recipients.length };
  },

  updateMailStatus: async (messageId, ownerIdentifier, updates) => {
    const list = get().allMails;
    const updated = list.map((m) => {
      if (m.messageId === messageId && (m.ownerUserId === ownerIdentifier || !ownerIdentifier)) {
        return { ...m, ...updates };
      }
      return m;
    });

    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
    } catch (e) {
      console.error('[useMailboxStore] 更新状态保存失败:', e);
    }
    set({ allMails: updated });

    // 尝试同步后端
    try {
      await fetch(`/api/v1/messages/${messageId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'X-Current-User-Id': ownerIdentifier,
        },
        body: JSON.stringify({
          targetBoxType: updates.boxType,
          isRead: updates.isRead,
          isStarred: updates.isStarred,
        }),
      });
    } catch {
      // 离线放行
    }
  },

  deleteMailPermanently: async (messageId, ownerIdentifier) => {
    const list = get().allMails;
    const updated = list.filter(
      (m) => !(m.messageId === messageId && (m.ownerUserId === ownerIdentifier || !ownerIdentifier))
    );
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
    } catch {
      // ignore
    }
    set({ allMails: updated });
  },

  getUserMails: (username: string, userJobNumber?: string, boxType: BoxType = 'INBOX') => {
    const list = get().allMails;
    return list.filter((m) => {
      if (m.boxType !== boxType) return false;

      const isOwner =
        m.ownerUserId === username ||
        (userJobNumber && m.ownerUserId === userJobNumber) ||
        m.ownerUserId === 'ALL' ||
        (boxType === 'INBOX' && m.senderUserId === 'SYSTEM_NOTIFIER' && (!m.ownerUserId || m.ownerUserId === username));

      return isOwner;
    });
  },

  getUnreadCount: (username: string, userJobNumber?: string) => {
    const list = get().allMails;
    return list.filter((m) => {
      if (m.boxType !== 'INBOX' || m.isRead) return false;
      return (
        m.ownerUserId === username ||
        (userJobNumber && m.ownerUserId === userJobNumber) ||
        m.ownerUserId === 'ALL'
      );
    }).length;
  },
}));
