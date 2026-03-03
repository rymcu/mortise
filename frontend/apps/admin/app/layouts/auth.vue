<script setup lang="ts">
const { siteName, siteDescription, siteFavicon, seoKeywords } = usePublicSiteConfig()

useHead({
  link: [{ rel: 'icon', href: siteFavicon }]
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
  <UContainer class="flex min-h-screen items-center justify-center py-10">
    <div class="w-full max-w-md">
      <slot />
    </div>
  </UContainer>
</template>
