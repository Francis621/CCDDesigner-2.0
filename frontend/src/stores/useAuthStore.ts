import { create } from 'zustand';

export type SecurityClassification = 'PUBLIC' | 'INTERNAL' | 'SECRET' | 'CONFIDENTIAL' | 'TOP_SECRET';

export interface UserProfile {
  username: string;
  realName: string;
  role: string;
  department: string;
  securityClearance: SecurityClassification;
}

interface AuthState {
  token: string | null;
  user: UserProfile;
  setSecurityClearance: (level: SecurityClassification) => void;
  setToken: (token: string) => void;
  switchRole: (roleKey: 'SystemAdmin' | 'ChiefMechanicalEngineer' | 'QualityOfficer' | 'SimulationEngineer') => void;
  isAdmin: () => boolean;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token: 'MOCK-JWT-ENGINEER-TOKEN-2026',
  user: {
    username: 'admin',
    realName: '系统管理员 (IT)',
    role: 'SystemAdmin',
    department: '企业信息技术部 (IT & 运维)',
    securityClearance: 'CONFIDENTIAL',
  },
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
  switchRole: (roleKey) => {
    if (roleKey === 'SystemAdmin') {
      set({
        user: {
          username: 'admin',
          realName: '系统管理员 (IT)',
          role: 'SystemAdmin',
          department: '企业信息技术部 (IT & 运维)',
          securityClearance: 'CONFIDENTIAL',
        },
      });
    } else if (roleKey === 'ChiefMechanicalEngineer') {
      set({
        user: {
          username: 'zhang_jg',
          realName: '张建国 (机械总工)',
          role: 'ChiefMechanicalEngineer',
          department: '高端机床机械结构总体室',
          securityClearance: 'CONFIDENTIAL',
        },
      });
    } else if (roleKey === 'QualityOfficer') {
      set({
        user: {
          username: 'zhao_qual',
          realName: '赵晓华 (专职审查员)',
          role: 'QualityOfficer',
          department: '整机质量检验与适航认证部',
          securityClearance: 'CONFIDENTIAL',
        },
      });
    } else {
      set({
        user: {
          username: 'wang_sim',
          realName: '王强 (仿真工程师)',
          role: 'SimulationEngineer',
          department: '数字化工程仿真与多体动力学室',
          securityClearance: 'INTERNAL',
        },
      });
    }
  },
  isAdmin: () => {
    const role = get().user.role;
    return role === 'SystemAdmin' || role === 'ADMIN';
  },
}));
