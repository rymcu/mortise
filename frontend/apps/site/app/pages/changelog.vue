<script setup lang="ts">
const appConfig = useAppConfig()

useSeoMeta({
  title: '更新日志',
  description: '查看 Mortise 项目的所有版本更新记录和发布说明。',
  ogTitle: '更新日志',
  ogDescription: '查看 Mortise 项目的所有版本更新记录和发布说明。'
})

const { data: versions, status } = await useFetch(computed(() => `https://ungh.cc/repos/${appConfig.repository}/releases`), {
  transform: (data: {
    releases: {
      name?: string
      tag: string
      publishedAt: string
      markdown: string
    }[]
  }) => {
    return data.releases.map(release => ({
      tag: release.tag,
      title: release.name || release.tag,
      date: release.publishedAt,
      markdown: release.markdown
    }))
  }
})
</script>

<template>
  <UPageHero
    title="更新日志"
    description="查看 Mortise 项目的所有版本更新记录和发布说明。"
    :links="[{
      label: 'GitHub Releases',
      icon: 'i-simple-icons-github',
      variant: 'subtle',
      color: 'neutral',
      to: `https://github.com/${appConfig.repository}/releases`,
      target: '_blank'
    }]"
  />

  <USeparator />

  <UContainer>
    <!-- 加载状态 -->
    <div
      v-if="status === 'pending'"
      class="flex justify-center py-24"
    >
      <UIcon
        name="i-lucide-loader-2"
        class="size-8 animate-spin text-muted"
      />
    </div>

    <!-- 无数据状态 -->
    <div
      v-else-if="!versions?.length"
      class="text-center py-24"
    >
      <UIcon
        name="i-lucide-inbox"
        class="size-12 text-muted mx-auto mb-4"
      />
      <p class="text-lg text-muted">
        暂无发布记录
      </p>
    </div>

    <!-- 版本列表 -->
    <UChangelogVersions
      v-else
      as="main"
      :indicator-motion="false"
      :ui="{
        root: 'py-16 sm:py-24',
        indicator: 'inset-y-0'
      }"
    >
      <UChangelogVersion
        v-for="version in versions"
        :key="version.tag"
        v-bind="version"
        :ui="{
          root: 'flex items-start',
          container: 'max-w-2xl',
          header: 'border-b border-default pb-4',
          title: 'text-3xl',
          date: 'text-xs/9 text-highlighted font-mono',
          indicator: 'sticky top-0 pt-16 -mt-16 sm:pt-24 sm:-mt-24'
        }"
      >
        <template #body>
          <MDC
            v-if="version.markdown"
            :value="version.markdown"
          />
        </template>
      </UChangelogVersion>
    </UChangelogVersions>
  </UContainer>
</template>
