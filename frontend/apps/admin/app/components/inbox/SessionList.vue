<script setup lang="ts">
/**
 * 会话列表侧边栏
 *
 * 展示所有用户咨询会话，支持选中、重试加载。
 */
import type { ChatSession } from '~/types/inbox'

const props = defineProps<{
  sessions: ChatSession[]
  sessionsLoading: boolean
  sessionsError: string
  selectedSessionId: number | null
}>()

const emit = defineEmits<{
  select: [session: ChatSession]
  reload: []
}>()

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
</script>

<template>
  <div class="border-default flex w-72 shrink-0 flex-col border-r">
    <div class="border-default border-b px-4 py-3">
      <p class="text-sm font-medium">用户咨询</p>
      <p class="text-muted text-xs">{{ props.sessions.length }} 个会话</p>
    </div>

    <div class="flex-1 overflow-y-auto">
      <div
        v-if="props.sessionsLoading && props.sessions.length === 0"
        class="text-muted py-8 text-center text-sm"
      >
        加载中...
      </div>
      <div
        v-else-if="props.sessionsError"
        class="flex flex-col items-center py-8 text-center"
      >
        <UIcon name="i-lucide-alert-circle" class="text-error mb-2 text-2xl" />
        <p class="text-muted text-xs">{{ props.sessionsError }}</p>
        <UButton
          size="xs"
          variant="ghost"
          class="mt-2"
          @click="emit('reload')"
        >
          重试
        </UButton>
      </div>
      <div
        v-else-if="props.sessions.length === 0"
        class="py-12 text-center"
      >
        <UIcon
          name="i-lucide-message-square"
          class="text-muted mb-2 text-3xl"
        />
        <p class="text-muted text-sm">暂无咨询会话</p>
      </div>
      <div
        v-for="session in props.sessions"
        v-else
        :key="session.id"
        class="hover:bg-elevated/60 flex cursor-pointer items-start gap-3 px-4 py-3 transition-colors"
        :class="{ 'bg-elevated': props.selectedSessionId === session.id }"
        @click="emit('select', session)"
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
</template>
