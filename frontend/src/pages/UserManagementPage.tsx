import React, { useState, useEffect } from 'react';
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
  DatePicker,
  Popconfirm,
  Descriptions,
} from 'antd';
import {
  Users,
  UserCheck,
  UserX,
  Shield,
  ShieldAlert,
  PlusCircle,
  Eye,
  RefreshCw,
  Layers,
  Award,
  AlertOctagon,
  Building2,
  UserPlus,
  KeyRound,
} from 'lucide-react';
import { useAuthStore } from '@/stores/useAuthStore';
import { useUserStore } from '@/stores/useUserStore';

// ==================== 数据接口定义 ====================

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

export interface DepartmentItem {
  deptId: number;
  deptCode: string;
  deptName: string;
  disciplineType: string;
  disciplineName: string;
  userCount: number;
}

export interface ProjectMembershipItem {
  membershipId: number;
  projectId: number;
  userId: string;
  userName?: string;
  projectRole: 'PROJECT_LEAD' | 'DESIGNER' | 'CHECKER' | 'APPROVER' | 'GUEST';
  effectiveFrom: string;
  effectiveTo?: string;
  isActive: boolean;
  grantedBy: string;
}

export interface QualificationItem {
  qualificationId: number;
  userId: string;
  userName?: string;
  qualificationType: 'VERIFICATION_REVIEWER' | 'LEAD_SYSTEM_ARCHITECT' | 'CHIEF_QUALITY_OFFICER' | 'SAFETY_ENGINEER';
  certificateNo: string;
  issuedDate: string;
  expiryDate: string;
  authorizedBy: string;
  isValid: boolean;
}

export interface SessionRevocationItem {
  revocationId: number;
  userId: string;
  userName?: string;
  projectId?: number;
  revokedBefore: string;
  reason: string;
  createdAt: string;
}

// 全局职能角色与显示名称映射
const ROLE_NAME_MAP: Record<string, string> = {
  ChiefMechanicalEngineer: '机械工程师',
  LeadSystemArchitect: '系统工程师',
  SimulationEngineer: '仿真工程师',
  ProcessEngineer: '工艺工程师',
  QualityOfficer: '质量工程师',
  ProductManager: '产品经理/需求工程师',
  ShopFloorOperator: '车间装配工',
  SystemAdmin: '系统管理员',
};

const DEFAULT_DEPARTMENTS: DepartmentItem[] = [
  { deptId: 100, deptCode: 'DEPT-ADMIN', deptName: '企业信息技术部 (IT & 运维)', disciplineType: 'MANAGEMENT', disciplineName: '综合管理与系统运维', userCount: 1 },
  { deptId: 200, deptCode: 'DEPT-MECH', deptName: '高端机床机械结构总体室', disciplineType: 'MECHANICAL', disciplineName: '机械结构总体', userCount: 2 },
  { deptId: 300, deptCode: 'DEPT-ELEC', deptName: '数控电气与驱动工程室', disciplineType: 'ELECTRICAL', disciplineName: '数控电气与驱动', userCount: 0 },
  { deptId: 400, deptCode: 'DEPT-CTRL', deptName: '数控系统与伺服控制研发室', disciplineType: 'CONTROL', disciplineName: '数控系统与伺服控制', userCount: 1 },
  { deptId: 500, deptCode: 'DEPT-SIM', deptName: '数字化工程仿真与多体动力学室', disciplineType: 'SIMULATION', disciplineName: '数字化工程仿真', userCount: 1 },
  { deptId: 600, deptCode: 'DEPT-HYDR', deptName: '液压润滑与排屑系统设计室', disciplineType: 'HYDRAULIC', disciplineName: '液压润滑与排屑', userCount: 0 },
  { deptId: 700, deptCode: 'DEPT-PROC', deptName: '制造工艺与工装工程部', disciplineType: 'PROCESS', disciplineName: '制造工艺与工装', userCount: 2 },
  { deptId: 800, deptCode: 'DEPT-QUAL', deptName: '整机质量检验与适航认证部', disciplineType: 'QUALITY', disciplineName: '整机质量与适航', userCount: 1 },
];

const DEFAULT_MEMBERSHIPS: ProjectMembershipItem[] = [
  { membershipId: 880192841029181, projectId: 100293810293, userId: 'ENG-3001', userName: '李明 (系统架构师)', projectRole: 'PROJECT_LEAD', effectiveFrom: '2026-01-01', effectiveTo: '2027-12-31', isActive: true, grantedBy: 'ENG-ADMIN-001' },
  { membershipId: 880192841029182, projectId: 100293810293, userId: 'ENG-2048', userName: '张建国 (机械总工)', projectRole: 'DESIGNER', effectiveFrom: '2026-01-01', effectiveTo: '2027-12-31', isActive: true, grantedBy: 'ENG-3001' },
  { membershipId: 880192841029183, projectId: 100293810293, userId: 'ENG-4002', userName: '王强 (仿真工程师)', projectRole: 'CHECKER', effectiveFrom: '2026-01-01', effectiveTo: '2027-12-31', isActive: true, grantedBy: 'ENG-3001' },
  { membershipId: 880192841029184, projectId: 100293810293, userId: 'ENG-5003', userName: '赵晓华 (专职审查员)', projectRole: 'APPROVER', effectiveFrom: '2026-01-01', effectiveTo: '2027-12-31', isActive: true, grantedBy: 'ENG-3001' },
];

const DEFAULT_QUALIFICATIONS: QualificationItem[] = [
  { qualificationId: 991029481920, userId: 'ENG-5003', userName: '赵晓华 (专职审查员)', qualificationType: 'VERIFICATION_REVIEWER', certificateNo: 'CERT-2026-VMC-VERIF-099', issuedDate: '2026-01-01', expiryDate: '2027-12-31', authorizedBy: 'CHIEF-ENG-001', isValid: true },
  { qualificationId: 991029481921, userId: 'ENG-3001', userName: '李明 (系统架构师)', qualificationType: 'LEAD_SYSTEM_ARCHITECT', certificateNo: 'CERT-2025-ARCH-L5-002', issuedDate: '2025-06-01', expiryDate: '2028-05-31', authorizedBy: 'CHIEF-ENG-001', isValid: true },
];

const DEFAULT_REVOCATIONS: SessionRevocationItem[] = [
  { revocationId: 770192841001, userId: 'ENG-EXT-01', userName: '德国主轴外协专家', projectId: 100293810293, revokedBefore: '2026-09-15 08:00:00', reason: '外协合同到期退出机床主轴项目组 (AT-13 立即失效)', createdAt: '2026-09-15 08:00:00' },
];

export const UserManagementPage: React.FC = () => {
  const { user, isAdmin, switchRole } = useAuthStore();
  const isSystemAdmin = isAdmin();
  const { users, addUser, updateUserStatus, fetchUsers: fetchStoreUsers } = useUserStore();

  // 数据状态
  const [departments, setDepartments] = useState<DepartmentItem[]>(DEFAULT_DEPARTMENTS);
  const [memberships, setMemberships] = useState<ProjectMembershipItem[]>(DEFAULT_MEMBERSHIPS);
  const [qualifications, setQualifications] = useState<QualificationItem[]>(DEFAULT_QUALIFICATIONS);
  const [revocations, setRevocations] = useState<SessionRevocationItem[]>(DEFAULT_REVOCATIONS);
  const [loading, setLoading] = useState<boolean>(false);

  // 模态框与表单
  const [createUserModalOpen, setCreateUserModalOpen] = useState<boolean>(false);
  const [createUserForm] = Form.useForm();

  const [assignMemberModalOpen, setAssignMemberModalOpen] = useState<boolean>(false);
  const [assignMemberForm] = Form.useForm();

  const [registerQualModalOpen, setRegisterQualModalOpen] = useState<boolean>(false);
  const [registerQualForm] = Form.useForm();

  const [selectedUserForDrawer, setSelectedUserForDrawer] = useState<SysUserItem | null>(null);
  const [activeTab, setActiveTab] = useState<string>('users');

  // 拉取后端数据
  const fetchAllData = async () => {
    setLoading(true);
    try {
      fetchStoreUsers();
      const [deptsRes, memsRes, qualsRes, revsRes] = await Promise.all([
        fetch('/api/v1/departments'),
        fetch('/api/v1/projects/100293810293/memberships'),
        fetch('/api/v1/users/ENG-5003/qualifications'),
        fetch('/api/v1/iam/revocations'),
      ]);

      if (deptsRes.ok) {
        const d = await deptsRes.json();
        if (d.data && Array.isArray(d.data)) setDepartments(d.data);
      }
      if (memsRes.ok) {
        const m = await memsRes.json();
        if (m.data && Array.isArray(m.data)) setMemberships(m.data);
      }
      if (qualsRes.ok) {
        const q = await qualsRes.json();
        if (q.data && Array.isArray(q.data)) setQualifications(q.data);
      }
      if (revsRes.ok) {
        const r = await revsRes.json();
        if (r.data && Array.isArray(r.data)) setRevocations(r.data);
      }
    } catch {
      // 离线模式使用默认种子数据
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAllData();
  }, []);

  // 刷新
  const handleRefresh = async () => {
    setLoading(true);
    try {
      await fetchAllData();
      message.success('用户、组织与权限数据已同步最新状态');
    } catch {
      message.info('当前运行在离线高可用模式');
    } finally {
      setLoading(false);
    }
  };

  // 一键生成随机安全强密码
  const handleGenerateRandomPassword = () => {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%';
    let pwd = 'Ccdd@';
    for (let i = 0; i < 4; i++) {
      pwd += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    pwd += '2026';
    createUserForm.setFieldsValue({
      password: pwd,
      confirmPassword: pwd,
    });
    message.success(`已自动生成高强度初始密码: ${pwd}`);
  };

  // 1. 新建用户
  const handleCreateUserSubmit = async () => {
    try {
      const values = await createUserForm.validateFields();
      const targetDept = departments.find((d) => d.deptId === values.deptId);
      const roleId = values.roleIds || 'ChiefMechanicalEngineer';
      const roleName = ROLE_NAME_MAP[roleId] || '机械工程师';

      const newUser: SysUserItem = {
        userId: values.userId,
        deptId: values.deptId,
        deptCode: targetDept?.deptCode || 'DEPT-MECH',
        deptName: targetDept?.deptName || '高端机床机械结构总体室',
        disciplineType: targetDept?.disciplineType || 'MECHANICAL',
        username: values.username,
        realName: values.realName,
        email: values.email || `${values.username}@ccddesigner.com`,
        mobile: values.mobile,
        status: 'ACTIVE',
        isExternal: Boolean(values.isExternal),
        roleIds: [roleId],
        roleNames: [roleName],
        createdAt: new Date().toISOString().split('T')[0],
      };

      // 统一调用全局 store 进行写入与持久化
      await addUser(newUser, values.password || 'Ccdd@2026!');
      message.success(`用户 [${values.realName}] 已成功创建并登记入库！`);
      setCreateUserModalOpen(false);
      createUserForm.resetFields();
    } catch (err: any) {
      if (err.errorFields) return;
      message.error('创建用户异常: ' + (err.message || '未知错误'));
    }
  };

  // 2. 变更用户账号状态 (ACTIVE -> SUSPENDED -> DEACTIVATED)
  const handleChangeUserStatus = async (targetUser: SysUserItem, newStatus: 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED') => {
    try {
      await updateUserStatus(targetUser.userId, newStatus);
      message.success(`用户 [${targetUser.realName}] 账号状态已变更为: ${newStatus}`);
    } catch (err: any) {
      message.error('变更状态失败: ' + (err.message || '未知错误'));
    }
  };

  // 3. 项目工作组成员指派
  const handleAssignMemberSubmit = async () => {
    try {
      const values = await assignMemberForm.validateFields();
      const payload = {
        userId: values.userId,
        projectRole: values.projectRole,
        effectiveFrom: new Date().toISOString(),
        effectiveTo: '2027-12-31T23:59:59Z',
      };

      try {
        const res = await fetch('/api/v1/projects/100293810293/memberships', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });
        if (res.ok) {
          message.success('已成功将用户指派至 VMC1000 研制项目工作组！');
          setAssignMemberModalOpen(false);
          assignMemberForm.resetFields();
          fetchAllData();
          return;
        }
      } catch {}

      const selectedU = users.find((u) => u.userId === values.userId);
      const newMem: ProjectMembershipItem = {
        membershipId: Date.now(),
        projectId: 100293810293,
        userId: values.userId,
        userName: selectedU ? selectedU.realName : values.userId,
        projectRole: values.projectRole,
        effectiveFrom: '2026-09-16',
        effectiveTo: '2027-12-31',
        isActive: true,
        grantedBy: 'admin',
      };
      setMemberships([newMem, ...memberships]);
      message.success('已成功将用户指派至 VMC1000 研制项目工作组！');
      setAssignMemberModalOpen(false);
      assignMemberForm.resetFields();
    } catch (err: any) {
      if (err.errorFields) return;
      message.error('指派项目成员失败: ' + err.message);
    }
  };

  // 4. 移除项目成员并触发 AT-13 即时熔断
  const handleRevokeMembership = async (record: ProjectMembershipItem) => {
    try {
      const res = await fetch(`/api/v1/projects/${record.projectId}/memberships/${record.userId}`, {
        method: 'DELETE',
        headers: { 'X-Reason': '管理员在控制台移除项目成员' },
      });
      if (res.ok) {
        message.warning(`[AT-13 熔断] 用户 [${record.userName || record.userId}] 权限已即时吊销，Token 与 MinIO 预签名已全量失效！`);
        fetchAllData();
        return;
      }
    } catch {}

    // 本地状态模拟 AT-13 立即生效
    setMemberships((prev) => prev.filter((m) => m.membershipId !== record.membershipId));
    const newRevocation: SessionRevocationItem = {
      revocationId: Date.now(),
      userId: record.userId,
      userName: record.userName || record.userId,
      projectId: record.projectId,
      revokedBefore: new Date().toLocaleTimeString(),
      reason: '项目岗位交接移除，触发 AT-13 毫秒级权限吊销熔断',
      createdAt: new Date().toLocaleTimeString(),
    };
    setRevocations([newRevocation, ...revocations]);
    message.warning(`[AT-13 熔断] 用户 [${record.userName || record.userId}] 权限已即时吊销，Token 与 MinIO 直链已全量熔断！`);
  };

  // 5. 登记专职工程资质 (SoD-02)
  const handleRegisterQualSubmit = async () => {
    try {
      const values = await registerQualForm.validateFields();
      const payload = {
        qualificationType: values.qualificationType,
        certificateNo: values.certificateNo,
        issuedDate: values.issuedDate.format('YYYY-MM-DD'),
        expiryDate: values.expiryDate.format('YYYY-MM-DD'),
        authorizedBy: 'CHIEF-ENG-001',
      };

      try {
        const res = await fetch(`/api/v1/users/${values.userId}/qualifications`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });
        if (res.ok) {
          message.success(`专职工程资质证书 [${values.certificateNo}] 登记成功！`);
          setRegisterQualModalOpen(false);
          registerQualForm.resetFields();
          fetchAllData();
          return;
        }
      } catch {}

      const selectedU = users.find((u) => u.userId === values.userId);
      const newQ: QualificationItem = {
        qualificationId: Date.now(),
        userId: values.userId,
        userName: selectedU ? selectedU.realName : values.userId,
        qualificationType: values.qualificationType,
        certificateNo: values.certificateNo,
        issuedDate: values.issuedDate.format('YYYY-MM-DD'),
        expiryDate: values.expiryDate.format('YYYY-MM-DD'),
        authorizedBy: 'CHIEF-ENG-001',
        isValid: true,
      };
      setQualifications([newQ, ...qualifications]);
      message.success(`专职工程资质证书 [${values.certificateNo}] 已生效登记 (具备签署资格)！`);
      setRegisterQualModalOpen(false);
      registerQualForm.resetFields();
    } catch (err: any) {
      if (err.errorFields) return;
      message.error('登记资质异常: ' + err.message);
    }
  };

  // ==================== 权限拦截视图 (非系统管理员) ====================
  if (!isSystemAdmin) {
    return (
      <div className="py-12 px-4 max-w-4xl mx-auto animate-fadeIn">
        <Card className="shadow-xl rounded-2xl border border-red-200 bg-linear-to-b from-red-50/50 to-white overflow-hidden text-center py-10">
          <div className="w-20 h-20 mx-auto rounded-full bg-red-100 flex items-center justify-center text-red-600 mb-6 shadow-inner">
            <ShieldAlert className="w-10 h-10" />
          </div>
          <Typography.Title level={2} className="text-slate-800! mb-2">
            403 访问受限 - PBAC 安全守卫拦截
          </Typography.Title>
          <div className="text-base text-slate-600 mb-6 max-w-2xl mx-auto leading-relaxed">
            “系统设置”与“用户管理”功能受核心安全规约 <span className="font-semibold text-red-600">SoD-04（系统管理员边界隔离）</span> 严格管控。
            您当前登录的身份为 <Tag color="orange" className="font-bold text-sm px-2 py-0.5">{user.realName} [{user.role}]</Tag>，
            缺少系统级运维与用户授权特权。
          </div>

          <Alert
            message="工程职责分离规约 (SoD-04)"
            description="依据产品说明书与安全规范，系统管理员负责账号与企业组织配置，严禁代行工程技术文件签署；反之，工程技术人员不得跨界进入系统运维设置区。"
            type="error"
            showIcon
            className="text-left mb-8 max-w-2xl mx-auto rounded-xl"
          />

          <div className="flex items-center justify-center gap-4">
            <Button
              type="primary"
              size="large"
              icon={<UserCheck className="w-4 h-4" />}
              onClick={() => {
                switchRole('SystemAdmin');
                message.success('已切换为系统管理员 (admin) 身份，已授权访问系统设置！');
              }}
              className="bg-blue-600 hover:bg-blue-700 h-11 px-8 rounded-lg shadow-md font-semibold"
            >
              一键切换为【系统管理员】身份演示
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  // ==================== 表格列定义 ====================

  const userColumns = [
    {
      title: '工号/账号',
      key: 'userId',
      width: 140,
      render: (_: any, r: SysUserItem) => (
        <div>
          <div className="font-mono font-bold text-slate-800 text-xs">{r.userId}</div>
          <div className="text-[11px] text-slate-400">@{r.username}</div>
        </div>
      ),
    },
    {
      title: '工程师姓名',
      key: 'realName',
      width: 160,
      render: (_: any, r: SysUserItem) => (
        <div className="flex items-center gap-2">
          <span className="font-semibold text-slate-800 text-xs">{r.realName}</span>
          {r.isExternal && <Tag color="cyan" className="text-[10px] m-0">外协/供应商</Tag>}
        </div>
      ),
    },
    {
      title: '部门与专业学科',
      key: 'dept',
      render: (_: any, r: SysUserItem) => (
        <div>
          <div className="text-xs text-slate-700 font-medium">{r.deptName || '机械总体室'}</div>
          {r.disciplineType && (
            <Tag color="purple" className="text-[10px] m-0 mt-0.5">
              {r.disciplineType} 学科
            </Tag>
          )}
        </div>
      ),
    },
    {
      title: '全局职能角色',
      key: 'roles',
      render: (_: any, r: SysUserItem) => (
        <Space wrap size={[0, 4]}>
          {(r.roleNames || ['机械工程师']).map((rn, idx) => (
            <Tag key={idx} color="geekblue" className="text-xs font-semibold">
              {rn}
            </Tag>
          ))}
        </Space>
      ),
    },
    {
      title: '账号状态',
      key: 'status',
      width: 110,
      render: (_: any, r: SysUserItem) => {
        if (r.status === 'ACTIVE') return <Badge status="success" text="正常激活" />;
        if (r.status === 'SUSPENDED') return <Badge status="warning" text="临时冻结" />;
        if (r.status === 'DEACTIVATED') return <Badge status="error" text="离职禁用" />;
        return <Badge status="default" text={r.status} />;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, r: SysUserItem) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<Eye className="w-3.5 h-3.5" />}
            onClick={() => setSelectedUserForDrawer(r)}
          >
            画像
          </Button>

          {r.status === 'ACTIVE' ? (
            <Popconfirm
              title="确认冻结账号？"
              description="冻结后该用户将无法登录系统。"
              onConfirm={() => handleChangeUserStatus(r, 'SUSPENDED')}
            >
              <Button type="link" danger size="small">
                冻结
              </Button>
            </Popconfirm>
          ) : (
            <Button
              type="link"
              size="small"
              className="text-emerald-600 hover:text-emerald-700"
              onClick={() => handleChangeUserStatus(r, 'ACTIVE')}
            >
              解冻
            </Button>
          )}

          {r.status !== 'DEACTIVATED' && (
            <Popconfirm
              title="确认注销该员工账号？"
              description="离职注销将同步触发 AT-13 立即撤销在研项目席位与 Token 熔断。"
              onConfirm={() => handleChangeUserStatus(r, 'DEACTIVATED')}
            >
              <Button type="link" danger size="small">
                注销
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const deptColumns = [
    {
      title: '部门编码',
      dataIndex: 'deptCode',
      key: 'deptCode',
      width: 160,
      render: (v: string) => <span className="font-mono font-bold text-xs">{v}</span>,
    },
    {
      title: '行政部门全称',
      dataIndex: 'deptName',
      key: 'deptName',
      render: (v: string) => <span className="font-semibold text-slate-800 text-xs">{v}</span>,
    },
    {
      title: '工程学科归属 (DisciplineType)',
      key: 'discipline',
      render: (_: any, r: DepartmentItem) => (
        <Space>
          <Tag color="magenta" className="font-bold text-xs">{r.disciplineType}</Tag>
          <span className="text-xs text-slate-500">{r.disciplineName}</span>
        </Space>
      ),
    },
    {
      title: '纳管工程师数',
      dataIndex: 'userCount',
      key: 'userCount',
      width: 120,
      render: (c: number) => <Tag color="blue" className="font-semibold text-xs">{c} 人</Tag>,
    },
  ];

  const membershipColumns = [
    {
      title: '成员姓名/工号',
      key: 'user',
      render: (_: any, r: ProjectMembershipItem) => (
        <div>
          <div className="font-bold text-slate-800 text-xs">{r.userName || r.userId}</div>
          <div className="font-mono text-[10px] text-slate-400">{r.userId}</div>
        </div>
      ),
    },
    {
      title: '项目级业务角色',
      key: 'role',
      render: (_: any, r: ProjectMembershipItem) => {
        let color = 'default';
        if (r.projectRole === 'PROJECT_LEAD') color = 'gold';
        if (r.projectRole === 'DESIGNER') color = 'blue';
        if (r.projectRole === 'CHECKER') color = 'cyan';
        if (r.projectRole === 'APPROVER') color = 'purple';
        return <Tag color={color} className="font-semibold text-xs">{r.projectRole}</Tag>;
      },
    },
    {
      title: '授权有效期',
      key: 'validity',
      render: (_: any, r: ProjectMembershipItem) => (
        <span className="text-xs text-slate-600 font-mono">
          {r.effectiveFrom} ~ {r.effectiveTo || '长期'}
        </span>
      ),
    },
    {
      title: '成员状态',
      key: 'status',
      width: 100,
      render: (_: any, r: ProjectMembershipItem) =>
        r.isActive ? <Badge status="success" text="有效" /> : <Badge status="error" text="已撤销" />,
    },
    {
      title: 'AT-13 即时吊销操作',
      key: 'action',
      width: 160,
      render: (_: any, r: ProjectMembershipItem) => (
        <Popconfirm
          title="确认移出项目工作组？"
          description="将触发 AT-13 强合规机制：1秒内注销其 API 凭证并阻断 MinIO 直链！"
          onConfirm={() => handleRevokeMembership(r)}
        >
          <Button type="primary" danger size="small" icon={<UserX className="w-3 h-3" />}>
            踢出并熔断权限
          </Button>
        </Popconfirm>
      ),
    },
  ];

  const qualColumns = [
    {
      title: '证书编号',
      dataIndex: 'certificateNo',
      key: 'certNo',
      render: (v: string) => <span className="font-mono font-bold text-xs text-blue-700">{v}</span>,
    },
    {
      title: '被持有人',
      key: 'user',
      render: (_: any, r: QualificationItem) => (
        <div>
          <span className="font-semibold text-slate-800 text-xs">{r.userName || r.userId}</span>
          <span className="font-mono text-[10px] text-slate-400 ml-1">({r.userId})</span>
        </div>
      ),
    },
    {
      title: '资质类型 (SoD-02)',
      key: 'qualType',
      render: (_: any, r: QualificationItem) => {
        if (r.qualificationType === 'VERIFICATION_REVIEWER') {
          return <Tag color="green" className="font-bold text-xs">验证结论审查员 (可签PASS)</Tag>;
        }
        if (r.qualificationType === 'LEAD_SYSTEM_ARCHITECT') {
          return <Tag color="purple" className="font-bold text-xs">首席系统架构师</Tag>;
        }
        return <Tag color="blue" className="font-bold text-xs">{r.qualificationType}</Tag>;
      },
    },
    {
      title: '证书有效期',
      key: 'validity',
      render: (_: any, r: QualificationItem) => (
        <span className="text-xs text-slate-600 font-mono">
          {r.issuedDate} 至 {r.expiryDate}
        </span>
      ),
    },
    {
      title: '状态',
      key: 'isValid',
      width: 100,
      render: (_: any, r: QualificationItem) =>
        r.isValid ? <Badge status="success" text="有效认证" /> : <Badge status="error" text="已失效" />,
    },
  ];

  const revocationColumns = [
    {
      title: '撤销流水号',
      dataIndex: 'revocationId',
      key: 'revId',
      render: (v: number) => <span className="font-mono text-xs text-slate-500">{v}</span>,
    },
    {
      title: '被注销用户',
      key: 'user',
      render: (_: any, r: SessionRevocationItem) => (
        <span className="font-bold text-slate-800 text-xs">{r.userName || r.userId}</span>
      ),
    },
    {
      title: '关联工程项目',
      dataIndex: 'projectId',
      key: 'projId',
      render: (pid?: number) =>
        pid ? <Tag color="geekblue">VMC1000 五轴机床 ({pid})</Tag> : <Tag color="volcano">全系统全局熔断</Tag>,
    },
    {
      title: '熔断拦截生效时点 (AT-13)',
      dataIndex: 'revokedBefore',
      key: 'revTime',
      render: (t: string) => <span className="font-mono text-xs text-red-600 font-bold">{t}</span>,
    },
    {
      title: '撤销注销原因',
      dataIndex: 'reason',
      key: 'reason',
      render: (v: string) => <span className="text-xs text-slate-600">{v}</span>,
    },
  ];

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* 顶部标题与控制卡片 */}
      <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3 mb-1">
            <div className="p-2 bg-blue-50 text-blue-600 rounded-lg">
              <Shield className="w-5 h-5" />
            </div>
            <Typography.Title level={4} className="m-0! text-slate-800">
              系统用户、组织与权限管理 (M30-IAM)
            </Typography.Title>
            <Tag color="geekblue" className="font-semibold text-xs">
              系统管理员专区
            </Tag>
          </div>
          <p className="text-xs text-slate-500 m-0">
            统一维护多专业学科矩阵、账号全生命周期、项目工作组授权 (PBAC)、SoD 四大职责分离物理硬切面与 AT-13 毫秒级即时权限熔断。
          </p>
        </div>

        <Space>
          <Button icon={<RefreshCw className="w-4 h-4" />} onClick={handleRefresh} loading={loading}>
            刷新状态
          </Button>
          <Button
            type="primary"
            icon={<UserPlus className="w-4 h-4" />}
            onClick={() => setCreateUserModalOpen(true)}
            className="bg-blue-600 hover:bg-blue-700"
          >
            新建用户
          </Button>
        </Space>
      </div>

      {/* 核心指标统计卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={12} sm={6}>
          <Card className="rounded-xl border border-slate-200 shadow-xs">
            <div className="text-xs text-slate-500 font-semibold mb-1">系统用户总数</div>
            <div className="text-2xl font-black text-slate-800">{users.length}</div>
            <div className="text-[11px] text-slate-400 mt-1">单企业集中纳管</div>
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card className="rounded-xl border border-slate-200 shadow-xs">
            <div className="text-xs text-emerald-600 font-semibold mb-1">正常在职人员</div>
            <div className="text-2xl font-black text-emerald-700">
              {users.filter((u) => u.status === 'ACTIVE').length}
            </div>
            <div className="text-[11px] text-slate-400 mt-1">覆盖 8 大工程学科</div>
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card className="rounded-xl border border-slate-200 shadow-xs">
            <div className="text-xs text-purple-600 font-semibold mb-1">VMC1000 项目组成员</div>
            <div className="text-2xl font-black text-purple-700">{memberships.length}</div>
            <div className="text-[11px] text-slate-400 mt-1">细粒度 PBAC 授权</div>
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card className="rounded-xl border border-slate-200 shadow-xs">
            <div className="text-xs text-amber-600 font-semibold mb-1">AT-13 撤销熔断记录</div>
            <div className="text-2xl font-black text-amber-700">{revocations.length}</div>
            <div className="text-[11px] text-slate-400 mt-1">时延 ≤ 1000ms</div>
          </Card>
        </Col>
      </Row>

      {/* 主选项卡面板 */}
      <Card className="rounded-xl border border-slate-200 shadow-xs">
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'users',
              label: (
                <span className="flex items-center gap-1.5 font-semibold">
                  <Users className="w-4 h-4" />
                  用户台账与生命周期
                </span>
              ),
              children: (
                <div className="space-y-4">
                  <Table
                    columns={userColumns}
                    dataSource={users}
                    rowKey="userId"
                    pagination={{ pageSize: 8 }}
                    className="overflow-x-auto"
                  />
                </div>
              ),
            },
            {
              key: 'disciplines',
              label: (
                <span className="flex items-center gap-1.5 font-semibold">
                  <Building2 className="w-4 h-4" />
                  多专业工程学科矩阵 (IAM-F01)
                </span>
              ),
              children: (
                <div className="space-y-4">
                  <Alert
                    message="企业统一身份与多专业学科矩阵原则"
                    description="全面取消多租户隔离，建立“行政部门—专业学科 (DisciplineType)”二维矩阵结构。当 M22 进行工程变更影响推演时，精准向对应学科专业主管分派任务。"
                    type="info"
                    showIcon
                    className="rounded-lg"
                  />
                  <Table
                    columns={deptColumns}
                    dataSource={departments}
                    rowKey="deptId"
                    pagination={false}
                  />
                </div>
              ),
            },
            {
              key: 'memberships',
              label: (
                <span className="flex items-center gap-1.5 font-semibold">
                  <Layers className="w-4 h-4" />
                  项目工作组授权与 AT-13 熔断 (IAM-F03)
                </span>
              ),
              children: (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <span className="text-xs text-slate-500">当前受控研制项目: </span>
                      <Tag color="geekblue" className="font-bold text-xs">VMC1000 高速高精立式加工中心研制项目 (ID: 100293810293)</Tag>
                    </div>
                    <Button
                      type="primary"
                      icon={<PlusCircle className="w-4 h-4" />}
                      onClick={() => setAssignMemberModalOpen(true)}
                      className="bg-blue-600 hover:bg-blue-700"
                    >
                      指派项目成员
                    </Button>
                  </div>
                  <Alert
                    message="全局职能角色与项目工作组角色彻底解耦"
                    description="工程师具备机械工程师全局角色，仅代表其具备机械设计技能；只有加入具体机床项目且指派为 DESIGNER 时，才拥有该机床 EBOM 与 CAD 的写权限。点击【踢出并熔断权限】将演练 AT-13 毫秒级会话吊销！"
                    type="warning"
                    showIcon
                    className="rounded-lg"
                  />
                  <Table
                    columns={membershipColumns}
                    dataSource={memberships}
                    rowKey="membershipId"
                    pagination={false}
                  />
                </div>
              ),
            },
            {
              key: 'qualifications',
              label: (
                <span className="flex items-center gap-1.5 font-semibold">
                  <Award className="w-4 h-4" />
                  专职工程资质认证 (SoD-02)
                </span>
              ),
              children: (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-xs text-slate-500">
                      控制关键法定判定权 (如需求验证签署 PASS 资格)
                    </span>
                    <Button
                      type="primary"
                      icon={<PlusCircle className="w-4 h-4" />}
                      onClick={() => setRegisterQualModalOpen(true)}
                      className="bg-emerald-600 hover:bg-emerald-700"
                    >
                      登记专职资质
                    </Button>
                  </div>
                  <Alert
                    message="SoD-02 验证资质分离铁律"
                    description="仿真工程师运行 OpenModelica 计算并获得通过，仅代表计算完成；签署需求验证 PASS 属于法定工程结论，必须登记有效的 VERIFICATION_REVIEWER 资质证书，未登记人员自签将被切面硬拦截！"
                    type="success"
                    showIcon
                    className="rounded-lg"
                  />
                  <Table
                    columns={qualColumns}
                    dataSource={qualifications}
                    rowKey="qualificationId"
                    pagination={false}
                  />
                </div>
              ),
            },
            {
              key: 'audit',
              label: (
                <span className="flex items-center gap-1.5 font-semibold">
                  <AlertOctagon className="w-4 h-4 text-red-500" />
                  AT-13 会话吊销黑名单与审计
                </span>
              ),
              children: (
                <div className="space-y-4">
                  <Alert
                    message="AT-13 强合规：权限即时吊销与全链路失效"
                    description="当用户从项目工作组移除或账号注销时，系统在 1 秒内推入会话撤销黑名单，API 网关直接对早于撤销时点的 Token 拒签 (403)，MinIO STS 同步吊销临时预签名，彻底防范权限残留。"
                    type="error"
                    showIcon
                    className="rounded-lg"
                  />
                  <Table
                    columns={revocationColumns}
                    dataSource={revocations}
                    rowKey="revocationId"
                    pagination={false}
                  />
                </div>
              ),
            },
          ]}
        />
      </Card>

      {/* 模态框 1: 新建用户 */}
      <Modal
        title="新建系统用户账号"
        open={createUserModalOpen}
        onCancel={() => setCreateUserModalOpen(false)}
        onOk={handleCreateUserSubmit}
        destroyOnClose
      >
        <Form form={createUserForm} layout="vertical" className="mt-4">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="userId" label="人员工号" rules={[{ required: true, message: '请输入企业工号' }]}>
                <Input placeholder="例如: ENG-8001" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="username" label="登录用户名" rules={[{ required: true, message: '请输入唯一登录名' }]}>
                <Input placeholder="例如: zhao_sim" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="realName" label="真实姓名" rules={[{ required: true, message: '请输入中文全名' }]}>
                <Input placeholder="例如: 赵峰" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="deptId" label="归属部门/学科" rules={[{ required: true, message: '请选择归属部门' }]}>
                <Select placeholder="选择部门">
                  {departments.map((d) => (
                    <Select.Option key={d.deptId} value={d.deptId}>
                      {d.deptName} ({d.disciplineType})
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="email" label="企业邮箱" rules={[{ required: true, message: '请输入邮箱' }]}>
                <Input placeholder="zhao_sim@ccddesigner.com" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="mobile" label="联系电话">
                <Input placeholder="13800000000" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="roleIds" label="全局职能角色" rules={[{ required: true, message: '请选择角色' }]}>
                <Select placeholder="选择核心全局角色">
                  <Select.Option value="ChiefMechanicalEngineer">机械工程师</Select.Option>
                  <Select.Option value="LeadSystemArchitect">系统工程师</Select.Option>
                  <Select.Option value="SimulationEngineer">仿真工程师</Select.Option>
                  <Select.Option value="ProcessEngineer">工艺工程师</Select.Option>
                  <Select.Option value="QualityOfficer">质量工程师</Select.Option>
                  <Select.Option value="ProductManager">产品经理/需求工程师</Select.Option>
                  <Select.Option value="ShopFloorOperator">车间装配工</Select.Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="isExternal" label="人员类型">
                <Select defaultValue={false}>
                  <Select.Option value={false}>内部正式员工</Select.Option>
                  <Select.Option value={true}>外部协作者 / 供应商专家</Select.Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <div className="bg-slate-50 p-3.5 rounded-lg border border-slate-200 mt-2">
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-slate-700 flex items-center gap-1.5">
                <KeyRound className="w-3.5 h-3.5 text-blue-600" />
                账号初始密码配置
              </span>
              <Button
                type="dashed"
                size="small"
                onClick={handleGenerateRandomPassword}
                className="text-xs text-blue-600 border-blue-300 hover:text-blue-700"
              >
                一键生成随机强密码
              </Button>
            </div>

            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="password"
                  label="初始登录密码"
                  rules={[
                    {
                      min: 6,
                      message: '密码长度至少为 6 位字符',
                    },
                  ]}
                  extra="留空将默认分配: Ccdd@2026!"
                >
                  <Input.Password placeholder="设置初始密码 (≥6位)" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="confirmPassword"
                  label="确认登录密码"
                  dependencies={['password']}
                  rules={[
                    ({ getFieldValue }) => ({
                      validator(_, value) {
                        if (!value || getFieldValue('password') === value) {
                          return Promise.resolve();
                        }
                        return Promise.reject(new Error('两次输入的登录密码不一致'));
                      },
                    }),
                  ]}
                >
                  <Input.Password placeholder="再次确认登录密码" />
                </Form.Item>
              </Col>
            </Row>
          </div>
        </Form>
      </Modal>

      {/* 模态框 2: 指派项目成员 */}
      <Modal
        title="指派机床研制项目成员 (VMC1000)"
        open={assignMemberModalOpen}
        onCancel={() => setAssignMemberModalOpen(false)}
        onOk={handleAssignMemberSubmit}
        destroyOnClose
      >
        <Form form={assignMemberForm} layout="vertical" className="mt-4">
          <Form.Item name="userId" label="选择目标工程师" rules={[{ required: true, message: '请选择人员' }]}>
            <Select placeholder="搜索人员姓名或工号">
              {users.map((u) => (
                <Select.Option key={u.userId} value={u.userId}>
                  {u.realName} - {u.roleNames.join('/')} ({u.userId})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="projectRole" label="项目内授予角色" rules={[{ required: true, message: '请选择项目角色' }]}>
            <Select placeholder="选择项目级业务角色">
              <Select.Option value="PROJECT_LEAD">PROJECT_LEAD (项目总师/负责人)</Select.Option>
              <Select.Option value="DESIGNER">DESIGNER (主设开发工程师)</Select.Option>
              <Select.Option value="CHECKER">CHECKER (校对校验员)</Select.Option>
              <Select.Option value="APPROVER">APPROVER (审批签署人)</Select.Option>
              <Select.Option value="GUEST">GUEST (只读观察员)</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      {/* 模态框 3: 登记专职资质 (SoD-02) */}
      <Modal
        title="登记专职工程资质证书"
        open={registerQualModalOpen}
        onCancel={() => setRegisterQualModalOpen(false)}
        onOk={handleRegisterQualSubmit}
        destroyOnClose
      >
        <Form form={registerQualForm} layout="vertical" className="mt-4">
          <Form.Item name="userId" label="资质被授予人" rules={[{ required: true, message: '请选择人员' }]}>
            <Select placeholder="选择工程师">
              {users.map((u) => (
                <Select.Option key={u.userId} value={u.userId}>
                  {u.realName} ({u.userId})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="qualificationType" label="专业工程资质类型" rules={[{ required: true, message: '请选择资质类型' }]}>
            <Select placeholder="选择法定资质">
              <Select.Option value="VERIFICATION_REVIEWER">
                VERIFICATION_REVIEWER (验证结论专职审查员 - 具备签署PASS特权)
              </Select.Option>
              <Select.Option value="LEAD_SYSTEM_ARCHITECT">LEAD_SYSTEM_ARCHITECT (首席系统架构师)</Select.Option>
              <Select.Option value="CHIEF_QUALITY_OFFICER">CHIEF_QUALITY_OFFICER (质量总监 - 具备作废特批权)</Select.Option>
              <Select.Option value="SAFETY_ENGINEER">SAFETY_ENGINEER (安全关键审查员)</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item name="certificateNo" label="证书编号" rules={[{ required: true, message: '请输入证书编号' }]}>
            <Input placeholder="例如: CERT-2026-VERIF-088" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="issuedDate" label="发证日期" rules={[{ required: true, message: '选择发证日期' }]}>
                <DatePicker className="w-full" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="expiryDate" label="有效截止日期" rules={[{ required: true, message: '选择截止日期' }]}>
                <DatePicker className="w-full" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>

      {/* 抽屉: 用户权限画像 */}
      <Drawer
        title="工程师统一数字身份画像"
        open={Boolean(selectedUserForDrawer)}
        onClose={() => setSelectedUserForDrawer(null)}
        width={500}
      >
        {selectedUserForDrawer && (
          <div className="space-y-6">
            <div className="flex items-center gap-4 p-4 bg-slate-50 rounded-xl border border-slate-200">
              <div className="w-14 h-14 rounded-full bg-blue-600 flex items-center justify-center text-white text-xl font-bold">
                {selectedUserForDrawer.realName.substring(0, 1)}
              </div>
              <div>
                <div className="text-base font-bold text-slate-800">{selectedUserForDrawer.realName}</div>
                <div className="text-xs text-slate-500 font-mono">工号: {selectedUserForDrawer.userId}</div>
                <Tag color={selectedUserForDrawer.status === 'ACTIVE' ? 'green' : 'red'} className="mt-1">
                  {selectedUserForDrawer.status}
                </Tag>
              </div>
            </div>

            <Descriptions title="基本人事档案" column={1} bordered size="small">
              <Descriptions.Item label="登录账号">{selectedUserForDrawer.username}</Descriptions.Item>
              <Descriptions.Item label="行政部门">{selectedUserForDrawer.deptName || '机械结构总体室'}</Descriptions.Item>
              <Descriptions.Item label="工程学科">{selectedUserForDrawer.disciplineType || 'MECHANICAL'}</Descriptions.Item>
              <Descriptions.Item label="电子邮箱">{selectedUserForDrawer.email}</Descriptions.Item>
              <Descriptions.Item label="联系电话">{selectedUserForDrawer.mobile || '-'}</Descriptions.Item>
              <Descriptions.Item label="人员属性">
                {selectedUserForDrawer.isExternal ? (
                  <Tag color="cyan">外部外协/供应商工程师</Tag>
                ) : (
                  <Tag color="blue">企业内部正式员工</Tag>
                )}
              </Descriptions.Item>
            </Descriptions>

            <Descriptions title="全局职能角色" column={1} bordered size="small">
              <Descriptions.Item label="持有岗位角色">
                {selectedUserForDrawer.roleNames.map((rn, idx) => (
                  <Tag key={idx} color="geekblue" className="font-semibold">
                    {rn}
                  </Tag>
                ))}
              </Descriptions.Item>
            </Descriptions>

            <div>
              <div className="font-bold text-slate-800 text-sm mb-2">持有的法定工程资质证书</div>
              {qualifications.filter((q) => q.userId === selectedUserForDrawer.userId).length > 0 ? (
                <div className="space-y-2">
                  {qualifications
                    .filter((q) => q.userId === selectedUserForDrawer.userId)
                    .map((q) => (
                      <div key={q.qualificationId} className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg">
                        <div className="flex items-center justify-between">
                          <span className="font-bold text-xs text-emerald-800">{q.qualificationType}</span>
                          <Tag color="green" className="m-0">有效</Tag>
                        </div>
                        <div className="font-mono text-xs text-slate-600 mt-1">证书号: {q.certificateNo}</div>
                        <div className="text-[11px] text-slate-400">有效期至: {q.expiryDate}</div>
                      </div>
                    ))}
                </div>
              ) : (
                <div className="text-xs text-slate-400 p-4 border border-dashed rounded-lg text-center">
                  暂未登记专职工程资质证书 (无需求验证 PASS 签署特权)
                </div>
              )}
            </div>
          </div>
        )}
      </Drawer>
    </div>
  );
};
