// 角色 API：管理小说角色的增删改查操作
import client from './client';
import type { Character, ApiResponse } from '../types';

export const characterApi = {
  // 获取指定小说的全部角色列表
  getAll: (novelId: number) =>
    client.get<ApiResponse<Character[]>>(`/novels/${novelId}/characters`),

  // 根据 ID 获取单个角色详情
  getById: (id: number) =>
    client.get<ApiResponse<Character>>(`/characters/${id}`),

  // 创建新角色：排除 id 和自动生成的时间戳字段
  create: (novelId: number, data: Omit<Character, 'id' | 'createdAt' | 'updatedAt'>) =>
    client.post<ApiResponse<Character>>(`/novels/${novelId}/characters`, data),

  // 更新角色信息：支持局部更新任意字段
  update: (id: number, data: Partial<Character>) =>
    client.put<ApiResponse<Character>>(`/characters/${id}`, data),

  // 删除指定角色
  delete: (id: number) =>
    client.delete<ApiResponse<void>>(`/characters/${id}`),
};
