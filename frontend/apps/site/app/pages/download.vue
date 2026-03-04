<script setup lang="ts">
const { data: page } = await useAsyncData('download', () => queryCollection('download').first())

if (!page.value) {
  throw createError({ statusCode: 404, statusMessage: 'Page not found', fatal: true })
}

useSeoMeta({
  title: '下载 - Mortise',
  description: '下载 Mortise 相关客户端和 SDK'
})
</script>

<template>
  <UContainer class="py-10">
    <UPageHeader
      :title="page?.title || '下载'"
      :description="page?.description"
      class="mb-10"
    />

    <div
      v-if="page?.items"
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
    >
      <UCard
        v-for="item in page.items"
        :key="item.platform"
        class="flex flex-col"
      >
        <div class="flex items-center gap-4 mb-4">
          <div class="flex items-center justify-center size-12 rounded-lg bg-accented/50 border border-muted border-dashed">
            <UIcon
              :name="item.icon"
              class="size-6 text-default"
            />
          </div>
          <div>
            <h3 class="font-semibold text-base">
              {{ item.platform }}
            </h3>
            <p class="text-xs text-muted">
              {{ item.version }}
            </p>
          </div>
        </div>

        <p
          v-if="item.description"
          class="text-sm text-muted mb-4 flex-1"
        >
          {{ item.description }}
        </p>

        <UButton
          :label="`下载 ${item.platform}`"
          :to="item.url"
          :target="item.url.startsWith('http') ? '_blank' : undefined"
          :disabled="item.url === '#'"
          :color="item.url === '#' ? 'neutral' : 'primary'"
          :variant="item.url === '#' ? 'subtle' : 'solid'"
          block
        />
      </UCard>
    </div>

    <!-- Docker 快速开始 -->
    <div class="mt-12">
      <USeparator class="mb-8" />
      <h2 class="text-xl font-semibold mb-4">
        Docker 快速部署
      </h2>
      <UCard>
        <pre class="text-sm overflow-x-auto"><code>docker pull mortise/mortise:latest
docker run -d \
  -p 9999:9999 \
  -e DB_URL=jdbc:postgresql://host/mortise \
  -e DB_USERNAME=mortise \
  -e DB_PASSWORD=your_password \
  --name mortise \
  mortise/mortise:latest</code></pre>
      </UCard>
    </div>
  </UContainer>
</template>
