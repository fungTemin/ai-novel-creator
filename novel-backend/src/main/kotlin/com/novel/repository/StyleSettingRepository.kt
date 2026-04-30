package com.novel.repository

import com.novel.entity.StyleSetting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface StyleSettingRepository : JpaRepository<StyleSetting, Long> {
    fun findByNovelId(novelId: Long): Optional<StyleSetting>
}
