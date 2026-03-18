import type { GlobalResult } from '@mortise/core-sdk'

export interface OAuth2ProviderInfo {
  registrationId: string
  clientName: string
  icon?: string
}

/**
 * 动态加载已启用的 OAuth2 登录提供商列表
 *
 * @param appType 登录入口类型：admin=管理端，site=用户端
 */
export function useOAuth2Providers(appType: string = 'admin') {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string | undefined

  const { data, pending, error } = useAsyncData<OAuth2ProviderInfo[]>(
    `oauth2-providers-${appType}`,
    async () => {
      if (!baseURL) return []
      try {
        const result = await $fetch<GlobalResult<OAuth2ProviderInfo[]>>(
          '/api/v1/admin/auth/oauth2-providers',
          { baseURL, query: { appType } },
        )
        return result.data ?? []
      } catch {
        return []
      }
    },
    { server: false, lazy: true },
  )

  function resolveIcon(registrationId: string): string {
    const id = registrationId.toLowerCase()
    if (id.includes('github')) return 'i-simple-icons-github'
    if (id.includes('google')) return 'i-simple-icons-google'
    if (id.includes('wechat') || id.includes('weixin')) return 'i-simple-icons-wechat'
    if (id.includes('gitee')) return 'i-simple-icons-gitee'
    return 'i-lucide-shield'
  }

  const providers = computed(() =>
    (data.value ?? []).map((p) => ({
      label: `使用 ${p.clientName} 登录`,
      icon: p.icon || resolveIcon(p.registrationId),
      registrationId: p.registrationId,
    })),
  )

  return { providers, pending, error }
}
