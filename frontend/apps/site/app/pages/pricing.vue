<script setup lang="ts">
const { data: page } = await useAsyncData('pricing', () => queryCollection('pricing').first())

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

const { open } = useChatWidget()

/** 咨询类按钮（to 指向 /about#contact）转换为打开聊天窗口 */
function getCardButton(card: Record<string, unknown>) {
  const btn = card.button as Record<string, unknown> | undefined
  if (!btn) return btn
  const to = btn.to as string | undefined
  if (to?.includes('/about#contact') || to?.includes('#contact')) {
    return {
      ...btn,
      to: undefined,
      onClick: () => open({ subject: card.title as string })
    }
  }
  return btn
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

    <!-- 定价卡片 -->
    <UPageSection>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <UPricingPlan
          v-for="(card, index) in page.cards"
          :key="index"
          :title="card.title"
          :description="card.description"
          :icon="card.icon"
          :price="card.price"
          :billing-cycle="card.billing_cycle"
          :highlight="card.highlight"
          :features="card.features"
          :button="getCardButton(card)"
        >
          <template
            v-if="card.name"
            #badge
          >
            <code class="text-xs font-mono text-muted bg-accented px-1.5 py-0.5 rounded">{{ card.name }}</code>
          </template>
        </UPricingPlan>
      </div>
    </UPageSection>

    <!-- 技术栈 Logos -->
    <UPageSection v-if="page.logos">
      <UPageLogos :title="page.logos.title">
        <UIcon
          v-for="icon in page.logos.icons"
          :key="icon"
          :name="icon"
          class="w-10 h-10 flex-shrink-0 text-muted"
        />
      </UPageLogos>
    </UPageSection>

    <!-- FAQ -->
    <UPageSection
      v-if="page.faq"
      :title="page.faq.title"
      :description="page.faq.description"
    >
      <UAccordion
        :items="page.faq.items"
        :unmount-on-hide="false"
        :default-value="['0']"
        type="multiple"
        class="max-w-3xl mx-auto"
        :ui="{
          trigger: 'text-base text-highlighted',
          body: 'text-base text-muted'
        }"
      />
    </UPageSection>
  </div>
</template>
