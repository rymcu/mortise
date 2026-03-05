import { defineStore } from 'pinia'
import { createAuthClient, unwrapGlobalResult, type AuthSession } from '@mortise/auth'

interface WebLoginResponse {
  memberId?: number
  username?: string
  nickname?: string
  avatarUrl?: string
  token?: string
  refreshToken?: string
  tokenType?: string
  expiresIn?: number
  refreshExpiresIn?: number
}

interface FetchCurrentUserOptions {
  noRetryOnUnauthorized?: boolean
}

/**
 * Cookie 存储的认证 store（SSR 安全）
 *
 * 使用 useCookie 替代 localStorage，保证刷新页面时服务端也能读取到 token，
 * 不再被中间件重定向到登录页。
 */
export const useAuthStore = defineStore('web-auth', () => {
  // ─── Cookie 持久化（SSR 安全） ───
  const tokenCookie = useCookie<string | null>('mortise-web-token', {
    sameSite: 'lax',
    maxAge: 7 * 24 * 60 * 60 // 7 天
  })
  const refreshTokenCookie = useCookie<string | null>('mortise-web-refresh-token', {
    sameSite: 'lax',
    maxAge: 30 * 24 * 60 * 60 // 30 天
  })
  const tokenTypeCookie = useCookie<string | null>('mortise-web-token-type', {
    sameSite: 'lax',
    maxAge: 7 * 24 * 60 * 60
  })
  const userCookie = useCookie<Record<string, unknown> | null>('mortise-web-user', {
    sameSite: 'lax',
    maxAge: 7 * 24 * 60 * 60
  })

  // ─── 响应式状态 ───
  const loading = ref(false)
  const refreshPromise = shallowRef<Promise<AuthSession | null> | null>(null)

  // ─── Getters ───
  const isAuthenticated = computed(() => Boolean(tokenCookie.value))

  const accessToken = computed(() => tokenCookie.value ?? '')

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

  function saveTokens(payload: WebLoginResponse) {
    if (!payload?.token) {
      throw new Error('登录响应缺少 token')
    }
    tokenCookie.value = payload.token
    refreshTokenCookie.value = payload.refreshToken ?? null
    tokenTypeCookie.value = payload.tokenType ?? 'Bearer'
    userCookie.value = {
      memberId: payload.memberId,
      username: payload.username,
      nickname: payload.nickname,
      avatarUrl: payload.avatarUrl
    }
  }

  function clearTokens() {
    tokenCookie.value = null
    refreshTokenCookie.value = null
    tokenTypeCookie.value = null
    userCookie.value = null
  }

  function setSessionUser(user: Record<string, unknown> | null) {
    userCookie.value = user
  }

  // ─── 兼容旧接口 ───

  /** 兼容旧调用（cookie 始终可读，无需手动恢复） */
  function restore() {
    /* no-op: cookie 在 SSR/CSR 均自动可用 */
  }

  /** 兼容旧调用 */
  function persist() {
    /* no-op: cookie 自动持久化 */
  }

  // ─── Actions ───

  async function login(account: string, password: string) {
    loading.value = true
    try {
      const payload = await buildClient().login<WebLoginResponse>({ account, password })
      saveTokens(payload)
      await fetchCurrentUser()
    }
    finally {
      loading.value = false
    }
  }

  async function exchangeOAuthState(state: string) {
    const payload = await buildClient().callback<WebLoginResponse>(state)
    saveTokens(payload)
    await fetchCurrentUser()
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
        const payload = await buildClient().refresh<WebLoginResponse>(
          refreshTokenCookie.value!
        )
        saveTokens(payload)
        return session.value
      }
      catch {
        logout()
        return null
      }
      finally {
        refreshPromise.value = null
      }
    })()

    return refreshPromise.value
  }

  async function startOAuthLogin(registrationId: string) {
    const config = useRuntimeConfig()
    const endpoint = `${config.public.auth.oauthAuthUrlPath}/${encodeURIComponent(registrationId)}`
    const response = await fetch(`${config.public.apiBase}${endpoint}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })

    const data = await unwrapGlobalResult<{ authorizationUrl: string }>(response)

    if (!data?.authorizationUrl) {
      throw new Error('未获取到 OAuth2 授权地址')
    }

    await navigateTo(data.authorizationUrl, { external: true })
  }

  /** 从后端刷新当前用户信息至 cookie */
  async function fetchCurrentUser(options: FetchCurrentUserOptions = {}) {
    if (!isAuthenticated.value) return null

    const { $api } = useNuxtApp()
    try {
      const requestOptions = options.noRetryOnUnauthorized
        ? { _retried: true }
        : undefined
      const res = await $api<{ code: number; data?: Record<string, unknown> }>(
        '/api/v1/app/auth/profile',
        requestOptions
      )
      const remoteUser = res?.data
      if (!remoteUser) return null

      const merged = {
        ...(userCookie.value ?? {}),
        ...remoteUser
      }
      userCookie.value = merged
      return merged
    }
    catch {
      return null
    }
  }

  /**
   * 刷新页面后重建 pinia 运行时状态：
   * 刷新后端用户信息（确保 cookie 中的用户数据最新）
   */
  async function restoreSession() {
    if (!isAuthenticated.value) return
    await fetchCurrentUser({ noRetryOnUnauthorized: false })
  }

  function logout() {
    clearTokens()
    // 同时清理旧 localStorage 数据（如果存在）
    if (import.meta.client && typeof localStorage !== 'undefined') {
      localStorage.removeItem('mortise.auth.session')
    }
  }

  return {
    // State
    loading,
    session,
    refreshPromise,
    // Getters
    isAuthenticated,
    accessToken,
    authHeader,
    // Actions
    restore,
    persist,
    login,
    exchangeOAuthState,
    refresh,
    startOAuthLogin,
    fetchCurrentUser,
    restoreSession,
    setSessionUser,
    logout,
    buildClient
  }
})
