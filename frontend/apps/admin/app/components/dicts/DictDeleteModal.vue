<script setup lang="ts">
/**
 * 删除字典弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  dict: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud('/api/v1/admin/dictionaries')

async function handleConfirm() {
  const ok = await remove(props.dict.id as number)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除字典"
    :message="`确定要删除字典「${dict.label}」吗？`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>
