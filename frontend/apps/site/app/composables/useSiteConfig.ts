interface FooterLink {
  label: string
  to?: string
}

interface FooterColumnLink {
  label: string
  to?: string
  target?: string
}

interface FooterColumn {
  label: string
  children?: FooterColumnLink[]
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
    to: normalizeConfigValue(values[linkKey]) ?? undefined,
  }
}

function isFooterColumnLink(value: unknown): value is FooterColumnLink {
  if (!value || typeof value !== 'object') {
    return false
  }

  const item = value as Record<string, unknown>
  return typeof item.label === 'string'
    && item.label.trim().length > 0
    && (item.to === undefined || typeof item.to === 'string')
    && (item.target === undefined || typeof item.target === 'string')
}

function isFooterColumn(value: unknown): value is FooterColumn {
  if (!value || typeof value !== 'object') {
    return false
  }

  const item = value as Record<string, unknown>
  return typeof item.label === 'string'
    && item.label.trim().length > 0
    && (item.children === undefined || (Array.isArray(item.children) && item.children.every(isFooterColumnLink)))
}

function parseFooterColumns(value: string | null | undefined): FooterColumn[] {
  const normalized = normalizeConfigValue(value)
  if (!normalized) {
    return []
  }

  try {
    const parsed = JSON.parse(normalized)
    if (!Array.isArray(parsed)) {
      return []
    }

    return parsed
      .filter(isFooterColumn)
      .map(column => ({
        label: column.label.trim(),
        children: column.children
          ?.filter(isFooterColumnLink)
          .map(link => ({
            label: link.label.trim(),
            to: normalizeConfigValue(link.to) ?? undefined,
            target: normalizeConfigValue(link.target) ?? undefined,
          }))
          .filter(link => link.label),
      }))
      .filter(column => column.label)
  }
  catch {
    return []
  }
}

/**
 * 站点展示配置视图层。
 *
 * 基于公开站点配置组装出站点页头、页脚、SEO 等可直接消费的字段，
 * 与底层的 usePublicSiteConfig 请求层分离。
 */
export const useSiteConfig = () => {
  const { values, pending, error } = usePublicSiteConfig()
  const { resolveUrl } = useMediaUrl()

  /** 系统名称，默认 Mortise */
  const siteName = computed(() => normalizeConfigValue(values.value['site.name']) ?? 'Mortise')

  /** 社区名称 */
  const communityName = computed(() => `${siteName.value} 社区`)

  /** 网站描述 */
  const siteDescription = computed(() => normalizeConfigValue(values.value['site.description']) ?? '')

  /** Logo URL（为空时降级显示图标） */
  const siteLogo = computed(() => {
    const raw = normalizeConfigValue(values.value['site.logo'])
    return raw ? resolveUrl(raw) ?? null : null
  })

  /** Favicon URL（为空时使用默认 /favicon.ico） */
  const siteFavicon = computed(() => {
    const raw = normalizeConfigValue(values.value['site.favicon'])
    return raw ? resolveUrl(raw) : '/favicon.ico'
  })

  /** 页脚版权 */
  const footerCopyright = computed(() =>
    normalizeConfigValue(values.value['footer.copyright']) ?? `${communityName.value} • © ${new Date().getFullYear()}`,
  )

  /** ICP 备案号（兼容旧调用别名） */
  const icp = computed(() => normalizeConfigValue(values.value['footer.icp']))

  /** 页脚备案 / 资质链接 */
  const footerLinks = computed<FooterLink[]>(() => {
    const rawLinks: Array<FooterLink | null> = [
      createFooterLink(values.value, 'footer.icp', 'footer.icp_link'),
      createFooterLink(values.value, 'footer.gov_beian', 'footer.gov_link'),
      createFooterLink(values.value, 'footer.telecom', 'footer.telecom_link'),
    ]

    return rawLinks.filter((item): item is FooterLink => item !== null)
  })

  const footerColumns = computed<FooterColumn[]>(() => parseFooterColumns(values.value['footer.columns']))

  /**
   * 页面标题模板：{page} → %s，{site} → 系统名称。
   * 默认值：%s - Mortise
   */
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
    footerColumns,
    titleTemplate
  }
}
