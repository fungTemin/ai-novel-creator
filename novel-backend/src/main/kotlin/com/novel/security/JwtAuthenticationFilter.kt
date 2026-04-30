package com.novel.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

// JWT 认证过滤器：每个请求执行一次，从 Authorization 头解析 JWT 并设置 SecurityContext
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    // 核心过滤逻辑：提取令牌、校验、加载用户、设置认证信息
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = getTokenFromRequest(request)

        // 令牌存在且有效时，从令牌解析用户名并加载用户详情
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token!!)) {
            val username = jwtTokenProvider.getUsernameFromToken(token)
            val userDetails = userDetailsService.loadUserByUsername(username)

            // 构建 UsernamePasswordAuthenticationToken 并注入 SecurityContext
            val authentication = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        }

        // 无论认证是否成功，继续过滤器链
        filterChain.doFilter(request, response)
    }

    // 从请求头中提取 Bearer Token
    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
