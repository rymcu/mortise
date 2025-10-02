# @PreAuthorize 注解不生效问题修复

## 问题描述

`@PreAuthorize("hasRole('admin')")` 注解在 Controller 类上不生效，管理员接口没有进行权限验证。

### 问题示例

```java
@Tag(name = "字典管理", description = "字典数据管理相关接口")
@RestController
@RequestMapping("/api/v1/admin/dictionaries")
@PreAuthorize("hasRole('admin')")  // ❌ 不生效
public class DictController {
    // ...
}
```

## 根本原因

### 1. 缺少方法级安全注解支持

`WebSecurityConfig` 中没有添加 `@EnableMethodSecurity` 注解，导致 Spring Security 无法识别和处理 `@PreAuthorize`、`@PostAuthorize`、`@Secured` 等方法级安全注解。

**Spring Security 6.x 的变化：**
- Spring Security 5.x 使用 `@EnableGlobalMethodSecurity(prePostEnabled = true)`
- Spring Security 6.x 使用 `@EnableMethodSecurity(prePostEnabled = true)`

### 2. 角色权限缺少 ROLE_ 前缀

Spring Security 的 `hasRole('admin')` 会自动查找带有 `ROLE_` 前缀的权限，即 `ROLE_admin`。

但在 `PermissionServiceImpl` 中，添加到用户权限集合中的角色权限没有 `ROLE_` 前缀：

```java
// ❌ 错误：直接添加 "admin"
permissions.add(role.getPermission());

// ✅ 正确：添加 "ROLE_admin"
permissions.add("ROLE_" + role.getPermission());
```

## 解决方案

### 1. 启用方法级安全注解

**文件：** `mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java`

```java
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // ✅ 添加此注解
@ConditionalOnClass(HttpSecurity.class)
public class WebSecurityConfig {
    // ...
}
```

**导入所需的类：**
```java
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
```

### 2. 为角色权限添加 ROLE_ 前缀

**文件：** `mortise-system/src/main/java/com/rymcu/mortise/system/service/impl/PermissionServiceImpl.java`

```java
@Override
public Set<String> findUserRolePermissionsByIdUser(Long idUser) {
    List<Role> roles = roleService.findRolesByIdUser(idUser);
    Set<String> permissions = new HashSet<>();
    for (Role role : roles) {
        if (StringUtils.isNotBlank(role.getPermission())) {
            // ✅ 为角色权限添加 ROLE_ 前缀
            permissions.add("ROLE_" + role.getPermission());
        }
    }
    return permissions;
}
```

## Spring Security 角色权限机制说明

### hasRole vs hasAuthority

Spring Security 提供了两种权限检查方式：

1. **`hasRole('admin')`**
   - 自动添加 `ROLE_` 前缀
   - 检查权限：`ROLE_admin`
   - 适用于角色管理

2. **`hasAuthority('ROLE_admin')`**
   - 不添加前缀
   - 检查权限：`ROLE_admin`
   - 适用于具体权限

### 推荐实践

```java
// ✅ 推荐：使用 hasRole，权限值添加 ROLE_ 前缀
@PreAuthorize("hasRole('admin')")
// authorities 中包含: ROLE_admin

// ✅ 也可以：使用 hasAuthority，明确指定完整权限名
@PreAuthorize("hasAuthority('ROLE_admin')")
// authorities 中包含: ROLE_admin

// ❌ 错误：使用 hasRole，但权限值没有 ROLE_ 前缀
@PreAuthorize("hasRole('admin')")
// authorities 中只有: admin
```

## 使用示例

### 类级别权限控制

```java
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('admin')")  // ✅ 整个 Controller 需要 admin 角色
public class UserController {
    // 所有方法都需要 admin 角色
}
```

### 方法级别权限控制

```java
@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {
    
    @GetMapping
    public List<Article> list() {
        // 公开接口，无需权限
    }
    
    @PostMapping
    @PreAuthorize("hasRole('user')")  // ✅ 需要 user 角色
    public Article create(@RequestBody Article article) {
        // 创建文章
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")  // ✅ 需要 admin 角色
    public void delete(@PathVariable Long id) {
        // 删除文章
    }
}
```

### 复杂权限表达式

```java
// 需要 admin 或 moderator 角色
@PreAuthorize("hasAnyRole('admin', 'moderator')")

// 需要同时拥有多个权限
@PreAuthorize("hasRole('admin') and hasAuthority('system:config:edit')")

// 检查当前用户是否是资源拥有者
@PreAuthorize("hasRole('user') and #userId == authentication.principal.id")

// 使用自定义方法检查权限
@PreAuthorize("@permissionService.canAccess(#id)")
```

## 验证方法

### 1. 检查配置

启动应用后，查看日志确认 `@EnableMethodSecurity` 已加载：

```
INFO o.s.s.c.a.m.c.MethodSecurityConfiguration - Enabling method-level security
```

### 2. 测试权限控制

1. **未登录访问受保护接口**
   ```bash
   curl http://localhost:8080/api/v1/admin/dictionaries
   ```
   预期结果：401 Unauthorized

2. **普通用户访问管理员接口**
   ```bash
   curl -H "Authorization: Bearer <user_token>" \
        http://localhost:8080/api/v1/admin/dictionaries
   ```
   预期结果：403 Forbidden

3. **管理员访问管理员接口**
   ```bash
   curl -H "Authorization: Bearer <admin_token>" \
        http://localhost:8080/api/v1/admin/dictionaries
   ```
   预期结果：200 OK

### 3. 查看用户权限

在登录后调用 `/api/v1/auth/me` 接口，检查返回的权限列表：

```json
{
  "code": "0",
  "data": {
    "account": "admin",
    "permissions": [
      "user",
      "menu:list",
      "menu:create",
      "ROLE_admin"  // ✅ 应该包含 ROLE_ 前缀
    ]
  }
}
```

## 注意事项

1. **重启应用**：修改配置类需要重启应用才能生效

2. **权限缓存**：如果使用了权限缓存，修改角色权限后需要清除缓存或重新登录

3. **测试环境**：在开发环境中可以添加日志来调试权限问题：
   ```java
   @Slf4j
   public class PermissionServiceImpl implements PermissionService {
       @Override
       public Set<String> findUserRolePermissionsByIdUser(Long idUser) {
           // ...
           log.info("用户 {} 的角色权限: {}", idUser, permissions);
           return permissions;
       }
   }
   ```

4. **兼容性**：如果有地方使用了 `hasAuthority('admin')`（不带 ROLE_ 前缀），需要改为：
   - `hasAuthority('ROLE_admin')` 或
   - `hasRole('admin')`

## 相关文档

- [Spring Security 方法安全](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
- [Spring Security 角色层级](https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html)
- [@PreAuthorize 注解](https://docs.spring.io/spring-security/reference/servlet/authorization/expression-based.html)

## 总结

修复 `@PreAuthorize` 不生效需要两步：

1. ✅ 在 `WebSecurityConfig` 添加 `@EnableMethodSecurity(prePostEnabled = true)`
2. ✅ 在 `PermissionServiceImpl` 为角色权限添加 `ROLE_` 前缀

修复后，所有使用 `@PreAuthorize("hasRole('...')")` 的接口都会正常进行权限验证。
