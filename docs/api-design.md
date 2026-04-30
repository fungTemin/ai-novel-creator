# API 接口设计

## 基础信息

- **Base URL:** `http://localhost:8080/api`
- **Content-Type:** `application/json`
- **认证方式:** JWT Bearer Token（除登录/注册外）

## 认证接口

### POST /api/auth/register — 用户注册

**Request:**
```json
{
  "username": "writer01",
  "email": "writer@example.com",
  "password": "securePass123"
}
```

**Response (200):**
```json
{
  "id": 1,
  "username": "writer01",
  "email": "writer@example.com",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### POST /api/auth/login — 用户登录

**Request:**
```json
{
  "username": "writer01",
  "password": "securePass123"
}
```

**Response (200):**
```json
{
  "id": 1,
  "username": "writer01",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## 小说接口

### GET /api/novels — 获取小说列表

**Response (200):**
```json
[
  {
    "id": 1,
    "title": "修仙之路",
    "description": "一个平凡少年的修仙故事",
    "genre": "玄幻",
    "status": "in_progress",
    "chapterCount": 12,
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-20T15:30:00"
  }
]
```

### POST /api/novels — 创建小说

**Request:**
```json
{
  "title": "修仙之路",
  "description": "一个平凡少年的修仙故事",
  "genre": "玄幻"
}
```

**Response (201):** 返回创建的小说对象

### GET /api/novels/{id} — 获取小说详情

### PUT /api/novels/{id} — 更新小说

### DELETE /api/novels/{id} — 删除小说

---

## 章节接口

### GET /api/novels/{novelId}/chapters — 获取章节列表

**Response (200):**
```json
[
  {
    "id": 1,
    "chapterNumber": 1,
    "title": "第一章 山村少年",
    "status": "completed",
    "wordCount": 3500,
    "summary": "主角出生于偏远山村...",
    "createdAt": "2024-01-15T10:00:00"
  }
]
```

### POST /api/novels/{novelId}/chapters — 创建章节

### GET /api/chapters/{id} — 获取章节详情（含完整内容）

### PUT /api/chapters/{id} — 更新章节

### DELETE /api/chapters/{id} — 删除章节

### PUT /api/novels/{novelId}/chapters/reorder — 重新排序章节

---

## 角色接口

### GET /api/novels/{novelId}/characters — 获取角色列表

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "林风",
    "role": "protagonist",
    "description": "黑发少年，身材瘦削但目光坚毅",
    "personality": "坚韧不拔、重情重义",
    "background": "出生于偏远山村，父母早亡...",
    "relationships": {"2": "师徒关系", "3": "青梅竹马"}
  }
]
```

### POST /api/novels/{novelId}/characters — 创建角色

### GET /api/characters/{id} — 获取角色详情

### PUT /api/characters/{id} — 更新角色

### DELETE /api/characters/{id} — 删除角色

---

## 世界观接口

### GET /api/novels/{novelId}/world-building — 获取世界观条目列表

**Query 参数:**
- `category` — 按分类过滤（geography / culture / magic_system / history / species）

**Response (200):**
```json
[
  {
    "id": 1,
    "category": "magic_system",
    "name": "灵气修炼体系",
    "description": "修仙世界的力量体系...",
    "details": {
      "levels": ["练气", "筑基", "金丹", "元婴", "化神"],
      "source": "天地灵气",
      "special_rules": ["双修功法需两人配合", "渡劫需要天劫锤炼"]
    }
  }
]
```

### POST /api/novels/{novelId}/world-building — 创建世界观条目

### GET /api/world-building/{id} — 获取条目详情

### PUT /api/world-building/{id} — 更新条目

### DELETE /api/world-building/{id} — 删除条目

---

## 大纲接口

### GET /api/novels/{novelId}/outlines — 获取大纲列表

### POST /api/novels/{novelId}/outlines — 创建大纲条目

### PUT /api/outlines/{id} — 更新大纲

### DELETE /api/outlines/{id} — 删除大纲

---

## 修订接口

### GET /api/chapters/{chapterId}/revisions — 获取修订历史

### POST /api/chapters/{chapterId}/revisions — 创建修订记录

---

## 风格设定接口

### GET /api/novels/{novelId}/style — 获取风格设定

### PUT /api/novels/{novelId}/style — 更新风格设定

---

## AI 生成接口

### POST /api/novels/{novelId}/generate/plot — AI 生成大纲

**Request:**
```json
{
  "prompt": "一个现代程序员穿越到修仙世界",
  "chapterCount": 20,
  "genre": "玄幻"
}
```

**Response (200):**
```json
{
  "outlines": [
    {
      "title": "第一章 穿越重生",
      "summary": "程序员李明意外穿越到修仙世界..."
    },
    {
      "title": "第二章 灵根觉醒",
      "summary": "李明发现自己拥有罕见的天灵根..."
    }
  ]
}
```

### POST /api/novels/{novelId}/generate/character — AI 生成角色

**Request:**
```json
{
  "prompt": "一个冷酷但内心温柔的女剑修",
  "role": "supporting"
}
```

### POST /api/novels/{novelId}/generate/chapter — AI 生成章节内容

**Request:**
```json
{
  "chapterId": 5,
  "instructions": "延续上一章的紧张氛围，主角与反派首次交锋",
  "wordCount": 3000
}
```

**Response (200):**
```json
{
  "content": "山风呼啸，林风站在悬崖边...\n\n...(AI 生成的完整章节内容)",
  "wordCount": 2856
}
```

### POST /api/novels/{novelId}/generate/world — AI 生成世界观

**Request:**
```json
{
  "category": "magic_system",
  "prompt": "基于灵气修炼的力量体系",
  "detail_level": "detailed"
}
```

### POST /api/chapters/{chapterId}/revise — AI 修订章节

**Request:**
```json
{
  "selected_text": "他走了过去。",  // 可选，为空则修订整章
  "instructions": "让这段描写更加生动，增加环境细节",
  "style": "enhance"  // enhance / simplify / formalize / emotional
}
```

**Response (200):**
```json
{
  "revised_content": "他迈开沉重的步伐，穿过弥漫着薄雾的林间小道...",
  "feedback": "增加了环境描写和动作细节，使场景更加生动",
  "changes": [
    {"type": "enhancement", "description": "添加了林间薄雾的环境描写"},
    {"type": "enhancement", "description": "用'沉重的步伐'替代了'走了过去'"}
  ]
}
```

---

## 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "参数校验失败",
  "errors": [
    {"field": "title", "message": "标题不能为空"}
  ]
}
```

### 分页响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [ ... ],
    "totalElements": 100,
    "totalPages": 10,
    "currentPage": 0,
    "pageSize": 10
  }
}
```
