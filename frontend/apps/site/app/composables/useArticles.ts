import type { BlogArticle, BlogArticleListResponse, BlogArticlesQuery, BlogComment } from '~/types/blog'

export const useArticles = () => {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string

  const fetchArticles = async (query: BlogArticlesQuery = {}) => {
    const { pageNum = 1, pageSize = 10, keyword, tag } = query

    const params: Record<string, unknown> = { pageNum, pageSize }
    if (keyword) params.keyword = keyword
    if (tag) params.tag = tag

    return await $fetch<{ data: BlogArticleListResponse }>('/api/v1/community/articles', {
      baseURL,
      params
    })
  }

  const fetchArticle = async (id: string) => {
    return await $fetch<{ data: BlogArticle }>(`/api/v1/community/articles/${id}`, {
      baseURL
    })
  }

  const trackArticleView = async (id: string) => {
    return await $fetch<{ data: boolean }>(`/api/v1/community/articles/${id}/views`, {
      baseURL,
      method: 'POST'
    })
  }

  const fetchComments = async (articleId: string, pageNum = 1, pageSize = 20) => {
    return await $fetch<{ data: { records: BlogComment[], total: number } }>(
      `/api/v1/community/articles/${articleId}/comments`,
      { baseURL, params: { pageNum, pageSize } }
    )
  }

  return {
    fetchArticles,
    fetchArticle,
    trackArticleView,
    fetchComments
  }
}
