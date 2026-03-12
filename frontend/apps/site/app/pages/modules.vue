<script setup lang="ts">
const { communityPath } = useCommunityBasePath()
const { fetchArticlesByProduct } = useCommunityArticles()
const { loadAllProducts } = useProductCatalog()

const { data: page } = await useAsyncData('modules', () => queryCollection('modules').first())

if (!page.value) {
  throw createError({ statusCode: 404, statusMessage: 'Page not found', fatal: true })
}

const title = page.value.seo?.title || page.value.title
const description = page.value.seo?.description || page.value.description

useSeoMeta({
  title,
  ogTitle: title,
  description,
  ogDescription: description
})

const statusMap: Record<string, { label: string, color: 'success' | 'warning' | 'neutral' }> = {
  stable: { label: '稳定', color: 'success' },
  beta: { label: '测试中', color: 'warning' },
  planned: { label: '规划中', color: 'neutral' }
}

const { data: linkedArticlesMap } = await useAsyncData('modules-linked-articles', async () => {
  const moduleEntries = page.value?.categories?.flatMap(category => category.modules ?? []) ?? []
  if (!moduleEntries.length) {
    return {}
  }

  const products = await loadAllProducts()
  if (!products.length) {
    return {}
  }

  const articleEntries = await Promise.all(moduleEntries.map(async (mod) => {
    const explicitProductId = typeof mod.productId === 'string' && mod.productId.trim().length
      ? mod.productId.trim()
      : ''
    const keyword = normalizeKeyword(typeof mod.productKeyword === 'string' && mod.productKeyword.trim().length
      ? mod.productKeyword
      : mod.name)

    const matchedProductId = explicitProductId || products.find((product) => {
      const haystack = normalizeKeyword([product.title, product.subtitle, product.description].filter(Boolean).join(' '))
      return !!keyword && haystack.includes(keyword)
    })?.id

    if (!matchedProductId) {
      return [mod.name, []]
    }

    const articles = await fetchArticlesByProduct(matchedProductId, 3)
    return [mod.name, articles]
  }))

  return Object.fromEntries(articleEntries)
})

function moduleLinkedArticles(moduleName: string) {
  return linkedArticlesMap.value?.[moduleName] ?? []
}

function normalizeKeyword(value: string) {
  return value.trim().toLowerCase()
}
</script>

<template>
  <div v-if="page">
    <!-- Hero -->
    <UPageHero
      :title="page.title"
      :description="page.description"
    >
      <template #top>
        <HeroBackground />
      </template>
    </UPageHero>

    <!-- 模块分类 -->
    <UPageSection
      v-for="(category, catIndex) in page.categories"
      :key="catIndex"
      :title="category.title"
      :description="category.description"
      :icon="category.icon"
    >
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="(mod, modIndex) in category.modules"
          :key="modIndex"
          class="relative"
        >
          <UBadge
            v-if="mod.status && statusMap[mod.status]"
            :label="statusMap[mod.status]!.label"
            :color="statusMap[mod.status]!.color"
            variant="soft"
            class="absolute top-3 right-3 z-10"
          />
          <div class="flex h-full flex-col gap-3">
            <UPageCard
              :title="mod.name"
              :description="mod.description"
              :icon="mod.icon"
              spotlight
              class="h-full"
              :ui="{ title: 'text-base font-semibold', description: 'text-sm leading-relaxed' }"
            >
              <template #footer>
                <div class="flex flex-wrap gap-1.5">
                  <UBadge
                    v-for="tag in (mod.tags || [])"
                    :key="tag"
                    :label="tag"
                    color="primary"
                    variant="subtle"
                  />
                </div>
              </template>
            </UPageCard>

            <UCard
              v-if="moduleLinkedArticles(mod.name).length"
              :ui="{ header: 'pb-3', body: 'pt-0' }"
            >
              <template #header>
                <div class="flex items-center justify-between gap-3">
                  <div>
                    <h3 class="text-sm font-semibold">相关文章</h3>
                    <p class="mt-1 text-xs text-muted">围绕该模块的社区教程与实战内容。</p>
                  </div>
                  <UIcon name="i-lucide-book-open-text" class="size-4 text-primary" />
                </div>
              </template>

              <div class="space-y-3">
                <NuxtLink
                  v-for="article in moduleLinkedArticles(mod.name)"
                  :key="article.id"
                  :to="communityPath(`/article/${article.id}`)"
                  class="block rounded-xl border border-default/60 px-3 py-3 transition hover:border-primary/40 hover:bg-elevated/50"
                >
                  <div class="line-clamp-2 text-sm font-medium text-highlighted">{{ article.title }}</div>
                  <p v-if="article.summary" class="mt-1 line-clamp-2 text-xs leading-5 text-muted">{{ article.summary }}</p>
                  <div class="mt-2 flex items-center justify-between text-xs text-muted">
                    <span>{{ article.author?.name || '匿名' }}</span>
                    <span>{{ article.publishedTime || article.createdTime || '' }}</span>
                  </div>
                </NuxtLink>
              </div>
            </UCard>
          </div>
        </div>
      </div>
    </UPageSection>

    <!-- CTA -->
    <USeparator />

    <UPageCTA
      v-if="page.cta"
      :title="page.cta.title"
      :description="page.cta.description"
      :links="page.cta.links"
      variant="naked"
    />
  </div>
</template>
