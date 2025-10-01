# Mortise 多模块重构计划

## 📋 重构目标

将当前单体应用重构为基于 Maven Module 的多模块单体应用，采用清晰的分层架构和 SPI 扩展机制。

## 🏗️ 模块结构

```
mortise/
├── pom.xml                        # 父 POM
├── mortise-common/                # 公共基础模块
├── mortise-core/                  # 核心领域模块  
├── mortise-log/                   # 日志模块
├── mortise-cache/                 # 缓存模块
├── mortise-notification/          # 通知模块
├── mortise-auth/                  # 鉴权模块
├── mortise-web/                   # Web 通用模块
├── mortise-system/                # 系统业务模块
├── mortise-monitor/               # 监控模块
└── mortise-app/                   # 主应用模块
```

## 📦 模块详细设计

### 1. mortise-common (公共基础模块)

**职责**：提供通用工具类、常量、枚举、基础异常定义

**包含内容**：
- `util/` - 工具类
  - `Utils.java`
  - `FileUtils.java`
  - `Html2TextUtil.java`
  - `BeanCopierUtil.java`
  - `SpringContextHolder.java`
  - `ContextHolderUtils.java`
  
- `constant/` - 基础常量
  - `ProjectConstant.java` (基础部分)
  
- `enumerate/` - 枚举
  - `Status.java`
  - `DelFlag.java`
  
- `exception/` - 基础异常
  - `BusinessException.java`
  - `ServiceException.java`
  - `ContentNotExistException.java`

**依赖**：无

---

### 2. mortise-core (核心领域模块)

**职责**：统一响应结构、领域事件、核心业务接口

**包含内容**：
- `result/` - 响应结构
  - `GlobalResult.java`
  - `ResultCode.java`
  
- `model/` - 基础模型
  - `BaseSearch.java`
  - `BaseOption.java`
  
- `event/` - 领域事件接口定义

**依赖**：
```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-common</artifactId>
</dependency>
```

---

### 3. mortise-log (日志模块)

**职责**：操作日志、API日志的注解、切面和存储

**包含内容**：
- `annotation/` - 日志注解
  - `@OperationLog`
  - `@ApiLog`
  
- `aspect/` - 日志切面
  - `OperationLogAspect.java`
  
- `entity/` - 日志实体
  - `OperationLogEntity.java`
  
- `spi/` - SPI 扩展点
  - `LogStorage.java` (接口)
  
- `impl/` - 默认实现
  - `DatabaseLogStorage.java`

**依赖**：
```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-common</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

---

### 4. mortise-cache (缓存模块)

**职责**：提供通用缓存接口和 Redis 实现

**包含内容**：
- `service/` - 缓存服务接口
  - `CacheService.java`
  
- `impl/` - 实现
  - `RedisCacheServiceImpl.java`
  
- `config/` - 基础配置
  - `BaseCacheConfig.java` (含 SPI 机制)
  
- `spi/` - SPI 扩展点
  - `CacheConfigurer.java`
  
- `constant/` - 基础常量
  - `BaseCacheConstant.java`

**依赖**：
```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-common</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

---

### 5. mortise-notification (通知模块)

**职责**：邮件、短信等通知功能的接口和实现

**包含内容**：
- `service/` - 通知服务接口
  - `NotificationService.java`
  
- `model/` - 请求模型
  - `EmailRequest.java`
  - `SmsRequest.java`
  
- `spi/` - SPI 扩展点
  - `NotificationSender.java`
  
- `impl/` - 具体实现
  - `EmailNotificationServiceImpl.java`
  - (原 `JavaMailService` 迁移到这里)

**依赖**：
```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-common</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-cache</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

---

### 6. mortise-auth (鉴权模块)

**职责**：JWT认证、OAuth2、权限注解和验证

**包含内容**：
- `annotation/` - 权限注解
  - `@PublicApi.java`
  - `@RequirePermission.java`
  - `@RequireRole.java`
  
- `filter/` - 过滤器
  - `JwtAuthenticationFilter.java` (原 auth/ 目录)
  - `JwtAuthenticationEntryPoint.java`
  - `RewriteAccessDenyFilter.java`
  
- `handler/` - 处理器
  - `OAuth2LoginSuccessHandler.java`
  - `OAuth2LogoutSuccessHandler.java`
  
- `util/` - 工具类
  - `JwtUtils.java`
  - `JwtConstants.java`
  
- `manager/` - Token 管理
  - `TokenManager.java`
  - `TokenModel.java`
  - `CacheTokenManager.java`
  
- `repository/` - 仓储
  - `CacheAuthorizationRequestRepository.java`
  
- `config/` - 安全配置
  - `WebSecurityConfig.java`
  - `JwtProperties.java`
  
- `spi/` - SPI 扩展点
  - `SecurityConfigurer.java`
  
- `scanner/` - 注解扫描
  - `PublicApiScanner.java`
  
- `cache/` - 认证缓存封装
  - `config/`
    - `AuthCacheConfigurer.java`
    - `OAuth2CacheConfigurer.java`
  - `constant/`
    - `AuthCacheConstant.java`
  - `AuthCacheService.java`
  - `impl/`
    - `AuthCacheServiceImpl.java`

**依赖**：
```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-common</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-core</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-cache</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-log</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
```

---

### 7. mortise-web (Web 通用模块)

**职责**：Web层通用配置、异常处理、限流等

**包含内容**：
- `config/` - Web 配置
  - `WebMvcConfig.java`
  - `JacksonConfig.java`
  - `OpenApiConfig.java`
  
- `exception/` - 全局异常处理
  - `BaseExceptionHandler.java`
  
- `interceptor/` - 拦截器
  
- `limiter/` - 限流
  - `Resilience4jRateLimitConfig.java`
  - `Resilience4jRateLimiter.java`
  
- `aspect/` - 切面
  - `Resilience4jRateLimitAspect.java`
  
- `spi/` - SPI 扩展点
  - `ExceptionHandler.java`
  
- `cache/` - Web 缓存配置
  - `config/`
    - `WebCacheConfigurer.java`

**依赖**：
```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-core</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-log</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-common</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

---

### 8. mortise-system (系统业务模块)

**职责**：用户、角色、权限、菜单、字典等系统功能

**包含内容**：
- `entity/` - 实体
  - `User.java`
  - `Role.java`
  - `Menu.java`
  - `Dict.java`
  - `DictType.java`
  - `UserRole.java`
  - `RoleMenu.java`
  
- `mapper/` - MyBatis Mapper
  - `UserMapper.java`
  - `RoleMapper.java`
  - `MenuMapper.java`
  - `DictMapper.java`
  - `DictTypeMapper.java`
  
- `model/` - DTO/VO
  - `UserInfo.java`
  - `UserSearch.java`
  - `RoleSearch.java`
  - 等等...
  
- `service/` - 业务服务
  - `UserService.java`
  - `RoleService.java`
  - `MenuService.java`
  - `DictService.java`
  - `DictTypeService.java`
  - `PermissionService.java`
  
- `service/impl/` - 服务实现
  - `UserServiceImpl.java`
  - 等等...
  
- `controller/` - REST API
  - `UserController.java`
  - `RoleController.java`
  - `MenuController.java`
  - `DictController.java`
  - `DictTypeController.java`
  
- `handler/` - 事件处理器
  - `RegisterHandler.java`
  - `AccountHandler.java`
  - `ResetPasswordHandler.java`
  - `UserLoginEventHandler.java`
  - `OidcUserEventHandler.java`
  
- `handler/event/` - 事件定义
  - `RegisterEvent.java`
  - `AccountEvent.java`
  - `ResetPasswordEvent.java`
  - `UserLoginEvent.java`
  - `OidcUserEvent.java`
  
- `serializer/` - 序列化器
  - `DictSerializer.java`
  
- `cache/` - 系统缓存封装 ⭐
  - `SystemCacheService.java`
  - `impl/`
    - `SystemCacheServiceImpl.java`
    - `UserCacheServiceImpl.java`
  - `config/`
    - `SystemCacheConfigurer.java`
  - `constant/`
    - `SystemCacheConstant.java`
  
- `notification/` - 系统通知封装 ⭐
  - `SystemNotificationService.java`
  - `impl/`
    - `SystemNotificationServiceImpl.java`
  
- `config/` - 系统模块配置
  - `SystemSecurityConfigurer.java` (实现 SecurityConfigurer)

**依赖**：
```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-core</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-auth</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-cache</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-log</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-notification</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-common</artifactId>
</dependency>
<dependency>
    <groupId>com.mybatis-flex</groupId>
    <artifactId>mybatis-flex-spring-boot3-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

---

### 9. mortise-monitor (监控模块)

**职责**：健康检查、性能监控、指标采集

**包含内容**：
- `health/` - 健康检查
  - `RedisHealthIndicator.java`
  - `Resilience4jRateLimiterHealthIndicator.java`
  
- `config/` - 监控配置
  - `DatabasePerformanceConfig.java`
  - `ApplicationPerformanceConfig.java`
  - `ApplicationStartupConfig.java`
  
- `spi/` - SPI 扩展点
  - `HealthCheck.java`
  - `MetricsCollector.java`

**依赖**：
```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-common</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-cache</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

### 10. mortise-app (主应用模块)

**职责**：应用启动类、应用级配置、业务 Controller

**包含内容**：
- `MortiseApplication.java` - 启动类
- `ServletInitializer.java` - Servlet 初始化
- `controller/` - 业务 Controller
  - `AuthController.java`
  - `ProfileController.java`
  - `NotificationController.java`
- `service/` - 应用服务
  - `AuthService.java`
  - `impl/`
    - `AuthServiceImpl.java`
    - `UserDetailsServiceImpl.java`
- `util/` - 应用工具类
  - `UserUtils.java`
- `config/` - 应用配置
  - `JasyptEncryptionConfig.java`
  - `RedisProperties.java`
  - `RedisListenerConfig.java`
  - `RedisKeyExpirationListener.java`
  - `TaskExecutorConfig.java`
- `resources/` - 资源文件
  - `application.yml`
  - `application-dev.yml`
  - `banner.txt`
  - `templates/` - 邮件模板

**依赖**：
```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-system</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-monitor</artifactId>
</dependency>
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

---

## 🔄 依赖关系图

```
mortise-app
  ├─> mortise-system
  ├─> mortise-monitor
  └─> mortise-web

mortise-system
  ├─> mortise-auth
  ├─> mortise-cache
  ├─> mortise-log
  ├─> mortise-notification
  ├─> mortise-core
  └─> mortise-common

mortise-web
  ├─> mortise-core
  ├─> mortise-log
  └─> mortise-common

mortise-auth
  ├─> mortise-cache
  ├─> mortise-log
  ├─> mortise-core
  └─> mortise-common

mortise-notification
  ├─> mortise-cache
  └─> mortise-common

mortise-monitor
  ├─> mortise-cache
  └─> mortise-common

mortise-cache
  └─> mortise-common

mortise-log
  └─> mortise-common

mortise-core
  └─> mortise-common
```

---

## 🚀 重构步骤

### Phase 1: 基础模块创建 (无依赖模块)
1. ✅ 创建父 POM
2. 创建 mortise-common
3. 创建 mortise-core

### Phase 2: 基础设施模块 (依赖 common/core)
4. 创建 mortise-log
5. 创建 mortise-cache
6. 创建 mortise-notification

### Phase 3: 应用层模块 (依赖基础设施)
7. 创建 mortise-auth
8. 创建 mortise-web
9. 创建 mortise-monitor

### Phase 4: 业务模块
10. 创建 mortise-system

### Phase 5: 主应用
11. 创建 mortise-app

### Phase 6: 清理
12. 备份原 src 目录
13. 删除原 src 目录
14. 重命名 pom-new.xml 为 pom.xml

---

## ⚠️ 注意事项

1. **编码格式**：所有文件统一使用 UTF-8 编码
2. **包路径调整**：
   - 基础模块：`com.rymcu.mortise.xxx`
   - 保持原有包结构风格
3. **SPI 机制**：
   - 所有扩展点都使用 `@Component` 自动注册
   - 通过 `List<XXXConfigurer>` 自动注入
4. **缓存配置拆分**：
   - 基础配置在 `mortise-cache`
   - 业务配置在各自模块的 `cache/config/XXXCacheConfigurer.java`
5. **通知封装**：
   - 基础能力在 `mortise-notification`
   - 业务封装在各自模块的 `notification/XXXNotificationService.java`

---

## ✅ 验证清单

- [ ] 所有模块编译通过
- [ ] 依赖关系正确无循环
- [ ] SPI 扩展机制工作正常
- [ ] 缓存配置正确加载
- [ ] 安全配置扩展正常
- [ ] 应用可以正常启动
- [ ] 所有 REST API 正常访问
- [ ] 数据库连接正常
- [ ] Redis 连接正常
- [ ] OAuth2 登录正常
- [ ] JWT 认证正常

---

**准备好开始重构了吗？请确认！** 🚀
