export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig()
  const auth = useAuthStore()

  const api = async <T>(
    request: string,
    options: Record<string, unknown> = {}
  ) => {
    const retried = Boolean(options._retried)
    const skipAuth = Boolean(options.skipAuth)
    const headers = {
      ...(options.headers as Record<string, string> | undefined)
    }

    if (auth.authHeader && !skipAuth) {
      headers.Authorization = auth.authHeader
    }

    try {
      return await $fetch<T>(request, {
        ...options,
        baseURL: config.public.apiBase,
        headers
      })
    } catch (error) {
      const responseStatus = (error as { response?: { status?: number } })
        ?.response?.status
      if (responseStatus !== 401 || retried) {
        throw error
      }

      const renewed = await auth.refresh()
      if (!renewed?.token) {
        await navigateTo('/app/auth/login')
        throw error
      }

      return $fetch<T>(request, {
        ...options,
        baseURL: config.public.apiBase,
        headers: {
          ...headers,
          Authorization: auth.authHeader
        }
      })
    }
  }

  return {
    provide: {
      api
    }
  }
})
