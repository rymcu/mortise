import type { SiteConfigPublicVO } from '~/types'
import type {GlobalResult} from "@mortise/core-sdk";

/**
 * 网站公开配置 composable
 *
 * 无需鉴权，使用 useAsyncData 缓存，可在任意组件/布局中调用。
 * 用于在页面头部（useHead / useSeoMeta）动态注入系统名称、Favicon、SEO 信息等。
 */
export function usePublicSiteConfig() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string | undefined
  const { resolveUrl } = useMediaUrl()

  const { data, pending, error } = useAsyncData<SiteConfigPublicVO | null>(
    'site-config-public',
    async () => {
      if (!baseURL) return null
      try {
        const result = await $fetch<GlobalResult<SiteConfigPublicVO>>(
          '/api/v1/admin/system/site-config/public',
          { baseURL }
        )
        return result.data ?? null
      } catch {
        return null
      }
    },
    { server: true, lazy: false }
  )

  /** 系统名称，默认 Mortise */
  const siteName = computed(() => data.value?.values?.['site.name'] ?? 'Mortise')

  /** 网站描述 */
  const siteDescription = computed(() => data.value?.values?.['site.description'] ?? '')

  /** 系统 Logo URL（为空时降级显示图标） */
  const siteLogo = computed(() => resolveUrl(data.value?.values?.['site.logo'] || ''))

  /** Favicon URL（为空时使用默认 /favicon.ico） */
  const siteFavicon = computed(() => resolveUrl(data.value?.values?.['site.favicon'] || '/favicon.ico'))

  /** 全局 SEO 关键词 */
  const seoKeywords = computed(() => data.value?.values?.['seo.keywords'] ?? '')

  /**
   * 页面标题模板：使用 Unhead 原生 %s 占位符。
   * 后端模板 {page} → %s，{site} → 系统名称。
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
    siteFavicon,
    seoKeywords,
    titleTemplate
  }
}
