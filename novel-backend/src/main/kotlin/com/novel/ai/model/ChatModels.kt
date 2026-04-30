package com.novel.ai.model

// AI 聊天补全请求体，兼容 OpenAI API 格式
data class ChatCompletionRequest(
    val model: String = "",                    // 模型名称
    val messages: List<Message> = emptyList(), // 对话消息列表
    val maxTokens: Int = 4096,                 // 最大生成 token 数
    val temperature: Double = 0.8,             // 生成温度（随机性控制）
    val stream: Boolean = false,               // 是否使用流式输出
    val responseFormat: ResponseFormat? = null // 响应格式（如 JSON 模式）
) {
    // 对话消息：role 为 system/user/assistant，content 为消息内容
    data class Message(
        val role: String,
        val content: String
    )

    // 响应格式设置，type="json_object" 强制 AI 输出 JSON
    data class ResponseFormat(
        val type: String = "json_object"
    )
}

// AI 聊天补全响应体
data class ChatCompletionResponse(
    val id: String = "",                    // 响应唯一标识
    val choices: List<Choice> = emptyList(),// 生成的候选项列表
    val usage: Usage? = null                // token 使用统计
) {
    // 单个生成候选项
    data class Choice(
        val message: Message? = null,     // 生成的消息
        val finishReason: String? = null  // 结束原因（stop/length 等）
    ) {
        // 生成的消息内容
        data class Message(
            val role: String = "",    // 角色（assistant）
            val content: String = ""  // 生成的文本内容
        )
    }

    // token 用量统计
    data class Usage(
        val promptTokens: Int = 0,      // 提示词 token 数
        val completionTokens: Int = 0,  // 生成结果 token 数
        val totalTokens: Int = 0        // 总 token 数
    )
}
