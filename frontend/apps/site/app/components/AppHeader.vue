<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui'

const auth = useAuthStore()
const { resolveUrl } = useMediaUrl()

const displayName = computed(() => {
  const u = auth.session?.user
  if (!u) return '用户'
  return (u.nickname as string) || (u.username as string) || '用户'
})

const avatarSrc = computed(() => {
  const u = auth.session?.user
  return resolveUrl(u?.avatarUrl as string | null) ?? undefined
})

async function handleLogout() {
  auth.logout()
  await navigateTo('/')
}

const userMenuItems = computed<DropdownMenuItem[][]>(() => [
  [
    {
      type: 'label' as const,
      label: displayName.value,
      avatar: { src: avatarSrc.value, alt: displayName.value }
    }
  ],
  [
    { label: '个人中心', icon: 'i-lucide-user', to: '/profile' }
  ],
  [
    {
      label: '退出登录',
      icon: 'i-lucide-log-out',
      color: 'error' as const,
      onSelect: handleLogout
    }
  ]
])

const items = [{
  label: '首页',
  to: '/'
}, {
  label: '模块',
  to: '/modules'
}, {
  label: '定价',
  to: '/pricing'
}, {
  label: '博客',
  to: '/blog'
}, {
  label: '文档',
  to: '/docs'
}, {
  label: '更新日志',
  to: '/changelog'
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
        to="/blog"
        variant="subtle"
        class="hidden lg:block"
      />

      <!-- Auth state -->
      <template v-if="auth.isAuthenticated">
        <UDropdownMenu :items="userMenuItems">
          <UButton variant="ghost" color="neutral" class="gap-2">
            <UAvatar :src="avatarSrc" :alt="displayName" size="xs" />
            <span class="hidden sm:inline">{{ displayName }}</span>
            <UIcon name="i-lucide-chevron-down" class="size-4" />
          </UButton>
        </UDropdownMenu>
      </template>
      <template v-else>
        <UButton variant="ghost" color="neutral" to="/auth/login" class="hidden sm:block">
          登录
        </UButton>
        <UButton to="/auth/register" class="hidden sm:block">
          注册
        </UButton>
      </template>

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
        to="/"
        variant="subtle"
        block
      />
    </template>
  </UHeader>
</template>
