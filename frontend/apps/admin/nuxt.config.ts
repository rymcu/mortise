import { resolve } from 'node:path'
import { resolveAppLayerExtends } from '../../scripts/layer-discovery.mjs'

// https://nuxt.com/docs/api/configuration/nuxt-config
const appRoot = __dirname
const layersRoot = resolve(__dirname, '../../layers')
const layers = resolveAppLayerExtends({
  appRoot,
  layersRoot,
  localLayersBase: '../../layers',
  appKind: 'admin'
})

export default defineNuxtConfig({
  extends: layers,
  modules: ['@nuxt/eslint', '@nuxt/ui', '@pinia/nuxt', '@vueuse/nuxt'],

  ssr: false,

  devtools: {
    enabled: true
  },

  app: {
    baseURL: '/admin/'
  },

  css: ['~/assets/css/main.css'],

  ui: {
    fonts: false
  },

  runtimeConfig: {
    public: {
      // 优先用环境变量；开发时使用相对路径 /mortise，经由 Vite 代理转发（消除 CORS）
      // 生产部署时必须通过 NUXT_PUBLIC_API_BASE 指定后端完整地址，如：
      //   NUXT_PUBLIC_API_BASE=https://api.example.com/mortise
      apiBase: process.env.NUXT_PUBLIC_API_BASE ?? '/mortise',
      auth: {
        loginPath: '/api/v1/admin/auth/login',
        refreshPath: '/api/v1/admin/auth/refresh-token',
        callbackPath: '/api/v1/admin/auth/callback',
        mePath: '/api/v1/admin/auth/me',
        oauthAuthorizeBasePath: '/oauth2/authorization'
      }
    }
  },

  routeRules: {
    '/api/**': {
      cors: true
    }
  },

  compatibilityDate: '2025-01-15',

  /**
   * 开发时通过 Vite 代理将 /mortise/** 请求转发到后端，避免浏览器 CORS 限制。
   * 生产环境无此代理，需通过 nginx 或 NUXT_PUBLIC_API_BASE 配置后端地址。
   */
  vite: {
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
  }
})
