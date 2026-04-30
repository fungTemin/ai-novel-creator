package com.novel.security

import com.novel.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

// 用户详情服务：Spring Security 认证时按用户名加载用户信息
@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    // 从数据库加载用户，返回 Spring Security UserDetails 对象
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("用户不存在: $username") }

        return CustomUserDetails(user)
    }
}

// 自定义 UserDetails 实现：在标准 Security User 基础上扩展了 getUserId 方法
class CustomUserDetails(
    val user: com.novel.entity.User
) : org.springframework.security.core.userdetails.User(
    user.username,
    user.passwordHash,
    listOf(SimpleGrantedAuthority("ROLE_USER"))
) {
    // 获取用户数据库 ID
    fun getUserId(): Long = user.id
}
