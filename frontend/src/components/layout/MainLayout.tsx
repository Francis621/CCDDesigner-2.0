import React, { useState } from 'react';
import { Layout, Dropdown, MenuProps, Tag, Tooltip } from 'antd';
import {
  LayoutDashboard,
  Layers,
  GitFork,
  ShieldCheck,
  Shield,
  Box,
  LogOut,
} from 'lucide-react';
import { useAuthStore, SecurityClassification } from '@/stores/useAuthStore';
import { useProjectStore } from '@/stores/useProjectStore';

const { Header, Sider, Content } = Layout;

interface MainLayoutProps {
  currentTab: string;
  onTabChange: (key: string) => void;
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({
  currentTab,
  onTabChange,
  children,
}) => {
  const [collapsed, setCollapsed] = useState(false);
  const { user, setSecurityClearance } = useAuthStore();
  const { currentProject } = useProjectStore();

  const securityMenuItems: MenuProps['items'] = [
    { key: 'PUBLIC', label: '公开 (PUBLIC)' },
    { key: 'INTERNAL', label: '内部受控 (INTERNAL)' },
    { key: 'SECRET', label: '秘密 (SECRET)' },
    { key: 'CONFIDENTIAL', label: '机密 (CONFIDENTIAL)' },
    { key: 'TOP_SECRET', label: '绝密 (TOP_SECRET)' },
  ];

  const handleSecurityChange: MenuProps['onClick'] = ({ key }) => {
    setSecurityClearance(key as SecurityClassification);
  };

  const getSecurityBadgeColor = (sec: SecurityClassification) => {
    switch (sec) {
      case 'CONFIDENTIAL':
        return 'red';
      case 'SECRET':
        return 'volcano';
      case 'INTERNAL':
        return 'blue';
      case 'TOP_SECRET':
        return 'magenta';
      default:
        return 'default';
    }
  };

  const menuItems = [
    {
      key: 'workbench',
      icon: <LayoutDashboard className="w-4 h-4" />,
      label: '协同工作台 (M01)',
    },
    {
      key: 'super-bom',
      icon: <Layers className="w-4 h-4" />,
      label: '150% Super BOM 向导 (M14)',
    },
    {
      key: 'thread',
      icon: <GitFork className="w-4 h-4" />,
      label: '数字主线因果拓扑 (M23)',
    },
    {
      key: 'mbom',
      icon: <ShieldCheck className="w-4 h-4" />,
      label: 'EBOM/MBOM 平衡看板 (M25)',
    },
  ];

  return (
    <Layout className="min-h-screen">
      {/* 侧边导航栏 */}
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={(value) => setCollapsed(value)}
        theme="dark"
        className="bg-slate-900 border-r border-slate-800"
        width={250}
      >
        <div className="p-4 flex items-center gap-3 border-b border-slate-800">
          <div className="w-9 h-9 rounded-lg bg-blue-600 flex items-center justify-center text-white font-black text-lg shadow-md">
            <Box className="w-5 h-5" />
          </div>
          {!collapsed && (
            <div>
              <div className="font-bold text-white tracking-wide text-sm">CCDDesigner 2.0</div>
              <div className="text-[10px] text-slate-400 font-mono">高端数控机床正向设计</div>
            </div>
          )}
        </div>

        <div className="py-4">
          <div className="px-3 space-y-1">
            {menuItems.map((item) => {
              const active = currentTab === item.key;
              return (
                <button
                  key={item.key}
                  onClick={() => onTabChange(item.key)}
                  className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-xs font-medium transition-all text-left ${
                    active
                      ? 'bg-blue-600 text-white shadow-sm'
                      : 'text-slate-300 hover:bg-slate-800/80 hover:text-white'
                  }`}
                >
                  <span className={active ? 'text-white' : 'text-slate-400'}>{item.icon}</span>
                  {!collapsed && <span>{item.label}</span>}
                </button>
              );
            })}
          </div>
        </div>

        {!collapsed && (
          <div className="absolute bottom-12 left-0 right-0 p-4 border-t border-slate-800 text-slate-400 text-xs">
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
              <span>单体聚合服务 (在线)</span>
            </div>
            <div className="text-[11px] text-slate-500 mt-1">PostgreSQL & MinIO 2PC 正常</div>
          </div>
        )}
      </Sider>

      <Layout className="bg-slate-50">
        {/* 顶部全局状态栏 */}
        <Header className="bg-white border-b border-slate-200 px-6 h-16 flex items-center justify-between shadow-xs sticky top-0 z-10">
          <div className="flex items-center gap-3">
            <Tag color="geekblue" className="font-mono text-xs font-semibold m-0">
              {currentProject.projectCode}
            </Tag>
            <span className="text-sm font-bold text-slate-800 hidden sm:inline">
              {currentProject.projectName}
            </span>
          </div>

          <div className="flex items-center gap-4">
            {/* Bell-LaPadula 密级切换控制器 */}
            <Dropdown menu={{ items: securityMenuItems, onClick: handleSecurityChange }}>
              <div className="flex items-center gap-1.5 cursor-pointer bg-slate-100 hover:bg-slate-200 px-3 py-1.5 rounded-lg transition-colors border border-slate-300">
                <Shield className="w-3.5 h-3.5 text-slate-600" />
                <span className="text-xs font-semibold text-slate-700">当前密级:</span>
                <Tag color={getSecurityBadgeColor(user.securityClearance)} className="m-0 text-xs font-bold">
                  {user.securityClearance}
                </Tag>
              </div>
            </Dropdown>

            {/* 用户身份与岗位 */}
            <div className="flex items-center gap-2 pl-3 border-l border-slate-200">
              <div className="text-right">
                <div className="text-xs font-bold text-slate-800">{user.realName}</div>
                <div className="text-[10px] text-slate-500">{user.role}</div>
              </div>
              <Tooltip title="安全登出">
                <button className="p-1.5 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded transition-colors">
                  <LogOut className="w-4 h-4" />
                </button>
              </Tooltip>
            </div>
          </div>
        </Header>

        {/* 主内容区域 */}
        <Content className="p-6 max-w-7xl mx-auto w-full">
          {children}
        </Content>
      </Layout>
    </Layout>
  );
};
