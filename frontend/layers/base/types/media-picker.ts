export type CompatMediaPickerColor =
  | 'primary'
  | 'secondary'
  | 'success'
  | 'info'
  | 'warning'
  | 'error'
  | 'neutral'

export interface CompatMediaPickerOption {
  label: string
  value: string
}

export interface CompatMediaPickerFilterField {
  key: string
  type: 'input' | 'select'
  placeholder: string
  className?: string
  items?: CompatMediaPickerOption[]
}

export interface CompatMediaPickerBadge {
  label: string
  color?: CompatMediaPickerColor
}

export interface CompatMediaPickerSelection {
  id: string
  label: string
  url?: string
  assetType?: string
  fileType?: string
  fileExtension?: string
  raw?: unknown
}

export interface CompatMediaPickerLoadContext {
  keyword: string
  pageNum: number
  filters: Record<string, string>
}

export interface CompatMediaPickerLoadResult {
  records: unknown[]
  total: number
  totalPage: number
  hasNext: boolean
  hasPrevious: boolean
}

export interface CompatMediaPickerProvider {
  filters?: CompatMediaPickerFilterField[]
  defaultFilters?: Record<string, string>
  searchPlaceholder?: string
  emptyText?: string
  load: (context: CompatMediaPickerLoadContext) => Promise<CompatMediaPickerLoadResult>
  toSelection: (record: unknown) => CompatMediaPickerSelection
  getKey: (record: unknown) => string
  getLabel: (record: unknown) => string
  getPreviewUrl?: (record: unknown) => string
  isPreviewImage?: (record: unknown) => boolean
  getIcon?: (record: unknown) => string
  getBadge?: (record: unknown) => CompatMediaPickerBadge | null
  getMetaLines?: (record: unknown) => string[]
}
