/**
 * 客户端定时检查 token 过期时间，在即将过期前自动刷新。
 */
export default defineNuxtPlugin(() => {
  const auth = useAuthStore()

  const REFRESH_THRESHOLD = 5 * 60 * 1000
  const CHECK_INTERVAL = 60 * 1000

  function getTokenExpiry(token: string): number | null {
    try {
      const parts = token.split('.')
      if (parts.length !== 3) return null
      const payloadPart = parts[1]
      if (!payloadPart) return null
      const payload = JSON.parse(atob(payloadPart))
      return typeof payload.exp === 'number' ? payload.exp * 1000 : null
    } catch {
      return null
    }
  }

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
    if (!expiry) return

    const remaining = expiry - Date.now()
    if (remaining < REFRESH_THRESHOLD) {
      await auth.refresh()
    }
  }

  async function handleVisibilityChange() {
    if (document.visibilityState !== 'visible') return
    if (!auth.isAuthenticated) return

    const token = useCookie<string | null>('mortise-web-token').value
    if (!token) return

    if (isTokenExpired(token)) {
      await auth.refresh()
    }
  }

  check()

  const timer = setInterval(check, CHECK_INTERVAL)
  document.addEventListener('visibilitychange', handleVisibilityChange)

  const nuxtApp = useNuxtApp()
  nuxtApp.hook('app:error', () => {
    clearInterval(timer)
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  })
})
