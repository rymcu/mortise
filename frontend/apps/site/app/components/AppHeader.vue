<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui'
import type { AppProduct, AppProductCategory } from '@mortise/core-sdk'

type ProductMenuSection = {
  id: string
  label: string
  products: AppProduct[]
}

function chunkItems<T>(items: T[], chunkCount: number): T[][] {
  if (!items.length) {
    return []
  }

  const normalizedChunkCount = Math.max(1, Math.min(chunkCount, items.length))
  const columns = Array.from({ length: normalizedChunkCount }, () => [] as T[])

  items.forEach((item, index) => {
    columns[index % normalizedChunkCount]!.push(item)
  })

  return columns
}

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

        <div
          v-if="isProductMenuOpen && activeCategoryEntry"
          class="absolute left-1/2 top-full z-50 mt-0 w-screen -translate-x-1/2 border-t border-default bg-default/98 shadow-2xl backdrop-blur"
          @mouseenter="openProductMenu"
        >
          <div class="grid min-h-[580px] grid-cols-[320px_minmax(0,1fr)]">
            <aside class="border-r border-default bg-elevated/40 px-6 py-6">
              <div class="mb-5 flex items-center justify-between gap-3">
                <div>
                  <div class="text-2xl font-semibold text-highlighted">产品</div>
                  <p class="mt-1 text-sm text-muted">按分类浏览 Mortise 产品能力</p>
                </div>
                <UIcon name="i-lucide-chevron-right" class="size-5 text-muted" />
              </div>

            <UInput
              v-model="productSearchKeyword"
              icon="i-lucide-search"
              class="mb-4"
              placeholder="搜索产品"
            />

            <div class="max-h-[430px] space-y-1 overflow-y-auto pr-1">
              <button
                v-for="entry in rootCategoryEntries"
                :key="entry.category.id"
                type="button"
                class="group flex w-full items-center justify-between gap-3 rounded-none border-l-2 px-5 py-3 text-left transition duration-200"
                :class="entry.category.id === activeCategoryEntry.category.id
                  ? 'border-primary bg-primary/8 text-primary shadow-sm'
                  : 'border-transparent text-highlighted hover:bg-elevated hover:text-primary'"
                @mouseenter="setActiveCategory(entry.category.id)"
                @focus="setActiveCategory(entry.category.id)"
              >
                <div class="min-w-0 flex-1">
                  <div class="font-medium transition duration-200 group-hover:translate-x-0.5">{{ entry.category.name }}</div>
                  <div class="mt-1 text-xs text-muted transition duration-200 group-hover:text-toned">
                    {{ getCategoryProductCount(entry.category.id) }} 个产品能力
                  </div>
                </div>
                <UIcon name="i-lucide-chevron-right" class="size-4 shrink-0 transition duration-200 group-hover:translate-x-0.5" />
              </button>
            </div>
          </aside>

          <div class="flex min-w-0 flex-col px-10 py-7">
            <div class="mb-6 flex items-start justify-between gap-6">
              <div>
                <h3 class="text-2xl font-semibold text-highlighted">{{ activeCategoryEntry.category.name }}</h3>
                <p class="mt-2 max-w-2xl text-sm leading-6 text-muted">
                  {{ activeCategoryEntry.category.description || '从当前分类中选择产品，进入详情页查看介绍、规格与相关文章。' }}
                </p>
                <div class="mt-4 flex flex-wrap items-center gap-3 text-sm">
                  <span class="inline-flex items-center rounded-full bg-elevated px-3 py-1 text-highlighted">
                    {{ activeProductCount }} 个产品
                  </span>
                  <span class="inline-flex items-center rounded-full bg-elevated px-3 py-1 text-muted">
                    {{ activeSections.length }} 个能力分组
                  </span>
                </div>
              </div>

              <NuxtLink
                to="/products"
                class="shrink-0 rounded-full border border-primary/20 px-4 py-2 text-sm font-medium text-primary transition hover:border-primary/40 hover:bg-primary/6 hover:text-primary/80"
              >
                查看全部产品
              </NuxtLink>
            </div>

            <div v-if="activeSections.length" class="grid flex-1 gap-10 xl:grid-cols-[minmax(0,1fr)_320px]">
              <div class="grid min-w-0 gap-8 xl:grid-cols-3">
                <div v-for="(column, columnIndex) in activeSectionColumns" :key="`column-${columnIndex}`" class="space-y-8 min-w-0">
                  <section v-for="section in column" :key="section.id" class="min-w-0">
                    <div class="mb-4 flex items-center justify-between gap-3 border-b border-default pb-3">
                      <h4 class="text-lg font-semibold text-highlighted">{{ section.label }}</h4>
                      <span class="shrink-0 text-xs text-muted">{{ section.products.length }} 项</span>
                    </div>
                    <div class="space-y-3">
                      <NuxtLink
                        v-for="product in section.products"
                        :key="product.id"
                        :to="`/products/${product.id}`"
                        class="group block rounded-xl border border-transparent px-3 py-3 -mx-3 transition duration-200 hover:border-primary/10 hover:bg-elevated/70"
                      >
                        <div class="line-clamp-1 text-[17px] font-medium text-highlighted transition duration-200 group-hover:text-primary">
                          {{ product.title }}
                        </div>
                        <p class="mt-1 line-clamp-2 text-sm leading-6 text-muted transition duration-200 group-hover:text-toned">
                          {{ product.subtitle || product.shortDescription || product.description || '暂无产品简介。' }}
                        </p>
                      </NuxtLink>
                    </div>
                  </section>
                </div>
              </div>

              <aside class="border-l border-default pl-8">
                <div class="rounded-2xl border border-default bg-elevated/35 p-5">
                  <div class="mb-5 flex items-center justify-between gap-3">
                    <h4 class="text-lg font-semibold text-highlighted">分类概览</h4>
                    <UIcon name="i-lucide-layout-panel-top" class="size-4 text-muted" />
                  </div>

                  <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
                    <div class="rounded-xl bg-default px-4 py-3">
                      <div class="text-xs text-muted">当前分类</div>
                      <div class="mt-1 text-base font-semibold text-highlighted">{{ activeCategoryEntry.category.name }}</div>
                    </div>
                    <div class="rounded-xl bg-default px-4 py-3">
                      <div class="text-xs text-muted">产品总数</div>
                      <div class="mt-1 text-base font-semibold text-highlighted">{{ activeProductCount }}</div>
                    </div>
                    <div class="rounded-xl bg-default px-4 py-3">
                      <div class="text-xs text-muted">能力分组</div>
                      <div class="mt-1 text-base font-semibold text-highlighted">{{ activeSections.length }}</div>
                    </div>
                  </div>
                </div>

                <div class="mt-6">
                  <div class="mb-5 flex items-center justify-between gap-3">
                    <h4 class="text-lg font-semibold text-highlighted">推荐浏览</h4>
                    <UIcon name="i-lucide-arrow-right" class="size-4 text-muted" />
                  </div>

                  <div class="space-y-4">
                    <NuxtLink
                      v-for="product in highlightedProducts"
                      :key="`featured-${product.id}`"
                      :to="`/products/${product.id}`"
                      class="group flex items-start gap-4 rounded-xl px-3 py-3 -mx-3 transition duration-200 hover:bg-elevated/70"
                    >
                      <div class="mt-0.5 flex size-11 shrink-0 items-center justify-center bg-elevated text-primary transition duration-200 group-hover:bg-primary/12">
                        <UIcon name="i-lucide-box" class="size-5" />
                      </div>
                      <div class="min-w-0">
                        <div class="line-clamp-2 text-base font-medium text-highlighted transition duration-200 group-hover:text-primary">
                          {{ product.title }}
                        </div>
                        <p class="mt-1 line-clamp-2 text-sm leading-6 text-muted transition duration-200 group-hover:text-toned">
                          {{ product.subtitle || product.shortDescription || '查看产品详情与相关文章' }}
                        </p>
                      </div>
                    </NuxtLink>
                  </div>
                </div>
              </aside>
            </div>

            <div v-else class="flex flex-1 items-center justify-center border border-dashed border-default">
              <div class="text-center">
                <div class="text-base font-medium text-highlighted">当前分类下暂无匹配产品</div>
                <p class="mt-2 text-sm text-muted">可以切换左侧分类，或清空搜索关键字后再试。</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </UHeader>
</template>
