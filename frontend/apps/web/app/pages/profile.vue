<script setup lang="ts">
const { profile, loading, error: profileError, fetchProfile, uploadAvatar, updateProfile } = useProfile()

// 如果没数据则获取（支持直接刷新访问）
if (!profile.value) {
  await fetchProfile()
}

const state = reactive({
  nickname: ''
})

// 待提交的头像 URL（上传完成后赋值）
const pendingAvatarUrl = ref<string | null>(null)
// 头像本地预览 URL
const avatarPreview = ref<string | null>(null)
const submitSuccess = ref('')
const submitError = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

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
  () => avatarPreview.value || resolveUrl(profile.value?.avatarUrl) || null
)

const canSubmit = computed(() => {
  const nickname = state.nickname.trim()
  return nickname.length > 0 && nickname.length <= 30
})

/** 触发文件选择框 */
function triggerAvatarUpload() {
  fileInput.value?.click()
}

/** 选择文件后立即上传 */
async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 本地预览
  avatarPreview.value = URL.createObjectURL(file)

  // 上传到服务器
  const url = await uploadAvatar(file)
  if (url) {
    pendingAvatarUrl.value = url
  } else {
    // 上传失败，清除预览
    avatarPreview.value = null
    pendingAvatarUrl.value = null
  }

  // 重置 input，允许再次选同一文件
  input.value = ''
}

async function onSubmit() {
  submitSuccess.value = ''
  submitError.value = ''

  if (!canSubmit.value) {
    submitError.value = '昵称不能为空且不超过 30 个字符'
    return
  }

  const success = await updateProfile({
    nickname: state.nickname.trim(),
    avatarUrl: pendingAvatarUrl.value
  })

  if (success) {
    submitSuccess.value = '资料保存成功'
    pendingAvatarUrl.value = null
    avatarPreview.value = null
  } else {
    submitError.value = profileError.value || '保存失败，请重试'
  }
}
</script>

<template>
  <UContainer class="py-8">
    <div class="mx-auto max-w-2xl space-y-6">
      <!-- 页面标题 -->
      <div class="flex items-center gap-3">
        <UButton
          variant="ghost"
          color="neutral"
          size="sm"
          icon="i-lucide-arrow-left"
          to="/"
        />
        <h1 class="text-2xl font-semibold">个人中心</h1>
      </div>

      <!-- 资料卡片 -->
      <UCard>
        <template #header>
          <h3 class="text-lg font-semibold">基本资料</h3>
        </template>

        <div class="space-y-6">
          <!-- 头像区域 -->
          <div class="flex items-center gap-5">
            <button
              type="button"
              class="group relative size-20 shrink-0 cursor-pointer overflow-hidden rounded-full"
              :disabled="loading"
              title="点击更换头像"
              @click="triggerAvatarUpload"
            >
              <img
                v-if="displayAvatar"
                :src="displayAvatar"
                alt="头像"
                class="size-full object-cover"
              />
              <div
                v-else
                class="bg-primary/10 flex size-full items-center justify-center"
              >
                <UIcon name="i-lucide-user" class="text-primary text-3xl" />
              </div>
              <!-- 悬浮上传提示遮罩 -->
              <div
                class="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 transition-opacity group-hover:opacity-100"
              >
                <UIcon name="i-lucide-camera" class="text-xl text-white" />
              </div>
            </button>

            <!-- 隐藏的文件输入框 -->
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              class="hidden"
              @change="onFileChange"
            />

            <div class="space-y-1">
              <div class="text-lg font-medium">
                {{ profile?.nickname || profile?.username || '-' }}
              </div>
              <div class="text-muted text-sm">
                {{ profile?.email || profile?.phone || '-' }}
              </div>
              <div class="text-muted text-xs">点击头像可更换图片</div>
            </div>
          </div>

          <USeparator />

          <!-- 只读信息 -->
          <div class="grid grid-cols-1 gap-4 text-sm sm:grid-cols-2">
            <div>
              <div class="text-muted mb-1">用户名</div>
              <div class="font-medium">{{ profile?.username || '-' }}</div>
            </div>
            <div>
              <div class="text-muted mb-1">邮箱</div>
              <div class="font-medium">{{ profile?.email || '-' }}</div>
            </div>
            <div>
              <div class="text-muted mb-1">手机号</div>
              <div class="font-medium">{{ profile?.phone || '-' }}</div>
            </div>
            <div>
              <div class="text-muted mb-1">注册时间</div>
              <div class="font-medium">
                {{ profile?.createdAt ? profile.createdAt.slice(0, 10) : '-' }}
              </div>
            </div>
          </div>

          <USeparator />

          <!-- 可编辑表单 -->
          <div class="space-y-4">
            <UFormField label="昵称" required>
              <UInput
                v-model="state.nickname"
                placeholder="请输入昵称"
                :disabled="loading"
                class="w-full"
              />
            </UFormField>

            <!-- 操作结果提示 -->
            <UAlert
              v-if="submitSuccess"
              color="success"
              variant="soft"
              :description="submitSuccess"
              icon="i-lucide-check-circle"
            />
            <UAlert
              v-if="submitError"
              color="error"
              variant="soft"
              :description="submitError"
              icon="i-lucide-alert-circle"
            />

            <div class="flex justify-end">
              <UButton
                :loading="loading"
                :disabled="!canSubmit"
                @click="onSubmit"
              >
                保存资料
              </UButton>
            </div>
          </div>
        </div>
      </UCard>
    </div>
  </UContainer>
</template>

