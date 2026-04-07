<script setup lang="ts">
import {
  createVoiceProfileFormState,
  toVoiceProfilePayload,
} from '~/types/voice'
import type {
  VoiceModelInfo,
  VoiceProfileFormState,
  VoiceSelectOption,
} from '~/types/voice'

const open = defineModel<boolean>('open', { default: false })

defineProps<{
  providerOptions: VoiceSelectOption[]
  models: VoiceModelInfo[]
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { create, loading, errorMessage } = useAdminCrud('/api/v1/admin/voice/profiles')

const formRef = ref()
const formData = ref<VoiceProfileFormState>(createVoiceProfileFormState())

watch(open, value => {
  if (value) {
    formData.value = createVoiceProfileFormState()
  }
})

async function handleConfirm() {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }

  const ok = await create(toVoiceProfilePayload(formData.value))
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="新增语音 Profile" :ui="{ content: 'sm:max-w-5xl' }">
    <template #body>
      <UAlert
        v-if="errorMessage"
        color="error"
        :title="errorMessage"
        class="mb-4"
      />
      <VoicesProfileForm
        ref="formRef"
        :provider-options="providerOptions"
        :models="models"
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