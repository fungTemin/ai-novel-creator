package com.novel.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

// 风格设置实体，对应数据库 style_settings 表，与小说一对一关联，定义创作风格
@Entity
@Table(name = "style_settings")
class StyleSetting(
    // 主键，自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    // 关联小说，一对一懒加载，非空且唯一
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false, unique = true)
    var novel: Novel = Novel(),

    // 叙事基调（严肃/轻松/暗黑/温暖等），最大长度 50
    @Column(length = 50)
    var tone: String? = null,

    // 叙事视角（第一人称/第三人称/多视角等），最大长度 50
    @Column(length = 50)
    var perspective: String? = null,

    // 目标读者群体描述，最大长度 100
    @Column(name = "target_audience", length = 100)
    var targetAudience: String? = null,

    // 风格详细描述，TEXT 类型
    @Column(name = "style_description", columnDefinition = "TEXT")
    var styleDescription: String? = null,

    // 创建时间，自动填充，不可更新
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    // 更新时间，自动填充
    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false, unique = true)
    var novel: Novel = Novel(),

    @Column(length = 50)
    var tone: String? = null,

    @Column(length = 50)
    var perspective: String? = null,

    @Column(name = "target_audience", length = 100)
    var targetAudience: String? = null,

    @Column(name = "style_description", columnDefinition = "TEXT")
    var styleDescription: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
