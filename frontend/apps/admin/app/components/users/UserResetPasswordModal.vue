<script setup lang="ts">
/**
 * 重置用户密码弹窗
 */
const props = defineProps<{ user: Record<string, unknown> }>()
const emit = defineEmits<{ (e: 'success'): void }>()

const { loading, postAction } = useAdminCrud('/api/v1/admin/users')
const open = ref(false)
const newPassword = ref('')

async function onConfirm() {
  if (!newPassword.value || newPassword.value.length < 6) return
  const result = await postAction(`/${props.user.id}/reset-password`, { password: newPassword.value })
  if (result !== null) {
    open.value = false
    newPassword.value = ''
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="重置密码">
    <UButton icon="i-lucide-key-round" color="neutral" variant="ghost" size="xs" @click="open = true">
      重置密码
    </UButton>
    <template #body>
      <div class="space-y-3">
        <p class="text-sm text-muted">
          为用户「{{ user.nickname || user.account }}」设置新密码
        </p>
        <UInput
          v-model="newPassword"
          type="password"
          placeholder="请输入新密码（至少 6 位）"
          class="w-full"
        />
      </div>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="primary" :loading="loading" :disabled="newPassword.length < 6" @click="onConfirm">确认重置</UButton>
        <UButton color="neutral" variant="subtle" :disabled="loading" @click="open = false">取消</UButton>
      </div>
    </template>
  </UModal>
</template>
