# 系统架构设计

## 整体架构

```
┌─────────────────────────────────────────────────┐
│                   前端 (React)                    │
│  ┌─────────┐ ┌──────────┐ ┌──────────────────┐  │
│  │  页面    │ │  组件库   │ │   状态管理        │  │
│  │ (Pages)  │ │ (AntD)   │ │ (Zustand)        │  │
│  └─────────┘ └──────────┘ └──────────────────┘  │
│                     │ HTTP (Axios)               │
└─────────────────────┼───────────────────────────┘
                      │
┌─────────────────────┼───────────────────────────┐
│                后端 (Spring Boot)                  │
│  ┌──────────────────────────────────────────┐    │
│  │              Controller 层                │    │
│  │  AuthController │ NovelController │ ...   │    │
│  └──────────────┬───────────────────────────┘    │
│  ┌──────────────┴───────────────────────────┐    │
│  │              Service 层                   │    │
│  │  AuthService │ NovelService │ AiService   │    │
│  └──────────────┬───────────────────────────┘    │
│  ┌──────────────┴───────────────────────────┐    │
│  │           Repository 层                   │    │
│  │        (Spring Data JPA)                  │    │
│  └──────────────────────────────────────────┘    │
│  ┌──────────────────────────────────────────┐    │
│  │          AI Provider 层                   │    │
│  │  AiProvider │ DeepSeekProvider │ XiaoMi   │    │
│  └──────────────────────────────────────────┘    │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────┼───────────────────────────┐
│               数据层                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│  │PostgreSQL│  │  Flyway   │  │   Redis(可选) │   │
│  └──────────┘  └──────────┘  └──────────────┘   │
└─────────────────────────────────────────────────┘
```

## 后端架构

### 技术选型
- **Kotlin** — 现代、简洁、空安全的 JVM 语言
- **Spring Boot 3** — 成熟的企业级框架，生态丰富
- **Spring Security** — 认证授权
- **Spring Data JPA** — ORM 和数据库访问
- **Flyway** — 数据库版本管理
- **JWT** — 无状态认证令牌

### 包结构
```
com.novel
├── NovelApplication.kt          # 启动类
├── config/                      # 配置类
│   ├── SecurityConfig.kt        # 安全配置
│   ├── JwtConfig.kt             # JWT 配置
│   ├── CorsConfig.kt            # 跨域配置
│   └── AiConfig.kt              # AI 提供者配置
├── entity/                      # JPA 实体
│   ├── User.kt
│   ├── Novel.kt
│   ├── Chapter.kt
│   ├── Character.kt
│   ├── WorldBuilding.kt
│   ├── PlotOutline.kt
│   ├── Revision.kt
│   └── StyleSetting.kt
├── repository/                  # 数据访问层
├── service/                     # 业务逻辑层
│   ├── AuthService.kt
│   ├── NovelService.kt
│   ├── ChapterService.kt
│   ├── CharacterService.kt
│   ├── WorldBuildingService.kt
│   ├── PlotService.kt
│   ├── RevisionService.kt
│   └── AiService.kt
├── controller/                  # REST 控制器
│   ├── AuthController.kt
│   ├── NovelController.kt
│   ├── ChapterController.kt
│   ├── CharacterController.kt
│   ├── WorldBuildingController.kt
│   ├── PlotController.kt
│   └── AiController.kt
├── dto/                         # 数据传输对象
│   ├── request/
│   └── response/
├── security/                    # 安全模块
│   ├── JwtTokenProvider.kt
│   ├── JwtAuthenticationFilter.kt
│   └── UserDetailsServiceImpl.kt
└── exception/                   # 异常处理
    ├── GlobalExceptionHandler.kt
    └── ResourceNotFoundException.kt
```

## 前端架构

### 技术选型
- **React 18** — 声明式 UI 库
- **TypeScript** — 类型安全
- **Vite** — 极速构建工具
- **Ant Design** — 企业级 UI 组件库
- **Zustand** — 轻量状态管理
- **React Query** — 服务端状态管理
- **React Router** — 客户端路由
- **Axios** — HTTP 客户端

### 目录结构
```
src/
├── main.tsx                     # 入口文件
├── App.tsx                      # 根组件
├── router.tsx                   # 路由配置
├── api/                         # API 封装
│   ├── client.ts                # Axios 实例
│   ├── auth.ts
│   ├── novel.ts
│   ├── chapter.ts
│   ├── character.ts
│   ├── worldBuilding.ts
│   └── ai.ts
├── pages/                       # 页面组件
│   ├── Login.tsx
│   ├── Register.tsx
│   ├── NovelList.tsx
│   ├── NovelDashboard.tsx
│   ├── ChapterEditor.tsx
│   ├── CharacterPage.tsx
│   └── WorldBuildingPage.tsx
├── components/                  # 通用组件
│   ├── Layout/
│   ├── NovelCard/
│   ├── ChapterTree/
│   ├── CharacterForm/
│   ├── AiChat/
│   └── MarkdownEditor/
├── stores/                      # Zustand 状态
│   ├── authStore.ts
│   └── novelStore.ts
├── types/                       # TypeScript 类型
│   ├── novel.ts
│   ├── chapter.ts
│   ├── character.ts
│   └── api.ts
└── utils/                       # 工具函数
    ├── token.ts
    └── format.ts
```

## 认证流程

```
用户登录 ──→ 后端验证 ──→ 生成 JWT Token
                              │
                              ▼
                        返回 Token 给前端
                              │
                              ▼
                        前端存储 Token (localStorage)
                              │
                              ▼
                   后续请求携带 Authorization: Bearer <token>
                              │
                              ▼
                   JwtAuthenticationFilter 验证 Token
                              │
                              ▼
                    SecurityContext 设置用户信息
```
