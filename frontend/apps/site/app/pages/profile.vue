<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

useSeoMeta({
  title: '个人中心',
  description: '查看和编辑您的个人资料'
})

// ── 左侧菜单 ──────────────────────────────────────────────────────────────────
type Section = 'info' | 'security'
const activeSection = ref<Section>('info')

const navItems: { key: Section; label: string; icon: string }[] = [
  { key: 'info', label: '个人资料', icon: 'i-lucide-user' },
  { key: 'security', label: '修改密码', icon: 'i-lucide-lock' }
]

const { profile, loading, error, fetchProfile, uploadAvatar, updateProfile, updatePassword } = useProfile()
const auth = useAuthStore()

// 使用 useAsyncData 避免 Suspense 阻塞页面切换
await useAsyncData('site-profile', () => fetchProfile())

// ── 个人资料表单 ─────────────────────────────────────────────

const { resolveUrl } = useMediaUrl()

const profileState = reactive({
  nickname: '',
  gender: '',
  birthDate: ''
})

const pendingAvatar = ref<string | null>(null)
const avatarPreview = ref<string | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const profileSuccess = ref('')

// watch 同步：profile 加载后填入表单
watch(
  () => profile.value,
  (p) => {
    if (p) {
      profileState.nickname = p.nickname ?? ''
      profileState.gender = p.gender ?? ''
      profileState.birthDate = p.birthDate ?? ''
    }
  },
  { immediate: true }
)

const displayAvatar = computed(
  () => avatarPreview.value || resolveUrl(profile.value?.avatarUrl) || null
)

const canSaveProfile = computed(() => {
  const nick = profileState.nickname.trim()
  return nick.length > 0 && nick.length <= 32
})

function triggerAvatarUpload() {
  fileInput.value?.click()
}

async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  avatarPreview.value = URL.createObjectURL(file)
  const url = await uploadAvatar(file)
  if (url) {
    pendingAvatar.value = url
  }
  else {
    avatarPreview.value = null
    pendingAvatar.value = null
  }
  input.value = ''
}

async function onProfileSubmit() {
  profileSuccess.value = ''
  if (!canSaveProfile.value) return
  const ok = await updateProfile({
    nickname: profileState.nickname.trim(),
    avatarUrl: pendingAvatar.value,
    gender: profileState.gender || null,
    birthDate: profileState.birthDate || null
  })
  if (ok) {
    profileSuccess.value = '个人资料已更新'
    pendingAvatar.value = null
    avatarPreview.value = null
  }
}

// ── 修改密码 ─────────────────────────────────────────────────

const passwordState = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const show = reactive({
  current: false,
  new: false,
  confirm: false
})

const passwordError = ref('')
const passwordSuccess = ref('')

const rules = [
  { regex: /.{8,}/, text: '至少 8 个字符' },
  { regex: /\d/, text: '至少 1 个数字' },
  { regex: /[a-z]/, text: '至少 1 个小写字母' },
  { regex: /[A-Z]/, text: '至少 1 个大写字母' }
]

const strength = computed(() =>
  rules.map(rule => ({ met: rule.regex.test(passwordState.newPassword), text: rule.text }))
)
const score = computed(() => strength.value.filter(r => r.met).length)
const color = computed(() => {
  if (score.value === 0) return 'neutral'
  if (score.value <= 1) return 'error'
  if (score.value <= 3) return 'warning'
  return 'success'
})
const strengthText = computed(() => {
  if (score.value === 0) return '请输入新密码'
  if (score.value <= 2) return '密码强度弱'
  if (score.value === 3) return '密码强度中'
  return '密码强度高'
})
const confirmMatched = computed(
  () => passwordState.confirmPassword.length > 0 && passwordState.newPassword === passwordState.confirmPassword
)
const canChangePassword = computed(
  () => passwordState.currentPassword.length > 0 && score.value === 4 && confirmMatched.value
)

async function onPasswordSubmit() {
  passwordError.value = ''
  passwordSuccess.value = ''
  if (!canChangePassword.value) {
    passwordError.value = '请满足密码强度要求，并确认两次输入的新密码一致'
    return
  }
  const ok = await updatePassword(passwordState.currentPassword, passwordState.newPassword)
  if (ok) {
    passwordSuccess.value = '密码修改成功，即将重新登录...'
    passwordState.currentPassword = ''
    passwordState.newPassword = ''
    passwordState.confirmPassword = ''
    setTimeout(() => auth.logout(), 2000)
  }
  else {
    passwordError.value = error.value || '修改密码失败'
  }
}

</script>

<template>
  <UContainer class="max-w-5xl py-10">
    <div class="flex flex-col gap-6 md:flex-row md:items-start">

      <!-- ── 左侧侧边栏 ──────────────────────────────────────────── -->
      <aside class="mx-auto w-full max-w-xs shrink-0 md:mx-0 md:sticky md:top-20 md:w-56">
        <!-- 用户信息卡 -->
        <div class="mb-4 flex flex-col items-center gap-3 rounded-xl border border-default bg-default p-4">
          <button
            type="button"
            style="width: 5rem; height: 5rem;"
            class="group relative shrink-0 cursor-pointer overflow-hidden rounded-full"
            :disabled="loading"
            title="点击更换头像"
            @click="triggerAvatarUpload"
          >
            <img
              v-if="displayAvatar"
              :src="displayAvatar"
              alt="头像"
              style="width: 5rem; height: 5rem;"
              class="rounded-full object-cover"
            />
            <div
              v-else
              class="bg-primary/10 flex h-full w-full items-center justify-center"
            >
              <UIcon name="i-lucide-user" class="text-primary text-3xl" />
            </div>
            <div
              class="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 transition-opacity group-hover:opacity-100"
            >
              <UIcon name="i-lucide-camera" class="text-xl text-white" />
            </div>
          </button>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            class="hidden"
            @change="onFileChange"
          />
          <div class="text-center">
            <div class="font-semibold">{{ profile?.nickname || profile?.username || '用户' }}</div>
            <div class="text-muted text-xs">@{{ profile?.username }}</div>
            <div class="text-muted mt-1 text-xs">点击头像可更换图片</div>
          </div>
        </div>

        <!-- 导航菜单 -->
        <nav class="overflow-hidden rounded-xl border border-default bg-default">
          <button
            v-for="item in navItems"
            :key="item.key"
            type="button"
            class="flex w-full items-center gap-3 px-4 py-3 text-sm transition-colors"
            :class="activeSection === item.key
              ? 'bg-primary/10 text-primary font-medium'
              : 'text-default hover:bg-elevated'"
            @click="activeSection = item.key"
          >
            <UIcon :name="item.icon" class="size-4 shrink-0" />
            {{ item.label }}
          </button>
        </nav>
      </aside>

      <!-- ── 右侧内容区 ──────────────────────────────────────────── -->
      <div class="min-w-0 flex-1">

        <!-- 个人资料 -->
        <UCard v-if="activeSection === 'info'">
          <template #header>
            <h2 class="text-lg font-semibold">个人资料</h2>
          </template>

          <div class="space-y-4">
            <div class="grid grid-cols-1 gap-4 text-sm sm:grid-cols-2">
              <div>
                <div class="text-muted">账号</div>
                <div class="font-medium">{{ profile?.username || '-' }}</div>
              </div>
              <div>
                <div class="text-muted">邮箱</div>
                <div class="font-medium">{{ profile?.email || '-' }}</div>
              </div>
            </div>

            <USeparator />

            <UFormField label="昵称" required>
              <UInput
                v-model="profileState.nickname"
                placeholder="请输入昵称"
                :disabled="loading"
                class="w-full"
              />
            </UFormField>

            <UFormField label="性别">
              <USelect
                v-model="profileState.gender"
                :options="[
                  { label: '保密', value: '' },
                  { label: '男', value: 'male' },
                  { label: '女', value: 'female' }
                ]"
                :disabled="loading"
                class="w-full"
              />
            </UFormField>

            <UFormField label="生日">
              <UInput
                v-model="profileState.birthDate"
                type="date"
                :disabled="loading"
                class="w-full"
              />
            </UFormField>

            <div class="flex justify-end">
              <UButton :loading="loading" :disabled="!canSaveProfile" @click="onProfileSubmit">
                保存资料
              </UButton>
            </div>
          </div>

          <UAlert
            v-if="profileSuccess"
            color="success"
            variant="soft"
            :title="profileSuccess"
            class="mt-4"
          />
          <UAlert
            v-if="error && activeSection === 'info'"
            color="error"
            variant="soft"
            :title="error"
            class="mt-4"
          />
        </UCard>

        <!-- 修改密码 -->
        <UCard v-else-if="activeSection === 'security'">
          <template #header>
            <h2 class="text-lg font-semibold">修改密码</h2>
          </template>

          <div class="space-y-4">
            <UFormField label="当前密码" required>
              <UInput
                v-model="passwordState.currentPassword"
                :type="show.current ? 'text' : 'password'"
                placeholder="请输入当前密码"
                :ui="{ trailing: 'pe-1' }"
                class="w-full"
              >
                <template #trailing>
                  <UButton
                    color="neutral"
                    variant="link"
                    size="sm"
                    :icon="show.current ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                    :aria-label="show.current ? '隐藏密码' : '显示密码'"
                    :aria-pressed="show.current"
                    @click="show.current = !show.current"
                  />
                </template>
              </UInput>
            </UFormField>

            <UFormField label="新密码" required>
              <UInput
                v-model="passwordState.newPassword"
                :type="show.new ? 'text' : 'password'"
                :color="color"
                placeholder="请输入新密码"
                :ui="{ trailing: 'pe-1' }"
                :aria-invalid="score < 4"
                class="w-full"
              >
                <template #trailing>
                  <UButton
                    color="neutral"
                    variant="link"
                    size="sm"
                    :icon="show.new ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                    :aria-label="show.new ? '隐藏密码' : '显示密码'"
                    :aria-pressed="show.new"
                    @click="show.new = !show.new"
                  />
                </template>
              </UInput>
            </UFormField>

            <UProgress :color="color" :indicator="strengthText" :model-value="score" :max="4" size="sm" />

            <ul class="space-y-1">
              <li
                v-for="(rule, i) in strength"
                :key="i"
                class="flex items-center gap-1"
                :class="rule.met ? 'text-success' : 'text-muted'"
              >
                <UIcon :name="rule.met ? 'i-lucide-circle-check' : 'i-lucide-circle-x'" class="size-4 shrink-0" />
                <span class="text-xs">{{ rule.text }}</span>
              </li>
            </ul>

            <UFormField label="确认新密码" required>
              <UInput
                v-model="passwordState.confirmPassword"
                :type="show.confirm ? 'text' : 'password'"
                :color="passwordState.confirmPassword ? (confirmMatched ? 'success' : 'error') : 'neutral'"
                placeholder="请再次输入新密码"
                :ui="{ trailing: 'pe-1' }"
                :aria-invalid="passwordState.confirmPassword.length > 0 && !confirmMatched"
                class="w-full"
              >
                <template #trailing>
                  <UButton
                    color="neutral"
                    variant="link"
                    size="sm"
                    :icon="show.confirm ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                    :aria-label="show.confirm ? '隐藏密码' : '显示密码'"
                    :aria-pressed="show.confirm"
                    @click="show.confirm = !show.confirm"
                  />
                </template>
              </UInput>
            </UFormField>

            <div class="flex justify-end">
              <UButton
                color="error"
                variant="outline"
                :loading="loading"
                :disabled="!canChangePassword"
                @click="onPasswordSubmit"
              >
                提交修改
              </UButton>
            </div>
          </div>

          <UAlert
            v-if="passwordError"
            color="error"
            variant="soft"
            :title="passwordError"
            class="mt-4"
          />
          <UAlert
            v-if="passwordSuccess"
            color="success"
            variant="soft"
            :title="passwordSuccess"
            class="mt-4"
          />
        </UCard>

      </div>
    </div>
  </UContainer>
</template>
