import { fetchAdminGet, fetchAdminPut } from '@mortise/core-sdk'
import type { SiteConfigGroupVO, SiteConfigSaveRequest } from '~/types'

/**
 * 网站配置 composable
 * 封装网站配置分组的加载与保存逻辑，与 useNotificationChannels 保持一致的结构。
 */
export function useSiteConfig() {
  const { $api } = useNuxtApp()

  const groups = ref<SiteConfigGroupVO[]>([])
  const loading = ref(false)
  const saving = ref(false)
  const errorMessage = ref('')

  /** 加载所有配置分组 */
  async function loadGroups() {
    loading.value = true
    errorMessage.value = ''
    try {
      const data = await fetchAdminGet<SiteConfigGroupVO[]>(
        $api,
        '/api/v1/admin/system/site-config'
      )
      groups.value = data ?? []
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '加载网站配置失败'
    } finally {
      loading.value = false
    }
  }

  /** 保存指定分组配置 */
  async function saveGroup(
    group: string,
    request: SiteConfigSaveRequest
  ): Promise<boolean> {
    saving.value = true
    errorMessage.value = ''
    try {
      await fetchAdminPut(
        $api,
        `/api/v1/admin/system/site-config/${group}`,
        request as unknown as Record<string, unknown>
      )
      // 同步更新本地状态
      const target = groups.value.find(g => g.group === group)
      if (target) {
        Object.assign(target.values, request.values)
      }
      return true
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '保存失败，请检查配置后重试'
      return false
    } finally {
      saving.value = false
    }
  }

  return { groups, loading, saving, errorMessage, loadGroups, saveGroup }
}
