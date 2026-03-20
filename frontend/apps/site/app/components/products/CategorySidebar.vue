<script setup lang="ts">
interface CategoryEntry {
  category: { id: string; name: string; description?: string }
  count: number
}

defineProps<{
  entries: CategoryEntry[]
  activeCategoryId: string
  loading: boolean
}>()

defineEmits<{
  select: [categoryId: string]
  'clear-filter': []
}>()
</script>

<template>
  <aside class="space-y-6 lg:sticky lg:top-24 lg:self-start">
    <div>
      <div class="text-3xl font-semibold text-highlighted">所有产品</div>
      <p class="mt-3 text-sm leading-6 text-muted">
        按类目筛选并浏览公开产品目录。
      </p>
    </div>

    <div class="space-y-4 border-t border-default pt-6">
      <div class="flex items-center justify-between gap-4">
        <div>
          <div class="text-xl font-semibold text-highlighted">类目筛选</div>
          <p class="mt-1 text-sm text-muted">按产品类别快速定位</p>
        </div>

        <button
          type="button"
          class="text-sm font-medium text-primary transition hover:text-primary/80"
          @click="$emit('clear-filter')"
        >
          清除筛选
        </button>
      </div>

      <div v-if="loading" class="space-y-3">
        <USkeleton v-for="n in 10" :key="n" class="h-11 rounded-none" />
      </div>

      <div v-else class="space-y-1">
        <button
          v-for="entry in entries"
          :key="entry.category.id"
          type="button"
          class="group flex w-full items-center justify-between gap-4 border-l-2 px-3 py-3 text-left transition duration-200"
          :class="entry.category.id === activeCategoryId
            ? 'border-primary bg-primary/6 text-primary'
            : 'border-transparent text-highlighted hover:bg-elevated/60 hover:text-primary'"
          @click="$emit('select', entry.category.id)"
        >
          <div class="min-w-0 flex-1">
            <div class="line-clamp-1 text-lg font-medium transition duration-200 group-hover:translate-x-0.5">
              {{ entry.category.name }}
            </div>
          </div>

          <div class="shrink-0 text-lg text-muted">
            ({{ entry.count }})
          </div>
        </button>
      </div>
    </div>
  </aside>
</template>
