<script setup lang="ts">
definePageMeta({
  layout: 'auth'
})

useSeoMeta({
  title: '服务条款',
  description: 'Mortise 管理端服务条款'
})

const sections = [
  {
    title: '1. 服务说明',
    content:
      'Mortise 管理端用于系统管理与运维配置。您应仅在合法授权范围内使用本系统。'
  },
  {
    title: '2. 账号责任',
    content:
      '您需妥善保管账号和密码，不得将账号借予他人。因账号管理不当导致的风险由账号持有人承担。'
  },
  {
    title: '3. 数据与安全',
    content:
      '您承诺不进行未授权访问、数据篡改或破坏系统稳定性的操作。系统可能记录必要的审计日志用于安全追溯。'
  },
  {
    title: '4. 合规与限制',
    content:
      '您不得利用本系统从事违法违规活动，不得传播恶意内容或进行任何损害平台与第三方权益的行为。'
  },
  {
    title: '5. 条款更新',
    content:
      '服务条款可能根据产品迭代与合规要求进行更新。继续使用系统即视为您接受更新后的条款。'
  }
]
</script>

<template>
  <UCard class="mx-auto w-full max-w-md">
    <template #header>
      <div class="text-center">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">服务条款</h1>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
          请在使用前仔细阅读以下内容
        </p>
      </div>
    </template>

    <div class="max-h-[60vh] space-y-4 overflow-y-auto pr-1 text-sm leading-6">
      <section v-for="item in sections" :key="item.title">
        <h2 class="mb-1 font-semibold text-gray-900 dark:text-white">
          {{ item.title }}
        </h2>
        <p class="text-gray-600 dark:text-gray-300">{{ item.content }}</p>
      </section>
    </div>

    <template #footer>
      <div class="flex justify-end">
        <UButton to="/auth/login" variant="soft">返回登录</UButton>
      </div>
    </template>
  </UCard>
</template>
