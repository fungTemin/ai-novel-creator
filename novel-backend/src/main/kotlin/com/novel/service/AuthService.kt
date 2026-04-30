package com.novel.service

import com.novel.dto.request.LoginRequest
import com.novel.dto.request.RegisterRequest
import com.novel.dto.response.AuthResponse
import com.novel.entity.User
import com.novel.repository.UserRepository
import com.novel.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 认证服务，处理用户注册和登录业务逻辑
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider
) {

    // 用户注册：校验用户名和邮箱唯一性，加密密码，保存用户并生成 JWT
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        require(!userRepository.existsByUsername(request.username)) { "用户名已存在" }
        require(!userRepository.existsByEmail(request.email)) { "邮箱已注册" }

        val user = User(
            username = request.username,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password)
        )
        val saved = userRepository.save(user)
        val token = jwtTokenProvider.generateToken(saved.id, saved.username)

        return AuthResponse(id = saved.id, username = saved.username, email = saved.email, token = token)
    }

    // 用户登录：通过 AuthenticationManager 校验凭证，生成 JWT 并返回用户信息
    fun login(request: LoginRequest): AuthResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )
        val token = jwtTokenProvider.generateToken(authentication)
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { BadCredentialsException("用户名或密码错误") }

        return AuthResponse(id = user.id, username = user.username, email = user.email, token = token)
    }
}
