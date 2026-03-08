import { defineStore } from 'pinia'
import { createAdminAuthStore } from '@mortise/auth'
import type { AuthApiInvoker } from '@mortise/auth'
import {
  fetchAdminAuthMenus,
  fetchAdminCurrentUser,
  type AdminMenuLink,
} from '@mortise/core-sdk'

/** 后端菜单 Link 结构 */
export type MenuLink = AdminMenuLink

const setupAdminAuthStore = createAdminAuthStore<MenuLink>({
  fetchCurrentUserRemote: fetchAdminCurrentUser,
  fetchMenusRemote: fetchAdminAuthMenus
})

/**
 * Cookie 存储的管理端认证 store（SSR 安全）
 *
 * 参考 old-code/design-mobile 的 useToken 方案，使用 useCookie 替代 localStorage，
 * 保证刷新页面时服务端也能读取到 token，不再被中间件重定向到登录页。
 */
export const useAuthStore = defineStore(
  'admin-auth',
  (): ReturnType<typeof setupAdminAuthStore> =>
    setupAdminAuthStore({
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
