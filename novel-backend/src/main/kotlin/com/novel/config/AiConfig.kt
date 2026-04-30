package com.novel.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

// AI 模型配置：从 application.yml 读取 ai 前缀的配置，支持多模型提供商
@Configuration
@ConfigurationProperties(prefix = "ai")
class AiConfig {
    // 默认使用的 AI 提供商（deepseek / xiaomi）
    var defaultProvider: String = "deepseek"
    // 各提供商的详细配置
    var providers: Map<String, ProviderConfig> = emptyMap()

    // 单个 AI 提供商的配置项
    class ProviderConfig {
        var apiUrl: String = ""     // API 请求地址
        var apiKey: String = ""     // API 密钥
        var model: String = ""      // 模型名称
        var maxTokens: Int = 4096   // 最大生成 token 数
        var temperature: Double = 0.8 // 生成温度
    }
}
