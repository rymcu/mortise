<script setup lang="ts">
import type {ChannelConfigSaveRequest, ChannelConfigVO} from "~/types";

/**
 * 渠道配置卡片组件
 * 展示单个通知渠道的配置表单，支持启用/禁用切换和保存操作
 */
const props = defineProps<{
  channel: ChannelConfigVO
  saving: boolean
}>()

const emit = defineEmits<{ save: [request: ChannelConfigSaveRequest] }>()

// 本地可编辑副本，避免直接修改 props
const localEnabled = ref(props.channel.enabled)
const localValues = reactive<Record<string, string>>({ ...props.channel.values })

// 当 props.channel 变更时（切换 Tab），同步重置本地状态
watch(
  () => props.channel,
  (newChannel) => {
    localEnabled.value = newChannel.enabled
    Object.keys(localValues).forEach(k => delete localValues[k])
    Object.assign(localValues, newChannel.values)
  }
)

function handleSave() {
  // 过滤掉未修改的密码字段（值为 "***" 视为未修改，不传该 key）
  const valuesToSave = Object.fromEntries(
    Object.entries(localValues).filter(([key, val]) => {
      const fieldDef = props.channel.schema.find(f => f.key === key)
      return !(fieldDef?.type === 'PASSWORD' && val === '***')
    })
  )
  emit('save', { enabled: localEnabled.value, values: valuesToSave })
}
</script>

<template>
  <UCard>
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-semibold text-base">{{ channel.label }}</span>
        <div class="flex items-center gap-2">
          <span class="text-muted text-sm">{{ localEnabled ? '已启用' : '已禁用' }}</span>
          <USwitch v-model="localEnabled" />
        </div>
      </div>
    </template>

    <div class="space-y-4">
      <NotificationChannelsChannelDynamicField
        v-for="field in channel.schema"
        :key="field.key"
        :field="field"
        :model-value="localValues[field.key]"
        @update:model-value="localValues[field.key] = $event"
      />
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
