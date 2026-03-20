import type { AgentChatMessage, AgentChatResponse } from '~/types/agent'

type ChatStatus = 'ready' | 'streaming' | 'error'

/**
 * Agent Chat composable —— 基于 fetch + ReadableStream 的 SSE 流式对话
 *
 * 使用 fetch API 替代 EventSource，解决 EventSource 无法携带
 * 自定义 Authorization 请求头导致 401 的问题。
 */
export function useAgentChat() {
  const config = useRuntimeConfig()
  const toast = useToast()

  const messages = ref<AgentChatMessage[]>([])
  const status = ref<ChatStatus>('ready')
  const error = ref<string | null>(null)
  const conversationId = ref<string | null>(null)

  let abortController: AbortController | null = null

  // ── 工具方法 ──────────────────────────────────────────────────────────

  function createId(): string {
    return crypto.randomUUID()
  }

  function formatTime(value = new Date()): string {
    return value.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  function pushMessage(role: AgentChatMessage['role'], text: string): string {
    const id = createId()
    messages.value.push({
      id,
      role,
      time: formatTime(),
      parts: [{ type: 'text', text }],
    })
    return id
  }

  function updateMessage(id: string, text: string) {
    const target = messages.value.find(item => item.id === id)
    if (target) {
      target.parts = [{ type: 'text', text }]
    }
  }

  function buildHeaders(): Record<string, string> {
    const auth = useAuthStore()
    const headers: Record<string, string> = {
      'Accept': 'text/event-stream',
      'Cache-Control': 'no-cache',
    }
    if (auth.authHeader) {
      headers.Authorization = auth.authHeader
    }
    return headers
  }

  // ── SSE 事件解析 ──────────────────────────────────────────────────────

  function parseSseEvents(chunk: string): Array<{ event: string, data: string }> {
    const events: Array<{ event: string, data: string }> = []
    const blocks = chunk.split('\n\n')

    for (const block of blocks) {
      if (!block.trim()) continue
      let event = 'message'
      let data = ''
      for (const line of block.split('\n')) {
        if (line.startsWith('event:')) {
          event = line.slice(6).trim()
        }
        else if (line.startsWith('data:')) {
          data += line.slice(5).trim()
        }
      }
      if (data) {
        events.push({ event, data })
      }
    }
    return events
  }

  // ── 核心：发送消息 ────────────────────────────────────────────────────

  async function send(text: string, modelType: string, modelName?: string) {
    const trimmed = text.trim()
    if (!trimmed || status.value === 'streaming') return

    // 检查登录状态
    const auth = useAuthStore()
    if (!auth.isAuthenticated) {
      toast.add({
        title: '请先登录',
        description: '登录后即可使用 Agent 对话功能',
        icon: 'i-lucide-lock',
        color: 'warning',
      })
      await navigateTo('/auth/login')
      return
    }

    error.value = null
    status.value = 'streaming'
    pushMessage('user', trimmed)
    const assistantId = pushMessage('assistant', '')

    // 构建 SSE URL
    const base = (config.public.apiBase as string || '').replace(/\/$/, '')
    const params = new URLSearchParams({ message: trimmed })
    if (modelType) params.set('modelType', modelType)
    if (modelName) params.set('modelName', modelName)
    if (conversationId.value) params.set('conversationId', conversationId.value)
    const url = `${base}/api/v1/agent/chat/stream?${params.toString()}`

    // 中止上一次请求
    stop()
    abortController = new AbortController()

    try {
      const response = await fetch(url, {
        method: 'GET',
        headers: buildHeaders(),
        signal: abortController.signal,
        credentials: 'include',
      })

      if (!response.ok) {
        if (response.status === 401) {
          error.value = '认证失败，请重新登录'
          toast.add({
            title: '认证失败',
            description: '登录已过期，请重新登录',
            icon: 'i-lucide-alert-circle',
            color: 'error',
          })
        }
        else {
          error.value = `请求失败 (${response.status})`
        }
        updateMessage(assistantId, error.value!)
        status.value = 'error'
        return
      }

      const reader = response.body?.getReader()
      if (!reader) {
        error.value = '服务端未返回流式响应'
        updateMessage(assistantId, error.value)
        status.value = 'error'
        return
      }

      const decoder = new TextDecoder()
      let buffer = ''
      let fullContent = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const events = parseSseEvents(buffer)

        // 保留未完成的最后一段
        const lastNewlineIdx = buffer.lastIndexOf('\n\n')
        buffer = lastNewlineIdx >= 0 ? buffer.slice(lastNewlineIdx + 2) : buffer

        for (const evt of events) {
          if (evt.event === 'done') {
            status.value = 'ready'
            break
          }
          if (evt.event === 'error') {
            error.value = evt.data || '流式传输出错'
            updateMessage(assistantId, error.value!)
            status.value = 'error'
            break
          }
          if (evt.event === 'message') {
            try {
              const payload: AgentChatResponse = JSON.parse(evt.data)
              if (payload.content) {
                fullContent = payload.content
                updateMessage(assistantId, fullContent)
              }
              if (payload.conversationId) {
                conversationId.value = payload.conversationId
              }
            }
            catch {
              // 非 JSON 格式，直接作为文本
              fullContent = evt.data
              updateMessage(assistantId, fullContent)
            }
          }
        }
      }

      // 如果流正常结束但未收到 done 事件
      if (status.value === 'streaming') {
        status.value = 'ready'
      }
      // 如果 assistant 消息仍为空，说明没有收到有效内容
      if (!fullContent) {
        updateMessage(assistantId, '未收到有效回复')
      }
    }
    catch (err) {
      if ((err as Error).name === 'AbortError') {
        // 用户主动停止
        status.value = 'ready'
        if (!messages.value.find(m => m.id === assistantId)?.parts[0]?.text) {
          updateMessage(assistantId, '（已停止）')
        }
        return
      }
      // 流被异常中断（ERR_INCOMPLETE_CHUNKED_ENCODING 等），保留已接收内容
      if (fullContent) {
        status.value = 'ready'
        return
      }
      error.value = '连接中断，请检查后端服务是否正常运行'
      updateMessage(assistantId, error.value)
      status.value = 'error'
      toast.add({
        title: '连接中断',
        description: error.value,
        icon: 'i-lucide-wifi-off',
        color: 'error',
      })
    }
  }

  // ── 停止 / 清空 ──────────────────────────────────────────────────────

  function stop() {
    if (abortController) {
      abortController.abort()
      abortController = null
    }
  }

  function clear() {
    stop()
    messages.value = []
    conversationId.value = null
    error.value = null
    status.value = 'ready'
  }

  onBeforeUnmount(() => {
    stop()
  })

  return {
    messages,
    status: readonly(status),
    error: readonly(error),
    conversationId: readonly(conversationId),
    send,
    stop,
    clear,
  }
}
