import type { SiteSession, SiteChatMessage, ConsultContext, SiteSessionItem } from '~/types/im'
import { SESSION_STATUS } from '~/types/im'

/**
 * apps/site 侧 IM 咋询 composable
 *
 * 管理会话的创建、消息拉取和消息发送。
 * 对接后端 /api/v1/app/im 系列接口（需用户已登录）。
 */
export function useImChat() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string

  // ── 状态 ─────────────────────────────────────────────────────────────────

  // session 使用 useState 跨路由持久化，确保页面跳转后仍能复用同一会话
  const session = useState<SiteSession | null>('im-chat-session', () => null)
  const messages = ref<SiteChatMessage[]>([])
  const sessions = ref<SiteSessionItem[]>([])
  const historyMessages = ref<SiteChatMessage[]>([])
  const loading = ref(false)
  const sending = ref(false)
  const sessionsLoading = ref(false)
  const historyLoading = ref(false)
  const error = ref<'auth' | 'network' | null>(null)

  /** 当前会话是否已关闭 */
  const isSessionClosed = computed(() => session.value?.status === SESSION_STATUS.CLOSED)

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
    const auth = useAuthStore()
    return auth.authHeader ? { Authorization: auth.authHeader } : {}
  }

  // ── 会话创建 ─────────────────────────────────────────────────────────────────

  async function createSession(ctx: ConsultContext = {}): Promise<boolean> {
    if (session.value) return true
    loading.value = true
    error.value = null
    try {
      // 1. 优先查询已有活跃会话（锁页面刷新后恢复）
      const activeRes = await $fetch<{ code: number; data: { id: number; status: number } | null }>(
        '/api/v1/app/im/sessions/active',
        {
          method: 'GET',
          baseURL,
          query: {
            contextType: ctx.contextType ?? undefined,
            contextId: ctx.contextId ?? undefined,
          },
          headers: buildHeaders(),
          credentials: 'include',
        }
      )
      if (activeRes.code === 200 && activeRes.data) {
        session.value = { id: activeRes.data.id, status: activeRes.data.status }
        await loadMessages()
        if (ctx.subject && messages.value.length === 0) await sendMessage(ctx.subject)
        startPolling()
        return true
      }

      // 2. 无活跃会话，新建
      const body: Record<string, unknown> = {}
      if (ctx.contextType) body.contextType = ctx.contextType
      if (ctx.contextId) body.contextId = ctx.contextId

      const createRes = await $fetch<{ code: number; data: { id: number; status: number } }>(
        '/api/v1/app/im/sessions',
        {
          method: 'POST',
          baseURL,
          body,
          headers: buildHeaders(),
          credentials: 'include',
        }
      )
      if (createRes.code === 200 && createRes.data) {
        session.value = { id: createRes.data.id, status: createRes.data.status }
        await loadMessages()
        if (ctx.subject && messages.value.length === 0) await sendMessage(ctx.subject)
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

  // ── 消息拉取 ─────────────────────────────────────────────────────────────────

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
          headers: buildHeaders(),
          credentials: 'include',
        }
      )
      if (res.code === 200) {
        messages.value = (res.data?.records ?? []).map((m) => ({
          id: String(m.id),
          role: mapRole(m.role),
          parts: [{ type: 'text' as const, text: m.content }],
          time: formatTime(m.createdTime),
        }))
      }
    } catch {
      // 静默忽略，保留旧消息
    }
  }

  // ── 会话状态同步 ─────────────────────────────────────────────────────────────────

  async function syncSessionStatus() {
    if (!session.value) return
    try {
      const res = await $fetch<{ code: number; data: { id: number; status: number } }>(
        `/api/v1/app/im/sessions/${session.value.id}`,
        {
          method: 'GET',
          baseURL,
          headers: buildHeaders(),
          credentials: 'include',
        }
      )
      if (res.code === 200 && res.data) {
        session.value = { ...session.value, status: res.data.status }
        // 会话已关闭时停止轮询
        if (res.data.status === SESSION_STATUS.CLOSED) stopPolling()
      }
    } catch {
      // 静默忽略
    }
  }

  // ── 消息发送 ─────────────────────────────────────────────────────────────────

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
          headers: buildHeaders(),
          credentials: 'include',
        }
      )
      if (res.code === 200 && res.data) {
        messages.value.push({
          id: String(res.data.id),
          role: mapRole(res.data.role),
          parts: [{ type: 'text', text: res.data.content }],
          time: formatTime(res.data.createdTime),
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

  // ── 历史会话 ─────────────────────────────────────────────────────────────────

  async function loadSessions(): Promise<void> {
    sessionsLoading.value = true
    try {
      const res = await $fetch<{ code: number; data: { records: SiteSessionItem[] } }>(
        '/api/v1/app/im/sessions',
        {
          method: 'GET',
          baseURL,
          query: { pageNum: 1, pageSize: 20 },
          headers: buildHeaders(),
          credentials: 'include',
        }
      )
      if (res.code === 200) {
        sessions.value = res.data?.records ?? []
      }
    } catch {
      // 静默忽略
    } finally {
      sessionsLoading.value = false
    }
  }

  async function openHistorySession(sessionId: number): Promise<void> {
    historyLoading.value = true
    historyMessages.value = []
    try {
      const res = await $fetch<{
        code: number
        data: { records: Array<{ id: number; role: string; content: string; createdTime: string }> }
      }>(
        `/api/v1/app/im/sessions/${sessionId}/messages`,
        {
          method: 'GET',
          baseURL,
          query: { pageNum: 1, pageSize: 100 },
          headers: buildHeaders(),
          credentials: 'include',
        }
      )
      if (res.code === 200) {
        historyMessages.value = (res.data?.records ?? []).map((m) => ({
          id: String(m.id),
          role: mapRole(m.role),
          parts: [{ type: 'text' as const, text: m.content }],
          time: formatTime(m.createdTime),
        }))
      }
    } catch {
      // 静默忽略
    } finally {
      historyLoading.value = false
    }
  }

  // ── 轮询 ─────────────────────────────────────────────────────────────────

  function startPolling() {
    stopPolling()
    pollTimer = setInterval(async () => {
      await loadMessages()
      await syncSessionStatus()
    }, 5000)
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
    sessions.value = []
    historyMessages.value = []
    error.value = null
    loading.value = false
    sending.value = false
  }

  /**
   * 恢复已有会话（打开 widget 且 session 已在 useState 中时调用）
   */
  async function resumeSession(): Promise<void> {
    if (!session.value) return
    loading.value = true
    error.value = null
    try {
      await syncSessionStatus()
      await loadMessages()
      if (!isSessionClosed.value) startPolling()
    } finally {
      loading.value = false
    }
  }

  return {
    session,
    sessions,
    messages,
    historyMessages,
    historyLoading,
    sessionsLoading,
    isSessionClosed,
    loading,
    sending,
    error,
    createSession,
    resumeSession,
    loadMessages,
    loadSessions,
    openHistorySession,
    sendMessage,
    startPolling,
    stopPolling,
    reset,
  }
}

