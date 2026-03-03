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
