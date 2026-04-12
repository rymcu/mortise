/**
 * 媒体 URL 工具
 *
 * 文件接口返回相对路径（如 /files/local-plus/xxx.jpg），
 * 需拼接 apiBase 才能在浏览器中正常访问。
 *
 * apiBase 可能是：
 * - 绝对 URL：https://api.example.com/mortise（生产环境，通过 NUXT_PUBLIC_API_BASE 注入）
 * - 相对路径：/mortise（开发环境，通过 Vite proxy 转发，消除 CORS）
 * - 空字符串：降级，文件路径原样返回
 *
 * 规则：
 * - 已是完整 URL（http/https/data:）→ 原样返回
 * - 相对路径（以 / 开头）→ 拼接 apiBase（去掉末尾斜杠），并防止重复前缀
 * - 其他 → 原样返回
 */
export function useMediaUrl() {
  const config = useRuntimeConfig()
  const base = (config.public.apiBase as string ?? '').replace(/\/$/, '')

  function tryParseUrl(value: string): URL | null {
    try {
      return new URL(value)
    } catch {
      return null
    }
  }

  function toSameOriginMortisePath(url: URL): string | null {
    if (!url.pathname.startsWith('/mortise/')) {
      return null
    }

    return `${url.pathname}${url.search}${url.hash}`
  }

  function rewriteAbsoluteMediaUrl(url: string): string {
    const parsed = tryParseUrl(url)

    if (!parsed) {
      return url
    }

    const sameOriginPath = toSameOriginMortisePath(parsed)
    if (sameOriginPath) {
      if (!base || base.startsWith('/') || parsed.protocol === 'http:') {
        return sameOriginPath
      }

      const parsedBase = tryParseUrl(base)
      if (parsedBase) {
        return `${parsedBase.origin}${sameOriginPath}`
      }
    }

    return url
  }

  function normalizeLegacyMarkdownUrls(markdown: string | null | undefined): string {
    if (!markdown) return ''

    return markdown
      .replace(/(!?\[[^\]]*\])\(\[(https?:\/\/[^\]\s)]+)\]\(\2\)\)/g, (_match, label: string, url: string) => {
        return `${label}(${url})`
      })
      .replace(/(\[[^\]]+\])\(\[(https?:\/\/[^)\]]+)\)([^\]]+)\]\(\2\)\3\)/g, (_match, label: string, url: string, suffix: string) => {
        return `${label}(${url})${suffix}`
      })
  }

  function resolveUrl(url: string | null | undefined): string | null {
    if (!url) return null
    // 已是完整 URL，直接返回
    if (/^data:/.test(url)) return url
    if (/^https?:/.test(url)) return rewriteAbsoluteMediaUrl(url)
    if (url.startsWith('/')) {
      // 防止重复拼接：如后端已返回 /mortise/files/...，
      // 而 base 也是 /mortise，避免生成 /mortise/mortise/files/...
      if (base && !url.startsWith(base + '/')) {
        return `${base}${url}`
      }
      return url
    }
    return url
  }

  function toStoredUrl(url: string | null | undefined): string | null {
    if (!url) return null
    if (!base) return url

    if (url === base) {
      return '/'
    }

    if (url.startsWith(base + '/')) {
      return url.slice(base.length)
    }

    return url
  }

  function transformMarkdownImageUrls(markdown: string | null | undefined, mapper: (url: string) => string): string {
    if (!markdown) return ''

    return markdown.replace(/!\[([^\]]*)\]\(([^)\s]+)([^)]*)\)/g, (_match, altText: string, url: string, suffix: string) => {
      return `![${altText}](${mapper(url)}${suffix})`
    })
  }

  function transformHtmlImageUrls(html: string | null | undefined, mapper: (url: string) => string): string {
    if (!html) return ''

    return html.replace(/(<img\b[^>]*\bsrc=(['"]))([^'"]+)((?:\2)[^>]*>)/gi, (_match, prefix: string, quote: string, url: string, suffix: string) => {
      return `${prefix}${mapper(url)}${suffix}`
    })
  }

  function resolveMarkdownMediaUrls(markdown: string | null | undefined): string {
    return transformMarkdownImageUrls(markdown, url => resolveUrl(url) ?? url)
  }

  function toStoredMarkdownMediaUrls(markdown: string | null | undefined): string {
    return transformMarkdownImageUrls(markdown, url => toStoredUrl(url) ?? url)
  }

  function resolveHtmlMediaUrls(html: string | null | undefined): string {
    return transformHtmlImageUrls(html, url => resolveUrl(url) ?? url)
  }

  return {
    normalizeLegacyMarkdownUrls,
    resolveUrl,
    toStoredUrl,
    resolveMarkdownMediaUrls,
    toStoredMarkdownMediaUrls,
    resolveHtmlMediaUrls,
  }
}
