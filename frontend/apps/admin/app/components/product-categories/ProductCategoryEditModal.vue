<script setup lang="ts">
/**
 * 编辑产品分类弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  category: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { update, loading, errorMessage } = useAdminCrud(
  '/api/v1/admin/product-categories'
)

const formRef = ref()
const formData = ref<Record<string, unknown>>({ ...props.category })

watch(
  () => props.category,
  (v) => {
    formData.value = { ...v }
  }
)

async function handleConfirm() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  const ok = await update(props.category.id as number, formData.value)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="编辑分类">
    <template #body>
      <UAlert
        v-if="errorMessage"
        color="error"
        :title="errorMessage"
        class="mb-4"
      />
      <ProductCategoriesProductCategoryForm
        ref="formRef"
        :data="category"
        @change="formData = $event"
      />
    </template>
    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="确定" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>
