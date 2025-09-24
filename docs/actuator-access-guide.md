# Spring Boot Actuator 访问指南

## 快速开始 🚀

**1分钟快速访问Actuator端点：**

```powershell
# 步骤1：登录获取Token
$loginData = @{
    account = "your_account"
    password = "your_password"
} | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri "http://localhost:9999/mortise/api/v1/auth/login" -Method POST -ContentType "application/json" -Body $loginData
$token = $loginResponse.data.token
$headers = @{"Authorization" = "Bearer $token"}

# 步骤2：访问Actuator端点
Invoke-RestMethod -Uri "http://localhost:9999/mortise/actuator/health" -Method GET -Headers $headers
Invoke-RestMethod -Uri "http://localhost:9999/mortise/actuator/info" -Method GET -Headers $headers
Invoke-RestMethod -Uri "http://localhost:9999/mortise/actuator/metrics" -Method GET -Headers $headers
```

## 概述

对于纯API后端项目，Spring Boot Actuator提供了强大的监控和管理端点。根据当前配置，你可以通过HTTP请求访问各种监控信息。

## 当前配置

根据 `application-dev.yml` 配置：

```yaml
server:
  port: 9999  # 应用运行在9999端口
  servlet:
    context-path: /mortise  # 应用上下文路径

management:
  endpoints:
    web:
      exposure:
        include: "*"        # 暴露所有端点
        exclude: shutdown   # 排除关闭端点
      base-path: /actuator  # 基础路径
      cors:
        allowed-origins: "*"
        allowed-methods: GET,POST
  endpoint:
    health:
      show-details: always
      show-components: always
```

⚠️ **重要**: 应用启用了Spring Security，所有Actuator端点需要认证才能访问。

## 访问方式

### 1. 基础访问URL
应用启动后，Actuator端点的基础URL是：
```
http://localhost:9999/mortise/actuator
```

### 2. 认证要求
由于应用启用了安全认证，访问Actuator端点需要：
- **JWT Token认证** (推荐)
- **HTTP Basic认证** (如果配置了默认用户)
- **OAuth2认证**

### 3. 获取认证Token

#### 完整登录示例
```powershell
# 使用实际凭据登录
$loginData = @{
    account = "your_account"
    password = "your_password"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:9999/mortise/api/v1/auth/login" -Method POST -ContentType "application/json" -Body $loginData

# 检查登录结果
if ($loginResponse.code -eq 200) {
    Write-Host "登录成功!" -ForegroundColor Green
    $token = $loginResponse.data.token
    $headers = @{"Authorization" = "Bearer $token"}
    
    # 现在可以访问Actuator端点了
    Invoke-RestMethod -Uri "http://localhost:9999/mortise/actuator/health" -Method GET -Headers $headers
} else {
    Write-Host "登录失败: $($loginResponse.message)" -ForegroundColor Red
}
```

#### 使用curl (Linux/macOS)
```bash
# 登录获取Token  
TOKEN=$(curl -X POST http://localhost:9999/mortise/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"your_account","password":"your_password"}' \
  | jq -r '.data.token')

# 使用Token访问Actuator
curl -X GET http://localhost:9999/mortise/actuator/health \
  -H "Authorization: Bearer $TOKEN"
```

### 4. 主要端点列表 (需要认证)

#### 健康检查
```bash
# 整体健康状态 (需要认证)
GET http://localhost:9999/mortise/actuator/health
Authorization: Bearer <your_jwt_token>

# 响应示例
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "SELECT 1"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.x.x"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1000000000,
        "free": 500000000,
        "threshold": 10485760
      }
    }
  },
  "groups": {
    "readiness": {
      "status": "UP"
    },
    "liveness": {
      "status": "UP"
    }
  }
}
```

#### 应用信息
```bash
GET http://localhost:9999/mortise/actuator/info
Authorization: Bearer <your_jwt_token>

# 包含应用版本、环境、构建信息等
```

#### 指标监控
```bash
# 所有可用指标
GET http://localhost:9999/mortise/actuator/metrics
Authorization: Bearer <your_jwt_token>

# 特定指标 (例如JVM内存使用)
GET http://localhost:9999/mortise/actuator/metrics/jvm.memory.used
Authorization: Bearer <your_jwt_token>

# HTTP请求指标
GET http://localhost:9999/mortise/actuator/metrics/http.server.requests
Authorization: Bearer <your_jwt_token>

# 数据库连接池指标
GET http://localhost:9999/mortise/actuator/metrics/hikaricp.connections.active
Authorization: Bearer <your_jwt_token>
```

#### Prometheus指标
```bash
# Prometheus格式的指标 (用于监控系统集成)
GET http://localhost:9999/mortise/actuator/prometheus
Authorization: Bearer <your_jwt_token>
```

#### 环境信息
```bash
# 环境变量和配置属性
GET http://localhost:9999/mortise/actuator/env
Authorization: Bearer <your_jwt_token>

# 特定配置属性
GET http://localhost:9999/mortise/actuator/env/spring.datasource.url
Authorization: Bearer <your_jwt_token>
```

#### 配置属性
```bash
# 所有配置属性
GET http://localhost:9999/mortise/actuator/configprops
Authorization: Bearer <your_jwt_token>
```

#### 应用映射
```bash
# 所有HTTP映射
GET http://localhost:9999/mortise/actuator/mappings
Authorization: Bearer <your_jwt_token>
```

#### Bean信息
```bash
# 所有Spring Bean
GET http://localhost:9999/mortise/actuator/beans
Authorization: Bearer <your_jwt_token>
```

#### 线程转储
```bash
GET http://localhost:9999/mortise/actuator/threaddump
Authorization: Bearer <your_jwt_token>
```

#### 堆转储
```bash
GET http://localhost:9999/mortise/actuator/heapdump
Authorization: Bearer <your_jwt_token>
```

### 5. 使用工具访问

#### 使用curl (需要认证)
```bash
# 先获取Token
curl -X POST http://localhost:9999/mortise/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"your_account","password":"your_password"}'

# 使用Token访问Actuator
curl -X GET http://localhost:9999/mortise/actuator/health \
  -H "Authorization: Bearer <your_jwt_token>"

# 获取Prometheus指标
curl -X GET http://localhost:9999/mortise/actuator/prometheus \
  -H "Authorization: Bearer <your_jwt_token>"
```

#### 使用PowerShell (Windows)
```powershell
# 获取Token
$loginData = @{
    account = "your_account"
    password = "your_password"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:9999/mortise/api/v1/auth/login" -Method POST -ContentType "application/json" -Body $loginData
$token = $loginResponse.data.access_token

# 设置认证头
$headers = @{"Authorization" = "Bearer $token"}

# 健康检查
Invoke-RestMethod -Uri "http://localhost:9999/mortise/actuator/health" -Method GET -Headers $headers

# 获取指标
Invoke-RestMethod -Uri "http://localhost:9999/mortise/actuator/metrics" -Method GET -Headers $headers

# 格式化输出
Invoke-RestMethod -Uri "http://localhost:9999/mortise/actuator/health" -Method GET -Headers $headers | ConvertTo-Json -Depth 10
```

#### 使用Postman
1. **获取Token**:
   - 创建POST请求: `http://localhost:9999/mortise/api/v1/auth/login`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON): `{"account":"your_account","password":"your_password"}`
   - 发送请求，复制响应中的 `access_token`

2. **访问Actuator**:
   - 创建GET请求: `http://localhost:9999/mortise/actuator/health`
   - Headers: `Authorization: Bearer <your_jwt_token>`
   - 发送请求查看响应

### 6. 浏览器访问 (需要登录)

由于需要认证，直接在浏览器访问会返回401错误：
```
http://localhost:9999/mortise/actuator
http://localhost:9999/mortise/actuator/health
```

需要先通过Web界面登录或使用API工具获取Token。

## 常用监控场景 (需要认证)

所有监控端点都需要在请求头中包含有效的JWT Token：
`Authorization: Bearer <your_jwt_token>`

### 1. 应用健康检查
```bash
# 检查应用是否健康
GET http://localhost:9999/mortise/actuator/health

# 检查就绪状态 (用于负载均衡器)
GET http://localhost:9999/mortise/actuator/health/readiness

# 检查存活状态 (用于重启决策)
GET http://localhost:9999/mortise/actuator/health/liveness
```

### 2. 性能监控
```bash
# JVM内存使用情况
GET http://localhost:9999/mortise/actuator/metrics/jvm.memory.used
GET http://localhost:9999/mortise/actuator/metrics/jvm.memory.max

# GC情况
GET http://localhost:9999/mortise/actuator/metrics/jvm.gc.pause

# HTTP请求统计
GET http://localhost:9999/mortise/actuator/metrics/http.server.requests

# 数据库连接池
GET http://localhost:9999/mortise/actuator/metrics/hikaricp.connections
GET http://localhost:9999/mortise/actuator/metrics/hikaricp.connections.active
```

### 3. 系统监控
```bash
# CPU使用率
GET http://localhost:9999/mortise/actuator/metrics/system.cpu.usage
GET http://localhost:9999/mortise/actuator/metrics/process.cpu.usage

# 磁盘空间
GET http://localhost:9999/mortise/actuator/health/diskSpace

# 线程信息
GET http://localhost:9999/mortise/actuator/metrics/jvm.threads.live
```

## 启动应用

由于需要Java 21运行Spring Boot 3.x，请确保：

### 1. 设置正确的Java版本
```powershell
# 设置环境变量
$env:JAVA_HOME = "C:\Users\ronger\.jdks\temurin-21.0.8"
$env:PATH = "C:\Users\ronger\.jdks\temurin-21.0.8\bin;" + $env:PATH

# 验证Java版本
java -version
```

### 2. 启动应用的方式

#### 方式一：Maven启动 (推荐开发环境)
```bash
mvn spring-boot:run
```

#### 方式二：Jar包启动 (推荐生产环境)
```bash
# 先编译
mvn clean package -DskipTests

# 然后运行
java -jar target/mortise-0.0.1.war
```

#### 方式三：IDE启动
直接在IDE中运行 `MortiseApplication.java`

## 监控集成

### 1. Prometheus集成
你的应用已经配置了Prometheus支持：
```bash
GET http://localhost:9999/mortise/actuator/prometheus
Authorization: Bearer <your_jwt_token>
```

这个端点返回Prometheus格式的指标，可以配置Prometheus服务器抓取：
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'mortise-app'
    static_configs:
      - targets: ['localhost:9999']
    metrics_path: '/mortise/actuator/prometheus'
    scrape_interval: 15s
    # 添加认证配置
    bearer_token: 'your_jwt_token'
    # 或者使用bearer_token_file
    bearer_token_file: '/path/to/token/file'
```

### 2. Grafana仪表板
可以使用以下指标创建Grafana仪表板：
- `jvm_memory_used_bytes` - JVM内存使用
- `jvm_gc_pause_seconds` - GC暂停时间
- `http_server_requests_seconds` - HTTP请求耗时
- `hikaricp_connections_active` - 数据库连接数
- `system_cpu_usage` - CPU使用率

### 3. 健康检查集成
可以配置负载均衡器或监控系统使用健康检查端点（需要配置认证）：
- **就绪检查**: `GET /mortise/actuator/health/readiness`
- **存活检查**: `GET /mortise/actuator/health/liveness`

### 4. 免认证访问配置

如果需要允许监控系统免认证访问Actuator端点，可以在安全配置中添加：

```java
// WebSecurityConfig.java
.authorizeHttpRequests((authorize) -> {
    // 现有的公开端点...
    authorize.requestMatchers("/api/v1/auth/**").permitAll();
    
    // 添加Actuator端点的免认证访问
    authorize.requestMatchers("/actuator/health").permitAll();
    authorize.requestMatchers("/actuator/health/**").permitAll();
    authorize.requestMatchers("/actuator/prometheus").permitAll();
    authorize.requestMatchers("/actuator/metrics").permitAll();
    
    authorize.anyRequest().authenticated();
})
```

**注意**: 生产环境中应谨慎开放Actuator端点，建议只开放必要的健康检查端点。

## 安全考虑

### 生产环境建议
1. **限制端点暴露**：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

2. **启用安全认证**：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: when-authorized
```

3. **网络隔离**：
```yaml
management:
  server:
    port: 8081  # 使用不同端口
    address: 127.0.0.1  # 仅本地访问
```

## 故障排查

### 常见问题
1. **404 Not Found**：确认应用已启动且端口正确
2. **403 Forbidden**：检查安全配置
3. **连接拒绝**：确认防火墙和网络配置

### 检查应用状态
```bash
# 检查端口是否监听
netstat -an | findstr :9999

# 检查进程
tasklist | findstr java
```

通过以上方式，你就可以全面监控和管理你的Spring Boot应用了！
