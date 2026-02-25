<script setup lang="ts">
/**
 * 删除 OAuth2 客户端弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  client: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud('/api/v1/admin/oauth2/client-configs')

async function handleConfirm() {
  const ok = await remove(props.client.id as number)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除 OAuth2 客户端"
    :message="`确定要删除客户端「${client.clientName || client.registrationId}」吗？`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>
