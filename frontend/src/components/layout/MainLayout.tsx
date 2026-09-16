import React, { useState } from 'react';
import { Layout, Menu, Dropdown, MenuProps, Tag, Tooltip } from 'antd';
import {
  LayoutDashboard,
  Mail,
  CheckSquare,
  FileSpreadsheet,
  Briefcase,
  GitMerge,
  Cpu,
  Package,
  Sliders,
  Layers,
  Settings,
  GitFork,
  PenTool,
  Factory,
  ShieldCheck,
  BookmarkCheck,
  GitCompare,
  FileText,
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

  // 严格按照用户需求文档规格构建的主菜单树
  const menuItems: MenuProps['items'] = [
    {
      key: 'workbench',
      icon: <LayoutDashboard className="w-4 h-4" />,
      label: '协同工作台',
    },
    {
      key: 'mailbox',
      icon: <Mail className="w-4 h-4" />,
      label: '邮箱',
    },
    {
      key: 'my-tasks',
      icon: <CheckSquare className="w-4 h-4" />,
      label: '我的任务',
    },
    {
      type: 'divider',
      className: 'bg-slate-800 my-2',
    },
    {
      key: 'order-mgmt',
      icon: <FileSpreadsheet className="w-4 h-4" />,
      label: '订单管理',
    },
    {
      key: 'project-mgmt',
      icon: <Briefcase className="w-4 h-4" />,
      label: '项目管理',
    },
    {
      key: 'process-mgmt',
      icon: <GitMerge className="w-4 h-4" />,
      label: '流程管理',
    },
    {
      key: 'system-design',
      icon: <Cpu className="w-4 h-4" />,
      label: '系统设计',
    },
    {
      key: 'product-platform',
      icon: <Package className="w-4 h-4" />,
      label: '产品平台',
    },
    {
      key: 'config-mgmt',
      icon: <Sliders className="w-4 h-4" />,
      label: '配置管理',
      children: [
        {
          key: 'super-bom',
          icon: <Layers className="w-4 h-4" />,
          label: '150% Super BOM 向导',
        },
      ],
    },
    {
      key: 'product-config',
      icon: <Settings className="w-4 h-4" />,
      label: '产品配置',
      children: [
        {
          key: 'thread',
          icon: <GitFork className="w-4 h-4" />,
          label: '数字主线因果拓扑',
        },
      ],
    },
    {
      key: 'detail-design',
      icon: <PenTool className="w-4 h-4" />,
      label: '详细设计',
    },
    {
      key: 'process-design',
      icon: <Factory className="w-4 h-4" />,
      label: '工艺设计',
      children: [
        {
          key: 'mbom',
          icon: <ShieldCheck className="w-4 h-4" />,
          label: 'EBOM/MBOM平衡看板',
        },
      ],
    },
    {
      key: 'baseline-mgmt',
      icon: <BookmarkCheck className="w-4 h-4" />,
      label: '基线管理',
    },
    {
      key: 'change-mgmt',
      icon: <GitCompare className="w-4 h-4" />,
      label: '变更管理',
    },
    {
      key: 'document-mgmt',
      icon: <FileText className="w-4 h-4" />,
      label: '图文档管理',
    },
  ];

  return (
    <Layout className="min-h-screen">
      {/* 侧边主菜单导航栏 */}
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={(value) => setCollapsed(value)}
        theme="dark"
        className="bg-slate-900 border-r border-slate-800 flex flex-col justify-between"
        width={250}
      >
        <div className="flex flex-col h-full">
          {/* 系统 Logo 与产品标识 */}
          <div className="p-4 flex items-center gap-3 border-b border-slate-800 shrink-0">
            <div className="w-9 h-9 rounded-lg bg-blue-600 flex items-center justify-center text-white font-black text-lg shadow-md shrink-0">
              <Box className="w-5 h-5" />
            </div>
            {!collapsed && (
              <div className="overflow-hidden">
                <div className="font-bold text-white tracking-wide text-sm truncate">CCDDesigner 2.0</div>
                <div className="text-[10px] text-slate-400 font-mono truncate">高端数控机床正向设计</div>
              </div>
            )}
          </div>

          {/* 滚动菜单主体 */}
          <div className="flex-1 overflow-y-auto py-2 custom-scrollbar">
            <Menu
              theme="dark"
              mode="inline"
              selectedKeys={[currentTab]}
              defaultOpenKeys={['config-mgmt', 'product-config', 'process-design']}
              items={menuItems}
              onClick={({ key }) => onTabChange(key)}
              className="bg-transparent border-r-0 text-xs"
            />
          </div>

          {/* 底部运行环境状态 */}
          {!collapsed && (
            <div className="p-4 border-t border-slate-800 text-slate-400 text-xs shrink-0">
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
                <span>主干聚合服务 (在线)</span>
              </div>
              <div className="text-[11px] text-slate-500 mt-1">PostgreSQL & MinIO 正常</div>
            </div>
          )}
        </div>
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
