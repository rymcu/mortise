<script setup lang="ts">
const { data: page } = await useAsyncData('home', () => queryCollection('home').first())
if (!page.value) {
  throw createError({ statusCode: 404, statusMessage: 'Page not found', fatal: true })
}

useSeoMeta({
  title: page.value.seo?.title || page.value.title,
  ogTitle: page.value.seo?.title || page.value.title,
  description: page.value.seo?.description || page.value.description,
  ogDescription: page.value.seo?.description || page.value.description
})
</script>

<template>
  <div
    v-if="page"
    class="relative"
  >
    <!-- Hero 区域 -->
    <UPageHero
      :description="page.description"
      :links="page.hero.links"
      :ui="{
        container: 'md:pt-18 lg:pt-20',
        title: 'max-w-3xl mx-auto'
      }"
    >
      <template #top>
        <HeroBackground />
      </template>

      <template #title>
        <MDC
          :value="page.title"
          unwrap="p"
        />
      </template>
    </UPageHero>

    <USeparator :ui="{ border: 'border-primary/30' }" />

    <!-- 功能特性区域 -->
    <UPageSection
      id="features"
      :description="page.features.description"
      class="relative overflow-hidden"
    >
      <div class="absolute rounded-full -left-10 top-10 size-[300px] z-10 bg-primary opacity-20 blur-[200px]" />
      <div class="absolute rounded-full -right-10 -bottom-10 size-[300px] z-10 bg-primary opacity-20 blur-[200px]" />

      <template #title>
        <MDC
          :value="page.features.title"
          class="*:leading-9"
        />
      </template>

      <template #features>
        <UPageCard
          v-for="(feature, index) in page.features.items"
          :key="index"
          :title="feature.title"
          :description="feature.description"
          :icon="feature.icon"
          :ui="{
            leading: 'bg-accented/50 p-2 rounded-md border border-muted border-dashed'
          }"
        />
      </template>
    </UPageSection>

    <USeparator :ui="{ border: 'border-primary/30' }" />

    <!-- 快速开始步骤 -->
    <UPageSection
      id="steps"
      :description="page.steps.description"
      class="relative overflow-hidden"
    >
      <template #title>
        <MDC :value="page.steps.title" />
      </template>

      <template #features>
        <UPageCard
          v-for="(step, index) in page.steps.items"
          :key="index"
          class="group"
          :ui="{ container: 'p-6 sm:p-6' }"
        >
          <div class="flex flex-col gap-3">
            <div class="flex items-center gap-3">
              <div class="flex items-center justify-center size-8 rounded-full bg-primary/10 text-primary font-bold text-sm">
                {{ index + 1 }}
              </div>
              <UIcon
                v-if="step.icon"
                :name="step.icon"
                class="size-5 text-primary"
              />
            </div>
            <h3 class="text-base font-semibold">
              {{ step.title }}
            </h3>
            <p class="text-sm text-muted">
              {{ step.description }}
            </p>
          </div>
        </UPageCard>
      </template>
    </UPageSection>

    <USeparator :ui="{ border: 'border-primary/30' }" />

    <!-- CTA 区域 -->
    <UPageSection
      v-if="page.cta"
      id="cta"
      :description="page.cta.description"
      :links="page.cta.links"
    >
      <template #title>
        <MDC :value="page.cta.title" />
      </template>
    </UPageSection>
  </div>
</template>
