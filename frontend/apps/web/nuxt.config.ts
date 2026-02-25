export default defineNuxtConfig({
  modules: ['@nuxt/eslint', '@nuxt/ui', '@pinia/nuxt'],
  devtools: { enabled: true },
  css: ['~/assets/css/main.css'],
  runtimeConfig: {
    public: {
      apiBase: '',
      auth: {
        loginPath: '/api/v1/app/auth/login',
        refreshPath: '/api/v1/app/auth/refresh-token',
        callbackPath: '/api/v1/app/oauth2/callback',
        mePath: '/api/v1/app/auth/profile',
        oauthAuthUrlPath: '/api/v1/app/oauth2/auth-url'
      }
    }
  },
  compatibilityDate: '2024-07-11'
})
