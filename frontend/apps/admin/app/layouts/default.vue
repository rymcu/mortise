<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'
import type { MenuLink } from '~/stores/auth'

const auth = useAuthStore()
const open = ref(false)

// 登录后自动加载菜单
onMounted(async () => {
  if (auth.isAuthenticated && (auth.userMenus ?? []).length === 0) {
    await auth.fetchMenus()
  }
})

/** 将后端 MenuLink 树递归转换为 NavigationMenuItem */
function toNavItems(links: MenuLink[]): NavigationMenuItem[] {
  return links
    .filter(l => l.status === 0 || l.status === undefined) // 只展示启用状态的菜单
    .sort((a, b) => (a.sortNo ?? 0) - (b.sortNo ?? 0))
    .map((link) => {
      const hasChildren = Boolean(link.children?.length)
      const item: NavigationMenuItem = {
        label: link.label,
        icon: link.icon || undefined,
        // 有子菜单时不设置 to，阻止点击跳转，仅展开/折叠
        to: hasChildren ? undefined : (link.to || undefined),
        defaultOpen: link.defaultOpen ?? undefined,
        onSelect: (!hasChildren && link.to) ? () => { open.value = false } : undefined
      }
      if (hasChildren) {
        item.children = toNavItems(link.children!)
      }
      return item
    })
}

/** 动态菜单 — 如果菜单尚未加载则使用默认菜单 */
const navItems = computed<NavigationMenuItem[]>(() => {
  const menus = auth.userMenus ?? []
  if (menus.length > 0) {
    return toNavItems(menus)
  }
  // 降级：菜单未加载时显示默认菜单
  return [{
    label: 'Dashboard',
    icon: 'i-lucide-house',
    to: '/dashboard',
    onSelect: () => { open.value = false }
  }]
})

async function logout() {
  auth.logout()
  await navigateTo('/auth/login')
}
</script>

<template>
  <UDashboardGroup unit="rem">
    <UDashboardSidebar
      id="default"
      v-model:open="open"
      collapsible
      resizable
      class="bg-elevated/25"
      :ui="{ footer: 'lg:border-t lg:border-default' }"
    >
      <template #header>
        <div class="px-2 py-1 text-sm font-semibold text-highlighted">
          Mortise Admin
        </div>
      </template>

      <template #default="{ collapsed }">
        <UNavigationMenu
          :collapsed="collapsed"
          :items="navItems"
          orientation="vertical"
          tooltip
          popover
        />
      </template>

      <template #footer>
        <UButton
          color="neutral"
          variant="ghost"
          icon="i-lucide-log-out"
          class="w-full justify-start"
          @click="logout"
        >
          退出登录
        </UButton>
      </template>
    </UDashboardSidebar>

    <slot />
  </UDashboardGroup>
</template>
