<script setup lang="ts">
import * as z from 'zod'
import {
  createVoiceProviderFormState,
  isOptionalJsonObjectString,
  voiceProviderTypeOptions,
} from '~/types/voice'
import type { VoiceProviderFormState, VoiceProviderInfo } from '~/types/voice'

const props = withDefaults(
  defineProps<{
    data?: VoiceProviderInfo | null
  }>(),
  {
    data: null,
  }
)

const emit = defineEmits<{
  (e: 'change', data: VoiceProviderFormState): void
}>()

const schema = z.object({
  name: z.string().min(1, '请输入提供商名称'),
  code: z.string().min(1, '请输入提供商编码'),
  providerType: z.string().min(1, '请选择提供商类型'),
  status: z.coerce.number().default(1),
  sortNo: z.coerce.number().default(0),
  defaultConfig: z.string().refine(isOptionalJsonObjectString, '默认配置必须是 JSON 对象'),
  remark: z.string().optional(),
})

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

const state = reactive<VoiceProviderFormState>(createVoiceProviderFormState(props.data ?? undefined))
const formRef = ref()

watch(
  () => props.data,
  value => {
    Object.assign(state, createVoiceProviderFormState(value ?? undefined))
  },
  { immediate: true }
)

watch(
  state,
  value => emit('change', { ...value }),
  { deep: true, immediate: true }
)

async function validate(): Promise<boolean> {
  try {
    if (formRef.value?.validate) {
      await formRef.value.validate()
    }
    return schema.safeParse(state).success
  } catch {
    return false
  }
}

defineExpose({ validate, state })
</script>

<template>
  <UForm ref="formRef" :schema="schema" :state="state" class="space-y-4">
    <div class="grid grid-cols-2 gap-4">
      <UFormField label="提供商名称" name="name" required>
        <UInput
          v-model="state.name"
          placeholder="请输入提供商名称"
          class="w-full"
        />
      </UFormField>

      <UFormField label="提供商编码" name="code" required>
        <UInput
          v-model="state.code"
          placeholder="请输入提供商编码"
          class="w-full"
        />
      </UFormField>

      <UFormField label="提供商类型" name="providerType" required>
        <USelect
          v-model="state.providerType"
          :items="voiceProviderTypeOptions"
          value-key="value"
          label-key="label"
          placeholder="请选择提供商类型"
          class="w-full"
        />
      </UFormField>

      <UFormField label="排序号" name="sortNo">
        <UInput
          v-model.number="state.sortNo"
          type="number"
          placeholder="数字越小越靠前"
          class="w-full"
        />
      </UFormField>
    </div>

    <UFormField label="默认配置 JSON" name="defaultConfig">
      <UTextarea
        v-model="state.defaultConfig"
        placeholder="请输入默认配置 JSON"
        :rows="5"
        class="w-full font-mono"
      />
    </UFormField>

    <UFormField label="备注" name="remark">
      <UTextarea
        v-model="state.remark"
        placeholder="请输入备注"
        :rows="3"
        class="w-full"
      />
    </UFormField>

    <UFormField label="状态" name="status">
      <URadioGroup
        v-model="state.status"
        :items="statusOptions"
        orientation="horizontal"
      />
    </UFormField>
  </UForm>
</template>