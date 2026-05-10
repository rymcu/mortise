<script setup lang="ts">
import type { SecurityDeviceSummary } from '~/types/security'

definePageMeta({
  middleware: 'auth'
})

useSeoMeta({
  title: '账号安全中心',
  description: '管理已授权的 Rodak 桌面设备'
})

const toast = useToast()
const { sessions, loading, error, fetchSessions, revokeSession } = useMemberSecurity()

await useAsyncData('standalone-member-security', () => fetchSessions())

const activeSessions = computed(() => sessions.value.filter(session => session.status === 'active'))
const revokedSessions = computed(() => sessions.value.filter(session => session.status !== 'active'))
const summaries = computed<SecurityDeviceSummary[]>(() => [
  { label: '已授权设备', value: String(sessions.value.length), icon: 'i-lucide-monitor-check' },
  { label: '活跃会话', value: String(activeSessions.value.length), icon: 'i-lucide-shield-check' },
  { label: '已撤销', value: String(revokedSessions.value.length), icon: 'i-lucide-shield-x' }
])

async function handleRefresh() {
  await fetchSessions()
}

function formatTime(value?: string | null): string {
  if (!value) {
    return '暂无记录'
  }
  const date = parseBackendDateTime(value)
  if (!date) {
    return '暂无记录'
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

function parseBackendDateTime(value: string): Date | null {
  const normalized = value.trim().replace(' ', 'T')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

function statusLabel(status: string): string {
  return status === 'active' ? '使用中' : '已撤销'
}

function statusColor(status: string): 'success' | 'neutral' {
  return status === 'active' ? 'success' : 'neutral'
}

async function handleRevoke(sessionId: string) {
  const ok = await revokeSession(sessionId)
  toast.add({
    title: ok ? '设备会话已撤销' : error.value || '撤销设备会话失败',
    color: ok ? 'success' : 'error',
    icon: ok ? 'i-lucide-check' : 'i-lucide-circle-alert'
  })
}
</script>

<template>
  <UContainer class="max-w-6xl py-8 sm:py-10">
    <section class="mb-8">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 class="text-2xl font-semibold tracking-tight sm:text-3xl">账号安全中心</h1>
          <p class="mt-2 max-w-2xl text-sm leading-6 text-muted">
            管理 Rodak 桌面端的浏览器授权会话。独立部署模板会继续使用当前 API Base 调用后端。
          </p>
        </div>
        <UButton
          icon="i-lucide-refresh-cw"
          color="neutral"
          variant="soft"
          :loading="loading"
          @click="handleRefresh"
        >
          刷新
        </UButton>
      </div>
    </section>

    <div class="mb-6 grid gap-4 sm:grid-cols-3">
      <UCard v-for="item in summaries" :key="item.label">
        <div class="flex items-center justify-between gap-4">
          <div>
            <div class="text-sm text-muted">{{ item.label }}</div>
            <div class="mt-2 text-2xl font-semibold">{{ item.value }}</div>
          </div>
          <UIcon :name="item.icon" class="size-6 text-primary" />
        </div>
      </UCard>
    </div>

    <UAlert
      v-if="error"
      class="mb-6"
      color="error"
      variant="soft"
      icon="i-lucide-circle-alert"
      :title="error"
    />

    <UCard>
      <template #header>
        <div>
          <h2 class="text-base font-semibold">Rodak 授权设备</h2>
          <p class="mt-1 text-sm text-muted">每台安装设备都是独立会话，撤销只影响所选设备。</p>
        </div>
      </template>

      <div v-if="!sessions.length && !loading" class="py-12 text-center">
        <UIcon name="i-lucide-monitor-off" class="mx-auto size-9 text-muted" />
        <p class="mt-3 text-sm text-muted">还没有 Rodak 桌面授权记录</p>
      </div>

      <div v-else class="divide-y divide-muted">
        <div
          v-for="session in sessions"
          :key="session.id"
          class="flex flex-col gap-4 py-4 sm:flex-row sm:items-center sm:justify-between"
        >
          <div class="min-w-0">
            <div class="flex flex-wrap items-center gap-2">
              <span class="font-medium">{{ session.deviceName || '未知设备' }}</span>
              <UBadge :color="statusColor(session.status)" variant="soft">
                {{ statusLabel(session.status) }}
              </UBadge>
            </div>
            <div class="mt-2 grid gap-1 text-sm text-muted sm:grid-cols-2">
              <span>客户端：{{ session.clientName || session.clientId || '未知客户端' }}</span>
              <span>最近活跃：{{ formatTime(session.lastActiveAt) }}</span>
              <span>授权时间：{{ formatTime(session.createdTime) }}</span>
              <span v-if="session.revokedAt">撤销时间：{{ formatTime(session.revokedAt) }}</span>
            </div>
          </div>
          <UButton
            v-if="session.status === 'active'"
            icon="i-lucide-ban"
            color="error"
            variant="soft"
            :loading="loading"
            @click="handleRevoke(session.id)"
          >
            撤销
          </UButton>
        </div>
      </div>
    </UCard>
  </UContainer>
</template>
