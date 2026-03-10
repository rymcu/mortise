# 商业子模块开发上手手册（后端版）

本手册面向 `mortise-community`、`mortise-commerce`、`mortise-payment` 一类商业后端子模块开发。

重点覆盖：

1. 安全规则接入
2. 通知能力接入
3. 日志与审计接入
4. 监控与指标接入
5. 后端模块脚手架与验证清单

## 目录

- [5 分钟速查表](#5-分钟速查表)
- [1. 先明确边界](#1-先明确边界)
- [2. 后端如何配置 Security](#2-后端如何配置-security)
- [3. 后端如何接入 Notify](#3-后端如何接入-notify)
- [4. 后端如何接入日志](#4-后端如何接入日志)
- [5. 后端如何接入监控](#5-后端如何接入监控)
- [6. 后端脚手架清单](#6-后端脚手架清单)
- [7. 真实示例索引](#7-真实示例索引)
- [8. 完成定义](#8-完成定义)

## 5 分钟速查表

| 需求 | 推荐落点 | 不推荐做法 |
|------|----------|------------|
| 新增公开/鉴权/管理接口权限 | `api/admin` 模块实现 `SecurityConfigurer` | 直接改 `mortise-auth` 统一规则 |
| 发站内信、邮件、微信通知 | `application` 注入 `NotificationService` | 在 Controller 里直接组装并发送 |
| 新增一种通知通道 | `application/infra` 实现 `NotificationSender` | 在业务代码里到处写第三方发送调用 |
| 记录接口审计 | Controller 上标 `@ApiLog` / `@OperationLog` | 在 Service 里散落手写日志 |
| 接入外部审计系统 | 实现 `LogStorage` SPI | 在每个接口里手写 MQ/HTTP 推送 |
| 上报业务指标 | `application/infra` 注入 `MeterRegistry` | 只打普通日志不做指标 |
| 做关键依赖健康检查 | `application/infra` 实现 `HealthIndicator` | 把检查逻辑塞进 Controller |

---

## 1. 先明确边界

商业模块开发前，先遵守仓库的两个硬约束：

1. **后端继续走分层模块**：`domain -> infra -> application -> admin/api`。
2. **同层模块不要直接硬依赖**：`mortise-auth`、`mortise-web-support`、`mortise-monitor` 这类基础模块与商业模块之间优先通过 SPI、Spring 事件、聚合装配协作，不要把逻辑硬塞进基础模块里。

推荐结构：

```text
mortise-xxx/
├── mortise-xxx-domain/
├── mortise-xxx-infra/
├── mortise-xxx-application/
├── mortise-xxx-admin/
└── mortise-xxx-api/
```

职责建议：

| 模块 | 职责 |
|------|------|
| `*-domain` | 实体、枚举、纯业务模型、请求/响应模型 |
| `*-infra` | Mapper、Repository、第三方适配器 |
| `*-application` | 用例编排、事务、事件、通知发起 |
| `*-admin` | 管理端 REST 接口、管理权限、审计注解 |
| `*-api` | 客户端 REST 接口、会员侧权限、回调入口 |

---

## 2. 后端如何配置 Security

### 2.1 结论

**不要去改 `mortise-auth` 里的统一规则列表，也不要在基础模块里硬编码你的业务路径。**

商业模块应当在自己的 `api` 或 `admin` 模块里实现 `SecurityConfigurer` SPI，把本模块的路径授权规则注册进去。

### 2.2 推荐放置位置

- 客户端接口规则：放在 `mortise-xxx-api/src/main/java/.../config/`
- 管理端接口规则：放在 `mortise-xxx-admin/src/main/java/.../config/`

命名建议：

- `XxxSecurityConfigurer`
- `XxxAdminSecurityConfigurer`

### 2.3 最小实现模板

```java
@Component
public class CommerceSecurityConfigurer implements SecurityConfigurer {

    @Override
    public int getOrder() {
        return 80;
    }

    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                .requestMatchers(HttpMethod.GET, "/api/v1/commerce/products", "/api/v1/commerce/products/**")
                .permitAll()
                .requestMatchers("/api/v1/commerce/**")
                .hasRole("MEMBER");
    }
}
```

### 2.4 设计准则

1. **公开读接口显式放行**：例如列表、详情、公共查询。
2. **写接口统一收口**：通常要求 `MEMBER` 或 `ADMIN` 角色。
3. **不要把业务判断写进 Controller**：权限规则放在 `SecurityConfigurer` 和 `@PreAuthorize`，业务校验放应用服务。
4. **不要跨模块改别人的安全配置**：你的模块只负责你的路径。

### 2.5 依赖建议

如果模块只需要 SPI 接口，优先让依赖尽量窄：

- `api/admin` 模块正常依赖 `mortise-web-support`
- 需要直接引用 `SecurityConfigurer` 时，可通过已有基础依赖链获取；若你的模块单独抽离时需要显式声明，优先保持为最小可用依赖，不把业务逻辑下沉到 `mortise-auth`

---

## 3. 后端如何接入 Notify

通知接入分两层理解：

1. **发送通知**：业务模块调用 `NotificationService`
2. **扩展新的通知通道**：业务模块实现 `NotificationSender` SPI

### 3.1 只是使用已有通知能力

如果你的商业模块只是要发系统通知、邮件、微信模板消息，一般只需要在 `application` 层依赖 `mortise-notification`，然后注入 `NotificationService` 发消息。

推荐调用位置：

- `application service`
- 领域事件监听器
- 支付/授权回调处理完成之后

不建议放在：

- Controller
- Mapper
- 实体方法

### 3.2 扩展新的通知通道

如果你的商业模块要新增一种通知类型或接管某种通知的真正发送逻辑，就在 `application` 或 `infra` 模块实现 `NotificationSender`。

```java
@Component
@RequiredArgsConstructor
public class CommerceSystemNotificationSender implements NotificationSender {

    private final CommerceNotificationService commerceNotificationService;

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public NotificationType supportType() {
        return NotificationType.SYSTEM;
    }

    @Override
    public boolean send(NotificationMessage message) {
        return commerceNotificationService.createSystemNotification(message);
    }
}
```

### 3.3 设计准则

1. **业务模块负责业务语义**。
2. **通知模块负责分发机制**：`NotificationServiceImpl` 会收集所有 `NotificationSender` 并按 `supportType()` 与 `getOrder()` 分发。
3. **真正的第三方发送细节放适配层**。
4. **不要在多个地方直接拼装同类消息**。

---

## 4. 后端如何接入日志

日志接入分两层：

1. **业务接口的审计记录**：用 `@ApiLog`、`@OperationLog`
2. **日志落库/落外部系统**：实现 `LogStorage` SPI

### 4.1 Controller 级接入规则

1. **查询接口标 `@ApiLog`**。
2. **有状态变更的接口同时标 `@ApiLog` + `@OperationLog`**。
3. **管理端所有变更操作必须有 `@OperationLog`**。

```java
@ApiLog(recordParams = true, recordResponseBody = false, value = "查询订单详情")
@GetMapping("/{id}")
public GlobalResult<OrderDetailVO> detail(@PathVariable Long id) {
    return GlobalResult.success(orderService.detail(id));
}

@ApiLog(recordParams = true, recordRequestBody = true, recordResponseBody = false, value = "取消订单")
@OperationLog(module = "订单", operation = "取消订单", recordParams = true, recordResult = true)
@PostMapping("/{id}/cancel")
public GlobalResult<Void> cancel(@PathVariable Long id) {
    orderService.cancel(id);
    return GlobalResult.success();
}
```

### 4.2 自定义日志存储

```java
@Component
public class CommerceAuditLogStorage implements LogStorage {

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public void save(OperationLogEntity log) {
        // 推送到你的审计系统
    }

    @Override
    public void saveApiLog(ApiLogEntity log) {
        // 推送 API 访问日志
    }
}
```

### 4.3 设计准则

1. **注解打在 Controller 方法上**，不要在 Service 上滥打审计注解。
2. **日志描述写业务语义**。
3. **敏感信息默认少记**。
4. **日志存储扩展不要侵入业务代码**。

---

## 5. 后端如何接入监控

监控接入通常有三类：

1. **Actuator 访问规则**
2. **业务指标上报**
3. **业务健康检查**

### 5.1 Actuator 安全接入

如果你的模块需要放行业务相关的 Actuator 端点，依然走 `SecurityConfigurer` SPI，不要改 `mortise-auth` 的硬编码。

### 5.2 业务指标上报

```java
@Configuration
public class CommerceMetricsConfig {

    public CommerceMetricsConfig(MeterRegistry meterRegistry) {
        Counter.builder("commerce.order.created.total")
                .description("创建订单总次数")
                .register(meterRegistry);
    }
}
```

如果指标值是动态快照，优先用 `gauge`；如果是累计事件，优先用 `counter`。

### 5.3 健康检查接入

```java
@Component
public class CommerceGatewayHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        boolean ok = pingGateway();
        return ok ? Health.up().withDetail("gateway", "ok").build()
                  : Health.down().withDetail("gateway", "unreachable").build();
    }
}
```

### 5.4 设计准则

1. **监控是模块自描述，不是全局散落日志**。
2. **指标命名带模块前缀**。
3. **健康检查只覆盖关键依赖**。
4. **公共标签交给 `mortise-monitor`**。

---

## 6. 后端脚手架清单

### 6.1 新建后端商业模块时

1. 建一个聚合父模块 `mortise-xxx/pom.xml`。
2. 建好 `domain / infra / application / admin / api` 五层。
3. `domain` 放实体、枚举、查询模型、请求/响应 DTO。
4. `infra` 放 Mapper、Repository、第三方客户端适配器。
5. `application` 放服务编排、事务、事件、通知发起、监控指标。
6. `admin` 和 `api` 只保留接口适配、参数绑定、权限注解、日志注解。
7. 在 `api/admin` 层分别补 `SecurityConfigurer`。
8. 在所有变更型接口标 `@ApiLog` + `@OperationLog`。
9. 若有通知需求，在 `application` 层注入 `NotificationService` 或实现 `NotificationSender`。
10. 若有监控需求，在 `application/infra` 层补 `MeterRegistry` 指标或 `HealthIndicator`。

### 6.2 最小 POM 模板

父聚合模块：

```xml
<project>
    <parent>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise</artifactId>
        <version>${revision}</version>
    </parent>

    <artifactId>mortise-xxx</artifactId>
    <packaging>pom</packaging>

    <modules>
        <module>mortise-xxx-domain</module>
        <module>mortise-xxx-application</module>
        <module>mortise-xxx-infra</module>
        <module>mortise-xxx-admin</module>
        <module>mortise-xxx-api</module>
    </modules>
</project>
```

`application` 模块最小骨架：

```xml
<dependencies>
    <dependency>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise-persistence</artifactId>
    </dependency>
    <dependency>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise-xxx-domain</artifactId>
    </dependency>
    <dependency>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise-xxx-infra</artifactId>
    </dependency>
    <dependency>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise-log</artifactId>
    </dependency>
    <dependency>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise-notification</artifactId>
    </dependency>
</dependencies>
```

`api/admin` 模块最小骨架：

```xml
<dependencies>
    <dependency>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise-web-support</artifactId>
    </dependency>
    <dependency>
        <groupId>com.rymcu</groupId>
        <artifactId>mortise-xxx-application</artifactId>
    </dependency>
</dependencies>
```

### 6.3 后端目录与类名模板

```text
mortise-xxx/
├── pom.xml
├── mortise-xxx-domain/
│   └── src/main/java/com/rymcu/mortise/xxx/
│       ├── entity/
│       ├── enums/
│       └── model/
├── mortise-xxx-infra/
│   └── src/main/java/com/rymcu/mortise/xxx/
│       ├── mapper/
│       └── repository/
├── mortise-xxx-application/
│   └── src/main/java/com/rymcu/mortise/xxx/
│       ├── service/
│       ├── service/impl/
│       ├── event/
│       ├── listener/
│       ├── sender/
│       └── config/
├── mortise-xxx-admin/
│   └── src/main/java/com/rymcu/mortise/xxx/admin/
│       ├── controller/
│       ├── model/
│       └── config/
└── mortise-xxx-api/
    └── src/main/java/com/rymcu/mortise/xxx/api/
        ├── controller/
        ├── model/
        └── config/
```

### 6.4 自检清单

1. `mvn -pl mortise-xxx-api -am clean compile -DskipTests`
2. `mvn -pl mortise-xxx-admin -am clean compile -DskipTests`
3. `mvn -pl mortise-app -am clean compile -DskipTests`
4. 至少选一个查询接口，确认 `@ApiLog` 是否正确记录。
5. 至少选一个变更接口，确认 `@OperationLog` 是否正确记录。
6. 若有通知能力，手工触发一次通知发送链路。
7. 若有健康检查或指标，确认 `/actuator/health` 或 `/actuator/prometheus` 能看到你的输出。

### 6.5 常见反模式

1. **把安全规则硬编码进 `mortise-auth`**。
2. **把通知直接塞进 Controller**。
3. **所有接口都堆进一个 Controller**。
4. **业务模块直接依赖同层基础模块内部实现**。
5. **只加业务代码不加可观测性**。

---

## 7. 真实示例索引

### 7.1 Security SPI 示例

- [mortise-auth/src/main/java/com/rymcu/mortise/auth/spi/SecurityConfigurer.java](../../mortise-auth/src/main/java/com/rymcu/mortise/auth/spi/SecurityConfigurer.java)
- [mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java](../../mortise-auth/src/main/java/com/rymcu/mortise/auth/config/WebSecurityConfig.java)
- [mortise-community/mortise-community-api/src/main/java/com/rymcu/mortise/community/api/config/CommunitySecurityConfigurer.java](../../mortise-community/mortise-community-api/src/main/java/com/rymcu/mortise/community/api/config/CommunitySecurityConfigurer.java)
- [mortise-system/mortise-system-admin/src/main/java/com/rymcu/mortise/system/config/SystemSecurityConfigurer.java](../../mortise-system/mortise-system-admin/src/main/java/com/rymcu/mortise/system/config/SystemSecurityConfigurer.java)
- [mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/MonitorSecurityConfigurer.java](../../mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/MonitorSecurityConfigurer.java)

### 7.2 Notify 示例

- [mortise-notification/src/main/java/com/rymcu/mortise/notification/spi/NotificationSender.java](../../mortise-notification/src/main/java/com/rymcu/mortise/notification/spi/NotificationSender.java)
- [mortise-notification/src/main/java/com/rymcu/mortise/notification/service/impl/NotificationServiceImpl.java](../../mortise-notification/src/main/java/com/rymcu/mortise/notification/service/impl/NotificationServiceImpl.java)
- [mortise-community/mortise-community-application/src/main/java/com/rymcu/mortise/community/sender/CommunitySystemNotificationSender.java](../../mortise-community/mortise-community-application/src/main/java/com/rymcu/mortise/community/sender/CommunitySystemNotificationSender.java)
- [mortise-wechat/src/main/java/com/rymcu/mortise/wechat/integration/WeChatNotificationSender.java](../../mortise-wechat/src/main/java/com/rymcu/mortise/wechat/integration/WeChatNotificationSender.java)

### 7.3 日志与审计示例

- [mortise-log/src/main/java/com/rymcu/mortise/log/annotation/ApiLog.java](../../mortise-log/src/main/java/com/rymcu/mortise/log/annotation/ApiLog.java)
- [mortise-log/src/main/java/com/rymcu/mortise/log/annotation/OperationLog.java](../../mortise-log/src/main/java/com/rymcu/mortise/log/annotation/OperationLog.java)
- [mortise-log/src/main/java/com/rymcu/mortise/log/aspect/OperationLogAspect.java](../../mortise-log/src/main/java/com/rymcu/mortise/log/aspect/OperationLogAspect.java)
- [mortise-persistence/src/main/java/com/rymcu/mortise/persistence/log/storage/DatabaseLogStorage.java](../../mortise-persistence/src/main/java/com/rymcu/mortise/persistence/log/storage/DatabaseLogStorage.java)
- [mortise-community/mortise-community-admin/src/main/java/com/rymcu/mortise/community/admin/controller/ArticleAdminController.java](../../mortise-community/mortise-community-admin/src/main/java/com/rymcu/mortise/community/admin/controller/ArticleAdminController.java)
- [mortise-commerce/mortise-commerce-admin/src/main/java/com/rymcu/mortise/commerce/admin/controller/OrderAdminController.java](../../mortise-commerce/mortise-commerce-admin/src/main/java/com/rymcu/mortise/commerce/admin/controller/OrderAdminController.java)

### 7.4 监控示例

- [mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/MetricsConfig.java](../../mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/MetricsConfig.java)
- [mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/ApplicationPerformanceConfig.java](../../mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/ApplicationPerformanceConfig.java)
- [mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/DatabasePerformanceConfig.java](../../mortise-monitor/src/main/java/com/rymcu/mortise/monitor/config/DatabasePerformanceConfig.java)
- [mortise-monitor/src/main/java/com/rymcu/mortise/monitor/health/DatabaseHealthIndicator.java](../../mortise-monitor/src/main/java/com/rymcu/mortise/monitor/health/DatabaseHealthIndicator.java)
- [mortise-monitor/src/main/java/com/rymcu/mortise/monitor/health/RedisHealthIndicator.java](../../mortise-monitor/src/main/java/com/rymcu/mortise/monitor/health/RedisHealthIndicator.java)
- [mortise-web-support/src/main/java/com/rymcu/mortise/web/health/Resilience4jRateLimiterHealthIndicator.java](../../mortise-web-support/src/main/java/com/rymcu/mortise/web/health/Resilience4jRateLimiterHealthIndicator.java)

### 7.5 模块聚合与依赖示例

- [mortise-community/pom.xml](../../mortise-community/pom.xml)
- [mortise-community/mortise-community-application/pom.xml](../../mortise-community/mortise-community-application/pom.xml)
- [mortise-community/mortise-community-api/pom.xml](../../mortise-community/mortise-community-api/pom.xml)
- [mortise-commerce/mortise-commerce-application/pom.xml](../../mortise-commerce/mortise-commerce-application/pom.xml)
- [mortise-commerce/mortise-commerce-api/pom.xml](../../mortise-commerce/mortise-commerce-api/pom.xml)
- [mortise-monitor/pom.xml](../../mortise-monitor/pom.xml)

---

## 8. 完成定义

一个新的商业后端模块在进入联调或提测前，至少应满足下面这些条件：

1. 目录层次清晰，职责没有混层。
2. 路由权限通过 `SecurityConfigurer` 或既有鉴权方式注册完成。
3. 查询接口与变更接口的日志策略已经补齐。
4. 有通知需求的流程已经统一收口到应用层或通知发送器。
5. 有关键外部依赖时，已经补了至少一种可观测性手段：指标、健康检查或日志存储扩展。
6. 至少完成一次最小编译校验和一次本地联调验证。
