<script setup lang="ts">
import { fetchAdminGet } from '@mortise/core-sdk'
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'
import {
  getVoiceJobStatusMeta,
  getVoiceJobTypeLabel,
  normalizeVoiceJob,
  normalizeVoiceJobs,
  voiceJobStatusOptions,
  voiceJobTypeOptions,
} from '~/types/voice'
import type { VoiceJobInfo, VoiceTableColumn } from '~/types/voice'

const columns: VoiceTableColumn[] = [
  { key: 'id', label: '任务 ID' },
  { key: 'jobType', label: '类型', align: 'center' },
  { key: 'status', label: '状态', align: 'center' },
  { key: 'profileName', label: 'Profile' },
  { key: 'userId', label: '用户 ID' },
  { key: 'durationMillis', label: '耗时(ms)', align: 'center' },
  { key: 'createdTime', label: '创建时间' },
  { key: 'resultSummary', label: '结果摘要' },
]

const { $api } = useNuxtApp()

const {
  loading,
  errorMessage,
  records: jobs,
  pageNum,
  total,
  totalPage,
  hasNext,
  hasPrevious,
  keyword,
  load: loadJobs,
} = usePagedAdminResource<VoiceJobInfo>({
  path: '/api/v1/admin/voice/jobs',
  errorMessage: '加载语音任务失败',
  transform: normalizeVoiceJobs,
  buildQuery: () => ({
    jobStatus: jobStatusFilter.value || undefined,
    jobType: jobTypeFilter.value || undefined,
  }),
})

const jobStatusFilter = ref('')
const jobTypeFilter = ref('')
const detailErrorMessage = ref('')
const showDetailSlideover = ref(false)
const currentJob = ref<VoiceJobInfo | null>(null)

const mergedErrorMessage = computed(() => {
  return errorMessage.value || detailErrorMessage.value
})

function displayProfile(row: VoiceJobInfo): string {
  return row.profileName || row.profileCode || row.profileId || '-'
}

async function refresh() {
  await loadJobs()
}

async function reloadFirstPage() {
  if (pageNum.value !== 1) {
    pageNum.value = 1
    return
  }
  await loadJobs()
}

function updateJobStatusFilter(value: unknown) {
  jobStatusFilter.value = String(value ?? '')
  void reloadFirstPage()
}

function updateJobTypeFilter(value: unknown) {
  jobTypeFilter.value = String(value ?? '')
  void reloadFirstPage()
}

async function openDetailSlideoverByRow(row: VoiceJobInfo) {
  detailErrorMessage.value = ''

  try {
    const detail = await fetchAdminGet<unknown>($api, `/api/v1/admin/voice/jobs/${row.id}`)
    currentJob.value = normalizeVoiceJob(detail)
    showDetailSlideover.value = true
  } catch (error) {
    detailErrorMessage.value = error instanceof Error ? error.message : '加载语音任务详情失败'
  }
}

await refresh()
</script>

<template>
  <UDashboardPanel id="voice-jobs">
    <template #header>
      <UDashboardNavbar title="任务记录">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <VoicesListCard
        :columns="columns"
        :rows="jobs"
        :loading="loading"
        :error-message="mergedErrorMessage"
        :total="total"
        :page-num="pageNum"
        :total-page="totalPage"
        :has-next="hasNext"
        :has-previous="hasPrevious"
        :keyword="keyword"
        search-placeholder="搜索任务 ID、来源模块、结果摘要或错误信息"
        empty-text="暂无语音任务数据"
        show-actions
        actions-label="详情"
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="refresh"
        @search-enter="reloadFirstPage"
      >
        <template #filters>
          <USelect
            :model-value="jobStatusFilter || undefined"
            :items="voiceJobStatusOptions"
            value-key="value"
            label-key="label"
            placeholder="全部状态"
            class="w-36"
            @update:model-value="updateJobStatusFilter"
          />
          <USelect
            :model-value="jobTypeFilter || undefined"
            :items="voiceJobTypeOptions"
            value-key="value"
            label-key="label"
            placeholder="全部类型"
            class="w-40"
            @update:model-value="updateJobTypeFilter"
          />
        </template>

        <template #cell-id="{ row }">
          <span class="font-mono text-xs">{{ row.id }}</span>
        </template>

        <template #cell-jobType="{ row }">
          {{ getVoiceJobTypeLabel(String(row.jobType || '')) }}
        </template>

        <template #cell-status="{ row }">
          <UBadge
            :color="getVoiceJobStatusMeta(String(row.status || '')).color"
            variant="subtle"
          >
            {{ getVoiceJobStatusMeta(String(row.status || '')).label }}
          </UBadge>
        </template>

        <template #cell-profileName="{ row }">
          {{ displayProfile(row as unknown as VoiceJobInfo) }}
        </template>

        <template #cell-userId="{ row }">
          <span class="font-mono text-xs">{{ row.userId || '-' }}</span>
        </template>

        <template #cell-resultSummary="{ row }">
          <span class="inline-block max-w-xs truncate align-middle" :title="String(row.resultSummary || '')">
            {{ row.resultSummary || '-' }}
          </span>
        </template>

        <template #actions="{ row }">
          <UButton
            icon="i-lucide-eye"
            color="primary"
            variant="ghost"
            size="xs"
            @click="openDetailSlideoverByRow(row as unknown as VoiceJobInfo)"
          >
            查看
          </UButton>
        </template>
      </VoicesListCard>

      <VoicesJobDetailSlideover
        v-model:open="showDetailSlideover"
        :job="currentJob"
      />
    </template>
  </UDashboardPanel>
</template>