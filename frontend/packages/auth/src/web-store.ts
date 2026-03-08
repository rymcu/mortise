import { computed, ref, shallowRef } from 'vue'
import { createAuthClient } from './client'
import { unwrapGlobalResult } from './result'
import type { AuthSession } from './types'

interface CookieRefLike<T> {
  value: T
}

interface CookieOptionsLike {
  sameSite?: 'lax' | 'strict' | 'none'
  maxAge?: number
}

interface AuthRuntimeConfigLike {
  public: {
    apiBase: string
    auth: {
      loginPath: string
      refreshPath: string
      callbackPath: string
      mePath?: string
      oauthAuthUrlPath: string
    }
  }
}

export interface WebLoginResponse {
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

export interface FetchCurrentUserOptions {
  noRetryOnUnauthorized?: boolean
}

export type AuthApiInvoker = <R>(
  request: string,
  options?: Record<string, unknown>,
) => Promise<R>

export interface WebAuthStoreRuntime {
  createCookie: <T>(
    name: string,
    options?: CookieOptionsLike,
  ) => CookieRefLike<T>
  getRuntimeConfig: () => AuthRuntimeConfigLike
  getApi: () => AuthApiInvoker
  navigateTo: (
    url: string,
    options?: { external?: boolean },
  ) => Promise<unknown>
  clearLegacySession: () => void
}

export interface CreateWebAuthStoreOptions {
  cookiePrefix?: string
  fetchCurrentUserRemote: (
    api: AuthApiInvoker,
    options?: Record<string, unknown>,
  ) => Promise<Record<string, unknown> | null>
  legacySessionKey?: string
}

function buildCookieName(prefix: string, suffix: string) {
  return `${prefix}-${suffix}`
}

async function fetchOAuthAuthorizationUrl(
  apiBase: string,
  oauthAuthUrlPath: string,
  registrationId: string,
) {
  const endpoint = `${oauthAuthUrlPath}/${encodeURIComponent(registrationId)}`
  const response = await fetch(`${apiBase}${endpoint}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  })

  const data = await unwrapGlobalResult<{ authorizationUrl: string }>(response)

  if (!data?.authorizationUrl) {
    throw new Error('未获取到 OAuth2 授权地址')
  }

  return data.authorizationUrl
}

export function createWebAuthStore(options: CreateWebAuthStoreOptions) {
  const cookiePrefix = options.cookiePrefix ?? 'mortise-web'

  return function setupWebAuthStore(runtime: WebAuthStoreRuntime) {
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

    const isAuthenticated = computed(() => Boolean(tokenCookie.value))
    const accessToken = computed(() => tokenCookie.value ?? '')
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

    function saveTokens(payload: WebLoginResponse) {
      if (!payload?.token) {
        throw new Error('登录响应缺少 token')
      }

      tokenCookie.value = payload.token
      refreshTokenCookie.value = payload.refreshToken ?? null
      tokenTypeCookie.value = payload.tokenType ?? 'Bearer'
      userCookie.value = {
        ...(userCookie.value ?? {}),
        memberId: payload.memberId,
        username: payload.username,
        nickname: payload.nickname,
        avatarUrl: payload.avatarUrl,
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

    function restore() {
      /* no-op: cookie 在 SSR/CSR 均自动可用 */
    }

    function persist() {
      /* no-op: cookie 自动持久化 */
    }

    async function fetchCurrentUser(fetchOptions: FetchCurrentUserOptions = {}) {
      if (!isAuthenticated.value) return null

      try {
        const requestOptions = fetchOptions.noRetryOnUnauthorized
          ? { _retried: true }
          : undefined
        const remoteUser = await options.fetchCurrentUserRemote(
          runtime.getApi(),
          requestOptions,
        )
        if (!remoteUser) return null

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
        const payload = await buildClient().login<WebLoginResponse>({
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
      const payload = await buildClient().callback<WebLoginResponse>(state)
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
          const payload = await buildClient().refresh<WebLoginResponse>(
            refreshTokenCookie.value!,
          )
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
      const config = runtime.getRuntimeConfig()
      const authorizationUrl = await fetchOAuthAuthorizationUrl(
        config.public.apiBase,
        config.public.auth.oauthAuthUrlPath,
        registrationId,
      )

      await runtime.navigateTo(authorizationUrl, { external: true })
    }

    async function restoreSession() {
      if (!isAuthenticated.value) return
      await fetchCurrentUser({ noRetryOnUnauthorized: false })
    }

    function logout() {
      clearTokens()
      runtime.clearLegacySession()
    }

    return {
      loading,
      session,
      refreshPromise,
      isAuthenticated,
      accessToken,
      authHeader,
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
      buildClient,
    }
  }
}