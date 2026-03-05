<script setup lang="ts">
/**
 * 编辑 SKU 弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  productId: number | string
  sku: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { update, loading, errorMessage } = useAdminCrud(
  `/api/v1/admin/products/${props.productId}/skus`
)

const form = ref<Record<string, unknown>>({})

watch(
  () => props.sku,
  (v) => {
    form.value = { ...v }
  },
  { immediate: true }
)

watch(open, (v) => {
  if (v) form.value = { ...props.sku }
})

async function handleConfirm() {
  const id = props.sku.id
  if (!id) return
  const ok = await update(String(id), form.value)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="编辑 SKU">
    <template #body>
      <UAlert
        v-if="errorMessage"
        color="error"
        :title="errorMessage"
        class="mb-4"
      />
      <ProductSkusProductSkuForm v-model="form" />
    </template>
    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton color="neutral" variant="ghost" @click="open = false">取消</UButton>
        <UButton color="primary" :loading="loading" @click="handleConfirm">保存</UButton>
      </div>
    </template>
  </UModal>
</template>
