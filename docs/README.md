# Mortise 项目文档

这个目录包含了 Mortise 项目的完整技术文档、最佳实践指南和自动化维护脚本。

## 🎯 文档概览

Mortise 是一个基于 **Spring Boot 3.5.6** 构建的现代化后台管理脚手架，集成了企业级的认证授权、监控、缓存、限流等功能。本文档库提供了从开发到运维的全方位技术指导。

### 📋 技术栈一览
- **Spring Boot 3.5.6** + **Spring Security 6** + **JWT**
- **MyBatis-Flex 1.11.0** + **PostgreSQL 17** + **Redis**
- **Spring Boot Actuator** + **Prometheus** + **Grafana**
- **Resilience4j** + **HikariCP** + **Docker Compose**

## 📂 文档结构

### 📊 监控与运维
- [`actuator-access-guide.md`](actuator-access-guide.md) - Spring Boot Actuator 访问指南
  - 如何访问和使用 Actuator 端点
  - 认证配置和安全考虑
  - 监控集成 (Prometheus, Grafana)
  - 故障排查指南

- [`database-monitoring-fixes.md`](database-monitoring-fixes.md) - 数据库监控修复
  - HikariCP 连接池监控优化
  - 空指针异常修复
  - 监控指标完善

- [`database-performance-optimization.md`](database-performance-optimization.md) - 数据库性能优化
  - 连接池配置优化
  - PostgreSQL 特定配置
  - 性能监控设置

### 🔧 配置优化
- [`spring-boot-autoconfiguration-optimization.md`](spring-boot-autoconfiguration-optimization.md) - Spring Boot 自动配置优化
  - 自动配置冲突解决方案
  - Bean 重复定义问题分析
  - 最佳实践建议

- [`configuration-fixes.md`](configuration-fixes.md) - 配置修复记录
  - Spring Boot 属性配置修复
  - 废弃属性更新
  - 配置验证问题解决

- [`mybatis-flex-config-fixes.md`](mybatis-flex-config-fixes.md) - MyBatis-Flex 配置修复
  - 配置属性修正
  - 映射器路径配置
  - 性能优化设置

- [`circular-dependency-fixes.md`](circular-dependency-fixes.md) - 循环依赖修复
  - Bean 循环依赖解决方案
  - @Lazy 注解使用
  - 依赖注入优化

- [`scheduled-method-fixes.md`](scheduled-method-fixes.md) - 定时方法修复
  - @Scheduled 方法参数问题
  - 定时任务配置优化
  - 依赖注入修复

- [`health-check-config-fixes.md`](health-check-config-fixes.md) - 健康检查配置修复
  - 健康检查端点配置
  - 自定义健康指示器
  - 监控组配置

- [`final-config-fixes.md`](final-config-fixes.md) - 最终配置修复
  - 综合配置问题解决
  - 生产环境优化
  - 配置最佳实践

### 🛡️ 限流与安全
- [`rate-limiting.md`](rate-limiting.md) - Resilience4j 限流方案 ⭐
  - 限流策略实现与配置
  - 注解使用指南与最佳实践
  - 性能配置优化技巧
  - 实际应用场景案例

- [`rate-limit-comparison.md`](rate-limit-comparison.md) - 限流方案技术对比
  - Resilience4j vs Spring Cloud Gateway vs Nginx
  - 性能测试结果与分析
  - 技术选型决策指南
  - 适用场景推荐

### 🏗️ 架构优化
- [`project-architecture-optimization.md`](project-architecture-optimization.md) - 项目架构优化 ⭐
  - 整体架构设计理念
  - 模块化改进策略
  - 代码结构优化实践
  - 可扩展性设计原则

### 🧪 缓存优化
- [`cache-optimization-guide.md`](cache-optimization-guide.md) - 缓存优化指南
  - Redis 缓存策略设计
  - 缓存穿透/击穿/雪崩防护
  - 性能优化技巧

- [`cache-unification-complete-report.md`](cache-unification-complete-report.md) - 缓存统一化报告
  - 缓存架构统一方案
  - 实施过程与效果分析

- [`dict-cache-implementation.md`](dict-cache-implementation.md) - 字典缓存实现
  - 系统字典缓存设计
  - 实现细节与使用指南

### 🔄 事件与监听
- [`event-listener-timing-analysis.md`](event-listener-timing-analysis.md) - 事件监听器时序分析
  - Spring 事件机制深度解析
  - 异步事件处理优化
  - 时序问题排查指南

## 🚀 快速开始

### 🐳 Docker 环境启动 (推荐)

```bash
# 1. 克隆项目
git clone https://github.com/rymcu/mortise.git
cd mortise

# 2. 启动完整环境 (PostgreSQL + Redis + 应用)
docker-compose up -d

# 3. 查看服务状态
docker-compose ps
```

### 💻 本地开发环境

```bash
# 1. 环境检查
java -version  # 需要 Java 21
mvn -version   # 需要 Maven 3.6+

# 2. 启动依赖服务
docker-compose up -d postgresql redis

# 3. 启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 📊 监控端点快速访问

1. **手动访问流程**
   ```bash
   # 获取认证 Token
   curl -X POST http://localhost:9999/mortise/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"account":"admin","password":"admin123"}'
   
   # 使用 Token 访问监控端点
   curl -H "Authorization: Bearer <your_token>" \
     http://localhost:9999/mortise/actuator/health
   ```

3. **常用监控地址**
   - 健康检查: http://localhost:9999/mortise/actuator/health
   - 应用信息: http://localhost:9999/mortise/actuator/info  
   - 性能指标: http://localhost:9999/mortise/actuator/metrics
   - Prometheus: http://localhost:9999/mortise/actuator/prometheus

## 📋 常用监控端点

| 端点 | 描述 | URL |
|------|------|-----|
| 健康检查 | 应用整体健康状态 | `/mortise/actuator/health` |
| 应用信息 | 版本、环境信息 | `/mortise/actuator/info` |
| 性能指标 | JVM、系统指标 | `/mortise/actuator/metrics` |
| Prometheus | 监控系统集成 | `/mortise/actuator/prometheus` |
| 端点列表 | 所有可用端点 | `/mortise/actuator` |

## 🔒 安全配置

### JWT 认证机制
所有 Actuator 端点和 API 接口都采用 JWT Token 认证：

```json
{
  "登录地址": "POST /mortise/api/v1/auth/login",
  "请求格式": {"account": "用户名", "password": "密码"},
  "认证头": "Authorization: Bearer <token>",
  "Token有效期": "24小时",
  "刷新机制": "自动刷新"
}
```

### 默认管理员账户
```json
{
  "account": "admin",
  "password": "admin123",
  "role": "ADMIN",
  "permissions": ["ALL"]
}
```

### 安全特性
- ✅ **密码加密**: BCrypt 算法加密存储
- ✅ **Token 安全**: JJWT 0.12.5 + HS256 签名
- ✅ **配置加密**: Jasypt 敏感配置加密
- ✅ **CORS 控制**: 跨域请求安全配置
- ✅ **限流保护**: Resilience4j 防止暴力攻击

## 📈 监控集成

### 🎯 Prometheus + Grafana 监控栈

#### Prometheus 配置
```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'mortise-application'
    static_configs:
      - targets: ['localhost:9999']
    metrics_path: '/mortise/actuator/prometheus'
    scrape_interval: 10s
    bearer_token: 'your_jwt_token_here'
    honor_labels: true
    honor_timestamps: true
```

#### 核心监控指标

**🔧 JVM 性能指标**
```yaml
- jvm_memory_used_bytes{area="heap"}     # 堆内存使用量
- jvm_memory_used_bytes{area="nonheap"}  # 非堆内存使用量
- jvm_gc_pause_seconds                   # GC 暂停时间
- jvm_threads_live_threads               # 活跃线程数
- jvm_classes_loaded_classes             # 已加载类数量
```

**🌐 HTTP 请求指标**
```yaml
- http_server_requests_seconds_count     # 请求总数
- http_server_requests_seconds_sum       # 请求总耗时
- http_server_requests_seconds_max       # 最大响应时间
```

**💾 数据库连接池指标**
```yaml
- hikaricp_connections_active           # 活跃连接数
- hikaricp_connections_idle             # 空闲连接数
- hikaricp_connections_pending          # 等待连接数
- hikaricp_connections_timeout_total    # 连接超时总数
```

**🖥️ 系统资源指标**
```yaml
- system_cpu_usage                      # CPU 使用率
- system_memory_usage                   # 系统内存使用率
- disk_free_bytes                       # 磁盘可用空间
- process_uptime_seconds                # 应用运行时间
```

#### Grafana 仪表板模板

推荐导入以下仪表板：
- **Spring Boot 2.1 Statistics**: Dashboard ID `6756`
- **JVM (Micrometer)**: Dashboard ID `4701`
- **Spring Boot APM**: Dashboard ID `12900`

自定义告警规则：
```yaml
# 内存使用率超过 80%
- alert: HighMemoryUsage
  expr: (jvm_memory_used_bytes / jvm_memory_max_bytes) * 100 > 80
  
# 数据库连接池使用率超过 90%
- alert: HighDBConnectionUsage  
  expr: (hikaricp_connections_active / hikaricp_connections_max) * 100 > 90

# HTTP 错误率超过 5%
- alert: HighErrorRate
  expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) * 100 > 5
```

## 🛠️ 维护指南

### 📝 文档维护规范

#### 文档结构要求
```
docs/
├── README.md                    # 文档总览 (本文件)
├── *.md                        # 技术文档 (markdown 格式)
├── *.ps1                       # PowerShell 脚本
├── assets/                     # 文档资源文件
│   ├── images/                # 图片资源
│   └── diagrams/              # 架构图表
└── templates/                  # 文档模板
```

#### 更新流程
1. **新增文档**: 在 `docs/` 目录创建 markdown 文件
2. **更新索引**: 同步更新本 README.md 的文档列表
3. **版本控制**: 在文档末尾标注更新日期和版本
4. **交叉引用**: 确保文档间的链接正确有效

### 🔧 脚本维护

#### PowerShell 脚本规范
- **环境要求**: Windows PowerShell 5.1+ 或 PowerShell Core 7+
- **错误处理**: 包含完整的异常捕获和错误提示
- **参数验证**: 对输入参数进行合法性检查
- **日志记录**: 关键操作需要日志输出

### 📊 文档版本记录

| 版本 | 日期 | 主要更新内容 | 维护者 |
|------|------|-------------|--------|
| **v3.0** | 2025-09-24 | 文档全面重构，增加监控指南、安全配置、架构优化等 | RYMCU Team |
| **v2.1** | 2025-09-23 | 新增缓存优化、事件监听等文档 | RYMCU Team |
| **v2.0** | 2025-09-20 | 文档整理版本，技术文档迁移至 docs 目录 | RYMCU Team |
| **v1.0** | 2025-09-15 | 初始版本，Actuator 访问指南和自动配置优化 | RYMCU Team |

### 🎯 文档质量标准

#### 技术文档要求
- ✅ **完整性**: 包含背景、方案、实施、验证四个部分
- ✅ **准确性**: 代码示例可直接运行，配置参数真实有效
- ✅ **时效性**: 定期更新，确保与最新版本保持同步
- ✅ **可读性**: 使用清晰的标题层级和代码高亮

#### 质量检查清单
```markdown
- [ ] 文档标题清晰明确
- [ ] 包含目录结构 (超过500行)
- [ ] 代码示例完整可运行
- [ ] 包含实际效果截图 (如适用)
- [ ] 标注更新日期和维护者
- [ ] 交叉引用链接有效
- [ ] 格式符合 Markdown 规范
```

## � 实用工具

### 📋 常用命令速查

```bash
# 应用管理
mvn spring-boot:run -Dspring-boot.run.profiles=dev  # 开发环境启动
mvn clean package -DskipTests                        # 打包 (跳过测试)
java -jar target/mortise-0.0.1.war                  # 生产环境启动

# Docker 管理  
docker-compose up -d                                 # 后台启动所有服务
docker-compose logs -f mortise                       # 查看应用日志
docker-compose restart mortise                       # 重启应用服务

# 数据库管理
psql -h localhost -p 5432 -U mortise -d postgres    # 连接 PostgreSQL
redis-cli -h localhost -p 6379                      # 连接 Redis

# 监控检查
curl -s http://localhost:9999/mortise/actuator/health | jq  # 健康检查
```

### 🔍 故障排查指南

#### 常见问题 & 解决方案

**1. 应用启动失败**
```bash
# 检查端口占用
netstat -ano | findstr :9999
# 检查 Java 版本
java -version
# 查看详细错误日志
mvn spring-boot:run -X
```

**2. 数据库连接失败**
```yaml
# 检查配置文件 application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: mortise
    password: your_encrypted_password
```

**3. Redis 连接异常**
```bash
# 测试 Redis 连接
redis-cli ping
# 检查配置
redis-cli config get requirepass
```

### �📞 技术支持

#### 🆘 获取帮助的最佳方式

1. **📚 查阅文档**: 优先查看本目录下的相关技术文档
2. **🐛 提交 Issue**: [GitHub Issues](https://github.com/rymcu/mortise/issues)
3. **💬 讨论交流**: [GitHub Discussions](https://github.com/rymcu/mortise/discussions)
4. **👥 联系团队**: 发送邮件至 support@rymcu.com

#### 📋 提交问题时请包含

- ✅ **环境信息**: OS、Java 版本、Maven 版本
- ✅ **错误日志**: 完整的错误堆栈信息
- ✅ **复现步骤**: 详细的操作步骤
- ✅ **配置文件**: 相关的配置信息 (脱敏处理)

## 🎯 快速导航

### 📖 按角色分类

**🔰 新手开发者**
- [快速开始](#-快速开始) → [actuator-access-guide.md](actuator-access-guide.md)
- [Spring Boot 自动配置优化](spring-boot-autoconfiguration-optimization.md)

**🏗️ 架构师**  
- [项目架构优化](project-architecture-optimization.md)
- [数据库性能优化](database-performance-optimization.md)
- [限流方案对比](rate-limit-comparison.md)

**🔧 运维工程师**
- [数据库监控修复](database-monitoring-fixes.md)
- [健康检查配置](health-check-config-fixes.md)
- [Actuator 访问指南](actuator-access-guide.md)

**🛡️ 安全工程师**
- [限流防护方案](rate-limiting.md)

### 🏷️ 按技术分类

**Spring Boot 相关**
- [自动配置优化](spring-boot-autoconfiguration-optimization.md)
- [配置修复记录](configuration-fixes.md)
- [循环依赖修复](circular-dependency-fixes.md)

**数据库相关**
- [MyBatis-Flex 配置](mybatis-flex-config-fixes.md)  
- [数据库性能优化](database-performance-optimization.md)
- [连接池监控](database-monitoring-fixes.md)

**缓存相关**
- [缓存优化指南](cache-optimization-guide.md)
- [字典缓存实现](dict-cache-implementation.md)
- [缓存统一化报告](cache-unification-complete-report.md)

---

**📅 最后更新**: 2025-09-24  
**🏢 维护团队**: [RYMCU 开发团队](https://github.com/rymcu)  
**📧 技术支持**: support@rymcu.com  
**🌟 项目主页**: https://github.com/rymcu/mortise
