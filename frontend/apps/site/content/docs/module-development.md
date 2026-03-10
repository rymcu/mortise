---
title: 新模块开发教程
description: 按 Mortise 现有分层与 Layer 约束新增后端业务模块或前端功能模块
order: 7
---

# 新模块开发教程

这篇教程面向准备在 Mortise 中新增业务模块的开发者。核心目标只有两个：

1. 新能力落到正确的模块边界里。
2. 新模块可以被最小成本地装配、验证和维护。

如果你的需求只是给现有模块补一个接口、页面或配置项，优先在原模块内扩展；只有当需求已经形成独立业务域或明显的可选能力时，才新建模块。

阅读这篇时，建议同时打开一个最接近的现有模块做对照，例如后端参考 `mortise-community`，前端参考 `frontend/layers/community`。

## 先判断你要建哪类模块

| 场景 | 推荐形式 | 典型位置 |
|------|----------|----------|
| 新增后端业务域，如预约、工单、课程 | 标准后端业务模块 | `mortise-xxx/` |
| 新增前端可选功能，如社区、商城 | Nuxt Layer | `frontend/layers/xxx/` |
| 跨 app 复用的鉴权、SDK、UI、类型 | 共享包 | `frontend/packages/*` |
| 只是给已有业务加页面或接口 | 扩展现有模块 | 原模块内部 |

## 一、后端新模块怎么建

Mortise 的标准业务模块不是一个 jar，而是一组分层子模块。

### 推荐目录结构

```text
mortise-xxx/
├── pom.xml
├── mortise-xxx-domain/
├── mortise-xxx-application/
├── mortise-xxx-infra/
├── mortise-xxx-admin/
└── mortise-xxx-api/
```

可直接参考已有聚合模块，例如 `mortise-community`。

### 每层职责

| 模块 | 放什么 |
|------|--------|
| `domain` | 实体、枚举、领域模型、请求/响应模型 |
| `infra` | Mapper、Repository、第三方客户端适配 |
| `application` | 用例编排、事务、事件、通知发起、指标上报 |
| `admin` | 管理端接口、管理权限、日志注解 |
| `api` | 客户端接口、会员侧权限、回调入口 |

### 第一步：建聚合父模块

父模块 `pom.xml` 最少要声明父工程和 5 个子模块：

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

除了创建这个聚合模块本身，还要记得把它接入主工程的编译链。最少要检查两个位置：

1. 根目录 `pom.xml` 是否已经声明新模块。
2. `mortise-app/pom.xml` 是否已经引入新模块需要暴露出来的依赖。

### 第二步：先把依赖方向定死

依赖方向应保持为：

```text
domain <- infra <- application <- admin/api
```

不要反过来依赖，也不要让 `admin` 直接依赖 `infra` 绕过应用层。

建议在真正写业务代码前，先把每个子模块的 `pom.xml` 建好，并用最小依赖编译一次，避免写到一半才发现分层走歪。

### 第三步：先处理数据库与持久化

如果模块需要落库，优先先把下面 3 件事补齐：

1. 表结构或字段变更对应的 Flyway SQL。
2. `infra` 层的 Mapper / Repository。
3. `domain` 层的实体、枚举和请求响应模型。

推荐顺序是先有表和实体，再写应用服务，再补接口层。这样后面接口联调时不会反复回头改模型。

### 第四步：补业务接口，不改基础模块硬编码

如果模块需要新增公开接口、会员接口或管理接口，不要去改 `mortise-auth` 的统一规则，而是在自己的 `api` 或 `admin` 模块中实现 `SecurityConfigurer`。

最小示例：

```java
@Component
public class XxxSecurityConfigurer implements SecurityConfigurer {

    @Override
    public int getOrder() {
        return 80;
    }

    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                .requestMatchers("/api/v1/xxx/public/**").permitAll()
                .requestMatchers("/api/v1/xxx/**").hasRole("MEMBER");
    }
}
```

        管理端规则通常放在 `mortise-xxx-admin`，客户端规则放在 `mortise-xxx-api`。不要把两类路径揉进同一个接口模块里。

        ### 第五步：把通知、日志、监控接到扩展点上

新模块通常会碰到三类横切能力：

| 能力 | 正确做法 | 不推荐 |
|------|----------|--------|
| 通知 | `application` 注入 `NotificationService` 或实现 `NotificationSender` | 在 Controller 里直接发消息 |
| 日志审计 | Controller 上使用 `@ApiLog`、`@OperationLog` | Service 里到处手写审计日志 |
| 指标/健康检查 | `application` 或 `infra` 注入 `MeterRegistry`、实现 `HealthIndicator` | 把监控逻辑塞进页面接口 |

### 第六步：把模块装配进应用

新增模块后，需要让 `mortise-app` 感知到它。常见方式是：

1. 将新模块加入根 `pom.xml` 或对应聚合编译链。
2. 在 `mortise-app` 中加入对新模块的依赖。
3. 确认 Spring 扫描路径和 Mapper 扫描能覆盖到新模块。

如果是商业模块，还要处理好 submodule 拉取与按需装配，不要把商业源码直接并进主仓。

### 第七步：按接口类型补最小闭环

一个新业务模块至少应该先跑通一条完整链路：

1. 一个查询接口。
2. 一个写接口。
3. 一条权限规则。
4. 一条日志记录。
5. 一次最小编译验证。

不要一开始就把所有接口铺开。先打通一条完整闭环，再批量复制扩展，会稳得多。

## 二、前端新功能模块怎么建

前端新模块先分两类：

### 场景 A：宿主 app 的常驻功能

如果它本来就是站点或管理端的固定能力，直接进入对应 app：

- 管理端功能放 `frontend/apps/admin`
- 站点常驻功能放 `frontend/apps/site`

### 场景 B：可选业务功能

如果它是按需启用的业务能力，优先做成 Nuxt Layer，而不是把页面散落到宿主 app。

推荐结构：

```text
frontend/layers/xxx/
├── package.json
├── nuxt.config.ts
├── README.md
├── app/
│   ├── pages/
│   ├── components/
│   ├── composables/
│   └── types/
└── server/
```

如果这个功能未来可能单独授权、按客户启用或独立发布，基本就应该优先做成 Layer。

### Layer 的基本规则

| 需求 | 应该放哪 |
|------|----------|
| 页面、局部业务组件 | `frontend/layers/xxx/app/*` |
| 跨 app 复用鉴权 | `frontend/packages/auth` |
| 跨 app 复用 API | `frontend/packages/core-sdk` |
| 共用 UI 组件 | `frontend/packages/ui` |

不要在多个页面或多个 Layer 里各写一套 fetch、登录态和类型定义。

### 新功能页面落地顺序

推荐按这个顺序推进：

1. 先确认这是宿主 app 常驻能力还是可选 Layer。
2. 先补共享类型、SDK 能力，再写页面。
3. 页面只做展示和交互，不承担认证、URL 拼接和复杂数据整形。
4. 最后再补路由守卫、菜单入口和联调文案。

### 新建 Layer 的最小文件

`package.json`：

```json
{
  "name": "@mortise/xxx-layer",
  "private": true,
  "version": "0.1.0",
  "type": "module",
  "dependencies": {
    "@mortise/core-sdk": "workspace:*"
  }
}
```

`nuxt.config.ts`：

```ts
export default defineNuxtConfig({
  imports: {
    dirs: ['stores']
  }
})
```

### 激活方式

```bash
cd frontend
pnpm layer:add <name>
```

这个脚本会检查 Layer 是否存在，把依赖写入目标 app，再执行 `pnpm install`。不要手动到处散改依赖。

如果功能不是可选 Layer，而是 `apps/site` 或 `apps/admin` 的常驻能力，也建议沿用同样的思路：共享逻辑先进 `packages/*`，页面最后再接。

## 三、什么时候该建 packages，而不是建模块

如果你做的是下面这些东西，通常不该单独做业务模块：

- 通用鉴权逻辑
- 通用 API SDK
- 共享 UI 组件
- 工程配置
- 多个页面都要复用的类型和工具函数

这些应该优先放在 `frontend/packages/*`，或放在后端已有基础模块中按边界扩展。

## 四、最小开发流程

### 后端模块

1. 先画清楚模块职责与分层。
2. 建聚合父模块和 5 个子模块骨架。
3. 先补 Flyway、实体、Mapper、Repository。
4. 再补 `application` 服务编排。
5. 最后再补 `admin/api` 接口和权限规则。
6. 用 `SecurityConfigurer`、`NotificationSender`、`LogStorage` 等扩展点接入横切能力。
7. 装配进 `mortise-app` 并做最小编译验证。

### 前端 Layer

1. 在 `frontend/layers/<name>` 建目录。
2. 先写 `package.json` 和 `nuxt.config.ts`。
3. 把共享类型和 API 能力优先放进 `packages/*`。
4. 页面、组件、composable 再按 Layer 目录组织。
5. 通过 `pnpm layer:add <name>` 激活。
6. 做一次类型检查和本地联调。

## 五、开发时通常会改到哪些文件

### 后端新模块

最常见会涉及：

- 根目录 `pom.xml`
- `mortise-app/pom.xml`
- `mortise-xxx/pom.xml`
- `mortise-xxx-domain/**`
- `mortise-xxx-infra/**`
- `mortise-xxx-application/**`
- `mortise-xxx-admin/**`
- `mortise-xxx-api/**`
- `mortise-app/src/main/resources/db/migration/**` 或对应迁移目录

### 前端新模块

最常见会涉及：

- `frontend/apps/site/**` 或 `frontend/apps/admin/**`
- `frontend/layers/xxx/**`
- `frontend/packages/core-sdk/**`
- `frontend/packages/auth/**`
- `frontend/packages/ui/**`

## 六、最小验证命令

### 后端

```bash
mvn -pl mortise-xxx-api -am clean compile -DskipTests
mvn -pl mortise-xxx-admin -am clean compile -DskipTests
mvn -pl mortise-app -am clean compile -DskipTests
```

### 前端

```bash
cd frontend
pnpm install
pnpm layer:add <name>
pnpm --filter @mortise/site typecheck
```

如果你改的是管理端常驻功能，可以把最后一条换成：

```bash
pnpm --filter @mortise/admin typecheck
```

## 七、常见反模式

### 后端

- 直接把业务路径硬编码进 `mortise-auth`
- 在 Controller 里写事务、通知、审计、第三方调用
- 让 `admin/api` 直接依赖 `infra`
- 新模块完全没有监控或日志策略

### 前端

- 把可选业务页面直接散落到 `apps/site`
- 在页面层直接拼后端 URL
- 重复实现宿主 app 的登录态逻辑
- 不通过 `pnpm layer:add`，手工修改多个地方的依赖

## 八、完成定义

一个新模块至少应满足：

1. 目录结构符合仓库现有模式。
2. 权限规则通过模块自己的扩展点注册。
3. 关键变更接口具备日志策略。
4. 需要通知或监控时，已经接入统一扩展点。
5. 完成过一次最小编译或类型校验。

如果你接下来要落地具体模块，优先先拷贝一个最接近的现有模块骨架，再删掉不需要的内容，而不是从零自由发挥。