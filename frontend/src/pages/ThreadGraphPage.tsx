import React, { useState } from 'react';
import { Card, Tag, Button, Drawer, Badge } from 'antd';
import {
  GitFork,
  Radio,
  FileText,
  Boxes,
  Activity,
  Factory,
  AlertTriangle,
  RotateCcw,
  Sparkles,
  Search,
} from 'lucide-react';

interface ThreadNode {
  id: string;
  urn: string;
  name: string;
  type: 'REQUIREMENT' | 'SYSML_BLOCK' | 'CAD_MODEL' | 'SIM_CASE' | 'MBOM_OP';
  status: 'RELEASED' | 'IN_WORK' | 'LOCKED';
  riskScore?: number;
  isImpacted?: boolean;
  level: number;
}

const INITIAL_NODES: ThreadNode[] = [
  {
    id: 'N1',
    urn: 'urn:ccdd:req:REQ-001',
    name: '【需求】机床主轴最大回转速度与动平衡指标 (≥12000 RPM, G1.0)',
    type: 'REQUIREMENT',
    status: 'RELEASED',
    level: 1,
  },
  {
    id: 'N2',
    urn: 'urn:ccdd:sysml:block:spindle-assembly',
    name: '【架构】SysML v2 直联主轴核心功能块 (SpindleUnit)',
    type: 'SYSML_BLOCK',
    status: 'RELEASED',
    level: 2,
  },
  {
    id: 'N3',
    urn: 'urn:ccdd:cad:part:CAD-850-SPN-BOX',
    name: '【三维CAD】主轴箱体结构三维轻量化模型 (STEP/3DTiles)',
    type: 'CAD_MODEL',
    status: 'LOCKED',
    level: 3,
  },
  {
    id: 'N4',
    urn: 'urn:ccdd:sim:SIM-THERMAL-01',
    name: '【仿真验证】主轴高转速瞬态热力学有限元热变形仿真工况',
    type: 'SIM_CASE',
    status: 'RELEASED',
    level: 3,
  },
  {
    id: 'N5',
    urn: 'urn:ccdd:mbom:op:OP10-BORING',
    name: '【制造工艺】车间装配工位 OP10: 主轴箱精密镗孔与基准刮研',
    type: 'MBOM_OP',
    status: 'IN_WORK',
    level: 4,
  },
  {
    id: 'N6',
    urn: 'urn:ccdd:mbom:op:OP30-BALANCING',
    name: '【制造工艺】车间装配工位 OP30: 整机动平衡现场测试与校准',
    type: 'MBOM_OP',
    status: 'IN_WORK',
    level: 4,
  },
];



export const ThreadGraphPage: React.FC = () => {
  const [nodes, setNodes] = useState<ThreadNode[]>(INITIAL_NODES);
  const [selectedNode, setSelectedNode] = useState<ThreadNode | null>(null);
  const [isImpactMode, setIsImpactMode] = useState<boolean>(false);
  const [impactDrawerVisible, setImpactDrawerVisible] = useState<boolean>(false);

  const getNodeIcon = (type: ThreadNode['type']) => {
    switch (type) {
      case 'REQUIREMENT':
        return <FileText className="w-4 h-4 text-blue-500" />;
      case 'SYSML_BLOCK':
        return <Boxes className="w-4 h-4 text-purple-500" />;
      case 'CAD_MODEL':
        return <Radio className="w-4 h-4 text-amber-500" />;
      case 'SIM_CASE':
        return <Activity className="w-4 h-4 text-emerald-500" />;
      case 'MBOM_OP':
        return <Factory className="w-4 h-4 text-cyan-500" />;
    }
  };

  const getNodeTypeLabel = (type: ThreadNode['type']) => {
    switch (type) {
      case 'REQUIREMENT':
        return '顶层需求';
      case 'SYSML_BLOCK':
        return 'SysML 构件';
      case 'CAD_MODEL':
        return 'CAD 制品';
      case 'SIM_CASE':
        return '仿真工况';
      case 'MBOM_OP':
        return '制造工艺';
    }
  };

  // 触发变更波及度推演 (Impact Analysis)
  const triggerImpactAnalysis = (rootId: string) => {
    setIsImpactMode(true);
    setNodes((prev) =>
      prev.map((n) => {
        if (n.id === rootId) {
          return { ...n, isImpacted: true, riskScore: 95 };
        }
        // 下游波及 N2 -> N3, N4, N5, N6
        if (['N3', 'N4', 'N5', 'N6'].includes(n.id)) {
          return { ...n, isImpacted: true, riskScore: 78 };
        }
        return { ...n, isImpacted: false, riskScore: 0 };
      })
    );
    setImpactDrawerVisible(true);
  };

  // 重置推演模式
  const resetImpactAnalysis = () => {
    setIsImpactMode(false);
    setNodes(INITIAL_NODES);
    setImpactDrawerVisible(false);
  };

  return (
    <div className="space-y-6">
      {/* 头部导航卡片 */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-2 bg-purple-100 text-purple-700 rounded-lg">
              <GitFork className="w-5 h-5" />
            </span>
            <h1 className="text-xl font-bold text-slate-900 m-0">
              M23: 数字主线全链路因果关系溯源拓扑爆炸图
            </h1>
          </div>
          <p className="text-sm text-slate-500 mt-1 m-0">
            基于 PostgreSQL 递归图遍历（D07 规格），打通需求、架构、三维模型、仿真计算与车间制造工位的全域因果网络。
          </p>
        </div>

        <div className="flex items-center gap-3">
          {isImpactMode ? (
            <Button
              icon={<RotateCcw className="w-4 h-4" />}
              onClick={resetImpactAnalysis}
              className="font-medium"
            >
              退出波及分析
            </Button>
          ) : (
            <Button
              type="primary"
              danger
              icon={<Sparkles className="w-4 h-4" />}
              onClick={() => triggerImpactAnalysis('N2')}
              className="font-medium"
            >
              一键变更风险推演 (Impact Analysis)
            </Button>
          )}
        </div>
      </div>

      {/* 拓扑画布工作区 */}
      <Card
        className={`border-slate-200 shadow-sm transition-all ${
          isImpactMode ? 'ring-2 ring-red-500/20 bg-red-50/10' : 'bg-slate-50/50'
        }`}
        title={
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-sm font-semibold">
              <Search className="w-4 h-4 text-slate-400" />
              <span>拓扑图交互视口 (支持 LOD 视口缩放与下钻)</span>
              {isImpactMode && (
                <Tag color="error" className="animate-pulse-slow font-bold">
                  ● 变更波及高危预警中
                </Tag>
              )}
            </div>
            <div className="flex items-center gap-3 text-xs text-slate-500">
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-blue-500 inline-block"></span> 需求
              </span>
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-purple-500 inline-block"></span> 架构
              </span>
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-amber-500 inline-block"></span> CAD
              </span>
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block"></span> 仿真
              </span>
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-cyan-500 inline-block"></span> MBOM
              </span>
            </div>
          </div>
        }
      >
        <div className="py-8 px-4 flex flex-col items-center gap-8 min-h-[460px] justify-center relative">
          {/* 层级 1: 需求层 */}
          <div className="flex justify-center w-full">
            {nodes
              .filter((n) => n.level === 1)
              .map((node) => (
                <div
                  key={node.id}
                  onClick={() => setSelectedNode(node)}
                  className={`p-4 rounded-xl border-2 bg-white shadow-sm hover:shadow-md transition-all cursor-pointer max-w-md ${
                    node.isImpacted
                      ? 'border-red-500 ring-4 ring-red-100'
                      : 'border-blue-400 hover:border-blue-600'
                  }`}
                >
                  <div className="flex items-center justify-between gap-3 mb-1">
                    <div className="flex items-center gap-2">
                      {getNodeIcon(node.type)}
                      <Tag color="blue">{getNodeTypeLabel(node.type)}</Tag>
                    </div>
                    <Badge status="success" text="已基线化" />
                  </div>
                  <h4 className="text-sm font-bold text-slate-800 m-0">{node.name}</h4>
                  <p className="text-[11px] font-mono text-slate-400 mt-1 m-0">{node.urn}</p>
                </div>
              ))}
          </div>

          <div className="w-0.5 h-6 bg-slate-300"></div>

          {/* 层级 2: SysML 构件层 */}
          <div className="flex justify-center w-full">
            {nodes
              .filter((n) => n.level === 2)
              .map((node) => (
                <div
                  key={node.id}
                  onClick={() => setSelectedNode(node)}
                  className={`p-4 rounded-xl border-2 bg-white shadow-sm hover:shadow-md transition-all cursor-pointer max-w-md ${
                    node.isImpacted
                      ? 'border-red-500 ring-4 ring-red-200 bg-red-50/30'
                      : 'border-purple-400 hover:border-purple-600'
                  }`}
                >
                  <div className="flex items-center justify-between gap-3 mb-1">
                    <div className="flex items-center gap-2">
                      {getNodeIcon(node.type)}
                      <Tag color="purple">{getNodeTypeLabel(node.type)}</Tag>
                      {node.isImpacted && <Tag color="error">变更发起源</Tag>}
                    </div>
                    <Badge status="processing" text="已发布" />
                  </div>
                  <h4 className="text-sm font-bold text-slate-800 m-0">{node.name}</h4>
                  <p className="text-[11px] font-mono text-slate-400 mt-1 m-0">{node.urn}</p>
                </div>
              ))}
          </div>

          <div className="w-0.5 h-6 bg-slate-300"></div>

          {/* 层级 3: CAD 与 仿真联合验证层 */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-3xl w-full">
            {nodes
              .filter((n) => n.level === 3)
              .map((node) => (
                <div
                  key={node.id}
                  onClick={() => setSelectedNode(node)}
                  className={`p-4 rounded-xl border-2 bg-white shadow-sm hover:shadow-md transition-all cursor-pointer ${
                    node.isImpacted
                      ? 'border-red-400 ring-2 ring-red-200 bg-red-50/20'
                      : node.type === 'CAD_MODEL'
                      ? 'border-amber-400 hover:border-amber-600'
                      : 'border-emerald-400 hover:border-emerald-600'
                  }`}
                >
                  <div className="flex items-center justify-between gap-2 mb-1">
                    <div className="flex items-center gap-2">
                      {getNodeIcon(node.type)}
                      <Tag color={node.type === 'CAD_MODEL' ? 'gold' : 'green'}>
                        {getNodeTypeLabel(node.type)}
                      </Tag>
                    </div>
                    {node.isImpacted ? (
                      <span className="text-xs font-bold text-red-600">波及风险 78分</span>
                    ) : (
                      <Badge status="default" text="就绪" />
                    )}
                  </div>
                  <h4 className="text-sm font-bold text-slate-800 m-0">{node.name}</h4>
                  <p className="text-[11px] font-mono text-slate-400 mt-1 m-0">{node.urn}</p>
                </div>
              ))}
          </div>

          <div className="w-0.5 h-6 bg-slate-300"></div>

          {/* 层级 4: 制造工位层 */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-3xl w-full">
            {nodes
              .filter((n) => n.level === 4)
              .map((node) => (
                <div
                  key={node.id}
                  onClick={() => setSelectedNode(node)}
                  className={`p-4 rounded-xl border-2 bg-white shadow-sm hover:shadow-md transition-all cursor-pointer ${
                    node.isImpacted
                      ? 'border-red-400 ring-2 ring-red-200 bg-red-50/20'
                      : 'border-cyan-400 hover:border-cyan-600'
                  }`}
                >
                  <div className="flex items-center justify-between gap-2 mb-1">
                    <div className="flex items-center gap-2">
                      {getNodeIcon(node.type)}
                      <Tag color="cyan">{getNodeTypeLabel(node.type)}</Tag>
                    </div>
                    {node.isImpacted ? (
                      <span className="text-xs font-bold text-red-600">待工艺变更评审</span>
                    ) : (
                      <Badge status="warning" text="编制中" />
                    )}
                  </div>
                  <h4 className="text-sm font-bold text-slate-800 m-0">{node.name}</h4>
                  <p className="text-[11px] font-mono text-slate-400 mt-1 m-0">{node.urn}</p>
                </div>
              ))}
          </div>
        </div>

        {/* 选中节点因果元数据快速查看栏 */}
        {selectedNode && (
          <div className="mt-4 p-4 bg-white rounded-lg border border-slate-200 flex flex-col md:flex-row justify-between items-start md:items-center gap-3">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-slate-500 uppercase">当前选中节点:</span>
                <Tag color="blue">{getNodeTypeLabel(selectedNode.type)}</Tag>
                <span className="font-bold text-slate-800 text-sm">{selectedNode.name}</span>
              </div>
              <p className="font-mono text-xs text-slate-400 mt-1 m-0">URN: {selectedNode.urn}</p>
            </div>
            <div className="flex items-center gap-2">
              <Button
                size="small"
                type="primary"
                className="bg-blue-600"
                onClick={() => triggerImpactAnalysis(selectedNode.id)}
              >
                以此节点为根推演波及
              </Button>
              <Button size="small" onClick={() => setSelectedNode(null)}>
                取消选择
              </Button>
            </div>
          </div>
        )}
      </Card>

      {/* 变更波及影响度推演抽屉 */}
      <Drawer
        title={
          <div className="flex items-center gap-2 text-red-600 font-bold">
            <AlertTriangle className="w-5 h-5" />
            <span>变更波及影响推演报告 (Impact Analysis Report)</span>
          </div>
        }
        placement="right"
        width={480}
        open={impactDrawerVisible}
        onClose={() => setImpactDrawerVisible(false)}
      >
        <div className="space-y-4">
          <div className="p-4 bg-red-50 rounded-xl border border-red-200">
            <div className="text-xs font-bold text-red-700 uppercase">变更发起源 (Root Cause)</div>
            <div className="font-bold text-slate-900 mt-1">SysML v2 直联主轴核心功能块</div>
            <div className="text-xs text-slate-600 mt-1">
              拟变更参数：额定扭矩由 105Nm 上调至 135Nm，最高转速保持 12000 RPM
            </div>
          </div>

          <div className="border-t border-slate-200 pt-3">
            <h4 className="font-bold text-slate-800 text-sm mb-2">受波及构件量化清单 (4 项)</h4>
            <div className="space-y-3">
              <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
                <div className="flex justify-between items-center">
                  <Tag color="gold">CAD 模型</Tag>
                  <span className="text-xs font-bold text-red-600">高风险 (85分)</span>
                </div>
                <div className="font-semibold text-xs text-slate-800 mt-1">主轴箱体结构三维轻量化模型</div>
                <div className="text-[11px] text-slate-500 mt-1">
                  影响原因：电机法兰接口与安装螺栓预紧力需重新校核
                </div>
              </div>

              <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
                <div className="flex justify-between items-center">
                  <Tag color="green">仿真工况</Tag>
                  <span className="text-xs font-bold text-red-600">必须重新仿真 (90分)</span>
                </div>
                <div className="font-semibold text-xs text-slate-800 mt-1">主轴瞬态热力学有限元热变形仿真</div>
                <div className="text-[11px] text-slate-500 mt-1">
                  影响原因：电机发热功率从 1.2kW 上升至 1.6kW，热位移需重新验证
                </div>
              </div>

              <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
                <div className="flex justify-between items-center">
                  <Tag color="cyan">制造工艺</Tag>
                  <span className="text-xs font-bold text-amber-600">中风险 (65分)</span>
                </div>
                <div className="font-semibold text-xs text-slate-800 mt-1">车间装配 OP10/OP30 工艺规程</div>
                <div className="text-[11px] text-slate-500 mt-1">
                  影响原因：装配扭矩要求变动，需修订 BOP 工艺卡片
                </div>
              </div>
            </div>
          </div>

          <div className="pt-4 border-t border-slate-200">
            <Button
              type="primary"
              danger
              block
              size="large"
              className="font-medium"
              onClick={() => {
                setImpactDrawerVisible(false);
              }}
            >
              发起工程变更通知单 (ECN-2026-0902)
            </Button>
          </div>
        </div>
      </Drawer>
    </div>
  );
};
