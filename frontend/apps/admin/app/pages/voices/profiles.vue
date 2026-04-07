<script setup lang="ts">
import { fetchAdminGet } from '@mortise/core-sdk'
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'
import { useVoiceCatalogLookups } from '~/composables/useVoiceCatalogLookups'
import {
  getVoiceStatusMeta,
  normalizeVoiceProfile,
  normalizeVoiceProfiles,
} from '~/types/voice'
import type { VoiceProfileInfo, VoiceTableColumn } from '~/types/voice'

const columns: VoiceTableColumn[] = [
  { key: 'name', label: '名称' },
  { key: 'code', label: '编码' },
  { key: 'language', label: '语言', align: 'center' },
  { key: 'asrModelId', label: 'ASR' },
  { key: 'vadModelId', label: 'VAD' },
  { key: 'ttsModelId', label: 'TTS' },
  { key: 'status', label: '状态', align: 'center' },
  { key: 'sortNo', label: '排序', align: 'center' },
]

const { $api } = useNuxtApp()

const {
  loading,
  errorMessage,
  records: profiles,
  pageNum,
  total,
  totalPage,
  hasNext,
  hasPrevious,
  keyword,
  load: loadProfiles,
} = usePagedAdminResource<VoiceProfileInfo>({
  path: '/api/v1/admin/voice/profiles',
  errorMessage: '加载语音 Profile 失败',
  transform: normalizeVoiceProfiles,
  buildQuery: () => ({
    status: statusFilter.value || undefined,
  }),
})

const {
  loading: lookupLoading,
  errorMessage: lookupErrorMessage,
  providerOptions,
  models,
  loadLookups,
  getModelName,
  getProviderName,
} = useVoiceCatalogLookups()

const { putAction, loading: statusLoading, errorMessage: actionErrorMessage } = useAdminCrud(
  '/api/v1/admin/voice/profiles'
)

const statusFilter = ref('')
const detailErrorMessage = ref('')

const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const currentProfile = ref<VoiceProfileInfo | null>(null)

const mergedErrorMessage = computed(() => {
  return (
    errorMessage.value
    || lookupErrorMessage.value
    || actionErrorMessage.value
    || detailErrorMessage.value
  )
})

function slotSummary(providerId?: string, modelId?: string): string {
  if (!providerId && !modelId) {
    return '-'
  }

  return `${getProviderName(providerId)} / ${getModelName(modelId)}`
}

async function refresh() {
  await Promise.all([loadProfiles(), loadLookups()])
}

async function reloadFirstPage() {
  if (pageNum.value !== 1) {
    pageNum.value = 1
    return
  }
  await loadProfiles()
}

function updateStatusFilter(value: unknown) {
  statusFilter.value = String(value ?? '')
  void reloadFirstPage()
}

async function openEditModal(row: VoiceProfileInfo) {
  detailErrorMessage.value = ''

  try {
    const detail = await fetchAdminGet<unknown>($api, `/api/v1/admin/voice/profiles/${row.id}`)
    currentProfile.value = normalizeVoiceProfile(detail)
    showEditModal.value = true
  } catch (error) {
    detailErrorMessage.value = error instanceof Error ? error.message : '加载语音 Profile 详情失败'
  }
}

function openDeleteModal(row: VoiceProfileInfo) {
  currentProfile.value = row
  showDeleteModal.value = true
}

async function toggleStatus(row: VoiceProfileInfo, targetStatus: number) {
  const action = targetStatus === 1 ? 'enable' : 'disable'
  const ok = await putAction(`${row.id}/${action}`)
  if (ok !== null) {
    await refresh()
  }
}

await refresh()
</script>

<template>
  <UDashboardPanel id="voice-profiles">
    <template #header>
      <UDashboardNavbar title="Profile 管理">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <VoicesListCard
        :columns="columns"
        :rows="profiles"
        :loading="loading || lookupLoading"
        :error-message="mergedErrorMessage"
        :total="total"
        :page-num="pageNum"
        :total-page="totalPage"
        :has-next="hasNext"
        :has-previous="hasPrevious"
        :keyword="keyword"
        search-placeholder="搜索 Profile、语言或槽位引用"
        empty-text="暂无语音 Profile 数据"
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

        <template #cell-language="{ row }">
          <span>{{ row.language || '-' }}</span>
        </template>

        <template #cell-asrModelId="{ row }">
          {{ slotSummary(String(row.asrProviderId || ''), String(row.asrModelId || '')) }}
        </template>

        <template #cell-vadModelId="{ row }">
          {{ slotSummary(String(row.vadProviderId || ''), String(row.vadModelId || '')) }}
        </template>

        <template #cell-ttsModelId="{ row }">
          {{ slotSummary(String(row.ttsProviderId || ''), String(row.ttsModelId || '')) }}
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
            @click="openEditModal(row as unknown as VoiceProfileInfo)"
          >
            编辑
          </UButton>
          <UButton
            :icon="Number(row.status) === 1 ? 'i-lucide-ban' : 'i-lucide-badge-check'"
            color="neutral"
            variant="ghost"
            size="xs"
            :loading="statusLoading"
            @click="toggleStatus(row as unknown as VoiceProfileInfo, Number(row.status) === 1 ? 0 : 1)"
          >
            {{ Number(row.status) === 1 ? '禁用' : '启用' }}
          </UButton>
          <UButton
            icon="i-lucide-trash-2"
            color="error"
            variant="ghost"
            size="xs"
            @click="openDeleteModal(row as unknown as VoiceProfileInfo)"
          >
            删除
          </UButton>
        </template>
      </VoicesListCard>

      <VoicesProfileAddModal
        v-model:open="showAddModal"
        :provider-options="providerOptions"
        :models="models"
        @success="refresh"
      />
      <VoicesProfileEditModal
        v-model:open="showEditModal"
        :profile="currentProfile"
        :provider-options="providerOptions"
        :models="models"
        @success="refresh"
      />
      <VoicesProfileDeleteModal
        v-model:open="showDeleteModal"
        :profile="currentProfile"
        @success="refresh"
      />
    </template>
  </UDashboardPanel>
</template>