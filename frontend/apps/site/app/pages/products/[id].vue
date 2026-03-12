<script setup lang="ts">
const route = useRoute()
const productId = computed(() => String(route.params.id ?? ''))
const { resolveUrl } = useMediaUrl()
const { communityPath } = useCommunityBasePath()
const { fetchArticlesByProduct } = useCommunityArticles()
const { fetchProductById, loadProductTypes, productTypeLabel } = useProductCatalog()
const fallbackCover = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 720"><rect width="1200" height="720" rx="32" fill="%23f3f4f6"/><text x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%236b7280" font-size="56" font-family="Arial, sans-serif">Mortise Product</text></svg>'

function toDisplayEntries(value?: Record<string, unknown>) {
  if (!value) {
    return []
  }

  return Object.entries(value)
    .filter(([, entryValue]) => entryValue !== null && entryValue !== undefined && `${entryValue}`.trim().length > 0)
    .map(([key, entryValue]) => ({
      key,
      label: key,
      value: Array.isArray(entryValue) ? entryValue.join('、') : String(entryValue),
    }))
}

const { data } = await useAsyncData(`site-product-${productId.value}`, async () => {
  await loadProductTypes()
  const product = await fetchProductById(productId.value)
  if (!product) {
    return null
  }

  const relatedArticles = await fetchArticlesByProduct(productId.value, 6)
  return { product, relatedArticles }
})

if (!data.value?.product) {
  throw createError({ statusCode: 404, statusMessage: '产品不存在', fatal: true })
}

const product = computed(() => data.value!.product)
const relatedArticles = computed(() => data.value?.relatedArticles ?? [])
const featureEntries = computed(() => toDisplayEntries(product.value.features))
const specificationEntries = computed(() => toDisplayEntries(product.value.specifications))

useSeoMeta({
  title: product.value.seoTitle || `${product.value.title} - 产品详情`,
  ogTitle: product.value.seoTitle || `${product.value.title} - 产品详情`,
  description: product.value.seoDescription || product.value.shortDescription || product.value.description || '',
  ogDescription: product.value.seoDescription || product.value.shortDescription || product.value.description || '',
})
</script>

<template>
  <div>
    <UPageHero
      :title="product.title"
      :description="product.subtitle || product.shortDescription || product.description || '暂无产品介绍。'"
    >
      <template #top>
        <HeroBackground />
      </template>

      <template #links>
        <div class="flex flex-wrap gap-3">
          <UButton to="/products" variant="soft" color="neutral" icon="i-lucide-arrow-left">
            返回产品列表
          </UButton>
          <UButton
            v-if="relatedArticles.length"
            :to="communityPath(`/article/${relatedArticles[0]?.id}`)"
            icon="i-lucide-book-open"
          >
            查看相关文章
          </UButton>
        </div>
      </template>
    </UPageHero>

    <UPageSection>
      <div class="grid gap-8 lg:grid-cols-[minmax(0,1.15fr)_360px]">
        <div class="space-y-6">
          <UCard :ui="{ body: 'space-y-5' }">
            <img
              :src="resolveUrl(product.coverImageUrl) || fallbackCover"
              :alt="product.title"
              class="aspect-[16/9] w-full rounded-3xl object-cover"
            >

            <div class="flex flex-wrap items-center gap-2">
              <UBadge color="primary" variant="subtle">
                {{ productTypeLabel(product.productType) }}
              </UBadge>
              <UBadge v-if="product.isFeatured" color="success" variant="soft">
                推荐产品
              </UBadge>
              <UBadge v-for="tag in product.tags || []" :key="`${product.id}-${tag}`" color="neutral" variant="outline">
                {{ tag }}
              </UBadge>
            </div>

            <div class="space-y-3">
              <h2 class="text-xl font-semibold">产品介绍</h2>
              <p class="whitespace-pre-line leading-7 text-muted">
                {{ product.description || product.shortDescription || '暂无更详细的介绍内容。' }}
              </p>
            </div>
          </UCard>

          <UCard v-if="featureEntries.length" :ui="{ body: 'space-y-4' }">
            <div>
              <h2 class="text-lg font-semibold">核心特性</h2>
              <p class="mt-1 text-sm text-muted">用于快速了解该产品当前对外公开的能力点。</p>
            </div>

            <div class="grid gap-3 sm:grid-cols-2">
              <div
                v-for="feature in featureEntries"
                :key="`${product.id}-feature-${feature.key}`"
                class="rounded-2xl border border-default/70 px-4 py-4"
              >
                <div class="text-sm font-medium text-highlighted">{{ feature.label }}</div>
                <div class="mt-2 text-sm leading-6 text-muted">{{ feature.value }}</div>
              </div>
            </div>
          </UCard>

          <UCard v-if="specificationEntries.length" :ui="{ body: 'space-y-4' }">
            <div>
              <h2 class="text-lg font-semibold">规格说明</h2>
              <p class="mt-1 text-sm text-muted">公开可见的规格、参数与补充信息。</p>
            </div>

            <div class="divide-y divide-default/60 rounded-2xl border border-default/70">
              <div
                v-for="specification in specificationEntries"
                :key="`${product.id}-spec-${specification.key}`"
                class="grid gap-2 px-4 py-4 sm:grid-cols-[180px_minmax(0,1fr)]"
              >
                <div class="text-sm font-medium text-highlighted">{{ specification.label }}</div>
                <div class="text-sm leading-6 text-muted">{{ specification.value }}</div>
              </div>
            </div>
          </UCard>
        </div>

        <aside class="space-y-6 lg:sticky lg:top-24 lg:self-start">
          <UCard :ui="{ body: 'space-y-3' }">
            <div>
              <h2 class="text-base font-semibold">产品信息</h2>
              <p class="mt-1 text-sm text-muted">便于快速确认当前产品定位与上架状态。</p>
            </div>

            <div class="space-y-3 text-sm">
              <div class="flex items-start justify-between gap-4">
                <span class="text-muted">产品类型</span>
                <span class="text-right text-highlighted">{{ productTypeLabel(product.productType) }}</span>
              </div>
              <div class="flex items-start justify-between gap-4">
                <span class="text-muted">发布时间</span>
                <span class="text-right text-highlighted">{{ product.publishedTime || product.createdTime || '暂无' }}</span>
              </div>
              <div class="flex items-start justify-between gap-4">
                <span class="text-muted">SEO 标题</span>
                <span class="text-right text-highlighted">{{ product.seoTitle || product.title }}</span>
              </div>
            </div>
          </UCard>

          <UCard :ui="{ header: 'pb-3', body: 'pt-0' }">
            <template #header>
              <div class="flex items-center justify-between gap-3">
                <div>
                  <h2 class="text-base font-semibold">相关文章</h2>
                  <p class="mt-1 text-sm text-muted">由社区创作者围绕该产品产出的公开内容。</p>
                </div>
                <UIcon name="i-lucide-book-open-text" class="size-4 text-primary" />
              </div>
            </template>

            <div v-if="relatedArticles.length" class="space-y-3">
              <NuxtLink
                v-for="article in relatedArticles"
                :key="article.id"
                :to="communityPath(`/article/${article.id}`)"
                class="block rounded-2xl border border-default/60 px-4 py-4 transition hover:border-primary/40 hover:bg-elevated/50"
              >
                <div class="line-clamp-2 text-sm font-medium text-highlighted">{{ article.title }}</div>
                <p v-if="article.summary" class="mt-1 line-clamp-2 text-xs leading-5 text-muted">{{ article.summary }}</p>
                <div class="mt-2 flex items-center justify-between text-xs text-muted">
                  <span>{{ article.author?.name || '匿名' }}</span>
                  <span>{{ article.publishedTime || article.createdTime || '' }}</span>
                </div>
              </NuxtLink>
            </div>

            <div v-else class="rounded-2xl border border-dashed border-default px-4 py-4 text-sm text-muted">
              该产品暂时还没有公开关联文章，欢迎前往社区率先分享使用经验。
            </div>
          </UCard>
        </aside>
      </div>
    </UPageSection>
  </div>
</template>
