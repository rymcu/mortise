# apps/admin Plan

后台管理端，基于 `nuxt-ui-templates/dashboard` 定制。

## 业务范围（OSS）

- 管理员登录（账号密码 + OAuth2）
- 用户管理
- 角色管理
- 菜单管理
- 字典管理
- OAuth2 客户端配置
- 审计/日志视图（按后端现有能力接入）

## 路由约定

- `/admin/auth/login`
- `/admin/auth/callback`
- `/admin/dashboard`
- `/admin/system/users`
- `/admin/system/roles`
- `/admin/system/menus`
- `/admin/system/dicts`
- `/admin/system/oauth2-clients`

## 迭代计划

1. **骨架迁移**
   - 迁移 dashboard 布局、侧栏、主题。
   - 替换示例页面为 Mortise 菜单结构。
2. **鉴权集成**
   - 接入 `packages/auth`。
   - 完成路由守卫、401 自动刷新、登出。
3. **系统模块接入**
   - 接入 `packages/core-sdk` 的 system API。
   - 交付用户/角色/菜单/字典 CRUD。
4. **运维能力增强**
   - 接入日志查询页与异常提示规范。
   - 完善权限按钮控制与空状态处理。

## 完成标准

- 管理端关键功能可闭环。
- 所有请求统一通过 SDK 与鉴权拦截器。
- 不出现任何商业模块菜单与路由。
