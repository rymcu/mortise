export interface UsePagedAdminResourceOptions<T> {
  path: string
  errorMessage: string
  buildQuery?: () => Record<string, unknown>
  transform?: (value: unknown) => T[]
}

export interface UseAdminListResourceOptions<T> {
  path: string
  errorMessage: string
  buildQuery?: () => Record<string, unknown>
  transform?: (value: unknown) => T[]
}