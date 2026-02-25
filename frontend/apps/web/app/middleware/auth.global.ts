export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuthStore()

  const publicRoutes = new Set([
    '/',
    '/app/auth/login',
    '/app/auth/register',
    '/app/auth/callback'
  ])

  if (publicRoutes.has(to.path)) {
    return
  }

  if (to.path.startsWith('/app') && !auth.isAuthenticated) {
    return navigateTo('/app/auth/login')
  }
})
