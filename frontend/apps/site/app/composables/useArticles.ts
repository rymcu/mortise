export interface Author {
  id: number
  name: string
  avatarUrl: string | null
}

export interface Article {
  id: number
  title: string
  summary: string
  content: string
  author?: Author
  authorName: string
  authorAvatar: string | null
  viewCount: number
  likeCount: number
  commentCount: number
  tags: string[]
  createdAt: string
  updatedAt: string
}

export interface ArticleListResponse {
  records: Article[]
  total: number
  current: number
  size: number
}

export interface ArticlesQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  tag?: string
}

export interface Comment {
  id: number
  content: string
  author?: Author
  authorName: string
  authorAvatar: string | null
  createdAt: string
}

export const useArticles = () => {
  const fetchArticles = async (query: ArticlesQuery = {}) => {
    const { pageNum = 1, pageSize = 10, keyword, tag } = query

    const params: Record<string, unknown> = { pageNum, pageSize }
    if (keyword) params.keyword = keyword
    if (tag) params.tag = tag

    return await $fetch<{ data: ArticleListResponse }>('/mortise/api/v1/community/articles', {
      params
    })
  }

  const fetchArticle = async (id: number | string) => {
    return await $fetch<{ data: Article }>(`/mortise/api/v1/community/articles/${id}`)
  }

  const trackArticleView = async (id: number | string) => {
    return await $fetch<{ data: boolean }>(`/mortise/api/v1/community/articles/${id}/views`, {
      method: 'POST'
    })
  }

  const fetchComments = async (articleId: number | string, pageNum = 1, pageSize = 20) => {
    return await $fetch<{ data: { records: Comment[], total: number } }>(
      `/mortise/api/v1/community/articles/${articleId}/comments`,
      { params: { pageNum, pageSize } }
    )
  }

  return {
    fetchArticles,
    fetchArticle,
    trackArticleView,
    fetchComments
  }
}
