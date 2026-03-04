<script setup lang="ts">
import { useSiteConfig } from '~/composables/useSiteConfig'

const { siteConfig } = useSiteConfig()
</script>

<template>
  <span class="font-bold text-xl text-primary flex items-center gap-1.5">
    <img
      v-if="siteConfig?.logoUrl"
      :src="siteConfig.logoUrl"
      :alt="siteConfig.siteName"
      class="size-6 shrink-0 object-contain"
    />
    <UIcon
      v-else
      name="i-lucide-sparkles"
      class="text-primary size-6 shrink-0"
    />
    <span>{{ siteConfig?.siteName || 'Mortise' }}</span>
  </span>
</template>
