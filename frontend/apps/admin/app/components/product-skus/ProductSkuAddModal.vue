<script setup lang="ts">
/**
 * 新增 SKU 弹窗
 * 新增 SKU 骨架后，紧接着调用 SkuPricing API 保存定价信息。
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  productId: number | string
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { $api } = useNuxtApp()
const { create: createSku, loading: skuLoading, errorMessage: skuError } = useAdminCrud(
  `/api/v1/admin/products/${props.productId}/skus`
)

const loading = ref(false)
const errorMessage = ref('')

interface SkuFormData {
  skuCode: string
  name: string
  description: string
  status: string
  isDefault: boolean
  attributes: Record<string, unknown> | undefined
  originalPrice: number | undefined
  currentPrice: number | undefined
  currency: string
  inventoryType: string
}

const defaultForm = (): SkuFormData => ({
  skuCode: '',
  name: '',
  description: '',
  status: 'active',
  isDefault: false,
  attributes: undefined,
  originalPrice: undefined,
  currentPrice: undefined,
  currency: 'CNY',
  inventoryType: 'unlimited'
})

const form = ref(defaultForm())

watch(open, (v) => {
  if (v) {
    form.value = defaultForm()
    errorMessage.value = ''
  }
})

async function handleConfirm() {
  loading.value = true
  errorMessage.value = ''
  try {
    // 提取 SKU 骨架字段
    const { originalPrice, currentPrice, currency, inventoryType, ...skuFields } = form.value
    const createdSku = await createSku<{ id: string }>(skuFields as Record<string, unknown>)
    if (!createdSku) {
      errorMessage.value = skuError.value || '创建SKU失败'
      return
    }
    // 提交定价信息
    const pricingRes = await $api<{ code: number; message?: string }>(
      `/api/v1/admin/commerce/skus/${createdSku.id}/pricing`,
      {
        method: 'POST',
        body: { originalPrice, currentPrice: currentPrice ?? originalPrice, currency, inventoryType }
      }
    )
    if (!pricingRes || pricingRes.code !== 200) {
      errorMessage.value = pricingRes?.message || '定价保存失败'
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
        <UButton color="primary" :loading="loading || skuLoading" @click="handleConfirm">保存</UButton>
      </div>
    </template>
  </UModal>
</template>
