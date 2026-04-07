<script setup lang="ts">
import { fetchAdminGet } from '@mortise/core-sdk'
import { useAdminListResource } from '~/composables/useAdminListResource'
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'
import {
  buildVoiceProviderOptions,
  getVoiceCapabilityLabel,
  getVoiceModelTypeLabel,
  getVoiceStatusMeta,
  normalizeVoiceModel,
  normalizeVoiceModels,
  normalizeVoiceProviders,
  voiceCapabilityOptions,
} from '~/types/voice'
import type {
  VoiceModelInfo,
  VoiceProviderInfo,
  VoiceTableColumn,
} from '~/types/voice'

const columns: VoiceTableColumn[] = [
  { key: 'name', label: '名称' },
  { key: 'code', label: '编码' },
  { key: 'providerId', label: 'Provider' },
  { key: 'capability', label: '能力', align: 'center' },
  { key: 'modelType', label: '模型类型', align: 'center' },
  { key: 'defaultModel', label: '默认', align: 'center' },
  { key: 'status', label: '状态', align: 'center' },
  { key: 'concurrencyLimit', label: '并发上限', align: 'center' },
]

const { $api } = useNuxtApp()

const {
  loading,
  errorMessage,
  records: models,
  pageNum,
  total,
  totalPage,
  hasNext,
  hasPrevious,
  keyword,
  load: loadModels,
} = usePagedAdminResource<VoiceModelInfo>({
  path: '/api/v1/admin/voice/models',
  errorMessage: '加载语音模型失败',
  transform: normalizeVoiceModels,
  buildQuery: () => ({
    status: statusFilter.value || undefined,
    capability: capabilityFilter.value || undefined,
    providerId: providerFilter.value || undefined,
  }),
})

const {
  errorMessage: providerErrorMessage,
  records: providers,
  load: loadProviders,
} = useAdminListResource<VoiceProviderInfo>({
  path: '/api/v1/admin/voice/providers/options',
  errorMessage: '加载 Provider 选项失败',
  transform: normalizeVoiceProviders,
})

const { putAction, loading: statusLoading, errorMessage: actionErrorMessage } = useAdminCrud(
  '/api/v1/admin/voice/models'
)

const statusFilter = ref('')
const capabilityFilter = ref('')
const providerFilter = ref('')
const detailErrorMessage = ref('')

const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const currentModel = ref<VoiceModelInfo | null>(null)

const providerOptions = computed(() => buildVoiceProviderOptions(providers.value))

const mergedErrorMessage = computed(() => {
  return (
    errorMessage.value
    || providerErrorMessage.value
    || actionErrorMessage.value
    || detailErrorMessage.value
  )
})

function getProviderName(providerId?: string): string {
  if (!providerId) {
    return '-'
  }

  const provider = providers.value.find(item => item.id === providerId)
  return provider?.name || provider?.code || providerId
}

async function refresh() {
  await Promise.all([loadModels(), loadProviders()])
}

async function reloadFirstPage() {
  if (pageNum.value !== 1) {
    pageNum.value = 1
    return
  }
  await loadModels()
}

function updateProviderFilter(value: unknown) {
  providerFilter.value = String(value ?? '')
  void reloadFirstPage()
}

function updateCapabilityFilter(value: unknown) {
  capabilityFilter.value = String(value ?? '')
  void reloadFirstPage()
}

function updateStatusFilter(value: unknown) {
  statusFilter.value = String(value ?? '')
  void reloadFirstPage()
}

async function openEditModal(row: VoiceModelInfo) {
  detailErrorMessage.value = ''

  try {
    const detail = await fetchAdminGet<unknown>($api, `/api/v1/admin/voice/models/${row.id}`)
    currentModel.value = normalizeVoiceModel(detail)
    showEditModal.value = true
  } catch (error) {
    detailErrorMessage.value = error instanceof Error ? error.message : '加载语音模型详情失败'
  }
}

function openDeleteModal(row: VoiceModelInfo) {
  currentModel.value = row
  showDeleteModal.value = true
}

async function toggleStatus(row: VoiceModelInfo, targetStatus: number) {
  const action = targetStatus === 1 ? 'enable' : 'disable'
  const ok = await putAction(`${row.id}/${action}`)
  if (ok !== null) {
    await refresh()
  }
}

await refresh()
</script>

<template>
  <UDashboardPanel id="voice-models">
    <template #header>
      <UDashboardNavbar title="Model 管理">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <VoicesListCard
        :columns="columns"
        :rows="models"
        :loading="loading"
        :error-message="mergedErrorMessage"
        :total="total"
        :page-num="pageNum"
        :total-page="totalPage"
        :has-next="hasNext"
        :has-previous="hasPrevious"
        :keyword="keyword"
        search-placeholder="搜索模型名称/编码/运行时名称"
        empty-text="暂无语音模型数据"
        show-actions
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="refresh"
        @search-enter="reloadFirstPage"
      >
        <template #filters>
          <USelect
            :model-value="providerFilter || undefined"
            :items="providerOptions"
            value-key="value"
            label-key="label"
            placeholder="全部 Provider"
            class="w-52"
            @update:model-value="updateProviderFilter"
          />
          <USelect
            :model-value="capabilityFilter || undefined"
            :items="voiceCapabilityOptions"
            value-key="value"
            label-key="label"
            placeholder="全部能力"
            class="w-40"
            @update:model-value="updateCapabilityFilter"
          />
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

        <template #cell-providerId="{ row }">
          {{ getProviderName(String(row.providerId || '')) }}
        </template>

        <template #cell-capability="{ row }">
          <UBadge color="info" variant="subtle">
            {{ getVoiceCapabilityLabel(String(row.capability || '')) }}
          </UBadge>
        </template>

        <template #cell-modelType="{ row }">
          {{ getVoiceModelTypeLabel(String(row.modelType || '')) }}
        </template>

        <template #cell-defaultModel="{ row }">
          <UBadge
            :color="Boolean(row.defaultModel) ? 'primary' : 'neutral'"
            variant="subtle"
          >
            {{ Boolean(row.defaultModel) ? '是' : '否' }}
          </UBadge>
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
            @click="openEditModal(row as unknown as VoiceModelInfo)"
          >
            编辑
          </UButton>
          <UButton
            :icon="Number(row.status) === 1 ? 'i-lucide-ban' : 'i-lucide-badge-check'"
            color="neutral"
            variant="ghost"
            size="xs"
            :loading="statusLoading"
            @click="toggleStatus(row as unknown as VoiceModelInfo, Number(row.status) === 1 ? 0 : 1)"
          >
            {{ Number(row.status) === 1 ? '禁用' : '启用' }}
          </UButton>
          <UButton
            icon="i-lucide-trash-2"
            color="error"
            variant="ghost"
            size="xs"
            @click="openDeleteModal(row as unknown as VoiceModelInfo)"
          >
            删除
          </UButton>
        </template>
      </VoicesListCard>

      <VoicesModelAddModal
        v-model:open="showAddModal"
        :provider-options="providerOptions"
        @success="refresh"
      />
      <VoicesModelEditModal
        v-model:open="showEditModal"
        :model="currentModel"
        :provider-options="providerOptions"
        @success="refresh"
      />
      <VoicesModelDeleteModal
        v-model:open="showDeleteModal"
        :model="currentModel"
        @success="refresh"
      />
    </template>
  </UDashboardPanel>
</template>