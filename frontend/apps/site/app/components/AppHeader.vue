<script setup lang="ts">
const items = [{
  label: '首页',
  to: '/'
}, {
  label: '社区',
  to: '/articles'
}, {
  label: '文档',
  to: '/docs'
}, {
  label: '下载',
  to: '/download'
}, {
  label: '关于',
  to: '/about'
}]
</script>

<template>
  <UHeader>
    <template #left>
      <NuxtLink to="/">
        <AppLogo class="shrink-0" />
      </NuxtLink>
    </template>

    <template #right>
      <UNavigationMenu
        :items="items"
        variant="link"
        class="hidden lg:block"
      />

      <UButton
        label="进入社区"
        to="/articles"
        variant="subtle"
        class="hidden lg:block"
      />

      <UColorModeButton />
    </template>

    <template #body>
      <UNavigationMenu
        :items="items"
        orientation="vertical"
        class="-mx-2.5"
      />
      <UButton
        class="mt-4"
        label="进入社区"
        to="/articles"
        variant="subtle"
        block
      />
    </template>
  </UHeader>
</template>
