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

  function resolveUrl(url: string | null | undefined): string | null {
    if (!url) return null
    // 已是完整 URL，直接返回
    if (/^(https?:|data:)/.test(url)) return url
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

  return { resolveUrl }
}
