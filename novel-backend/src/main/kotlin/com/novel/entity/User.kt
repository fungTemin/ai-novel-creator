package com.novel.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

// 用户实体，对应数据库 users 表，存储系统用户信息
@Entity
@Table(name = "users")
class User(
    // 主键，自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    // 用户名，唯一且非空，最大长度 50
    @Column(nullable = false, unique = true, length = 50)
    var username: String = "",

    // 邮箱地址，唯一且非空，最大长度 100
    @Column(nullable = false, unique = true, length = 100)
    var email: String = "",

    // 经过 BCrypt 加密的密码哈希值，非空
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",

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

    @Column(nullable = false, unique = true, length = 50)
    var username: String = "",

    @Column(nullable = false, unique = true, length = 100)
    var email: String = "",

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
