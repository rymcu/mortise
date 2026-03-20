<script setup lang="ts">
import type { AppProduct, AppProductCategory } from '@mortise/core-sdk'

type ProductSection = {
  id: string
  label: string
  description?: string
  products: AppProduct[]
}

type ProductCategoryEntry = {
  category: AppProductCategory
  sections: ProductSection[]
  total: number
}

const route = useRoute()
const searchKeyword = ref(typeof route.query.q === 'string' ? route.query.q : '')
const activeCategoryId = ref(typeof route.query.category === 'string' ? route.query.category : '')
const hasInitializedCategory = ref(false)

const {
  categories,
  products,
  loading,
  loadAllProducts,
  loadCategories,
  loadProductTypes,
  productTypeLabel,
} = useProductCatalog()

await useAsyncData('site-products', async () => {
  await Promise.all([loadProductTypes(), loadCategories(), loadAllProducts()])
  return true
})

function sortProducts(items: AppProduct[]) {
  return [...items].sort((left, right) => {
    const leftSort = left.sortNo ?? Number.MAX_SAFE_INTEGER
    const rightSort = right.sortNo ?? Number.MAX_SAFE_INTEGER
    if (leftSort !== rightSort) {
      return leftSort - rightSort
    }
    return left.title.localeCompare(right.title, 'zh-CN')
  })
}

const filteredProducts = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return products.value
  }

  return products.value.filter(product =>
    [product.title, product.subtitle, product.shortDescription, product.description]
      .filter(Boolean)
      .some(field => field!.toLowerCase().includes(keyword)),
  )
})

const productsByCategory = computed(() => {
  const map = new Map<string, AppProduct[]>()
  for (const product of filteredProducts.value) {
    const key = product.categoryId || ''
    const current = map.get(key) ?? []
    current.push(product)
    map.set(key, current)
  }

  return map
})

function buildSections(node: AppProductCategory, isRoot = false): ProductSection[] {
  const ownProducts = sortProducts(productsByCategory.value.get(node.id) ?? [])
  const sections: ProductSection[] = ownProducts.length
    ? [{
        id: `${node.id}-section`,
        label: isRoot ? '核心产品' : node.name,
        description: isRoot ? node.description : undefined,
        products: ownProducts,
      }]
    : []

  if (node.children?.length) {
    for (const child of node.children) {
      sections.push(...buildSections(child))
    }
  }

  return sections
}

const rootCategoryEntries = computed<ProductCategoryEntry[]>(() =>
  categories.value
    .map((category) => {
      const sections = buildSections(category, true)
      const total = sections.reduce((sum, section) => sum + section.products.length, 0)
      return {
        category,
        sections,
        total,
      }
    })
    .filter(entry => entry.total > 0),
)

watch(rootCategoryEntries, (entries) => {
  if (!entries.length) {
    activeCategoryId.value = ''
    hasInitializedCategory.value = true
    return
  }

  if (activeCategoryId.value && !entries.some(entry => entry.category.id === activeCategoryId.value)) {
    activeCategoryId.value = ''
  }

  if (!hasInitializedCategory.value) {
    const routeCategoryId = typeof route.query.category === 'string' ? route.query.category : ''
    activeCategoryId.value = entries.some(entry => entry.category.id === routeCategoryId)
      ? routeCategoryId
      : entries[0]!.category.id
    hasInitializedCategory.value = true
  }
}, { immediate: true })

const visibleCategoryEntries = computed(() => {
  if (!activeCategoryId.value) {
    return rootCategoryEntries.value
  }

  const current = rootCategoryEntries.value.find(entry => entry.category.id === activeCategoryId.value)
  return current ? [current] : rootCategoryEntries.value
})

const sidebarEntries = computed(() =>
  rootCategoryEntries.value.map(entry => ({
    category: entry.category,
    count: entry.total,
  })),
)

const totalVisibleProducts = computed(() =>
  visibleCategoryEntries.value.reduce((sum, entry) => sum + entry.total, 0),
)

watch([searchKeyword, activeCategoryId], ([keyword, categoryId]) => {
  navigateTo({
    path: '/products',
    query: {
      ...(keyword.trim() ? { q: keyword.trim() } : {}),
      ...(categoryId ? { category: categoryId } : {}),
    },
  }, { replace: true })
})

function selectCategory(categoryId: string) {
  activeCategoryId.value = categoryId
}

function clearCategoryFilter() {
  activeCategoryId.value = ''
}

useSeoMeta({
  title: '产品列表',
  ogTitle: '产品列表',
  description: '浏览 Mortise 的公开产品目录，按分类快速查找产品，并进入详情页了解介绍、规格与相关文章。',
  ogDescription: '浏览 Mortise 的公开产品目录，按分类快速查找产品，并进入详情页了解介绍、规格与相关文章。',
})
</script>

<template>
  <div>
    <UPageHero
      title="所有产品"
      description="按产品分类浏览 Mortise 已公开的产品能力，快速找到对应产品并进入详情页查看介绍与相关文章。"
    >
      <template #top>
        <HeroBackground />
      </template>
    </UPageHero>

    <UPageSection>
      <div class="grid gap-10 lg:grid-cols-[260px_minmax(0,1fr)] xl:grid-cols-[280px_minmax(0,1fr)]">
        <ProductsCategorySidebar
          :entries="sidebarEntries"
          :active-category-id="activeCategoryId"
          :loading="loading"
          @select="selectCategory"
          @clear-filter="clearCategoryFilter"
        />

        <div class="min-w-0">
          <div class="border-t border-default pt-6">
            <div class="flex flex-col gap-5 border-b border-default pb-6 md:flex-row md:items-end md:justify-between">
              <div>
                <div class="text-sm text-muted">搜索产品</div>
                <div class="mt-2 text-sm text-muted">
                  当前共展示 {{ totalVisibleProducts }} 个产品
                </div>
              </div>

              <div class="w-full md:max-w-md">
                <UInput
                  v-model="searchKeyword"
                  icon="i-lucide-search"
                  size="xl"
                  class="w-full"
                  variant="none"
                  placeholder="搜索全部产品"
                  :ui="{
                    base: 'h-12 border-0 border-b border-default rounded-none px-0 focus:border-primary',
                    leadingIcon: 'text-muted'
                  }"
                />
              </div>
            </div>

            <div v-if="loading" class="space-y-12 py-8">
              <div v-for="section in 3" :key="section" class="space-y-5">
                <USkeleton class="h-8 w-56 rounded-none" />
                <USkeleton class="h-24 w-full rounded-none" />
                <div class="grid gap-6 xl:grid-cols-3 md:grid-cols-2">
                  <USkeleton v-for="n in 3" :key="n" class="h-52 rounded-none" />
                </div>
              </div>
            </div>

            <div v-else-if="visibleCategoryEntries.length" class="space-y-14 py-8">
              <section
                v-for="entry in visibleCategoryEntries"
                :key="entry.category.id"
                class="space-y-8 border-t border-default pt-8 first:border-t-0 first:pt-0"
              >
                <div class="space-y-4">
                  <div class="flex items-center gap-3">
                    <h2 class="text-4xl font-semibold tracking-tight text-highlighted">
                      {{ entry.category.name }}
                    </h2>
                    <UIcon name="i-lucide-chevron-right" class="size-6 text-primary" />
                  </div>
                  <p class="max-w-5xl text-lg leading-9 text-toned">
                    {{ entry.category.description || '浏览当前分类下的产品能力，点击卡片进入详情页了解产品介绍、规格与相关文章。' }}
                  </p>
                </div>

                <div class="space-y-10">
                  <section
                    v-for="section in entry.sections"
                    :key="section.id"
                    class="space-y-5"
                  >
                    <div class="flex items-center justify-between gap-4">
                      <div>
                        <h3 class="text-2xl font-semibold text-highlighted">{{ section.label }}</h3>
                        <p v-if="section.description" class="mt-2 text-sm leading-6 text-muted">
                          {{ section.description }}
                        </p>
                      </div>
                    </div>

                    <div class="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
                      <NuxtLink
                        v-for="product in section.products"
                        :key="product.id"
                        :to="`/products/${product.id}`"
                        class="group min-h-[220px] border border-default bg-default px-8 py-7 transition duration-200 hover:border-primary/30 hover:shadow-sm"
                      >
                        <div class="line-clamp-2 text-[22px] font-semibold leading-9 text-highlighted transition duration-200 group-hover:text-primary">
                          {{ product.title }}
                        </div>

                        <p class="mt-6 line-clamp-4 text-base leading-9 text-muted transition duration-200 group-hover:text-toned">
                          {{ product.subtitle || product.shortDescription || product.description || productTypeLabel(product.productType) }}
                        </p>
                      </NuxtLink>
                    </div>
                  </section>
                </div>
              </section>
            </div>

            <div v-else class="flex min-h-[320px] items-center justify-center py-8">
              <div class="text-center">
                <div class="text-xl font-semibold text-highlighted">暂无匹配产品</div>
                <p class="mt-3 text-sm text-muted">请尝试切换分类或调整搜索关键词。</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </UPageSection>
  </div>
</template>
