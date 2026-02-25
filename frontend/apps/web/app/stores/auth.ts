import { defineStore } from 'pinia'
import { createAuthClient, clearSession, loadSession, saveSession, unwrapGlobalResult, type AuthSession } from '@mortise/auth'

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

function toSession(payload: WebLoginResponse): AuthSession {
  if (!payload?.token) {
    throw new Error('登录响应缺少 token')
  }

  return {
    token: payload.token,
    refreshToken: payload.refreshToken,
    tokenType: payload.tokenType || 'Bearer',
    expiresIn: payload.expiresIn,
    refreshExpiresIn: payload.refreshExpiresIn,
    user: {
      memberId: payload.memberId,
      username: payload.username,
      nickname: payload.nickname,
      avatarUrl: payload.avatarUrl
    },
    raw: payload as Record<string, unknown>
  }
}

export const useAuthStore = defineStore('web-auth', {
  state: () => ({
    session: null as AuthSession | null,
    loading: false,
    refreshPromise: null as Promise<AuthSession | null> | null
  }),

  getters: {
    isAuthenticated: state => Boolean(state.session?.token),
    accessToken: state => state.session?.token || '',
    authHeader: state => {
      if (!state.session?.token) {
        return ''
      }
      const type = state.session.tokenType || 'Bearer'
      return `${type} ${state.session.token}`
    }
  },

  actions: {
    buildClient() {
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
    },

    restore() {
      this.session = loadSession()
    },

    persist() {
      if (this.session) {
        saveSession(this.session)
      }
    },

    async login(account: string, password: string) {
      this.loading = true
      try {
        const client = this.buildClient()
        const payload = await client.login<WebLoginResponse>({ account, password })
        this.session = toSession(payload)
        this.persist()
      } finally {
        this.loading = false
      }
    },

    async exchangeOAuthState(state: string) {
      const client = this.buildClient()
      const payload = await client.callback<WebLoginResponse>(state)
      this.session = toSession(payload)
      this.persist()
    },

    async refresh() {
      if (!this.session?.refreshToken) {
        this.logout()
        return null
      }

      if (this.refreshPromise) {
        return this.refreshPromise
      }

      this.refreshPromise = (async () => {
        try {
          const client = this.buildClient()
          const payload = await client.refresh<WebLoginResponse>(this.session!.refreshToken!)
          this.session = toSession(payload)
          this.persist()
          return this.session
        } catch {
          this.logout()
          return null
        } finally {
          this.refreshPromise = null
        }
      })()

      return this.refreshPromise
    },

    async startOAuthLogin(registrationId: string) {
      const config = useRuntimeConfig()
      const endpoint = `${config.public.auth.oauthAuthUrlPath}/${encodeURIComponent(registrationId)}`
      const response = await fetch(`${config.public.apiBase}${endpoint}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      })

      const data = await unwrapGlobalResult<{ authorizationUrl: string }>(response)

      if (!data?.authorizationUrl) {
        throw new Error('未获取到 OAuth2 授权地址')
      }

      await navigateTo(data.authorizationUrl, { external: true })
    },

    logout() {
      this.session = null
      clearSession()
    }
  }
})
