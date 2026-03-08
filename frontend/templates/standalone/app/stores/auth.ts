import { defineStore } from 'pinia'
import { createWebAuthStore } from '@mortise/auth'
import type { AuthApiInvoker } from '@mortise/auth'
import { fetchSiteCurrentUser } from '@mortise/core-sdk'

const setupWebAuthStore = createWebAuthStore({
  fetchCurrentUserRemote: fetchSiteCurrentUser
})

/**
 * Cookie 存储的认证 store（SSR 安全）
 *
 * 使用 useCookie 替代 localStorage，保证刷新页面时服务端也能读取到 token，
 * 不再被中间件重定向到登录页。
 */
export const useAuthStore = defineStore('web-auth', (): ReturnType<typeof setupWebAuthStore> =>
  setupWebAuthStore({
    createCookie: useCookie,
    getRuntimeConfig: useRuntimeConfig,
    getApi: (): AuthApiInvoker => useNuxtApp().$api as AuthApiInvoker,
    navigateTo: async (url, options) => {
      await navigateTo(url, options)
    },
    clearLegacySession: () => {
      if (import.meta.client && typeof localStorage !== 'undefined') {
        localStorage.removeItem('mortise.auth.session')
      }
    }
  })
)
