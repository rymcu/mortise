<script setup lang="ts">
/**
 * 删除用户弹窗
 */
const props = defineProps<{ user: Record<string, unknown> }>()
const emit = defineEmits<{ (e: 'success'): void }>()

const { loading, remove } = useAdminCrud('/api/v1/admin/users')
const open = ref(false)

async function onConfirm() {
  const ok = await remove(props.user.id as number)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除用户"
    :message="`确定要删除用户「${user.nickname || user.account || ''}」吗？`"
    :loading="loading"
    @confirm="onConfirm"
  >
    <template #default />
  </AdminConfirmDeleteModal>
  <UButton
    icon="i-lucide-trash-2"
    color="error"
    variant="ghost"
    size="xs"
    @click="open = true"
  >
    删除
  </UButton>
</template>
