# Community 商用修复试运行清单

## 文档目的

本清单用于承接本轮 `community` 商用阻断项修复后的试运行准备工作，目标是把“代码已修复并通过核心自动验证”推进到“可合并、可试运行、可复核”。

本轮范围仅包含以下内容：

- 内容访问与审核状态守卫收口
- 社区事务事件改为提交后生效
- 浏览量幂等上报与 30 分钟去重
- Nuxt community layer 宿主集成修复
- `@mortise/site` 的 typecheck 清障

本轮不包含以下内容：

- 新社区功能扩展
- 接口路径调整
- 数据库字段变更或 Flyway 迁移
- 与本轮无关的既有 lint warning 清零

## 变更摘要

### 后端

- `POST /api/v1/community/articles/{id}/views` 已改为幂等计数语义。
- 未发布文章对非作者统一不可见；接口保持原有返回形态，不额外破坏调用方。
- 已发布会员文仍保留“非会员可见详情壳，但不可看全文、不可评论、不计浏览”的既有产品语义。
- 文章审核改状态时保留 `previousStatus`，确保审计日志和发布事件判断正确。
- 社区事务事件统一改为 `AFTER_COMMIT` 生效，避免事务回滚后遗留积分、通知、统计等幽灵副作用。

### 前端

- community layer 新增内部别名：
  - `#community-types`
  - `#community-utils`
  - `#community-composables`
- layer 内部自引用已切换到显式别名，不再依赖 `~/...` 指向宿主根目录。
- `@mortise/site` 补齐本轮 typecheck 阻塞项，确保宿主站点可稳定集成 community layer。

## 已完成自动验证

以下命令已在当前仓库执行并通过：

```powershell
mvn -pl mortise-community/mortise-community-application -am test
mvn -pl mortise-community/mortise-community-admin -am test
mvn -pl mortise-community/mortise-community-api -am clean compile -DskipTests
mvn -pl mortise-community/mortise-community-infra -am test
pnpm --filter @mortise/site typecheck
pnpm lint
pnpm --filter @mortise/site build
```

验证覆盖重点：

- 文章详情、评论、浏览计数访问矩阵
- 审核状态变更与发布事件触发条件
- 浏览去重窗口内幂等计数
- 事务事件监听器的 `AFTER_COMMIT` 配置
- community layer 在宿主站点中的类型解析

## 人工冒烟清单

### 1. 文章访问矩阵

#### 匿名用户

- 访问公开已发布文章：可见详情壳、可见全文、可见评论，浏览计数可增加。
- 访问会员已发布文章：可见详情壳，不可见全文，不可评论，浏览计数不增加。
- 访问私密已发布文章：不可见详情。
- 访问草稿、待审核、驳回、归档文章：不可见详情。

#### 作者

- 访问自己的草稿、待审核、驳回、归档文章：可见详情预览。
- 访问自己的私密已发布文章：可见详情。
- 访问自己的会员已发布文章：按统一规则校验当前实现是否仍不计浏览。

#### 会员用户

- 访问会员已发布文章：可见详情壳与全文，可评论，浏览计数可增加。
- 访问公开已发布文章：行为与匿名一致，但应保持登录态相关 UI 正常。

### 2. 评论行为

- 非会员访问会员文评论区：评论列表不可用或为空，且无法新增评论。
- 非作者访问未发布文章评论区：评论列表返回空分页，且无法新增评论。
- 作者访问自己未发布文章：确认仍不开放评论与浏览计数。

### 3. 审核流

- 验证 `待审核 -> 发布`：审计日志前后状态正确，发布奖励/通知/统计只触发一次。
- 验证 `驳回 -> 待审核`：审计日志前后状态正确，不触发发布奖励。
- 验证 `发布 -> 驳回`：审计日志前后状态正确，不应误发 `ArticlePublishedEvent`。
- 制造一次事务回滚场景：确认积分、通知、统计不会在回滚后残留。

### 4. 浏览去重

- 同一登录账号在 30 分钟窗口内重复上报：仅第一次增加浏览量。
- 同一匿名指纹在 30 分钟窗口内重复上报：仅第一次增加浏览量。
- 去重窗口过期后再次上报：浏览量可再次增加。
- 无法生成稳定指纹时：接口返回成功，但不累计浏览量。

### 5. 前端集成

- 社区首页可正常加载。
- 专题页可正常加载。
- 文章详情页可正常加载。
- 创作中心可正常加载。
- 宿主站点中的 community 页面无 layer 自引用解析错误。

## 试运行前确认项

- 向产品和运营明确说明：浏览去重窗口当前固定为 30 分钟。
- 向联调同学明确说明：浏览接口现为“幂等上报”，重复请求成功不代表重复加数。
- 向测试同学明确说明：未发布文章对非作者统一不可见，会员文对非会员只保留详情壳。
- 保留当前实现语义：作者对已发布会员文的浏览计数不做特殊放开，保持统一规则。

## 已知非阻断项

- `frontend/apps/site/app/pages/blog/[id].vue` 仍存在 `vue/no-v-html` lint warning。
- 上述 warning 为仓库既有问题，不是本轮 community 商用修复新增问题。
- `pnpm --filter @mortise/site build` 过程中仍可能出现 Nuxt/Tailwind sourcemap、chunk size、icon/node exports 等 warning，但当前不阻断构建成功。

## 提交建议

- `mortise-community` 子模块单独提交，便于后端回滚与 cherry-pick。
- `frontend/layers/community` 子模块单独提交，便于 layer 独立复用与集成。
- 主仓库单独提交 submodule 指针、`apps/site` 清障改动与本试运行文档。

## 验收结论

当前 `community` 已达到“可合并并进入试运行验证”的最低门槛，但在正式对外放量前，仍建议至少完成一次带账号矩阵的人工冒烟，以确认访问守卫、审核链路和浏览去重在真实环境下与产品预期一致。
