<script setup lang="ts">
/**
 * 编辑用户弹窗
 */
const props = defineProps<{ user: Record<string, unknown> }>()
const emit = defineEmits<{ (e: 'success'): void }>()

const { loading, update } = useAdminCrud('/api/v1/admin/users')
const open = ref(false)
const formRef = ref<{ validate: () => Promise<boolean>; state: Record<string, unknown> } | null>(null)

function openModal() {
  open.value = true
}

async function onSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  const result = await update(props.user.id as number, formRef.value!.state)
  if (result !== null) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="编辑用户">
    <UButton icon="i-lucide-pencil" color="primary" variant="ghost" size="xs" @click="openModal">
      编辑
    </UButton>
    <template #body>
      <UsersUserForm ref="formRef" :data="user" edit-mode />
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="primary" :loading="loading" @click="onSubmit">保存</UButton>
        <UButton color="neutral" variant="subtle" :disabled="loading" @click="open = false">取消</UButton>
      </div>
    </template>
  </UModal>
</template>
