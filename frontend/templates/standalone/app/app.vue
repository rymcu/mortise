<script setup lang="ts">
const colorMode = useColorMode()
const color = computed(() => (colorMode.value === 'dark' ? '#1b1718' : 'white'))

const publicSiteConfig = usePublicSiteConfig()

if (import.meta.server && publicSiteConfig.pending.value) {
  await publicSiteConfig.refresh()
}

const { siteName, siteDescription, siteFavicon, seoKeywords, titleTemplate } =
  publicSiteConfig

useHead(() => ({
  titleTemplate: titleTemplate.value,
  link: [{ rel: 'icon', href: siteFavicon.value }],
  meta: [
    { charset: 'utf-8' },
    { name: 'viewport', content: 'width=device-width, initial-scale=1' },
    { key: 'theme-color', name: 'theme-color', content: color.value }
  ],
  htmlAttrs: { lang: 'zh-CN' }
}))

useSeoMeta({
  title: siteName.value,
  description: siteDescription.value,
  keywords: seoKeywords.value,
  ogTitle: siteName.value,
  ogDescription: siteDescription.value
})
</script>

<template>
  <UApp>
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>
