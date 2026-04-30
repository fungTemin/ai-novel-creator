package com.novel.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

// 情节大纲实体，对应数据库 plot_outlines 表，存储每条大纲条目
@Entity
@Table(name = "plot_outlines")
class PlotOutline(
    // 主键，自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    // 所属小说，多对一懒加载
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    var novel: Novel = Novel(),

    // 大纲条目标题，非空，最大长度 200
    @Column(nullable = false, length = 200)
    var title: String = "",

    // 条目摘要描述，TEXT 类型
    @Column(columnDefinition = "TEXT")
    var summary: String? = null,

    // 排序索引，决定大纲条目的先后顺序，非空
    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,

    // 创建时间，自动填充，不可更新
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    // 更新时间，自动填充
    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
