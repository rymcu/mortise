<script setup lang="ts">
/**
 * 消息收件箱页面
 */

const messages = ref<Array<{
  id: number
  title: string
  content: string
  time: string
  read: boolean
}>>([])

const loading = ref(false)
</script>

<template>
  <UDashboardPanel id="inbox">
    <template #header>
      <UDashboardNavbar title="消息中心">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="max-w-3xl mx-auto">
        <UCard>
          <div v-if="loading" class="py-8 text-center text-muted">
            加载中...
          </div>
          <div v-else-if="messages.length === 0" class="py-12 text-center">
            <UIcon name="i-lucide-inbox" class="text-4xl text-muted mb-3" />
            <p class="text-muted">暂无消息</p>
          </div>
          <div v-else class="divide-y divide-default">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="flex items-start gap-3 py-3 px-2 hover:bg-elevated/50 cursor-pointer transition-colors"
            >
              <div class="mt-1">
                <span
                  class="inline-block h-2 w-2 rounded-full"
                  :class="msg.read ? 'bg-transparent' : 'bg-primary'"
                />
              </div>
              <div class="flex-1 min-w-0">
                <div class="font-medium text-sm" :class="{ 'text-muted': msg.read }">
                  {{ msg.title }}
                </div>
                <div class="text-xs text-muted mt-0.5 truncate">{{ msg.content }}</div>
              </div>
              <div class="text-xs text-muted whitespace-nowrap">{{ msg.time }}</div>
            </div>
          </div>
        </UCard>
      </div>
    </template>
  </UDashboardPanel>
</template>
