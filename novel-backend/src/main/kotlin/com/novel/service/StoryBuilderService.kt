package com.novel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.novel.config.AiConfig
import com.novel.dto.request.*
import com.novel.dto.response.*
import com.novel.entity.Novel
import com.novel.repository.NovelRepository
// 引入 Spring AI 的 ChatModel 接口和 Prompt 构建类
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

// 对话式故事构建服务：通过分步骤引导式对话，协助作者从梗概到成稿完成整部小说创作
// 支持：故事梗概 → 题材风格 → 角色构思 → 世界观框架 → 情节大纲 → 逐章创作 → 审阅打磨 → 完成
@Service
class StoryBuilderService(
    private val aiConfig: AiConfig,
    @Qualifier("deepSeekChatModel") private val deepSeekChatModel: ChatModel,
    @Qualifier("xiaoMiChatModel") private val xiaoMiChatModel: ChatModel,
    private val novelService: NovelService,
    private val characterService: CharacterService,
    private val worldBuildingService: WorldBuildingService,
    private val plotOutlineService: PlotOutlineService,
    private val styleSettingService: StyleSettingService,
    private val chapterService: ChapterService,
    private val novelRepository: NovelRepository,
    private val objectMapper: ObjectMapper
) {
    // 会话缓存：进程内存储各用户的创作会话状态（key = "$userId:$novelId"）
    private val sessions = ConcurrentHashMap<String, SessionData>()

    // 总步骤数（排除 StoryStep.complete 完成步骤）
    private val totalSteps = StoryStep.entries.size - 1

    // 会话数据：记录当前进度、上下文摘要、聊天历史和已完成步骤
    private data class SessionData(
        val novelId: Long,
        var currentStep: StoryStep = StoryStep.story_premise,
        val contextSummary: MutableMap<String, String> = mutableMapOf(),
        val chatHistory: MutableList<StoryChatMessage> = mutableListOf(),
        val completedSteps: MutableList<StoryStep> = mutableListOf()
    )

    // 获取当前默认 AI 模型提供商对应的 Spring AI ChatModel
    private fun getChatModel(): ChatModel {
        return when (aiConfig.defaultProvider.lowercase()) {
            "deepseek" -> deepSeekChatModel
            "xiaomi" -> xiaoMiChatModel
            else -> deepSeekChatModel
        }
    }

    // 通过 Spring AI ChatModel 统一调用 AI，传入系统提示和用户消息，返回 AI 生成的文本
    private fun callModel(systemPrompt: String, userMessage: String): String {
        val chatModel = getChatModel()
        val prompt = Prompt(listOf(SystemMessage(systemPrompt), UserMessage(userMessage)))
        val response = chatModel.call(prompt)
        return response.result.output?.text ?: ""
    }

    // 生成会话缓存键
    private fun sessionKey(novelId: Long, userId: Long) = "$userId:$novelId"

    // 启动创作引导会话，返回欢迎消息和初始提示
    fun start(novelId: Long, userId: Long): StoryBuilderResponse {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val key = sessionKey(novelId, userId)
        val session = SessionData(novelId = novelId)
        sessions[key] = session
        return buildStepResponse(session, "欢迎来到 AI 创作助手！我将引导你一步步完成小说的创作。\n\n首先，请告诉我你的故事梗概：你有一个什么样的故事想法？主角是谁？故事发生在什么样的世界？")
    }

    // 跳转到指定步骤（如果该步骤已完成，则直接恢复；否则重新开始该步骤）
    fun startStep(novelId: Long, userId: Long, request: StoryBuilderNextRequest): StoryBuilderResponse {
        val key = sessionKey(novelId, userId)
        val session = sessions[key] ?: return start(novelId, userId)
        val step = try { StoryStep.valueOf(request.step) } catch (e: Exception) { StoryStep.story_premise }

        if (session.completedSteps.contains(step)) {
            session.currentStep = step
        }

        return when (step) {
            StoryStep.story_premise -> handleStoryPremise(session, userId, "", true)
            StoryStep.genre_style -> handleGenreStyle(session, userId)
            StoryStep.character_concept -> handleCharacterConcept(session, userId)
            StoryStep.world_concept -> handleWorldConcept(session, userId)
            StoryStep.plot_outline -> handlePlotOutline(session, userId)
            StoryStep.chapter_drafting -> handleChapterDrafting(session, userId)
            StoryStep.review_polish -> handleReviewPolish(session, userId)
            StoryStep.complete -> handleComplete(session, userId)
        }
    }

    // 处理用户对话：支持 restart（重新开始）、back（上一步）、finish（完成）等动作
    fun chat(novelId: Long, userId: Long, request: StoryBuilderRequest): StoryBuilderResponse {
        val key = sessionKey(novelId, userId)
        val session = sessions[key] ?: return start(novelId, userId)

        when (request.action) {
            "restart" -> return restart(novelId, userId)
            "back" -> return goBack(session, userId)
            "finish" -> return handleComplete(session, userId)
        }

        session.chatHistory.add(StoryChatMessage(role = "user", content = request.message))

        return when (session.currentStep) {
            StoryStep.story_premise -> handleStoryPremise(session, userId, request.message, false)
            StoryStep.genre_style -> handleGenreStyle(session, userId)
            StoryStep.character_concept -> handleCharacterConcept(session, userId)
            StoryStep.world_concept -> handleWorldConcept(session, userId)
            StoryStep.plot_outline -> handlePlotOutline(session, userId)
            StoryStep.chapter_drafting -> handleChapterDrafting(session, userId)
            StoryStep.review_polish -> handleReviewPolish(session, userId)
            StoryStep.complete -> handleComplete(session, userId)
        }
    }

    // ============================================================
    // 以下为各步骤的处理器方法，每个方法负责该步骤的 AI 对话逻辑
    // ============================================================

    // 第一步：故事梗概 — 收集并审视用户的故事创意，提供改进建议和引导问题
    private fun handleStoryPremise(session: SessionData, userId: Long, message: String, isStart: Boolean): StoryBuilderResponse {
        if (isStart) {
            session.currentStep = StoryStep.story_premise
            return buildStepResponse(session, "## 📝 故事梗概\n\n请描述你的故事创意，包括以下要素：\n\n- **故事背景**：发生在什么时代/世界？\n- **主角**：主角是什么样的人？\n- **核心冲突**：故事的主要矛盾和驱动力是什么？\n- **独特卖点**：这个故事有什么特别之处？\n\n> 你可以从一句话梗概开始，也可以写一段详细描述。")
        }

        val systemPrompt = """
你是一位富有创意的小说编辑，正在帮助作者完善故事梗概。

【作者当前的想法】
$message

请从以下方面给予反馈和建议：
1. 故事核心是否清晰、有吸引力
2. 主角设定是否有辨识度
3. 冲突是否足够强烈
4. 建议如何改进和完善

要求：
- 给出具体的改进建议
- 提出 2-3 个引导性问题，帮助作者深化想法
- 语气鼓励、专业
- 如果梗概已经比较完整，给出肯定并询问是否继续前进
""".trimIndent()

        val aiResponse = callModel(systemPrompt, "请给作者提供反馈和引导")
        session.contextSummary["story_premise"] = message

        val canProceed = message.length > 30
        return buildStepResponse(session, aiResponse, canProceed = canProceed)
    }

    // 第二步：题材风格 — 根据故事梗概推荐最适合的题材类型、叙事基调和视角
    private fun handleGenreStyle(session: SessionData, userId: Long): StoryBuilderResponse {
        session.currentStep = StoryStep.genre_style
        val premise = session.contextSummary["story_premise"] ?: "未提供"
        val existing = session.contextSummary["genre_style"] ?: ""

        if (existing.isNotBlank()) {
            return buildStepResponse(session, existing, canProceed = true)
        }

        val systemPrompt = """
你是一位了解所有文学类型的小说策划师。

【故事梗概】
$premise

请根据上面的梗概，给作者推荐最适合的创作方向：

1. **题材类型**：玄幻/科幻/都市/历史/悬疑/言情等，并说明为什么适合
2. **叙事基调**：严肃正剧/轻松幽默/暗黑深沉/温暖治愈等
3. **叙事视角**：第一人称/第三人称/多视角
4. **目标读者**：适合什么样的读者群体

请以友好、建议的口吻给出推荐，并解释每个推荐的理由。最后询问作者是否认同这些建议，或者是否有自己的想法。
""".trimIndent()

        val response = callModel(systemPrompt, "请推荐题材和风格")
        session.contextSummary["genre_style"] = response
        return buildStepResponse(session, response, canProceed = true)
    }

    // 第三步：角色构思 — 根据故事设计核心角色阵容，分析已有角色并提供完善建议
    private fun handleCharacterConcept(session: SessionData, userId: Long): StoryBuilderResponse {
        session.currentStep = StoryStep.character_concept
        val premise = session.contextSummary["story_premise"] ?: "未提供"
        val genreStyle = session.contextSummary["genre_style"] ?: "未设定"
        val existing = session.contextSummary["character_concept"] ?: ""
        val existingChars = characterService.getCharacters(session.novelId, userId)

        if (existing.isNotBlank()) {
            return buildStepResponse(session, existing, canProceed = true)
        }

        val existingText = existingChars.joinToString("\n") { "- ${it.name}(${it.role ?: "角色"}): ${it.description ?: ""} ${it.personality ?: ""}" }

        val systemPrompt = """
你是一位擅长角色设计的作家。

【故事梗概】
$premise

【题材风格】
$genreStyle

【已有角色】
${existingText.ifEmpty { "暂无" }}

请为这个故事设计核心角色阵容，包括：

1. **主角**：姓名、性格特点、背景故事、成长潜力
2. **重要配角**：伙伴、导师、恋人等
3. **反派/对手**：冲突的源泉

对于每个角色，请提供：
- 角色名
- 一句话角色定位
- 核心性格特征
- 在故事中的作用

如果有已有角色，请补充和完善。最后问作者希望手动创建、让 AI 生成具体角色档案，还是继续下一步。
""".trimIndent()

        val response = callModel(systemPrompt, "请设计角色阵容")
        session.contextSummary["character_concept"] = response
        return buildStepResponse(session, response, canProceed = true,
            options = listOf("让 AI 生成完整角色档案", "自己手动添加角色", "继续下一步"))
    }

    // 第四步：世界观框架 — 构建小说的世界观设定框架（力量体系、地理、社会结构等）
    private fun handleWorldConcept(session: SessionData, userId: Long): StoryBuilderResponse {
        session.currentStep = StoryStep.world_concept
        val premise = session.contextSummary["story_premise"] ?: "未提供"
        val genreStyle = session.contextSummary["genre_style"] ?: "未设定"
        val characters = session.contextSummary["character_concept"] ?: "未设定"
        val existing = session.contextSummary["world_concept"] ?: ""
        val existingWorld = worldBuildingService.getAll(session.novelId, userId, null)

        if (existing.isNotBlank()) {
            return buildStepResponse(session, existing, canProceed = true)
        }

        val existingText = existingWorld.joinToString("\n") { "- [${it.category}] ${it.name}: ${it.description ?: ""}" }

        val systemPrompt = """
你是一位富有想象力的世界观架构师。

【故事梗概】
$premise

【题材风格】
$genreStyle

【角色设定】
$characters

【已有世界观】
${existingText.ifEmpty { "暂无" }}

请为这个故事构建世界观框架，包括以下方面的建议：

1. **力量体系**（如适用）：修炼/魔法/科技/超能力等
2. **地理环境**：主要场景和地域特色
3. **社会结构**：政治体系、文化习俗、种族构成
4. **历史背景**：重要的历史事件和传说
5. **特殊规则**：这个世界独有的规则和限制

请给出框架性建议，不需要过于详细。最后询问作者是否要 AI 生成具体设定，或自己补充。
""".trimIndent()

        val response = callModel(systemPrompt, "请构建世界观框架")
        session.contextSummary["world_concept"] = response
        return buildStepResponse(session, response, canProceed = true,
            options = listOf("AI 生成详细设定", "自己手动添加设定", "继续下一步"))
    }

    // 第五步：情节大纲 — 根据已有设定生成完整章节大纲，并自动保存到数据库
    private fun handlePlotOutline(session: SessionData, userId: Long): StoryBuilderResponse {
        session.currentStep = StoryStep.plot_outline
        val premise = session.contextSummary["story_premise"] ?: "未提供"
        val genreStyle = session.contextSummary["genre_style"] ?: "未设定"
        val characters = session.contextSummary["character_concept"] ?: "未设定"
        val world = session.contextSummary["world_concept"] ?: "未设定"
        val existing = session.contextSummary["plot_outline"] ?: ""
        val existingOutlines = plotOutlineService.getAll(session.novelId, userId)

        if (existingOutlines.isNotEmpty()) {
            return buildStepResponse(session, "现有大纲共 ${existingOutlines.size} 条：\n" +
                existingOutlines.joinToString("\n") { "- ${it.title}: ${it.summary ?: ""}" } +
                "\n\n你可以让 AI 重新生成大纲，或直接开始逐章创作。",
                canProceed = true,
                options = listOf("重新生成大纲", "开始逐章创作"))
        }

        val systemPrompt = """
你是一位资深小说策划编辑。

请根据以下所有信息生成一份完整的章节大纲。

【故事梗概】
$premise

【题材风格】
$genreStyle

【角色设定】
$characters

【世界观框架】
$world

请生成 15-25 章的大纲，要求：
1. 每章有标题和 1-2 句话的摘要
2. 情节有起承转合，节奏合理
3. 角色成长弧线完整
4. 前后呼应，有伏笔和回收
5. 结尾有力

以 JSON 格式输出：
{
  "outlines": [
    {"title": "第一章 标题", "summary": "本章摘要"},
    {"title": "第二章 标题", "summary": "本章摘要"}
  ]
}

生成后请以友好的方式展示给作者，并询问是否满意或需要调整。
""".trimIndent()

        val response = callModel(systemPrompt, "请生成完整大纲")
        session.contextSummary["plot_outline"] = response

        try {
            val json = response.substringAfter("{").substringBeforeLast("}")
            val result: Map<String, List<Map<String, String>>> = objectMapper.readValue("{$json}")
            val outlines = result["outlines"] ?: emptyList()
            outlines.forEachIndexed { index, outline ->
                try {
                    plotOutlineService.create(session.novelId, userId, PlotOutlineRequest(
                        title = outline["title"] ?: "第${index + 1}章",
                        summary = outline["summary"] ?: "",
                        orderIndex = index
                    ))
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        val savedCount = plotOutlineService.getAll(session.novelId, userId).size
        return buildStepResponse(session, response + "\n\n---\n✅ 已自动保存 ${savedCount} 条大纲。你可以继续调整，或进入逐章创作。",
            canProceed = true,
            options = listOf("调整大纲", "开始逐章创作"))
    }

    // 第六步：逐章创作 — 展示已有章节状态和大纲，引导用户选择要创作的章节
    private fun handleChapterDrafting(session: SessionData, userId: Long): StoryBuilderResponse {
        session.currentStep = StoryStep.chapter_drafting
        val chapters = chapterService.getChapters(session.novelId, userId)
        val outlines = plotOutlineService.getAll(session.novelId, userId)
        val outlineText = outlines.joinToString("\n") { "${it.orderIndex + 1}. ${it.title}" }

        val chapterList = chapters.joinToString("\n") { "第${it.chapterNumber}章 ${it.title ?: "未命名"}（${it.wordCount}字）" }
        val chapterPrompt = chapters.firstOrNull()?.let {
            "目前已有 ${chapters.size} 章内容，可以继续创作新章节或修改已有章节。"
        } ?: "目前还没有写任何章节。大纲已就绪，可以开始创作了！"

        return buildStepResponse(session,
            "## ✍️ 逐章创作\n\n$chapterPrompt\n\n" +
            if (outlineText.isNotBlank()) "### 现有大纲\n$outlineText\n\n" else "" +
            if (chapterList.isNotBlank()) "### 已创作\n$chapterList\n\n" else "" +
            "请告诉我你想写哪一章？（例如：\"写第一章\"或\"继续写第三章\"）" +
            "\n\n你也可以描述这一章的具体内容要求，AI 会帮你生成。",
            canProceed = true,
            options = listOf("写第一章", "继续写下一章", "修改已有章节", "进入审阅打磨"))
    }

    // 第七步：审阅打磨 — 展示创作进度，提供整体审查、单章修订和一致性检查的入口
    private fun handleReviewPolish(session: SessionData, userId: Long): StoryBuilderResponse {
        session.currentStep = StoryStep.review_polish
        val chapters = chapterService.getChapters(session.novelId, userId)
        val outlines = plotOutlineService.getAll(session.novelId, userId)
        val totalWords = chapters.sumOf { it.wordCount }

        val chapterStatus = chapters.joinToString("\n") {
            "第${it.chapterNumber}章「${it.title ?: "未命名"}」 — ${it.wordCount}字 — ${it.status}"
        }

        return buildStepResponse(session,
            "## 🔍 审阅打磨\n\n" +
            "**当前进度**\n" +
            "- 大纲：${outlines.size} 条\n" +
            "- 章节：${chapters.size} 章\n" +
            "- 总字数：$totalWords 字\n\n" +
            "### 章节状态\n$chapterStatus\n\n" +
            "在这一步你可以：\n" +
            "1. **整体审查** — 让 AI 检查故事的连贯性和节奏\n" +
            "2. **单章修订** — 对特定章节进行润色和优化\n" +
            "3. **一致性检查** — 检查设定和角色的前后一致\n\n" +
            "请告诉我你想要做什么？",
            canProceed = true,
            options = listOf("整体审查", "修订单章内容", "一致性检查", "完成创作"))
    }

    // 第八步：完成 — 展示创作成果统计，提供后续操作建议
    private fun handleComplete(session: SessionData, userId: Long): StoryBuilderResponse {
        session.currentStep = StoryStep.complete
        val chapters = chapterService.getChapters(session.novelId, userId)
        val totalWords = chapters.sumOf { it.wordCount }

        session.completedSteps.add(StoryStep.complete)

        return buildStepResponse(session,
            "## 🎉 创作完成！\n\n" +
            "恭喜你完成了小说的初步创作！以下是你的创作成果：\n\n" +
            "- 📚 总章节数：${chapters.size} 章\n" +
            "- 📝 总字数：$totalWords 字\n" +
            "- 📋 大纲条目：${plotOutlineService.getAll(session.novelId, userId).size} 条\n\n" +
            "你可以随时回到之前的步骤进行调整，也可以继续写新的章节。\n\n" +
            "**后续可以做的事情：**\n" +
            "- 导出小说（Markdown/TXT）\n" +
            "- 继续写更多章节\n" +
            "- 对特定章节进行 AI 修订\n" +
            "- 调整世界观和角色设定",
            canProceed = true,
            options = listOf("回到梗概", "导出小说", "继续写章节"))
    }

    // 返回上一步骤
    private fun goBack(session: SessionData, userId: Long): StoryBuilderResponse {
        val prevStep = when (session.currentStep) {
            StoryStep.genre_style -> StoryStep.story_premise
            StoryStep.character_concept -> StoryStep.genre_style
            StoryStep.world_concept -> StoryStep.character_concept
            StoryStep.plot_outline -> StoryStep.world_concept
            StoryStep.chapter_drafting -> StoryStep.plot_outline
            StoryStep.review_polish -> StoryStep.chapter_drafting
            StoryStep.complete -> StoryStep.review_polish
            else -> StoryStep.story_premise
        }
        session.currentStep = prevStep
        return buildStepResponse(session, "已回到「${prevStep.getLabel()}」步骤。")
    }

    // 重新开始：清除会话缓存，从头开始创作引导
    private fun restart(novelId: Long, userId: Long): StoryBuilderResponse {
        sessions.remove(sessionKey(novelId, userId))
        return start(novelId, userId)
    }

    // 构建步骤响应：记录 AI 回复到聊天历史中，组装完整的 StoryBuilderResponse
    private fun buildStepResponse(
        session: SessionData,
        message: String,
        canProceed: Boolean = false,
        options: List<String> = emptyList()
    ): StoryBuilderResponse {
        val msg = StoryChatMessage(role = "assistant", content = message, options = options)
        session.chatHistory.add(msg)

        return StoryBuilderResponse(
            state = StoryBuilderState(
                novelId = session.novelId,
                currentStep = session.currentStep,
                stepIndex = session.currentStep.ordinal + 1,
                totalSteps = totalSteps,
                chatHistory = session.chatHistory.toList(),
                contextSummary = session.contextSummary.toMap(),
                completedSteps = session.completedSteps.toList(),
                suggestions = options
            ),
            message = message,
            options = options,
            canProceed = canProceed
        )
    }
}
