import { existsSync, readdirSync, readFileSync } from 'node:fs'
import { resolve, join } from 'node:path'

// ── Layer 自动扫描 ───────────────────────────────────────────────────────
// 约定：frontend/layers/ 下所有含 package.json + nuxt.config.ts 的子目录均为 Nuxt Layer。
// 基础层 (base) 始终接入，可选层 (如 community、commerce) 仅在 pnpm install 完成后自动激活。
// 新增模块无需修改本文件。
const appRoot = __dirname
const layersRoot = resolve(__dirname, '../../layers')
const localLayersBase = '../../layers'
const appPkg = JSON.parse(readFileSync(resolve(appRoot, 'package.json'), 'utf-8')) as {
    dependencies?: Record<string, string>
    devDependencies?: Record<string, string>
    optionalDependencies?: Record<string, string>
}
const declaredLayerDeps = new Set([
    ...Object.keys(appPkg.dependencies ?? {}),
    ...Object.keys(appPkg.devDependencies ?? {}),
    ...Object.keys(appPkg.optionalDependencies ?? {})
])

const layers: string[] = []
if (existsSync(layersRoot)) {
    for (const entry of readdirSync(layersRoot, { withFileTypes: true })) {
        if (!entry.isDirectory()) continue
        const pkgPath = join(layersRoot, entry.name, 'package.json')
        const nuxtCfg = join(layersRoot, entry.name, 'nuxt.config.ts')
        if (!existsSync(pkgPath) || !existsSync(nuxtCfg)) continue
        try {
            const pkg = JSON.parse(readFileSync(pkgPath, 'utf-8'))
            if (pkg.name) {
                const isBaseLayer = entry.name === 'base'
                const isDeclared = declaredLayerDeps.has(pkg.name)
                const isInstalled = existsSync(resolve(appRoot, 'node_modules', ...pkg.name.split('/')))
                if (!isBaseLayer && !(isDeclared && isInstalled)) continue
                // 使用相对路径，让 Nuxt 将其识别为本地 layer，正确生成 composable auto-import
                layers.push(join(localLayersBase, entry.name).replace(/\\/g, '/'))
            }
        } catch {
            // Layer 未安装或 submodule 未初始化，跳过
        }
    }
}

export default defineNuxtConfig({
    extends: layers,

    modules: ['@nuxt/eslint', '@nuxt/ui', '@pinia/nuxt', '@nuxt/content', '@nuxt/image'],

    ssr: true,

    devtools: {enabled: true},

    css: ['~/assets/css/main.css'],

    mdc: {
        highlight: {
            noApiRoute: false,
            langs: ['diff', 'ts', 'vue', 'css', 'java', 'xml', 'yaml', 'json', 'bash', 'sql']
        }
    },

    ui: {
        fonts: false
    },

    runtimeConfig: {
        public: {
            // 优先用环境变量，默认本地开发地址
            apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:9999/mortise',
            auth: {
                loginPath: '/api/v1/app/auth/login',
                refreshPath: '/api/v1/app/auth/refresh-token',
                callbackPath: '/api/v1/app/oauth2/callback',
                mePath: '/api/v1/app/auth/profile',
                oauthAuthUrlPath: '/api/v1/app/oauth2/auth-url'
            }
        }
    },

    devServer: {
        port: 3001
    },

    compatibilityDate: '2025-01-15',

    vite: {
        optimizeDeps: {
            exclude: ['@nuxtjs/mdc']
        },
        server: {
            proxy: {
                '/mortise': {
                    target: 'http://localhost:9999',
                    changeOrigin: true
                }
            }
        }
    },

    eslint: {
        config: {
            stylistic: {
                commaDangle: 'never',
                braceStyle: '1tbs'
            }
        }
    },
})
