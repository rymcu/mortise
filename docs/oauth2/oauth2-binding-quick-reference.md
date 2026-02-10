# OAuth2 绑定重构 - 快速参考

## 📁 新增文件清单

### 实体和数据访问
```
mortise-system/src/main/java/com/rymcu/mortise/system/
├── entity/
│   └── UserOAuth2Binding.java                    ✅ OAuth2绑定实体
├── mapper/
│   └── UserOAuth2BindingMapper.java              ✅ MyBatis Mapper
├── service/
│   ├── UserOAuth2BindingService.java             ✅ 服务接口
│   └── impl/
│       └── UserOAuth2BindingServiceImpl.java     ✅ 服务实现
```

### 数据库迁移
```
mortise-system/src/main/resources/db/migration/
└── V2__Create_User_OAuth2_Binding_Table.sql      ✅ 数据库迁移脚本
```

### 文档
```
docs/
├── oauth2-binding-refactoring.md                 ✅ 重构说明文档
├── oauth2-binding-usage-examples.md              ✅ 使用示例文档
└── oauth2-binding-refactoring-summary.md         ✅ 完成总结文档
```

## 🔄 修改的文件

```
mortise-system/src/main/java/com/rymcu/mortise/system/service/impl/
└── AuthServiceImpl.java                          🔧 重构OAuth2登录逻辑
```

### 主要变更
- ✅ 新增 `UserOAuth2BindingService` 依赖
- ✅ 重构 `findOrCreateUserFromOAuth2()` 方法
- ✅ 新增 `createOAuth2Binding()` 方法
- ✅ 新增 `updateOAuth2Binding()` 方法
- ✅ 简化 `createNewUserFromOAuth2()` 方法
- ✅ 移除 `updateExistingUser()` 方法

## 🗄️ 数据库变更

### 新表: `mortise_user_oauth2_binding`

```sql
CREATE TABLE mortise_user_oauth2_binding (
    id            BIGINT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    provider      VARCHAR(50) NOT NULL,
    open_id       VARCHAR(255) NOT NULL,
    union_id      VARCHAR(255),
    nickname      VARCHAR(100),
    avatar        VARCHAR(500),
    email         VARCHAR(100),
    access_token  TEXT,
    refresh_token TEXT,
    expires_at    TIMESTAMP,
    raw_data      TEXT,
    created_time  TIMESTAMP,
    updated_time  TIMESTAMP,
    UNIQUE KEY uk_provider_openid (provider, open_id),
    KEY idx_user_id (user_id),
    KEY idx_provider (provider)
);
```

### 索引说明
- `uk_provider_openid`: 唯一索引，确保同一提供商的 openId 唯一
- `idx_user_id`: 普通索引，提升按用户查询性能
- `idx_provider`: 普通索引，提升按提供商查询性能

## 🚀 使用指南

### 1. OAuth2 登录（自动处理）

```java
// AuthServiceImpl 会自动处理 OAuth2 绑定
@Override
public User findOrCreateUserFromOAuth2(StandardOAuth2UserInfo userInfo) {
    // 1. 查找已存在的绑定（支持 openId 和 unionId 查找）
    // 2. 通过邮箱匹配现有用户
    // 3. 创建新用户 + 绑定
    // ...
}
```

**微信 UnionID 支持**:
- ✅ 自动识别同一用户在不同微信应用（公众号、小程序）中的身份
- ✅ 优先通过 `openId` 查找，回退使用 `unionId` 查找
- ✅ 自动更新 `openId`，保持 `unionId` 不变

### 2. 查询用户绑定

```java
// 根据 provider 和 openId 查询
UserOAuth2Binding binding = userOAuth2BindingService.findByProviderAndOpenId("github", "12345");

// 根据 provider 和 unionId 查询（仅微信）
UserOAuth2Binding binding = userOAuth2BindingService.findByProviderAndUnionId("wechat", "oUnion789");

// 根据 userId 和 provider 查询
UserOAuth2Binding binding = userOAuth2BindingService.findByUserIdAndProvider(userId, "github");

// 查询用户的所有绑定
List<UserOAuth2Binding> bindings = userOAuth2BindingService.list(
    QueryWrapper.create().where(UserOAuth2Binding::getUserId).eq(userId)
);
```

### 3. 创建绑定

```java
UserOAuth2Binding binding = new UserOAuth2Binding();
binding.setUserId(userId);
binding.setProvider("wechat");
binding.setOpenId("oXYZ123");
binding.setUnionId("oUnion789");  // 微信 UnionID（可选）
binding.setNickname("username");
binding.setAvatar("https://...");
binding.setEmail("user@example.com");
binding.setCreatedTime(LocalDateTime.now());
binding.setUpdatedTime(LocalDateTime.now());

userOAuth2BindingService.save(binding);
```

## ⚠️ 注意事项

### 1. 迁移步骤
1. 确保数据库连接正常
2. 运行应用，Flyway 会自动执行迁移脚本
3. 验证数据迁移：
   ```sql
   SELECT COUNT(*) FROM mortise.mortise_user_oauth2_binding;
   ```

### 2. 兼容性
- `oauth2Login(OidcUser, String)` 方法已标记为 `@Deprecated`
- 旧接口仍然可用，内部调用新实现
- 建议迁移到新方法：`findOrCreateUserFromOAuth2(StandardOAuth2UserInfo)`

### 3. 数据完整性
- 唯一索引确保 `(provider, open_id)` 组合唯一
- 避免重复绑定
- 支持并发创建场景

### 4. 可选清理
确认迁移成功后，可执行以下 SQL 清理冗余字段：
```sql
ALTER TABLE mortise.mortise_user DROP COLUMN provider;
ALTER TABLE mortise.mortise_user DROP COLUMN open_id;
```

## 🔍 故障排查

### 问题 1: 数据库迁移失败
**现象**: Flyway 报错
**解决**: 
1. 检查数据库权限
2. 确认 Flyway 配置正确
3. 查看 `flyway_schema_history` 表

### 问题 2: 唯一索引冲突
**现象**: `Duplicate entry` 错误
**解决**: 
1. 检查是否有重复的 `(provider, open_id)` 组合
2. 清理重复数据
3. 重新运行迁移

### 问题 3: 找不到 UserOAuth2BindingService
**现象**: `NoSuchBeanDefinitionException`
**解决**:
1. 确认 `UserOAuth2BindingServiceImpl` 有 `@Service` 注解
2. 检查包扫描配置
3. 重启应用

## 📊 性能优化建议

### 1. 索引优化
- ✅ 已添加 `(provider, open_id)` 唯一索引
- ✅ 已添加 `user_id` 索引
- ✅ 已添加 `provider` 索引

### 2. 查询优化
```java
// 推荐: 使用索引查询
userOAuth2BindingService.findByProviderAndOpenId(provider, openId);

// 避免: 全表扫描
userOAuth2BindingService.list(); // 慎用
```

### 3. 缓存建议
- 考虑缓存用户的绑定列表
- 缓存键: `user:oauth2:bindings:{userId}`
- 过期时间: 30分钟

## 📚 相关文档链接

- [详细重构文档](./oauth2-binding-refactoring.md)
- [使用示例文档](./oauth2-binding-usage-examples.md)
- [完成总结文档](./oauth2-binding-refactoring-summary.md)
- [微信 UnionID 支持文档](./wechat-unionid-support.md) ⭐ 新增

## ✅ 验证清单

部署前检查：
- [ ] 数据库迁移脚本已审核
- [ ] 所有新文件已提交
- [ ] 单元测试已编写并通过
- [ ] 集成测试已通过
- [ ] 文档已更新
- [ ] 代码已审核

部署后验证：
- [ ] 数据库表已创建
- [ ] 已有数据已迁移
- [ ] OAuth2 登录功能正常
- [ ] 没有编译错误
- [ ] 没有运行时错误
- [ ] 性能测试通过

## 🎯 下一步

1. **立即执行**
   - [ ] 提交代码到版本控制
   - [ ] 在测试环境部署
   - [ ] 验证数据迁移

2. **短期任务**
   - [ ] 编写单元测试
   - [ ] 编写集成测试
   - [ ] 更新 API 文档

3. **中期规划**
   - [ ] 实现绑定管理接口
   - [ ] 优化前端用户设置页面
   - [ ] 添加审计日志

---

**创建时间**: 2025-10-04  
**版本**: v1.0.0  
**维护者**: ronger
