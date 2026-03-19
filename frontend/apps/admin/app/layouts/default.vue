<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'
import type { MenuLink } from '~/stores/auth'

const auth = useAuthStore()
const open = ref(false)
const route = useRoute()

const { siteName, siteLogo } = usePublicSiteConfig()

/** 从菜单树中递归查找与当前路径精确匹配的菜单项标签 */
function findMenuLabel(links: MenuLink[], path: string): string | undefined {
  for (const link of links) {
    if (link.to === path) return link.label
    if (link.children?.length) {
      const found = findMenuLabel(link.children, path)
      if (found) return found
    }
  }
  return undefined
}

/** 当前页面标题，随路由变化自动更新 */
const currentPageTitle = computed(() =>
  findMenuLabel(auth.userMenus ?? [], route.path)
)

useHead({ title: currentPageTitle })

// sessionRestore.client.ts 插件在页面刷新/新标签页时已阻塞等待数据加载完成。
// 此处 onMounted 仅兜底：SPA 内登录成功后首次进入 layout，无页面刷新的场景。
onMounted(async () => {
  if (auth.isAuthenticated && (auth.userMenus ?? []).length === 0) {
    await auth.restoreSession()
  }
})

/** 将后端 MenuLink 树递归转换为 NavigationMenuItem */
function toNavItems(links: MenuLink[]): NavigationMenuItem[] {
  return links
    .filter((l) => l.status === 1 || l.status === undefined) // 只展示启用状态的菜单
    .sort((a, b) => (a.sortNo ?? 0) - (b.sortNo ?? 0))
    .map((link) => {
      const hasChildren = Boolean(link.children?.length)
      const item: NavigationMenuItem = {
        label: link.label,
        icon: link.icon || undefined,
        // 有子菜单时不设置 to，阻止点击跳转，仅展开/折叠
        to: hasChildren ? undefined : link.to || undefined,
        defaultOpen: link.defaultOpen ?? undefined,
        onSelect:
          !hasChildren && link.to
            ? () => {
                open.value = false
              }
            : undefined
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
  return [
    {
      label: 'Dashboard',
      icon: 'i-lucide-house',
      to: '/dashboard',
      onSelect: () => {
        open.value = false
      }
    }
  ]
})


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
      <template #header="{ collapsed }">
        <div
            class="flex w-full items-center"
            :class="collapsed ? 'justify-center' : 'justify-between'"
        >
          <NuxtLink
              to="/"
              class="text-highlighted flex items-end gap-1.5"
              @click="open = false"
          >
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
            <span v-if="!collapsed" class="text-lg font-bold">{{ siteName }}</span>
          </NuxtLink>
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

      <template #footer="{ collapsed }">
        <UserMenu :collapsed="collapsed" />
      </template>
    </UDashboardSidebar>

    <slot />
  </UDashboardGroup>
</template>
