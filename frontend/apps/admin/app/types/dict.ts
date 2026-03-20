export interface DictTypeInfo {
  id: string
  typeCode?: string
  label?: string
  description?: string
  sortNo?: number
  status?: number
  createdTime?: string
}

export interface DictInfo {
  id: string
  dictTypeCode?: string
  label?: string
  value?: string
  sortNo?: number
  status?: number
  icon?: string
  color?: string
  createdTime?: string
}
