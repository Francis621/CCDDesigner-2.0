import React, { useState } from 'react';
import { ConfigProvider, theme } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { MainLayout } from './components/layout/MainLayout';
import { WorkbenchPage } from './pages/WorkbenchPage';
import { SuperBomWizardPage } from './pages/SuperBomWizardPage';
import { ThreadGraphPage } from './pages/ThreadGraphPage';
import { MbomBalancePage } from './pages/MbomBalancePage';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('workbench');

  const renderContent = () => {
    switch (activeTab) {
      case 'workbench':
        return <WorkbenchPage onNavigate={(tab) => setActiveTab(tab)} />;
      case 'super-bom':
        return <SuperBomWizardPage />;
      case 'thread':
        return <ThreadGraphPage />;
      case 'mbom':
        return <MbomBalancePage />;
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
