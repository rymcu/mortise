<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui'

const auth = useAuthStore()
const router = useRouter()
const colorMode = useColorMode()

/** 从 session.user 中读取显示名称 */
const displayName = computed(() => {
  const u = auth.session?.user
  if (!u) return '未登录'
  return (u.nickname as string) || (u.username as string) || '用户'
})

/** 头像 URL */
const { resolveUrl } = useMediaUrl()
const avatarSrc = computed(() => {
  const u = auth.session?.user
  return resolveUrl(u?.avatarUrl as string | null) ?? undefined
})

async function handleLogout() {
  auth.logout()
  await router.push('/auth/login')
}

const userMenuItems = computed<DropdownMenuItem[][]>(() => [
  [
    {
      type: 'label' as const,
      label: displayName.value,
      avatar: {
        src: avatarSrc.value,
        alt: displayName.value
      }
    }
  ],
  [
    {
      label: '个人中心',
      icon: 'i-lucide-user',
      to: '/profile'
    }
  ],
  [
    {
      label: colorMode.value === 'dark' ? '浅色模式' : '深色模式',
      icon: colorMode.value === 'dark' ? 'i-lucide-sun' : 'i-lucide-moon',
      onSelect() {
        colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
      }
    }
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
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <!-- 顶部导航栏 -->
    <header class="border-default bg-background/80 sticky top-0 z-50 border-b backdrop-blur">
      <UContainer>
        <div class="flex h-14 items-center justify-between">
          <!-- Logo -->
          <NuxtLink to="/" class="text-highlighted flex items-center gap-2 font-bold">
            <UIcon name="i-lucide-sparkles" class="text-primary size-5 shrink-0" />
            <span>Mortise</span>
          </NuxtLink>

          <!-- 右侧操作区 -->
          <div class="flex items-center gap-2">
            <template v-if="auth.isAuthenticated">
              <UDropdownMenu :items="userMenuItems">
                <UButton variant="ghost" color="neutral" class="gap-2">
                  <UAvatar
                    :src="avatarSrc"
                    :alt="displayName"
                    size="xs"
                  />
                  <span class="hidden sm:inline">{{ displayName }}</span>
                  <UIcon name="i-lucide-chevron-down" class="size-4" />
                </UButton>
              </UDropdownMenu>
            </template>
            <template v-else>
              <UButton variant="ghost" color="neutral" to="/auth/login">
                登录
              </UButton>
              <UButton to="/auth/register">
                注册
              </UButton>
            </template>
          </div>
        </div>
      </UContainer>
    </header>

    <!-- 主体内容 -->
    <main class="flex-1">
      <slot />
    </main>
  </div>
</template>
