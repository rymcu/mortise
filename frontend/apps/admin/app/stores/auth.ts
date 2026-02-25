import { defineStore } from 'pinia'
import { createAuthClient, type AuthSession } from '@mortise/auth'

interface AdminLoginResponse {
  id?: number
  account?: string
  token?: string
  refreshToken?: string
}

/** 后端菜单 Link 结构 */
export interface MenuLink {
  id: string
  label: string
  icon?: string
  to?: string
  status?: number
  sortNo?: number
  parentId?: string | null
  tooltip?: string | null
  children?: MenuLink[]
  defaultOpen?: boolean
}

/**
 * Cookie 存储的管理端认证 store（SSR 安全）
 *
 * 参考 old-code/design-mobile 的 useToken 方案，使用 useCookie 替代 localStorage，
 * 保证刷新页面时服务端也能读取到 token，不再被中间件重定向到登录页。
 */
export const useAuthStore = defineStore('admin-auth', () => {
  // ─── Cookie 持久化（SSR 安全） ───
  const tokenCookie = useCookie<string | null>('mortise-admin-token', {
    sameSite: 'lax',
    maxAge: 7 * 24 * 60 * 60 // 7 天
  })
  const refreshTokenCookie = useCookie<string | null>('mortise-admin-refresh-token', {
    sameSite: 'lax',
    maxAge: 30 * 24 * 60 * 60 // 30 天
  })
  const tokenTypeCookie = useCookie<string | null>('mortise-admin-token-type', {
    sameSite: 'lax',
    maxAge: 7 * 24 * 60 * 60
  })
  const userCookie = useCookie<Record<string, unknown> | null>('mortise-admin-user', {
    sameSite: 'lax',
    maxAge: 7 * 24 * 60 * 60
  })

  // ─── 响应式状态 ───
  const loading = ref(false)
  const refreshPromise = shallowRef<Promise<AuthSession | null> | null>(null)
  const userMenus = ref<MenuLink[]>([])

  // ─── Getters ───
  const isAuthenticated = computed(() => Boolean(tokenCookie.value))

  const authHeader = computed(() => {
    if (!tokenCookie.value) return ''
    return `${tokenTypeCookie.value ?? 'Bearer'} ${tokenCookie.value}`
  })

  /** 兼容旧接口，返回只读 session 对象 */
  const session = computed<AuthSession | null>(() => {
    if (!tokenCookie.value) return null
    return {
      token: tokenCookie.value,
      refreshToken: refreshTokenCookie.value ?? undefined,
      tokenType: tokenTypeCookie.value ?? 'Bearer',
      user: userCookie.value ?? undefined
    }
  })

  // ─── 内部工具 ───
  function buildClient() {
    const config = useRuntimeConfig()
    return createAuthClient({
      baseURL: config.public.apiBase,
      endpoints: {
        loginPath: config.public.auth.loginPath,
        refreshPath: config.public.auth.refreshPath,
        callbackPath: config.public.auth.callbackPath,
        mePath: config.public.auth.mePath
      }
    })
  }

  function saveTokens(payload: AdminLoginResponse) {
    if (!payload?.token) {
      throw new Error('登录响应缺少 token')
    }
    tokenCookie.value = payload.token
    refreshTokenCookie.value = payload.refreshToken ?? null
    tokenTypeCookie.value = 'Bearer'
    userCookie.value = { id: payload.id, account: payload.account }
  }

  function clearTokens() {
    tokenCookie.value = null
    refreshTokenCookie.value = null
    tokenTypeCookie.value = null
    userCookie.value = null
  }

  // ─── Actions ───

  /** 兼容旧调用（cookie 始终可读，无需手动恢复） */
  function restore() { /* no-op: cookie 在 SSR/CSR 均自动可用 */ }

  /** 兼容旧调用 */
  function persist() { /* no-op: cookie 自动持久化 */ }

  async function login(account: string, password: string) {
    loading.value = true
    try {
      const payload = await buildClient().login<AdminLoginResponse>({ account, password })
      saveTokens(payload)
    } finally {
      loading.value = false
    }
  }

  async function exchangeOAuthState(state: string) {
    const payload = await buildClient().callback<AdminLoginResponse>(state)
    saveTokens(payload)
  }

  async function refresh(): Promise<AuthSession | null> {
    if (!refreshTokenCookie.value) {
      logout()
      return null
    }

    // 防止并发刷新
    if (refreshPromise.value) {
      return refreshPromise.value
    }

    refreshPromise.value = (async () => {
      try {
        const payload = await buildClient().refresh<AdminLoginResponse>(refreshTokenCookie.value!)
        saveTokens(payload)
        return session.value
      } catch {
        logout()
        return null
      } finally {
        refreshPromise.value = null
      }
    })()

    return refreshPromise.value
  }

  async function startOAuthLogin(registrationId: string) {
    const config = useRuntimeConfig()
    const base = config.public.auth.oauthAuthorizeBasePath
    const url = `${config.public.apiBase}${base}/${encodeURIComponent(registrationId)}`
    await navigateTo(url, { external: true })
  }

  /** 从后端加载当前用户的菜单树 */
  async function fetchMenus() {
    if (!isAuthenticated.value) return
    const { $api } = useNuxtApp()
    try {
      const res = await $api<{ code: number; data: MenuLink[] }>('/api/v1/admin/auth/menus')
      userMenus.value = res?.data ?? []
    } catch {
      userMenus.value = []
    }
  }

  function logout() {
    clearTokens()
    userMenus.value = []
    // 同时清理旧 localStorage 数据（如果存在）
    if (import.meta.client && typeof localStorage !== 'undefined') {
      localStorage.removeItem('mortise.auth.session')
    }
  }

  return {
    // State
    loading,
    session,
    userMenus,
    refreshPromise,
    // Getters
    isAuthenticated,
    authHeader,
    // Actions
    restore,
    persist,
    login,
    exchangeOAuthState,
    refresh,
    startOAuthLogin,
    fetchMenus,
    logout,
    buildClient
  }
})
