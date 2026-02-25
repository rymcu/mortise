<script setup lang="ts">
/**
 * 编辑微信账号弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  account: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { update, loading, errorMessage } = useAdminCrud('/api/v1/admin/wechat/accounts')

const formRef = ref()
const formData = ref<Record<string, unknown>>({ ...props.account })

watch(() => props.account, (v) => { formData.value = { ...v } })

async function handleConfirm() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  const ok = await update(props.account.id as number, formData.value)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="编辑微信账号">
    <template #body>
      <UAlert v-if="errorMessage" color="error" :title="errorMessage" class="mb-4" />
      <WechatAccountsWeChatAccountForm ref="formRef" :data="account" @change="formData = $event" />
    </template>
    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="确定" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>
