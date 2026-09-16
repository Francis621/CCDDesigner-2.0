import React, { useState, useEffect } from 'react';
import { Card, Tag, Input, Modal, List, Badge, Button } from 'antd';
import {
  Search,
  Cpu,
  Layers,
  GitFork,
  Activity,
  ShieldCheck,
  Zap,
  ArrowUpRight,
  Clock,
  ExternalLink,
  ChevronRight,
} from 'lucide-react';
import { useProjectStore } from '@/stores/useProjectStore';
import { useAuthStore, SecurityClassification } from '@/stores/useAuthStore';

interface SearchItem {
  id: string;
  category: 'SYSML' | 'BOM' | 'SIMULATION' | 'THREAD';
  title: string;
  code: string;
  description: string;
  security: SecurityClassification;
  targetTab: string;
}

const MOCK_SEARCH_DATA: SearchItem[] = [
  {
    id: '1',
    category: 'SYSML',
    title: '主轴箱传动总成结构定义 (SpindleBox Package)',
    code: 'sysml::vmc850::spindle_box',
    description: 'SysML v2 核心逻辑构件，约束主轴电机与轴承游隙参数',
    security: 'CONFIDENTIAL',
    targetTab: 'thread',
  },
  {
    id: '2',
    category: 'BOM',
    title: 'BT40-12000rpm 直联式高速主轴组件',
    code: 'BOM-MAT-SPINDLE-BT40',
    description: '150% Super BOM 可选装配单元，依赖规则 RULE_BT40_OILMIST',
    security: 'INTERNAL',
    targetTab: 'super-bom',
  },
  {
    id: '3',
    category: 'SIMULATION',
    title: '主轴阶跃切削负载热伸长瞬态热力学仿真',
    code: 'SIM-CASE-2026-09-H850',
    description: 'OpenModelica + FMU 联合仿真工况，最大预测热变形 12.4μm',
    security: 'CONFIDENTIAL',
    targetTab: 'workbench',
  },
  {
    id: '4',
    category: 'THREAD',
    title: '全链路拓扑追溯：从切削刚度需求到 MBOM OP20 铣削工位',
    code: 'URN:ccdd:thread:vmc850:stiffness-chain',
    description: 'PostgreSQL 递归图计算，覆盖 18 个因果关系节点',
    security: 'SECRET',
    targetTab: 'thread',
  },
  {
    id: '5',
    category: 'BOM',
    title: '高压中心出水系统增压泵单元 (70Bar)',
    code: 'BOM-OPT-COOL-CTS70',
    description: '与主轴密封形式存在互斥规则，需配合直联电机选配',
    security: 'INTERNAL',
    targetTab: 'super-bom',
  },
];

interface WorkbenchPageProps {
  onNavigate: (key: string) => void;
}

export const WorkbenchPage: React.FC<WorkbenchPageProps> = ({ onNavigate }) => {
  const { currentProject } = useProjectStore();
  const { user } = useAuthStore();
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState<SearchItem[]>(MOCK_SEARCH_DATA);

  // 监听 Cmd + K / Ctrl + K 全局快捷键
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setIsSearchOpen(true);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // 快捷检索过滤
  useEffect(() => {
    if (!searchKeyword.trim()) {
      setSearchResults(MOCK_SEARCH_DATA);
    } else {
      const kw = searchKeyword.toLowerCase();
      setSearchResults(
        MOCK_SEARCH_DATA.filter(
          (item) =>
            item.title.toLowerCase().includes(kw) ||
            item.code.toLowerCase().includes(kw) ||
            item.description.toLowerCase().includes(kw)
        )
      );
    }
  }, [searchKeyword]);

  const getSecurityColor = (sec: SecurityClassification) => {
    switch (sec) {
      case 'CONFIDENTIAL':
        return 'red';
      case 'SECRET':
        return 'volcano';
      case 'INTERNAL':
        return 'blue';
      default:
        return 'default';
    }
  };

  return (
    <div className="space-y-6">
      {/* 顶部机床工程上下文总览卡片 */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-800 to-indigo-950 text-white rounded-xl p-6 shadow-xl border border-slate-700">
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-4">
          <div>
            <div className="flex items-center gap-3 mb-2">
              <span className="px-2.5 py-0.5 rounded text-xs font-semibold bg-blue-500/20 text-blue-300 border border-blue-500/30">
                {currentProject.projectCode}
              </span>
              <Tag color="cyan" className="m-0 font-medium">
                {currentProject.activeBranch}
              </Tag>
              <Tag color="green" className="m-0 font-medium">
                {currentProject.currentRevision}
              </Tag>
              <Badge status="processing" text={<span className="text-emerald-400 text-xs font-semibold">协同建模就绪</span>} />
            </div>
            <h1 className="text-2xl font-bold tracking-tight text-white m-0">
              {currentProject.projectName}
            </h1>
            <p className="text-slate-400 text-sm mt-1 m-0">
              机型架构: {currentProject.machineType} | 责任工程师: {user.realName}
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => setIsSearchOpen(true)}
              className="flex items-center gap-2 bg-slate-700/60 hover:bg-slate-700 text-slate-200 px-4 py-2 rounded-lg border border-slate-600 transition-all text-sm font-medium shadow-sm hover:border-slate-500"
            >
              <Search className="w-4 h-4 text-slate-400" />
              <span>快速搜索...</span>
              <kbd className="bg-slate-800 text-slate-300 px-1.5 py-0.5 rounded text-xs font-mono border border-slate-600">
                ⌘K
              </kbd>
            </button>
            <Button
              type="primary"
              className="bg-blue-600 hover:bg-blue-500 h-9 font-medium"
              onClick={() => onNavigate('super-bom')}
            >
              配置新机型
            </Button>
          </div>
        </div>
      </div>

      {/* 四大核心指标卡片 */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="border-slate-200 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider m-0">
                MBSE 逻辑构件
              </p>
              <h3 className="text-2xl font-bold text-slate-800 mt-1 m-0">142 个</h3>
              <p className="text-xs text-emerald-600 mt-1 m-0 flex items-center gap-1 font-medium">
                <ArrowUpRight className="w-3 h-3" /> SysON / Flexo 2PC 已同步
              </p>
            </div>
            <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
              <Cpu className="w-6 h-6" />
            </div>
          </div>
        </Card>

        <Card className="border-slate-200 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider m-0">
                150% Super BOM 规则
              </p>
              <h3 className="text-2xl font-bold text-slate-800 mt-1 m-0">86 条</h3>
              <p className="text-xs text-blue-600 mt-1 m-0 flex items-center gap-1 font-medium">
                <Zap className="w-3 h-3" /> 规则求解静态无冲突
              </p>
            </div>
            <div className="p-3 bg-indigo-50 text-indigo-600 rounded-xl">
              <Layers className="w-6 h-6" />
            </div>
          </div>
        </Card>

        <Card className="border-slate-200 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider m-0">
                数字主线拓扑边数
              </p>
              <h3 className="text-2xl font-bold text-slate-800 mt-1 m-0">1,248 关系</h3>
              <p className="text-xs text-purple-600 mt-1 m-0 flex items-center gap-1 font-medium">
                <Activity className="w-3 h-3" /> PostgreSQL 递归遍历 &lt; 85ms
              </p>
            </div>
            <div className="p-3 bg-purple-50 text-purple-600 rounded-xl">
              <GitFork className="w-6 h-6" />
            </div>
          </div>
        </Card>

        <Card className="border-slate-200 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider m-0">
                制造物料平衡率
              </p>
              <h3 className="text-2xl font-bold text-emerald-600 mt-1 m-0">100.0%</h3>
              <p className="text-xs text-emerald-600 mt-1 m-0 flex items-center gap-1 font-medium">
                <ShieldCheck className="w-3 h-3" /> 守恒残差为 0 (已达 MRR 门禁)
              </p>
            </div>
            <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
              <ShieldCheck className="w-6 h-6" />
            </div>
          </div>
        </Card>
      </div>

      {/* 核心工作流导航卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div
          onClick={() => onNavigate('super-bom')}
          className="group bg-white rounded-xl p-6 border border-slate-200 shadow-sm hover:border-blue-500 hover:shadow-lg transition-all cursor-pointer relative overflow-hidden"
        >
          <div className="absolute top-0 right-0 w-24 h-24 bg-blue-50 rounded-bl-full -mr-6 -mt-6 group-hover:scale-110 transition-transform"></div>
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2.5 bg-blue-600 text-white rounded-lg">
              <Layers className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-bold text-slate-800 m-0 group-hover:text-blue-600 transition-colors">
              M14: 150% Super BOM 规则向导
            </h3>
          </div>
          <p className="text-slate-600 text-sm leading-relaxed mb-4">
            面向五轴数控机床多变型配置，提供可视化特性向导、规则 DSL 实时求解试算、冲突检测及多解歧义手工裁决。
          </p>
          <div className="flex items-center text-blue-600 font-semibold text-xs gap-1">
            进入配置向导 <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
          </div>
        </div>

        <div
          onClick={() => onNavigate('thread')}
          className="group bg-white rounded-xl p-6 border border-slate-200 shadow-sm hover:border-purple-500 hover:shadow-lg transition-all cursor-pointer relative overflow-hidden"
        >
          <div className="absolute top-0 right-0 w-24 h-24 bg-purple-50 rounded-bl-full -mr-6 -mt-6 group-hover:scale-110 transition-transform"></div>
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2.5 bg-purple-600 text-white rounded-lg">
              <GitFork className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-bold text-slate-800 m-0 group-hover:text-purple-600 transition-colors">
              M23: 数字主线全链路因果拓扑
            </h3>
          </div>
          <p className="text-slate-600 text-sm leading-relaxed mb-4">
            基于 PostgreSQL 递归图遍历，实现从需求、SysML 架构、三维 CAD 到仿真验证的上下游因果追溯与变更波及度推演。
          </p>
          <div className="flex items-center text-purple-600 font-semibold text-xs gap-1">
            探寻因果网络 <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
          </div>
        </div>

        <div
          onClick={() => onNavigate('mbom')}
          className="group bg-white rounded-xl p-6 border border-slate-200 shadow-sm hover:border-emerald-500 hover:shadow-lg transition-all cursor-pointer relative overflow-hidden"
        >
          <div className="absolute top-0 right-0 w-24 h-24 bg-emerald-50 rounded-bl-full -mr-6 -mt-6 group-hover:scale-110 transition-transform"></div>
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2.5 bg-emerald-600 text-white rounded-lg">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-bold text-slate-800 m-0 group-hover:text-emerald-600 transition-colors">
              M25: EBOM/MBOM 平衡残差看板
            </h3>
          </div>
          <p className="text-slate-600 text-sm leading-relaxed mb-4">
            设计 BOM 向车间工艺装配 BOM 拆分重组，严密检验 100% 消耗守恒残差，阻断未平衡投产，保障制造下发闭环。
          </p>
          <div className="flex items-center text-emerald-600 font-semibold text-xs gap-1">
            校验制造平衡 <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
          </div>
        </div>
      </div>

      {/* Cmd + K 全局快捷搜索弹窗 */}
      <Modal
        title={null}
        footer={null}
        open={isSearchOpen}
        onCancel={() => setIsSearchOpen(false)}
        width={680}
        closable={false}
        className="top-20"
        styles={{ body: { padding: 0 } }}
      >
        <div className="p-4 border-b border-slate-200 flex items-center gap-3">
          <Search className="w-5 h-5 text-slate-400" />
          <Input
            placeholder="搜索型号、SysML 构件、BOM 物料、仿真工况或因果追溯... (Esc 退出)"
            bordered={false}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            className="text-base p-0 focus:shadow-none"
            autoFocus
          />
          <kbd className="text-xs bg-slate-100 text-slate-500 px-2 py-0.5 rounded border border-slate-300">
            ESC
          </kbd>
        </div>

        <div className="max-h-96 overflow-y-auto p-2">
          <List
            dataSource={searchResults}
            renderItem={(item) => (
              <div
                key={item.id}
                onClick={() => {
                  setIsSearchOpen(false);
                  onNavigate(item.targetTab);
                }}
                className="p-3 hover:bg-blue-50/60 rounded-lg cursor-pointer transition-colors border-b border-slate-100 last:border-none flex items-start justify-between gap-3"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-sm text-slate-800">{item.title}</span>
                    <Tag color={getSecurityColor(item.security)} className="text-[10px] m-0 py-0 leading-normal">
                      {item.security}
                    </Tag>
                  </div>
                  <div className="text-xs font-mono text-blue-600">{item.code}</div>
                  <div className="text-xs text-slate-500">{item.description}</div>
                </div>
                <div className="text-slate-400 hover:text-blue-600 flex items-center gap-1 text-xs whitespace-nowrap pt-1">
                  前往 <ExternalLink className="w-3.5 h-3.5" />
                </div>
              </div>
            )}
          />
        </div>

        <div className="p-3 bg-slate-50 border-t border-slate-200 text-xs text-slate-500 flex justify-between items-center px-4 rounded-b-lg">
          <div className="flex items-center gap-4">
            <span>
              <kbd className="bg-white border px-1 rounded shadow-xs">↑</kbd>{' '}
              <kbd className="bg-white border px-1 rounded shadow-xs">↓</kbd> 导航
            </span>
            <span>
              <kbd className="bg-white border px-1 rounded shadow-xs">Enter</kbd> 选择
            </span>
          </div>
          <span className="flex items-center gap-1">
            <Clock className="w-3 h-3 text-slate-400" />
            基于本地索引与后端异步快速检索
          </span>
        </div>
      </Modal>
    </div>
  );
};
