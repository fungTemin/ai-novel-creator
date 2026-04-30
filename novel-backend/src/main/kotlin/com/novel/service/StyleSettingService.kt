package com.novel.service

import com.novel.dto.request.StyleSettingRequest
import com.novel.dto.response.StyleSettingResponse
import com.novel.entity.StyleSetting
import com.novel.repository.StyleSettingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 风格设置服务，提供小说创作风格的获取和更新（不存在则创建）
@Service
class StyleSettingService(
    private val styleSettingRepository: StyleSettingRepository,
    private val novelService: NovelService
) {

    // 获取小说风格设置，未设置时返回 null
    fun get(novelId: Long, userId: Long): StyleSettingResponse? {
        novelService.findOwnedNovel(novelId, userId)
        return styleSettingRepository.findByNovelId(novelId)
            .map { it.toResponse() }
            .orElse(null)
    }

    // 更新风格设置：已存在则合并更新，不存在则新建
    @Transactional
    fun update(novelId: Long, userId: Long, request: StyleSettingRequest): StyleSettingResponse {
        val novel = novelService.findOwnedNovel(novelId, userId)
        val existing = styleSettingRepository.findByNovelId(novelId)
        val style = if (existing.isPresent) {
            existing.get().apply {
                request.tone?.let { tone = it }
                request.perspective?.let { perspective = it }
                request.targetAudience?.let { targetAudience = it }
                request.styleDescription?.let { styleDescription = it }
            }
        } else {
            StyleSetting(
                novel = novel,
                tone = request.tone,
                perspective = request.perspective,
                targetAudience = request.targetAudience,
                styleDescription = request.styleDescription
            )
        }
        return styleSettingRepository.save(style).toResponse()
    }

    // 扩展函数：StyleSetting -> StyleSettingResponse
    fun StyleSetting.toResponse(): StyleSettingResponse = StyleSettingResponse(
        id = id,
        tone = tone,
        perspective = perspective,
        targetAudience = targetAudience,
        styleDescription = styleDescription,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
