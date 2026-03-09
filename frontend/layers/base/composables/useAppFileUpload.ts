type UploadResponse = {
  code?: number
  message?: string
  data?: {
    url?: string | null
  } | null
}

export interface UploadFileOptions {
  endpoint: string
  fieldName?: string
  fallbackMessage?: string
  accept?: string | string[]
  maxSize?: number
  fileKindLabel?: string
}

/**
 * 通用文件上传能力。
 *
 * 职责保持单一：仅负责把单个文件通过 FormData 上传，并返回后端生成的 URL。
 * 具体的 loading / error / toast 由调用方自行控制，便于在不同页面复用。
 */
export function useAppFileUpload() {
  const { $api } = useNuxtApp()

  function normalizeAccept(accept?: string | string[]): string[] {
    if (!accept) return []
    if (Array.isArray(accept)) {
      return accept.map(item => item.trim()).filter(Boolean)
    }
    return accept.split(',').map(item => item.trim()).filter(Boolean)
  }

  function matchesAccept(file: File, rule: string): boolean {
    if (!rule) return true

    if (rule.endsWith('/*')) {
      const prefix = rule.slice(0, -1)
      return file.type.startsWith(prefix)
    }

    if (rule.startsWith('.')) {
      return file.name.toLowerCase().endsWith(rule.toLowerCase())
    }

    return file.type === rule
  }

  function formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`
    if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`
    return `${(bytes / (1024 * 1024)).toFixed(bytes % (1024 * 1024) === 0 ? 0 : 1)} MB`
  }

  function validateFile(file: File, options: UploadFileOptions) {
    const fileKindLabel = options.fileKindLabel ?? '文件'
    const acceptedRules = normalizeAccept(options.accept)

    if (acceptedRules.length > 0 && !acceptedRules.some(rule => matchesAccept(file, rule))) {
      throw new Error(`${fileKindLabel}格式不支持`)
    }

    if (options.maxSize && file.size > options.maxSize) {
      throw new Error(`${fileKindLabel}大小不能超过 ${formatFileSize(options.maxSize)}`)
    }
  }

  async function uploadFile(file: File, options: UploadFileOptions): Promise<string> {
    validateFile(file, options)

    const formData = new FormData()
    formData.append(options.fieldName ?? 'file', file)

    const response = await $api<UploadResponse>(options.endpoint, {
      method: 'POST',
      body: formData,
    })

    if (response?.code !== undefined && response.code !== 200) {
      throw new Error(response.message || options.fallbackMessage || '文件上传失败')
    }

    const url = response?.data?.url
    if (!url) {
      throw new Error(response?.message || options.fallbackMessage || '文件上传失败')
    }

    return url
  }

  return {
    uploadFile,
  }
}