# 系统监控模块实现计划

## 优先级：P3

## 模块说明

`mortise-monitor` 集成了 Spring Boot Actuator，并放行了所有 `/actuator/**` 端点。前端通过直接调用 Actuator 端点展示系统状态监控面板。

## 可用 Actuator 端点

| 端点 | 说明 |
|------|------|
| `GET /actuator/health` | 健康状态（包含 Redis、DB 健康指标） |
| `GET /actuator/info` | 应用信息（版本、构建时间等） |
| `GET /actuator/metrics` | 可用指标列表 |
| `GET /actuator/metrics/{name}` | 单个指标详情 |
| `GET /actuator/prometheus` | Prometheus 格式指标（包含 JVM、HTTP 等） |
| `GET /actuator/ratelimiters` | 限流器状态 |

常用指标：
- `jvm.memory.used` / `jvm.memory.max` — JVM 堆内存
- `jvm.threads.live` — 活跃线程数
- `system.cpu.usage` — 系统 CPU 使用率
- `process.cpu.usage` — JVM 进程 CPU 使用率
- `http.server.requests` — HTTP 请求统计

## 前端实现

### 监控仪表盘页面

**文件**：`frontend/apps/admin/app/pages/monitor/index.vue`

- 路由：`/admin/monitor`
- 页面分区：
  1. **健康状态卡片** — DB / Redis / Disk Space / Application 各显示 UP/DOWN badge
  2. **JVM 内存** — 堆内存已用 / 最大，进度条展示使用率
  3. **CPU 使用率** — 系统 CPU + JVM CPU 双指标数值
  4. **线程** — 活跃线程数、峰值线程数
  5. **HTTP 请求概览** — 总数、平均耗时、错误率（from `http.server.requests`）
  6. **应用信息** — 版本号、Java 版本、主机名（from `/actuator/info`）
  7. **限流器** — 各限流器名称 + 失败率（from `/actuator/ratelimiters`）
- 顶部"刷新"按钮 + 自动轮询（30s 间隔）
- 数据加载中展示 Skeleton

### 所需 Pages 目录结构

```
pages/
  monitor/
    index.vue
```

## 进度状态

- [ ] monitor/index.vue
