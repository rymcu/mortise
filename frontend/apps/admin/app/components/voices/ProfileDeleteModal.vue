<script setup lang="ts">
import type { VoiceProfileInfo } from '~/types/voice'

const open = defineModel<boolean>('open', { default: false })

const props = withDefaults(
  defineProps<{
    profile?: VoiceProfileInfo | null
  }>(),
  {
    profile: null,
  }
)

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud('/api/v1/admin/voice/profiles')

async function handleConfirm() {
  if (!props.profile?.id) {
    return
  }

  const ok = await remove(props.profile.id)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除语音 Profile"
    :message="`确定要删除 Profile「${profile?.name || profile?.code || '-'}」吗？`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>