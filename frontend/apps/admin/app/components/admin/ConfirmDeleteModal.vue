<script setup lang="ts">
/**
 * 通用确认删除弹窗
 * 通过 v-model:open 控制打开/关闭
 */
const props = withDefaults(
  defineProps<{
    open: boolean
    title?: string
    message?: string
    loading?: boolean
  }>(),
  {
    title: '确认删除',
    message: '确定要删除此记录吗？删除后无法恢复。',
    loading: false
  }
)

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'confirm'): void
}>()

const isOpen = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v)
})
</script>

<template>
  <UModal v-model:open="isOpen" :title="title">
    <template #body>
      <p class="text-muted text-sm">
        {{ message }}
      </p>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="error" :loading="loading" @click="emit('confirm')">
          确认删除
        </UButton>
        <UButton
          color="neutral"
          variant="subtle"
          :disabled="loading"
          @click="isOpen = false"
        >
          取消
        </UButton>
      </div>
    </template>
  </UModal>
</template>
