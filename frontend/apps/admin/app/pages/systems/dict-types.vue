<script setup lang="ts">
/**
 * 字典管理 - 主从视图
 * 左侧：字典类型列表（可选择）
 * 右侧：选中类型下的字典项列表（含 CRUD）
 */
import { usePagedAdminResource } from '~/composables/usePagedAdminResource'
import { fetchAdminPage } from '@mortise/core-sdk'

interface DictTypeInfo {
  id: number
  typeCode?: string
  label?: string
  description?: string
  sortNo?: number
  status?: number
  createdTime?: string
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

// ============ 字典类型（左侧） ============
const {
  loading: typesLoading,
  errorMessage: typesError,
  records: typeRecords,
  pageNum: typesPageNum,
  pageSize: typesPageSize,
  total: typesTotal,
  keyword: typesKeyword,
  load: loadTypes
} = usePagedAdminResource<DictTypeInfo>({
  path: '/api/v1/admin/dictionary-types',
  errorMessage: '加载字典类型失败'
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
        pageNum: dictPageNum.value,
        pageSize: dictPageSize.value
      }
    )
    dictRecords.value = page.records || []
    dictTotal.value = page.totalRow || 0
  } catch (err) {
    dictsError.value = err instanceof Error ? err.message : '加载字典数据失败'
  } finally {
    dictsLoading.value = false
  }
}

watch([dictPageNum, dictPageSize], () => {
  if (selectedType.value) loadDicts()
})

const dictColumns = [
  { key: 'label', label: '标签' },
  { key: 'value', label: '值' },
  { key: 'sortNo', label: '排序' },
  { key: 'status', label: '状态' },
  { key: 'icon', label: '图标' },
  { key: 'color', label: '颜色' }
]

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
    selectedType.value &&
    currentTypeRow.value?.id === selectedType.value.id
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
  if (dictPageNum.value > 1) dictPageNum.value--
}
function dictNextPage() {
  if (dictRecords.value.length >= dictPageSize.value) dictPageNum.value++
}
function typesPrevPage() {
  if (typesPageNum.value > 1) typesPageNum.value--
}
function typesNextPage() {
  if (typeRecords.value.length >= typesPageSize.value) typesPageNum.value++
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
        <div class="w-80 shrink-0">
          <UCard>
            <div class="mb-3 flex items-center justify-between gap-2">
              <h3 class="text-sm font-medium">字典类型</h3>
              <div class="flex items-center gap-1">
                <UButton
                  icon="i-lucide-plus"
                  color="primary"
                  variant="ghost"
                  size="xs"
                  @click="showTypeAddModal = true"
                />
                <UButton
                  icon="i-lucide-refresh-cw"
                  color="neutral"
                  variant="ghost"
                  size="xs"
                  :loading="typesLoading"
                  @click="loadTypes"
                />
              </div>
            </div>

            <UInput
              v-model="typesKeyword"
              placeholder="搜索类型编码/名称"
              icon="i-lucide-search"
              size="sm"
              class="mb-3"
              @keyup.enter="loadTypes"
            />

            <UAlert
              v-if="typesError"
              color="error"
              variant="soft"
              :title="typesError"
              class="mb-2"
            />

            <!-- 类型列表 -->
            <div class="max-h-[60vh] space-y-1 overflow-y-auto">
              <div
                v-for="type in typeRecords"
                :key="type.id"
                class="cursor-pointer rounded-lg px-3 py-2 text-sm transition-colors"
                :class="
                  selectedType?.id === type.id
                    ? 'bg-primary/10 text-primary ring-primary/20 ring-1'
                    : 'hover:bg-elevated/50'
                "
                @click="selectType(type)"
              >
                <div class="flex items-center justify-between">
                  <div class="min-w-0 flex-1">
                    <div class="truncate font-medium">
                      {{ type.label }}
                    </div>
                    <div class="text-muted truncate text-xs">
                      {{ type.typeCode }}
                    </div>
                  </div>
                  <div class="ml-2 flex shrink-0 items-center gap-1">
                    <UButton
                      icon="i-lucide-pencil"
                      color="neutral"
                      variant="ghost"
                      size="xs"
                      @click.stop="
                        openTypeEditModal(
                          type as unknown as Record<string, unknown>
                        )
                      "
                    />
                    <UButton
                      icon="i-lucide-trash-2"
                      color="error"
                      variant="ghost"
                      size="xs"
                      @click.stop="
                        openTypeDeleteModal(
                          type as unknown as Record<string, unknown>
                        )
                      "
                    />
                  </div>
                </div>
              </div>
              <div
                v-if="!typeRecords.length && !typesLoading"
                class="text-muted py-4 text-center text-sm"
              >
                暂无字典类型
              </div>
            </div>

            <!-- 类型翻页 -->
            <div
              class="text-muted mt-3 flex items-center justify-between text-xs"
            >
              <span>共 {{ typesTotal }} 条</span>
              <div class="flex items-center gap-1">
                <UButton
                  color="neutral"
                  variant="ghost"
                  size="xs"
                  :disabled="typesPageNum <= 1"
                  @click="typesPrevPage"
                >
                  上一页
                </UButton>
                <UButton
                  color="neutral"
                  variant="ghost"
                  size="xs"
                  :disabled="typeRecords.length < typesPageSize"
                  @click="typesNextPage"
                >
                  下一页
                </UButton>
              </div>
            </div>
          </UCard>
        </div>

        <!-- 右侧：字典项列表 -->
        <div class="min-w-0 flex-1">
          <UCard>
            <template v-if="selectedType">
              <div class="mb-4 flex items-center justify-between gap-2">
                <div>
                  <h3 class="text-sm font-medium">
                    {{ selectedType.label }}
                    <span class="text-muted font-normal"
                      >（{{ selectedType.typeCode }}）</span
                    >
                  </h3>
                  <p
                    v-if="selectedType.description"
                    class="text-muted mt-0.5 text-xs"
                  >
                    {{ selectedType.description }}
                  </p>
                </div>
                <div class="flex items-center gap-2">
                  <UButton
                    icon="i-lucide-plus"
                    color="primary"
                    variant="soft"
                    size="sm"
                    @click="showDictAddModal = true"
                  >
                    新增字典
                  </UButton>
                  <UButton
                    icon="i-lucide-refresh-cw"
                    color="neutral"
                    variant="soft"
                    size="sm"
                    :loading="dictsLoading"
                    @click="loadDicts"
                  >
                    刷新
                  </UButton>
                </div>
              </div>

              <UAlert
                v-if="dictsError"
                color="error"
                variant="soft"
                :title="dictsError"
                class="mb-4"
              />

              <div class="overflow-x-auto">
                <table class="min-w-full text-sm">
                  <thead>
                    <tr class="border-default border-b">
                      <th
                        v-for="col in dictColumns"
                        :key="col.key"
                        class="px-2 py-2 text-left"
                      >
                        {{ col.label }}
                      </th>
                      <th class="px-2 py-2 text-right">操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="dict in dictRecords"
                      :key="dict.id"
                      class="border-default/60 hover:bg-elevated/50 border-b transition-colors"
                    >
                      <td class="px-2 py-2">
                        {{ dict.label || '-' }}
                      </td>
                      <td class="text-muted px-2 py-2">
                        {{ dict.value || '-' }}
                      </td>
                      <td class="text-muted px-2 py-2">
                        {{ dict.sortNo ?? '-' }}
                      </td>
                      <td class="px-2 py-2">
                        <UBadge
                          :color="dict.status === 0 ? 'success' : 'neutral'"
                          variant="subtle"
                        >
                          {{ dict.status === 0 ? '启用' : '禁用' }}
                        </UBadge>
                      </td>
                      <td class="px-2 py-2">
                        <UIcon
                          v-if="dict.icon"
                          :name="String(dict.icon)"
                          class="text-lg"
                        />
                        <span v-else class="text-muted">-</span>
                      </td>
                      <td class="px-2 py-2">
                        <div
                          v-if="dict.color"
                          class="flex items-center gap-1.5"
                        >
                          <span
                            class="inline-block h-3 w-3 rounded-full"
                            :class="`bg-${dict.color}-500`"
                          />
                          <span class="text-xs">{{ dict.color }}</span>
                        </div>
                        <span v-else class="text-muted">-</span>
                      </td>
                      <td class="px-2 py-2 text-right">
                        <div class="flex items-center justify-end gap-1">
                          <UButton
                            icon="i-lucide-pencil"
                            color="primary"
                            variant="ghost"
                            size="xs"
                            @click="
                              openDictEditModal(
                                dict as unknown as Record<string, unknown>
                              )
                            "
                          >
                            编辑
                          </UButton>
                          <UButton
                            icon="i-lucide-trash-2"
                            color="error"
                            variant="ghost"
                            size="xs"
                            @click="
                              openDictDeleteModal(
                                dict as unknown as Record<string, unknown>
                              )
                            "
                          >
                            删除
                          </UButton>
                        </div>
                      </td>
                    </tr>
                    <tr v-if="!dictRecords.length && !dictsLoading">
                      <td
                        :colspan="dictColumns.length + 1"
                        class="text-muted py-6 text-center"
                      >
                        暂无字典数据
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div
                class="text-muted mt-4 flex items-center justify-between text-sm"
              >
                <span>共 {{ dictTotal }} 条</span>
                <div class="flex items-center gap-2">
                  <UButton
                    color="neutral"
                    variant="ghost"
                    :disabled="dictPageNum <= 1"
                    @click="dictPrevPage"
                  >
                    上一页
                  </UButton>
                  <span>第 {{ dictPageNum }} 页</span>
                  <UButton
                    color="neutral"
                    variant="ghost"
                    :disabled="dictRecords.length < dictPageSize"
                    @click="dictNextPage"
                  >
                    下一页
                  </UButton>
                </div>
              </div>
            </template>

            <!-- 未选择类型时的占位 -->
            <div
              v-else
              class="text-muted flex flex-col items-center justify-center py-16"
            >
              <UIcon name="i-lucide-book-open" class="mb-3 text-4xl" />
              <p class="text-sm">请从左侧选择一个字典类型</p>
              <p class="mt-1 text-xs">选择后可查看和管理该类型下的字典项</p>
            </div>
          </UCard>
        </div>
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
