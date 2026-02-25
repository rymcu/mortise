<script setup lang="ts">
/**
 * 编辑 OAuth2 客户端弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  client: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { update, loading, errorMessage } = useAdminCrud('/api/v1/admin/oauth2/client-configs')

const formRef = ref()
const formData = ref<Record<string, unknown>>({ ...props.client })

watch(() => props.client, (v) => { formData.value = { ...v } })

async function handleConfirm() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  const ok = await update(props.client.id as number, formData.value)
  if (ok) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="编辑 OAuth2 客户端">
    <template #body>
      <UAlert v-if="errorMessage" color="error" :title="errorMessage" class="mb-4" />
      <Oauth2ClientsOauth2ClientForm ref="formRef" :data="client" @change="formData = $event" />
    </template>
    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="确定" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>
