<script setup lang="ts">
const { data: docs } = await useAsyncData('docs-nav', () =>
  queryCollection('docs').order('order', 'ASC').all()
)

useSeoMeta({
  title: '文档中心 - Mortise',
  description: 'Mortise 开发文档，包含快速开始、架构介绍、API 参考等内容'
})
</script>

<template>
  <UContainer class="py-10">
    <UPageHeader
      title="文档中心"
      description="从快速开始到深入理解 Mortise 架构"
      class="mb-10"
    />

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <NuxtLink
        v-for="doc in docs"
        :key="doc.path"
        :to="doc.path"
        class="group"
      >
        <UCard class="h-full transition-shadow hover:shadow-md">
          <div class="flex flex-col gap-2">
            <h2 class="text-base font-semibold group-hover:text-primary transition-colors">
              {{ doc.title }}
            </h2>
            <p
              v-if="doc.description"
              class="text-sm text-muted"
            >
              {{ doc.description }}
            </p>
          </div>
          <template #footer>
            <span class="text-xs text-primary flex items-center gap-1">
              阅读文档
              <UIcon name="i-lucide-arrow-right" class="size-3" />
            </span>
          </template>
        </UCard>
      </NuxtLink>
    </div>
  </UContainer>
</template>
