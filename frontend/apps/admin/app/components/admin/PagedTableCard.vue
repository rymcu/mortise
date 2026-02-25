<script setup lang="ts">
interface ColumnDef {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
}

const props = withDefaults(defineProps<{
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
}>(), {
  errorMessage: '',
  searchPlaceholder: '搜索',
  emptyText: '暂无数据'
})

const emit = defineEmits<{
  (e: 'update:keyword', value: string): void
  (e: 'update:pageNum', value: number): void
  (e: 'refresh'): void
  (e: 'searchEnter'): void
}>()

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
    <UAlert v-if="errorMessage" color="error" variant="soft" :title="errorMessage" class="mb-4" />

    <UCard>
      <div class="mb-4 flex flex-wrap items-center gap-2">
        <UInput
          :model-value="keyword"
          :placeholder="searchPlaceholder"
          icon="i-lucide-search"
          class="w-72"
          @update:model-value="emit('update:keyword', String($event || ''))"
          @keyup.enter="emit('searchEnter')"
        />
        <UButton color="neutral" variant="soft" icon="i-lucide-refresh-cw" :loading="loading" @click="emit('refresh')">
          刷新
        </UButton>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full text-sm">
          <thead>
            <tr class="border-b border-default">
              <th
                v-for="column in columns"
                :key="column.key"
                class="py-2"
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
            <tr v-for="row in rows" :key="String(row.id || JSON.stringify(row))" class="border-b border-default/60">
              <td
                v-for="column in columns"
                :key="column.key"
                class="py-2"
                :class="{
                  'text-left': (column.align || 'left') === 'left',
                  'text-center': column.align === 'center',
                  'text-right': column.align === 'right'
                }"
              >
                <slot :name="`cell-${column.key}`" :row="row">
                  {{ cellText(row, column.key) }}
                </slot>
              </td>
            </tr>
            <tr v-if="!rows.length && !loading">
              <td :colspan="columns.length" class="py-6 text-center text-muted">
                {{ emptyText }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mt-4 flex items-center justify-between text-sm text-muted">
        <span>共 {{ total }} 条</span>
        <div class="flex items-center gap-2">
          <UButton color="neutral" variant="ghost" :disabled="pageNum <= 1" @click="prevPage">上一页</UButton>
          <span>第 {{ pageNum }} 页</span>
          <UButton color="neutral" variant="ghost" :disabled="rows.length < pageSize" @click="nextPage">下一页</UButton>
        </div>
      </div>
    </UCard>
  </div>
</template>
