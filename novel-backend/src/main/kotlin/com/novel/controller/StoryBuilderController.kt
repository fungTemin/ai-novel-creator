package com.novel.controller

import com.novel.dto.request.StoryBuilderNextRequest
import com.novel.dto.request.StoryBuilderRequest
import com.novel.dto.response.ApiResponse
import com.novel.security.SecurityUtils
import com.novel.service.StoryBuilderService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

// 对话式故事构建控制器：提供分步骤引导创作的交互接口
@RestController
@RequestMapping("/api")
class StoryBuilderController(
    private val storyBuilderService: StoryBuilderService
) {

    // 启动创作引导会话 POST /api/novels/{novelId}/builder/start
    @PostMapping("/novels/{novelId}/builder/start")
    fun start(
        @PathVariable novelId: Long,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(
        storyBuilderService.start(novelId, SecurityUtils.getUserId(auth))
    )

    // 跳转到指定步骤 POST /api/novels/{novelId}/builder/step
    @PostMapping("/novels/{novelId}/builder/step")
    fun startStep(
        @PathVariable novelId: Long,
        @RequestBody request: StoryBuilderNextRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(
        storyBuilderService.startStep(novelId, SecurityUtils.getUserId(auth), request)
    )

    // 发送对话消息 POST /api/novels/{novelId}/builder/chat
    @PostMapping("/novels/{novelId}/builder/chat")
    fun chat(
        @PathVariable novelId: Long,
        @RequestBody request: StoryBuilderRequest,
        auth: Authentication
    ): ResponseEntity<*> = ResponseUtils.ok(
        storyBuilderService.chat(novelId, SecurityUtils.getUserId(auth), request)
    )
}
