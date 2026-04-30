# 数据库设计

## ER 图

```
┌─────────────┐       ┌─────────────┐
│    users    │       │   novels    │
├─────────────┤       ├─────────────┤
│ id (PK)     │──┐    │ id (PK)     │
│ username    │  │    │ user_id(FK) │◄──┐
│ email       │  └───►│ title       │   │
│ password    │       │ description │   │
│ created_at  │       │ genre       │   │
│ updated_at  │       │ status      │   │
└─────────────┘       │ created_at  │   │
                      │ updated_at  │   │
                      └─────────────┘   │
                            │           │
         ┌──────────────────┼───────────┤
         │                  │           │
         ▼                  ▼           │
┌──────────────┐  ┌──────────────┐     │
│   chapters   │  │  characters  │     │
├──────────────┤  ├──────────────┤     │
│ id (PK)      │  │ id (PK)      │     │
│ novel_id(FK) │  │ novel_id(FK) │     │
│ chapter_num  │  │ name         │     │
│ title        │  │ description  │     │
│ content      │  │ traits       │     │
│ status       │  │ background   │     │
│ word_count   │  │ relationships│     │
│ created_at   │  │ created_at   │     │
│ updated_at   │  │ updated_at   │     │
└──────────────┘  └──────────────┘     │
       │                                │
       ▼                                │
┌──────────────┐              ┌────────────────┐
│  revisions   │              │ world_building  │
├──────────────┤              ├────────────────┤
│ id (PK)      │              │ id (PK)        │
│ chapter_id   │              │ novel_id(FK)   │
│ orig_content │              │ category       │
│ revised_cont │              │ name           │
│ feedback     │              │ description    │
│ created_at   │              │ details (JSON) │
└──────────────┘              │ created_at     │
                              │ updated_at     │
                              └────────────────┘
                                      │
                              ┌───────┴────────┐
                              │ style_settings │
                              ├────────────────┤
                              │ id (PK)        │
                              │ novel_id(FK)   │
                              │ tone           │
                              │ perspective    │
                              │ audience       │
                              │ style_desc     │
                              └────────────────┘
```

## 表结构定义

### users — 用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PK | 主键 |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 邮箱 |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希 |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |

### novels — 小说表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PK | 主键 |
| user_id | BIGINT | FK → users.id, NOT NULL | 所属用户 |
| title | VARCHAR(200) | NOT NULL | 小说标题 |
| description | TEXT | | 小说简介 |
| genre | VARCHAR(50) | | 类型（玄幻、言情、科幻等） |
| status | VARCHAR(20) | DEFAULT 'draft' | 状态：draft / in_progress / completed |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |

### chapters — 章节表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PK | 主键 |
| novel_id | BIGINT | FK → novels.id, ON DELETE CASCADE | 所属小说 |
| chapter_number | INTEGER | NOT NULL | 章节序号 |
| title | VARCHAR(200) | | 章节标题 |
| content | TEXT | | 章节内容 |
| status | VARCHAR(20) | DEFAULT 'draft' | 状态：draft / writing / revised / completed |
| word_count | INTEGER | DEFAULT 0 | 字数 |
| summary | TEXT | | 章节摘要（用于上下文传递给 AI） |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |

**索引：** (novel_id, chapter_number) UNIQUE

### characters — 角色表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PK | 主键 |
| novel_id | BIGINT | FK → novels.id, ON DELETE CASCADE | 所属小说 |
| name | VARCHAR(100) | NOT NULL | 角色名 |
| role | VARCHAR(50) | | 角色类型：protagonist / antagonist / supporting |
| description | TEXT | | 外貌描述 |
| personality | TEXT | | 性格特征 |
| background | TEXT | | 背景故事 |
| relationships | JSONB | | 与其他角色的关系 |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |

### world_building — 世界观表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PK | 主键 |
| novel_id | BIGINT | FK → novels.id, ON DELETE CASCADE | 所属小说 |
| category | VARCHAR(50) | NOT NULL | 分类：geography / culture / magic_system / history / species |
| name | VARCHAR(200) | NOT NULL | 条目名称 |
| description | TEXT | | 简要描述 |
| details | JSONB | | 详细设定（JSON 格式，灵活存储） |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |

### plot_outlines — 大纲表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PK | 主键 |
| novel_id | BIGINT | FK → novels.id, ON DELETE CASCADE | 所属小说 |
| title | VARCHAR(200) | NOT NULL | 大纲标题 |
| summary | TEXT | | 大纲摘要 |
| order_index | INTEGER | NOT NULL | 排序序号 |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |

### revisions — 修订记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PK | 主键 |
| chapter_id | BIGINT | FK → chapters.id, ON DELETE CASCADE | 所属章节 |
| original_content | TEXT | | 原始内容 |
| revised_content | TEXT | | 修订后内容 |
| feedback | TEXT | | AI 反馈意见 |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |

### style_settings — 风格设定表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PK | 主键 |
| novel_id | BIGINT | FK → novels.id, ON DELETE CASCADE | 所属小说 |
| tone | VARCHAR(50) | | 基调：严肃 / 幽默 / 悲伤 / 轻松 |
| perspective | VARCHAR(50) | | 视角：第一人称 / 第三人称 / 上帝视角 |
| target_audience | VARCHAR(100) | | 目标读者 |
| style_description | TEXT | | 风格描述 |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |
