# 社区运营模块调整计划

## 优先级：P0（核心增长飞轮）

## 背景

商业计划中社区承担的角色是**增长飞轮的核心引擎**：

```
学生课堂使用 → 毕业后继续购买 → 在社区分享经验 → 吸引更多关注 → 更多学校采用
```

要让飞轮转起来，社区必须具备三个关键能力：**留住用户、激励内容产出、连接产品与教育**。

## 现状评估

### 已有能力

| 功能 | 状态 |
|------|------|
| 文章 CRUD（Markdown、标签、专题） | ✅ 完整 |
| 层级评论（物化路径） | ✅ 完整 |
| 标签/专题分类 | ✅ 完整 |
| 文章-产品关联（`ArticleProduct`） | ✅ 完整 |
| 用户资料聚合（SPI 从 member 模块） | ✅ 完整 |
| 管理后台基础操作 | ✅ 完整 |
| 通知模块基础设施（多通道：In-app、邮件、微信） | ✅ 就绪 |

### 核心缺失

数据库中有 `likeCount`、`commentCount` 字段，但**没有任何社交动作端点**——无点赞、关注、收藏、积分、徽章、通知触发、活动流。

---

## 第一阶段：社交互动基础（0–3 个月）

对应路线图"0–6 个月"阶段，目标：**让用户愿意回来、愿意互动**。

### 1. 点赞/收藏系统

**后端**

- 新增数据库表：
  - `mortise_article_like`（user_id, article_id, created_time）
  - `mortise_article_favorite`（user_id, article_id, created_time）
  - `mortise_comment_like`（user_id, comment_id, created_time）
- 新增 Domain 实体：`ArticleLike`、`ArticleFavorite`、`CommentLike`
- 新增 API 端点：
  - `POST /api/v1/community/articles/{id}/like` — 点赞
  - `DELETE /api/v1/community/articles/{id}/like` — 取消点赞
  - `POST /api/v1/community/articles/{id}/favorite` — 收藏
  - `DELETE /api/v1/community/articles/{id}/favorite` — 取消收藏
  - `POST /api/v1/community/comments/{id}/like` — 评论点赞
  - `DELETE /api/v1/community/comments/{id}/like` — 取消评论点赞
  - `GET /api/v1/community/users/me/favorites` — 我的收藏列表
- 计数更新策略：点赞/收藏操作后异步更新 `likeCount` 字段，避免热点行锁竞争

**前端**

- 文章列表/详情页：点赞按钮（心形图标 + 计数）
- 文章详情页：收藏按钮（书签图标）
- 评论区：评论点赞按钮
- 新增"我的收藏"页面
- 登录状态下回显当前用户的点赞/收藏状态

**安全配置**

- `POST/DELETE /api/v1/community/articles/*/like` — MEMBER 角色
- `POST/DELETE /api/v1/community/articles/*/favorite` — MEMBER 角色
- `POST/DELETE /api/v1/community/comments/*/like` — MEMBER 角色
- `GET /api/v1/community/users/me/favorites` — MEMBER 角色

### 2. 通知系统对接

**后端**

- 新增领域事件：
  - `ArticleLikedEvent`
  - `CommentRepliedEvent`
  - `ArticleFavoritedEvent`
- 新增事件监听器 `CommunityEventListener`，订阅上述事件并调用已有的 notification 模块发送通知
- 通知场景：
  - 文章收到评论 → 通知文章作者（In-app + 可选邮件）
  - 评论收到回复 → 通知评论作者（In-app）
  - 文章被点赞 → 通知文章作者（In-app，合并同类项：「xxx 等 5 人赞了你的文章」）
- 新增 API 端点：
  - `GET /api/v1/community/notifications` — 通知列表（分页）
  - `PATCH /api/v1/community/notifications/{id}/read` — 标记已读
  - `PATCH /api/v1/community/notifications/read-all` — 全部已读
  - `GET /api/v1/community/notifications/unread-count` — 未读数

**前端**

- 导航栏通知铃铛图标 + 未读数角标
- 通知下拉面板 / 通知列表页
- 点击通知跳转到对应文章/评论

### 3. 内容可见性增强

- 利用已有的 `ContentVisibility.MEMBERS_ONLY` 枚举，实现会员专属内容
- 文章编辑器增加"会员专属"开关
- 前端对未登录/非会员用户展示模糊遮罩 + 登录/升级引导

### 进度状态

- [ ] 数据库迁移脚本（点赞/收藏表）
- [ ] ArticleLike / ArticleFavorite / CommentLike 实体
- [ ] SocialInteractionService（点赞/收藏业务逻辑）
- [ ] LikeController / FavoriteController
- [ ] 安全配置更新（CommunitySecurityConfigurer）
- [ ] 领域事件定义
- [ ] CommunityEventListener（对接 notification 模块）
- [ ] 通知 API 端点
- [ ] 前端：点赞/收藏按钮组件
- [ ] 前端：我的收藏页面
- [ ] 前端：通知铃铛 + 通知列表
- [ ] 前端：会员专属内容展示

---

## 第二阶段：激励与内容生态（3–9 个月）

对应路线图"6–18 个月"阶段，目标：**激励高质量内容产出，形成自循环**。

### 4. 用户关注系统

**后端**

- 新增数据库表：`mortise_user_follow`（follower_id, following_id, created_time）
- 新增 API 端点：
  - `POST /api/v1/community/users/{id}/follow` — 关注
  - `DELETE /api/v1/community/users/{id}/follow` — 取关
  - `GET /api/v1/community/users/{id}/followers` — 粉丝列表
  - `GET /api/v1/community/users/{id}/following` — 关注列表
- 新增事件：`UserFollowedEvent` → 通知被关注者
- member 模块 `followerCount`/`followingCount` 字段维护

**前端**

- 用户主页：关注/取关按钮、粉丝数/关注数展示
- 关注者/关注列表页面
- 关注动态流（可选，展示所关注用户的新文章）

### 5. 积分与声望系统

**后端**

- 利用 member 模块已有的 `points` 字段，定义积分规则：

| 行为 | 积分 |
|------|------|
| 发表文章 | +20 |
| 文章被点赞 | +2 |
| 发表评论 | +5 |
| 评论被采纳 | +10 |
| 每日签到 | +1 |
| 文章被收藏 | +3 |

- 声望等级体系：

| 等级 | 积分要求 | 称号 |
|------|---------|------|
| Lv.1 | 0 | 新手 |
| Lv.2 | 100 | 入门者 |
| Lv.3 | 500 | 贡献者 |
| Lv.4 | 2000 | 专家 |
| Lv.5 | 10000 | 布道师 |

- 新增 `GamificationService`，在社交动作事件监听中触发积分变更
- 新增 API 端点：
  - `GET /api/v1/community/leaderboard` — 积分排行榜
  - `GET /api/v1/community/users/{id}/points-history` — 积分变更记录

**前端**

- 用户主页展示等级徽章和积分
- 排行榜页面
- 积分明细页面

### 6. 徽章/成就系统

**后端**

- 新增数据库表：
  - `mortise_badge`（id, name, icon, description, condition_type, condition_value）
  - `mortise_user_badge`（user_id, badge_id, earned_time）
- 预设徽章：
  - 🏅 首篇文章 — 发表第一篇文章
  - ❤️ 10 赞达人 — 单篇文章获得 10 个赞
  - 📚 教材贡献者 — 内容被教材引用
  - 🔧 开源贡献者 — 提交硬件开源 PR
  - 🎯 连续签到 7 天
  - 🌟 百赞作者 — 累计获得 100 个赞
- 徽章触发在 `GamificationService` 中统一判定

**前端**

- 用户主页"徽章墙"展示
- 获得新徽章时的 Toast / 弹窗庆祝动画

### 7. 产品关联内容体系增强

- 利用已有的 `ArticleProduct` 关联，在产品详情页自动聚合社区教程
- 新增"产品专栏"概念：每款开发板对应一个内容专区
- 文章列表支持按产品筛选
- 区分"官方教程"与"社区投稿"（通过文章标记或作者角色判断）

### 8. @ 提及与文内引用

**后端**

- Markdown 内容解析 `@用户名` 语法
- 保存时提取被提及用户列表
- 触发 `UserMentionedEvent` → 通知被提及用户

**前端**

- Markdown 编辑器支持 `@` 自动补全（搜索用户名）
- 渲染时将 `@用户名` 展示为可点击链接

### 进度状态

- [ ] 用户关注表迁移脚本
- [ ] UserFollow 实体与 FollowService
- [ ] FollowController
- [ ] 积分规则引擎（GamificationService）
- [ ] 积分排行榜 API
- [ ] 徽章表迁移脚本
- [ ] Badge / UserBadge 实体与徽章触发逻辑
- [ ] 产品专栏聚合查询
- [ ] @ 提及解析与通知
- [ ] 前端：关注/取关按钮与列表
- [ ] 前端：积分/等级/排行榜页面
- [ ] 前端：徽章墙
- [ ] 前端：编辑器 @ 自动补全

---

## 第三阶段：教育与生态闭环（9–18 个月）

对应路线图"18–36 个月"阶段，目标：**服务学校和企业场景，建立生态壁垒**。

### 9. 教育版块

- "课程空间"：教师创建班级，学生提交作业（文章形式）
- 教学用标签体系（按教材章节自动分类）
- 学校管理员角色，查看班级学习数据
- 与 `mortise-member` 模块的会员等级联动（学校批量注册学生账号）

### 10. 活动与竞赛

- 社区活动模块：线上打卡、项目挑战赛
- 活动与产品线联动（例：ESP32 创客马拉松）
- 活动排行榜与奖品系统

### 11. 开发者等级认证

- 基于积分 + 作品质量颁发认证
- 认证开发者享受企业定制对接优先权
- 与教育背书形成闭环（学校用 → 社区学 → 认证 → 就业）

### 进度状态

- [ ] 课程空间数据模型设计
- [ ] 教师/班级管理 API
- [ ] 作业提交与批改流程
- [ ] 活动模块数据模型
- [ ] 活动创建/报名/打卡 API
- [ ] 开发者认证规则与审核流程
- [ ] 前端：课程空间页面
- [ ] 前端：活动广场页面
- [ ] 前端：认证申请与展示

---

## 架构调整

### 新增目录结构

```
mortise-community/
├── mortise-community-domain/
│   ├── entity/
│   │   ├── ArticleLike.java          ← 新增
│   │   ├── ArticleFavorite.java      ← 新增
│   │   ├── CommentLike.java          ← 新增
│   │   ├── UserFollow.java           ← 新增
│   │   ├── Badge.java                ← 新增（第二阶段）
│   │   └── UserBadge.java            ← 新增（第二阶段）
│   └── event/                         ← 新增
│       ├── ArticleLikedEvent.java
│       ├── ArticleFavoritedEvent.java
│       ├── CommentRepliedEvent.java
│       ├── UserFollowedEvent.java
│       └── UserMentionedEvent.java
├── mortise-community-application/
│   ├── SocialInteractionService.java  ← 新增
│   ├── GamificationService.java       ← 新增（第二阶段）
│   └── listener/                      ← 新增
│       └── CommunityEventListener.java
└── mortise-community-api/
    └── controller/
        ├── LikeController.java        ← 新增
        ├── FavoriteController.java     ← 新增
        ├── FollowController.java       ← 新增（第二阶段）
        └── NotificationController.java ← 新增
```

### 架构原则

- **事件驱动**：社交动作通过 Spring `ApplicationEvent` 解耦，notification 模块订阅处理
- **计数异步更新**：点赞数等高频写入使用异步 + 定期刷新，避免热点行锁竞争
- **保持 SPI 模式**：关注系统涉及 member/community 两个域，通过 SPI 接口解耦
- **JSONB 扩展字段**：利用已有的 `extData` 字段存储活动元数据，避免频繁 DDL

---

## 优先级总览

| 优先级 | 功能 | 对飞轮的作用 | 阶段 |
|--------|------|------------|------|
| **P0** | 点赞 + 收藏 | 最基础的互动反馈，没有这个社区是"死"的 | 第一阶段 |
| **P0** | 通知系统对接 | 评论回复无通知 = 用户流失 | 第一阶段 |
| **P1** | 关注系统 | 建立用户间社交关系，提升留存 | 第二阶段 |
| **P1** | 产品-内容联动增强 | 支撑"买板子→看教程→发作品"链路 | 第二阶段 |
| **P2** | 积分/声望/徽章 | 激励内容产出，形成社区氛围 | 第二阶段 |
| **P2** | @ 提及 | 提升互动质量 | 第二阶段 |
| **P3** | 教育版块 | 支撑学校场景，需要前面的基础 | 第三阶段 |
| **P3** | 活动/竞赛/认证 | 生态壁垒，长期价值 | 第三阶段 |
