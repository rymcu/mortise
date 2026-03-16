<script setup lang="ts">
import type { SiteConfigSaveRequest } from '~/types'

/**
 * 网站配置管理页面
 * 提供 Tab 切换不同配置分组，查看并编辑各分组配置参数。
 * 参考 notification-channels.vue 的实现方式。
 */
const { groups, loading, saving, errorMessage, loadGroups, saveGroup } = useSiteConfig()

definePageMeta({
  alias: ['/systems/website-config']
})

await loadGroups()

// 当前激活的 Tab（默认选第一个分组）
const activeGroup = ref(groups.value[0]?.group ?? '')

const activeGroupData = computed(
  () => groups.value.find(g => g.group === activeGroup.value) ?? null
)

const toast = useToast()

async function handleSave(request: SiteConfigSaveRequest) {
  const ok = await saveGroup(activeGroup.value, request)
  if (ok) {
    toast.add({ title: '保存成功', color: 'success' })
  } else {
    toast.add({ title: '保存失败', description: errorMessage.value, color: 'error' })
  }
}
</script>

<template>
  <UDashboardPanel id="system-site-config">
    <template #header>
      <UDashboardNavbar title="网站配置">
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

        <template v-else-if="groups.length > 0">
          <!-- 分组 Tab -->
          <UTabs
            v-model="activeGroup"
            :items="groups.map(g => ({ label: g.label, value: g.group }))"
            class="mb-4"
          />

          <!-- 配置卡片 -->
          <SiteConfigGroupCard
            v-if="activeGroupData"
            :group="activeGroupData"
            :saving="saving"
            @save="handleSave"
          />
        </template>

        <!-- 空状态 -->
        <div
          v-else
          class="text-center text-muted py-12"
        >
          暂无配置数据
        </div>
      </div>
    </template>
  </UDashboardPanel>
</template>
