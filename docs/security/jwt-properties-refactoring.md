# JWT 配置重构文档

## 📅 重构日期
2025-10-01

## 🎯 重构目标
删除冗余的 `JwtProperties` 配置类，将 JWT 相关配置统一到 `JwtTokenUtil` 中，实现配置化管理。

---

## 📋 重构内容

### 1. **删除的文件**
- ❌ `com.rymcu.mortise.auth.config.JwtProperties`

**删除原因：**
- 零依赖：没有任何代码在使用该类
- 功能冗余：`JwtTokenUtil` 已通过 `@Value` 实现配置注入
- 简化架构：减少不必要的配置类

### 2. **增强的类**

#### `JwtTokenUtil`
**新增配置项：**
```java
@Value("${jwt.header:Authorization}")
private String tokenHeader;

@Value("${jwt.token-prefix:Bearer }")
private String tokenPrefix;
```

**新增方法：**
```java
public String getTokenHeader()    // 获取 Token 请求头名称
public String getTokenPrefix()    // 获取 Token 前缀
```

### 3. **修改的类**

#### `JwtAuthenticationFilter`
**移除硬编码：**
```java
// ❌ 删除
private static final String TOKEN_HEADER = "Authorization";
private static final String TOKEN_PREFIX = "Bearer ";
```

**改为动态获取：**
```java
// ✅ 使用配置化的值
String bearerToken = request.getHeader(jwtTokenUtil.getTokenHeader());
if (bearerToken != null && bearerToken.startsWith(jwtTokenUtil.getTokenPrefix())) {
    return bearerToken.substring(jwtTokenUtil.getTokenPrefix().length());
}
```

### 4. **更新的配置文件**

#### `application-dev.yml`
```yaml
# JWT 配置
jwt:
  secret: ${JWT_SECRET:w0gADMTuedSB1PS4f59vwJaOV7n2fYcAAAAhALwcBo1hcJ8ELdByH/qcmQ1fWKK7}
  expiration: 3600000        # 1小时，单位毫秒
  header: Authorization      # Token 请求头名称
  token-prefix: "Bearer "    # Token 前缀（注意有空格）
```

---

## ✨ 重构优势

### 1️⃣ **简化架构**
- 删除了冗余的 `JwtProperties` 类
- 配置逻辑集中在 `JwtTokenUtil` 中
- 减少了代码维护成本

### 2️⃣ **配置化管理**
- `header` 和 `tokenPrefix` 从硬编码改为可配置
- 支持通过配置文件动态修改
- 提供合理的默认值

### 3️⃣ **保持一致性**
- 所有 JWT 配置统一通过 `@Value` 注入
- 配置方式统一，避免混淆
- 符合 Spring Boot 最佳实践

### 4️⃣ **向后兼容**
- 所有配置项都有默认值
- 不需要修改现有配置即可正常运行
- 支持渐进式配置迁移

---

## 📝 配置说明

### JWT 配置项

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `jwt.secret` | String | `mortise-secret-key-...` | JWT 签名密钥 |
| `jwt.expiration` | Long | `1800000` | Token 过期时间（毫秒），默认 30 分钟 |
| `jwt.header` | String | `Authorization` | Token 请求头名称 |
| `jwt.token-prefix` | String | `Bearer ` | Token 前缀（注意有空格） |

### 使用示例

#### 生产环境配置
```yaml
jwt:
  secret: ${JWT_SECRET}                    # 从环境变量读取
  expiration: 7200000                      # 2小时
  header: X-Auth-Token                     # 自定义请求头
  token-prefix: "Token "                   # 自定义前缀
```

#### 开发环境配置
```yaml
jwt:
  secret: dev-secret-key-for-testing-only
  expiration: 86400000                     # 24小时（方便调试）
  # header 和 token-prefix 使用默认值
```

---

## 🔧 使用指南

### 在代码中获取配置

```java
@Resource
private JwtTokenUtil jwtTokenUtil;

// 获取 Token 请求头名称
String header = jwtTokenUtil.getTokenHeader();

// 获取 Token 前缀
String prefix = jwtTokenUtil.getTokenPrefix();
```

### 前端对接示例

```javascript
// 默认配置
axios.defaults.headers.common['Authorization'] = 'Bearer ' + token;

// 自定义配置（假设修改了 header 和 token-prefix）
axios.defaults.headers.common['X-Auth-Token'] = 'Token ' + token;
```

---

## ✅ 验证清单

- [x] 删除 `JwtProperties.java`
- [x] `JwtTokenUtil` 添加 `tokenHeader` 和 `tokenPrefix` 配置
- [x] `JwtAuthenticationFilter` 移除硬编码
- [x] 更新 `application-dev.yml` 配置
- [x] 编译通过（`mortise-auth` 模块）
- [x] 编译通过（`mortise-app` 模块）
- [x] 所有配置项都有合理的默认值
- [x] 配置注释清晰明确

---

## 🎉 重构完成

所有 JWT 配置现已统一到 `JwtTokenUtil` 中，支持通过配置文件灵活调整，代码更加简洁、可维护。

**作者：** GitHub Copilot  
**日期：** 2025-10-01
