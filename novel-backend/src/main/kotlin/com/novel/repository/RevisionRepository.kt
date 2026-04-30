package com.novel.repository

import com.novel.entity.Revision
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RevisionRepository : JpaRepository<Revision, Long> {
    fun findByChapterIdOrderByCreatedAtDesc(chapterId: Long): List<Revision>
}
