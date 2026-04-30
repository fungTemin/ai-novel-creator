package com.novel.ai

import com.novel.ai.model.ChatCompletionRequest
import com.novel.ai.model.ChatCompletionResponse
import com.novel.config.AiConfig
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

// AI 模型提供者基类：封装调用第三方大语言模型 API 的通用逻辑（创建 WebClient、发送请求、解析响应）
open class AiProvider(
    protected val config: AiConfig.ProviderConfig
) {
    protected val logger = LoggerFactory.getLogger(javaClass)

    // 创建 WebClient 实例，自动注入 Authorization 请求头
    protected open fun createWebClient(): WebClient {
        return WebClient.builder()
            .defaultHeader("Authorization", "Bearer ${config.apiKey}")
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    // 发送聊天补全请求：传入 system 提示词和用户消息，返回 AI 生成的文本内容
    open fun generateCompletion(systemPrompt: String, userMessage: String): String {
        val url = config.apiUrl.ifBlank { throw RuntimeException("AI API URL 未配置") }

        // 组装请求体，包含 model、messages、maxTokens、temperature 等参数
        val request = ChatCompletionRequest(
            model = config.model,
            messages = listOf(
                ChatCompletionRequest.Message("system", systemPrompt),
                ChatCompletionRequest.Message("user", userMessage)
            ),
            maxTokens = config.maxTokens,
            temperature = config.temperature
        )

        // 发起 HTTP POST 请求并阻塞等待响应
        val response = createWebClient().post()
            .uri(url)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<ChatCompletionResponse>()
            .block()
            ?: throw RuntimeException("AI 响应为空")

        // 提取 AI 回复内容
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw RuntimeException("AI 未返回内容")

        logger.debug("AI 生成完成，tokens: prompt=${response.usage?.promptTokens}, completion=${response.usage?.completionTokens}")
        return content
    }

    // 获取当前使用的模型名称
    fun getModelName(): String = config.model
}

// 流式聊天补全响应块（用于 SSE 流式输出场景）
data class ChatCompletionStreamChunk(
    val choices: List<StreamChoice> = emptyList()
) {
    data class StreamChoice(
        val delta: Delta = Delta(),
        val index: Int = 0
    )

    data class Delta(
        val content: String? = null,
        val role: String? = null
    )
}
