# WeChat 模块重构 - 快速参考卡片

## 🎯 核心改进

| 项目 | 重构前 | 重构后 | 改进 |
|------|--------|--------|------|
| Service数量 | 4个 | 2个 | ✅ 简化50% |
| 架构模式 | 无统一规范 | Service/ServiceImpl | ✅ 清晰一致 |
| API返回 | 不统一 | GlobalResult | ✅ 标准化 |
| 分页支持 | ❌ 无 | ✅ 有 | ✅ 完善 |
| 文档 | 零散 | 7个核心文档 | ✅ 完整 |

## 📁 文件结构

```
mortise-wechat/
├── REFACTORING_README.md          ⭐ 重构完成通知
├── REFACTORING_CHECKLIST.md       ⭐ 实施检查清单  
├── IMPLEMENTATION_GUIDE.md         ⭐ 实施指南(NEW)
├── QUICK_REFERENCE.md              ⭐ 快速参考(本文件)
└── docs/
    ├── REFACTORING_SUMMARY.md      ⭐ 重构总结
    ├── REFACTORING_PLAN.md         ⭐ 重构方案
    ├── ARCHITECTURE_DIAGRAM.md     ⭐ 架构演进图
    └── QUICK_START.md              ⭐ 快速上手
```

## 🔑 关键类说明

### Service 层

| 类名 | 职责 | 状态 |
|------|------|------|
| `WeChatAccountService` | 账号管理接口 | ✅ 已完成 |
| `WeChatAccountServiceImpl` | 账号管理实现 | ✅ 已完成 |
| `WeChatConfigService` | 配置加载接口 | ✅ 已完成 |
| `WeChatConfigServiceImpl` | 配置加载实现 | ⏳ 待实现 |

### Controller 层

| 类名 | 路径 | 状态 |
|------|------|------|
| `WeChatAccountController` | `/api/v1/admin/wechat/accounts` | ⏳ 待重构 |
| `WeChatLoginController` | `/api/v1/wechat/login` | ⏳ 待重构 |
| `WeChatMessageController` | `/api/v1/admin/wechat/messages` | ⏳ 待重构 |
| `WeChatPortalController` | `/api/v1/wechat/portal/{appId}` | ⏳ 待重构 |

## 📝 常用代码片段

### 1. 分页查询
```java
@GetMapping
public GlobalResult<Page<WeChatAccount>> pageAccounts(
    @Valid WeChatAccountSearch search
) {
    Page<WeChatAccount> page = new Page<>(search.getPageNum(), search.getPageSize());
    page = accountService.pageAccounts(page, search);
    return GlobalResult.success(page);
}
```

### 2. 创建资源
```java
@PostMapping
public GlobalResult<Long> createAccount(@Valid @RequestBody CreateAccountRequest request) {
    WeChatAccount account = new WeChatAccount();
    // ... 设置属性
    Long id = accountService.createAccount(account);
    return GlobalResult.success(id);
}
```

### 3. 更新资源
```java
@PutMapping("/{id}")
public GlobalResult<Boolean> updateAccount(
    @PathVariable Long id,
    @Valid @RequestBody UpdateAccountRequest request
) {
    WeChatAccount account = new WeChatAccount();
    account.setId(id);
    // ... 设置属性
    boolean result = accountService.updateAccount(account);
    return GlobalResult.success(result);
}
```

### 4. 删除资源
```java
@DeleteMapping("/{id}")
public GlobalResult<Boolean> deleteAccount(@PathVariable Long id) {
    boolean result = accountService.deleteAccount(id);
    return GlobalResult.success(result);
}
```

### 5. 批量操作
```java
@PostMapping("/{id}/configs")
public GlobalResult<Boolean> batchSaveConfigs(
    @PathVariable Long id,
    @Valid @RequestBody BatchSaveConfigsRequest request
) {
    boolean result = accountService.batchSaveConfigs(id, request.getConfigs());
    return GlobalResult.success(result);
}
```

## 🔧 常用工具方法

### QueryWrapper 查询
```java
QueryWrapper query = QueryWrapper.create()
    .from(WeChatAccount.class)
    .where("del_flag = 0")
    .and("account_type = {0}", accountType)
    .orderBy("created_time DESC");
```

### 加密/解密
```java
// 加密
private String encryptValue(String value) {
    if (stringEncryptor.isPresent()) {
        return stringEncryptor.get().encrypt(value);
    }
    return value;
}

// 解密
private String decryptValue(String value, boolean needDecrypt) {
    if (needDecrypt && stringEncryptor.isPresent()) {
        return stringEncryptor.get().decrypt(value);
    }
    return value;
}
```

### 脱敏
```java
private String maskString(String str) {
    if (str == null || str.length() <= 6) {
        return "***";
    }
    return str.substring(0, 3) + "***" + str.substring(str.length() - 3);
}
```

## 📊 实施进度

```
架构设计    ████████████████████ 100%
文档产出    ████████████████████ 100%
Service接口 ████████████████████ 100%
Service实现 ██████████░░░░░░░░░░  50%
Controller  ░░░░░░░░░░░░░░░░░░░░   0%
DTO/VO      ░░░░░░░░░░░░░░░░░░░░   0%
测试        ░░░░░░░░░░░░░░░░░░░░   0%
────────────────────────────────────
总体进度    ████████░░░░░░░░░░░░  40%
```

## 🎓 学习资源

### 必读文档（按优先级）
1. 🔴 [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) - 实施指南
2. 🔴 [QUICK_START.md](./docs/QUICK_START.md) - 快速上手
3. 🟡 [REFACTORING_SUMMARY.md](./docs/REFACTORING_SUMMARY.md) - 重构总结
4. 🟡 [ARCHITECTURE_DIAGRAM.md](./docs/ARCHITECTURE_DIAGRAM.md) - 架构图
5. 🟢 [REFACTORING_PLAN.md](./docs/REFACTORING_PLAN.md) - 详细方案

### 工具链
- **构建**: Maven
- **ORM**: mybatis-flex
- **缓存**: Spring Cache + Redis
- **加密**: Jasypt
- **文档**: Swagger/OpenAPI 3
- **测试**: JUnit 5 + Mockito

## ⚡ 快速命令

```powershell
# 编译
mvn clean compile -pl mortise-wechat -am

# 测试
mvn test -pl mortise-wechat

# 打包
mvn package -pl mortise-wechat -am -DskipTests

# 代码检查
mvn checkstyle:check -pl mortise-wechat

# 查看依赖树
mvn dependency:tree -pl mortise-wechat
```

## 🐛 常见问题

### Q1: TableDef 找不到？
**A**: 使用字符串查询代替
```java
// ❌ 错误
.where(WECHAT_ACCOUNT.DEL_FLAG.eq(0))

// ✅ 正确
.where("del_flag = 0")
```

### Q2: StringEncryptor 注入失败？
**A**: 使用 Optional 包装
```java
@Qualifier("jasyptStringEncryptor")
private final Optional<StringEncryptor> stringEncryptor;
```

### Q3: 缓存不生效？
**A**: 检查缓存配置
```java
@Cacheable(value = "wechat:config", key = "'mp:default'", unless = "#result == null")
```

### Q4: 事务不回滚？
**A**: 添加 rollbackFor
```java
@Transactional(rollbackFor = Exception.class)
```

## 📞 获取帮助

- 📖 查看文档: `docs/INDEX.md`
- 🔍 搜索问题: 检查 `IMPLEMENTATION_GUIDE.md`
- 💬 提问: 联系团队负责人

---

**版本**: v1.0.0  
**更新日期**: 2025-10-06  
**维护者**: ronger
