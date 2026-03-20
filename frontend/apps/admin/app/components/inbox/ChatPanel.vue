<script setup lang="ts">
/**
 * 聊天面板（右侧消息区）
 *
 * 展示选中会话的消息列表、顶栏信息和回复输入。
 */
import type { ChatSession, InboxChatMessage } from '~/types/inbox'

const props = defineProps<{
  selectedSession: ChatSession | null
  currentMessages: InboxChatMessage[]
  messagesLoading: boolean
  replyText: string
  replying: boolean
}>()

const emit = defineEmits<{
  'update:replyText': [value: string]
  'submit': []
}>()

const localReplyText = computed({
  get: () => props.replyText,
  set: (v: string) => emit('update:replyText', v),
})
</script>

<template>
  <div class="flex min-w-0 flex-1 flex-col">
    <!-- 未选中占位 -->
    <div
      v-if="!props.selectedSession"
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
            :src="props.selectedSession.userAvatar || undefined"
            :alt="props.selectedSession.userName || '用户'"
            size="sm"
          />
          <div>
            <p class="text-sm font-medium">
              {{ props.selectedSession.userName || `用户 ${props.selectedSession.userId}` }}
            </p>
            <p class="text-muted text-xs">
              用户 ID：{{ props.selectedSession.userId }}
              <template v-if="props.selectedSession.contextTitle">
                · {{ props.selectedSession.contextTitle }}
              </template>
            </p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <UBadge
            :label="props.selectedSession.status === 0 ? '进行中' : props.selectedSession.status === 2 ? '等待中' : '已关闭'"
            :color="props.selectedSession.status === 0 ? 'success' : props.selectedSession.status === 2 ? 'warning' : 'neutral'"
            variant="subtle"
            size="xs"
          />
        </div>
      </div>

      <!-- 消息列表区 -->
      <div class="flex-1 overflow-y-auto px-4 py-4">
        <div
          v-if="props.messagesLoading && props.currentMessages.length === 0"
          class="text-muted py-8 text-center text-sm"
        >
          <UIcon name="i-lucide-loader-circle" class="animate-spin text-2xl" />
        </div>
        <div
          v-else-if="props.currentMessages.length === 0"
          class="py-12 text-center"
        >
          <p class="text-muted text-sm">暂无消息记录</p>
        </div>
        <div
          v-for="msg in props.currentMessages"
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
                ? { src: props.selectedSession.userAvatar || undefined, alt: props.selectedSession.userName || '用户' }
                : { icon: 'i-lucide-headset', alt: '客服' }
            "
          />
        </div>
      </div>

      <!-- 回复输入 -->
      <div class="border-default shrink-0 border-t px-4 pb-4 pt-3">
        <UChatPrompt
          v-model="localReplyText"
          placeholder="输入回复内容（Enter 发送）"
          variant="subtle"
          :disabled="props.replying"
          @submit="emit('submit')"
        >
          <template #footer>
            <div class="flex w-full items-center justify-end">
              <UChatPromptSubmit
                color="primary"
                size="sm"
                :loading="props.replying"
                :disabled="!localReplyText.trim() || props.replying"
              />
            </div>
          </template>
        </UChatPrompt>
      </div>
    </template>
  </div>
</template>
