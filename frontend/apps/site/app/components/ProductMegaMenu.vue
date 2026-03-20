<script setup lang="ts">
import type { ProductMenuSection, CategoryEntry } from '~/types/product-menu'

defineProps<{
  activeCategoryEntry: CategoryEntry
  rootCategoryEntries: CategoryEntry[]
  activeSectionColumns: ProductMenuSection[][]
  activeSections: ProductMenuSection[]
  activeProductCount: number
  highlightedProducts: { id: string; title: string; subtitle?: string | null; shortDescription?: string | null; description?: string | null }[]
  productSearchKeyword: string
  getCategoryProductCount: (categoryId: string) => number
}>()

defineEmits<{
  mouseenter: []
  'update:productSearchKeyword': [value: string]
  'set-active-category': [categoryId: string]
}>()
</script>

<template>
  <div
    class="absolute left-1/2 top-full z-50 mt-0 w-screen -translate-x-1/2 border-t border-default bg-default/98 shadow-2xl backdrop-blur"
    @mouseenter="$emit('mouseenter')"
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
          :model-value="productSearchKeyword"
          icon="i-lucide-search"
          class="mb-4"
          placeholder="搜索产品"
          @update:model-value="$emit('update:productSearchKeyword', $event as string)"
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
            @mouseenter="$emit('set-active-category', entry.category.id)"
            @focus="$emit('set-active-category', entry.category.id)"
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
</template>
