<script setup lang="ts">
definePageMeta({
  layout: 'auth'
})
</script>

<template>
  <UCard>
    <template #header>
      <h1 class="text-xl font-semibold">用户注册</h1>
    </template>
    <p>注册流程将在下一阶段接入 /api/v1/app/auth/register。</p>
    <div class="mt-4">
      <UButton to="/app/auth/login" block>返回登录</UButton>
    </div>
  </UCard>
</template>
