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

/**
 * UPricingPlan 布局修正：
 * - titleWrapper: 改为 flex-col，badge 另起一行，title 独占全宽不换行
 * - priceWrapper: flex-wrap，billing-cycle 另起一行显示
 */
const planUi = {
  titleWrapper: 'flex flex-col gap-2 items-start',
  title: 'text-highlighted text-2xl sm:text-3xl font-semibold whitespace-nowrap',
  priceWrapper: 'flex flex-wrap items-baseline gap-x-2 mt-6',
  billing: 'basis-full mt-1'
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
          :discount="card.discount"
          :billing-cycle="card.billing_cycle"
          :highlight="card.highlight"
          :features="card.features"
          :button="getCardButton(card)"
          :ui="planUi"
        >
          <template
            v-if="card.name || card.badge"
            #badge
          >
            <div class="flex items-center gap-2">
              <UBadge
                v-if="card.badge"
                color="primary"
                variant="subtle"
                size="sm"
              >
                {{ card.badge }}
              </UBadge>
              <code
                v-if="card.name"
                class="text-xs font-mono text-muted bg-accented px-1.5 py-0.5 rounded"
              >
                {{ card.name }}
              </code>
            </div>
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
          class="w-10 h-10 shrink-0 text-muted"
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
