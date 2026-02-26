<script setup lang="ts">
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'

interface Oauth2ClientInfo {
  id: number
  registrationId?: string
  clientId?: string
  clientName?: string
  authorizationGrantType?: string
  scopes?: string
  redirectUri?: string
  isEnabled?: number
  status?: number
  createdTime?: string
}

const columns = [
  { key: 'registrationId', label: '注册 ID' },
  { key: 'clientId', label: '客户端 ID' },
  { key: 'clientName', label: '名称' },
  { key: 'authorizationGrantType', label: '授权类型' },
  { key: 'scopes', label: '授权范围' },
  { key: 'isEnabled', label: '启用' },
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
  load: loadData
} = usePagedAdminResource<Oauth2ClientInfo>({
  path: '/api/v1/admin/oauth2/client-configs',
  errorMessage: '加载 OAuth2 客户端失败'
})

await loadData()

// CRUD 弹窗状态
const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const currentRow = ref<Record<string, unknown>>({})

function openEditModal(row: Record<string, unknown>) {
  currentRow.value = { ...row }
  showEditModal.value = true
}

function openDeleteModal(row: Record<string, unknown>) {
  currentRow.value = { ...row }
  showDeleteModal.value = true
}
</script>

<template>
  <UDashboardPanel id="system-oauth2-clients">
    <template #header>
      <UDashboardNavbar title="OAuth2 客户端">
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
        search-placeholder="搜索客户端 ID/注册 ID"
        empty-text="暂无 OAuth2 客户端数据"
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="loadData"
        @search-enter="loadData"
      >
        <template #toolbar>
          <UButton
            icon="i-lucide-plus"
            color="primary"
            variant="soft"
            @click="showAddModal = true"
          >
            新增
          </UButton>
        </template>

        <template #cell-scopes="{ row }">
          <div v-if="row.scopes" class="flex flex-wrap gap-1">
            <UBadge
              v-for="(scope, i) in String(row.scopes)
                .split(/[\s,]+/)
                .filter(Boolean)
                .slice(0, 3)"
              :key="i"
              color="info"
              variant="subtle"
              size="xs"
            >
              {{ scope }}
            </UBadge>
            <UBadge
              v-if="
                String(row.scopes)
                  .split(/[\s,]+/)
                  .filter(Boolean).length > 3
              "
              color="neutral"
              variant="subtle"
              size="xs"
            >
              +{{
                String(row.scopes)
                  .split(/[\s,]+/)
                  .filter(Boolean).length - 3
              }}
            </UBadge>
          </div>
          <span v-else>-</span>
        </template>
        <template #cell-isEnabled="{ row }">
          <UBadge
            :color="row.isEnabled === 0 ? 'success' : 'neutral'"
            variant="subtle"
          >
            {{ row.isEnabled === 0 ? '启用' : '禁用' }}
          </UBadge>
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
      <Oauth2ClientsOauth2ClientAddModal
        v-model:open="showAddModal"
        @success="loadData"
      />
      <Oauth2ClientsOauth2ClientEditModal
        v-model:open="showEditModal"
        :client="currentRow"
        @success="loadData"
      />
      <Oauth2ClientsOauth2ClientDeleteModal
        v-model:open="showDeleteModal"
        :client="currentRow"
        @success="loadData"
      />
    </template>
  </UDashboardPanel>
</template>
