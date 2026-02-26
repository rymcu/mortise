<script setup lang="ts">
/**
 * 删除微信账号弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  account: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { remove, loading, errorMessage } = useAdminCrud(
  '/api/v1/admin/wechat/accounts'
)

async function handleConfirm() {
  const ok = await remove(props.account.id as number)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <AdminConfirmDeleteModal
    v-model:open="open"
    title="删除微信账号"
    :message="`确定要删除微信账号「${account.accountName}」吗？`"
    :loading="loading"
    :error-message="errorMessage"
    @confirm="handleConfirm"
  />
</template>
