# Mortise 项目文档

这个目录包含了 Mortise 项目的技术文档、使用指南和维护脚本。

## 📂 文档结构

### 📊 监控与运维
- [`actuator-access-guide.md`](actuator-access-guide.md) - Spring Boot Actuator 访问指南
  - 如何访问和使用 Actuator 端点
  - 认证配置和安全考虑
  - 监控集成 (Prometheus, Grafana)
  - 故障排查指南

- [`test-actuator.ps1`](test-actuator.ps1) - Actuator 访问演示脚本
  - 自动化测试 Actuator 端点
  - 登录认证示例
  - 性能指标展示

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
- [`rate-limiting.md`](rate-limiting.md) - Resilience4j 限流方案
  - 限流策略实现
  - 注解使用指南
  - 性能配置优化

- [`rate-limit-comparison.md`](rate-limit-comparison.md) - 限流方案对比
  - 不同限流技术对比
  - 性能测试结果
  - 技术选型建议

### 🏗️ 架构优化
- [`project-architecture-optimization.md`](project-architecture-optimization.md) - 项目架构优化
  - 整体架构设计
  - 模块化改进
  - 代码结构优化

## 🚀 快速开始

### 访问 Actuator 监控端点

1. **确保应用正在运行**
   ```bash
   # 在项目根目录启动应用
   mvn spring-boot:run
   ```

2. **运行测试脚本**
   ```powershell
   # 从项目根目录运行
   .\docs\test-actuator.ps1
   ```

3. **手动访问端点**
   ```powershell
   # 登录获取 Token
   $loginData = @{
       account = "ronger@rymcu.com"
       password = "XzHvhX4CDaN696oQAXdmlcsrqgWbkxRl"
   } | ConvertTo-Json
   
   $response = Invoke-RestMethod -Uri "http://localhost:9999/mortise/api/v1/auth/login" -Method POST -ContentType "application/json" -Body $loginData
   $token = $response.data.token
   $headers = @{"Authorization" = "Bearer $token"}
   
   # 访问健康检查
   Invoke-RestMethod -Uri "http://localhost:9999/mortise/actuator/health" -Method GET -Headers $headers
   ```

## 📋 常用监控端点

| 端点 | 描述 | URL |
|------|------|-----|
| 健康检查 | 应用整体健康状态 | `/mortise/actuator/health` |
| 应用信息 | 版本、环境信息 | `/mortise/actuator/info` |
| 性能指标 | JVM、系统指标 | `/mortise/actuator/metrics` |
| Prometheus | 监控系统集成 | `/mortise/actuator/prometheus` |
| 端点列表 | 所有可用端点 | `/mortise/actuator` |

## 🔒 安全配置

所有 Actuator 端点都需要 JWT Token 认证：
- **登录地址**: `POST /mortise/api/v1/auth/login`
- **请求格式**: `{"account": "用户名", "password": "密码"}`
- **认证头**: `Authorization: Bearer <token>`

## 📈 监控集成

### Prometheus 配置
```yaml
scrape_configs:
  - job_name: 'mortise-app'
    static_configs:
      - targets: ['localhost:9999']
    metrics_path: '/mortise/actuator/prometheus'
    scrape_interval: 15s
    bearer_token: 'your_jwt_token'
```

### Grafana 仪表板
推荐监控指标：
- `jvm_memory_used_bytes` - JVM内存使用
- `jvm_gc_pause_seconds` - GC暂停时间
- `http_server_requests_seconds` - HTTP请求耗时
- `hikaricp_connections_active` - 数据库连接数
- `system_cpu_usage` - CPU使用率

## 🛠️ 维护说明

### 文档更新
- 所有新增的技术文档都应放在 `docs` 目录下
- 更新文档时请同步更新此 README
- 保持文档的时效性和准确性

### 脚本维护
- PowerShell 脚本需要在 Windows 环境下测试
- 添加新的自动化脚本时，请提供使用说明
- 确保脚本的跨环境兼容性

### 版本记录
- v2.0 - 文档整理版本：将所有技术文档迁移至 docs 目录，完善文档结构
- v1.0 - 初始版本，包含 Actuator 访问指南和自动配置优化报告
- 后续版本请在此记录重大更新

## 📞 技术支持

如有问题或建议，请：
1. 查阅相关文档
2. 运行测试脚本诊断问题
3. 联系项目维护团队

---

**更新日期**: 2025-09-23  
**维护者**: RYMCU 开发团队