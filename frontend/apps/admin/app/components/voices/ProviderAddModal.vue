<script setup lang="ts">
import {
  createVoiceProviderFormState,
  toVoiceProviderPayload,
} from '~/types/voice'
import type { VoiceProviderFormState } from '~/types/voice'

const open = defineModel<boolean>('open', { default: false })

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { create, loading, errorMessage } = useAdminCrud('/api/v1/admin/voice/providers')

const formRef = ref()
const formData = ref<VoiceProviderFormState>(createVoiceProviderFormState())

watch(open, value => {
  if (value) {
    formData.value = createVoiceProviderFormState()
  }
})

async function handleConfirm() {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }

  const ok = await create(toVoiceProviderPayload(formData.value))
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="新增语音提供商" :ui="{ content: 'sm:max-w-3xl' }">
    <template #body>
      <UAlert
        v-if="errorMessage"
        color="error"
        :title="errorMessage"
        class="mb-4"
      />
      <VoicesProviderForm ref="formRef" @change="formData = $event" />
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="确定" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>