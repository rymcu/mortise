import type { ProfileInfo } from '~/types'

/**
 * 用户资料 composable
 * 使用 useState 实现跨页面状态共享
 */
export function useProfile() {
  const { $api } = useNuxtApp()
  const auth = useAuthStore()

  const profile = useState<ProfileInfo | null>('admin-profile', () => null)
  const loading = useState('admin-profile-loading', () => false)
  const error = useState('admin-profile-error', () => '')

  /** 获取用户资料 */
  async function fetchProfile(): Promise<ProfileInfo | null> {
    // 如果已有数据直接返回
    if (profile.value) {
      return profile.value
    }

    loading.value = true
    error.value = ''
    try {
      const res = await $api<{ code: number; data: ProfileInfo }>(
        '/api/v1/admin/auth/profile'
      )
      profile.value = res?.data ?? null
      return profile.value
    } catch (e) {
      error.value = e instanceof Error ? e.message : '获取资料失败'
      return null
    } finally {
      loading.value = false
    }
  }

  /** 更新用户资料 */
  async function updateProfile(data: {
    nickname: string
    email: string | null
  }): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      await $api('/api/v1/admin/auth/profile', {
        method: 'PUT',
        body: data
      })
      // 同步刷新用户会话信息
      await auth.fetchCurrentUser()
      // 刷新本地资料数据
      await fetchProfile()
      return true
    } catch (e) {
      error.value = e instanceof Error ? e.message : '保存资料失败'
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    profile: readonly(profile),
    loading: readonly(loading),
    error: readonly(error),
    fetchProfile,
    updateProfile
  }
}
