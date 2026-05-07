export interface AdminSelectOption {
  label: string
  value: string
}

export interface ProductSkuInfo {
  id: string
  skuCode?: string
  name?: string
  description?: string
  status?: string
  isDefault?: boolean
  attributes?: Record<string, unknown>
  createdTime?: string
}

export interface ProductSkuTargetInfo {
  id: string
  productSkuId?: string
  targetType?: string
  targetId?: string
  quantity?: number
  validityDays?: number
  accessLevel?: string
  conditions?: Record<string, unknown>
  metadata?: Record<string, unknown>
  status?: number
  createdTime?: string
  updatedTime?: string
}

export interface ProductSkuTargetFormState {
  targetType: string
  targetId: string
  quantity: number
  validityDays: number | null
  accessLevel: string
  status: number
  conditions: string
  metadata: string
}

export const productTargetStatusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
] satisfies AdminSelectOption[]
