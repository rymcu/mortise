# OAuth2 多提供商扩展设计文档

## 一、架构概览

### 1.1 设计原则

- **开放封闭原则**：对扩展开放，对修改封闭
- **策略模式**：每个 OAuth2 提供商独立实现策略接口
- **SPI 机制**：通过 Spring 自动发现和注入策略
- **数据标准化**：统一的用户信息数据模型
- **多租户隔离**：系统用户和会员用户分离

### 1.2 模块划分

```
mortise-auth (认证基础设施层)
├── SPI 接口
│   ├── OAuth2ProviderStrategy      # OAuth2 提供商策略接口 (SPI)
│   └── StandardOAuth2UserInfo       # 标准化用户信息模型
├── 核心服务
│   └── OAuth2UserInfoExtractor      # 用户信息提取器
└── 策略实现 (开箱即用)
    ├── GitHubProviderStrategy       # GitHub 策略
    ├── GoogleProviderStrategy       # Google 策略
    ├── WeChatProviderStrategy       # 微信策略
    └── LogtoProviderStrategy        # Logto 策略

mortise-system (管理后台业务层)
├── User (mortise_user)          # 系统用户表
└── OAuth2: Logto                # 使用 Logto 单点登录

mortise-member (用户端业务层)
├── Member (mortise_member)                    # 会员用户表
├── MemberOAuth2Binding (mortise_member_oauth2_binding)  # OAuth2 绑定表
└── MemberOAuth2Service                        # OAuth2 业务逻辑
```

## 二、数据模型设计

### 2.1 mortise_member (会员用户表)

```sql
CREATE TABLE mortise_member (
    id BIGINT PRIMARY KEY,
    account VARCHAR(50) UNIQUE,     -- 账号（可选，支持账号密码登录）
    password VARCHAR(100),           -- 密码（可选）
    nickname VARCHAR(50),            -- 昵称
    email VARCHAR(100),              -- 邮箱
    phone VARCHAR(20),               -- 手机号
    avatar VARCHAR(255),             -- 头像
    gender INTEGER DEFAULT 0,        -- 性别
    status INTEGER DEFAULT 1,        -- 状态
    created_time TIMESTAMP,
    last_login_time TIMESTAMP
);
```

### 2.2 mortise_member_oauth2_binding (OAuth2 绑定表)

```sql
CREATE TABLE mortise_member_oauth2_binding (
    id BIGINT PRIMARY KEY,
    member_id BIGINT NOT NULL,              -- 会员 ID
    provider VARCHAR(50) NOT NULL,          -- 提供商: github, google, wechat
    open_id VARCHAR(100) NOT NULL,          -- OAuth2 唯一标识
    union_id VARCHAR(100),                  -- 微信 UnionID
    nickname VARCHAR(100),                  -- OAuth2 昵称
    avatar VARCHAR(255),                    -- OAuth2 头像
    email VARCHAR(100),                     -- OAuth2 邮箱
    access_token TEXT,                      -- 访问令牌
    refresh_token TEXT,                     -- 刷新令牌
    expires_at TIMESTAMP,                   -- 过期时间
    raw_data TEXT,                          -- 原始数据 (JSON)
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    UNIQUE KEY uk_provider_openid (provider, open_id),
    KEY idx_member_id (member_id),
    KEY idx_union_id (union_id)
);
```

**设计要点**：

1. **一对多关系**：一个会员可以绑定多个 OAuth2 账号
2. **唯一约束**：`(provider, open_id)` 确保不重复绑定
3. **微信特殊处理**：支持 `union_id` 字段，用于开放平台多应用关联
4. **数据同步**：保存 OAuth2 提供商的原始数据，便于后续同步更新

## 三、核心接口设计

### 3.1 OAuth2ProviderStrategy (SPI)

```java
public interface OAuth2ProviderStrategy {
    String getProviderType();                           // 提供商标识
    boolean supports(String registrationId);            // 是否支持
    StandardOAuth2UserInfo extractUserInfo(OAuth2User); // 提取用户信息
    int getOrder();                                     // 优先级
}
```

### 3.2 StandardOAuth2UserInfo (标准化数据模型)

```java
@Data
@Builder
public class StandardOAuth2UserInfo {
    private String provider;        // 提供商
    private String openId;          // 唯一标识
    private String unionId;         // 微信 UnionID
    private String nickname;        // 昵称
    private String avatar;          // 头像
    private String email;           // 邮箱
    private String phone;           // 手机号
    private Integer gender;         // 性别
    private Map<String, Object> rawAttributes;  // 原始数据
}
```

### 3.3 MemberOAuth2Service (业务层)

```java
public interface MemberOAuth2Service {
    Member oauth2Login(StandardOAuth2UserInfo userInfo);            // OAuth2 登录
    MemberOAuth2Binding bindOAuth2Account(Long memberId, ...);      // 绑定
    Boolean unbindOAuth2Account(Long memberId, String provider);    // 解绑
    MemberOAuth2Binding findBinding(String provider, String openId);
    MemberOAuth2Binding findBindingByUnionId(String unionId);       // 微信 UnionID 查找
}
```

## 四、使用流程

### 4.1 OAuth2 登录流程

```
1. 用户点击「使用 GitHub 登录」
   ↓
2. 跳转到 /oauth2/authorization/github
   ↓
3. GitHub 授权后回调
   ↓
4. OAuth2LoginSuccessHandler 处理
   - OAuth2UserInfoExtractor.extractUserInfo(oauth2User, "github")
   - 自动选择 GitHubProviderStrategy
   - 提取 StandardOAuth2UserInfo
   ↓
5. MemberOAuth2Service.oauth2Login(userInfo)
   - 查找绑定记录：findBinding(provider, openId)
   - 如果存在：返回已绑定的 Member
   - 如果不存在：
     a. 尝试通过 email 匹配现有 Member（可选）
     b. 或创建新 Member
     c. 创建 OAuth2Binding 记录
   ↓
6. 生成 JWT Token 返回
```

### 4.2 账号绑定流程

```
1. 已登录会员进入「账号设置」
   ↓
2. 点击「绑定 Google 账号」
   ↓
3. Google 授权后回调
   ↓
4. MemberOAuth2Service.bindOAuth2Account(memberId, userInfo)
   - 检查该 Google 账号是否已被其他会员绑定
   - 如果未绑定：创建绑定记录
   - 如果已绑定：提示错误
```

## 五、配置示例

### 5.1 application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          # GitHub
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: read:user,user:email
            redirect-uri: "{baseUrl}/oauth2/code/github"

          # Google
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
            redirect-uri: "{baseUrl}/oauth2/code/google"

          # 微信（需要自定义 Provider）
          wechat:
            client-id: ${WECHAT_APP_ID}
            client-secret: ${WECHAT_APP_SECRET}
            scope: snsapi_login
            redirect-uri: "{baseUrl}/oauth2/code/wechat"
            authorization-grant-type: authorization_code
            client-authentication-method: client_secret_post
            provider: wechat

        provider:
          # 微信自定义 Provider
          wechat:
            authorization-uri: https://open.weixin.qq.com/connect/qrconnect
            token-uri: https://api.weixin.qq.com/sns/oauth2/access_token
            user-info-uri: https://api.weixin.qq.com/sns/userinfo
            user-name-attribute: openid
```

## 六、提供商策略实现示例

### 6.1 GitHubProviderStrategy

```java
@Component
public class GitHubProviderStrategy implements OAuth2ProviderStrategy {
    @Override
    public String getProviderType() {
        return "github";
    }

    @Override
    public boolean supports(String registrationId) {
        return "github".equalsIgnoreCase(registrationId);
    }

    @Override
    public StandardOAuth2UserInfo extractUserInfo(OAuth2User oauth2User) {
        return StandardOAuth2UserInfo.builder()
                .provider("github")
                .openId(String.valueOf(oauth2User.getAttribute("id")))
                .nickname(oauth2User.getAttribute("login"))
                .email(oauth2User.getAttribute("email"))
                .avatar(oauth2User.getAttribute("avatar_url"))
                .rawAttributes(oauth2User.getAttributes())
                .build();
    }
}
```

### 6.2 WeChatProviderStrategy

```java
@Component
public class WeChatProviderStrategy implements OAuth2ProviderStrategy {
    @Override
    public StandardOAuth2UserInfo extractUserInfo(OAuth2User oauth2User) {
        return StandardOAuth2UserInfo.builder()
                .provider("wechat")
                .openId(oauth2User.getAttribute("openid"))
                .unionId(oauth2User.getAttribute("unionid"))  // 开放平台
                .nickname(oauth2User.getAttribute("nickname"))
                .avatar(oauth2User.getAttribute("headimgurl"))
                .gender(oauth2User.getAttribute("sex"))
                .country(oauth2User.getAttribute("country"))
                .province(oauth2User.getAttribute("province"))
                .city(oauth2User.getAttribute("city"))
                .rawAttributes(oauth2User.getAttributes())
                .build();
    }
}
```

## 七、扩展新提供商步骤

### 步骤 1：在 application.yml 中添加配置

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          新提供商:
            client-id: xxx
            client-secret: xxx
            scope: xxx
```

### 步骤 2：在 mortise-auth 模块创建策略类

```java
// mortise-auth/src/main/java/com/rymcu/mortise/auth/strategy/新提供商ProviderStrategy.java
package com.rymcu.mortise.auth.strategy;

@Component
public class 新提供商ProviderStrategy implements OAuth2ProviderStrategy {
    // 实现接口方法
}
```

### 步骤 3：完成！

Spring Boot 会自动发现并注册该策略，mortise-member 和 mortise-system 模块都可以直接使用。

## 八、优势

1. **零侵入扩展**：新增提供商无需修改核心代码
2. **类型安全**：编译时检查，避免运行时错误
3. **数据标准化**：统一的用户信息模型，便于业务层处理
4. **多账号绑定**：支持一个用户绑定多个 OAuth2 账号
5. **微信特殊支持**：支持 UnionID，解决开放平台多应用场景
6. **灵活配置**：支持动态启用/禁用提供商
7. **数据同步**：保存原始数据，便于后续同步更新

## 九、注意事项

### 9.1 微信 OAuth2 特殊处理

微信的 OAuth2 实现与标准协议有差异：

1. **Token 接口**：使用 `client_secret_post` 而非 `client_secret_basic`
2. **UnionID**：开放平台需要特殊处理
3. **扫码登录**：网页应用使用 `snsapi_login`，公众号使用 `snsapi_base` 或 `snsapi_userinfo`

### 9.2 安全建议

1. **Token 加密存储**：`access_token` 和 `refresh_token` 应加密存储
2. **绑定校验**：绑定前需验证用户身份（已登录）
3. **解绑保护**：至少保留一种登录方式（如手机号或邮箱）
4. **数据脱敏**：日志中不输出敏感信息

### 9.3 性能优化

1. **缓存策略**：缓存 OAuth2 绑定关系
2. **批量查询**：支持批量查询绑定关系
3. **索引优化**：对高频查询字段建立索引

## 十、总结

这套设计方案实现了：

✅ **可扩展性**：轻松接入新的 OAuth2 提供商  
✅ **低耦合**：auth 层提供基础能力，业务层实现具体策略  
✅ **高内聚**：每个提供商策略独立，互不影响  
✅ **易维护**：清晰的分层架构，便于理解和维护  
✅ **生产就绪**：考虑了安全、性能、特殊场景等实际需求
