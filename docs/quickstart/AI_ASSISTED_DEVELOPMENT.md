# AI 辅助开发指南

本项目在 `.github/` 目录下维护了一套 Copilot 自定义指令和 prompt，帮助开发者在 AI 辅助下保持一致的工程规范。

## 文件结构

```
.github/
├── copilot-instructions.md                    # 全局指令（自动加载）
├── instructions/
│   ├── backend-module-boundaries.instructions.md   # 后端模块边界
│   ├── frontend-nuxt-patterns.instructions.md      # 前端 Nuxt 模式
│   ├── permission-conventions.instructions.md      # 权限命名规范
│   ├── java.instructions.md                        # Java 编码规范
│   └── springboot.instructions.md                  # Spring Boot 规范
└── prompts/
    ├── mortise-env-triage.prompt.md            # 环境链路排障
    ├── tdd-workflow.prompt.md                  # TDD 红-绿-重构工作流
    ├── write-prd.prompt.md                     # PRD 编写
    └── triage-issue.prompt.md                  # Bug 根因排查
```

**instructions** 按文件匹配模式自动注入上下文，开发者无需手动触发。

**prompts** 需要开发者主动调用，适用于特定工作流场景。

## 在 VS Code Copilot Chat 中使用

### 自动指令（Instructions）

编辑 `.java` 文件时，`java.instructions.md` 和 `springboot.instructions.md` 会自动注入 Chat 上下文，无需额外操作。

编辑前端 `.vue` / `.ts` 文件时，`frontend-nuxt-patterns.instructions.md` 会自动生效。

### 手动 Prompt

1. 打开 Copilot Chat 面板
2. 点击输入框左侧的 📎（附件）按钮
3. 选择 **Prompt...**
4. 从列表中选择需要的 prompt（如 `tdd-workflow`）
5. 在输入框中补充具体描述，发送即可

所有 prompt 均配置了 `agent: "agent"`，会以 Agent 模式运行（可读写文件、执行终端命令）。

### 可用 Prompt 速查

| Prompt | 用途 | 示例输入 |
|--------|------|----------|
| `tdd-workflow` | TDD 开发新功能或修复 Bug | "用 TDD 实现文章收藏功能" |
| `write-prd` | 编写产品需求文档 | "为会员等级体系编写 PRD" |
| `triage-issue` | Bug 根因分析 | "会员列表分页返回空数据" |
| `mortise-env-triage` | 环境/配置问题排障 | "mortise-app 启动失败 401" |

## 在 GitHub Copilot CLI 中使用

### 自动指令

CLI 启动时会自动加载以下位置的指令：

```
.github/copilot-instructions.md
.github/instructions/**/*.instructions.md
```

这些指令在每次对话中自动生效，覆盖模块边界、编码规范、权限约定等。

### Prompt 文件

CLI **不会**自动加载 `.github/prompts/` 目录。使用方式有两种：

**方式一：`@` 引用文件（推荐）**

```
@.github/prompts/tdd-workflow.prompt.md 帮我用 TDD 实现用户注册功能
```

CLI 会读取 prompt 文件内容作为上下文，并按其中的工作流指导执行。

**方式二：自然语言描述**

```
我想用 TDD 方式开发，参考项目的 TDD 工作流规范
```

由于 `copilot-instructions.md` 中已引用了所有 prompt 文件路径，CLI 可能会主动参考相关内容。

## 选择合适的工具

| 场景 | 推荐工具 |
|------|----------|
| 日常编码、小幅修改 | VS Code Copilot Chat（inline / panel） |
| 多文件重构、新功能开发 | VS Code Chat Agent 模式 或 Copilot CLI |
| 环境问题排查 | Copilot CLI（可直接执行终端命令） |
| 代码审查 | VS Code Chat + `/review` 或 CLI `/review` |

## 自定义与扩展

### 添加新的 Instruction

在 `.github/instructions/` 下创建 `*.instructions.md` 文件，frontmatter 中通过 `applyTo` 指定匹配模式：

```yaml
---
description: '描述'
applyTo: '**/*.java'
---
```

### 添加新的 Prompt

在 `.github/prompts/` 下创建 `*.prompt.md` 文件：

```yaml
---
description: "prompt 描述"
name: "prompt-name"
argument-hint: "输入提示"
agent: "agent"
---
```

建议遵循现有 prompt 的风格：使用简体中文、引用相关 instruction 文件、提供结构化的输出模板。
