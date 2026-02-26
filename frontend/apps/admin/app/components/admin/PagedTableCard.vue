<script setup lang="ts">
interface ColumnDef {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
}

const props = withDefaults(
  defineProps<{
    columns: ColumnDef[]
    rows: Record<string, unknown>[]
    loading: boolean
    errorMessage?: string
    total: number
    pageNum: number
    pageSize: number
    keyword: string
    searchPlaceholder?: string
    emptyText?: string
    /** 是否显示操作列 */
    showActions?: boolean
    /** 操作列标题 */
    actionsLabel?: string
  }>(),
  {
    errorMessage: '',
    searchPlaceholder: '搜索',
    emptyText: '暂无数据',
    showActions: false,
    actionsLabel: '操作'
  }
)

const emit = defineEmits<{
  (e: 'update:keyword', value: string): void
  (e: 'update:pageNum', value: number): void
  (e: 'refresh' | 'searchEnter'): void
}>()

/** 最终列 = 原始列 + (可选)操作列 */
const allColumns = computed(() => {
  const base = [...props.columns]
  if (props.showActions) {
    base.push({ key: '_actions', label: props.actionsLabel, align: 'right' })
  }
  return base
})

function cellText(row: Record<string, unknown>, key: string): string {
  const value = row[key]
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return String(value)
}

function prevPage() {
  if (props.pageNum > 1) {
    emit('update:pageNum', props.pageNum - 1)
  }
}

function nextPage() {
  if (props.rows.length >= props.pageSize) {
    emit('update:pageNum', props.pageNum + 1)
  }
}
</script>

<template>
  <div>
    <UAlert
      v-if="errorMessage"
      color="error"
      variant="soft"
      :title="errorMessage"
      class="mb-4"
    />

    <UCard>
      <div class="mb-4 flex flex-wrap items-center justify-between gap-2">
        <div class="flex flex-wrap items-center gap-2">
          <UInput
            :model-value="keyword"
            :placeholder="searchPlaceholder"
            icon="i-lucide-search"
            class="w-72"
            @update:model-value="emit('update:keyword', String($event || ''))"
            @keyup.enter="emit('searchEnter')"
          />
          <UButton
            color="neutral"
            variant="soft"
            icon="i-lucide-refresh-cw"
            :loading="loading"
            @click="emit('refresh')"
          >
            刷新
          </UButton>
        </div>
        <!-- 工具栏右侧插槽：放置新增、批量删除等按钮 -->
        <div class="flex items-center gap-2">
          <slot name="toolbar" />
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full text-sm">
          <thead>
            <tr class="border-default border-b">
              <th
                v-for="column in allColumns"
                :key="column.key"
                class="px-2 py-2"
                :class="{
                  'text-left': (column.align || 'left') === 'left',
                  'text-center': column.align === 'center',
                  'text-right': column.align === 'right'
                }"
              >
                {{ column.label }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="row in rows"
              :key="String(row.id || JSON.stringify(row))"
              class="border-default/60 hover:bg-elevated/50 border-b transition-colors"
            >
              <td
                v-for="column in allColumns"
                :key="column.key"
                class="px-2 py-2"
                :class="{
                  'text-left': (column.align || 'left') === 'left',
                  'text-center': column.align === 'center',
                  'text-right': column.align === 'right'
                }"
              >
                <!-- 操作列使用专用插槽 -->
                <template v-if="column.key === '_actions'">
                  <div class="flex items-center justify-end gap-1">
                    <slot name="actions" :row="row" />
                  </div>
                </template>
                <slot v-else :name="`cell-${column.key}`" :row="row">
                  {{ cellText(row, column.key) }}
                </slot>
              </td>
            </tr>
            <tr v-if="!rows.length && !loading">
              <td
                :colspan="allColumns.length"
                class="text-muted py-6 text-center"
              >
                {{ emptyText }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="text-muted mt-4 flex items-center justify-between text-sm">
        <span>共 {{ total }} 条</span>
        <div class="flex items-center gap-2">
          <UButton
            color="neutral"
            variant="ghost"
            :disabled="pageNum <= 1"
            @click="prevPage"
            >上一页</UButton
          >
          <span>第 {{ pageNum }} 页</span>
          <UButton
            color="neutral"
            variant="ghost"
            :disabled="rows.length < pageSize"
            @click="nextPage"
            >下一页</UButton
          >
        </div>
      </div>
    </UCard>
  </div>
</template>
