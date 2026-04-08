<script setup lang="ts">
import type { AgentModelItem } from '../../types/agent'
import { DEFAULT_PROVIDER_ICON, PROVIDER_ICON_MAP } from '../../types/agent'

interface ProviderGroup {
  providerCode: string
  providerName: string
  models: readonly AgentModelItem[]
}

const props = defineProps<{
  providers: readonly ProviderGroup[]
  loading?: boolean
}>()

const modelValue = defineModel<string>({ required: true })

const open = ref(false)
const search = ref('')

/** 当前选中模型的显示信息 */
const selectedDisplay = computed(() => {
  for (const p of props.providers) {
    for (const m of p.models) {
      const val = `${p.providerCode}:${m.modelName}`
      if (val === modelValue.value) {
        return {
          providerName: p.providerName,
          displayName: m.displayName,
          icon: PROVIDER_ICON_MAP[p.providerCode] ?? DEFAULT_PROVIDER_ICON,
        }
      }
    }
  }
  return null
})

/** 按搜索词过滤提供商和模型 */
const filteredProviders = computed(() => {
  const q = search.value.toLowerCase().trim()
  if (!q) return props.providers

  return props.providers
    .map(p => ({
      ...p,
      models: p.models.filter(
        m =>
          m.displayName.toLowerCase().includes(q)
          || m.modelName.toLowerCase().includes(q)
          || p.providerName.toLowerCase().includes(q),
      ),
    }))
    .filter(p => p.models.length > 0)
})

function selectModel(providerCode: string, modelName: string) {
  modelValue.value = `${providerCode}:${modelName}`
  open.value = false
  search.value = ''
}

function isSelected(providerCode: string, modelName: string) {
  return modelValue.value === `${providerCode}:${modelName}`
}
</script>

<template>
  <UPopover v-model:open="open" :content="{ side: 'top', align: 'start' }">
    <!-- 触发按钮 -->
    <UButton
      :icon="selectedDisplay?.icon ?? 'i-lucide-cpu'"
      :label="selectedDisplay?.displayName ?? (loading ? '加载中...' : '选择模型')"
      :loading="loading"
      size="sm"
      color="neutral"
      variant="ghost"
      trailing-icon="i-lucide-chevron-down"
      :ui="{
        trailingIcon: open ? 'rotate-180 transition-transform duration-200' : 'transition-transform duration-200',
      }"
    />

    <!-- 弹出面板 -->
    <template #content>
      <div class="flex w-72 flex-col sm:w-80">
        <!-- 搜索框 -->
        <div class="border-b border-default p-2">
          <UInput
            v-model="search"
            icon="i-lucide-search"
            placeholder="搜索模型..."
            size="sm"
            variant="ghost"
            autofocus
            class="w-full"
          />
        </div>

        <!-- 模型列表 -->
        <div class="max-h-72 overflow-y-auto p-1">
          <template v-if="filteredProviders.length === 0">
            <div class="px-3 py-6 text-center text-sm text-muted">
              未找到匹配的模型
            </div>
          </template>

          <template v-for="provider in filteredProviders" :key="provider.providerCode">
            <!-- 提供商分组标题 -->
            <div class="flex items-center gap-2 px-2 pb-1 pt-2 text-xs font-medium text-muted">
              <UIcon
                :name="PROVIDER_ICON_MAP[provider.providerCode] ?? DEFAULT_PROVIDER_ICON"
                class="size-3.5 shrink-0"
              />
              {{ provider.providerName }}
            </div>

            <!-- 模型列表项 -->
            <button
              v-for="model in provider.models"
              :key="model.modelName"
              class="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left text-sm transition-colors hover:bg-elevated"
              :class="isSelected(provider.providerCode, model.modelName) ? 'bg-elevated text-highlighted' : 'text-default'"
              @click="selectModel(provider.providerCode, model.modelName)"
            >
              <UIcon
                :name="PROVIDER_ICON_MAP[provider.providerCode] ?? DEFAULT_PROVIDER_ICON"
                class="size-4 shrink-0"
              />
              <span class="flex-1 truncate">{{ model.displayName }}</span>
              <UIcon
                v-if="isSelected(provider.providerCode, model.modelName)"
                name="i-lucide-check"
                class="size-4 shrink-0 text-primary"
              />
            </button>
          </template>
        </div>
      </div>
    </template>
  </UPopover>
</template>
