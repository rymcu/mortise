interface SiteConfigPublicVO {
  values: Record<string, string>
}

/**
 * 公开站点配置请求层。
 *
 * 独立 community 应用复用 site 的公开配置能力，
 * 这样社区页头、页脚和 SEO 可以继续读取后端站点设置。
 */
export const usePublicSiteConfig = () => {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string | undefined
  const { resolveUrl } = useMediaUrl()
  const nuxtApp = useNuxtApp()

  const { data, pending, error, refresh } = useAsyncData<SiteConfigPublicVO | null>(
    'site-config-public',
    async () => {
      if (!baseURL) {
        return null
      }

      try {
        const result = await $fetch<{ data: SiteConfigPublicVO }>(
          '/api/v1/admin/system/site-config/public',
          { baseURL }
        )
        return result.data ?? null
      } catch {
        return null
      }
    },
    {
      server: true,
      lazy: false,
      getCachedData: key => nuxtApp.payload.data[key] ?? nuxtApp.static.data[key]
    }
  )

  const values = computed(() => data.value?.values ?? {})
  const siteName = computed(() => values.value['site.name'] ?? 'Mortise')
  const siteDescription = computed(() => values.value['site.description'] ?? '')
  const siteLogo = computed(() => resolveUrl(values.value['site.logo'] || ''))
  const siteFavicon = computed(() => {
    const raw = values.value['site.favicon'] || ''
    return raw ? resolveUrl(raw) : '/favicon.ico'
  })
  const seoKeywords = computed(() => values.value['seo.keywords'] ?? '')
  const titleTemplate = computed<string>(() => {
    const tpl = values.value['seo.title_template'] ?? '{page} - {site}'
    return tpl.replace('{page}', '%s').replace('{site}', siteName.value)
  })

  return {
    data,
    values,
    pending,
    error,
    refresh,
    siteName,
    siteDescription,
    siteLogo,
    siteFavicon,
    seoKeywords,
    titleTemplate
  }
}
