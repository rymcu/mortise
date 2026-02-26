<script setup lang="ts">
definePageMeta({
  layout: 'auth'
})

const route = useRoute()
const auth = useAuthStore()
const statusText = ref('正在处理登录回调...')
const hasError = ref(false)

onMounted(async () => {
  const state = typeof route.query.state === 'string' ? route.query.state : ''
  if (!state) {
    hasError.value = true
    statusText.value = '缺少 state 参数，无法完成登录。'
    return
  }

  try {
    await auth.exchangeOAuthState(state)
    await navigateTo('/app/profile')
  } catch (error) {
    hasError.value = true
    statusText.value =
      error instanceof Error ? error.message : '登录回调处理失败'
  }
})
</script>

<template>
  <UCard>
    <template #header>
      <h1 class="text-lg font-semibold">OAuth2 回调</h1>
    </template>

    <UAlert
      :color="hasError ? 'error' : 'primary'"
      variant="soft"
      :title="statusText"
    />

    <template v-if="hasError">
      <div class="mt-4">
        <UButton to="/app/auth/login" block>返回登录</UButton>
      </div>
    </template>
  </UCard>
</template>
