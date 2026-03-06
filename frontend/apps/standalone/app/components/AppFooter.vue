<script setup lang="ts">
</script>

<template>
  <USeparator class="h-px" />

  <UFooter>
    <template #left>
      <!-- ★ 自定义版权信息 -->
      <p class="text-sm text-muted">
        Mortise • © {{ new Date().getFullYear() }} • MIT License
      </p>
    </template>

    <template #right>
      <UButton
        to="https://github.com/rymcu/mortise"
        target="_blank"
        icon="i-simple-icons-github"
        color="neutral"
        variant="ghost"
        aria-label="GitHub"
      />
    </template>
  </UFooter>
</template>
