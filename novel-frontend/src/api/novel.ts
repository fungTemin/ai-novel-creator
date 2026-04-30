// 小说 CRUD API：管理小说作品的增删改查
import client from './client';
import type { Novel, ApiResponse } from '../types';

export const novelApi = {
  // 获取当前用户的所有小说列表
  getAll: () => client.get<ApiResponse<Novel[]>>('/novels'),

  // 根据 ID 获取单部小说的详细信息
  getById: (id: number) => client.get<ApiResponse<Novel>>(`/novels/${id}`),

  // 创建新小说：需要提供标题，可选简介和题材
  create: (data: { title: string; description?: string; genre?: string }) =>
    client.post<ApiResponse<Novel>>('/novels', data),

  // 更新小说信息：支持局部更新标题、简介、题材和状态
  update: (id: number, data: { title?: string; description?: string; genre?: string; status?: string }) =>
    client.put<ApiResponse<Novel>>(`/novels/${id}`, data),

  // 删除小说及其所有关联数据（章节、角色等）
  delete: (id: number) => client.delete<ApiResponse<void>>(`/novels/${id}`),
};
