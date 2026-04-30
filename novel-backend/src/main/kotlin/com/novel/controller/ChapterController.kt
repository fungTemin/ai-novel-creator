package com.novel.controller

import com.novel.dto.request.ChapterRequest
import com.novel.security.SecurityUtils
import com.novel.service.ChapterService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

// 章节控制器：提供章节的增删改查接口
@RestController
@RequestMapping("/api")
class ChapterController(
    private val chapterService: ChapterService
) {

    // 获取某小说的章节列表 GET /api/novels/{novelId}/chapters
    @GetMapping("/novels/{novelId}/chapters")
    fun getAll(@PathVariable novelId: Long, auth: Authentication): ResponseEntity<*> {
        val chapters = chapterService.getChapters(novelId, SecurityUtils.getUserId(auth))
        return ResponseUtils.ok(chapters)
    }

    // 获取单章详情 GET /api/chapters/{id}
    @GetMapping("/chapters/{id}")
    fun getOne(@PathVariable id: Long, auth: Authentication): ResponseEntity<*> {
        val chapter = chapterService.getChapter(id, SecurityUtils.getUserId(auth))
        return ResponseUtils.ok(chapter)
    }

    // 创建章节 POST /api/novels/{novelId}/chapters
    @PostMapping("/novels/{novelId}/chapters")
    fun create(
        @PathVariable novelId: Long,
        @Valid @RequestBody request: ChapterRequest,
        auth: Authentication
    ): ResponseEntity<*> {
        val chapter = chapterService.createChapter(novelId, SecurityUtils.getUserId(auth), request)
        return ResponseUtils.ok(chapter)
    }

    // 更新章节 PUT /api/chapters/{id}
    @PutMapping("/chapters/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: ChapterRequest,
        auth: Authentication
    ): ResponseEntity<*> {
        val chapter = chapterService.updateChapter(id, SecurityUtils.getUserId(auth), request)
        return ResponseUtils.ok(chapter)
    }

    // 删除章节 DELETE /api/chapters/{id}
    @DeleteMapping("/chapters/{id}")
    fun delete(@PathVariable id: Long, auth: Authentication): ResponseEntity<*> {
        chapterService.deleteChapter(id, SecurityUtils.getUserId(auth))
        return ResponseUtils.message("删除成功")
    }
}
