<script setup lang="ts">
import { fetchAdminPut } from '@mortise/core-sdk'

/**
 * 资料设置页面
 */
const auth = useAuthStore()
const user = computed(
  () => (auth.session?.user as Record<string, unknown> | undefined) ?? {}
)

const state = reactive({
  nickname: '',
  email: ''
})

const loading = ref(false)
const submitError = ref('')
const submitSuccess = ref('')

watchEffect(() => {
  state.nickname = String(user.value.nickname ?? '')
  state.email = String(user.value.email ?? '')
})

const canSubmit = computed(() => {
  const nickname = state.nickname.trim()
  if (!nickname || nickname.length > 30) {
    return false
  }

  const email = state.email.trim()
  if (!email) {
    return true
  }

  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
})

async function onSubmit() {
  submitError.value = ''
  submitSuccess.value = ''

  const userId = Number(user.value.id)
  if (!Number.isFinite(userId) || userId <= 0) {
    submitError.value = '当前用户信息缺失，无法保存资料'
    return
  }

  if (!canSubmit.value) {
    submitError.value = '请检查昵称和邮箱格式后再提交'
    return
  }

  const { $api } = useNuxtApp()
  loading.value = true
  try {
    await fetchAdminPut<boolean>($api, `/api/v1/admin/users/${userId}`, {
      id: userId,
      account: user.value.account,
      nickname: state.nickname.trim(),
      email: state.email.trim() || null
    })
    await auth.fetchCurrentUser()
    submitSuccess.value = '资料保存成功'
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : '资料保存失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <UDashboardPanel id="settings-profile">
    <template #header>
      <UDashboardNavbar title="资料设置">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="mx-auto max-w-2xl space-y-6">
        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold">个人资料</h3>
          </template>
          <div class="space-y-4">
            <div class="flex items-center gap-4">
              <div
                class="bg-primary/10 flex h-16 w-16 items-center justify-center rounded-full"
              >
                <UIcon name="i-lucide-user" class="text-primary text-2xl" />
              </div>
              <div>
                <div class="font-medium">
                  {{ user.nickname || user.account || '-' }}
                </div>
                <div class="text-muted text-sm">
                  {{ user.email || '-' }}
                </div>
              </div>
            </div>

            <div class="grid grid-cols-1 gap-4 text-sm sm:grid-cols-2">
              <div>
                <div class="text-muted">账号</div>
                <div class="font-medium">
                  {{ user.account || '-' }}
                </div>
              </div>
              <div>
                <div class="text-muted">用户 ID</div>
                <div class="font-medium">
                  {{ user.id || '-' }}
                </div>
              </div>
            </div>

            <USeparator />

            <div class="space-y-4">
              <UFormField label="昵称" required>
                <UInput
                  v-model="state.nickname"
                  placeholder="请输入昵称"
                  :disabled="loading"
                  class="w-full"
                />
              </UFormField>

              <UFormField label="邮箱">
                <UInput
                  v-model="state.email"
                  type="email"
                  placeholder="请输入邮箱（可选）"
                  :disabled="loading"
                  class="w-full"
                />
              </UFormField>

              <div class="flex justify-end">
                <UButton :loading="loading" :disabled="!canSubmit" @click="onSubmit">
                  保存资料
                </UButton>
              </div>
            </div>
          </div>
        </UCard>

        <UAlert
          v-if="submitSuccess"
          color="success"
          variant="soft"
          :title="submitSuccess"
        />
        <UAlert v-if="submitError" color="error" variant="soft" :title="submitError" />
      </div>
    </template>
  </UDashboardPanel>
</template>
