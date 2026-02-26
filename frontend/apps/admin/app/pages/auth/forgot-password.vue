<script setup lang="ts">
import * as z from 'zod'
import type { FormSubmitEvent } from '@nuxt/ui'
import type { GlobalResult } from '@mortise/core-sdk'

definePageMeta({
  layout: 'auth'
})

useSeoMeta({
  title: '忘记密码',
  description: '通过邮箱验证码重置管理员密码'
})

const { $api } = useNuxtApp()

const requestLoading = ref(false)
const resetLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const requestSchema = z.object({
  email: z.string().min(1, '请输入邮箱').email('请输入有效邮箱地址')
})

const resetSchema = z
  .object({
    code: z.string().min(1, '请输入验证码'),
    password: z.string().min(8, '新密码至少 8 位'),
    passwordConfirm: z.string().min(1, '请再次输入新密码')
  })
  .refine((data) => data.password === data.passwordConfirm, {
    path: ['passwordConfirm'],
    message: '两次输入的新密码不一致'
  })

type RequestSchema = z.output<typeof requestSchema>
type ResetSchema = z.output<typeof resetSchema>

const requestState = reactive<RequestSchema>({
  email: ''
})

const resetState = reactive<ResetSchema>({
  code: '',
  password: '',
  passwordConfirm: ''
})

function getErrorMessage(error: unknown, fallback: string) {
  const maybeData = (error as { data?: { message?: string } })?.data
  const maybeMessage = maybeData?.message
  if (typeof maybeMessage === 'string' && maybeMessage.trim()) {
    return maybeMessage
  }
  return error instanceof Error && error.message ? error.message : fallback
}

async function sendResetCode(_event: FormSubmitEvent<RequestSchema>) {
  errorMessage.value = ''
  successMessage.value = ''
  requestLoading.value = true

  try {
    const res = await $api<GlobalResult<string>>(
      '/api/v1/admin/auth/password/request',
      {
        method: 'GET',
        query: { email: requestState.email },
        skipAuth: true
      }
    )
    successMessage.value = res?.message || '验证码已发送，请查收邮箱。'
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '发送验证码失败')
  } finally {
    requestLoading.value = false
  }
}

async function resetPassword(_event: FormSubmitEvent<ResetSchema>) {
  errorMessage.value = ''
  successMessage.value = ''
  resetLoading.value = true

  try {
    await $api<GlobalResult<boolean>>('/api/v1/admin/auth/password/reset', {
      method: 'PATCH',
      body: {
        code: resetState.code,
        password: resetState.password
      },
      skipAuth: true
    })

    successMessage.value = '密码重置成功，请使用新密码登录。'
    setTimeout(async () => {
      await navigateTo('/auth/login')
    }, 1000)
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '重置密码失败')
  } finally {
    resetLoading.value = false
  }
}
</script>

<template>
  <UCard class="mx-auto w-full max-w-md">
    <div class="mb-5 text-center">
      <div class="pointer-events-none mb-2 flex justify-center">
        <UIcon name="i-lucide-key-round" class="h-8 w-8 shrink-0 text-primary" />
      </div>
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">忘记密码</h1>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
        输入邮箱获取验证码，然后重置登录密码
      </p>
    </div>

    <UAlert
      v-if="errorMessage"
      color="error"
      variant="soft"
      :title="errorMessage"
      class="mb-4"
    />
    <UAlert
      v-if="successMessage"
      color="success"
      variant="soft"
      :title="successMessage"
      class="mb-4"
    />

    <UCard class="mb-4" variant="subtle">
      <template #header>
        <h2 class="text-sm font-semibold">第一步：发送验证码</h2>
      </template>

      <UForm
        :schema="requestSchema"
        :state="requestState"
        class="space-y-3"
        @submit="sendResetCode"
      >
        <UFormField label="邮箱" name="email" required>
          <UInput
            v-model="requestState.email"
            type="email"
            placeholder="请输入注册邮箱"
            class="w-full"
          />
        </UFormField>

        <UButton type="submit" block :loading="requestLoading">
          发送验证码
        </UButton>
      </UForm>
    </UCard>

    <UCard variant="subtle">
      <template #header>
        <h2 class="text-sm font-semibold">第二步：重置密码</h2>
      </template>

      <UForm
        :schema="resetSchema"
        :state="resetState"
        class="space-y-3"
        @submit="resetPassword"
      >
        <UFormField label="验证码" name="code" required>
          <UInput
            v-model="resetState.code"
            placeholder="请输入邮箱收到的验证码"
            class="w-full"
          />
        </UFormField>

        <UFormField label="新密码" name="password" required>
          <UInput
            v-model="resetState.password"
            type="password"
            placeholder="请输入新密码（至少 8 位）"
            class="w-full"
          />
        </UFormField>

        <UFormField label="确认新密码" name="passwordConfirm" required>
          <UInput
            v-model="resetState.passwordConfirm"
            type="password"
            placeholder="请再次输入新密码"
            class="w-full"
          />
        </UFormField>

        <UButton type="submit" block :loading="resetLoading">重置密码</UButton>
      </UForm>
    </UCard>

    <div class="mt-4 text-center text-sm text-gray-500 dark:text-gray-400">
      记起密码了？
      <ULink to="/auth/login" class="text-primary font-medium">返回登录</ULink>
    </div>
  </UCard>
</template>
