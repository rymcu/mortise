# 文件管理模块实现计划

## 优先级：P4

## 模块说明

`SystemFileController` 目前只有上传接口，文件元数据由 `x-file-storage` 库保存在 `FileDetail` 表中（通过 `FileDetailMapper`）。需要补充文件列表查询和删除接口，再实现前端管理页面。

## 后端实现

### 1. 增强 `SystemFileController`

**文件**：`mortise-system/mortise-system-admin/src/main/java/com/rymcu/mortise/system/controller/SystemFileController.java`

新增接口：
```
GET    /api/v1/admin/files    — 分页查询文件列表（filename/platform/contentType过滤）
DELETE /api/v1/admin/files/{id} — 删除文件（同时删除存储层文件）
```

- 注入 `FileDetailMapper`（来自 `mortise-file`，已在 `mortise-system-admin` 依赖中）
- 注入 `FileStorageService` 用于删除实际存储文件

### 文件查询参数

- `keyword` — 模糊匹配原始文件名
- `platform` — 存储平台（local / oss 等）
- `pageNum` / `pageSize`

## 前端实现

### 文件管理页面

**文件**：`frontend/apps/admin/app/pages/systems/files.vue`

- 路由：`/admin/systems/files`
- 分页表格，列：
  - 缩略图（图片类型展示小图）
  - 原始文件名
  - 内容类型（MIME）
  - 文件大小（格式化）
  - 存储平台
  - 上传时间
  - 操作：预览（新标签页）/ 删除（确认弹窗）
- 工具栏：关键词搜索
- 空状态提示

## 进度状态

- [ ] SystemFileController 新增 list + delete
- [ ] files.vue
