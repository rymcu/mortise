import { createTokenRefreshController } from '@mortise/auth'

/**
 * 客户端定时检查 token 过期时间，在即将过期前自动刷新。
 *
 * 原理：解码 JWT payload 获取 exp 字段，
 * 当剩余时间 < REFRESH_THRESHOLD 时主动调用 refresh()。
 * 若 token 不是 JWT 或没有 exp，则退化为被动刷新（由 api.ts 的 401 拦截处理）。
 *
 * 额外处理：监听 visibilitychange 事件，解决以下场景：
 * - 浏览器后台标签页长时间挂起（setInterval 被节流/暂停）
 * - 隔天回到页面时 token 已过期但页面不重新渲染
 */
export default defineNuxtPlugin(() => {
  const controller = createTokenRefreshController({
    getStore: useAuthStore,
    getToken: () => useCookie<string | null>('mortise-web-token').value
  })

  controller.start()

  // 应用卸载时清理
  const nuxtApp = useNuxtApp()
  nuxtApp.hook('app:error', () => {
    controller.stop()
  })
})
