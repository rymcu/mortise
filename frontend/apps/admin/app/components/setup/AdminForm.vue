<script setup lang="ts">
import type { ZodSchema } from 'zod'

const adminState = defineModel<{
  adminNickname: string
  adminEmail: string
  adminPassword: string
  adminPasswordConfirm: string
}>('adminState', { required: true })

const showAdminPassword = defineModel<boolean>('showAdminPassword', {
  required: true
})
const showAdminPasswordConfirm = defineModel<boolean>(
  'showAdminPasswordConfirm',
  { required: true }
)

defineProps<{
  adminSchema: ZodSchema
}>()

const emit = defineEmits<{
  submit: [event: unknown]
  prev: []
}>()
</script>

<template>
  <UForm
    :schema="adminSchema"
    :state="adminState"
    class="space-y-4"
    @submit="emit('submit', $event)"
  >
    <UFormField label="管理员昵称" name="adminNickname" required>
      <UInput
        v-model="adminState.adminNickname"
        placeholder="请输入管理员昵称"
        class="w-full"
      />
    </UFormField>

    <UFormField label="管理员邮箱" name="adminEmail" required>
      <UInput
        v-model="adminState.adminEmail"
        type="email"
        placeholder="请输入管理员邮箱"
        class="w-full"
      />
    </UFormField>

    <UFormField label="管理员密码" name="adminPassword" required>
      <UInput
        v-model="adminState.adminPassword"
        :type="showAdminPassword ? 'text' : 'password'"
        placeholder="请输入管理员密码（至少 8 位）"
        :ui="{ trailing: 'pe-1' }"
        class="w-full"
      >
        <template #trailing>
          <UButton
            color="neutral"
            variant="link"
            size="sm"
            :icon="showAdminPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
            :aria-label="showAdminPassword ? '隐藏密码' : '显示密码'"
            :aria-pressed="showAdminPassword"
            @click="showAdminPassword = !showAdminPassword"
          />
        </template>
      </UInput>
    </UFormField>

    <UFormField label="确认密码" name="adminPasswordConfirm" required>
      <UInput
        v-model="adminState.adminPasswordConfirm"
        :type="showAdminPasswordConfirm ? 'text' : 'password'"
        placeholder="请再次输入密码"
        :ui="{ trailing: 'pe-1' }"
        class="w-full"
      >
        <template #trailing>
          <UButton
            color="neutral"
            variant="link"
            size="sm"
            :icon="
              showAdminPasswordConfirm ? 'i-lucide-eye-off' : 'i-lucide-eye'
            "
            :aria-label="showAdminPasswordConfirm ? '隐藏密码' : '显示密码'"
            :aria-pressed="showAdminPasswordConfirm"
            @click="showAdminPasswordConfirm = !showAdminPasswordConfirm"
          />
        </template>
      </UInput>
    </UFormField>

    <div class="flex justify-between pt-2">
      <UButton color="neutral" variant="ghost" @click="emit('prev')">
        <template #leading>
          <UIcon name="i-lucide-arrow-left" />
        </template>
        上一步
      </UButton>
      <UButton type="submit">
        下一步
        <template #trailing>
          <UIcon name="i-lucide-arrow-right" />
        </template>
      </UButton>
    </div>
  </UForm>
</template>
