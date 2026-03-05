<script setup lang="ts">
/**
 * 新增 SKU 弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  productId: number | string
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { create, loading, errorMessage } = useAdminCrud(
  `/api/v1/admin/products/${props.productId}/skus`
)

const defaultForm = () => ({
  skuCode: '',
  name: '',
  description: '',
  status: 'active',
  isDefault: false,
  attributes: undefined as Record<string, unknown> | undefined
})

const form = ref(defaultForm())

watch(open, (v) => {
  if (v) form.value = defaultForm()
})

async function handleConfirm() {
  const ok = await create(form.value)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="新增 SKU">
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
