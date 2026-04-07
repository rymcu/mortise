<script setup lang="ts">
import { toVoiceModelPayload } from '~/types/voice'
import type {
  VoiceModelFormState,
  VoiceModelInfo,
  VoiceSelectOption,
} from '~/types/voice'

const open = defineModel<boolean>('open', { default: false })

const props = withDefaults(
  defineProps<{
    model?: VoiceModelInfo | null
    providerOptions: VoiceSelectOption[]
  }>(),
  {
    model: null,
  }
)

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { update, loading, errorMessage } = useAdminCrud('/api/v1/admin/voice/models')

const formRef = ref()
const formData = ref<VoiceModelFormState | null>(null)

async function handleConfirm() {
  if (!props.model?.id) {
    return
  }

  const valid = await formRef.value?.validate()
  if (!valid || !formData.value) {
    return
  }

  const ok = await update(props.model.id, toVoiceModelPayload(formData.value))
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="编辑语音模型" :ui="{ content: 'sm:max-w-4xl' }">
    <template #body>
      <UAlert
        v-if="errorMessage"
        color="error"
        :title="errorMessage"
        class="mb-4"
      />
      <VoicesModelForm
        ref="formRef"
        :data="model"
        :provider-options="providerOptions"
        @change="formData = $event"
      />
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="保存" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>