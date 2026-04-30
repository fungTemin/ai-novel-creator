package com.novel.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

// 世界观设定实体，对应数据库 world_building 表，按分类管理小说世界设定
@Entity
@Table(name = "world_building")
class WorldBuilding(
    // 主键，自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    // 所属小说，多对一懒加载
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    var novel: Novel = Novel(),

    // 分类（如 geography, magic_system, culture 等英文分类名），非空
    @Column(nullable = false, length = 50)
    var category: String = "",

    // 设定条目名称，非空，最大长度 200
    @Column(nullable = false, length = 200)
    var name: String = "",

    // 简要描述，TEXT 类型
    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    // 详细设定（可存储 JSON 格式扩展信息），TEXT 类型
    @Column(columnDefinition = "TEXT")
    var details: String? = null,

    // 创建时间，自动填充，不可更新
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    // 更新时间，自动填充
    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
