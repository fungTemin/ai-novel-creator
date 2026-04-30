package com.novel.controller

import com.novel.dto.request.*
import com.novel.security.SecurityUtils
import com.novel.service.*
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

// AI 智能控制器：聚合所有 AI 驱动的小说创作接口，包括生成、审查、修订及世界观/大纲管理
@RestController
@RequestMapping("/api")
class AiController(
    private val aiService: AiService,
    private val worldBuildingService: WorldBuildingService,
    private val plotOutlineService: PlotOutlineService,
    private val styleSettingService: StyleSettingService,
    private val revisionService: RevisionService
) {

    // AI 生成大纲 POST /api/novels/{novelId}/generate/plot
    @PostMapping("/novels/{novelId}/generate/plot")
    fun generatePlot(
        @PathVariable novelId: Long,
        @RequestBody request: GeneratePlotRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(mapOf("outlines" to aiService.generatePlot(novelId, SecurityUtils.getUserId(auth), request)))

    // AI 生成角色 POST /api/novels/{novelId}/generate/character
    @PostMapping("/novels/{novelId}/generate/character")
    fun generateCharacter(
        @PathVariable novelId: Long,
        @RequestBody request: GenerateCharacterRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.generateCharacter(novelId, SecurityUtils.getUserId(auth), request))

    // AI 生成章节正文 POST /api/novels/{novelId}/generate/chapter
    @PostMapping("/novels/{novelId}/generate/chapter")
    fun generateChapter(
        @PathVariable novelId: Long,
        @RequestBody request: GenerateChapterRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.generateChapter(novelId, SecurityUtils.getUserId(auth), request))

    // AI 生成单条世界观设定 POST /api/novels/{novelId}/generate/world
    @PostMapping("/novels/{novelId}/generate/world")
    fun generateWorld(
        @PathVariable novelId: Long,
        @RequestBody request: GenerateWorldRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.generateWorld(novelId, SecurityUtils.getUserId(auth), request))

    // AI 批量生成世界观设定 POST /api/novels/{novelId}/generate/world/batch
    @PostMapping("/novels/{novelId}/generate/world/batch")
    fun generateWorldBatch(
        @PathVariable novelId: Long,
        @RequestBody request: GenerateWorldBatchRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.generateWorldBatch(novelId, SecurityUtils.getUserId(auth), request))

    // AI 推荐世界观分类 POST /api/novels/{novelId}/generate/world/suggest-categories
    @PostMapping("/novels/{novelId}/generate/world/suggest-categories")
    fun suggestWorldCategories(
        @PathVariable novelId: Long,
        @RequestBody request: SuggestWorldCategoriesRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.suggestWorldCategories(novelId, SecurityUtils.getUserId(auth), request))

    // AI 扩展世界观细节 POST /api/world-building/{id}/expand
    @PostMapping("/world-building/{id}/expand")
    fun expandWorldDetail(
        @PathVariable id: Long,
        @RequestBody request: ExpandWorldDetailRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.expandWorldDetail(worldBuildingService.findEntry(id).novel.id, SecurityUtils.getUserId(auth), request))

    // AI 审查世界观一致性 POST /api/novels/{novelId}/world-building/review-consistency
    @PostMapping("/novels/{novelId}/world-building/review-consistency")
    fun reviewWorldConsistency(
        @PathVariable novelId: Long,
        @RequestBody request: ReviewWorldConsistencyRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.reviewWorldConsistency(novelId, SecurityUtils.getUserId(auth), request))

    // AI 审查情节大纲 POST /api/novels/{novelId}/generate/plot/review
    @PostMapping("/novels/{novelId}/generate/plot/review")
    fun reviewPlot(
        @PathVariable novelId: Long,
        @RequestBody request: ReviewPlotRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.reviewPlot(novelId, SecurityUtils.getUserId(auth), request))

    // AI 扩展情节 POST /api/novels/{novelId}/generate/plot/expand
    @PostMapping("/novels/{novelId}/generate/plot/expand")
    fun expandPlot(
        @PathVariable novelId: Long,
        @RequestBody request: ExpandPlotRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.expandPlot(novelId, SecurityUtils.getUserId(auth), request))

    // AI 推荐情节分支 POST /api/novels/{novelId}/generate/plot/branches
    @PostMapping("/novels/{novelId}/generate/plot/branches")
    fun suggestPlotBranches(
        @PathVariable novelId: Long,
        @RequestBody request: SuggestPlotBranchesRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.suggestPlotBranches(novelId, SecurityUtils.getUserId(auth), request))

    // AI 整合角色弧线 POST /api/novels/{novelId}/generate/plot/character-arc
    @PostMapping("/novels/{novelId}/generate/plot/character-arc")
    fun integrateCharacterArc(
        @PathVariable novelId: Long,
        @RequestBody request: IntegrateCharacterArcRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.integrateCharacterArc(novelId, SecurityUtils.getUserId(auth), request))

    // AI 审查伏笔设计 POST /api/novels/{novelId}/generate/plot/foreshadowing
    @PostMapping("/novels/{novelId}/generate/plot/foreshadowing")
    fun reviewForeshadowing(
        @PathVariable novelId: Long,
        @RequestBody request: ReviewForeshadowingRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.reviewForeshadowing(novelId, SecurityUtils.getUserId(auth), request))

    // AI 修订章节内容 POST /api/chapters/{chapterId}/revise
    @PostMapping("/chapters/{chapterId}/revise")
    fun reviseChapter(
        @PathVariable chapterId: Long,
        @RequestBody request: ReviseChapterRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(aiService.reviseChapter(chapterId, SecurityUtils.getUserId(auth), request))

    // 获取世界观列表（支持按分类过滤） GET /api/novels/{novelId}/world-building
    @GetMapping("/novels/{novelId}/world-building")
    fun getWorldBuilding(
        @PathVariable novelId: Long,
        @RequestParam(required = false) category: String?,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(worldBuildingService.getAll(novelId, SecurityUtils.getUserId(auth), category))

    // 创建世界观条目 POST /api/novels/{novelId}/world-building
    @PostMapping("/novels/{novelId}/world-building")
    fun createWorldBuilding(
        @PathVariable novelId: Long,
        @RequestBody request: WorldBuildingRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(worldBuildingService.create(novelId, SecurityUtils.getUserId(auth), request))

    // 更新世界观条目 PUT /api/world-building/{id}
    @PutMapping("/world-building/{id}")
    fun updateWorldBuilding(
        @PathVariable id: Long,
        @RequestBody request: WorldBuildingRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(worldBuildingService.update(id, SecurityUtils.getUserId(auth), request))

    // 删除世界观条目 DELETE /api/world-building/{id}
    @DeleteMapping("/world-building/{id}")
    fun deleteWorldBuilding(@PathVariable id: Long, auth: Authentication): ResponseEntity<*> {
        worldBuildingService.delete(id, SecurityUtils.getUserId(auth))
        return ResponseUtils.message("删除成功")
    }

    // 获取大纲列表 GET /api/novels/{novelId}/outlines
    @GetMapping("/novels/{novelId}/outlines")
    fun getOutlines(@PathVariable novelId: Long, auth: Authentication): ResponseEntity<*> =
        ResponseUtils.ok(plotOutlineService.getAll(novelId, SecurityUtils.getUserId(auth)))

    // 创建大纲条目 POST /api/novels/{novelId}/outlines
    @PostMapping("/novels/{novelId}/outlines")
    fun createOutline(
        @PathVariable novelId: Long,
        @RequestBody request: PlotOutlineRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(plotOutlineService.create(novelId, SecurityUtils.getUserId(auth), request))

    // 更新大纲条目 PUT /api/outlines/{id}
    @PutMapping("/outlines/{id}")
    fun updateOutline(
        @PathVariable id: Long,
        @RequestBody request: PlotOutlineRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(plotOutlineService.update(id, SecurityUtils.getUserId(auth), request))

    // 删除大纲条目 DELETE /api/outlines/{id}
    @DeleteMapping("/outlines/{id}")
    fun deleteOutline(@PathVariable id: Long, auth: Authentication): ResponseEntity<*> {
        plotOutlineService.delete(id, SecurityUtils.getUserId(auth))
        return ResponseUtils.message("删除成功")
    }

    // 获取风格设置 GET /api/novels/{novelId}/style
    @GetMapping("/novels/{novelId}/style")
    fun getStyle(@PathVariable novelId: Long, auth: Authentication): ResponseEntity<*> =
        ResponseUtils.ok(styleSettingService.get(novelId, SecurityUtils.getUserId(auth)))

    // 更新风格设置 PUT /api/novels/{novelId}/style
    @PutMapping("/novels/{novelId}/style")
    fun updateStyle(
        @PathVariable novelId: Long,
        @RequestBody request: StyleSettingRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(styleSettingService.update(novelId, SecurityUtils.getUserId(auth), request))

    // 获取章节修订历史 GET /api/chapters/{chapterId}/revisions
    @GetMapping("/chapters/{chapterId}/revisions")
    fun getRevisions(@PathVariable chapterId: Long, auth: Authentication): ResponseEntity<*> =
        ResponseUtils.ok(revisionService.getRevisions(chapterId, SecurityUtils.getUserId(auth)))
}
