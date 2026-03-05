<script setup lang="ts">
/**
 * 消息收件箱 / 用户咨询会话页
 *
 * 布局：左侧会话列表 + 右侧消息区（UChatMessage + UChatPrompt）
 * 数据：对接后端 /api/v1/admin/im 系列接口
 */
import type { GlobalResult, PageResult } from '@mortise/core-sdk'
import type { ChatSession, InboxChatMessage } from '~/types/inbox'

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

interface BackendSession {
  id: number
  userId: number
  userName: string | null
  userAvatar: string | null
  status: number
  contextType: string | null
  contextId: number | null
  contextTitle: string | null
  lastMessage: string | null
  unreadCount: number
  updatedTime: string | null
}

interface BackendMessage {
  id: number
  sessionId: number
  role: string
  senderId: number | null
  content: string
  createdTime: string
}

const sessions = ref<ChatSession[]>([])
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

  replying.value = true
  try {
    const res = await $api<GlobalResult<BackendMessage>>(
      `/api/v1/admin/im/sessions/${selectedSessionId.value}/reply`,
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
      if (!messagesMap.value[selectedSessionId.value]) {
        messagesMap.value[selectedSessionId.value] = []
      }
      messagesMap.value[selectedSessionId.value].push(msg)

      // 更新会话最后一条消息
      const session = sessions.value.find((s) => s.id === selectedSessionId.value)
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
        <!-- ── 左侧：会话列表 ─────────────────────────────────────────────── -->
        <div class="border-default flex w-72 shrink-0 flex-col border-r">
          <div class="border-default border-b px-4 py-3">
            <p class="text-sm font-medium">用户咨询</p>
            <p class="text-muted text-xs">{{ sessions.length }} 个会话</p>
          </div>

          <div class="flex-1 overflow-y-auto">
            <div
              v-if="sessionsLoading && sessions.length === 0"
              class="text-muted py-8 text-center text-sm"
            >
              加载中...
            </div>
            <div
              v-else-if="sessionsError"
              class="flex flex-col items-center py-8 text-center"
            >
              <UIcon name="i-lucide-alert-circle" class="text-error mb-2 text-2xl" />
              <p class="text-muted text-xs">{{ sessionsError }}</p>
              <UButton
                size="xs"
                variant="ghost"
                class="mt-2"
                @click="loadSessions"
              >
                重试
              </UButton>
            </div>
            <div
              v-else-if="sessions.length === 0"
              class="py-12 text-center"
            >
              <UIcon
                name="i-lucide-message-square"
                class="text-muted mb-2 text-3xl"
              />
              <p class="text-muted text-sm">暂无咨询会话</p>
            </div>
            <div
              v-for="session in sessions"
              v-else
              :key="session.id"
              class="hover:bg-elevated/60 flex cursor-pointer items-start gap-3 px-4 py-3 transition-colors"
              :class="{ 'bg-elevated': selectedSessionId === session.id }"
              @click="selectSession(session)"
            >
              <UAvatar
                :src="session.userAvatar || undefined"
                :alt="session.userName || '用户'"
                size="md"
                class="shrink-0"
              />
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between gap-1">
                  <span class="truncate text-sm font-medium">
                    {{ session.userName || `用户 ${session.userId}` }}
                  </span>
                  <span class="text-muted shrink-0 text-xs">
                    {{ formatTime(session.updatedTime) }}
                  </span>
                </div>
                <div class="mt-0.5 flex items-center justify-between gap-1">
                  <span class="text-muted truncate text-xs">
                    {{ session.lastMessage || '暂无消息' }}
                  </span>
                  <UBadge
                    v-if="session.unreadCount > 0"
                    :label="String(session.unreadCount)"
                    color="primary"
                    variant="solid"
                    size="xs"
                    class="shrink-0"
                  />
                </div>
                <!-- 上下文标签 -->
                <div v-if="session.contextTitle" class="mt-1">
                  <span class="rounded bg-accented px-1 py-0.5 font-mono text-xs text-muted">
                    {{ session.contextTitle }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ── 右侧：消息区 ───────────────────────────────────────────────── -->
        <div class="flex min-w-0 flex-1 flex-col">
          <!-- 未选中占位 -->
          <div
            v-if="!selectedSession"
            class="flex flex-1 flex-col items-center justify-center"
          >
            <UIcon
              name="i-lucide-message-circle"
              class="text-muted mb-3 text-5xl"
            />
            <p class="text-muted text-sm">请从左侧选择一个会话</p>
          </div>

          <!-- 已选中会话 -->
          <template v-else>
            <!-- 顶栏 -->
            <div
              class="border-default flex shrink-0 items-center justify-between gap-3 border-b px-5 py-3"
            >
              <div class="flex items-center gap-3">
                <UAvatar
                  :src="selectedSession.userAvatar || undefined"
                  :alt="selectedSession.userName || '用户'"
                  size="sm"
                />
                <div>
                  <p class="text-sm font-medium">
                    {{ selectedSession.userName || `用户 ${selectedSession.userId}` }}
                  </p>
                  <p class="text-muted text-xs">
                    用户 ID：{{ selectedSession.userId }}
                    <template v-if="selectedSession.contextTitle">
                      · {{ selectedSession.contextTitle }}
                    </template>
                  </p>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <UBadge
                  :label="selectedSession.status === 0 ? '进行中' : selectedSession.status === 2 ? '等待中' : '已关闭'"
                  :color="selectedSession.status === 0 ? 'success' : selectedSession.status === 2 ? 'warning' : 'neutral'"
                  variant="subtle"
                  size="xs"
                />
              </div>
            </div>

            <!-- 消息列表区 -->
            <div class="flex-1 overflow-y-auto px-4 py-4">
              <div
                v-if="messagesLoading && currentMessages.length === 0"
                class="text-muted py-8 text-center text-sm"
              >
                <UIcon name="i-lucide-loader-circle" class="animate-spin text-2xl" />
              </div>
              <div
                v-else-if="currentMessages.length === 0"
                class="py-12 text-center"
              >
                <p class="text-muted text-sm">暂无消息记录</p>
              </div>
              <div
                v-for="msg in currentMessages"
                v-else
                :key="msg.id"
                class="mb-1 last:mb-0"
              >
                <p
                  class="text-muted mb-1 text-xs"
                  :class="msg.role === 'assistant' ? 'text-right' : 'text-left'"
                >
                  {{ msg.time }}
                </p>
                <UChatMessage
                  :parts="msg.parts"
                  :side="msg.role === 'assistant' ? 'right' : 'left'"
                  :avatar="
                    msg.role === 'user'
                      ? { src: selectedSession.userAvatar || undefined, alt: selectedSession.userName || '用户' }
                      : { icon: 'i-lucide-headset', alt: '客服' }
                  "
                />
              </div>
            </div>

            <!-- 回复输入 -->
            <div class="border-default shrink-0 border-t px-4 pb-4 pt-3">
              <UChatPrompt
                v-model="replyText"
                placeholder="输入回复内容（Enter 发送）"
                variant="subtle"
                :disabled="replying"
                @submit="sendReply"
              >
                <template #footer>
                  <div class="flex w-full items-center justify-end">
                    <UChatPromptSubmit
                      color="primary"
                      size="sm"
                      :loading="replying"
                      :disabled="!replyText.trim() || replying"
                    />
                  </div>
                </template>
              </UChatPrompt>
            </div>
          </template>
        </div>
      </div>
    </template>
  </UDashboardPanel>
</template>
