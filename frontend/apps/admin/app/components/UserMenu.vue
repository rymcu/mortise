<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui'

defineProps<{
  collapsed?: boolean
}>()

const auth = useAuthStore()
const colorMode = useColorMode()
const appConfig = useAppConfig()
const router = useRouter()

const colors = [
  'red', 'orange', 'amber', 'yellow', 'lime', 'green',
  'emerald', 'teal', 'cyan', 'sky', 'blue', 'indigo',
  'violet', 'purple', 'fuchsia', 'pink', 'rose'
]
const neutrals = ['slate', 'gray', 'zinc', 'neutral', 'stone']

/** 从 session.user 中读取显示名称 */
const displayName = computed(() => {
  const u = auth.session?.user
  if (!u) return '未登录'
  return (u.nickname as string) || (u.account as string) || '管理员'
})

/** 头像 URL（后端可扩展） */
const { resolveUrl } = useMediaUrl()
const avatarSrc = computed(() => {
  const u = auth.session?.user
  return resolveUrl(u?.avatar as string | null) ?? undefined
})

/** 头像文字 fallback */
const avatarAlt = computed(() => displayName.value)

async function handleLogout() {
  auth.logout()
  await router.push('/auth/login')
}

const items = computed<DropdownMenuItem[][]>(() => [
  [
    {
      type: 'label',
      label: displayName.value,
      avatar: {
        src: avatarSrc.value,
        alt: avatarAlt.value
      }
    }
  ],
  [
    {
      label: '个人中心',
      icon: 'i-lucide-user',
      to: '/settings/profile'
    },
    {
      label: '账号设置',
      icon: 'i-lucide-settings',
      to: '/settings'
    }
  ],
  [
    {
      label: '主题',
      icon: 'i-lucide-palette',
      children: [
        {
          label: '主色',
          slot: 'chip',
          chip: appConfig.ui.colors.primary,
          content: { align: 'center', collisionPadding: 16 },
          children: colors.map((color) => ({
            label: color,
            chip: color,
            slot: 'chip',
            checked: appConfig.ui.colors.primary === color,
            type: 'checkbox',
            onSelect(e: Event) {
              e.preventDefault()
              appConfig.ui.colors.primary = color
            }
          }))
        },
        {
          label: '中性色',
          slot: 'chip',
          chip:
            appConfig.ui.colors.neutral === 'neutral'
              ? 'old-neutral'
              : appConfig.ui.colors.neutral,
          content: { align: 'end', collisionPadding: 16 },
          children: neutrals.map((color) => ({
            label: color,
            chip: color === 'neutral' ? 'old-neutral' : color,
            slot: 'chip',
            type: 'checkbox',
            checked: appConfig.ui.colors.neutral === color,
            onSelect(e: Event) {
              e.preventDefault()
              appConfig.ui.colors.neutral = color
            }
          }))
        }
      ]
    },
    {
      label: '外观',
      icon: 'i-lucide-sun-moon',
      children: [
        {
          label: '浅色',
          icon: 'i-lucide-sun',
          type: 'checkbox',
          checked: colorMode.value === 'light',
          onSelect(e: Event) {
            e.preventDefault()
            colorMode.preference = 'light'
          }
        },
        {
          label: '深色',
          icon: 'i-lucide-moon',
          type: 'checkbox',
          checked: colorMode.value === 'dark',
          onUpdateChecked(checked: boolean) {
            if (checked) colorMode.preference = 'dark'
          },
          onSelect(e: Event) {
            e.preventDefault()
          }
        },
        {
          label: '跟随系统',
          icon: 'i-lucide-monitor',
          type: 'checkbox',
          checked: colorMode.value === 'system',
          onSelect(e: Event) {
            e.preventDefault()
            colorMode.preference = 'system'
          }
        }
      ]
    }
  ],
  [
    {
      label: '退出登录',
      icon: 'i-lucide-log-out',
      onSelect: handleLogout
    }
  ]
])
</script>

<template>
  <UDropdownMenu
    :items="items"
    :content="{ align: 'center', collisionPadding: 12 }"
    :ui="{
      content: collapsed ? 'w-48' : 'w-(--reka-dropdown-menu-trigger-width)'
    }"
  >
    <UButton
      v-bind="{
        avatar: { src: avatarSrc, alt: avatarAlt },
        label: collapsed ? undefined : displayName,
        trailingIcon: collapsed ? undefined : 'i-lucide-chevrons-up-down'
      }"
      color="neutral"
      variant="ghost"
      block
      :square="collapsed"
      class="data-[state=open]:bg-elevated"
      :ui="{ trailingIcon: 'text-dimmed' }"
    />

    <template #chip-leading="{ item }">
      <div class="inline-flex items-center justify-center shrink-0 size-5">
        <span
          class="rounded-full ring ring-bg bg-(--chip-light) dark:bg-(--chip-dark) size-2"
          :style="{
            '--chip-light': `var(--color-${(item as any).chip}-500)`,
            '--chip-dark': `var(--color-${(item as any).chip}-400)`
          }"
        />
      </div>
    </template>
  </UDropdownMenu>
</template>
