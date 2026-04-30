package com.novel.ai

import com.novel.config.AiConfig
import org.springframework.stereotype.Component

// DeepSeek AI 模型提供者，从配置中读取 deepseek 相关的 API 地址、密钥和模型参数
@Component
class DeepSeekProvider(
    config: AiConfig
) : AiProvider(config.providers["deepseek"] ?: AiConfig.ProviderConfig())
