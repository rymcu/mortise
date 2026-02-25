export interface GlobalResultEnvelope<T> {
  code: number
  message: string
  data: T
}

export interface AuthSession {
  token: string
  refreshToken?: string
  tokenType?: string
  expiresIn?: number
  refreshExpiresIn?: number
  user?: Record<string, unknown>
  raw?: Record<string, unknown>
}

export interface AuthEndpoints {
  loginPath: string
  refreshPath: string
  callbackPath: string
  mePath?: string
}

export interface AuthClientOptions {
  baseURL: string
  endpoints: AuthEndpoints
  fetcher?: typeof fetch
}
