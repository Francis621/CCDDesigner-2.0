import React, { useState } from 'react';
import { ConfigProvider, theme } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { MainLayout } from './components/layout/MainLayout';
import { WorkbenchPage } from './pages/WorkbenchPage';
import { SuperBomWizardPage } from './pages/SuperBomWizardPage';
import { ThreadGraphPage } from './pages/ThreadGraphPage';
import { MbomBalancePage } from './pages/MbomBalancePage';
import { ProjectGateManagementPage } from './pages/ProjectGateManagementPage';
import { GenericModulePage } from './pages/GenericModulePage';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('workbench');

  const renderContent = () => {
    switch (activeTab) {
      // 1. 个人协同工作区
      case 'workbench':
        return <WorkbenchPage onNavigate={(tab) => setActiveTab(tab)} />;
      case 'mailbox':
        return (
          <GenericModulePage
            moduleKey="mailbox"
            moduleName="邮箱系统"
            category="个人协同中心"
            description="集中接收项目通知、制造下发批次回执异常告警及 ECN 工程变更审批邮件。"
            onNavigate={setActiveTab}
          />
        );
      case 'my-tasks':
        return (
          <GenericModulePage
            moduleKey="my-tasks"
            moduleName="我的任务"
            category="个人协同中心"
            description="汇总个人待办的设计校审、MRR制造就绪签署、BOP工艺审查及仿真验证工单。"
            onNavigate={setActiveTab}
          />
        );

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
        return <ProjectGateManagementPage />;
      case 'process-mgmt':
        return (
          <GenericModulePage
            moduleKey="process-mgmt"
            moduleName="流程管理"
            category="流程与质量部"
            description="驱动 IPD 集成产品开发流程、三员审计签批、制造就绪审查流及异常仲裁机制。"
            onNavigate={setActiveTab}
          />
        );
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
        return (
          <GenericModulePage
            moduleKey="baseline-mgmt"
            moduleName="基线管理"
            category="配置控制部"
            description="冻结设计/工艺工程基线，严格管理基线版本比对、锁定标志位及下发凭据凭证。"
            onNavigate={setActiveTab}
          />
        );
      case 'change-mgmt':
        return (
          <GenericModulePage
            moduleKey="change-mgmt"
            moduleName="变更管理"
            category="工程变更委员会 (CCB)"
            description="实施 ECR 变更申请、ECO 变更指令、波及影响自动推演及车间下发批次召回闭环。"
            onNavigate={setActiveTab}
          />
        );
      case 'document-mgmt':
        return (
          <GenericModulePage
            moduleKey="document-mgmt"
            moduleName="图文档管理"
            category="档案中心"
            description="基于 MinIO 对象存储纳管二维工程图样、SOP作业指导书、检验单及 3D 轻量化浏览文件。"
            onNavigate={setActiveTab}
          />
        );

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
      <MainLayout currentTab={activeTab} onTabChange={setActiveTab}>
        {renderContent()}
      </MainLayout>
    </ConfigProvider>
  );
};

export default App;
