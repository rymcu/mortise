# UserOnlineStatusExpirationHandler 迁移说明

## 变更概述

将 `UserOnlineStatusExpirationHandler` 从 `mortise-cache` 模块迁移到 `mortise-system` 模块，以符合业务逻辑归属原则。

## 具体变更

### 1. 新增用户在线状态缓存常量

**文件**：`mortise-system/src/main/java/com/rymcu/mortise/system/constant/SystemCacheConstant.java`

```java
/**
 * 用户在线状态缓存
 */
public static final String USER_ONLINE_STATUS_CACHE = "userOnlineStatus";
public static final long USER_ONLINE_STATUS_EXPIRE_MINUTES = 30;
```

### 2. 创建业务处理器

**文件**：`mortise-system/src/main/java/com/rymcu/mortise/system/handler/UserOnlineStatusExpirationHandler.java`

- 实现了 `CacheExpirationHandler` SPI 接口
- 使用 `SystemCacheConstant.USER_ONLINE_STATUS_CACHE` 常量
- 调用 `UserService.updateLastOnlineTimeByAccount()` 方法
- 完整的异常处理和日志记录

### 3. 删除示例代码

**删除**：`mortise-cache/src/main/java/com/rymcu/mortise/cache/handler/` 目录
- 移除了在基础设施模块中不应该存在的业务逻辑示例

## 架构优势

### 1. 职责清晰
- ✅ 缓存基础设施 (`mortise-cache`)：提供 SPI 接口和监听框架
- ✅ 业务逻辑 (`mortise-system`)：实现具体的业务处理逻辑

### 2. 依赖合理
- ✅ 业务模块依赖基础设施模块 ✓
- ✅ 基础设施模块不依赖业务模块 ✓

### 3. 可维护性
- ✅ 业务逻辑修改只影响对应的业务模块
- ✅ 基础设施保持稳定，提供标准化接口

## 验证结果

- ✅ 编译通过：`mvn clean compile -pl mortise-cache,mortise-system -am -q`
- ✅ 依赖正确：`mortise-system` 已依赖 `mortise-cache`
- ✅ 服务可用：`UserService.updateLastOnlineTimeByAccount()` 方法存在

## 使用方式

其他业务模块可以参考 `UserOnlineStatusExpirationHandler` 的实现方式：

1. 在业务模块中实现 `CacheExpirationHandler` 接口
2. 使用 `@Component` 注解注册为 Spring Bean
3. 实现 `supports()` 方法判断是否处理特定缓存键
4. 在 `handle()` 方法中实现业务逻辑

这样的设计完全符合 SPI 模式和模块化架构原则。