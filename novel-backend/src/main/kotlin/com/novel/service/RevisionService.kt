package com.novel.service

import com.novel.dto.response.RevisionResponse
import com.novel.entity.Revision
import com.novel.repository.RevisionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 修订记录服务，管理 AI 修订历史（查询和创建）
@Service
class RevisionService(
    private val revisionRepository: RevisionRepository,
    private val chapterService: ChapterService
) {

    // 获取某章节的修订历史，按时间降序排列，无权限时返回空列表
    fun getRevisions(chapterId: Long, userId: Long): List<RevisionResponse> {
        val chapter = chapterService.findChapter(chapterId)
        if (chapter.novel.user.id != userId) return emptyList()
        return revisionRepository.findByChapterIdOrderByCreatedAtDesc(chapterId)
            .map { it.toResponse() }
    }

    // 创建修订记录，保存原文、修订后内容和 AI 反馈
    @Transactional
    fun createRevision(
        chapterId: Long,
        userId: Long,
        originalContent: String,
        revisedContent: String,
        feedback: String
    ): RevisionResponse {
        val chapter = chapterService.findChapter(chapterId)
        if (chapter.novel.user.id != userId) throw IllegalArgumentException("无权限")

        val revision = Revision(
            chapter = chapter,
            originalContent = originalContent,
            revisedContent = revisedContent,
            feedback = feedback
        )
        return revisionRepository.save(revision).toResponse()
    }

    // 扩展函数：Revision -> RevisionResponse
    fun Revision.toResponse(): RevisionResponse = RevisionResponse(
        id = id,
        originalContent = originalContent,
        revisedContent = revisedContent,
        feedback = feedback,
        createdAt = createdAt
    )
}
