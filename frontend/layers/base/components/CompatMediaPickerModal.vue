<script setup lang="ts">
import type {
  CompatMediaPickerProvider,
  CompatMediaPickerSelection,
} from '../types/media-picker'

const open = defineModel<boolean>('open', { default: false })

const props = withDefaults(
  defineProps<{
    provider: CompatMediaPickerProvider
    title?: string
    description?: string
    filterValues?: Record<string, string>
  }>(),
  {
    title: '选择媒体资源',
    description: '从当前媒体源中选择已存在的资源，并回填到当前表单。',
    filterValues: () => ({}),
  },
)

const emit = defineEmits<{
  (e: 'select', selection: CompatMediaPickerSelection): void
}>()

const loading = ref(false)
const errorMessage = ref('')
const records = ref<unknown[]>([])
const pageNum = ref(1)
const total = ref(0)
const totalPage = ref(0)
const hasNext = ref(false)
const hasPrevious = ref(false)
const keyword = ref('')
const filters = reactive<Record<string, string>>({})

const resolvedTitle = computed(() => props.title || '选择媒体资源')
const resolvedDescription = computed(() =>
  props.description || '从当前媒体源中选择已存在的资源，并回填到当前表单。',
)
const searchPlaceholder = computed(() =>
  props.provider.searchPlaceholder || '搜索媒体资源',
)
const emptyText = computed(() =>
  props.provider.emptyText || '当前筛选条件下暂无可选媒体资源',
)

function syncFilters() {
  const nextFilters = {
    ...(props.provider.defaultFilters || {}),
    ...(props.filterValues || {}),
  }

  for (const key of Object.keys(filters)) {
    if (!(key in nextFilters)) {
      delete filters[key]
    }
  }

  Object.assign(filters, nextFilters)
}

async function load() {
  loading.value = true
  errorMessage.value = ''

  try {
    const page = await props.provider.load({
      keyword: keyword.value.trim(),
      pageNum: pageNum.value,
      filters: { ...filters },
    })

    records.value = page.records || []
    total.value = page.total || 0
    totalPage.value = page.totalPage || 0
    hasNext.value = Boolean(page.hasNext)
    hasPrevious.value = Boolean(page.hasPrevious)
  } catch (error) {
    records.value = []
    total.value = 0
    totalPage.value = 0
    hasNext.value = false
    hasPrevious.value = false
    errorMessage.value = error instanceof Error ? error.message : '加载媒体资源失败'
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  if (pageNum.value === 1) {
    void load()
    return
  }

  pageNum.value = 1
}

function selectRecord(record: unknown) {
  emit('select', props.provider.toSelection(record))
  open.value = false
}

watch(
  () => [props.provider, props.filterValues],
  () => {
    syncFilters()
  },
  { immediate: true },
)

watch(open, async (value) => {
  if (!value) {
    return
  }

  pageNum.value = 1
  await load()
})

watch(pageNum, (value, oldValue) => {
  if (!open.value || value === oldValue) {
    return
  }

  void load()
})
</script>

<template>
  <UModal
    v-model:open="open"
    :title="resolvedTitle"
    :ui="{ content: 'sm:max-w-6xl' }"
  >
    <template #body>
      <div class="space-y-4">
        <UAlert
          color="primary"
          variant="soft"
          :description="resolvedDescription"
        />

        <div class="flex flex-wrap items-center gap-2">
          <UInput
            v-model="keyword"
            class="min-w-56 flex-1"
            :placeholder="searchPlaceholder"
            @keyup.enter="applyFilters"
          />

          <template v-for="field in props.provider.filters || []" :key="field.key">
            <USelect
              v-if="field.type === 'select'"
              v-model="filters[field.key]"
              :items="field.items || []"
              value-key="value"
              label-key="label"
              :placeholder="field.placeholder"
              :class="field.className || 'w-32'"
            />

            <UInput
              v-else
              v-model="filters[field.key]"
              :placeholder="field.placeholder"
              :class="field.className || 'w-36'"
              @keyup.enter="applyFilters"
            />
          </template>

          <UButton color="neutral" variant="soft" :loading="loading" @click="applyFilters">
            筛选
          </UButton>
        </div>

        <UAlert
          v-if="errorMessage"
          color="error"
          variant="soft"
          :title="errorMessage"
        />

        <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <button
            v-for="record in records"
            :key="props.provider.getKey(record)"
            type="button"
            class="border-default hover:border-primary/50 hover:bg-elevated/60 flex h-full flex-col overflow-hidden rounded-xl border text-left transition-colors"
            @click="selectRecord(record)"
          >
            <div class="bg-elevated/50 aspect-video overflow-hidden border-b border-default">
              <img
                v-if="props.provider.getPreviewUrl?.(record) && (props.provider.isPreviewImage?.(record) ?? true)"
                :src="props.provider.getPreviewUrl?.(record)"
                :alt="props.provider.getLabel(record)"
                class="h-full w-full object-cover"
              >
              <div
                v-else
                class="text-muted flex h-full flex-col items-center justify-center gap-2"
              >
                <UIcon :name="props.provider.getIcon?.(record) || 'i-lucide-image'" class="size-8" />
                <span class="text-sm">{{ props.provider.getLabel(record) }}</span>
              </div>
            </div>

            <div class="space-y-2 p-4">
              <div class="flex items-start justify-between gap-2">
                <p class="line-clamp-2 text-sm font-medium">
                  {{ props.provider.getLabel(record) }}
                </p>
                <UBadge
                  v-if="props.provider.getBadge?.(record)"
                  :color="props.provider.getBadge?.(record)?.color || 'neutral'"
                  variant="subtle"
                  size="sm"
                >
                  {{ props.provider.getBadge?.(record)?.label }}
                </UBadge>
              </div>

              <p
                v-for="line in props.provider.getMetaLines?.(record) || []"
                :key="line"
                class="truncate text-xs text-muted"
              >
                {{ line }}
              </p>
            </div>
          </button>

          <div
            v-if="!records.length && !loading"
            class="text-muted col-span-full rounded-xl border border-dashed border-default px-4 py-12 text-center text-sm"
          >
            {{ emptyText }}
          </div>
        </div>

        <div class="flex items-center justify-between text-sm text-muted">
          <span>共 {{ total }} 条</span>
          <div class="flex items-center gap-2">
            <UButton size="xs" color="neutral" variant="soft" :disabled="!hasPrevious" @click="pageNum--">
              上一页
            </UButton>
            <span>{{ pageNum }} / {{ totalPage || 1 }}</span>
            <UButton size="xs" color="neutral" variant="soft" :disabled="!hasNext" @click="pageNum++">
              下一页
            </UButton>
          </div>
        </div>
      </div>
    </template>

    <template #footer>
      <div class="flex w-full justify-end">
        <UButton color="neutral" variant="ghost" @click="open = false">
          关闭
        </UButton>
      </div>
    </template>
  </UModal>
</template>
