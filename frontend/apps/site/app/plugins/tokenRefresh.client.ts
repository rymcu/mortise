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
  const auth = useAuthStore()

  /** 提前 5 分钟刷新 */
  const REFRESH_THRESHOLD = 5 * 60 * 1000
  /** 每 60 秒检查一次 */
  const CHECK_INTERVAL = 60 * 1000

  /** 解码 JWT 获取过期时间戳（毫秒） */
  function getTokenExpiry(token: string): number | null {
    try {
      const parts = token.split('.')
      if (parts.length !== 3) return null
      const payloadPart = parts[1]
      if (!payloadPart) return null
      const payload = JSON.parse(atob(payloadPart))
      return typeof payload.exp === 'number' ? payload.exp * 1000 : null
    }
    catch {
      return null
    }
  }

  /** 检查 token 是否已过期 */
  function isTokenExpired(token: string): boolean {
    const expiry = getTokenExpiry(token)
    if (!expiry) return false
    return Date.now() >= expiry
  }

  async function check() {
    if (!auth.isAuthenticated) return

    const token = useCookie<string | null>('mortise-web-token').value
    if (!token) return

    const expiry = getTokenExpiry(token)
    if (!expiry) return // 非 JWT 或无 exp，退化为被动刷新

    const remaining = expiry - Date.now()
    if (remaining < REFRESH_THRESHOLD) {
      await auth.refresh()
    }
  }

  /**
   * 页面从后台恢复时执行检查：
   * 1. 若 token 已过期 → 刷新 token
   * 解决浏览器后台挂起 setInterval 导致隔天回来 token 已过期的问题。
   */
  async function handleVisibilityChange() {
    if (document.visibilityState !== 'visible') return
    if (!auth.isAuthenticated) return

    const token = useCookie<string | null>('mortise-web-token').value
    if (!token) return

    if (isTokenExpired(token)) {
      await auth.refresh()
    }
  }

  // 立即检查一次
  check()

  // 定时检查（后台标签页可能被浏览器节流，不可完全依赖此定时器）
  const timer = setInterval(check, CHECK_INTERVAL)

  // 监听标签页可见性变化
  document.addEventListener('visibilitychange', handleVisibilityChange)

  // 应用卸载时清理
  const nuxtApp = useNuxtApp()
  nuxtApp.hook('app:error', () => {
    clearInterval(timer)
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  })
})
