export interface CategoryTree {
  id: string
  name: string
  slug?: string
  description?: string
  status?: number
  isActive?: boolean
  sortNo?: number
  parentId?: string | null
  children?: CategoryTree[]
}

export interface ProductInfo {
  id: string
  title?: string
  subtitle?: string
  productType?: string
  categoryId?: string
  status?: number
  isFeatured?: boolean
  sortNo?: number
  coverImageUrl?: string
  createdTime?: string
  publishedTime?: string
}
