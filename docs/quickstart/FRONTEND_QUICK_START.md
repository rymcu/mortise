# 前端快速上手指南

本指南面向首次接触 Mortise 前端工作区的开发者，覆盖从环境准备到运行开发服务器的完整流程。

---

## 1. 前置条件

### 1.1 必需工具

| 工具 | 版本要求 | 安装方式 |
|------|----------|----------|
| **Node.js** | 20+ | https://nodejs.org 或 [fnm](https://github.com/Schniz/fnm) |
| **pnpm** | 10+ | `npm install -g pnpm` |
| **Git** | 2.30+ | https://git-scm.com |

> ⚠️ 前端工作区**只支持 pnpm**，不支持 npm 或 yarn。

### 1.2 验证环境

```bash
node -v    # 应输出 v20.x.x 或更高
pnpm -v    # 应输出 10.x.x 或更高
```

### 1.3 后端服务

前端开发需要后端 API 支持。根目录的 `compose.yaml` 仅启动依赖服务（PostgreSQL、Redis、Logto、Nginx），Spring Boot 应用需单独运行：

```bash
# 步骤 1：在项目根目录启动依赖服务
docker compose up -d

# 步骤 2：启动 Spring Boot 应用（任选其一）
# 方式 A：Maven
cd mortise-app && mvn spring-boot:run

# 方式 B：直接运行 jar
java -jar mortise-app/target/mortise-app-*.jar
```

后端启动后，API 默认监听 `http://localhost:9999/mortise`。

---

## 2. 克隆与安装

```bash
# 克隆主仓（如已克隆跳过此步）
git clone https://github.com/rymcu/mortise.git
cd mortise/frontend

# 安装所有依赖（apps + packages 一次搞定）
pnpm install
```

安装完成后，目录结构如下：

```
frontend/
├── apps/
│   ├── admin/      # 后台管理端
│   └── web/        # 用户端/会员端
└── packages/
    ├── auth/       # 鉴权逻辑
    ├── core-sdk/   # API SDK
    ├── ui/         # 共享组件
    └── config/     # 工程配置
```

---

## 3. 环境变量配置

**本地开发无需配置任何环境变量**，两个应用均已内置可用的默认值：

| 应用 | 默认 `apiBase` | 说明 |
|------|----------------|------|
| admin | `/mortise` | 相对路径，由 Vite proxy 转发至 `localhost:9999` |
| web | `http://localhost:9999/mortise` | 直连本地后端 |

仅当后端**不在 `localhost:9999`** 时（如连接测试/生产环境），才需要通过环境变量覆盖：

```bash
# 方式 A：在 frontend/ 目录创建 .env.local（不会提交到 Git）
echo "NUXT_PUBLIC_API_BASE=https://api.your-domain.com/mortise" > .env.local

# 方式 B：启动时直接传入
NUXT_PUBLIC_API_BASE=https://api.your-domain.com/mortise pnpm dev:admin
```

> ⚠️ **注意**：admin 应用的 Vite proxy 仅对相对路径 `/mortise/...` 生效。若将 `NUXT_PUBLIC_API_BASE` 设为完整 URL（含 `http://`），请求将绕过代理直连后端，**本地开发会触发 CORS**。请仅在连接非 localhost 后端时才设置此变量。

---

## 4. 启动开发服务器

### 4.1 管理端

```bash
pnpm dev:admin
```

启动后访问：**http://localhost:3000/admin/**

### 4.2 用户端

```bash
pnpm dev:web
```

启动后访问：**http://localhost:3001/**

### 4.3 Vite 代理说明

两个应用均已预配置 Vite 开发代理：

```
/mortise/** → http://localhost:9999
```

这意味着开发时**无需配置 CORS**，也无需启动 nginx，请求直接通过代理转发到后端。

---

## 5. 登录测试

### 管理端登录

1. 打开 http://localhost:3000/admin/auth/login
2. 输入后端初始化的管理员账号（默认见后端 Flyway 迁移脚本）
3. 登录成功后跳转至 `/admin/dashboard`

### 用户端登录

1. 打开 http://localhost:3001/app/auth/login
2. 可先注册一个测试账号：http://localhost:3001/app/auth/register
3. 注册成功后使用账号密码登录

---

## 6. 项目结构详解

### Monorepo 工作区

```
frontend/
├── apps/
│   ├── admin/                  # @mortise/admin
│   │   ├── app/
│   │   │   ├── pages/          # 路由页面
│   │   │   ├── layouts/        # 页面布局
│   │   │   ├── components/     # 业务组件
│   │   │   ├── composables/    # 组合式函数
│   │   │   ├── middleware/     # 路由守卫
│   │   │   ├── stores/         # Pinia 状态
│   │   │   └── types/          # 类型定义
│   │   ├── nuxt.config.ts
│   │   └── package.json
│   └── web/                    # @mortise/web
│       ├── app/
│       │   ├── pages/
│       │   ├── layouts/
│       │   ├── middleware/
│       │   └── stores/
│       ├── nuxt.config.ts
│       └── package.json
└── packages/
    ├── auth/                   # @mortise/auth
    │   └── src/
    │       ├── client.ts       # 请求客户端（Token 注入 + 401 刷新）
    │       ├── result.ts       # API 响应标准化
    │       ├── storage.ts      # Token 持久化
    │       └── types.ts        # 鉴权类型
    ├── core-sdk/               # @mortise/core-sdk
    │   └── src/
    │       ├── admin.ts        # 管理端 API 封装
    │       ├── types.ts        # DTO/VO 类型
    │       └── index.ts        # 导出入口
    ├── ui/                     # @mortise/ui（共享组件）
    └── config/                 # @mortise/config（工程配置）
```

### 管理端已有页面

| 模块 | 路由前缀 | 说明 |
|------|----------|------|
| 鉴权 | `/admin/auth/` | 登录、回调、忘记密码 |
| 仪表盘 | `/admin/dashboard` | 数据概览 |
| 会员 | `/admin/members` | 会员列表 |
| 用户管理 | `/admin/systems/users` | CRUD |
| 角色管理 | `/admin/systems/roles` | CRUD |
| 菜单管理 | `/admin/systems/menus` | 树形 CRUD |
| 字典类型 | `/admin/systems/dict-types` | CRUD |
| 字典项 | `/admin/systems/dictionaries` | CRUD |
| OAuth2 客户端 | `/admin/systems/oauth2-clients` | 第三方登录配置 |
| 通知渠道 | `/admin/systems/notification-channels` | 通知参数配置 |
| 微信公众号 | `/admin/systems/wechat-accounts` | 多账号管理 |
| 站点配置 | `/admin/systems/site-config` | 全局参数 |
| 个人设置 | `/admin/settings/` | 资料/通知/安全 |
| 消息中心 | `/admin/inbox` | 消息列表 |

---

## 7. 常用开发命令

```bash
# 开发
pnpm dev:admin          # 启动管理端
pnpm dev:web            # 启动用户端

# 构建
pnpm build              # 构建所有应用

# 代码质量
pnpm lint               # ESLint 校验（所有 app）
pnpm format             # Prettier 格式化（所有文件）
pnpm format:check       # Prettier 检查（不修改）
pnpm lint:prettier-check  # ESLint + Prettier 联合检查（CI 推荐）

# TypeScript
pnpm typecheck          # 类型检查（所有 app）
pnpm typecheck:packages # 类型检查（所有 packages）
pnpm typecheck:auth     # 仅检查 @mortise/auth
pnpm typecheck:core-sdk # 仅检查 @mortise/core-sdk
```

---

## 8. Docker Compose 部署

如需通过 Docker 部署前端，在 `frontend/` 目录执行：

```bash
docker compose -f compose.yaml up -d --build
```

启动后访问：

| 应用 | 地址 |
|------|------|
| 管理端 | http://localhost:3101/admin/ |
| 用户端 | http://localhost:3102/ |
| 官网（预留） | http://localhost:3103/ |

自定义后端地址：

```bash
# Linux/macOS
NUXT_PUBLIC_API_BASE=https://api.your-domain.com/mortise \
  docker compose -f compose.yaml up -d --build

# PowerShell
$env:NUXT_PUBLIC_API_BASE = "https://api.your-domain.com/mortise"
docker compose -f compose.yaml up -d --build
```

---

## 9. 开发规范

### 9.1 密码输入

所有密码输入框必须实现显示/隐藏切换，**不得使用**静态 `type="password"`：

```vue
<script setup lang="ts">
const showPassword = ref(false)
</script>

<UInput
  v-model="state.password"
  :type="showPassword ? 'text' : 'password'"
  :ui="{ trailing: 'pe-1' }"
>
  <template #trailing>
    <UButton
      color="neutral"
      variant="link"
      size="sm"
      :icon="showPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
      :aria-label="showPassword ? '隐藏密码' : '显示密码'"
      :aria-pressed="showPassword"
      @click="showPassword = !showPassword"
    />
  </template>
</UInput>
```

### 9.2 API 调用

- 所有 API 调用通过 `@mortise/core-sdk` 发起，**不在页面层直接拼接 URL**
- Token 注入与刷新由 `@mortise/auth` 的 `client.ts` 自动处理
- 禁止引入 commerce/order/payment 相关接口

### 9.3 路由守卫

需要登录的页面在 `middleware/` 目录添加 `auth` middleware。执行 `navigateTo('/admin/auth/login')` 进行重定向，**不要**直接操作 `window.location`。

---

## 10. 常见问题

### Q: pnpm install 报错 `ERR_PNPM_OUTDATED_LOCKFILE`

删除 `node_modules` 和 `pnpm-lock.yaml` 后重新安装：

```bash
# 在 frontend/ 目录
Remove-Item -Recurse -Force node_modules
pnpm install
```

### Q: 启动报错 `Failed to fetch` / 接口 404

检查后端服务是否已启动：

```bash
curl http://localhost:9999/mortise/actuator/health
```

如果后端未启动，先执行：

```bash
# 项目根目录
docker compose up -d
```

### Q: 登录后跳转到空白页

检查 `nuxt.config.ts` 的 `app.baseURL` 是否与访问路径一致：
- 管理端 baseURL 为 `/admin/`，需通过 http://localhost:3000/admin/ 访问

### Q: 修改 packages 后 app 未更新

pnpm workspace 支持热更新，一般无需重启。如仍未更新，重启对应 app 的开发服务器：

```bash
# 停止后重新启动
pnpm dev:admin
```

### Q: TypeScript 报错但代码可以运行

先运行类型检查确认问题范围：

```bash
pnpm typecheck
pnpm typecheck:packages
```

packages 中的类型错误不会阻止 app 运行，但需要修复以保证代码质量。
