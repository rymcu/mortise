<script setup lang="ts">
/**
 * 个人设置页面
 */
const auth = useAuthStore()
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
      <div class="max-w-2xl mx-auto space-y-6">
        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold">基本信息</h3>
          </template>
          <div class="space-y-4">
            <div class="flex items-center gap-4">
              <div class="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center">
                <UIcon name="i-lucide-user" class="text-2xl text-primary" />
              </div>
              <div>
                <div class="font-medium">{{ auth.user?.nickname || auth.user?.account || '-' }}</div>
                <div class="text-sm text-muted">{{ auth.user?.email || '-' }}</div>
              </div>
            </div>
          </div>
        </UCard>

        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold">账号安全</h3>
          </template>
          <div class="space-y-3">
            <div class="flex items-center justify-between py-2">
              <div>
                <div class="font-medium">修改密码</div>
                <div class="text-sm text-muted">定期修改密码以保障账号安全</div>
              </div>
              <UButton variant="soft" size="sm">修改</UButton>
            </div>
          </div>
        </UCard>
      </div>
    </template>
  </UDashboardPanel>
</template>
