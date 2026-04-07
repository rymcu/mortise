import { fetchAdminGet } from '@mortise/core-sdk'
import {
  buildVoiceModelOptions,
  buildVoiceProviderOptions,
  normalizeVoiceModels,
  normalizeVoiceProviders,
} from '~/types/voice'
import type {
  VoiceCapability,
  VoiceModelInfo,
  VoiceProviderInfo,
} from '~/types/voice'

export function useVoiceCatalogLookups() {
  const { $api } = useNuxtApp()

  const loading = ref(false)
  const errorMessage = ref('')
  const providers = ref<VoiceProviderInfo[]>([])
  const models = ref<VoiceModelInfo[]>([])

  async function loadLookups() {
    loading.value = true
    errorMessage.value = ''

    try {
      const [providerData, modelData] = await Promise.all([
        fetchAdminGet<unknown>($api, '/api/v1/admin/voice/providers/options'),
        fetchAdminGet<unknown>($api, '/api/v1/admin/voice/models/options'),
      ])

      providers.value = normalizeVoiceProviders(providerData)
      models.value = normalizeVoiceModels(modelData)
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '加载语音目录选项失败'
    } finally {
      loading.value = false
    }
  }

  const providerOptions = computed(() => buildVoiceProviderOptions(providers.value))

  function getModelOptions(capability: VoiceCapability, providerId?: string) {
    return buildVoiceModelOptions(models.value, capability, providerId)
  }

  function getProviderName(providerId?: string): string {
    if (!providerId) {
      return '-'
    }

    const provider = providers.value.find(item => item.id === providerId)
    return provider?.name || provider?.code || providerId
  }

  function getModelName(modelId?: string): string {
    if (!modelId) {
      return '-'
    }

    const model = models.value.find(item => item.id === modelId)
    return model?.name || model?.code || modelId
  }

  return {
    loading,
    errorMessage,
    providers,
    models,
    providerOptions,
    loadLookups,
    getModelOptions,
    getProviderName,
    getModelName,
  }
}