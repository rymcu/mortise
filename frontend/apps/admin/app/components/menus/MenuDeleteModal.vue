<script setup lang="ts">
/**
 * 删除菜单弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  menu: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud('/api/v1/admin/menus')

async function handleConfirm() {
  const ok = await remove(props.menu.id as number)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除菜单"
    :message="`确定要删除菜单「${menu.label}」吗？`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>
