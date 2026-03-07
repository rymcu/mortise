# Project-Level Copilot Instructions

## Build Tool

- This project uses **Maven** as the build tool.
- **Always use `mvn` (system-installed Maven) instead of `mvnw` or `mvnw.cmd` (Maven Wrapper).**
- Example commands:
  - Build: `mvn clean package -DskipTests`
  - Full build with tests: `mvn clean install`
  - Run tests: `mvn test`
  - Run application: `mvn spring-boot:run`

## Project Overview

- This is a **Spring Boot** multi-module Java project named **Mortise**.
- The project uses **Java 21+**, **MyBatis-Flex**, **PostgreSQL**, and **Spring Security OAuth2**.
- Frontend is in `frontend/` directory, using **pnpm** as package manager.

## Terminal Environment

- Default terminal is **PowerShell 7 (pwsh)**.
- Use PowerShell syntax and cmdlets when running terminal commands.

## Language

- Code comments, commit messages, and documentation should be in **Chinese (简体中文)** unless specified otherwise.

## Agent Skills

- Frontend skill files are located in `frontend/.claude/skills/` directory.
- When working on frontend code, read the relevant skill files before implementing.

## Frontend UI Constraints

### ID 类型约定 (ID Types)

- **前端所有 ID 字段（包括路由参数、接口请求/响应中的 `id`、`articleId`、`userId` 等）一律使用 `string` 类型，禁止使用 `number`。**
- 后端返回的雪花 ID 超出 JavaScript `Number.MAX_SAFE_INTEGER`，使用 `number` 会导致精度丢失。
- 路由参数（`route.params.xxx`）本身即为 `string`，无需转换。

```ts
// ❌ 禁止：将 ID 定义为 number
interface Article { id: number; authorId: number }

// ✅ 正确：ID 一律为 string
interface Article { id: string; authorId: string }
```

```ts
// ❌ 禁止：将路由参数转为 number
const articleId = computed(() => Number(route.params.id))

// ✅ 正确：直接使用 string
const articleId = computed(() => route.params.id as string)
```

### TypeScript 类型定义 (TypeScript Types)

- **禁止在 `.vue` 文件的 `<script setup>` 中定义 `interface` 或 `type`。**
- 所有 `interface`、`type` 必须提取到对应的 `.ts` 类型文件（通常放在 `app/types/` 目录）并通过 `import type` 引入。
- `.vue` 文件中只允许使用通过 `import type` 引入的类型。

```ts
// ❌ 禁止：直接在 .vue 文件中定义 interface
// <script setup lang="ts">
// interface User { id: number; name: string }

// ✅ 正确：提取到 app/types/user.ts，然后在 .vue 中引入
// import type { User } from '~/types/user'
```

### 密码输入 (Password Input)

- **所有 `type="password"` 的 `UInput` 必须实现显示/隐藏密码切换功能**，不得使用静态 `type="password"`。
- 使用 `ref` 管理 `show` 状态，通过 `:type="show ? 'text' : 'password'"` 动态切换。
- 在 `#trailing` slot 中放置切换按钮，图标使用 `i-lucide-eye` / `i-lucide-eye-off`，需设置 `aria-label` 和 `aria-pressed`。
- 父级 `UInput` 必须设置 `:ui="{ trailing: 'pe-1' }"`。

```vue
<!-- 标准密码输入实现模板 -->
<script setup lang="ts">
const showPassword = ref(false)
</script>

<UInput
  v-model="state.password"
  :type="showPassword ? 'text' : 'password'"
  :ui="{ trailing: 'pe-1' }"
  placeholder="请输入密码"
  class="w-full"
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
