<script setup lang="ts">
useSeoMeta({
  title: 'Agent Chat',
  description: '使用 SSE 与 Mortise Agent 实时对话',
})

const { messages, status, error, send, stop, clear } = useAgentChat()
const { providers, defaultModel, loading: modelsLoading, parseModelValue, fetchModels } = useAgentModels()

const input = ref('')
const selectedModelValue = ref('')

const hasMessages = computed(() => messages.value.length > 0)

// 页面加载时获取模型列表
onMounted(async () => {
  await fetchModels()
  if (!selectedModelValue.value && defaultModel.value) {
    selectedModelValue.value = defaultModel.value
  }
})

function handleSubmit() {
  if (!input.value.trim() || status.value === 'streaming') return
  const { modelType, modelName } = parseModelValue(selectedModelValue.value)
  send(input.value, modelType, modelName)
  input.value = ''
}

function handleClear() {
  clear()
}

const quickPrompts = [
  { label: '帮我计算 23 × 19', icon: 'i-lucide-calculator' },
  { label: '现在上海几点？', icon: 'i-lucide-clock' },
  { label: '用 Java 写一个快速排序', icon: 'i-lucide-code' },
  { label: '解释一下 Spring Security 的原理', icon: 'i-lucide-shield' },
]
</script>

<template>
  <UContainer class="flex min-h-[calc(100vh-var(--header-height,64px))] flex-col py-4 sm:py-6">
    <!-- 无消息时：欢迎页 -->
    <div
      v-if="!hasMessages"
      class="flex flex-1 flex-col items-center justify-center gap-6"
    >
      <div class="text-center">
        <div class="mx-auto mb-4 flex size-16 items-center justify-center rounded-2xl bg-primary/10 text-primary">
          <UIcon name="i-lucide-bot" class="size-8" />
        </div>
        <h1 class="text-2xl font-bold text-highlighted sm:text-3xl">
          Mortise Agent
        </h1>
        <p class="mt-2 text-sm text-muted">
          选择模型，输入问题，实时获取 AI 回复。
        </p>
      </div>

      <div class="flex flex-wrap justify-center gap-2">
        <UButton
          v-for="prompt in quickPrompts"
          :key="prompt.label"
          :icon="prompt.icon"
          :label="prompt.label"
          size="sm"
          color="neutral"
          variant="outline"
          class="rounded-full"
          @click="input = prompt.label; handleSubmit()"
        />
      </div>

      <UChatPrompt
        v-model="input"
        placeholder="输入你的问题（Enter 发送）"
        variant="subtle"
        class="w-full max-w-2xl"
        :ui="{ base: 'px-1.5' }"
        @submit="handleSubmit"
      >
        <template #footer>
          <div class="flex items-center gap-1">
            <ChatModelSelect
              v-model="selectedModelValue"
              :providers="providers"
              :loading="modelsLoading"
            />
          </div>

          <UChatPromptSubmit
            color="neutral"
            size="sm"
          />
        </template>
      </UChatPrompt>
    </div>

    <!-- 有消息时：对话区 -->
    <template v-else>
      <UChatMessages
        should-auto-scroll
        :messages="messages"
        :status="status === 'streaming' ? 'streaming' : 'ready'"
        :spacing-offset="140"
        class="flex-1 pb-4 sm:pb-6"
      >
        <template #content="{ message }">
          <template
            v-for="(part, index) in message.parts"
            :key="`${message.id}-${index}`"
          >
            <p
              v-if="part.type === 'text'"
              class="whitespace-pre-wrap"
            >
              {{ part.text }}
            </p>
          </template>
        </template>
      </UChatMessages>

      <UChatPrompt
        v-model="input"
        placeholder="继续提问（Enter 发送）"
        variant="subtle"
        :error="error ? new Error(error) : undefined"
        class="sticky bottom-0 z-10 rounded-b-none"
        :ui="{ base: 'px-1.5' }"
        @submit="handleSubmit"
      >
        <template #footer>
          <div class="flex items-center gap-1">
            <ChatModelSelect
              v-model="selectedModelValue"
              :providers="providers"
              :loading="modelsLoading"
            />

            <UButton
              icon="i-lucide-trash-2"
              size="sm"
              color="neutral"
              variant="ghost"
              @click="handleClear"
            />
          </div>

          <UChatPromptSubmit
            :status="status === 'streaming' ? 'streaming' : 'ready'"
            color="neutral"
            size="sm"
            @stop="stop()"
          />
        </template>
      </UChatPrompt>
    </template>
  </UContainer>
</template>
