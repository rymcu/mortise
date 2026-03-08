import { createTokenRefreshController } from '@mortise/auth'

/**
 * 客户端定时检查 token 过期时间，在即将过期前自动刷新。
 */
export default defineNuxtPlugin(() => {
  const controller = createTokenRefreshController({
    getStore: useAuthStore,
    getToken: () => useCookie<string | null>('mortise-web-token').value
  })

  controller.start()

  const nuxtApp = useNuxtApp()
  nuxtApp.hook('app:error', () => {
    controller.stop()
  })
})
