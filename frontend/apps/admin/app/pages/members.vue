<script setup lang="ts">
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'

interface MemberInfo {
  id: number
  username?: string
  email?: string
  phone?: string
  nickname?: string
  memberLevel?: string
  points?: number
  status?: number
  lastLoginTime?: string
  createdTime?: string
}

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'nickname', label: '昵称' },
  { key: 'username', label: '用户名' },
  { key: 'email', label: '邮箱' },
  { key: 'phone', label: '手机号' },
  { key: 'memberLevel', label: '等级' },
  { key: 'points', label: '积分' },
  { key: 'status', label: '状态' },
  { key: 'lastLoginTime', label: '最后登录' }
]

const {
  loading,
  errorMessage,
  records,
  pageNum,
  pageSize,
  total,
  keyword,
  load: loadMembers
} = usePagedAdminResource<MemberInfo>({
  path: '/api/v1/admin/members',
  errorMessage: '加载会员失败'
})

await loadMembers()

// CRUD 弹窗状态
const showEditModal = ref(false)
const currentRow = ref<Record<string, unknown>>({})

function openEditModal(row: Record<string, unknown>) {
  currentRow.value = { ...row }
  showEditModal.value = true
}
</script>

<template>
  <UDashboardPanel id="members">
    <template #header>
      <UDashboardNavbar title="会员管理">
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
        search-placeholder="搜索用户名/邮箱/昵称"
        empty-text="暂无会员数据"
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="loadMembers"
        @search-enter="loadMembers"
      >
        <template #cell-status="{ row }">
          <UBadge
            :color="row.status === 0 ? 'success' : 'neutral'"
            variant="subtle"
          >
            {{ row.status === 0 ? '启用' : '禁用' }}
          </UBadge>
        </template>

        <template #actions="{ row }">
          <UButton
            :icon="row.status === 0 ? 'i-lucide-pause' : 'i-lucide-play'"
            :color="row.status === 0 ? 'warning' : 'success'"
            variant="ghost"
            size="xs"
            @click="openEditModal(row)"
          >
            {{ row.status === 0 ? '禁用' : '启用' }}
          </UButton>
        </template>
      </AdminPagedTableCard>

      <!-- 弹窗 -->
      <MembersMemberEditModal
        v-model:open="showEditModal"
        :member="currentRow"
        @success="loadMembers"
      />
    </template>
  </UDashboardPanel>
</template>
