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
}

export const useAuthStore = create<AuthState>((set) => ({
  token: 'MOCK-JWT-ENGINEER-TOKEN-2026',
  user: {
    username: 'lead_engineer_zhang',
    realName: '张建国 (主任设计师)',
    role: 'CHIEF_MECHANICAL_ENGINEER',
    department: '高端数控机床正向设计院 - 五轴总体室',
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
}));
