import { fetchAdminPage } from '@mortise/core-sdk'
import type { CompatMediaPickerProvider } from '~/types/media-picker'

export interface AdminFilePickerRecord {
  id: string
  url?: string
  size?: number
  filename?: string
  originalFilename?: string
  ext?: string
  contentType?: string
  platform?: string
  thUrl?: string
  createTime?: string
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === 'object' && !Array.isArray(value)
}

function toOptionalString(value: unknown): string | undefined {
  if (typeof value === 'string' && value.trim()) {
    return value.trim()
  }

  if (typeof value === 'number' && Number.isFinite(value)) {
    return String(value)
  }

  return undefined
}

function toOptionalNumber(value: unknown): number | undefined {
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

function getFileLabel(record: AdminFilePickerRecord): string {
  return record.originalFilename || record.filename || record.id
}

function formatFileSize(size?: number): string {
  if (!size || size <= 0) {
    return ''
  }

  if (size < 1024) {
    return `${size} B`
  }

  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }

  if (size < 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(1)} MB`
  }

  return `${(size / (1024 * 1024 * 1024)).toFixed(2)} GB`
}

export function normalizeAdminFilePickerRecord(value: unknown): AdminFilePickerRecord | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  if (!id) {
    return null
  }

  return {
    id,
    url: toOptionalString(value.url),
    size: toOptionalNumber(value.size),
    filename: toOptionalString(value.filename),
    originalFilename: toOptionalString(value.originalFilename),
    ext: toOptionalString(value.ext),
    contentType: toOptionalString(value.contentType),
    platform: toOptionalString(value.platform),
    thUrl: toOptionalString(value.thUrl),
    createTime: toOptionalString(value.createTime)
  }
}

export function isAdminFilePickerImage(record: Pick<AdminFilePickerRecord, 'contentType' | 'ext'>): boolean {
  if (record.contentType?.startsWith('image/')) {
    return true
  }

  return ['png', 'jpg', 'jpeg', 'gif', 'webp', 'svg', 'bmp', 'ico'].includes(
    (record.ext || '').toLowerCase()
  )
}

function getFileIcon(record: AdminFilePickerRecord): string {
  if (isAdminFilePickerImage(record)) {
    return 'i-lucide-image'
  }

  if (record.contentType?.startsWith('video/')) {
    return 'i-lucide-film'
  }

  if (record.contentType?.startsWith('audio/')) {
    return 'i-lucide-audio-lines'
  }

  if (record.contentType?.includes('json')) {
    return 'i-lucide-braces'
  }

  if (record.contentType?.includes('zip') || record.ext?.toLowerCase() === 'zip') {
    return 'i-lucide-file-archive'
  }

  return 'i-lucide-file'
}

export function useAdminFilePickerProvider(): CompatMediaPickerProvider {
  const { $api } = useNuxtApp()
  const { resolveUrl } = useMediaUrl()

  return {
    searchPlaceholder: '搜索文件名 / 原始文件名',
    emptyText: '当前筛选条件下暂无可选文件',
    async load(context) {
      const page = await fetchAdminPage<unknown>($api, '/api/v1/admin/files', {
        pageNumber: context.pageNum,
        pageSize: 12,
        keyword: context.keyword || undefined
      })

      const records = Array.isArray(page.records)
        ? page.records
            .map(record => normalizeAdminFilePickerRecord(record))
            .filter((record): record is AdminFilePickerRecord => !!record)
        : []

      return {
        records,
        total: Number(page.totalRow || 0),
        totalPage: Number(page.totalPage || 0),
        hasNext: Boolean(page.hasNext),
        hasPrevious: Boolean(page.hasPrevious)
      }
    },
    toSelection(record) {
      const file = normalizeAdminFilePickerRecord(record)
      if (!file) {
        return {
          id: '',
          label: '未知文件',
          raw: record
        }
      }

      return {
        id: file.id,
        label: getFileLabel(file),
        url: file.url,
        fileType: file.contentType,
        fileExtension: file.ext,
        raw: file
      }
    },
    getKey(record) {
      return normalizeAdminFilePickerRecord(record)?.id || ''
    },
    getLabel(record) {
      const file = normalizeAdminFilePickerRecord(record)
      return file ? getFileLabel(file) : '未知文件'
    },
    getPreviewUrl(record) {
      const file = normalizeAdminFilePickerRecord(record)
      const url = file?.thUrl || file?.url
      return resolveUrl(url) || url || ''
    },
    isPreviewImage(record) {
      const file = normalizeAdminFilePickerRecord(record)
      return file ? isAdminFilePickerImage(file) : false
    },
    getIcon(record) {
      const file = normalizeAdminFilePickerRecord(record)
      return file ? getFileIcon(file) : 'i-lucide-file'
    },
    getBadge(record) {
      const file = normalizeAdminFilePickerRecord(record)
      if (!file?.platform) {
        return null
      }

      return {
        label: file.platform,
        color: 'neutral'
      }
    },
    getMetaLines(record) {
      const file = normalizeAdminFilePickerRecord(record)
      if (!file) {
        return []
      }

      const sizeLabel = formatFileSize(file.size)
      const typeLine = [file.contentType, sizeLabel].filter(Boolean).join(' · ')

      return [typeLine, file.filename || file.id, file.createTime].filter(
        (line): line is string => Boolean(line)
      )
    }
  }
}
