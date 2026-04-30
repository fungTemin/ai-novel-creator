// AI 相关 API：世界观、大纲、风格设定及 AI 生成/审查功能的接口层
import client from './client';
import type {
  WorldBuilding,
  PlotOutline,
  StyleSetting,
  ApiResponse,
  GeneratePlotRequest,
  GenerateCharacterRequest,
  GenerateChapterRequest,
  GenerateWorldRequest,
  ReviseChapterRequest,
  GenerateContentResponse,
  ReviseContentResponse,
  Character,
  GenerateWorldBatchRequest,
  SuggestWorldCategoriesRequest,
  ExpandWorldDetailRequest,
  ReviewWorldConsistencyRequest,
  ReviewPlotRequest,
  ExpandPlotRequest,
  SuggestPlotBranchesRequest,
  IntegrateCharacterArcRequest,
  ReviewForeshadowingRequest,
  SuggestedCategory,
  WorldConsistencyIssue,
  PlotReviewResult,
  PlotBranch,
  CharacterArcSuggestion,
  ForeshadowingReviewResult,
} from '../types';

// 世界观设定 CRUD
export const worldBuildingApi = {
  // 获取世界观列表，可选按分类过滤
  getAll: (novelId: number, category?: string) =>
    client.get<ApiResponse<WorldBuilding[]>>(`/novels/${novelId}/world-building`, {
      params: category ? { category } : undefined,
    }),

  // 创建世界观条目（排除 id 和时间戳）
  create: (novelId: number, data: Omit<WorldBuilding, 'id' | 'createdAt' | 'updatedAt'>) =>
    client.post<ApiResponse<WorldBuilding>>(`/novels/${novelId}/world-building`, data),

  // 更新世界观条目
  update: (id: number, data: Partial<WorldBuilding>) =>
    client.put<ApiResponse<WorldBuilding>>(`/world-building/${id}`, data),

  // 删除世界观条目
  delete: (id: number) =>
    client.delete<ApiResponse<void>>(`/world-building/${id}`),
};

// 故事大纲 CRUD
export const plotApi = {
  // 获取所有大纲节点
  getAll: (novelId: number) =>
    client.get<ApiResponse<PlotOutline[]>>(`/novels/${novelId}/outlines`),

  // 创建大纲节点（排除 id 和时间戳）
  create: (novelId: number, data: Omit<PlotOutline, 'id' | 'createdAt' | 'updatedAt'>) =>
    client.post<ApiResponse<PlotOutline>>(`/novels/${novelId}/outlines`, data),

  // 更新大纲节点
  update: (id: number, data: Partial<PlotOutline>) =>
    client.put<ApiResponse<PlotOutline>>(`/outlines/${id}`, data),

  // 删除大纲节点
  delete: (id: number) =>
    client.delete<ApiResponse<void>>(`/outlines/${id}`),
};

// 创作风格设定
export const styleApi = {
  // 获取小说的风格配置
  get: (novelId: number) =>
    client.get<ApiResponse<StyleSetting>>(`/novels/${novelId}/style`),

  // 更新风格配置（语气、视角、目标读者等）
  update: (novelId: number, data: Partial<StyleSetting>) =>
    client.put<ApiResponse<StyleSetting>>(`/novels/${novelId}/style`, data),
};

// AI 智能创作功能集合
export const aiApi = {
  // AI 生成故事大纲：基于构思、章节数和题材输出大纲节点列表
  generatePlot: (novelId: number, data: GeneratePlotRequest) =>
    client.post<ApiResponse<{ outlines: PlotOutline[] }>>(`/novels/${novelId}/generate/plot`, data),

  // AI 生成角色：根据描述和角色类型创建完整角色设定
  generateCharacter: (novelId: number, data: GenerateCharacterRequest) =>
    client.post<ApiResponse<Character>>(`/novels/${novelId}/generate/character`, data),

  // AI 生成章节内容：依据指令和字数要求生成正文
  generateChapter: (novelId: number, data: GenerateChapterRequest) =>
    client.post<ApiResponse<GenerateContentResponse>>(`/novels/${novelId}/generate/chapter`, data),

  // AI 生成世界观设定：按分类和描述粒度生成单条设定
  generateWorld: (novelId: number, data: GenerateWorldRequest) =>
    client.post<ApiResponse<WorldBuilding>>(`/novels/${novelId}/generate/world`, data),

  // AI 批量生成世界观：一次请求生成多个分类的多条设定
  generateWorldBatch: (novelId: number, data: GenerateWorldBatchRequest) =>
    client.post<ApiResponse<WorldBuilding[]>>(`/novels/${novelId}/generate/world/batch`, data),

  // AI 建议世界观分类：基于题材和已有分类推荐新的分类方向
  suggestWorldCategories: (novelId: number, data: SuggestWorldCategoriesRequest) =>
    client.post<ApiResponse<SuggestedCategory[]>>(`/novels/${novelId}/generate/world/suggest-categories`, data),

  // AI 扩展世界观细节：对已有设定从指定方面进行深度扩展
  expandWorldDetail: (_novelId: number, worldId: number, data: ExpandWorldDetailRequest) =>
    client.post<ApiResponse<WorldBuilding>>(`/world-building/${worldId}/expand`, data),

  // AI 世界观一致性检查：检测不同设定条目间的矛盾
  reviewWorldConsistency: (novelId: number, data: ReviewWorldConsistencyRequest) =>
    client.post<ApiResponse<WorldConsistencyIssue[]>>(`/novels/${novelId}/world-building/review-consistency`, data),

  // AI 审查故事大纲：分析一致性、节奏、完整性等维度
  reviewPlot: (novelId: number, data: ReviewPlotRequest) =>
    client.post<ApiResponse<PlotReviewResult>>(`/novels/${novelId}/generate/plot/review`, data),

  // AI 扩展情节：在大纲基础上细化子情节或补充新节点
  expandPlot: (novelId: number, data: ExpandPlotRequest) =>
    client.post<ApiResponse<PlotOutline[]>>(`/novels/${novelId}/generate/plot/expand`, data),

  // AI 建议情节分支：围绕关键节点提出多个可选发展方向
  suggestPlotBranches: (novelId: number, data: SuggestPlotBranchesRequest) =>
    client.post<ApiResponse<PlotBranch[]>>(`/novels/${novelId}/generate/plot/branches`, data),

  // AI 角色弧线整合分析：关联大纲节点与角色成长轨迹
  integrateCharacterArc: (novelId: number, data: IntegrateCharacterArcRequest) =>
    client.post<ApiResponse<CharacterArcSuggestion[]>>(`/novels/${novelId}/generate/plot/character-arc`, data),

  // AI 伏笔审查：分析伏笔埋设与回收的完整性
  reviewForeshadowing: (novelId: number, data: ReviewForeshadowingRequest) =>
    client.post<ApiResponse<ForeshadowingReviewResult>>(`/novels/${novelId}/generate/plot/foreshadowing`, data),

  // AI 修订章节内容：支持选中文本局部修订和全文修订
  reviseChapter: (chapterId: number, data: ReviseChapterRequest) =>
    client.post<ApiResponse<ReviseContentResponse>>(`/chapters/${chapterId}/revise`, data),
};
