import { restoreSessionSafely } from '@mortise/auth'

/**
 * 页面刷新后 Pinia 内存状态恢复插件（客户端，阻塞式）
 *
 * 问题背景：
 * - Pinia store 是纯内存状态，页面刷新后会丢失
 * - Cookie 中的 token 在刷新后自动可用，但 cookie 中的用户信息可能过时
 *
 * 解决方案：
 * - 使用 async plugin（阻塞式）：Nuxt 会等待此插件完成后再挂载应用
 * - 保证组件首次渲染时，用户信息均已就绪
 */
export default defineNuxtPlugin(async () => {
  await restoreSessionSafely(useAuthStore())
})
