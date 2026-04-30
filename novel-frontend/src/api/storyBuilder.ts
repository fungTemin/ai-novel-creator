// StoryBuilder 对话式创作 API：分步骤引导用户完成小说的构建过程
import client from './client';
import type { ApiResponse, StoryBuilderResponse } from '../types';

export const storyBuilderApi = {
  // 启动创作助手：初始化会话状态，返回首条引导消息
  start: (novelId: number) =>
    client.post<ApiResponse<StoryBuilderResponse>>(`/novels/${novelId}/builder/start`),

  // 跳转到指定创作步骤（如故事梗概→题材风格→角色构思等）
  startStep: (novelId: number, data: { novelId: number; step: string; action: string }) =>
    client.post<ApiResponse<StoryBuilderResponse>>(`/novels/${novelId}/builder/step`, data),

  // 发送对话消息：用户输入文本或选择选项，AI 回复并更新状态
  chat: (novelId: number, data: { novelId: number; message: string; action: string }) =>
    client.post<ApiResponse<StoryBuilderResponse>>(`/novels/${novelId}/builder/chat`, data),
};
