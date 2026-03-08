#!/usr/bin/env node
/**
 * create-standalone.mjs — 独立部署应用一键生成脚本
 *
 * 基于 templates/standalone 模板，自动检测可用 Layer 并生成配置完备的独立应用。
 *
 * 用法：
 *   node scripts/create-standalone.mjs
 *   pnpm create:standalone
 */

import { createInterface } from 'node:readline'
import { existsSync, readdirSync, readFileSync, writeFileSync, cpSync, mkdirSync } from 'node:fs'
import { resolve, join } from 'node:path'
import { fileURLToPath } from 'node:url'

// ── 路径常量 ──────────────────────────────────────────────────────────────
const __dirname = fileURLToPath(new URL('.', import.meta.url))
const ROOT = resolve(__dirname, '..')
const LAYERS_DIR = resolve(ROOT, 'layers')
const APPS_DIR = resolve(ROOT, 'apps')
const TEMPLATE_DIR = resolve(ROOT, 'templates', 'standalone')

// ── Nuxt UI 可用主色 ─────────────────────────────────────────────────────
const COLORS = [
  'red', 'orange', 'amber', 'yellow', 'lime', 'green', 'emerald',
  'teal', 'cyan', 'sky', 'blue', 'indigo', 'violet', 'purple',
  'fuchsia', 'pink', 'rose',
]

// ── readline 工具 ─────────────────────────────────────────────────────────
const rl = createInterface({ input: process.stdin, output: process.stdout })

function ask(question) {
  return new Promise(resolve => rl.question(question, resolve))
}

function print(msg = '') {
  console.log(msg)
}

function printBanner() {
  print()
  print('╔══════════════════════════════════════════════╗')
  print('║   Mortise 独立部署应用生成器                 ║')
  print('║   基于 templates/standalone 模板自动创建     ║')
  print('╚══════════════════════════════════════════════╝')
  print()
}

// ── 自动检测可用 Layer ────────────────────────────────────────────────────

function detectLayers() {
  if (!existsSync(LAYERS_DIR)) return []

  const layers = []
  for (const entry of readdirSync(LAYERS_DIR, { withFileTypes: true })) {
    if (!entry.isDirectory()) continue
    if (entry.name === 'base') continue // 排除 base layer

    const pkgPath = join(LAYERS_DIR, entry.name, 'package.json')
    const nuxtCfg = join(LAYERS_DIR, entry.name, 'nuxt.config.ts')
    if (!existsSync(pkgPath) || !existsSync(nuxtCfg)) continue

    try {
      const pkg = JSON.parse(readFileSync(pkgPath, 'utf-8'))
      layers.push({
        dirName: entry.name,            // e.g. 'community'
        packageName: pkg.name || '',     // e.g. '@mortise/community-layer'
      })
    } catch {
      // 无效 package.json，跳过
    }
  }
  return layers
}

// ── 步骤 1：选择 Layer ────────────────────────────────────────────────────

async function stepSelectLayers(availableLayers) {
  print('📦 检测到以下可用 Layer：')
  print()
  availableLayers.forEach((l, i) => {
    print(`  [${i + 1}] ${l.packageName}  (layers/${l.dirName})`)
  })
  print()

  const input = await ask(`请选择要部署的 Layer（输入编号，多个用逗号分隔，如 1,2）: `)
  const indices = input.split(',').map(s => parseInt(s.trim(), 10) - 1)

  const selected = []
  for (const idx of indices) {
    if (idx >= 0 && idx < availableLayers.length) {
      selected.push(availableLayers[idx])
    }
  }

  if (selected.length === 0) {
    print('❌ 未选择任何有效 Layer，退出。')
    process.exit(1)
  }

  print()
  print(`✅ 已选择: ${selected.map(l => l.packageName).join(', ')}`)
  return selected
}

// ── 步骤 2：部署名称 ──────────────────────────────────────────────────────

async function stepAppName(selectedLayers) {
  const defaultName = selectedLayers.length === 1
    ? `my-${selectedLayers[0].dirName}`
    : 'my-app'

  print()
  const input = await ask(`请输入应用名称（用于 apps/<name> 目录和 @mortise/<name> 包名）[${defaultName}]: `)
  const appName = input.trim() || defaultName

  // 校验名称
  if (!/^[a-z0-9]([a-z0-9-]*[a-z0-9])?$/.test(appName)) {
    print('❌ 应用名称只能包含小写字母、数字和连字符，且不能以连字符开头或结尾。')
    process.exit(1)
  }

  const targetDir = resolve(APPS_DIR, appName)
  if (existsSync(targetDir)) {
    print(`❌ 目录 apps/${appName} 已存在，请换一个名称或先删除。`)
    process.exit(1)
  }

  print(`✅ 应用目录: apps/${appName}`)
  print(`   包名: @mortise/${appName}`)
  return appName
}

// ── 步骤 3：可选参数 ──────────────────────────────────────────────────────

async function stepOptionalParams(selectedLayers) {
  print()
  print('⚙️  可选参数（直接回车跳过使用默认值）：')
  print()

  // 端口
  const portInput = await ask('  开发服务器端口 [3002]: ')
  const port = portInput.trim() ? parseInt(portInput.trim(), 10) : 3002

  // 主色
  const colorInput = await ask(`  主题色 (${COLORS.join('/')}) [green]: `)
  const primaryColor = colorInput.trim() || 'green'
  if (!COLORS.includes(primaryColor)) {
    print(`  ⚠️  "${primaryColor}" 不在预设色值中，将直接使用（可能需要自定义）`)
  }

  // 应用显示名称
  const defaultDisplayName = selectedLayers.length === 1
    ? `Mortise ${selectedLayers[0].dirName.charAt(0).toUpperCase() + selectedLayers[0].dirName.slice(1)}`
    : 'Mortise'
  const displayNameInput = await ask(`  应用显示名称 [${defaultDisplayName}]: `)
  const displayName = displayNameInput.trim() || defaultDisplayName

  // 应用描述
  const defaultDesc = `${displayName} 独立应用`
  const descInput = await ask(`  应用描述 [${defaultDesc}]: `)
  const description = descInput.trim() || defaultDesc

  print()
  return { port, primaryColor, displayName, description }
}

// ── 步骤 4：生成应用 ──────────────────────────────────────────────────────

function generateApp(appName, selectedLayers, params) {
  const targetDir = resolve(APPS_DIR, appName)

  print('🚀 开始生成...')
  print()

  // 4.1 复制模板目录
  print('  📁 复制模板 templates/standalone → apps/' + appName)
  cpSync(TEMPLATE_DIR, targetDir, { recursive: true })

  // 4.2 修改 package.json
  print('  📝 配置 package.json')
  const pkgPath = join(targetDir, 'package.json')
  const pkg = JSON.parse(readFileSync(pkgPath, 'utf-8'))
  pkg.name = `@mortise/${appName}`
  for (const layer of selectedLayers) {
    pkg.dependencies[layer.packageName] = 'workspace:*'
  }
  writeFileSync(pkgPath, JSON.stringify(pkg, null, 2) + '\n', 'utf-8')

  // 4.3 修改 nuxt.config.ts
  print('  📝 配置 nuxt.config.ts')
  const nuxtCfgPath = join(targetDir, 'nuxt.config.ts')
  let nuxtCfg = readFileSync(nuxtCfgPath, 'utf-8')

  const layerExtends = ['\'@mortise/base-layer\'', ...selectedLayers.map(l => `'${l.packageName}'`)].join(', ')
  nuxtCfg = nuxtCfg.replace(
    /const LAYER_EXTENDS = \[.*?\]/,
    `const LAYER_EXTENDS = [${layerExtends}]`,
  )

  const routePrefixes = selectedLayers.map(l => `'${l.dirName}'`).join(', ')
  nuxtCfg = nuxtCfg.replace(
    /const ROUTE_PREFIXES = \[.*?\]\s*as\s*string\[\]/,
    `const ROUTE_PREFIXES = [${routePrefixes}]`,
  )
  // 如果替换掉了 as string[]，没有 as 也要处理
  nuxtCfg = nuxtCfg.replace(
    /const ROUTE_PREFIXES = \[.*?\](\s+\/\/)/,
    `const ROUTE_PREFIXES = [${routePrefixes}]$1`,
  )

  nuxtCfg = nuxtCfg.replace(
    /port:\s*\d+,/,
    `port: ${params.port},`,
  )

  writeFileSync(nuxtCfgPath, nuxtCfg, 'utf-8')

  // 4.4 修改 app/app.config.ts
  print('  📝 配置 app.config.ts')
  const appCfgPath = join(targetDir, 'app', 'app.config.ts')
  const basePathEntries = selectedLayers.map(l => `  ${l.dirName}: {\n    basePath: '',\n  },`).join('\n')

  const appCfgContent = `/**
 * ${params.displayName} 独立应用配置
 */
export default defineAppConfig({
${basePathEntries}
  ui: {
    colors: {
      primary: '${params.primaryColor}',
      neutral: 'zinc',
    },
  },
})
`
  writeFileSync(appCfgPath, appCfgContent, 'utf-8')

  // 4.5 修改 app/app.vue
  print('  📝 配置 app.vue')
  const appVuePath = join(targetDir, 'app', 'app.vue')
  let appVue = readFileSync(appVuePath, 'utf-8')
  appVue = appVue.replace(
    /const APP_NAME = '.*?'/,
    `const APP_NAME = '${params.displayName}'`,
  )
  appVue = appVue.replace(
    /const APP_DESC = '.*?'/,
    `const APP_DESC = '${params.description}'`,
  )
  writeFileSync(appVuePath, appVue, 'utf-8')

  // 4.6 修改 AppHeader.vue 品牌名
  print('  📝 配置 AppHeader.vue')
  const headerPath = join(targetDir, 'app', 'components', 'AppHeader.vue')
  let header = readFileSync(headerPath, 'utf-8')
  header = header.replace(
    /<span class="font-bold text-lg">.*?<\/span>/,
    `<span class="font-bold text-lg">${params.displayName}</span>`,
  )
  writeFileSync(headerPath, header, 'utf-8')

  // 4.7 修改 AppFooter.vue 品牌名
  print('  📝 配置 AppFooter.vue')
  const footerPath = join(targetDir, 'app', 'components', 'AppFooter.vue')
  let footer = readFileSync(footerPath, 'utf-8')
  footer = footer.replace(
    /Mortise • ©/,
    `${params.displayName} • ©`,
  )
  writeFileSync(footerPath, footer, 'utf-8')

  return targetDir
}

// ── 打印摘要 ──────────────────────────────────────────────────────────────

function printSummary(appName, selectedLayers, params) {
  print()
  print('╔══════════════════════════════════════════════╗')
  print('║   ✅ 应用创建成功！                          ║')
  print('╚══════════════════════════════════════════════╝')
  print()
  print(`  📁 目录:   apps/${appName}`)
  print(`  📦 包名:   @mortise/${appName}`)
  print(`  🔗 Layer:  ${selectedLayers.map(l => l.packageName).join(', ')}`)
  print(`  🎨 主题色: ${params.primaryColor}`)
  print(`  🌐 端口:   ${params.port}`)
  print()
  print('  后续步骤：')
  print()
  print('    # 安装依赖')
  print('    pnpm install')
  print()
  print('    # 启动开发服务器')
  print(`    pnpm --filter @mortise/${appName} dev`)
  print()
  print('    # 构建生产版本')
  print(`    pnpm --filter @mortise/${appName} build`)
  print()
}

// ── 主流程 ────────────────────────────────────────────────────────────────

async function main() {
  printBanner()

  // 校验模板存在
  if (!existsSync(TEMPLATE_DIR)) {
    print('❌ 模板目录 templates/standalone 不存在，请确认项目结构完整。')
    process.exit(1)
  }

  // 步骤 1：检测并选择 Layer
  const availableLayers = detectLayers()
  if (availableLayers.length === 0) {
    print('❌ 未检测到可用的业务 Layer（layers/ 下除 base 外无有效 Layer）。')
    print('   请先通过 git submodule 添加 Layer 再运行本脚本。')
    process.exit(1)
  }
  const selectedLayers = await stepSelectLayers(availableLayers)

  // 步骤 2：部署名称
  const appName = await stepAppName(selectedLayers)

  // 步骤 3：可选参数
  const params = await stepOptionalParams(selectedLayers)

  // 确认
  print('────────────────────────────────────────────────')
  print('  即将创建应用：')
  print(`    目录:    apps/${appName}`)
  print(`    Layer:   ${selectedLayers.map(l => l.packageName).join(', ')}`)
  print(`    前缀:    ${selectedLayers.map(l => '/' + l.dirName).join(', ')} → /`)
  print(`    端口:    ${params.port}`)
  print(`    主题色:  ${params.primaryColor}`)
  print(`    名称:    ${params.displayName}`)
  print('────────────────────────────────────────────────')
  print()

  const confirm = await ask('确认创建？(Y/n): ')
  if (confirm.trim().toLowerCase() === 'n') {
    print('已取消。')
    process.exit(0)
  }

  print()

  // 步骤 4：生成
  generateApp(appName, selectedLayers, params)

  // 完成摘要
  printSummary(appName, selectedLayers, params)

  rl.close()
}

main().catch((err) => {
  console.error('脚本执行失败:', err)
  rl.close()
  process.exit(1)
})
