/**
 * 独立部署应用 — 通用模板
 *
 * 将某个 Nuxt Layer 的页面独立部署为根路径应用，
 * 无需官网（site）即可单独运行。
 *
 * ── 使用方式 ──────────────────────────────────────
 * 1. cp -r templates/standalone apps/<your-app>
 * 2. 修改 package.json 的 name 和 layer 依赖
 * 3. 修改 app/app.config.ts 中的 basePath 覆盖
 * 4. 按需修改 app/app.vue、AppHeader.vue 中的品牌信息
 * 5. 如需修改开发端口，调整 devServer.port
 * ─────────────────────────────────────────────────
 */
import { resolve } from 'node:path'
import { resolveAppLayerEntries } from '../../scripts/layer-discovery.mjs'

const appRoot = __dirname
const layersRoot = resolve(__dirname, '../../layers')
const layerEntries = resolveAppLayerEntries({
  appRoot,
  layersRoot,
  localLayersBase: '../../layers',
  appKind: 'site'
})
const layers = layerEntries.map(entry => entry.localPath)
const routePrefixes = layerEntries
  .filter(entry => !entry.isBase)
  .map(entry => entry.routePrefix)
  .filter(Boolean)

export default defineNuxtConfig({
  extends: layers,

  modules: ['@nuxt/eslint', '@nuxt/ui', '@pinia/nuxt', '@nuxt/content', '@nuxt/image'],

  ssr: true,

  devtools: { enabled: true },

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
      apiBase:
        process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:9999/mortise',
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
    port: 3002 // ★ 按需修改端口
  },

  compatibilityDate: '2025-01-15',

  vite: {
    ssr: {
      external: ['isomorphic-dompurify']
    },
    optimizeDeps: {
      exclude: ['@nuxtjs/mdc'],
      include: ['extend']
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

  /**
   * 路由重写：将 Layer 页面的前缀路由提升到根路径。
   * 依据已启用 Layer 自动推导的前缀处理，无需手动编写。
   */
  hooks: {
    'pages:extend'(pages) {
      if (routePrefixes.length) {
        liftLayerRoutes(pages, routePrefixes)
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
  }
})

type NuxtPage = { path: string, children?: NuxtPage[] }

/**
 * 递归重写路由路径：去除指定前缀，将 Layer 页面提升到根路径。
 * 例如已启用 community-layer 时：
 *   /community       → /
 *   /community/topic  → /topic
 */
function liftLayerRoutes(pages: NuxtPage[], prefixes: string[]) {
  for (const page of pages) {
    for (const prefix of prefixes) {
      const full = `/${prefix}`
      if (page.path === full) {
        page.path = '/'
      } else if (page.path.startsWith(`${full}/`)) {
        page.path = page.path.replace(full, '')
      }
    }
    if (page.children?.length) {
      liftLayerRoutes(page.children, prefixes)
    }
  }
}
