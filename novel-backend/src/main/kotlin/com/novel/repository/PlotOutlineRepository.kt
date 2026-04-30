package com.novel.repository

import com.novel.entity.PlotOutline
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlotOutlineRepository : JpaRepository<PlotOutline, Long> {
    fun findByNovelIdOrderByOrderIndexAsc(novelId: Long): List<PlotOutline>
}
