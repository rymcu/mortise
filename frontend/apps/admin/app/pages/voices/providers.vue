<script setup lang="ts">
import { fetchAdminGet } from '@mortise/core-sdk'
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'
import {
  getVoiceProviderTypeLabel,
  getVoiceStatusMeta,
  normalizeVoiceProvider,
  normalizeVoiceProviders,
} from '~/types/voice'
import type { VoiceProviderInfo, VoiceTableColumn } from '~/types/voice'

const columns: VoiceTableColumn[] = [
  { key: 'name', label: '名称' },
  { key: 'code', label: '编码' },
  { key: 'providerType', label: '类型' },
  { key: 'status', label: '状态', align: 'center' },
  { key: 'sortNo', label: '排序', align: 'center' },
  { key: 'createdTime', label: '创建时间' },
]

const { $api } = useNuxtApp()

const {
  loading,
  errorMessage,
  records: providers,
  pageNum,
  total,
  totalPage,
  hasNext,
  hasPrevious,
  keyword,
  load: loadProviders,
} = usePagedAdminResource<VoiceProviderInfo>({
  path: '/api/v1/admin/voice/providers',
  errorMessage: '加载语音提供商失败',
  transform: normalizeVoiceProviders,
  buildQuery: () => ({
    status: statusFilter.value || undefined,
  }),
})

const { putAction, loading: statusLoading, errorMessage: actionErrorMessage } = useAdminCrud(
  '/api/v1/admin/voice/providers'
)

const statusFilter = ref('')
const detailErrorMessage = ref('')

const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const currentProvider = ref<VoiceProviderInfo | null>(null)

const mergedErrorMessage = computed(() => {
  return errorMessage.value || actionErrorMessage.value || detailErrorMessage.value
})

async function refresh() {
  await loadProviders()
}

async function reloadFirstPage() {
  if (pageNum.value !== 1) {
    pageNum.value = 1
    return
  }
  await loadProviders()
}

function updateStatusFilter(value: unknown) {
  statusFilter.value = String(value ?? '')
  void reloadFirstPage()
}

async function openEditModal(row: VoiceProviderInfo) {
  detailErrorMessage.value = ''

  try {
    const detail = await fetchAdminGet<unknown>($api, `/api/v1/admin/voice/providers/${row.id}`)
    currentProvider.value = normalizeVoiceProvider(detail)
    showEditModal.value = true
  } catch (error) {
    detailErrorMessage.value = error instanceof Error ? error.message : '加载提供商详情失败'
  }
}

function openDeleteModal(row: VoiceProviderInfo) {
  currentProvider.value = row
  showDeleteModal.value = true
}

async function toggleStatus(row: VoiceProviderInfo, targetStatus: number) {
  const action = targetStatus === 1 ? 'enable' : 'disable'
  const ok = await putAction(`${row.id}/${action}`)
  if (ok !== null) {
    await refresh()
  }
}

await refresh()
</script>

<template>
  <UDashboardPanel id="voice-providers">
    <template #header>
      <UDashboardNavbar title="Provider 管理">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <VoicesListCard
        :columns="columns"
        :rows="providers"
        :loading="loading"
        :error-message="mergedErrorMessage"
        :total="total"
        :page-num="pageNum"
        :total-page="totalPage"
        :has-next="hasNext"
        :has-previous="hasPrevious"
        :keyword="keyword"
        search-placeholder="搜索提供商名称/编码/类型"
        empty-text="暂无语音提供商数据"
        show-actions
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="refresh"
        @search-enter="reloadFirstPage"
      >
        <template #filters>
          <USelect
            :model-value="statusFilter || undefined"
            :items="[
              { label: '启用', value: '1' },
              { label: '禁用', value: '0' }
            ]"
            value-key="value"
            label-key="label"
            placeholder="全部状态"
            class="w-36"
            @update:model-value="updateStatusFilter"
          />
        </template>

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

        <template #cell-providerType="{ row }">
          {{ getVoiceProviderTypeLabel(String(row.providerType || '')) }}
        </template>

        <template #cell-status="{ row }">
          <UBadge
            :color="getVoiceStatusMeta(Number(row.status)).color"
            variant="subtle"
          >
            {{ getVoiceStatusMeta(Number(row.status)).label }}
          </UBadge>
        </template>

        <template #actions="{ row }">
          <UButton
            icon="i-lucide-pencil"
            color="primary"
            variant="ghost"
            size="xs"
            @click="openEditModal(row as unknown as VoiceProviderInfo)"
          >
            编辑
          </UButton>
          <UButton
            :icon="Number(row.status) === 1 ? 'i-lucide-ban' : 'i-lucide-badge-check'"
            color="neutral"
            variant="ghost"
            size="xs"
            :loading="statusLoading"
            @click="toggleStatus(row as unknown as VoiceProviderInfo, Number(row.status) === 1 ? 0 : 1)"
          >
            {{ Number(row.status) === 1 ? '禁用' : '启用' }}
          </UButton>
          <UButton
            icon="i-lucide-trash-2"
            color="error"
            variant="ghost"
            size="xs"
            @click="openDeleteModal(row as unknown as VoiceProviderInfo)"
          >
            删除
          </UButton>
        </template>
      </VoicesListCard>

      <VoicesProviderAddModal v-model:open="showAddModal" @success="refresh" />
      <VoicesProviderEditModal
        v-model:open="showEditModal"
        :provider="currentProvider"
        @success="refresh"
      />
      <VoicesProviderDeleteModal
        v-model:open="showDeleteModal"
        :provider="currentProvider"
        @success="refresh"
      />
    </template>
  </UDashboardPanel>
</template>