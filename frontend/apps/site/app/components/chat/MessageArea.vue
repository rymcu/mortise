<script setup lang="ts">
import type { SiteChatMessage } from '~/types/im'

const props = defineProps<{
  messages: SiteChatMessage[]
  isSessionClosed: boolean
  loading: boolean
  sending: boolean
  error: string | null
}>()

const inputText = defineModel<string>('inputText', { default: '' })

const emit = defineEmits<{
  submit: []
  retry: []
  newConsult: []
}>()
</script>

<template>
  <!-- 加载中 -->
  <div
    v-if="props.loading"
    class="flex flex-1 flex-col items-center justify-center"
  >
    <UIcon name="i-lucide-loader-circle" class="text-muted animate-spin text-3xl" />
    <p class="text-muted mt-2 text-sm">正在连接客服...</p>
  </div>

  <!-- 需要登录 -->
  <div
    v-else-if="props.error === 'auth'"
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
        @click="emit('retry')"
      />
      <p class="text-muted text-xs">
        或发邮件至
        <a href="mailto:ronger@rymcu.com" class="text-primary underline">ronger@rymcu.com</a>
      </p>
    </div>
  </div>

  <!-- 网络错误 -->
  <div
    v-else-if="props.error === 'network'"
    class="flex flex-1 flex-col items-center justify-center gap-4 px-6 text-center"
  >
    <UIcon name="i-lucide-wifi-off" class="text-muted text-4xl" />
    <p class="text-muted text-sm">连接失败，请稍后重试</p>
    <UButton
      label="重试"
      variant="outline"
      color="neutral"
      @click="emit('retry')"
    />
  </div>

  <!-- 聊天区 -->
  <template v-else>
    <!-- 消息列表 -->
    <div class="flex-1 overflow-y-auto px-3 py-3">
      <!-- 欢迎语 -->
      <div v-if="props.messages.length === 0" class="mb-3">
        <UChatMessage
          id="welcome"
          role="assistant"
          :parts="[{ type: 'text', text: '您好，欢迎咨询！请描述您的需求，客服将尽快回复。' }]"
          side="left"
          :avatar="{ icon: 'i-lucide-headset', alt: '客服' }"
        />
      </div>
      <!-- 消息记录 -->
      <div
        v-for="msg in props.messages"
        :key="msg.id"
        class="mb-1 last:mb-0"
      >
        <p
          class="text-muted mb-0.5 text-xs"
          :class="msg.role === 'user' ? 'text-right' : 'text-left'"
        >
          {{ msg.time }}
        </p>
        <UChatMessage
          :id="msg.id"
          :role="msg.role"
          :parts="msg.parts"
          :side="msg.role === 'user' ? 'right' : 'left'"
          :avatar="
            msg.role === 'user'
              ? undefined
              : { icon: 'i-lucide-headset', alt: '客服' }
          "
        />
      </div>
      <!-- 会话已关闭分隔线 -->
      <div v-if="props.isSessionClosed && props.messages.length > 0" class="flex items-center gap-2 py-4 opacity-50">
        <div class="border-default flex-1 border-t border-dashed" />
        <span class="text-muted text-xs">本次咨询已结束</span>
        <div class="border-default flex-1 border-t border-dashed" />
      </div>
    </div>

    <!-- 输入区（会话进行中） -->
    <div v-if="!props.isSessionClosed" class="border-default shrink-0 border-t px-3 pb-3 pt-2">
      <UChatPrompt
        v-model="inputText"
        placeholder="输入问题（Enter 发送）"
        variant="subtle"
        :disabled="props.sending"
        @submit="emit('submit')"
      >
        <template #footer>
          <div class="flex w-full items-center justify-end">
            <UChatPromptSubmit
              color="primary"
              size="sm"
              :loading="props.sending"
              :disabled="!inputText?.trim() || props.sending"
            />
          </div>
        </template>
      </UChatPrompt>
    </div>

    <!-- 会话已结束操作区 -->
    <div
      v-else
      class="border-default shrink-0 border-t px-4 py-3 text-center"
    >
      <p class="text-muted mb-2 text-xs">如需再次咨询，可发起新会话</p>
      <UButton
        label="发起新咨询"
        color="primary"
        block
        @click="emit('newConsult')"
      />
    </div>
  </template>
</template>
