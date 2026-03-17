import { fetchAdminPage } from '@mortise/core-sdk'
import type { UsePagedAdminResourceOptions } from '~/types/resource'

function toNumber(value: unknown, fallback = 0) {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }

  return fallback
}

export function usePagedAdminResource<T>(
  options: UsePagedAdminResourceOptions
) {
  const { $api } = useNuxtApp()

  const loading = ref(false)
  const errorMessage = ref('')
  const records = ref<T[]>([])
  const pageNum = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const totalPage = ref(0)
  const hasNext = ref(false)
  const hasPrevious = ref(false)
  const keyword = ref('')

  async function load() {
    loading.value = true
    errorMessage.value = ''

    try {
      const page = await fetchAdminPage<T>($api, options.path, {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        query: keyword.value || undefined,
        ...options.buildQuery?.()
      })

      records.value = page.records || []
      total.value = toNumber(page.totalRow)
      totalPage.value = toNumber(page.totalPage)

      const currentPageNumber = toNumber(page.pageNumber, pageNum.value)

      if (currentPageNumber > 0 && currentPageNumber !== pageNum.value) {
        pageNum.value = currentPageNumber
      }

      hasPrevious.value = Boolean(page.hasPrevious)
      hasNext.value = Boolean(page.hasNext)
    } catch (error) {
      errorMessage.value =
        error instanceof Error ? error.message : options.errorMessage
    } finally {
      loading.value = false
    }
  }

  watch([pageNum, pageSize], () => {
    load()
  })

  return {
    loading,
    errorMessage,
    records,
    pageNum,
    pageSize,
    total,
    totalPage,
    hasNext,
    hasPrevious,
    keyword,
    load
  }
}
