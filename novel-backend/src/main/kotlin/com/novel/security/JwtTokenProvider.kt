package com.novel.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import javax.crypto.SecretKey
import java.util.*

// JWT 令牌提供者：负责生成、解析和验证 JWT 令牌
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${jwt.expiration}") private val jwtExpiration: Long
) {

    // 基于配置的密钥字符串生成 HMAC 签名密钥
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    // 从 Authentication 对象生成 JWT
    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as CustomUserDetails
        return generateToken(userPrincipal.getUserId(), userPrincipal.username)
    }

    // 仅用用户名生成 JWT（不含 userId 声明）
    fun generateToken(username: String): String {
        return generateToken(null, username)
    }

    // 核心生成方法：携带 userId 和 username 签发 JWT
    fun generateToken(userId: Long?, username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)

        val builder = Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)

        if (userId != null) {
            builder.claim("userId", userId)
        }

        return builder.compact()
    }

    // 从令牌中提取用户名
    fun getUsernameFromToken(token: String): String {
        return getClaims(token).subject
    }

    // 从令牌中提取用户 ID
    fun getUserIdFromToken(token: String): Long {
        val userId = getClaims(token)["userId"]?.toString()?.toLong()
            ?: throw RuntimeException("Token 中不含用户 ID")
        return userId
    }

    // 验证令牌是否有效（签名正确且在有效期内）
    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (ex: JwtException) {
            false
        } catch (ex: IllegalArgumentException) {
            false
        }
    }

    // 解析 JWT 的 Claims 负载
    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
