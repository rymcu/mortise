<script setup lang="ts">
import type { CategoryTree } from '~/types/product'

defineProps<{
  filteredTree: CategoryTree[]
  expandedIds: Set<string>
  selectedCategory: CategoryTree | null
  treeLoading: boolean
  treeError: string
  treeKeyword: string
  productTotal: number
}>()

defineEmits<{
  'update:treeKeyword': [value: string]
  'toggle-expand': [id: string]
  'select-category': [cat: CategoryTree]
  'clear-filter': []
  'add-category': []
  'edit-category': [cat: CategoryTree]
  'delete-category': [cat: CategoryTree]
  'reload': []
}>()
</script>

<template>
  <div class="w-72 shrink-0">
    <UCard class="h-full">
      <div class="mb-3 flex items-center justify-between gap-2">
        <h3 class="text-sm font-medium">产品分类</h3>
        <div class="flex items-center gap-1">
          <UButton
            icon="i-lucide-plus"
            color="primary"
            variant="ghost"
            size="xs"
            @click="$emit('add-category')"
          />
          <UButton
            icon="i-lucide-refresh-cw"
            color="neutral"
            variant="ghost"
            size="xs"
            :loading="treeLoading"
            @click="$emit('reload')"
          />
        </div>
      </div>

      <UInput
        :model-value="treeKeyword"
        placeholder="搜索分类名称"
        icon="i-lucide-search"
        size="sm"
        class="mb-2"
        @update:model-value="$emit('update:treeKeyword', $event as string)"
      />

      <UAlert
        v-if="treeError"
        color="error"
        variant="soft"
        :title="treeError"
        class="mb-2"
      />

      <!-- 全部产品入口 -->
      <div
        class="mb-1 flex cursor-pointer items-center gap-2 rounded-lg px-3 py-2 text-sm transition-colors"
        :class="
          !selectedCategory
            ? 'bg-primary/10 text-primary ring-primary/20 ring-1'
            : 'hover:bg-elevated/50'
        "
        @click="$emit('clear-filter')"
      >
        <UIcon name="i-lucide-layout-grid" class="size-4 shrink-0" />
        <span class="font-medium">全部产品</span>
        <span class="text-muted ml-auto text-xs">{{
          productTotal
        }}</span>
      </div>

      <!-- 分类树节点 -->
      <div class="max-h-[65vh] overflow-y-auto">
        <template v-if="filteredTree.length">
          <div
            v-for="node in filteredTree"
            :key="node.id"
          >
            <!-- 一级分类 -->
            <div
              class="group flex cursor-pointer items-center gap-1 rounded-lg px-2 py-1.5 text-sm transition-colors"
              :class="
                selectedCategory?.id === node.id
                  ? 'bg-primary/10 text-primary ring-primary/20 ring-1'
                  : 'hover:bg-elevated/50'
              "
              @click="$emit('select-category', node)"
            >
              <!-- 展开/折叠图标 -->
              <UButton
                v-if="node.children?.length"
                :icon="
                  expandedIds.has(node.id)
                    ? 'i-lucide-chevron-down'
                    : 'i-lucide-chevron-right'
                "
                color="neutral"
                variant="ghost"
                size="xs"
                class="shrink-0"
                @click.stop="$emit('toggle-expand', node.id)"
              />
              <span v-else class="size-6 shrink-0" />

              <div class="min-w-0 flex-1">
                <div class="truncate font-medium">{{ node.name }}</div>
              </div>

              <!-- 操作按钮，hover 显示 -->
              <div
                class="ml-1 flex shrink-0 items-center gap-0.5 opacity-0 group-hover:opacity-100"
              >
                <UButton
                  icon="i-lucide-pencil"
                  color="neutral"
                  variant="ghost"
                  size="xs"
                  @click.stop="$emit('edit-category', node)"
                />
                <UButton
                  icon="i-lucide-trash-2"
                  color="error"
                  variant="ghost"
                  size="xs"
                  @click.stop="$emit('delete-category', node)"
                />
              </div>
            </div>

            <!-- 子分类 -->
            <div
              v-if="node.children?.length && expandedIds.has(node.id)"
              class="ml-4"
            >
              <div
                v-for="child in node.children"
                :key="child.id"
                class="group flex cursor-pointer items-center gap-1 rounded-lg px-2 py-1.5 text-sm transition-colors"
                :class="
                  selectedCategory?.id === child.id
                    ? 'bg-primary/10 text-primary ring-primary/20 ring-1'
                    : 'hover:bg-elevated/50'
                "
                @click="$emit('select-category', child)"
              >
                <span class="size-6 shrink-0" />
                <div class="min-w-0 flex-1">
                  <div class="text-muted truncate">{{ child.name }}</div>
                </div>
                <div
                  class="ml-1 flex shrink-0 items-center gap-0.5 opacity-0 group-hover:opacity-100"
                >
                  <UButton
                    icon="i-lucide-pencil"
                    color="neutral"
                    variant="ghost"
                    size="xs"
                    @click.stop="$emit('edit-category', child)"
                  />
                  <UButton
                    icon="i-lucide-trash-2"
                    color="error"
                    variant="ghost"
                    size="xs"
                    @click.stop="$emit('delete-category', child)"
                  />
                </div>
              </div>
            </div>
          </div>
        </template>
        <div
          v-else-if="!treeLoading"
          class="text-muted py-4 text-center text-sm"
        >
          暂无分类数据
        </div>
      </div>
    </UCard>
  </div>
</template>
