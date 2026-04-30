// 认证状态管理（zustand）：持久化用户登录状态到 localStorage
import { create } from 'zustand';
import type { User } from '../types';

// AuthState 接口定义：用户数据、JWT token 及身份操作方法
interface AuthState {
  user: User | null;
  token: string | null;
  setAuth: (user: User, token: string) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}

// 创建全局认证 store：初始化时从 localStorage 恢复已保存的会话
export const useAuthStore = create<AuthState>((set, get) => ({
  // 启动时从 localStorage 恢复用户信息和 token
  user: JSON.parse(localStorage.getItem('user') || 'null'),
  token: localStorage.getItem('token'),

  // 设置认证信息：同步写入 localStorage 并更新内存状态
  setAuth: (user, token) => {
    localStorage.setItem('user', JSON.stringify(user));
    localStorage.setItem('token', token);
    set({ user, token });
  },

  // 登出：清除 localStorage 中的会话数据并重置状态
  logout: () => {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    set({ user: null, token: null });
  },

  // 检查是否已登录：依据 token 是否存在判断
  isAuthenticated: () => {
    return !!get().token;
  },
}));
