export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuthStore()

  const publicRoutes = new Set([
    '/',
    '/auth/login',
    '/auth/register',
    '/auth/callback',
    '/auth/forgot-password'
  ])

  if (publicRoutes.has(to.path)) {
    return
  }

  if (!auth.isAuthenticated) {
    return navigateTo('/auth/login')
  }
})
