package com.novel.repository

import com.novel.entity.WorldBuilding
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorldBuildingRepository : JpaRepository<WorldBuilding, Long> {
    fun findByNovelId(novelId: Long): List<WorldBuilding>
    fun findByNovelIdAndCategory(novelId: Long, category: String): List<WorldBuilding>
}
