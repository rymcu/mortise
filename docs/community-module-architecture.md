# 社区模块架构文档

> `mortise-community` — 内容社区模块，提供文章发布、评论互动、社交关注、内容审核、游戏化等功能。
> 本模块通过 Maven `pro` profile 激活，属于商业扩展层。

## 子模块结构

```
mortise-community/
├── mortise-community-domain        # 领域层：实体、枚举、值对象、请求/响应模型
├── mortise-community-application   # 应用层：Service 接口/实现、事件、监听器、支撑类
├── mortise-community-infra         # 基础设施层：MyBatis-Flex Mapper、Flyway 迁移脚本
├── mortise-community-admin         # 管理端控制器与 DTO
└── mortise-community-api           # 公开 API 控制器与 DTO
```

依赖方向：`admin/api → application → infra → domain`，遵循全局分层规则（见 `AGENTS.md`）。

## 实体一览（24 个）

### 核心内容

| 实体 | 表名 | 说明 |
|------|------|------|
| `Article` | `mortise_article` | 文章主体（标题、正文、状态、可见性、互动计数） |
| `Comment` | `mortise_comment` | 层级评论（物化路径、根评论、回复目标用户） |
| `Tag` | `mortise_tag` | 标签 |
| `Topic` | `mortise_topic` | 话题/专栏 |
| `TopicTag` | `mortise_topic_tag` | 话题-标签关联 |
| `ArticleTag` | `mortise_article_tag` | 文章-标签关联 |
| `ArticleTopic` | `mortise_article_topic` | 文章-话题关联 |
| `ArticleProduct` | `mortise_article_product` | 文章-商品关联（跨模块） |

### 社交互动

| 实体 | 表名 | 说明 |
|------|------|------|
| `ArticleLike` | `mortise_article_like` | 文章点赞关系（软删除） |
| `ArticleFavorite` | `mortise_article_favorite` | 文章收藏关系（软删除） |
| `CommentLike` | `mortise_comment_like` | 评论点赞关系（软删除） |
| `CommunityFollowRelation` | `mortise_community_follow_relation` | 用户关注关系 |
| `Collection` | `mortise_collection` | 收藏夹 |
| `CollectionArticle` | `mortise_collection_article` | 收藏夹-文章关联 |
| `CollectionMember` | `mortise_collection_member` | 收藏夹协作成员 |

### 用户画像与游戏化

| 实体 | 表名 | 说明 |
|------|------|------|
| `CommunityProfile` | `mortise_community_profile` | 社区用户资料 |
| `CommunityUserStat` | `mortise_community_user_stat` | 用户统计（文章数、评论数等） |
| `CommunityCheckinRecord` | `mortise_community_checkin_record` | 签到记录 |
| `CommunityGamificationRule` | `mortise_community_gamification_rule` | 游戏化积分规则 |
| `ArticleHotScore` | `mortise_article_hot_score` | 文章热度评分 |

### 运营与审核

| 实体 | 表名 | 说明 |
|------|------|------|
| `CommunityAuditLog` | `mortise_community_audit_log` | 审核操作日志 |
| `CommunityNotification` | `mortise_community_notification` | 社区通知 |
| `SensitiveWord` | `mortise_sensitive_word` | 敏感词库 |
| `SensitiveWordPullLog` | `mortise_sensitive_word_pull_log` | 敏感词在线拉取日志 |

## 枚举定义（10 个）

| 枚举 | 说明 |
|------|------|
| `ArticleStatus` | 文章状态（草稿/已发布/已归档） |
| `ArticleSortMode` | 文章排序方式 |
| `CommentStatus` | 评论状态（待审核/已通过/已拒绝） |
| `ContentVisibility` | 可见性（公开/私密/仅会员） |
| `CollectionMemberRole` | 收藏夹成员角色 |
| `CollectionMemberStatus` | 收藏夹成员状态 |
| `CommunityNotificationEventType` | 通知事件类型 |
| `EnableStatus` | 启用/禁用状态 |
| `FollowRelationStatus` | 关注关系状态 |
| `HotScoreWindowType` | 热度评分时间窗口类型 |

## Service 接口（16 个）

| 接口 | 实现类 | 核心职责 |
|------|--------|----------|
| `ArticleService` | `ArticleServiceImpl` | 文章 CRUD、发布、审核、搜索 |
| `CommentService` | `CommentServiceImpl` | 评论发布、层级计算、审核 |
| `SocialInteractionService` | `SocialInteractionServiceImpl` | 点赞/收藏文章、点赞评论 |
| `CollectionService` | `CollectionServiceImpl` | 收藏夹管理 |
| `TagService` | `TagServiceImpl` | 标签管理 |
| `TopicService` | `TopicServiceImpl` | 话题管理 |
| `CommunityProfileService` | `CommunityProfileServiceImpl` | 社区用户资料 |
| `CommunityUserStatService` | `CommunityUserStatServiceImpl` | 用户统计数据维护 |
| `CommunityFollowRelationService` | `CommunityFollowRelationServiceImpl` | 关注/取消关注 |
| `CommunityCheckinService` | `CommunityCheckinServiceImpl` | 签到 |
| `CommunityGamificationRuleService` | `CommunityGamificationRuleServiceImpl` | 游戏化积分规则 |
| `CommunityNotificationService` | `CommunityNotificationServiceImpl` | 社区通知 |
| `CommunityAuditLogService` | `CommunityAuditLogServiceImpl` | 审核日志记录 |
| `ArticleHotScoreService` | `ArticleHotScoreServiceImpl` | 文章热度评分 |
| `SensitiveWordService` | `SensitiveWordServiceImpl` | 敏感词管理与同步 |
| `ArticleTocService` | — | 文章目录提取 |

## 领域事件（12 个）

社区模块通过 Spring `ApplicationEventPublisher` 发布事件，由 `CommunityEventListener` 统一监听处理。

| 事件 | 触发场景 |
|------|----------|
| `ArticlePublishedEvent` | 文章首次发布 |
| `ArticleViewedEvent` | 文章被浏览 |
| `ArticleLikedEvent` | 文章被点赞 |
| `ArticleFavoritedEvent` | 文章被收藏 |
| `ArticleProductsBoundEvent` | 文章绑定商品 |
| `ArticleTocRefreshEvent` | 文章目录需刷新 |
| `CommentRepliedEvent` | 评论被回复 |
| `InteractionCountRefreshEvent` | 互动计数需刷新 |
| `CommunityUserStatRefreshEvent` | 用户统计需刷新 |
| `UserCheckedInEvent` | 用户签到 |
| `UserFollowedEvent` | 用户关注 |
| `UserMentionedEvent` | 文章 @提及用户 |

## SPI 集成

社区模块通过 SPI 接口与其他模块解耦：

### 消费的 SPI（来自其他模块）

| SPI 接口 | 来源模块 | 用途 |
|----------|----------|------|
| `ContentModerationProvider` | `mortise-core` | 内容审核链 |
| `MemberCapabilityProvider` | `mortise-core` | 会员权限判断 |
| `UserProfileProvider` | `mortise-core` | 获取用户资料 |

### 提供的 SPI 实现

| 实现类 | 实现接口 | 说明 |
|--------|----------|------|
| `KeywordContentModerationProvider` | `ContentModerationProvider` | 关键词内容审核 |
| `DfaSensitiveWordModerationProvider` | `ContentModerationProvider` | DFA 敏感词审核 |
| `CommunitySystemNotificationSender` | `NotificationSender` | 社区通知发送渠道 |

## 支撑类

| 类 | 职责 |
|----|------|
| `ContentModerationSupport` | 按优先级选择并执行 `ContentModerationProvider` 链 |
| `ArticleMentionExtractor` | 从 Markdown 提取 `@mention` |
| `ArticleTocExtractor` | 从 Markdown 提取目录结构 |
| `ArticleMarkdownNormalizer` | 规范化 Markdown 格式 |
| `DbSensitiveWordDeny` | 数据库驱动的敏感词拒绝列表 |

## 事件处理器与定时任务

| 类 | 类型 | 职责 |
|----|------|------|
| `CommunityEventListener` | `@EventListener` | 统一处理社区领域事件 |
| `CommunityUserStatRefreshHandler` | `@EventListener` | 刷新用户统计缓存 |
| `ArticleTocRefreshHandler` | `@TransactionalEventListener` | 事务提交后异步刷新文章目录 |
| `ArticleHotScoreTask` | `@Scheduled` | 定时计算文章热度评分 |
| `CommunityUserStatCompensationTask` | `@Scheduled` | 定时对账用户统计计数 |

## Flyway 迁移脚本

社区模块占用全局版本号 V80–V94（V90 属于 mortise-member，V91 未使用）。

| 版本 | 文件 | 说明 |
|------|------|------|
| V80 | `V80__Create_Community_Tables.sql` | 核心社区表 |
| V81 | `V81__Create_Community_Social_And_Notification_Tables.sql` | 社交与通知表 |
| V82 | `V82__Create_Community_Collection_Tables.sql` | 收藏夹表 |
| V83 | `V83__Create_Community_Topic_Tag_Relation.sql` | 话题-标签关联表 |
| V84 | `V84__Create_Community_Checkin_Tables.sql` | 签到表 |
| V85 | `V85__Refactor_Community_Gamification_Rule_Table.sql` | 游戏化规则重构 |
| V86 | `V86__Init_Community_Menus.sql` | 社区菜单种子数据 |
| V87 | `V87__Alter_Community_Article_Status_For_Moderation.sql` | 文章审核状态字段 |
| V88 | `V88__Create_Community_Audit_Log_Table.sql` | 审核日志表 |
| V89 | `V89__Create_Sensitive_Word_Table.sql` | 敏感词表 |
| V92 | `V92__Create_Article_Hot_Score_Table.sql` | 文章热度评分表 |
| V93 | `V93__Create_Sensitive_Word_Pull_Log_Table.sql` | 敏感词拉取日志表 |
| V94 | `V94__Init_Community_Sensitive_Word_Menus.sql` | 敏感词菜单种子数据 |

## 测试覆盖

| 测试类 | 测试数 | 覆盖范围 |
|--------|--------|----------|
| `ContentModerationSupportTest` | 6 | Provider 选择逻辑、审核结果传递 |
| `SocialInteractionServiceImplTest` | 20 | 点赞/取消点赞/收藏/取消收藏（文章+评论） |
| `CommentServiceImplTest` | 15 | 评论发布、嵌套回复、删除、状态更新 |
| `ArticleMentionExtractorTest` | 2 | @提及提取 |
| `ArticleTocExtractorTest` | 3 | 目录提取 |
| `ArticleMarkdownNormalizerTest` | 3 | Markdown 规范化 |

共计 **49 个单元测试**。
