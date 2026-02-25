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

const schema = z.object({
  account: z.string().min(1, '请输入账号'),
  password: z.string().min(6, '密码至少为 6 个字符'),
  remember: z.boolean().optional()
})

type Schema = z.output<typeof schema>

const state = reactive<Schema>({
  account: '',
  password: '',
  remember: false
})

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
    errorMessage.value = error instanceof Error ? error.message : 'OAuth2 登录失败'
  }
}
</script>

<template>
  <UCard class="w-full max-w-sm mx-auto">
    <div class="text-center mb-6">
      <div class="mb-2 pointer-events-none flex justify-center">
        <UIcon name="i-lucide-lock" class="w-8 h-8 shrink-0 text-gray-900 dark:text-white" />
      </div>
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
        欢迎回来
      </h1>
      <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
        管理端登录
      </p>
    </div>

    <UForm
      :schema="schema"
      :state="state"
      class="space-y-4"
      @submit="onSubmit"
    >
      <UFormField label="账号" name="account" required>
        <UInput v-model="state.account" placeholder="请输入账号" class="w-full" />
      </UFormField>

      <UFormField label="密码" name="password" required>
        <template #hint>
          <ULink to="#" class="text-primary font-medium" tabindex="-1">忘记密码？</ULink>
        </template>
        <UInput
          v-model="state.password"
          type="password"
          placeholder="请输入密码"
          class="w-full"
        />
      </UFormField>

      <UFormField name="remember">
        <UCheckbox v-model="state.remember" label="记住我" />
      </UFormField>

      <UButton type="submit" block :loading="auth.loading">
        登录
      </UButton>

      <USeparator label="或" class="my-4" />

      <div class="space-y-2">
        <UButton
          color="neutral"
          variant="soft"
          block
          icon="i-lucide-shield"
          @click="loginWithOAuth('logto-admin')"
        >
          使用 Logto 登录
        </UButton>
        <UButton
          color="neutral"
          variant="soft"
          block
          icon="i-simple-icons-github"
          @click="loginWithOAuth('github-app')"
        >
          使用 GitHub 登录
        </UButton>
      </div>

      <UAlert
        v-if="errorMessage"
        color="error"
        variant="soft"
        :title="errorMessage"
        class="mt-4"
      />
    </UForm>

    <template #footer>
      <div class="text-center text-sm text-gray-500 dark:text-gray-400">
        登录即表示您同意我们的 <ULink to="#" class="text-primary font-medium">服务条款</ULink>。
      </div>
    </template>
  </UCard>
</template>
