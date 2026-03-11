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

## 基于当前代码的实现盘点（2026-03）

> 以下盘点以当前仓库实际代码为准，用于修正文档与实现之间的偏差，避免后续按过期假设继续拆任务。

### 已完成或接近完成

| 能力 | 当前状态 | 说明 |
|------|----------|------|
| 文章 CRUD / 详情 / 编辑 | ✅ 已完成 | 包含 Markdown 编辑、专题/标签/合集绑定、可见性切换、详情页展示 |
| 标签 / 专题体系 | ✅ 已完成 | 公开查询、后台管理、编辑器选择链路已形成闭环 |
| 点赞 / 收藏 / 评论点赞 | ✅ 已完成 | 数据库、实体、服务、控制器、前端交互均已落地 |
| 通知中心基础能力 | ✅ 已完成 | 已有事件、监听器、通知 API、前端铃铛与通知列表页 |
| 会员内容基础支持 | ✅ 基本可用 | 已支持 `MEMBERS_ONLY`、详情遮罩、编辑器设置 |
| 用户资料聚合 / 社区主页 | ✅ 已完成 | 已与 member 模块资料聚合打通 |
| 关注动作 | ⚠️ 部分完成 | 已有关注关系表、服务与按钮，但粉丝/关注列表、通知、动态流未补齐 |
| 产品关联 | ✅ 已完成 MVP | 公开模块页已按产品关联展示社区文章，后端接口已返回前端可直接消费的 `ArticleVO` |

### 与原计划不一致的点

1. 文档中“点赞/收藏/通知系统对接”为待实现，但代码中已基本落地。
2. 文档中关注系统处于第二阶段待实现，但当前仓库已经有基础关注关系和关注/取关动作。
3. 文档中仍将“无任何社交动作端点”作为核心缺失，这一判断已过期。
4. 当前真正优先的问题，已经从“无能力”转为“已有能力的安全收口与体验闭环”。

### 当前应优先收口的问题

1. **文章列表权限收口**：公开文章接口不应信任 `adminView` 查询参数决定是否放开过滤。
2. **合集公开可见性收口**：公开合集列表需补 `status` / `visibility` 默认过滤。
3. **评论审核可见性收口**：公开评论列表需明确只返回审核通过评论。
4. **通知评论定位补齐**：通知点击应支持精确跳转到评论锚点，而不仅是文章详情。
5. **合集创作闭环收口**：排序、备注等写侧能力仅保留在受保护编辑页，公开详情页不暴露创作者入口。

### 明确尚未开始

- `@` 提及解析与通知
- 教育版块、活动竞赛、认证体系

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

#### 当前落地进展（写回闭环已完成）

- 社区公开接口已新增 `GET /api/v1/community/profiles/leaderboard`
- 社区受保护接口已新增 `GET /api/v1/community/profiles/me/points-history`
- 当前排行榜通过 `UserProfileProvider` SPI 读取 member 模块中的积分榜快照，避免社区直接依赖会员实现
- `mortise-core` 已新增 `UserPointProvider` SPI，由 `mortise-member` 落地积分写回、等级映射与历史读取
- `mortise-member` 已新增 `mortise_member_point_history` 历史表，并通过 `bizKey` 做幂等去重，避免同一业务重复发分
- 社区已在文章首次发布、文章获赞、文章被收藏、发表评论四类稳定事件上接入积分写回
- 用户主页已展示当前积分与等级徽章，用户菜单已补“积分明细”入口
- 前端已新增公开 `community/leaderboard` 页面，展示前 20 名用户的排名、积分与等级标识
- 前端已新增受保护 `community/points-history` 页面，可查看个人积分变化记录

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

#### 当前落地进展（foundation 已完成）

- `mortise-core` 已新增 `UserBadgeProvider` SPI 与徽章模型，保持 community 与 member 的模块边界
- `mortise-member` 已新增 `mortise_badge` / `mortise_user_badge` 两张表，并预置 3 枚社区徽章：
  - `community_first_article`：首篇文章
  - `community_article_ten_likes`：单篇 10 赞达人
  - `community_author_hundred_likes`：累计百赞作者
- 社区事件监听器已在文章首次发布、文章获赞时自动判定并发放徽章
- 徽章发放已通过 `user_id + badge_id` 做幂等约束，避免重复授予
- 社区通知中心已新增“获得新徽章”系统通知
- 用户主页已新增“徽章墙”展示，公开资料接口会直接返回该用户已获得徽章列表
- 当前先落地社区内可稳定判定的 3 枚徽章；教材贡献、开源贡献、连续签到等跨域徽章暂未接入

#### 当前落地进展（体验打磨已完成）

- 徽章规则已新增 `community_first_comment`，用户发表首条评论时会自动获得“首条评论”徽章
- 顶部通知预览在读取到新的 `badge_awarded` 未读通知时，会弹出一次 toast 提示，避免用户只能被动进入通知中心查看
- `badge_awarded` 通知点击后会直接跳转到当前用户主页的“徽章墙”区域，减少查找路径
- 通知列表页同样已支持新徽章通知直达徽章墙

#### 当前落地进展（规则扩展已完成）

- 徽章规则已继续扩展两枚成长型徽章：
  - `community_five_articles`：累计发布 5 篇社区文章
  - `community_ten_comments`：累计发表 10 条社区评论
- 这两枚徽章继续沿用现有文章发布 / 评论发布事件判定，不引入新的跨模块依赖
- 用户主页徽章墙无需额外改造，达到条件后会自动展示新增徽章
- 当前阶段已覆盖“首次创作 / 持续创作 / 首次互动 / 持续互动 / 质量反馈”几类基础社区成就

#### 当前落地进展（数据库规则解释执行已完成）

- 徽章触发逻辑已从“代码硬编码徽章编码 + 阈值”改为“数据库定义规则，代码解释执行”
- `mortise_badge` 中的 `condition_type / condition_value` 现在是实际生效的规则来源
- `community` 模块只负责在文章发布、评论发布、点赞等事件里计算当前指标值，并按 `condition_type` 调用统一解释逻辑
- `member` 模块通过 `UserBadgeProvider.listBadgeDefinitions()` SPI 提供启用中的徽章定义，保持模块边界不变
- 现在新增同类社区徽章时，通常只需要插入新的 `mortise_badge` 规则数据，无需再修改 Java 中的阈值常量
- 当前解释器已支持的规则类型包括：
  - `article_publish_count`
  - `comment_publish_count`
  - `article_like_count`
  - `author_total_likes`

#### 当前落地进展（跨域徽章契约已完成）

- `UserBadgeProvider` 已新增按指标批量判定的入口：`awardBadges(UserBadgeMetricCommand)`
- 徽章解释执行进一步下沉到 `member` 模块：`community` 不再自行遍历徽章定义，而是只上报 `condition_type -> metricValue`
- 这使得其他业务模块后续也能复用同一入口：只要计算出指标并调用 SPI，即可按数据库规则自动发放徽章
- 已新增跨域徽章种子规则示例：
  - `product_reference_count`
  - `curriculum_citation_count`
  - `checkin_streak_days`
- 当前这三类规则已具备数据库定义与 SPI 承载能力，待对应模块补指标上报后即可生效

### 7. 产品关联内容体系增强

- 利用已有的 `ArticleProduct` 关联，在产品详情页自动聚合社区教程
- 新增"产品专栏"概念：每款开发板对应一个内容专区
- 文章列表支持按产品筛选
- 区分"官方教程"与"社区投稿"（通过文章标记或作者角色判断）

#### 当前落地进展（MVP 已完成）

- 后端 `GET /api/v1/community/articles/by-product/{id}` 已从裸 `Article` 返回改为 `ArticleVO`
- 该接口已补齐公开口径过滤：仅返回 `PUBLISHED + PUBLIC` 的文章，避免公共站点误展示私密或会员内容
- 站点 `modules` 页面已增加“相关文章”区块，按模块匹配公共产品后展示 3 篇关联社区文章
- 当前采用“内容配置可显式提供 `productId`，否则按 `productKeyword / 模块名` 匹配公共产品列表”的 MVP 策略，避免在内容文件中硬编码不稳定 ID
- 这一步先把“产品 -> 社区教程”链路接到站点模块总览页，后续若补公共产品详情页，可直接复用同一后端接口

### 8. @ 提及与文内引用

**后端**

- Markdown 内容解析 `@用户名` 语法
- 保存时提取被提及用户列表
- 触发 `UserMentionedEvent` → 通知被提及用户

**前端**

- Markdown 编辑器支持 `@` 自动补全（搜索用户名）
- 渲染时将 `@用户名` 展示为可点击链接

#### 当前落地进展（MVP 已完成）

- 后端已新增 `UserMentionedEvent`，文章保存时会从 TipTap mention markdown 中提取被提及用户 ID
- 当前仅对 `PUBLISHED + PUBLIC` 文章发送提及通知，避免草稿、私密或会员内容泄露
- 文章重复编辑时会跳过已存在 mention，仅对新增公开提及发送通知
- 前端文章编辑器已接入 `UEditorMentionMenu`，输入 `@` 即可远程搜索社区用户并插入 mention 节点
- 文章详情 Markdown 渲染前会先把 mention 语法转换为社区用户主页链接，读侧可直接点击跳转

### 进度状态

- [ ] 用户关注表迁移脚本
- [ ] UserFollow 实体与 FollowService
- [ ] FollowController
- [ ] 积分规则引擎（GamificationService）
- [x] 积分排行榜 API
- [ ] 徽章表迁移脚本
- [ ] Badge / UserBadge 实体与徽章触发逻辑
- [ ] 产品专栏聚合查询
- [x] @ 提及解析与通知
- [ ] 前端：关注/取关按钮与列表
- [x] 前端：积分/等级/排行榜页面
- [ ] 前端：徽章墙
- [x] 前端：编辑器 @ 自动补全

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

## 下一步可执行任务（按推荐顺序）

### 第一批：先收口已上线能力

1. **收口文章列表权限**
   - 目标：移除公开接口对 `adminView` 的越权信任。
   - 输出：公开文章列表只返回允许公开浏览内容；“我的草稿 / 归档”走安全专用查询。
   - 状态：✅ 已完成（已新增受保护的 `/community/articles/mine`，公开列表不再信任 `adminView`）

2. **收口合集公开列表**
   - 目标：默认过滤 `DISABLED`、`PRIVATE`、`MEMBERS_ONLY` 等不应公开展示的合集。
   - 输出：公开合集列表与详情访问策略一致。
   - 状态：✅ 已完成（公开合集分页已改为独立公开口径；匿名仅看 `PUBLIC`，会员可见 `MEMBERS_ONLY`，`PRIVATE` 不进入公开列表）

3. **收口评论审核可见性**
   - 目标：公开评论接口仅返回审核通过评论，后台审核接口保留全量。
   - 输出：评论审核语义与前台展示语义明确分离。
   - 状态：✅ 已完成（公开评论控制器已固定注入 `APPROVED` 状态，后台审核分页未受影响）

4. **补通知评论锚点定位**
   - 目标：通知跳转支持落到具体评论，而不是只打开文章页。
   - 输出：通知下拉与通知列表页统一支持评论级定位。
   - 状态：✅ 已完成（通知链接已带评论 hash，文章页会按需继续加载评论分页并滚动到目标评论）

### 第二批：补社区与合集之间的读写闭环

5. **补合集编辑页**
   - 目标：基于现有创建页补齐 update 链路。
   - 输出：合集具备创建 / 编辑完整闭环。
   - 状态：✅ 已完成（已补受保护编辑页并复用创建表单，创作中心改为左侧菜单切换文章/合集列表，编辑入口仅收录在合集列表视图）

6. **补合集成员管理（基础版）**
   - 目标：先做 direct add，不引入邀请确认。
   - 输出：支持添加成员、移除成员、调整角色。
   - 状态：✅ 已完成（后端已补成员增删改接口，前端已在合集编辑页提供 direct add、角色调整与移除；当前仅 OWNER 可管理成员）

7. **补合集排序与备注维护**
   - 目标：修正 `sortNo` 语义为合集内顺序，并开放 `note` 写侧。
   - 输出：合集从“文章聚合”升级为“可编排系列”。
   - 状态：✅ 已完成（合集编辑页已支持批量保存顺序与备注，公开详情页同步展示读侧信息）

8. **补文章详情页合集联动**
   - 目标：在文章详情页展示所属合集、系列上下篇和合集入口。
   - 输出：打通合集读侧联动体验。
   - 状态：✅ 已完成（文章详情接口已返回所属合集系列摘要与上下篇，前端详情页已接入所属系列区块）

### 第三批：再进入原规划第二阶段

9. **补关注生态闭环**
   - 在已有关注动作基础上补粉丝列表、关注列表、关注通知与动态流。
   - 状态：✅ 已完成（已补关注通知、粉丝/关注列表页与最小关注动态流，登录用户可查看所关注作者的最新公开文章）

10. **补产品内容联动前端闭环**
   - 将已有 `ArticleProduct` 查询能力接入产品详情页或社区筛选页面。

11. **再启动积分 / 徽章 / @ 提及**
   - 仅在第一、二批收口完成后推进，避免把既有半成品与新能力并行堆积。

### 最新进展补充（2026-03）

- 已补“关注通知闭环”第一刀：
  - 后端新增 `UserFollowedEvent`
  - 关注成功后发布事件并写入 `user_followed` 社区通知
  - 通知列表页与顶部铃铛预览已支持跳转到关注者主页
- 已补“粉丝 / 关注列表页”第二刀：
  - 新增用户主页下的粉丝列表页与关注列表页
  - 用户主页顶部统计卡可直接跳转到对应列表
  - 复用现有关注分页接口展示关注关系建立时间与用户简介
- 已补“关注动态流”第三刀：
  - 后端新增 `/api/v1/community/articles/following`
  - 前端新增 `community/feed` 页面并复用现有文章卡片
  - 顶部导航与用户菜单已增加“关注动态”入口
- 已移除个人主页中的编辑入口，公开主页仅保留浏览与互动能力
- 这一步将关注动作从“静默写库 + 数字变化”升级为“被关注者可感知的反馈”
- 下一步建议继续补：
  - 产品内容联动前端闭环
