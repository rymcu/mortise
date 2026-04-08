<script setup lang="ts">
import type { ProfileNavItem, ProfileSection } from '../types/profile'

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
const { resolveUrl } = useMediaUrl()

const profileState = reactive({ nickname: '', gender: '', birthDate: '' })
const pendingAvatar = ref<string | null>(null)
const avatarPreview = ref<string | null>(null)
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
    profileState.nickname.trim(), profileState.gender, profileState.birthDate,
    profile.value?.email, profile.value?.phone, displayAvatar.value,
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

async function onAvatarFileChange(file: File | null | undefined) {
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
}

function clearPendingAvatar() {
  avatarPreview.value = null
  pendingAvatar.value = null
  profileHint.value = ''
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
      <ProfileSidebar
        :display-avatar="displayAvatar"
        :display-name="displayName"
        :username="profile?.username || 'unknown'"
        :active-section="activeSection"
        :loading="loading"
        :profile-summary="profileSummary"
        :pending-avatar="pendingAvatar"
        :nav-items="navItems"
        @update:active-section="activeSection = $event"
        @avatar-change="onAvatarFileChange"
        @avatar-clear="clearPendingAvatar()"
      />

      <div class="min-w-0 space-y-6">
        <ProfileEditForm
          v-if="activeSection === 'info'"
          :nickname="profileState.nickname"
          :gender="selectedGender"
          :birth-date="profileState.birthDate"
          :gender-items="genderItems"
          :loading="loading"
          :can-save="canSaveProfile"
          :hint="profileHint"
          :success="profileSuccess"
          :error="activeSection === 'info' ? (error || '') : ''"
          :display-username="profile?.username || '-'"
          :display-email="profile?.email || '-'"
          :display-phone="profile?.phone || '-'"
          @update:nickname="profileState.nickname = $event"
          @update:gender="selectedGender = $event"
          @update:birth-date="profileState.birthDate = $event"
          @submit="onProfileSubmit"
        />

        <ProfilePasswordForm
          v-else-if="activeSection === 'security'"
          :current-password="passwordState.currentPassword"
          :new-password="passwordState.newPassword"
          :confirm-password="passwordState.confirmPassword"
          :show-current="show.current"
          :show-new="show.new"
          :show-confirm="show.confirm"
          :loading="loading"
          :score="score"
          :strength="strength"
          :color="color"
          :strength-text="strengthText"
          :confirm-matched="confirmMatched"
          :can-change-password="canChangePassword"
          :error="passwordError"
          :success="passwordSuccess"
          @update:current-password="passwordState.currentPassword = $event"
          @update:new-password="passwordState.newPassword = $event"
          @update:confirm-password="passwordState.confirmPassword = $event"
          @update:show-current="show.current = $event"
          @update:show-new="show.new = $event"
          @update:show-confirm="show.confirm = $event"
          @submit="onPasswordSubmit"
        />
      </div>
    </div>
  </UContainer>
</template>
