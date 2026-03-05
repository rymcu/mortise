import type { ConsultContext } from '~/types/im'

/**
 * 浮动咨询窗口全局状态
 *
 * 使用 Nuxt useState 确保 SSR 和 CSR 共享同一份状态。
 * pricing.vue / about.vue 等调用 open() 即可唤起组件。
 */
export function useChatWidget() {
  const isOpen = useState<boolean>('chat-widget-open', () => false)
  const context = useState<ConsultContext>('chat-widget-context', () => ({}))

  function open(ctx: ConsultContext = {}) {
    context.value = ctx
    isOpen.value = true
  }

  function close() {
    isOpen.value = false
  }

  return { isOpen, context, open, close }
}
