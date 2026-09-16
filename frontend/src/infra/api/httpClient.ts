import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import CryptoJS from 'crypto-js';

// 简单浏览器端 UUID 生成辅助函数
export function generateUUID(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

export const apiClient: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
    'X-Client-Platform': 'CCDDesigner-Web-2.0',
  },
});

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  // 1. 获取会话认证与安全上下文
  const token = localStorage.getItem('ccdd_auth_token') || 'MOCK-JWT-ENGINEER-TOKEN';
  const projectId = localStorage.getItem('ccdd_current_project_id') || 'PRJ-VMC850';
  const clearance = localStorage.getItem('ccdd_security_clearance') || 'CONFIDENTIAL';

  config.headers.set('Authorization', `Bearer ${token}`);
  config.headers.set('X-Project-Id', projectId);
  config.headers.set('X-Security-Clearance', clearance);

  // 2. 幂等令牌 (Idempotency-Key) 自动生成与注入（针对有状态写操作）
  const method = config.method?.toUpperCase();
  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method || '')) {
    if (!config.headers.has('Idempotency-Key')) {
      config.headers.set('Idempotency-Key', `IDEMP-${Date.now()}-${generateUUID().slice(0, 8)}`);
    }

    // 3. 计算工业指令防篡改签名 (HMAC-SHA256)
    const timestamp = Date.now().toString();
    const payloadStr = config.data
      ? typeof config.data === 'string'
        ? config.data
        : JSON.stringify(config.data)
      : '';
    const signSource = `${method}|${config.url}|${timestamp}|${payloadStr}`;
    const signature = CryptoJS.HmacSHA256(signSource, token).toString();

    config.headers.set('X-Request-Timestamp', timestamp);
    config.headers.set('X-Request-Signature', signature);
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 403) {
      console.error('[PBAC-DENIED] 越权操作或当前密级不够访问此受控资产');
    }
    return Promise.reject(error.response?.data || error.message);
  }
);
