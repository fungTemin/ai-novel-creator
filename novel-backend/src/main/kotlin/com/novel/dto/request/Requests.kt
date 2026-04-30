package com.novel.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// 注册请求 DTO
data class RegisterRequest(
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    val username: String,

    @field:NotBlank(message = "邮箱不能为空")
    val email: String,

    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 6, message = "密码长度不能少于6位")
    val password: String
)

// 登录请求 DTO
data class LoginRequest(
    @field:NotBlank(message = "用户名不能为空")
    val username: String,

    @field:NotBlank(message = "密码不能为空")
    val password: String
)

// 小说创建/更新请求 DTO
data class NovelRequest(
    @field:NotBlank(message = "标题不能为空")
    @field:Size(max = 200, message = "标题长度不能超过200")
    val title: String,
    val description: String? = null,
    val genre: String? = null
)

// 章节创建/更新请求 DTO
data class ChapterRequest(
    val chapterNumber: Int? = null,
    val title: String? = null,
    val content: String? = null,
    val summary: String? = null
)

// 角色创建/更新请求 DTO
data class CharacterRequest(
    @field:NotBlank(message = "角色名不能为空")
    val name: String,
    val role: String? = null,
    val description: String? = null,
    val personality: String? = null,
    val background: String? = null,
    val relationships: String? = null
)

// 世界观条目创建/更新请求 DTO
data class WorldBuildingRequest(
    @field:NotBlank(message = "分类不能为空")
    val category: String,
    @field:NotBlank(message = "名称不能为空")
    val name: String,
    val description: String? = null,
    val details: String? = null
)

// 情节大纲条目创建/更新请求 DTO
data class PlotOutlineRequest(
    @field:NotBlank(message = "标题不能为空")
    val title: String,
    val summary: String? = null,
    val orderIndex: Int = 0
)

// 风格设置请求 DTO
data class StyleSettingRequest(
    val tone: String? = null,
    val perspective: String? = null,
    val targetAudience: String? = null,
    val styleDescription: String? = null
)

// AI 生成大纲请求 DTO
data class GeneratePlotRequest(
    val prompt: String = "",
    val chapterCount: Int = 20,
    val genre: String? = null
)

// AI 生成角色请求 DTO
data class GenerateCharacterRequest(
    val prompt: String = "",
    val role: String = "supporting"
)

// AI 生成章节请求 DTO
data class GenerateChapterRequest(
    val chapterId: Long? = null,
    val instructions: String = "",
    val wordCount: Int = 3000
)

// AI 创建世界观请求 DTO
data class GenerateWorldRequest(
    val category: String = "",
    val prompt: String = "",
    val detailLevel: String = "detailed"
)

// AI 批量生成世界观请求 DTO
data class GenerateWorldBatchRequest(
    val categories: List<String> = emptyList(),
    val prompt: String = "",
    val countPerCategory: Int = 3
)

// AI 推荐世界观分类请求 DTO
data class SuggestWorldCategoriesRequest(
    val genre: String = "",
    val existingCategories: List<String> = emptyList()
)

// AI 扩展世界观细节请求 DTO
data class ExpandWorldDetailRequest(
    val worldId: Long,
    val aspect: String = "",
    val detailLevel: String = "detailed"
)

// AI 审查世界观一致性请求 DTO
data class ReviewWorldConsistencyRequest(
    val worldIds: List<Long> = emptyList()
)

// AI 审查情节大纲请求 DTO
data class ReviewPlotRequest(
    val focus: String = "consistency"
)

// AI 扩展情节请求 DTO
data class ExpandPlotRequest(
    val outlineId: Long? = null,
    val focus: String = "subplot",
    val depth: Int = 1
)

// AI 推荐情节分支请求 DTO
data class SuggestPlotBranchesRequest(
    val outlineId: Long? = null,
    val keyPoint: String = "",
    val branchCount: Int = 3
)

// AI 整合角色弧线请求 DTO
data class IntegrateCharacterArcRequest(
    val outlineIds: List<Long> = emptyList(),
    val characterId: Long? = null
)

// AI 审查伏笔设计请求 DTO
data class ReviewForeshadowingRequest(
    val outlineIds: List<Long> = emptyList()
)

// AI 修订章节请求 DTO
data class ReviseChapterRequest(
    val selectedText: String? = null,
    val instructions: String = "",
    val style: String = "enhance"
)

// ===== StoryBuilder 对话式创作 =====

// StoryBuilder 对话消息请求
data class StoryBuilderRequest(
    val novelId: Long,
    val message: String = "",
    val action: String = "continue"  // continue / restart / back / finish
)

// StoryBuilder 跳转步骤请求
data class StoryBuilderNextRequest(
    val novelId: Long,
    val step: String,           // story_premise, genre_style 等步骤名
    val action: String = "start"
)
