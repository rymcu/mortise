<script setup lang="ts">
/**
 * 通知设置页面
 */
</script>

<template>
  <UDashboardPanel id="settings-notifications">
    <template #header>
      <UDashboardNavbar title="通知设置">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="max-w-2xl mx-auto space-y-6">
        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold">通知偏好</h3>
          </template>
          <div class="space-y-4">
            <div class="flex items-center justify-between py-2">
              <div>
                <div class="font-medium">系统通知</div>
                <div class="text-sm text-muted">接收系统维护、更新等通知</div>
              </div>
              <input type="checkbox" checked class="accent-primary h-4 w-4" />
            </div>
            <div class="flex items-center justify-between py-2">
              <div>
                <div class="font-medium">安全通知</div>
                <div class="text-sm text-muted">接收登录异常、密码修改等安全通知</div>
              </div>
              <input type="checkbox" checked class="accent-primary h-4 w-4" />
            </div>
            <div class="flex items-center justify-between py-2">
              <div>
                <div class="font-medium">邮件通知</div>
                <div class="text-sm text-muted">通过邮件接收重要通知</div>
              </div>
              <input type="checkbox" class="accent-primary h-4 w-4" />
            </div>
          </div>
        </UCard>
      </div>
    </template>
  </UDashboardPanel>
</template>
