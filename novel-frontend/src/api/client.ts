// 基于 axios 的 HTTP 客户端封装，处理请求鉴权和全局错误拦截
import axios from 'axios';

// 创建 axios 实例，配置基础路径为 /api（通过 vite proxy 转发）和 60 秒超时
const client = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器：自动从 localStorage 读取 JWT token 并附加到请求头
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：统一处理 401 未授权错误，清除过期 token 并跳转登录页
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default client;
