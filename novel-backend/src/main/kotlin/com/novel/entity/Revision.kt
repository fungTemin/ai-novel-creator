package com.novel.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

// AI 修订记录实体，对应数据库 revisions 表，存储每次 AI 修订的内容和历史
@Entity
@Table(name = "revisions")
class Revision(
    // 主键，自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    // 所属章节，多对一懒加载
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    var chapter: Chapter = Chapter(),

    // 修订前的原文内容，TEXT 类型
    @Column(name = "original_content", columnDefinition = "TEXT")
    var originalContent: String? = null,

    // 修订后的内容，TEXT 类型
    @Column(name = "revised_content", columnDefinition = "TEXT")
    var revisedContent: String? = null,

    // AI 给出的修订反馈说明，TEXT 类型
    @Column(columnDefinition = "TEXT")
    var feedback: String? = null,

    // 创建时间（修订时间），自动填充，不可更新
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
