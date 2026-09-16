import React, { useState } from 'react';
import { Card, Tag, Button, Progress, Table, notification, Modal } from 'antd';
import {
  ShieldCheck,
  CheckCircle2,
  Send,
  AlertCircle,
} from 'lucide-react';
import confetti from 'canvas-confetti';

interface EbomItem {
  id: string;
  itemCode: string;
  itemName: string;
  totalQty: number;
  consumedQty: number;
  unit: string;
}

interface MbomStationItem {
  id: string;
  stationCode: string;
  itemCode: string;
  itemName: string;
  allocatedQty: number;
  unit: string;
  sourceType: 'DESIGN_EBOM' | 'MANUFACTURING_ADDED';
}

const INITIAL_EBOM_ITEMS: EbomItem[] = [
  {
    id: 'E1',
    itemCode: 'MAT-SCR-M12-50',
    itemName: '高强度主轴法兰安装螺栓 M12x50 (12.9级)',
    totalQty: 16,
    consumedQty: 16,
    unit: '件',
  },
  {
    id: 'E2',
    itemCode: 'MAT-BRG-7014C',
    itemName: '精密主轴角接触球轴承 7014C/P4',
    totalQty: 4,
    consumedQty: 4,
    unit: '件',
  },
  {
    id: 'E3',
    itemCode: 'MAT-SEN-VIB-01',
    itemName: '压电式主轴三向振动加速度传感器',
    totalQty: 2,
    consumedQty: 1, // 故意留下 1 个欠消耗，以演示残差守恒校验
    unit: '支',
  },
];

const INITIAL_MBOM_ITEMS: MbomStationItem[] = [
  {
    id: 'M1',
    stationCode: 'OP10 (主轴粗装)',
    itemCode: 'MAT-SCR-M12-50',
    itemName: '高强度主轴法兰安装螺栓 M12x50',
    allocatedQty: 8,
    unit: '件',
    sourceType: 'DESIGN_EBOM',
  },
  {
    id: 'M2',
    stationCode: 'OP20 (主轴总成精调)',
    itemCode: 'MAT-SCR-M12-50',
    itemName: '高强度主轴法兰安装螺栓 M12x50',
    allocatedQty: 8,
    unit: '件',
    sourceType: 'DESIGN_EBOM',
  },
  {
    id: 'M3',
    stationCode: 'OP20 (主轴总成精调)',
    itemCode: 'MAT-BRG-7014C',
    itemName: '精密主轴角接触球轴承 7014C/P4',
    allocatedQty: 4,
    unit: '件',
    sourceType: 'DESIGN_EBOM',
  },
  {
    id: 'M4',
    stationCode: 'OP30 (电气布线)',
    itemCode: 'MAT-SEN-VIB-01',
    itemName: '压电式主轴三向振动加速度传感器',
    allocatedQty: 1,
    unit: '支',
    sourceType: 'DESIGN_EBOM',
  },
  {
    id: 'M5',
    stationCode: 'OP20 (主轴总成精调)',
    itemCode: 'MAT-GLUE-243',
    itemName: '乐泰 243 中强度螺纹锁固胶 (工艺辅料)',
    allocatedQty: 1,
    unit: '瓶',
    sourceType: 'MANUFACTURING_ADDED', // 严禁伪造设计来源
  },
];

export const MbomBalancePage: React.FC = () => {
  const [ebomList, setEbomList] = useState<EbomItem[]>(INITIAL_EBOM_ITEMS);
  const [mbomList, setMbomList] = useState<MbomStationItem[]>(INITIAL_MBOM_ITEMS);
  const [isDispatchModalOpen, setIsDispatchModalOpen] = useState(false);

  // 计算物料守恒平衡指标
  const totalRequired = ebomList.reduce((acc, cur) => acc + cur.totalQty, 0);
  const totalConsumed = ebomList.reduce((acc, cur) => acc + cur.consumedQty, 0);
  const balanceResidual = totalRequired - totalConsumed;
  const balancePercent = Math.round((totalConsumed / totalRequired) * 100);
  const isFullyBalanced = balanceResidual === 0;

  // 修复物料消耗差额（一键平衡操作）
  const handleAutoBalance = () => {
    setEbomList((prev) =>
      prev.map((item) => ({ ...item, consumedQty: item.totalQty }))
    );
    setMbomList((prev) => [
      ...prev,
      {
        id: 'M6',
        stationCode: 'OP30 (电气布线)',
        itemCode: 'MAT-SEN-VIB-01',
        itemName: '压电式主轴三向振动加速度传感器',
        allocatedQty: 1,
        unit: '支',
        sourceType: 'DESIGN_EBOM',
      },
    ]);

    confetti({
      particleCount: 60,
      spread: 60,
      origin: { y: 0.5 },
    });

    notification.success({
      message: '物料 100% 消耗守恒平衡校验通过',
      description: '所有 EBOM 设计物料均已在 MBOM 工艺树中完成可解释映射，残差为 0。',
    });
  };

  const ebomColumns = [
    {
      title: '设计物料编码',
      dataIndex: 'itemCode',
      key: 'itemCode',
      className: 'font-mono text-xs font-semibold text-blue-600',
    },
    {
      title: '物料规格名称',
      dataIndex: 'itemName',
      key: 'itemName',
      className: 'font-medium text-slate-800 text-xs',
    },
    {
      title: '设计总量',
      dataIndex: 'totalQty',
      key: 'totalQty',
      render: (val: number, row: EbomItem) => `${val} ${row.unit}`,
    },
    {
      title: 'MBOM消耗量',
      dataIndex: 'consumedQty',
      key: 'consumedQty',
      render: (val: number, row: EbomItem) => {
        const isDiff = val !== row.totalQty;
        return (
          <span className={`font-bold ${isDiff ? 'text-amber-600' : 'text-emerald-600'}`}>
            {val} {row.unit} {isDiff && '(欠消耗)'}
          </span>
        );
      },
    },
    {
      title: '消耗平衡率',
      key: 'rate',
      render: (_: unknown, row: EbomItem) => {
        const pct = Math.round((row.consumedQty / row.totalQty) * 100);
        return (
          <Progress
            percent={pct}
            size="small"
            status={pct === 100 ? 'success' : 'exception'}
            className="m-0"
          />
        );
      },
    },
  ];

  const mbomColumns = [
    {
      title: '装配工位 (BOP)',
      dataIndex: 'stationCode',
      key: 'stationCode',
      className: 'font-semibold text-xs text-slate-700',
    },
    {
      title: '工位分配物料',
      dataIndex: 'itemName',
      key: 'itemName',
      className: 'text-xs text-slate-800',
    },
    {
      title: '工位用量',
      key: 'qty',
      render: (_: unknown, row: MbomStationItem) => `${row.allocatedQty} ${row.unit}`,
    },
    {
      title: '物料来源类型',
      dataIndex: 'sourceType',
      key: 'sourceType',
      render: (val: MbomStationItem['sourceType']) =>
        val === 'DESIGN_EBOM' ? (
          <Tag color="blue">设计源头 (EBOM)</Tag>
        ) : (
          <Tag color="orange">车间辅料 (MANUFACTURING)</Tag>
        ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* 头部标题卡片 */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-2 bg-emerald-100 text-emerald-700 rounded-lg">
              <ShieldCheck className="w-5 h-5" />
            </span>
            <h1 className="text-xl font-bold text-slate-900 m-0">
              M25: 制造工程 EBOM/MBOM 拆分重组与平衡残差看板
            </h1>
          </div>
          <p className="text-sm text-slate-500 mt-1 m-0">
            遵循 D08 规格，严守 100% 物料消耗守恒与来源严禁伪造设计规范，保障车间下发逐项对账无遗漏。
          </p>
        </div>

        <div className="flex items-center gap-3">
          {!isFullyBalanced && (
            <Button
              type="default"
              onClick={handleAutoBalance}
              className="text-amber-600 border-amber-300 bg-amber-50 hover:bg-amber-100 font-medium"
            >
              模拟分配剩余物料
            </Button>
          )}
          <Button
            type="primary"
            disabled={!isFullyBalanced}
            className={`font-semibold flex items-center gap-1 ${
              isFullyBalanced ? 'bg-emerald-600 hover:bg-emerald-500' : ''
            }`}
            onClick={() => setIsDispatchModalOpen(true)}
          >
            <Send className="w-4 h-4" /> 签署制造就绪 (MRR) 并下发 MES
          </Button>
        </div>
      </div>

      {/* 顶部物料守恒平衡率仪表看板 */}
      <Card className="border-slate-200 shadow-sm">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-center">
          <div>
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
              物料守恒平衡状态
            </span>
            <div className="flex items-center gap-3 mt-1">
              <h2 className="text-3xl font-extrabold m-0 text-slate-800">
                {balancePercent}%
              </h2>
              {isFullyBalanced ? (
                <Tag color="success" className="px-2.5 py-1 text-xs font-bold flex items-center gap-1">
                  <CheckCircle2 className="w-3.5 h-3.5" /> 100% 严格守恒
                </Tag>
              ) : (
                <Tag color="warning" className="px-2.5 py-1 text-xs font-bold flex items-center gap-1">
                  <AlertCircle className="w-3.5 h-3.5" /> 欠消耗残差: {balanceResidual} 件
                </Tag>
              )}
            </div>
            <p className="text-xs text-slate-500 mt-2 m-0">
              总需求物料 {totalRequired} 件，当前 MBOM 已规划消耗 {totalConsumed} 件
            </p>
          </div>

          <div className="col-span-2">
            <Progress
              percent={balancePercent}
              strokeColor={isFullyBalanced ? '#10b981' : '#f59e0b'}
              strokeWidth={14}
              status={isFullyBalanced ? 'success' : 'active'}
            />
            {!isFullyBalanced && (
              <p className="text-xs text-amber-600 font-medium mt-2 m-0 flex items-center gap-1">
                <AlertCircle className="w-3.5 h-3.5" />
                【系统安全硬阻断】物料未完全达到 100% 消耗平衡前，禁止签署 MRR 下发车间！
              </p>
            )}
          </div>
        </div>
      </Card>

      {/* 左右分栏双树对比与拆分重组卡片 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 左栏：设计 EBOM 清单 */}
        <Card
          title={
            <div className="flex items-center justify-between">
              <span className="text-sm font-bold text-blue-800">
                1. 设计工程 BOM (EBOM 原型)
              </span>
              <Tag color="blue">数据权威: 研发设计中心</Tag>
            </div>
          }
          className="border-slate-200 shadow-sm"
        >
          <Table
            dataSource={ebomList}
            columns={ebomColumns}
            rowKey="id"
            pagination={false}
            size="small"
          />
        </Card>

        {/* 右栏：制造工艺 MBOM 工位树 */}
        <Card
          title={
            <div className="flex items-center justify-between">
              <span className="text-sm font-bold text-emerald-800">
                2. 车间装配工艺 BOM (MBOM)
              </span>
              <Tag color="green">数据权威: 智能装配车间</Tag>
            </div>
          }
          className="border-slate-200 shadow-sm"
        >
          <Table
            dataSource={mbomList}
            columns={mbomColumns}
            rowKey="id"
            pagination={false}
            size="small"
          />
        </Card>
      </div>

      {/* 下发成功与逐项回执模拟模态框 */}
      <Modal
        title={
          <div className="flex items-center gap-2 text-emerald-600 font-bold">
            <CheckCircle2 className="w-5 h-5" />
            <span>制造下发批次已生成并启动逐项回执对账 (M26)</span>
          </div>
        }
        open={isDispatchModalOpen}
        onCancel={() => setIsDispatchModalOpen(false)}
        footer={[
          <Button key="ok" type="primary" className="bg-emerald-600" onClick={() => setIsDispatchModalOpen(false)}>
            确认并进入对账流水台
          </Button>,
        ]}
      >
        <div className="space-y-3 py-2 text-sm text-slate-700">
          <p>
            <span className="font-bold">下发批次流水号: </span>
            <span className="font-mono text-blue-600">DISPATCH-20260916-VMC850-OP01</span>
          </p>
          <p>
            <span className="font-bold">目标接收系统: </span>
            <span className="bg-slate-100 px-2 py-0.5 rounded text-xs">MES 装配生产执行系统</span>
          </p>
          <p>
            <span className="font-bold">回执对账模式: </span>
            <span>严格遵守双阶段确认（HTTP 200 不代表消费成功，需逐行回执状态确认）</span>
          </p>
          <div className="p-3 bg-emerald-50 rounded border border-emerald-200 text-xs text-emerald-800">
            已向发件箱 (Outbox) 写入 5 项制造领料事务，等待 MES WebSocket 异步回执对账。
          </div>
        </div>
      </Modal>
    </div>
  );
};
