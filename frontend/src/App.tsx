import React, { useState } from 'react';
import { ConfigProvider, theme } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { MainLayout } from './components/layout/MainLayout';
import { WorkbenchPage } from './pages/WorkbenchPage';
import { SuperBomWizardPage } from './pages/SuperBomWizardPage';
import { ThreadGraphPage } from './pages/ThreadGraphPage';
import { MbomBalancePage } from './pages/MbomBalancePage';
import { ProjectGateManagementPage } from './pages/ProjectGateManagementPage';
import { DocumentManagementPage } from './pages/DocumentManagementPage';
import { BaselineManagementPage } from './pages/BaselineManagementPage';
import { ChangeManagementPage } from './pages/ChangeManagementPage';
import { UserManagementPage } from './pages/UserManagementPage';
import { MailboxPage } from './pages/MailboxPage';
import { WorkflowCenterPage } from './pages/WorkflowCenterPage';
import { GenericModulePage } from './pages/GenericModulePage';
import { LoginPage } from './pages/LoginPage';
import { useAuthStore } from './stores/useAuthStore';

export const App: React.FC = () => {
  const { isAuthenticated } = useAuthStore();
  const [activeTab, setActiveTab] = useState<string>('workbench');

  const renderContent = () => {
    switch (activeTab) {
      // 1. 个人协同工作区
      case 'workbench':
        return <WorkbenchPage onNavigate={(tab) => setActiveTab(tab)} />;
      case 'mailbox':
        return <MailboxPage onNavigate={(tab) => setActiveTab(tab)} />;
      case 'my-tasks':
      case 'workflow-center':
        return <WorkflowCenterPage onNavigate={(tab) => setActiveTab(tab)} />;

      // 2. 主干工程业务流程
      case 'order-mgmt':
        return (
          <GenericModulePage
            moduleKey="order-mgmt"
            moduleName="订单管理"
            category="营销与交付中心"
            description="管理客户定制化机床销售意向、交付节点、合同选配参数及商业配置输入。"
            onNavigate={setActiveTab}
          />
        );
      case 'project-mgmt':
      case 'project-mgmt-overview':
        return <ProjectGateManagementPage activeSubAction="overview" onNavigateSubAction={setActiveTab} />;
      case 'project-create':
        return <ProjectGateManagementPage activeSubAction="create" onNavigateSubAction={setActiveTab} />;
      case 'project-edit':
        return <ProjectGateManagementPage activeSubAction="edit" onNavigateSubAction={setActiveTab} />;
      case 'project-delete':
        return <ProjectGateManagementPage activeSubAction="delete" onNavigateSubAction={setActiveTab} />;
      case 'process-mgmt':
        return <WorkflowCenterPage onNavigate={(tab) => setActiveTab(tab)} />;
      case 'system-design':
        return (
          <GenericModulePage
            moduleKey="system-design"
            moduleName="系统设计"
            category="系统架构室"
            description="基于 SysML v2 规范进行机床整机需求分解、多体动力学逻辑架构及软硬件接口设计。"
            onNavigate={setActiveTab}
          />
        );
      case 'product-platform':
        return (
          <GenericModulePage
            moduleKey="product-platform"
            moduleName="产品平台"
            category="平台技术部"
            description="沉淀五轴数控机床主轴、转台、立柱等标准化通用平台及可复用资产库。"
            onNavigate={setActiveTab}
          />
        );

      // 配置管理与产品配置
      case 'super-bom':
        return <SuperBomWizardPage />;
      case 'thread':
        return <ThreadGraphPage />;

      case 'detail-design':
        return (
          <GenericModulePage
            moduleKey="detail-design"
            moduleName="详细设计"
            category="机械电气设计部"
            description="承接三维参数化 CAD 装配体建模、工程图样细化、热态有限元分析及多物理场仿真。"
            onNavigate={setActiveTab}
          />
        );

      // 工艺设计
      case 'mbom':
        return <MbomBalancePage />;

      // 闭环管控
      case 'baseline-mgmt':
        return <BaselineManagementPage />;
      case 'change-mgmt':
        return <ChangeManagementPage />;
      case 'document-mgmt':
      case 'document-overview':
      case 'document-checkout':
      case 'document-upload':
      case 'document-export':
        return <DocumentManagementPage />;

      // 系统设置与用户管理 (管理员专区)
      case 'system-settings':
      case 'user-mgmt':
        return <UserManagementPage />;

      default:
        return <WorkbenchPage onNavigate={(tab) => setActiveTab(tab)} />;
    }
  };

  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        algorithm: theme.defaultAlgorithm,
        token: {
          colorPrimary: '#1677ff',
          borderRadius: 8,
          fontFamily:
            "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
        },
        components: {
          Card: {
            headerFontSize: 14,
          },
          Button: {
            controlHeight: 36,
          },
        },
      }}
    >
      {!isAuthenticated ? (
        <LoginPage />
      ) : (
        <MainLayout currentTab={activeTab} onTabChange={setActiveTab}>
          {renderContent()}
        </MainLayout>
      )}
    </ConfigProvider>
  );
};

export default App;
