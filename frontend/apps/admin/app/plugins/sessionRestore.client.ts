import { restoreSessionSafely } from '@mortise/auth'

/**
 * 页面刷新后 Pinia 内存状态恢复插件（客户端，阻塞式）
 *
 * 问题背景：
 * - Pinia store 是纯内存状态，页面刷新/新标签页后全部丢失
 * - Cookie 中的 token 在刷新后自动可用，但 userMenus、完整用户信息（含头像）等运行时状态需要重新获取
 *
 * 解决方案：
 * - 使用 async plugin（阻塞式）：Nuxt 会等待此插件完成后再挂载应用
 * - 保证组件首次渲染时，菜单和用户信息均已就绪，消除"菜单空白/无头像"闪烁
 */
export default defineNuxtPlugin(async () => {
  await restoreSessionSafely(useAuthStore())
})
