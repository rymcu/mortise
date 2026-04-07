<script setup lang="ts">
import type { VoiceModelInfo } from '~/types/voice'

const open = defineModel<boolean>('open', { default: false })

const props = withDefaults(
  defineProps<{
    model?: VoiceModelInfo | null
  }>(),
  {
    model: null,
  }
)

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud('/api/v1/admin/voice/models')

async function handleConfirm() {
  if (!props.model?.id) {
    return
  }

  const ok = await remove(props.model.id)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除语音模型"
    :message="`确定要删除模型「${model?.name || model?.code || '-'}」吗？`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>