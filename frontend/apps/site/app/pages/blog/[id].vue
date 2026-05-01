<script setup lang="ts">
const route = useRoute()
const { fetchArticle, fetchComments, trackArticleView } = useArticles()

const id = computed(() => route.params.id as string)

const { data: articleData, error } = await useAsyncData(
  `article-${id.value}`,
  () => fetchArticle(id.value)
)

if (error.value || !articleData.value?.data) {
  throw createError({ statusCode: 404, statusMessage: '文章不存在', fatal: true })
}

const article = computed(() => articleData.value!.data)

const sanitizedContent = useSanitizedHtml(computed(() => article.value?.content ?? ''))

const { data: commentsData } = await useAsyncData(
  `article-comments-${id.value}`,
  () => fetchComments(id.value)
)

const comments = computed(() => commentsData.value?.data?.records || [])

if (import.meta.client) {
  watch(id, (currentId) => {
    void trackArticleView(currentId)
  }, { immediate: true })
}

useSeoMeta({
  title: () => article.value?.title,
  description: () => article.value?.summary || ''
})
</script>

<template>
  <UContainer class="py-10 max-w-4xl">
    <article v-if="article">
      <!-- 文章头部 -->
      <UPageHeader
        :title="article.title"
        class="mb-6"
      >
        <template #description>
          <div class="flex flex-wrap items-center gap-4 text-sm text-muted mt-2">
            <span class="flex items-center gap-1.5">
              <UAvatar
                :src="article.author?.avatarUrl || article.authorAvatar || ''"
                :alt="article.author?.name || article.authorName"
                size="xs"
              />
              {{ article.author?.name || article.authorName }}
            </span>
            <span class="flex items-center gap-1">
              <UIcon name="i-lucide-calendar" class="size-4" />
              {{ new Date(article.createdAt).toLocaleDateString('zh-CN') }}
            </span>
            <span class="flex items-center gap-1">
              <UIcon name="i-lucide-eye" class="size-4" />
              {{ article.viewCount || 0 }} 次阅读
            </span>
          </div>

          <!-- 标签 -->
          <div
            v-if="article.tags?.length"
            class="flex flex-wrap gap-1.5 mt-3"
          >
            <UBadge
              v-for="tag in article.tags"
              :key="tag"
              :label="tag"
              color="primary"
              variant="subtle"
            />
          </div>
        </template>
      </UPageHeader>

      <USeparator class="mb-8" />

      <!-- 文章内容 -->
      <!-- eslint-disable vue/no-v-html -->
      <div
        class="prose prose-neutral dark:prose-invert max-w-none"
        v-html="sanitizedContent"
      />
      <!-- eslint-enable vue/no-v-html -->

      <USeparator class="my-10" />

      <!-- 评论区 -->
      <section>
        <h2 class="text-xl font-semibold mb-6 flex items-center gap-2">
          <UIcon name="i-lucide-message-circle" class="size-5" />
          评论（{{ article.commentCount || 0 }}）
        </h2>

        <!-- 评论提示 -->
        <UAlert
          icon="i-lucide-info"
          color="primary"
          variant="subtle"
          title="登录后发表评论"
          description="请登录 Mortise 账号后参与讨论"
          class="mb-6"
        >
          <template #actions>
            <UButton
              label="去登录"
              size="sm"
              :to="`/auth/login`"
              target="_blank"
            />
          </template>
        </UAlert>

        <!-- 评论列表 -->
        <div
          v-if="comments.length > 0"
          class="flex flex-col gap-4"
        >
          <UCard
            v-for="comment in comments"
            :key="comment.id"
          >
            <div class="flex items-start gap-3">
              <UAvatar
                :src="comment.author?.avatarUrl || comment.authorAvatar || ''"
                :alt="comment.author?.name || comment.authorName"
                size="sm"
              />
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <span class="text-sm font-medium">{{ comment.author?.name || comment.authorName }}</span>
                  <span class="text-xs text-muted">
                    {{ new Date(comment.createdAt).toLocaleDateString('zh-CN') }}
                  </span>
                </div>
                <p class="text-sm text-default">
                  {{ comment.content }}
                </p>
              </div>
            </div>
          </UCard>
        </div>

        <UEmpty
          v-else
          label="暂无评论"
          description="来做第一个评论的人吧"
          icon="i-lucide-message-circle"
          class="mt-4"
        />
      </section>
    </article>
  </UContainer>
</template>
