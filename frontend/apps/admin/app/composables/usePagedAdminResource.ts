import { fetchAdminPage } from '@mortise/core-sdk'

interface UsePagedAdminResourceOptions {
  path: string
  errorMessage: string
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
        pageNumber: pageNum.value,
        pageSize: pageSize.value,
        keyword: keyword.value || undefined
      })

      records.value = page.records || []
      total.value = page.totalRow || 0
      totalPage.value = page.totalPage || 0

      if (
        typeof page.pageNumber === 'number' &&
        page.pageNumber > 0 &&
        page.pageNumber !== pageNum.value
      ) {
        pageNum.value = page.pageNumber
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
