package com.novel.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

// 小说实体，对应数据库 novels 表，聚合章节、角色、世界观、大纲、风格等所有子实体
@Entity
@Table(name = "novels")
class Novel(
    // 主键，自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    // 所属用户，多对一懒加载
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    // 小说标题，非空，最大长度 200
    @Column(nullable = false, length = 200)
    var title: String = "",

    // 小说简介，TEXT 类型
    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    // 题材类型（如玄幻、科幻、言情等），最大长度 50
    @Column(length = 50)
    var genre: String? = null,

    // 作品状态（draft-草稿, writing-写作中, completed-已完成, published-已发布），非空
    @Column(nullable = false, length = 20)
    var status: String = "draft",

    // 章节列表，一对多级联，按章节号升序排列
    @OneToMany(mappedBy = "novel", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("chapterNumber ASC")
    var chapters: MutableList<Chapter> = mutableListOf(),

    // 角色列表，一对多级联
    @OneToMany(mappedBy = "novel", cascade = [CascadeType.ALL], orphanRemoval = true)
    var characters: MutableList<Character> = mutableListOf(),

    // 世界观条目列表，一对多级联
    @OneToMany(mappedBy = "novel", cascade = [CascadeType.ALL], orphanRemoval = true)
    var worldBuildingEntries: MutableList<WorldBuilding> = mutableListOf(),

    // 情节大纲列表，一对多级联，按排序索引升序排列
    @OneToMany(mappedBy = "novel", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    var plotOutlines: MutableList<PlotOutline> = mutableListOf(),

    // 风格设置，一对一级联
    @OneToOne(mappedBy = "novel", cascade = [CascadeType.ALL], orphanRemoval = true)
    var styleSetting: StyleSetting? = null,

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    @Column(nullable = false, length = 200)
    var title: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(length = 50)
    var genre: String? = null,

    @Column(nullable = false, length = 20)
    var status: String = "draft",

    @OneToMany(mappedBy = "novel", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("chapterNumber ASC")
    var chapters: MutableList<Chapter> = mutableListOf(),

    @OneToMany(mappedBy = "novel", cascade = [CascadeType.ALL], orphanRemoval = true)
    var characters: MutableList<Character> = mutableListOf(),

    @OneToMany(mappedBy = "novel", cascade = [CascadeType.ALL], orphanRemoval = true)
    var worldBuildingEntries: MutableList<WorldBuilding> = mutableListOf(),

    @OneToMany(mappedBy = "novel", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    var plotOutlines: MutableList<PlotOutline> = mutableListOf(),

    @OneToOne(mappedBy = "novel", cascade = [CascadeType.ALL], orphanRemoval = true)
    var styleSetting: StyleSetting? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
