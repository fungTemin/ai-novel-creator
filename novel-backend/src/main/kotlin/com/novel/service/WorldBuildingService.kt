package com.novel.service

import com.novel.dto.request.WorldBuildingRequest
import com.novel.dto.response.WorldBuildingResponse
import com.novel.entity.WorldBuilding
import com.novel.exception.ResourceNotFoundException
import com.novel.repository.WorldBuildingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 世界观服务，提供世界观设定的增删改查，支持按分类筛选
@Service
class WorldBuildingService(
    private val worldBuildingRepository: WorldBuildingRepository,
    private val novelService: NovelService
) {

    // 获取世界观列表，可传入 category 参数按分类过滤
    fun getAll(novelId: Long, userId: Long, category: String?): List<WorldBuildingResponse> {
        novelService.findOwnedNovel(novelId, userId)
        val list = if (category != null) {
            worldBuildingRepository.findByNovelIdAndCategory(novelId, category)
        } else {
            worldBuildingRepository.findByNovelId(novelId)
        }
        return list.map { it.toResponse() }
    }

    // 创建世界观条目
    @Transactional
    fun create(novelId: Long, userId: Long, request: WorldBuildingRequest): WorldBuildingResponse {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val entry = WorldBuilding(
            novel = novel,
            category = request.category,
            name = request.name,
            description = request.description,
            details = request.details
        )
        return worldBuildingRepository.save(entry).toResponse()
    }

    // 更新世界观条目
    @Transactional
    fun update(id: Long, userId: Long, request: WorldBuildingRequest): WorldBuildingResponse {
        val entry = findEntry(id)
        verifyOwnership(entry, userId)

        entry.category = request.category
        entry.name = request.name
        entry.description = request.description
        entry.details = request.details

        return worldBuildingRepository.save(entry).toResponse()
    }

    // 删除世界观条目
    @Transactional
    fun delete(id: Long, userId: Long) {
        val entry = findEntry(id)
        verifyOwnership(entry, userId)
        worldBuildingRepository.delete(entry)
    }

    // 按 ID 查询条目
    fun findEntry(id: Long): WorldBuilding {
        return worldBuildingRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("世界观条目不存在") }
    }

    // 校验所有权
    private fun verifyOwnership(entry: WorldBuilding, userId: Long) {
        if (entry.novel.user.id != userId) throw ResourceNotFoundException("世界观条目不存在")
    }

    // 扩展函数：WorldBuilding -> WorldBuildingResponse
    fun WorldBuilding.toResponse(): WorldBuildingResponse = WorldBuildingResponse(
        id = id,
        category = category,
        name = name,
        description = description,
        details = details,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
