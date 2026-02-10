# 微信 OAuth2 策略整合说明

## 📋 变更概述

**日期**: 2025-10-07  
**类型**: 代码重构 - 消除重复定义  
**影响范围**: 微信 OAuth2 认证

## 🎯 变更目标

消除 `WeChatProviderStrategy` 和 `WeChatOAuth2Adapter` 之间的重复定义，统一微信 OAuth2 认证实现。

## 📊 变更前状态

### 存在的问题

1. **重复定义**: 两个类都实现了 `OAuth2ProviderStrategy` 接口
2. **功能重叠**: 都处理微信 OAuth2 用户信息提取
3. **配置冲突**: 两个 `@Component` 可能导致 Spring Bean 选择混乱
4. **维护成本**: 需要同时维护两套相似的代码

### 原有类对比

| 特性 | WeChatProviderStrategy | WeChatOAuth2Adapter |
|------|----------------------|-------------------|
| **模块位置** | `mortise-auth` | `mortise-wechat` |
| **PROVIDER_TYPE** | `"wechat"` | `"wechat-qrcode"` |
| **Order优先级** | `30` | `35` |
| **支持的registrationId** | `wechat*` | `wechat-qrcode`, `wechat-h5` |
| **条件注册** | 无条件 | `@ConditionalOnBean(WeChatLoginService)` |
| **额外功能** | 无 | `getUserInfoByCode()` 方法 |

## ✅ 变更内容

### 1. 删除的文件

- ❌ `mortise-auth/src/main/java/com/rymcu/mortise/auth/strategy/WeChatProviderStrategy.java`

### 2. 增强的文件

- ✅ `mortise-wechat/src/main/java/com/rymcu/mortise/wechat/integration/WeChatOAuth2Adapter.java`

### 3. 具体改动

#### `WeChatOAuth2Adapter` 的改进

**改动点 1: PROVIDER_TYPE 统一**
```java
// 变更前
private static final String PROVIDER_TYPE = "wechat-qrcode";

// 变更后
private static final String PROVIDER_TYPE = "wechat";
```

**改动点 2: 支持更广泛的 registrationId**
```java
// 变更前
@Override
public boolean supports(String registrationId) {
    return registrationId != null && 
           (registrationId.equals("wechat-qrcode") || 
            registrationId.equals("wechat-h5"));
}

// 变更后
@Override
public boolean supports(String registrationId) {
    // 支持所有微信相关的 OAuth2 认证
    // 包括: wechat, wechat-open, wechat-qrcode, wechat-h5 等
    return registrationId != null && 
           registrationId.toLowerCase().startsWith("wechat");
}
```

**改动点 3: 优先级调整**
```java
// 变更前
@Override
public int getOrder() {
    return 35;
}

// 变更后
@Override
public int getOrder() {
    // 优先级高于其他策略，确保微信登录优先匹配
    return 30;
}
```

**改动点 4: 日志和注释优化**
```java
// 变更前
log.debug("提取微信扫码登录用户信息: {}", attributes);

// 变更后
log.debug("提取微信用户信息: {}", attributes);

// 并添加详细的字段注释
.openId((String) attributes.get("openid"))        // 微信 OpenID
.unionId((String) attributes.get("unionid"))      // 微信 UnionID（开放平台）
// ... 其他字段
```

## 🔍 技术细节

### 为什么保留 WeChatOAuth2Adapter？

1. **功能更完整**: 包含 `getUserInfoByCode()` 方法，支持直接通过授权码获取用户信息
2. **模块归属合理**: 位于 `mortise-wechat` 模块，职责更清晰
3. **条件加载**: 使用 `@ConditionalOnBean(WeChatLoginService.class)`，只在微信模块启用时生效
4. **集成 WxJava SDK**: 与微信公众号 SDK 深度集成

### 统一后的支持范围

现在 `WeChatOAuth2Adapter` 支持所有以 `wechat` 开头的 `registrationId`：

- ✅ `wechat` - 微信开放平台网站应用
- ✅ `wechat-open` - 微信开放平台
- ✅ `wechat-qrcode` - 微信扫码登录
- ✅ `wechat-h5` - 微信H5登录
- ✅ `wechat-*` - 任何自定义微信登录场景

## 📝 配置示例

无需修改现有配置，所有微信相关的 OAuth2 配置都继续有效：

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          # 标准微信登录
          wechat:
            client-id: ${WECHAT_APP_ID}
            client-secret: ${WECHAT_APP_SECRET}
            scope: snsapi_login
            provider: wechat
          
          # 微信扫码登录
          wechat-qrcode:
            client-id: ${WECHAT_APP_ID}
            client-secret: ${WECHAT_APP_SECRET}
            scope: snsapi_login
            provider: wechat
          
          # 微信H5登录
          wechat-h5:
            client-id: ${WECHAT_APP_ID}
            client-secret: ${WECHAT_APP_SECRET}
            scope: snsapi_userinfo
            provider: wechat
        
        provider:
          wechat:
            authorization-uri: https://open.weixin.qq.com/connect/qrconnect
            token-uri: https://api.weixin.qq.com/sns/oauth2/access_token
            user-info-uri: https://api.weixin.qq.com/sns/userinfo
            user-name-attribute: openid
```

## 🧪 测试验证

### 验证步骤

1. ✅ 编译检查
   ```bash
   mvn clean compile -pl mortise-auth,mortise-wechat
   ```

2. ✅ 单元测试
   ```bash
   mvn test -pl mortise-wechat
   ```

3. ✅ 集成测试
   - 测试标准微信登录 (`wechat`)
   - 测试扫码登录 (`wechat-qrcode`)
   - 测试H5登录 (`wechat-h5`)

### 预期结果

- ✅ Spring 容器中只有一个微信 OAuth2 策略 Bean
- ✅ 所有微信登录场景都能正常工作
- ✅ 用户信息正确提取和转换
- ✅ UnionID 正确处理（如果有）

## 🔄 回滚方案

如果需要回滚此变更：

1. 恢复 `WeChatProviderStrategy.java` 文件
2. 将 `WeChatOAuth2Adapter` 的改动还原：
   - `PROVIDER_TYPE` 改回 `"wechat-qrcode"`
   - `supports()` 方法改回只支持 `wechat-qrcode` 和 `wechat-h5`
   - `getOrder()` 改回 `35`

## 📚 相关文档

- [OAuth2 多提供商扩展架构](./OAUTH2_MULTI_PROVIDER_DESIGN.md)
- [OAuth2 实现总结](./OAUTH2_IMPLEMENTATION_SUMMARY.md)
- [微信集成架构改进](./wechat-integration-architecture-improvement.md)
- [微信模块 README](../mortise-wechat/README.md)

## 👥 影响范围

### 受影响的模块

- ✅ `mortise-auth` - 删除了 `WeChatProviderStrategy`
- ✅ `mortise-wechat` - 增强了 `WeChatOAuth2Adapter`

### 不受影响的部分

- ✅ 其他 OAuth2 提供商策略（GitHub, Google 等）
- ✅ OAuth2 核心架构和 SPI 接口
- ✅ 现有的微信登录配置
- ✅ 用户数据和业务逻辑

## ✨ 优势

1. **消除重复**: 只有一个微信 OAuth2 策略实现
2. **职责清晰**: 微信相关功能都在 `mortise-wechat` 模块
3. **易于维护**: 只需维护一套代码
4. **功能完整**: 保留了所有必要的功能和扩展性
5. **兼容性好**: 支持所有微信 OAuth2 场景

## 🎉 总结

此次整合成功消除了 `WeChatProviderStrategy` 和 `WeChatOAuth2Adapter` 之间的重复定义，统一了微信 OAuth2 认证实现。整合后的 `WeChatOAuth2Adapter` 功能更完整，支持范围更广，且保持了良好的兼容性。

---

**变更执行人**: GitHub Copilot  
**审核状态**: ⏳ 待审核  
**部署状态**: ⏳ 待部署
