<script setup lang="ts">
/**
 * 产品状态更新弹窗
 * 状态机：0-草稿 → 1-上架 → 2-下架 → 3-停产
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  product: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { patchAction, loading, errorMessage } = useAdminCrud(
  '/api/v1/admin/products'
)

const statusOptions = [
  { value: 0, label: '草稿', color: 'neutral' },
  { value: 1, label: '上架', color: 'success' },
  { value: 2, label: '下架', color: 'warning' },
  { value: 3, label: '停产', color: 'error' }
]

const selectedStatus = ref<number>(
  typeof props.product.status === 'number' ? props.product.status : 0
)

watch(
  () => props.product.status,
  (v) => {
    selectedStatus.value = typeof v === 'number' ? v : 0
  }
)

async function handleConfirm() {
  const ok = await patchAction(
    `${props.product.id}/status`,
    { status: selectedStatus.value }
  )
  if (ok !== null) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="更新产品状态">
    <template #body>
      <UAlert
        v-if="errorMessage"
        color="error"
        :title="errorMessage"
        class="mb-4"
      />
      <p class="text-muted mb-4 text-sm">
        当前产品：<span class="text-highlighted font-medium">{{
          product.title
        }}</span>
      </p>
      <div class="grid grid-cols-2 gap-3">
        <label
          v-for="opt in statusOptions"
          :key="opt.value"
          class="border-default flex cursor-pointer items-center gap-3 rounded-lg border p-3 transition-colors"
          :class="
            selectedStatus === opt.value
              ? 'border-primary bg-primary/5'
              : 'hover:bg-elevated/50'
          "
        >
          <input
            v-model.number="selectedStatus"
            type="radio"
            :value="opt.value"
            class="accent-primary"
          />
          <div>
            <UBadge :color="opt.color as any" variant="subtle" size="sm">
              {{ opt.label }}
            </UBadge>
          </div>
        </label>
      </div>
    </template>
    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="确定" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>
