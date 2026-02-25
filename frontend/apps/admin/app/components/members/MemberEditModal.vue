<script setup lang="ts">
/**
 * 编辑会员状态弹窗
 */
const open = defineModel<boolean>('open', { default: false })

const props = defineProps<{
  member: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { $api } = useNuxtApp()
const { loading, errorMessage, putAction } = useAdminCrud('/api/v1/admin/members')

const statusOptions = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]

const selectedStatus = ref<number>((props.member.status as number) ?? 0)

watch(() => props.member, (v) => {
  selectedStatus.value = (v.status as number) ?? 0
})

async function handleConfirm() {
  const id = props.member.id as number
  if (selectedStatus.value === 0) {
    const ok = await putAction(`${id}/enable`)
    if (ok) { open.value = false; emit('success') }
  } else {
    const ok = await putAction(`${id}/disable`)
    if (ok) { open.value = false; emit('success') }
  }
}
</script>

<template>
  <UModal v-model:open="open" title="编辑会员状态">
    <template #body>
      <UAlert v-if="errorMessage" color="error" :title="errorMessage" class="mb-4" />
      <div class="space-y-4">
        <div class="text-sm text-gray-600 dark:text-gray-400">
          会员：<span class="font-medium text-gray-900 dark:text-white">{{ member.nickname || member.username }}</span>
        </div>

        <div>
          <label class="block text-sm font-medium mb-2">状态</label>
          <div class="flex gap-4">
            <label v-for="opt in statusOptions" :key="opt.value" class="flex items-center gap-1.5 cursor-pointer">
              <input
                type="radio"
                :value="opt.value"
                :checked="selectedStatus === opt.value"
                @change="selectedStatus = opt.value"
                class="accent-primary"
              />
              <span class="text-sm">{{ opt.label }}</span>
            </label>
          </div>
        </div>
      </div>
    </template>
    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton variant="ghost" label="取消" @click="open = false" />
        <UButton label="确定" :loading="loading" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>
