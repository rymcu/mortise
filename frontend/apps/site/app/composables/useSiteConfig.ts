interface SiteConfigPublicVO {
  values: Record<string, string>
}

/**
 * 网站公开配置 composable
 *
 * 使用 useAsyncData 缓存，SSR 期间只请求一次，客户端路由跳转时直接复用缓存。
 * 可在任意组件 / 布局中调用，无需手动触发。
 */
export const useSiteConfig = () => {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string | undefined
  const { resolveUrl } = useMediaUrl()

  const nuxtApp = useNuxtApp()

  const { data, pending, error } = useAsyncData<SiteConfigPublicVO | null>(
    'site-config-public',
    async () => {
      if (!baseURL) return null
      try {
        const result = await $fetch<{ data: SiteConfigPublicVO }>(
          '/api/v1/admin/system/site-config/public',
            { baseURL }
        )
        return result.data ?? null
      }
      catch {
        return null
      }
    },
    {
      server: true,
      lazy: false,
      // 有缓存时直接返回，避免客户端路由跳转时重复请求
      getCachedData: (key) => nuxtApp.payload.data[key] ?? nuxtApp.static.data[key]
    }
  )

  /** 系统名称，默认 Mortise */
  const siteName = computed(() => data.value?.values?.['site.name'] ?? 'Mortise')

  /** 网站描述 */
  const siteDescription = computed(() => data.value?.values?.['site.description'] ?? '')

  /** Logo URL（为空时降级显示图标） */
  const siteLogo = computed(() => data.value?.values?.['site.logo'] || null)

  /** ICP 备案号 */
  const icp = computed(() => data.value?.values?.['site.icp'] || null)

  /**
   * 页面标题模板：{page} → %s，{site} → 系统名称。
   * 默认值：%s - Mortise
   */
  const titleTemplate = computed<string>(() => {
    const tpl = data.value?.values?.['seo.title_template'] ?? '{page} - {site}'
    return tpl.replace('{page}', '%s').replace('{site}', siteName.value)
  })

  return {
    pending,
    error,
    siteName,
    siteDescription,
    siteLogo,
    icp,
    titleTemplate
  }
}
