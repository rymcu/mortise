/**
 * 页面刷新后 Pinia 内存状态恢复插件（客户端，阻塞式）
 */
export default defineNuxtPlugin(async () => {
  const auth = useAuthStore()

  if (!auth.isAuthenticated) return

  try {
    await auth.restoreSession()
  } catch {
    // 静默忽略，由 api.ts 的 401 拦截兜底
  }
})
