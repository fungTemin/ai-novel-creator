# 开发工作流记录

> 本文档记录 AI 小说创作系统的完整开发过程，从架构设计到功能迭代的每一步。

---

## 阶段一：架构设计与项目初始化

### Step 1 — 需求分析与架构设计

**时间：** 开发初始

**决策记录：**

| 维度 | 选择 |
|------|------|
| 界面形态 | Web 应用 |
| 技术栈 | 后端 Kotlin + Spring Boot 3 / 前端 React 18 + TypeScript + Vite |
| AI 后端 | DeepSeek + 小米 AI（均兼容 OpenAI 接口） |
| 数据库 | PostgreSQL + Flyway 版本管理 |
| 认证 | JWT（jjwt 0.12） |
| 功能范围 | 所有核心功能：大纲/角色/章节/世界观/风格/修订 |

**产出文档：**

- `docs/architecture.md` — 系统架构设计（后端包结构、前端目录、认证流程）
- `docs/database.md` — 数据库 ER 图 + 8 张表结构定义
- `docs/api-design.md` — REST API 接口设计（全部端点定义）
- `docs/ai-provider.md` — AI 提供者策略 + 提示词模板

### Step 2 — 创建项目文件夹 & 保存架构文档

```bash
mkdir novel-ai-system
mkdir novel-ai-system/docs/
mkdir novel-ai-system/novel-backend/
mkdir novel-ai-system/novel-frontend/
```

保存上述 4 份架构文档到 `docs/` 目录。

---

## 阶段二：后端脚手架搭建

### Step 3 — Spring Boot 项目初始化

**依赖清单：**

```kotlin
// build.gradle.kts
implementation("spring-boot-starter-web")
implementation("spring-boot-starter-data-jpa")
implementation("spring-boot-starter-security")
implementation("spring-boot-starter-validation")
implementation("spring-boot-starter-webflux")  // 后随 Spring AI 迁移移除
implementation("jackson-module-kotlin")
implementation("kotlin-reflect")
implementation("postgresql")
implementation("flyway-core")
implementation("jjwt:0.12.5")
```

**项目结构：**

```
novel-backend/src/main/kotlin/com/novel/
├── NovelApplication.kt
├── config/          (SecurityConfig, CorsConfig, AiConfig)
├── entity/          (User, Novel, Chapter, Character, WorldBuilding, PlotOutline, Revision, StyleSetting)
├── repository/      (8 个 JPA 接口)
├── service/         (业务逻辑层)
├── controller/      (REST 控制器)
├── dto/             (请求/响应数据传输对象)
├── security/        (JWT 认证)
└── exception/       (全局异常处理)
```

### Step 4 — 创建 Flyway 迁移脚本

```sql
-- V1__init_schema.sql: 8 张表 + 外键 + 索引
-- V2__fix_jsonb_columns.sql: JSONB → TEXT 类型修复
```

---

## 阶段三：后端 Service + Controller + AI Provider

### Step 5 — Service 层实现（9 个）

| Service | 功能 |
|---------|------|
| AuthService | 注册 / 登录 / JWT 生成 |
| NovelService | 小说 CRUD + 所有权校验 |
| ChapterService | 章节 CRUD + 自动编号 + 字数统计 |
| CharacterService | 角色 CRUD |
| WorldBuildingService | 世界观 CRUD + 分类过滤 |
| PlotOutlineService | 大纲 CRUD + 序号排序 |
| StyleSettingService | 风格设定获取/更新 |
| RevisionService | 修订记录管理 |
| AiService | AI 生成：大纲/角色/章节/世界观/修订 + 增强功能（10 项） |

### Step 6 — AI Provider 层（自定义实现，后被 Spring AI 替换）

```
AiProvider (基类) → WebClient + OpenAI 兼容 API
  ├── DeepSeekProvider
  └── XiaoMiProvider
```

### Step 7 — Controller 层实现（5 个）

| Controller | 端点数 | 功能 |
|------------|--------|------|
| AuthController | 2 | 注册 / 登录 |
| NovelController | 5 | 小说 CRUD |
| ChapterController | 5 | 章节 CRUD |
| CharacterController | 5 | 角色 CRUD |
| AiController | 19 | AI 生成 + 增强功能 + 世界观/大纲/风格/修订 CRUD |

---

## 阶段四：前端开发

### Step 8 — Vite + React 项目初始化

**依赖：**

- React 18 + TypeScript
- Ant Design 5 (UI 组件库)
- React Router 6 (路由)
- Zustand (状态管理)
- React Query (服务端状态)
- Axios (HTTP)

**目录结构：**

```
novel-frontend/src/
├── api/          (7 个 API 模块)
├── pages/        (6 个页面)
├── components/   (通用组件)
├── stores/       (状态管理)
├── types/        (TypeScript 类型)
└── utils/        (工具函数)
```

### Step 9 — 页面开发

| 页面 | 路由 | 功能 |
|------|------|------|
| Login | `/login` | 用户登录 |
| Register | `/register` | 用户注册 |
| NovelList | `/novels` | 小说列表（CRUD） |
| NovelDashboard | `/novels/:id` | 小说详情面板（Tabs: 章节/角色/大纲/世界观） |
| ChapterEditor | `/novels/:novelId/chapters/:chapterId` | 章节编辑器（含 AI 辅助） |

---

## 阶段五：AI 功能增强

### Step 10 — 世界观创作增强

| 功能 | 后端方法 | API 端点 |
|------|----------|----------|
| 批量生成 | `generateWorldBatch` | `POST /novels/{id}/generate/world/batch` |
| 建议分类 | `suggestWorldCategories` | `POST /novels/{id}/generate/world/suggest-categories` |
| 细节扩展 | `expandWorldDetail` | `POST /world-building/{id}/expand` |
| 一致性检查 | `reviewWorldConsistency` | `POST /novels/{id}/world-building/review-consistency` |

### Step 11 — 大纲创作增强

| 功能 | 后端方法 | API 端点 |
|------|----------|----------|
| 大纲审查 | `reviewPlot` | `POST /novels/{id}/generate/plot/review` |
| 情节扩展 | `expandPlot` | `POST /novels/{id}/generate/plot/expand` |
| 分支建议 | `suggestPlotBranches` | `POST /novels/{id}/generate/plot/branches` |
| 角色弧整合 | `integrateCharacterArc` | `POST /novels/{id}/generate/plot/character-arc` |
| 伏笔分析 | `reviewForeshadowing` | `POST /novels/{id}/generate/plot/foreshadowing` |

### Step 12 — 对话式创作助手（StoryBuilder）

7 步递进式引导创作：

```
story_premise → genre_style → character_concept → world_concept → plot_outline → chapter_drafting → review_polish → complete
```

**新增文件：**

- `service/StoryBuilderService.kt` — 会话管理 + 7 步提示词模板
- `controller/StoryBuilderController.kt` — 3 个 API 端点
- `pages/StoryBuilderPage.tsx` — 向导式 UI（步骤条 + 聊天 + 选项按钮）
- `api/storyBuilder.ts` — 前端 API 层

---

## 阶段六：数据库部署与集成测试

### Step 13 — Docker Compose 部署 PostgreSQL

```yaml
services:
  postgres:
    image: postgres:16-alpine
    ports:
      - "5433:5432"
    environment:
      POSTGRES_DB: novel_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
```

### Step 14 — API 集成测试

```bash
# 注册 → 登录 → 创建小说 → 创建章节 → 创建角色 → 创建世界观 → 设置风格
```

所有端点返回 200，数据库 8 张表全部创建成功。

---

## 阶段七：代码质量与配置管理

### Step 15 — 全文件中文注释

| 范围 | 文件数 | 注释行数 |
|------|--------|----------|
| 后端 | 40 个文件 | — |
| 前端 | 19 个文件 | 221 行 |

### Step 16 — GitHub 仓库创建

```bash
git init
gh repo create ai-novel-creator --public
git push origin master
```

仓库地址：`https://github.com/fungTemin/ai-novel-creator`

### Step 17 — 敏感配置分离

- `application.yml` → 公共配置（占位符 API Key，可提交）
- `application-local.yml` → 本地开发配置（含真实 API Key，已 gitignore）

**激活方式：** `--spring.profiles.active=local`

### Step 18 — 创建开发分支

```bash
git branch develop
git push -u origin develop
```

后续所有开发在 `develop` 分支进行。

---

## 阶段八：架构重构 — Spring AI 迁移

### Step 19 — 替换自定义 AI Provider

**变更：**

| 删除 | 新增 |
|------|------|
| `ai/AiProvider.kt` | — |
| `ai/DeepSeekProvider.kt` | `config/AiProviderConfig.kt` |
| `ai/XiaoMiProvider.kt` | — |
| `ai/model/ChatModels.kt` | — |
| `spring-boot-starter-webflux` | `spring-ai-openai-spring-boot-starter` |

**架构变化：**

```
之前: AiProvider (WebClient) → DeepSeekProvider / XiaoMiProvider
现在: Spring AI ChatModel → OpenAiChatModel(DeepSeek) / OpenAiChatModel(Xiaomi)
```

**核心代码变化：**

```kotlin
// 之前
getProvider().generateCompletion(systemPrompt, userMessage)

// 现在
chatModel.call(Prompt(listOf(SystemMessage(systemPrompt), UserMessage(userMessage)))).result.output.text
```

**依赖：** `spring-ai-bom:1.0.0-M6`

**禁用自动配置：**

```yaml
spring:
  autoconfigure:
    exclude: org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration
```

---

## 阶段九：Bug 修复

### Step 20 — 弹窗卡死修复

| Bug | 原因 | 修复 |
|-----|------|------|
| 审查大纲参数弹窗无法关闭 | `open={!!(reviewForm && !reviewPlotModal)}` 恒真，`onCancel` 空函数 | 增加 `reviewParamModal` 独立状态 |
| 建议分类参数弹窗恒显示 | `open={!!(suggestForm && !suggestCategoryModal)}` 恒真 | 删除无用的空模态框 |

---

## 阶段十：后续计划

### Pending：UI/UX 打磨

| 任务 | 优先级 |
|------|--------|
| AI 流式输出（SSE 逐字显示生成内容） | 中 |
| Markdown 渲染预览 | 中 |
| 章节拖拽排序 | 低 |
| 响应式适配 | 低 |
| 全局 Loading + 骨架屏 | 低 |

### Pending：新功能

| 任务 | 优先级 |
|------|--------|
| 小说导出（Markdown / TXT / EPUB） | 高 |
| 多 Agent 联动系统（Orchestrator 编排） | 高 |
| AI 对话写作（侧边聊天面板） | 高 |
| 写作统计仪表盘 | 中 |
| 模板库 | 低 |

### Pending：质量保障

| 任务 | 优先级 |
|------|--------|
| Service 层单元测试 | 高 |
| Controller 层集成测试 | 高 |
| Docker Compose 全栈编排 | 中 |
| CI/CD (GitHub Actions) | 中 |

---

## Git 提交历史

```
c3aacbc fix: 修复建议分类模态框恒真导致的卡死问题
be6116f fix: 分离敏感配置至 application-local.yml 并修复弹窗卡死
c12842d feat: 增加 spring ai 对接
c4182af feat: AI 小说创作系统完整实现
05d8cee chore: init project with .gitignore
```

---

*最后更新：2026-04-30*
