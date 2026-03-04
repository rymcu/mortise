<script setup lang="ts">
const route = useRoute()
const slug = computed(() => {
  const s = route.params.slug
  return Array.isArray(s) ? s.join('/') : s
})

const path = computed(() => `/docs/${slug.value}`)

const { data: page } = await useAsyncData(
  `doc-${slug.value}`,
  () => queryCollection('docs').path(path.value).first()
)

if (!page.value) {
  throw createError({ statusCode: 404, statusMessage: '文档不存在', fatal: true })
}

const { data: navDocs } = await useAsyncData('docs-nav-side', () =>
  queryCollection('docs').order('order', 'ASC').all()
)

useSeoMeta({
  title: () => `${page.value?.title} - Mortise 文档`,
  description: () => page.value?.description || ''
})
</script>

<template>
  <UContainer class="py-10">
    <div class="flex gap-8">
      <!-- 左侧导航 -->
      <aside class="hidden lg:block w-56 shrink-0">
        <div class="sticky top-20">
          <p class="text-xs font-semibold text-muted uppercase tracking-wider mb-3">
            文档目录
          </p>
          <UNavigationMenu
            :items="navDocs?.map(doc => ({
              label: doc.title,
              to: doc.path,
              active: doc.path === path
            })) || []"
            orientation="vertical"
          />
        </div>
      </aside>

      <!-- 文档内容 -->
      <main class="flex-1 min-w-0">
        <UPageHeader
          v-if="page"
          :title="page.title"
          :description="page.description"
          class="mb-8"
        />

        <ContentRenderer
          v-if="page"
          :value="page"
          class="prose prose-neutral dark:prose-invert max-w-none"
        />
      </main>
    </div>
  </UContainer>
</template>
