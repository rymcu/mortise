# 微信模块配置简化说明

## 背景

项目最初使用 `wx-java-mp-spring-boot-starter`，后来为了更好的控制和灵活性，改为直接使用 `weixin-java-mp` 核心库，并基于数据库实现动态配置。

## 配置类演变

### ❌ 已移除的配置

#### 1. WxMpStandardConfiguration
- **用途**：兼容 `wx-java-mp-spring-boot-starter` 的配置方式
- **配置来源**：`application.yml` 中的 `wx.mp` 配置
- **移除原因**：
  - 不再使用 `wx-java-mp-spring-boot-starter`
  - 配置方式不够灵活
  - 无法支持多账号动态管理
  - 与数据库配置方案重复

#### 2. @Component 和 @ConfigurationProperties
在 `WeChatMpProperties` 和 `WeChatOpenProperties` 中：
- **移除原因**：
  - 这些类不是 Spring Bean，是数据传输对象（DTO）
  - 由 `WeChatMultiAccountConfigService` 从数据库动态创建
  - 避免 Bean 重复注册问题

### ✅ 保留的配置（推荐）

#### WeChatMpConfiguration
**核心特性**：
- ✅ 从数据库动态加载配置
- ✅ 支持多账号管理
- ✅ 支持配置缓存
- ✅ 支持敏感信息加密
- ✅ 优雅降级（数据库表不存在时不影响启动）
- ✅ 灵活的配置开关

**配置来源**：
```
数据库表：
  - mortise_wechat_account  （账号基本信息）
  - mortise_wechat_config   （账号扩展配置）
```

**启用方式**：
```yaml
wechat:
  mp:
    enabled: true  # 启用微信公众号功能
```

## 当前架构

### 依赖关系

```
pom.xml
  └─ weixin-java-mp (核心库)
       ↓
WeChatMpConfiguration
  ├─ WeChatMultiAccountConfigService (从数据库加载配置)
  │    ├─ WeChatAccountMapper
  │    ├─ WeChatConfigMapper
  │    └─ StringEncryptor (配置解密)
  │
  └─ StringRedisTemplate (Redis 统一管理)
       ↓
  创建 WxMpService Bean
```

### 配置流程

```
1. 应用启动
   └─ 读取 application.yml: wechat.mp.enabled=true
      
2. WeChatMpConfiguration 初始化
   └─ 检查 WeChatMultiAccountConfigService 是否可用
      
3. 从数据库加载配置
   └─ WeChatMultiAccountConfigService.loadDefaultMpConfig()
      ├─ 查询 mortise_wechat_account 表
      ├─ 查询 mortise_wechat_config 表
      ├─ 解密敏感信息（appSecret, aesKey）
      └─ 创建 WeChatMpProperties 对象
      
4. 创建 WxMpService
   ├─ 如果配置存在且启用 → 创建服务
   ├─ 如果使用 Redis → 注入 StringRedisTemplate
   └─ 如果不使用 Redis → 使用内存存储
   
5. 服务可用
   └─ 其他模块可以注入 Optional<WxMpService>
```

## 配置示例

### application.yml
```yaml
# 1. 启用微信模块
wechat:
  mp:
    enabled: true    # 启用公众号功能
  open:
    enabled: true    # 启用开放平台功能

# 2. Redis 配置（可选，用于存储 AccessToken）
spring:
  data:
    redis:
      host: 192.168.21.238
      port: 6379
      password: ENC(encrypted-password)
      database: 1
```

### 数据库配置

#### 创建公众号账号
```sql
INSERT INTO mortise_wechat_account 
(account_type, account_name, app_id, app_secret, is_default, is_enabled)
VALUES 
('mp', '官方公众号', 'wx1234567890abcdef', 'ENC(encrypted_secret)', 1, 1);
```

#### 添加扩展配置
```sql
-- 假设账号ID为1
INSERT INTO mortise_wechat_config 
(account_id, config_key, config_value, is_encrypted)
VALUES 
(1, 'token', 'your_token', 0),
(1, 'aesKey', 'ENC(encrypted_aes_key)', 1);
```

### 使用 REST API 配置（推荐）
```bash
# 创建公众号账号
POST /api/wechat/admin/accounts
Content-Type: application/json

{
  "accountType": "mp",
  "accountName": "官方公众号",
  "appId": "wx1234567890abcdef",
  "appSecret": "your-app-secret",
  "isDefault": true,
  "isEnabled": true
}
```

## 配置对比

### 文件配置方式（已废弃）

```yaml
# ❌ 不推荐：配置写死在文件中
wx:
  mp:
    configs:
      - app-id: wx1234567890abcdef
        secret: your-app-secret
        token: your-token
        aes-key: your-aes-key
        enabled: true
```

**缺点**：
- ❌ 配置写死，不灵活
- ❌ 修改需要重启应用
- ❌ 敏感信息暴露在配置文件
- ❌ 不支持动态多账号

### 数据库配置方式（推荐）

```yaml
# ✅ 推荐：只需开关
wechat:
  mp:
    enabled: true
```

**优点**：
- ✅ 配置存储在数据库，动态管理
- ✅ 支持运行时修改（需重启）
- ✅ 敏感信息加密存储
- ✅ 支持多账号
- ✅ 支持配置缓存
- ✅ 通过 API 或管理界面配置

## Bean 命名规范

### 公众号服务
```java
@Bean(name = "wxMpService")  // 主 Bean
@Primary
public Optional<WxMpService> wxMpService() { ... }

@Bean  // 多账号服务映射
public Optional<Map<Long, WxMpService>> wxMpServiceMap() { ... }
```

### 开放平台服务
```java
@Bean  // 主 Bean
@Primary
public Optional<WxOpenService> wxOpenService() { ... }

@Bean  // 多账号服务映射
public Optional<Map<Long, WxOpenService>> wxOpenServiceMap() { ... }
```

## 注意事项

### 1. Optional 包装
所有微信服务 Bean 都使用 `Optional` 包装：
```java
@Service
public class MyService {
    private final Optional<WxMpService> wxMpService;
    
    public void doSomething() {
        if (wxMpService.isEmpty()) {
            throw new RuntimeException("微信服务未初始化");
        }
        wxMpService.get().doSomething();
    }
}
```

### 2. 配置开关
- `wechat.mp.enabled=true` 才会创建 Bean
- 设置为 `false` 或不配置，则不创建 Bean

### 3. 数据库表
必须先执行 Flyway 迁移创建表：
```sql
resources/db/migration/V1__Create_WeChat_Multi_Account_Tables.sql
```

### 4. Redis 可选
- 开发环境：可以不使用 Redis（内存存储）
- 生产环境：建议使用 Redis（分布式部署）

## 迁移指南

### 从 wx-java-mp-spring-boot-starter 迁移

#### 步骤 1：修改 pom.xml
```xml
<!-- 移除 -->
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>wx-java-mp-spring-boot-starter</artifactId>
</dependency>

<!-- 添加 -->
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>weixin-java-mp</artifactId>
</dependency>
```

#### 步骤 2：删除 WxMpStandardConfiguration.java
```bash
rm src/main/java/.../config/WxMpStandardConfiguration.java
```

#### 步骤 3：移除配置文件中的 wx.mp.configs
```yaml
# 删除这些配置
wx:
  mp:
    configs: [...]
```

#### 步骤 4：保留简化配置
```yaml
wechat:
  mp:
    enabled: true
```

#### 步骤 5：在数据库中配置账号
使用 REST API 或直接插入数据库。

#### 步骤 6：重启应用验证
查看日志确认初始化成功：
```
✓ 微信公众号默认服务初始化成功（数据库配置），AppID: wx12***cdef
✓ 微信公众号多账号服务初始化完成，共 1 个账号
```

## 相关文档

- [优雅启动修复](./GRACEFUL_STARTUP_FIX.md)
- [Redis 统一管理](./REDIS_UNIFIED_MANAGEMENT.md)
- [多账号使用指南](./WECHAT_MULTI_ACCOUNT_GUIDE.md)

## 总结

通过移除 `WxMpStandardConfiguration` 和相关注解：

1. ✅ **简化了架构**：只保留一套配置方案
2. ✅ **避免了冲突**：解决了 Bean 重复注册问题
3. ✅ **提升了灵活性**：完全基于数据库动态配置
4. ✅ **统一了管理**：Redis、配置、多账号统一管理
5. ✅ **增强了安全性**：敏感信息加密存储在数据库

这是一个更加现代化、灵活且易于维护的架构！
