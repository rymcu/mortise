<script setup lang="ts">
/**
 * 消息收件箱 / 用户咨询会话页
 *
 * 布局：左侧会话列表 + 右侧消息区（UChatMessage + UChatPrompt）
 * 数据：对接后端 /api/v1/admin/im 系列接口
 */
import type { GlobalResult, PageResult } from '@mortise/core-sdk'
import type { BackendMessage, BackendSession, ChatSession, InboxChatMessage } from '~/types/inbox'

const { $api } = useNuxtApp()

// ── 工具函数 ──────────────────────────────────────────────────────────────────

/** 将后端 LocalDateTime 字符串格式化为可读时间显示 */
function formatTime(iso: string | null | undefined): string {
  if (!iso) return ''
  const date = new Date(iso)
  const now = new Date()
  const isToday =
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()
  const isYesterday =
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate() - 1
  const hhmm = date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  if (isToday) return hhmm
  if (isYesterday) return `昨天 ${hhmm}`
  return `${date.getMonth() + 1}/${date.getDate()} ${hhmm}`
}

/** 将后端 role（USER/ADMIN）映射为前端 role（user/assistant） */
function mapRole(role: string): 'user' | 'assistant' {
  return role === 'USER' ? 'user' : 'assistant'
}

// ── 会话列表 ──────────────────────────────────────────────────────────────────

const sessions= ref<ChatSession[]>([])
const sessionsLoading = ref(false)
const sessionsError = ref('')
const selectedSessionId = ref<number | null>(null)

const selectedSession = computed(
  () => sessions.value.find((s) => s.id === selectedSessionId.value) ?? null
)

async function loadSessions() {
  sessionsLoading.value = true
  sessionsError.value = ''
  try {
    const res = await $api<GlobalResult<PageResult<BackendSession>>>(
      '/api/v1/admin/im/sessions',
      { method: 'GET', query: { pageNum: 1, pageSize: 50 } }
    )
    if (res.code === 200) {
      sessions.value = (res.data?.records ?? []).map((s) => ({
        id: s.id,
        userId: s.userId,
        userName: s.userName,
        userAvatar: s.userAvatar,
        status: s.status as 0 | 1 | 2,
        contextType: s.contextType,
        contextId: s.contextId,
        contextTitle: s.contextTitle,
        lastMessage: s.lastMessage,
        unreadCount: s.unreadCount,
        updatedTime: s.updatedTime
      }))
    }
  } catch {
    sessionsError.value = '加载会话列表失败'
  } finally {
    sessionsLoading.value = false
  }
}

async function selectSession(session: ChatSession) {
  selectedSessionId.value = session.id
  await loadMessages(session.id)
  // 标记已读
  if (session.unreadCount > 0) {
    try {
      await $api(`/api/v1/admin/im/sessions/${session.id}/read`, { method: 'PUT' })
      session.unreadCount = 0
    } catch {
      // 静默忽略
    }
  }
}

// ── 消息记录 ──────────────────────────────────────────────────────────────────

const messagesMap = ref<Record<number, InboxChatMessage[]>>({})
const messagesLoading = ref(false)

const currentMessages = computed<InboxChatMessage[]>(() =>
  selectedSessionId.value !== null
    ? (messagesMap.value[selectedSessionId.value] ?? [])
    : []
)

async function loadMessages(sessionId: number) {
  messagesLoading.value = true
  try {
    const res = await $api<GlobalResult<PageResult<BackendMessage>>>(
      `/api/v1/admin/im/sessions/${sessionId}/messages`,
      { method: 'GET', query: { pageNum: 1, pageSize: 100 } }
    )
    if (res.code === 200) {
      messagesMap.value[sessionId] = (res.data?.records ?? []).map((m) => ({
        id: String(m.id),
        sessionId: m.sessionId,
        role: mapRole(m.role),
        parts: [{ type: 'text' as const, text: m.content }],
        time: formatTime(m.createdTime),
        senderId: m.senderId
      }))
    }
  } catch {
    // 保留旧消息，不清空
  } finally {
    messagesLoading.value = false
  }
}

// ── 消息轮询 ──────────────────────────────────────────────────────────────────

let pollTimer: ReturnType<typeof setInterval> | null = null

function startPolling() {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (selectedSessionId.value !== null) {
      await loadMessages(selectedSessionId.value)
    }
  }, 5000)
}

function stopPolling() {
  if (pollTimer !== null) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(async () => {
  await loadSessions()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})

// ── 回复输入 ──────────────────────────────────────────────────────────────────

const replyText = ref('')
const replying = ref(false)

async function sendReply() {
  const content = replyText.value.trim()
  if (!content || selectedSessionId.value === null || replying.value) return
  const currentSessionId = selectedSessionId.value

  replying.value = true
  try {
    const res = await $api<GlobalResult<BackendMessage>>(
      `/api/v1/admin/im/sessions/${currentSessionId}/reply`,
      { method: 'POST', body: { content } }
    )
    if (res.code === 200 && res.data) {
      const msg: InboxChatMessage = {
        id: String(res.data.id),
        sessionId: res.data.sessionId,
        role: 'assistant',
        parts: [{ type: 'text', text: res.data.content }],
        time: formatTime(res.data.createdTime),
        senderId: res.data.senderId
      }
      if (!messagesMap.value[currentSessionId]) {
        messagesMap.value[currentSessionId] = []
      }
      messagesMap.value[currentSessionId]?.push(msg)

      // 更新会话最后一条消息
      const session = sessions.value.find((s) => s.id === currentSessionId)
      if (session) {
        session.lastMessage = content
        session.updatedTime = res.data.createdTime
      }
      replyText.value = ''
    }
  } catch {
    // TODO: 可接入 toast 通知
  } finally {
    replying.value = false
  }
}
</script>

<template>
  <UDashboardPanel id="inbox">
    <template #header>
      <UDashboardNavbar title="消息中心">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
        <template #right>
          <UButton
            icon="i-lucide-refresh-cw"
            color="neutral"
            variant="ghost"
            size="sm"
            :loading="sessionsLoading"
            aria-label="刷新会话"
            @click="loadSessions"
          />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="flex h-full overflow-hidden">
        <InboxSessionList
          :sessions="sessions"
          :sessions-loading="sessionsLoading"
          :sessions-error="sessionsError"
          :selected-session-id="selectedSessionId"
          @select="selectSession"
          @reload="loadSessions"
        />
        <InboxChatPanel
          :selected-session="selectedSession"
          :current-messages="currentMessages"
          :messages-loading="messagesLoading"
          :reply-text="replyText"
          :replying="replying"
          @update:reply-text="replyText = $event"
          @submit="sendReply"
        />
      </div>
    </template>
  </UDashboardPanel>
</template>
