# OAuth2 多提供商架构图

## 整体架构

```mermaid
graph TB
    subgraph "mortise-auth 基础设施层"
        A1[OAuth2ProviderStrategy<br/>SPI 接口]
        A2[StandardOAuth2UserInfo<br/>标准化数据模型]
        A3[OAuth2UserInfoExtractor<br/>用户信息提取器]
        
        subgraph "提供商策略实现 (开箱即用)"
            C1[GitHubProviderStrategy]
            C2[GoogleProviderStrategy]
            C3[WeChatProviderStrategy]
            C4[LogtoProviderStrategy]
        end
    end

    subgraph "mortise-member 业务层"
        B1[MemberOAuth2Service<br/>业务逻辑接口]
        B2[MemberOAuth2ServiceImpl<br/>业务逻辑实现]
        
        subgraph "数据持久层"
            D1[(Member<br/>会员表)]
            D2[(MemberOAuth2Binding<br/>绑定表)]
        end
    end

    subgraph "mortise-system 管理层"
        E1[(User<br/>系统用户表)]
        E2[AuthService]
    end

    C1 -.实现.-> A1
    C2 -.实现.-> A1
    C3 -.实现.-> A1
    C4 -.实现.-> A1
    
    A3 -->|使用| A1
    A3 -->|返回| A2
    
    B2 -->|调用| A3
    B2 -->|操作| D1
    B2 -->|操作| D2
    B1 <-.实现.- B2
    
    E2 -->|调用| A3
    E2 -->|管理员登录| E1

    style A1 fill:#e1f5ff
    style A2 fill:#e1f5ff
    style A3 fill:#e1f5ff
    style B1 fill:#fff4e6
    style B2 fill:#fff4e6
    style C1 fill:#d1fae5
    style C2 fill:#d1fae5
    style C3 fill:#d1fae5
    style C4 fill:#d1fae5
    style D1 fill:#f3f4f6
    style D2 fill:#f3f4f6
    style E1 fill:#fef3c7
    style E2 fill:#fef3c7
```

## 数据模型关系

```mermaid
erDiagram
    MEMBER ||--o{ MEMBER_OAUTH2_BINDING : has
    
    MEMBER {
        bigint id PK
        varchar account UK
        varchar nickname
        varchar email
        varchar avatar
        int status
        timestamp created_time
    }
    
    MEMBER_OAUTH2_BINDING {
        bigint id PK
        bigint member_id FK
        varchar provider
        varchar open_id
        varchar union_id
        varchar nickname
        varchar avatar
        varchar email
        text access_token
        text raw_data
        timestamp created_time
    }
    
    USER {
        bigint id PK
        varchar account UK
        varchar nickname
        varchar email
        varchar open_id
        varchar provider
    }
```

## OAuth2 登录时序图

```mermaid
sequenceDiagram
    participant User as 用户
    participant Browser as 浏览器
    participant Backend as 后端服务
    participant OAuth2 as OAuth2提供商
    participant Extractor as OAuth2UserInfoExtractor
    participant Strategy as ProviderStrategy
    participant Service as MemberOAuth2Service
    participant DB as 数据库

    User->>Browser: 点击「GitHub 登录」
    Browser->>Backend: GET /oauth2/authorization/github
    Backend->>OAuth2: 重定向到 GitHub 授权页
    OAuth2->>User: 显示授权确认页
    User->>OAuth2: 确认授权
    OAuth2->>Backend: 回调 /oauth2/code/github?code=xxx
    Backend->>OAuth2: 用 code 换取 access_token
    OAuth2->>Backend: 返回 token + 用户信息
    Backend->>Extractor: extractUserInfo(oauth2User, "github")
    Extractor->>Strategy: 查找支持 "github" 的策略
    Strategy->>Extractor: GitHubProviderStrategy
    Strategy->>Extractor: 提取并返回 StandardOAuth2UserInfo
    Extractor->>Backend: 返回标准化数据
    Backend->>Service: oauth2Login(userInfo)
    Service->>DB: 查找绑定记录
    alt 已绑定
        DB->>Service: 返回已绑定的 Member
    else 未绑定
        Service->>DB: 创建 Member + OAuth2Binding
        DB->>Service: 返回新创建的 Member
    end
    Service->>Backend: 返回 Member
    Backend->>Backend: 生成 JWT Token
    Backend->>Browser: 返回 Token (JSON)
    Browser->>User: 登录成功
```

## 策略模式工作流程

```mermaid
graph LR
    A[OAuth2User<br/>GitHub 用户数据] --> B{OAuth2UserInfoExtractor}
    B --> C{查找策略}
    
    C -->|registrationId=github| D[GitHubProviderStrategy]
    C -->|registrationId=google| E[GoogleProviderStrategy]
    C -->|registrationId=wechat| F[WeChatProviderStrategy]
    
    D --> G[extractUserInfo]
    E --> G
    F --> G
    
    G --> H[StandardOAuth2UserInfo<br/>标准化数据]
    
    H --> I[MemberOAuth2Service<br/>业务处理]
    
    style B fill:#e1f5ff
    style C fill:#f0f9ff
    style D fill:#dbeafe
    style E fill:#dbeafe
    style F fill:#dbeafe
    style G fill:#fef3c7
    style H fill:#dcfce7
    style I fill:#fff4e6
```

## 账号绑定/解绑流程

```mermaid
stateDiagram-v2
    [*] --> 未绑定
    
    未绑定 --> OAuth2授权: 点击绑定
    OAuth2授权 --> 检查绑定状态: 授权成功
    
    检查绑定状态 --> 绑定失败: 已被其他用户绑定
    检查绑定状态 --> 已绑定: 创建绑定记录
    
    绑定失败 --> 未绑定: 返回错误
    
    已绑定 --> 检查登录方式: 点击解绑
    
    检查登录方式 --> 解绑失败: 无其他登录方式
    检查登录方式 --> 未绑定: 删除绑定记录
    
    解绑失败 --> 已绑定: 至少保留一种登录方式
    
    已绑定 --> [*]: 账号设置页面
```

## 多提供商数据标准化

```mermaid
graph TD
    subgraph "不同提供商的原始数据"
        A1[GitHub<br/>id, login, email, avatar_url]
        A2[Google<br/>sub, name, email, picture]
        A3[微信<br/>openid, nickname, headimgurl, sex]
        A4[Logto<br/>sub, name, email, picture]
    end
    
    subgraph "策略层提取"
        B1[GitHubProviderStrategy]
        B2[GoogleProviderStrategy]
        B3[WeChatProviderStrategy]
        B4[LogtoProviderStrategy]
    end
    
    subgraph "标准化数据模型"
        C[StandardOAuth2UserInfo<br/>---<br/>provider: string<br/>openId: string<br/>nickname: string<br/>email: string<br/>avatar: string<br/>gender: integer<br/>rawAttributes: map]
    end
    
    A1 --> B1
    A2 --> B2
    A3 --> B3
    A4 --> B4
    
    B1 --> C
    B2 --> C
    B3 --> C
    B4 --> C
    
    C --> D[业务层统一处理]
    
    style A1 fill:#fef3c7
    style A2 fill:#fef3c7
    style A3 fill:#fef3c7
    style A4 fill:#fef3c7
    style B1 fill:#dbeafe
    style B2 fill:#dbeafe
    style B3 fill:#dbeafe
    style B4 fill:#dbeafe
    style C fill:#dcfce7
    style D fill:#fff4e6
```

## 微信 UnionID 处理流程

```mermaid
graph TD
    A[微信登录] --> B{是否有 UnionID?}
    
    B -->|否| C[只用 OpenID 查找]
    B -->|是| D[优先用 UnionID 查找]
    
    C --> E{找到绑定记录?}
    D --> F{找到绑定记录?}
    
    E -->|是| G[返回已绑定会员]
    E -->|否| H[创建新会员]
    
    F -->|是| I{OpenID 是否匹配?}
    F -->|否| H
    
    I -->|是| G
    I -->|否| J[同一 UnionID 的新 OpenID<br/>创建新绑定记录]
    
    J --> G
    H --> K[创建绑定记录]
    K --> L[返回新会员]
    
    style A fill:#e1f5ff
    style D fill:#fef3c7
    style J fill:#fef3c7
    style G fill:#dcfce7
    style L fill:#dcfce7
```

---

**说明**：

1. **整体架构图**：展示三层架构关系
2. **数据模型关系图**：展示数据库表关系
3. **登录时序图**：完整的 OAuth2 登录流程
4. **策略模式工作流程**：展示策略自动选择机制
5. **账号绑定/解绑流程**：状态转换图
6. **数据标准化流程**：展示不同提供商数据的统一化
7. **微信 UnionID 处理**：展示微信特殊场景的处理逻辑

这些图表可以直接在支持 Mermaid 的 Markdown 查看器中渲染（如 GitHub、Typora、VS Code）。
