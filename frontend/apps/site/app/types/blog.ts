/**
 * Blog 页面类型定义
 * 用于 apps/site 的简化博客展示场景
 */

export interface BlogAuthor {
  id: string
  name: string
  avatarUrl: string | null
}

export interface BlogArticle {
  id: string
  title: string
  summary: string
  content: string
  author?: BlogAuthor
  authorName: string
  authorAvatar: string | null
  viewCount: number
  likeCount: number
  commentCount: number
  tags: string[]
  createdAt: string
  updatedAt: string
}

export interface BlogArticleListResponse {
  records: BlogArticle[]
  total: number
  current: number
  size: number
}

export interface BlogArticlesQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  tag?: string
}

export interface BlogComment {
  id: string
  content: string
  author?: BlogAuthor
  authorName: string
  authorAvatar: string | null
  createdAt: string
}
