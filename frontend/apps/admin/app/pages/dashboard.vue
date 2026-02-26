<script setup lang="ts">
const { $api } = useNuxtApp()

interface DashboardStats {
  userCount: number
  roleCount: number
  menuCount: number
  memberCount: number
}

const stats = ref<DashboardStats>({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  memberCount: 0
})

const loading = ref(true)

// 尝试加载统计数据（接口可选，失败时保持默认值）
onMounted(async () => {
  try {
    const res = await $api<{ code: number; data: DashboardStats }>(
      '/api/v1/admin/dashboard/stats'
    )
    if (res?.data) {
      stats.value = res.data
    }
  } catch {
    // 统计接口暂未实现时忽略
  } finally {
    loading.value = false
  }
})

const statCards = computed(() => [
  {
    title: '用户数',
    icon: 'i-lucide-user',
    value: stats.value.userCount,
    color: 'text-primary'
  },
  {
    title: '角色数',
    icon: 'i-lucide-shield',
    value: stats.value.roleCount,
    color: 'text-blue-500'
  },
  {
    title: '菜单数',
    icon: 'i-lucide-layout-grid',
    value: stats.value.menuCount,
    color: 'text-amber-500'
  },
  {
    title: '会员数',
    icon: 'i-lucide-users',
    value: stats.value.memberCount,
    color: 'text-emerald-500'
  }
])
</script>

<template>
  <UDashboardPanel id="dashboard">
    <template #header>
      <UDashboardNavbar title="Dashboard">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-6 p-6">
        <!-- 统计卡片 -->
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <UCard v-for="card in statCards" :key="card.title">
            <div class="flex items-center gap-4">
              <div class="bg-primary/10 rounded-lg p-2.5">
                <UIcon
                  :name="card.icon"
                  :class="[card.color, 'h-5 w-5 shrink-0']"
                />
              </div>
              <div>
                <p class="text-muted text-sm">{{ card.title }}</p>
                <p class="text-highlighted text-2xl font-semibold">
                  {{ loading ? '...' : card.value }}
                </p>
              </div>
            </div>
          </UCard>
        </div>

        <!-- 快捷操作 -->
        <UCard>
          <template #header>
            <h3 class="text-highlighted text-base font-semibold">快捷操作</h3>
          </template>
          <div class="flex flex-wrap gap-2">
            <UButton
              to="/system/users"
              color="primary"
              variant="soft"
              icon="i-lucide-user"
            >
              用户管理
            </UButton>
            <UButton
              to="/system/roles"
              color="neutral"
              variant="soft"
              icon="i-lucide-shield"
            >
              角色管理
            </UButton>
            <UButton
              to="/system/menus"
              color="neutral"
              variant="soft"
              icon="i-lucide-layout-grid"
            >
              菜单管理
            </UButton>
            <UButton
              to="/members"
              color="neutral"
              variant="soft"
              icon="i-lucide-users"
            >
              会员管理
            </UButton>
          </div>
        </UCard>
      </div>
    </template>
  </UDashboardPanel>
</template>
