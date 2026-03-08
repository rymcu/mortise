/**
 * 命名路由守卫（非全局）
 *
 * 仅在需要保护的页面中显式声明：
 *   definePageMeta({ middleware: 'auth' })
 *
 * 未登录时跳转登录页，并通过 redirect query 参数在登录后回到原路径。
 */
export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuthStore()
  if (!auth.isAuthenticated) {
    return navigateTo(`/auth/login?redirect=${encodeURIComponent(to.fullPath)}`)
  }
})
