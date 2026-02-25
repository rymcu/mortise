<script setup lang="ts">
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'

interface MenuInfo {
  id: number
  label?: string
  permission?: string
  href?: string
  menuType?: number
  sortNo?: number
  status?: number
}

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'label', label: '菜单名' },
  { key: 'permission', label: '权限' },
  { key: 'href', label: '路由' },
  { key: 'menuType', label: '类型' },
  { key: 'status', label: '状态' }
]


function formatMenuType(type?: number): string {
  if (type === 0) return '目录'
  if (type === 1) return '菜单'
  if (type === 2) return '按钮'
  return '-'
}

const {
  loading,
  errorMessage,
  records,
  pageNum,
  pageSize,
  total,
  keyword,
  load: loadMenus
} = usePagedAdminResource<MenuInfo>({
  path: '/api/v1/admin/menus',
  errorMessage: '加载菜单失败'
})

await loadMenus()
</script>

<template>
  <UDashboardPanel id="system-menus">
    <template #header>
      <UDashboardNavbar title="Menus">
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
        search-placeholder="搜索菜单名/权限"
        empty-text="暂无菜单数据"
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="loadMenus"
        @search-enter="loadMenus"
      >
        <template #cell-menuType="{ row }">
          {{ formatMenuType(Number(row.menuType)) }}
        </template>
        <template #cell-status="{ row }">
          <UBadge :color="row.status === 0 ? 'success' : 'neutral'" variant="subtle">
            {{ row.status === 0 ? '启用' : '禁用' }}
          </UBadge>
        </template>
      </AdminPagedTableCard>
    </template>
  </UDashboardPanel>
</template>
