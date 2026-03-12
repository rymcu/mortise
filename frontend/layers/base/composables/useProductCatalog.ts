import {
  fetchAppProductCategories,
  fetchAppProductById,
  fetchAppProductsByType,
  fetchAppProductTypes,
  type AppProductCategory,
  type AppProduct,
  type ApiInvoker,
} from '@mortise/core-sdk'

export function useProductCatalog() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string

  const productTypes = useState<Record<string, string>>('product-catalog-types', () => ({}))
  const categories = useState<AppProductCategory[]>('product-catalog-categories', () => [])
  const products = useState<AppProduct[]>('product-catalog-products', () => [])
  const loading = ref(false)
  const error = ref('')

  const productMap = computed(() => new Map(products.value.map(product => [product.id, product] as const)))
  const api: ApiInvoker = async <R>(request: string, options?: Record<string, unknown>) =>
    await $fetch<R>(request, { baseURL, ...options })

  async function loadProductTypes(force = false) {
    if (!force && Object.keys(productTypes.value).length) {
      return productTypes.value
    }

    productTypes.value = await fetchAppProductTypes(api)
    return productTypes.value
  }

  async function loadCategories(force = false) {
    if (!force && categories.value.length) {
      return categories.value
    }

    categories.value = await fetchAppProductCategories(api)
    return categories.value
  }

  async function loadAllProducts(force = false) {
    if (!force && products.value.length) {
      return products.value
    }

    loading.value = true
    error.value = ''
    try {
      const types = await loadProductTypes(force)
      const typeKeys = Object.keys(types)
      if (!typeKeys.length) {
        products.value = []
        return products.value
      }

      const productLists = await Promise.all(
        typeKeys.map(productType => fetchAppProductsByType(api, productType)),
      )

      const deduped = new Map<string, AppProduct>()
      for (const product of productLists.flat()) {
        deduped.set(product.id, product)
      }

      products.value = [...deduped.values()].sort((left, right) => {
        const leftSort = left.sortNo ?? Number.MAX_SAFE_INTEGER
        const rightSort = right.sortNo ?? Number.MAX_SAFE_INTEGER
        if (leftSort !== rightSort) {
          return leftSort - rightSort
        }
        return left.title.localeCompare(right.title, 'zh-CN')
      })
    }
    catch (err) {
      error.value = err instanceof Error ? err.message : '获取产品目录失败'
      products.value = []
    }
    finally {
      loading.value = false
    }

    return products.value
  }

  async function fetchProductById(id: string) {
    if (!id) {
      return null
    }

    const cached = productMap.value.get(id)
    if (cached) {
      return cached
    }

    try {
      return await fetchAppProductById(api, id)
    }
    catch (err) {
      error.value = err instanceof Error ? err.message : '获取产品详情失败'
      return null
    }
  }

  function productTypeLabel(code?: string | null) {
    if (!code) {
      return '未分类'
    }
    return productTypes.value[code] || code
  }

  return {
    productTypes,
    categories,
    products,
    productMap,
    loading,
    error,
    loadProductTypes,
    loadCategories,
    loadAllProducts,
    fetchProductById,
    productTypeLabel,
  }
}
