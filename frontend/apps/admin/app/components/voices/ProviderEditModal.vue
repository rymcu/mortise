<script setup lang="ts">
import { toVoiceProviderPayload } from '~/types/voice'
import type { VoiceProviderFormState, VoiceProviderInfo } from '~/types/voice'

const open = defineModel<boolean>('open', { default: false })

const props = withDefaults(
  defineProps<{
    provider?: VoiceProviderInfo | null
  }>(),
  {
    provider: null,
  }
)

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { update, loading, errorMessage } = useAdminCrud('/api/v1/admin/voice/providers')

const formRef = ref()
const formData = ref<VoiceProviderFormState | null>(null)

async function handleConfirm() {
  if (!props.provider?.id) {
    return
  }

  const valid = await formRef.value?.validate()
  if (!valid || !formData.value) {
    return
  }

  const ok = await update(props.provider.id, toVoiceProviderPayload(formData.value))
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="编辑语音提供商" :ui="{ content: 'sm:max-w-3xl' }">
    <template #body>
      <UAlert
        v-if="errorMessage"
        color="error"
        :title="errorMessage"
        class="mb-4"
      />
      <VoicesProviderForm
        ref="formRef"
        :data="provider"
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