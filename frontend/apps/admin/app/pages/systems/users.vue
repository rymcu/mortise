<script setup lang="ts">
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'

interface UserInfo {
  id: number
  account?: string
  nickname?: string
  email?: string
  phone?: string
  status?: number
  roleNames?: string
  lastLoginTime?: string
}

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'account', label: '账号' },
  { key: 'nickname', label: '昵称' },
  { key: 'email', label: '邮箱' },
  { key: 'phone', label: '手机号' },
  { key: 'roleNames', label: '角色' },
  { key: 'status', label: '状态' }
]

const {
  loading,
  errorMessage,
  records,
  pageNum,
  pageSize,
  total,
  keyword,
  load: loadUsers
} = usePagedAdminResource<UserInfo>({
  path: '/api/v1/admin/users',
  errorMessage: '加载用户失败'
})

await loadUsers()

// 配置角色弹窗
const showRoleModal = ref(false)
const currentRoleUser = ref<Record<string, unknown>>({})

function openRoleModal(row: Record<string, unknown>) {
  currentRoleUser.value = { ...row }
  showRoleModal.value = true
}
</script>

<template>
  <UDashboardPanel id="system-users">
    <template #header>
      <UDashboardNavbar title="用户管理">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <AdminPagedTableCard
        :columns="columns"
        :rows="records"
        :loading="loading"
        :error-message="errorMessage"
        :total="total"
        :page-num="pageNum"
        :page-size="pageSize"
        :keyword="keyword"
        show-actions
        search-placeholder="搜索账号/昵称"
        empty-text="暂无用户数据"
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="loadUsers"
        @search-enter="loadUsers"
      >
        <template #toolbar>
          <UsersUserAddModal @success="loadUsers" />
        </template>

        <template #cell-status="{ row }">
          <UBadge :color="row.status === 0 ? 'success' : 'neutral'" variant="subtle">
            {{ row.status === 0 ? '启用' : '禁用' }}
          </UBadge>
        </template>

        <template #actions="{ row }">
          <UsersUserEditModal :user="row" @success="loadUsers" />
          <UButton icon="i-lucide-shield" color="primary" variant="ghost" size="xs" @click="openRoleModal(row)">
            角色
          </UButton>
          <UsersUserResetPasswordModal :user="row" @success="loadUsers" />
          <UsersUserDeleteModal :user="row" @success="loadUsers" />
        </template>
      </AdminPagedTableCard>

      <!-- 配置角色弹窗 -->
      <UsersUserRoleModal v-model:open="showRoleModal" :user="currentRoleUser" @success="loadUsers" />
    </template>
  </UDashboardPanel>
</template>
