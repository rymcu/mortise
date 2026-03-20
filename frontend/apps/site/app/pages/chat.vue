<script setup lang="ts">
type ChatPart = { type: 'text'; text: string }
type ChatItem = {
  id: string
  role: 'user' | 'assistant'
  parts: ChatPart[]
  time: string
}

const config = useRuntimeConfig()

useSeoMeta({
  title: 'Agent Chat',
  description: '使用 SSE 与 Mortise Agent 实时对话',
})

const inputText = ref('')
const messages = ref<ChatItem[]>([])
const sending = ref(false)
const error = ref<string | null>(null)
const eventSource = ref<EventSource | null>(null)

const modelType = ref('openai')
const modelOptions = [
  { label: 'OpenAI', value: 'openai' },
  { label: 'Anthropic', value: 'anthropic' },
  { label: 'DeepSeek', value: 'deepseek' },
  { label: 'Ollama', value: 'ollama' },
]

const hasMessages = computed(() => messages.value.length > 0)
const canSend = computed(() => inputText.value.trim().length > 0 && !sending.value)
const statusLabel = computed(() => (sending.value ? '对话中' : error.value ? '异常' : '就绪'))
const statusColor = computed(() => (error.value ? 'error' : sending.value ? 'warning' : 'success'))

function createId(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function formatTime(value = new Date()): string {
  return value.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function pushMessage(role: ChatItem['role'], text: string): string {
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

function closeStream() {
  if (eventSource.value) {
    eventSource.value.close()
    eventSource.value = null
  }
}

function buildSseUrl(message: string) {
  const base = (config.public.apiBase || '').replace(/\/$/, '')
  const params = new URLSearchParams({ message })
  if (modelType.value) {
    params.set('modelType', modelType.value)
  }
  return `${base}/api/v1/app/agent/chat/stream?${params.toString()}`
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || sending.value) return

  error.value = null
  pushMessage('user', text)
  inputText.value = ''

  const assistantId = pushMessage('assistant', '正在处理...')
  sending.value = true

  closeStream()
  const url = buildSseUrl(text)
  const source = new EventSource(url, { withCredentials: true })
  eventSource.value = source

  source.addEventListener('message', (event) => {
    try {
      const payload = JSON.parse(event.data)
      const content = typeof payload === 'string' ? payload : payload?.content
      updateMessage(assistantId, content || '未返回内容')
    } catch (ex) {
      updateMessage(assistantId, event.data || '未返回内容')
    }
  })

  source.addEventListener('done', () => {
    sending.value = false
    closeStream()
  })

  source.addEventListener('error', () => {
    sending.value = false
    error.value = '连接失败，请稍后重试'
    updateMessage(assistantId, '发生错误，请重试。')
    closeStream()
  })
}

function handleClear() {
  closeStream()
  messages.value = []
  error.value = null
}

onBeforeUnmount(() => {
  closeStream()
})
</script>

<template>
  <div class="relative overflow-hidden">
    <div class="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top,#00dc8233,transparent_45%),radial-gradient(circle_at_20%_80%,#00a1552a,transparent_50%)]" />

    <UPageHero
      title="Mortise Agent Chat"
      description="使用 SSE 与 Mortise Agent 实时对话，快速获取答案与工具调用建议。"
      class="relative"
      :ui="{ title: 'max-w-3xl mx-auto' }"
    >
      <template #links>
        <UBadge :color="statusColor" variant="subtle">
          {{ statusLabel }}
        </UBadge>
      </template>
    </UPageHero>

    <UPageSection class="relative">
      <div class="grid gap-8 lg:grid-cols-[1.1fr_1.4fr]">
        <UCard class="border-default bg-elevated/50">
          <template #header>
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-lg font-semibold text-highlighted">对话指引</h3>
                <p class="text-sm text-muted">支持工具调用意图识别与多模型选择。</p>
              </div>
              <UIcon name="i-lucide-sparkles" class="size-5 text-primary" />
            </div>
          </template>

          <div class="space-y-4 text-sm text-muted">
            <p>
              直接输入需求，例如“帮我计算 23*19”或“现在上海几点”。系统会根据意图选择是否需要工具调用。
            </p>
            <div class="rounded-xl border border-default bg-default px-4 py-3">
              <div class="text-xs uppercase text-muted/80">提示</div>
              <ul class="mt-2 list-disc pl-4">
                <li>若出现错误，请尝试切换模型或刷新页面。</li>
                <li>SSE 需要保持页面在前台以保证连接稳定。</li>
                <li>建议在正式环境使用同源代理，避免跨域限制。</li>
              </ul>
            </div>
          </div>
        </UCard>

        <UCard class="border-default bg-default/90 backdrop-blur">
          <template #header>
            <div class="flex flex-wrap items-center justify-between gap-3">
              <div class="flex items-center gap-3">
                <div class="flex size-10 items-center justify-center rounded-xl bg-primary/10 text-primary">
                  <UIcon name="i-lucide-message-circle" class="size-5" />
                </div>
                <div>
                  <div class="text-sm text-muted">当前模型</div>
                  <div class="text-base font-semibold text-highlighted">SSE 对话通道</div>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <USelect
                  v-model="modelType"
                  :items="modelOptions"
                  class="min-w-[160px]"
                  size="sm"
                />
                <UButton
                  label="清空"
                  variant="outline"
                  color="neutral"
                  size="sm"
                  @click="handleClear"
                />
              </div>
            </div>
          </template>

          <div class="flex h-[520px] flex-col">
            <div class="flex-1 overflow-y-auto px-2 py-4">
              <div v-if="!hasMessages" class="flex h-full flex-col items-center justify-center gap-3 text-center">
                <UIcon name="i-lucide-bot" class="text-muted text-4xl" />
                <div>
                  <p class="text-sm font-medium text-highlighted">开启对话</p>
                  <p class="text-xs text-muted">输入问题，SSE 将实时返回结果。</p>
                </div>
              </div>
              <div v-else class="space-y-3">
                <div
                  v-for="msg in messages"
                  :key="msg.id"
                  class="space-y-1"
                >
                  <p
                    class="text-xs text-muted"
                    :class="msg.role === 'user' ? 'text-right' : 'text-left'"
                  >
                    {{ msg.time }}
                  </p>
                  <UChatMessage
                    :id="msg.id"
                    :role="msg.role"
                    :parts="msg.parts"
                    :side="msg.role === 'user' ? 'right' : 'left'"
                    :avatar="msg.role === 'assistant' ? { icon: 'i-lucide-bot', alt: 'Agent' } : undefined"
                  />
                </div>
              </div>
            </div>

            <div class="border-default border-t px-3 py-3">
              <UChatPrompt
                v-model="inputText"
                placeholder="输入你的问题（Enter 发送）"
                variant="subtle"
                :disabled="sending"
                @submit="handleSend"
              >
                <template #footer>
                  <div class="flex w-full items-center justify-between gap-3">
                    <div class="text-xs text-muted">
                      {{ error || '连接状态：' + statusLabel }}
                    </div>
                    <UChatPromptSubmit
                      color="primary"
                      size="sm"
                      :loading="sending"
                      :disabled="!canSend"
                    />
                  </div>
                </template>
              </UChatPrompt>
            </div>
          </div>
        </UCard>
      </div>
    </UPageSection>
  </div>
</template>
