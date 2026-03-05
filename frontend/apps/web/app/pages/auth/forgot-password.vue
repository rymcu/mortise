<script setup lang="ts">
import * as z from 'zod'
import type { FormSubmitEvent } from '@nuxt/ui'

definePageMeta({
  layout: 'auth'
})

useSeoMeta({
  title: '找回密码',
  description: '通过邮箱验证码重置密码'
})

const config = useRuntimeConfig()

// 步骤：1 - 输入邮箱发送验证码，2 - 输入验证码和新密码
const step = ref<1 | 2>(1)
const sentAccount = ref('')
const countdown = ref(0)
const errorMessage = ref('')

let countdownTimer: ReturnType<typeof setInterval> | null = null

function startCountdown() {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer!)
      countdownTimer = null
    }
  }, 1000)
}

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

// ── Step 1: 发送验证码 ─────────────────────────────────────

const step1Schema = z.object({
  account: z.string().email('请输入有效的邮箱地址')
})

type Step1Schema = z.output<typeof step1Schema>

const step1State = reactive<Step1Schema>({ account: '' })
const step1Loading = ref(false)

async function onStep1Submit(event: FormSubmitEvent<Step1Schema>) {
  errorMessage.value = ''
  step1Loading.value = true
  try {
    const res = await $fetch<{ code: number; message: string; data: boolean }>(
      '/api/v1/app/auth/send-code',
      {
        method: 'POST',
        baseURL: config.public.apiBase as string,
        body: { type: 'email', email: event.data.account }
      }
    )
    if (res.code !== 200) throw new Error(res.message || '发送失败')

    sentAccount.value = event.data.account
    startCountdown()
    step.value = 2
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '发送验证码失败'
  } finally {
    step1Loading.value = false
  }
}

async function resendCode() {
  if (countdown.value > 0) return
  errorMessage.value = ''
  step1Loading.value = true
  try {
    const res = await $fetch<{ code: number; message: string; data: boolean }>(
      '/api/v1/app/auth/send-code',
      {
        method: 'POST',
        baseURL: config.public.apiBase as string,
        body: { type: 'email', email: sentAccount.value }
      }
    )
    if (res.code !== 200) throw new Error(res.message || '发送失败')
    startCountdown()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '重新发送失败'
  } finally {
    step1Loading.value = false
  }
}

// ── Step 2: 验证码 + 新密码 ────────────────────────────────

const step2Schema = z
  .object({
    code: z.string().min(4, '请输入验证码'),
    newPassword: z.string().min(6, '密码至少为 6 个字符'),
    confirmPassword: z.string().min(1, '请确认密码')
  })
  .superRefine(({ newPassword, confirmPassword }, ctx) => {
    if (newPassword !== confirmPassword) {
      ctx.addIssue({
        code: 'custom',
        message: '两次输入的密码不一致',
        path: ['confirmPassword']
      })
    }
  })

type Step2Schema = z.output<typeof step2Schema>

const step2State = reactive<Step2Schema>({
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const step2Loading = ref(false)

async function onStep2Submit(event: FormSubmitEvent<Step2Schema>) {
  errorMessage.value = ''
  step2Loading.value = true
  try {
    const res = await $fetch<{ code: number; message: string; data: boolean }>(
      '/api/v1/app/auth/reset-password',
      {
        method: 'POST',
        baseURL: config.public.apiBase as string,
        body: {
          type: 'email',
          account: sentAccount.value,
          code: event.data.code,
          newPassword: event.data.newPassword
        }
      }
    )
    if (res.code !== 200) throw new Error(res.message || '重置失败')

    await navigateTo('/auth/login')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '密码重置失败'
  } finally {
    step2Loading.value = false
  }
}
</script>

<template>
  <UCard class="mx-auto w-full max-w-sm">
    <!-- 步骤指示器 -->
    <template #header>
      <div class="flex items-center gap-3">
        <UButton
          v-if="step === 2"
          icon="i-lucide-arrow-left"
          color="neutral"
          variant="ghost"
          size="xs"
          aria-label="返回"
          @click="step = 1; errorMessage = ''"
        />
        <div>
          <h1 class="text-lg font-semibold">找回密码</h1>
          <p class="text-muted text-sm">
            {{ step === 1 ? '输入邮箱接收验证码' : `验证码已发送至 ${sentAccount}` }}
          </p>
        </div>
      </div>
    </template>

    <!-- Step 1: 输入邮箱 -->
    <UForm
      v-if="step === 1"
      :schema="step1Schema"
      :state="step1State"
      class="space-y-4"
      @submit="onStep1Submit"
    >
      <UFormField label="邮箱" name="account" required>
        <UInput
          v-model="step1State.account"
          type="text"
          placeholder="请输入注册邮箱"
          class="w-full"
          autocomplete="email"
        />
      </UFormField>

      <UButton
        type="submit"
        block
        :loading="step1Loading"
      >
        发送验证码
      </UButton>

      <UAlert
        v-if="errorMessage"
        color="error"
        variant="soft"
        :title="errorMessage"
      />
    </UForm>

    <!-- Step 2: 验证码 + 新密码 -->
    <UForm
      v-else
      :schema="step2Schema"
      :state="step2State"
      class="space-y-4"
      @submit="onStep2Submit"
    >
      <UFormField label="验证码" name="code" required>
        <div class="flex gap-2">
          <UInput
            v-model="step2State.code"
            placeholder="请输入验证码"
            class="flex-1"
            autocomplete="one-time-code"
          />
          <UButton
            color="neutral"
            variant="outline"
            :disabled="countdown > 0"
            :loading="step1Loading"
            @click="resendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : '重新发送' }}
          </UButton>
        </div>
      </UFormField>

      <UFormField label="新密码" name="newPassword" required>
        <UInput
          v-model="step2State.newPassword"
          type="password"
          placeholder="请设置新密码（至少 6 个字符）"
          class="w-full"
          autocomplete="new-password"
        />
      </UFormField>

      <UFormField label="确认密码" name="confirmPassword" required>
        <UInput
          v-model="step2State.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          class="w-full"
          autocomplete="new-password"
        />
      </UFormField>

      <UButton
        type="submit"
        block
        :loading="step2Loading"
      >
        重置密码
      </UButton>

      <UAlert
        v-if="errorMessage"
        color="error"
        variant="soft"
        :title="errorMessage"
      />
    </UForm>

    <template #footer>
      <div class="text-center text-sm text-gray-500 dark:text-gray-400">
        想起密码了？
        <ULink to="/auth/login" class="text-primary font-medium">返回登录</ULink>
      </div>
    </template>
  </UCard>
</template>
