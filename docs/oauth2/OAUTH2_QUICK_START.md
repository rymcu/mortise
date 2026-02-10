# OAuth2 多提供商快速开始指南

## 📚 目录

1. [前置准备](#前置准备)
2. [数据库迁移](#数据库迁移)
3. [配置 OAuth2 客户端](#配置-oauth2-客户端)
4. [实现提供商策略](#实现提供商策略)
5. [配置安全过滤器](#配置安全过滤器)
6. [测试登录流程](#测试登录流程)
7. [常见问题](#常见问题)

---

## 前置准备

### 1. 创建 mortise-member 模块

如果还没有 `mortise-member` 模块，需要先创建：

```xml
<!-- mortise-member/pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>mortise-member</artifactId>
    <name>mortise-member</name>
    
    <dependencies>
        <!-- 依赖 mortise-auth -->
        <dependency>
            <groupId>com.rymcu</groupId>
            <artifactId>mortise-auth</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- 其他依赖 -->
    </dependencies>
</project>
```

### 2. 注册 OAuth2 应用

分别在各平台注册 OAuth2 应用，获取 Client ID 和 Client Secret：

**GitHub**:
- 注册地址: https://github.com/settings/developers
- 回调 URL: `http://localhost:8080/oauth2/code/github`

**Google**:
- 注册地址: https://console.cloud.google.com/apis/credentials
- 回调 URL: `http://localhost:8080/oauth2/code/google`

**微信开放平台**:
- 注册地址: https://open.weixin.qq.com/
- 回调 URL: `http://localhost:8080/oauth2/code/wechat`

---

## 数据库迁移

### 1. 执行 SQL 脚本

```bash
# 执行建表脚本
psql -U postgres -d mortise -f docs/sql/member_oauth2_schema.sql
```

或手动执行：

```sql
-- 创建会员表
CREATE TABLE mortise_member (
    id BIGINT PRIMARY KEY,
    account VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    avatar VARCHAR(255),
    gender INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP
);

-- 创建 OAuth2 绑定表
CREATE TABLE mortise_member_oauth2_binding (
    id BIGINT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    open_id VARCHAR(100) NOT NULL,
    union_id VARCHAR(100),
    nickname VARCHAR(100),
    avatar VARCHAR(255),
    email VARCHAR(100),
    access_token TEXT,
    refresh_token TEXT,
    expires_at TIMESTAMP,
    raw_data TEXT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (provider, open_id)
);
```

---

## 配置 OAuth2 客户端

### 1. 配置 application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          # ========== GitHub ==========
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: read:user,user:email
            redirect-uri: "{baseUrl}/oauth2/code/github"

          # ========== Google ==========
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
            redirect-uri: "{baseUrl}/oauth2/code/google"

          # ========== 微信 ==========
          wechat:
            client-id: ${WECHAT_APP_ID}
            client-secret: ${WECHAT_APP_SECRET}
            scope: snsapi_login
            redirect-uri: "{baseUrl}/oauth2/code/wechat"
            authorization-grant-type: authorization_code
            client-authentication-method: client_secret_post
            provider: wechat

        provider:
          # 微信需要自定义 Provider
          wechat:
            authorization-uri: https://open.weixin.qq.com/connect/qrconnect
            token-uri: https://api.weixin.qq.com/sns/oauth2/access_token
            user-info-uri: https://api.weixin.qq.com/sns/userinfo
            user-name-attribute: openid
```

### 2. 配置环境变量

```bash
# .env 文件
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

WECHAT_APP_ID=your_wechat_app_id
WECHAT_APP_SECRET=your_wechat_app_secret
```

---

## 实现提供商策略

### 1. 已提供的策略 (mortise-auth 模块)

以下策略已经在 `mortise-auth` 模块中实现，**开箱即用**：

- ✅ `GitHubProviderStrategy` - GitHub 登录
- ✅ `GoogleProviderStrategy` - Google 登录  
- ✅ `WeChatProviderStrategy` - 微信登录（支持 UnionID）
- ✅ `LogtoProviderStrategy` - Logto OIDC

这些策略会被 Spring Boot 自动扫描并注册，**mortise-member 和 mortise-system 模块都可以直接使用**。

### 2. 自定义新提供商策略

如果需要支持其他提供商（如 Facebook、Twitter），在 **mortise-auth 模块**创建新策略类：

```java
// mortise-auth/src/main/java/com/rymcu/mortise/auth/strategy/FacebookProviderStrategy.java
package com.rymcu.mortise.auth.strategy;

import com.rymcu.mortise.auth.spi.OAuth2ProviderStrategy;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class FacebookProviderStrategy implements OAuth2ProviderStrategy {
    
    @Override
    public String getProviderType() {
        return "facebook";
    }

    @Override
    public boolean supports(String registrationId) {
        return "facebook".equalsIgnoreCase(registrationId);
    }

    @Override
    public StandardOAuth2UserInfo extractUserInfo(OAuth2User oauth2User) {
        return StandardOAuth2UserInfo.builder()
                .provider("facebook")
                .openId(oauth2User.getAttribute("id"))
                .nickname(oauth2User.getAttribute("name"))
                .email(oauth2User.getAttribute("email"))
                .avatar(oauth2User.getAttribute("picture"))
                .rawAttributes(oauth2User.getAttributes())
                .build();
    }
}
```

**优势**：策略在 `mortise-auth` 模块中，所有业务模块（member、system）都可以复用。

---

## 配置安全过滤器

### 1. 创建会员安全配置

```java
@Configuration
@EnableWebSecurity
public class MemberSecurityConfig {

    @Resource
    @Qualifier("memberOAuth2LoginSuccessHandler")
    private AuthenticationSuccessHandler oauth2LoginSuccessHandler;

    @Bean("memberSecurityFilterChain")
    @Order(2)  // 低于系统管理的优先级
    public SecurityFilterChain memberSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/member/**", "/oauth2/**")  // 会员端路径
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> 
                    authorization.baseUri("/oauth2/authorization"))
                .redirectionEndpoint(redirection -> 
                    redirection.baseUri("/oauth2/code/*"))
                .successHandler(oauth2LoginSuccessHandler)
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/member/oauth2/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
```

### 2. 路由规划建议

```
系统管理端 (mortise-system):
  - /api/v1/admin/**        管理后台 API
  - /api/v1/auth/**         管理员登录（Logto）
  
会员端 (mortise-member):
  - /api/v1/member/**       会员 API
  - /oauth2/authorization/* OAuth2 授权入口
  - /oauth2/code/*          OAuth2 回调地址
```

---

## 测试登录流程

### 1. 启动应用

```bash
mvn clean install
mvn spring-boot:run
```

### 2. GitHub 登录测试

#### 方式 1：浏览器访问

```
http://localhost:8080/oauth2/authorization/github
```

登录成功后会返回 JSON：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "01HXXX..."
  }
}
```

#### 方式 2：前端调用

```javascript
// React 示例
const handleGitHubLogin = () => {
  window.location.href = '/oauth2/authorization/github';
};

// 或使用弹窗
const handleGitHubLoginPopup = () => {
  const popup = window.open(
    '/oauth2/authorization/github',
    'OAuth2 Login',
    'width=600,height=700'
  );
  
  // 监听消息
  window.addEventListener('message', (event) => {
    if (event.data.token) {
      // 保存 token
      localStorage.setItem('token', event.data.token);
    }
  });
};
```

### 3. 验证数据库

```sql
-- 查看会员表
SELECT * FROM mortise_member;

-- 查看绑定表
SELECT * FROM mortise_member_oauth2_binding;
```

---

## 常见问题

### Q1: 登录后跳转到错误页面？

**A**: 检查 `redirect-uri` 配置是否正确，确保与 OAuth2 提供商注册的回调地址一致。

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            redirect-uri: "{baseUrl}/oauth2/code/github"  # 使用 {baseUrl} 自动适配
```

### Q2: 微信登录报错？

**A**: 微信 OAuth2 与标准协议有差异，需要特殊配置：

```yaml
wechat:
  client-authentication-method: client_secret_post  # 重要！
  provider: wechat
```

### Q3: 如何处理同一个邮箱绑定多个 OAuth2 账号？

**A**: 在 `MemberOAuth2ServiceImpl.oauth2Login()` 中已实现：

- 首次登录时，通过邮箱匹配现有会员
- 如果匹配成功，自动绑定 OAuth2 账号
- 如果不匹配，创建新会员

可以根据业务需求调整策略。

### Q4: 如何实现账号绑定/解绑功能？

**A**: 调用 `MemberOAuth2Service` 的方法：

```java
// 绑定
MemberOAuth2Binding binding = memberOAuth2Service.bindOAuth2Account(
    memberId, 
    userInfo
);

// 解绑
Boolean success = memberOAuth2Service.unbindOAuth2Account(
    memberId, 
    "github"
);
```

### Q5: 如何获取用户的访问令牌调用第三方 API？

**A**: 访问令牌已保存在 `MemberOAuth2Binding` 表中：

```java
MemberOAuth2Binding binding = memberOAuth2Service.findBinding(
    "github", 
    openId
);

String accessToken = binding.getAccessToken();

// 使用访问令牌调用 GitHub API
RestTemplate restTemplate = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth(accessToken);

ResponseEntity<String> response = restTemplate.exchange(
    "https://api.github.com/user",
    HttpMethod.GET,
    new HttpEntity<>(headers),
    String.class
);
```

### Q6: 如何区分系统管理员和普通会员？

**A**: 使用不同的数据表和安全配置：

| 用户类型 | 数据表 | OAuth2 提供商 | 路径前缀 |
|---------|--------|--------------|---------|
| 系统管理员 | `mortise_user` | Logto | `/api/v1/admin/**` |
| 普通会员 | `mortise_member` | GitHub/Google/微信 | `/api/v1/member/**` |

---

## 下一步

### 1. 实现前端登录页面

```html
<button onclick="loginWithGitHub()">
  <img src="github-icon.svg" /> 使用 GitHub 登录
</button>

<button onclick="loginWithGoogle()">
  <img src="google-icon.svg" /> 使用 Google 登录
</button>

<button onclick="loginWithWeChat()">
  <img src="wechat-icon.svg" /> 使用微信登录
</button>
```

### 2. 实现账号设置页面

```
我的账号
├── 基本信息
│   ├── 昵称、头像、邮箱
│   └── 修改密码
└── 账号绑定
    ├── GitHub ✓ 已绑定 [解绑]
    ├── Google ✗ 未绑定 [绑定]
    └── 微信 ✓ 已绑定 [解绑]
```

### 3. 添加更多提供商

- Facebook
- Twitter (X)
- Apple
- 钉钉
- 企业微信
- ...

---

## 总结

✅ **已完成**:
- OAuth2 多提供商策略架构
- GitHub、Google、微信、Logto 策略实现
- 会员 OAuth2 登录、绑定、解绑功能
- 数据库表结构设计
- 完整的业务逻辑实现

🚀 **开箱即用**:
- 只需配置 OAuth2 客户端信息
- 自动扫描并注册策略
- 零侵入扩展新提供商

📖 **参考文档**:
- [OAuth2 多提供商设计文档](OAUTH2_MULTI_PROVIDER_DESIGN.md)
- [Spring Security OAuth2 文档](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
