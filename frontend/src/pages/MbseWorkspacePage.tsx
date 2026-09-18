import React, { useEffect, useState } from 'react';
import {
  Card, Row, Col, Space, Button, Tag, Tabs, Table, Tree,
  Input, Modal, message, Badge, Tooltip, Alert, Divider, Typography
} from 'antd';
import {
  Cpu, Lock, Unlock, AlertTriangle,
  RefreshCw, Play, Send, FileCode, Layers, GitCommit,
  Crosshair, ShieldCheck, Box, Zap, Database, Server
} from 'lucide-react';
import { useMbseWorkspaceStore } from '../stores/useMbseWorkspaceStore';
import { useAuthStore } from '../stores/useAuthStore';

const { Text, Title, Paragraph } = Typography;
const { TextArea } = Input;

export const MbseWorkspacePage: React.FC = () => {
  const { user } = useAuthStore();
  const currentUserId = user?.username || 'ENG-MECH-1042';

  const {
    workspace,
    modelText,
    currentChecksum,
    latestValidation,
    workingBindings,
    candidateSnapshots,
    selectedElementId,
    highlightedElementId,
    activeBottomTab,
    isLockedByMe,
    initWorkspace,
    acquireSessionLock,
    releaseSessionLock,
    switchChannel,
    bindRequirement,
    validateModel,
    captureCandidateSnapshot,
    updateModelText,
    injectSyntaxError,
    tamperParameter,
    resetToHealthy,
    setSelectedElement,
    setHighlightedElement,
    setActiveBottomTab,
  } = useMbseWorkspaceStore();

  const [loading, setLoading] = useState(false);
  const [releaseModalVisible, setReleaseModalVisible] = useState(false);
  const [snapshotDesc, setSnapshotDesc] = useState('VMC1000 X轴进给系统架构与需求完全匹配，准备移交阶段门发布');
  const [localCode, setLocalCode] = useState(modelText);

  useEffect(() => {
    initWorkspace(currentUserId);
  }, []);

  useEffect(() => {
    setLocalCode(modelText);
  }, [modelText]);

  const hasLock = isLockedByMe(currentUserId);

  // 申请编辑锁
  const handleAcquireLock = async () => {
    setLoading(true);
    try {
      const res = await acquireSessionLock(currentUserId);
      if (res.success) {
        message.success(res.message);
      } else {
        Modal.warning({
          title: '排他锁控制冲突 (CST-M04-01)',
          content: res.message,
        });
      }
    } finally {
      setLoading(false);
    }
  };

  // 释放编辑锁
  const handleReleaseLock = async () => {
    setLoading(true);
    try {
      await releaseSessionLock(currentUserId);
      message.success('排他编辑锁已主动释放，当前恢复为只读浏览模式');
    } finally {
      setLoading(false);
    }
  };

  // 切换主编辑通道
  const handleSwitchChannel = async (channel: 'GRAPHICAL' | 'TEXTUAL') => {
    const res = await switchChannel(channel, currentUserId);
    if (res.success) {
      message.success(res.message);
    } else {
      Modal.error({
        title: '通道切换安全拦截 (CST-M04-01)',
        content: res.message,
      });
    }
  };

  // 执行 OpenSysML 语义诊断
  const handleValidate = async () => {
    setLoading(true);
    try {
      const outcome = await validateModel();
      if (outcome.status === 'PASSED') {
        message.success('OpenSysML 语义诊断完全通过！0 错误、0 警告');
      } else if (outcome.status === 'PASSED_WITH_WARNING') {
        message.warning(`OpenSysML 诊断通过，存在 ${outcome.warningCount} 条非阻塞性工程警告`);
      } else {
        message.error(`OpenSysML 语义诊断失败！检测到 ${outcome.errorCount} 个语法或未解析引用错误`);
      }
    } finally {
      setLoading(false);
    }
  };

  // 提交发布快照
  const handleCaptureSnapshot = async () => {
    setLoading(true);
    try {
      const res = await captureCandidateSnapshot(currentChecksum, snapshotDesc, currentUserId);
      if (res.success) {
        Modal.success({
          title: '候选快照固化并交接成功 (M04 -> M06)',
          content: (
            <div>
              <p>{res.message}</p>
              <p><strong>快照 SHA-256：</strong><code>{currentChecksum}</code></p>
              <p><strong>发布作业编号：</strong><code>{res.snapshot?.releaseOperationId}</code></p>
              <p style={{ color: '#10b981', fontSize: 12 }}>已进入 M06 模型受控发布协调器两阶段暂存流水线，草稿模型不污染正式基线。</p>
            </div>
          ),
        });
        setReleaseModalVisible(false);
      } else {
        Modal.error({
          title: '发布门禁安全拦截 (PUB-03 / PUB-04)',
          content: res.message,
        });
      }
    } finally {
      setLoading(false);
    }
  };

  // 构件定位
  const handleLocateElement = (elementId: string) => {
    setSelectedElement(elementId);
    setHighlightedElement(elementId);
    message.info(`视口已自动高亮聚焦至构件：${elementId}`);
    setTimeout(() => {
      setHighlightedElement(null);
    }, 2500);
  };

  // 树结构定义 (VMC1000 7大骨架包)
  const treeData = [
    {
      title: '01_Requirements (需求规范包)',
      key: 'pkg_req',
      children: [
        { title: 'XAxisStrokeReq (X轴有效行程 >= 1000mm)', key: 'elem_req_stroke' },
        { title: 'SpindleSpeedReq (主轴最高转速 >= 18000rpm)', key: 'elem_req_spindle' },
        ...workingBindings.map(b => ({
          title: `[投影] ${b.plmObjectCode} (${b.qualifiedName.split('::').pop()})`,
          key: b.sysonElementId,
        })),
      ],
    },
    {
      title: '02_FunctionalBehavior (功能行为包)',
      key: 'pkg_behavior',
      children: [
        { title: 'HighSpeedMillingAction (高速铣削主动作)', key: 'elem_act_milling' },
      ],
    },
    {
      title: '03_LogicalArchitecture (逻辑架构包)',
      key: 'pkg_logical',
      children: [
        { title: 'CncMotionController (CNC运动控制器)', key: 'elem_part_cnc' },
        { title: 'busPort (PROFINET 工业以太网总线端口)', key: 'elem_port_bus' },
      ],
    },
    {
      title: '04_PhysicalArchitecture (物理架构与装配)',
      key: 'pkg_physical',
      children: [
        { title: 'VMC1000Structure (五轴机床整机物理结构)', key: 'elem_part_vmc' },
        { title: 'xFeedSystem (X轴高精度滚珠丝杠进给系统)', key: 'elem_part_xfeed' },
        { title: 'spindleModule (18000rpm 电主轴单元)', key: 'elem_part_spindle' },
        { title: 'ctrlPort (伺服驱动控制接口端子)', key: 'elem_port_servo' },
      ],
    },
    {
      title: '05_Interfaces (接口与契约包)',
      key: 'pkg_interfaces',
      children: [
        { title: 'ServoInterfacePort (数字伺服接口)', key: 'elem_intf_servo' },
        { title: 'IndustrialEthernetPort (实时总线契约)', key: 'elem_intf_bus' },
      ],
    },
    {
      title: '06_Parameters (M07 受控工程参数映射)',
      key: 'pkg_params',
      children: [
        { title: 'MachineMassLimit = 8500.0 kg', key: 'elem_param_mass' },
      ],
    },
    {
      title: '07_VerificationContext (验证工况与闭环分析)',
      key: 'pkg_verify',
      children: [
        { title: 'DynamicStiffnessAnalysis (进给动态刚度工况)', key: 'elem_ctx_stiff' },
      ],
    },
  ];

  // 受控需求库列表 (M03)
  const controlledRequirements = [
    { id: 3001, code: 'REQ-X-001', name: 'X轴工作行程不小于 1000mm', threshold: '>= 1000 mm', status: 'RELEASED' },
    { id: 3002, code: 'REQ-SPINDLE-002', name: '高速电主轴最高转速不低于 18000rpm', threshold: '>= 18000 rpm', status: 'RELEASED' },
    { id: 3003, code: 'REQ-FEED-003', name: 'X/Y/Z三轴快移速度不小于 48m/min', threshold: '>= 48 m/min', status: 'IN_REVIEW' },
  ];

  // 哈希比对状态
  const isHashMatching = latestValidation ? currentChecksum === latestValidation.sourceChecksum : false;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10, minHeight: 'calc(100vh - 110px)' }}>
      {/* 顶部：项目上下文、四态标识、通道与锁控栏 */}
      <Card bodyStyle={{ padding: '10px 16px' }} style={{ borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Space size="middle" align="center">
              <span style={{ fontSize: 17, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 8 }}>
                <Cpu className="w-5 h-5 text-blue-600" />
                {workspace.modelProjectName}
              </span>
              <Tag color="cyan">Rev A (PLM 草稿修订版本)</Tag>
              <Tooltip title="核心设计原则：工作模型(Working) != 已发布模型(Published) != PLM Revision != 工程基线(Baseline)">
                <Tag color="purple" style={{ cursor: 'pointer' }}>
                  <ShieldCheck className="w-3.5 h-3.5 inline mr-1" />
                  四态彻底解耦保护
                </Tag>
              </Tooltip>
              <Tag color={workspace.primaryChannel === 'GRAPHICAL' ? 'blue' : 'geekblue'}>
                主写通道: {workspace.primaryChannel === 'GRAPHICAL' ? '图形通道 (SysON)' : '形式化文本通道 (OpenSysML)'}
              </Tag>
            </Space>
          </Col>

          <Col>
            <Space size="small">
              {/* 通道切换按钮 */}
              <Button.Group size="small">
                <Button
                  type={workspace.primaryChannel === 'GRAPHICAL' ? 'primary' : 'default'}
                  onClick={() => handleSwitchChannel('GRAPHICAL')}
                  disabled={hasLock}
                >
                  SysON 图形通道
                </Button>
                <Button
                  type={workspace.primaryChannel === 'TEXTUAL' ? 'primary' : 'default'}
                  onClick={() => handleSwitchChannel('TEXTUAL')}
                  disabled={hasLock}
                >
                  OpenSysML 文本通道
                </Button>
              </Button.Group>

              {/* 编辑锁状态与申请 */}
              {hasLock ? (
                <Button
                  size="small"
                  type="primary"
                  danger
                  icon={<Unlock className="w-3.5 h-3.5" />}
                  onClick={handleReleaseLock}
                  loading={loading}
                >
                  释放排他编辑锁 ({workspace.boundUserId})
                </Button>
              ) : (
                <Button
                  size="small"
                  type="primary"
                  icon={<Lock className="w-3.5 h-3.5" />}
                  onClick={handleAcquireLock}
                  loading={loading}
                >
                  申请排他编辑锁
                </Button>
              )}

              {/* 快捷操作 */}
              <Button
                size="small"
                type="primary"
                icon={<Play className="w-3.5 h-3.5" />}
                onClick={handleValidate}
                loading={loading}
              >
                执行模型诊断
              </Button>

              <Button
                size="small"
                style={{ backgroundColor: '#10b981', color: '#fff', borderColor: '#10b981' }}
                icon={<Send className="w-3.5 h-3.5" />}
                onClick={() => setReleaseModalVisible(true)}
              >
                捕获快照并交接发布
              </Button>

              <Button size="small" icon={<RefreshCw className="w-3.5 h-3.5" />} onClick={() => initWorkspace(currentUserId)}>
                刷新
              </Button>
            </Space>
          </Col>
        </Row>

        {/* 快捷沙箱测试触发条 */}
        <div style={{ marginTop: 8, paddingTop: 8, borderTop: '1px dashed #f0f0f0', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Space size="small">
            <span style={{ fontSize: 12, color: '#64748b' }}>🧪 规格验收快捷注入测试：</span>
            <Button size="small" onClick={injectSyntaxError}>
              注入未解析引用 (测 AT-02 / PUB-03 阻断)
            </Button>
            <Button size="small" onClick={tamperParameter}>
              微调进给轴阻尼参数 (测 PUB-04 哈希防篡改)
            </Button>
            <Button size="small" onClick={resetToHealthy}>
              一键恢复标准规范模型
            </Button>
          </Space>

          <Space size="small">
            <span style={{ fontSize: 11, color: '#64748b' }}>实时全模型快照 SHA-256：</span>
            <code style={{ fontSize: 11, background: '#f8fafc', padding: '2px 6px', borderRadius: 4, color: isHashMatching ? '#10b981' : '#f59e0b' }}>
              {currentChecksum ? `${currentChecksum.substring(0, 24)}...` : '计算中...'}
            </code>
            {latestValidation && (
              <Badge
                status={isHashMatching ? 'success' : 'warning'}
                text={isHashMatching ? '哈希与上次校验完全一致 (零漂移)' : '⚠️ 模型在校验后发生改动 (哈希漂移)'}
              />
            )}
          </Space>
        </div>
      </Card>

      {/* 主体三栏布局 */}
      <Row gutter={10} style={{ flex: 1 }}>
        {/* 左侧栏：工程导航树与受控需求投影库 */}
        <Col span={6} style={{ display: 'flex', flexDirection: 'column' }}>
          <Card
            bodyStyle={{ padding: 12, flex: 1, overflowY: 'auto' }}
            style={{ borderRadius: 8, height: 480, display: 'flex', flexDirection: 'column' }}
          >
            <Tabs
              defaultActiveKey="tree"
              size="small"
              items={[
                {
                  key: 'tree',
                  label: (
                    <span>
                      <Layers className="w-3.5 h-3.5 inline mr-1" />
                      模型架构树 (VMC1000)
                    </span>
                  ),
                  children: (
                    <div style={{ marginTop: 4 }}>
                      <Tree
                        defaultExpandAll
                        treeData={treeData}
                        onSelect={(keys) => {
                          if (keys[0]) handleLocateElement(String(keys[0]));
                        }}
                      />
                    </div>
                  ),
                },
                {
                  key: 'reqs',
                  label: (
                    <span>
                      <Database className="w-3.5 h-3.5 inline mr-1" />
                      受控需求库 (M03)
                    </span>
                  ),
                  children: (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                      <Alert
                        message="点击需求一键向 SysON 工作区投影生成 RequirementUsage 临时工作绑定"
                        type="info"
                        showIcon
                        style={{ fontSize: 11, padding: '4px 8px' }}
                      />
                      {controlledRequirements.map(req => {
                        const isBound = workingBindings.some(b => b.plmObjectCode === req.code);
                        return (
                          <div
                            key={req.id}
                            style={{
                              padding: 8,
                              border: '1px solid #e2e8f0',
                              borderRadius: 6,
                              background: isBound ? '#f0fdf4' : '#fff',
                            }}
                          >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <Text strong style={{ fontSize: 12 }}>{req.code}</Text>
                              <Tag color={req.status === 'RELEASED' ? 'green' : 'orange'}>{req.status}</Tag>
                            </div>
                            <div style={{ fontSize: 11, color: '#64748b', margin: '4px 0' }}>{req.name}</div>
                            <div style={{ fontSize: 11, color: '#0284c7' }}>指标阈值: {req.threshold}</div>
                            <div style={{ marginTop: 6, display: 'flex', justifyContent: 'flex-end' }}>
                              {isBound ? (
                                <Tag color="success">已投影至工作区</Tag>
                              ) : (
                                <Button
                                  size="small"
                                  type="link"
                                  icon={<Zap className="w-3.5 h-3.5" />}
                                  onClick={() => bindRequirement(req)}
                                  disabled={!hasLock}
                                >
                                  投影关联到模型
                                </Button>
                              )}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  ),
                },
              ]}
            />
          </Card>
        </Col>

        {/* 中间视口：SysON 图形视口 / OpenSysML 文本视口 */}
        <Col span={12} style={{ display: 'flex', flexDirection: 'column' }}>
          <Card
            bodyStyle={{ padding: 0, height: 480, position: 'relative', overflow: 'hidden' }}
            style={{ borderRadius: 8, height: 480 }}
          >
            {/* 非持锁模式下的半透明只读保护蒙层 */}
            {!hasLock && (
              <div
                style={{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  right: 0,
                  bottom: 0,
                  backgroundColor: 'rgba(248, 250, 252, 0.55)',
                  backdropFilter: 'blur(1px)',
                  zIndex: 10,
                  display: 'flex',
                  alignItems: 'flex-start',
                  justifyContent: 'center',
                  paddingTop: 16,
                  pointerEvents: 'none',
                }}
              >
                <div style={{ background: '#fff', border: '1px solid #cbd5e1', borderRadius: 6, padding: '6px 14px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }}>
                  <Text type="secondary" style={{ fontSize: 12, display: 'flex', alignItems: 'center', gap: 6 }}>
                    <Lock className="w-4 h-4 text-amber-500" />
                    当前处于<strong>只读浏览保护模式</strong>。若需修改模型，请在顶栏点击<strong>【申请排他编辑锁】</strong>。
                  </Text>
                </div>
              </div>
            )}

            {workspace.primaryChannel === 'GRAPHICAL' ? (
              /* SysON 图形建模视口集成画布 */
              <div style={{ width: '100%', height: '100%', background: '#f8fafc', padding: 16, display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                  <Space>
                    <Badge status="processing" text="SysON Web 图形编辑器内核 (v2026.09)" />
                    <span style={{ fontSize: 11, color: '#64748b' }}>视角: 五轴机床物理装配与进给伺服接口拓扑</span>
                  </Space>
                  <Tag color="geekblue">BPMN/SysML 图元已就绪</Tag>
                </div>

                {/* 仿真 SysON 画布容器 */}
                <div
                  style={{
                    flex: 1,
                    border: '1px solid #cbd5e1',
                    borderRadius: 6,
                    background: '#ffffff',
                    position: 'relative',
                    overflow: 'hidden',
                    backgroundImage: 'radial-gradient(#e2e8f0 1px, transparent 1px)',
                    backgroundSize: '16px 16px',
                    padding: 16,
                  }}
                >
                  {/* 整机结构容器包 */}
                  <div
                    style={{
                      border: '2px dashed #94a3b8',
                      borderRadius: 8,
                      padding: 12,
                      background: 'rgba(241, 245, 249, 0.4)',
                      height: '100%',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 12,
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Text strong style={{ fontSize: 13, color: '#334155' }}>
                        «block» VMC1000Structure (五轴立式加工中心本体)
                      </Text>
                      <Tag color="blue">Root Part</Tag>
                    </div>

                    <Row gutter={12} style={{ flex: 1 }}>
                      {/* X 轴进给系统 */}
                      <Col span={12}>
                        <div
                          onClick={() => handleLocateElement('elem_part_xfeed')}
                          style={{
                            border: selectedElementId === 'elem_part_xfeed' ? '2px solid #2563eb' : '1px solid #cbd5e1',
                            borderRadius: 6,
                            padding: 10,
                            background: '#fff',
                            cursor: 'pointer',
                            boxShadow: highlightedElementId === 'elem_part_xfeed' ? '0 0 12px #3b82f6' : '0 1px 2px rgba(0,0,0,0.05)',
                            transition: 'all 0.3s',
                          }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Text strong style={{ fontSize: 12, color: '#1e40af' }}>«part» XAxisFeedSystem</Text>
                            <Tag color="green">已满足需求</Tag>
                          </div>
                          <div style={{ fontSize: 11, color: '#475569', marginTop: 6 }}>
                            <div>• stroke = 1020.0 mm</div>
                            <div>• maxVelocity = 48.0 m/min</div>
                            <div>• dampingRatio = 0.05</div>
                          </div>
                          <div style={{ marginTop: 8, padding: '4px 6px', background: '#f1f5f9', borderRadius: 4, fontSize: 10, color: '#0369a1' }}>
                            🔌 Port: ctrlPort (ServoInterfacePort)
                          </div>
                        </div>
                      </Col>

                      {/* 高速主轴单元 */}
                      <Col span={12}>
                        <div
                          onClick={() => handleLocateElement('elem_part_spindle')}
                          style={{
                            border: selectedElementId === 'elem_part_spindle' ? '2px solid #2563eb' : '1px solid #cbd5e1',
                            borderRadius: 6,
                            padding: 10,
                            background: '#fff',
                            cursor: 'pointer',
                            boxShadow: highlightedElementId === 'elem_part_spindle' ? '0 0 12px #3b82f6' : '0 1px 2px rgba(0,0,0,0.05)',
                            transition: 'all 0.3s',
                          }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Text strong style={{ fontSize: 12, color: '#1e40af' }}>«part» HighSpeedSpindle</Text>
                            <Tag color="orange">未绑定量纲</Tag>
                          </div>
                          <div style={{ fontSize: 11, color: '#475569', marginTop: 6 }}>
                            <div>• maxSpeed = 18000.0 rpm</div>
                            <div>• ratedTorque = 120.0</div>
                          </div>
                          <div style={{ marginTop: 8, padding: '4px 6px', background: '#fef3c7', borderRadius: 4, fontSize: 10, color: '#b45309' }}>
                            ⚠️ 诊断提示: ratedTorque 需显式绑定 N.m
                          </div>
                        </div>
                      </Col>
                    </Row>

                    {/* 连线说明与满足关系 */}
                    <div style={{ padding: 8, background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: 4, fontSize: 11, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Space>
                        <Tag color="purple">satisfies</Tag>
                        <span>XAxisFeedSystem 满足需求 <strong>REQ-X-001 (行程 &gt;= 1000mm)</strong></span>
                      </Space>
                      <Space>
                        <Tag color="cyan">connects</Tag>
                        <span>CncMotionController.busPort &lt;--&gt; IndustrialEthernet</span>
                      </Space>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              /* OpenSysML 形式化文本编辑器 */
              <div style={{ width: '100%', height: '100%', background: '#1e293b', color: '#f8fafc', padding: 12, display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                  <Space>
                    <FileCode className="w-4 h-4 text-emerald-400" />
                    <span style={{ fontSize: 12, color: '#cbd5e1' }}>SysML v2 规范代码编辑器 (OpenSysML Text Engine)</span>
                  </Space>
                  <Space>
                    <Button
                      size="small"
                      type="primary"
                      onClick={() => updateModelText(localCode)}
                      disabled={!hasLock}
                    >
                      保存文本变更 (重新计算哈希)
                    </Button>
                  </Space>
                </div>

                <TextArea
                  value={localCode}
                  onChange={(e) => setLocalCode(e.target.value)}
                  readOnly={!hasLock}
                  style={{
                    flex: 1,
                    fontFamily: 'SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace',
                    fontSize: 12,
                    background: '#0f172a',
                    color: '#e2e8f0',
                    border: '1px solid #334155',
                    borderRadius: 4,
                    resize: 'none',
                    lineHeight: 1.5,
                  }}
                />
              </div>
            )}
          </Card>
        </Col>

        {/* 右侧栏：语义属性与绑定详情 */}
        <Col span={6} style={{ display: 'flex', flexDirection: 'column' }}>
          <Card
            title={
              <Space>
                <Box className="w-4 h-4 text-indigo-600" />
                <span style={{ fontSize: 13 }}>构件语义属性与数字主线</span>
              </Space>
            }
            bodyStyle={{ padding: 12, flex: 1, overflowY: 'auto' }}
            style={{ borderRadius: 8, height: 480 }}
          >
            {selectedElementId === 'elem_part_xfeed' ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                <div>
                  <Tag color="blue">«part def» PartUsage</Tag>
                  <Title level={5} style={{ margin: '6px 0 2px 0' }}>XAxisFeedSystem</Title>
                  <Text type="secondary" style={{ fontSize: 11 }}>限定名: VMC1000::PhysicalArchitecture::XAxisFeedSystem</Text>
                </div>

                <Divider style={{ margin: '6px 0' }} />

                <div>
                  <Text strong style={{ fontSize: 12, color: '#0369a1' }}>📌 关联 PLM 权威主数据 (M03/M05)</Text>
                  <div style={{ fontSize: 11, color: '#475569', marginTop: 4 }}>
                    <div>• 关联需求编号: <code>REQ-X-001 (Rev A)</code></div>
                    <div>• 需求名称: X轴有效行程不小于 1000mm</div>
                    <div>• 满足判定: <Tag color="green">SATISFIED (1020mm &gt;= 1000mm)</Tag></div>
                    <div>• 临时工作绑定 ID: <code>BND-881920192</code></div>
                  </div>
                </div>

                <Divider style={{ margin: '6px 0' }} />

                <div>
                  <Text strong style={{ fontSize: 12 }}>受控工程参数集 (M07 映射)</Text>
                  <div style={{ fontSize: 11, color: '#475569', marginTop: 4 }}>
                    <div>• stroke: <code>1020.0 mm</code></div>
                    <div>• maxVelocity: <code>48.0 m/min</code></div>
                    <div>• dampingRatio: <code>0.05</code> (无量纲阻尼比)</div>
                  </div>
                </div>

                <Divider style={{ margin: '6px 0' }} />

                <div>
                  <Text strong style={{ fontSize: 12 }}>接口契约端点 (M05)</Text>
                  <div style={{ fontSize: 11, color: '#475569', marginTop: 4 }}>
                    <div>• 端口: <code>ctrlPort</code> (ServoInterfacePort)</div>
                    <div>• 协议标准: EXT-SERVO-DRIVE-V1.0</div>
                  </div>
                </div>
              </div>
            ) : selectedElementId === 'elem_part_spindle' ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                <div>
                  <Tag color="blue">«part def» PartUsage</Tag>
                  <Title level={5} style={{ margin: '6px 0 2px 0' }}>HighSpeedSpindle</Title>
                  <Text type="secondary" style={{ fontSize: 11 }}>限定名: VMC1000::PhysicalArchitecture::HighSpeedSpindle</Text>
                </div>

                <Divider style={{ margin: '6px 0' }} />

                <div>
                  <Text strong style={{ fontSize: 12, color: '#d97706' }}>⚠️ 语义诊断提示 (OpenSysML)</Text>
                  <p style={{ fontSize: 11, color: '#b45309', margin: '4px 0' }}>
                    <code>ratedTorque</code> 属性缺少显式量纲单位绑定。建议在 M07 参数注册表绑定 <code>N.m</code>。
                  </p>
                </div>
              </div>
            ) : (
              <div style={{ padding: 20, textAlign: 'center', color: '#94a3b8' }}>
                <Paragraph style={{ fontSize: 12 }}>点击左侧树或画布元素查看详细语义属性</Paragraph>
              </div>
            )}
          </Card>
        </Col>
      </Row>

      {/* 底部：工程状态面板 (Tabs 包含 OpenSysML 语义诊断、候选快照与发布交接、工作期临时绑定、架构原则) */}
      <Card bodyStyle={{ padding: '8px 14px' }} style={{ borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
        <Tabs
          activeKey={activeBottomTab}
          onChange={setActiveBottomTab}
          size="small"
          items={[
            {
              key: 'diagnostics',
              label: (
                <span>
                  <AlertTriangle className="w-3.5 h-3.5 inline mr-1 text-amber-500" />
                  OpenSysML 语义诊断
                  {latestValidation && (
                    <Badge
                      count={latestValidation.errorCount + latestValidation.warningCount}
                      style={{
                        marginLeft: 6,
                        backgroundColor: latestValidation.errorCount > 0 ? '#ef4444' : '#f59e0b',
                      }}
                    />
                  )}
                </span>
              ),
              children: (
                <div>
                  {latestValidation ? (
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                        <Space>
                          <Tag color={latestValidation.status === 'PASSED' ? 'green' : latestValidation.status === 'PASSED_WITH_WARNING' ? 'orange' : 'red'}>
                            诊断状态: {latestValidation.status}
                          </Tag>
                          <span style={{ fontSize: 12, color: '#64748b' }}>
                            错误数: <strong style={{ color: '#ef4444' }}>{latestValidation.errorCount}</strong> | 警告数: <strong style={{ color: '#f59e0b' }}>{latestValidation.warningCount}</strong>
                          </span>
                          <span style={{ fontSize: 11, color: '#94a3b8' }}>
                            引擎: OpenSysML-Validator-v2026.09-LSP | 诊断时间: {new Date(latestValidation.completedAt).toLocaleTimeString()}
                          </span>
                        </Space>
                        <Button size="small" icon={<Play className="w-3 h-3" />} onClick={handleValidate}>
                          重新诊断
                        </Button>
                      </div>

                      <Table
                        size="small"
                        rowKey={(r) => r.errorCode + r.elementId}
                        pagination={false}
                        dataSource={latestValidation.diagnostics}
                        columns={[
                          {
                            title: '级别',
                            dataIndex: 'severity',
                            width: 90,
                            render: (sev) => (
                              <Tag color={sev === 'ERROR' ? 'red' : sev === 'WARNING' ? 'orange' : 'blue'}>
                                {sev}
                              </Tag>
                            ),
                          },
                          {
                            title: '错误代码',
                            dataIndex: 'errorCode',
                            width: 170,
                            render: (code) => <code>{code}</code>,
                          },
                          {
                            title: '诊断违规信息与建议',
                            dataIndex: 'message',
                            render: (msg, r) => (
                              <div>
                                <div><Text strong>{msg}</Text></div>
                                <div style={{ fontSize: 11, color: '#059669' }}>💡 修复建议: {r.recommendation}</div>
                              </div>
                            ),
                          },
                          {
                            title: '模型构件限定名',
                            dataIndex: 'qualifiedName',
                            width: 280,
                            render: (name) => <code style={{ fontSize: 11 }}>{name}</code>,
                          },
                          {
                            title: '源码位置',
                            dataIndex: 'sourceLocation',
                            width: 150,
                            render: (loc) => <span style={{ fontSize: 11, color: '#64748b' }}>{loc}</span>,
                          },
                          {
                            title: '操作',
                            width: 100,
                            render: (_, r) => (
                              <Button
                                size="small"
                                type="link"
                                icon={<Crosshair className="w-3.5 h-3.5" />}
                                onClick={() => handleLocateElement(r.elementId)}
                              >
                                定位构件
                              </Button>
                            ),
                          },
                        ]}
                      />
                    </div>
                  ) : (
                    <div style={{ padding: 16, textAlign: 'center' }}>
                      <Button type="primary" icon={<Play className="w-3.5 h-3.5" />} onClick={handleValidate}>
                        首次执行 OpenSysML 语义诊断
                      </Button>
                    </div>
                  )}
                </div>
              ),
            },
            {
              key: 'snapshots',
              label: (
                <span>
                  <GitCommit className="w-3.5 h-3.5 inline mr-1 text-emerald-600" />
                  一致性候选快照与发布交接 (M04 $\rightarrow$ M06)
                </span>
              ),
              children: (
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                    <Space size="middle">
                      <Tag color="cyan">PUB-04 守卫检查：{isHashMatching ? '通过 (一致)' : '阻断 (篡改漂移)'}</Tag>
                      <span style={{ fontSize: 12, color: '#64748b' }}>
                        当前模型哈希: <code>{currentChecksum ? currentChecksum.substring(0, 16) + '...' : '-'}</code>
                      </span>
                      <span style={{ fontSize: 12, color: '#64748b' }}>
                        诊断固化哈希: <code>{latestValidation ? latestValidation.sourceChecksum.substring(0, 16) + '...' : '-'}</code>
                      </span>
                    </Space>
                    <Button
                      size="small"
                      type="primary"
                      style={{ backgroundColor: '#10b981', borderColor: '#10b981' }}
                      onClick={() => setReleaseModalVisible(true)}
                    >
                      捕获新快照并提交发布
                    </Button>
                  </div>

                  <Table
                    size="small"
                    rowKey="snapshotId"
                    pagination={false}
                    dataSource={candidateSnapshots}
                    columns={[
                      {
                        title: '快照防伪令牌 (Token)',
                        dataIndex: 'snapshotToken',
                        render: (t) => <code>{t}</code>,
                      },
                      {
                        title: '固化 SHA-256 摘要',
                        dataIndex: 'sourceChecksum',
                        render: (hash) => <code style={{ fontSize: 11 }}>{hash}</code>,
                      },
                      {
                        title: '状态',
                        dataIndex: 'status',
                        render: (status) => (
                          <Tag color={status === 'HANDED_OVER' ? 'green' : 'blue'}>
                            {status === 'HANDED_OVER' ? '已移交 M06 流水线' : status}
                          </Tag>
                        ),
                      },
                      {
                        title: 'M06 发布作业编号',
                        dataIndex: 'releaseOperationId',
                        render: (op) => <code>{op}</code>,
                      },
                      {
                        title: '捕获人',
                        dataIndex: 'capturedBy',
                      },
                      {
                        title: '生成时间戳',
                        dataIndex: 'capturedAt',
                        render: (t) => new Date(t).toLocaleString(),
                      },
                    ]}
                  />
                </div>
              ),
            },
            {
              key: 'bindings',
              label: (
                <span>
                  <Database className="w-3.5 h-3.5 inline mr-1 text-blue-600" />
                  工作期临时需求绑定 (WorkingElementBinding)
                </span>
              ),
              children: (
                <div>
                  <Alert
                    message="落实两阶段绑定原则：工程师在 SysON 中频繁增删元素期间，PLM 仅维系草稿临时映射；发布时才固化不可变数字主线。"
                    type="info"
                    style={{ fontSize: 11, marginBottom: 8, padding: '4px 10px' }}
                  />
                  <Table
                    size="small"
                    rowKey="bindingId"
                    pagination={false}
                    dataSource={workingBindings}
                    columns={[
                      { title: 'PLM 需求编号', dataIndex: 'plmObjectCode', render: (c) => <strong>{c}</strong> },
                      { title: 'SysML 元素类型', dataIndex: 'elementType', render: (t) => <Tag color="blue">{t}</Tag> },
                      { title: '模型限定名 (QualifiedName)', dataIndex: 'qualifiedName', render: (q) => <code>{q}</code> },
                      { title: 'SysON 元素 UUID', dataIndex: 'sysonElementId', render: (id) => <code style={{ fontSize: 11 }}>{id}</code> },
                      { title: '同步时间', dataIndex: 'lastSyncedAt', render: (t) => new Date(t).toLocaleTimeString() },
                    ]}
                  />
                </div>
              ),
            },
            {
              key: 'principles',
              label: (
                <span>
                  <ShieldCheck className="w-3.5 h-3.5 inline mr-1 text-purple-600" />
                  系统架构与安全规约 (ADR-01/02 & CST)
                </span>
              ),
              children: (
                <div style={{ fontSize: 12, color: '#475569', lineHeight: 1.8 }}>
                  <Row gutter={16}>
                    <Col span={6}>
                      <Card size="small" title="四态彻底解耦原则" style={{ borderRadius: 6 }}>
                        <Text strong>Working ≠ Published ≠ Revision ≠ Baseline</Text>
                        <p style={{ margin: '4px 0 0 0', fontSize: 11 }}>SysON 负责图形编辑，OpenSysML 负责语义诊断，Flexo 负责模型仓库，CCDDesigner 负责工程治理，草稿变更对正式主线零污染。</p>
                      </Card>
                    </Col>
                    <Col span={6}>
                      <Card size="small" title="CST-M04-01 单主通道与锁控" style={{ borderRadius: 6 }}>
                        <Text strong>排他编辑会话锁</Text>
                        <p style={{ margin: '4px 0 0 0', fontSize: 11 }}>GRAPHICAL 与 TEXTUAL 通道严格互斥，通道切换前必须主动释放排他锁，防止无锁并发覆盖。</p>
                      </Card>
                    </Col>
                    <Col span={6}>
                      <Card size="small" title="PUB-04 变更防篡改守卫" style={{ borderRadius: 6 }}>
                        <Text strong>校验摘要强匹配</Text>
                        <p style={{ margin: '4px 0 0 0', fontSize: 11 }}>快照提交时实时比对哈希；若校验后模型发生任何改动，一票否决发布申请并阻断交接。</p>
                      </Card>
                    </Col>
                    <Col span={6}>
                      <Card size="small" title="MBSE Gateway SPI 隔离" style={{ borderRadius: 6 }}>
                        <Text strong>标准 SPI 多工具适配</Text>
                        <p style={{ margin: '4px 0 0 0', fontSize: 11 }}>通过 ModelAuthoringAdapter 与 ModelValidationAdapter 屏蔽 SysON/OpenSysML 私有 API。</p>
                      </Card>
                    </Col>
                  </Row>
                </div>
              ),
            },
            {
              key: 'deployment',
              label: (
                <span>
                  <Server className="w-3.5 h-3.5 inline mr-1 text-blue-500" />
                  各组件部署规范与容器拓扑
                </span>
              ),
              children: (
                <div style={{ fontSize: 12, color: '#475569' }}>
                  <Row gutter={16}>
                    <Col span={8}>
                      <Card
                        size="small"
                        title={
                          <Space>
                            <Tag color="blue">SysON</Tag>
                            <span>建模创作环境</span>
                          </Space>
                        }
                        extra={<Tag color="green">Docker 容器化</Tag>}
                        style={{ borderRadius: 6 }}
                      >
                        <div>• <strong>部署形态</strong>: 容器化 Spring Boot + 静态前端资源</div>
                        <div style={{ marginTop: 4 }}>• <strong>存储依赖</strong>: 独立 Postgres 实例 (<code>syson_db :5434</code>)</div>
                        <div style={{ marginTop: 4 }}>• <strong>扩缩容策略</strong>: 按项目/租户垂直隔离，必要时分片</div>
                        <div style={{ marginTop: 4 }}>• <strong>对外端口</strong>: Web/API <code>8085</code> | DB <code>5434</code></div>
                        <div style={{ marginTop: 6, padding: '4px 8px', background: '#eff6ff', borderRadius: 4, fontSize: 11, color: '#1d4ed8' }}>
                          容器服务: <code>syson-server</code>, <code>syson-postgres</code>
                        </div>
                      </Card>
                    </Col>

                    <Col span={8}>
                      <Card
                        size="small"
                        title={
                          <Space>
                            <Tag color="purple">OpenSysML</Tag>
                            <span>语义诊断计算服务</span>
                          </Space>
                        }
                        extra={<Tag color="cyan">无状态水平扩展</Tag>}
                        style={{ borderRadius: 6 }}
                      >
                        <div>• <strong>部署形态</strong>: <code>sysml-grpc</code> 容器，纯无状态</div>
                        <div style={{ marginTop: 4 }}>• <strong>存储依赖</strong>: 无 (纯校验计算型服务)</div>
                        <div style={{ marginTop: 4 }}>• <strong>扩缩容策略</strong>: 按请求量水平扩展 (<code>--scale opensysml=N</code>)</div>
                        <div style={{ marginTop: 4 }}>• <strong>对外端口</strong>: gRPC <code>50051</code> | HTTP <code>8086</code></div>
                        <div style={{ marginTop: 6, padding: '4px 8px', background: '#faf5ff', borderRadius: 4, fontSize: 11, color: '#7e22ce' }}>
                          负载分发: Nginx 网关轮询 + 多容器弹性伸缩
                        </div>
                      </Card>
                    </Col>

                    <Col span={8}>
                      <Card
                        size="small"
                        title={
                          <Space>
                            <Tag color="orange">Flexo MMS</Tag>
                            <span>模型仓库与联邦服务</span>
                          </Space>
                        }
                        extra={<Tag color="volcano">官方部署栈</Tag>}
                        style={{ borderRadius: 6 }}
                      >
                        <div>• <strong>部署形态</strong>: Docker Compose 官方部署栈</div>
                        <div style={{ marginTop: 4 }}>• <strong>存储依赖</strong>: Apache Jena Fuseki RDF 四元组存储</div>
                        <div style={{ marginTop: 4 }}>• <strong>扩缩容策略</strong>: 按 Org/Repo 分片，读写分离</div>
                        <div style={{ marginTop: 4 }}>• <strong>对外端口</strong>: Layer 1 <code>8088</code> | SPARQL <code>3030</code></div>
                        <div style={{ marginTop: 6, padding: '4px 8px', background: '#fff7ed', borderRadius: 4, fontSize: 11, color: '#c2410c' }}>
                          四元组具名图: <code>urn:ccdd:staging</code> / <code>production</code>
                        </div>
                      </Card>
                    </Col>
                  </Row>

                  <div style={{ marginTop: 12, padding: '8px 12px', background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: 6, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Space size="middle">
                      <Tag color="geekblue">统一反向代理网关</Tag>
                      <span>MBSE Gateway (Nginx) 监听端口: <code>8084</code></span>
                      <span style={{ color: '#94a3b8' }}>| 路由: <code>/syson/</code>, <code>/opensysml/</code>, <code>/flexo/</code>, <code>/sparql/</code></span>
                    </Space>
                    <Tag color="blue">启停命令: ./deploy/scripts/deploy-local.sh up --scale 2</Tag>
                  </div>
                </div>
              ),
            },
          ]}
        />
      </Card>

      {/* 提交发布快照模态框 */}
      <Modal
        title={
          <Space>
            <Send className="w-4 h-4 text-emerald-600" />
            <span>捕获一致性候选快照并向 M06 发起发布交接</span>
          </Space>
        }
        open={releaseModalVisible}
        onCancel={() => setReleaseModalVisible(false)}
        onOk={handleCaptureSnapshot}
        confirmLoading={loading}
        okText="确认固化快照并交接"
        cancelText="取消"
        width={580}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <Alert
            message="发布前准入守卫核查 (PUB-03 / PUB-04)"
            description={
              <div>
                <div>• OpenSysML 语义诊断状态：<Tag color={latestValidation?.status === 'PASSED' ? 'green' : latestValidation?.status === 'PASSED_WITH_WARNING' ? 'orange' : 'red'}>{latestValidation?.status || '未执行'}</Tag></div>
                <div>• 哈希一致性守卫：<Tag color={isHashMatching ? 'green' : 'red'}>{isHashMatching ? 'PASSED (零漂移)' : 'FAILED (发生修改)'}</Tag></div>
              </div>
            }
            type={latestValidation?.status === 'PASSED' && isHashMatching ? 'success' : 'warning'}
            showIcon
          />

          <div>
            <Text strong style={{ fontSize: 12 }}>当前候选快照 SHA-256 摘要：</Text>
            <div style={{ background: '#f8fafc', padding: 8, borderRadius: 4, marginTop: 4 }}>
              <code style={{ fontSize: 11, wordBreak: 'break-all' }}>{currentChecksum}</code>
            </div>
          </div>

          <div>
            <Text strong style={{ fontSize: 12 }}>快照版本说明：</Text>
            <Input
              style={{ marginTop: 4 }}
              value={snapshotDesc}
              onChange={(e) => setSnapshotDesc(e.target.value)}
              placeholder="请输入快照说明"
            />
          </div>
        </div>
      </Modal>
    </div>
  );
};
