<script setup lang="ts">
import * as z from 'zod'
import type { FormSubmitEvent } from '@nuxt/ui'
import type { SystemInitInfo } from '~/types/system-init'

definePageMeta({
  layout: 'auth'
})

useSeoMeta({
  title: '系统初始化',
  description: '首次使用请完成系统初始化配置'
})

const { checkInitStatus, initializeSystem, getInitProgress } = useSystemInit()

// 步骤状态
const currentStep = ref(0)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const progress = ref(0)
const isInitializing = ref(false)
const showAdminPassword = ref(false)
const showAdminPasswordConfirm = ref(false)
let progressTimer: ReturnType<typeof setInterval> | null = null

// 检查是否已初始化
const initialized = ref(false)
onMounted(async () => {
  try {
    initialized.value = await checkInitStatus()
    if (initialized.value) {
      await navigateTo('/auth/login')
    }
  } catch {
    // 接口不可用时允许继续显示表单
  }
})

const steps = [
  { label: '系统信息', icon: 'i-lucide-settings' },
  { label: '管理员账号', icon: 'i-lucide-user' },
  { label: '确认初始化', icon: 'i-lucide-check-circle' }
]

// 表单校验
const systemSchema = z.object({
  systemName: z
    .string()
    .min(1, '请输入系统名称')
    .max(50, '系统名称最多 50 个字符'),
  systemDescription: z.string().max(200, '系统描述最多 200 个字符').optional()
})

const adminSchema = z
  .object({
    adminNickname: z
      .string()
      .min(1, '请输入管理员昵称')
      .max(30, '昵称最多 30 个字符'),
    adminEmail: z
      .string()
      .min(1, '请输入管理员邮箱')
      .email('请输入有效的邮箱地址'),
    adminPassword: z
      .string()
      .min(8, '密码至少 8 个字符')
      .max(32, '密码最多 32 个字符'),
    adminPasswordConfirm: z.string().min(1, '请确认密码')
  })
  .refine((data) => data.adminPassword === data.adminPasswordConfirm, {
    message: '两次输入的密码不一致',
    path: ['adminPasswordConfirm']
  })

type SystemSchema = z.output<typeof systemSchema>
type AdminSchema = z.output<typeof adminSchema>

const systemState = reactive<SystemSchema>({
  systemName: 'Mortise',
  systemDescription: ''
})

const adminState = reactive<AdminSchema>({
  adminNickname: '系统管理员',
  adminEmail: '',
  adminPassword: '',
  adminPasswordConfirm: ''
})

function nextStep() {
  errorMessage.value = ''
  currentStep.value++
}

function prevStep() {
  errorMessage.value = ''
  currentStep.value--
}

function onSystemSubmit(_event: FormSubmitEvent<SystemSchema>) {
  nextStep()
}

function onAdminSubmit(_event: FormSubmitEvent<AdminSchema>) {
  nextStep()
}

// 轮询初始化进度
function startProgressPolling() {
  progressTimer = setInterval(async () => {
    try {
      progress.value = await getInitProgress()
      if (progress.value >= 100) {
        stopProgressPolling()
      }
    } catch {
      // 忽略进度查询错误
    }
  }, 1000)
}

function stopProgressPolling() {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

onUnmounted(() => {
  stopProgressPolling()
})

async function doInitialize() {
  loading.value = true
  errorMessage.value = ''
  isInitializing.value = true
  progress.value = 0

  startProgressPolling()

  try {
    const initInfo: SystemInitInfo = {
      adminPassword: adminState.adminPassword,
      adminNickname: adminState.adminNickname,
      adminEmail: adminState.adminEmail,
      systemName: systemState.systemName,
      systemDescription: systemState.systemDescription
    }

    await initializeSystem(initInfo)
    stopProgressPolling()
    progress.value = 100
    successMessage.value = '系统初始化成功！即将跳转到登录页面...'

    setTimeout(async () => {
      await navigateTo('/auth/login')
    }, 2000)
  } catch (error) {
    stopProgressPolling()
    errorMessage.value =
      error instanceof Error ? error.message : '系统初始化失败'
    isInitializing.value = false
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <UCard class="mx-auto w-full max-w-lg">
    <div class="mb-6 text-center">
      <div class="pointer-events-none mb-2 flex justify-center">
        <UIcon name="i-lucide-rocket" class="text-primary h-8 w-8 shrink-0" />
      </div>
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
        系统初始化
      </h1>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
        首次使用请完成以下配置
      </p>
    </div>

    <!-- 步骤指示器 -->
    <div class="mb-8 flex items-center justify-center">
      <div
        v-for="(step, index) in steps"
        :key="index"
        class="flex items-center"
      >
        <div
          class="flex items-center gap-2 rounded-full px-3 py-1.5 text-sm transition-colors"
          :class="[
            index === currentStep
              ? 'bg-primary/10 text-primary font-medium'
              : index < currentStep
                ? 'text-primary'
                : 'text-gray-400 dark:text-gray-500'
          ]"
        >
          <UIcon
            :name="index < currentStep ? 'i-lucide-check-circle' : step.icon"
            class="h-4 w-4 shrink-0"
          />
          <span class="hidden sm:inline">{{ step.label }}</span>
        </div>
        <UIcon
          v-if="index < steps.length - 1"
          name="i-lucide-chevron-right"
          class="mx-1 h-4 w-4 shrink-0 text-gray-300 dark:text-gray-600"
        />
      </div>
    </div>

    <!-- 步骤 1: 系统信息 -->
    <UForm
      v-if="currentStep === 0"
      :schema="systemSchema"
      :state="systemState"
      class="space-y-4"
      @submit="onSystemSubmit"
    >
      <UFormField label="系统名称" name="systemName" required>
        <UInput
          v-model="systemState.systemName"
          placeholder="请输入系统名称"
          class="w-full"
        />
      </UFormField>

      <UFormField label="系统描述" name="systemDescription">
        <UTextarea
          v-model="systemState.systemDescription"
          placeholder="请输入系统描述（选填）"
          class="w-full"
          :rows="3"
        />
      </UFormField>

      <div class="flex justify-end pt-2">
        <UButton type="submit">
          下一步
          <template #trailing>
            <UIcon name="i-lucide-arrow-right" />
          </template>
        </UButton>
      </div>
    </UForm>

    <!-- 步骤 2: 管理员账号 -->
    <SetupAdminForm
      v-if="currentStep === 1"
      v-model:admin-state="adminState"
      v-model:show-admin-password="showAdminPassword"
      v-model:show-admin-password-confirm="showAdminPasswordConfirm"
      :admin-schema="adminSchema"
      @submit="onAdminSubmit"
      @prev="prevStep"
    />

    <!-- 步骤 3: 确认初始化 -->
    <SetupConfirmStep
      v-if="currentStep === 2"
      :system-state="systemState"
      :admin-state="adminState"
      :is-initializing="isInitializing"
      :progress="progress"
      :success-message="successMessage"
      :error-message="errorMessage"
      :loading="loading"
      @prev="prevStep"
      @initialize="doInitialize"
    />
  </UCard>
</template>
