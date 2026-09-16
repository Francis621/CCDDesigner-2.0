import React from 'react';
import { Card, Tag, Button, Empty } from 'antd';
import {
  ArrowLeft,
  ShieldAlert,
  Compass,
} from 'lucide-react';
import { useProjectStore } from '@/stores/useProjectStore';

interface GenericModulePageProps {
  moduleKey: string;
  moduleName: string;
  category: string;
  description: string;
  onNavigate: (tab: string) => void;
}

export const GenericModulePage: React.FC<GenericModulePageProps> = ({
  moduleKey,
  moduleName,
  category,
  description,
  onNavigate,
}) => {
  const { currentProject } = useProjectStore();

  return (
    <div className="space-y-6">
      {/* 顶部标题卡片 */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-2 bg-blue-50 text-blue-600 rounded-lg">
              <Compass className="w-5 h-5" />
            </span>
            <h1 className="text-xl font-bold text-slate-900 m-0">
              {moduleName}
            </h1>
            <Tag color="blue">{category}</Tag>
          </div>
          <p className="text-sm text-slate-500 mt-1 m-0">
            {description}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            icon={<ArrowLeft className="w-4 h-4" />}
            onClick={() => onNavigate('workbench')}
          >
            返回协同工作台
          </Button>
        </div>
      </div>

      {/* 模块详情与业务占位展示 */}
      <Card className="border-slate-200 shadow-sm">
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={
            <div className="space-y-3 py-6">
              <h3 className="text-base font-bold text-slate-800 m-0">
                【{moduleName}】领域数据通道已就绪
              </h3>
              <p className="text-xs text-slate-500 max-w-md mx-auto m-0 leading-relaxed">
                当前项目 <span className="font-mono text-blue-600 font-semibold">{currentProject.projectCode}</span> 已纳管入主干数据总线。本模块正在对接底层微服务，可先通过【协同工作台】、【配置管理】、【产品配置】或【工艺设计】查看关联业务。
              </p>
              <div className="flex flex-wrap justify-center gap-2 pt-2">
                <Button
                  size="small"
                  type="primary"
                  onClick={() => onNavigate('super-bom')}
                  className="text-xs"
                >
                  前往 150% Super BOM 向导
                </Button>
                <Button
                  size="small"
                  onClick={() => onNavigate('thread')}
                  className="text-xs"
                >
                  查看数字主线因果拓扑
                </Button>
                <Button
                  size="small"
                  onClick={() => onNavigate('mbom')}
                  className="text-xs"
                >
                  查看 EBOM/MBOM 平衡看板
                </Button>
              </div>
            </div>
          }
        />

        <div className="mt-6 pt-6 border-t border-slate-100 grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
            <span className="text-slate-400 font-mono">模块系统标识:</span>
            <div className="font-mono font-bold text-slate-800 mt-0.5">{moduleKey}</div>
          </div>
          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
            <span className="text-slate-400 font-mono">数据权威部门:</span>
            <div className="font-semibold text-slate-800 mt-0.5">{category}</div>
          </div>
          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
            <span className="text-slate-400 font-mono">密级控制规范:</span>
            <div className="font-semibold text-emerald-700 mt-0.5 flex items-center gap-1">
              <ShieldAlert className="w-3.5 h-3.5" /> PBAC 动态密级与访问受控
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};
