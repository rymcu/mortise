<script setup lang="ts">
/**
 * 个人设置页面
 */
const { profile, fetchProfile } = useProfile()

onMounted(() => {
  fetchProfile()
})
</script>

<template>
  <UDashboardPanel id="settings">
    <template #header>
      <UDashboardNavbar title="个人设置">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="mx-auto max-w-2xl space-y-6">
        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold">个人资料</h3>
          </template>
          <div class="space-y-4">
            <div class="flex items-center gap-4">
              <div
                class="bg-primary/10 flex h-16 w-16 items-center justify-center rounded-full"
              >
                <UIcon name="i-lucide-user" class="text-primary text-2xl" />
              </div>
              <div>
                <div class="font-medium">
                  {{ profile?.nickname || profile?.account || '-' }}
                </div>
                <div class="text-muted text-sm">{{ profile?.email || '-' }}</div>
              </div>
            </div>

            <div class="flex justify-end">
              <UButton to="/settings/profile" variant="soft" size="sm"
                >编辑资料</UButton
              >
            </div>
          </div>
        </UCard>

        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold">设置功能</h3>
          </template>
          <div class="space-y-4">
            <div class="flex items-center justify-between py-1">
              <div>
                <div class="font-medium">资料</div>
                <div class="text-muted text-sm">查看并更新个人资料信息</div>
              </div>
              <UButton to="/settings/profile" variant="soft" size="sm"
                >进入</UButton
              >
            </div>

            <div class="flex items-center justify-between py-1">
              <div>
                <div class="font-medium">消息</div>
                <div class="text-muted text-sm">管理系统与安全通知偏好</div>
              </div>
              <UButton to="/settings/notifications" variant="soft" size="sm"
                >进入</UButton
              >
            </div>

            <div class="flex items-center justify-between py-1">
              <div>
                <div class="font-medium">安全</div>
                <div class="text-muted text-sm">修改密码并检查密码强度限制</div>
              </div>
              <UButton to="/settings/security" variant="soft" size="sm"
                >进入</UButton
              >
            </div>
          </div>
        </UCard>
      </div>
    </template>
  </UDashboardPanel>
</template>
