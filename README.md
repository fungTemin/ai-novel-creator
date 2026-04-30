# AI 小说创作系统

> 基于人工智能的辅助小说创作平台 — 从构思到完稿的全流程 AI 辅助写作

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?logo=spring)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite)](https://vitejs.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-9-CC0200?logo=flyway)](https://flywaydb.org/)

---

## 目录

- [概览](#概览)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
  - [前置条件](#前置条件)
  - [1. 启动数据库](#1-启动数据库)
  - [2. 启动后端](#2-启动后端)
  - [3. 启动前端](#3-启动前端)
- [项目结构](#项目结构)
- [API 概览](#api-概览)
- [AI 功能详解](#ai-功能详解)
  - [世界观创作](#世界观创作)
  - [大纲创作](#大纲创作)
  - [章节写作与修订](#章节写作与修订)
- [开发进度](#开发进度)
- [后续规划](#后续规划)

---

## 概览

**AI 小说创作系统**是一个面向网络小说作者的全栈 Web 应用，利用大语言模型（DeepSeek / 小米 AI）辅助完成从故事构思、世界观构建、角色设计到逐章写作与编辑修订的全部创作流程。

### 设计理念

- **AI 辅助而非替代** — AI 生成内容后作者可自由修改和调整
- **上下文连贯** — 每次 AI 调用都注入前文摘要、角色档案和世界观设定
- **多维度创作** — 大纲、角色、世界观、风格多维度协同工作
- **渐进式交付** — 按 MVP（最小可行产品）思路逐步迭代

---

## 功能特性

### 基础管理

| 模块 | 功能 |
|------|------|
| 用户系统 | JWT 认证登录注册 |
| 小说管理 | 创建/编辑/删除/多作品切换 |
| 章节管理 | 增删改查、自动序号、字数统计 |
| 角色管理 | 角色档案、分类（主角/反派/配角） |
| 风格设定 | 基调、视角、目标读者、风格描述 |
| 修订历史 | 自动保存 AI 修订记录 |

### AI 世界观创作

| 功能 | 描述 |
|------|------|
| 单条生成 | 按分类生成单条世界观设定（geography / magic_system / history 等） |
| **批量生成** | 一次指定多个分类，每个分类生成多条设定，自动避免重复 |
| **建议分类** | AI 分析小说类型和已有分类，推荐还需要构建的领域 |
| **一致性检查** | 检测所有设定之间的事实/逻辑矛盾、力量体系失衡、时间线冲突 |
| **细节扩展** | 在现有设定基础上，按指定方向扩展细节描述和内部机制 |

### AI 大纲创作

| 功能 | 描述 |
|------|------|
| 大纲生成 | 根据故事构思生成完整章节大纲（标题 + 摘要） |
| **一致性审查** | 审查大纲的逻辑性、节奏、角色弧线，指出漏洞和问题 |
| **情节扩展** | 在现有大纲基础上生成支线/过渡/高潮等补充情节节点 |
| **分支建议** | 在关键情节点提供多个不同的发展方向及后续影响分析 |
| **角色弧线整合** | 分析角色在大纲中的参与度，建议最佳出场和成长时机 |
| **伏笔分析** | 分析伏笔设计，建议埋设位置和回收时机 |

### AI 章节写作与修订

| 功能 | 描述 |
|------|------|
| 章节生成 | 整合风格/前文摘要/角色/世界观，生成完整章节内容 |
| 内容修订 | 支持选中文本或整章修订，增强描写/简化/正式化/情感化 |
| 修订反馈 | 自动生成修改说明和改动列表 |

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端语言 | Kotlin |
| 后端框架 | Spring Boot 3.2.5 + Spring Security + Spring Data JPA |
| 数据库 | PostgreSQL 16 + Flyway 版本管理 |
| 认证 | JWT（jjwt 0.12） |
| AI 接口 | OpenAI 兼容 API → DeepSeek / 小米 AI |
| HTTP 客户端 | Spring WebFlux WebClient |
| 前端框架 | React 18 + TypeScript |
| 构建工具 | Vite 5 |
| UI 组件 | Ant Design 5 |
| 状态管理 | Zustand + React Query |

---

## 快速开始

### 前置条件

- JDK 17+
- Node.js 18+
- Docker（推荐）或本地 PostgreSQL 16
- DeepSeek API Key（[注册获取](https://platform.deepseek.com/)）或小米 AI API Key

### 1. 启动数据库

使用 Docker Compose 一键启动 PostgreSQL：

```bash
cd novel-ai-system
docker compose up -d
```

数据库将在 `localhost:5433` 监听，默认用户/密码 `postgres/postgres`，数据库名 `novel_db`。

Flyway 会在后端启动时自动创建所有表并执行迁移。

### 2. 启动后端

```bash
cd novel-backend

# 首次需要配置 AI API Key（可选，不配置不影响 CRUD）
export DEEPSEEK_API_KEY=your-key-here

# 启动
gradle bootRun
```

后端将在 `localhost:8080` 启动。

> 也可使用 `gradle build -x test` 构建 JAR 后通过 `java -jar` 运行。

### 3. 启动前端

```bash
cd novel-frontend
npm install
npm run dev
```

前端将在 `localhost:3000` 启动，已配置代理转发 `/api` 到后端 8080 端口。

### 验证运行

```bash
# 注册
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"writer","email":"writer@test.com","password":"pass123"}'

# 登录获取 token
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"writer","password":"pass123"}'

# 创建小说（需要替换 TOKEN）
curl -X POST http://localhost:8080/api/novels \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -d '{"title":"我的第一部小说","genre":"玄幻"}'
```

---

## 项目结构

```
novel-ai-system/
├── docker-compose.yml                     # PostgreSQL 容器配置
├── .gitignore
│
├── docs/                                  # 设计文档
│   ├── architecture.md                    # 系统架构设计
│   ├── database.md                        # 数据库 ER 图与表结构
│   ├── api-design.md                      # 完整 REST API 接口定义
│   └── ai-provider.md                     # AI Provider 设计与提示词模板
│
├── novel-backend/                         # Spring Boot 后端
│   ├── build.gradle.kts                   # Gradle 依赖管理
│   ├── settings.gradle.kts
│   └── src/main/
│       ├── kotlin/com/novel/
│       │   ├── NovelApplication.kt        # 启动类
│       │   ├── ai/                        # AI 提供者层
│       │   │   ├── AiProvider.kt          # 抽象基类（OpenAI 兼容调用）
│       │   │   ├── DeepSeekProvider.kt    # DeepSeek 实现
│       │   │   ├── XiaoMiProvider.kt      # 小米 AI 实现
│       │   │   └── model/ChatModels.kt    # 请求/响应数据模型
│       │   ├── config/                    # 配置（Security, CORS, AI）
│       │   ├── controller/                # REST 控制器（5 个）
│       │   ├── dto/                       # 请求/响应 DTO
│       │   ├── entity/                    # JPA 实体（8 个）
│       │   ├── exception/                 # 全局异常处理
│       │   ├── repository/                # Spring Data JPA 接口（8 个）
│       │   ├── security/                  # JWT 认证过滤器
│       │   └── service/                   # 业务逻辑（9 个 Service）
│       └── resources/
│           ├── application.yml            # 应用配置
│           └── db/migration/              # Flyway 迁移脚本
│
└── novel-frontend/                        # React 前端
    ├── index.html
    ├── package.json
    ├── vite.config.ts                     # Vite 配置（含 API 代理）
    ├── tsconfig.json
    └── src/
        ├── main.tsx                       # 入口
        ├── App.tsx                        # 路由
        ├── api/                           # Axios API 层
        ├── components/                    # 通用组件
        ├── pages/                         # 页面（Login, Register, NovelList, NovelDashboard, ChapterEditor）
        ├── stores/                        # Zustand 状态管理
        ├── types/                         # TypeScript 类型定义
        └── index.css
```

---

## API 概览

| 组 | 端点 | 说明 |
|----|------|------|
| **认证** | `POST /api/auth/register` | 用户注册 |
| | `POST /api/auth/login` | 用户登录 |
| **小说** | `GET/POST /api/novels` | 列表 / 创建 |
| | `GET/PUT/DELETE /api/novels/{id}` | 详情 / 更新 / 删除 |
| **章节** | `GET/POST /api/novels/{id}/chapters` | 列表 / 创建 |
| | `GET/PUT/DELETE /api/chapters/{id}` | 详情 / 更新 / 删除 |
| **角色** | `GET/POST /api/novels/{id}/characters` | 列表 / 创建 |
| | `GET/PUT/DELETE /api/characters/{id}` | 详情 / 更新 / 删除 |
| **世界观** | `GET/POST /api/novels/{id}/world-building` | 列表（支持 category 过滤）/ 创建 |
| | `PUT/DELETE /api/world-building/{id}` | 更新 / 删除 |
| **大纲** | `GET/POST /api/novels/{id}/outlines` | 列表 / 创建 |
| | `PUT/DELETE /api/outlines/{id}` | 更新 / 删除 |
| **风格** | `GET/PUT /api/novels/{id}/style` | 查看 / 更新风格设定 |
| **修订** | `GET /api/chapters/{id}/revisions` | 查看修订历史 |
| **AI 生成** | `POST /api/novels/{id}/generate/plot` | 生成大纲 |
| | `POST /api/novels/{id}/generate/character` | 生成角色 |
| | `POST /api/novels/{id}/generate/chapter` | 生成章节内容 |
| | `POST /api/novels/{id}/generate/world` | 生成世界观 |
| | `POST /api/novels/{id}/generate/world/batch` | 批量生成世界观 |
| | `POST /api/novels/{id}/generate/world/suggest-categories` | 建议分类 |
| | `POST /api/novels/{id}/generate/plot/review` | 审查大纲 |
| | `POST /api/novels/{id}/generate/plot/expand` | 扩展情节 |
| | `POST /api/novels/{id}/generate/plot/branches` | 分支建议 |
| | `POST /api/novels/{id}/generate/plot/character-arc` | 角色弧线整合 |
| | `POST /api/novels/{id}/generate/plot/foreshadowing` | 伏笔分析 |
| | `POST /api/world-building/{id}/expand` | 扩展世界观细节 |
| | `POST /api/novels/{id}/world-building/review-consistency` | 世界观一致性检查 |
| | `POST /api/chapters/{id}/revise` | AI 修订章节 |

完整接口文档见 `docs/api-design.md`。

---

## AI 功能详解

### 世界观创作

每个 AI 调用都会注入已有的世界观上下文，确保新设定与旧设定保持自洽。

```
用户输入: 分类 + 描述
     │
     ▼
 构建提示词（含已有设定、风格、角色信息）
     │
     ▼
 调用 DeepSeek / 小米 AI
     │
     ▼
 解析 JSON 响应 → 写入数据库
     │
     ▼
 检查一致性（可选）
```

### 大纲创作

```
用户输入: 故事构思 + 章节数
     │
     ▼
 生成初始大纲
     │
     ├── 审查一致性/节奏/角色弧
     ├── 在关键节点 → 建议多分支
     ├── 在薄弱环节 → 扩展支线/过渡
     ├── 整合角色成长弧线
     └── 分析伏笔设计
```

### 章节写作与修订

章节生成时自动注入以下上下文，确保连贯性：

- **风格设定**: 视角（第一/三人称）、基调（严肃/幽默）、目标读者
- **前文摘要**: 最近 3 章摘要（滑动窗口机制）
- **角色档案**: 核心角色的性格和背景
- **世界观**: 所有相关设定条目

---

## 开发进度

### 第一阶段 — 核心架构 ✅

- [x] 系统架构设计
- [x] 数据库设计（8 表 + Flyway 迁移）
- [x] REST API 接口设计（19 个端���）
- [x] AI Provider 层设计（提示词模板）

### 第二阶段 — 后端开发 ✅

- [x] Spring Boot 项目搭建（Kotlin）
- [x] JPA 实体与 Repository 层（8 个）
- [x] Service 业务逻辑层（9 个）
- [x] AI Provider 实现（DeepSeek + 小米 AI）
- [x] AiService（6 种 AI 生成 + 10 种增强功能）
- [x] JWT 认证体系
- [x] REST 控制器（5 个）
- [x] 全局异常处理

### 第三阶段 — 前端开发 ✅

- [x] Vite + React + TypeScript 项目搭建
- [x] Ant Design UI 框架集成
- [x] 用户登录/注册
- [x] 小说列表管理
- [x] 小说详情面板（Tabs: 章节/角色/大纲/世界观）
- [x] 章节编辑器（含 AI 辅助）
- [x] 增强功能 UI（批量生成、审查、扩展等）

### 第四阶段 — 数据库 & 集成测试 ✅

- [x] PostgreSQL 容器化部署
- [x] Flyway 自动化迁移（V1 建表 + V2 类型修复）
- [x] API 端点全部测试通过

### 第五阶段 — 打磨优化 ⬜

| 任务 | 优先级 | 状态 |
|------|--------|------|
| **UI/UX 优化**: 修复弹窗交互流程、Loading 状态、错误提示 | 高 | ⬜ |
| **AI 流式输出**: SSE 实现逐 token 显示生成内容 | 中 | ⬜ |
| **Markdown 渲染**: 预览模式下支持富文本渲染 | 中 | ⬜ |
| **章节拖拽排序**: 支持在章节列表中拖拽重排序 | 低 | ⬜ |
| **响应式适配**: 适配平板和手机端 | 低 | ⬜ |

### 第六阶段 — 新功能 ⬜

| 任务 | 优先级 | 状态 |
|------|--------|------|
| **小说导出**: 支持 Markdown / TXT / EPUB 导出 | 高 | ⬜ |
| **AI 对话写作**: 与 AI 对话式协作创作 | 高 | ⬜ |
| **写作统计**: 字数趋势、写作速度、章节分布图表 | 中 | ⬜ |
| **多语言支持**: i18n 国际化 | 低 | ⬜ |
| **模板库**: 提供各类型小说的预设风格和世界观模板 | 低 | ⬜ |

### 第七阶段 — 测试 & 部署 ⬜

| 任务 | 优先级 | 状态 |
|------|--------|------|
| **单元测试**: Service 层 + Controller 层 | 高 | ⬜ |
| **集成测试**: 完整 API 测试套件 | 高 | ⬜ |
| **Docker 化**: 前后端容器化编排 | 中 | ⬜ |
| **CI/CD**: GitHub Actions 自动化构建 | 中 | ⬜ |

---

## 后续规划

### 短期（第五阶段 — UI/UX 打磨）

1. **修复弹窗交互**: 当前"建议分类""审查大纲"等弹窗在触发前有一个多余的空表单弹窗，需要合并为一步操作
2. **全局 Loading**: 为所有异步操作添加统一的加载状态和骨架屏
3. **优化章节编辑器**: 添加自动保存草稿、字数实时统计、Markdown 快捷工具栏
4. **AI 流式输出**: 章节生成时使用 SSE 流式逐字显示内容，提升用户体验

### 中期（第六阶段 — 新功能）

1. **小说导出**: 后端实现 Markdown/TXT/EPUB 导出，前端添加导出按钮
2. **AI 聊天模式**: 在写作页面增加侧边 AI 对话面板，实时提问和建议
3. **写作仪表盘**: 使用 ECharts/AntV 实现写作趋势、字数统计等可视化

### 长期（第七阶段 — 质量保障）

1. **自动化测试**: 使用 Spring MockMvc + JUnit 5 + Kotest 编写单元测试和集成测试
2. **Docker Compose 全栈**: 添加前端 Nginx 和后端容器到 docker-compose
3. **CI/CD 流水线**: GitHub Actions 自动构建、测试、部署

---

## 常见问题

### Q: 如何切换 AI 提供者？

修改 `application.yml` 中的 `ai.default-provider` 为 `deepseek` 或 `xiaomi`，并配置对应 API Key。

### Q: 不配置 AI API Key 能使用吗？

可以。CRUD 操作完全不需要 AI。只有 AI 生成功能会调用 API，未配置时会返回错误提示。

### Q: 数据库端口被占用怎么办？

修改 `docker-compose.yml` 中的 `ports` 映射（如 `5434:5432`），同步更新 `application.yml` 中的数据库连接 URL。
