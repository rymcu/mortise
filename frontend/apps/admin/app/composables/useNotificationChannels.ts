import { fetchAdminGet, fetchAdminPut } from '@mortise/core-sdk'
import type {ChannelConfigSaveRequest, ChannelConfigVO} from "~/types";

/**
 * 通知渠道配置 composable
 * 封装渠道配置的加载与保存逻辑
 */
export function useNotificationChannels() {
  const { $api } = useNuxtApp()

  const channels = ref<ChannelConfigVO[]>([])
  const loading = ref(false)
  const saving = ref(false)
  const errorMessage = ref('')

  /** 加载所有渠道配置 */
  async function loadChannels() {
    loading.value = true
    errorMessage.value = ''
    try {
      const data = await fetchAdminGet<ChannelConfigVO[]>(
        $api,
        '/api/v1/admin/notification/channels'
      )
      channels.value = data ?? []
    }
    catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '加载通知渠道配置失败'
    }
    finally {
      loading.value = false
    }
  }

  /** 保存指定渠道配置 */
  async function saveChannel(
    channel: string,
    request: ChannelConfigSaveRequest
  ): Promise<boolean> {
    saving.value = true
    errorMessage.value = ''
    try {
      await fetchAdminPut(
        $api,
        `/api/v1/admin/notification/channels/${channel}`,
        request as unknown as Record<string, unknown>
      )
      // 更新本地状态
      const target = channels.value.find(c => c.channel === channel)
      if (target) {
        target.enabled = request.enabled
        // 密码字段保留脱敏显示，其余字段同步更新
        Object.entries(request.values).forEach(([k, v]) => {
          const fieldDef = target.schema.find(f => f.key === k)
          target.values[k] = fieldDef?.type === 'PASSWORD' ? '***' : v
        })
      }
      return true
    }
    catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '保存失败，请检查配置后重试'
      return false
    }
    finally {
      saving.value = false
    }
  }

  return { channels, loading, saving, errorMessage, loadChannels, saveChannel }
}
