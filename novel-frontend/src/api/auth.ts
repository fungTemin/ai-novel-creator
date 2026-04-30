// 认证相关 API：处理用户注册和登录请求
import client from './client';
import type { AuthResponse, ApiResponse } from '../types';

export const authApi = {
  // 用户注册：提交用户名、邮箱和密码，返回用户信息和 JWT token
  register: (data: { username: string; email: string; password: string }) =>
    client.post<ApiResponse<AuthResponse>>('/auth/register', data),

  // 用户登录：提交用户名和密码，返回用户信息和 JWT token
  login: (data: { username: string; password: string }) =>
    client.post<ApiResponse<AuthResponse>>('/auth/login', data),
};
