type ApiFetch = <T>(
  request: string,
  options?: Record<string, unknown>
) => Promise<T>

declare module '#app' {
  interface NuxtApp {
    $api: ApiFetch
  }
}

declare module 'vue' {
  interface ComponentCustomProperties {
    $api: ApiFetch
  }
}

export {}
