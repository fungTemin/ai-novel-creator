// 章节 API：管理小说章节的增删改查及排序操作
import client from './client';
import type { Chapter, ChapterBrief, ApiResponse } from '../types';

export const chapterApi = {
  // 获取指定小说的所有章节概要（不含正文，适用于列表展示）
  getAll: (novelId: number) =>
    client.get<ApiResponse<ChapterBrief[]>>(`/novels/${novelId}/chapters`),

  // 获取单章完整内容（含正文），用于编辑器加载
  getById: (id: number) =>
    client.get<ApiResponse<Chapter>>(`/chapters/${id}`),

  // 创建新章节：可指定章节号、标题、内容和摘要
  create: (novelId: number, data: { chapterNumber?: number; title?: string; content?: string; summary?: string }) =>
    client.post<ApiResponse<Chapter>>(`/novels/${novelId}/chapters`, data),

  // 更新章节信息：支持局部更新标题、内容、摘要和状态
  update: (id: number, data: { title?: string; content?: string; summary?: string; status?: string }) =>
    client.put<ApiResponse<Chapter>>(`/chapters/${id}`, data),

  // 删除指定章节
  delete: (id: number) =>
    client.delete<ApiResponse<void>>(`/chapters/${id}`),

  // 批量调整章节顺序：传入排序后的章节 ID 数组
  reorder: (novelId: number, chapterIds: number[]) =>
    client.put<ApiResponse<void>>(`/novels/${novelId}/chapters/reorder`, { chapterIds }),
};
