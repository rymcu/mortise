<script setup lang="ts">
/**
 * 删除产品分类弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  category: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud(
  '/api/v1/admin/product-categories'
)

async function handleConfirm() {
  const ok = await remove(props.category.id as number)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除分类"
    :message="`确定要删除分类「${category.name}」吗？删除前请确认该分类下无产品及子分类。`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>
