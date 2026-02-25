<script setup lang="ts">
const auth = useAuthStore()

if (!auth.isAuthenticated) {
  await navigateTo('/app/auth/login')
}
</script>

<template>
  <UContainer class="py-10">
    <UCard>
      <template #header>
        <h1 class="text-xl font-semibold">个人中心</h1>
      </template>

      <p class="mb-3">当前已登录。</p>
      <pre class="text-xs overflow-auto">{{ auth.session }}</pre>

      <div class="mt-4">
        <UButton color="neutral" @click="auth.logout(); navigateTo('/app/auth/login')">
          退出登录
        </UButton>
      </div>
    </UCard>
  </UContainer>
</template>
