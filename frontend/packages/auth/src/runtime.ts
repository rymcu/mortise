import type { AuthSession } from './types'

export interface AuthApiRequestOptions extends Record<string, unknown> {
  headers?: Record<string, string>
  _retried?: boolean
  skipAuth?: boolean
}

export interface AuthApiStore {
  authHeader?: string | null
  refresh(): Promise<AuthSession | null>
}

export interface SessionRestoreStore {
  isAuthenticated: boolean
  restoreSession(): Promise<unknown>
}

export interface TokenRefreshStore {
  isAuthenticated: boolean
  refresh(): Promise<unknown>
}

type MaybePromise<T> = T | Promise<T>

export interface CreateAuthApiOptions<TStore extends AuthApiStore> {
  baseURL: string
  getStore: () => TStore
  fetcher: <T>(request: string, options?: Record<string, unknown>) => Promise<T>
  onUnauthorized?: () => MaybePromise<void>
}

export interface CreateTokenRefreshControllerOptions<
  TStore extends TokenRefreshStore,
> {
  getStore: () => TStore
  getToken: () => string | null
  onAfterCheck?: (store: TStore) => MaybePromise<void>
  onVisible?: (store: TStore) => MaybePromise<void>
  refreshThresholdMs?: number
  checkIntervalMs?: number
}

function parseAuthOptions(options: AuthApiRequestOptions) {
  const { _retried, skipAuth, ...fetchOptions } = options

  return {
    retried: Boolean(_retried),
    skipAuth: Boolean(skipAuth),
    fetchOptions,
  }
}

export function createAuthApi<TStore extends AuthApiStore>(
  options: CreateAuthApiOptions<TStore>,
) {
  return async function api<T>(
    request: string,
    requestOptions: AuthApiRequestOptions = {},
  ): Promise<T> {
    const auth = options.getStore()
    const { retried, skipAuth, fetchOptions } = parseAuthOptions(requestOptions)
    const headers = {
      ...(fetchOptions.headers as Record<string, string> | undefined),
    }

    if (auth.authHeader && !skipAuth) {
      headers.Authorization = auth.authHeader
    }

    try {
      return await options.fetcher<T>(request, {
        ...fetchOptions,
        baseURL: options.baseURL,
        headers,
      })
    } catch (error) {
      const responseStatus = (error as { response?: { status?: number } })
        ?.response?.status
      if (responseStatus !== 401 || retried) {
        throw error
      }

      const renewed = await auth.refresh()
      if (!renewed?.token) {
        await options.onUnauthorized?.()
        throw error
      }

      return options.fetcher<T>(request, {
        ...fetchOptions,
        baseURL: options.baseURL,
        headers: {
          ...headers,
          Authorization: auth.authHeader ?? '',
        },
      })
    }
  }
}

export async function restoreSessionSafely(
  store: SessionRestoreStore,
): Promise<void> {
  if (!store.isAuthenticated) {
    return
  }

  try {
    await store.restoreSession()
  } catch {
    // 静默忽略，由应用侧 401 拦截与路由守卫兜底
  }
}

export function createTokenRefreshController<TStore extends TokenRefreshStore>(
  options: CreateTokenRefreshControllerOptions<TStore>,
) {
  const refreshThresholdMs = options.refreshThresholdMs ?? 5 * 60 * 1000
  const checkIntervalMs = options.checkIntervalMs ?? 60 * 1000
  let timer: ReturnType<typeof setInterval> | null = null

  function getTokenExpiry(token: string): number | null {
    try {
      const parts = token.split('.')
      if (parts.length !== 3) return null
      const payloadPart = parts[1]
      if (!payloadPart) return null
      const payload = JSON.parse(atob(payloadPart)) as { exp?: unknown }
      return typeof payload.exp === 'number' ? payload.exp * 1000 : null
    } catch {
      return null
    }
  }

  function isTokenExpired(token: string): boolean {
    const expiry = getTokenExpiry(token)
    if (!expiry) return false
    return Date.now() >= expiry
  }

  async function check() {
    const store = options.getStore()
    if (!store.isAuthenticated) return

    const token = options.getToken()
    if (!token) return

    const expiry = getTokenExpiry(token)
    if (!expiry) return

    const remaining = expiry - Date.now()
    if (remaining < refreshThresholdMs) {
      await store.refresh()
    }

    await options.onAfterCheck?.(store)
  }

  async function handleVisibilityChange() {
    if (typeof document === 'undefined' || document.visibilityState !== 'visible') {
      return
    }

    const store = options.getStore()
    if (!store.isAuthenticated) return

    const token = options.getToken()
    if (!token) return

    if (isTokenExpired(token)) {
      await store.refresh()
    }

    await options.onVisible?.(store)
  }

  function start() {
    if (typeof document === 'undefined') {
      return
    }

    void check()
    timer = setInterval(() => {
      void check()
    }, checkIntervalMs)
    document.addEventListener('visibilitychange', handleVisibilityChange)
  }

  function stop() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }

    if (typeof document !== 'undefined') {
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  }

  return {
    check,
    handleVisibilityChange,
    start,
    stop,
    getTokenExpiry,
    isTokenExpired,
  }
}