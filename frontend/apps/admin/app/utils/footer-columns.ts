import type {
  FooterColumnEditorValue,
  FooterColumnLinkEditorValue,
  ParsedFooterColumnsEditorValue
} from '~/types/site-config'

interface FooterColumnLinkPayload {
  label: string
  to?: string
  target?: string
}

interface FooterColumnPayload {
  label: string
  children?: FooterColumnLinkPayload[]
}

type FooterLinkTargetOption = {
  label: string
  value: string
}

export const FOOTER_LINK_TARGET_OPTIONS: FooterLinkTargetOption[] = [
  { label: '当前窗口 (_self)', value: '_self' },
  { label: '新窗口 (_blank)', value: '_blank' },
  { label: '父级窗口 (_parent)', value: '_parent' },
  { label: '顶层窗口 (_top)', value: '_top' }
]

function createEditorId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }

  return `footer-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

function normalizeValue(value: string | null | undefined) {
  return value?.trim() ?? ''
}

function isFooterColumnLinkPayload(value: unknown): value is FooterColumnLinkPayload {
  if (!value || typeof value !== 'object') {
    return false
  }

  const item = value as Record<string, unknown>
  return typeof item.label === 'string'
    && (item.to === undefined || typeof item.to === 'string')
    && (item.target === undefined || typeof item.target === 'string')
}

function isFooterColumnPayload(value: unknown): value is FooterColumnPayload {
  if (!value || typeof value !== 'object') {
    return false
  }

  const item = value as Record<string, unknown>
  return typeof item.label === 'string'
    && (item.children === undefined || (Array.isArray(item.children) && item.children.every(isFooterColumnLinkPayload)))
}

function toEditorLink(link: FooterColumnLinkPayload): FooterColumnLinkEditorValue {
  return {
    id: createEditorId(),
    label: normalizeValue(link.label),
    to: normalizeValue(link.to),
    target: normalizeValue(link.target)
  }
}

function toEditorColumn(column: FooterColumnPayload): FooterColumnEditorValue {
  return {
    id: createEditorId(),
    label: normalizeValue(column.label),
    children: (column.children ?? []).map(toEditorLink)
  }
}

export function createEmptyFooterColumnLink(): FooterColumnLinkEditorValue {
  return {
    id: createEditorId(),
    label: '',
    to: '',
    target: ''
  }
}

export function createEmptyFooterColumn(): FooterColumnEditorValue {
  return {
    id: createEditorId(),
    label: '',
    children: [createEmptyFooterColumnLink()]
  }
}

export function parseFooterColumnsEditorValue(value: string | null | undefined): ParsedFooterColumnsEditorValue {
  const normalized = normalizeValue(value)

  if (!normalized) {
    return {
      columns: [],
      hasInvalidInput: false
    }
  }

  try {
    const parsed = JSON.parse(normalized)

    if (!Array.isArray(parsed)) {
      return {
        columns: [],
        hasInvalidInput: true
      }
    }

    return {
      columns: parsed.filter(isFooterColumnPayload).map(toEditorColumn),
      hasInvalidInput: false
    }
  } catch {
    return {
      columns: [],
      hasInvalidInput: true
    }
  }
}

export function serializeFooterColumnsEditorValue(columns: FooterColumnEditorValue[]): string {
  const payload = columns
    .map((column): FooterColumnPayload | null => {
      const label = normalizeValue(column.label)

      if (!label) {
        return null
      }

      const children = column.children
        .map((link): FooterColumnLinkPayload | null => {
          const linkLabel = normalizeValue(link.label)

          if (!linkLabel) {
            return null
          }

          const to = normalizeValue(link.to)
          const target = normalizeValue(link.target)

          return {
            label: linkLabel,
            to: to || undefined,
            target: target || undefined
          }
        })
        .filter((item): item is FooterColumnLinkPayload => item !== null)

      return {
        label,
        children: children.length ? children : undefined
      }
    })
    .filter((item): item is FooterColumnPayload => item !== null)

  return payload.length ? JSON.stringify(payload, null, 2) : ''
}
