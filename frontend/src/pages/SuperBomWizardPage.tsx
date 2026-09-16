import React, { useState } from 'react';
import {
  Card,
  Radio,
  Select,
  Button,
  Tag,
  Alert,
  Modal,
  Input,
  Table,
  notification,
} from 'antd';
import {
  Layers,
  AlertTriangle,
  CheckCircle2,
  Lock,
  Sparkles,
  HelpCircle,
  FileCheck,
} from 'lucide-react';
import confetti from 'canvas-confetti';

interface ResolvedItem {
  key: string;
  itemNumber: string;
  itemName: string;
  category: string;
  quantity: number;
  unit: string;
  selectionRule: string;
}

export const SuperBomWizardPage: React.FC = () => {
  // 选配变量输入
  const [spindleType, setSpindleType] = useState<string>('DIRECT_DRIVE');
  const [maxRpm, setMaxRpm] = useState<number>(12000);
  const [toolCapacity, setToolCapacity] = useState<number>(24);
  const [coolingMode, setCoolingMode] = useState<string>('OIL_MIST');
  const [coolantThroughSpindle, setCoolantThroughSpindle] = useState<string>('CTS_70BAR');

  // 手工裁决显式覆盖变量 (Explicit Override)
  const [arbitratedMotor, setArbitratedMotor] = useState<string | null>(null);
  const [arbitrationReason, setArbitrationReason] = useState<string>('');

  // 状态机
  const [isSolving, setIsSolving] = useState<boolean>(false);
  const [conflictError, setConflictError] = useState<string | null>(null);
  const [isAmbiguityModalOpen, setIsAmbiguityModalOpen] = useState<boolean>(false);
  const [isSolved, setIsSolved] = useState<boolean>(false);
  const [resultSha256, setResultSha256] = useState<string>('');
  const [resolvedItems, setResolvedItems] = useState<ResolvedItem[]>([]);

  // 模拟求解核心逻辑（对齐 D05 规则 DSL 与求解器）
  const handleSolve = () => {
    setIsSolving(true);
    setConflictError(null);

    setTimeout(() => {
      setIsSolving(false);

      // 规则 1: 互斥冲突检测（AT-05-02）
      // 规则：CTS_70BAR（70Bar高压中心出水）与 AIR_BLAST（单纯风冷）互斥
      if (coolantThroughSpindle === 'CTS_70BAR' && coolingMode === 'AIR_BLAST') {
        setConflictError(
          '【规则静态冲突 ERROR-RULE-401】高压中心出水系统 (CTS_70BAR) 必须配备油雾润滑冷却 (OIL_MIST) 或切削液喷淋，严禁与纯风冷 (AIR_BLAST) 共存！'
        );
        setIsSolved(false);
        return;
      }

      // 规则 2: 多解歧义硬阻断检测（AT-05-03）
      // 当选择 12000 RPM + 直联驱动，且未经过工程师手工裁决时，存在两个电机备选分支
      if (spindleType === 'DIRECT_DRIVE' && maxRpm === 12000 && !arbitratedMotor) {
        setIsAmbiguityModalOpen(true);
        setIsSolved(false);
        return;
      }

      // 求解成功：生成 100% 确定解
      const motorItemName =
        arbitratedMotor === 'MOTOR_SIEMENS_1PH8'
          ? '西门子 1PH8133-1DF00 高动态同步电主轴电机 (15kW)'
          : arbitratedMotor === 'MOTOR_FANUC_ALPHA'
          ? '发那科 αiI 12/12000 宽频交流主轴电机 (18.5kW)'
          : '标准直联伺服电机 12000RPM';

      const items: ResolvedItem[] = [
        {
          key: '1',
          itemNumber: 'P-850-SPN-101',
          itemName: 'BT40 高刚性五轴专用直联主轴总成',
          category: '机械核心构件',
          quantity: 1,
          unit: '台',
          selectionRule: "SPINDLE_TYPE == 'DIRECT_DRIVE'",
        },
        {
          key: '2',
          itemNumber: arbitratedMotor === 'MOTOR_SIEMENS_1PH8' ? 'P-850-MOT-102A' : 'P-850-MOT-102B',
          itemName: motorItemName,
          category: '电气驱动构件',
          quantity: 1,
          unit: '台',
          selectionRule: "EXPLICIT_ARBITRATION_OVERRIDE",
        },
        {
          key: '3',
          itemNumber: 'P-850-ATC-' + toolCapacity,
          itemName: `${toolCapacity} 把刀伞式/机械手自动换刀刀库 (ATC)`,
          category: '辅助工装',
          quantity: 1,
          unit: '套',
          selectionRule: `TOOL_CAPACITY == ${toolCapacity}`,
        },
        {
          key: '4',
          itemNumber: 'P-850-CTS-70B',
          itemName: '高压中心出水 70Bar 柱塞增压泵与旋转接头',
          category: '冷却液压',
          quantity: 1,
          unit: '套',
          selectionRule: "CTS == 'CTS_70BAR'",
        },
      ];

      const mockHash = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855'.slice(0, 48);
      setResultSha256(mockHash);
      setResolvedItems(items);
      setIsSolved(true);

      // 触发撒花动画
      confetti({
        particleCount: 80,
        spread: 70,
        origin: { y: 0.6 },
      });

      notification.success({
        message: '150% Super BOM 求解成功',
        description: `已通过全量变型规则验证，生成唯一确定性实例，指纹已固化: ${mockHash.slice(0, 16)}...`,
      });
    }, 400);
  };

  // 确认手工裁决
  const handleConfirmArbitration = () => {
    if (!arbitratedMotor) {
      notification.warning({ message: '请选择一项进行裁决' });
      return;
    }
    if (!arbitrationReason.trim()) {
      notification.warning({ message: '必须填写工程裁决审批依据与设计说明' });
      return;
    }
    setIsAmbiguityModalOpen(false);
    handleSolve();
  };

  const columns = [
    {
      title: '物料编号',
      dataIndex: 'itemNumber',
      key: 'itemNumber',
      className: 'font-mono text-xs font-semibold text-blue-600',
    },
    {
      title: '物料名称与规格',
      dataIndex: 'itemName',
      key: 'itemName',
      className: 'font-medium text-slate-800 text-sm',
    },
    {
      title: '大类属性',
      dataIndex: 'category',
      key: 'category',
      render: (text: string) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: '数量/单位',
      key: 'quantity',
      render: (_: unknown, record: ResolvedItem) => (
        <span>
          {record.quantity} {record.unit}
        </span>
      ),
    },
    {
      title: '触发判定规则 (Rule DSL)',
      dataIndex: 'selectionRule',
      key: 'selectionRule',
      className: 'font-mono text-xs text-slate-500',
      render: (text: string) => (
        <span className="bg-slate-100 px-1.5 py-0.5 rounded border border-slate-200">
          {text}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* 头部标题卡片 */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-2 bg-blue-100 text-blue-700 rounded-lg">
              <Layers className="w-5 h-5" />
            </span>
            <h1 className="text-xl font-bold text-slate-900 m-0">
              M14: 150% Super BOM 规则向导与求解器
            </h1>
          </div>
          <p className="text-sm text-slate-500 mt-1 m-0">
            遵循 D05 规则 DSL 规范，支持静态规则互斥排查、零隐式默认值多解硬阻断与 100% 实例基准签名固化。
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Tag color="purple" className="px-3 py-1 font-semibold text-xs m-0">
            DSL 版本: V2.1.0-STRICT
          </Tag>
          <Tag color="green" className="px-3 py-1 font-semibold text-xs m-0">
            求解模式: DETERMINISTIC
          </Tag>
        </div>
      </div>

      {/* 规则冲突告警条 */}
      {conflictError && (
        <Alert
          message="规则求解失败：检测到严重变型规则逻辑冲突"
          description={conflictError}
          type="error"
          showIcon
          icon={<AlertTriangle className="w-5 h-5 text-red-500" />}
          className="border-red-300 bg-red-50"
        />
      )}

      {/* 特性选配表单卡片 */}
      <Card title="1. 五轴机床核心特性选配 (Feature Configuration)" className="border-slate-200 shadow-sm">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-700 flex items-center gap-1">
              主轴驱动方式 (SPINDLE_TYPE)
              <HelpCircle className="w-3.5 h-3.5 text-slate-400" />
            </label>
            <Select
              className="w-full"
              value={spindleType}
              onChange={(val) => {
                setSpindleType(val);
                setArbitratedMotor(null);
              }}
              options={[
                { value: 'DIRECT_DRIVE', label: '直联式高速主轴 (Direct Drive)' },
                { value: 'BUILT_IN_MOTOR', label: '内藏式电主轴 (Motor Spindle)' },
                { value: 'GEAR_DRIVE', label: '大扭矩齿轮变速箱主轴' },
              ]}
            />
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-700 flex items-center gap-1">
              主轴最高转速 (MAX_SPEED_RPM)
            </label>
            <Radio.Group
              value={maxRpm}
              onChange={(e) => {
                setMaxRpm(e.target.value);
                setArbitratedMotor(null);
              }}
              className="w-full"
            >
              <Radio.Button value={8000} className="w-1/3 text-center">8000</Radio.Button>
              <Radio.Button value={12000} className="w-1/3 text-center">12000</Radio.Button>
              <Radio.Button value={18000} className="w-1/3 text-center">18000</Radio.Button>
            </Radio.Group>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-700 flex items-center gap-1">
              刀库容量 (TOOL_CAPACITY)
            </label>
            <Radio.Group
              value={toolCapacity}
              onChange={(e) => setToolCapacity(e.target.value)}
              className="w-full"
            >
              <Radio.Button value={24} className="w-1/3 text-center">24 把</Radio.Button>
              <Radio.Button value={30} className="w-1/3 text-center">30 把</Radio.Button>
              <Radio.Button value={40} className="w-1/3 text-center">40 把</Radio.Button>
            </Radio.Group>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-700 flex items-center gap-1">
              冷却与高压出水 (COOLING)
            </label>
            <div className="flex gap-2">
              <Select
                className="w-1/2"
                value={coolingMode}
                onChange={(val) => setCoolingMode(val)}
                options={[
                  { value: 'OIL_MIST', label: '油气润滑' },
                  { value: 'AIR_BLAST', label: '风冷喷气' },
                ]}
              />
              <Select
                className="w-1/2"
                value={coolantThroughSpindle}
                onChange={(val) => setCoolantThroughSpindle(val)}
                options={[
                  { value: 'CTS_70BAR', label: '70Bar 出水' },
                  { value: 'NONE', label: '无中心出水' },
                ]}
              />
            </div>
          </div>
        </div>

        {arbitratedMotor && (
          <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg flex items-center justify-between">
            <div className="text-xs text-blue-800">
              <span className="font-bold">已生效的显式裁决覆盖项: </span>
              {arbitratedMotor === 'MOTOR_SIEMENS_1PH8' ? '西门子 1PH8 电机' : '发那科 αiI 电机'}
              <span className="text-slate-500 ml-2">（依据：{arbitrationReason}）</span>
            </div>
            <Button
              size="small"
              type="link"
              onClick={() => {
                setArbitratedMotor(null);
                setArbitrationReason('');
              }}
            >
              清除裁决重置
            </Button>
          </div>
        )}

        <div className="mt-6 pt-4 border-t border-slate-100 flex justify-end gap-3">
          <Button
            type="primary"
            size="large"
            loading={isSolving}
            onClick={handleSolve}
            className="bg-blue-600 hover:bg-blue-500 font-semibold flex items-center gap-2"
          >
            <Sparkles className="w-4 h-4" />
            执行规则求解 (Solve 100% Instance)
          </Button>
        </div>
      </Card>

      {/* 求解结果展示卡片 */}
      {isSolved && (
        <Card
          title={
            <div className="flex items-center gap-2 text-emerald-700">
              <CheckCircle2 className="w-5 h-5 text-emerald-600" />
              <span>2. 100% 实例物料基准快照 (Deterministic Resolved BOM)</span>
            </div>
          }
          extra={
            <div className="flex items-center gap-2 font-mono text-xs text-slate-600">
              <Lock className="w-3.5 h-3.5 text-slate-500" />
              <span>SHA-256: {resultSha256}</span>
            </div>
          }
          className="border-emerald-200 shadow-sm"
        >
          <Table
            dataSource={resolvedItems}
            columns={columns}
            pagination={false}
            size="middle"
          />

          <div className="mt-4 flex justify-between items-center bg-slate-50 p-3 rounded-lg border border-slate-200">
            <div className="text-xs text-slate-500 flex items-center gap-1">
              <FileCheck className="w-4 h-4 text-emerald-600" />
              该物料清单已完成零歧义校验，可一键下发至 M25 制造工艺装配树。
            </div>
            <Button type="primary" className="bg-emerald-600 hover:bg-emerald-500 font-medium">
              固化并发布 100% BOM 修订版本 (Rev.01)
            </Button>
          </div>
        </Card>
      )}

      {/* 多解歧义手工裁决模态框 (AT-05-03) */}
      <Modal
        title={
          <div className="flex items-center gap-2 text-amber-600 font-bold">
            <AlertTriangle className="w-5 h-5" />
            <span>检测到多解歧义：触发工程师手工裁决模态框 (AT-05-03)</span>
          </div>
        }
        open={isAmbiguityModalOpen}
        onCancel={() => setIsAmbiguityModalOpen(false)}
        footer={[
          <Button key="cancel" onClick={() => setIsAmbiguityModalOpen(false)}>
            取消放弃
          </Button>,
          <Button
            key="confirm"
            type="primary"
            className="bg-blue-600"
            onClick={handleConfirmArbitration}
          >
            确认裁决并重新求解
          </Button>,
        ]}
        width={700}
      >
        <div className="space-y-4 pt-2">
          <Alert
            type="warning"
            showIcon
            message="禁止零隐式默认多解"
            description="在当前转速 12000 RPM 与直联主轴配置下，系统规则库命中 2 个等价互斥候选电机，系统依据《工业安全规约》禁止静默随机选取，必须由工程师进行显式裁决！"
          />

          <div className="space-y-3">
            <label className="text-sm font-bold text-slate-800">
              请选择本次装配采纳的电机分支：
            </label>
            <Radio.Group
              className="w-full space-y-2"
              value={arbitratedMotor}
              onChange={(e) => setArbitratedMotor(e.target.value)}
            >
              <div
                className={`p-3 border rounded-lg cursor-pointer transition-colors ${
                  arbitratedMotor === 'MOTOR_SIEMENS_1PH8'
                    ? 'border-blue-500 bg-blue-50/50'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <Radio value="MOTOR_SIEMENS_1PH8">
                  <span className="font-bold text-slate-800">
                    分支 A: 西门子 1PH8133 同步电机 (15kW / 105Nm)
                  </span>
                  <p className="text-xs text-slate-500 m-0 pl-6 mt-0.5">
                    优势：低频重切削扭矩大，过载倍数高，适合难加工材料钛合金加工
                  </p>
                </Radio>
              </div>

              <div
                className={`p-3 border rounded-lg cursor-pointer transition-colors ${
                  arbitratedMotor === 'MOTOR_FANUC_ALPHA'
                    ? 'border-blue-500 bg-blue-50/50'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <Radio value="MOTOR_FANUC_ALPHA">
                  <span className="font-bold text-slate-800">
                    分支 B: 发那科 αiI 12/12000 宽频交流电机 (18.5kW / 85Nm)
                  </span>
                  <p className="text-xs text-slate-500 m-0 pl-6 mt-0.5">
                    优势：高速动态响应快，功率因数优良，适合铝合金高频铣削
                  </p>
                </Radio>
              </div>
            </Radio.Group>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-700">
              工程裁决依据与签批意见 (强制必填)：
            </label>
            <Input.TextArea
              rows={3}
              placeholder="请输入本次手工选定该电机的工艺要求、刚度校核编号或订单特殊技术协议..."
              value={arbitrationReason}
              onChange={(e) => setArbitrationReason(e.target.value)}
            />
          </div>
        </div>
      </Modal>
    </div>
  );
};
