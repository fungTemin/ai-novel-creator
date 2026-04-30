package com.novel.service

import com.novel.dto.request.NovelRequest
import com.novel.dto.response.ChapterBriefResponse
import com.novel.dto.response.NovelResponse
import com.novel.entity.Novel
import com.novel.entity.User
import com.novel.exception.ResourceNotFoundException
import com.novel.repository.NovelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 小说服务，提供小说的增删改查及权限校验
@Service
class NovelService(
    private val novelRepository: NovelRepository
) {

    // 获取指定用户的所有小说列表
    fun getNovelsByUser(userId: Long): List<NovelResponse> {
        return novelRepository.findByUserId(userId).map { it.toResponse() }
    }

    // 获取单篇小说详情（同时校验该小说属于当前用户）
    fun getNovel(novelId: Long, userId: Long): NovelResponse {
        val novel = findOwnedNovel(novelId, userId)
        return novel.toResponse()
    }

    // 创建新小说，绑定创建者用户
    @Transactional
    fun createNovel(user: User, request: NovelRequest): NovelResponse {
        val novel = Novel(
            user = user,
            title = request.title,
            description = request.description,
            genre = request.genre
        )
        return novelRepository.save(novel).toResponse()
    }

    // 更新小说基本信息（标题、简介、题材）
    @Transactional
    fun updateNovel(novelId: Long, userId: Long, request: NovelRequest): NovelResponse {
        val novel = findOwnedNovel(novelId, userId)
        novel.title = request.title
        novel.description = request.description
        novel.genre = request.genre
        return novelRepository.save(novel).toResponse()
    }

    // 删除小说
    @Transactional
    fun deleteNovel(novelId: Long, userId: Long) {
        val novel = findOwnedNovel(novelId, userId)
        novelRepository.delete(novel)
    }

    // 按 ID 和用户 ID 查询小说，不存在则抛出异常（用于权限校验）
    fun findOwnedNovel(novelId: Long, userId: Long): Novel {
        return novelRepository.findByUserIdAndId(userId, novelId)
            ?: throw ResourceNotFoundException("小说不存在")
    }

    // 内部扩展函数：将 Novel 实体转换为 NovelResponse DTO
    private fun Novel.toResponse(): NovelResponse = NovelResponse(
        id = id,
        title = title,
        description = description,
        genre = genre,
        status = status,
        chapterCount = chapters.size,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
