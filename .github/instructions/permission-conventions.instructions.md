---
description: '权限命名规范与 API 级别权限控制指南'
applyTo: '**/*Controller.java, **/menus.json'
---

# 权限命名规范

## 权限标识命名规范

### 格式

```
模块:资源:操作
```

### 示例

| 权限标识 | 说明 |
|---------|------|
| `system:user:list` | 系统模块-用户资源-列表查询 |
| `system:user:add` | 系统模块-用户资源-新增 |
| `community:article:audit` | 社区模块-文章资源-审核 |
| `member:edit` | 会员模块-编辑 |

### 操作类型

| 操作 | 说明 | 适用场景 |
|------|------|---------|
| `list` | 列表/分页查询 | GET 请求，返回列表或分页数据 |
| `query` | 详情查询 | GET 请求，返回单条记录详情 |
| `add` | 新增 | POST 请求，创建新记录 |
| `edit` | 编辑 | PUT/PATCH 请求，修改记录 |
| `delete` | 删除 | DELETE 请求，删除记录 |
| `assign` | 分配 | PUT/POST 请求，分配角色/权限等 |
| `audit` | 审核 | PATCH 请求，审核/更新状态 |
| `export` | 导出 | GET 请求，导出数据 |
| `import` | 导入 | POST 请求，导入数据 |
| `upload` | 上传 | POST 请求，上传文件 |
| `download` | 下载 | GET 请求，下载文件 |
| `clear` | 清空 | DELETE 请求，清空全部数据 |

## Controller 权限注解规范

### 原则

1. **移除类级别 `@PreAuthorize("hasRole('ADMIN')")`**
2. **在方法级别添加 `@PreAuthorize("hasAuthority('权限标识')")`**
3. **公开接口不需要权限注解**

### 示例

```java
@Tag(name = "用户管理", description = "用户管理相关接口")
@AdminController
@RequestMapping("/users")
// ❌ 不要在类级别添加 @PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @GetMapping
    @PreAuthorize("hasAuthority('system:user:list')")  // ✅ 方法级别权限
    public GlobalResult<Page<UserInfo>> listUsers(...) { }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public GlobalResult<UserInfo> getUser(...) { }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public GlobalResult<Long> createUser(...) { }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public GlobalResult<Boolean> updateUser(...) { }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public GlobalResult<Boolean> deleteUser(...) { }

    // 公开接口无需权限
    @GetMapping("/options")
    public GlobalResult<List<Option>> getUserOptions(...) { }
}
```

### 组合权限

当操作需要多个权限之一时，使用 `or`：

```java
@PostMapping
@PreAuthorize("hasAuthority('system:tag:add') or hasAuthority('system:tag:edit')")
public GlobalResult<Long> createOrUpdateTag(...) { }
```

## 菜单数据规范

### menuType 定义

| menuType | 说明 | 权限级别 |
|----------|------|---------|
| `0` | 目录 | 模块级，如 `system`、`community` |
| `1` | 菜单 | 资源级，如 `system:user`、`community:article` |
| `2` | 按钮/API | 操作级，如 `system:user:add`、`community:article:audit` |

### menus.json 结构

```json
[
  { "label": "系统", "permission": "system", "menuType": 0, "parentPermission": null },
  { "label": "用户", "permission": "system:user", "menuType": 1, "parentPermission": "system" },
  { "label": "新增用户", "permission": "system:user:add", "menuType": 2, "parentPermission": "system:user" },
  { "label": "编辑用户", "permission": "system:user:edit", "menuType": 2, "parentPermission": "system:user" }
]
```

## 权限验证流程

```
用户请求 → JWT 解析 → 加载用户权限 → Controller 权限校验
                                    ↓
                    PermissionServiceImpl.findUserPermissionsByIdUser()
                                    ↓
                    ┌─────────────────────────────────────┐
                    │ 菜单权限 (menu.permission)           │
                    │ → 直接添加到 authorities             │
                    │ → 例: "system:user:add"             │
                    ├─────────────────────────────────────┤
                    │ 角色权限 (role.permission)           │
                    │ → 添加 "ROLE_" 前缀                  │
                    │ → 例: "ADMIN" → "ROLE_ADMIN"        │
                    └─────────────────────────────────────┘
```

## 常见问题

### Q: `hasRole` vs `hasAuthority`？

- `hasRole('ADMIN')`：自动添加 `ROLE_` 前缀，匹配 `ROLE_ADMIN`
- `hasAuthority('system:user:add')`：精确匹配，用于 API 级权限

### Q: 为什么移除类级别权限？

为了实现细粒度的权限控制，不同用户可以有不同的操作权限。例如：
- 用户 A 有 `system:user:list` 和 `system:user:query`，只能查看
- 用户 B 有 `system:user:*`，可以完整管理

### Q: 公开接口如何处理？

公开接口（如字典选项、公开配置）不需要添加 `@PreAuthorize` 注解。
