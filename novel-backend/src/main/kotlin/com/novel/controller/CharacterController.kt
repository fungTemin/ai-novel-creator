package com.novel.controller

import com.novel.dto.request.CharacterRequest
import com.novel.security.SecurityUtils
import com.novel.service.CharacterService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

// 角色控制器：提供小说角色的增删改查接口
@RestController
@RequestMapping("/api")
class CharacterController(
    private val characterService: CharacterService
) {

    // 获取某小说的角色列表 GET /api/novels/{novelId}/characters
    @GetMapping("/novels/{novelId}/characters")
    fun getAll(@PathVariable novelId: Long, auth: Authentication): ResponseEntity<*> {
        val chars = characterService.getCharacters(novelId, SecurityUtils.getUserId(auth))
        return ResponseUtils.ok(chars)
    }

    // 获取单角色详情 GET /api/characters/{id}
    @GetMapping("/characters/{id}")
    fun getOne(@PathVariable id: Long, auth: Authentication): ResponseEntity<*> {
        val character = characterService.findCharacter(id)
        return ResponseUtils.ok(characterService.toResponse(character))
    }

    // 创建新角色 POST /api/novels/{novelId}/characters
    @PostMapping("/novels/{novelId}/characters")
    fun create(
        @PathVariable novelId: Long,
        @Valid @RequestBody request: CharacterRequest,
        auth: Authentication
    ): ResponseEntity<*> {
        val character = characterService.createCharacter(novelId, SecurityUtils.getUserId(auth), request)
        return ResponseUtils.ok(character)
    }

    // 更新角色 PUT /api/characters/{id}
    @PutMapping("/characters/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CharacterRequest,
        auth: Authentication
    ): ResponseEntity<*> {
        val character = characterService.updateCharacter(id, SecurityUtils.getUserId(auth), request)
        return ResponseUtils.ok(character)
    }

    // 删除角色 DELETE /api/characters/{id}
    @DeleteMapping("/characters/{id}")
    fun delete(@PathVariable id: Long, auth: Authentication): ResponseEntity<*> {
        characterService.deleteCharacter(id, SecurityUtils.getUserId(auth))
        return ResponseUtils.message("删除成功")
    }
}
