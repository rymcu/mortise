import { createAuthApi } from '@mortise/auth'
import type { AuthApiRequestOptions } from '@mortise/auth'

type AppApiInvoker = <T>(
  request: string,
  options?: AuthApiRequestOptions,
) => Promise<T>

export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig()
  const api: AppApiInvoker = createAuthApi({
    baseURL: config.public.apiBase,
    getStore: useAuthStore,
    fetcher: $fetch,
    onUnauthorized: async () => {
      await navigateTo('/auth/login')
    }
  })

  return {
    provide: {
      api
    }
  }
})
