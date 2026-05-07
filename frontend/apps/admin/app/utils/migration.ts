export function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === 'object' && !Array.isArray(value)
}

export function toOptionalString(value: unknown): string | undefined {
  if (typeof value === 'string' && value.trim()) {
    return value
  }

  if (typeof value === 'number' || typeof value === 'bigint') {
    return String(value)
  }

  return undefined
}

export function toOptionalNumber(value: unknown): number | undefined {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }

  return undefined
}

export function toOptionalBoolean(value: unknown): boolean | undefined {
  if (typeof value === 'boolean') {
    return value
  }

  if (typeof value === 'string') {
    if (value === 'true') return true
    if (value === 'false') return false
  }

  return undefined
}

export function toStringArray(value: unknown): string[] | undefined {
  if (!Array.isArray(value)) {
    return undefined
  }

  const result = value
    .map(item => toOptionalString(item))
    .filter((item): item is string => !!item)

  return result.length ? result : undefined
}

export function toNumberArray(value: unknown): number[] | undefined {
  if (!Array.isArray(value)) {
    return undefined
  }

  const result = value
    .map(item => toOptionalNumber(item))
    .filter((item): item is number => item !== undefined)

  return result.length ? result : undefined
}

export function toDateTimeLocalValue(value?: string | null): string {
  if (!value) {
    return ''
  }

  const normalized = value.trim().replace(' ', 'T')
  return normalized.length >= 16 ? normalized.slice(0, 16) : normalized
}

export function fromDateTimeLocalValue(value: string): string | null {
  const normalized = value.trim()
  if (!normalized) {
    return null
  }

  return `${normalized.replace('T', ' ')}:00`
}

export function toPayloadOptionalString(value: string): string | null {
  return value.trim() ? value.trim() : null
}

export function toPayloadOptionalId(value: string): string | null {
  const normalized = value.trim()
  return normalized || null
}

export function formatDateTime(value?: string | null, fallback = '-'): string {
  if (!value) {
    return fallback
  }

  const date = new Date(value.replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

export function formatDate(value?: string | null, fallback = '-'): string {
  if (!value) {
    return fallback
  }

  if (value.length >= 10) {
    return value.slice(0, 10)
  }

  return value
}

export function formatMoney(
  value?: string | number | null,
  currency = 'CNY',
  fallback = '-'
): string {
  if (value === null || value === undefined || value === '') {
    return fallback
  }

  const amount = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(amount)) {
    return String(value)
  }

  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount)
}

export function jsonTextFromValue(value?: unknown): string {
  if (!value || typeof value !== 'object') {
    return ''
  }

  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return ''
  }
}

export function parseJsonObjectText(value: string): {
  success: boolean
  data: Record<string, unknown> | null
  error?: string
} {
  const normalized = value.trim()
  if (!normalized) {
    return {
      success: true,
      data: null,
    }
  }

  try {
    const parsed = JSON.parse(normalized)
    if (!isRecord(parsed)) {
      return {
        success: false,
        data: null,
        error: 'JSON 必须是对象',
      }
    }

    return {
      success: true,
      data: parsed,
    }
  } catch {
    return {
      success: false,
      data: null,
      error: 'JSON 格式不正确',
    }
  }
}

export function isOptionalJsonObjectString(value: string): boolean {
  return parseJsonObjectText(value).success
}

export function parseCommaSeparatedStrings(value: string): string[] {
  return value
    .split(',')
    .map(item => item.trim())
    .filter(Boolean)
}

export function parseCommaSeparatedNumbers(value: string): number[] {
  return parseCommaSeparatedStrings(value)
    .map(item => Number(item))
    .filter(item => Number.isFinite(item))
}

export function joinCommaSeparated(value?: Array<string | number> | null): string {
  if (!value?.length) {
    return ''
  }

  return value.join(', ')
}

export function normalizeFileExtension(filename: string): string {
  const trimmed = filename.trim()
  if (!trimmed.includes('.')) {
    return ''
  }

  return trimmed.slice(trimmed.lastIndexOf('.') + 1).toLowerCase()
}

export function normalizeMimeType(file: File): string {
  return file.type || 'application/octet-stream'
}

export function normalizeFilename(file: File): string {
  return file.name.trim() || `file-${Date.now()}`
}

