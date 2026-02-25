import { unwrapGlobalResult } from './result'
import type { AuthClientOptions } from './types'

function joinURL(baseURL: string, path: string): string {
  if (!baseURL) {
    return path
  }
  if (baseURL.endsWith('/') && path.startsWith('/')) {
    return `${baseURL.slice(0, -1)}${path}`
  }
  if (!baseURL.endsWith('/') && !path.startsWith('/')) {
    return `${baseURL}/${path}`
  }
  return `${baseURL}${path}`
}

export function createAuthClient(options: AuthClientOptions) {
  const fetcher = options.fetcher ?? fetch

  async function request<T>(path: string, init?: RequestInit): Promise<T> {
    const response = await fetcher(joinURL(options.baseURL, path), {
      headers: {
        'Content-Type': 'application/json',
        ...(init?.headers || {})
      },
      ...init
    })

    return unwrapGlobalResult<T>(response)
  }

  return {
    login<T>(payload: Record<string, unknown>) {
      return request<T>(options.endpoints.loginPath, {
        method: 'POST',
        body: JSON.stringify(payload)
      })
    },

    refresh<T>(refreshToken: string) {
      return request<T>(options.endpoints.refreshPath, {
        method: 'POST',
        body: JSON.stringify({ refreshToken })
      })
    },

    callback<T>(state: string) {
      const url = `${options.endpoints.callbackPath}?state=${encodeURIComponent(state)}`
      return request<T>(url, { method: 'GET' })
    },

    me<T>() {
      if (!options.endpoints.mePath) {
        throw new Error('mePath is not configured')
      }
      return request<T>(options.endpoints.mePath, { method: 'GET' })
    }
  }
}
