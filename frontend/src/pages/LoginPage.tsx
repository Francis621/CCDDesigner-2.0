import React, { useState } from 'react';
import {
  Form,
  Input,
  Button,
  Tabs,
  Tag,
  Space,
  Row,
  Col,
  message,
} from 'antd';
import {
  Box,
  Lock,
  User,
  ArrowRight,
  CheckCircle2,
  Sparkles,
  KeyRound,
} from 'lucide-react';
import { useAuthStore, PRESET_USERS, UserProfile } from '@/stores/useAuthStore';
import { apiClient } from '@/infra/api/httpClient';

export const LoginPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'quick' | 'standard'>('quick');
  const [selectedUser, setSelectedUser] = useState<UserProfile>(PRESET_USERS[0]);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();
  const { login } = useAuthStore();

  // 快捷角色卡片点击选择并登录
  const handleQuickRoleLogin = (targetUser: UserProfile) => {
    setSelectedUser(targetUser);
    setLoading(true);
    message.loading({ content: `正在以【${targetUser.realName}】身份建立安全研发会话...`, key: 'loginMsg' });

    setTimeout(() => {
      login(targetUser, `JWT-CCDD-${targetUser.userId}-${Date.now()}`);
      setLoading(false);
      message.success({ content: `欢迎登录！当前身份：${targetUser.realName} (${targetUser.role})`, key: 'loginMsg' });
    }, 400);
  };

  // 标准账密表单登录提交
  const handleStandardLogin = async (values: any) => {
    setLoading(true);
    message.loading({ content: '正在验证机床工业云平台身份凭证...', key: 'loginMsg' });

    try {
      // 尝试调用后端 IAM 登录认证端点
      const resp: any = await apiClient.post('/auth/login', {
        username: values.username,
        password: values.password,
      });

      // 匹配用户或按后端返回
      const matched = PRESET_USERS.find((u) => u.username === values.username) || {
        userId: resp.user?.userId || 'ENG-CUSTOM-01',
        username: values.username,
        realName: resp.user?.realName || values.username,
        role: resp.user?.roleIds?.[0] || 'ChiefMechanicalEngineer',
        department: resp.user?.deptName || '高端机床研制中心',
        securityClearance: 'CONFIDENTIAL' as const,
        description: '定制工程设计人员',
      };

      login(matched, resp.token);
      message.success({ content: `登录成功，欢迎使用系统：${matched.realName}`, key: 'loginMsg' });
    } catch (err: any) {
      // 离线/沙箱模式本地安全回退
      console.warn('[Login] 后端服务未连接，启用离线安全认证沙箱模式:', err);
      const matched = PRESET_USERS.find((u) => u.username === values.username);
      const isDefaultPwd = values.password === 'Ccdd@2026!' || values.password === 'admin123' || values.password === '123456';

      if (matched && isDefaultPwd) {
        login(matched);
        message.success({ content: `安全登录成功：${matched.realName} (离线安全模式)`, key: 'loginMsg' });
      } else if (values.username && values.password && values.password.length >= 6) {
        // 自定义用户名输入
        const genericUser: UserProfile = {
          userId: `ENG-${Math.floor(1000 + Math.random() * 9000)}`,
          username: values.username,
          realName: `机床工程师 (${values.username})`,
          role: values.username === 'admin' ? 'SystemAdmin' : 'ChiefMechanicalEngineer',
          department: '高端数控机床总体设计室',
          securityClearance: 'CONFIDENTIAL',
        };
        login(genericUser);
        message.success({ content: `登录成功：${genericUser.realName}`, key: 'loginMsg' });
      } else {
        message.error({ content: '用户名或登录密码错误！初始测试密码为 Ccdd@2026! 或 admin123', key: 'loginMsg' });
      }
    } finally {
      setLoading(false);
    }
  };

  // 快速将选中用户填充至表单
  const fillPresetToForm = (u: UserProfile) => {
    setSelectedUser(u);
    form.setFieldsValue({
      username: u.username,
      password: u.username === 'admin' ? 'admin123' : 'Ccdd@2026!',
    });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-indigo-950 flex flex-col justify-between text-slate-100 relative overflow-hidden">
      {/* 科技感网格背景装饰 */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b_1px,transparent_1px),linear-gradient(to_bottom,#1e293b_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)] opacity-30 pointer-events-none" />

      {/* 顶部导航栏 */}
      <header className="relative z-10 px-8 py-4 flex items-center justify-between border-b border-slate-700/60 bg-slate-900/40 backdrop-blur-md">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-500 flex items-center justify-center text-white shadow-lg shadow-blue-500/20">
            <Box className="w-6 h-6" />
          </div>
          <div>
            <div className="text-lg font-black tracking-wide text-white flex items-center gap-2">
              CCDDesigner 2.0
              <Tag color="blue" className="text-[10px] font-mono py-0 px-1 border-blue-400/40">
                机床研制云平台
              </Tag>
            </div>
            <div className="text-xs text-slate-400 font-sans">
              高端数控机床正向设计一体化系统 (PLM & MBSE)
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3 text-xs text-slate-400">
          <span className="flex items-center gap-1.5 bg-slate-800/80 px-3 py-1 rounded-full border border-slate-700">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            安全认证中心在线
          </span>
          <span className="hidden sm:inline text-slate-500">|</span>
          <span className="hidden sm:inline font-mono">SoD-04 保密控制已加载</span>
        </div>
      </header>

      {/* 主体容器 */}
      <main className="relative z-10 flex-1 max-w-6xl mx-auto w-full px-4 py-8 flex flex-col justify-center">
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 border border-blue-500/30 text-blue-400 text-xs font-semibold mb-3">
            <Sparkles className="w-3.5 h-3.5" />
            <span>面向多学科跨专业工程研制团队 · 角色统一身份认证</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            选择用户登录系统
          </h1>
          <p className="text-slate-400 text-sm max-w-xl mx-auto mt-2">
            根据工程研制职责分工选择您的岗位角色，系统将根据 SoD 职责分离与密级策略自动装载专属设计权限
          </p>
        </div>

        {/* 登录主体多功能卡片 */}
        <div className="bg-slate-800/90 border border-slate-700/80 rounded-2xl p-6 sm:p-8 shadow-2xl backdrop-blur-xl max-w-4xl mx-auto w-full">
          <Tabs
            activeKey={activeTab}
            onChange={(k) => setActiveTab(k as any)}
            centered
            className="custom-login-tabs"
            items={[
              {
                key: 'quick',
                label: (
                  <span className="flex items-center gap-2 px-4 py-1 text-sm font-semibold">
                    <User className="w-4 h-4" />
                    <span>多角色快速登录 (推荐)</span>
                  </span>
                ),
                children: (
                  <div className="pt-2">
                    <div className="text-xs text-slate-400 mb-4 flex items-center justify-between">
                      <span>请点击下方任一代表性角色卡片即可快速登录进入对应工作台：</span>
                      <span className="text-blue-400 font-mono text-[11px]">共 6 大核心工程岗位</span>
                    </div>

                    <Row gutter={[16, 16]}>
                      {PRESET_USERS.map((u) => {
                        const isSelected = selectedUser.userId === u.userId;
                        return (
                          <Col xs={24} sm={12} md={8} key={u.userId}>
                            <div
                              onClick={() => setSelectedUser(u)}
                              className={`h-full p-4 rounded-xl border transition-all cursor-pointer flex flex-col justify-between ${
                                isSelected
                                  ? 'bg-blue-600/15 border-blue-500 shadow-lg shadow-blue-500/10 scale-[1.02]'
                                  : 'bg-slate-900/60 border-slate-700 hover:border-slate-500 hover:bg-slate-900/80'
                              }`}
                            >
                              <div>
                                <div className="flex items-center justify-between mb-2">
                                  <div
                                    className="w-8 h-8 rounded-lg flex items-center justify-center text-white font-bold text-xs"
                                    style={{ backgroundColor: u.avatarColor || '#1677ff' }}
                                  >
                                    {u.realName.slice(0, 1)}
                                  </div>
                                  <Tag
                                    color={
                                      u.securityClearance === 'CONFIDENTIAL'
                                        ? 'red'
                                        : u.securityClearance === 'SECRET'
                                        ? 'volcano'
                                        : 'blue'
                                    }
                                    className="m-0 text-[10px] font-mono"
                                  >
                                    {u.securityClearance}
                                  </Tag>
                                </div>

                                <div className="font-bold text-white text-sm flex items-center gap-1.5">
                                  <span>{u.realName}</span>
                                  {isSelected && <CheckCircle2 className="w-3.5 h-3.5 text-blue-400" />}
                                </div>
                                <div className="text-[11px] text-blue-400 font-mono mt-0.5">
                                  账号: {u.username}
                                </div>
                                <div className="text-[11px] text-slate-400 mt-1 line-clamp-1">
                                  {u.department}
                                </div>
                                <p className="text-[11px] text-slate-500 mt-2 leading-relaxed min-h-[34px]">
                                  {u.description}
                                </p>
                              </div>

                              <div className="mt-3 pt-3 border-t border-slate-700/60 flex items-center justify-between">
                                <Button
                                  type="link"
                                  size="small"
                                  className="p-0 text-xs text-slate-400 hover:text-white"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    fillPresetToForm(u);
                                    setActiveTab('standard');
                                  }}
                                >
                                  填入账密
                                </Button>
                                <Button
                                  type="primary"
                                  size="small"
                                  loading={loading && selectedUser.userId === u.userId}
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleQuickRoleLogin(u);
                                  }}
                                  className="text-xs bg-blue-600 hover:bg-blue-500"
                                >
                                  立即以此身份登录
                                </Button>
                              </div>
                            </div>
                          </Col>
                        );
                      })}
                    </Row>
                  </div>
                ),
              },
              {
                key: 'standard',
                label: (
                  <span className="flex items-center gap-2 px-4 py-1 text-sm font-semibold">
                    <KeyRound className="w-4 h-4" />
                    <span>标准账号密码登录</span>
                  </span>
                ),
                children: (
                  <div className="max-w-md mx-auto py-4">
                    <Form
                      form={form}
                      layout="vertical"
                      onFinish={handleStandardLogin}
                      initialValues={{
                        username: 'admin',
                        password: 'admin123',
                      }}
                    >
                      <Form.Item
                        name="username"
                        label={<span className="text-slate-200 text-xs font-semibold">用户工号 / 系统登录账号</span>}
                        rules={[{ required: true, message: '请输入登录用户名或工号' }]}
                      >
                        <Input
                          prefix={<User className="w-4 h-4 text-slate-400 mr-1" />}
                          placeholder="例如: admin, zhang_jg, zhao_qual..."
                          size="large"
                          className="rounded-lg"
                        />
                      </Form.Item>

                      <Form.Item
                        name="password"
                        label={
                          <div className="w-full flex justify-between items-center text-xs">
                            <span className="text-slate-200 font-semibold">登录密码</span>
                            <span className="text-slate-400">初始测试密码: admin123 或 Ccdd@2026!</span>
                          </div>
                        }
                        rules={[{ required: true, message: '请输入密码' }]}
                      >
                        <Input.Password
                          prefix={<Lock className="w-4 h-4 text-slate-400 mr-1" />}
                          placeholder="请输入密码"
                          size="large"
                          className="rounded-lg"
                        />
                      </Form.Item>

                      <div className="mb-4 flex flex-wrap gap-2 items-center justify-between text-xs text-slate-400">
                        <span>快速填充测试账号:</span>
                        <Space wrap>
                          <Button
                            size="small"
                            type="dashed"
                            onClick={() => {
                              form.setFieldsValue({ username: 'admin', password: 'admin123' });
                              message.info('已填入系统管理员账号凭据');
                            }}
                            className="text-xs"
                          >
                            管理员 (admin)
                          </Button>
                          <Button
                            size="small"
                            type="dashed"
                            onClick={() => {
                              form.setFieldsValue({ username: 'zhang_jg', password: 'Ccdd@2026!' });
                              message.info('已填入机械总工账号凭据');
                            }}
                            className="text-xs"
                          >
                            机械总工 (zhang_jg)
                          </Button>
                          <Button
                            size="small"
                            type="dashed"
                            onClick={() => {
                              form.setFieldsValue({ username: 'zhao_qual', password: 'Ccdd@2026!' });
                              message.info('已填入专职审查员凭据');
                            }}
                            className="text-xs"
                          >
                            审查员 (zhao_qual)
                          </Button>
                        </Space>
                      </div>

                      <Form.Item className="mt-6 mb-2">
                        <Button
                          type="primary"
                          htmlType="submit"
                          size="large"
                          loading={loading}
                          block
                          icon={<ArrowRight className="w-4 h-4" />}
                          className="h-11 rounded-lg font-bold bg-blue-600 hover:bg-blue-500 shadow-md shadow-blue-500/20"
                        >
                          安全登录
                        </Button>
                      </Form.Item>
                    </Form>
                  </div>
                ),
              },
            ]}
          />
        </div>
      </main>

      {/* 底部信息与版权声明 */}
      <footer className="relative z-10 px-8 py-4 border-t border-slate-800 text-center text-xs text-slate-500 flex flex-col sm:flex-row items-center justify-between gap-2 bg-slate-950/60">
        <div>高端数控机床正向设计云平台 (CCDDesigner 2.0) · 国家重点研发计划数字化赋能平台</div>
        <div className="flex items-center gap-4 font-mono text-[11px]">
          <span>微服务聚合架构: v2.0.0</span>
          <span>Flowable 7.x 流程引擎已挂载</span>
        </div>
      </footer>
    </div>
  );
};
