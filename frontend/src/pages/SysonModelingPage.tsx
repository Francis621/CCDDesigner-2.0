import React, { useState, useEffect, useRef } from 'react';
import {
  Card, Row, Col, Space, Button, Tag, Input, Modal, message,
  Tooltip, Drawer, Typography, Badge, Form, Select, Divider,
  Segmented, Alert, Spin
} from 'antd';
import {
  Cpu, ExternalLink, RefreshCw, Maximize2, Minimize2,
  Settings, Box, PlusCircle, Layers, CheckCircle2,
  Lock, Unlock, Download, ShieldCheck, HelpCircle
} from 'lucide-react';
import { useAuthStore } from '../stores/useAuthStore';
import { useMbseWorkspaceStore } from '../stores/useMbseWorkspaceStore';
import sysonHtmlContent from '../../public/syson/index.html?raw';

const { Text, Title, Paragraph } = Typography;

export type SysonSourceType = 'embedded' | 'docker' | 'gateway' | 'custom';

// 预置数控机床常用构件库
interface CncComponentDef {
  id: string;
  name: string;
  type: string;
  packageName: string;
  description: string;
  defaultAttrs: Record<string, string | number>;
  ports: string[];
}

const CNC_PRESET_COMPONENTS: CncComponentDef[] = [
  {
    id: 'elem_part_xfeed',
    name: 'XAxisFeedSystem',
    type: 'PartUsage',
    packageName: '04_PhysicalArchitecture',
    description: 'X轴高精度滚珠丝杠进给伺服系统',
    defaultAttrs: { stroke: '1020.0 mm', maxVelocity: '48.0 m/min', dampingRatio: '0.05' },
    ports: ['ctrlPort: ServoInterfacePort', 'sensorPort: OpticalScalePort'],
  },
  {
    id: 'elem_part_yfeed',
    name: 'YAxisFeedSystem',
    type: 'PartUsage',
    packageName: '04_PhysicalArchitecture',
    description: 'Y轴滑动导轨进给传动单元',
    defaultAttrs: { stroke: '800.0 mm', maxVelocity: '48.0 m/min', dampingRatio: '0.05' },
    ports: ['ctrlPort: ServoInterfacePort'],
  },
  {
    id: 'elem_part_zfeed',
    name: 'ZAxisFeedSystem',
    type: 'PartUsage',
    packageName: '04_PhysicalArchitecture',
    description: 'Z轴重载立柱垂直升降进给机构 (带气动配重)',
    defaultAttrs: { stroke: '600.0 mm', maxVelocity: '36.0 m/min', dampingRatio: '0.06' },
    ports: ['ctrlPort: ServoInterfacePort', 'brakePort: SafetyBrakePort'],
  },
  {
    id: 'elem_part_spindle',
    name: 'HighSpeedSpindle',
    type: 'PartUsage',
    packageName: '04_PhysicalArchitecture',
    description: '18000 rpm 同步电主轴单元',
    defaultAttrs: { maxSpeed: '18000.0 rpm', ratedTorque: '120.0 N.m', power: '22.0 kW' },
    ports: ['drivePort: InverterPort', 'coolingPort: WaterCoolingPort'],
  },
  {
    id: 'elem_part_rotary',
    name: 'BCAxisRotaryTable',
    type: 'PartUsage',
    packageName: '04_PhysicalArchitecture',
    description: '五轴联动力矩电机直接驱动双轴摇篮转台',
    defaultAttrs: { maxTiltAngle: '±120 deg', maxRotSpeed: '100.0 rpm' },
    ports: ['torqueDrivePort: DirectDrivePort'],
  },
  {
    id: 'elem_part_cnc',
    name: 'CncMotionController',
    type: 'PartUsage',
    packageName: '03_LogicalArchitecture',
    description: '高档五轴数控系统插补运动控制器',
    defaultAttrs: { cycleTime: '0.5 ms', interpChannels: '5 Axes' },
    ports: ['busPort: IndustrialEthernetPort', 'hmiPort: EthernetPort'],
  },
  {
    id: 'elem_part_atc',
    name: 'ToolMagazine_ATC',
    type: 'PartUsage',
    packageName: '04_PhysicalArchitecture',
    description: '30把刀臂式凸轮换刀机构 (换刀时间 < 1.8s)',
    defaultAttrs: { capacity: 30, toolChangeTime: '1.8 s' },
    ports: ['pneumaticPort: AirValvePort'],
  },
];

export const SysonModelingPage: React.FC = () => {
  const { user } = useAuthStore();
  const currentUserId = user?.username || 'ENG-MECH-1042';

  const {
    workspace,
    isLockedByMe,
    acquireSessionLock,
    releaseSessionLock,
  } = useMbseWorkspaceStore();

  // 数据源模式：默认为内置的高保真 SysON Web 客户端 (100%立即可见可操作)
  const [sourceType, setSourceType] = useState<SysonSourceType>('embedded');
  const [dockerEndpoint, setDockerEndpoint] = useState('http://localhost:8085/workspaces/syson-proj-uuid-88192a01-c918');
  const [gatewayEndpoint, setGatewayEndpoint] = useState('http://localhost:8084/syson/');
  const [customEndpoint, setCustomEndpoint] = useState('');
  const [iframeKey, setIframeKey] = useState(1);
  const [iframeLoading, setIframeLoading] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [isServerOnline, setIsServerOnline] = useState<boolean | null>(null);
  const [settingsModalVisible, setSettingsModalVisible] = useState(false);
  const [componentDrawerVisible, setComponentDrawerVisible] = useState(false);
  const [activeMode, setActiveMode] = useState<'iframe' | 'canvas'>('iframe');

  // 计算当前生效的 SysON 视口 URL
  const getEffectiveUrl = (): string => {
    switch (sourceType) {
      case 'embedded':
        return '/syson/index.html';
      case 'docker':
        return dockerEndpoint;
      case 'gateway':
        return gatewayEndpoint;
      case 'custom':
        return customEndpoint || '/syson/index.html';
      default:
        return '/syson/index.html';
    }
  };

  // 画布上的动态建模构件列表 (支持实时交互建模)
  const [modelComponents, setModelComponents] = useState<CncComponentDef[]>([
    CNC_PRESET_COMPONENTS[0], // XAxisFeedSystem
    CNC_PRESET_COMPONENTS[3], // HighSpeedSpindle
    CNC_PRESET_COMPONENTS[5], // CncMotionController
  ]);
  const [selectedCompId, setSelectedCompId] = useState<string>('elem_part_xfeed');
  const [newCompModalVisible, setNewCompModalVisible] = useState(false);
  const [form] = Form.useForm();

  const iframeRef = useRef<HTMLIFrameElement>(null);
  const hasLock = isLockedByMe(currentUserId);

  // 探活 SysON 容器服务
  const checkSysonHealth = async () => {
    try {
      // 探测 SysON 容器服务 (端口: 8085)
      await fetch('http://localhost:8085/health', { method: 'GET', mode: 'no-cors' });
      // 在 no-cors 模式下若未抛网络异常，说明端口有响应
      setIsServerOnline(true);
    } catch {
      setIsServerOnline(false);
    }
  };

  useEffect(() => {
    checkSysonHealth();
    const interval = setInterval(checkSysonHealth, 15000);
    return () => clearInterval(interval);
  }, []);

  // 刷新 iframe
  const handleReloadIframe = () => {
    setIframeKey(prev => prev + 1);
    message.info('SysON 视口已重新加载');
  };

  // 全屏切换
  const handleToggleFullscreen = () => {
    setIsFullscreen(!isFullscreen);
  };

  // 申请/释放锁
  const handleToggleLock = async () => {
    if (hasLock) {
      await releaseSessionLock(currentUserId);
      message.success('排他编辑锁已释放，恢复只读浏览状态');
    } else {
      const res = await acquireSessionLock(currentUserId);
      if (res.success) {
        message.success(res.message);
      } else {
        Modal.warning({
          title: '排他锁控制冲突 (CST-M04-01)',
          content: res.message,
        });
      }
    }
  };

  // 添加构件到当前模型
  const handleAddComponent = (comp: CncComponentDef) => {
    if (modelComponents.some(c => c.id === comp.id)) {
      message.warning(`构件 [${comp.name}] 已存在于当前模型中`);
      return;
    }
    setModelComponents([...modelComponents, comp]);
    setSelectedCompId(comp.id);
    message.success(`已成功向 SysON 建模画布注入构件：${comp.name}`);
    setComponentDrawerVisible(false);

    // 尝试向 iframe 派发 postMessage 消息以通知 SysON 原生端
    if (iframeRef.current && iframeRef.current.contentWindow) {
      try {
        iframeRef.current.contentWindow.postMessage(
          { action: 'ADD_COMPONENT', payload: comp },
          '*'
        );
      } catch (e) {
        console.warn('postMessage 派发受跨域限制', e);
      }
    }
  };

  // 自定义新建构件
  const handleCreateCustomComponent = (values: any) => {
    const customComp: CncComponentDef = {
      id: 'custom_comp_' + Date.now(),
      name: values.name,
      type: values.type,
      packageName: values.packageName,
      description: values.description || '用户自定义数控机床正向设计构件',
      defaultAttrs: {},
      ports: values.port ? [values.port] : ['defaultPort: SystemInterface'],
    };
    handleAddComponent(customComp);
    setNewCompModalVisible(false);
    form.resetFields();
  };

  // 导出当前模型的 SysML 代码
  const handleExportSysML = () => {
    const code = `package VMC1000_SystemModel {\n` +
      `    doc /* 在 SysON Web 端建模导出的机床正向设计模型 */\n` +
      modelComponents.map(c =>
        `    package '${c.packageName}' {\n` +
        `        part def ${c.name} {\n` +
        `            doc /* ${c.description} */\n` +
        Object.entries(c.defaultAttrs).map(([k, v]) => `            attribute ${k} = ${v};\n`).join('') +
        c.ports.map(p => `            port ${p};\n`).join('') +
        `        }\n` +
        `    }\n`
      ).join('\n') +
      `}\n`;

    const blob = new Blob([code], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'VMC1000_SysON_Export.sysml';
    a.click();
    URL.revokeObjectURL(url);
    message.success('已成功导出当前 SysON 建模工程为 SysML v2 标准代码包');
  };

  const selectedComp = modelComponents.find(c => c.id === selectedCompId);

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: 10,
        height: isFullscreen ? '100vh' : 'calc(100vh - 110px)',
        position: isFullscreen ? 'fixed' : 'relative',
        top: isFullscreen ? 0 : 'auto',
        left: isFullscreen ? 0 : 'auto',
        right: isFullscreen ? 0 : 'auto',
        bottom: isFullscreen ? 0 : 'auto',
        zIndex: isFullscreen ? 9999 : 1,
        backgroundColor: '#f1f5f9',
        padding: isFullscreen ? 12 : 0,
      }}
    >
      {/* 顶部控制栏 */}
      <Card
        bodyStyle={{ padding: '10px 16px' }}
        style={{ borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}
      >
        <Row justify="space-between" align="middle">
          <Col>
            <Space size="middle" align="center">
              <span style={{ fontSize: 17, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 8 }}>
                <Cpu className="w-5 h-5 text-blue-600" />
                系统设计 · 系统建模 (SysON Web 视口集成)
              </span>
              <Tag color="cyan">工程: SMP-VMC1000-01 (Rev A)</Tag>
              <Tag color="blue">通道: GRAPHICAL (图形通道)</Tag>
              <Tooltip title="SysON 采用独立 Postgres 实例 (syson_db :5434)，实现项目与租户垂直物理隔离">
                <Tag color="purple" style={{ cursor: 'pointer' }}>
                  <ShieldCheck className="w-3.5 h-3.5 inline mr-1" />
                  独立 Postgres 存储隔离
                </Tag>
              </Tooltip>
              {isServerOnline ? (
                <Badge status="success" text={<span style={{ fontSize: 12, color: '#16a34a' }}>SysON 容器服务已连接 (:8085)</span>} />
              ) : (
                <Badge status="processing" text={<span style={{ fontSize: 12, color: '#0284c7' }}>SysON Web 嵌入式交互视口已就绪</span>} />
              )}
            </Space>
          </Col>

          <Col>
            <Space size="small">
              {/* SysON 客户端数据源切换 */}
              <Segmented
                size="small"
                value={sourceType}
                onChange={(val) => {
                  const newSource = val as SysonSourceType;
                  setSourceType(newSource);
                  setIframeLoading(true);
                  setIframeKey(prev => prev + 1);
                  if (newSource === 'docker' && !isServerOnline) {
                    message.warning('提示：本地 8085 端口的 SysON 容器未启动，若外部加载失败可随时切回内置客户端');
                  } else {
                    message.info(`已切换视口源为: ${newSource === 'embedded' ? '内置 SysON 客户端' : newSource === 'docker' ? '外部 Docker 容器 (:8085)' : '网关代理 (:8084)'}`);
                  }
                }}
                options={[
                  { label: '🚀 内置客户端 (推荐)', value: 'embedded' },
                  { label: '🐳 Docker (:8085)', value: 'docker' },
                  { label: '🌐 网关 (:8084)', value: 'gateway' },
                ]}
              />

              {/* 视口模式切换 */}
              <Button.Group size="small">
                <Button
                  type={activeMode === 'iframe' ? 'primary' : 'default'}
                  onClick={() => setActiveMode('iframe')}
                >
                  原生 iframe 视口
                </Button>
                <Button
                  type={activeMode === 'canvas' ? 'primary' : 'default'}
                  onClick={() => setActiveMode('canvas')}
                >
                  备用可视化画布
                </Button>
              </Button.Group>

              {/* 构件库抽屉 */}
              <Button
                size="small"
                type="primary"
                icon={<PlusCircle className="w-3.5 h-3.5" />}
                onClick={() => setComponentDrawerVisible(true)}
              >
                添加数控机床构件
              </Button>

              {/* 排他编辑锁 */}
              {hasLock ? (
                <Button
                  size="small"
                  type="primary"
                  danger
                  icon={<Unlock className="w-3.5 h-3.5" />}
                  onClick={handleToggleLock}
                >
                  释放锁 ({workspace.boundUserId})
                </Button>
              ) : (
                <Button
                  size="small"
                  type="primary"
                  icon={<Lock className="w-3.5 h-3.5" />}
                  onClick={handleToggleLock}
                >
                  申请排他编辑锁
                </Button>
              )}

              {/* 刷新 iframe */}
              <Tooltip title="刷新 SysON 建模视口">
                <Button size="small" icon={<RefreshCw className="w-3.5 h-3.5" />} onClick={handleReloadIframe} />
              </Tooltip>

              {/* 在新窗口打开 */}
              <Tooltip title="在新浏览器标签页中打开当前 SysON 建模界面">
                <Button
                  size="small"
                  icon={<ExternalLink className="w-3.5 h-3.5" />}
                  onClick={() => window.open(getEffectiveUrl(), '_blank')}
                />
              </Tooltip>

              {/* 导出 SysML 代码 */}
              <Tooltip title="导出当前建模内容为 SysML v2 代码包">
                <Button size="small" icon={<Download className="w-3.5 h-3.5" />} onClick={handleExportSysML} />
              </Tooltip>

              {/* 全屏切换 */}
              <Tooltip title={isFullscreen ? '退出全屏' : '全屏建模模式'}>
                <Button
                  size="small"
                  icon={isFullscreen ? <Minimize2 className="w-3.5 h-3.5" /> : <Maximize2 className="w-3.5 h-3.5" />}
                  onClick={handleToggleFullscreen}
                />
              </Tooltip>

              {/* 端点配置 */}
              <Tooltip title="SysON 服务端点设置">
                <Button
                  size="small"
                  icon={<Settings className="w-3.5 h-3.5" />}
                  onClick={() => setSettingsModalVisible(true)}
                />
              </Tooltip>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 主体视口区域 */}
      <div style={{ flex: 1, display: 'flex', gap: 10, minHeight: 0 }}>
        {/* 左侧：当前已实例化的机床构件树 */}
        <Card
          title={
            <Space>
              <Layers className="w-4 h-4 text-blue-600" />
              <span style={{ fontSize: 13 }}>当前系统模型构件 ({modelComponents.length})</span>
            </Space>
          }
          extra={
            <Button size="small" type="link" onClick={() => setNewCompModalVisible(true)}>
              新建构件
            </Button>
          }
          bodyStyle={{ padding: 8, overflowY: 'auto', flex: 1 }}
          style={{ width: 260, borderRadius: 8, display: 'flex', flexDirection: 'column' }}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            {modelComponents.map(comp => {
              const isSelected = comp.id === selectedCompId;
              return (
                <div
                  key={comp.id}
                  onClick={() => setSelectedCompId(comp.id)}
                  style={{
                    padding: '8px 10px',
                    borderRadius: 6,
                    border: isSelected ? '1.5px solid #2563eb' : '1px solid #e2e8f0',
                    background: isSelected ? '#eff6ff' : '#fff',
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Text strong style={{ fontSize: 12, color: isSelected ? '#1d4ed8' : '#1e293b' }}>
                      {comp.name}
                    </Text>
                    <Tag color="blue" style={{ fontSize: 10, margin: 0 }}>{comp.packageName.split('_')[1] || 'Part'}</Tag>
                  </div>
                  <div style={{ fontSize: 11, color: '#64748b', marginTop: 2, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {comp.description}
                  </div>
                </div>
              );
            })}
          </div>
        </Card>

        {/* 中间：iframe 嵌入核心视口 */}
        <Card
          bodyStyle={{ padding: 0, height: '100%', position: 'relative', overflow: 'hidden' }}
          style={{ flex: 1, borderRadius: 8, display: 'flex', flexDirection: 'column' }}
        >
          {activeMode === 'iframe' ? (
            /* 原生 iframe 方式嵌入 SysON Web 端 */
            <div style={{ width: '100%', height: '100%', position: 'relative', background: '#ffffff', display: 'flex', flexDirection: 'column' }}>
              {/* 当用户主动切换到外部容器且未检测到服务时，给出清晰警告与一键切回按钮 */}
              {sourceType === 'docker' && isServerOnline === false && (
                <Alert
                  type="warning"
                  showIcon
                  message="未检测到本地运行的 SysON 容器服务 (端口: 8085)"
                  description={
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 4 }}>
                      <span>当前未运行 Docker 容器。如需立即建模，可点击右侧按钮恢复使用内置 SysON 客户端：</span>
                      <Button size="small" type="primary" onClick={() => setSourceType('embedded')}>
                        恢复使用内置 SysON 客户端
                      </Button>
                    </div>
                  }
                  style={{ borderRadius: 0, borderTop: 0, borderLeft: 0, borderRight: 0 }}
                />
              )}

              <div style={{ flex: 1, position: 'relative', width: '100%', height: '100%', minHeight: 0 }}>
                {iframeLoading && (
                  <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(255,255,255,0.8)', zIndex: 10 }}>
                    <Spin tip="正在载入 SysON 建模视口..." />
                  </div>
                )}
                <iframe
                  ref={iframeRef}
                  key={iframeKey}
                  src={sourceType === 'embedded' ? undefined : getEffectiveUrl()}
                  srcDoc={sourceType === 'embedded' ? sysonHtmlContent : undefined}
                  title="Eclipse SysON Web Canvas"
                  onLoad={() => setIframeLoading(false)}
                  style={{
                    width: '100%',
                    height: '100%',
                    border: 'none',
                    display: 'block',
                  }}
                  allow="fullscreen"
                />
              </div>

              {/* 底部视口状态与快捷切回条 */}
              <div
                style={{
                  position: 'absolute',
                  bottom: 10,
                  right: 12,
                  background: 'rgba(255, 255, 255, 0.95)',
                  backdropFilter: 'blur(6px)',
                  border: '1px solid #cbd5e1',
                  borderRadius: 6,
                  padding: '5px 12px',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                  fontSize: 11,
                  color: '#475569',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 10,
                  zIndex: 20,
                }}
              >
                <span>模式: <Tag color={sourceType === 'embedded' ? 'cyan' : 'blue'} style={{ margin: 0 }}>{sourceType === 'embedded' ? '内置 SysON Web 客户端' : sourceType === 'docker' ? '外部 Docker 容器' : '网关代理'}</Tag></span>
                <span>端点: <code>{getEffectiveUrl()}</code></span>
                {sourceType !== 'embedded' && (
                  <Button size="small" type="link" style={{ padding: 0 }} onClick={() => setSourceType('embedded')}>
                    一键切回内置客户端
                  </Button>
                )}
              </div>
            </div>
          ) : (
            /* 可视化 SysON 建模画布 (支持直接在页面上操作建模) */
            <div
              style={{
                width: '100%',
                height: '100%',
                background: '#f8fafc',
                padding: 16,
                overflowY: 'auto',
                backgroundImage: 'radial-gradient(#cbd5e1 1px, transparent 1px)',
                backgroundSize: '20px 20px',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                <Space>
                  <Tag color="geekblue">SysON Web 可交互建模画布 (SysML v2)</Tag>
                  <span style={{ fontSize: 11, color: '#64748b' }}>可直接在画布中选中构件、编辑参数与连接接口</span>
                </Space>
                <Button size="small" icon={<PlusCircle className="w-3.5 h-3.5" />} onClick={() => setComponentDrawerVisible(true)}>
                  从机床标准库添加构件
                </Button>
              </div>

              {/* 机床整机包架构视图 */}
              <div
                style={{
                  border: '2px dashed #94a3b8',
                  borderRadius: 8,
                  padding: 16,
                  background: 'rgba(241, 245, 249, 0.5)',
                  minHeight: 460,
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
                  <div>
                    <Text strong style={{ fontSize: 14, color: '#1e293b' }}>
                      «package» VMC1000_SystemModel (五轴立式加工中心)
                    </Text>
                    <div style={{ fontSize: 11, color: '#64748b' }}>
                      基于 SysML v2 规范组织机床需求、功能行为、逻辑架构与物理装配
                    </div>
                  </div>
                  <Tag color="blue">SysML v2 Standard Compliant</Tag>
                </div>

                <Row gutter={[16, 16]}>
                  {modelComponents.map(comp => {
                    const isSelected = comp.id === selectedCompId;
                    return (
                      <Col span={8} key={comp.id}>
                        <div
                          onClick={() => setSelectedCompId(comp.id)}
                          style={{
                            background: '#ffffff',
                            border: isSelected ? '2px solid #2563eb' : '1px solid #cbd5e1',
                            borderRadius: 6,
                            padding: 12,
                            boxShadow: isSelected ? '0 0 10px rgba(37, 99, 235, 0.2)' : '0 1px 3px rgba(0,0,0,0.05)',
                            cursor: 'pointer',
                            transition: 'all 0.2s',
                          }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div>
                              <Tag color="blue" style={{ fontSize: 10 }}>«part def»</Tag>
                              <div style={{ fontSize: 13, fontWeight: 700, color: '#1e3a8a', marginTop: 2 }}>{comp.name}</div>
                            </div>
                            <Tag color="green">ACTIVE</Tag>
                          </div>
                          <div style={{ fontSize: 11, color: '#64748b', margin: '6px 0' }}>{comp.description}</div>

                          {/* 属性参数 */}
                          <div style={{ background: '#f8fafc', padding: '6px 8px', borderRadius: 4, fontSize: 11, color: '#334155' }}>
                            {Object.entries(comp.defaultAttrs).map(([k, v]) => (
                              <div key={k}>• {k}: <code>{v}</code></div>
                            ))}
                          </div>

                          {/* 端口契约 */}
                          <div style={{ marginTop: 8, display: 'flex', flexDirection: 'column', gap: 4 }}>
                            {comp.ports.map((p, idx) => (
                              <div key={idx} style={{ fontSize: 10, color: '#0369a1', background: '#e0f2fe', padding: '2px 6px', borderRadius: 3 }}>
                                🔌 {p}
                              </div>
                            ))}
                          </div>
                        </div>
                      </Col>
                    );
                  })}
                </Row>
              </div>
            </div>
          )}
        </Card>

        {/* 右侧：选中构件属性与编辑面板 */}
        <Card
          title={
            <Space>
              <Box className="w-4 h-4 text-indigo-600" />
              <span style={{ fontSize: 13 }}>构件属性编辑器</span>
            </Space>
          }
          bodyStyle={{ padding: 12, overflowY: 'auto', flex: 1 }}
          style={{ width: 280, borderRadius: 8, display: 'flex', flexDirection: 'column' }}
        >
          {selectedComp ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <div>
                <Tag color="blue">«{selectedComp.type}»</Tag>
                <Title level={5} style={{ margin: '4px 0 2px 0' }}>{selectedComp.name}</Title>
                <Text type="secondary" style={{ fontSize: 11 }}>所属包: {selectedComp.packageName}</Text>
              </div>

              <Divider style={{ margin: '6px 0' }} />

              <div>
                <Text strong style={{ fontSize: 12 }}>工程参数与规格</Text>
                <div style={{ fontSize: 11, color: '#475569', marginTop: 4, display: 'flex', flexDirection: 'column', gap: 4 }}>
                  {Object.entries(selectedComp.defaultAttrs).map(([key, val]) => (
                    <div key={key} style={{ display: 'flex', justifyContent: 'space-between', background: '#f8fafc', padding: '3px 6px', borderRadius: 4 }}>
                      <span>{key}:</span>
                      <code>{val}</code>
                    </div>
                  ))}
                </div>
              </div>

              <Divider style={{ margin: '6px 0' }} />

              <div>
                <Text strong style={{ fontSize: 12 }}>接口与物理端口 (Port)</Text>
                <div style={{ fontSize: 11, color: '#0369a1', marginTop: 4, display: 'flex', flexDirection: 'column', gap: 4 }}>
                  {selectedComp.ports.map((port, idx) => (
                    <div key={idx} style={{ background: '#e0f2fe', padding: '3px 6px', borderRadius: 4 }}>
                      • {port}
                    </div>
                  ))}
                </div>
              </div>

              <Divider style={{ margin: '6px 0' }} />

              <div>
                <Text strong style={{ fontSize: 12 }}>PLM 关联主数据 (M03/M05)</Text>
                <div style={{ fontSize: 11, color: '#64748b', marginTop: 4 }}>
                  <div>• 所属工程: SMP-VMC1000-01</div>
                  <div>• 修订版本: Rev A (草稿)</div>
                  <div>• 责任人: {currentUserId}</div>
                </div>
              </div>

              <Button
                danger
                size="small"
                style={{ marginTop: 10 }}
                onClick={() => {
                  if (modelComponents.length <= 1) {
                    message.warning('至少保留一个机床基础构件');
                    return;
                  }
                  setModelComponents(modelComponents.filter(c => c.id !== selectedComp.id));
                  setSelectedCompId(modelComponents[0].id);
                  message.success(`已从建模画布移除构件 [${selectedComp.name}]`);
                }}
              >
                从当前模型中移除
              </Button>
            </div>
          ) : (
            <div style={{ padding: 20, textAlign: 'center', color: '#94a3b8', fontSize: 12 }}>
              请在中间视口中点击选择构件进行编辑
            </div>
          )}
        </Card>
      </div>

      {/* 底部架构说明栏 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 12px', background: '#fff', borderRadius: 6, border: '1px solid #e2e8f0', fontSize: 11, color: '#64748b' }}>
        <Space size="middle">
          <span><CheckCircle2 className="w-3.5 h-3.5 text-green-500 inline mr-1" />SysON Web 容器化部署</span>
          <span><ShieldCheck className="w-3.5 h-3.5 text-blue-500 inline mr-1" />独立 Postgres 实例 (syson_db :5434) 垂直物理隔离</span>
          <span><HelpCircle className="w-3.5 h-3.5 text-purple-500 inline mr-1" />两阶段绑定: 草稿期临时映射，发布期固化数字主线</span>
        </Space>
        <span>MBSE 网关反向代理: <code>http://localhost:8084/syson/</code></span>
      </div>

      {/* 添加构件抽屉 */}
      <Drawer
        title="常用数控机床 SysML v2 构件库 (点击一键注入模型)"
        placement="right"
        width={420}
        open={componentDrawerVisible}
        onClose={() => setComponentDrawerVisible(false)}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <Paragraph style={{ fontSize: 12, color: '#64748b' }}>
            选择数控机床标准化构件库直接注入 SysON 建模画布，支持机床运动进给、高速主轴、回转转台与数控系统：
          </Paragraph>

          {CNC_PRESET_COMPONENTS.map(comp => {
            const isAdded = modelComponents.some(c => c.id === comp.id);
            return (
              <div
                key={comp.id}
                style={{
                  border: '1px solid #e2e8f0',
                  borderRadius: 6,
                  padding: 10,
                  background: isAdded ? '#f0fdf4' : '#fff',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Text strong style={{ fontSize: 13, color: '#1e3a8a' }}>{comp.name}</Text>
                  <Tag color="blue">{comp.packageName.split('_')[1]}</Tag>
                </div>
                <div style={{ fontSize: 11, color: '#64748b', margin: '4px 0' }}>{comp.description}</div>
                <div style={{ fontSize: 11, color: '#0369a1' }}>
                  端口: {comp.ports.join(', ')}
                </div>
                <div style={{ marginTop: 8, display: 'flex', justifyContent: 'flex-end' }}>
                  {isAdded ? (
                    <Tag color="success">已在当前模型中</Tag>
                  ) : (
                    <Button size="small" type="primary" onClick={() => handleAddComponent(comp)}>
                      注入 SysON 画布
                    </Button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </Drawer>

      {/* 新建模构件弹窗 */}
      <Modal
        title="新建数控机床模型构件"
        open={newCompModalVisible}
        onCancel={() => setNewCompModalVisible(false)}
        onOk={() => form.submit()}
        okText="创建并添加"
        cancelText="取消"
      >
        <Form form={form} layout="vertical" onFinish={handleCreateCustomComponent}>
          <Form.Item name="name" label="构件名称 (SysML Part Name)" rules={[{ required: true, message: '请输入构件英文标识' }]}>
            <Input placeholder="例如: WorkpieceCoolingSystem" />
          </Form.Item>
          <Form.Item name="packageName" label="所属架构包" initialValue="04_PhysicalArchitecture">
            <Select
              options={[
                { label: '01_Requirements (需求包)', value: '01_Requirements' },
                { label: '02_FunctionalBehavior (功能行为包)', value: '02_FunctionalBehavior' },
                { label: '03_LogicalArchitecture (逻辑架构包)', value: '03_LogicalArchitecture' },
                { label: '04_PhysicalArchitecture (物理架构包)', value: '04_PhysicalArchitecture' },
                { label: '05_Interfaces (接口契约包)', value: '05_Interfaces' },
              ]}
            />
          </Form.Item>
          <Form.Item name="type" label="SysML 元素类型" initialValue="PartUsage">
            <Select
              options={[
                { label: 'PartUsage (部件)', value: 'PartUsage' },
                { label: 'RequirementUsage (需求)', value: 'RequirementUsage' },
                { label: 'ActionUsage (动作行为)', value: 'ActionUsage' },
                { label: 'Port (接口端口)', value: 'Port' },
              ]}
            />
          </Form.Item>
          <Form.Item name="port" label="默认端口定义" initialValue="ctrlPort: ServoInterfacePort">
            <Input placeholder="例如: ctrlPort: ServoInterfacePort" />
          </Form.Item>
          <Form.Item name="description" label="构件工程说明">
            <Input.TextArea placeholder="请输入该机床部件的正向设计功能说明" rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      {/* SysON 服务端点设置弹窗 */}
      <Modal
        title="SysON 视口服务连接配置"
        open={settingsModalVisible}
        onCancel={() => setSettingsModalVisible(false)}
        onOk={() => {
          setSettingsModalVisible(false);
          handleReloadIframe();
        }}
        okText="保存并刷新"
        cancelText="取消"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <Paragraph style={{ fontSize: 12, color: '#64748b', margin: 0 }}>
            配置内嵌 iframe 连接的 SysON Web 客户端源。支持在平台内置的高保真建模视口、本地独立 Docker 容器或统一反代网关之间自由切换：
          </Paragraph>
          <div>
            <Text strong style={{ fontSize: 12 }}>视口数据源模式：</Text>
            <div style={{ marginTop: 6 }}>
              <Segmented
                value={sourceType}
                onChange={(val) => setSourceType(val as SysonSourceType)}
                options={[
                  { label: '内置客户端', value: 'embedded' },
                  { label: 'Docker 容器 (:8085)', value: 'docker' },
                  { label: '网关反代 (:8084)', value: 'gateway' },
                  { label: '自定义端点', value: 'custom' },
                ]}
              />
            </div>
          </div>
          {sourceType === 'docker' && (
            <div>
              <Text strong style={{ fontSize: 12 }}>SysON 容器端点 URL：</Text>
              <Input
                value={dockerEndpoint}
                onChange={(e) => setDockerEndpoint(e.target.value)}
                placeholder="http://localhost:8085/workspaces/syson-proj-uuid-88192a01-c918"
                style={{ marginTop: 4 }}
              />
            </div>
          )}
          {sourceType === 'gateway' && (
            <div>
              <Text strong style={{ fontSize: 12 }}>网关反向代理 URL：</Text>
              <Input
                value={gatewayEndpoint}
                onChange={(e) => setGatewayEndpoint(e.target.value)}
                placeholder="http://localhost:8084/syson/"
                style={{ marginTop: 4 }}
              />
            </div>
          )}
          {sourceType === 'custom' && (
            <div>
              <Text strong style={{ fontSize: 12 }}>自定义端点 URL：</Text>
              <Input
                value={customEndpoint}
                onChange={(e) => setCustomEndpoint(e.target.value)}
                placeholder="http://192.168.1.100:8085/syson"
                style={{ marginTop: 4 }}
              />
            </div>
          )}
          <div style={{ background: '#f8fafc', padding: 10, borderRadius: 6, fontSize: 11, color: '#64748b' }}>
            <div>• <strong>内置客户端模式</strong>: 无需启动任何外部容器，即开即用，支持 SysML v2 交互建模与规范导出；</div>
            <div>• <strong>容器化独立端点</strong>: <code>http://localhost:8085</code> (需运行 Docker)；</div>
            <div>• <strong>统一网关反代端点</strong>: <code>http://localhost:8084/syson/</code>；</div>
            <div>• <strong>独立 Postgres 存储</strong>: <code>5434</code> (syson_workspace_db)。</div>
          </div>
        </div>
      </Modal>
    </div>
  );
};
