<script setup lang="ts">
import { useAdminListResource } from '~/composables/useAdminListResource'
import {
  getVoiceRuntimeConfigStatusMeta,
  getVoiceRuntimeProbeStatusMeta,
  normalizeVoiceRuntimeNodes,
} from '~/types/voice'
import type { VoiceRuntimeNodeInfo, VoiceTableColumn } from '~/types/voice'

const columns: VoiceTableColumn[] = [
  { key: 'nodeId', label: '节点标识' },
  { key: 'baseUrl', label: 'Base URL' },
  { key: 'configStatus', label: '配置状态', align: 'center' },
  { key: 'probeStatus', label: '探测状态', align: 'center' },
  { key: 'latencyMillis', label: '耗时', align: 'center' },
  { key: 'checkedTime', label: '探测时间' },
  { key: 'loadedModels', label: '已加载模型' },
  { key: 'detail', label: '详情' },
]

const {
  loading,
  errorMessage,
  records: runtimeNodes,
  load: loadRuntimeNodes,
} = useAdminListResource<VoiceRuntimeNodeInfo>({
  path: '/api/v1/admin/voice/runtime/nodes',
  errorMessage: '加载运行时节点失败',
  transform: normalizeVoiceRuntimeNodes,
})

const keyword = ref('')

const filteredRuntimeNodes = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()
  if (!normalizedKeyword) {
    return runtimeNodes.value
  }

  return runtimeNodes.value.filter(node => [
    node.nodeId,
    node.baseUrl,
    node.configStatus,
    node.probeStatus,
    node.detail,
    node.checkedTime,
    node.loadedModels?.join(' '),
  ].some(field => field?.toLowerCase().includes(normalizedKeyword)))
})

function getRuntimeNodeConfigStatusMeta(row: unknown) {
  const runtimeNode = row as VoiceRuntimeNodeInfo
  return getVoiceRuntimeConfigStatusMeta(runtimeNode.configStatus)
}

function getRuntimeNodeProbeStatusMetaValue(row: unknown) {
  const runtimeNode = row as VoiceRuntimeNodeInfo
  return getVoiceRuntimeProbeStatusMeta(runtimeNode.probeStatus)
}

function formatCheckedTime(value: unknown): string {
  const checkedTime = typeof value === 'string' ? value : ''
  return checkedTime ? checkedTime.replace('T', ' ') : '-'
}

await loadRuntimeNodes()
</script>

<template>
  <UDashboardPanel id="voice-runtime">
    <template #header>
      <UDashboardNavbar title="运行时节点">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <UAlert
        color="info"
        variant="subtle"
        title="当前同时展示配置状态与一次即时健康探测"
        description="节点启用后会优先探测 /actuator/health，404 时回退 /health；结果反映最近一次页面刷新时的连通性，不代表完整 ASR/TTS 调用链已通过。"
        class="mb-4"
      />

      <VoicesListCard
        :columns="columns"
        :rows="filteredRuntimeNodes"
        :loading="loading"
        :error-message="errorMessage"
        :total="filteredRuntimeNodes.length"
        :keyword="keyword"
        search-placeholder="搜索节点标识、地址、配置状态、探测状态或模型"
        empty-text="暂无运行时节点数据"
        @update:keyword="keyword = $event"
        @refresh="loadRuntimeNodes"
      >
        <template #cell-configStatus="{ row }">
          <UBadge
            :color="getRuntimeNodeConfigStatusMeta(row).color"
            variant="subtle"
          >
            {{ getRuntimeNodeConfigStatusMeta(row).label }}
          </UBadge>
        </template>

        <template #cell-probeStatus="{ row }">
          <UBadge
            :color="getRuntimeNodeProbeStatusMetaValue(row).color"
            variant="subtle"
          >
            {{ getRuntimeNodeProbeStatusMetaValue(row).label }}
          </UBadge>
        </template>

        <template #cell-latencyMillis="{ row }">
          <span>{{ typeof row.latencyMillis === 'number' ? `${row.latencyMillis} ms` : '-' }}</span>
        </template>

        <template #cell-checkedTime="{ row }">
          <span>{{ formatCheckedTime(row.checkedTime) }}</span>
        </template>

        <template #cell-loadedModels="{ row }">
          <div v-if="Array.isArray(row.loadedModels) && row.loadedModels.length" class="flex flex-wrap gap-1">
            <UBadge
              v-for="model in row.loadedModels"
              :key="String(model)"
              color="primary"
              variant="subtle"
              size="xs"
            >
              {{ model }}
            </UBadge>
          </div>
          <span v-else class="text-muted">-</span>
        </template>
      </VoicesListCard>
    </template>
  </UDashboardPanel>
</template>