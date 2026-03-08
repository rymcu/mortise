import { computed, ref, shallowRef } from 'vue'
import { createAuthClient } from './client'
import type { AuthSession } from './types'
import type { AuthApiInvoker, WebAuthStoreRuntime } from './web-store'

interface CookieRefLike<T> {
  value: T
}

interface CookieOptionsLike {
  sameSite?: 'lax' | 'strict' | 'none'
  maxAge?: number
}

interface AdminAuthRuntimeConfigLike {
  public: {
    apiBase: string
    auth: {
      loginPath: string
      refreshPath: string
      callbackPath: string
      mePath?: string
      oauthAuthorizeBasePath: string
    }
  }
}

export interface AdminLoginResponse {
  id?: number
  account?: string
  token?: string
  refreshToken?: string
}

export interface AdminFetchCurrentUserOptions {
  noRetryOnUnauthorized?: boolean
}

export interface AdminAuthStoreRuntime {
  createCookie: <T>(
    name: string,
    options?: CookieOptionsLike,
  ) => CookieRefLike<T>
  getRuntimeConfig: () => AdminAuthRuntimeConfigLike
  getApi: () => AuthApiInvoker
  navigateTo: WebAuthStoreRuntime['navigateTo']
  clearLegacySession: () => void
}

export interface CreateAdminAuthStoreOptions<TMenu> {
  cookiePrefix?: string
  fetchCurrentUserRemote: (
    api: AuthApiInvoker,
    options?: Record<string, unknown>,
  ) => Promise<Record<string, unknown> | null>
  fetchMenusRemote: (api: AuthApiInvoker) => Promise<TMenu[]>
}

function buildCookieName(prefix: string, suffix: string) {
  return `${prefix}-${suffix}`
}

export function createAdminAuthStore<TMenu>(
  options: CreateAdminAuthStoreOptions<TMenu>,
) {
  const cookiePrefix = options.cookiePrefix ?? 'mortise-admin'

  return function setupAdminAuthStore(runtime: AdminAuthStoreRuntime) {
    const tokenCookie = runtime.createCookie<string | null>(
      buildCookieName(cookiePrefix, 'token'),
      {
        sameSite: 'lax',
        maxAge: 7 * 24 * 60 * 60,
      },
    )
    const refreshTokenCookie = runtime.createCookie<string | null>(
      buildCookieName(cookiePrefix, 'refresh-token'),
      {
        sameSite: 'lax',
        maxAge: 30 * 24 * 60 * 60,
      },
    )
    const tokenTypeCookie = runtime.createCookie<string | null>(
      buildCookieName(cookiePrefix, 'token-type'),
      {
        sameSite: 'lax',
        maxAge: 7 * 24 * 60 * 60,
      },
    )
    const userCookie = runtime.createCookie<Record<string, unknown> | null>(
      buildCookieName(cookiePrefix, 'user'),
      {
        sameSite: 'lax',
        maxAge: 7 * 24 * 60 * 60,
      },
    )

    const loading = ref(false)
    const refreshPromise = shallowRef<Promise<AuthSession | null> | null>(null)
    const userMenus = ref<TMenu[]>([])

    const isAuthenticated = computed(() => Boolean(tokenCookie.value))
    const authHeader = computed(() => {
      if (!tokenCookie.value) return ''
      return `${tokenTypeCookie.value ?? 'Bearer'} ${tokenCookie.value}`
    })
    const session = computed<AuthSession | null>(() => {
      if (!tokenCookie.value) return null
      return {
        token: tokenCookie.value,
        refreshToken: refreshTokenCookie.value ?? undefined,
        tokenType: tokenTypeCookie.value ?? 'Bearer',
        user: userCookie.value ?? undefined,
      }
    })

    function buildClient() {
      const config = runtime.getRuntimeConfig()
      return createAuthClient({
        baseURL: config.public.apiBase,
        endpoints: {
          loginPath: config.public.auth.loginPath,
          refreshPath: config.public.auth.refreshPath,
          callbackPath: config.public.auth.callbackPath,
          mePath: config.public.auth.mePath,
        },
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

    function setSessionUser(user: Record<string, unknown> | null) {
      userCookie.value = user
    }

    function restore() {
      /* no-op: cookie 在 SSR/CSR 均自动可用 */
    }

    function persist() {
      /* no-op: cookie 自动持久化 */
    }

    async function fetchMenus() {
      if (!isAuthenticated.value) return

      try {
        userMenus.value = await options.fetchMenusRemote(runtime.getApi())
      } catch {
        userMenus.value = []
      }
    }

    async function fetchCurrentUser(
      fetchOptions: AdminFetchCurrentUserOptions = {},
    ) {
      if (!isAuthenticated.value) {
        return null
      }

      try {
        const requestOptions = fetchOptions.noRetryOnUnauthorized
          ? { _retried: true }
          : undefined
        const remoteUser = await options.fetchCurrentUserRemote(
          runtime.getApi(),
          requestOptions,
        )
        if (!remoteUser) {
          return null
        }

        const merged = {
          ...(userCookie.value ?? {}),
          ...remoteUser,
        }
        userCookie.value = merged
        return merged
      } catch {
        return null
      }
    }

    async function login(account: string, password: string) {
      loading.value = true
      try {
        const payload = await buildClient().login<AdminLoginResponse>({
          account,
          password,
        })
        saveTokens(payload)
        await fetchCurrentUser()
      } finally {
        loading.value = false
      }
    }

    async function exchangeOAuthState(state: string) {
      const payload = await buildClient().callback<AdminLoginResponse>(state)
      saveTokens(payload)
      await fetchCurrentUser()
    }

    async function refresh(): Promise<AuthSession | null> {
      if (!refreshTokenCookie.value) {
        logout()
        return null
      }

      if (refreshPromise.value) {
        return refreshPromise.value
      }

      refreshPromise.value = (async () => {
        try {
          const payload = await buildClient().refresh<AdminLoginResponse>(
            refreshTokenCookie.value!,
          )
          saveTokens(payload)
          await fetchCurrentUser({ noRetryOnUnauthorized: true })
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
      const config = runtime.getRuntimeConfig()
      const base = config.public.auth.oauthAuthorizeBasePath
      const url = `${config.public.apiBase}${base}/${encodeURIComponent(registrationId)}`
      await runtime.navigateTo(url, { external: true })
    }

    async function restoreSession() {
      if (!isAuthenticated.value) return

      await Promise.all([
        fetchCurrentUser({ noRetryOnUnauthorized: false }),
        (async () => {
          if ((userMenus.value ?? []).length === 0) {
            await fetchMenus()
          }
        })(),
      ])
    }

    function logout() {
      clearTokens()
      userMenus.value = []
      runtime.clearLegacySession()
    }

    return {
      loading,
      session,
      userMenus,
      refreshPromise,
      isAuthenticated,
      authHeader,
      restore,
      persist,
      login,
      exchangeOAuthState,
      refresh,
      startOAuthLogin,
      fetchMenus,
      fetchCurrentUser,
      restoreSession,
      setSessionUser,
      logout,
      buildClient,
    }
  }
}