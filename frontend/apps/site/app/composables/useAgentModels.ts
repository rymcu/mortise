import type { AgentModelOption, AgentProviderInfo } from '~/types/agent'
import { DEFAULT_PROVIDER_ICON, PROVIDER_ICON_MAP } from '~/types/agent'

interface GlobalResult<T> {
  code: number
  message: string
  data: T
}

/**
 * 从后端动态获取可用 AI 模型列表
 *
 * 返回扁平化后的 USelectMenu 选项（providerCode:modelName 格式），
 * 以及模型选择 → 发送参数的拆分工具方法。
 */
export function useAgentModels() {
  const { $api } = useNuxtApp()
  const toast = useToast()

  const providers = ref<AgentProviderInfo[]>([])
  const loading = ref(false)
  const loaded = ref(false)

  /** 扁平化为 USelectMenu 选项 */
  const modelOptions = computed<AgentModelOption[]>(() => {
    return providers.value.flatMap((p) => {
      const icon = PROVIDER_ICON_MAP[p.providerCode] ?? DEFAULT_PROVIDER_ICON
      return p.models.map(m => ({
        label: `${p.providerName} - ${m.displayName}`,
        value: `${p.providerCode}:${m.modelName}`,
        icon,
      }))
    })
  })

  /** 默认选中第一个可用模型 */
  const defaultModel = computed(() => modelOptions.value[0]?.value ?? '')

  /** 拆分 "providerCode:modelName" → { modelType, modelName } */
  function parseModelValue(value: string) {
    const sep = value.indexOf(':')
    if (sep < 0) return { modelType: value, modelName: '' }
    return {
      modelType: value.slice(0, sep),
      modelName: value.slice(sep + 1),
    }
  }

  async function fetchModels() {
    if (loading.value) return
    loading.value = true
    try {
      const res = await $api<GlobalResult<AgentProviderInfo[]>>(
        '/api/v1/agent/models',
        { method: 'GET' },
      )
      if (!res || res.code !== 200) {
        throw new Error(res?.message || '获取模型列表失败')
      }
      providers.value = res.data ?? []
      loaded.value = true
    }
    catch (err) {
      toast.add({
        title: '模型加载失败',
        description: (err as Error).message || '无法获取可用模型列表',
        icon: 'i-lucide-alert-circle',
        color: 'error',
      })
    }
    finally {
      loading.value = false
    }
  }

  return {
    providers: readonly(providers),
    modelOptions,
    defaultModel,
    loading: readonly(loading),
    loaded: readonly(loaded),
    parseModelValue,
    fetchModels,
  }
}
