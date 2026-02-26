<script setup lang="ts">
import * as z from 'zod'
import type { FormSubmitEvent } from '@nuxt/ui'

definePageMeta({
  layout: 'auth'
})

useSeoMeta({
  title: '管理端登录',
  description: '登录您的账号以继续'
})

const auth = useAuthStore()
const errorMessage = ref('')

const fields = [
  {
    name: 'account',
    type: 'text' as const,
    label: '账号',
    placeholder: '请输入账号',
    required: true
  },
  {
    name: 'password',
    type: 'password' as const,
    label: '密码',
    placeholder: '请输入密码',
    required: true
  },
  {
    name: 'remember',
    type: 'checkbox' as const,
    label: '记住我'
  }
]

const providers = [
  {
    label: '使用 Logto 登录',
    icon: 'i-lucide-shield',
    onClick: () => loginWithOAuth('logto-admin')
  },
  {
    label: '使用 GitHub 登录',
    icon: 'i-simple-icons-github',
    onClick: () => loginWithOAuth('github-app')
  }
]

const schema = z.object({
  account: z.string().min(1, '请输入账号'),
  password: z.string().min(6, '密码至少为 6 个字符'),
  remember: z.boolean().optional()
})

type Schema = z.output<typeof schema>

async function onSubmit(event: FormSubmitEvent<Schema>) {
  errorMessage.value = ''
  try {
    await auth.login(event.data.account, event.data.password)
    await navigateTo('/dashboard')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败'
  }
}

async function loginWithOAuth(registrationId: string) {
  errorMessage.value = ''
  try {
    await auth.startOAuthLogin(registrationId)
  } catch (error) {
    errorMessage.value =
      error instanceof Error ? error.message : 'OAuth2 登录失败'
  }
}
</script>

<template>
  <UAuthForm
    :fields="fields"
    :schema="schema"
    :providers="providers"
    title="欢迎回来"
    icon="i-lucide-lock"
    :submit="{ label: '登录', loading: auth.loading }"
    @submit="onSubmit"
  >
    <template #password-hint>
      <ULink to="/auth/forgot-password" class="text-primary font-medium"
        >忘记密码？</ULink
      >
    </template>

    <template #footer>
      <div class="space-y-2 text-sm text-gray-500 dark:text-gray-400">
        <p>
          登录即表示您同意我们的
          <ULink to="/terms/service" class="text-primary font-medium"
            >服务条款</ULink
          >。
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
