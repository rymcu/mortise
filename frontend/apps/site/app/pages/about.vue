<script setup lang="ts">
const { data: page } = await useAsyncData('about', () => queryCollection('about').first())

if (!page.value) {
  throw createError({ statusCode: 404, statusMessage: 'Page not found', fatal: true })
}

useSeoMeta({
  title: '关于 - Mortise',
  description: page.value.description
})
</script>

<template>
  <UContainer class="py-10 max-w-3xl">
    <UPageHeader
      :title="page?.title || '关于'"
      :description="page?.description"
      class="mb-10"
    />

    <!-- 项目使命 -->
    <section
      v-if="page?.mission"
      class="mb-10"
    >
      <h2 class="text-xl font-semibold mb-4 flex items-center gap-2">
        <UIcon name="i-lucide-target" class="size-5 text-primary" />
        项目使命
      </h2>
      <p class="text-muted leading-relaxed">
        {{ page.mission }}
      </p>
    </section>

    <USeparator class="my-8" />

    <!-- 技术栈 -->
    <section class="mb-10">
      <h2 class="text-xl font-semibold mb-4 flex items-center gap-2">
        <UIcon name="i-lucide-layers" class="size-5 text-primary" />
        核心技术
      </h2>
      <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
        <div
          v-for="tech in ['Spring Boot 3', 'MyBatis-Flex', 'PostgreSQL', 'Vue 3 + Nuxt 4', 'Spring Security OAuth2', 'Flyway', 'Redis', 'Docker']"
          :key="tech"
          class="flex items-center gap-2 p-3 rounded-lg bg-accented/50 border border-muted text-sm"
        >
          <UIcon name="i-lucide-check-circle" class="size-4 text-primary shrink-0" />
          {{ tech }}
        </div>
      </div>
    </section>

    <USeparator class="my-8" />

    <!-- 开源协议 -->
    <section
      v-if="page?.license"
      id="license"
      class="mb-10"
    >
      <h2 class="text-xl font-semibold mb-4 flex items-center gap-2">
        <UIcon name="i-lucide-scale" class="size-5 text-primary" />
        开源协议
      </h2>
      <UCard>
        <div class="flex items-center justify-between">
          <div>
            <p class="font-semibold text-lg">
              {{ page.license.type }} License
            </p>
            <p class="text-sm text-muted mt-1">
              本项目完全开源，欢迎贡献代码和提交 Issue。
            </p>
          </div>
          <UButton
            label="查看协议"
            :to="page.license.url"
            target="_blank"
            variant="outline"
            icon="i-lucide-external-link"
          />
        </div>
      </UCard>
    </section>

    <USeparator class="my-8" />

    <!-- 团队 -->
    <section
      v-if="page?.team?.length"
      class="mb-10"
    >
      <h2 class="text-xl font-semibold mb-4 flex items-center gap-2">
        <UIcon name="i-lucide-users" class="size-5 text-primary" />
        项目团队
      </h2>
      <div class="flex flex-col gap-4">
        <UCard
          v-for="member in page.team"
          :key="member.name"
        >
          <div class="flex items-center gap-4">
            <UAvatar
              :src="member.avatar || ''"
              :alt="member.name"
              size="lg"
            />
            <div class="flex-1">
              <p class="font-semibold">
                {{ member.name }}
              </p>
              <p class="text-sm text-muted">
                {{ member.role }}
              </p>
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
    </section>

    <!-- GitHub 链接 -->
    <div
      v-if="page?.github"
      class="text-center py-8"
    >
      <UButton
        label="在 GitHub 上查看源码"
        :to="page.github"
        target="_blank"
        size="lg"
        icon="i-simple-icons-github"
        color="neutral"
        variant="outline"
      />
    </div>
  </UContainer>
</template>
