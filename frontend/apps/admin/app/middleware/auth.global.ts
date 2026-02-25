export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuthStore()
  const initialized = useState<boolean | null>('system-initialized', () => null)

  const publicRoutes = new Set([
    '/auth/login',
    '/auth/callback',
    '/setup'
  ])

  // 系统未初始化时，非 setup 页面一律跳转到初始化引导
  if (initialized.value === false && to.path !== '/setup') {
    return navigateTo('/setup')
  }

  // 系统已初始化时，阻止访问 setup 页面
  if (initialized.value === true && to.path === '/setup') {
    return navigateTo('/auth/login')
  }

  if (publicRoutes.has(to.path)) {
    return
  }

  // Cookie 方案：SPA 模式下 cookie 在客户端始终可读
  if (!auth.isAuthenticated) {
    return navigateTo('/auth/login')
  }
})
