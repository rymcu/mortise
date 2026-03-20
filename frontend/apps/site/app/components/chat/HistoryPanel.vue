<script setup lang="ts">
import type { SiteChatMessage, SiteSessionItem } from '~/types/im'
import { SESSION_STATUS } from '~/types/im'

const props = defineProps<{
  view: 'history' | 'detail'
  sessions: SiteSessionItem[]
  sessionsLoading: boolean
  historyMessages: SiteChatMessage[]
  historyLoading: boolean
}>()

const emit = defineEmits<{
  openDetail: [item: SiteSessionItem]
}>()

// ── 辅助格式化 ────────────────────────────────────────────────────────────

function statusLabel(status: number): string {
  if (status === SESSION_STATUS.CLOSED) return '已结束'
  if (status === SESSION_STATUS.OPEN) return '进行中'
  return '等待接入'
}

function statusColor(status: number): 'neutral' | 'success' {
  return status === SESSION_STATUS.CLOSED ? 'neutral' : 'success'
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<template>
  <!-- ── 历史列表 ── -->
  <template v-if="props.view === 'history'">
    <div v-if="props.sessionsLoading" class="flex flex-1 flex-col items-center justify-center">
      <UIcon name="i-lucide-loader-circle" class="text-muted animate-spin text-3xl" />
    </div>
    <div
      v-else-if="props.sessions.length === 0"
      class="flex flex-1 flex-col items-center justify-center gap-2 px-6 text-center"
    >
      <UIcon name="i-lucide-message-square-x" class="text-muted text-4xl" />
      <p class="text-muted text-sm">暂无历史咨询记录</p>
    </div>
    <div v-else class="divide-default flex-1 divide-y overflow-y-auto">
      <UButton
        v-for="item in props.sessions"
        :key="item.id"
        block
        color="neutral"
        variant="ghost"
        class="justify-start rounded-none px-4 py-3 text-left transition-colors hover:bg-muted/50"
        @click="emit('openDetail', item)"
      >
        <div class="mb-1 flex items-center justify-between">
          <UBadge
            :label="statusLabel(item.status)"
            :color="statusColor(item.status)"
            variant="subtle"
            size="xs"
          />
          <span class="text-muted text-xs">{{ formatDate(item.updatedTime) }}</span>
        </div>
        <p class="text-muted truncate text-sm">{{ item.lastMessage || '（无消息）' }}</p>
      </UButton>
    </div>
  </template>

  <!-- ── 历史详情（只读） ── -->
  <template v-else>
    <div v-if="props.historyLoading" class="flex flex-1 flex-col items-center justify-center">
      <UIcon name="i-lucide-loader-circle" class="text-muted animate-spin text-3xl" />
    </div>
    <div v-else class="flex-1 overflow-y-auto px-3 py-3">
      <div v-if="props.historyMessages.length === 0" class="flex flex-1 flex-col items-center justify-center gap-2 pt-8 text-center">
        <p class="text-muted text-sm">该会话无消息记录</p>
      </div>
      <template v-else>
        <div
          v-for="msg in props.historyMessages"
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
            :avatar="msg.role === 'user' ? undefined : { icon: 'i-lucide-headset', alt: '客服' }"
          />
        </div>
        <!-- 会话结束分隔线 -->
        <div class="flex items-center gap-2 py-4 opacity-50">
          <div class="border-default flex-1 border-t border-dashed" />
          <span class="text-muted text-xs">会话已结束</span>
          <div class="border-default flex-1 border-t border-dashed" />
        </div>
      </template>
    </div>
  </template>
</template>
