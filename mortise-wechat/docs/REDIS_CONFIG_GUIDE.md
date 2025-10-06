# Redis 统一管理 - 快速配置指南

## 配置说明

微信模块现在已经统一使用项目的 Redis 配置，不再需要单独配置 Redis 连接信息。

### ✅ 推荐配置方式

```yaml
# application.yml
spring:
  data:
    redis:
      host: 192.168.21.238    # Redis 主机地址
      port: 6379              # Redis 端口
      password: your-password # Redis 密码
      database: 1             # 使用的数据库编号

# 微信配置
wx:
  mp:
    use-redis: true           # 启用 Redis 存储（使用项目的 Redis 配置）
    configs:
      - app-id: wx1234567890abcdef
        secret: your-app-secret
        token: your-token
        aes-key: your-aes-key
        account-name: 默认公众号
        enabled: true
```

### ❌ 不推荐的配置方式（已废弃）

```yaml
# ❌ 不要这样配置，会被忽略
wx:
  mp:
    use-redis: true
    redis-config:           # 此配置已不再使用
      host: xxx
      port: xxx
      timeout: xxx
```

## 优势

1. **统一管理**：所有 Redis 连接在一处配置
2. **资源优化**：共享 Redis 连接池
3. **配置简化**：减少重复配置
4. **自动降级**：Redis 不可用时自动切换到内存存储

## 环境配置

### 开发环境（可选 Redis）
```yaml
wx:
  mp:
    use-redis: false  # 使用内存存储，不依赖 Redis
```

### 生产环境（必须 Redis）
```yaml
wx:
  mp:
    use-redis: true   # 使用 Redis，支持集群部署
```

## 详细文档

- [Redis 统一管理方案](./REDIS_UNIFIED_MANAGEMENT.md)
- [优雅启动修复说明](./GRACEFUL_STARTUP_FIX.md)
