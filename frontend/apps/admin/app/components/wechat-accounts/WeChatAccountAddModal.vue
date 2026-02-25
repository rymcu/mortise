<script setup lang="ts">
/**
 * 新增微信账号弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { create, loading, errorMessage } = useAdminCrud('/api/v1/admin/wechat/accounts')

const formRef = ref()
const formData = ref<Record<string, unknown>>({})

async function handleConfirm() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  const ok = await create(formData.value)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="新增微信账号">
    <template #body>
      <UAlert v-if="errorMessage" color="error" :title="errorMessage" class="mb-4" />
      <WechatAccountsWeChatAccountForm ref="formRef" @change="formData = $event" />
    </template>
    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="确定" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>
