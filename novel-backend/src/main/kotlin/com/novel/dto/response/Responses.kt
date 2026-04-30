package com.novel.dto.response

import java.time.LocalDateTime

// 通用 API 响应包装体
data class ApiResponse<T>(
    val code: Int = 200,
    val message: String = "success",
    val data: T? = null
)

// 认证响应（注册/登录）
data class AuthResponse(
    val id: Long,
    val username: String,
    val email: String? = null,
    val token: String
)

// 小说完整响应
data class NovelResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val genre: String?,
    val status: String,
    val chapterCount: Int = 0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// 章节完整响应（含正文）
data class ChapterResponse(
    val id: Long,
    val chapterNumber: Int,
    val title: String?,
    val content: String?,
    val status: String,
    val wordCount: Int,
    val summary: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// 章节简要响应（不含正文）
data class ChapterBriefResponse(
    val id: Long,
    val chapterNumber: Int,
    val title: String?,
    val status: String,
    val wordCount: Int,
    val summary: String?,
    val createdAt: LocalDateTime
)

// 角色响应
data class CharacterResponse(
    val id: Long,
    val name: String,
    val role: String?,
    val description: String?,
    val personality: String?,
    val background: String?,
    val relationships: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// 世界观条目响应
data class WorldBuildingResponse(
    val id: Long,
    val category: String,
    val name: String,
    val description: String?,
    val details: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// 情节大纲条目响应
data class PlotOutlineResponse(
    val id: Long,
    val title: String,
    val summary: String?,
    val orderIndex: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// 修订记录响应
data class RevisionResponse(
    val id: Long,
    val originalContent: String?,
    val revisedContent: String?,
    val feedback: String?,
    val createdAt: LocalDateTime
)

// 风格设置响应
data class StyleSettingResponse(
    val id: Long,
    val tone: String?,
    val perspective: String?,
    val targetAudience: String?,
    val styleDescription: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// AI 大纲生成响应
data class GeneratePlotResponse(
    val outlines: List<PlotOutlineResponse>
)

// AI 章节生成响应
data class GenerateContentResponse(
    val content: String,
    val wordCount: Int
)

// AI 修订内容响应
data class ReviseContentResponse(
    val revisedContent: String,
    val feedback: String,
    val changes: List<ChangeDetail>
)

// 单条修订变更说明
data class ChangeDetail(
    val type: String,
    val description: String
)

// AI 批量世界观生成响应
data class WorldBatchResult(
    val entries: List<WorldBuildingResponse>
)

// AI 推荐的世界观分类
data class SuggestedCategory(
    val category: String,
    val reason: String,
    val priority: Int
)

// 世界观一致性问题
data class WorldConsistencyIssue(
    val issue: String,
    val severity: String,
    val firstEntry: String,
    val secondEntry: String?,
    val suggestion: String
)

// 大纲审查结果
data class PlotReviewResult(
    val summary: String,
    val issues: List<PlotIssue>,
    val suggestions: List<String>,
    val pacing: PacingAnalysis?
)

// 单条大纲问题
data class PlotIssue(
    val type: String,
    val description: String,
    val severity: String,
    val outlineTitle: String?,
    val suggestion: String
)

// 节奏分析
data class PacingAnalysis(
    val overallRating: String,
    val slowParts: List<String>,
    val fastParts: List<String>,
    val recommendation: String
)

// 情节分支建议
data class PlotBranch(
    val title: String,
    val summary: String,
    val impact: String
)

// 角色弧线整合建议
data class CharacterArcSuggestion(
    val characterName: String,
    val currentArc: String,
    val suggestions: List<String>,
    val relatedOutlines: List<String>
)

// 伏笔审查结果
data class ForeshadowingReviewResult(
    val analysis: String,
    val suggestions: List<ForeshadowingSuggestion>
)

// 单条伏笔建议
data class ForeshadowingSuggestion(
    val foreshadowAt: String,
    val payoffAt: String,
    val hint: String,
    val importance: String
)

// ===== StoryBuilder 对话式创作 =====

// StoryBuilder 会话状态
data class StoryBuilderState(
    val novelId: Long,
    val currentStep: StoryStep,
    val stepIndex: Int,
    val totalSteps: Int,
    val chatHistory: List<StoryChatMessage>,
    val contextSummary: Map<String, String>,
    val completedSteps: List<StoryStep>,
    val suggestions: List<String> = emptyList()
) {
    // 创作进度百分比
    val progress: Double get() = stepIndex.toDouble() / totalSteps.toDouble()
}

// StoryBuilder 聊天消息
data class StoryChatMessage(
    val role: String,            // "user" | "assistant" | "system"
    val content: String,
    val options: List<String> = emptyList()
)

// StoryBuilder 响应
data class StoryBuilderResponse(
    val state: StoryBuilderState,
    val message: String,
    val options: List<String> = emptyList(),
    val canProceed: Boolean = false,
    val warning: String? = null
)

// 创作引导步骤枚举
enum class StoryStep(private val label: String, private val desc: String) {
    story_premise("故事梗概", "提供你的故事核心创意和初步构思"),
    genre_style("题材风格", "确定小说的类型、基调和叙事风格"),
    character_concept("角色构思", "设计核心角色及其关系"),
    world_concept("世界观框架", "构建虚构世界的基础设定"),
    plot_outline("情节大纲", "生成完整的故事章节大纲"),
    chapter_drafting("逐章创作", "分章节进行内容创作"),
    review_polish("审阅打磨", "整体检查和优化作品"),
    complete("完成", "创作完成");

    fun getLabel(): String = label
    fun getDescription(): String = desc
}
