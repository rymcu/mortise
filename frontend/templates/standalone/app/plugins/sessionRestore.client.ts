import { restoreSessionSafely } from '@mortise/auth'

/**
 * 页面刷新后 Pinia 内存状态恢复插件（客户端，阻塞式）
 */
export default defineNuxtPlugin(async () => {
  await restoreSessionSafely(useAuthStore())
})
