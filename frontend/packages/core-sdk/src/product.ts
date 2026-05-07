import type { ApiInvoker } from './admin'
import type { GlobalResult } from './types'

function assertSuccess<T>(
  response: GlobalResult<T> | null | undefined,
  fallbackMessage: string,
) {
  if (!response || response.code !== 200) {
    throw new Error(response?.message || fallbackMessage)
  }

  return response.data
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === 'object' && !Array.isArray(value)
}

function toOptionalString(value: unknown): string | undefined {
  if (typeof value === 'string' && value.trim()) {
    return value
  }
  if (typeof value === 'number' || typeof value === 'bigint') {
    return String(value)
  }
  return undefined
}

function toStringArray(value: unknown): string[] | undefined {
  if (!Array.isArray(value)) {
    return undefined
  }

  const result = value
    .map(item => toOptionalString(item))
    .filter((item): item is string => !!item)

  return result.length ? result : undefined
}

function toRecord(value: unknown): Record<string, unknown> | undefined {
  return isRecord(value) ? value : undefined
}

export interface AppProduct {
  id: string
  title: string
  subtitle?: string
  description?: string
  shortDescription?: string
  coverImageUrl?: string
  galleryImages?: string[]
  productType?: string
  categoryId?: string
  tags?: string[]
  features?: Record<string, unknown>
  specifications?: Record<string, unknown>
  seoTitle?: string
  seoDescription?: string
  seoKeywords?: string
  status?: number
  isFeatured?: boolean
  sortNo?: number
  viewCount?: number
  saleCount?: number
  createdTime?: string
  updatedTime?: string
  publishedTime?: string
}

export interface AppProductCategory {
  id: string
  name: string
  slug?: string
  description?: string
  parentId?: string
  imageUrl?: string
  sortNo?: number
  isActive?: boolean
  status?: number
  children?: AppProductCategory[]
}

export interface AppProductSkuTarget {
  id: string
  productSkuId?: string
  targetType?: string
  targetId?: string
  quantity?: number
  validityDays?: number
  accessLevel?: string
  conditions?: Record<string, unknown>
  metadata?: Record<string, unknown>
  status?: number
  createdTime?: string
  updatedTime?: string
}

export interface AppProductDetail extends AppProduct {
  skuTargets?: AppProductSkuTarget[]
}

export function normalizeAppProduct(value: unknown): AppProduct | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  const title = toOptionalString(value.title)
  if (!id || !title) {
    return null
  }

  return {
    id,
    title,
    subtitle: toOptionalString(value.subtitle),
    description: toOptionalString(value.description),
    shortDescription: toOptionalString(value.shortDescription),
    coverImageUrl: toOptionalString(value.coverImageUrl),
    galleryImages: toStringArray(value.galleryImages),
    productType: toOptionalString(value.productType),
    categoryId: toOptionalString(value.categoryId),
    tags: toStringArray(value.tags),
    features: toRecord(value.features),
    specifications: toRecord(value.specifications),
    seoTitle: toOptionalString(value.seoTitle),
    seoDescription: toOptionalString(value.seoDescription),
    seoKeywords: toOptionalString(value.seoKeywords),
    status: typeof value.status === 'number' ? value.status : undefined,
    isFeatured: typeof value.isFeatured === 'boolean' ? value.isFeatured : undefined,
    sortNo: typeof value.sortNo === 'number' ? value.sortNo : undefined,
    viewCount: typeof value.viewCount === 'number' ? value.viewCount : undefined,
    saleCount: typeof value.saleCount === 'number' ? value.saleCount : undefined,
    createdTime: toOptionalString(value.createdTime),
    updatedTime: toOptionalString(value.updatedTime),
    publishedTime: toOptionalString(value.publishedTime),
  }
}

export function normalizeAppProducts(value: unknown): AppProduct[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeAppProduct(item))
    .filter((item): item is AppProduct => !!item)
}

export function normalizeAppProductSkuTarget(value: unknown): AppProductSkuTarget | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  if (!id) {
    return null
  }

  return {
    id,
    productSkuId: toOptionalString(value.productSkuId),
    targetType: toOptionalString(value.targetType),
    targetId: toOptionalString(value.targetId),
    quantity: typeof value.quantity === 'number' ? value.quantity : undefined,
    validityDays: typeof value.validityDays === 'number' ? value.validityDays : undefined,
    accessLevel: toOptionalString(value.accessLevel),
    conditions: toRecord(value.conditions),
    metadata: toRecord(value.metadata),
    status: typeof value.status === 'number' ? value.status : undefined,
    createdTime: toOptionalString(value.createdTime),
    updatedTime: toOptionalString(value.updatedTime),
  }
}

export function normalizeAppProductSkuTargets(value: unknown): AppProductSkuTarget[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeAppProductSkuTarget(item))
    .filter((item): item is AppProductSkuTarget => !!item)
}

export function normalizeAppProductDetail(value: unknown): AppProductDetail | null {
  if (!isRecord(value)) {
    return null
  }

  const product = normalizeAppProduct(value)
  if (!product) {
    return null
  }

  const skuTargets = normalizeAppProductSkuTargets(value.skuTargets)

  return {
    ...product,
    skuTargets: skuTargets.length ? skuTargets : undefined,
  }
}

export function normalizeAppProductCategory(value: unknown): AppProductCategory | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  const name = toOptionalString(value.name)
  if (!id || !name) {
    return null
  }

  const children = Array.isArray(value.children)
    ? value.children
        .map(item => normalizeAppProductCategory(item))
        .filter((item): item is AppProductCategory => !!item)
    : undefined

  return {
    id,
    name,
    slug: toOptionalString(value.slug),
    description: toOptionalString(value.description),
    parentId: toOptionalString(value.parentId),
    imageUrl: toOptionalString(value.imageUrl),
    sortNo: typeof value.sortNo === 'number' ? value.sortNo : undefined,
    isActive: typeof value.isActive === 'boolean' ? value.isActive : undefined,
    status: typeof value.status === 'number' ? value.status : undefined,
    children: children?.length ? children : undefined,
  }
}

export function normalizeAppProductCategories(value: unknown): AppProductCategory[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeAppProductCategory(item))
    .filter((item): item is AppProductCategory => !!item)
}

export async function fetchAppProductTypes(api: ApiInvoker): Promise<Record<string, string>> {
  const response = await api<GlobalResult<Record<string, string>>>('/api/v1/products/types', {
    method: 'GET',
  })

  return assertSuccess(response, '获取产品类型失败') ?? {}
}

export async function fetchAppProductsByType(api: ApiInvoker, productType: string): Promise<AppProduct[]> {
  const response = await api<GlobalResult<unknown[]>>('/api/v1/products', {
    method: 'GET',
    query: { productType },
  })

  return normalizeAppProducts(assertSuccess(response, '获取产品列表失败'))
}

export async function fetchAllAppProducts(api: ApiInvoker): Promise<AppProduct[]> {
  const productTypes = await fetchAppProductTypes(api)
  const typeKeys = Object.keys(productTypes)
  if (!typeKeys.length) {
    return []
  }

  const productLists = await Promise.all(typeKeys.map(productType => fetchAppProductsByType(api, productType)))
  const deduped = new Map<string, AppProduct>()
  for (const product of productLists.flat()) {
    deduped.set(product.id, product)
  }

  return [...deduped.values()].sort((left, right) => {
    const leftSort = left.sortNo ?? Number.MAX_SAFE_INTEGER
    const rightSort = right.sortNo ?? Number.MAX_SAFE_INTEGER
    if (leftSort !== rightSort) {
      return leftSort - rightSort
    }

    return left.title.localeCompare(right.title, 'zh-CN')
  })
}

export async function fetchAppProductById(api: ApiInvoker, id: string): Promise<AppProduct | null> {
  const detail = await fetchAppProductDetail(api, id)
  if (!detail) {
    return null
  }

  return detail
}

export async function fetchAppProductDetail(api: ApiInvoker, id: string): Promise<AppProductDetail | null> {
  const response = await api<GlobalResult<unknown>>(`/api/v1/products/${id}`, {
    method: 'GET',
  })

  return normalizeAppProductDetail(assertSuccess(response, '获取产品详情失败'))
}

export async function fetchAppProductCategories(api: ApiInvoker): Promise<AppProductCategory[]> {
  const response = await api<GlobalResult<unknown[]>>('/api/v1/product-categories/tree', {
    method: 'GET',
  })

  return normalizeAppProductCategories(assertSuccess(response, '获取产品分类失败'))
}
