export default defineNuxtConfig({
  extends: ['../../packages/nuxt-layer'],

  modules: ['@nuxt/eslint', '@nuxt/ui', '@pinia/nuxt', '@nuxt/content', '@nuxt/image'],
  
  devtools: { enabled: true },

  ssr: true,

  css: ['~/assets/css/main.css'],

  ui: {
    fonts: false
  },

  runtimeConfig: {
    public: {
      // 优先用环境变量，默认本地开发地址
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:9999/mortise',
      // web 用户端地址，生产环境通过 NUXT_PUBLIC_WEB_URL 覆盖
      webUrl: process.env.NUXT_PUBLIC_WEB_URL || 'http://localhost:3000'
    }
  },

  devServer: {
    port: 3002
  },

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

  mdc: {
    highlight: {
      noApiRoute: false,
      langs: ['diff', 'ts', 'vue', 'css', 'java', 'xml', 'yaml', 'json', 'bash', 'sql']
    }
  },

  compatibilityDate: '2025-01-15',

  eslint: {
    config: {
      stylistic: {
        commaDangle: 'never',
        braceStyle: '1tbs'
      }
    }
  }
})
