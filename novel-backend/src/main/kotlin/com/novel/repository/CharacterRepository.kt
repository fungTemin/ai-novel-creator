package com.novel.repository

import com.novel.entity.Character
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CharacterRepository : JpaRepository<Character, Long> {
    fun findByNovelId(novelId: Long): List<Character>
    fun findByNovelIdAndRole(novelId: Long, role: String): List<Character>
}
