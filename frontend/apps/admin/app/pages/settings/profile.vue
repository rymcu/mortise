<script setup lang="ts">
/**
 * 资料设置页面
 */

const { profile, loading, error, fetchProfile, updateProfile } = useProfile()

// 如果没数据则获取（支持直接刷新访问）
if (!profile.value) {
  await fetchProfile()
}

const state = reactive({
  nickname: '',
  email: ''
})

const submitSuccess = ref('')

// 响应式同步 profile 数据到表单
watch(
  () => profile.value,
  (newProfile) => {
    if (newProfile) {
      state.nickname = newProfile.nickname || ''
      state.email = newProfile.email || ''
    }
  },
  { immediate: true }
)

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
  submitSuccess.value = ''

  if (!canSubmit.value) {
    error.value = '请检查昵称和邮箱格式后再提交'
    return
  }

  const success = await updateProfile({
    nickname: state.nickname.trim(),
    email: state.email.trim() || null
  })

  if (success) {
    submitSuccess.value = '资料保存成功'
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
                  {{ profile?.nickname || profile?.account || '-' }}
                </div>
                <div class="text-muted text-sm">
                  {{ profile?.email || '-' }}
                </div>
              </div>
            </div>

            <div class="grid grid-cols-1 gap-4 text-sm sm:grid-cols-2">
              <div>
                <div class="text-muted">账号</div>
                <div class="font-medium">
                  {{ profile?.account || '-' }}
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
        <UAlert v-if="error" color="error" variant="soft" :title="error" />
      </div>
    </template>
  </UDashboardPanel>
</template>
