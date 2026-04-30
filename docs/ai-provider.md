# AI 提供者设计

## 架构概述

系统采用**策略模式**抽象 AI 提供者层，支持多个 AI 后端无缝切换。

```
┌─────────────────────────────────────────┐
│              AiService                   │
│  (业务层调用，不关心具体 AI 提供者)       │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│           AiProvider (接口)              │
│  - generateCompletion(prompt): String    │
│  - generateStreaming(prompt): Flow       │
│  - getModelName(): String               │
└──────────────────┬──────────────────────┘
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
  ┌──────────┐ ┌──────────┐ ┌──────────┐
  │ DeepSeek │ │  XiaoMi  │ │   更多... │
  │ Provider │ │ Provider │ │          │
  └──────────┘ └──────────┘ └──────────┘
```

## AI 提供者

### DeepSeek

- **API 地址:** `https://api.deepseek.com/v1/chat/completions`
- **API 格式:** OpenAI 兼容
- **推荐模型:** `deepseek-chat` (通用), `deepseek-coder` (代码)
- **特点:** 中文能力强、性价比高、支持长上下文

### 小米 AI (XiaoMi)

- **API 地址:** 小米大模型 API 端点
- **API 格式:** OpenAI 兼容
- **特点:** 小米生态集成、中文优化

## 提示词工程

### 系统提示词模板

#### 大纲生成
```
你是一位资深小说策划编辑，擅长构建引人入胜的故事大纲。

请根据以下要求生成小说大纲：
- 题材类型：{genre}
- 故事简介：{prompt}
- 预计章节数：{chapterCount}
- 写作风格：{style}

要求：
1. 每章需要有标题和简要摘要
2. 情节要有起承转合，节奏合理
3. 前后呼应，有伏笔设计
4. 角色成长弧线清晰

请以 JSON 格式返回。
```

#### 角色创建
```
你是一位经验丰富的小说角色设计师。

请创建一个角色：
- 角色类型：{role}
- 角色描述：{prompt}
- 所在小说背景：{novelContext}

要求：
1. 姓名要有特色，符合世界观
2. 外貌描写生动具体
3. 性格特征立体，有优缺点
4. 背景故事完整，与主线相关

请以 JSON 格式返回。
```

#### 章节写作
```
你是一位专业的网络小说作家，擅长{genre}类小说的创作。

【小说信息】
标题：{novelTitle}
简介：{novelDescription}

【风格要求】
- 叙事视角：{perspective}
- 基调：{tone}
- 目标读者：{audience}

【当前章节信息】
章节号：第{chapterNumber}章
章节标题：{chapterTitle}
章节大纲：{chapterSummary}

【前文摘要】
{previousSummary}

【角色列表】
{characterList}

【世界观设定】
{worldBuilding}

请根据以上信息撰写本章内容，要求：
1. 字数约 {wordCount} 字
2. 情节紧凑，节奏流畅
3. 人物对话自然生动
4. 环境描写适当
5. 结尾留有悬念或承上启下
```

#### 章节修订
```
你是一位资深小说编辑，正在审阅一部{genre}小说。

【小说风格】
{styleSettings}

【修订要求】
{instructions}

【待修订内容】
{selectedText || fullChapter}

请对以上内容进行修订，要求：
1. 保持原作的核心情节和人物性格
2. 根据修订要求进行针对性改进
3. 提升文字表达的文学性
4. 保持前后文的连贯性

请返回：
1. 修订后的内容
2. 修改说明（简要列出主要改动）
```

#### 世界观构建
```
你是一位想象力丰富的世界观架构师。

请为一部{genre}小说创建世界观设定：
- 分类：{category}
- 描述：{prompt}
- 已有世界观：{existingWorld}

要求：
1. 设定要自洽、合理
2. 有足够的深度和细节
3. 与已有设定不冲突
4. 为后续创作留有扩展空间

请以 JSON 格式返回详细设定。
```

## 上下文管理策略

### 短上下文（单章节生成）
- 当前章节大纲
- 前一章摘要
- 相关角色信息
- 世界观关键设定

### 长上下文（整体风格一致性）
- 小说元数据（标题、类型、简介）
- 风格设定
- 全部章节摘要列表
- 核心角色档案

### 上下文窗口限制处理
1. **优先级排序:** 风格设定 > 当前大纲 > 前文摘要 > 角色信息 > 世界观
2. **摘要压缩:** 超过限制时，对旧章节摘要进行压缩
3. **滑动窗口:** 只保留最近 N 章的详细摘要

## 流式响应

支持 AI 生成内容的流式输出（SSE），提升用户体验：

```
GET /api/novels/{id}/generate/chapter/stream

Content-Type: text/event-stream

data: {"content": "山风", "done": false}
data: {"content": "呼啸", "done": false}
data: {"content": "，林风", "done": false}
...
data: {"content": "", "done": true, "wordCount": 2856}
```

## 配置示例

```yaml
# application.yml
ai:
  default-provider: deepseek
  providers:
    deepseek:
      api-url: https://api.deepseek.com/v1/chat/completions
      api-key: ${DEEPSEEK_API_KEY}
      model: deepseek-chat
      max-tokens: 4096
      temperature: 0.8
    xiaomi:
      api-url: ${XIAOMI_API_URL}
      api-key: ${XIAOMI_API_KEY}
      model: ${XIAOMI_MODEL}
      max-tokens: 4096
      temperature: 0.8
```
