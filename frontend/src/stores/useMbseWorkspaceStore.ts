import { create } from 'zustand';

// SHA-256 快速摘要算法 (支持在纯前端沙箱中精准计算十六进制哈希)
async function sha256Hex(message: string): Promise<string> {
  const msgBuffer = new TextEncoder().encode(message);
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

export interface WorkingElementBindingItem {
  bindingId: number;
  workspaceId: number;
  plmObjectType: string;
  plmObjectId: number;
  plmObjectCode: string;
  sysonElementId: string;
  elementType: string;
  qualifiedName: string;
  lastSyncedAt: string;
}

export interface DiagnosticItem {
  severity: 'ERROR' | 'WARNING' | 'INFO';
  errorCode: string;
  message: string;
  elementId: string;
  qualifiedName: string;
  sourceLocation: string;
  recommendation: string;
}

export interface ValidationOutcome {
  validationId: number;
  workspaceId: number;
  status: 'PASSED' | 'PASSED_WITH_WARNING' | 'FAILED';
  sourceChecksum: string;
  errorCount: number;
  warningCount: number;
  diagnostics: DiagnosticItem[];
  startedAt: string;
  completedAt: string;
}

export interface CandidateSnapshotItem {
  snapshotId: number;
  snapshotToken: string;
  status: 'STAGED' | 'HANDED_OVER' | 'EXPIRED' | 'INVALIDATED';
  sourceChecksum: string;
  releaseOperationId: string;
  capturedBy: string;
  capturedAt: string;
  description?: string;
}

export interface WorkspaceInfo {
  workspaceId: number;
  modelProjectId: number;
  modelProjectName: string;
  modelCode: string;
  primaryChannel: 'GRAPHICAL' | 'TEXTUAL';
  currentWorkspaceState: 'ACTIVE' | 'LOCKED' | 'RELEASING' | 'ARCHIVED';
  channelLockToken: string | null;
  channelLockExpiresAt: string | null;
  boundUserId: string | null;
  sysonProjectUri: string;
  compatibilityProfileId: string;
}

interface MbseWorkspaceStore {
  workspace: WorkspaceInfo;
  modelText: string;
  currentChecksum: string;
  latestValidation: ValidationOutcome | null;
  workingBindings: WorkingElementBindingItem[];
  candidateSnapshots: CandidateSnapshotItem[];
  selectedElementId: string | null;
  highlightedElementId: string | null;
  activeBottomTab: string;
  isLockedByMe: (currentUserId: string) => boolean;

  // 核心业务动作
  initWorkspace: (currentUserId: string) => Promise<void>;
  acquireSessionLock: (currentUserId: string) => Promise<{ success: boolean; message: string }>;
  releaseSessionLock: (currentUserId: string) => Promise<boolean>;
  switchChannel: (targetChannel: 'GRAPHICAL' | 'TEXTUAL', currentUserId: string) => Promise<{ success: boolean; message: string }>;
  bindRequirement: (req: { id: number; code: string; name: string }) => Promise<void>;
  validateModel: () => Promise<ValidationOutcome>;
  captureCandidateSnapshot: (expectedChecksum: string, description: string, currentUserId: string) => Promise<{ success: boolean; message: string; snapshot?: CandidateSnapshotItem }>;
  updateModelText: (newText: string) => Promise<void>;
  injectSyntaxError: () => Promise<void>;
  tamperParameter: () => Promise<void>;
  resetToHealthy: () => Promise<void>;
  setSelectedElement: (elementId: string | null) => void;
  setHighlightedElement: (elementId: string | null) => void;
  setActiveBottomTab: (tab: string) => void;
}

const STORAGE_KEY = 'ccdd_mbse_workspace_v1';

const DEFAULT_VMC1000_SYSML = `package VMC1000_SystemModel {
    doc /* CCDDesigner 2.0 数控机床正向设计系统模型 - VMC1000 */
    
    package '01_Requirements' {
        doc /* 映射 PLM M03 需求规范集 */
        requirement def XAxisStrokeReq {
            doc /* X轴有效行程不小于 1000mm */
            attribute minStroke: Real = 1000.0;
        }
        
        requirement def SpindleSpeedReq {
            doc /* 高速电主轴最高转速不低于 18000 rpm */
            attribute minSpeed: Real = 18000.0;
        }
    }
    
    package '02_FunctionalBehavior' {
        action def HighSpeedMillingAction {
            in item workpiece: Workpiece;
            out item finishedPart: Workpiece;
        }
    }
    
    package '03_LogicalArchitecture' {
        part def CncMotionController {
            port busPort: IndustrialEthernetPort;
        }
    }
    
    package '04_PhysicalArchitecture' {
        part def VMC1000Structure {
            part xFeedSystem: XAxisFeedSystem;
            part spindleModule: HighSpeedSpindle;
        }
        
        part def XAxisFeedSystem {
            attribute stroke: Real = 1020.0;
            attribute maxVelocity: Real = 48.0;
            attribute dampingRatio: Real = 0.05;
            port ctrlPort: ServoInterfacePort;
        }
        
        part def HighSpeedSpindle {
            attribute maxSpeed: Real = 18000.0;
            attribute ratedTorque: Real = 120.0;
        }
    }
    
    package '05_Interfaces' {
        interface def ServoInterfacePort;
        interface def IndustrialEthernetPort;
    }
    
    package '06_Parameters' {
        doc /* M07 受控工程参数映射 */
        attribute def MachineMassLimit = 8500.0;
    }
    
    package '07_VerificationContext' {
        doc /* CDR / PDR 验证分析工况与多学科协同约束 */
    }
}`;

export const useMbseWorkspaceStore = create<MbseWorkspaceStore>((set, get) => ({
  workspace: {
    workspaceId: 801928410290182,
    modelProjectId: 701928410293812,
    modelProjectName: 'VMC1000 五轴立式加工中心系统工程模型',
    modelCode: 'SMP-VMC1000-01',
    primaryChannel: 'GRAPHICAL',
    currentWorkspaceState: 'ACTIVE',
    channelLockToken: null,
    channelLockExpiresAt: null,
    boundUserId: null,
    sysonProjectUri: 'syson-proj-uuid-88192a01-c918',
    compatibilityProfileId: 'SYSML_V2_CNC_V2026_09',
  },
  modelText: DEFAULT_VMC1000_SYSML,
  currentChecksum: '',
  latestValidation: null,
  workingBindings: [
    {
      bindingId: 1,
      workspaceId: 801928410290182,
      plmObjectType: 'RequirementRevision',
      plmObjectId: 3001,
      plmObjectCode: 'REQ-X-001',
      sysonElementId: 'elem_req_stroke_1000',
      elementType: 'RequirementUsage',
      qualifiedName: "VMC1000_SystemModel::'01_Requirements'::XAxisStrokeReqUsage",
      lastSyncedAt: new Date().toISOString(),
    },
  ],
  candidateSnapshots: [],
  selectedElementId: 'elem_part_xfeed',
  highlightedElementId: null,
  activeBottomTab: 'diagnostics',

  isLockedByMe: (currentUserId: string) => {
    const ws = get().workspace;
    if (!ws.channelLockToken || !ws.channelLockExpiresAt) return false;
    const expires = new Date(ws.channelLockExpiresAt).getTime();
    return expires > Date.now() && ws.boundUserId === currentUserId;
  },

  initWorkspace: async (_currentUserId?: string) => {
    // 优先读取本地持久化状态
    let initialText = DEFAULT_VMC1000_SYSML;
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.modelText) initialText = parsed.modelText;
        set({
          workspace: parsed.workspace || get().workspace,
          modelText: initialText,
          latestValidation: parsed.latestValidation || null,
          workingBindings: parsed.workingBindings || get().workingBindings,
          candidateSnapshots: parsed.candidateSnapshots || [],
        });
      } catch (e) {
        console.warn('解析本地工作区缓存失败，使用预置数据', e);
      }
    }

    const hash = await sha256Hex(initialText);
    set({ currentChecksum: hash });

    // 尝试与后端进行同步
    try {
      const res = await fetch(`/api/v1/mbse/workspaces/${get().workspace.workspaceId}`);
      if (res.ok) {
        const data = await res.json();
        if (data.data) {
          set(state => ({
            workspace: {
              ...state.workspace,
              primaryChannel: data.data.primaryChannel || state.workspace.primaryChannel,
              channelLockToken: data.data.channelLockToken,
              channelLockExpiresAt: data.data.channelLockExpiresAt,
              boundUserId: data.data.boundUserId,
            }
          }));
        }
      }
    } catch {
      // 离线沙箱静默模式
    }
  },

  acquireSessionLock: async (currentUserId: string) => {
    const ws = get().workspace;
    const now = Date.now();
    // 检查冲突
    if (ws.channelLockToken && ws.channelLockExpiresAt && new Date(ws.channelLockExpiresAt).getTime() > now) {
      if (ws.boundUserId && ws.boundUserId !== currentUserId) {
        return {
          success: false,
          message: `申请冲突：编辑锁已被工程师 [${ws.boundUserId}] 持有，当前处于只读保护模式`,
        };
      }
    }

    const token = 'tkt_sess_' + Math.random().toString(36).substring(2, 10);
    const expiresAt = new Date(Date.now() + 120 * 60 * 1000).toISOString();
    const updatedWs: WorkspaceInfo = {
      ...ws,
      channelLockToken: token,
      channelLockExpiresAt: expiresAt,
      boundUserId: currentUserId,
    };

    set({ workspace: updatedWs });
    saveToStorage(get());

    try {
      await fetch(`/api/v1/mbse/workspaces/${ws.workspaceId}/sessions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-User-Id': currentUserId },
        body: JSON.stringify({ requestLock: true, lockTimeoutMinutes: 120 }),
      });
    } catch {
      // 离线沙箱
    }

    return { success: true, message: `已成功获取排他编辑会话锁 (会话有效期 120 分钟)` };
  },

  releaseSessionLock: async (currentUserId: string) => {
    const ws = get().workspace;
    const updatedWs: WorkspaceInfo = {
      ...ws,
      channelLockToken: null,
      channelLockExpiresAt: null,
      boundUserId: null,
    };
    set({ workspace: updatedWs });
    saveToStorage(get());

    try {
      await fetch(`/api/v1/mbse/workspaces/${ws.workspaceId}/sessions`, {
        method: 'DELETE',
        headers: { 'X-User-Id': currentUserId },
      });
    } catch {
      // 离线沙箱
    }
    return true;
  },

  switchChannel: async (targetChannel: 'GRAPHICAL' | 'TEXTUAL', currentUserId: string) => {
    const ws = get().workspace;
    const now = Date.now();
    // CST-M04-01: 通道切换前必须释放排他锁
    if (ws.channelLockToken && ws.channelLockExpiresAt && new Date(ws.channelLockExpiresAt).getTime() > now) {
      return {
        success: false,
        message: `通道切换被拦截 (CST-M04-01)：当前仍处于排他编辑锁定状态 (持有者: ${ws.boundUserId})，必须先主动释放锁！`,
      };
    }

    const updatedWs: WorkspaceInfo = { ...ws, primaryChannel: targetChannel };
    set({ workspace: updatedWs });
    saveToStorage(get());

    try {
      await fetch(`/api/v1/mbse/workspaces/${ws.workspaceId}/channel`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'X-User-Id': currentUserId },
        body: JSON.stringify({ targetChannel }),
      });
    } catch {
      // 离线沙箱
    }

    return { success: true, message: `主编辑通道已成功切换为：${targetChannel === 'GRAPHICAL' ? '图形通道 (SysON)' : '形式化文本通道 (OpenSysML)'}` };
  },

  bindRequirement: async (req) => {
    const ws = get().workspace;
    const currentText = get().modelText;
    const newElementId = 'syson_elem_req_' + Math.random().toString(36).substring(2, 7);
    const qualifiedName = `VMC1000_SystemModel::'01_Requirements'::${req.code}Usage`;

    const newBinding: WorkingElementBindingItem = {
      bindingId: Date.now(),
      workspaceId: ws.workspaceId,
      plmObjectType: 'RequirementRevision',
      plmObjectId: req.id,
      plmObjectCode: req.code,
      sysonElementId: newElementId,
      elementType: 'RequirementUsage',
      qualifiedName,
      lastSyncedAt: new Date().toISOString(),
    };

    // 注入 SysML 源码
    const snippet = `\n    // [PLM M03 投影关联] ${req.code} - ${req.name}\n` +
      `    requirement ${req.code}Usage : '01_Requirements'::XAxisStrokeReq {\n` +
      `        doc /* 关联 PLM 权威需求: ${req.code} */\n` +
      `    }\n`;

    const updatedText = currentText + snippet;
    const newHash = await sha256Hex(updatedText);

    set(state => ({
      modelText: updatedText,
      currentChecksum: newHash,
      workingBindings: [newBinding, ...state.workingBindings],
      selectedElementId: newElementId,
    }));
    saveToStorage(get());

    try {
      await fetch(`/api/v1/mbse/workspaces/${ws.workspaceId}/elements/bind-requirement`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          requirementRevisionId: req.id,
          requirementCode: req.code,
          requirementName: req.name,
          targetPackageName: '01_Requirements',
        }),
      });
    } catch {
      // 离线沙箱
    }
  },

  validateModel: async () => {
    const text = get().modelText;
    const hash = await sha256Hex(text);
    const diagnostics: DiagnosticItem[] = [];
    let errorCount = 0;
    let warningCount = 0;

    // 模拟 OpenSysML 语义诊断算法
    if (text.includes('UNRESOLVED') || text.includes('syntax_error_mock') ||
      (text.includes('SpindleCoolingPort') && !text.includes('FluidInterfaces'))) {
      errorCount++;
      diagnostics.push({
        severity: 'ERROR',
        errorCode: 'SYSML-UNRESOLVED-REF',
        message: "Unresolved reference: Cannot find definition for 'SpindleCoolingPort'",
        elementId: 'elem_port_cooling',
        qualifiedName: 'VMC1000_SystemModel::Spindle::CoolingLoop::PortIn',
        sourceLocation: 'PhysicalArchitecture.sysml:142:18',
        recommendation: "Check if package 'FluidInterfaces' is properly imported in header.",
      });
    }

    if (!text.includes('N.m') || text.includes('ratedTorque')) {
      warningCount++;
      diagnostics.push({
        severity: 'WARNING',
        errorCode: 'SYSML-ATTR-WARN',
        message: "Attribute 'ratedTorque' has no explicit unit binding in M07 parameter registry",
        elementId: 'elem_attr_torque',
        qualifiedName: 'VMC1000_SystemModel::PhysicalArchitecture::HighSpeedSpindle::ratedTorque',
        sourceLocation: 'PhysicalArchitecture.sysml:45:12',
        recommendation: "Bind unit 'N.m' from ISO/IEC 80000 standard library.",
      });
    }

    diagnostics.push({
      severity: 'INFO',
      errorCode: 'SYSML-PORT-INFO',
      message: 'IndustrialEthernetPort conforms to InterfaceContract: EXT-PROFINET-V2.4 specification',
      elementId: 'elem_port_bus',
      qualifiedName: 'VMC1000_SystemModel::LogicalArchitecture::CncMotionController::busPort',
      sourceLocation: 'LogicalArchitecture.sysml:22:5',
      recommendation: 'Verified against system bus architecture profile.',
    });

    const status = errorCount > 0 ? 'FAILED' : (warningCount > 0 ? 'PASSED_WITH_WARNING' : 'PASSED');
    const outcome: ValidationOutcome = {
      validationId: Date.now(),
      workspaceId: get().workspace.workspaceId,
      status,
      sourceChecksum: hash,
      errorCount,
      warningCount,
      diagnostics,
      startedAt: new Date(Date.now() - 320).toISOString(),
      completedAt: new Date().toISOString(),
    };

    set({ latestValidation: outcome, activeBottomTab: 'diagnostics' });
    saveToStorage(get());

    try {
      await fetch(`/api/v1/mbse/workspaces/${get().workspace.workspaceId}/validate`, {
        method: 'POST',
      });
    } catch {
      // 离线沙箱
    }

    return outcome;
  },

  captureCandidateSnapshot: async (expectedChecksum: string, description: string, currentUserId: string) => {
    const val = get().latestValidation;
    const currentHash = get().currentChecksum;

    // 1. PUB-03: 诊断失败一票否决
    if (!val || val.status === 'FAILED') {
      return {
        success: false,
        message: '发布受控拦截 (PUB-03)：无法从包含语法/语义错误的校验中捕获候选快照，请先修复模型错误！',
      };
    }

    // 2. PUB-04: 核心摘要强校验 (防篡改)
    if (currentHash !== val.sourceChecksum || (expectedChecksum && currentHash !== expectedChecksum)) {
      return {
        success: false,
        message: `检测到模型篡改漂移 (PUB-04)：模型在上次校验后被修改！快照实时哈希 [${currentHash.substring(0, 16)}...] 与校验哈希 [${val.sourceChecksum.substring(0, 16)}...] 不一致，必须重新执行语义诊断！`,
      };
    }

    const token = 'TKT_SNAP_' + Math.random().toString(36).substring(2, 12);
    const releaseOpId = 'op_m06_release_' + Math.random().toString(36).substring(2, 8);
    const snapshot: CandidateSnapshotItem = {
      snapshotId: Date.now(),
      snapshotToken: token,
      status: 'HANDED_OVER',
      sourceChecksum: currentHash,
      releaseOperationId: releaseOpId,
      capturedBy: currentUserId,
      capturedAt: new Date().toISOString(),
      description: description || 'VMC1000 X轴进给系统架构与需求完全匹配快照',
    };

    set(state => ({
      candidateSnapshots: [snapshot, ...state.candidateSnapshots],
      activeBottomTab: 'snapshots',
    }));
    saveToStorage(get());

    try {
      await fetch(`/api/v1/mbse/workspaces/${get().workspace.workspaceId}/candidate-snapshots`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-User-Id': currentUserId },
        body: JSON.stringify({
          validationId: val.validationId,
          expectedChecksum: currentHash,
          snapshotDescription: description,
        }),
      });
    } catch {
      // 离线沙箱
    }

    return {
      success: true,
      message: `候选快照已固化并原子移交 M06 发布流水线！令牌: ${token}`,
      snapshot,
    };
  },

  updateModelText: async (newText: string) => {
    const hash = await sha256Hex(newText);
    set({ modelText: newText, currentChecksum: hash });
    saveToStorage(get());

    try {
      await fetch(`/api/v1/mbse/workspaces/${get().workspace.workspaceId}/model-content`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rawSysmlContent: newText }),
      });
    } catch {
      // 离线沙箱
    }
  },

  injectSyntaxError: async () => {
    const current = get().modelText;
    const errorSnippet = '\n    // [测试注入: 模拟未解析引用与语法错误]\n    part def BadFeedComponent {\n        port errorPort: UNRESOLVED SpindleCoolingPort;\n    }\n';
    await get().updateModelText(current + errorSnippet);
  },

  tamperParameter: async () => {
    const current = get().modelText;
    // 微调进给轴阻尼比参数
    const modified = current.replace('attribute dampingRatio: Real = 0.05;', 'attribute dampingRatio: Real = 0.08; /* 微调阻尼 */');
    if (modified !== current) {
      await get().updateModelText(modified);
    } else {
      await get().updateModelText(current + '\n    // [微调参数] 阻尼参数调整');
    }
  },

  resetToHealthy: async () => {
    await get().updateModelText(DEFAULT_VMC1000_SYSML);
    await get().validateModel();
  },

  setSelectedElement: (elementId) => set({ selectedElementId: elementId }),
  setHighlightedElement: (elementId) => set({ highlightedElementId: elementId }),
  setActiveBottomTab: (tab) => set({ activeBottomTab: tab }),
}));

function saveToStorage(state: MbseWorkspaceStore) {
  try {
    const payload = {
      workspace: state.workspace,
      modelText: state.modelText,
      latestValidation: state.latestValidation,
      workingBindings: state.workingBindings,
      candidateSnapshots: state.candidateSnapshots,
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
  } catch {
    // 忽略存储超限
  }
}
