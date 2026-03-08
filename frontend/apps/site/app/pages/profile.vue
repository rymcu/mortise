<script setup lang="ts">
import type { ProfileNavItem, ProfileSection } from '~/types/profile'

definePageMeta({
  middleware: 'auth'
})

useSeoMeta({
  title: '个人中心',
  description: '查看和编辑您的个人资料'
})

const activeSection = ref<ProfileSection>('info')

const navItems: ProfileNavItem[] = [
  { key: 'info', label: '个人资料', icon: 'i-lucide-user', description: '维护昵称、头像和基础档案' },
  { key: 'security', label: '修改密码', icon: 'i-lucide-lock', description: '更新登录密码与安全设置' }
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
const profileHint = ref('')
const hiddenGenderValue = '__hidden__'

const genderItems = [
  { label: '保密', value: hiddenGenderValue },
  { label: '男', value: 'male' },
  { label: '女', value: 'female' }
]

const selectedGender = computed({
  get: () => profileState.gender || hiddenGenderValue,
  set: (value: string) => {
    profileState.gender = value === hiddenGenderValue ? '' : value
  }
})

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

const displayName = computed(() => profile.value?.nickname || profile.value?.username || '用户')

const profileSummary = computed(() => [
  { label: '账号', value: profile.value?.username || '-', icon: 'i-lucide-at-sign' },
  { label: '邮箱', value: profile.value?.email || '未绑定', icon: 'i-lucide-mail' },
  { label: '手机号', value: profile.value?.phone || '未绑定', icon: 'i-lucide-smartphone' },
])

const completionCount = computed(() => {
  const fields = [
    profileState.nickname.trim(),
    profileState.gender,
    profileState.birthDate,
    profile.value?.email,
    profile.value?.phone,
    displayAvatar.value,
  ]
  return fields.filter(Boolean).length
})

const strengthSummary = computed(() => {
  if (score.value === 0) return '待设置'
  if (score.value <= 2) return '较弱'
  if (score.value === 3) return '中等'
  return '较强'
})

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
  profileHint.value = ''
  avatarPreview.value = URL.createObjectURL(file)
  const url = await uploadAvatar(file)
  if (url) {
    pendingAvatar.value = url
    profileHint.value = '新头像已上传，记得点击“保存资料”后生效。'
  }
  else {
    avatarPreview.value = null
    pendingAvatar.value = null
  }
  input.value = ''
}

async function onProfileSubmit() {
  profileSuccess.value = ''
  profileHint.value = ''
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
  <UContainer class="max-w-6xl py-8 sm:py-10">
    <UCard class="mb-6 overflow-hidden border-default/70 bg-[linear-gradient(135deg,rgba(15,23,42,0.98),rgba(8,47,73,0.94))] text-white">
      <div class="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
        <div class="max-w-3xl">
          <div class="mb-3 inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/8 px-3 py-1 text-xs tracking-[0.18em] text-white/72 uppercase">
            <UIcon name="i-lucide-user-cog" class="size-3.5" />
            Site Profile
          </div>
          <h1 class="text-2xl font-semibold tracking-tight sm:text-3xl">个人中心</h1>
          <p class="mt-3 max-w-2xl text-sm leading-7 text-white/78 sm:text-base">
            这里应该是一个维护工作台，不是信息堆叠页。你可以集中处理头像、昵称、基础档案和账户安全。
          </p>
        </div>

        <div class="grid gap-3 sm:grid-cols-3 lg:w-[26rem] lg:grid-cols-1">
          <div class="rounded-2xl border border-white/10 bg-white/8 p-4">
            <div class="text-xs text-white/62">当前用户</div>
            <div class="mt-3 flex items-center gap-3">
              <UAvatar :src="displayAvatar || undefined" :alt="displayName" size="lg" class="ring-2 ring-white/20" />
              <div class="min-w-0">
                <div class="truncate text-sm font-medium">{{ displayName }}</div>
                <div class="truncate text-xs text-white/62">@{{ profile?.username || 'unknown' }}</div>
              </div>
            </div>
          </div>

          <div class="rounded-2xl border border-white/10 bg-white/8 p-4">
            <div class="text-xs text-white/62">资料完成度</div>
            <div class="mt-3 text-2xl font-semibold">{{ completionCount }}/6</div>
            <div class="mt-1 text-xs text-white/62">头像、昵称、性别、生日、邮箱、手机号</div>
          </div>

          <div class="rounded-2xl border border-white/10 bg-white/8 p-4">
            <div class="text-xs text-white/62">密码强度</div>
            <div class="mt-3 text-2xl font-semibold">{{ strengthSummary }}</div>
            <div class="mt-1 text-xs text-white/62">当前表单会实时评估新密码强度</div>
          </div>
        </div>
      </div>
    </UCard>

    <div class="grid gap-6 xl:grid-cols-[280px_minmax(0,1fr)] xl:items-start">
      <aside class="space-y-4 xl:sticky xl:top-20">
        <UCard>
          <div class="flex flex-col items-center gap-4 text-center">
            <button
              type="button"
              style="width: 5.5rem; height: 5.5rem;"
              class="group relative shrink-0 cursor-pointer overflow-hidden rounded-full ring-4 ring-primary/10"
              :disabled="loading"
              title="点击更换头像"
              @click="triggerAvatarUpload"
            >
              <img
                v-if="displayAvatar"
                :src="displayAvatar"
                alt="头像"
                style="width: 5.5rem; height: 5.5rem;"
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

            <div>
              <div class="font-semibold">{{ displayName }}</div>
              <div class="text-muted mt-1 text-xs">@{{ profile?.username || 'unknown' }}</div>
            </div>

            <div class="w-full space-y-2">
              <div
                v-for="item in profileSummary"
                :key="item.label"
                class="flex items-center justify-between gap-3 rounded-xl bg-elevated/60 px-3 py-2 text-sm"
              >
                <div class="flex items-center gap-2 text-muted">
                  <UIcon :name="item.icon" class="size-4 shrink-0" />
                  <span>{{ item.label }}</span>
                </div>
                <span class="max-w-[10rem] truncate text-right font-medium text-highlighted">{{ item.value }}</span>
              </div>
            </div>

            <p class="text-muted text-xs">点击头像后需保存资料才会正式生效。</p>
          </div>
        </UCard>

        <nav class="overflow-hidden rounded-2xl border border-default bg-default">
          <button
            v-for="item in navItems"
            :key="item.key"
            type="button"
            class="flex w-full items-start gap-3 px-4 py-4 text-left transition-colors"
            :class="activeSection === item.key
              ? 'bg-primary/10 text-primary'
              : 'text-default hover:bg-elevated'"
            @click="activeSection = item.key"
          >
            <UIcon :name="item.icon" class="mt-0.5 size-4 shrink-0" />
            <div>
              <div class="text-sm font-medium">{{ item.label }}</div>
              <div class="mt-1 text-xs" :class="activeSection === item.key ? 'text-primary/80' : 'text-muted'">
                {{ item.description }}
              </div>
            </div>
          </button>
        </nav>
      </aside>

      <div class="min-w-0 space-y-6">
        <UCard v-if="activeSection === 'info'">
          <template #header>
            <div class="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <h2 class="text-lg font-semibold">个人资料</h2>
                <p class="text-muted mt-1 text-sm">维护站点侧的基础身份信息，影响站内展示与账号识别。</p>
              </div>
              <UBadge color="neutral" variant="soft">基础档案</UBadge>
            </div>
          </template>

          <div class="space-y-6">
            <div class="grid grid-cols-1 gap-4 text-sm sm:grid-cols-2 xl:grid-cols-3">
              <div>
                <div class="text-muted">账号</div>
                <div class="font-medium">{{ profile?.username || '-' }}</div>
              </div>
              <div>
                <div class="text-muted">邮箱</div>
                <div class="font-medium">{{ profile?.email || '-' }}</div>
              </div>
              <div>
                <div class="text-muted">手机号</div>
                <div class="font-medium">{{ profile?.phone || '-' }}</div>
              </div>
            </div>

            <USeparator />

            <div class="grid gap-4 lg:grid-cols-2">
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
                  v-model="selectedGender"
                  :items="genderItems"
                  value-key="value"
                  label-key="label"
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

              <UCard class="border-dashed">
                <div class="space-y-2 text-sm">
                  <div class="font-medium text-highlighted">维护提示</div>
                  <p class="text-muted leading-6">昵称会直接影响评论、文章作者名等公开展示；头像变更后需要点击保存资料才会更新到站点资料。</p>
                </div>
              </UCard>
            </div>

            <UAlert
              v-if="profileHint"
              color="info"
              variant="soft"
              :title="profileHint"
            />

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
            <div class="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <h2 class="text-lg font-semibold">修改密码</h2>
                <p class="text-muted mt-1 text-sm">使用更强的密码组合来提升账户安全性。</p>
              </div>
              <UBadge color="error" variant="soft">安全设置</UBadge>
            </div>
          </template>

          <div class="space-y-5">
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

            <div class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_240px] lg:items-start">
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

              <UCard class="border-dashed">
                <div class="space-y-2 text-sm">
                  <div class="font-medium text-highlighted">安全建议</div>
                  <p class="text-muted leading-6">避免和其他站点复用密码。修改成功后系统会要求你重新登录。</p>
                </div>
              </UCard>
            </div>

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
