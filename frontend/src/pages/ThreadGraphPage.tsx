import React, { useState } from 'react';
import { Card, Tag, Button, Drawer, Badge, Descriptions } from 'antd';
import {
  GitFork,
  Radio,
  FileText,
  Boxes,
  Layers,
  RotateCcw,
  Sparkles,
  Search,
  Database,
  Send,
  CheckCircle2,
  GitCommit,
  ShieldCheck,
  ArrowDown,
} from 'lucide-react';

export type NodeType =
  | 'REQUIREMENT'
  | 'SYSML_BLOCK'
  | 'CAD_MODEL'
  | 'EBOM_REV'
  | 'MBOM_REV'
  | 'BOP_ROUTING'
  | 'HANDOFF_PKG'
  | 'RECEIPT_RECONCILIATION';

export interface ThreadNode {
  id: string;
  urn: string;
  name: string;
  type: NodeType;
  status: 'RELEASED' | 'IN_WORK' | 'LOCKED' | 'CLOSED';
  riskScore?: number;
  isImpacted?: boolean;
  level: number;
  domainDept: string;
  extraDetails?: {
    digestSha256?: string;
    batchNo?: string;
    balanceVerified?: boolean;
    operationCount?: number;
    receiptRatio?: string;
  };
}

const INITIAL_THREAD_NODES: ThreadNode[] = [
  {
    id: 'N1',
    urn: 'urn:ccdd:req:REQ-001',
    name: '【需求指标】机床主轴额定转速≥12000 RPM与动平衡G0.4精度',
    type: 'REQUIREMENT',
    status: 'RELEASED',
    level: 1,
    domainDept: '研发总体部',
  },
  {
    id: 'N2',
    urn: 'urn:ccdd:sysml:SpindleAssembly',
    name: '【SysML架构】直联主轴总成物理逻辑块 (SpindleUnit)',
    type: 'SYSML_BLOCK',
    status: 'RELEASED',
    level: 2,
    domainDept: '系统架构室',
  },
  {
    id: 'N3',
    urn: 'urn:ccdd:ebom:EBOM-VMC850-REV01',
    name: '【设计工程BOM】VMC-850五轴加工中心主轴单元设计BOM (16螺钉/4轴承/2传感器)',
    type: 'EBOM_REV',
    status: 'RELEASED',
    level: 3,
    domainDept: '机械工程设计部',
    extraDetails: {
      digestSha256: 'a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0',
    },
  },
  {
    id: 'N4',
    urn: 'urn:ccdd:mbom:MBOM-VMC850-REV01',
    name: '【制造工程BOM】车间MBOM拆分重组 (物料100%消耗守恒, 残差Residual=0)',
    type: 'MBOM_REV',
    status: 'RELEASED',
    level: 4,
    domainDept: '工艺规划部',
    extraDetails: {
      balanceVerified: true,
    },
  },
  {
    id: 'N5',
    urn: 'urn:ccdd:bop:ROUT-VMC850-SPINDLE-01',
    name: '【BOP工艺路线】主轴精密刮研装配与15000rpm跑车路线 (4工序无环拓扑)',
    type: 'BOP_ROUTING',
    status: 'RELEASED',
    level: 5,
    domainDept: '装配工艺组',
    extraDetails: {
      operationCount: 4,
    },
  },
  {
    id: 'N6',
    urn: 'urn:ccdd:handoff:DISPATCH-20260916-VMC850-01',
    name: '【制造下发批次】MES车间工单发件箱 (含全包SHA-256数字签名与MRR签署)',
    type: 'HANDOFF_PKG',
    status: 'RELEASED',
    level: 6,
    domainDept: '生产计划科 (MES集成)',
    extraDetails: {
      batchNo: 'DISPATCH-20260916-VMC850-01',
      digestSha256: '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08',
    },
  },
  {
    id: 'N7',
    urn: 'urn:ccdd:receipt:RECONCILED-CONFIRMED',
    name: '【MES回执对账闭环】4/4项物料逐项库位核收无误 (RECONCILED_CONFIRMED)',
    type: 'RECEIPT_RECONCILIATION',
    status: 'CLOSED',
    level: 6,
    domainDept: '智能装配车间现场',
    extraDetails: {
      receiptRatio: '4 / 4 100% 收讫',
    },
  },
];

export const ThreadGraphPage: React.FC = () => {
  const [nodes, setNodes] = useState<ThreadNode[]>(INITIAL_THREAD_NODES);
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
      case 'EBOM_REV':
        return <Database className="w-4 h-4 text-indigo-500" />;
      case 'MBOM_REV':
        return <Layers className="w-4 h-4 text-emerald-500" />;
      case 'BOP_ROUTING':
        return <GitCommit className="w-4 h-4 text-cyan-500" />;
      case 'HANDOFF_PKG':
        return <Send className="w-4 h-4 text-orange-500" />;
      case 'RECEIPT_RECONCILIATION':
        return <CheckCircle2 className="w-4 h-4 text-teal-500" />;
    }
  };

  const getNodeTypeTag = (type: ThreadNode['type']) => {
    switch (type) {
      case 'REQUIREMENT':
        return <Tag color="blue">顶层需求</Tag>;
      case 'SYSML_BLOCK':
        return <Tag color="purple">SysML架构</Tag>;
      case 'CAD_MODEL':
        return <Tag color="gold">CAD三维</Tag>;
      case 'EBOM_REV':
        return <Tag color="geekblue">设计 EBOM</Tag>;
      case 'MBOM_REV':
        return <Tag color="green">制造 MBOM (守恒)</Tag>;
      case 'BOP_ROUTING':
        return <Tag color="cyan">BOP工艺路线</Tag>;
      case 'HANDOFF_PKG':
        return <Tag color="volcano">制造下发包 (SHA-256)</Tag>;
      case 'RECEIPT_RECONCILIATION':
        return <Tag color="success">MES对账闭环</Tag>;
    }
  };

  // 触发变更波及度推演 (Impact Analysis)
  const triggerImpactAnalysis = (rootId: string) => {
    setIsImpactMode(true);
    setNodes((prev) =>
      prev.map((n) => {
        if (n.id === rootId) {
          return { ...n, isImpacted: true, riskScore: 98 };
        }
        // 下游波及 N2 -> N3 -> N4 -> N5 -> N6 -> N7
        if (['N3', 'N4', 'N5', 'N6', 'N7'].includes(n.id)) {
          return { ...n, isImpacted: true, riskScore: 85 };
        }
        return { ...n, isImpacted: false, riskScore: 0 };
      })
    );
    setImpactDrawerVisible(true);
  };

  // 重置推演模式
  const resetImpactAnalysis = () => {
    setIsImpactMode(false);
    setNodes(INITIAL_THREAD_NODES);
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
            落实 D07/D08 规格：打通【需求 ➔ 架构 ➔ EBOM ➔ MBOM ➔ BOP ➔ 制造下发批次 ➔ MES对账闭环】的 6 级端到端穿透因果拓扑图。
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
              <span>数字主线因果全景拓扑视口</span>
              {isImpactMode && (
                <Tag color="error" className="font-bold">
                  ● 变更波及高危预警中 (影响下游设计BOM、工艺及下发批次)
                </Tag>
              )}
            </div>
            <div className="flex items-center gap-2 text-xs text-slate-500">
              <span className="flex items-center gap-1">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" /> 100% 来源严密追溯
              </span>
            </div>
          </div>
        }
      >
        <div className="py-6 px-4 flex flex-col items-center gap-4 min-h-[520px] justify-center relative">
          {/* 渲染各层级 */}
          {[1, 2, 3, 4, 5, 6].map((lvl) => {
            const levelNodes = nodes.filter((n) => n.level === lvl);
            return (
              <React.Fragment key={lvl}>
                <div className="flex flex-wrap justify-center gap-4 w-full">
                  {levelNodes.map((node) => (
                    <div
                      key={node.id}
                      onClick={() => setSelectedNode(node)}
                      className={`p-3.5 rounded-xl border-2 bg-white shadow-sm hover:shadow-md transition-all cursor-pointer w-full max-w-xl ${
                        node.isImpacted
                          ? 'border-red-500 ring-4 ring-red-100'
                          : 'border-slate-200 hover:border-blue-500'
                      }`}
                    >
                      <div className="flex items-center justify-between gap-3 mb-1.5">
                        <div className="flex items-center gap-2">
                          {getNodeIcon(node.type)}
                          {getNodeTypeTag(node.type)}
                          <span className="text-xs text-slate-400 font-mono">#{node.id}</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <span className="text-xs text-slate-500">{node.domainDept}</span>
                          <Badge
                            status={node.status === 'CLOSED' || node.status === 'RELEASED' ? 'success' : 'processing'}
                          />
                        </div>
                      </div>
                      <h4 className="text-xs font-bold text-slate-800 m-0 leading-snug">
                        {node.name}
                      </h4>
                      <div className="flex items-center justify-between mt-1.5 text-[11px] text-slate-400 font-mono">
                        <span className="truncate max-w-[320px]">{node.urn}</span>
                        {node.extraDetails?.batchNo && (
                          <span className="text-blue-600 font-semibold">
                            批次: {node.extraDetails.batchNo}
                          </span>
                        )}
                        {node.extraDetails?.receiptRatio && (
                          <span className="text-emerald-600 font-bold">
                            {node.extraDetails.receiptRatio}
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>

                {lvl < 6 && (
                  <div className="flex items-center justify-center my-0.5 text-slate-300">
                    <ArrowDown className="w-4 h-4" />
                  </div>
                )}
              </React.Fragment>
            );
          })}
        </div>
      </Card>

      {/* 节点详细因果属性抽屉 */}
      <Drawer
        title={
          <div className="flex items-center gap-2">
            {selectedNode && getNodeIcon(selectedNode.type)}
            <span className="font-bold text-sm">数字主线节点资产因果属性与元数据</span>
          </div>
        }
        placement="right"
        width={480}
        onClose={() => setSelectedNode(null)}
        open={!!selectedNode}
      >
        {selectedNode && (
          <div className="space-y-4 text-xs">
            <Descriptions column={1} bordered size="small">
              <Descriptions.Item label="节点全局 URN">
                <span className="font-mono text-blue-700 select-all">{selectedNode.urn}</span>
              </Descriptions.Item>
              <Descriptions.Item label="节点类型">
                {getNodeTypeTag(selectedNode.type)}
              </Descriptions.Item>
              <Descriptions.Item label="权威管辖部门">
                <span className="font-semibold text-slate-800">{selectedNode.domainDept}</span>
              </Descriptions.Item>
              <Descriptions.Item label="生命周期状态">
                <Tag color={selectedNode.status === 'CLOSED' ? 'green' : 'blue'}>
                  {selectedNode.status}
                </Tag>
              </Descriptions.Item>
              {selectedNode.extraDetails?.digestSha256 && (
                <Descriptions.Item label="SHA-256 全包数字签名">
                  <span className="font-mono text-[10px] break-all text-slate-600">
                    {selectedNode.extraDetails.digestSha256}
                  </span>
                </Descriptions.Item>
              )}
              {selectedNode.extraDetails?.balanceVerified && (
                <Descriptions.Item label="消耗平衡验证">
                  <Tag color="success">100% 消耗守恒通过 (残差为 0)</Tag>
                </Descriptions.Item>
              )}
              {selectedNode.extraDetails?.receiptRatio && (
                <Descriptions.Item label="MES 逐项异步对账状态">
                  <Tag color="success">{selectedNode.extraDetails.receiptRatio} (全收讫闭环)</Tag>
                </Descriptions.Item>
              )}
            </Descriptions>

            <div className="p-3 bg-slate-50 rounded border border-slate-200 space-y-2">
              <h5 className="font-bold text-slate-800 m-0">数字主线上下游拓扑关系</h5>
              <p className="text-slate-600 m-0">
                本节点已通过 PostgreSQL CTE 递归建立全局权威因果关系网，任何上游变更（如 SysML 架构调整）将沿主线自动标注波及风险。
              </p>
            </div>
          </div>
        )}
      </Drawer>

      {/* 变更波及推演评估抽屉 */}
      <Drawer
        title={
          <div className="flex items-center gap-2 text-red-600 font-bold">
            <Sparkles className="w-4 h-4" />
            <span>SysML架构变更 ➔ 制造车间波及推演评估报告 (D07)</span>
          </div>
        }
        placement="bottom"
        height={320}
        onClose={() => setImpactDrawerVisible(false)}
        open={impactDrawerVisible}
      >
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 text-xs">
          <div className="p-3 bg-red-50 rounded-lg border border-red-200">
            <span className="text-slate-500">变更波及风险等级</span>
            <div className="text-xl font-bold text-red-700 mt-1">CRITICAL (极高风险)</div>
            <p className="text-[11px] text-red-600 mt-1 m-0">
              波及范围横跨设计 EBOM、工艺 MBOM 及已发布的 MES 制造下发批次单。
            </p>
          </div>
          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
            <span className="text-slate-500">受波及下游制品总数</span>
            <div className="text-xl font-bold text-slate-800 mt-1">5 项下游制品</div>
            <p className="text-[11px] text-slate-500 mt-1 m-0">涵盖 BOP 4 工序编排及 MES 4 项领料回执</p>
          </div>
          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200 col-span-2">
            <span className="text-slate-500">工程变更处理建议 (ECO Guidance)</span>
            <p className="text-slate-700 mt-1 m-0 leading-relaxed">
              检测到下游下发批次 <strong className="font-mono text-blue-600">DISPATCH-20260916-VMC850-01</strong> 已在车间执行对账。若变更 SysML 架构，必须发起工业工程变更单 (ECN)，通过双阶段确认对车间工单实施锁定并召回再平衡。
            </p>
          </div>
        </div>
      </Drawer>
    </div>
  );
};
