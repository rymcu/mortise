export interface UsePagedAdminResourceOptions {
  path: string
  errorMessage: string
  buildQuery?: () => Record<string, unknown>
}