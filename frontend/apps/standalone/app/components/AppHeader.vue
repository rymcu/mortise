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
      avatar: { src: avatarSrc.value, alt: displayName.value },
    },
  ],
  [
    { label: '个人中心', icon: 'i-lucide-user', to: '/profile' },
  ],
  [
    {
      label: '退出登录',
      icon: 'i-lucide-log-out',
      color: 'error' as const,
      onSelect: handleLogout,
    },
  ],
])

// ★ 自定义导航菜单
const items = [
  { label: '首页', to: '/' },
]
</script>

<template>
  <UHeader>
    <template #left>
      <!-- ★ 自定义品牌 Logo 和名称 -->
      <NuxtLink to="/" class="flex items-center gap-2">
        <UIcon name="i-lucide-box" class="size-6 text-primary-500" />
        <span class="font-bold text-lg">Mortise</span>
      </NuxtLink>
    </template>

    <template #right>
      <UNavigationMenu
        :items="items"
        variant="link"
        class="hidden lg:block"
      />

      <!-- 用户状态 -->
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
    </template>
  </UHeader>
</template>
