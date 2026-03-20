<script setup lang="ts">
const route = useRoute()

const isOnChatPage = computed(() => route.path === '/chat')
</script>

<template>
  <NuxtLink
    v-if="!isOnChatPage"
    to="/chat"
    class="fixed bottom-20 right-6 z-50 flex size-12 items-center justify-center rounded-full bg-primary text-white shadow-lg transition-transform hover:scale-105 active:scale-95"
    aria-label="AI 对话"
  >
    <UIcon name="i-lucide-bot" class="size-5" />
  </NuxtLink>
</template>
