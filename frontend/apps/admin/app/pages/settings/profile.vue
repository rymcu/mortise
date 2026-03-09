<script setup lang="ts">
/**
 * 资料设置页面
 */

const { profile, loading, error: profileError, fetchProfile, uploadAvatar, updateProfile } = useProfile()

// 如果没数据则获取（支持直接刷新访问）
if (!profile.value) {
  await fetchProfile()
}

const state = reactive({
  nickname: ''
})

// 待提交的头像 URL（上传完成后赋值）
const pendingAvatar = ref<string | null>(null)
// 头像本地预览 URL
const avatarPreview = ref<string | null>(null)
const avatarUploadFile = ref<File | null>(null)
const submitSuccess = ref('')
const submitError = ref('')

// 响应式同步 profile 数据到表单
watch(
  () => profile.value,
  (newProfile) => {
    if (newProfile) {
      state.nickname = newProfile.nickname || ''
    }
  },
  { immediate: true }
)

/** 显示的头像：优先本地预览，其次 profile 头像，最后 null */
const { resolveUrl } = useMediaUrl()
const displayAvatar = computed(
  () => avatarPreview.value || resolveUrl(profile.value?.avatar) || null
)

const canSubmit = computed(() => {
  const nickname = state.nickname.trim()
  return !(!nickname || nickname.length > 30)
})

/** 选择文件后立即上传 */
async function onAvatarFileChange(file: File | null | undefined) {
  if (!file) return

  // 本地预览
  avatarPreview.value = URL.createObjectURL(file)

  // 上传到服务器
  const url = await uploadAvatar(file)
  if (url) {
    pendingAvatar.value = url
  } else {
    // 上传失败，清除预览
    avatarPreview.value = null
    pendingAvatar.value = null
  }

  avatarUploadFile.value = null
}

function clearPendingAvatar(removeFile?: () => void) {
  avatarUploadFile.value = null
  avatarPreview.value = null
  pendingAvatar.value = null
  removeFile?.()
}

async function onSubmit() {
  submitSuccess.value = ''
  submitError.value = ''

  if (!canSubmit.value) {
    submitError.value = '请检查昵称格式后再提交'
    return
  }

  const success = await updateProfile({
    nickname: state.nickname.trim(),
    avatar: pendingAvatar.value
  })

  if (success) {
    submitSuccess.value = '资料保存成功'
    pendingAvatar.value = null
    avatarPreview.value = null
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
      <div class="space-y-6 p-4">
        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold">个人资料</h3>
          </template>
          <div class="space-y-4">
            <UFileUpload
              v-slot="{ open, removeFile }"
              v-model="avatarUploadFile"
              accept="image/*"
              :preview="false"
              :interactive="false"
              :reset="true"
              :disabled="loading"
              class="w-full"
              @update:model-value="onAvatarFileChange"
            >
              <div class="flex items-center gap-4">
                <!-- 头像区域，点击上传 -->
                <UButton
                  type="button"
                  variant="ghost"
                  color="neutral"
                  class="group relative h-16 w-16 shrink-0 overflow-hidden rounded-full p-0"
                  :disabled="loading"
                  title="点击更换头像"
                  @click="open()"
                >
                  <img
                    v-if="displayAvatar"
                    :src="displayAvatar"
                    alt="头像"
                    class="h-full w-full object-cover"
                  />
                  <div
                    v-else
                    class="bg-primary/10 flex h-full w-full items-center justify-center"
                  >
                    <UIcon name="i-lucide-user" class="text-primary text-2xl" />
                  </div>
                  <!-- 悬浮上传提示遮罩 -->
                  <div
                    class="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 transition-opacity group-hover:opacity-100"
                  >
                    <UIcon name="i-lucide-camera" class="text-xl text-white" />
                  </div>
                </UButton>

                <div>
                  <div class="font-medium">
                    {{ profile?.nickname || profile?.account || '-' }}
                  </div>
                  <div class="text-muted text-sm">
                    {{ profile?.email || '-' }}
                  </div>
                  <div class="mt-1 flex flex-wrap items-center gap-2">
                    <UButton color="neutral" variant="outline" size="sm" icon="i-lucide-upload" @click="open()">
                      {{ pendingAvatar ? '重新选择头像' : '上传头像' }}
                    </UButton>
                    <UButton
                      v-if="pendingAvatar"
                      color="error"
                      variant="link"
                      size="sm"
                      class="px-0"
                      @click="clearPendingAvatar(removeFile)"
                    >
                      移除本次更改
                    </UButton>
                  </div>
                </div>
              </div>
            </UFileUpload>

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
                <div class="text-sm">
                  {{ profile?.email || '-' }}
                  <span class="text-muted ml-2 text-xs">如需修改，请前往安全设置</span>
                </div>
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
        <UAlert v-if="submitError || profileError" color="error" variant="soft" :title="submitError || profileError" />
      </div>
    </template>
  </UDashboardPanel>
</template>

