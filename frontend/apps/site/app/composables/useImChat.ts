import type { SiteSession, SiteChatMessage, ConsultContext } from '~/types/im'

/**
 * apps/site 侧 IM 咨询 composable
 *
 * 管理会话的创建、消息拉取和消息发送。
 * 对接后端 /api/v1/app/im 系列接口（需用户已登录）。
 */
export function useImChat() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string

  // ── 状态 ─────────────────────────────────────────────────────────────────

  const session = ref<SiteSession | null>(null)
  const messages = ref<SiteChatMessage[]>([])
  const loading = ref(false)
  const sending = ref(false)
  const error = ref<'auth' | 'network' | null>(null)

  let pollTimer: ReturnType<typeof setInterval> | null = null

  // ── 工具 ─────────────────────────────────────────────────────────────────

  function formatTime(iso: string): string {
    const date = new Date(iso)
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  function mapRole(role: string): 'user' | 'assistant' {
    return role === 'USER' ? 'user' : 'assistant'
  }

  function buildHeaders(): Record<string, string> {
    return {}
  }

  // ── 会话创建 ─────────────────────────────────────────────────────────────

  async function createSession(ctx: ConsultContext = {}): Promise<boolean> {
    if (session.value) return true
    loading.value = true
    error.value = null
    try {
      const body: Record<string, unknown> = {}
      if (ctx.contextType) body.contextType = ctx.contextType
      if (ctx.contextId) body.contextId = ctx.contextId

      const res = await $fetch<{ code: number; data: { id: number; status: number } }>(
        '/api/v1/app/im/sessions',
        {
          method: 'POST',
          baseURL,
          body,
          headers: buildHeaders(),
          credentials: 'include'
        }
      )
      if (res.code === 200 && res.data) {
        session.value = { id: res.data.id, status: res.data.status }
        // 发送预设问题
        if (ctx.subject) {
          await sendMessage(ctx.subject)
        }
        startPolling()
        return true
      }
      error.value = 'network'
      return false
    } catch (err) {
      const status = (err as { response?: { status?: number } })?.response?.status
      error.value = status === 401 || status === 403 ? 'auth' : 'network'
      return false
    } finally {
      loading.value = false
    }
  }

  // ── 消息拉取 ─────────────────────────────────────────────────────────────

  async function loadMessages() {
    if (!session.value) return
    try {
      const res = await $fetch<{
        code: number
        data: { records: Array<{ id: number; role: string; content: string; createdTime: string }> }
      }>(
        `/api/v1/app/im/sessions/${session.value.id}/messages`,
        {
          method: 'GET',
          baseURL,
          query: { pageNum: 1, pageSize: 100 },
          credentials: 'include'
        }
      )
      if (res.code === 200) {
        messages.value = (res.data?.records ?? []).map((m) => ({
          id: String(m.id),
          role: mapRole(m.role),
          parts: [{ type: 'text' as const, text: m.content }],
          time: formatTime(m.createdTime)
        }))
      }
    } catch {
      // 静默忽略，保留旧消息
    }
  }

  // ── 消息发送 ─────────────────────────────────────────────────────────────

  async function sendMessage(content: string): Promise<boolean> {
    if (!session.value || !content.trim() || sending.value) return false
    sending.value = true
    try {
      const res = await $fetch<{
        code: number
        data: { id: number; role: string; content: string; createdTime: string }
      }>(
        `/api/v1/app/im/sessions/${session.value.id}/messages`,
        {
          method: 'POST',
          baseURL,
          body: { content: content.trim() },
          credentials: 'include'
        }
      )
      if (res.code === 200 && res.data) {
        messages.value.push({
          id: String(res.data.id),
          role: mapRole(res.data.role),
          parts: [{ type: 'text', text: res.data.content }],
          time: formatTime(res.data.createdTime)
        })
        return true
      }
      return false
    } catch {
      return false
    } finally {
      sending.value = false
    }
  }

  // ── 轮询 ─────────────────────────────────────────────────────────────────

  function startPolling() {
    stopPolling()
    pollTimer = setInterval(loadMessages, 5000)
  }

  function stopPolling() {
    if (pollTimer !== null) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  function reset() {
    stopPolling()
    session.value = null
    messages.value = []
    error.value = null
    loading.value = false
    sending.value = false
  }

  return {
    session,
    messages,
    loading,
    sending,
    error,
    createSession,
    loadMessages,
    sendMessage,
    startPolling,
    stopPolling,
    reset
  }
}
