export default defineNuxtConfig({
  extends: ['../../packages/nuxt-layer'],

  modules: ['@nuxt/eslint', '@nuxt/ui', '@pinia/nuxt'],
  devtools: { enabled: true },
  css: ['~/assets/css/main.css'],
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

  compatibilityDate: '2025-01-15'
})
