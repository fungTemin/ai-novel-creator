package com.novel.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

// 章节实体，对应数据库 chapters 表，同一小说内章节号唯一
@Entity
@Table(
    name = "chapters",
    uniqueConstraints = [UniqueConstraint(columnNames = ["novel_id", "chapter_number"])]
)
class Chapter(
    // 主键，自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    // 所属小说，多对一懒加载
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    var novel: Novel = Novel(),

    // 章节序号（同一小说内唯一），非空
    @Column(name = "chapter_number", nullable = false)
    var chapterNumber: Int = 0,

    // 章节标题，最大长度 200
    @Column(length = 200)
    var title: String? = null,

    // 章节正文内容，TEXT 类型
    @Column(columnDefinition = "TEXT")
    var content: String? = null,

    // 章节状态（draft-草稿, writing-写作中, revised-已修订），非空
    @Column(nullable = false, length = 20)
    var status: String = "draft",

    // 字数统计，非空
    @Column(name = "word_count", nullable = false)
    var wordCount: Int = 0,

    // 章节摘要/大纲描述，TEXT 类型
    @Column(columnDefinition = "TEXT")
    var summary: String? = null,

    // 修订历史，一对多级联
    @OneToMany(mappedBy = "chapter", cascade = [CascadeType.ALL], orphanRemoval = true)
    var revisions: MutableList<Revision> = mutableListOf(),

    // 创建时间，自动填充，不可更新
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    // 更新时间，自动填充
    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
