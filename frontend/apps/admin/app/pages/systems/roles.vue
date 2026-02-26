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

// CRUD 弹窗状态管理
const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const showUserModal = ref(false)
const showMenuModal = ref(false)
const currentRow = ref<Record<string, unknown>>({})

function openEditModal(row: Record<string, unknown>) {
  currentRow.value = { ...row }
  showEditModal.value = true
}

function openDeleteModal(row: Record<string, unknown>) {
  currentRow.value = { ...row }
  showDeleteModal.value = true
}

function openUserModal(row: Record<string, unknown>) {
  currentRow.value = { ...row }
  showUserModal.value = true
}

function openMenuModal(row: Record<string, unknown>) {
  currentRow.value = { ...row }
  showMenuModal.value = true
}
</script>

<template>
  <UDashboardPanel id="system-roles">
    <template #header>
      <UDashboardNavbar title="角色管理">
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
        search-placeholder="搜索角色名/权限"
        empty-text="暂无角色数据"
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="loadRoles"
        @search-enter="loadRoles"
      >
        <template #toolbar>
          <UButton
            icon="i-lucide-plus"
            color="primary"
            variant="soft"
            @click="showAddModal = true"
          >
            新增角色
          </UButton>
        </template>

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
            icon="i-lucide-users"
            color="info"
            variant="ghost"
            size="xs"
            @click="openUserModal(row)"
          >
            用户
          </UButton>
          <UButton
            icon="i-lucide-folder-tree"
            color="info"
            variant="ghost"
            size="xs"
            @click="openMenuModal(row)"
          >
            菜单
          </UButton>
          <UButton
            icon="i-lucide-pencil"
            color="primary"
            variant="ghost"
            size="xs"
            @click="openEditModal(row)"
          >
            编辑
          </UButton>
          <UButton
            icon="i-lucide-trash-2"
            color="error"
            variant="ghost"
            size="xs"
            @click="openDeleteModal(row)"
          >
            删除
          </UButton>
        </template>
      </AdminPagedTableCard>

      <!-- 弹窗 -->
      <RolesRoleAddModal v-model:open="showAddModal" @success="loadRoles" />
      <RolesRoleEditModal
        v-model:open="showEditModal"
        :role="currentRow"
        @success="loadRoles"
      />
      <RolesRoleDeleteModal
        v-model:open="showDeleteModal"
        :role="currentRow"
        @success="loadRoles"
      />
      <RolesRoleUserModal
        v-model:open="showUserModal"
        :role="currentRow"
        @success="loadRoles"
      />
      <RolesRoleMenuModal
        v-model:open="showMenuModal"
        :role="currentRow"
        @success="loadRoles"
      />
    </template>
  </UDashboardPanel>
</template>
