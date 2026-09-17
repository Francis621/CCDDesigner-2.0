import { create } from 'zustand';

export interface SysUserItem {
  userId: string;
  deptId: number;
  deptCode?: string;
  deptName?: string;
  disciplineType?: string;
  username: string;
  realName: string;
  email: string;
  mobile?: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED' | 'LOCKED';
  isExternal: boolean;
  roleIds: string[];
  roleNames: string[];
  lastLoginAt?: string;
  createdAt?: string;
}

export const INITIAL_DEFAULT_USERS: SysUserItem[] = [
  {
    userId: 'ENG-ADMIN-001',
    deptId: 100,
    deptCode: 'DEPT-ADMIN',
    deptName: '企业信息技术部 (IT & 运维)',
    disciplineType: 'MANAGEMENT',
    username: 'admin',
    realName: '系统管理员 (IT)',
    email: 'admin@ccddesigner.com',
    mobile: '13800000001',
    status: 'ACTIVE',
    isExternal: false,
    roleIds: ['SystemAdmin'],
    roleNames: ['系统管理员'],
    lastLoginAt: '2026-09-17 15:00:00',
    createdAt: '2026-01-01',
  },
  {
    userId: 'ENG-2048',
    deptId: 200,
    deptCode: 'DEPT-MECH',
    deptName: '高端机床机械结构总体室',
    disciplineType: 'MECHANICAL',
    username: 'zhang_jg',
    realName: '张建国 (机械总工)',
    email: 'zhang_jg@ccddesigner.com',
    mobile: '13800000002',
    status: 'ACTIVE',
    isExternal: false,
    roleIds: ['ChiefMechanicalEngineer'],
    roleNames: ['机械工程师', '机械总工'],
    lastLoginAt: '2026-09-17 14:30:00',
    createdAt: '2026-01-01',
  },
  {
    userId: 'ENG-3001',
    deptId: 400,
    deptCode: 'DEPT-CTRL',
    deptName: '数控系统与伺服控制研发室',
    disciplineType: 'CONTROL',
    username: 'li_sys',
    realName: '李明 (系统架构师)',
    email: 'li_ming@ccddesigner.com',
    mobile: '13800000003',
    status: 'ACTIVE',
    isExternal: false,
    roleIds: ['LeadSystemArchitect'],
    roleNames: ['系统工程师与总体架构师'],
    lastLoginAt: '2026-09-17 13:10:00',
    createdAt: '2026-01-01',
  },
  {
    userId: 'ENG-4002',
    deptId: 500,
    deptCode: 'DEPT-SIM',
    deptName: '数字化工程仿真与多体动力学室',
    disciplineType: 'SIMULATION',
    username: 'wang_sim',
    realName: '王强 (仿真工程师)',
    email: 'wang_qiang@ccddesigner.com',
    mobile: '13800000004',
    status: 'ACTIVE',
    isExternal: false,
    roleIds: ['SimulationEngineer'],
    roleNames: ['仿真工程师'],
    lastLoginAt: '2026-09-17 11:20:00',
    createdAt: '2026-01-01',
  },
  {
    userId: 'ENG-5003',
    deptId: 800,
    deptCode: 'DEPT-QUAL',
    deptName: '整机质量检验与适航认证部',
    disciplineType: 'QUALITY',
    username: 'zhao_qual',
    realName: '赵晓华 (专职审查员)',
    email: 'zhao_xh@ccddesigner.com',
    mobile: '13800000005',
    status: 'ACTIVE',
    isExternal: false,
    roleIds: ['QualityOfficer'],
    roleNames: ['质量与服务工程师', '专职审查员'],
    lastLoginAt: '2026-09-17 10:05:00',
    createdAt: '2026-01-01',
  },
  {
    userId: 'ENG-6004',
    deptId: 700,
    deptCode: 'DEPT-PROC',
    deptName: '制造工艺与工装工程部',
    disciplineType: 'PROCESS',
    username: 'sun_proc',
    realName: '孙工 (工艺主管)',
    email: 'sun_proc@ccddesigner.com',
    mobile: '13800000006',
    status: 'ACTIVE',
    isExternal: false,
    roleIds: ['ProcessEngineer'],
    roleNames: ['工艺工程师'],
    lastLoginAt: '2026-09-17 09:30:00',
    createdAt: '2026-01-01',
  },
  {
    userId: 'ENG-7005',
    deptId: 700,
    deptCode: 'DEPT-PROC',
    deptName: '制造工艺与工装工程部',
    disciplineType: 'PROCESS',
    username: 'qian_field',
    realName: '钱师傅 (车间装配工)',
    email: 'qian_field@ccddesigner.com',
    mobile: '13800000007',
    status: 'ACTIVE',
    isExternal: false,
    roleIds: ['ShopFloorOperator'],
    roleNames: ['车间装配工'],
    lastLoginAt: '2026-09-17 08:45:00',
    createdAt: '2026-01-01',
  },
  {
    userId: 'ENG-EXT-01',
    deptId: 200,
    deptCode: 'DEPT-MECH',
    deptName: '高端机床机械结构总体室',
    disciplineType: 'MECHANICAL',
    username: 'ext_supplier',
    realName: '德国主轴外协专家',
    email: 'spindle_ext@supplier.de',
    mobile: '13900000008',
    status: 'ACTIVE',
    isExternal: true,
    roleIds: ['ChiefMechanicalEngineer'],
    roleNames: ['外协专家'],
    lastLoginAt: '2026-09-16 15:30:00',
    createdAt: '2026-01-01',
  },
];

const STORAGE_KEY = 'ccdd_system_users_v2';

// 从本地持久化存储加载已有用户
function loadStoredUsers(): SysUserItem[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed: SysUserItem[] = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.length > 0) {
        // 合并默认用户以确保核心种子不丢失
        const mergedMap = new Map<string, SysUserItem>();
        INITIAL_DEFAULT_USERS.forEach((u) => mergedMap.set(u.userId, u));
        parsed.forEach((u) => mergedMap.set(u.userId, u));
        return Array.from(mergedMap.values());
      }
    }
  } catch (e) {
    console.warn('[useUserStore] 读取本地用户存储失败，重置为默认种子数据:', e);
  }
  return INITIAL_DEFAULT_USERS;
}

interface UserState {
  users: SysUserItem[];
  loading: boolean;
  fetchUsers: () => Promise<void>;
  addUser: (newUser: SysUserItem, rawPassword?: string) => Promise<boolean>;
  updateUserStatus: (userId: string, newStatus: SysUserItem['status']) => Promise<boolean>;
  getActiveUsers: () => SysUserItem[];
}

export const useUserStore = create<UserState>((set, get) => ({
  users: loadStoredUsers(),
  loading: false,

  fetchUsers: async () => {
    set({ loading: true });
    try {
      const res = await fetch('/api/v1/users');
      if (res.ok) {
        const json = await res.json();
        if (json && json.data && Array.isArray(json.data) && json.data.length > 0) {
          // 将后端返回用户与本地已创建用户合并去重（以 userId 和 username 为准）
          const currentList = get().users;
          const map = new Map<string, SysUserItem>();

          // 先载入后端用户
          (json.data as any[]).forEach((item) => {
            map.set(item.userId, {
              userId: item.userId,
              deptId: item.deptId || 200,
              deptCode: item.deptCode,
              deptName: item.deptName || '高端机床研制中心',
              disciplineType: item.disciplineType || 'MECHANICAL',
              username: item.username,
              realName: item.realName || item.username,
              email: item.email || `${item.username}@ccddesigner.com`,
              mobile: item.mobile,
              status: item.status || 'ACTIVE',
              isExternal: Boolean(item.isExternal),
              roleIds: item.roleIds || ['ChiefMechanicalEngineer'],
              roleNames: item.roleNames || ['机械工程师'],
              lastLoginAt: item.lastLoginAt,
              createdAt: item.createdAt || '2026-09-17',
            });
          });

          // 保留本地手动新增但后端尚未持久化（如离线状态）的用户
          currentList.forEach((u) => {
            if (!map.has(u.userId)) {
              map.set(u.userId, u);
            }
          });

          const merged = Array.from(map.values());
          localStorage.setItem(STORAGE_KEY, JSON.stringify(merged));
          set({ users: merged });
          return;
        }
      }
    } catch {
      // 离线状态保持当前已加载用户
    } finally {
      set({ loading: false });
    }
  },

  addUser: async (newUser: SysUserItem, rawPassword?: string) => {
    // 1. 尝试调用后端 IAM 接口
    try {
      await fetch('/api/v1/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: newUser.userId,
          deptId: newUser.deptId,
          username: newUser.username,
          realName: newUser.realName,
          email: newUser.email,
          mobile: newUser.mobile,
          isExternal: newUser.isExternal,
          roleIds: newUser.roleIds,
          password: rawPassword || 'Ccdd@2026!',
        }),
      });
    } catch (e) {
      console.warn('[useUserStore] 后端写入不可用，转入本地持久化存储:', e);
    }

    // 2. 无论后端是否联通，必先写入本地 store 与 localStorage 持久化！
    const currentUsers = get().users;
    // 去重更新
    const filtered = currentUsers.filter((u) => u.userId !== newUser.userId && u.username !== newUser.username);
    const updated = [newUser, ...filtered];

    localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
    set({ users: updated });
    return true;
  },

  updateUserStatus: async (userId: string, newStatus: SysUserItem['status']) => {
    try {
      await fetch(`/api/v1/users/${userId}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ targetStatus: newStatus }),
      });
    } catch (e) {
      console.warn('[useUserStore] 后端状态更新不可用，转入本地更新:', e);
    }

    const currentUsers = get().users;
    const updated = currentUsers.map((u) => (u.userId === userId ? { ...u, status: newStatus } : u));
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
    set({ users: updated });
    return true;
  },

  getActiveUsers: () => {
    const list = get().users;
    return list.filter((u) => u.status === 'ACTIVE' || !u.status);
  },
}));
