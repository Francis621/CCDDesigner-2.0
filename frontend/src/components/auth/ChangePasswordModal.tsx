import React, { useState } from 'react';
import { Modal, Form, Input, Button, message, Alert, Progress, Space, Typography, Tag } from 'antd';
import { Lock, KeyRound, UserCheck } from 'lucide-react';
import { useAuthStore } from '@/stores/useAuthStore';

const { Text } = Typography;

interface ChangePasswordModalProps {
  visible: boolean;
  onClose: () => void;
}

export const ChangePasswordModal: React.FC<ChangePasswordModalProps> = ({ visible, onClose }) => {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState<number>(0);
  const { user, changePassword } = useAuthStore();

  // 计算密码强度：根据长度、数字、字母、特殊字符评估
  const computePasswordStrength = (pwd: string) => {
    if (!pwd) return 0;
    let score = 0;
    if (pwd.length >= 6) score += 25;
    if (pwd.length >= 10) score += 25;
    if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) score += 25;
    if (/[0-9]/.test(pwd) && /[^A-Za-z0-9]/.test(pwd)) score += 25;
    return score;
  };

  const getStrengthInfo = (score: number) => {
    if (score < 30) return { text: '弱', color: '#ff4d4f', percent: 25 };
    if (score < 70) return { text: '中等', color: '#faad14', percent: 60 };
    return { text: '高强', color: '#52c41a', percent: 100 };
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setPasswordStrength(computePasswordStrength(val));
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);

      const res = await changePassword(values.oldPassword, values.newPassword);
      message.success(res.message || '密码修改成功！');
      form.resetFields();
      setPasswordStrength(0);
      onClose();
    } catch (err: any) {
      if (err.errorFields) return; // Antd 表单校验自身错误
      message.error(err.message || '密码修改失败，请重试');
    } finally {
      setSubmitting(false);
    }
  };

  const strength = getStrengthInfo(passwordStrength);

  return (
    <Modal
      open={visible}
      title={
        <Space>
          <KeyRound className="w-5 h-5 text-blue-600" />
          <span className="font-bold text-slate-800">修改登录密码</span>
        </Space>
      }
      onCancel={() => {
        form.resetFields();
        setPasswordStrength(0);
        onClose();
      }}
      footer={[
        <Button key="cancel" onClick={onClose} disabled={submitting}>
          取消
        </Button>,
        <Button key="submit" type="primary" loading={submitting} onClick={handleSubmit}>
          确认修改密码
        </Button>,
      ]}
      width={500}
      destroyOnClose
    >
      <div className="py-2">
        {/* 当前账号提示信息 */}
        <div className="bg-slate-50 border border-slate-200 rounded-lg orientation-card p-3 mb-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-500">当前登录用户</div>
            <div className="text-sm font-bold text-slate-800 flex items-center gap-1.5 mt-0.5">
              <UserCheck className="w-4 h-4 text-blue-600" />
              <span>{user.realName}</span>
              <span className="font-mono text-xs text-slate-500">(@{user.username})</span>
            </div>
            <div className="text-[11px] text-slate-400 mt-0.5">{user.department}</div>
          </div>
          <div className="text-right">
            <Tag color="blue" className="m-0 font-mono text-xs">
              工号: {user.userId}
            </Tag>
          </div>
        </div>

        <Alert
          type="info"
          showIcon
          message="工程密码安全合规要求"
          description="密码长度需 ≥6 位；支持包含字母、数字及特殊符号组合。修改完成后将同步更新身份认证安全密钥。"
          className="mb-4 text-xs"
        />

        <Form form={form} layout="vertical" preserve={false}>
          <Form.Item
            name="oldPassword"
            label="原登录密码"
            rules={[{ required: true, message: '请输入当前正在使用的原密码' }]}
          >
            <Input.Password
              prefix={<Lock className="w-4 h-4 text-slate-400 mr-1" />}
              placeholder="请输入当前原密码 (初始测试密码为 Ccdd@2026! 或 admin123)"
            />
          </Form.Item>

          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '新密码长度至少需要 6 个字符' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (value && value === getFieldValue('oldPassword')) {
                    return Promise.reject(new Error('新密码不能与原密码完全一致'));
                  }
                  return Promise.resolve();
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<Lock className="w-4 h-4 text-slate-400 mr-1" />}
              placeholder="请输入符合安全复杂度要求的新密码 (≥6位)"
              onChange={handlePasswordChange}
            />
          </Form.Item>

          {passwordStrength > 0 && (
            <div className="mb-4 px-1">
              <div className="flex justify-between text-xs mb-1">
                <Text type="secondary">密码安全强度:</Text>
                <span style={{ color: strength.color, fontWeight: 600 }}>{strength.text}</span>
              </div>
              <Progress
                percent={strength.percent}
                strokeColor={strength.color}
                showInfo={false}
                size="small"
              />
            </div>
          )}

          <Form.Item
            name="confirmPassword"
            label="确认新密码"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: '请再次输入新密码以核对' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的新密码不一致，请重新输入'));
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<Lock className="w-4 h-4 text-slate-400 mr-1" />}
              placeholder="请再次输入新密码"
            />
          </Form.Item>
        </Form>
      </div>
    </Modal>
  );
};
