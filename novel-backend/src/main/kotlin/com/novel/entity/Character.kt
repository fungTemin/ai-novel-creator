package com.novel.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

// 角色实体，对应数据库 characters 表，存储小说角色的详细信息
@Entity
@Table(name = "characters")
class Character(
    // 主键，自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    // 所属小说，多对一懒加载
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    var novel: Novel = Novel(),

    // 角色名称，非空，最大长度 100
    @Column(nullable = false, length = 100)
    var name: String = "",

    // 角色定位（主角/反派/配角/恋人等），最大长度 50
    @Column(length = 50)
    var role: String? = null,

    // 外貌描述，TEXT 类型
    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    // 性格特征，TEXT 类型
    @Column(columnDefinition = "TEXT")
    var personality: String? = null,

    // 背景故事，TEXT 类型
    @Column(columnDefinition = "TEXT")
    var background: String? = null,

    // 人际关系描述，TEXT 类型
    @Column(columnDefinition = "TEXT")
    var relationships: String? = null,

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
    @JoinColumn(name = "novel_id", nullable = false)
    var novel: Novel = Novel(),

    @Column(nullable = false, length = 100)
    var name: String = "",

    @Column(length = 50)
    var role: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(columnDefinition = "TEXT")
    var personality: String? = null,

    @Column(columnDefinition = "TEXT")
    var background: String? = null,

    @Column(columnDefinition = "TEXT")
    var relationships: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
