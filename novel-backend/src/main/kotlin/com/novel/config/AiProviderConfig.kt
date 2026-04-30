package com.novel.config

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Spring AI 模型提供者配置
 * 将自定义 ai.providers 配置映射为 Spring AI 的 ChatModel Bean
 * 支持 DeepSeek、小米 AI 等所有兼容 OpenAI 接口的模型
 */
@Configuration
class AiProviderConfig(
    private val aiConfig: AiConfig
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /** DeepSeek ChatModel Bean */
    @Bean("deepSeekChatModel")
    fun deepSeekChatModel(): ChatModel {
        val cfg = aiConfig.providers["deepseek"]
            ?: throw IllegalStateException("缺少 DeepSeek 配置")
        return buildChatModel(cfg, "deepseek")
    }

    /** 小米 AI ChatModel Bean */
    @Bean("xiaoMiChatModel")
    fun xiaoMiChatModel(): ChatModel {
        val cfg = aiConfig.providers["xiaomi"]
            ?: throw IllegalStateException("缺少小米 AI 配置")
        return buildChatModel(cfg, "xiaomi")
    }

    /**
     * 根据配置构建 Spring AI ChatModel 实例
     * @param cfg 单个提供商的配置
     * @param name 提供商名称（仅用于日志）
     */
    private fun buildChatModel(cfg: AiConfig.ProviderConfig, name: String): ChatModel {
        logger.info("初始化 AI 模型: provider={}, model={}, baseUrl={}", name, cfg.model, cfg.baseUrl)

        // 构建 OpenAI 兼容的 API 客户端
        val api = OpenAiApi.builder()
            .baseUrl(cfg.baseUrl)
            .apiKey(cfg.apiKey)
            .build()

        // 构建 ChatModel，设置默认选项
        return OpenAiChatModel.builder()
            .openAiApi(api)
            .defaultOptions(OpenAiChatOptions.builder()
                .model(cfg.model)
                .temperature(cfg.temperature)
                .maxTokens(cfg.maxTokens)
                .build())
            .build()
    }
}
