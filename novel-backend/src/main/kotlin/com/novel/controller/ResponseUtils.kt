package com.novel.controller

import com.novel.dto.response.ApiResponse
import org.springframework.http.ResponseEntity

// 统一响应工具类，封装前端 API 的标准返回格式
object ResponseUtils {
    // 成功返回数据（code=200, message="success"）
    fun <T> ok(data: T?): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.ok(ApiResponse(data = data))
    }

    // 仅返回操作消息，无数据体
    fun message(message: String): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.ok(ApiResponse<Nothing>(message = message))
    }
}
