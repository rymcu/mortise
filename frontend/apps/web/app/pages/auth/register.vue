<script setup lang="ts">
import * as z from 'zod'
import type { FormSubmitEvent } from '@nuxt/ui'

definePageMeta({
  layout: 'auth'
})

useSeoMeta({
  title: '用户注册',
  description: '创建您的账号'
})

const config = useRuntimeConfig()
const errorMessage = ref('')

const fields = [
  {
    name: 'username',
    type: 'text' as const,
    label: '用户名',
    placeholder: '请设置用户名（8-20 位字母、数字或下划线）',
    required: true
  },
  {
    name: 'email',
    type: 'text' as const,
    label: '邮箱',
    placeholder: '请输入邮箱地址',
    required: true
  },
  {
    name: 'password',
    type: 'password' as const,
    label: '密码',
    placeholder: '请设置密码（至少 6 个字符）',
    required: true
  },
  {
    name: 'confirmPassword',
    type: 'password' as const,
    label: '确认密码',
    placeholder: '请再次输入密码',
    required: true
  }
]

const schema = z
  .object({
    username: z
      .string()
      .min(8, '用户名至少为 8 个字符')
      .max(20, '用户名最多为 20 个字符')
      .regex(/^[a-zA-Z0-9_]+$/, '用户名只能包含字母、数字和下划线'),
    email: z.string().email('请输入有效的邮箱地址'),
    password: z.string().min(6, '密码至少为 6 个字符'),
    confirmPassword: z.string().min(1, '请确认密码')
  })
  .superRefine(({ password, confirmPassword }, ctx) => {
    if (password !== confirmPassword) {
      ctx.addIssue({
        code: 'custom',
        message: '两次输入的密码不一致',
        path: ['confirmPassword']
      })
    }
  })

type Schema = z.output<typeof schema>

async function apiPost<T>(path: string, body: unknown): Promise<T> {
  const res = await $fetch<{ code: number; message: string; data: T }>(path, {
    method: 'POST',
    baseURL: config.public.apiBase as string,
    body
  })
  if (res.code !== 200) {
    throw new Error(res.message || '操作失败')
  }
  return res.data
}

async function onSubmit(event: FormSubmitEvent<Schema>) {
  errorMessage.value = ''
  try {
    await apiPost('/api/v1/app/auth/register', {
      username: event.data.username,
      email: event.data.email,
      password: event.data.password
    })
    await navigateTo('/auth/login')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '注册失败'
  }
}
</script>

<template>
  <UAuthForm
    :fields="fields"
    :schema="schema"
    title="创建账号"
    icon="i-lucide-user-plus"
    :submit="{ label: '注册' }"
    @submit="onSubmit"
  >
    <template #description>
      已有账号？
      <ULink to="/auth/login" class="text-primary font-medium">登录</ULink>
    </template>

    <template #footer>
      <div class="space-y-2 text-sm text-gray-500 dark:text-gray-400">
        <p>
          注册即表示您同意我们的
          <ULink to="#" class="text-primary font-medium">服务条款</ULink>。
        </p>
        <UAlert
          v-if="errorMessage"
          color="error"
          variant="soft"
          :title="errorMessage"
        />
      </div>
    </template>
  </UAuthForm>
</template>
