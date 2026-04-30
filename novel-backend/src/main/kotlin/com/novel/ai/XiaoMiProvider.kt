package com.novel.ai

import com.novel.config.AiConfig
import org.springframework.stereotype.Component

// 小米 AI 模型提供者，从配置中读取 xiaomi 相关的 API 地址、密钥和模型参数
@Component
class XiaoMiProvider(
    config: AiConfig
) : AiProvider(config.providers["xiaomi"] ?: AiConfig.ProviderConfig())
