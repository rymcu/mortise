<script setup lang="ts">
import {
  createVoiceModelFormState,
  toVoiceModelPayload,
} from '~/types/voice'
import type { VoiceModelFormState, VoiceSelectOption } from '~/types/voice'

const open = defineModel<boolean>('open', { default: false })

defineProps<{
  providerOptions: VoiceSelectOption[]
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { create, loading, errorMessage } = useAdminCrud('/api/v1/admin/voice/models')

const formRef = ref()
const formData = ref<VoiceModelFormState>(createVoiceModelFormState())

watch(open, value => {
  if (value) {
    formData.value = createVoiceModelFormState()
  }
})

async function handleConfirm() {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }

  const ok = await create(toVoiceModelPayload(formData.value))
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="新增语音模型" :ui="{ content: 'sm:max-w-4xl' }">
    <template #body>
      <UAlert
        v-if="errorMessage"
        color="error"
        :title="errorMessage"
        class="mb-4"
      />
      <VoicesModelForm
        ref="formRef"
        :provider-options="providerOptions"
        @change="formData = $event"
      />
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="确定" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>