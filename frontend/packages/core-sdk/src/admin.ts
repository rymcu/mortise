import type { GlobalResult, PageResult } from './types'

export type ApiInvoker = <R>(request: string, options?: Record<string, unknown>) => Promise<R>

export async function fetchAdminPage<T>(
  api: ApiInvoker,
  path: string,
  query: Record<string, unknown>
): Promise<PageResult<T>> {
  const response = await api<GlobalResult<PageResult<T>>>(path, {
    method: 'GET',
    query
  })

  if (!response || response.code !== 200) {
    throw new Error(response?.message || '请求失败')
  }

  return response.data
}
