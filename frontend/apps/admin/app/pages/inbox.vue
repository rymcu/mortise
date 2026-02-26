<script setup lang="ts">
/**
 * 消息收件箱页面
 */

const messages = ref<
  Array<{
    id: number
    title: string
    content: string
    time: string
    read: boolean
  }>
>([])

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
      <div class="mx-auto max-w-3xl">
        <UCard>
          <div v-if="loading" class="text-muted py-8 text-center">
            加载中...
          </div>
          <div v-else-if="messages.length === 0" class="py-12 text-center">
            <UIcon name="i-lucide-inbox" class="text-muted mb-3 text-4xl" />
            <p class="text-muted">暂无消息</p>
          </div>
          <div v-else class="divide-default divide-y">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="hover:bg-elevated/50 flex cursor-pointer items-start gap-3 px-2 py-3 transition-colors"
            >
              <div class="mt-1">
                <span
                  class="inline-block h-2 w-2 rounded-full"
                  :class="msg.read ? 'bg-transparent' : 'bg-primary'"
                />
              </div>
              <div class="min-w-0 flex-1">
                <div
                  class="text-sm font-medium"
                  :class="{ 'text-muted': msg.read }"
                >
                  {{ msg.title }}
                </div>
                <div class="text-muted mt-0.5 truncate text-xs">
                  {{ msg.content }}
                </div>
              </div>
              <div class="text-muted text-xs whitespace-nowrap">
                {{ msg.time }}
              </div>
            </div>
          </div>
        </UCard>
      </div>
    </template>
  </UDashboardPanel>
</template>
