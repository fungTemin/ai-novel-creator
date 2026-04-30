package com.novel.service

import com.novel.dto.request.PlotOutlineRequest
import com.novel.dto.response.PlotOutlineResponse
import com.novel.entity.PlotOutline
import com.novel.exception.ResourceNotFoundException
import com.novel.repository.PlotOutlineRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 情节大纲服务，提供大纲条目的增删改查，按排序索引排列
@Service
class PlotOutlineService(
    private val plotOutlineRepository: PlotOutlineRepository,
    private val novelService: NovelService
) {

    // 获取某小说下的所有大纲条目，按 orderIndex 升序
    fun getAll(novelId: Long, userId: Long): List<PlotOutlineResponse> {
        novelService.findOwnedNovel(novelId, userId)
        return plotOutlineRepository.findByNovelIdOrderByOrderIndexAsc(novelId)
            .map { it.toResponse() }
    }

    // 创建大纲条目
    @Transactional
    fun create(novelId: Long, userId: Long, request: PlotOutlineRequest): PlotOutlineResponse {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val outline = PlotOutline(
            novel = novel,
            title = request.title,
            summary = request.summary,
            orderIndex = request.orderIndex
        )
        return plotOutlineRepository.save(outline).toResponse()
    }

    // 更新大纲条目
    @Transactional
    fun update(id: Long, userId: Long, request: PlotOutlineRequest): PlotOutlineResponse {
        val outline = findOutline(id)
        verifyOwnership(outline, userId)

        outline.title = request.title
        outline.summary = request.summary
        outline.orderIndex = request.orderIndex

        return plotOutlineRepository.save(outline).toResponse()
    }

    // 删除大纲条目
    @Transactional
    fun delete(id: Long, userId: Long) {
        val outline = findOutline(id)
        verifyOwnership(outline, userId)
        plotOutlineRepository.delete(outline)
    }

    // 按 ID 查询大纲条目
    fun findOutline(id: Long): PlotOutline {
        return plotOutlineRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("大纲不存在") }
    }

    // 校验所有权
    private fun verifyOwnership(outline: PlotOutline, userId: Long) {
        if (outline.novel.user.id != userId) throw ResourceNotFoundException("大纲不存在")
    }

    // 扩展函数：PlotOutline -> PlotOutlineResponse
    fun PlotOutline.toResponse(): PlotOutlineResponse = PlotOutlineResponse(
        id = id,
        title = title,
        summary = summary,
        orderIndex = orderIndex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
