import DOMPurify from 'isomorphic-dompurify'

type SanitizeElementHookData = {
  tagName: string
}

type DOMPurifyWithHooks = typeof DOMPurify & {
  addHook: (
    entryPoint: 'uponSanitizeElement',
    hook: (node: Node, data: SanitizeElementHookData) => void
  ) => void
}

const ALLOWED_IFRAME_HOSTS = new Set([
  'player.bilibili.com',
  'www.youtube.com',
  'www.youtube-nocookie.com',
  'player.vimeo.com',
  'open.spotify.com',
  'music.163.com'
])

function isAllowedIframeSrc(src: string | null): boolean {
  if (!src) {
    return false
  }

  try {
    const url = new URL(src)
    return url.protocol === 'https:' && ALLOWED_IFRAME_HOSTS.has(url.hostname)
  } catch {
    return false
  }
}

(DOMPurify as DOMPurifyWithHooks).addHook('uponSanitizeElement', (node, data) => {
  if (data.tagName !== 'iframe') {
    return
  }

  const element = node as Element
  if (!isAllowedIframeSrc(element.getAttribute('src'))) {
    element.remove()
  }
})

/**
 * 对 HTML 字符串进行 XSS 清洗，保留文章渲染所需的安全标签和属性。
 *
 * 用法：
 * ```ts
 * const clean = useSanitizedHtml(rawHtml)  // Ref<string> → Ref<string>
 * const clean = useSanitizedHtml('<p>hello</p>')  // string → string
 * ```
 */
export function useSanitizedHtml(html: MaybeRef<string>): ComputedRef<string>
export function useSanitizedHtml(html: string): string
export function useSanitizedHtml(html: MaybeRef<string>): ComputedRef<string> | string {
  const purify = (raw: string) =>
    DOMPurify.sanitize(raw, {
      // 允许文章常用标签（prose 排版 + 代码高亮 + 媒体）
      ADD_TAGS: ['iframe'],
      ADD_ATTR: ['target', 'rel', 'loading', 'decoding', 'allow', 'allowfullscreen', 'frameborder'],
      // 禁止表单和脚本
      FORBID_TAGS: ['form', 'input', 'textarea', 'select', 'button', 'style'],
      FORBID_ATTR: ['onerror', 'onload', 'onclick', 'onmouseover'],
      ALLOW_UNKNOWN_PROTOCOLS: false,
    })

  if (isRef(html)) {
    return computed(() => purify(toValue(html)))
  }
  return purify(html)
}
