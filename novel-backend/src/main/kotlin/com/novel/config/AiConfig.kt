package com.novel.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * AI 模型配置：从 application.yml 读取 ai.prometheus 前缀的配置
 * 支持多个 AI 模型提供商（DeepSeek / 小米 AI 等）
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
class AiConfig {
    /** 默认使用的 AI 提供商名称（deepseek / xiaomi） */
    var defaultProvider: String = "deepseek"

    /** 各 AI 提供商的详细配置 */
    var providers: Map<String, ProviderConfig> = emptyMap()

    /**
     * 单个 AI 提供商的配置项
     * 使用 Spring AI OpenAiApi 兼容的字段名
     */
    class ProviderConfig {
        /** API 请求基础地址（如 https://api.deepseek.com） */
        var baseUrl: String = ""

        /** API 密钥 */
        var apiKey: String = ""

        /** 模型名称（如 deepseek-chat, mimo-v2.5） */
        var model: String = ""

        /** 最大生成 token 数 */
        var maxTokens: Int = 4096

        /** 生成温度（0-2，越高随机性越强） */
        var temperature: Double = 0.8
    }
}
