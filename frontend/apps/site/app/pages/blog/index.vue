<script setup lang="ts">
import type { Article } from '~/composables/useArticles'

const { fetchArticles } = useArticles()

const page = ref(1)
const pageSize = 12
const keyword = ref('')
const tag = ref('')

const searchKeyword = ref('')

const { data, pending, refresh: _refresh } = await useAsyncData(
  'articles',
  () => fetchArticles({ pageNum: page.value, pageSize, keyword: keyword.value, tag: tag.value }),
  { watch: [page, keyword, tag] }
)

const articles = computed<Article[]>(() => data.value?.data?.records || [])
const total = computed(() => data.value?.data?.total || 0)
const totalPages = computed(() => Math.ceil(total.value / pageSize))

function search() {
  keyword.value = searchKeyword.value
  page.value = 1
}

useSeoMeta({
  title: '社区文章',
  description: 'Mortise 开发者社区，分享技术文章和开发经验'
})
</script>

<template>
  <UContainer class="py-10">
    <!-- 页面标题 -->
    <UPageHeader
      title="社区文章"
      description="发现优质技术内容，与开发者共同成长"
      class="mb-8"
    />

    <!-- 搜索和过滤 -->
    <div class="flex flex-col sm:flex-row gap-4 mb-8">
      <UInput
        v-model="searchKeyword"
        placeholder="搜索文章..."
        icon="i-lucide-search"
        class="w-full sm:max-w-xs"
        @keyup.enter="search"
      />
      <UButton
        label="搜索"
        icon="i-lucide-search"
        @click="search"
      />
      <UButton
        v-if="keyword || tag"
        label="清除筛选"
        color="neutral"
        variant="ghost"
        icon="i-lucide-x"
        @click="keyword = ''; tag = ''; searchKeyword = ''; page = 1"
      />
    </div>

    <!-- 加载状态 -->
    <div
      v-if="pending"
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
    >
      <USkeleton
        v-for="i in pageSize"
        :key="i"
        class="h-48 rounded-lg"
      />
    </div>

    <!-- 文章列表 -->
    <template v-else>
      <div
        v-if="articles.length > 0"
        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
      >
        <NuxtLink
          v-for="article in articles"
          :key="article.id"
          :to="`/blog/${article.id}`"
          class="group"
        >
          <UCard
            class="h-full transition-shadow hover:shadow-md"
            :ui="{ body: 'flex flex-col gap-3 h-full' }"
          >
            <div class="flex flex-col gap-2 flex-1">
              <h2 class="text-base font-semibold line-clamp-2 group-hover:text-primary transition-colors">
                {{ article.title }}
              </h2>
              <p
                v-if="article.summary"
                class="text-sm text-muted line-clamp-3 flex-1"
              >
                {{ article.summary }}
              </p>
            </div>

            <!-- 标签 -->
            <div
              v-if="article.tags?.length"
              class="flex flex-wrap gap-1"
            >
              <UBadge
                v-for="t in article.tags.slice(0, 3)"
                :key="t"
                :label="t"
                color="primary"
                variant="subtle"
                size="xs"
                class="cursor-pointer"
                @click.prevent="tag = t; page = 1"
              />
            </div>

            <!-- 作者和统计 -->
            <div class="flex items-center justify-between text-xs text-muted pt-2 border-t border-default">
              <span class="flex items-center gap-1">
                <UIcon name="i-lucide-user" class="size-3" />
                {{ article.authorName }}
              </span>
              <div class="flex items-center gap-3">
                <span class="flex items-center gap-1">
                  <UIcon name="i-lucide-eye" class="size-3" />
                  {{ article.viewCount || 0 }}
                </span>
                <span class="flex items-center gap-1">
                  <UIcon name="i-lucide-heart" class="size-3" />
                  {{ article.likeCount || 0 }}
                </span>
                <span class="flex items-center gap-1">
                  <UIcon name="i-lucide-message-circle" class="size-3" />
                  {{ article.commentCount || 0 }}
                </span>
              </div>
            </div>
          </UCard>
        </NuxtLink>
      </div>

      <!-- 空状态 -->
      <UEmpty
        v-else
        label="暂无文章"
        description="没有找到相关文章，请尝试其他关键词"
        icon="i-lucide-file-text"
      />

      <!-- 分页 -->
      <div
        v-if="totalPages > 1"
        class="flex justify-center mt-8"
      >
        <UPagination
          v-model:page="page"
          :total="total"
          :page-size="pageSize"
          show-edges
        />
      </div>
    </template>
  </UContainer>
</template>
