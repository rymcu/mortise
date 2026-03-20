<script setup lang="ts">
/**
 * 字典类型列表面板（左侧）
 * 展示字典类型列表，支持搜索、翻页、选择、编辑和删除操作
 */
import type { DictTypeInfo } from '~/types/dict'

const props = defineProps<{
  records: DictTypeInfo[]
  loading: boolean
  errorMessage: string
  total: number
  hasNext: boolean
  hasPrevious: boolean
  selectedType: DictTypeInfo | null
}>()

const keyword = defineModel<string>('keyword', { default: '' })

const emit = defineEmits<{
  'select': [type: DictTypeInfo]
  'add': []
  'edit': [row: Record<string, unknown>]
  'delete': [row: Record<string, unknown>]
  'reload': []
  'prev-page': []
  'next-page': []
}>()
</script>

<template>
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
            @click="emit('add')"
          />
          <UButton
            icon="i-lucide-refresh-cw"
            color="neutral"
            variant="ghost"
            size="xs"
            :loading="props.loading"
            @click="emit('reload')"
          />
        </div>
      </div>

      <UInput
        v-model="keyword"
        placeholder="搜索类型编码/名称"
        icon="i-lucide-search"
        size="sm"
        class="mb-3"
        @keyup.enter="emit('reload')"
      />

      <UAlert
        v-if="props.errorMessage"
        color="error"
        variant="soft"
        :title="props.errorMessage"
        class="mb-2"
      />

      <!-- 类型列表 -->
      <div class="max-h-[60vh] space-y-1 overflow-y-auto">
        <div
          v-for="type in props.records"
          :key="type.id"
          class="cursor-pointer rounded-lg px-3 py-2 text-sm transition-colors"
          :class="
            props.selectedType?.id === type.id
              ? 'bg-primary/10 text-primary ring-primary/20 ring-1'
              : 'hover:bg-elevated/50'
          "
          @click="emit('select', type)"
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
                  emit('edit', type as unknown as Record<string, unknown>)
                "
              />
              <UButton
                icon="i-lucide-trash-2"
                color="error"
                variant="ghost"
                size="xs"
                @click.stop="
                  emit('delete', type as unknown as Record<string, unknown>)
                "
              />
            </div>
          </div>
        </div>
        <div
          v-if="!props.records.length && !props.loading"
          class="text-muted py-4 text-center text-sm"
        >
          暂无字典类型
        </div>
      </div>

      <!-- 类型翻页 -->
      <div
        class="text-muted mt-3 flex items-center justify-between text-xs"
      >
        <span>共 {{ props.total }} 条</span>
        <div class="flex items-center gap-1">
          <UButton
            color="neutral"
            variant="ghost"
            size="xs"
            :disabled="!props.hasPrevious"
            @click="emit('prev-page')"
          >
            上一页
          </UButton>
          <UButton
            color="neutral"
            variant="ghost"
            size="xs"
            :disabled="!props.hasNext"
            @click="emit('next-page')"
          >
            下一页
          </UButton>
        </div>
      </div>
    </UCard>
  </div>
</template>
