<script setup lang="ts">
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'

interface WeChatAccountInfo {
  id: number
  accountName?: string
  accountType?: string
  appId?: string
  isDefault?: number
  isEnabled?: number
  remark?: string
  createdTime?: string
}

const columns = [
  { key: 'accountName', label: '账号名称' },
  { key: 'accountType', label: '账号类型' },
  { key: 'appId', label: 'AppID' },
  { key: 'isDefault', label: '默认' },
  { key: 'isEnabled', label: '启用' },
  { key: 'remark', label: '备注' },
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
} = usePagedAdminResource<WeChatAccountInfo>({
  path: '/api/v1/admin/wechat/accounts',
  errorMessage: '加载微信账号失败'
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
  <UDashboardPanel id="system-wechat-accounts">
    <template #header>
      <UDashboardNavbar title="微信账号">
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
        search-placeholder="搜索账号名称/类型"
        empty-text="暂无微信账号数据"
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

        <template #cell-isDefault="{ row }">
          <UBadge
            :color="row.isDefault === 0 ? 'success' : 'neutral'"
            variant="subtle"
          >
            {{ row.isDefault === 0 ? '是' : '否' }}
          </UBadge>
        </template>
        <template #cell-isEnabled="{ row }">
          <UBadge
            :color="row.isEnabled === 0 ? 'success' : 'neutral'"
            variant="subtle"
          >
            {{ row.isEnabled === 0 ? '启用' : '禁用' }}
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
      <WechatAccountsWeChatAccountAddModal
        v-model:open="showAddModal"
        @success="loadData"
      />
      <WechatAccountsWeChatAccountEditModal
        v-model:open="showEditModal"
        :account="currentRow"
        @success="loadData"
      />
      <WechatAccountsWeChatAccountDeleteModal
        v-model:open="showDeleteModal"
        :account="currentRow"
        @success="loadData"
      />
    </template>
  </UDashboardPanel>
</template>
