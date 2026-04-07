<script setup lang="ts">
import type { VoiceProviderInfo } from '~/types/voice'

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

const { remove, loading, errorMessage } = useAdminCrud('/api/v1/admin/voice/providers')

async function handleConfirm() {
  if (!props.provider?.id) {
    return
  }

  const ok = await remove(props.provider.id)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除语音提供商"
    :message="`确定要删除提供商「${provider?.name || provider?.code || '-'}」吗？`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>