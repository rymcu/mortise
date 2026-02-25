<script setup lang="ts">
/**
 * 编辑菜单弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  menu: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { update, loading, errorMessage } = useAdminCrud('/api/v1/admin/menus')

const formRef = ref()
const formData = ref<Record<string, unknown>>({ ...props.menu })

watch(() => props.menu, (v) => { formData.value = { ...v } })

async function handleConfirm() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  const ok = await update(props.menu.id as number, formData.value)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="编辑菜单">
    <template #body>
      <UAlert v-if="errorMessage" color="error" :title="errorMessage" class="mb-4" />
      <MenusMenuForm ref="formRef" :data="menu" @change="formData = $event" />
    </template>
    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="确定" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>
