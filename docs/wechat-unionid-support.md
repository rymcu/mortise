# 微信 UnionID 支持文档

## 概述

本文档说明 `UserOAuth2Binding` 如何支持微信的 `unionId` 字段，以及如何利用 `unionId` 实现微信多应用账号关联。

## 什么是 UnionID？

### 微信的两种用户标识

1. **OpenID**
   - 用户在某个公众号或小程序中的唯一标识
   - 同一用户在不同应用中的 OpenID 不同
   - 示例：
     - 用户 A 在公众号 1 的 OpenID: `oXYZ123`
     - 用户 A 在小程序 1 的 OpenID: `oABC456`

2. **UnionID**
   - 同一用户在同一开放平台账号下的唯一标识
   - 同一用户在该开放平台下所有应用的 UnionID 相同
   - 示例：
     - 用户 A 在公众号 1 的 UnionID: `oUnion789`
     - 用户 A 在小程序 1 的 UnionID: `oUnion789` (相同)

### UnionID 的作用

- **跨应用识别用户**: 识别用户在不同微信应用（公众号、小程序、网页授权）中的身份
- **账号关联**: 自动关联同一用户在不同应用的账号
- **避免重复注册**: 用户在一个应用注册后，在其他应用登录时自动关联

## 实现逻辑

### 1. 数据库设计

```sql
CREATE TABLE mortise_user_oauth2_binding (
    ...
    open_id VARCHAR(255) NOT NULL,      -- 微信 OpenID
    union_id VARCHAR(255),              -- 微信 UnionID（可选）
    ...
    UNIQUE KEY uk_provider_openid (provider, open_id),
    KEY idx_provider_unionid (provider, union_id)  -- 新增 unionId 索引
);
```

**索引说明**:
- `uk_provider_openid`: 唯一索引，确保同一应用的 OpenID 唯一
- `idx_provider_unionid`: 复合索引，支持通过 UnionID 快速查询

### 2. 查找逻辑

`AuthServiceImpl.findExistingBinding()` 方法实现了智能查找：

```java
private UserOAuth2Binding findExistingBinding(StandardOAuth2UserInfo userInfo) {
    // 1. 优先通过 provider + openId 查找（标准方式）
    UserOAuth2Binding binding = userOAuth2BindingService.findByProviderAndOpenId(
        userInfo.getProvider(), userInfo.getOpenId()
    );
    
    if (binding != null) {
        return binding;
    }
    
    // 2. 如果是微信且有 unionId，尝试通过 unionId 查找
    if ("wechat".equalsIgnoreCase(userInfo.getProvider()) 
            && StringUtils.isNotBlank(userInfo.getUnionId())) {
        
        binding = userOAuth2BindingService.findByProviderAndUnionId(
            userInfo.getProvider(), userInfo.getUnionId()
        );
        
        if (binding != null) {
            // 找到了！更新 openId（可能是从公众号切换到小程序）
            if (!Objects.equals(binding.getOpenId(), userInfo.getOpenId())) {
                binding.setOpenId(userInfo.getOpenId());
                binding.setUpdatedTime(LocalDateTime.now());
                userOAuth2BindingService.updateById(binding);
            }
        }
    }
    
    return binding;
}
```

### 3. 场景示例

#### 场景 1: 用户首次通过公众号登录

```
输入:
- provider: "wechat"
- openId: "oXYZ123"
- unionId: "oUnion789"

流程:
1. 查找 (provider="wechat", openId="oXYZ123") -> 未找到
2. 查找 (provider="wechat", unionId="oUnion789") -> 未找到
3. 创建新用户
4. 创建绑定记录:
   - openId: "oXYZ123"
   - unionId: "oUnion789"
```

#### 场景 2: 用户通过小程序登录（已在公众号注册过）

```
输入:
- provider: "wechat"
- openId: "oABC456" (小程序的 OpenID，与公众号不同)
- unionId: "oUnion789" (与公众号相同)

流程:
1. 查找 (provider="wechat", openId="oABC456") -> 未找到
2. 查找 (provider="wechat", unionId="oUnion789") -> 找到了！
3. 返回已存在的用户
4. 更新绑定记录的 openId:
   - openId: "oABC456" (更新为小程序的)
   - unionId: "oUnion789" (保持不变)
```

#### 场景 3: 用户再次通过公众号登录

```
输入:
- provider: "wechat"
- openId: "oXYZ123"
- unionId: "oUnion789"

流程:
1. 查找 (provider="wechat", openId="oXYZ123") -> 未找到（因为上次更新成小程序的了）
2. 查找 (provider="wechat", unionId="oUnion789") -> 找到了！
3. 返回已存在的用户
4. 更新绑定记录的 openId:
   - openId: "oXYZ123" (更新回公众号的)
   - unionId: "oUnion789" (保持不变)
```

## 服务层 API

### UserOAuth2BindingService

新增了 `findByProviderAndUnionId` 方法：

```java
public interface UserOAuth2BindingService extends IService<UserOAuth2Binding> {
    
    /**
     * 根据提供商和 OpenID 查找绑定关系
     */
    UserOAuth2Binding findByProviderAndOpenId(String provider, String openId);
    
    /**
     * 根据提供商和 UnionID 查找绑定关系（仅微信）
     * 
     * @param provider OAuth2 提供商
     * @param unionId  微信 UnionID
     * @return 绑定关系，如果不存在则返回 null
     */
    UserOAuth2Binding findByProviderAndUnionId(String provider, String unionId);
    
    /**
     * 根据用户 ID 和提供商查找绑定关系
     */
    UserOAuth2Binding findByUserIdAndProvider(Long userId, String provider);
}
```

### 使用示例

```java
// 查找微信绑定（通过 unionId）
UserOAuth2Binding binding = userOAuth2BindingService.findByProviderAndUnionId(
    "wechat", 
    "oUnion789"
);

if (binding != null) {
    // 找到绑定，获取用户信息
    User user = userService.getById(binding.getUserId());
}
```

## 优势

### 1. 自动账号关联

用户在公众号注册后，通过小程序登录时：
- ✅ 自动识别为同一用户
- ✅ 无需重复注册
- ✅ 使用同一账号数据

### 2. OpenID 自动更新

用户在不同应用间切换时：
- ✅ 自动更新当前应用的 OpenID
- ✅ 保持 UnionID 不变
- ✅ 确保后续 API 调用正确

### 3. 灵活的查询策略

- 优先使用 OpenID（更精确）
- 回退使用 UnionID（跨应用）
- 兼容没有 UnionID 的场景（早期应用）

## 注意事项

### 1. UnionID 的获取条件

**需要满足以下条件之一**:
- 公众号：已认证的服务号或订阅号
- 小程序：已绑定到微信开放平台
- 网页授权：使用微信开放平台的网页授权

**无法获取 UnionID 的场景**:
- 未认证的公众号
- 未绑定开放平台的小程序
- 测试号

### 2. 数据一致性

由于 OpenID 会被更新，需要注意：
- ✅ 使用 userId 作为用户的唯一标识
- ✅ 不要在其他表中直接存储 OpenID
- ⚠️ 调用微信 API 时使用最新的 OpenID

### 3. 性能考虑

- ✅ 已添加 `(provider, union_id)` 索引
- ✅ 查询优先使用 OpenID（更快）
- ✅ UnionID 查询作为回退方案

### 4. 安全性

- UnionID 和 OpenID 都是敏感信息
- 不应在前端或日志中暴露
- 建议对存储的值进行脱敏

## 测试建议

### 单元测试

```java
@Test
void testWeChatUnionIdBinding() {
    // 1. 用户通过公众号登录
    StandardOAuth2UserInfo publicAccountInfo = StandardOAuth2UserInfo.builder()
            .provider("wechat")
            .openId("oPublic123")
            .unionId("oUnion789")
            .nickname("张三")
            .build();
    
    User user1 = authService.findOrCreateUserFromOAuth2(publicAccountInfo);
    assertNotNull(user1);
    
    // 2. 同一用户通过小程序登录（不同 openId，相同 unionId）
    StandardOAuth2UserInfo miniProgramInfo = StandardOAuth2UserInfo.builder()
            .provider("wechat")
            .openId("oMini456")  // 不同的 OpenID
            .unionId("oUnion789")  // 相同的 UnionID
            .nickname("张三")
            .build();
    
    User user2 = authService.findOrCreateUserFromOAuth2(miniProgramInfo);
    
    // 3. 验证是同一个用户
    assertEquals(user1.getId(), user2.getId());
    
    // 4. 验证 OpenID 已更新
    UserOAuth2Binding binding = userOAuth2BindingService.findByUserIdAndProvider(
        user1.getId(), "wechat"
    );
    assertEquals("oMini456", binding.getOpenId());
    assertEquals("oUnion789", binding.getUnionId());
}
```

### 集成测试

1. **公众号 -> 小程序 -> 公众号**
   - 验证 OpenID 正确切换
   - 验证 UnionID 保持不变
   - 验证始终是同一用户

2. **并发登录测试**
   - 同时从公众号和小程序登录
   - 验证不会创建重复用户
   - 验证数据一致性

3. **没有 UnionID 的场景**
   - 验证仍然可以正常登录
   - 验证只使用 OpenID 查找

## 数据迁移

如果已有微信绑定数据，需要：

1. **添加 UnionID 字段**（已在 V2 迁移脚本中）
2. **添加索引**（已在 V2 迁移脚本中）
3. **可选：回填 UnionID**
   ```sql
   -- 如果有其他途径获取 unionId，可以更新到表中
   UPDATE mortise_user_oauth2_binding 
   SET union_id = ?
   WHERE provider = 'wechat' AND user_id = ?;
   ```

## 最佳实践

### 1. 微信应用配置

在微信开放平台：
- ✅ 绑定所有相关应用（公众号、小程序）
- ✅ 使用同一个开放平台账号
- ✅ 确保所有应用都能获取 UnionID

### 2. 代码实现

```java
// 推荐：使用 StandardOAuth2UserInfo 封装
StandardOAuth2UserInfo userInfo = StandardOAuth2UserInfo.builder()
        .provider("wechat")
        .openId(wechatUser.getOpenid())
        .unionId(wechatUser.getUnionid())  // 可能为 null
        .nickname(wechatUser.getNickname())
        .avatar(wechatUser.getHeadimgurl())
        .build();

User user = authService.findOrCreateUserFromOAuth2(userInfo);
```

### 3. 日志记录

```java
log.info("微信登录: openId={}, unionId={}, userId={}", 
    userInfo.getOpenId(), 
    userInfo.getUnionId(),
    user.getId()
);
```

## 总结

通过支持微信 UnionID：

1. ✅ **实现了跨应用账号关联** - 用户在不同微信应用中自动关联
2. ✅ **提升了用户体验** - 无需重复注册，一次注册处处可用
3. ✅ **保证了数据一致性** - OpenID 自动更新，UnionID 保持不变
4. ✅ **提高了查询效率** - 通过索引优化查询性能
5. ✅ **兼容性良好** - 支持没有 UnionID 的场景

这个设计符合微信开放平台的最佳实践，能够完美支持微信生态的多应用场景。

---

**相关文档**:
- [OAuth2 绑定重构文档](./oauth2-binding-refactoring.md)
- [使用示例文档](./oauth2-binding-usage-examples.md)
- [微信开放平台文档](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html)
