// ========================================
// 核心数据模型（Core Data Models）
// ========================================

// 用户信息：存储当前登录用户的基本身份数据
export interface User {
  id: number;
  username: string;
  email?: string;
}

// 小说作品：一部小说的完整元数据，包含标题、简介、题材、状态及章节数
export interface Novel {
  id: number;
  title: string;
  description: string | null;
  genre: string | null;
  status: string;
  chapterCount: number;
  createdAt: string;
  updatedAt: string;
}

// 章节完整内容：包含正文、标题、摘要及字数统计等所有字段
export interface Chapter {
  id: number;
  chapterNumber: number;
  title: string | null;
  content: string | null;
  status: string;
  wordCount: number;
  summary: string | null;
  createdAt: string;
  updatedAt: string;
}

// 章节概要：列表场景下使用的轻量版本，不包含正文内容以减少传输量
export interface ChapterBrief {
  id: number;
  chapterNumber: number;
  title: string | null;
  status: string;
  wordCount: number;
  summary: string | null;
  createdAt: string;
}

// 角色：小说中的人物设定，包含名称、类型、外貌、性格、背景和关系网
export interface Character {
  id: number;
  name: string;
  role: string | null;
  description: string | null;
  personality: string | null;
  background: string | null;
  relationships: string | null;
  createdAt: string;
  updatedAt: string;
}

// 世界观设定：如地理、文化、力量体系等，按分类组织
export interface WorldBuilding {
  id: number;
  category: string;
  name: string;
  description: string | null;
  details: string | null;
  createdAt: string;
  updatedAt: string;
}

// 故事大纲：情节节点的概要描述，通过 orderIndex 排序以形成线性叙事流
export interface PlotOutline {
  id: number;
  title: string;
  summary: string | null;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

// 修订记录：保存每次 AI 修订前后的内容对比及反馈，便于追溯
export interface Revision {
  id: number;
  originalContent: string | null;
  revisedContent: string | null;
  feedback: string | null;
  createdAt: string;
}

// 风格设定：控制整体创作风格，包括语气、视角、目标读者群等
export interface StyleSetting {
  id: number;
  tone: string | null;
  perspective: string | null;
  targetAudience: string | null;
  styleDescription: string | null;
  createdAt: string;
  updatedAt: string;
}

// ========================================
// 通用 API 响应格式（Generic API Response）
// ========================================

// 统一后端响应包裹：code 状态码、message 提示信息、data 泛型数据
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T | null;
}

// 登录/注册成功后的响应：包含用户基础信息和 JWT token
export interface AuthResponse {
  id: number;
  username: string;
  email?: string;
  token: string;
}

// ========================================
// AI 生成请求/响应（AI Generation Contracts）
// ========================================

// 生成大纲请求：基于故事构思、预计章数和题材
export interface GeneratePlotRequest {
  prompt: string;
  chapterCount: number;
  genre?: string;
}

// 生成角色请求：描述角色特征并指定其在故事中的定位
export interface GenerateCharacterRequest {
  prompt: string;
  role: string;
}

// 生成章节请求：控制 AI 生成特定章节的内容和字数
export interface GenerateChapterRequest {
  chapterId?: number;
  instructions: string;
  wordCount: number;
}

// 生成世界观设定请求：按分类和描述粒度生成
export interface GenerateWorldRequest {
  category: string;
  prompt: string;
  detailLevel: string;
}

// AI 修订章节请求：支持选中文本局部修订或全文修订
export interface ReviseChapterRequest {
  selectedText?: string;
  instructions: string;
  style: string;
}

// AI 生成文本返回：生成的正文内容及实际字数
export interface GenerateContentResponse {
  content: string;
  wordCount: number;
}

// AI 修订返回：修订后内容、反馈说明及变更明细列表
export interface ReviseContentResponse {
  revisedContent: string;
  feedback: string;
  changes: Array<{
    type: string;
    description: string;
  }>;
}

// ========================================
// 增强的世界观和大纲功能类型（Advanced World & Plot）
// ========================================

// 世界观批量生成：一次请求覆盖多个分类，每类生成指定数量的设定
export interface GenerateWorldBatchRequest {
  categories: string[];
  prompt: string;
  countPerCategory: number;
}

// 建议世界观分类：基于小说题材和已有分类推荐新的分类方向
export interface SuggestWorldCategoriesRequest {
  genre: string;
  existingCategories: string[];
}

// 扩展世界观细节：对已有设定从指定方面进行深度扩展
export interface ExpandWorldDetailRequest {
  worldId: number;
  aspect: string;
  detailLevel: string;
}

// 世界观一致性检查：检测不同设定条目之间的矛盾或不一致
export interface ReviewWorldConsistencyRequest {
  worldIds: number[];
}

// 大纲审查请求：指定审查焦点（一致性/节奏/完整性等）
export interface ReviewPlotRequest {
  focus: string;
}

// 情节扩展：在大纲指定节点上细化或延伸子情节
export interface ExpandPlotRequest {
  outlineId?: number;
  focus: string;
  depth: number;
}

// 情节分支建议：围绕关键节点提出多个可选发展方向
export interface SuggestPlotBranchesRequest {
  outlineId?: number;
  keyPoint: string;
  branchCount: number;
}

// 角色弧线整合：关联大纲节点与角色成长曲线，分析角色发展轨迹
export interface IntegrateCharacterArcRequest {
  outlineIds: number[];
  characterId?: number;
}

// 伏笔检查：分析大纲中伏笔的埋设与回收是否完整
export interface ReviewForeshadowingRequest {
  outlineIds: number[];
}

// 分类建议条目：包含分类名称、推荐理由和优先级评分
export interface SuggestedCategory {
  category: string;
  reason: string;
  priority: number;
}

// 世界观一致性问题：描述冲突条目及其严重程度和修改建议
export interface WorldConsistencyIssue {
  issue: string;
  severity: string;
  firstEntry: string;
  secondEntry: string | null;
  suggestion: string;
}

// 大纲审查问题详情：问题类型、描述、严重级别及相关大纲标题
export interface PlotIssue {
  type: string;
  description: string;
  severity: string;
  outlineTitle: string | null;
  suggestion: string;
}

// 叙事节奏分析：整体评分、偏慢/偏快段落及优化建议
export interface PacingAnalysis {
  overallRating: string;
  slowParts: string[];
  fastParts: string[];
  recommendation: string;
}

// 大纲审查结果汇总：总结、问题列表、改进建议及节奏分析
export interface PlotReviewResult {
  summary: string;
  issues: PlotIssue[];
  suggestions: string[];
  pacing: PacingAnalysis | null;
}

// 情节分支：标题、概要及其对故事走向的影响说明
export interface PlotBranch {
  title: string;
  summary: string;
  impact: string;
}

// 角色弧线建议：角色当前发展轨迹的描述及优化建议
export interface CharacterArcSuggestion {
  characterName: string;
  currentArc: string;
  suggestions: string[];
  relatedOutlines: string[];
}

// 伏笔设定项：伏笔埋设位置、回收揭示位置及线索提示
export interface ForeshadowingSuggestion {
  foreshadowAt: string;
  payoffAt: string;
  hint: string;
  importance: string;
}

// 伏笔审查结果：整体分析说明及伏笔建议列表
export interface ForeshadowingReviewResult {
  analysis: string;
  suggestions: ForeshadowingSuggestion[];
}

// ========================================
// StoryBuilder 对话式创作（Conversational Builder）
// ========================================

// StoryBuilder 状态：追踪当前对话式创作进度、步骤及上下文摘要
export interface StoryBuilderState {
  novelId: number;
  currentStep: string;
  stepIndex: number;
  totalSteps: number;
  chatHistory: StoryChatMessage[];
  contextSummary: Record<string, string>;
  completedSteps: string[];
  suggestions: string[];
  progress: number;
}

// 对话消息：角色（用户/AI/系统）、正文内容及可选快捷选项
export interface StoryChatMessage {
  role: string;
  content: string;
  options?: string[];
}

// StoryBuilder API 响应：更新后的完整状态、AI 回复消息和操作选项
export interface StoryBuilderResponse {
  state: StoryBuilderState;
  message: string;
  options: string[];
  canProceed: boolean;
  warning: string | null;
}
