<script setup lang="ts">
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'

interface RoleInfo {
  id: number
  label?: string
  permission?: string
  status?: number
  createdTime?: string
}

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'label', label: '角色名' },
  { key: 'permission', label: '权限标识' },
  { key: 'status', label: '状态' },
  { key: 'createdTime', label: '创建时间' }
]

const {
  loading,
  errorMessage,
  records,
  pageNum,
  pageSize,
  total,
  keyword,
  load: loadRoles
} = usePagedAdminResource<RoleInfo>({
  path: '/api/v1/admin/roles',
  errorMessage: '加载角色失败'
})

await loadRoles()
</script>

<template>
  <UDashboardPanel id="system-roles">
    <template #header>
      <UDashboardNavbar title="Roles">
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
        search-placeholder="搜索角色名/权限"
        empty-text="暂无角色数据"
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="loadRoles"
        @search-enter="loadRoles"
      >
        <template #cell-status="{ row }">
          <UBadge :color="row.status === 0 ? 'success' : 'neutral'" variant="subtle">
            {{ row.status === 0 ? '启用' : '禁用' }}
          </UBadge>
        </template>
      </AdminPagedTableCard>
    </template>
  </UDashboardPanel>
</template>
