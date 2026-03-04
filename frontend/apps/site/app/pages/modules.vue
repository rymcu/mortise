<script setup lang="ts">
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
