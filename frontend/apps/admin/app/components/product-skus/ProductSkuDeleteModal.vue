<script setup lang="ts">
/**
 * 删除 SKU 确认弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  productId: number | string
  sku: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud(
  `/api/v1/admin/products/${props.productId}/skus`
)

async function handleConfirm() {
  const id = props.sku.id
  if (!id) return
  const ok = await remove(String(id))
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    :title="`删除 SKU「${sku.name ?? ''}」`"
    :message="`确定要删除 SKU（${sku.skuCode ?? ''}）吗？删除后不可恢复。`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>
