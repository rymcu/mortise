<script setup lang="ts">
/**
 * 字典项表格面板（右侧）
 * 展示选中字典类型下的字典项列表，支持增删改和翻页
 */
import type { DictTypeInfo, DictInfo } from '~/types/dict'

const props = defineProps<{
  selectedType: DictTypeInfo | null
  records: DictInfo[]
  loading: boolean
  errorMessage: string
  total: number
  pageNum: number
  hasNext: boolean
  hasPrevious: boolean
}>()

const emit = defineEmits<{
  'add': []
  'edit': [row: Record<string, unknown>]
  'delete': [row: Record<string, unknown>]
  'reload': []
  'prev-page': []
  'next-page': []
}>()

const dictColumns = [
  { key: 'label', label: '标签' },
  { key: 'value', label: '值' },
  { key: 'sortNo', label: '排序' },
  { key: 'status', label: '状态' },
  { key: 'icon', label: '图标' },
  { key: 'color', label: '颜色' },
]
</script>

<template>
  <div class="min-w-0 flex-1">
    <UCard>
      <template v-if="props.selectedType">
        <div class="mb-4 flex items-center justify-between gap-2">
          <div>
            <h3 class="text-sm font-medium">
              {{ props.selectedType.label }}
              <span class="text-muted font-normal"
                >（{{ props.selectedType.typeCode }}）</span
              >
            </h3>
            <p
              v-if="props.selectedType.description"
              class="text-muted mt-0.5 text-xs"
            >
              {{ props.selectedType.description }}
            </p>
          </div>
          <div class="flex items-center gap-2">
            <UButton
              icon="i-lucide-plus"
              color="primary"
              variant="soft"
              size="sm"
              @click="emit('add')"
            >
              新增字典
            </UButton>
            <UButton
              icon="i-lucide-refresh-cw"
              color="neutral"
              variant="soft"
              size="sm"
              :loading="props.loading"
              @click="emit('reload')"
            >
              刷新
            </UButton>
          </div>
        </div>

        <UAlert
          v-if="props.errorMessage"
          color="error"
          variant="soft"
          :title="props.errorMessage"
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
                v-for="dict in props.records"
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
                    :color="dict.status === 1 ? 'success' : 'neutral'"
                    variant="subtle"
                  >
                    {{ dict.status === 1 ? '启用' : '禁用' }}
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
                        emit(
                          'edit',
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
                        emit(
                          'delete',
                          dict as unknown as Record<string, unknown>
                        )
                      "
                    >
                      删除
                    </UButton>
                  </div>
                </td>
              </tr>
              <tr v-if="!props.records.length && !props.loading">
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
          <span>共 {{ props.total }} 条</span>
          <div class="flex items-center gap-2">
            <UButton
              color="neutral"
              variant="ghost"
              :disabled="!props.hasPrevious"
              @click="emit('prev-page')"
            >
              上一页
            </UButton>
            <span>第 {{ props.pageNum }} 页</span>
            <UButton
              color="neutral"
              variant="ghost"
              :disabled="!props.hasNext"
              @click="emit('next-page')"
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
</template>
