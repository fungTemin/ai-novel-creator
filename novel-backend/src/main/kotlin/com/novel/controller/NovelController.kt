package com.novel.controller

import com.novel.dto.request.NovelRequest
import com.novel.repository.UserRepository
import com.novel.security.SecurityUtils
import com.novel.service.NovelService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

// 小说控制器：提供小说的增删改查 RESTful 接口
@RestController
@RequestMapping("/api/novels")
class NovelController(
    private val novelService: NovelService,
    private val userRepository: UserRepository
) {

    // 获取当前用户所有小说 GET /api/novels
    @GetMapping
    fun getAll(auth: Authentication): ResponseEntity<*> {
        val novels = novelService.getNovelsByUser(SecurityUtils.getUserId(auth))
        return ResponseUtils.ok(novels)
    }

    // 获取单篇小说详情 GET /api/novels/{id}
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long, auth: Authentication): ResponseEntity<*> {
        val novel = novelService.getNovel(id, SecurityUtils.getUserId(auth))
        return ResponseUtils.ok(novel)
    }

    // 创建新小说 POST /api/novels
    @PostMapping
    fun create(@Valid @RequestBody request: NovelRequest, auth: Authentication): ResponseEntity<*> {
        val userId = SecurityUtils.getUserId(auth)
        val user = userRepository.findById(userId).orElseThrow()
        val novel = novelService.createNovel(user, request)
        return ResponseUtils.ok(novel)
    }

    // 更新小说信息 PUT /api/novels/{id}
    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: NovelRequest, auth: Authentication): ResponseEntity<*> {
        val novel = novelService.updateNovel(id, SecurityUtils.getUserId(auth), request)
        return ResponseUtils.ok(novel)
    }

    // 删除小说 DELETE /api/novels/{id}
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, auth: Authentication): ResponseEntity<*> {
        novelService.deleteNovel(id, SecurityUtils.getUserId(auth))
        return ResponseUtils.message("删除成功")
    }
}
