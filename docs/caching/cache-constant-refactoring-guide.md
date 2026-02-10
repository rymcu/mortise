# 缓存常量拆分重构指南

## 📋 重构概述

将 `mortise-cache` 模块中的 `CacheConstant` 按业务职责拆分到各模块：

### 拆分后的结构

```
mortise-cache/CacheConstant.java
├── CACHE_NAME_PREFIX (保留)
├── DEFAULT_EXPIRE_MINUTES (保留)
├── DEFAULT_EXPIRE_HOURS (新增)
├── DEFAULT_EXPIRE_DAYS (新增)
├── HOT_DATA_CACHE (保留)
├── STATISTICS_CACHE (保留)
└── TEMP_DATA_CACHE (保留)

mortise-auth/AuthCacheConstant.java (新建)
├── JWT_TOKEN_CACHE
├── AUTH_TOKEN_CACHE
├── AUTH_REFRESH_TOKEN_CACHE
├── OAUTH2_AUTHORIZATION_REQUEST_CACHE
├── OAUTH2_AUTHORIZATION_CODE_CACHE
├── OAUTH2_ACCESS_TOKEN_CACHE
├── USER_SESSION_CACHE
├── USER_ONLINE_STATUS_CACHE
├── ACCOUNT_SEQUENCE_CACHE
├── LOGIN_VERIFICATION_CODE_CACHE
├── REGISTER_VERIFICATION_CODE_CACHE
├── VERIFICATION_CODE_CACHE
├── PASSWORD_RESET_CACHE
├── PASSWORD_RESET_TOKEN_CACHE
├── LOGIN_FAIL_COUNT_CACHE
└── ACCOUNT_LOCK_CACHE

mortise-system/SystemCacheConstant.java (新建)
├── USER_INFO_CACHE
├── USER_DETAIL_CACHE
├── USER_PERMISSIONS_CACHE
├── USER_ROLES_CACHE
├── ROLE_INFO_CACHE
├── ROLE_PERMISSION_CACHE
├── ROLE_MENU_CACHE
├── PERMISSION_INFO_CACHE
├── MENU_DATA_CACHE
├── MENU_TREE_CACHE
├── USER_MENU_CACHE
├── DICT_DATA_CACHE
├── DICT_TYPE_CACHE
├── DICT_ITEMS_CACHE
├── SYSTEM_CONFIG_CACHE
├── SYSTEM_PARAM_CACHE
├── SYSTEM_SETTING_CACHE
├── DEPT_INFO_CACHE
├── DEPT_TREE_CACHE
├── ORG_STRUCTURE_CACHE
├── OPERATION_LOG_TEMP_CACHE
└── LOGIN_LOG_TEMP_CACHE

mortise-core/CoreCacheConstant.java (新建)
├── DOMAIN_ENTITY_CACHE
├── AGGREGATE_ROOT_CACHE
├── BUSINESS_RULE_CACHE
├── VALIDATION_RULE_CACHE
├── ENUM_VALUES_CACHE
├── CONSTANT_MAPPING_CACHE
├── DOMAIN_EVENT_TEMP_CACHE
└── EVENT_PROCESS_STATUS_CACHE
```

## 🔧 需要修改的文件

### 1. mortise-auth 模块

#### OAuth2CacheConfigurer.java
```java
// 修改前
import com.rymcu.mortise.cache.constant.CacheConstant;
CacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE
CacheConstant.OAUTH2_AUTHORIZATION_REQUEST_EXPIRE_MINUTES

// 修改后
import com.rymcu.mortise.auth.constant.AuthCacheConstant;
AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_CACHE
AuthCacheConstant.OAUTH2_AUTHORIZATION_REQUEST_EXPIRE_MINUTES
```

### 2. mortise-system 模块

#### SystemCacheConfigurer.java
```java
// 修改前
import com.rymcu.mortise.cache.constant.CacheConstant;
CacheConstant.USER_INFO_CACHE
CacheConstant.USER_SESSION_CACHE
CacheConstant.USER_PERMISSIONS_CACHE
CacheConstant.ROLE_PERMISSION_CACHE
CacheConstant.MENU_DATA_CACHE
CacheConstant.DICT_DATA_CACHE
CacheConstant.SYSTEM_CONFIG_CACHE
CacheConstant.AUTH_TOKEN_CACHE
CacheConstant.AUTH_REFRESH_TOKEN_CACHE
CacheConstant.VERIFICATION_CODE_CACHE
CacheConstant.PASSWORD_RESET_CACHE
CacheConstant.ACCOUNT_SEQUENCE_CACHE
CacheConstant.JWT_TOKEN_CACHE
CacheConstant.USER_ONLINE_STATUS_CACHE

// 修改后
import com.rymcu.mortise.system.constant.SystemCacheConstant;
import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.cache.constant.CacheConstant;

SystemCacheConstant.USER_INFO_CACHE
AuthCacheConstant.USER_SESSION_CACHE (移至 auth)
SystemCacheConstant.USER_PERMISSIONS_CACHE
SystemCacheConstant.ROLE_PERMISSION_CACHE
SystemCacheConstant.MENU_DATA_CACHE
SystemCacheConstant.DICT_DATA_CACHE
SystemCacheConstant.SYSTEM_CONFIG_CACHE
AuthCacheConstant.AUTH_TOKEN_CACHE
AuthCacheConstant.AUTH_REFRESH_TOKEN_CACHE
AuthCacheConstant.VERIFICATION_CODE_CACHE
AuthCacheConstant.PASSWORD_RESET_CACHE
AuthCacheConstant.ACCOUNT_SEQUENCE_CACHE
AuthCacheConstant.JWT_TOKEN_CACHE
AuthCacheConstant.USER_ONLINE_STATUS_CACHE
CacheConstant.HOT_DATA_CACHE (保留)
CacheConstant.STATISTICS_CACHE (保留)
CacheConstant.TEMP_DATA_CACHE (保留)
```

#### SystemCacheServiceImpl.java
```java
// 需要修改所有 CacheConstant.XXX 的引用
// 用户相关 → SystemCacheConstant
// 字典、菜单、配置 → SystemCacheConstant
```

## 📝 修改原则

1. **认证相关** → `AuthCacheConstant`
   - JWT、OAuth2、Session、Token
   - 验证码、密码重置
   - 登录限制

2. **系统业务** → `SystemCacheConstant`
   - 用户、角色、权限
   - 菜单、字典、配置
   - 部门组织

3. **通用缓存** → `CacheConstant`
   - 热点数据
   - 统计数据
   - 临时数据
   - 默认过期时间

4. **领域模型** → `CoreCacheConstant`
   - 领域对象
   - 业务规则
   - 事件处理

## ✅ 验证步骤

1. 编译所有模块：
   ```bash
   mvn clean compile
   ```

2. 检查引用：
   ```bash
   grep -rn "CacheConstant\." --include="*.java"
   ```

3. 启动应用验证缓存配置是否正常加载

## 📚 相关文档

- 模块架构说明: `docs/module-dependency-and-spi-architecture.md`
- 缓存配置指南: `docs/cache-unification-complete-report.md`
