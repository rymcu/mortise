<script setup lang="ts">
import * as z from 'zod'
import {
  buildVoiceModelOptions,
  createVoiceProfileFormState,
  isOptionalJsonObjectString,
} from '~/types/voice'
import type {
  VoiceModelInfo,
  VoiceProfileFormState,
  VoiceProfileInfo,
  VoiceSelectOption,
} from '~/types/voice'

const props = withDefaults(
  defineProps<{
    data?: VoiceProfileInfo | null
    providerOptions: VoiceSelectOption[]
    models: VoiceModelInfo[]
  }>(),
  {
    data: null,
  }
)

const emit = defineEmits<{
  (e: 'change', data: VoiceProfileFormState): void
}>()

const schema = z.object({
  name: z.string().min(1, '请输入 Profile 名称'),
  code: z.string().min(1, '请输入 Profile 编码'),
  language: z.string().optional(),
  asrProviderId: z.string().optional(),
  asrModelId: z.string().optional(),
  vadProviderId: z.string().optional(),
  vadModelId: z.string().optional(),
  ttsProviderId: z.string().optional(),
  ttsModelId: z.string().optional(),
  defaultParams: z.string().refine(isOptionalJsonObjectString, '默认参数必须是 JSON 对象'),
  status: z.coerce.number().default(1),
  sortNo: z.coerce.number().default(0),
  remark: z.string().optional(),
}).superRefine((value, ctx) => {
  const slots = [
    ['ASR', value.asrProviderId, value.asrModelId],
    ['VAD', value.vadProviderId, value.vadModelId],
    ['TTS', value.ttsProviderId, value.ttsModelId],
  ]

  for (const [label, providerId, modelId] of slots) {
    if ((providerId && !modelId) || (!providerId && modelId)) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: `${label} Provider 与 Model 必须成对选择`,
      })
    }
  }
})

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

const state = reactive<VoiceProfileFormState>(createVoiceProfileFormState(props.data ?? undefined))
const formRef = ref()

const asrModelOptions = computed(() => {
  return buildVoiceModelOptions(props.models, 'ASR', state.asrProviderId || undefined)
})

const vadModelOptions = computed(() => {
  return buildVoiceModelOptions(props.models, 'VAD', state.vadProviderId || undefined)
})

const ttsModelOptions = computed(() => {
  return buildVoiceModelOptions(props.models, 'TTS', state.ttsProviderId || undefined)
})

function ensureOptionValue(value: string, options: VoiceSelectOption[]): string {
  return options.some(option => option.value === value) ? value : ''
}

watch(
  () => props.data,
  value => {
    Object.assign(state, createVoiceProfileFormState(value ?? undefined))
  },
  { immediate: true }
)

watch(
  () => state.asrProviderId,
  () => {
    state.asrModelId = ensureOptionValue(state.asrModelId, asrModelOptions.value)
  }
)

watch(
  () => state.vadProviderId,
  () => {
    state.vadModelId = ensureOptionValue(state.vadModelId, vadModelOptions.value)
  }
)

watch(
  () => state.ttsProviderId,
  () => {
    state.ttsModelId = ensureOptionValue(state.ttsModelId, ttsModelOptions.value)
  }
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
      <UFormField label="Profile 名称" name="name" required>
        <UInput v-model="state.name" placeholder="请输入 Profile 名称" class="w-full" />
      </UFormField>

      <UFormField label="Profile 编码" name="code" required>
        <UInput v-model="state.code" placeholder="请输入 Profile 编码" class="w-full" />
      </UFormField>

      <UFormField label="语言" name="language">
        <UInput v-model="state.language" placeholder="如：zh-CN" class="w-full" />
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

    <div class="grid grid-cols-2 gap-4 rounded-lg border border-default p-4">
      <div class="col-span-2 text-sm font-medium">ASR 槽位</div>
      <UFormField label="ASR Provider" name="asrProviderId">
        <USelect
          :model-value="state.asrProviderId || undefined"
          :items="providerOptions"
          value-key="value"
          label-key="label"
          placeholder="选择 ASR Provider"
          class="w-full"
          @update:model-value="state.asrProviderId = String($event ?? '')"
        />
      </UFormField>
      <UFormField label="ASR Model" name="asrModelId">
        <USelect
          :model-value="state.asrModelId || undefined"
          :items="asrModelOptions"
          value-key="value"
          label-key="label"
          placeholder="选择 ASR Model"
          class="w-full"
          @update:model-value="state.asrModelId = String($event ?? '')"
        />
      </UFormField>
    </div>

    <div class="grid grid-cols-2 gap-4 rounded-lg border border-default p-4">
      <div class="col-span-2 text-sm font-medium">VAD 槽位</div>
      <UFormField label="VAD Provider" name="vadProviderId">
        <USelect
          :model-value="state.vadProviderId || undefined"
          :items="providerOptions"
          value-key="value"
          label-key="label"
          placeholder="选择 VAD Provider"
          class="w-full"
          @update:model-value="state.vadProviderId = String($event ?? '')"
        />
      </UFormField>
      <UFormField label="VAD Model" name="vadModelId">
        <USelect
          :model-value="state.vadModelId || undefined"
          :items="vadModelOptions"
          value-key="value"
          label-key="label"
          placeholder="选择 VAD Model"
          class="w-full"
          @update:model-value="state.vadModelId = String($event ?? '')"
        />
      </UFormField>
    </div>

    <div class="grid grid-cols-2 gap-4 rounded-lg border border-default p-4">
      <div class="col-span-2 text-sm font-medium">TTS 槽位</div>
      <UFormField label="TTS Provider" name="ttsProviderId">
        <USelect
          :model-value="state.ttsProviderId || undefined"
          :items="providerOptions"
          value-key="value"
          label-key="label"
          placeholder="选择 TTS Provider"
          class="w-full"
          @update:model-value="state.ttsProviderId = String($event ?? '')"
        />
      </UFormField>
      <UFormField label="TTS Model" name="ttsModelId">
        <USelect
          :model-value="state.ttsModelId || undefined"
          :items="ttsModelOptions"
          value-key="value"
          label-key="label"
          placeholder="选择 TTS Model"
          class="w-full"
          @update:model-value="state.ttsModelId = String($event ?? '')"
        />
      </UFormField>
    </div>

    <UFormField label="默认参数 JSON" name="defaultParams">
      <UTextarea
        v-model="state.defaultParams"
        placeholder="请输入默认参数 JSON"
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