<script setup lang="ts">
/**
 * 删除产品弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  product: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud('/api/v1/admin/products')

async function handleConfirm() {
  const ok = await remove(props.product.id as number)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除产品"
    :message="`确定要删除产品「${product.title}」吗？此操作不可恢复。`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>
