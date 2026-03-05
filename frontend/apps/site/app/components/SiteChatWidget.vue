<script setup lang="ts">
import type { ConsultContext } from '~/types/im'

/**
 * 浮动咨询聊天组件
 *
 * 通过 useChatWidget().open(ctx) 唤起。
 * 对接后端 /api/v1/app/im 接口，需用户已登录。
 */
const { isOpen, context, open, close } = useChatWidget()
const config = useRuntimeConfig()
const webUrl = (config.public as { webUrl?: string }).webUrl ?? ''

const {
  messages,
  loading,
  sending,
  error,
  createSession,
  sendMessage,
  stopPolling,
  reset
} = useImChat()

const inputText = ref('')
const initialized = ref(false)

// ── 开关面板 ──────────────────────────────────────────────────────────────

watch(isOpen, async (val) => {
  if (val && !initialized.value) {
    initialized.value = true
    await createSession(context.value as ConsultContext)
  }
})

function handleClose() {
  close()
}

// ── 重置（用于重新尝试或关闭后下次重开） ─────────────────────────────────────

function retryOrLogin() {
  if (error.value === 'auth') {
    const loginUrl = `${webUrl}/auth/login?redirect=/`
    window.open(loginUrl, '_blank')
  } else {
    reset()
    initialized.value = false
    // 重新发起
    createSession(context.value as ConsultContext)
  }
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
          <div class="flex items-center gap-2">
            <UIcon name="i-lucide-headset" class="size-4 text-primary" />
            <span class="text-sm font-semibold">在线咨询</span>
          </div>
          <UButton
            icon="i-lucide-x"
            color="neutral"
            variant="ghost"
            size="xs"
            aria-label="关闭"
            @click="handleClose"
          />
        </div>

        <!-- 内容区 -->
        <div class="flex min-h-0 flex-1 flex-col overflow-hidden">
          <!-- 加载中 -->
          <div
            v-if="loading"
            class="flex flex-1 flex-col items-center justify-center"
          >
            <UIcon name="i-lucide-loader-circle" class="text-muted animate-spin text-3xl" />
            <p class="text-muted mt-2 text-sm">正在连接客服...</p>
          </div>

          <!-- 需要登录 -->
          <div
            v-else-if="error === 'auth'"
            class="flex flex-1 flex-col items-center justify-center gap-4 px-6 text-center"
          >
            <UIcon name="i-lucide-lock" class="text-muted text-4xl" />
            <div>
              <p class="text-sm font-medium">请先登录后再咨询</p>
              <p class="text-muted mt-1 text-xs">登录后即可与客服实时沟通</p>
            </div>
            <div class="flex w-full flex-col gap-2">
              <UButton
                label="去登录"
                color="primary"
                block
                @click="retryOrLogin"
              />
              <p class="text-muted text-xs">
                或发邮件至
                <a href="mailto:ronger@rymcu.com" class="text-primary underline">ronger@rymcu.com</a>
              </p>
            </div>
          </div>

          <!-- 网络错误 -->
          <div
            v-else-if="error === 'network'"
            class="flex flex-1 flex-col items-center justify-center gap-4 px-6 text-center"
          >
            <UIcon name="i-lucide-wifi-off" class="text-muted text-4xl" />
            <p class="text-muted text-sm">连接失败，请稍后重试</p>
            <UButton
              label="重试"
              variant="outline"
              color="neutral"
              @click="retryOrLogin"
            />
          </div>

          <!-- 聊天区 -->
          <template v-else>
            <!-- 消息列表 -->
            <div class="flex-1 overflow-y-auto px-3 py-3">
              <!-- 欢迎语 -->
              <div v-if="messages.length === 0" class="mb-3">
                <UChatMessage
                  :parts="[{ type: 'text', text: '您好，欢迎咨询！请描述您的需求，客服将尽快回复。' }]"
                  side="left"
                  :avatar="{ icon: 'i-lucide-headset', alt: '客服' }"
                />
              </div>
              <!-- 历史消息 -->
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="mb-1 last:mb-0"
              >
                <p
                  class="text-muted mb-0.5 text-xs"
                  :class="msg.role === 'assistant' ? 'text-right' : 'text-left'"
                >
                  {{ msg.time }}
                </p>
                <UChatMessage
                  :parts="msg.parts"
                  :side="msg.role === 'assistant' ? 'right' : 'left'"
                  :avatar="
                    msg.role === 'user'
                      ? undefined
                      : { icon: 'i-lucide-headset', alt: '客服' }
                  "
                />
              </div>
            </div>

            <!-- 输入区 -->
            <div class="border-default shrink-0 border-t px-3 pb-3 pt-2">
              <UChatPrompt
                v-model="inputText"
                placeholder="输入问题（Enter 发送）"
                variant="subtle"
                :disabled="sending"
                @submit="handleSend"
              >
                <template #footer>
                  <div class="flex w-full items-center justify-end">
                    <UChatPromptSubmit
                      color="primary"
                      size="sm"
                      :loading="sending"
                      :disabled="!inputText.trim() || sending"
                    />
                  </div>
                </template>
              </UChatPrompt>
            </div>
          </template>
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
