export interface SiteConfig {
  siteName: string
  siteDescription: string
  logoUrl: string | null
  icp: string | null
}

export const useSiteConfig = () => {
  const config = useState<SiteConfig | null>('siteConfig', () => null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const fetchSiteConfig = async () => {
    if (config.value) return config.value

    loading.value = true
    error.value = null

    try {
      const data = await $fetch<{ data: SiteConfig }>('/mortise/api/v1/admin/system/site-config/public')
      config.value = data.data
    }
    catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载站点配置失败'
      // 降级使用默认配置
      config.value = {
        siteName: 'Mortise',
        siteDescription: '现代化 Java 全栈开发框架',
        logoUrl: null,
        icp: null
      }
    }
    finally {
      loading.value = false
    }

    return config.value
  }

  return {
    siteConfig: config,
    loading: readonly(loading),
    error: readonly(error),
    fetchSiteConfig
  }
}
