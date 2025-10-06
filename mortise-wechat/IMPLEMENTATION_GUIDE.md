# WeChat 模块重构实施指南

## 📋 当前进度

### ✅ 已完成
1. **架构设计** - 100%
   - [x] 4个Service简化为2个
   - [x] Service/ServiceImpl模式设计
   - [x] API规范定义

2. **文档产出** - 100%
   - [x] REFACTORING_README.md
   - [x] docs/REFACTORING_SUMMARY.md
   - [x] docs/REFACTORING_PLAN.md
   - [x] docs/ARCHITECTURE_DIAGRAM.md
   - [x] docs/QUICK_START.md
   - [x] REFACTORING_CHECKLIST.md

3. **Service接口** - 100%
   - [x] WeChatAccountService
   - [x] WeChatConfigService

4. **Model类** - 100%
   - [x] WeChatAccountSearch（已完善）

5. **Service实现** - 50%
   - [x] WeChatAccountServiceImpl（已完成）
   - [ ] WeChatConfigServiceImpl（待实现）

### ⏳ 待完成
- [ ] WeChatConfigServiceImpl
- [ ] Controller层重构（4个）
- [ ] Request/Response DTO
- [ ] 单元测试
- [ ] 集成测试

## 🚀 下一步实施步骤

### 步骤 1: 完成 WeChatConfigServiceImpl

创建文件：`src/main/java/com/rymcu/mortise/wechat/service/impl/WeChatConfigServiceImpl.java`

```java
package com.rymcu.mortise.wechat.service.impl;

import com.rymcu.mortise.wechat.config.WeChatMpProperties;
import com.rymcu.mortise.wechat.config.WeChatOpenProperties;
import com.rymcu.mortise.wechat.entity.WeChatAccount;
import com.rymcu.mortise.wechat.entity.WeChatConfig;
import com.rymcu.mortise.wechat.mapper.WeChatAccountMapper;
import com.rymcu.mortise.wechat.mapper.WeChatConfigMapper;
import com.rymcu.mortise.wechat.service.WeChatConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatConfigServiceImpl implements WeChatConfigService {

    private final WeChatAccountMapper accountMapper;
    private final WeChatConfigMapper configMapper;
    
    @Qualifier("jasyptStringEncryptor")
    private final Optional<StringEncryptor> stringEncryptor;

    @Override
    @Cacheable(value = "wechat:config", key = "'mp:default'", unless = "#result == null")
    public WeChatMpProperties loadDefaultMpConfig() {
        return loadMpConfigByAccountId(null);
    }

    @Override
    @Cacheable(value = "wechat:config", key = "'mp:' + (#accountId != null ? #accountId : 'default')", 
               unless = "#result == null")
    public WeChatMpProperties loadMpConfigByAccountId(Long accountId) {
        log.info("从数据库加载微信公众号配置，accountId: {}", accountId);

        WeChatAccount account = accountId != null
            ? accountMapper.selectOneById(accountId)
            : accountMapper.selectDefaultByType("mp");

        if (account == null) {
            log.warn("未找到微信公众号账号");
            return null;
        }

        List<WeChatConfig> configs = configMapper.selectByAccountId(account.getId());
        Map<String, WeChatConfig> configMap = configs.stream()
                .collect(Collectors.toMap(WeChatConfig::getConfigKey, Function.identity()));

        WeChatMpProperties properties = new WeChatMpProperties();
        properties.setEnabled(account.getIsEnabled() == 1);
        properties.setAppId(account.getAppId());
        properties.setAppSecret(decryptValue(account.getAppSecret(), true));
        properties.setToken(getConfigValue(configMap, "token", false));
        properties.setAesKey(getConfigValue(configMap, "aesKey", true));

        log.info("微信公众号配置加载完成，account: {}, enabled: {}, appId: {}",
                account.getAccountName(), properties.isEnabled(), maskString(properties.getAppId()));

        return properties;
    }

    @Override
    public WeChatMpProperties loadMpConfigByAppId(String appId) {
        WeChatAccount account = accountMapper.selectByAppId(appId);
        if (account == null) {
            log.warn("未找到 AppID 对应的公众号账号: {}", appId);
            return null;
        }
        return loadMpConfigByAccountId(account.getId());
    }

    @Override
    @Cacheable(value = "wechat:config", key = "'open:default'", unless = "#result == null")
    public WeChatOpenProperties loadDefaultOpenConfig() {
        return loadOpenConfigByAccountId(null);
    }

    @Override
    @Cacheable(value = "wechat:config", key = "'open:' + (#accountId != null ? #accountId : 'default')",
               unless = "#result == null")
    public WeChatOpenProperties loadOpenConfigByAccountId(Long accountId) {
        log.info("从数据库加载微信开放平台配置，accountId: {}", accountId);

        WeChatAccount account = accountId != null
            ? accountMapper.selectOneById(accountId)
            : accountMapper.selectDefaultByType("open");

        if (account == null) {
            log.warn("未找到微信开放平台账号");
            return null;
        }

        List<WeChatConfig> configs = configMapper.selectByAccountId(account.getId());
        Map<String, WeChatConfig> configMap = configs.stream()
                .collect(Collectors.toMap(WeChatConfig::getConfigKey, Function.identity()));

        WeChatOpenProperties properties = new WeChatOpenProperties();
        properties.setEnabled(account.getIsEnabled() == 1);
        properties.setAppId(account.getAppId());
        properties.setAppSecret(decryptValue(account.getAppSecret(), true));
        properties.setRedirectUri(getConfigValue(configMap, "redirectUri", false));
        properties.setQrCodeExpireSeconds(getIntValue(configMap, "qrCodeExpireSeconds", 300));

        log.info("微信开放平台配置加载完成，account: {}, enabled: {}, appId: {}",
                account.getAccountName(), properties.isEnabled(), maskString(properties.getAppId()));

        return properties;
    }

    @Override
    @CacheEvict(value = "wechat:config", allEntries = true)
    public void refreshCache() {
        log.info("微信配置缓存已刷新");
    }

    // ==================== 私有方法 ====================

    private String decryptValue(String value, boolean needDecrypt) {
        if (value == null || !needDecrypt) {
            return value;
        }

        if (stringEncryptor.isPresent()) {
            try {
                return stringEncryptor.get().decrypt(value);
            } catch (Exception e) {
                log.error("解密失败，将使用原值: {}", e.getMessage());
                return value;
            }
        } else {
            log.warn("加密器未配置，无法解密值");
            return value;
        }
    }

    private String getConfigValue(Map<String, WeChatConfig> configMap, String key, boolean needDecrypt) {
        WeChatConfig config = configMap.get(key);
        if (config == null || config.getConfigValue() == null) {
            return null;
        }

        String value = config.getConfigValue();
        if (needDecrypt && config.getIsEncrypted() != null && config.getIsEncrypted() == 1) {
            value = decryptValue(value, true);
        }

        return value;
    }

    private int getIntValue(Map<String, WeChatConfig> configMap, String key, int defaultValue) {
        String value = getConfigValue(configMap, key, false);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置 {} 值 {} 不是有效的整数，使用默认值 {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    private String maskString(String str) {
        if (str == null || str.length() <= 6) {
            return "***";
        }
        return str.substring(0, 3) + "***" + str.substring(str.length() - 3);
    }
}
```

### 步骤 2: 重构 WeChatAccountController

文件路径：`src/main/java/com/rymcu/mortise/wechat/controller/WeChatAccountController.java`

参考示例已在 `docs/QUICK_START.md` 中提供。

关键点：
1. 使用 `@Tag` 添加Swagger标签
2. 所有方法返回 `GlobalResult<T>`
3. 使用 `@Operation` 添加方法说明
4. 使用 `@Valid` 验证参数
5. 使用 `WeChatAccountService` 替代旧的Service

### 步骤 3: 创建 Request DTO

目录：`src/main/java/com/rymcu/mortise/wechat/model/request/`

需要创建：
- `CreateAccountRequest.java`
- `UpdateAccountRequest.java`
- `BatchSaveConfigsRequest.java`

示例代码在 `docs/REFACTORING_PLAN.md` 中。

### 步骤 4: 添加单元测试

目录：`src/test/java/com/rymcu/mortise/wechat/service/`

创建：
- `WeChatAccountServiceTest.java`
- `WeChatConfigServiceTest.java`

## 📝 实施注意事项

### 1. 编译错误处理

如果遇到 TableDef 找不到的错误：
```java
// 不要使用
import static com.rymcu.mortise.wechat.entity.table.WeChatAccountTableDef.WECHAT_ACCOUNT;

// 改用字符串查询
QueryWrapper.create()
    .from(WeChatAccount.class)
    .where("del_flag = 0")
```

### 2. 依赖注入

StringEncryptor 使用 Optional 包装：
```java
@Qualifier("jasyptStringEncryptor")
private final Optional<StringEncryptor> stringEncryptor;

// 使用时
if (stringEncryptor.isPresent()) {
    String encrypted = stringEncryptor.get().encrypt(value);
}
```

### 3. 缓存注解

确保正确使用缓存注解：
```java
@Cacheable(value = "wechat:config", key = "'mp:default'", unless = "#result == null")
@CacheEvict(value = "wechat:config", allEntries = true)
```

### 4. 事务管理

所有修改操作添加事务：
```java
@Transactional(rollbackFor = Exception.class)
@CacheEvict(value = "wechat:config", allEntries = true)
public boolean createAccount(WeChatAccount account) {
    // ...
}
```

## 🎯 快速命令

### 编译项目
```powershell
cd d:\rymcu2024\mortise
mvn clean compile -pl mortise-wechat -am
```

### 运行测试
```powershell
mvn test -pl mortise-wechat
```

### 检查代码风格
```powershell
mvn checkstyle:check -pl mortise-wechat
```

## 📚 参考文档

1. **架构设计** - `docs/ARCHITECTURE_DIAGRAM.md`
2. **详细方案** - `docs/REFACTORING_PLAN.md`
3. **快速上手** - `docs/QUICK_START.md`
4. **实施清单** - `REFACTORING_CHECKLIST.md`

## ✅ 验收标准

### Service层
- [x] WeChatAccountServiceImpl 编译通过
- [ ] WeChatConfigServiceImpl 编译通过
- [ ] 单元测试通过
- [ ] 代码覆盖率 > 80%

### Controller层
- [ ] 所有接口返回 GlobalResult
- [ ] 支持分页查询
- [ ] Swagger 文档完整
- [ ] 集成测试通过

### 整体
- [ ] 无编译错误
- [ ] 无SonarQube严重问题
- [ ] 代码评审通过
- [ ] 文档更新完整

---

**最后更新**: 2025-10-06  
**当前进度**: 40%  
**预计完成**: 2025-10-09
