package com.novel.service

import com.novel.dto.request.ChapterRequest
import com.novel.dto.response.ChapterBriefResponse
import com.novel.dto.response.ChapterResponse
import com.novel.entity.Chapter
import com.novel.entity.Novel
import com.novel.exception.ResourceNotFoundException
import com.novel.repository.ChapterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 章节服务，提供章节的增删改查及所属权校验
@Service
class ChapterService(
    private val chapterRepository: ChapterRepository,
    private val novelService: NovelService
) {

    // 获取某小说下所有章节的简要列表，按章节号升序
    fun getChapters(novelId: Long, userId: Long): List<ChapterBriefResponse> {
        novelService.findOwnedNovel(novelId, userId)
        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId)
            .map { it.toBriefResponse() }
    }

    // 获取单章详情，同时校验章节所有权
    fun getChapter(chapterId: Long, userId: Long): ChapterResponse {
        val chapter = findChapter(chapterId)
        verifyOwnership(chapter, userId)
        return chapter.toResponse()
    }

    // 创建章节，若未指定章节号则自动分配
    @Transactional
    fun createChapter(novelId: Long, userId: Long, request: ChapterRequest): ChapterResponse {
        val novel = novelService.findOwnedNovel(novelId, userId)

        val chapterNumber = request.chapterNumber
            ?: (chapterRepository.countByNovelId(novelId) + 1).toInt()

        // 检查章节号是否已被占用
        val existing = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber)
        require(existing == null) { "章节序号 $chapterNumber 已存在" }

        val chapter = Chapter(
            novel = novel,
            chapterNumber = chapterNumber,
            title = request.title,
            content = request.content,
            summary = request.summary,
            wordCount = request.content?.length ?: 0
        )
        return chapterRepository.save(chapter).toResponse()
    }

    // 更新章节内容，更新时自动统计字数，草稿状态转为写作中
    @Transactional
    fun updateChapter(chapterId: Long, userId: Long, request: ChapterRequest): ChapterResponse {
        val chapter = findChapter(chapterId)
        verifyOwnership(chapter, userId)

        request.title?.let { chapter.title = it }
        request.content?.let {
            chapter.content = it
            chapter.wordCount = it.length
            if (chapter.status == "draft") chapter.status = "writing"
        }
        request.summary?.let { chapter.summary = it }

        return chapterRepository.save(chapter).toResponse()
    }

    // 删除章节
    @Transactional
    fun deleteChapter(chapterId: Long, userId: Long) {
        val chapter = findChapter(chapterId)
        verifyOwnership(chapter, userId)
        chapterRepository.delete(chapter)
    }

    // 按 ID 查询章节，不存在则抛异常
    fun findChapter(chapterId: Long): Chapter {
        return chapterRepository.findById(chapterId)
            .orElseThrow { ResourceNotFoundException("章节不存在") }
    }

    // 校验章节是否属于给定用户（通过小说所属用户判断）
    private fun verifyOwnership(chapter: Chapter, userId: Long) {
        if (chapter.novel.user.id != userId) throw ResourceNotFoundException("章节不存在")
    }

    // 扩展函数：Chapter -> 含正文的完整响应
    private fun Chapter.toResponse(): ChapterResponse = ChapterResponse(
        id = id,
        chapterNumber = chapterNumber,
        title = title,
        content = content,
        status = status,
        wordCount = wordCount,
        summary = summary,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    // 扩展函数：Chapter -> 不含正文的简要响应
    private fun Chapter.toBriefResponse(): ChapterBriefResponse = ChapterBriefResponse(
        id = id,
        chapterNumber = chapterNumber,
        title = title,
        status = status,
        wordCount = wordCount,
        summary = summary,
        createdAt = createdAt
    )
}
