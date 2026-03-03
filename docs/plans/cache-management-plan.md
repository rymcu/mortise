# 缓存管理模块实现计划

## 优先级：P2

## 模块说明

后端 `SystemCacheController` 已在 `/api/v1/admin/system/cache` 下提供操作接口，前端只需实现管理界面。

## 已有后端接口

```
DELETE /api/v1/admin/system/cache/user/{userId}    — 清除指定用户缓存
DELETE /api/v1/admin/system/cache/dict/{dictType}  — 清除指定字典缓存
DELETE /api/v1/admin/system/cache/dict/all         — 清除所有字典缓存
POST   /api/v1/admin/system/cache/verification-code — 发送验证码
POST   /api/v1/admin/system/cache/verification-code/verify — 验证验证码
```

## 前端实现

### 缓存管理页面

**文件**：`frontend/apps/admin/app/pages/systems/cache.vue`

- 路由：`/admin/systems/cache`
- UI 布局：卡片式，分组展示缓存操作项
  - **用户缓存** — 输入用户ID，点击按钮清除
  - **字典缓存** — 输入字典类型，点击清除；一键清除全部字典缓存（带二次确认）
- 操作反馈：成功/失败 Toast 通知
- 所有操作带 loading 状态

## 进度状态

- [ ] cache.vue
