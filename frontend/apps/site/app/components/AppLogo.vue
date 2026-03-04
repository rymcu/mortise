<script setup lang="ts">
const { siteName, siteLogo } = useSiteConfig()
</script>

<template>
  <span class="font-bold text-xl text-primary flex items-center gap-1.5">
    <img
      v-if="siteLogo"
      :src="siteLogo"
      :alt="siteName"
      class="size-6 shrink-0 object-contain"
    />
    <UIcon
      v-else
      name="i-lucide-sparkles"
      class="text-primary size-6 shrink-0"
    />
    <span>{{ siteName }}</span>
  </span>
</template>
