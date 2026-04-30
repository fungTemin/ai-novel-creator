package com.novel.exception

import com.novel.dto.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

// 全局异常处理器：统一捕获各层异常，返回标准 ApiResponse 格式
@RestControllerAdvice
class GlobalExceptionHandler {

    // 资源不存在异常（404）
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(code = 404, message = ex.message ?: "资源不存在"))
    }

    // 登录凭证错误（401）
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse(code = 401, message = "用户名或密码错误"))
    }

    // 用户不存在（401）
    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFound(ex: UsernameNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse(code = 401, message = ex.message ?: "用户不存在"))
    }

    // 参数校验失败（400），返回各字段的具体错误信息
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Map<String, String>>> {
        val errors = ex.bindingResult.fieldErrors.associate { error: FieldError ->
            (error.field to (error.defaultMessage ?: "参数无效"))
        }
        return ResponseEntity.badRequest()
            .body(ApiResponse(code = 400, message = "参数校验失败", data = errors))
    }

    // 非法参数异常（400）
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.badRequest()
            .body(ApiResponse(code = 400, message = ex.message ?: "参数错误"))
    }

    // 通用异常兜底（500）
    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse(code = 500, message = "服务器内部错误"))
    }
}

// 资源不存在运行期异常
class ResourceNotFoundException(message: String) : RuntimeException(message)
