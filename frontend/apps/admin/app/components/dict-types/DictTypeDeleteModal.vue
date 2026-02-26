<script setup lang="ts">
/**
 * 删除字典类型弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  dictType: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud(
  '/api/v1/admin/dictionary-types'
)

async function handleConfirm() {
  const ok = await remove(props.dictType.id as number)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除字典类型"
    :message="`确定要删除字典类型「${dictType.label}」吗？`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>
