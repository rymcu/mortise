# WeChat 模块重构 - 快速上手指南

## 📋 概述

本指南帮助你快速理解和使用重构后的 WeChat 模块。

## 🎯 核心变化

### 从4个Service简化到2个

**之前** ❌
- WeChatConfigService
- WeChatConfigManagementService  
- WeChatAccountManagementService
- WeChatMultiAccountConfigService

**现在** ✅
- **WeChatAccountService** - 账号和配置管理
- **WeChatConfigService** - 配置加载使用

## 🚀 5分钟快速开始

### 1. 创建微信账号

```java
@RestController
@RequestMapping("/api/v1/admin/wechat/accounts")
public class WeChatAccountController {
    
    @Resource
    private WeChatAccountService accountService;
    
    @PostMapping
    public GlobalResult<Long> createAccount(@RequestBody CreateAccountRequest request) {
        WeChatAccount account = new WeChatAccount();
        account.setAccountType("mp");  // mp-公众号, open-开放平台
        account.setAccountName("RYMCU公众号");
        account.setAppId("wxabcdefg123456");
        account.setAppSecret("your-secret");  // 自动加密
        account.setIsDefault(1);  // 设为默认
        account.setIsEnabled(1);  // 启用
        
        Long id = accountService.createAccount(account);
        return GlobalResult.success(id);
    }
}
```

### 2. 保存配置

```java
@PostMapping("/{id}/configs")
public GlobalResult<Boolean> batchSaveConfigs(
    @PathVariable Long id,
    @RequestBody BatchSaveConfigsRequest request
) {
    boolean result = accountService.batchSaveConfigs(id, request.getConfigs());
    return GlobalResult.success(result);
}
```

请求示例：
```json
{
    "configs": [
        {
            "configKey": "token",
            "configValue": "mytoken123",
            "configLabel": "Token",
            "isEncrypted": false
        },
        {
            "configKey": "aesKey",
            "configValue": "myaeskey456",
            "configLabel": "AES Key",
            "isEncrypted": true
        }
    ]
}
```

### 3. 加载配置使用

```java
@Service
public class MyWeChatService {
    
    @Resource
    private WeChatConfigService configService;
    
    public void doSomething() {
        // 方式1: 加载默认公众号配置
        WeChatMpProperties mpConfig = configService.loadDefaultMpConfig();
        
        // 方式2: 按账号ID加载
        WeChatMpProperties mpConfig = configService.loadMpConfigByAccountId(1L);
        
        // 方式3: 按AppID加载
        WeChatMpProperties mpConfig = configService.loadMpConfigByAppId("wxabcdefg123456");
        
        // 使用配置
        String appId = mpConfig.getAppId();
        String appSecret = mpConfig.getAppSecret();  // 已自动解密
        String token = mpConfig.getToken();
    }
}
```

## 📚 完整API列表

### WeChatAccountController

**基础路径**: `/api/v1/admin/wechat/accounts`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页查询账号 |
| GET | `/{id}` | 获取详情 |
| POST | `/` | 创建账号 |
| PUT | `/{id}` | 更新账号 |
| DELETE | `/{id}` | 删除账号 |
| PATCH | `/{id}/default` | 设为默认 |
| PATCH | `/{id}/status` | 启用/禁用 |
| GET | `/{id}/configs` | 获取配置列表 |
| POST | `/{id}/configs` | 批量保存配置 |
| DELETE | `/{id}/configs/{key}` | 删除配置 |
| POST | `/cache/refresh` | 刷新缓存 |

### 查询示例

```bash
# 分页查询
curl -X GET "http://localhost:8080/api/v1/admin/wechat/accounts?pageNum=1&pageSize=10&accountType=mp"

# 获取详情
curl -X GET "http://localhost:8080/api/v1/admin/wechat/accounts/1"

# 创建账号
curl -X POST "http://localhost:8080/api/v1/admin/wechat/accounts" \
  -H "Content-Type: application/json" \
  -d '{
    "accountType": "mp",
    "accountName": "RYMCU公众号",
    "appId": "wxabcdefg123456",
    "appSecret": "secret123456",
    "isDefault": true,
    "isEnabled": true
  }'

# 保存配置
curl -X POST "http://localhost:8080/api/v1/admin/wechat/accounts/1/configs" \
  -H "Content-Type: application/json" \
  -d '{
    "configs": [
      {"configKey": "token", "configValue": "mytoken", "isEncrypted": false},
      {"configKey": "aesKey", "configValue": "myaeskey", "isEncrypted": true}
    ]
  }'
```

## 💡 常用场景

### 场景1: 多公众号管理

```java
// 1. 创建主公众号（默认）
WeChatAccount mainAccount = new WeChatAccount();
mainAccount.setAccountType("mp");
mainAccount.setAccountName("主公众号");
mainAccount.setAppId("wx111111");
mainAccount.setIsDefault(1);  // 设为默认
accountService.createAccount(mainAccount);

// 2. 创建子公众号
WeChatAccount subAccount = new WeChatAccount();
subAccount.setAccountType("mp");
subAccount.setAccountName("子公众号");
subAccount.setAppId("wx222222");
subAccount.setIsDefault(0);  // 非默认
accountService.createAccount(subAccount);

// 3. 使用默认公众号
WeChatMpProperties defaultConfig = configService.loadDefaultMpConfig();

// 4. 使用指定公众号
WeChatMpProperties subConfig = configService.loadMpConfigByAppId("wx222222");
```

### 场景2: 配置的批量导入导出

```java
// 导出配置
@GetMapping("/{id}/configs/export")
public GlobalResult<List<WeChatConfig>> exportConfigs(@PathVariable Long id) {
    List<WeChatConfig> configs = accountService.listConfigs(id);
    return GlobalResult.success(configs);
}

// 导入配置
@PostMapping("/{id}/configs/import")
public GlobalResult<Boolean> importConfigs(
    @PathVariable Long id,
    @RequestBody List<WeChatConfig> configs
) {
    boolean result = accountService.batchSaveConfigs(id, configs);
    accountService.refreshCache();  // 刷新缓存
    return GlobalResult.success(result);
}
```

### 场景3: 配置热更新

```java
@PostMapping("/{id}/configs")
public GlobalResult<Boolean> updateConfig(
    @PathVariable Long id,
    @RequestBody WeChatConfig config
) {
    // 1. 保存配置
    boolean result = accountService.saveConfig(
        id,
        config.getConfigKey(),
        config.getConfigValue(),
        config.getIsEncrypted() == 1
    );
    
    // 2. 自动刷新缓存（ServiceImpl内部处理）
    // 无需手动调用 refreshCache()
    
    return GlobalResult.success(result);
}
```

### 场景4: 账号状态管理

```java
// 启用账号
@PatchMapping("/{id}/status")
public GlobalResult<Boolean> toggleAccount(
    @PathVariable Long id,
    @RequestParam Boolean enabled
) {
    boolean result = accountService.toggleAccount(id, enabled);
    return GlobalResult.success(result);
}

// 设置默认账号
@PatchMapping("/{id}/default")
public GlobalResult<Boolean> setDefault(@PathVariable Long id) {
    // 自动取消同类型的其他默认账号
    boolean result = accountService.setDefaultAccount(id);
    return GlobalResult.success(result);
}
```

## 🔐 安全特性

### 1. 自动加密

```java
// 创建账号时，AppSecret自动加密
account.setAppSecret("my-secret");
accountService.createAccount(account);  // 自动加密存储

// 加载配置时，自动解密
WeChatMpProperties config = configService.loadDefaultMpConfig();
String appSecret = config.getAppSecret();  // 已解密
```

### 2. 敏感信息脱敏

```java
// API返回时自动脱敏
{
    "appId": "wxabcdefg123456",
    "appSecret": "my-***-et",  // 脱敏显示
    ...
}
```

### 3. 权限控制

```java
// 管理接口需要ADMIN权限
@PreAuthorize("hasRole('ADMIN')")
public class WeChatAccountController {
    // ...
}

// 公开接口无需权限
@RestController
@RequestMapping("/api/v1/wechat/portal")
public class WeChatPortalController {
    // ...
}
```

## 🎨 最佳实践

### 1. 使用缓存

```java
// 配置会自动缓存到Redis
WeChatMpProperties config = configService.loadDefaultMpConfig();  // 第一次查DB
WeChatMpProperties config = configService.loadDefaultMpConfig();  // 第二次读缓存

// 更新配置后自动失效缓存
accountService.saveConfig(id, key, value, false);  // 自动清除缓存
```

### 2. 分页查询

```java
@GetMapping
public GlobalResult<Page<WeChatAccount>> pageAccounts(
    @RequestParam(defaultValue = "1") Integer pageNum,
    @RequestParam(defaultValue = "10") Integer pageSize,
    @RequestParam(required = false) String accountType
) {
    WeChatAccountSearch search = new WeChatAccountSearch();
    search.setPageNum(pageNum);
    search.setPageSize(pageSize);
    search.setAccountType(accountType);
    
    Page<WeChatAccount> page = new Page<>(pageNum, pageSize);
    page = accountService.pageAccounts(page, search);
    
    return GlobalResult.success(page);
}
```

### 3. 异常处理

```java
try {
    accountService.createAccount(account);
} catch (IllegalArgumentException e) {
    // 参数错误
    return GlobalResult.failure("参数错误: " + e.getMessage());
} catch (Exception e) {
    // 系统错误
    log.error("创建账号失败", e);
    return GlobalResult.failure("系统错误");
}
```

## 📖 相关文档

- [详细重构方案](./REFACTORING_PLAN.md)
- [重构总结](./REFACTORING_SUMMARY.md)
- [架构演进图](./ARCHITECTURE_DIAGRAM.md)
- [多账号使用指南](./WECHAT_MULTI_ACCOUNT_GUIDE.md)

## ❓ 常见问题

### Q1: 如何迁移旧代码？

**A**: 旧Service → 新Service映射

```java
// 旧代码
WeChatMultiAccountConfigService.loadMpConfigByAccountId(1L);

// 新代码
WeChatConfigService.loadMpConfigByAccountId(1L);
```

### Q2: 如何处理多账号？

**A**: 使用不同的账号ID或AppID加载

```java
// 方式1: 按ID
configService.loadMpConfigByAccountId(1L);
configService.loadMpConfigByAccountId(2L);

// 方式2: 按AppID
configService.loadMpConfigByAppId("wx111111");
configService.loadMpConfigByAppId("wx222222");
```

### Q3: 配置更新后何时生效？

**A**: 立即生效，缓存自动失效

```java
accountService.saveConfig(id, key, value, false);
// 下次加载配置时会重新从数据库读取
```

### Q4: 如何手动刷新缓存？

**A**: 调用 refreshCache()

```java
accountService.refreshCache();  // 清除所有账号缓存
configService.refreshCache();   // 清除所有配置缓存
```

## 🎯 下一步

1. ✅ 阅读本快速指南
2. ⏳ 查看 [详细重构方案](./REFACTORING_PLAN.md)
3. ⏳ 实现 ServiceImpl
4. ⏳ 重构 Controller
5. ⏳ 添加单元测试

---

**文档版本**: v1.0.0  
**最后更新**: 2025-10-06  
**维护者**: ronger
