<script setup lang="ts">
/**
 * 图标选择器组件
 * 从 Lucide / Simple Icons 集合中搜索并选择图标
 */

const props = withDefaults(
  defineProps<{
    modelValue?: string
    placeholder?: string
  }>(),
  {
    modelValue: '',
    placeholder: '选择图标'
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const open = ref(false)
const search = ref('')
const activeCollection = ref<'lucide' | 'simple-icons'>('lucide')

interface IconifyJSON {
  prefix: string
  icons: Record<string, unknown>
}

const collections = reactive({
  'lucide': [] as string[],
  'simple-icons': [] as string[]
})
const loading = ref(false)

const MAX_DISPLAY = 200

const collectionOptions = [
  { key: 'lucide' as const, label: 'Lucide', prefix: 'i-lucide-' },
  { key: 'simple-icons' as const, label: 'Simple Icons', prefix: 'i-simple-icons-' }
]

async function loadCollection(key: 'lucide' | 'simple-icons') {
  if (collections[key].length > 0) return
  loading.value = true
  try {
    const mod = key === 'lucide'
      ? await import('@iconify-json/lucide/icons.json')
      : await import('@iconify-json/simple-icons/icons.json')
    const data = (('default' in mod ? mod.default : mod) as unknown) as IconifyJSON
    collections[key] = Object.keys(data.icons).sort()
  } finally {
    loading.value = false
  }
}

const currentPrefix = computed(() =>
  collectionOptions.find(c => c.key === activeCollection.value)!.prefix
)

const filteredIcons = computed(() => {
  const icons: string[] = collections[activeCollection.value]
  const prefix = currentPrefix.value
  const term = search.value.toLowerCase().trim()

  const filtered = term
    ? icons.filter(name => name.includes(term))
    : icons

  return {
    items: filtered.slice(0, MAX_DISPLAY).map(name => ({
      name: `${prefix}${name}`,
      label: name
    })),
    total: filtered.length
  }
})

function selectIcon(iconName: string) {
  emit('update:modelValue', iconName)
  open.value = false
}

function clearIcon() {
  emit('update:modelValue', '')
}

// 根据当前值自动切换到对应集合
function detectCollection(value: string): 'lucide' | 'simple-icons' {
  if (value.startsWith('i-simple-icons-')) return 'simple-icons'
  return 'lucide'
}

watch(open, (isOpen) => {
  if (isOpen) {
    search.value = ''
    if (props.modelValue) {
      activeCollection.value = detectCollection(props.modelValue)
    }
    loadCollection(activeCollection.value)
  }
})

watch(activeCollection, (key) => {
  loadCollection(key)
})
</script>

<template>
  <UPopover v-model:open="open" :content="{ side: 'bottom', align: 'start', sideOffset: 4 }">
    <!-- 触发器：模拟表单输入控件外观 -->
    <button
      type="button"
      class="flex items-center gap-2 w-full rounded-[calc(var(--ui-radius)*1.5)] border border-(--ui-border) bg-(--ui-bg) px-2.5 py-1.5 text-sm cursor-pointer transition-colors hover:border-(--ui-border-accented) focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-(--ui-primary)"
    >
      <UIcon
        v-if="modelValue"
        :name="modelValue"
        class="size-5 shrink-0"
      />
      <UIcon
        v-else
        name="i-lucide-smile-plus"
        class="size-5 shrink-0 text-(--ui-text-dimmed)"
      />
      <span
        class="flex-1 truncate text-start"
        :class="modelValue ? '' : 'text-(--ui-text-muted)'"
      >
        {{ modelValue || placeholder }}
      </span>
      <!-- 清除按钮 -->
      <span
        v-if="modelValue"
        role="button"
        tabindex="-1"
        class="shrink-0 text-(--ui-text-muted) hover:text-(--ui-text)"
        @click.stop.prevent="clearIcon"
        @mousedown.stop.prevent
      >
        <UIcon name="i-lucide-x" class="size-4" />
      </span>
      <UIcon
        v-else
        name="i-lucide-chevron-down"
        class="size-4 shrink-0 text-(--ui-text-dimmed)"
      />
    </button>

    <!-- 弹出面板 -->
    <template #content>
      <div class="w-80 p-3 space-y-3">
        <!-- 集合切换 -->
        <div class="flex gap-1">
          <button
            v-for="col in collectionOptions"
            :key="col.key"
            type="button"
            class="px-3 py-1 text-xs rounded-full transition-colors"
            :class="
              activeCollection === col.key
                ? 'bg-(--ui-primary) text-(--ui-bg)'
                : 'bg-(--ui-bg-elevated) text-(--ui-text-muted) hover:text-(--ui-text)'
            "
            @click="activeCollection = col.key"
          >
            {{ col.label }}
          </button>
        </div>

        <!-- 搜索输入 -->
        <UInput
          v-model="search"
          placeholder="搜索图标名称..."
          icon="i-lucide-search"
          size="sm"
          autofocus
          class="w-full"
        />

        <!-- 结果统计 -->
        <p class="text-xs text-(--ui-text-dimmed)">
          <template v-if="loading">加载中...</template>
          <template v-else-if="filteredIcons.total > MAX_DISPLAY">
            显示前 {{ MAX_DISPLAY }} 个，共 {{ filteredIcons.total }} 个结果
          </template>
          <template v-else>
            {{ filteredIcons.total }} 个图标
          </template>
        </p>

        <!-- 图标网格 -->
        <div class="max-h-64 overflow-y-auto -mx-1">
          <!-- 加载状态 -->
          <div v-if="loading" class="flex justify-center py-8">
            <UIcon name="i-lucide-loader-2" class="size-5 animate-spin text-(--ui-text-muted)" />
          </div>
          <!-- 无结果 -->
          <div
            v-else-if="filteredIcons.items.length === 0"
            class="flex flex-col items-center py-8 text-(--ui-text-muted)"
          >
            <UIcon name="i-lucide-search-x" class="size-8 mb-2" />
            <span class="text-sm">未找到匹配的图标</span>
          </div>
          <!-- 图标列表 -->
          <div v-else class="grid grid-cols-8 gap-0.5 px-1">
            <UTooltip
              v-for="icon in filteredIcons.items"
              :key="icon.name"
              :text="icon.label"
            >
              <button
                type="button"
                class="flex items-center justify-center p-2 rounded-md transition-colors"
                :class="
                  modelValue === icon.name
                    ? 'bg-(--ui-primary)/10 text-(--ui-primary) ring-1 ring-(--ui-primary)'
                    : 'hover:bg-(--ui-bg-elevated) text-(--ui-text)'
                "
                @click="selectIcon(icon.name)"
              >
                <UIcon :name="icon.name" class="size-5" />
              </button>
            </UTooltip>
          </div>
        </div>
      </div>
    </template>
  </UPopover>
</template>
