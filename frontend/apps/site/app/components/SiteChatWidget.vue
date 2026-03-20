<script setup lang="ts">
import type { ConsultContext, SiteSessionItem } from '~/types/im'

/**
 * 浮动咨询聊天组件
 *
 * 通过 useChatWidget().open(ctx) 唤起。
 * 对接后端 /api/v1/app/im 接口，需用户已登录。
 */
const { isOpen, context, open, close } = useChatWidget()

const {
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
  sendMessage,
  loadSessions,
  openHistorySession,
  stopPolling,
  reset,
} = useImChat()

const inputText = ref('')

/** 视图：'chat' = 当前会话，'history' = 历史列表，'detail' = 历史详情（只读） */
const view = ref<'chat' | 'history' | 'detail'>('chat')
const historyView = computed(() => view.value as 'history' | 'detail')

// ── 开关面板 ──────────────────────────────────────────────────────────────

watch(isOpen, async (val) => {
  if (val) {
    view.value = 'chat'
    if (session.value) {
      await resumeSession()
    } else {
      await createSession(context.value as ConsultContext)
    }
  } else {
    stopPolling()
  }
})

function handleClose() {
  close()
}

// ── 历史记录 ──────────────────────────────────────────────────────────────

async function handleHistory() {
  view.value = 'history'
  await loadSessions()
}

function handleBackFromHistory() {
  view.value = 'chat'
}

async function handleOpenDetail(item: SiteSessionItem) {
  view.value = 'detail'
  await openHistorySession(item.id)
}

function handleBackFromDetail() {
  view.value = 'history'
}

// ── 重试 / 重新咨询 ───────────────────────────────────────────────────────

function retryOrLogin() {
  if (error.value === 'auth') {
    navigateTo('/auth/login?returnToChat=1')
  } else {
    reset()
    createSession(context.value as ConsultContext)
  }
}

async function handleNewConsult() {
  reset()
  view.value = 'chat'
  await createSession(context.value as ConsultContext)
}

// ── 发送消息 ──────────────────────────────────────────────────────────────

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || sending.value) return
  const ok = await sendMessage(text)
  if (ok) inputText.value = ''
}

// ── 卸载清理 ──────────────────────────────────────────────────────────────

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <!-- 浮动按钮（始终显示） -->
  <div class="fixed bottom-6 right-6 z-50 flex flex-col items-end gap-3">
    <!-- 聊天面板 -->
    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0 translate-y-4 scale-95"
      enter-to-class="opacity-100 translate-y-0 scale-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100 translate-y-0 scale-100"
      leave-to-class="opacity-0 translate-y-4 scale-95"
    >
      <div
        v-if="isOpen"
        class="bg-background border-default flex w-80 flex-col rounded-xl border shadow-xl sm:w-96"
        style="height: 480px"
      >
        <!-- 顶栏 -->
        <div class="border-default flex shrink-0 items-center justify-between border-b px-4 py-3">
          <!-- 左侧 -->
          <div class="flex items-center gap-2">
            <UButton
              v-if="view !== 'chat'"
              icon="i-lucide-arrow-left"
              color="neutral"
              variant="ghost"
              size="xs"
              aria-label="返回"
              @click="view === 'detail' ? handleBackFromDetail() : handleBackFromHistory()"
            />
            <UIcon v-else name="i-lucide-headset" class="size-4 text-primary" />
            <span class="text-sm font-semibold">
              {{ view === 'history' ? '历史记录' : view === 'detail' ? '会话详情' : '在线咨询' }}
            </span>
          </div>
          <!-- 右侧 -->
          <div class="flex items-center gap-0.5">
            <UButton
              v-if="view === 'chat'"
              icon="i-lucide-clock-3"
              color="neutral"
              variant="ghost"
              size="xs"
              aria-label="历史记录"
              @click="handleHistory"
            />
            <UButton
              icon="i-lucide-x"
              color="neutral"
              variant="ghost"
              size="xs"
              aria-label="关闭"
              @click="handleClose"
            />
          </div>
        </div>

        <!-- 内容区 -->
        <div class="flex min-h-0 flex-1 flex-col overflow-hidden">
          <!-- ── 历史列表 / 历史详情 ── -->
          <ChatHistoryPanel
            v-if="view === 'history' || view === 'detail'"
            :view="historyView"
            :sessions="sessions"
            :sessions-loading="sessionsLoading"
            :history-messages="historyMessages"
            :history-loading="historyLoading"
            @open-detail="handleOpenDetail"
          />

          <!-- ── 当前聊天 ── -->
          <ChatMessageArea
            v-else
            v-model:input-text="inputText"
            :messages="messages"
            :is-session-closed="isSessionClosed"
            :loading="loading"
            :sending="sending"
            :error="error"
            @submit="handleSend"
            @retry="retryOrLogin"
            @new-consult="handleNewConsult"
          />
        </div>
      </div>
    </Transition>

    <!-- 悬浮按钮 -->
    <UButton
      :icon="isOpen ? 'i-lucide-x' : 'i-lucide-message-circle'"
      color="primary"
      size="lg"
      class="rounded-full shadow-lg"
      :aria-label="isOpen ? '关闭咨询' : '在线咨询'"
      @click="isOpen ? handleClose() : open()"
    />
  </div>
</template>
