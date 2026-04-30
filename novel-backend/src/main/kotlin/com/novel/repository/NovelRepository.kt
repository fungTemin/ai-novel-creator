package com.novel.repository

import com.novel.entity.Novel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NovelRepository : JpaRepository<Novel, Long> {
    fun findByUserId(userId: Long): List<Novel>
    fun findByUserIdAndId(userId: Long, id: Long): Novel?
}
