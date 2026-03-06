#!/usr/bin/env node
/**
 * layer.mjs — Layer 依赖管理脚本
 *
 * 用法：
 *   node scripts/layer.mjs add <layerName>      # 添加 layer 到 site
 *   node scripts/layer.mjs remove <layerName>   # 从 site 移除 layer
 *   node scripts/layer.mjs list                 # 列出可用 layer
 *
 *   # 指定目标应用（默认 site）
 *   node scripts/layer.mjs add community --app my-shop
 *
 * pnpm 快捷命令（在 package.json scripts 中注册）：
 *   pnpm layer:add community
 *   pnpm layer:remove community
 *   pnpm layer:list
 */

import { existsSync, readdirSync, readFileSync, writeFileSync } from 'node:fs'
import { resolve, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { execSync } from 'node:child_process'

// ── 路径常量 ──────────────────────────────────────────────────────────────
const __dirname = fileURLToPath(new URL('.', import.meta.url))
const ROOT = resolve(__dirname, '..')
const LAYERS_DIR = resolve(ROOT, 'layers')
const APPS_DIR = resolve(ROOT, 'apps')

// ── 工具函数 ──────────────────────────────────────────────────────────────

function print(msg = '') {
  console.log(msg)
}

function error(msg) {
  console.error(`\x1b[31m✗ ${msg}\x1b[0m`)
  process.exit(1)
}

function success(msg) {
  console.log(`\x1b[32m✓ ${msg}\x1b[0m`)
}

function info(msg) {
  console.log(`\x1b[36mℹ ${msg}\x1b[0m`)
}

/**
 * 解析命令行参数
 * node scripts/layer.mjs <command> [layerName] [--app <appName>]
 */
function parseArgs() {
  const args = process.argv.slice(2)
  const command = args[0]
  let layerName = args[1] && !args[1].startsWith('--') ? args[1] : null
  const appIndex = args.indexOf('--app')
  const appName = appIndex !== -1 ? args[appIndex + 1] : 'site'
  return { command, layerName, appName }
}

/**
 * 将 layer 目录名转为包名，支持两种输入：
 *   'community'              → '@mortise/community-layer'
 *   '@mortise/community-layer' → '@mortise/community-layer'
 */
function resolvePackageName(input) {
  if (!input) return null
  if (input.startsWith('@mortise/') && input.endsWith('-layer')) return input
  return `@mortise/${input}-layer`
}

/**
 * 将 layer 包名转为目录名：
 *   '@mortise/community-layer' → 'community'
 *   'community'                → 'community'
 */
function resolveDirName(input) {
  if (!input) return null
  if (input.startsWith('@mortise/') && input.endsWith('-layer')) {
    return input.replace('@mortise/', '').replace('-layer', '')
  }
  return input
}

// ── 读写 package.json ─────────────────────────────────────────────────────

function readPkg(pkgPath) {
  try {
    return JSON.parse(readFileSync(pkgPath, 'utf-8'))
  } catch {
    error(`无法读取 ${pkgPath}`)
  }
}

function writePkg(pkgPath, pkg) {
  writeFileSync(pkgPath, JSON.stringify(pkg, null, 2) + '\n', 'utf-8')
}

// ── 命令实现 ──────────────────────────────────────────────────────────────

/**
 * list：列出 layers/ 中所有可用 layer（排除 base）
 */
function cmdList() {
  if (!existsSync(LAYERS_DIR)) {
    error('layers/ 目录不存在')
  }

  const layers = []
  for (const entry of readdirSync(LAYERS_DIR, { withFileTypes: true })) {
    if (!entry.isDirectory() || entry.name === 'base') continue
    const pkgPath = join(LAYERS_DIR, entry.name, 'package.json')
    const nuxtCfg = join(LAYERS_DIR, entry.name, 'nuxt.config.ts')
    if (!existsSync(pkgPath) || !existsSync(nuxtCfg)) continue
    try {
      const pkg = JSON.parse(readFileSync(pkgPath, 'utf-8'))
      layers.push({ dir: entry.name, packageName: pkg.name || `@mortise/${entry.name}-layer` })
    } catch {
      // skip
    }
  }

  if (layers.length === 0) {
    info('暂无可用的业务 Layer（layers/ 下除 base 外无有效 Layer）')
    return
  }

  print()
  print('可用 Layer：')
  print()
  for (const l of layers) {
    print(`  ${l.packageName.padEnd(36)}  layers/${l.dir}`)
  }
  print()
  print('添加命令示例：')
  print(`  pnpm layer:add ${layers[0].dir}`)
}

/**
 * add：向指定 app 的 package.json 添加 layer 依赖
 */
function cmdAdd(layerName, appName) {
  const packageName = resolvePackageName(layerName)
  const dirName = resolveDirName(layerName)

  // 校验 layer 目录存在
  const layerDir = join(LAYERS_DIR, dirName)
  if (!existsSync(join(layerDir, 'package.json')) || !existsSync(join(layerDir, 'nuxt.config.ts'))) {
    error(`Layer "${dirName}" 不存在（${layerDir} 缺少 package.json 或 nuxt.config.ts）\n  请先通过 git submodule 添加该 Layer 再运行本命令`)
  }

  // 读取目标 app 的 package.json
  const appPkgPath = join(APPS_DIR, appName, 'package.json')
  if (!existsSync(appPkgPath)) {
    error(`应用 "${appName}" 不存在（${appPkgPath} 未找到）`)
  }

  const pkg = readPkg(appPkgPath)

  // 已存在则跳过
  if (pkg.dependencies?.[packageName]) {
    info(`${packageName} 已存在于 apps/${appName}/package.json，无需重复添加`)
    return
  }

  // 添加到 dependencies（保持字母排序）
  pkg.dependencies = pkg.dependencies || {}
  pkg.dependencies[packageName] = 'workspace:*'

  // 按 key 排序，让 @mortise/* 集中在一起
  const sorted = Object.fromEntries(Object.entries(pkg.dependencies).sort(([a], [b]) => a.localeCompare(b)))
  pkg.dependencies = sorted

  writePkg(appPkgPath, pkg)
  success(`已将 ${packageName} 添加到 apps/${appName}/package.json`)

  // 自动执行 pnpm install
  print()
  info('正在执行 pnpm install...')
  try {
    execSync('pnpm install', { cwd: ROOT, stdio: 'inherit' })
    print()
    success(`Layer "${dirName}" 已激活！重启开发服务器后生效。`)
    print()
    print(`  访问路径: http://localhost:<port>/${dirName}`)
    print()
  } catch {
    error('pnpm install 失败，请手动执行')
  }
}

/**
 * remove：从指定 app 的 package.json 移除 layer 依赖
 */
function cmdRemove(layerName, appName) {
  const packageName = resolvePackageName(layerName)
  const dirName = resolveDirName(layerName)

  const appPkgPath = join(APPS_DIR, appName, 'package.json')
  if (!existsSync(appPkgPath)) {
    error(`应用 "${appName}" 不存在（${appPkgPath} 未找到）`)
  }

  const pkg = readPkg(appPkgPath)

  // 不存在则跳过
  if (!pkg.dependencies?.[packageName]) {
    info(`${packageName} 不在 apps/${appName}/package.json 中，无需移除`)
    return
  }

  delete pkg.dependencies[packageName]
  writePkg(appPkgPath, pkg)
  success(`已从 apps/${appName}/package.json 移除 ${packageName}`)

  // 自动执行 pnpm install
  print()
  info('正在执行 pnpm install...')
  try {
    execSync('pnpm install', { cwd: ROOT, stdio: 'inherit' })
    print()
    success(`Layer "${dirName}" 已停用！重启开发服务器后生效。`)
    print()
  } catch {
    error('pnpm install 失败，请手动执行')
  }
}

// ── 主流程 ────────────────────────────────────────────────────────────────

const { command, layerName, appName } = parseArgs()

switch (command) {
  case 'list':
    cmdList()
    break

  case 'add':
    if (!layerName) error('请提供 layer 名称，例如：pnpm layer:add community')
    cmdAdd(layerName, appName)
    break

  case 'remove':
  case 'rm':
    if (!layerName) error('请提供 layer 名称，例如：pnpm layer:remove community')
    cmdRemove(layerName, appName)
    break

  default:
    print()
    print('用法：')
    print('  pnpm layer:list                         # 列出可用 layer')
    print('  pnpm layer:add <layerName>              # 添加 layer 到 site 应用')
    print('  pnpm layer:remove <layerName>           # 从 site 应用移除 layer')
    print('  pnpm layer:add <layerName> --app <app>  # 指定目标应用')
    print()
    print('示例：')
    print('  pnpm layer:add community')
    print('  pnpm layer:add commerce')
    print('  pnpm layer:remove community')
    print('  pnpm layer:add community --app my-shop')
    print()
    break
}
