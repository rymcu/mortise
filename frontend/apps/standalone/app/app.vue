<script setup lang="ts">
const colorMode = useColorMode()
const color = computed(() => (colorMode.value === 'dark' ? '#1b1718' : 'white'))

// ★ 修改应用名称和描述
const APP_NAME = 'Mortise'
const APP_DESC = 'Mortise 独立应用'

useHead({
  titleTemplate: `%s - ${APP_NAME}`,
  meta: [
    { charset: 'utf-8' },
    { name: 'viewport', content: 'width=device-width, initial-scale=1' },
    { key: 'theme-color', name: 'theme-color', content: color },
  ],
  htmlAttrs: { lang: 'zh-CN' },
})

useSeoMeta({
  title: APP_NAME,
  description: APP_DESC,
  ogTitle: APP_NAME,
  ogDescription: APP_DESC,
})
</script>

<template>
  <UApp>
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>
