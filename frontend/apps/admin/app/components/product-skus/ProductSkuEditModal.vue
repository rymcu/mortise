<script setup lang="ts">
/**
 * 编辑 SKU 弹窗
 * 同时加载并更新 SKU 骨架与 SkuPricing 定价信息。
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  productId: number | string
  sku: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { $api } = useNuxtApp()
const { update: updateSku, loading: skuLoading, errorMessage: skuError } = useAdminCrud(
  `/api/v1/admin/products/${props.productId}/skus`
)

const loading = ref(false)
const errorMessage = ref('')
const form = ref<Record<string, unknown>>({})

async function loadPricing(skuId: string | number) {
  try {
    const res = await $api<{ code: number; data?: Record<string, unknown> }>(
      `/api/v1/admin/commerce/skus/${skuId}/pricing`,
      { method: 'GET' }
    )
    if (res?.code === 200 && res.data) {
      form.value = {
        ...form.value,
        originalPrice: res.data.originalPrice,
        currentPrice: res.data.currentPrice,
        currency: res.data.currency ?? 'CNY',
        inventoryType: res.data.inventoryType ?? 'unlimited'
      }
    }
  } catch {
    // 定价尚未创建时忽略错误
  }
}

watch(
  () => props.sku,
  (v) => { form.value = { ...v } },
  { immediate: true }
)

watch(open, async (v) => {
  if (v) {
    form.value = { ...props.sku }
    errorMessage.value = ''
    if (props.sku.id) await loadPricing(props.sku.id as string)
  }
})

async function handleConfirm() {
  const id = props.sku.id
  if (!id) return
  loading.value = true
  errorMessage.value = ''
  try {
    const { originalPrice, currentPrice, currency, inventoryType, ...skuFields } = form.value
    const ok = await updateSku(String(id), skuFields)
    if (!ok) {
      errorMessage.value = skuError.value || '更新SKU失败'
      return
    }
    const pricingRes = await $api<{ code: number; message?: string }>(
      `/api/v1/admin/commerce/skus/${id}/pricing`,
      {
        method: 'PUT',
        body: { originalPrice, currentPrice: currentPrice ?? originalPrice, currency, inventoryType }
      }
    )
    if (!pricingRes || pricingRes.code !== 200) {
      errorMessage.value = pricingRes?.message || '定价更新失败'
      return
    }
    open.value = false
    emit('success')
  } finally {
    loading.value = false
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
        <UButton color="primary" :loading="loading || skuLoading" @click="handleConfirm">保存</UButton>
      </div>
    </template>
  </UModal>
</template>
