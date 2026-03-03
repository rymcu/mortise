export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig()

  const api = async <T>(
    request: string,
    options: Record<string, unknown> = {}
  ) => {
    // 在每次请求时获取 auth store，确保 cookie 已正确初始化
    const auth = useAuthStore()

    // 排除自定义属性，避免传递给 $fetch
    const { _retried, skipAuth, ...fetchOptions } = options as Record<string, unknown> & {
      _retried?: boolean
      skipAuth?: boolean
    }
    const retried = Boolean(_retried)
    const headers = {
      ...(fetchOptions.headers as Record<string, string> | undefined)
    }

    if (auth.authHeader && !skipAuth) {
      headers.Authorization = auth.authHeader
    }

    try {
      return await $fetch<T>(request, {
        ...fetchOptions,
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
        await navigateTo('/auth/login')
        throw error
      }

      return $fetch<T>(request, {
        ...fetchOptions,
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
