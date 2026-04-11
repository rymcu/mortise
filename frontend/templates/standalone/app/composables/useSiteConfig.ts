interface FooterLink {
  label: string
  to?: string
}

function normalizeConfigValue(value: string | null | undefined) {
  const trimmed = value?.trim()
  return trimmed ? trimmed : null
}

function createFooterLink(values: Record<string, string>, labelKey: string, linkKey: string): FooterLink | null {
  const label = normalizeConfigValue(values[labelKey])
  if (!label) {
    return null
  }

  return {
    label,
    to: normalizeConfigValue(values[linkKey]) ?? undefined
  }
}

/**
 * 站点展示配置视图层。
 *
 * 兼容 community layer 对 useSiteConfig 的依赖，
 * 让 standalone 应用也能复用统一的站点品牌和页脚配置。
 */
export const useSiteConfig = () => {
  const { values, pending, error } = usePublicSiteConfig()
  const { resolveUrl } = useMediaUrl()

  const siteName = computed(() => normalizeConfigValue(values.value['site.name']) ?? 'Mortise')
  const communityName = computed(() => `${siteName.value} 社区`)
  const siteDescription = computed(() => normalizeConfigValue(values.value['site.description']) ?? '')
  const siteLogo = computed(() => {
    const raw = normalizeConfigValue(values.value['site.logo'])
    return raw ? resolveUrl(raw) ?? null : null
  })
  const siteFavicon = computed(() => {
    const raw = normalizeConfigValue(values.value['site.favicon'])
    return raw ? resolveUrl(raw) : '/favicon.ico'
  })
  const footerCopyright = computed(() =>
    normalizeConfigValue(values.value['footer.copyright']) ?? `${communityName.value} • © ${new Date().getFullYear()}`
  )
  const icp = computed(() => normalizeConfigValue(values.value['footer.icp']))
  const footerLinks = computed<FooterLink[]>(() => {
    const rawLinks: Array<FooterLink | null> = [
      createFooterLink(values.value, 'footer.icp', 'footer.icp_link'),
      createFooterLink(values.value, 'footer.gov_beian', 'footer.gov_link'),
      createFooterLink(values.value, 'footer.telecom', 'footer.telecom_link')
    ]

    return rawLinks.filter((item): item is FooterLink => item !== null)
  })
  const titleTemplate = computed<string>(() => {
    const tpl = values.value['seo.title_template'] ?? '{page} - {site}'
    return tpl.replace('{page}', '%s').replace('{site}', siteName.value)
  })

  return {
    values,
    pending,
    error,
    siteName,
    communityName,
    siteDescription,
    siteLogo,
    siteFavicon,
    icp,
    footerCopyright,
    footerLinks,
    titleTemplate
  }
}
