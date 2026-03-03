<script setup lang="ts">
import type {ChannelConfigSaveRequest} from "~/types";

/**
 * 通知渠道配置管理页面
 * 提供 Tab 切换不同渠道，查看并编辑各渠道配置参数
 */
const { channels, loading, saving, errorMessage, loadChannels, saveChannel }
  = useNotificationChannels()

await loadChannels()

// 当前激活的 Tab（默认选第一个渠道）
const activeChannel = ref(channels.value[0]?.channel ?? '')

const activeChannelData = computed(
  () => channels.value.find(c => c.channel === activeChannel.value) ?? null
)

const toast = useToast()

async function handleSave(request: ChannelConfigSaveRequest) {
  const ok = await saveChannel(activeChannel.value, request)
  if (ok) {
    toast.add({ title: '保存成功', color: 'success' })
  }
  else {
    toast.add({ title: '保存失败', description: errorMessage.value, color: 'error' })
  }
}
</script>

<template>
  <UDashboardPanel id="system-notification-channels">
    <template #header>
      <UDashboardNavbar title="通知渠道配置">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="p-4">
        <!-- 错误提示 -->
        <UAlert
          v-if="errorMessage && !loading"
          color="error"
          variant="soft"
          :title="errorMessage"
          class="mb-4"
        />

        <!-- 骨架屏 -->
        <div v-if="loading" class="space-y-3">
          <USkeleton
            v-for="n in 5"
            :key="n"
            class="h-10 w-full"
          />
        </div>

        <template v-else-if="channels.length > 0">
          <!-- 渠道 Tab 列表 -->
          <UTabs
            v-model="activeChannel"
            :items="channels.map(c => ({ label: c.label, value: c.channel }))"
            class="mb-4"
          />

          <!-- 渠道配置卡片 -->
          <NotificationChannelsChannelConfigCard
            v-if="activeChannelData"
            :channel="activeChannelData"
            :saving="saving"
            @save="handleSave"
          />
        </template>

        <!-- 空状态 -->
        <div
          v-else
          class="text-center text-muted py-12"
        >
          暂无通知渠道配置
        </div>
      </div>
    </template>
  </UDashboardPanel>
</template>
