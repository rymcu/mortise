/**
 * 客户端启动时执行一次性清理：
 * 1. 迁移旧 localStorage 会话到 cookie（如果 cookie 中无 token 但 localStorage 中有）
 * 2. 清理旧 localStorage 数据
 */
export default defineNuxtPlugin(() => {
  try {
    const oldRaw = localStorage.getItem('mortise.auth.session')
    if (oldRaw) {
      const old = JSON.parse(oldRaw) as { token?: string; refreshToken?: string; user?: Record<string, unknown> }
      // 如果 cookie 中还没有 token，将旧会话迁移过去
      const tokenCookie = useCookie<string | null>('mortise-admin-token')
      if (!tokenCookie.value && old.token) {
        const refreshCookie = useCookie<string | null>('mortise-admin-refresh-token')
        const typeCookie = useCookie<string | null>('mortise-admin-token-type')
        const userCookieVal = useCookie<Record<string, unknown> | null>('mortise-admin-user')
        tokenCookie.value = old.token
        refreshCookie.value = old.refreshToken ?? null
        typeCookie.value = 'Bearer'
        userCookieVal.value = old.user ?? null
      }
      localStorage.removeItem('mortise.auth.session')
    }
  } catch {
    // 静默忽略迁移错误
  }
})
