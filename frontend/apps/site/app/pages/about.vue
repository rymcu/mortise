<script setup lang="ts">
const { data: page } = await useAsyncData('about', () => queryCollection('about').first())

if (!page.value) {
  throw createError({ statusCode: 404, statusMessage: 'Page not found', fatal: true })
}

useSeoMeta({
  title: '关于 Mortise',
  description: page.value.description
})

const { open } = useChatWidget()

/** 在原有渠道末尾追加「在线咨询」入口 */
const allChannels = computed(() => [
  ...(page.value?.contact?.channels ?? []),
  { icon: 'i-lucide-message-circle', label: '在线咨询', description: '与客服实时沟通', chat: true }
])

function handleChannelClick(ch: Record<string, unknown>) {
  if (ch.chat) {
    open({ subject: '商业咨询' })
  }
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
      <template #links>
        <UButton
          v-if="page.github"
          label="查看源码"
          :to="page.github"
          target="_blank"
          icon="i-simple-icons-github"
          color="neutral"
          variant="outline"
        />
        <UButton
          label="快速开始"
          to="/docs/getting-started"
          icon="i-lucide-book-open"
          color="primary"
        />
      </template>
    </UPageHero>

    <!-- 项目亮点 -->
    <UPageSection
      v-if="page.highlights?.length"
      title="为什么选择 Mortise"
      description="基础扎实、边界清晰、开箱即用。"
    >
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div
          v-for="h in page.highlights"
          :key="h.title"
          class="flex flex-col gap-3 p-5 rounded-xl border border-default bg-background hover:bg-accented/30 transition-colors"
        >
          <UIcon :name="h.icon" class="size-8 text-primary" />
          <p class="font-semibold text-highlighted">{{ h.title }}</p>
          <p class="text-sm text-muted leading-relaxed">{{ h.description }}</p>
        </div>
      </div>
    </UPageSection>

    <USeparator />

    <!-- 项目使命 + 技术栈 -->
    <UPageSection>
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 max-w-5xl mx-auto">
        <!-- 使命 -->
        <div v-if="page.mission">
          <h2 class="text-xl font-semibold mb-4 flex items-center gap-2">
            <UIcon name="i-lucide-target" class="size-5 text-primary" />
            项目使命
          </h2>
          <p class="text-muted leading-relaxed text-base">
            {{ page.mission }}
          </p>
        </div>

        <!-- 核心技术 -->
        <div>
          <h2 class="text-xl font-semibold mb-4 flex items-center gap-2">
            <UIcon name="i-lucide-layers" class="size-5 text-primary" />
            核心技术
          </h2>
          <div class="grid grid-cols-2 gap-2">
            <div
              v-for="tech in ['Spring Boot 3', 'MyBatis-Flex', 'PostgreSQL', 'Vue 3 + Nuxt 4', 'Spring Security OAuth2', 'Flyway', 'Redis', 'Docker']"
              :key="tech"
              class="flex items-center gap-2 px-3 py-2 rounded-lg bg-accented/50 border border-muted text-sm"
            >
              <UIcon name="i-lucide-check-circle" class="size-4 text-primary shrink-0" />
              {{ tech }}
            </div>
          </div>
        </div>
      </div>
    </UPageSection>

    <USeparator />

    <!-- 开源协议 -->
    <UPageSection
      v-if="page.license"
      id="license"
      title="开源协议"
      description="主仓库基础模块采用 MIT 协议，商业扩展模块单独授权。"
    >
      <div class="max-w-2xl mx-auto">
        <UCard>
          <div class="flex items-center justify-between gap-4">
            <div>
              <p class="font-semibold text-lg text-highlighted">
                {{ page.license.type }} License
              </p>
              <p class="text-sm text-muted mt-1">
                主仓库基础模块永久免费开源，欢迎贡献代码和提交 Issue。
              </p>
            </div>
            <UButton
              label="查看协议"
              :to="page.license.url"
              target="_blank"
              variant="outline"
              color="neutral"
              trailing-icon="i-lucide-external-link"
            />
          </div>
        </UCard>
      </div>
    </UPageSection>

    <USeparator />

    <!-- 联系我们 -->
    <UPageSection
      v-if="page.contact"
      id="contact"
      :title="page.contact.title"
      :description="page.contact.description"
    >
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 max-w-4xl mx-auto">
        <UCard
          v-for="ch in allChannels"
          :key="ch.label"
          :as="(ch.wechat || ch.chat) ? 'div' : 'a'"
          v-bind="(ch.wechat || ch.chat) ? {} : { href: ch.to, target: '_blank' }"
          class="flex flex-col items-center text-center gap-3 p-6 hover:bg-accented/30 transition-colors cursor-pointer"
          @click="handleChannelClick(ch as Record<string, unknown>)"
        >
          <UIcon :name="ch.icon" class="size-8 text-primary" />
          <p class="font-semibold text-highlighted">{{ ch.label }}</p>
          <p class="text-sm text-muted">{{ ch.description }}</p>
        </UCard>
      </div>
    </UPageSection>

    <USeparator />

    <!-- 团队 -->
    <UPageSection
      v-if="page.team?.length"
      title="项目团队"
    >
      <div class="flex flex-wrap justify-center gap-4">
        <UCard
          v-for="member in page.team"
          :key="member.name"
          class="w-64"
        >
          <div class="flex items-center gap-4">
            <UAvatar
              :src="member.avatar || ''"
              :alt="member.name"
              size="lg"
            />
            <div class="flex-1 min-w-0">
              <p class="font-semibold truncate">{{ member.name }}</p>
              <p class="text-sm text-muted">{{ member.role }}</p>
            </div>
            <UButton
              v-if="member.github"
              icon="i-simple-icons-github"
              :to="member.github"
              target="_blank"
              color="neutral"
              variant="ghost"
            />
          </div>
        </UCard>
      </div>
    </UPageSection>
  </div>
</template>
