<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui'
import type { AppProduct, AppProductCategory } from '@mortise/core-sdk'
import type { ProductMenuSection } from '../types/product-menu'
import { chunkItems } from '~/utils/chunk'

const route = useRoute()
const auth = useAuthStore()
const { resolveUrl } = useMediaUrl()
const { categories, products, loadCategories, loadAllProducts } = useProductCatalog()

await useAsyncData('header-product-menu', async () => {
  await Promise.all([loadCategories(), loadAllProducts()])
  return true
})

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

const navItems = [
  { label: '首页', to: '/' },
  { label: '模块', to: '/modules' },
  { label: '定价', to: '/pricing' },
  { label: '博客', to: '/blog' },
  { label: '文档', to: '/docs' },
  { label: '更新日志', to: '/changelog' },
  { label: '关于', to: '/about' }
]

const productSearchKeyword = ref('')
const isProductMenuOpen = ref(false)
const activeCategoryId = ref('')
let closeTimer: ReturnType<typeof setTimeout> | null = null

const productsByCategory = computed(() => {
  const map = new Map<string, AppProduct[]>()
  for (const product of products.value) {
    const key = product.categoryId ?? ''
    const current = map.get(key) ?? []
    current.push(product)
    map.set(key, current)
  }
  return map
})

function buildSections(node: AppProductCategory, parentTrail = ''): ProductMenuSection[] {
  const sectionLabel = parentTrail ? `${parentTrail} / ${node.name}` : node.name
  const ownProducts = (productsByCategory.value.get(node.id) ?? []).sort((left, right) => {
    const leftSort = left.sortNo ?? Number.MAX_SAFE_INTEGER
    const rightSort = right.sortNo ?? Number.MAX_SAFE_INTEGER
    if (leftSort !== rightSort) {
      return leftSort - rightSort
    }
    return left.title.localeCompare(right.title, 'zh-CN')
  })

  const sections: ProductMenuSection[] = ownProducts.length
    ? [{ id: node.id, label: sectionLabel, products: ownProducts }]
    : []

  if (node.children?.length) {
    for (const child of node.children) {
      sections.push(...buildSections(child, sectionLabel))
    }
  }

  return sections
}

const rootCategoryEntries = computed(() =>
  categories.value
    .map((category) => ({
      category,
      sections: buildSections(category),
    }))
    .filter(entry => entry.sections.some(section => section.products.length > 0)),
)

watch(rootCategoryEntries, (entries) => {
  if (!entries.length) {
    activeCategoryId.value = ''
    return
  }

  if (!entries.some(entry => entry.category.id === activeCategoryId.value)) {
    activeCategoryId.value = entries[0]!.category.id
  }
}, { immediate: true })

const activeCategoryEntry = computed(() =>
  rootCategoryEntries.value.find(entry => entry.category.id === activeCategoryId.value) ?? rootCategoryEntries.value[0] ?? null,
)

const activeSections = computed(() => {
  const entry = activeCategoryEntry.value
  if (!entry) {
    return []
  }

  const keyword = productSearchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return entry.sections
  }

  return entry.sections
    .map(section => ({
      ...section,
      products: section.products.filter(product =>
        [product.title, product.subtitle, product.shortDescription, product.description]
          .filter(Boolean)
          .some(value => value!.toLowerCase().includes(keyword)),
      ),
    }))
    .filter(section => section.products.length > 0)
})

const activeSectionColumns = computed(() => chunkItems(activeSections.value, 3))

const activeProductCount = computed(() =>
  activeSections.value.reduce((total, section) => total + section.products.length, 0),
)

const highlightedProducts = computed(() =>
  activeSections.value.flatMap(section => section.products).slice(0, 8),
)

function getCategoryProductCount(categoryId: string): number {
  const entry = rootCategoryEntries.value.find(item => item.category.id === categoryId)
  if (!entry) {
    return 0
  }

  return entry.sections.reduce((total, section) => total + section.products.length, 0)
}

function cancelCloseMenu() {
  if (closeTimer) {
    clearTimeout(closeTimer)
    closeTimer = null
  }
}

function openProductMenu() {
  cancelCloseMenu()
  isProductMenuOpen.value = true
}

function scheduleCloseMenu() {
  cancelCloseMenu()
  closeTimer = setTimeout(() => {
    isProductMenuOpen.value = false
    productSearchKeyword.value = ''
  }, 120)
}

function setActiveCategory(categoryId: string) {
  activeCategoryId.value = categoryId
}

onBeforeUnmount(() => {
  cancelCloseMenu()
})
</script>

<template>
  <UHeader>
    <template #left>
      <NuxtLink to="/">
        <AppLogo class="shrink-0" />
      </NuxtLink>
    </template>

    <template #right>

      <UButton
        label="进入社区"
        to="/community"
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

    <div class="relative hidden lg:flex items-center gap-1" @mouseleave="scheduleCloseMenu">
      <NuxtLink
        v-for="item in navItems.slice(0, 2)"
        :key="item.to"
        :to="item.to"
        class="rounded-md px-3 py-2 text-sm font-medium transition hover:text-primary"
        :class="route.path === item.to ? 'text-primary' : 'text-highlighted'"
      >
        {{ item.label }}
      </NuxtLink>

      <button
        type="button"
        class="inline-flex items-center gap-1 rounded-md px-3 py-2 text-sm font-medium transition hover:text-primary"
        :class="isProductMenuOpen || route.path.startsWith('/products') ? 'text-primary' : 'text-highlighted'"
        @mouseenter="openProductMenu"
        @focus="openProductMenu"
      >
        <span>产品</span>
        <UIcon
          name="i-lucide-chevron-down"
          class="size-4 transition"
          :class="isProductMenuOpen ? 'rotate-180' : ''"
        />
      </button>

      <NuxtLink
        v-for="item in navItems.slice(2)"
        :key="item.to"
        :to="item.to"
        class="rounded-md px-3 py-2 text-sm font-medium transition hover:text-primary"
        :class="route.path === item.to ? 'text-primary' : 'text-highlighted'"
      >
        {{ item.label }}
      </NuxtLink>

        <ProductMegaMenu
          v-if="isProductMenuOpen && activeCategoryEntry"
          :active-category-entry="activeCategoryEntry"
          :root-category-entries="rootCategoryEntries"
          :active-section-columns="activeSectionColumns"
          :active-sections="activeSections"
          :active-product-count="activeProductCount"
          :highlighted-products="highlightedProducts"
          :product-search-keyword="productSearchKeyword"
          :get-category-product-count="getCategoryProductCount"
          @mouseenter="openProductMenu"
          @update:product-search-keyword="productSearchKeyword = $event"
          @set-active-category="setActiveCategory"
        />
    </div>
  </UHeader>
</template>
