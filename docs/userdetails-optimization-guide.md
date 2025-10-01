# UserDetails 优化指南

## 问题分析

原来的 `UserDetailInfo` 只存储了：
- username (账户名)
- password (密码)
- state (状态)
- authorities (权限)

这导致在 Controller 中需要重新查询数据库获取完整用户信息。

## 优化方案

### 1. 扩展 UserDetailInfo 存储 User 对象

已完成以下修改：

#### UserDetailInfo.java
```java
public class UserDetailInfo implements UserDetails {
    // 原有字段...
    
    /**
     * 存储完整的 User 对象
     */
    private User user;
    
    /**
     * 新增构造方法，支持传入完整的 User 对象
     */
    public UserDetailInfo(User user, Collection<? extends GrantedAuthority> authorities) {
        this.username = user.getAccount();
        this.password = user.getPassword();
        this.state = user.getStatus();
        this.authorities = authorities;
        this.user = user;
    }
    
    /**
     * 获取完整的 User 对象
     */
    public User getUser() {
        return user;
    }
}
```

#### UserDetailsServiceImpl.java
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userMapper.selectOneByQuery(/*...*/);
    if (Objects.nonNull(user)) {
        Set<String> permissions = permissionService.findUserPermissionsByIdUser(user.getId());
        Set<GrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        // 使用新的构造方法，传入完整的 User 对象
        return new UserDetailInfo(user, authorities);
    }
    throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
}
```

### 2. Controller 中的最佳实践

#### 优化前（需要重新查询数据库）
```java
@GetMapping("/menus")
public GlobalResult<List<Link>> getUserMenus(@AuthenticationPrincipal UserDetails userDetails) {
    log.info("获取用户菜单: {}", userDetails.getUsername());
    // ❌ 需要重新查询数据库
    User user = userService.findByAccount(userDetails.getUsername());
    List<Link> menus = authService.userMenus(user);
    return GlobalResult.success(menus);
}
```

#### 优化后（直接获取，无需查询）
```java
@GetMapping("/menus")
public GlobalResult<List<Link>> getUserMenus(@AuthenticationPrincipal UserDetailInfo userDetails) {
    log.info("获取用户菜单: {}", userDetails.getUsername());
    // ✅ 直接从 UserDetails 获取，无需查询数据库
    User user = userDetails.getUser();
    List<Link> menus = authService.userMenus(user);
    return GlobalResult.success(menus);
}
```

### 3. 进一步优化 AuthService

如果只需要用户 ID，可以优化 Service 方法签名：

```java
// AuthService 接口
List<Link> userMenus(Long userId);

// Controller
@GetMapping("/menus")
public GlobalResult<List<Link>> getUserMenus(@AuthenticationPrincipal UserDetailInfo userDetails) {
    log.info("获取用户菜单: {}", userDetails.getUsername());
    // ✅ 只传递必要的参数
    List<Link> menus = authService.userMenus(userDetails.getUser().getId());
    return GlobalResult.success(menus);
}
```

## 使用示例

### 示例 1: 获取当前用户信息
```java
@GetMapping("/current")
public GlobalResult<User> getCurrentUser(@AuthenticationPrincipal UserDetailInfo userDetails) {
    return GlobalResult.success(userDetails.getUser());
}
```

### 示例 2: 获取用户权限
```java
@GetMapping("/permissions")
public GlobalResult<Set<String>> getUserPermissions(@AuthenticationPrincipal UserDetailInfo userDetails) {
    Set<String> permissions = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
    return GlobalResult.success(permissions);
}
```

### 示例 3: 在 Service 层获取当前用户
```java
@Service
public class SomeService {
    
    public void someMethod() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailInfo) {
            UserDetailInfo userDetails = (UserDetailInfo) authentication.getPrincipal();
            User currentUser = userDetails.getUser();
            // 使用 currentUser...
        }
    }
}
```

## 优势对比

| 对比项 | 优化前 | 优化后 |
|--------|--------|--------|
| 数据库查询 | 每次需要用户信息都要查询 | Spring Security 认证时查询一次 |
| 性能 | 较差（多次数据库查询） | 较好（减少数据库压力） |
| 代码简洁性 | 需要注入 UserService | 直接从 UserDetails 获取 |
| 可测试性 | 需要 mock UserService | 只需 mock UserDetailInfo |
| 数据一致性 | 可能不一致（多次查询） | 一致（同一次认证） |

## 注意事项

1. **Session 数据更新**：如果用户信息更新（如昵称、邮箱），需要用户重新登录才能看到最新数据
   - 解决方案：提供强制刷新 token 的接口
   
2. **内存占用**：每个 UserDetails 会存储完整的 User 对象
   - 影响：Session/JWT Payload 会略大
   - 建议：合理设置 Session 过期时间

3. **敏感信息**：确保 User 对象中的敏感字段使用 `@JsonIgnore` 标注
   ```java
   public class User {
       @JsonIgnore
       private String password;
   }
   ```

## 迁移清单

- [x] 扩展 `UserDetailInfo` 添加 `user` 字段
- [x] 更新 `UserDetailsServiceImpl` 使用新构造方法
- [ ] 更新所有 Controller 方法，将 `UserDetails` 改为 `UserDetailInfo`
- [ ] 移除不必要的 `userService.findByAccount()` 调用
- [ ] 考虑优化 `AuthService.userMenus()` 方法签名
- [ ] 添加单元测试验证功能

## 总结

通过在 `UserDetailInfo` 中存储完整的 `User` 对象：
- ✅ 减少数据库查询
- ✅ 提升应用性能
- ✅ 简化代码逻辑
- ✅ 符合 Spring Security 最佳实践
