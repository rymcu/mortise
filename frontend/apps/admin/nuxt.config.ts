// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
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
      apiBase: 'http://localhost:9999/mortise',
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

  compatibilityDate: '2024-07-11',

  eslint: {
    config: {
      stylistic: {
        commaDangle: 'never',
        braceStyle: '1tbs'
      }
    }
  }
})
