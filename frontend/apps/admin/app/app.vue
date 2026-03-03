<script setup lang="ts">
const colorMode = useColorMode()

const color = computed(() => (colorMode.value === 'dark' ? '#1b1718' : 'white'))

const { siteName, siteDescription, siteFavicon, seoKeywords, titleTemplate } =
  usePublicSiteConfig()

useHead({
  titleTemplate,
  meta: [
    { charset: 'utf-8' },
    { name: 'viewport', content: 'width=device-width, initial-scale=1' },
    { key: 'theme-color', name: 'theme-color', content: color }
  ],
  link: [{ rel: 'icon', href: siteFavicon }],
  htmlAttrs: {
    lang: 'zh-CN'
  }
})

useSeoMeta({
  title: siteName,
  description: siteDescription,
  keywords: seoKeywords,
  ogTitle: siteName,
  ogDescription: siteDescription
})
</script>

<template>
  <UApp>
    <NuxtLoadingIndicator />

    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>
