import type { AuthApiRequestOptions } from '@mortise/auth'

type AppApiInvoker = <T>(
  request: string,
  options?: AuthApiRequestOptions
) => Promise<T>

declare module '#app' {
  interface NuxtApp {
    $api: AppApiInvoker
  }
}

declare module 'vue' {
  interface ComponentCustomProperties {
    $api: AppApiInvoker
  }
}

export {}
