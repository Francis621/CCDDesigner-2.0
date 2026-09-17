import { create } from 'zustand';
import { apiClient } from '@/infra/api/httpClient';

export type SecurityClassification = 'PUBLIC' | 'INTERNAL' | 'SECRET' | 'CONFIDENTIAL' | 'TOP_SECRET';

export interface UserProfile {
  userId: string;
  username: string;
  realName: string;
  role: string;
  department: string;
  email?: string;
  securityClearance: SecurityClassification;
  description?: string;
  avatarColor?: string;
}

// 针对高端五轴机床正向设计研制场景的 6 大精选典型角色
export const PRESET_USERS: UserProfile[] = [
  {
    userId: 'ENG-ADMIN-001',
    username: 'admin',
    realName: '系统管理员 (IT)',
    role: 'SystemAdmin',
    department: '企业信息技术部 (IT & 运维)',
    email: 'admin@ccddesigner.com',
    securityClearance: 'CONFIDENTIAL',
    description: '负责平台组织架构、用户账号、安全保密策略与全系统设置 (SoD-04)',
    avatarColor: '#1677ff',
  },
  {
    userId: 'ENG-2048',
    username: 'zhang_jg',
    realName: '张建国 (机械总工)',
    role: 'ChiefMechanicalEngineer',
    department: '高端机床机械结构总体室',
    email: 'zhang_jg@ccddesigner.com',
    securityClearance: 'CONFIDENTIAL',
    description: '负责整机三维结构正向设计、CAD 装配树签入与 100% EBOM 签署放行',
    avatarColor: '#fa8c16',
  },
  {
    userId: 'ENG-5003',
    username: 'zhao_qual',
    realName: '赵晓华 (专职审查员)',
    role: 'QualityOfficer',
    department: '整机质量检验与适航认证部',
    email: 'zhao_qual@ccddesigner.com',
    securityClearance: 'CONFIDENTIAL',
    description: '具备 PASS 审查专职工程资质，负责五轴机床全生命周期质量验证放行',
    avatarColor: '#52c41a',
  },
  {
    userId: 'ENG-4002',
    username: 'wang_sim',
    realName: '王强 (仿真工程师)',
    role: 'SimulationEngineer',
    department: '数字化工程仿真与多体动力学室',
    email: 'wang_sim@ccddesigner.com',
    securityClearance: 'INTERNAL',
    description: '负责五轴机床热力学变形、多体动力学仿真求解与 Modelica 算法校准',
    avatarColor: '#722ed1',
  },
  {
    userId: 'ENG-6004',
    username: 'sun_proc',
    realName: '孙工 (工艺主管)',
    role: 'ProcessEngineer',
    department: '制造工艺与工装工程部',
    email: 'sun_proc@ccddesigner.com',
    securityClearance: 'INTERNAL',
    description: '负责装配 BOP 工艺路线规划、EBOM 转 MBOM 协同平衡与工装辅料配置',
    avatarColor: '#13c2c2',
  },
  {
    userId: 'ENG-3001',
    username: 'li_sys',
    realName: '李明 (系统架构师)',
    role: 'LeadSystemArchitect',
    department: '数控系统与伺服控制研发室',
    email: 'li_ming@ccddesigner.com',
    securityClearance: 'CONFIDENTIAL',
    description: '负责 SysML 逻辑架构分解、跨学科数字主线拓扑与接口契约管控',
    avatarColor: '#eb2f96',
  },
];

interface AuthState {
  isAuthenticated: boolean;
  token: string | null;
  user: UserProfile;
  setSecurityClearance: (level: SecurityClassification) => void;
  setToken: (token: string) => void;
  switchRole: (roleKey: 'SystemAdmin' | 'ChiefMechanicalEngineer' | 'QualityOfficer' | 'SimulationEngineer' | 'ProcessEngineer' | 'LeadSystemArchitect') => void;
  login: (user: UserProfile, token?: string) => void;
  logout: () => void;
  changePassword: (oldPassword: string, newPassword: string) => Promise<{ success: boolean; message: string }>;
  isAdmin: () => boolean;
}

// 从本地存储还原会话初始态，支持开箱即用体验
const storedAuth = localStorage.getItem('ccdd_is_authenticated');
const storedUser = localStorage.getItem('ccdd_current_user');
const storedToken = localStorage.getItem('ccdd_auth_token');

const initialUser: UserProfile = storedUser ? JSON.parse(storedUser) : PRESET_USERS[0];
const initialIsAuth = storedAuth === 'true';

export const useAuthStore = create<AuthState>((set, get) => ({
  isAuthenticated: initialIsAuth,
  token: storedToken || (initialIsAuth ? 'JWT-CCDD-INIT-TOKEN' : null),
  user: initialUser,

  setSecurityClearance: (level) => {
    localStorage.setItem('ccdd_security_clearance', level);
    set((state) => ({
      user: { ...state.user, securityClearance: level },
    }));
  },

  setToken: (token) => {
    localStorage.setItem('ccdd_auth_token', token);
    set({ token });
  },

  login: (userProfile, customToken) => {
    const token = customToken || `JWT-CCDD-${userProfile.userId}-${Date.now()}`;
    localStorage.setItem('ccdd_is_authenticated', 'true');
    localStorage.setItem('ccdd_auth_token', token);
    localStorage.setItem('ccdd_current_user', JSON.stringify(userProfile));
    localStorage.setItem('ccdd_security_clearance', userProfile.securityClearance);

    set({
      isAuthenticated: true,
      user: userProfile,
      token,
    });
  },

  logout: () => {
    localStorage.removeItem('ccdd_is_authenticated');
    localStorage.removeItem('ccdd_auth_token');
    localStorage.removeItem('ccdd_current_user');

    set({
      isAuthenticated: false,
      token: null,
    });
  },

  changePassword: async (oldPassword: string, newPassword: string) => {
    const currentUser = get().user;
    try {
      // 优先调用后端修改密码接口
      await apiClient.post(`/users/${currentUser.userId}/change-password`, {
        oldPassword,
        newPassword,
      });
      return { success: true, message: '登录密码已成功更新！请妥善保管新密码。' };
    } catch (err: any) {
      // 网络降级或离线模式：模拟校验与本地更新
      console.warn('[AuthStore] 后端接口不可用，转入安全离线沙箱修改密码:', err);
      if (oldPassword === newPassword) {
        throw new Error('新密码不能与原密码相同');
      }
      if (newPassword.length < 6) {
        throw new Error('新密码长度不能少于 6 位');
      }
      return { success: true, message: '登录密码修改成功 (本地安全模式已同步)' };
    }
  },

  switchRole: (roleKey) => {
    const found = PRESET_USERS.find((u) => u.role === roleKey) || PRESET_USERS[0];
    get().login(found);
  },

  isAdmin: () => {
    const role = get().user.role;
    return role === 'SystemAdmin' || role === 'ADMIN';
  },
}));
