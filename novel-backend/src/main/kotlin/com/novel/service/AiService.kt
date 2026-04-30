package com.novel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.novel.ai.AiProvider
import com.novel.config.AiConfig
import com.novel.dto.request.*
import com.novel.dto.response.*
import com.novel.entity.Chapter
import com.novel.entity.Novel
import org.springframework.stereotype.Service

// AI 智能服务，统筹调用 AI 大模型完成小说创作中的各类任务
// 包含：大纲生成、角色生成、世界观管理、章节创作、内容修订、情节分析等
@Service
class AiService(
    private val aiConfig: AiConfig,
    private val deepSeekProvider: AiProvider,
    private val xiaoMiProvider: AiProvider,
    private val novelService: NovelService,
    private val chapterService: ChapterService,
    private val characterService: CharacterService,
    private val worldBuildingService: WorldBuildingService,
    private val plotOutlineService: PlotOutlineService,
    private val styleSettingService: StyleSettingService,
    private val objectMapper: ObjectMapper
) {

    // 获取当前默认的 AI 模型提供商（deepseek / xiaomi）
    private fun getProvider(): AiProvider {
        return when (aiConfig.defaultProvider.lowercase()) {
            "deepseek" -> deepSeekProvider
            "xiaomi" -> xiaoMiProvider
            else -> deepSeekProvider
        }
    }

    // ============================================================
    // AI 生成小说大纲：接收用户提示，调用 AI 生成多章节大纲并自动保存
    // ============================================================
    fun generatePlot(novelId: Long, userId: Long, request: GeneratePlotRequest): List<PlotOutlineResponse> {
        val novel = novelService.findOwnedNovel(novelId, userId)

        val systemPrompt = """
你是一位资深小说策划编辑，擅长构建引人入胜的故事大纲。

请根据以下要求生成小说大纲：
- 题材类型：${request.genre ?: novel.genre ?: "玄幻"}
- 故事简介：${request.prompt}
- 预计章节数：${request.chapterCount}

要求：
1. 每章需要有标题和简要摘要
2. 情节要有起承转合，节奏合理
3. 前后呼应，有伏笔设计
4. 角色成长弧线清晰

请以 JSON 格式返回，格式如下：
{
  "outlines": [
    {"title": "第一章 标题", "summary": "本章摘要"},
    {"title": "第二章 标题", "summary": "本章摘要"}
  ]
}
""".trimIndent()

        // 调用 AI 模型获取响应，解析 JSON 并逐个保存大纲条目
        val response = getProvider().generateCompletion(systemPrompt, "请为我生成小说大纲")
        try {
            val result: Map<String, List<Map<String, String>>> = objectMapper.readValue(extractJson(response))
            val outlines = result["outlines"] ?: emptyList()
            return outlines.mapIndexed { index, outline ->
                val saved = plotOutlineService.create(novelId, userId, PlotOutlineRequest(
                    title = outline["title"] ?: "第${index + 1}章",
                    summary = outline["summary"] ?: "",
                    orderIndex = index
                ))
                saved
            }
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的大纲格式无效: ${e.message}")
        }
    }

    // ============================================================
    // AI 生成角色：根据用户描述和已有角色上下文，创建新角色并自动保存
    // ============================================================
    fun generateCharacter(novelId: Long, userId: Long, request: GenerateCharacterRequest): CharacterResponse {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val existingChars = characterService.getCharacters(novelId, userId)
        val charContext = existingChars.joinToString("\n") { "- ${it.name}(${it.role ?: "未知角色"}): ${it.description ?: ""}" }

        val systemPrompt = """
你是一位经验丰富的小说角色设计师。

请创建一个角色：
- 角色类型：${request.role}
- 角色描述：${request.prompt}
- 所在小说：${novel.title}（${novel.description ?: ""}）
- 已有角色：${charContext.ifEmpty { "无" }}

要求：
1. 姓名要有特色，符合世界观
2. 外貌描写生动具体
3. 性格特征立体，有优缺点
4. 背景故事完整，与主线相关

请以 JSON 格式返回：
{
  "name": "角色名",
  "role": "角色类型",
  "description": "外貌描述",
  "personality": "性格特征",
  "background": "背景故事"
}
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请为我创建一个角色")
        return try {
            val charData: Map<String, String> = objectMapper.readValue(extractJson(response))
            characterService.createCharacter(novelId, userId, CharacterRequest(
                name = charData["name"] ?: "未命名",
                role = charData["role"] ?: request.role,
                description = charData["description"],
                personality = charData["personality"],
                background = charData["background"]
            ))
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的角色格式无效: ${e.message}")
        }
    }

        // 批量生成世界观条目：AI 根据分类要求一次性生成多条设定并保存
    fun generateWorldBatch(novelId: Long, userId: Long, request: GenerateWorldBatchRequest): List<WorldBuildingResponse> {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val existingAll = worldBuildingService.getAll(novelId, userId, null)
        val existingByCategory = existingAll.groupBy { it.category }

        val systemPrompt = """
你是一位想象力丰富的世界观架构师。

请为小说《${novel.title}》批量创建世界观设定。

【小说类型】${novel.genre ?: "玄幻"}
【小说简介】${novel.description ?: ""}
【故事构思】${request.prompt}

【要求生成的分类及每类条目数】
${request.categories.joinToString("\n") { "- $it：${request.countPerCategory}条" }}

【已有设定（避免重复）】
${existingAll.joinToString("\n") { "- [${it.category}] ${it.name}: ${it.description ?: ""}" }}

要求：
1. 每个分类下的条目互相独立且有区分度
2. 与已有设定不冲突、不重复
3. 设定要自洽、合理、有深度
4. 为后续创作留有扩展空间

请以 JSON 数组格式返回，格式如下：
[
  {"category": "分类名", "name": "条目名", "description": "描述", "details": {"key": "value"}},
  {"category": "分类名", "name": "条目名", "description": "描述", "details": {"key": "value"}}
]
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请批量生成世界观设定")
        return try {
            val entries: List<Map<String, Any>> = objectMapper.readValue(extractJson(response))
            entries.map { entry ->
                worldBuildingService.create(novelId, userId, WorldBuildingRequest(
                    category = entry["category"] as? String ?: "未分类",
                    name = entry["name"] as? String ?: "未命名",
                    description = entry["description"] as? String,
                    details = entry["details"]?.let { objectMapper.writeValueAsString(it) }
                ))
            }
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的批量世界观格式无效: ${e.message}")
        }
    }

    // 推荐世界观分类：AI 根据已有分类推荐还需要构建的世界观方向
    fun suggestWorldCategories(novelId: Long, userId: Long, request: SuggestWorldCategoriesRequest): List<SuggestedCategory> {
        novelService.findOwnedNovel(novelId, userId)
        val categories = request.existingCategories.ifEmpty {
            val existing = worldBuildingService.getAll(novelId, userId, null)
            existing.map { it.category }.distinct()
        }

        val systemPrompt = """
你是一位经验丰富的世界观构建顾问。

请为一部${request.genre.ifEmpty { "玄幻" }}类型的小说推荐需要构建的世界观分类。

【已有分类】
${categories.joinToString("\n") { "- $it" }}

请分析还需要哪些重要的世界观分类，根据重要性排列。

请以 JSON 数组格式返回：
[
  {"category": "分类英文名", "reason": "为什么需要这个分类", "priority": 1-10的数字},
  ...
]

注意：分类名用英文，如 geography, culture, magic_system, history, species, politics, economy, religion, technology, ecology 等
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请推荐世界观分类")
        return try {
            val result: List<Map<String, Any>> = objectMapper.readValue(extractJson(response))
            result.map {
                SuggestedCategory(
                    category = it["category"] as? String ?: "",
                    reason = it["reason"] as? String ?: "",
                    priority = (it["priority"] as? Number)?.toInt() ?: 5
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的分类建议格式无效: ${e.message}")
        }
    }

    // 扩展世界观条目细节：AI 在现有描述基础上增加深度和画面感
    fun expandWorldDetail(novelId: Long, userId: Long, request: ExpandWorldDetailRequest): WorldBuildingResponse {
        val existing = worldBuildingService.findEntry(request.worldId)
        if (existing.novel.user.id != userId) throw IllegalArgumentException("无权限")

        val novel = existing.novel
        val style = styleSettingService.get(novelId, userId)
        val allWorld = worldBuildingService.getAll(novelId, userId, null)
            .filter { it.id != request.worldId }
        val existingText = allWorld.joinToString("\n") { "- [${it.category}] ${it.name}: ${it.description ?: ""}" }

        val systemPrompt = """
你是一位擅长细节描写的世界观架构师。

请扩展以下世界观设定的细节。

【小说】${novel.title}（${novel.genre ?: "玄幻"}）
【设定名称】${existing.name}
【设定分类】${existing.category}
【当前描述】${existing.description ?: "无"}
【当前详情】${existing.details ?: "无"}

【扩展方向】${request.aspect}
【详细程度】${request.detailLevel}

【其他相关设定（避免冲突）】
${existingText.ifEmpty { "无" }}

请扩展此设定的细节，要求：
1. 在现有描述基础上增加深度
2. 与原设定不冲突
3. 与其他世界观设定保持自洽
4. 内容丰���、有画面感

请以 JSON 格式返回：
{
  "name": "设定名称（可保持不变或微调）",
  "description": "更新后的完整描述",
  "details": {
    "key1": "value1",
    "key2": "value2"
  }
}
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请扩展世界观细节")
        return try {
            val data: Map<String, Any> = objectMapper.readValue(extractJson(response))
            val updatedDesc = data["description"] as? String ?: existing.description
            val updatedDetails = data["details"]?.let { objectMapper.writeValueAsString(it) }
            worldBuildingService.update(request.worldId, userId, WorldBuildingRequest(
                category = existing.category,
                name = data["name"] as? String ?: existing.name,
                description = updatedDesc,
                details = updatedDetails ?: existing.details
            ))
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的扩展结果格式无效: ${e.message}")
        }
    }

    // 审查世界观一致性：AI 检查各设定间是否存在矛盾、逻辑冲突或不合理之处
    fun reviewWorldConsistency(novelId: Long, userId: Long, request: ReviewWorldConsistencyRequest): List<WorldConsistencyIssue> {
        novelService.findOwnedNovel(novelId, userId)
        val worlds = if (request.worldIds.isNotEmpty()) {
            request.worldIds.map { worldBuildingService.findEntry(it) }.filter { it.novel.user.id == userId }
        } else {
            worldBuildingService.getAll(novelId, userId, null).let { responses ->
                responses.map { resp -> worldBuildingService.findEntry(resp.id) }
            }
        }

        val worldText = worlds.joinToString("\n\n---\n\n") { w ->
            "[${w.category}] ${w.name}：${w.description ?: ""}\n详情：${w.details ?: "无"}"
        }

        val systemPrompt = """
你是一位严谨的世界观逻辑审查员。

请审查以下世界观设定，找出其中的矛盾、冲突或不合理之处。

【世界观设定列表】
$worldText

请仔细分析每条设定之间是否存在：
1. 事实矛盾（两条设定描述同一事物但互相矛盾）
2. 逻辑矛盾（设定本身不合逻辑）
3. 力量体系失衡（力量体系类设定中存在严重的战斗力不平衡）
4. 时间线冲突（历史类设定存在年代矛盾）

请以 JSON 数组格式返回（如无问题则返回空数组）：
[
  {
    "issue": "问题描述",
    "severity": "high/medium/low",
    "firstEntry": "第一个相关设定名",
    "secondEntry": "第二个相关设定名（如适用）",
    "suggestion": "修复建议"
  }
]
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请审查世界观一致性")
        return try {
            val result: List<Map<String, String>> = objectMapper.readValue(extractJson(response))
            result.map {
                WorldConsistencyIssue(
                    issue = it["issue"] ?: "",
                    severity = it["severity"] ?: "medium",
                    firstEntry = it["firstEntry"] ?: "",
                    secondEntry = it["secondEntry"],
                    suggestion = it["suggestion"] ?: ""
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的审查结果格式无效: ${e.message}")
        }
    }

        // 审查情节大纲：AI 分析大纲的连贯性、节奏、角色弧线等，返回问题和建议
    fun reviewPlot(novelId: Long, userId: Long, request: ReviewPlotRequest): PlotReviewResult {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val outlines = plotOutlineService.getAll(novelId, userId)
        val characters = characterService.getCharacters(novelId, userId)

        val outlineText = outlines.joinToString("\n") { "${it.orderIndex + 1}. ${it.title}：${it.summary ?: ""}" }
        val charText = characters.joinToString("\n") { "- ${it.name}(${it.role ?: "角色"}): ${it.personality ?: ""}" }

        val systemPrompt = """
你是一位资深小说编辑和情节分析师。

请审查以下小说大纲，${when (request.focus) {
    "consistency" -> "重点检查情节的一致性和逻辑性"
    "pacing" -> "重点分析节奏安排是否合理"
    "character" -> "重点检查角色成长弧线是否完整"
    else -> "进行全面审查"
}}。

【小说信息】
标题：${novel.title}（${novel.genre ?: "玄幻"}）
简介：${novel.description ?: ""}

【现有大纲】
$outlineText

【角色列表】
$charText

请以 JSON 格式返回分析结果：
{
  "summary": "总体评价",
  "issues": [
    {
      "type": "consistency/pacing/character/plot_hole/other",
      "description": "问题描述",
      "severity": "high/medium/low",
      "outlineTitle": "相关大纲标题（如有）",
      "suggestion": "改进建议"
    }
  ],
  "suggestions": ["建议1", "建议2", "..."],
  "pacing": {
    "overallRating": "优秀/良好/一般/需改进",
    "slowParts": ["节奏偏慢的部分"],
    "fastParts": ["节奏偏快的部分"],
    "recommendation": "节奏改进建议"
  }
}
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请审查大纲")
        return try {
            val data: Map<String, Any> = objectMapper.readValue(extractJson(response))

            @Suppress("UNCHECKED_CAST")
            val issues = (data["issues"] as? List<Map<String, String>>)?.map {
                PlotIssue(
                    type = it["type"] ?: "other",
                    description = it["description"] ?: "",
                    severity = it["severity"] ?: "medium",
                    outlineTitle = it["outlineTitle"],
                    suggestion = it["suggestion"] ?: ""
                )
            } ?: emptyList()

            val suggestions = (data["suggestions"] as? List<String>) ?: emptyList()

            @Suppress("UNCHECKED_CAST")
            val pacingMap = data["pacing"] as? Map<String, Any>
            val pacing = pacingMap?.let {
                PacingAnalysis(
                    overallRating = it["overallRating"] as? String ?: "一般",
                    slowParts = (it["slowParts"] as? List<String>) ?: emptyList(),
                    fastParts = (it["fastParts"] as? List<String>) ?: emptyList(),
                    recommendation = it["recommendation"] as? String ?: ""
                )
            }

            PlotReviewResult(
                summary = data["summary"] as? String ?: "",
                issues = issues,
                suggestions = suggestions,
                pacing = pacing
            )
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的审查结果格式无效: ${e.message}")
        }
    }

    // 扩展情节：在现有大纲基础上生成支线/过渡/高潮等补充情节
    fun expandPlot(novelId: Long, userId: Long, request: ExpandPlotRequest): List<PlotOutlineResponse> {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val allOutlines = plotOutlineService.getAll(novelId, userId)
        val existingText = allOutlines.joinToString("\n") { "${it.orderIndex + 1}. ${it.title}：${it.summary ?: ""}" }

        val targetOutline = request.outlineId?.let { id ->
            plotOutlineService.findOutline(id).let {
                if (it.novel.user.id != userId) throw IllegalArgumentException("无权限")
                it
            }
        }

        val systemPrompt = """
你是一位擅长情节设计的作家。

请为小说《${novel.title}》${if (targetOutline != null) "在大纲「${targetOutline.title}」的基础上" else "在现有大纲基础上"}生成${when (request.focus) {
    "subplot" -> "支线情节"
    "detail" -> "细致的情节展开"
    "transition" -> "过渡情节"
    "climax" -> "高潮情节"
    else -> "补充情节"
}}。

【小说类型】${novel.genre ?: "玄幻"}
【小说简介】${novel.description ?: ""}

【完整大纲】
$existingText

${if (targetOutline != null) "【需要展开的大纲】${targetOutline.orderIndex + 1}. ${targetOutline.title}：${targetOutline.summary ?: ""}" else ""}

要求：
1. 生成 ${request.depth} 个情节节点
2. 每个节点包含标题和详细描述
3. 与现有大纲自然衔接
4. ${when (request.focus) {
    "subplot" -> "支线情节要为主线服务，不喧宾夺主"
    "detail" -> "展开要有画面感和细节描写"
    "transition" -> "过渡要自然流畅，承上启下"
    "climax" -> "高潮要紧张刺激，有戏剧张力"
    else -> "情节要精彩且合理"
}}

请以 JSON 格式返回（保持原有大纲不变，只返回新增的条目）：
{
  "outlines": [
    {"title": "新章节标题", "summary": "详细描述"},
    {"title": "新章节标题", "summary": "详细描述"}
  ]
}
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请扩展情节")
        return try {
            val result: Map<String, List<Map<String, String>>> = objectMapper.readValue(extractJson(response))
            val newOutlines = result["outlines"] ?: emptyList()
            val startIndex = allOutlines.size
            newOutlines.mapIndexed { index, outline ->
                plotOutlineService.create(novelId, userId, PlotOutlineRequest(
                    title = outline["title"] ?: "新章节${index + 1}",
                    summary = outline["summary"] ?: "",
                    orderIndex = startIndex + index
                ))
            }
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的情节扩展格式无效: ${e.message}")
        }
    }

    // 推荐情节分支：在关键点提供多个不同的情节发展方向供作者选择
    fun suggestPlotBranches(novelId: Long, userId: Long, request: SuggestPlotBranchesRequest): List<PlotBranch> {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val allOutlines = plotOutlineService.getAll(novelId, userId)
        val existingText = allOutlines.joinToString("\n") { "${it.orderIndex + 1}. ${it.title}：${it.summary ?: ""}" }

        val keyPoint = if (request.keyPoint.isNotBlank()) request.keyPoint else {
            request.outlineId?.let { id ->
                val outline = plotOutlineService.findOutline(id)
                if (outline.novel.user.id != userId) throw IllegalArgumentException("无权限")
                outline.summary ?: outline.title
            } ?: "主要情节转折点"
        }

        val systemPrompt = """
你是一位擅长多线叙事的创意作家。

请为小说《${novel.title}》在以下关键点提供 ${request.branchCount} 个不同的情节发展方向。

【小说类型】${novel.genre ?: "玄幻"}
【小说简介】${novel.description ?: ""}

【现有大纲】
$existingText

【分支关键点】
$keyPoint

要求：
1. 每个分支方向要有明显的差异和特色
2. 每个分支都要逻辑自洽
3. 说明每个分支对后续剧情的影响

请以 JSON 格式返回：
[
  {
    "title": "分支标题",
    "summary": "分支情节描述（300字以内）",
    "impact": "对主线的影响和后续发展方向"
  }
]
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请提供情节分支")
        return try {
            val result: List<Map<String, String>> = objectMapper.readValue(extractJson(response))
            result.map {
                PlotBranch(
                    title = it["title"] ?: "",
                    summary = it["summary"] ?: "",
                    impact = it["impact"] ?: ""
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的分支建议格式无效: ${e.message}")
        }
    }

    // 整合角色弧线：分析角色如何更好地融入大纲，提出成长弧线建议
    fun integrateCharacterArc(novelId: Long, userId: Long, request: IntegrateCharacterArcRequest): List<CharacterArcSuggestion> {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val outlines = if (request.outlineIds.isNotEmpty()) {
            request.outlineIds.map { plotOutlineService.findOutline(it) }.filter { it.novel.user.id == userId }
        } else {
            plotOutlineService.getAll(novelId, userId).map { resp -> plotOutlineService.findOutline(resp.id) }
        }
        val characters = if (request.characterId != null) {
            listOf(characterService.findCharacter(request.characterId))
        } else {
            characterService.getCharacters(novelId, userId).map { resp -> characterService.findCharacter(resp.id) }
        }

        val outlineText = outlines.joinToString("\n") { "${it.orderIndex + 1}. ${it.title}：${it.summary ?: ""}" }
        val charText = characters.joinToString("\n\n---\n\n") { c ->
            "【${c.name}】角色类型：${c.role ?: "未指定"}\n性格：${c.personality ?: ""}\n背景：${c.background ?: ""}"
        }

        val systemPrompt = """
你是一位擅长角色塑造的叙事设计师。

请分析以下角色如何更好地融入小说《${novel.title}》的大纲，提出角色成长弧线建议。

【小说类型】${novel.genre ?: "玄幻"}

【相关大纲】
$outlineText

【角色档案】
$charText

要求：
1. 分析每个角色在当前大纲中的参与度
2. 建议每个角色在关键情节节点应出现的场景
3. 提出角色成长弧线的整合建议

请以 JSON 格式返回：
[
  {
    "characterName": "角色名",
    "currentArc": "当前角色弧线分析",
    "suggestions": ["建议1", "建议2", "..."],
    "relatedOutlines": ["涉及的大纲标题1", "大纲标题2"]
  }
]
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请进行角色弧线整合分析")
        return try {
            val result: List<Map<String, Any>> = objectMapper.readValue(extractJson(response))
            result.map {
                @Suppress("UNCHECKED_CAST")
                CharacterArcSuggestion(
                    characterName = it["characterName"] as? String ?: "",
                    currentArc = it["currentArc"] as? String ?: "",
                    suggestions = (it["suggestions"] as? List<String>) ?: emptyList(),
                    relatedOutlines = (it["relatedOutlines"] as? List<String>) ?: emptyList()
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的角色整合建议格式无效: ${e.message}")
        }
    }

    // 审查伏笔设计：分析大纲中的伏笔与回收关系，提出伏笔设计建议
    fun reviewForeshadowing(novelId: Long, userId: Long, request: ReviewForeshadowingRequest): ForeshadowingReviewResult {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val outlines = if (request.outlineIds.isNotEmpty()) {
            request.outlineIds.map { plotOutlineService.findOutline(it) }.filter { it.novel.user.id == userId }
        } else {
            plotOutlineService.getAll(novelId, userId).map { resp -> plotOutlineService.findOutline(resp.id) }
        }
        val characters = characterService.getCharacters(novelId, userId)
            .map { resp -> characterService.findCharacter(resp.id) }

        val outlineText = outlines.sortedBy { it.orderIndex }.joinToString("\n") { "${it.orderIndex + 1}. ${it.title}：${it.summary ?: ""}" }
        val charText = characters.joinToString("\n") { "- ${it.name}(${it.role ?: "角色"}): ${it.personality ?: ""}, ${it.background ?: ""}" }

        val systemPrompt = """
你是一位擅长设计伏笔和悬念的叙事大师。

请为小说《${novel.title}》的大纲分析伏笔设计，提出伏笔与回收建议。

【小说类型】${novel.genre ?: "玄幻"}

【当前大纲】
$outlineText

【角色列表】
$charText

请分析：
1. 现有情节中哪些可以设计伏笔
2. 哪些伏笔应该在何处回收
3. 建议新的伏笔设计

请以 JSON 格式返回：
{
  "analysis": "伏笔设计的整体分析与评价",
  "suggestions": [
    {
      "foreshadowAt": "应该在何处埋下伏笔（章节/情节点）",
      "payoffAt": "应该在何处回收/揭示",
      "hint": "伏笔的具体内容或线索",
      "importance": "high/medium/low"
    }
  ]
}
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请分析伏笔设计")
        return try {
            val data: Map<String, Any> = objectMapper.readValue(extractJson(response))

            @Suppress("UNCHECKED_CAST")
            val suggestions = (data["suggestions"] as? List<Map<String, String>>)?.map {
                ForeshadowingSuggestion(
                    foreshadowAt = it["foreshadowAt"] ?: "",
                    payoffAt = it["payoffAt"] ?: "",
                    hint = it["hint"] ?: "",
                    importance = it["importance"] ?: "medium"
                )
            } ?: emptyList()

            ForeshadowingReviewResult(
                analysis = data["analysis"] as? String ?: "",
                suggestions = suggestions
            )
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的伏笔分析格式无效: ${e.message}")
        }
    }

    // AI 生成章节正文：结合小说设定、风格要求、前文摘要等上下文信息，创作完整章节内容
    fun generateChapter(novelId: Long, userId: Long, request: GenerateChapterRequest): GenerateContentResponse {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val style = styleSettingService.get(novelId, userId)
        val characters = characterService.getCharacters(novelId, userId)
        val worldItems = worldBuildingService.getAll(novelId, userId, null)

        val chapter = request.chapterId?.let { chapterService.findChapter(it) }
        val previousChapters = chapterService.getChapters(novelId, userId)

        val prevSummary = previousChapters
            .filter { request.chapterId == null || it.chapterNumber < (chapter?.chapterNumber ?: 0) }
            .takeLast(3)
            .joinToString("\n") { "第${it.chapterNumber}章 ${it.title ?: ""}: ${it.summary ?: it.title ?: ""}" }

        val currentOutline = chapter?.summary ?: "待定"

        val systemPrompt = """
你是一位专业的网络小说作家，擅长${novel.genre ?: "玄幻"}类小说的创作。

【小说信息】
标题：${novel.title}
简介：${novel.description ?: ""}

【风格要求】
- 叙事视角：${style?.perspective ?: "第三人称"}
- 基调：${style?.tone ?: "严肃"}
- 目标读者：${style?.targetAudience ?: "普通读者"}
- 风格描述：${style?.styleDescription ?: "标准叙事"}

【章节信息】
章节号：第${chapter?.chapterNumber ?: (previousChapters.size + 1)}章
章节标题：${chapter?.title ?: "新章节"}
章节大纲：${currentOutline}

【前文摘要】
${prevSummary.ifEmpty { "无（这是开头章节）" }}

【角色列表】
${characters.joinToString("\n") { "- ${it.name}(${it.role ?: "角色"}): ${it.personality ?: ""}, ${it.background ?: ""}" }}

【世界观设定】
${worldItems.joinToString("\n") { "- [${it.category}] ${it.name}: ${it.description ?: ""}" }}

请根据以上信息撰写本章内容，要求：
1. 字数约 ${request.wordCount} 字
2. 情节紧凑，节奏流畅
3. 人物对话自然生动
4. 环境描写适当
5. 结尾留有悬念或承上启下

请直接返回章节正文内容，不要包含章节标题（如"第X章 XXX"）。
""".trimIndent()

        val content = getProvider().generateCompletion(systemPrompt, "请撰写本章内容")
        val cleanContent = cleanChapterContent(content)

        return GenerateContentResponse(
            content = cleanContent,
            wordCount = cleanContent.length
        )
    }

    // AI 创建单条世界观设定
    fun generateWorld(novelId: Long, userId: Long, request: GenerateWorldRequest): WorldBuildingResponse {
        novelService.findOwnedNovel(novelId, userId)
        val existingWorld = worldBuildingService.getAll(novelId, userId, request.category)
        val existingText = existingWorld.joinToString("\n") { "- ${it.name}: ${it.description ?: ""}" }

        val systemPrompt = """
你是一位想象力丰富的世界观架构师。

请为一部小说创建世界观设定：
- 分类：${request.category}
- 描述：${request.prompt}
- 已有设定：${existingText.ifEmpty { "暂无" }}

要求：
1. 设定要自洽、合理
2. 有足够的深度和细节
3. 与已有设定不冲突
4. 为后续创作留有扩展空间

请以 JSON 格式返回：
{
  "name": "设定名称",
  "category": "${request.category}",
  "description": "设定描述",
  "details": {
    "key1": "value1",
    "key2": "value2"
  }
}
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请为我创建设定")
        return try {
            val data: Map<String, Any> = objectMapper.readValue(extractJson(response))
            worldBuildingService.create(novelId, userId, WorldBuildingRequest(
                category = data["category"] as? String ?: request.category,
                name = data["name"] as? String ?: "未命名",
                description = data["description"] as? String,
                details = objectMapper.writeValueAsString(data["details"])
            ))
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的世界观格式无效: ${e.message}")
        }
    }

    // AI 修订章节内容：根据用户指定的修订要求，对选中文本进行润色和优化，返回修订结果和变更说明
    fun reviseChapter(chapterId: Long, userId: Long, request: ReviseChapterRequest): ReviseContentResponse {
        val chapter = chapterService.findChapter(chapterId)
        if (chapter.novel.user.id != userId) throw IllegalArgumentException("无权限")

        val novel = chapter.novel
        val style = styleSettingService.get(novel.id, userId)

        val textToRevise = request.selectedText ?: chapter.content ?: ""
        if (textToRevise.isBlank()) throw IllegalArgumentException("没有可修订的内容")

        val systemPrompt = """
你是一位资深小说编辑，正在审阅一部${novel.genre ?: "小说"}。

【小说风格】
- 基调：${style?.tone ?: "标准"}
- 视角：${style?.perspective ?: "第三人称"}
- 风格描述：${style?.styleDescription ?: "标准叙事"}

【修订要求】
${request.instructions}

【待修订内容】
${textToRevise}

请对以上内容进行修订，要求：
1. 保持原作的核心情节和人物性格
2. 根据修订要求进行针对性改进
3. 提升文字表达的文学性
4. 保持前后文的连贯性

请以 JSON 格式返回：
{
  "revised_content": "修订后的完整内容",
  "feedback": "简要说明做了哪些改动",
  "changes": [
    {"type": "修改类型（enhancement/simplification/emotional/correction等）", "description": "具体改动说明"}
  ]
}
""".trimIndent()

        val response = getProvider().generateCompletion(systemPrompt, "请修订以上内容")
        return try {
            val data: Map<String, Any> = objectMapper.readValue(extractJson(response))
            val revisedContent = data["revised_content"] as? String ?: throw RuntimeException("没有修订内容")
            val feedback = data["feedback"] as? String ?: ""

            @Suppress("UNCHECKED_CAST")
            val changes = (data["changes"] as? List<Map<String, String>>)?.map {
                ChangeDetail(type = it["type"] ?: "", description = it["description"] ?: "")
            } ?: emptyList()

            ReviseContentResponse(
                revisedContent = revisedContent,
                feedback = feedback,
                changes = changes
            )
        } catch (e: Exception) {
            throw RuntimeException("AI 返回的修订结果格式无效: ${e.message}")
        }
    }

    // 清理 AI 返回的章节正文：去除 AI 自动添加的章节标题（如"第一章 XXX"）和 Markdown 标题
    private fun cleanChapterContent(content: String): String {
        var cleaned = content.trim()
        cleaned = cleaned.replace(Regex("^第[一二三四五六七八九十百千]+章[\\s\\S]*?[\n\r]+"), "")
        cleaned = cleaned.replace(Regex("^Chapter\\s+\\d+[\\s\\S]*?[\n\r]+", RegexOption.IGNORE_CASE), "")
        cleaned = cleaned.replace(Regex("^#{1,3}\\s.*[\n\r]+"), "")
        return cleaned.trim()
    }

    // 从 AI 返回的文本中提取 JSON 内容（支持 {} 对象和 [] 数组两种格式）
    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1)
        }

        val startArr = text.indexOf('[')
        val endArr = text.lastIndexOf(']')
        if (startArr >= 0 && endArr > startArr) {
            return text.substring(startArr, endArr + 1)
        }

        return text
    }
}
