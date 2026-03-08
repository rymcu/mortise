import type { ApiInvoker } from './admin'
import type { GlobalResult } from './types'

export interface AdminMenuLink {
  id: string
  label: string
  icon?: string
  to?: string
  status?: number
  sortNo?: number
  parentId?: string | null
  tooltip?: string | null
  children?: AdminMenuLink[]
  defaultOpen?: boolean
}

function assertSuccess<T>(
  response: GlobalResult<T> | null | undefined,
  fallbackMessage: string,
) {
  if (!response || response.code !== 200) {
    throw new Error(response?.message || fallbackMessage)
  }

  return response.data
}

export async function fetchAdminAuthMenus(
  api: ApiInvoker,
): Promise<AdminMenuLink[]> {
  const response = await api<GlobalResult<AdminMenuLink[]>>(
    '/api/v1/admin/auth/menus',
    { method: 'GET' },
  )

  return assertSuccess(response, '获取菜单失败') ?? []
}

export async function fetchAdminCurrentUser(
  api: ApiInvoker,
  options?: Record<string, unknown>,
): Promise<Record<string, unknown> | null> {
  const response = await api<GlobalResult<{ user?: Record<string, unknown> }>>(
    '/api/v1/admin/auth/me',
    {
      method: 'GET',
      ...options,
    },
  )

  return assertSuccess(response, '获取当前用户失败')?.user ?? null
}

export async function fetchSiteCurrentUser(
  api: ApiInvoker,
  options?: Record<string, unknown>,
): Promise<Record<string, unknown> | null> {
  const response = await api<GlobalResult<Record<string, unknown>>>(
    '/api/v1/app/auth/profile',
    {
      method: 'GET',
      ...options,
    },
  )

  return assertSuccess(response, '获取当前用户失败') ?? null
}