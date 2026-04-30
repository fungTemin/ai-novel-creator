package com.novel.security

import org.springframework.security.core.Authentication

// 安全工具类：从 Authentication 对象中提取当前登录用户的 ID
object SecurityUtils {
    // 从认证信息中获取用户 ID（principal 必须为 CustomUserDetails 类型）
    fun getUserId(authentication: Authentication): Long {
        val principal = authentication.principal
        if (principal is CustomUserDetails) {
            return principal.getUserId()
        }
        throw RuntimeException("无法获取用户 ID")
    }
}
