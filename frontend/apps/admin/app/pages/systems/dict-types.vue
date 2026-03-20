<script setup lang="ts">
/**
 * 字典管理 - 主从视图
 * 左侧：字典类型列表（可选择）
 * 右侧：选中类型下的字典项列表（含 CRUD）
 */
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'
import { fetchAdminPage } from '@mortise/core-sdk'
import type { DictTypeInfo, DictInfo } from '~/types/dict'

const { $api } = useNuxtApp()

// ============ 字典类型（左侧） ============
const {
  loading: typesLoading,
  errorMessage: typesError,
  records: typeRecords,
  pageNum: typesPageNum,
  total: typesTotal,
  hasNext: typesHasNext,
  hasPrevious: typesHasPrevious,
  keyword: typesKeyword,
  load: loadTypes,
} = usePagedAdminResource<DictTypeInfo>({
  path: '/api/v1/admin/dictionary-types',
  errorMessage: '加载字典类型失败',
})

await loadTypes()

// 选中的字典类型
const selectedType = ref<DictTypeInfo | null>(null)

function selectType(type: DictTypeInfo) {
  selectedType.value = type
  dictPageNum.value = 1
  loadDicts()
}

// ============ 字典项（右侧） ============
const dictsLoading = ref(false)
const dictsError = ref('')
const dictRecords = ref<DictInfo[]>([])
const dictPageNum = ref(1)
const dictPageSize = ref(10)
const dictTotal = ref(0)
const dictTotalPage = ref(0)
const dictHasNext = ref(false)
const dictHasPrevious = ref(false)

async function loadDicts() {
  if (!selectedType.value?.typeCode) {
    dictRecords.value = []
    dictTotal.value = 0
    return
  }
  dictsLoading.value = true
  dictsError.value = ''
  try {
    const page = await fetchAdminPage<DictInfo>(
      $api,
      '/api/v1/admin/dictionaries',
      {
        dictTypeCode: selectedType.value.typeCode,
        pageNumber: dictPageNum.value,
        pageSize: dictPageSize.value,
      },
    )
    dictRecords.value = page.records || []
    dictTotal.value = page.totalRow || 0
    dictTotalPage.value = page.totalPage || 0

    if (
      typeof page.pageNumber === 'number'
      && page.pageNumber > 0
      && page.pageNumber !== dictPageNum.value
    ) {
      dictPageNum.value = page.pageNumber
    }

    dictHasPrevious.value = Boolean(page.hasPrevious)
    dictHasNext.value = Boolean(page.hasNext)
  }
  catch (err) {
    dictsError.value = err instanceof Error ? err.message : '加载字典数据失败'
  }
  finally {
    dictsLoading.value = false
  }
}

watch([dictPageNum, dictPageSize], () => {
  if (selectedType.value) loadDicts()
})

// ============ 字典类型 CRUD 弹窗 ============
const showTypeAddModal = ref(false)
const showTypeEditModal = ref(false)
const showTypeDeleteModal = ref(false)
const currentTypeRow = ref<Record<string, unknown>>({})

function openTypeEditModal(row: Record<string, unknown>) {
  currentTypeRow.value = { ...row }
  showTypeEditModal.value = true
}

function openTypeDeleteModal(row: Record<string, unknown>) {
  currentTypeRow.value = { ...row }
  showTypeDeleteModal.value = true
}

function onTypesSuccess() {
  loadTypes()
  // 如果删除了选中的类型，清空右侧
  if (
    selectedType.value
    && currentTypeRow.value?.id === selectedType.value.id
  ) {
    selectedType.value = null
    dictRecords.value = []
  }
}

// ============ 字典项 CRUD 弹窗 ============
const showDictAddModal = ref(false)
const showDictEditModal = ref(false)
const showDictDeleteModal = ref(false)
const currentDictRow = ref<Record<string, unknown>>({})

function openDictEditModal(row: Record<string, unknown>) {
  currentDictRow.value = { ...row }
  showDictEditModal.value = true
}

function openDictDeleteModal(row: Record<string, unknown>) {
  currentDictRow.value = { ...row }
  showDictDeleteModal.value = true
}

// 分页导航
function dictPrevPage() {
  if (dictHasPrevious.value) dictPageNum.value--
}
function dictNextPage() {
  if (dictHasNext.value) dictPageNum.value++
}
function typesPrevPage() {
  if (typesHasPrevious.value) typesPageNum.value--
}
function typesNextPage() {
  if (typesHasNext.value) typesPageNum.value++
}
</script>

<template>
  <UDashboardPanel id="system-dict-types">
    <template #header>
      <UDashboardNavbar title="字典管理">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="flex h-full gap-4">
        <!-- 左侧：字典类型列表 -->
        <DictTypesTypeListPanel
          v-model:keyword="typesKeyword"
          :records="typeRecords"
          :loading="typesLoading"
          :error-message="typesError"
          :total="typesTotal"
          :has-next="typesHasNext"
          :has-previous="typesHasPrevious"
          :selected-type="selectedType"
          @select="selectType"
          @add="showTypeAddModal = true"
          @edit="openTypeEditModal"
          @delete="openTypeDeleteModal"
          @reload="loadTypes"
          @prev-page="typesPrevPage"
          @next-page="typesNextPage"
        />

        <!-- 右侧：字典项列表 -->
        <DictTypesItemsTablePanel
          :selected-type="selectedType"
          :records="dictRecords"
          :loading="dictsLoading"
          :error-message="dictsError"
          :total="dictTotal"
          :page-num="dictPageNum"
          :has-next="dictHasNext"
          :has-previous="dictHasPrevious"
          @add="showDictAddModal = true"
          @edit="openDictEditModal"
          @delete="openDictDeleteModal"
          @reload="loadDicts"
          @prev-page="dictPrevPage"
          @next-page="dictNextPage"
        />
      </div>

      <!-- 字典类型弹窗 -->
      <DictTypesDictTypeAddModal
        v-model:open="showTypeAddModal"
        @success="onTypesSuccess"
      />
      <DictTypesDictTypeEditModal
        v-model:open="showTypeEditModal"
        :dict-type="currentTypeRow"
        @success="onTypesSuccess"
      />
      <DictTypesDictTypeDeleteModal
        v-model:open="showTypeDeleteModal"
        :dict-type="currentTypeRow"
        @success="onTypesSuccess"
      />

      <!-- 字典项弹窗 -->
      <DictsDictAddModal v-model:open="showDictAddModal" @success="loadDicts" />
      <DictsDictEditModal
        v-model:open="showDictEditModal"
        :dict="currentDictRow"
        @success="loadDicts"
      />
      <DictsDictDeleteModal
        v-model:open="showDictDeleteModal"
        :dict="currentDictRow"
        @success="loadDicts"
      />
    </template>
  </UDashboardPanel>
</template>
