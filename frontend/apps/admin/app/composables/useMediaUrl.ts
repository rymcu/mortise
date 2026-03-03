/**
 * 媒体 URL 工具
 *
 * 文件接口返回相对路径（如 /files/local-plus/xxx.jpg），
 * 需拼接 apiBase 才能在浏览器中正常访问。
 *
 * 规则：
 * - 已是完整 URL（http/https/data:）→ 原样返回
 * - 以 / 开头的相对路径 → 拼接 apiBase（去掉末尾斜杠）
 * - 其他 → 原样返回
 */
export function useMediaUrl() {
  const config = useRuntimeConfig()
  const base = (config.public.apiBase as string).replace(/\/$/, '')

  function resolveUrl(url: string | null | undefined): string | null {
    if (!url) return null
    if (/^(https?:|data:)/.test(url)) return url
    if (url.startsWith('/')) return `${base}${url}`
    return url
  }

  return { resolveUrl }
}
