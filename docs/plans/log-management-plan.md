# 日志管理模块实现计划

## 优先级：P1（最高）

## 模块说明

后端 `mortise-log` 模块提供了操作日志（OperationLog）和 API 访问日志（ApiLog）的采集基础设施，数据持久化在 `mortise-persistence` 的 `OperationLogMapper` / `ApiLogMapper`。管理界面需提供查询和清理功能。

## 后端实现

### 1. 新增 `LogSearch` 查询模型

**文件**：`mortise-system/mortise-system-domain/src/main/java/com/rymcu/mortise/system/model/LogSearch.java`

字段：
- `keyword` — 关键词（模糊匹配操作人/用户名/URI/操作描述）
- `clientType` — 客户端类型：system / app / web / api
- `module` — 模块名（仅 OperationLog）
- `operation` — 操作类型（仅 OperationLog）
- `startDate` / `endDate` — 时间范围（继承自 BaseSearch）
- `success` — 是否成功（可选）
- `pageNum` / `pageSize` — 分页（继承自 BaseSearch）

### 2. 新增 `LogQueryService` 接口

**文件**：`mortise-system/mortise-system-application/src/main/java/com/rymcu/mortise/system/service/LogQueryService.java`

方法：
```java
Page<OperationLog> findOperationLogs(Page<OperationLog> page, LogSearch search);
Page<ApiLog>       findApiLogs(Page<ApiLog> page, LogSearch search);
Boolean            deleteOperationLog(Long id);
Boolean            deleteApiLog(Long id);
```

### 3. 新增 `LogQueryServiceImpl` 实现

**文件**：`mortise-system/mortise-system-application/src/main/java/com/rymcu/mortise/system/service/impl/LogQueryServiceImpl.java`

- 使用 `OperationLogMapper` / `ApiLogMapper`（来自 `mortise-persistence`，已在依赖链中）
- 通过 `QueryWrapper` 构建动态条件：keyword 模糊匹配、时间范围过滤、clientType/module 精确匹配

### 4. 新增 `LogController`

**文件**：`mortise-system/mortise-system-admin/src/main/java/com/rymcu/mortise/system/controller/LogController.java`

接口：
```
GET    /api/v1/admin/logs/operation   — 分页查询操作日志
DELETE /api/v1/admin/logs/operation/{id} — 删除操作日志
GET    /api/v1/admin/logs/api         — 分页查询 API 日志
DELETE /api/v1/admin/logs/api/{id}    — 删除 API 日志
```

权限：`@PreAuthorize("hasRole('ADMIN')")`

## 前端实现

### 5. 操作日志页面

**文件**：`frontend/apps/admin/app/pages/systems/operation-logs.vue`

- 路由：`/admin/systems/operation-logs`
- 分页表格，列：ID / 客户端类型 / 模块 / 操作 / 操作人 / 请求方法+URI / 耗时 / 成功 / 操作时间
- 过滤搜索：关键词 + 客户端类型下拉 + 时间范围
- 行操作：查看详情（Slideover）/ 删除

### 6. API 日志页面

**文件**：`frontend/apps/admin/app/pages/systems/api-logs.vue`

- 路由：`/admin/systems/api-logs`
- 分页表格，列：ID / 客户端类型 / API描述 / 用户 / 请求方法+URI / 状态码 / 耗时 / 成功 / 请求时间
- 过滤搜索：关键词 + 客户端类型下拉 + 时间范围
- 行操作：查看详情（Slideover）/ 删除

## 进度状态

- [ ] LogSearch 模型
- [ ] LogQueryService 接口
- [ ] LogQueryServiceImpl 实现
- [ ] LogController
- [ ] operation-logs.vue
- [ ] api-logs.vue
