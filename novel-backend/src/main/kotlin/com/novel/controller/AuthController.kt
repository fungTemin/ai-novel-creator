package com.novel.controller

import com.novel.dto.request.LoginRequest
import com.novel.dto.request.RegisterRequest
import com.novel.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// 认证控制器：处理用户注册和登录请求
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    // 用户注册接口 POST /api/auth/register
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<*> {
        val auth = authService.register(request)
        return ResponseUtils.ok(auth)
    }

    // 用户登录接口 POST /api/auth/login
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<*> {
        val auth = authService.login(request)
        return ResponseUtils.ok(auth)
    }
}
