import { fetchAdminGet } from '@mortise/core-sdk'
import type { UseAdminListResourceOptions } from '~/types/resource'

export function useAdminListResource<T>(options: UseAdminListResourceOptions<T>) {
  const { $api } = useNuxtApp()

  const loading = ref(false)
  const errorMessage = ref('')
  const records = ref<T[]>([])

  async function load() {
    loading.value = true
    errorMessage.value = ''

    try {
      const query = options.buildQuery?.()
      const data = await fetchAdminGet<unknown>($api, options.path, query ? { query } : undefined)

      if (options.transform) {
        records.value = options.transform(data)
      } else if (Array.isArray(data)) {
        records.value = data as T[]
      } else {
        records.value = []
      }
    } catch (error) {
      errorMessage.value =
        error instanceof Error ? error.message : options.errorMessage
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    errorMessage,
    records,
    load,
  }
}