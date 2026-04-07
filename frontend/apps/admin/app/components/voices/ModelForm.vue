<script setup lang="ts">
import * as z from 'zod'
import {
  createVoiceModelFormState,
  voiceCapabilityOptions,
  voiceModelTypeOptions,
} from '~/types/voice'
import type {
  VoiceModelFormState,
  VoiceModelInfo,
  VoiceSelectOption,
} from '~/types/voice'

const props = withDefaults(
  defineProps<{
    data?: VoiceModelInfo | null
    providerOptions: VoiceSelectOption[]
  }>(),
  {
    data: null,
  }
)

const emit = defineEmits<{
  (e: 'change', data: VoiceModelFormState): void
}>()

const schema = z.object({
  providerId: z.string().min(1, '请选择所属 Provider'),
  name: z.string().min(1, '请输入模型名称'),
  code: z.string().min(1, '请输入模型编码'),
  capability: z.string().min(1, '请选择能力类型'),
  modelType: z.string().min(1, '请选择模型类型'),
  runtimeName: z.string().optional(),
  version: z.string().optional(),
  language: z.string().optional(),
  concurrencyLimit: z.number().nullable().optional(),
  defaultModel: z.boolean().default(false),
  status: z.coerce.number().default(1),
  remark: z.string().optional(),
})

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

const state = reactive<VoiceModelFormState>(createVoiceModelFormState(props.data ?? undefined))
const formRef = ref()

watch(
  () => props.data,
  value => {
    Object.assign(state, createVoiceModelFormState(value ?? undefined))
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
      <UFormField label="所属 Provider" name="providerId" required>
        <USelect
          v-model="state.providerId"
          :items="providerOptions"
          value-key="value"
          label-key="label"
          placeholder="请选择所属 Provider"
          class="w-full"
        />
      </UFormField>

      <UFormField label="模型名称" name="name" required>
        <UInput v-model="state.name" placeholder="请输入模型名称" class="w-full" />
      </UFormField>

      <UFormField label="模型编码" name="code" required>
        <UInput v-model="state.code" placeholder="请输入模型编码" class="w-full" />
      </UFormField>

      <UFormField label="能力类型" name="capability" required>
        <USelect
          v-model="state.capability"
          :items="voiceCapabilityOptions"
          value-key="value"
          label-key="label"
          placeholder="请选择能力类型"
          class="w-full"
        />
      </UFormField>

      <UFormField label="模型类型" name="modelType" required>
        <USelect
          v-model="state.modelType"
          :items="voiceModelTypeOptions"
          value-key="value"
          label-key="label"
          placeholder="请选择模型类型"
          class="w-full"
        />
      </UFormField>

      <UFormField label="运行时名称" name="runtimeName">
        <UInput
          v-model="state.runtimeName"
          placeholder="请输入运行时名称"
          class="w-full"
        />
      </UFormField>

      <UFormField label="版本号" name="version">
        <UInput v-model="state.version" placeholder="如：v1" class="w-full" />
      </UFormField>

      <UFormField label="语言" name="language">
        <UInput v-model="state.language" placeholder="如：zh-CN" class="w-full" />
      </UFormField>

      <UFormField label="并发上限" name="concurrencyLimit">
        <UInputNumber
          :model-value="state.concurrencyLimit ?? undefined"
          placeholder="为空表示不限"
          class="w-full"
          @update:model-value="state.concurrencyLimit = $event === undefined ? null : Number($event)"
        />
      </UFormField>

      <UFormField label="默认模型" name="defaultModel">
        <div class="pt-2">
          <UCheckbox v-model="state.defaultModel" label="设为默认模型" />
        </div>
      </UFormField>
    </div>

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