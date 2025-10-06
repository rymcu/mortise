# OAuth2 策略模块位置调整说明

## 📍 调整内容

将 OAuth2 提供商策略实现从 `mortise-member` 模块移至 `mortise-auth` 模块。

## 🔄 变更对比

### 调整前（初始设计）

```
mortise-auth/
└── spi/
    ├── OAuth2ProviderStrategy.java      # SPI 接口
    └── StandardOAuth2UserInfo.java      # 数据模型

mortise-member/
└── oauth2/
    └── strategy/
        ├── GitHubProviderStrategy.java   ❌
        ├── GoogleProviderStrategy.java   ❌
        ├── WeChatProviderStrategy.java   ❌
        └── LogtoProviderStrategy.java    ❌
```

### 调整后（推荐架构）✅

```
mortise-auth/
├── spi/
│   ├── OAuth2ProviderStrategy.java      # SPI 接口
│   └── StandardOAuth2UserInfo.java      # 数据模型
├── service/
│   └── OAuth2UserInfoExtractor.java     # 提取器
└── strategy/
    ├── GitHubProviderStrategy.java      ✅
    ├── GoogleProviderStrategy.java      ✅
    ├── WeChatProviderStrategy.java      ✅
    └── LogtoProviderStrategy.java       ✅

mortise-member/
└── service/
    └── MemberOAuth2Service.java         # 使用策略
```

## 🎯 调整理由

| 方面 | 调整前 | 调整后 |
|------|--------|--------|
| **复用性** | ❌ 每个模块需要自己实现策略 | ✅ 所有模块共享策略 |
| **维护性** | ❌ 修改策略需要在多处修改 | ✅ 统一管理，一处修改 |
| **清晰性** | ❌ 策略分散在各业务模块 | ✅ 认证相关逻辑集中在 auth 模块 |
| **灵活性** | ❌ 系统管理员和会员不能共享策略 | ✅ 两种用户都可使用同一套策略 |

## 💡 设计原则

### 单一职责原则

```
mortise-auth (认证基础设施)
├── 定义：OAuth2 认证能力的 SPI 接口
├── 实现：常用 OAuth2 提供商的策略
└── 目标：提供开箱即用的认证能力

mortise-member (业务层)
├── 使用：OAuth2 策略进行认证
├── 实现：会员业务逻辑
└── 目标：专注于业务功能
```

### 依赖倒置原则

```
高层模块 (mortise-member)
    ↓ 依赖抽象
SPI 接口 (OAuth2ProviderStrategy)
    ↑ 实现
低层模块 (GitHubProviderStrategy, GoogleProviderStrategy)
```

## 📦 使用场景示例

### 场景 1：会员端使用 GitHub 登录

```java
// mortise-member 模块
@Service
public class MemberOAuth2ServiceImpl {
    
    @Resource
    private OAuth2UserInfoExtractor extractor; // 来自 mortise-auth
    
    public Member oauth2Login(OAuth2User oauth2User, String registrationId) {
        // 自动使用 mortise-auth 提供的 GitHubProviderStrategy
        StandardOAuth2UserInfo userInfo = extractor.extractUserInfo(oauth2User, registrationId);
        // ... 业务逻辑
    }
}
```

### 场景 2：系统管理端使用 Logto 登录

```java
// mortise-system 模块
@Service
public class AuthServiceImpl {
    
    @Resource
    private OAuth2UserInfoExtractor extractor; // 同样来自 mortise-auth
    
    public TokenUser oauth2Login(OidcUser oidcUser, String registrationId) {
        // 自动使用 mortise-auth 提供的 LogtoProviderStrategy
        StandardOAuth2UserInfo userInfo = extractor.extractUserInfo(oidcUser, registrationId);
        // ... 业务逻辑
    }
}
```

### 场景 3：扩展新提供商

```java
// 在 mortise-auth 模块中新增
@Component
public class FacebookProviderStrategy implements OAuth2ProviderStrategy {
    // 实现接口
}

// mortise-member 和 mortise-system 都自动获得 Facebook 登录能力！
```

## ✅ 优势总结

1. **开箱即用**
   - `mortise-auth` 提供 4 种常用策略（GitHub、Google、微信、Logto）
   - 业务模块无需自己实现策略

2. **统一管理**
   - 所有 OAuth2 提供商策略在一个模块中
   - 便于维护和升级

3. **跨模块复用**
   - `mortise-member` 和 `mortise-system` 共享策略
   - 新增模块也能自动获得所有策略

4. **清晰的职责划分**
   - `mortise-auth`：认证基础设施
   - `mortise-member`：会员业务逻辑
   - `mortise-system`：管理业务逻辑

## 🔧 迁移步骤（如果您已使用旧架构）

### 1. 删除旧文件

```bash
# 删除 mortise-member 中的策略文件
rm -rf mortise-member/src/main/java/com/rymcu/mortise/member/oauth2/strategy/
```

### 2. 使用新文件

新的策略文件已创建在：
```
mortise-auth/src/main/java/com/rymcu/mortise/auth/strategy/
├── GitHubProviderStrategy.java
├── GoogleProviderStrategy.java
├── WeChatProviderStrategy.java
└── LogtoProviderStrategy.java
```

### 3. 更新导入语句（如需要）

如果有直接引用策略类的地方，更新包名：
```java
// 旧的
import com.rymcu.mortise.member.oauth2.strategy.GitHubProviderStrategy;

// 新的
import com.rymcu.mortise.auth.strategy.GitHubProviderStrategy;
```

但通常不需要这一步，因为业务层通过 `OAuth2UserInfoExtractor` 自动选择策略。

## 📚 相关文档已更新

以下文档已更新以反映新架构：
- ✅ `OAUTH2_MULTI_PROVIDER_DESIGN.md`
- ✅ `OAUTH2_QUICK_START.md`
- ✅ `OAUTH2_IMPLEMENTATION_SUMMARY.md`

---

**总结**：这次调整让架构更加清晰、合理，符合单一职责原则和依赖倒置原则。感谢提出这个优化建议！🎉
