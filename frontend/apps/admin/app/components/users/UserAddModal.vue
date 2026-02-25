<script setup lang="ts">
/**
 * 新增用户弹窗
 */
const emit = defineEmits<{ (e: 'success'): void }>()

const { loading, create } = useAdminCrud('/api/v1/admin/users')
const open = ref(false)
const formRef = ref<{ validate: () => Promise<boolean>; state: Record<string, unknown> } | null>(null)

function openModal() {
  open.value = true
}

async function onSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  const result = await create(formRef.value!.state)
  if (result !== null) {
    open.value = false
    emit('success')
  }
}
</script>

<template>
  <UModal v-model:open="open" title="新增用户">
    <UButton icon="i-lucide-plus" color="primary" variant="soft" @click="openModal">
      新增用户
    </UButton>
    <template #body>
      <UsersUserForm ref="formRef" />
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="primary" :loading="loading" @click="onSubmit">保存</UButton>
        <UButton color="neutral" variant="subtle" :disabled="loading" @click="open = false">取消</UButton>
      </div>
    </template>
  </UModal>
</template>
