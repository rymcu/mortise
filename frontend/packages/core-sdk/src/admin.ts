import type { GlobalResult, PageResult } from './types'

export type ApiInvoker = <R>(
  request: string,
  options?: Record<string, unknown>
) => Promise<R>

/** 分页查询 */
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

/** 获取单条记录 */
export async function fetchAdminGet<T>(
  api: ApiInvoker,
  path: string
): Promise<T> {
  const response = await api<GlobalResult<T>>(path, { method: 'GET' })
  if (!response || response.code !== 200) {
    throw new Error(response?.message || '请求失败')
  }
  return response.data
}

/** 新增 */
export async function fetchAdminPost<T>(
  api: ApiInvoker,
  path: string,
  body: Record<string, unknown>
): Promise<T> {
  const response = await api<GlobalResult<T>>(path, {
    method: 'POST',
    body
  })
  if (!response || response.code !== 200) {
    throw new Error(response?.message || '操作失败')
  }
  return response.data
}

/** 更新 */
export async function fetchAdminPut<T>(
  api: ApiInvoker,
  path: string,
  body: Record<string, unknown>
): Promise<T> {
  const response = await api<GlobalResult<T>>(path, {
    method: 'PUT',
    body
  })
  if (!response || response.code !== 200) {
    throw new Error(response?.message || '操作失败')
  }
  return response.data
}

/** 删除 */
export async function fetchAdminDelete<T = void>(
  api: ApiInvoker,
  path: string
): Promise<T> {
  const response = await api<GlobalResult<T>>(path, { method: 'DELETE' })
  if (!response || response.code !== 200) {
    throw new Error(response?.message || '删除失败')
  }
  return response.data
}

/** 批量删除 */
export async function fetchAdminBatchDelete<T = void>(
  api: ApiInvoker,
  path: string,
  ids: (string | number)[]
): Promise<T> {
  const response = await api<GlobalResult<T>>(path, {
    method: 'DELETE',
    body: ids
  })
  if (!response || response.code !== 200) {
    throw new Error(response?.message || '删除失败')
  }
  return response.data
}
