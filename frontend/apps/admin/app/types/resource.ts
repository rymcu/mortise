export interface AdminTableColumn {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
}

export interface AdminResourcePageProps {
  panelId: string
  title: string
  path: string
  columns: AdminTableColumn[]
  searchPlaceholder?: string
  emptyText?: string
  errorMessage?: string
}

export interface AdminPagedTableCardProps {
  columns: AdminTableColumn[]
  rows: Record<string, unknown>[]
  loading: boolean
  errorMessage?: string
  total: number
  pageNum: number
  pageSize: number
  totalPage?: number
  hasNext?: boolean
  hasPrevious?: boolean
  keyword: string
  showSearch?: boolean
  searchPlaceholder?: string
  emptyText?: string
  showActions?: boolean
  actionsLabel?: string
}

export interface AdminPagedTableCardEmits {
  (event: 'update:keyword', value: string): void
  (event: 'update:pageNum', value: number): void
  (event: 'refresh' | 'searchEnter'): void
}

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
