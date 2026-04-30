package com.novel.repository

import com.novel.entity.Chapter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChapterRepository : JpaRepository<Chapter, Long> {
    fun findByNovelIdOrderByChapterNumberAsc(novelId: Long): List<Chapter>
    fun findByNovelIdAndChapterNumber(novelId: Long, chapterNumber: Int): Chapter?
    fun countByNovelId(novelId: Long): Long
}
