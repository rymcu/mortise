# OAuth2 绑定表重构文档

## 概述

本次重构将 OAuth2 绑定信息从 `User` 实体中分离到独立的 `UserOAuth2Binding` 实体，实现更规范的多账号绑定管理。

## 重构内容

### 1. 新增实体类

#### `UserOAuth2Binding.java`
- **位置**: `mortise-system/src/main/java/com/rymcu/mortise/system/entity/UserOAuth2Binding.java`
- **设计参考**: `mortise-member` 模块的 `MemberOAuth2Binding`
- **表名**: `mortise_user_oauth2_binding`

**字段说明**:
- `id`: 主键，使用 FlexId 生成器
- `userId`: 用户 ID（外键关联 `mortise_user.id`）
- `provider`: OAuth2 提供商（github, google, wechat, logto）
- `openId`: OAuth2 提供商的用户唯一标识
- `unionId`: 微信 UnionID（仅微信开放平台）
- `nickname`: OAuth2 提供商返回的昵称
- `avatar`: OAuth2 提供商返回的头像
- `email`: OAuth2 提供商返回的邮箱
- `accessToken`: 访问令牌（可选，用于调用第三方 API）
- `refreshToken`: 刷新令牌（可选）
- `expiresAt`: 令牌过期时间
- `rawData`: 原始用户数据（JSON 格式）
- `createdTime`: 创建时间
- `updatedTime`: 更新时间

**索引设计**:
- 唯一索引：`uk_provider_openid (provider, open_id)` - 确保同一提供商的 openId 唯一
- 普通索引：`idx_user_id (user_id)` - 提升按用户查询性能
- 普通索引：`idx_provider (provider)` - 提升按提供商查询性能

### 2. 新增服务层

#### `UserOAuth2BindingMapper.java`
- **位置**: `mortise-system/src/main/java/com/rymcu/mortise/system/mapper/UserOAuth2BindingMapper.java`
- **继承**: `BaseMapper<UserOAuth2Binding>`

#### `UserOAuth2BindingService.java`
- **位置**: `mortise-system/src/main/java/com/rymcu/mortise/system/service/UserOAuth2BindingService.java`
- **方法**:
  - `findByProviderAndOpenId(String provider, String openId)` - 根据提供商和 OpenID 查找绑定
  - `findByUserIdAndProvider(Long userId, String provider)` - 根据用户 ID 和提供商查找绑定

#### `UserOAuth2BindingServiceImpl.java`
- **位置**: `mortise-system/src/main/java/com/rymcu/mortise/system/service/impl/UserOAuth2BindingServiceImpl.java`
- **实现**: `UserOAuth2BindingService`

### 3. 重构 `AuthServiceImpl`

#### 主要变更

**新增依赖**:
```java
@Resource
private UserOAuth2BindingService userOAuth2BindingService;
```

**重构 `findOrCreateUserFromOAuth2()` 方法**:

1. **查找已存在的 OAuth2 绑定**（替代原来直接查询 User 表）
   ```java
   UserOAuth2Binding existingBinding = userOAuth2BindingService.findByProviderAndOpenId(
       userInfo.getProvider(), userInfo.getOpenId()
   );
   ```

2. **通过邮箱匹配现有用户时创建绑定**（替代原来更新 User 表的 provider/openId）
   ```java
   createOAuth2Binding(existingUser.getId(), userInfo);
   ```

3. **创建新用户时同时创建绑定**
   ```java
   createOAuth2Binding(newUser.getId(), userInfo);
   ```

**新增辅助方法**:

1. `createOAuth2Binding(Long userId, StandardOAuth2UserInfo userInfo)`
   - 创建 OAuth2 绑定记录
   - 存储提供商返回的昵称、头像、邮箱等信息

2. `updateOAuth2Binding(UserOAuth2Binding binding, StandardOAuth2UserInfo userInfo)`
   - 更新 OAuth2 绑定信息（昵称、头像、邮箱）
   - 只在数据发生变化时更新

**移除的方法**:
- `updateExistingUser()` - 替换为 `updateOAuth2Binding()`

**简化的方法**:
- `createNewUserFromOAuth2()` - 移除了对 `provider` 和 `openId` 字段的设置

### 4. 数据库迁移

#### `V2__Create_User_OAuth2_Binding_Table.sql`
- **位置**: `mortise-system/src/main/resources/db/migration/V2__Create_User_OAuth2_Binding_Table.sql`

**迁移步骤**:

1. **创建新表**: `mortise_user_oauth2_binding`

2. **数据迁移**: 将 `mortise_user` 表中已有的 `provider` 和 `open_id` 数据迁移到新表
   ```sql
   INSERT INTO mortise.mortise_user_oauth2_binding (...)
   SELECT ... FROM mortise.mortise_user
   WHERE provider IS NOT NULL AND open_id IS NOT NULL
   ```

3. **可选清理**: 注释掉的 ALTER TABLE 语句可在确认迁移成功后执行，删除 User 表中的冗余字段

## 优势

### 1. **支持多账号绑定**
- 一个用户可以绑定多个 OAuth2 账号
- 例如：同时绑定 GitHub、Google、微信等

### 2. **数据分离**
- OAuth2 特定信息（token、原始数据等）不污染 User 表
- User 表保持核心用户信息的简洁性

### 3. **更好的扩展性**
- 新增 OAuth2 提供商无需修改 User 表结构
- 可以存储更多提供商特定的信息（accessToken、refreshToken、rawData）

### 4. **更清晰的职责划分**
- User 表：核心用户信息
- UserOAuth2Binding 表：OAuth2 绑定关系和第三方账号信息

### 5. **与 mortise-member 模块保持一致**
- 两个模块使用相同的设计模式
- 便于理解和维护

## 兼容性

### 向后兼容

1. **保留 `oauth2Login()` 方法**
   - 标记为 `@Deprecated`
   - 内部转换为新的实现方式

2. **数据迁移**
   - 自动迁移已有的 OAuth2 绑定数据
   - 不影响现有用户

### 升级建议

1. **运行数据库迁移**
   ```bash
   # Flyway 会自动执行 V2__Create_User_OAuth2_Binding_Table.sql
   ```

2. **验证数据迁移**
   ```sql
   -- 检查迁移的数据量
   SELECT COUNT(*) FROM mortise.mortise_user_oauth2_binding;
   
   -- 检查是否有遗漏
   SELECT COUNT(*) FROM mortise.mortise_user 
   WHERE provider IS NOT NULL AND open_id IS NOT NULL;
   ```

3. **（可选）清理冗余字段**
   ```sql
   -- 确认迁移成功后执行
   ALTER TABLE mortise.mortise_user DROP COLUMN provider;
   ALTER TABLE mortise.mortise_user DROP COLUMN open_id;
   ```

## 测试建议

### 1. 单元测试
- `UserOAuth2BindingServiceImpl` 的查询方法
- `AuthServiceImpl` 的 OAuth2 登录流程

### 2. 集成测试
- OAuth2 首次登录（创建新用户 + 绑定）
- OAuth2 重复登录（查找已有绑定）
- 邮箱匹配现有用户（创建绑定关联）
- 并发创建处理

### 3. 性能测试
- 查询绑定关系的性能
- 索引效果验证

## 注意事项

1. **唯一约束**: `(provider, open_id)` 组合必须唯一，防止重复绑定
2. **事务处理**: OAuth2 登录流程需要在事务中执行，确保用户创建和绑定创建的原子性
3. **并发控制**: 使用唯一索引和异常捕获处理并发创建场景
4. **数据完整性**: userId 应该添加外键约束（可选，根据实际需求）

## 后续优化

1. **添加解绑功能**
   - 用户可以解绑某个 OAuth2 账号
   - 保留至少一种登录方式

2. **支持账号合并**
   - 允许将多个 OAuth2 账号合并到同一用户

3. **Token 刷新机制**
   - 自动刷新过期的 accessToken
   - 使用 refreshToken 获取新的 accessToken

4. **安全性增强**
   - Token 加密存储
   - 敏感信息脱敏

5. **审计日志**
   - 记录绑定/解绑操作
   - 记录 OAuth2 登录历史

## 参考资料

- [MemberOAuth2Binding 设计](../mortise-member/src/main/java/com/rymcu/mortise/member/entity/MemberOAuth2Binding.java)
- [MyBatis-Flex 文档](https://mybatis-flex.com/)
- [OAuth2 最佳实践](https://oauth.net/2/)
