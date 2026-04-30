package com.novel.service

import com.novel.dto.request.CharacterRequest
import com.novel.dto.response.CharacterResponse
import com.novel.entity.Character
import com.novel.exception.ResourceNotFoundException
import com.novel.repository.CharacterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 角色服务，提供小说角色的增删改查及所有权校验
@Service
class CharacterService(
    private val characterRepository: CharacterRepository,
    private val novelService: NovelService
) {

    // 获取某小说下的所有角色列表
    fun getCharacters(novelId: Long, userId: Long): List<CharacterResponse> {
        novelService.findOwnedNovel(novelId, userId)
        return characterRepository.findByNovelId(novelId).map { toResponse(it) }
    }

    // 创建新角色，绑定到指定小说
    @Transactional
    fun createCharacter(novelId: Long, userId: Long, request: CharacterRequest): CharacterResponse {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val character = Character(
            novel = novel,
            name = request.name,
            role = request.role,
            description = request.description,
            personality = request.personality,
            background = request.background,
            relationships = request.relationships
        )
        return toResponse(characterRepository.save(character))
    }

    // 更新角色信息
    @Transactional
    fun updateCharacter(characterId: Long, userId: Long, request: CharacterRequest): CharacterResponse {
        val character = findCharacter(characterId)
        verifyOwnership(character, userId)

        character.name = request.name
        character.role = request.role
        character.description = request.description
        character.personality = request.personality
        character.background = request.background
        character.relationships = request.relationships

        return toResponse(characterRepository.save(character))
    }

    // 删除角色
    @Transactional
    fun deleteCharacter(characterId: Long, userId: Long) {
        val character = findCharacter(characterId)
        verifyOwnership(character, userId)
        characterRepository.delete(character)
    }

    // 按 ID 查询角色
    fun findCharacter(characterId: Long): Character {
        return characterRepository.findById(characterId)
            .orElseThrow { ResourceNotFoundException("角色不存在") }
    }

    // 校验角色所有权（通过小说所属用户判断）
    private fun verifyOwnership(character: Character, userId: Long) {
        if (character.novel.user.id != userId) throw ResourceNotFoundException("角色不存在")
    }

    // 将 Character 实体转换为 CharacterResponse DTO
    fun toResponse(character: Character): CharacterResponse = CharacterResponse(
        id = character.id,
        name = character.name,
        role = character.role,
        description = character.description,
        personality = character.personality,
        background = character.background,
        relationships = character.relationships,
        createdAt = character.createdAt,
        updatedAt = character.updatedAt
    )
}
