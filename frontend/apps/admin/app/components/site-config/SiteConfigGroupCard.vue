<script setup lang="ts">
import type { SiteConfigGroupVO, SiteConfigSaveRequest } from '~/types'
import FooterColumnsEditor from '~/components/site-config/FooterColumnsEditor.vue'

/**
 * 网站配置分组卡片组件
 * 展示单个配置分组的表单，支持保存操作。
 * 结构与 NotificationChannelsChannelConfigCard 保持一致。
 */
const props = defineProps<{
  group: SiteConfigGroupVO
  saving: boolean
}>()

const emit = defineEmits<{ save: [request: SiteConfigSaveRequest] }>()

// 本地可编辑副本，避免直接修改 props
const localValues = reactive<Record<string, string>>({ ...props.group.values })

// 当 props.group 变更时（切换 Tab），同步重置本地状态
watch(
  () => props.group,
  (newGroup) => {
    for (const k of Object.keys(localValues)) {
      // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
      delete localValues[k]
    }
    Object.assign(localValues, newGroup.values)
  }
)

function isFooterColumnsField(fieldKey: string) {
  return props.group.group === 'footer' && fieldKey === 'footer.columns'
}

function handleSave() {
  emit('save', { values: { ...localValues } })
}
</script>

<template>
  <UCard>
    <template #header>
      <span class="font-semibold text-base">{{ group.label }}</span>
    </template>

    <div class="space-y-4">
      <template
        v-for="field in group.schema"
        :key="field.key"
      >
        <FooterColumnsEditor
          v-if="isFooterColumnsField(field.key)"
          :field="field"
          :model-value="localValues[field.key]"
          @update:model-value="localValues[field.key] = $event"
        />
        <DynamicFormField
          v-else
          :field="field"
          :model-value="localValues[field.key]"
          @update:model-value="localValues[field.key] = $event"
        />
      </template>
    </div>

    <template #footer>
      <div class="flex justify-end">
        <UButton :loading="saving" @click="handleSave">
          保存配置
        </UButton>
      </div>
    </template>
  </UCard>
</template>
