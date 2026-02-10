# AuthenticationManager Bean 配置修复

## 问题描述

应用启动时出现以下错误：

```
Description:
A component required a bean of type 'org.springframework.security.authentication.AuthenticationManager' 
that could not be found.

Action:
Consider defining a bean of type 'org.springframework.security.authentication.AuthenticationManager' 
in your configuration.
```

## 根本原因

在 Spring Security 6.x 中，`AuthenticationManager` 不再自动暴露为 Bean。需要在配置类中显式地创建并暴露该 Bean。

`AuthServiceImpl` 类中注入了 `AuthenticationManager`：

```java
@Resource
private AuthenticationManager authenticationManager;
```

但 `WebSecurityConfig` 中没有提供对应的 Bean 定义。

## 解决方案

在 `WebSecurityConfig` 中添加 `AuthenticationManager` Bean 的配置。

### 修改的文件

**文件**: `mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java`

### 添加的导入

```java
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
```

### 添加的 Bean 方法

```java
/**
 * 认证管理器
 * 
 * @param configuration 认证配置
 * @return AuthenticationManager
 * @throws Exception 异常
 */
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
}
```

## 技术说明

### Spring Security 6.x 推荐方式

Spring Security 6.x 推荐通过 `AuthenticationConfiguration` 来获取 `AuthenticationManager`，而不是手动创建 `DaoAuthenticationProvider`。

这种方式的优势：
1. **自动配置**: Spring Security 会自动配置好所有必要的认证提供者
2. **避免废弃 API**: 不使用已废弃的构造函数和方法
3. **简化配置**: 代码更简洁，无需手动设置 `UserDetailsService` 和 `PasswordEncoder`
4. **更好的集成**: 与 Spring Security 的其他组件更好地集成

### 工作原理

1. Spring Security 会自动检测到 `UserDetailsService` Bean
2. 结合 `PasswordEncoder` Bean，自动创建 `DaoAuthenticationProvider`
3. 通过 `AuthenticationConfiguration.getAuthenticationManager()` 获取配置好的 `AuthenticationManager`

## 参考

- 原项目配置: [WebSecurityConfig.java](https://github.com/rymcu/mortise/blob/master/src/main/java/com/rymcu/mortise/config/WebSecurityConfig.java)
- Spring Security 官方文档: [Authentication Architecture](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html)

## 验证

编译项目验证修复：

```bash
mvn clean compile
```

结果：**BUILD SUCCESS** ✅

## 相关文件

- `mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java` - 安全配置
- `mortise-system/src/main/java/com/rymcu/mortise/system/service/impl/AuthServiceImpl.java` - 认证服务实现

## 日期

2025-10-01
