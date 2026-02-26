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
  const keyword = ref('')

  async function load() {
    loading.value = true
    errorMessage.value = ''

    try {
      const page = await fetchAdminPage<T>($api, options.path, {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: keyword.value || undefined
      })

      records.value = page.records || []
      total.value = page.totalRow || 0
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
    keyword,
    load
  }
}
