<script setup lang="ts">
/**
 * 字典管理页面 - 带字典类型筛选
 * 支持通过下拉选择字典类型进行过滤
 */
import { fetchAdminPage } from '@mortise/core-sdk'
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'

interface DictTypeOption {
  id: number
  typeCode: string
  label: string
}

interface DictInfo {
  id: number
  dictTypeCode?: string
  label?: string
  value?: string
  sortNo?: number
  status?: number
  icon?: string
  color?: string
  createdTime?: string
}

const { $api } = useNuxtApp()

// 加载字典类型下拉选项
const dictTypes = ref<DictTypeOption[]>([])
const selectedTypeCode = ref('')

async function loadDictTypes() {
  try {
    const page = await fetchAdminPage<DictTypeOption>(
      $api,
      '/api/v1/admin/dictionary-types',
      {
        pageNum: 1,
        pageSize: 999
      }
    )
    dictTypes.value = (page.records || []) as DictTypeOption[]
  } catch {
    // 静默失败
  }
}

await loadDictTypes()

const columns = [
  { key: 'dictTypeCode', label: '字典类型' },
  { key: 'label', label: '标签' },
  { key: 'value', label: '值' },
  { key: 'sortNo', label: '排序' },
  { key: 'status', label: '状态' },
  { key: 'icon', label: '图标' },
  { key: 'color', label: '颜色' }
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
} = usePagedAdminResource<DictInfo>({
  path: '/api/v1/admin/dictionaries',
  errorMessage: '加载字典失败'
})

await loadData()

// 按类型筛选时重新加载
watch(selectedTypeCode, () => {
  pageNum.value = 1
  loadData()
})

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
  <UDashboardPanel id="system-dictionaries">
    <template #header>
      <UDashboardNavbar title="字典数据">
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
        search-placeholder="搜索标签/值"
        empty-text="暂无字典数据"
        @update:keyword="keyword = $event"
        @update:page-num="pageNum = $event"
        @refresh="loadData"
        @search-enter="loadData"
      >
        <template #toolbar>
          <!-- 字典类型筛选 -->
          <select
            v-model="selectedTypeCode"
            class="border-default bg-default focus:ring-primary h-8 rounded-md border px-2 text-sm focus:ring-2 focus:outline-none"
          >
            <option value="">全部类型</option>
            <option
              v-for="dt in dictTypes"
              :key="dt.typeCode"
              :value="dt.typeCode"
            >
              {{ dt.label }}（{{ dt.typeCode }}）
            </option>
          </select>
          <UButton
            icon="i-lucide-plus"
            color="primary"
            variant="soft"
            @click="showAddModal = true"
          >
            新增
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
        <template #cell-icon="{ row }">
          <UIcon v-if="row.icon" :name="String(row.icon)" class="text-lg" />
          <span v-else>-</span>
        </template>
        <template #cell-color="{ row }">
          <div v-if="row.color" class="flex items-center gap-1.5">
            <span
              class="inline-block h-3 w-3 rounded-full"
              :class="`bg-${row.color}-500`"
            />
            <span class="text-xs">{{ row.color }}</span>
          </div>
          <span v-else>-</span>
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
      <DictsDictAddModal v-model:open="showAddModal" @success="loadData" />
      <DictsDictEditModal
        v-model:open="showEditModal"
        :dict="currentRow"
        @success="loadData"
      />
      <DictsDictDeleteModal
        v-model:open="showDeleteModal"
        :dict="currentRow"
        @success="loadData"
      />
    </template>
  </UDashboardPanel>
</template>
