<script setup lang="ts">
/**
 * 删除角色弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  role: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud('/api/v1/admin/roles')

async function handleConfirm() {
  const ok = await remove(props.role.id as number)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除角色"
    :message="`确定要删除角色「${role.label}」吗？`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>
