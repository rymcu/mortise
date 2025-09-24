<pre align="center">

███╗   ███╗  ██████╗  ██████╗  ████████╗ ██╗ ███████╗ ███████╗
████╗ ████║ ██╔═══██╗ ██╔══██╗ ╚══██╔══╝ ██║ ██╔════╝ ██╔════╝
██╔████╔██║ ██║   ██║ ██████╔╝    ██║    ██║ ███████╗ █████╗  
██║╚██╔╝██║ ██║   ██║ ██╔══██╗    ██║    ██║ ╚════██║ ██╔══╝  
██║ ╚═╝ ██║ ╚██████╔╝ ██║  ██║    ██║    ██║ ███████║ ███████╗
╚═╝     ╚═╝  ╚═════╝  ╚═╝  ╚═╝    ╚═╝    ╚═╝ ╚══════╝ ╚══════╝


Built by RYMCU
</pre>

## Introduction

一款现代化的后台管理脚手架项目，基于 Spring Boot 3.5.6 构建，集成了完整的认证授权、监控、缓存等企业级功能。

### 🚀 技术栈

**后端核心**
- **Spring Boot 3.5.6** - 主框架
- **Spring Security 6** - 安全认证框架
- **JWT (JJWT 0.12.5)** - Token 认证
- **MyBatis-Flex 1.11.0** - ORM 框架
- **Redis** - 缓存与会话存储
- **PostgreSQL 17** - 主数据库

**监控与运维**
- **Spring Boot Actuator** - 应用监控
- **Prometheus** - 指标收集
- **Grafana** - 监控面板
- **HikariCP** - 数据库连接池监控

**其他组件**
- **Resilience4j** - 限流熔断
- **Jasypt** - 配置加密
- **Apache Commons** - 工具库集合
- **X-File-Storage** - 文件存储

## Features

### ✅ 已实现功能
- [x] **用户管理** - 用户注册、登录、信息管理
- [x] **角色管理** - 角色定义、权限分配
- [x] **菜单管理** - 动态菜单、层级管理
- [x] **权限管理** - 基于角色的访问控制 (RBAC)
- [x] **JWT 认证** - 无状态认证机制
- [x] **Redis 缓存** - 数据缓存与会话管理
- [x] **监控集成** - Actuator + Prometheus + Grafana
- [x] **限流保护** - Resilience4j 实现
- [x] **配置加密** - 敏感配置加密存储
- [x] **文档完善** - 详细的技术文档

### 🚧 开发中功能
- [ ] **字典管理** - 系统字典配置
- [ ] **事件日志** - 操作审计日志
- [ ] **对象存储** - 文件上传管理
- [ ] **API 文档** - Swagger/OpenAPI 集成

## Requirements

### 💻 开发环境
- **Java**: Eclipse Temurin 21
- **数据库**: PostgreSQL 17 (推荐) / MySQL 8.0+
- **构建工具**: Maven 3.6.0+
- **缓存**: Redis 6.0+
- **IDE**: IntelliJ IDEA / Eclipse

### 🐳 运行环境
- **Docker & Docker Compose** (推荐)
- **操作系统**: Windows 10+, macOS, Linux

## 🚀 Quick Start

### 方式一：Docker Compose (推荐)

1. **克隆项目**
   ```bash
   git clone https://github.com/rymcu/mortise.git
   cd mortise
   ```

2. **配置本地域名**
   ```bash
   # Windows
   .\update_hosts.bat
   
   # macOS/Linux
   ./update_hosts.sh
   ```

3. **生成 SSL 证书**
   ```bash
   # 安装 mkcert
   # Windows: choco install mkcert
   # macOS: brew install mkcert
   # Linux: 参考官方文档
   
   # 生成证书
   mkcert -install
   mkcert -key-file key.pem -cert-file cert.pem rymcu.local *.rymcu.local
   ```

4. **启动服务**
   ```bash
   docker-compose up -d
   ```

5. **配置代理管理器**
   - 访问 `http://localhost:81`
   - 默认账号: `admin@example.com` / `changeme`
   - 添加代理规则:
     - `npm.rymcu.local` → `app:81`
     - `auth.rymcu.local` → `logto:3010`
     - `logto.rymcu.local` → `logto:3011`
     - `rymcu.local` → `app:80`

### 方式二：本地开发

1. **环境准备**
   ```bash
   # 确保已安装 Java 21, Maven, PostgreSQL, Redis
   java -version
   mvn -version
   ```

2. **数据库配置**
   ```sql
   -- PostgreSQL
   CREATE DATABASE mortise;
   CREATE USER mortise WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE mortise TO mortise;
   ```

3. **配置文件**
   ```bash
   # 复制配置文件
   cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
   
   # 修改数据库和 Redis 连接信息
   ```

4. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

## 📊 监控与管理

### Actuator 端点访问

应用启动后，可通过以下方式访问监控端点：

1. **获取访问令牌**
   ```bash
   curl -X POST http://localhost:9999/mortise/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"account":"your_account","password":"your_password"}'
   ```

2. **访问监控端点**
   ```bash
   # 使用返回的 token
   curl -H "Authorization: Bearer <token>" \
     http://localhost:9999/mortise/actuator/health
   ```

### 监控端点列表

| 端点 | 描述 | URL |
|------|------|-----|
| 健康检查 | 应用整体健康状态 | `/mortise/actuator/health` |
| 应用信息 | 版本、环境信息 | `/mortise/actuator/info` |
| 性能指标 | JVM、系统指标 | `/mortise/actuator/metrics` |
| Prometheus | 监控系统集成 | `/mortise/actuator/prometheus` |
| 端点列表 | 所有可用端点 | `/mortise/actuator` |

### Prometheus + Grafana 集成

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'mortise-app'
    static_configs:
      - targets: ['localhost:9999']
    metrics_path: '/mortise/actuator/prometheus'
    scrape_interval: 15s
    bearer_token: 'your_jwt_token'
```

推荐监控指标：
- `jvm_memory_used_bytes` - JVM内存使用
- `jvm_gc_pause_seconds` - GC暂停时间  
- `hikaricp_connections_active` - 数据库连接数
- `http_server_requests_seconds` - HTTP请求耗时


## 📚 文档

项目包含完整的技术文档，位于 `docs/` 目录：

### 🔧 配置与优化
- [Spring Boot 自动配置优化](docs/spring-boot-autoconfiguration-optimization.md)
- [数据库性能优化](docs/database-performance-optimization.md)
- [MyBatis-Flex 配置](docs/mybatis-flex-config-fixes.md)
- [循环依赖修复](docs/circular-dependency-fixes.md)

### 📊 监控与运维
- [Actuator 访问指南](docs/actuator-access-guide.md)
- [数据库监控配置](docs/database-monitoring-fixes.md)
- [健康检查配置](docs/health-check-config-fixes.md)

### 🛡️ 安全与限流
- [限流方案实现](docs/rate-limiting.md)
- [限流方案对比](docs/rate-limit-comparison.md)

### 🏗️ 架构设计
- [项目架构优化](docs/project-architecture-optimization.md)

查看完整文档列表：[docs/README.md](docs/README.md)

## 🤝 贡献指南

### 开发规范
1. 代码风格遵循阿里巴巴 Java 开发手册
2. 提交信息使用 [Conventional Commits](https://www.conventionalcommits.org/) 格式
3. 新增功能需要包含单元测试
4. 重要变更需要更新相关文档

### 参与贡献
1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交变更 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📞 技术支持

- **文档**: 查阅 [docs](docs/) 目录下的技术文档
- **问题反馈**: [GitHub Issues](https://github.com/rymcu/mortise/issues)
- **功能建议**: [GitHub Discussions](https://github.com/rymcu/mortise/discussions)

## 📄 License

本项目基于 [MIT License](./LICENSE) 开源协议。

## 🙏 致谢

感谢所有为本项目做出贡献的开发者：

[![Contributors](https://contrib.rocks/image?repo=rymcu/mortise&max=1000)](https://github.com/rymcu/mortise/graphs/contributors)

---

**更新日期**: 2025-09-24  
**维护者**: [RYMCU 开发团队](https://github.com/rymcu)
