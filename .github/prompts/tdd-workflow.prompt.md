---
description: "使用 TDD 红-绿-重构循环和垂直切片方式开发功能或修复 Bug"
name: "tdd-workflow"
argument-hint: "描述要实现的功能或要修复的 Bug"
agent: "agent"
---

使用**测试驱动开发**（TDD）工作流来实现功能或修复 Bug。

核心原则：测试应通过**公开接口**验证**行为**，而不是实现细节。代码可以彻底重写，测试不应因此失败。

先阅读并遵循这些仓库文件：
- [工作区说明](../copilot-instructions.md)
- [后端模块边界](../instructions/backend-module-boundaries.instructions.md)
- [前端 Nuxt 模式](../instructions/frontend-nuxt-patterns.instructions.md)

## 核心理念

### 垂直切片（Vertical Slice）

**禁止**先写完所有测试再写所有实现（水平切片）。正确做法是一次写一个测试、实现一次，循环往复：

```
错误（水平切片）：
  RED:   test1, test2, test3, test4, test5
  GREEN: impl1, impl2, impl3, impl4, impl5

正确（垂直切片）：
  RED→GREEN: test1→impl1
  RED→GREEN: test2→impl2
  RED→GREEN: test3→impl3
```

### 深模块（Deep Module）

好的模块 = 小接口 + 深实现。设计接口时问三个问题：
- 能否减少方法数量？
- 能否简化参数？
- 能否把更多复杂度藏在内部？

在 Mortise 中，这与模块分层天然契合：`*-domain` 和 `*-application` 的 Service 接口应保持精简，内部编排复杂度对调用方透明。

## 工作流程

### 1. 规划

编码前与用户确认：

- 需要哪些接口变更
- 优先测试哪些行为（不可能测试所有东西，聚焦关键路径）
- 识别深模块机会

提问："公开接口应该是什么样的？哪些行为最重要需要测试？"

### 2. Tracer Bullet（示踪弹）

写**一个**测试来确认系统的**一件事**：

```
RED:   写第一个行为测试 → 测试失败
GREEN: 写最小代码使测试通过 → 测试通过
```

这是示踪弹——证明端到端路径可行。

### 3. 增量循环

对剩余每个行为：

```
RED:   写下一个测试 → 失败
GREEN: 最小代码使其通过 → 通过
```

规则：
- 一次一个测试
- 只写足够让当前测试通过的代码
- 不要预判后续测试
- 测试聚焦可观察行为

### 4. 重构

所有测试通过后再重构：
- 提取重复代码
- 深化模块（把复杂度藏到简单接口后面）
- 在自然之处应用 SOLID 原则
- 每次重构后运行测试

**永远不要在 RED 阶段重构。** 先到 GREEN。

## 好测试 vs 坏测试

### 好测试：通过公开接口验证行为

**后端示例**（Java / Spring Boot）：

```java
// 好：测试可观察行为
@Test
void 用户可以通过有效购物车结账() {
    var cart = cartService.create(userId);
    cartService.addItem(cart.getId(), productId, 1);
    var order = checkoutService.checkout(cart.getId(), paymentMethod);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
}
```

**前端示例**（Vue / TypeScript）：

```typescript
// 好：测试用户可见行为
test('用户可以登录后看到仪表盘', async () => {
  await login({ username: 'test', password: 'pass' })
  const dashboard = await getDashboard()
  expect(dashboard.username).toBe('test')
})
```

### 坏测试：耦合实现细节

```java
// 坏：Mock 内部协作者，测试实现而非行为
@Test
void checkout调用paymentService() {
    var mockPayment = mock(PaymentService.class);
    checkoutService.checkout(cartId, paymentMethod);
    verify(mockPayment).process(any()); // 重构后这个测试就废了
}

// 坏：绕过接口直接查数据库验证
@Test
void createUser保存到数据库() {
    userService.createUser(new CreateUserRequest("Alice"));
    var row = jdbcTemplate.queryForMap("SELECT * FROM users WHERE name = ?", "Alice");
    assertThat(row).isNotNull(); // 应该通过 userService.getUser() 验证
}
```

## 何时使用 Mock

只在**系统边界**使用 Mock：
- 外部 API（支付、短信、邮件）
- 时间 / 随机数
- 第三方服务（微信、支付宝）

**不要** Mock：
- 自己的类 / 模块
- 内部协作者
- 自己控制的任何东西

在 Mortise 中，通过 SPI 接口和依赖注入实现可测试性，而非到处 Mock：

```java
// 好：通过 SPI 接口注入，测试时提供测试实现
public class OrderService {
    private final PaymentPort paymentPort; // SPI 接口

    public OrderService(PaymentPort paymentPort) {
        this.paymentPort = paymentPort;
    }
}
```

## 每个循环的检查清单

```
[ ] 测试描述行为，而不是实现
[ ] 测试只使用公开接口
[ ] 测试能在内部重构后存活
[ ] 代码是这个测试的最小实现
[ ] 没有添加投机性功能
```

## 验证

- 后端：`mvn -pl <模块> -am test`
- 前端：`pnpm --filter <app/package> test`（如有测试配置）
- 每个 RED→GREEN 循环后确认测试状态
